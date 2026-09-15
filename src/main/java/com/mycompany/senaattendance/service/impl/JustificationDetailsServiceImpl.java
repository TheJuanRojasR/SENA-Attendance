package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Justification;
import com.mycompany.senaattendance.domain.JustificationDetails;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.StateJustification;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.JustificationDetailsRepository;
import com.mycompany.senaattendance.repository.JustificationDetailsSearchCriteria;
import com.mycompany.senaattendance.repository.JustificationRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.JustificationDetailsService;
import com.mycompany.senaattendance.service.dto.JustificationDTO;
import com.mycompany.senaattendance.service.dto.JustificationDetailsDTO;
import com.mycompany.senaattendance.service.mapper.JustificationDetailsMapper;
import com.mycompany.senaattendance.service.util.BusinessDays;
import com.mycompany.senaattendance.web.rest.errors.BadRequestAlertException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
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
 * the instructor decision over a part arrives with UC010.
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

    private final UserRepository userRepository;

    private final UserProfileRepository userProfileRepository;

    private final Clock clock;

    public JustificationDetailsServiceImpl(
        JustificationDetailsRepository justificationDetailsRepository,
        JustificationDetailsMapper justificationDetailsMapper,
        JustificationRepository justificationRepository,
        ClassSectionRepository classSectionRepository,
        UserRepository userRepository,
        UserProfileRepository userProfileRepository,
        Clock clock
    ) {
        this.justificationDetailsRepository = justificationDetailsRepository;
        this.justificationDetailsMapper = justificationDetailsMapper;
        this.justificationRepository = justificationRepository;
        this.classSectionRepository = classSectionRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
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
            // The part reopens as pending, so it has no response left; the rejection reason is
            // kept as the trace of why it was rejected until the next decision (UC010) replaces it.
            part.setStateJustification(StateJustification.PENDIENTE);
            part.setResponseDate(null);
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

    @Override
    public Optional<JustificationDetailsDTO> findOne(String id) {
        LOG.debug("Request to get JustificationDetails : {}", id);
        Optional<JustificationDetails> justificationDetails = justificationDetailsRepository.findOneWithEagerRelationships(id);
        if (isCurrentUserAdmin()) {
            return justificationDetails.map(justificationDetailsMapper::toDto);
        }
        String currentProfileId = currentUserProfileId();
        return justificationDetails.filter(details -> isOwnedBy(details, currentProfileId)).map(justificationDetailsMapper::toDto);
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
     * @return the error thrown when a non-admin operates on a part that is not theirs.
     */
    private static BadRequestAlertException notYourJustification() {
        return new BadRequestAlertException("Solo puedes gestionar tus propias justificaciones", ENTITY_NAME, "notYourJustification");
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
