package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.service.dto.TrimesterDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.senaattendance.domain.Trimester}.
 */
public interface TrimesterService {
    /**
     * Save a trimester.
     *
     * @param trimesterDTO the entity to save.
     * @return the persisted entity.
     */
    TrimesterDTO save(TrimesterDTO trimesterDTO);

    /**
     * Updates a trimester.
     *
     * @param trimesterDTO the entity to update.
     * @return the persisted entity.
     */
    TrimesterDTO update(TrimesterDTO trimesterDTO);

    /**
     * Partially updates a trimester.
     *
     * @param trimesterDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<TrimesterDTO> partialUpdate(TrimesterDTO trimesterDTO);

    /**
     * Get all the trimesters.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<TrimesterDTO> findAll(Pageable pageable);

    /**
     * Search and filter trimesters.
     *
     * <p>A 4-digit term is matched against the {@code startDate} year; any other
     * non-blank term is matched as a case-insensitive name substring. The optional
     * {@code status} filter is ANDed with the text filter. A blank term with no status
     * returns all trimesters.
     *
     * @param searchTerm the term to search for (may be blank).
     * @param status the status to filter by (may be null for all).
     * @param pageable the pagination information.
     * @return a page of matching trimesters.
     */
    Page<TrimesterDTO> search(String searchTerm, Boolean status, Pageable pageable);

    /**
     * Get the "id" trimester.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<TrimesterDTO> findOne(String id);

    /**
     * Synchronises every trimester's {@code status} with today versus its
     * {@code [startDate, endDate]} range. Only trimesters whose computed status differs
     * from the persisted status are rewritten.
     */
    void syncStatuses();

    /**
     * Delete the "id" trimester.
     *
     * @param id the id of the entity.
     */
    void delete(String id);
}
