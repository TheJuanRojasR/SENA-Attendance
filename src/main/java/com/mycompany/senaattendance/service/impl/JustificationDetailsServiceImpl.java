package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.Attendance;
import com.mycompany.senaattendance.domain.AuditLog;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.GlobalConfiguration;
import com.mycompany.senaattendance.domain.Justification;
import com.mycompany.senaattendance.domain.JustificationDetails;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.StateAttendance;
import com.mycompany.senaattendance.domain.enumeration.StateJustification;
import com.mycompany.senaattendance.repository.AttendanceRepository;
import com.mycompany.senaattendance.repository.AuditLogRepository;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.GlobalConfigurationRepository;
import com.mycompany.senaattendance.repository.JustificationDetailsRepository;
import com.mycompany.senaattendance.repository.JustificationDetailsSearchCriteria;
import com.mycompany.senaattendance.repository.JustificationRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.AlertaService;
import com.mycompany.senaattendance.service.JustificationDetailsService;
import com.mycompany.senaattendance.service.JustificationNotificationPort;
import com.mycompany.senaattendance.service.dto.JustificationDTO;
import com.mycompany.senaattendance.service.dto.JustificationDetailsDTO;
import com.mycompany.senaattendance.service.mapper.JustificationDetailsMapper;
import com.mycompany.senaattendance.service.util.BusinessDays;
import com.mycompany.senaattendance.web.rest.errors.BadRequestAlertException;
import com.mycompany.senaattendance.web.rest.vm.JustificationDecisionVM;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.mycompany.senaattendance.domain.JustificationDetails}.
 *
 * <p>An apprentice only reads and writes the parts of their own justifications (UC011): every
 * operation is scoped to their profile, a part of another apprentice resolves as not found on
 * reads and as {@code notYourJustification} on writes. An administrator keeps full access, and
 * the instructor reads and decides the parts of their own materias through UC010.
 *
 * <p>The update and partial update are the A5 correction: they only ever copy the apprentice
 * correction text and file, so a client can never decide a part through those endpoints. The
 * remaining fields, including the decision, are server-owned.
 */
@Service
public class JustificationDetailsServiceImpl implements JustificationDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(JustificationDetailsServiceImpl.class);

    private static final String ENTITY_NAME = "justificationDetails";

    /**
     * Business days the apprentice has to correct a rejected part, counted from the day after
     * the rejection (UC011, A5/E5).
     */
    private static final int CORRECTION_BUSINESS_DAYS = 2;

    private final JustificationDetailsRepository justificationDetailsRepository;

    private final JustificationDetailsMapper justificationDetailsMapper;

    private final JustificationRepository justificationRepository;

    private final ClassSectionRepository classSectionRepository;

    private final AttendanceRepository attendanceRepository;

    private final AuditLogRepository auditLogRepository;

    private final CurrentUserContext currentUserContext;

    private final GlobalConfigurationRepository globalConfigurationRepository;

    private final JustificationNotificationPort justificationNotificationPort;

    private final AlertaService alertaService;

    private final Clock clock;

    public JustificationDetailsServiceImpl(
        JustificationDetailsRepository justificationDetailsRepository,
        JustificationDetailsMapper justificationDetailsMapper,
        JustificationRepository justificationRepository,
        ClassSectionRepository classSectionRepository,
        AttendanceRepository attendanceRepository,
        AuditLogRepository auditLogRepository,
        CurrentUserContext currentUserContext,
        GlobalConfigurationRepository globalConfigurationRepository,
        JustificationNotificationPort justificationNotificationPort,
        AlertaService alertaService,
        Clock clock
    ) {
        this.justificationDetailsRepository = justificationDetailsRepository;
        this.justificationDetailsMapper = justificationDetailsMapper;
        this.justificationRepository = justificationRepository;
        this.classSectionRepository = classSectionRepository;
        this.attendanceRepository = attendanceRepository;
        this.auditLogRepository = auditLogRepository;
        this.currentUserContext = currentUserContext;
        this.globalConfigurationRepository = globalConfigurationRepository;
        this.justificationNotificationPort = justificationNotificationPort;
        this.alertaService = alertaService;
        this.clock = clock;
    }

    @Override
    public JustificationDetailsDTO save(JustificationDetailsDTO justificationDetailsDTO) {
        LOG.debug("Request to save JustificationDetails : {}", justificationDetailsDTO);
        validateJustificationOwnership(idOf(justificationDetailsDTO.getJustification()));
        JustificationDetails justificationDetails = justificationDetailsMapper.toEntity(justificationDetailsDTO);
        justificationDetails = justificationDetailsRepository.save(justificationDetails);
        return justificationDetailsMapper.toDto(justificationDetails);
    }

    /**
     * Updates a part with the same correction contract as the partial update (UC011, A5): only
     * the apprentice correction text and file are copied from the payload, while the state, the
     * rejection reason, the response date and the relationships keep their persisted values.
     * A payload that carries those server-owned fields still has to satisfy the required fields
     * of the DTO, but their values are ignored, so a client can never decide a part through this
     * endpoint.
     *
     * @param justificationDetailsDTO the correction payload.
     * @return the persisted part.
     * @throws BadRequestAlertException with the key {@code notYourJustification} for a non-admin
     *         whose part does not exist or belongs to another apprentice, or the correction keys
     *         of {@link #applyCorrection(JustificationDetails, JustificationDetailsDTO)}.
     */
    @Override
    public JustificationDetailsDTO update(JustificationDetailsDTO justificationDetailsDTO) {
        LOG.debug("Request to update JustificationDetails : {}", justificationDetailsDTO);
        return partialUpdate(justificationDetailsDTO).orElseThrow(JustificationDetailsServiceImpl::idNotFound);
    }

    @Override
    public Optional<JustificationDetailsDTO> partialUpdate(JustificationDetailsDTO justificationDetailsDTO) {
        LOG.debug("Request to partially update JustificationDetails : {}", justificationDetailsDTO);

        return justificationDetailsRepository
            .findById(justificationDetailsDTO.getId())
            .map(existingJustificationDetails -> {
                validateOwnership(existingJustificationDetails);
                applyCorrection(existingJustificationDetails, justificationDetailsDTO);
                return existingJustificationDetails;
            })
            .map(justificationDetailsRepository::save)
            .map(justificationDetailsMapper::toDto);
    }

    /**
     * Applies a correction (UC011, A5) over a part. Only the apprentice correction text and file
     * are ever copied from the payload: the state, the response date, the rejection reason and
     * the relationships stay server-owned, so a client patch can never decide a part.
     *
     * <p>A rejected part can only be corrected inside the two business days after its rejection;
     * outside that window it stays rejected for good (E5). A pending part has no rejection to
     * reopen, so it keeps its state and only sees its correction fields updated. An accepted or
     * cancelled part is already processed and is rejected with {@code alreadyProcessed} (E3).
     *
     * @param part the persisted part to correct.
     * @param payload the correction payload.
     * @throws BadRequestAlertException with the key {@code alreadyProcessed} or
     *         {@code correctionExpired}.
     */
    private void applyCorrection(JustificationDetails part, JustificationDetailsDTO payload) {
        StateJustification state = part.getStateJustification();
        if (state != StateJustification.PENDIENTE && state != StateJustification.RECHAZADA) {
            throw alreadyProcessed();
        }
        if (state == StateJustification.RECHAZADA && !isWithinCorrectionWindow(part.getResponseDate())) {
            throw new BadRequestAlertException("El plazo para subsanar esta justificación ha vencido", ENTITY_NAME, "correctionExpired");
        }
        if (payload.getCorrectionText() != null) {
            part.setCorrectionText(payload.getCorrectionText());
        }
        if (payload.getCorrectionFileUrl() != null) {
            part.setCorrectionFileUrl(payload.getCorrectionFileUrl());
        }
        if (payload.getCorrectionFileUrlContentType() != null) {
            part.setCorrectionFileUrlContentType(payload.getCorrectionFileUrlContentType());
        }
        if (state == StateJustification.RECHAZADA) {
            // The part reopens as pending, so it has no response left; the late mark goes with the
            // response and the rejection reason is kept as the trace of why it was rejected until
            // the next decision (UC010) replaces it.
            part.setStateJustification(StateJustification.PENDIENTE);
            part.setResponseDate(null);
            part.setLateDecision(null);
        }
    }

    /**
     * The correction window of a rejected part (UC011, A5/E5): two business days counted from
     * the day after the rejection, so a rejection on a Friday can be corrected until the
     * following Tuesday. A part without a response date cannot prove a valid window and counts
     * as expired.
     *
     * @param responseDate the instant the part was rejected, or {@code null}.
     * @return whether today is still within the correction window.
     */
    private boolean isWithinCorrectionWindow(Instant responseDate) {
        if (responseDate == null) {
            return false;
        }
        LocalDate rejectionDay = LocalDate.ofInstant(responseDate, clock.getZone());
        LocalDate deadline = BusinessDays.plus(rejectionDay, CORRECTION_BUSINESS_DAYS);
        return !LocalDate.now(clock).isAfter(deadline);
    }

    @Override
    public Page<JustificationDetailsDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all JustificationDetailses");
        if (isCurrentUserAdmin()) {
            return justificationDetailsRepository.findAll(pageable).map(justificationDetailsMapper::toDto);
        }
        return findCurrentApprenticeDetails(pageable);
    }

    @Override
    public Page<JustificationDetailsDTO> findAllWithEagerRelationships(Pageable pageable) {
        if (isCurrentUserAdmin()) {
            return justificationDetailsRepository.findAllWithEagerRelationships(pageable).map(justificationDetailsMapper::toDto);
        }
        return findCurrentApprenticeDetails(pageable);
    }

    /**
     * Reads one part. An administrator reads any part; an apprentice reads the parts of their own
     * justifications and an instructor the parts of their own materias (UC010, flow step 4). A part
     * outside the readable scope resolves as empty, so the response is {@code 404} and the read
     * never leaks the existence of a part of another apprentice or another instructor. The detail
     * carries the evidence and the type of its header, which is the support the instructor reviews
     * before deciding.
     *
     * @param id the id of the part.
     * @return the readable part, or empty when it does not exist or is out of scope.
     */
    @Override
    public Optional<JustificationDetailsDTO> findOne(String id) {
        LOG.debug("Request to get JustificationDetails : {}", id);
        Optional<JustificationDetails> justificationDetails = justificationDetailsRepository.findOneWithEagerRelationships(id);
        if (isCurrentUserAdmin()) {
            return justificationDetails.map(justificationDetailsMapper::toDtoWithEvidence);
        }
        String currentProfileId = currentUserProfileId();
        return justificationDetails
            .filter(details -> isOwnedBy(details, currentProfileId) || isAssignedTo(details, currentProfileId))
            .map(justificationDetailsMapper::toDtoWithEvidence);
    }

    /**
     * Reads the parts the current instructor must decide and their history (UC010, A1). The
     * pending state is the default so the tray opens on the work left to do; passing a state
     * turns the same query into the decision history. An instructor without assigned materias,
     * or a request date range without any justification, reads no records.
     *
     * @param stateJustification the state to include; {@code null} defaults to pending parts.
     * @param classSectionId the materia to filter by (may be null for every materia).
     * @param createdFrom the first request date to include (may be null for no lower bound).
     * @param createdTo the last request date to include (may be null for no upper bound).
     * @param pageable the pagination information.
     * @return the page of readable parts matching the filters.
     */
    @Override
    public Page<JustificationDetailsDTO> findPendingForCurrentUser(
        StateJustification stateJustification,
        String classSectionId,
        LocalDate createdFrom,
        LocalDate createdTo,
        Pageable pageable
    ) {
        LOG.debug("Request to get the page of JustificationDetails the current user can decide");
        List<ObjectId> classSectionScope = null;
        if (!isCurrentUserAdmin()) {
            classSectionScope = currentInstructorClassSectionIds();
            if (classSectionScope.isEmpty()) {
                return Page.empty(pageable);
            }
        }
        List<ObjectId> justificationIds = justificationIdsRequestedBetween(createdFrom, createdTo);
        if (justificationIds != null && justificationIds.isEmpty()) {
            return Page.empty(pageable);
        }
        JustificationDetailsSearchCriteria criteria = new JustificationDetailsSearchCriteria(
            stateJustification == null ? StateJustification.PENDIENTE : stateJustification,
            classSectionId,
            justificationIds
        );
        return justificationDetailsRepository.searchParts(criteria, classSectionScope, pageable).map(justificationDetailsMapper::toDto);
    }

    /**
     * Decides one part (UC010, flow step 5). The order of the checks makes the most specific
     * situation win: the decision must be one of the two decision states, the current instructor
     * must be the one assigned to the materia of the part at this moment (an administrator
     * decides any part), and the part must still be pending. A rejection always carries a reason
     * (E1) and an approval of a part marked out of time always carries the exception reason (A2).
     *
     * <p>Approving converts every {@code FALLA} record of the apprentice in that materia inside
     * the justified period to {@code JUSTIFICADA}, links it to the justification and audits the
     * change. The deadline mark is never recalculated and a closed trimester does not block a
     * pending decision, so the conversion applies even when the apprentice is no longer enrolled.
     * A decision that arrives after the instructor response deadline is marked as late. Every
     * decision notifies the resulting state once through the UC018 port, and an approval also
     * re-evaluates the active absence alerts so the ones that dropped below their threshold are
     * resolved automatically (UC013, A4) through the alert channel.
     *
     * @param id the id of the part to decide.
     * @param decision the state and the reasons of the decision.
     * @return the persisted part, or empty when it does not exist.
     * @throws BadRequestAlertException with the key of the first rule the decision violates.
     */
    @Override
    public Optional<JustificationDetailsDTO> decide(String id, JustificationDecisionVM decision) {
        LOG.debug("Request to decide JustificationDetails : {}, {}", id, decision);
        return justificationDetailsRepository.findById(id).map(part -> {
            validateDecisionState(decision.getStateJustification());
            validateDecisionInstructor(part);
            if (part.getStateJustification() != StateJustification.PENDIENTE) {
                throw noLongerPending();
            }
            validateDecisionReasons(part, decision);
            applyDecision(part, decision);
            JustificationDetails decided = justificationDetailsRepository.save(part);
            notifyDecision(decided);
            resolveAlertsBelowThreshold(decided);
            return justificationDetailsMapper.toDto(decided);
        });
    }

    /**
     * Rejects a decision that is neither an approval nor a rejection: the pending state is not a
     * decision and cannot be applied through this endpoint.
     *
     * @param stateJustification the requested state.
     * @throws BadRequestAlertException with the key {@code invalidDecisionState}.
     */
    private static void validateDecisionState(StateJustification stateJustification) {
        if (stateJustification != StateJustification.ACEPTADA && stateJustification != StateJustification.RECHAZADA) {
            throw new BadRequestAlertException("La decisión solo puede ser Aceptada o Rechazada", ENTITY_NAME, "invalidDecisionState");
        }
    }

    /**
     * Rejects a decision made by anyone but the instructor currently assigned to the materia of
     * the part (E4). An administrator keeps full access. A materia without instructor, or an
     * instructor without a resolvable profile, cannot be the assigned one.
     *
     * @param part the part being decided.
     * @throws BadRequestAlertException with the key {@code notYourClassSection}.
     */
    private void validateDecisionInstructor(JustificationDetails part) {
        if (isCurrentUserAdmin()) {
            return;
        }
        if (!isAssignedTo(part, currentUserProfileId())) {
            throw notYourClassSection();
        }
    }

    /**
     * Rejects a rejection without a reason (E1) and an approval of a part marked out of time
     * without the additional exception reason (A2). A part whose justification cannot prove its
     * deadline mark counts as in time: only an explicit {@code false} demands the reason.
     *
     * @param part the part being decided.
     * @param decision the requested decision.
     * @throws BadRequestAlertException with the key {@code rejectionReasonRequired} or
     *         {@code outOfTimeReasonRequired}.
     */
    private static void validateDecisionReasons(JustificationDetails part, JustificationDecisionVM decision) {
        if (decision.getStateJustification() == StateJustification.RECHAZADA && isBlank(decision.getRejectionReason())) {
            throw new BadRequestAlertException("El motivo de rechazo es obligatorio", ENTITY_NAME, "rejectionReasonRequired");
        }
        if (
            decision.getStateJustification() == StateJustification.ACEPTADA &&
            Boolean.FALSE.equals(onTimeOf(part)) &&
            isBlank(decision.getOutOfTimeReason())
        ) {
            throw new BadRequestAlertException(
                "Debes registrar el motivo de la aprobación fuera de tiempo",
                ENTITY_NAME,
                "outOfTimeReasonRequired"
            );
        }
    }

    /**
     * Applies the decision over the part and, on approval, over the covered attendance. The
     * response date is the decision instant, the late-decision mark is derived from the response
     * deadline of the instructor and the deadline mark of the justification is preserved: it is
     * never recalculated here.
     *
     * @param part the part being decided.
     * @param decision the requested decision.
     */
    private void applyDecision(JustificationDetails part, JustificationDecisionVM decision) {
        Instant decisionDate = Instant.now(clock);
        part.setResponseDate(decisionDate);
        part.setLateDecision(isLateDecision(part, decisionDate));
        if (decision.getStateJustification() == StateJustification.RECHAZADA) {
            part.setStateJustification(StateJustification.RECHAZADA);
            part.setRejectionReason(decision.getRejectionReason());
            part.setOutOfTimeReason(null);
            return;
        }
        part.setStateJustification(StateJustification.ACEPTADA);
        part.setRejectionReason("");
        part.setOutOfTimeReason(decision.getOutOfTimeReason());
        justifyFailures(part, decisionDate);
    }

    /**
     * Notifies the state change of one decision through the UC018 port, exactly once per decided
     * part. The port receives the header and the resulting state; a part without a resolvable
     * header has no apprentice to notify and is skipped.
     *
     * @param part the decided part.
     */
    private void notifyDecision(JustificationDetails part) {
        Justification justification = part.getJustification();
        if (justification == null) {
            return;
        }
        justificationNotificationPort.stateChanged(justification, part.getStateJustification());
    }

    /**
     * Re-evaluates the alerts affected by an approval, so the ones that stayed below their
     * threshold are resolved automatically (UC013, A4). Only an accepted part triggers it,
     * because a rejection converts no failure and cannot lower a count.
     *
     * <p>The decision is the primary operation and the resolution is derived from it, so a
     * failure of the evaluation is logged instead of turning a persisted decision into an error
     * response. The alert change travels through the alert channel, never through the
     * justification port.
     *
     * @param part the decided part.
     */
    private void resolveAlertsBelowThreshold(JustificationDetails part) {
        if (part.getStateJustification() != StateJustification.ACEPTADA) {
            return;
        }
        Justification justification = part.getJustification();
        ClassSection classSection = part.getClassSection();
        if (
            justification == null ||
            justification.getStudent() == null ||
            justification.getStudent().getId() == null ||
            classSection == null ||
            classSection.getId() == null
        ) {
            return;
        }
        try {
            alertaService.resolveBelowThreshold(justification.getStudent().getId(), classSection.getId(), LocalDate.now(clock));
        } catch (RuntimeException e) {
            LOG.warn("Could not resolve the absence alerts after approving the part {}", part.getId(), e);
        }
    }

    /**
     * Converts the {@code FALLA} records of the apprentice in the materia of the part, inside the
     * justified period, to {@code JUSTIFICADA}. The records are linked to the justification of the
     * part and every real change is audited with the profile that decided. A part without a
     * resolvable justification, apprentice or materia has no attendance to convert.
     *
     * @param part the approved part.
     * @param decisionDate the instant of the decision.
     */
    private void justifyFailures(JustificationDetails part, Instant decisionDate) {
        Justification justification = part.getJustification();
        ClassSection classSection = part.getClassSection();
        if (
            justification == null ||
            classSection == null ||
            justification.getStudent() == null ||
            justification.getStartDate() == null ||
            justification.getEndDate() == null ||
            !isObjectId(classSection.getId())
        ) {
            return;
        }
        List<Attendance> failures = attendanceRepository.findByStudentIdAndClassSectionIdInAndDateBetweenAndStateAttendance(
            justification.getStudent().getId(),
            List.of(new ObjectId(classSection.getId())),
            justification.getStartDate(),
            justification.getEndDate(),
            StateAttendance.FALLA
        );
        UserProfile modifiedBy = currentUserProfile();
        for (Attendance attendance : failures) {
            attendance.setStateAttendance(StateAttendance.JUSTIFICADA);
            attendance.setModifiedByJustification(justification);
            Attendance savedAttendance = attendanceRepository.save(attendance);
            recordStateChange(savedAttendance, StateAttendance.FALLA, StateAttendance.JUSTIFICADA, modifiedBy, decisionDate);
        }
    }

    /**
     * Writes the audit entry of a real attendance change, following the UC009 pattern: the state
     * before, the state after, the instant of the change and the profile that made it.
     *
     * @param attendance the persisted record whose state changed.
     * @param previousState the state before the change.
     * @param newState the state after the change.
     * @param modifiedBy the profile that decided.
     * @param editDate the instant of the change.
     */
    private void recordStateChange(
        Attendance attendance,
        StateAttendance previousState,
        StateAttendance newState,
        UserProfile modifiedBy,
        Instant editDate
    ) {
        AuditLog auditLog = new AuditLog()
            .previousState(previousState)
            .newState(newState)
            .editDate(editDate)
            .modifiedBy(modifiedBy)
            .attendance(attendance);
        auditLogRepository.save(auditLog);
    }

    /**
     * @param part the part being decided.
     * @return the deadline mark of the justification of the part, or {@code null} when the
     *         justification cannot be resolved.
     */
    private static Boolean onTimeOf(JustificationDetails part) {
        return part.getJustification() == null ? null : part.getJustification().getOnTime();
    }

    /**
     * @param value the candidate text.
     * @return whether the text is missing or blank.
     */
    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Decides whether the decision arrived after the instructor response deadline (UC010): the
     * deadline is the request date of the part header plus the configured business days, so a
     * decision on the deadline day itself is still in time. A part without a provable request
     * date is never late, the same way a justification without a deadline mark counts as in time.
     *
     * @param part the part being decided.
     * @param decisionDate the instant of the decision.
     * @return whether the decision is late.
     */
    private boolean isLateDecision(JustificationDetails part, Instant decisionDate) {
        Justification justification = part.getJustification();
        Instant requestDate = justification == null ? null : justification.getCreatedDate();
        if (requestDate == null) {
            return false;
        }
        ZoneId zone = clock.getZone();
        LocalDate deadline = BusinessDays.plus(LocalDate.ofInstant(requestDate, zone), instructorResponseDays());
        return LocalDate.ofInstant(decisionDate, zone).isAfter(deadline);
    }

    /**
     * Reads the instructor response window (UC019). A missing or null configuration falls back to
     * the same default the configuration service seeds.
     *
     * @return the configured {@code instructorResponseDays}, one or more.
     */
    private int instructorResponseDays() {
        return globalConfigurationRepository
            .findById(GlobalConfiguration.GLOBAL_CONFIGURATION_ID)
            .map(GlobalConfiguration::getInstructorResponseDays)
            .filter(Objects::nonNull)
            .orElse(GlobalConfigurationServiceImpl.DEFAULT_INSTRUCTOR_RESPONSE_DAYS);
    }

    /**
     * Resolves the materias assigned to the current instructor, which are the readable scope of
     * the tray. A user without a resolvable profile or without assigned materias reads no
     * records.
     *
     * @return the ObjectId values of the assigned materias, possibly empty.
     */
    private List<ObjectId> currentInstructorClassSectionIds() {
        String currentProfileId = currentUserProfileId();
        if (currentProfileId == null) {
            return List.of();
        }
        return classSectionRepository
            .findByInstructorId(currentProfileId)
            .stream()
            .map(ClassSection::getId)
            .filter(JustificationDetailsServiceImpl::isObjectId)
            .map(ObjectId::new)
            .toList();
    }

    /**
     * Resolves the justifications whose request date falls inside the requested range, which are
     * the ids the tray query intersects with. The header carries the request date because a part
     * has no auditing of its own; the range is half-open on the upper bound so a whole day is
     * included.
     *
     * @param from the first request date to include, or {@code null}.
     * @param to the last request date to include, or {@code null}.
     * @return the ObjectId values of the matching justifications, or {@code null} when the
     *         request carries no date range.
     */
    private List<ObjectId> justificationIdsRequestedBetween(LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return null;
        }
        ZoneId zone = clock.getZone();
        List<Justification> justifications;
        if (from == null) {
            justifications = justificationRepository.findByCreatedDateBefore(to.plusDays(1).atStartOfDay(zone).toInstant());
        } else if (to == null) {
            justifications = justificationRepository.findByCreatedDateGreaterThanEqual(from.atStartOfDay(zone).toInstant());
        } else {
            justifications = justificationRepository.findByCreatedDateGreaterThanEqualAndCreatedDateBefore(
                from.atStartOfDay(zone).toInstant(),
                to.plusDays(1).atStartOfDay(zone).toInstant()
            );
        }
        return justifications
            .stream()
            .map(Justification::getId)
            .filter(JustificationDetailsServiceImpl::isObjectId)
            .map(ObjectId::new)
            .toList();
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete JustificationDetails : {}", id);
        justificationDetailsRepository.findById(id).ifPresent(this::validateOwnership);
        justificationDetailsRepository.deleteById(id);
    }

    /**
     * Reads the parts of the justifications of the current apprentice. A user without a resolvable
     * profile, or without justifications, reads no records.
     *
     * @param pageable the pagination information.
     * @return the page of parts owned by the current apprentice.
     */
    private Page<JustificationDetailsDTO> findCurrentApprenticeDetails(Pageable pageable) {
        String currentProfileId = currentUserProfileId();
        if (currentProfileId == null) {
            return Page.empty(pageable);
        }
        List<ObjectId> justificationIds = justificationRepository
            .findAllByStudentId(currentProfileId)
            .stream()
            .map(Justification::getId)
            .filter(JustificationDetailsServiceImpl::isObjectId)
            .map(ObjectId::new)
            .toList();
        if (justificationIds.isEmpty()) {
            return Page.empty(pageable);
        }
        return justificationDetailsRepository.findByJustificationIdIn(justificationIds, pageable).map(justificationDetailsMapper::toDto);
    }

    /**
     * Rejects a write whose resulting justification is not owned by the current apprentice.
     *
     * @param justificationId the id of the justification that would own the part.
     * @throws BadRequestAlertException with the key {@code notYourJustification}.
     */
    private void validateJustificationOwnership(String justificationId) {
        if (isCurrentUserAdmin()) {
            return;
        }
        Justification justification = justificationId == null ? null : justificationRepository.findById(justificationId).orElse(null);
        if (justification == null || !isOwnedBy(justification, currentUserProfileId())) {
            throw notYourJustification();
        }
    }

    /**
     * @param justificationDetails the part to check.
     * @throws BadRequestAlertException with the key {@code notYourJustification}.
     */
    private void validateOwnership(JustificationDetails justificationDetails) {
        if (isCurrentUserAdmin()) {
            return;
        }
        if (!isOwnedBy(justificationDetails, currentUserProfileId())) {
            throw notYourJustification();
        }
    }

    /**
     * @param justificationDetails the part to check.
     * @param profileId the profile id of the current user, or {@code null}.
     * @return whether the part belongs to a justification of that profile.
     */
    private static boolean isOwnedBy(JustificationDetails justificationDetails, String profileId) {
        return justificationDetails.getJustification() != null && isOwnedBy(justificationDetails.getJustification(), profileId);
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
     * @param justificationDetails the part to check.
     * @param profileId the profile id of the current user, or {@code null}.
     * @return whether the materia of the part is currently assigned to that instructor profile
     *         (UC010, E4): the readable scope of the decision flow.
     */
    private static boolean isAssignedTo(JustificationDetails justificationDetails, String profileId) {
        ClassSection classSection = justificationDetails.getClassSection();
        UserProfile instructor = classSection == null ? null : classSection.getInstructor();
        return instructor != null && profileId != null && profileId.equals(instructor.getId());
    }

    /**
     * @param justification the justification whose id is read.
     * @return the id of the justification, or {@code null} when it is absent.
     */
    private static String idOf(Justification justification) {
        return justification == null ? null : justification.getId();
    }

    /**
     * @param justification the justification DTO whose id is read.
     * @return the id of the justification, or {@code null} when it is absent.
     */
    private static String idOf(JustificationDTO justification) {
        return justification == null ? null : justification.getId();
    }

    /**
     * @param id the candidate id.
     * @return whether the id is a 24-hex string convertible into an {@code ObjectId}.
     */
    private static boolean isObjectId(String id) {
        return id != null && id.length() == 24 && ObjectId.isValid(id);
    }

    /**
     * @return whether the current user is an administrator, who reads and writes every part.
     */
    private static boolean isCurrentUserAdmin() {
        return SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
    }

    /**
     * @return the profile of the authenticated user, or {@code null} when the session has no
     *         login or the account has no profile.
     */
    private UserProfile currentUserProfile() {
        return currentUserContext.profile().orElse(null);
    }

    /**
     * @return the profile id of the authenticated user, or {@code null} when the session has no
     *         login or the account has no profile.
     */
    private String currentUserProfileId() {
        UserProfile profile = currentUserProfile();
        return profile == null ? null : profile.getId();
    }

    /**
     * @return the error thrown when a non-admin operates on a part that is not theirs.
     */
    private static BadRequestAlertException notYourJustification() {
        return new BadRequestAlertException("Solo puedes gestionar tus propias justificaciones", ENTITY_NAME, "notYourJustification");
    }

    /**
     * @return the error thrown when a non-admin decides a part of the materia of another
     *         instructor (UC010, E4).
     */
    private static BadRequestAlertException notYourClassSection() {
        return new BadRequestAlertException(
            "Esta justificación pertenece a la materia de otro instructor",
            ENTITY_NAME,
            "notYourClassSection"
        );
    }

    /**
     * @return the error thrown when a decision targets a part that is no longer pending (UC010,
     *         E2).
     */
    private static BadRequestAlertException noLongerPending() {
        return new BadRequestAlertException("Esta justificación ya no está pendiente", ENTITY_NAME, "alreadyProcessed");
    }

    /**
     * @return the error thrown when the part to update does not exist.
     */
    private static BadRequestAlertException idNotFound() {
        return new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
    }

    /**
     * @return the error thrown when the part carries a decision and can no longer be corrected
     *         (UC011, E3).
     */
    private static BadRequestAlertException alreadyProcessed() {
        return new BadRequestAlertException("Esta justificación ya fue procesada y no puede modificarse", ENTITY_NAME, "alreadyProcessed");
    }
}
