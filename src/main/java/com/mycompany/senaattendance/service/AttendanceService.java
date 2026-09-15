package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.service.dto.AttendanceDTO;
import com.mycompany.senaattendance.service.dto.AttendanceSessionDTO;
import com.mycompany.senaattendance.web.rest.vm.AttendanceSessionVM;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.senaattendance.domain.Attendance}.
 */
public interface AttendanceService {
    /**
     * Save a attendance.
     *
     * @param attendanceDTO the entity to save.
     * @return the persisted entity.
     */
    AttendanceDTO save(AttendanceDTO attendanceDTO);

    /**
     * Registers the attendance session of a class section on a session date (UC009). Every
     * confirmation is upserted by materia, aprendiz and fecha, and the apprentices left out of
     * the payload keep no record for that date, so the session may come back incomplete.
     *
     * @param attendanceSessionVM the materia, the session date and the confirmed marks.
     * @return the persisted session with its records and the derived completeness.
     */
    AttendanceSessionDTO saveSession(AttendanceSessionVM attendanceSessionVM);

    /**
     * Updates a attendance.
     *
     * @param attendanceDTO the entity to update.
     * @return the persisted entity.
     */
    AttendanceDTO update(AttendanceDTO attendanceDTO);

    /**
     * Partially updates a attendance.
     *
     * @param attendanceDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<AttendanceDTO> partialUpdate(AttendanceDTO attendanceDTO);

    /**
     * Get all the attendances.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AttendanceDTO> findAll(Pageable pageable);

    /**
     * Get all the attendances with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AttendanceDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" attendance.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AttendanceDTO> findOne(String id);

    /**
     * Delete the "id" attendance.
     *
     * @param id the id of the entity.
     */
    void delete(String id);
}
