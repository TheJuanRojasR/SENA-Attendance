package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.Justification;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.repository.JustificationRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.JustificationService;
import com.mycompany.senaattendance.service.dto.JustificationDTO;
import com.mycompany.senaattendance.service.dto.UserProfileDTO;
import com.mycompany.senaattendance.service.mapper.JustificationMapper;
import com.mycompany.senaattendance.web.rest.errors.BadRequestAlertException;
import java.time.Instant;
import java.util.Optional;
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
 */
@Service
public class JustificationServiceImpl implements JustificationService {

    private static final Logger LOG = LoggerFactory.getLogger(JustificationServiceImpl.class);

    private static final String ENTITY_NAME = "justification";

    private final JustificationRepository justificationRepository;

    private final JustificationMapper justificationMapper;

    private final UserRepository userRepository;

    private final UserProfileRepository userProfileRepository;

    public JustificationServiceImpl(
        JustificationRepository justificationRepository,
        JustificationMapper justificationMapper,
        UserRepository userRepository,
        UserProfileRepository userProfileRepository
    ) {
        this.justificationRepository = justificationRepository;
        this.justificationMapper = justificationMapper;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public JustificationDTO save(JustificationDTO justificationDTO) {
        LOG.debug("Request to save Justification : {}", justificationDTO);
        validateStudentIsCurrentApprentice(justificationDTO.getStudent());
        Justification justification = justificationMapper.toEntity(justificationDTO);

        justification.setCreatedDate(Instant.now());
        Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
        if (currentUserLogin.isPresent()) {
            justification.setCreatedBy(currentUserLogin.get());
        }

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
                justificationMapper.partialUpdate(existingJustification, justificationDTO);
                validateProfileIsCurrentApprentice(
                    existingJustification.getStudent() == null ? null : existingJustification.getStudent().getId()
                );

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
}
