package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.service.dto.UserProfileDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.senaattendance.domain.UserProfile}.
 */
public interface UserProfileService {
    /**
     * Save a userProfile.
     *
     * @param userProfileDTO the entity to save.
     * @return the persisted entity.
     */
    UserProfileDTO save(UserProfileDTO userProfileDTO);

    /**
     * Updates a userProfile.
     *
     * @param userProfileDTO the entity to update.
     * @return the persisted entity.
     */
    UserProfileDTO update(UserProfileDTO userProfileDTO);

    /**
     * Partially updates a userProfile.
     *
     * @param userProfileDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<UserProfileDTO> partialUpdate(UserProfileDTO userProfileDTO);

    /**
     * Get all the userProfiles.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<UserProfileDTO> findAll(Pageable pageable);

    /**
     * Get all the userProfiles with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<UserProfileDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" userProfile.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<UserProfileDTO> findOne(String id);

    /**
     * Delete the "id" userProfile.
     *
     * @param id the id of the entity.
     */
    void delete(String id);

    // --------------------------- New methods ---------------------------

    /**
     * Get all the apprentices.
     *
     * @return the list of apprentice user profiles.
     */
    List<UserProfileDTO> findAllApprentices();
}
