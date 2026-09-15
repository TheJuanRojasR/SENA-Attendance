package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.domain.enumeration.StateJustification;
import com.mycompany.senaattendance.service.dto.JustificationDetailsDTO;
import com.mycompany.senaattendance.web.rest.vm.JustificationDecisionVM;
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
     * Updates a justificationDetails with the correction contract of A5: only the apprentice
     * correction fields are copied, and the state, the rejection reason, the response date and
     * the relationships keep their persisted values. A payload that carries those server-owned
     * fields must still satisfy the required shape of the DTO, but their values are ignored.
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
     * Decide one part (UC010, flow step 5). Only the instructor currently assigned to the materia
     * of the part can decide it, and an administrator can decide any part. Approving converts the
     * {@code FALLA} records of the apprentice in that materia inside the justified period to
     * {@code JUSTIFICADA}, with the audit entry of every change; rejecting only registers the
     * reason. The decision applies to a pending part, records the response date, marks the
     * decision as late when it arrives after the instructor response deadline and preserves the
     * deadline mark of the justification.
     *
     * @param id the id of the part to decide.
     * @param decision the state and the reasons of the decision.
     * @return the persisted part, or empty when it does not exist.
     */
    Optional<JustificationDetailsDTO> decide(String id, JustificationDecisionVM decision);

    /**
     * Delete the "id" justificationDetails.
     *
     * @param id the id of the entity.
     */
    void delete(String id);
}
