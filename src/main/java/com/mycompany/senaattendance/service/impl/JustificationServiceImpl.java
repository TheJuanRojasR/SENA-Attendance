package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.Attendance;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.GlobalConfiguration;
import com.mycompany.senaattendance.domain.Justification;
import com.mycompany.senaattendance.domain.JustificationDetails;
import com.mycompany.senaattendance.domain.JustificationType;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.StateAttendance;
import com.mycompany.senaattendance.domain.enumeration.StateJustification;
import com.mycompany.senaattendance.repository.AttendanceRepository;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.GlobalConfigurationRepository;
import com.mycompany.senaattendance.repository.JustificationDetailsRepository;
import com.mycompany.senaattendance.repository.JustificationRepository;
import com.mycompany.senaattendance.repository.JustificationTypeRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.JustificationService;
import com.mycompany.senaattendance.service.dto.ClassSectionDTO;
import com.mycompany.senaattendance.service.dto.JustificationDTO;
import com.mycompany.senaattendance.service.dto.UserProfileDTO;
import com.mycompany.senaattendance.service.mapper.JustificationMapper;
import com.mycompany.senaattendance.service.util.BusinessDays;
import com.mycompany.senaattendance.web.rest.errors.BadRequestAlertException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.mycompany.senaattendance.domain.Justification}.
 *
 * <p>An apprentice only reads and writes their own justifications (UC011): every operation is
 * scoped to their profile and a justification of another apprentice resolves as not found on
 * reads and as {@code notYourJustification} on writes. An administrator keeps full access.
 *
 * <p>The covered failure dates are derived from the real {@code FALLA} attendance of the
 * apprentice in the materias of the parts: they feed the deadline mark and the per-type quota,
 * so no covered-date field is persisted.
 */
@Service
public class JustificationServiceImpl implements JustificationService {

    private static final Logger LOG = LoggerFactory.getLogger(JustificationServiceImpl.class);

    private static final String ENTITY_NAME = "justification";

    private final JustificationRepository justificationRepository;

    private final JustificationMapper justificationMapper;

    private final UserRepository userRepository;

    private final UserProfileRepository userProfileRepository;

    private final JustificationDetailsRepository justificationDetailsRepository;

    private final JustificationTypeRepository justificationTypeRepository;

    private final AttendanceRepository attendanceRepository;

    private final ClassSectionRepository classSectionRepository;

    private final GlobalConfigurationRepository globalConfigurationRepository;

    private final Clock clock;

    public JustificationServiceImpl(
        JustificationRepository justificationRepository,
        JustificationMapper justificationMapper,
        UserRepository userRepository,
        UserProfileRepository userProfileRepository,
        JustificationDetailsRepository justificationDetailsRepository,
        JustificationTypeRepository justificationTypeRepository,
        AttendanceRepository attendanceRepository,
        ClassSectionRepository classSectionRepository,
        GlobalConfigurationRepository globalConfigurationRepository,
        Clock clock
    ) {
        this.justificationRepository = justificationRepository;
        this.justificationMapper = justificationMapper;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.justificationDetailsRepository = justificationDetailsRepository;
        this.justificationTypeRepository = justificationTypeRepository;
        this.attendanceRepository = attendanceRepository;
        this.classSectionRepository = classSectionRepository;
        this.globalConfigurationRepository = globalConfigurationRepository;
        this.clock = clock;
    }

    @Override
    public JustificationDTO save(JustificationDTO justificationDTO) {
        LOG.debug("Request to save Justification : {}", justificationDTO);
        validateStudentIsCurrentApprentice(justificationDTO.getStudent());
        List<ClassSection> classSections = requestedClassSections(justificationDTO);
        JustificationRequest request = new JustificationRequest(
            studentIdOf(justificationDTO),
            justificationTypeIdOf(justificationDTO),
            justificationDTO.getStartDate(),
            justificationDTO.getEndDate(),
            justificationDTO.getEvidenceContentType(),
            justificationDTO.getEvidence(),
            classSections,
            null
        );

        Justification justification = justificationMapper.toEntity(justificationDTO);
        justification.setOnTime(applyCreationRules(request));
        justification.setCreatedDate(Instant.now());
        Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
        if (currentUserLogin.isPresent()) {
            justification.setCreatedBy(currentUserLogin.get());
        }

        justification = justificationRepository.save(justification);
        justification.setDetailses(persistParts(classSections, justification));
        justification = justificationRepository.save(justification);
        return justificationMapper.toDto(justification);
    }

    @Override
    public JustificationDTO update(JustificationDTO justificationDTO) {
        LOG.debug("Request to update Justification : {}", justificationDTO);
        validateOwnedJustification(justificationDTO.getId());
        validateStudentIsCurrentApprentice(justificationDTO.getStudent());
        Justification justification = justificationMapper.toEntity(justificationDTO);

        Optional<Justification> optionalJustification = justificationRepository.findById(justification.getId());
        if (optionalJustification.isPresent()) {
            Justification existingJustification = optionalJustification.get();
            justification.setCreatedBy(existingJustification.getCreatedBy());
            justification.setCreatedDate(existingJustification.getCreatedDate());
            justification.setDetailses(existingJustification.getDetailses());
            if (isPending(existingJustification.getId())) {
                JustificationRequest request = new JustificationRequest(
                    studentIdOf(justificationDTO),
                    justificationTypeIdOf(justificationDTO),
                    justificationDTO.getStartDate(),
                    justificationDTO.getEndDate(),
                    justificationDTO.getEvidenceContentType(),
                    justificationDTO.getEvidence(),
                    effectiveClassSections(justificationDTO, existingJustification),
                    existingJustification.getId()
                );
                justification.setOnTime(applyCreationRules(request));
            } else {
                justification.setOnTime(existingJustification.getOnTime());
            }
        } else {
            justification.setCreatedDate(Instant.now());
            Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
            if (currentUserLogin.isPresent()) {
                justification.setCreatedBy(currentUserLogin.get());
            }
        }

        justification = justificationRepository.save(justification);
        return justificationMapper.toDto(justification);
    }

    @Override
    public Optional<JustificationDTO> partialUpdate(JustificationDTO justificationDTO) {
        LOG.debug("Request to partially update Justification : {}", justificationDTO);

        return justificationRepository
            .findById(justificationDTO.getId())
            .map(existingJustification -> {
                validateOwnership(existingJustification);
                // The deadline mark is server-owned: a client patch never sets it directly.
                justificationDTO.setOnTime(null);
                justificationMapper.partialUpdate(existingJustification, justificationDTO);
                validateProfileIsCurrentApprentice(
                    existingJustification.getStudent() == null ? null : existingJustification.getStudent().getId()
                );
                if (isPending(existingJustification.getId())) {
                    JustificationRequest request = new JustificationRequest(
                        existingJustification.getStudent() == null ? null : existingJustification.getStudent().getId(),
                        justificationTypeIdOf(existingJustification),
                        existingJustification.getStartDate(),
                        existingJustification.getEndDate(),
                        existingJustification.getEvidenceContentType(),
                        existingJustification.getEvidence(),
                        effectiveClassSections(justificationDTO, existingJustification),
                        existingJustification.getId()
                    );
                    existingJustification.setOnTime(applyCreationRules(request));
                }

                return existingJustification;
            })
            .map(justificationRepository::save)
            .map(justificationMapper::toDto);
    }

    @Override
    public Page<JustificationDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Justifications");
        if (isCurrentUserAdmin()) {
            return justificationRepository.findAll(pageable).map(justificationMapper::toDto);
        }
        return findCurrentApprenticeJustifications(pageable);
    }

    @Override
    public Page<JustificationDTO> findAllWithEagerRelationships(Pageable pageable) {
        if (isCurrentUserAdmin()) {
            return justificationRepository.findAllWithEagerRelationships(pageable).map(justificationMapper::toDto);
        }
        return findCurrentApprenticeJustifications(pageable);
    }

    @Override
    public Optional<JustificationDTO> findOne(String id) {
        LOG.debug("Request to get Justification : {}", id);
        Optional<Justification> justification = justificationRepository.findOneWithEagerRelationships(id);
        if (isCurrentUserAdmin()) {
            return justification.map(justificationMapper::toDto);
        }
        String currentProfileId = currentUserProfileId();
        return justification.filter(found -> isOwnedBy(found, currentProfileId)).map(justificationMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete Justification : {}", id);
        justificationRepository.findById(id).ifPresent(this::validateOwnership);
        justificationRepository.deleteById(id);
    }

    /**
     * Runs the creation rules shared by create and edit and returns the deadline mark: the
     * covered failure dates are derived from the real attendance, the per-type quota is enforced
     * and the mark is computed from the last covered failure.
     *
     * @param request the values of the create or edit operation.
     * @return whether the submission is within the configured deadline.
     */
    private Boolean applyCreationRules(JustificationRequest request) {
        Set<LocalDate> failureDates = findFailureDates(request);
        validateQuota(request, failureDates);
        return computeOnTime(failureDates);
    }

    /**
     * Computes the deadline mark (UC011, use-cases.md:999): the business day after the last
     * covered failure counts as day one, and the submission day must not be after the deadline
     * built from the {@code studentJustificationDays} of the global configuration (UC019).
     *
     * @param failureDates the distinct failure dates covered by the justification.
     * @return whether the submission is within the deadline, or {@code null} without failures.
     */
    private Boolean computeOnTime(Set<LocalDate> failureDates) {
        LocalDate lastFailure = failureDates.stream().max(LocalDate::compareTo).orElse(null);
        if (lastFailure == null) {
            return null;
        }
        LocalDate deadline = BusinessDays.justificationDeadline(lastFailure, studentJustificationDays());
        return !LocalDate.now(clock).isAfter(deadline);
    }

    /**
     * Rejects a submission whose days would exceed the per-type quota (E6). The quota counts the
     * distinct failure dates covered by justifications of the same type and apprentice whose
     * parts are pending or accepted; rejected and cancelled parts release their dates, and a date
     * already covered is never counted twice.
     *
     * @param request the values of the create or edit operation.
     * @param requestedDates the distinct failure dates the submission covers.
     * @throws BadRequestAlertException with the key {@code quotaExceeded} and the remaining days.
     */
    private void validateQuota(JustificationRequest request, Set<LocalDate> requestedDates) {
        JustificationType justificationType = findJustificationType(request.justificationTypeId());
        if (justificationType == null || justificationType.getLimitPerTrimester() == null) {
            return;
        }
        int limit = justificationType.getLimitPerTrimester();
        Set<LocalDate> alreadyCovered = coveredFailureDates(
            request.studentId(),
            justificationType.getId(),
            request.excludedJustificationId()
        );
        long remaining = Math.max(0, limit - alreadyCovered.size());
        Set<LocalDate> total = new HashSet<>(alreadyCovered);
        total.addAll(requestedDates);
        if (total.size() > limit) {
            String message =
                "Ya alcanzaste el límite de días para este tipo de justificación en el trimestre. Te quedan " +
                remaining +
                " días disponibles";
            throw new BadRequestAlertException(message, message, ENTITY_NAME, "quotaExceeded");
        }
    }

    /**
     * Derives the distinct failure dates already covered by the pending or accepted parts of the
     * justifications of one type and apprentice. Each part covers the real {@code FALLA} dates of
     * the apprentice in its materia, inside the range of its justification.
     *
     * @param studentId the apprentice profile id.
     * @param justificationTypeId the justification type id.
     * @param excludedJustificationId the justification being edited, which must not count against
     *        itself, or {@code null} on create.
     * @return the distinct covered failure dates.
     */
    private Set<LocalDate> coveredFailureDates(String studentId, String justificationTypeId, String excludedJustificationId) {
        if (studentId == null || justificationTypeId == null) {
            return Set.of();
        }
        List<Justification> justifications = justificationRepository
            .findByJustificationTypeIdAndStudentId(justificationTypeId, studentId)
            .stream()
            .filter(justification -> !Objects.equals(justification.getId(), excludedJustificationId))
            .toList();
        List<ObjectId> justificationIds = justifications
            .stream()
            .map(Justification::getId)
            .filter(JustificationServiceImpl::isObjectId)
            .map(ObjectId::new)
            .toList();
        if (justificationIds.isEmpty()) {
            return Set.of();
        }
        Map<String, Justification> byId = justifications
            .stream()
            .collect(Collectors.toMap(Justification::getId, Function.identity(), (first, second) -> first));

        Set<LocalDate> dates = new HashSet<>();
        for (JustificationDetails part : justificationDetailsRepository.findAllByJustificationIdIn(justificationIds)) {
            if (!countsForQuota(part.getStateJustification())) {
                continue;
            }
            Justification justification = part.getJustification() == null ? null : byId.get(part.getJustification().getId());
            if (justification == null || part.getClassSection() == null) {
                continue;
            }
            dates.addAll(
                findFailureDates(studentId, justification.getStartDate(), justification.getEndDate(), List.of(part.getClassSection()))
            );
        }
        return dates;
    }

    /**
     * Derives the distinct {@code FALLA} dates of one apprentice in the given materias, inside a
     * date range. The materias are matched as explicit ObjectIds because the class section is a
     * DBRef and the query compares its referenced id.
     *
     * @param studentId the apprentice profile id.
     * @param startDate the first day of the range, inclusive.
     * @param endDate the last day of the range, inclusive.
     * @param classSections the materias of the justification.
     * @return the distinct failure dates, possibly empty.
     */
    private Set<LocalDate> findFailureDates(
        String studentId,
        LocalDate startDate,
        LocalDate endDate,
        Collection<ClassSection> classSections
    ) {
        List<ObjectId> classSectionIds = classSections
            .stream()
            .filter(Objects::nonNull)
            .map(ClassSection::getId)
            .filter(JustificationServiceImpl::isObjectId)
            .map(ObjectId::new)
            .distinct()
            .toList();
        if (classSectionIds.isEmpty() || studentId == null || startDate == null || endDate == null) {
            return Set.of();
        }
        return attendanceRepository
            .findByStudentIdAndClassSectionIdInAndDateBetweenAndStateAttendance(
                studentId,
                classSectionIds,
                startDate,
                endDate,
                StateAttendance.FALLA
            )
            .stream()
            .map(Attendance::getDate)
            .collect(Collectors.toSet());
    }

    /**
     * @param request the values of the create or edit operation.
     * @return the distinct failure dates covered by the request.
     */
    private Set<LocalDate> findFailureDates(JustificationRequest request) {
        return findFailureDates(request.studentId(), request.startDate(), request.endDate(), request.classSections());
    }

    /**
     * Reads the deadline window of the apprentice (UC019). A missing or null configuration falls
     * back to the same default the configuration service seeds.
     *
     * @return the configured {@code studentJustificationDays}, one or more.
     */
    private int studentJustificationDays() {
        return globalConfigurationRepository
            .findById(GlobalConfiguration.GLOBAL_CONFIGURATION_ID)
            .map(GlobalConfiguration::getStudentJustificationDays)
            .filter(Objects::nonNull)
            .orElse(GlobalConfigurationServiceImpl.DEFAULT_STUDENT_JUSTIFICATION_DAYS);
    }

    /**
     * @param justificationTypeId the justification type id, possibly {@code null}.
     * @return the persisted type, or {@code null} when it does not exist.
     */
    private JustificationType findJustificationType(String justificationTypeId) {
        return justificationTypeId == null ? null : justificationTypeRepository.findById(justificationTypeId).orElse(null);
    }

    /**
     * @param state the state of a part.
     * @return whether the part reserves quota: only pending and accepted parts count.
     */
    private static boolean countsForQuota(StateJustification state) {
        return state == StateJustification.PENDIENTE || state == StateJustification.ACEPTADA;
    }

    /**
     * Reads the parts of a justification, used to decide whether it is still pending and which
     * materias an edit keeps.
     *
     * @param justificationId the justification id.
     * @return the parts of that justification, possibly empty.
     */
    private List<JustificationDetails> partsOf(String justificationId) {
        if (!isObjectId(justificationId)) {
            return List.of();
        }
        return justificationDetailsRepository.findAllByJustificationIdIn(List.of(new ObjectId(justificationId)));
    }

    /**
     * A justification is pending while it has parts and none of them carries a decision: an
     * accepted, rejected or cancelled part closes the edits that recalculate the mark and quota.
     *
     * @param justificationId the justification id.
     * @return whether the justification is still pending.
     */
    private boolean isPending(String justificationId) {
        List<JustificationDetails> parts = partsOf(justificationId);
        return !parts.isEmpty() && parts.stream().noneMatch(part -> isDecided(part.getStateJustification()));
    }

    /**
     * @param state the state of a part.
     * @return whether the part already carries a decision.
     */
    private static boolean isDecided(StateJustification state) {
        return state == StateJustification.ACEPTADA || state == StateJustification.RECHAZADA || state == StateJustification.CANCELADA;
    }

    /**
     * Resolves the materias the payload asks to justify. Only persisted materias count, so an
     * unknown id behaves as a materia without failures.
     *
     * @param justificationDTO the payload, which may carry the parts.
     * @return the affected materias, possibly empty.
     */
    private List<ClassSection> requestedClassSections(JustificationDTO justificationDTO) {
        if (justificationDTO.getDetailses() == null) {
            return List.of();
        }
        return justificationDTO
            .getDetailses()
            .stream()
            .map(details -> details == null ? null : details.getClassSection())
            .filter(Objects::nonNull)
            .map(ClassSectionDTO::getId)
            .filter(Objects::nonNull)
            .distinct()
            .map(classSectionRepository::findById)
            .flatMap(Optional::stream)
            .toList();
    }

    /**
     * Resolves the materias of an edit: the payload wins when it carries parts, otherwise the
     * persisted parts of the justification are reused.
     *
     * @param justificationDTO the payload.
     * @param existingJustification the persisted justification being edited.
     * @return the affected materias, possibly empty.
     */
    private List<ClassSection> effectiveClassSections(JustificationDTO justificationDTO, Justification existingJustification) {
        List<ClassSection> classSections = requestedClassSections(justificationDTO);
        if (!classSections.isEmpty() || existingJustification.getId() == null) {
            return classSections;
        }
        return partsOf(existingJustification.getId())
            .stream()
            .map(JustificationDetails::getClassSection)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    /**
     * Creates one pending part per affected materia (UC011, flow step 6). A new justification has
     * no decision yet, so the decision text starts empty; the model requires a response date on
     * every part, so the creation instant is stored until the instructor decides.
     *
     * @param classSections the affected materias.
     * @param justification the persisted justification that owns the parts.
     * @return the persisted pending parts.
     */
    private Set<JustificationDetails> persistParts(List<ClassSection> classSections, Justification justification) {
        Set<JustificationDetails> parts = new HashSet<>();
        for (ClassSection classSection : classSections) {
            JustificationDetails part = new JustificationDetails()
                .stateJustification(StateJustification.PENDIENTE)
                .rejectionReason("")
                .correctionText("")
                .correctionFileUrlContentType("")
                .responseDate(Instant.now(clock))
                .classSection(classSection)
                .justification(justification);
            parts.add(justificationDetailsRepository.save(part));
        }
        return parts;
    }

    /**
     * @param justificationDTO the payload.
     * @return the profile id of the student, or {@code null} when the payload carries none.
     */
    private static String studentIdOf(JustificationDTO justificationDTO) {
        return justificationDTO.getStudent() == null ? null : justificationDTO.getStudent().getId();
    }

    /**
     * @param justificationDTO the payload.
     * @return the id of the requested type, or {@code null} when the payload carries none.
     */
    private static String justificationTypeIdOf(JustificationDTO justificationDTO) {
        return justificationDTO.getJustificationType() == null ? null : justificationDTO.getJustificationType().getId();
    }

    /**
     * @param justification the persisted justification.
     * @return the id of its type, or {@code null} when the relationship is missing.
     */
    private static String justificationTypeIdOf(Justification justification) {
        return justification.getJustificationType() == null ? null : justification.getJustificationType().getId();
    }

    /**
     * @param id the candidate id.
     * @return whether the id is a 24-hex string convertible into an {@code ObjectId}.
     */
    private static boolean isObjectId(String id) {
        return id != null && id.length() == 24 && ObjectId.isValid(id);
    }

    /**
     * Reads the justifications of the current apprentice. A user without a resolvable profile
     * reads no records.
     *
     * @param pageable the pagination information.
     * @return the page of justifications owned by the current apprentice.
     */
    private Page<JustificationDTO> findCurrentApprenticeJustifications(Pageable pageable) {
        String currentProfileId = currentUserProfileId();
        if (currentProfileId == null) {
            return Page.empty(pageable);
        }
        return justificationRepository.findByStudentId(currentProfileId, pageable).map(justificationMapper::toDto);
    }

    /**
     * Rejects a payload whose student is not the current apprentice: a non-admin only creates or
     * updates justifications for their own profile.
     *
     * @param student the student of the payload.
     * @throws BadRequestAlertException with the key {@code notYourJustification}.
     */
    private void validateStudentIsCurrentApprentice(UserProfileDTO student) {
        validateProfileIsCurrentApprentice(student == null ? null : student.getId());
    }

    /**
     * Rejects a payload whose resulting student is not the current apprentice: a non-admin only
     * creates or updates justifications for their own profile.
     *
     * @param studentId the student profile id of the payload.
     * @throws BadRequestAlertException with the key {@code notYourJustification}.
     */
    private void validateProfileIsCurrentApprentice(String studentId) {
        if (isCurrentUserAdmin()) {
            return;
        }
        String currentProfileId = currentUserProfileId();
        if (studentId == null || !studentId.equals(currentProfileId)) {
            throw notYourJustification();
        }
    }

    /**
     * Rejects an operation on a justification of another apprentice.
     *
     * @param justification the persisted justification to operate on.
     * @throws BadRequestAlertException with the key {@code notYourJustification}.
     */
    private void validateOwnership(Justification justification) {
        if (isCurrentUserAdmin()) {
            return;
        }
        if (!isOwnedBy(justification, currentUserProfileId())) {
            throw notYourJustification();
        }
    }

    /**
     * Rejects an operation on an unknown or foreign justification.
     *
     * @param justificationId the id of the justification to operate on.
     * @throws BadRequestAlertException with the key {@code notYourJustification}.
     */
    private void validateOwnedJustification(String justificationId) {
        if (isCurrentUserAdmin()) {
            return;
        }
        Justification justification = justificationId == null ? null : justificationRepository.findById(justificationId).orElse(null);
        if (justification == null) {
            throw notYourJustification();
        }
        validateOwnership(justification);
    }

    /**
     * @param justification the justification to check.
     * @param profileId the profile id of the current user, or {@code null}.
     * @return whether the justification belongs to that profile.
     */
    private static boolean isOwnedBy(Justification justification, String profileId) {
        return (profileId != null && justification.getStudent() != null && profileId.equals(justification.getStudent().getId()));
    }

    /**
     * @return whether the current user is an administrator, who reads and writes every
     *         justification.
     */
    private static boolean isCurrentUserAdmin() {
        return SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
    }

    /**
     * @return the profile id of the authenticated user, or {@code null} when the session has no
     *         login or the account has no profile.
     */
    private String currentUserProfileId() {
        return SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .map(User::getId)
            .flatMap(userProfileRepository::findOneByUserId)
            .map(UserProfile::getId)
            .orElse(null);
    }

    /**
     * @return the error thrown when a non-admin operates on a justification that is not theirs.
     */
    private static BadRequestAlertException notYourJustification() {
        return new BadRequestAlertException("Solo puedes gestionar tus propias justificaciones", ENTITY_NAME, "notYourJustification");
    }

    /**
     * The values the creation rules run on, whether they come from a create payload or from a
     * persisted justification being edited.
     *
     * @param studentId the apprentice profile id.
     * @param justificationTypeId the requested justification type id.
     * @param startDate the first day of the range.
     * @param endDate the last day of the range.
     * @param evidenceContentType the MIME type of the support.
     * @param evidence the support bytes.
     * @param classSections the affected materias.
     * @param excludedJustificationId the justification being edited, excluded from its own quota,
     *        or {@code null} on create.
     */
    private record JustificationRequest(
        String studentId,
        String justificationTypeId,
        LocalDate startDate,
        LocalDate endDate,
        String evidenceContentType,
        byte[] evidence,
        List<ClassSection> classSections,
        String excludedJustificationId
    ) {}
}
