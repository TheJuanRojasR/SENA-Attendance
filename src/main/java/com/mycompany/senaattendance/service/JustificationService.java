package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.service.dto.JustificationDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.senaattendance.domain.Justification}.
 */
public interface JustificationService {
    /**
     * Save a justification.
     *
     * @param justificationDTO the entity to save.
     * @return the persisted entity.
     */
    JustificationDTO save(JustificationDTO justificationDTO);

    /**
     * Updates a justification.
     *
     * @param justificationDTO the entity to update.
     * @return the persisted entity.
     */
    JustificationDTO update(JustificationDTO justificationDTO);

    /**
     * Partially updates a justification.
     *
     * @param justificationDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<JustificationDTO> partialUpdate(JustificationDTO justificationDTO);

    /**
     * Get all the justifications.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<JustificationDTO> findAll(Pageable pageable);

    /**
     * Get all the justifications with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<JustificationDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" justification.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<JustificationDTO> findOne(String id);

    /**
     * Delete the "id" justification.
     *
     * @param id the id of the entity.
     */
    void delete(String id);
}
