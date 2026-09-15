package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.domain.enumeration.StateJustification;
import com.mycompany.senaattendance.service.dto.JustificationDetailsDTO;
import java.time.LocalDate;
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
     * Get the page of parts the current user must decide and their history (UC010, A1). An
     * instructor reads the parts of the materias assigned to them, and an administrator reads
     * every part.
     *
     * @param stateJustification the state to include; {@code null} defaults to pending parts.
     * @param classSectionId the materia to filter by (may be null for every materia).
     * @param createdFrom the first request date to include (may be null for no lower bound).
     * @param createdTo the last request date to include (may be null for no upper bound).
     * @param pageable the pagination information.
     * @return the page of parts matching the filters inside the readable scope.
     */
    Page<JustificationDetailsDTO> findPendingForCurrentUser(
        StateJustification stateJustification,
        String classSectionId,
        LocalDate createdFrom,
        LocalDate createdTo,
        Pageable pageable
    );

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
