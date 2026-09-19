package com.mycompany.senaattendance.web.rest;

import com.mycompany.senaattendance.domain.enumeration.AlertaState;
import com.mycompany.senaattendance.domain.enumeration.AlertaType;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.AlertaInboxService;
import com.mycompany.senaattendance.service.dto.AlertaDTO;
import com.mycompany.senaattendance.web.rest.vm.AlertaAttendVM;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for the absence alerts (UC013).
 *
 * <p>The administrator reads and attends every alert; the instructor reads and attends the
 * consecutive alerts of their materias and the accumulated alerts of their fichas, resolved in
 * the service. A foreign alert answers {@code 404}, so the read never leaks its existence. The
 * apprentice has no access to this resource: their notice arrives through the inbox (UC018).
 */
@RestController
@RequestMapping("/api/alerts")
@PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
public class AlertaResource {

    private static final Logger LOG = LoggerFactory.getLogger(AlertaResource.class);

    private final AlertaInboxService alertaInboxService;

    public AlertaResource(AlertaInboxService alertaInboxService) {
        this.alertaInboxService = alertaInboxService;
    }

    /**
     * {@code GET /alerts} : get the alerts the authenticated user can read, newest first. The
     * optional filters by type, state, ficha, apprentice and generation range are combined with
     * AND, and the results are paginated (20 per page by default) with the total count and the
     * pagination links (UC013, A1).
     *
     * @param pageable the pagination information.
     * @param type the alert type to filter by (optional).
     * @param state the alert state to filter by (optional).
     * @param gradeId the ficha to filter by (optional).
     * @param studentId the apprentice profile to filter by (optional).
     * @param from the first generation instant to include (optional).
     * @param to the last generation instant to include (optional).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the page of alerts in
     *         body.
     */
    @GetMapping("")
    public ResponseEntity<List<AlertaDTO>> getAlerts(
        @org.springdoc.core.annotations.ParameterObject @PageableDefault(
            size = 20,
            sort = "generatedAt",
            direction = Sort.Direction.DESC
        ) Pageable pageable,
        @RequestParam(name = "type", required = false) AlertaType type,
        @RequestParam(name = "state", required = false) AlertaState state,
        @RequestParam(name = "gradeId", required = false) String gradeId,
        @RequestParam(name = "studentId", required = false) String studentId,
        @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
        @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        LOG.debug("REST request to get the page of Alerts the current user can read");
        Page<AlertaDTO> page = alertaInboxService.findAllForCurrentUser(type, state, gradeId, studentId, from, to, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET /alerts/students/:studentId} : get the alert history of one apprentice (UC013,
     * A5), paginated and restricted to the readable scope of the current user.
     *
     * @param studentId the apprentice profile id.
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the page of alerts of
     *         that apprentice in body.
     */
    @GetMapping("/students/{studentId}")
    public ResponseEntity<List<AlertaDTO>> getAlertsOfStudent(
        @PathVariable("studentId") String studentId,
        @org.springdoc.core.annotations.ParameterObject @PageableDefault(
            size = 20,
            sort = "generatedAt",
            direction = Sort.Direction.DESC
        ) Pageable pageable
    ) {
        LOG.debug("REST request to get the page of Alerts of apprentice {}", studentId);
        Page<AlertaDTO> page = alertaInboxService.findByStudentForCurrentUser(studentId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET /alerts/:id} : get one alert the authenticated user can read (UC013, A1). A
     * foreign alert answers {@code 404}.
     *
     * @param id the id of the alert to read.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the alert in body, or
     *         with status {@code 404 (Not Found)} when it does not exist or is out of scope.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AlertaDTO> getAlert(@PathVariable("id") String id) {
        LOG.debug("REST request to get Alert : {}", id);
        return ResponseUtil.wrapOrNotFound(alertaInboxService.findOneForCurrentUser(id));
    }

    /**
     * {@code PATCH /alerts/:id/read} : mark one readable alert as read (UC013, A2). The transition
     * only applies from unread, so calling it again is idempotent.
     *
     * @param id the id of the alert to mark as read.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the updated alert, or
     *         with status {@code 404 (Not Found)} when it does not exist or is out of scope.
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<AlertaDTO> markAlertAsRead(@PathVariable("id") String id) {
        LOG.debug("REST request to mark Alert {} as read", id);
        return ResponseUtil.wrapOrNotFound(alertaInboxService.markAsRead(id));
    }

    /**
     * {@code PATCH /alerts/:id/attend} : mark one readable alert as attended with the follow-up
     * observation (UC013, A3). Only the user who can read the alert can attend it, and a resolved
     * alert is history that cannot be attended.
     *
     * @param id the id of the alert to attend.
     * @param attendVM the observation of the follow-up.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the updated alert, or
     *         with status {@code 404 (Not Found)} when it does not exist or is out of scope.
     */
    @PatchMapping("/{id}/attend")
    public ResponseEntity<AlertaDTO> attendAlert(@PathVariable("id") String id, @Valid @RequestBody AlertaAttendVM attendVM) {
        LOG.debug("REST request to mark Alert {} as attended", id);
        return ResponseUtil.wrapOrNotFound(alertaInboxService.attend(id, attendVM.getObservation()));
    }
}
