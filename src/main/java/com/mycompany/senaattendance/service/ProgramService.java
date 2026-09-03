package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.service.dto.ProgramActivatedResponseDTO;
import com.mycompany.senaattendance.service.dto.ProgramDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.senaattendance.domain.Program}.
 */
public interface ProgramService {
    /**
     * Save a program.
     *
     * @param programDTO the entity to save.
     * @return the persisted entity.
     */
    ProgramDTO save(ProgramDTO programDTO);

    /**
     * Updates a program.
     *
     * @param programDTO the entity to update.
     * @return the persisted entity.
     */
    ProgramDTO update(ProgramDTO programDTO);

    /**
     * Partially updates a program.
     *
     * @param programDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ProgramDTO> partialUpdate(ProgramDTO programDTO);

    /**
     * Set the activation status of a program.
     *
     * <p>Idempotent: if the program already has the target {@code status} it is
     * returned unchanged. When the program is deactivated and still has active
     * fichas (grades), the response carries a dynamic {@code warning} and the
     * count of active fichas.
     *
     * @param id the program id.
     * @param status the target status ({@code true} = active, {@code false} = inactive).
     * @return the updated program together with any deactivation warning.
     */
    ProgramActivatedResponseDTO setActivated(String id, Boolean status);

    /**
     * Get all the programs.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ProgramDTO> findAll(Pageable pageable);

    /**
     * Search and filter programs.
     *
     * <p>Matches programs by code or name containing {@code searchTerm} (ignored when
     * blank), and optionally filters by {@code status}. Results are combined and paginated.
     *
     * @param searchTerm the code/name fragment to search for (may be blank).
     * @param status the status to filter by (may be null for all).
     * @param pageable the pagination information.
     * @return a page of matching programs.
     */
    Page<ProgramDTO> search(String searchTerm, Boolean status, Pageable pageable);

    /**
     * Get the "id" program.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ProgramDTO> findOne(String id);

    /**
     * Delete the "id" program.
     *
     * @param id the id of the entity.
     */
    void delete(String id);
}
