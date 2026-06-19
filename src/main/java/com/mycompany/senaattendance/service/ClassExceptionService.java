package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.service.dto.ClassExceptionDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.senaattendance.domain.ClassException}.
 */
public interface ClassExceptionService {
    /**
     * Save a classException.
     *
     * @param classExceptionDTO the entity to save.
     * @return the persisted entity.
     */
    ClassExceptionDTO save(ClassExceptionDTO classExceptionDTO);

    /**
     * Updates a classException.
     *
     * @param classExceptionDTO the entity to update.
     * @return the persisted entity.
     */
    ClassExceptionDTO update(ClassExceptionDTO classExceptionDTO);

    /**
     * Partially updates a classException.
     *
     * @param classExceptionDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ClassExceptionDTO> partialUpdate(ClassExceptionDTO classExceptionDTO);

    /**
     * Get all the classExceptions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ClassExceptionDTO> findAll(Pageable pageable);

    /**
     * Get all the classExceptions with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ClassExceptionDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" classException.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ClassExceptionDTO> findOne(String id);

    /**
     * Delete the "id" classException.
     *
     * @param id the id of the entity.
     */
    void delete(String id);
}
