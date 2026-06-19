package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.service.dto.JustificationTypeDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.mycompany.senaattendance.domain.JustificationType}.
 */
public interface JustificationTypeService {
    /**
     * Save a justificationType.
     *
     * @param justificationTypeDTO the entity to save.
     * @return the persisted entity.
     */
    JustificationTypeDTO save(JustificationTypeDTO justificationTypeDTO);

    /**
     * Updates a justificationType.
     *
     * @param justificationTypeDTO the entity to update.
     * @return the persisted entity.
     */
    JustificationTypeDTO update(JustificationTypeDTO justificationTypeDTO);

    /**
     * Partially updates a justificationType.
     *
     * @param justificationTypeDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<JustificationTypeDTO> partialUpdate(JustificationTypeDTO justificationTypeDTO);

    /**
     * Get all the justificationTypes.
     *
     * @return the list of entities.
     */
    List<JustificationTypeDTO> findAll();

    /**
     * Get the "id" justificationType.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<JustificationTypeDTO> findOne(String id);

    /**
     * Delete the "id" justificationType.
     *
     * @param id the id of the entity.
     */
    void delete(String id);
}
