package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.service.dto.JustificationDetailsDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.senaattendance.domain.JustificationDetails}.
 */
public interface JustificationDetailsService {
    /**
     * Save a justificationDetails.
     *
     * @param justificationDetailsDTO the entity to save.
     * @return the persisted entity.
     */
    JustificationDetailsDTO save(JustificationDetailsDTO justificationDetailsDTO);

    /**
     * Updates a justificationDetails.
     *
     * @param justificationDetailsDTO the entity to update.
     * @return the persisted entity.
     */
    JustificationDetailsDTO update(JustificationDetailsDTO justificationDetailsDTO);

    /**
     * Partially updates a justificationDetails.
     *
     * @param justificationDetailsDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<JustificationDetailsDTO> partialUpdate(JustificationDetailsDTO justificationDetailsDTO);

    /**
     * Get all the justificationDetailses.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<JustificationDetailsDTO> findAll(Pageable pageable);

    /**
     * Get all the justificationDetailses with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<JustificationDetailsDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" justificationDetails.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<JustificationDetailsDTO> findOne(String id);

    /**
     * Delete the "id" justificationDetails.
     *
     * @param id the id of the entity.
     */
    void delete(String id);
}
