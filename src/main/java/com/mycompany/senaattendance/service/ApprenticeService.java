package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.service.dto.ApprenticeDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.senaattendance.domain.Apprentice}.
 */
public interface ApprenticeService {
    /**
     * Save a apprentice.
     *
     * @param apprenticeDTO the entity to save.
     * @return the persisted entity.
     */
    ApprenticeDTO save(ApprenticeDTO apprenticeDTO);

    /**
     * Updates a apprentice.
     *
     * @param apprenticeDTO the entity to update.
     * @return the persisted entity.
     */
    ApprenticeDTO update(ApprenticeDTO apprenticeDTO);

    /**
     * Partially updates a apprentice.
     *
     * @param apprenticeDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ApprenticeDTO> partialUpdate(ApprenticeDTO apprenticeDTO);

    /**
     * Get all the apprentices.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ApprenticeDTO> findAll(Pageable pageable);

    /**
     * Get all the apprentices with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ApprenticeDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" apprentice.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ApprenticeDTO> findOne(String id);

    /**
     * Delete the "id" apprentice.
     *
     * @param id the id of the entity.
     */
    void delete(String id);
}
