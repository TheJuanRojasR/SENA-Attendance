package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.service.dto.GradeDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.senaattendance.domain.Grade}.
 */
public interface GradeService {
    /**
     * Save a grade.
     *
     * @param gradeDTO the entity to save.
     * @return the persisted entity.
     */
    GradeDTO save(GradeDTO gradeDTO);

    /**
     * Updates a grade.
     *
     * @param gradeDTO the entity to update.
     * @return the persisted entity.
     */
    GradeDTO update(GradeDTO gradeDTO);

    /**
     * Partially updates a grade.
     *
     * @param gradeDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<GradeDTO> partialUpdate(GradeDTO gradeDTO);

    /**
     * Get all the grades.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<GradeDTO> findAll(Pageable pageable);

    /**
     * Get all the grades with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<GradeDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" grade.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<GradeDTO> findOne(String id);

    /**
     * Delete the "id" grade.
     *
     * @param id the id of the entity.
     */
    void delete(String id);

    // --------------------------- New methods ---------------------------

    /**
     * Get all Grades with state ACTIVA
     *
     * @return list of active grades
     */
    List<GradeDTO> findActiveGrades();
}
