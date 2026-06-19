package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.service.dto.ClassSectionDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.senaattendance.domain.ClassSection}.
 */
public interface ClassSectionService {
    /**
     * Save a classSection.
     *
     * @param classSectionDTO the entity to save.
     * @return the persisted entity.
     */
    ClassSectionDTO save(ClassSectionDTO classSectionDTO);

    /**
     * Updates a classSection.
     *
     * @param classSectionDTO the entity to update.
     * @return the persisted entity.
     */
    ClassSectionDTO update(ClassSectionDTO classSectionDTO);

    /**
     * Partially updates a classSection.
     *
     * @param classSectionDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ClassSectionDTO> partialUpdate(ClassSectionDTO classSectionDTO);

    /**
     * Get all the classSections.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ClassSectionDTO> findAll(Pageable pageable);

    /**
     * Get all the classSections with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ClassSectionDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" classSection.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ClassSectionDTO> findOne(String id);

    /**
     * Delete the "id" classSection.
     *
     * @param id the id of the entity.
     */
    void delete(String id);
}
