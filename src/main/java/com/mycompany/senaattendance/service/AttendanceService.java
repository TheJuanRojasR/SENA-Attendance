package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.domain.enumeration.StateAttendance;
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
     * Registers the attendance session of a class section on a session date (UC009). Every
     * confirmation is upserted by materia, aprendiz and fecha, and the apprentices left out of
     * the payload keep no record for that date, so the session may come back incomplete.
     *
     * @param attendanceSessionVM the materia, the session date and the confirmed marks.
     * @return the persisted session with its records and the derived completeness.
     */
    AttendanceSessionDTO saveSession(AttendanceSessionVM attendanceSessionVM);

    /**
     * Edits the state of one attendance record (A2). Only the assigned instructor of the materia
     * can do it, only Presente or Falla are accepted, and the trimester of the session date must
     * be active; the materia, the apprentice and the date of the record are preserved.
     *
     * @param id the id of the record to edit.
     * @param stateAttendance the new state.
     * @return the persisted record, or empty when it does not exist.
     */
    Optional<AttendanceDTO> updateState(String id, StateAttendance stateAttendance);

    /**
     * Gets a page of the attendance history the current user can read: every record for an
     * administrator, and only the records of the assigned materias for an instructor.
     *
     * @param pageable the pagination information.
     * @return the page of readable records.
     */
    Page<AttendanceDTO> findAllForCurrentUser(Pageable pageable);

    /**
     * Gets one attendance record when the current user can read it: every record for an
     * administrator, and only the records of the assigned materias for an instructor.
     *
     * @param id the id of the record.
     * @return the record, or empty when it does not exist or is outside the readable scope.
     */
    Optional<AttendanceDTO> findOneForCurrentUser(String id);
}
