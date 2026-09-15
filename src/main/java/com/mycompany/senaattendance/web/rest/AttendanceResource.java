package com.mycompany.senaattendance.web.rest;

import com.mycompany.senaattendance.domain.enumeration.StateAttendance;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.AttendanceService;
import com.mycompany.senaattendance.service.dto.AttendanceDTO;
import com.mycompany.senaattendance.service.dto.AttendanceSessionDTO;
import com.mycompany.senaattendance.web.rest.errors.BadRequestAlertException;
import com.mycompany.senaattendance.web.rest.vm.AttendanceSessionVM;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.senaattendance.domain.Attendance}.
 *
 * <p>Attendance has no generic CRUD: records are born from the session registration (UC009),
 * edited one by one through the state edit of A2, and read through the scoped history. Creating
 * or deleting a record outside that flow is not part of the use case.
 */
@RestController
@RequestMapping("/api/attendances")
public class AttendanceResource {

    private static final Logger LOG = LoggerFactory.getLogger(AttendanceResource.class);

    private static final String ENTITY_NAME = "attendance";

    @Value("${jhipster.clientApp.name:senaAttendance}")
    private String applicationName;

    private final AttendanceService attendanceService;

    public AttendanceResource(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * {@code PUT  /attendances/session} : registers the attendance session of a class section on
     * a session date (UC009). The request carries the materia, the date and the confirmed marks;
     * every mark is upserted and the apprentices left out keep no record, so the response reports
     * whether the session is complete. Saving the same session again is idempotent.
     *
     * @param attendanceSessionVM the materia, the session date and the confirmed marks.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the persisted
     *         session, or with status {@code 400 (Bad Request)} when a UC009 rule is violated.
     */
    @PutMapping("/session")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<AttendanceSessionDTO> saveAttendanceSession(@Valid @RequestBody AttendanceSessionVM attendanceSessionVM) {
        LOG.debug("REST request to save an Attendance session : {}", attendanceSessionVM);
        return ResponseEntity.ok().body(attendanceService.saveSession(attendanceSessionVM));
    }

    /**
     * {@code PATCH  /attendances/:id} : edits the state of one attendance record, which is the
     * only change A2 allows. The record is located by the path id and the payload only carries
     * {@code stateAttendance}; anything sent about the materia, the apprentice or the date is
     * ignored, and the service rejects the edit when the current user is not the instructor
     * assigned to the materia or when the trimester of the record is already closed.
     *
     * @param id the id of the attendance record to edit.
     * @param attendanceDTO the payload with the new state.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated
     *         record, or with status {@code 404 (Not Found)} when the record does not exist.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<AttendanceDTO> partialUpdateAttendance(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody AttendanceDTO attendanceDTO
    ) {
        LOG.debug("REST request to edit the state of Attendance : {}, {}", id, attendanceDTO);
        if (attendanceDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, attendanceDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return ResponseUtil.wrapOrNotFound(
            attendanceService.updateState(id, attendanceDTO.getStateAttendance()),
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, id)
        );
    }

    /**
     * {@code GET  /attendances} : gets the attendance history the current user can read, with the
     * optional filters of A1 combined. An administrator reads every record; an instructor only the
     * records of the materias assigned to them, so filtering by a materia outside that scope
     * returns an empty page. An apprentice reads only their own records (UC011, step 1), which is
     * what lets them pick the F marks to justify. {@code studentId} is the apprentice profile id,
     * the same identity the session registration uses and the response exposes.
     *
     * @param pageable the pagination information, 20 records per page by default.
     * @param classSectionId the materia to filter by (optional).
     * @param date the session date to filter by (optional).
     * @param studentId the apprentice profile id to filter by (optional).
     * @param stateAttendance the state to filter by (optional).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the page of records.
     */
    @GetMapping("")
    @PreAuthorize(
        "hasAnyAuthority(\"" +
            AuthoritiesConstants.ADMIN +
            "\", \"" +
            AuthoritiesConstants.INSTRUCTOR +
            "\", \"" +
            AuthoritiesConstants.APPRENTICE +
            "\")"
    )
    public ResponseEntity<List<AttendanceDTO>> getAllAttendances(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "classSectionId", required = false) String classSectionId,
        @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam(name = "studentId", required = false) String studentId,
        @RequestParam(name = "stateAttendance", required = false) StateAttendance stateAttendance
    ) {
        LOG.debug("REST request to get a page of Attendances");
        Page<AttendanceDTO> page = attendanceService.findAllForCurrentUser(classSectionId, date, studentId, stateAttendance, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /attendances/:id} : gets one attendance record when the current user can read
     * it. An administrator reads every record; an instructor only the records of the materias
     * assigned to them, and an apprentice only their own records. A record outside the readable
     * scope resolves as not found.
     *
     * @param id the id of the record to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the record,
     *         or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    @PreAuthorize(
        "hasAnyAuthority(\"" +
            AuthoritiesConstants.ADMIN +
            "\", \"" +
            AuthoritiesConstants.INSTRUCTOR +
            "\", \"" +
            AuthoritiesConstants.APPRENTICE +
            "\")"
    )
    public ResponseEntity<AttendanceDTO> getAttendance(@PathVariable("id") String id) {
        LOG.debug("REST request to get Attendance : {}", id);
        return ResponseUtil.wrapOrNotFound(attendanceService.findOneForCurrentUser(id));
    }
}
