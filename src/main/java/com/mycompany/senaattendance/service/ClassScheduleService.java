package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.service.dto.ClassScheduleDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.senaattendance.domain.ClassSchedule}.
 */
public interface ClassScheduleService {
    /**
     * Save a classSchedule.
     *
     * @param classScheduleDTO the entity to save.
     * @return the persisted entity.
     */
    ClassScheduleDTO save(ClassScheduleDTO classScheduleDTO);

    /**
     * Updates a classSchedule.
     *
     * @param classScheduleDTO the entity to update.
     * @return the persisted entity.
     */
    ClassScheduleDTO update(ClassScheduleDTO classScheduleDTO);

    /**
     * Partially updates a classSchedule.
     *
     * @param classScheduleDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ClassScheduleDTO> partialUpdate(ClassScheduleDTO classScheduleDTO);

    /**
     * Get all the classSchedules.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ClassScheduleDTO> findAll(Pageable pageable);

    /**
     * Get all the classSchedules with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ClassScheduleDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get all the classSchedules the current user can read. An administrator reads every
     * schedule and an instructor only the schedules of the materias assigned to them.
     *
     * @param pageable the pagination information.
     * @return the page of readable entities.
     */
    Page<ClassScheduleDTO> findAllForCurrentUser(Pageable pageable);

    /**
     * Get all the classSchedules the current user can read with eager load of many-to-many
     * relationships. An administrator reads every schedule and an instructor only the schedules
     * of the materias assigned to them.
     *
     * @param pageable the pagination information.
     * @return the page of readable entities.
     */
    Page<ClassScheduleDTO> findAllWithEagerRelationshipsForCurrentUser(Pageable pageable);

    /**
     * Get the "id" classSchedule.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ClassScheduleDTO> findOne(String id);

    /**
     * Get the "id" classSchedule when the current user can read it. An administrator reads any
     * schedule and an instructor only the schedules of the materias assigned to them, so a
     * schedule outside that scope resolves as empty.
     *
     * @param id the id of the entity.
     * @return the readable entity.
     */
    Optional<ClassScheduleDTO> findOneForCurrentUser(String id);

    /**
     * Delete the "id" classSchedule.
     *
     * @param id the id of the entity.
     */
    void delete(String id);
}
