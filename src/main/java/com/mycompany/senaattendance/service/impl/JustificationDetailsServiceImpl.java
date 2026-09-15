package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.Justification;
import com.mycompany.senaattendance.domain.JustificationDetails;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.repository.JustificationDetailsRepository;
import com.mycompany.senaattendance.repository.JustificationRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.JustificationDetailsService;
import com.mycompany.senaattendance.service.dto.JustificationDTO;
import com.mycompany.senaattendance.service.dto.JustificationDetailsDTO;
import com.mycompany.senaattendance.service.mapper.JustificationDetailsMapper;
import com.mycompany.senaattendance.web.rest.errors.BadRequestAlertException;
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
 */
@Service
public class JustificationDetailsServiceImpl implements JustificationDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(JustificationDetailsServiceImpl.class);

    private static final String ENTITY_NAME = "justificationDetails";

    private final JustificationDetailsRepository justificationDetailsRepository;

    private final JustificationDetailsMapper justificationDetailsMapper;

    private final JustificationRepository justificationRepository;

    private final UserRepository userRepository;

    private final UserProfileRepository userProfileRepository;

    public JustificationDetailsServiceImpl(
        JustificationDetailsRepository justificationDetailsRepository,
        JustificationDetailsMapper justificationDetailsMapper,
        JustificationRepository justificationRepository,
        UserRepository userRepository,
        UserProfileRepository userProfileRepository
    ) {
        this.justificationDetailsRepository = justificationDetailsRepository;
        this.justificationDetailsMapper = justificationDetailsMapper;
        this.justificationRepository = justificationRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public JustificationDetailsDTO save(JustificationDetailsDTO justificationDetailsDTO) {
        LOG.debug("Request to save JustificationDetails : {}", justificationDetailsDTO);
        validateJustificationOwnership(idOf(justificationDetailsDTO.getJustification()));
        JustificationDetails justificationDetails = justificationDetailsMapper.toEntity(justificationDetailsDTO);
        justificationDetails = justificationDetailsRepository.save(justificationDetails);
        return justificationDetailsMapper.toDto(justificationDetails);
    }

    @Override
    public JustificationDetailsDTO update(JustificationDetailsDTO justificationDetailsDTO) {
        LOG.debug("Request to update JustificationDetails : {}", justificationDetailsDTO);
        validatePersistedOwnership(justificationDetailsDTO.getId());
        validateJustificationOwnership(idOf(justificationDetailsDTO.getJustification()));
        JustificationDetails justificationDetails = justificationDetailsMapper.toEntity(justificationDetailsDTO);
        justificationDetails = justificationDetailsRepository.save(justificationDetails);
        return justificationDetailsMapper.toDto(justificationDetails);
    }

    @Override
    public Optional<JustificationDetailsDTO> partialUpdate(JustificationDetailsDTO justificationDetailsDTO) {
        LOG.debug("Request to partially update JustificationDetails : {}", justificationDetailsDTO);

        return justificationDetailsRepository
            .findById(justificationDetailsDTO.getId())
            .map(existingJustificationDetails -> {
                validateOwnership(existingJustificationDetails);
                justificationDetailsMapper.partialUpdate(existingJustificationDetails, justificationDetailsDTO);
                validateJustificationOwnership(idOf(existingJustificationDetails.getJustification()));

                return existingJustificationDetails;
            })
            .map(justificationDetailsRepository::save)
            .map(justificationDetailsMapper::toDto);
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
     * Rejects a write whose persisted part belongs to another apprentice.
     *
     * @param id the id of the part to write.
     * @throws BadRequestAlertException with the key {@code notYourJustification}.
     */
    private void validatePersistedOwnership(String id) {
        if (isCurrentUserAdmin()) {
            return;
        }
        JustificationDetails justificationDetails = id == null ? null : justificationDetailsRepository.findById(id).orElse(null);
        if (justificationDetails == null) {
            throw notYourJustification();
        }
        validateOwnership(justificationDetails);
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
}
