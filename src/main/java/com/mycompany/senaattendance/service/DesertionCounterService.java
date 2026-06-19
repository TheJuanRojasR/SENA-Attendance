package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.service.dto.DesertionCounterDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.senaattendance.domain.DesertionCounter}.
 */
public interface DesertionCounterService {
    /**
     * Save a desertionCounter.
     *
     * @param desertionCounterDTO the entity to save.
     * @return the persisted entity.
     */
    DesertionCounterDTO save(DesertionCounterDTO desertionCounterDTO);

    /**
     * Updates a desertionCounter.
     *
     * @param desertionCounterDTO the entity to update.
     * @return the persisted entity.
     */
    DesertionCounterDTO update(DesertionCounterDTO desertionCounterDTO);

    /**
     * Partially updates a desertionCounter.
     *
     * @param desertionCounterDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<DesertionCounterDTO> partialUpdate(DesertionCounterDTO desertionCounterDTO);

    /**
     * Get all the desertionCounters.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<DesertionCounterDTO> findAll(Pageable pageable);

    /**
     * Get all the desertionCounters with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<DesertionCounterDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" desertionCounter.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<DesertionCounterDTO> findOne(String id);

    /**
     * Delete the "id" desertionCounter.
     *
     * @param id the id of the entity.
     */
    void delete(String id);
}
