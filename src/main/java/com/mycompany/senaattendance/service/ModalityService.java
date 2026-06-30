package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.service.dto.ModalityDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.mycompany.senaattendance.domain.Modality}.
 */
public interface ModalityService {
    /**
     * Save a modality.
     *
     * @param modalityDTO the entity to save.
     * @return the persisted entity.
     */
    ModalityDTO save(ModalityDTO modalityDTO);

    /**
     * Updates a modality.
     *
     * @param modalityDTO the entity to update.
     * @return the persisted entity.
     */
    ModalityDTO update(ModalityDTO modalityDTO);

    /**
     * Partially updates a modality.
     *
     * @param modalityDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ModalityDTO> partialUpdate(ModalityDTO modalityDTO);

    /**
     * Get all the modalities.
     *
     * @return the list of entities.
     */
    List<ModalityDTO> findAll();

    /**
     * Get the "id" modality.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ModalityDTO> findOne(String id);

    /**
     * Delete the "id" modality.
     *
     * @param id the id of the entity.
     */
    void delete(String id);

    List<ModalityDTO> findActiveModalities();
}
