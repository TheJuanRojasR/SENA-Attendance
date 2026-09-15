package com.mycompany.senaattendance.web.rest;

import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.GradeService;
import com.mycompany.senaattendance.service.dto.GradeDTO;
import com.mycompany.senaattendance.web.rest.errors.BadRequestAlertException;
import com.mycompany.senaattendance.web.rest.vm.GradeIdVM;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.senaattendance.domain.Grade}.
 */
@RestController
@RequestMapping("/api/grades")
public class GradeResource {

    private static final Logger LOG = LoggerFactory.getLogger(GradeResource.class);

    private static final String ENTITY_NAME = "grade";

    @Value("${jhipster.clientApp.name:senaAttendance}")
    private String applicationName;

    private final GradeService gradeService;

    private final GradeRepository gradeRepository;

    public GradeResource(GradeService gradeService, GradeRepository gradeRepository) {
        this.gradeService = gradeService;
        this.gradeRepository = gradeRepository;
    }

    /**
     * {@code POST  /grades} : Create a new grade.
     *
     * @param gradeDTO the gradeDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new gradeDTO, or with status {@code 400 (Bad Request)} if the grade has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<GradeDTO> createGrade(@Valid @RequestBody GradeDTO gradeDTO) throws URISyntaxException {
        LOG.debug("REST request to save Grade : {}", gradeDTO);
        if (gradeDTO.getId() != null) {
            throw new BadRequestAlertException("A new grade cannot already have an ID", ENTITY_NAME, "idexists");
        }
        gradeDTO = gradeService.save(gradeDTO);
        return ResponseEntity.created(new URI("/api/grades/" + gradeDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, gradeDTO.getId()))
            .body(gradeDTO);
    }

    /**
     * {@code PUT  /grades} : Updates an existing grade; the id is taken from the request body.
     *
     * @param gradeDTO the gradeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated gradeDTO,
     * or with status {@code 400 (Bad Request)} if the gradeDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the gradeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<GradeDTO> updateGrade(@Valid @RequestBody GradeDTO gradeDTO) throws URISyntaxException {
        String id = gradeDTO.getId();
        LOG.debug("REST request to update Grade : {}", gradeDTO);
        if (id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        if (!gradeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        gradeDTO = gradeService.update(gradeDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, gradeDTO.getId()))
            .body(gradeDTO);
    }

    /**
     * {@code PATCH  /grades} : Partial updates given fields of an existing grade, field will ignore if it is null.
     * The id is taken from the request body.
     *
     * @param gradeDTO the gradeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated gradeDTO,
     * or with status {@code 400 (Bad Request)} if the gradeDTO is not valid,
     * or with status {@code 404 (Not Found)} if the gradeDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the gradeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<GradeDTO> partialUpdateGrade(@NotNull @RequestBody GradeDTO gradeDTO) throws URISyntaxException {
        String id = gradeDTO.getId();
        LOG.debug("REST request to partial update Grade partially : {}", gradeDTO);
        if (id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        if (!gradeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<GradeDTO> result = gradeService.partialUpdate(gradeDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, gradeDTO.getId())
        );
    }

    /**
     * {@code PATCH  /grades/postponed} : Postpone the ficha identified by {@code id}. Only a
     * PENDIENTE or ACTIVA ficha can be postponed, moving it to APLAZADA. The body must carry
     * the ficha {@code id}.
     *
     * @param gradeIdVM the request body carrying the ficha id.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and body the updated
     *         ficha, {@code 400 (Bad Request)} if the id is invalid, no ficha matches it or
     *         the current state cannot be postponed, or {@code 403 (Forbidden)} for non-admin
     *         users.
     */
    @PatchMapping("/postponed")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<GradeDTO> postponeGrade(@Valid @RequestBody GradeIdVM gradeIdVM) {
        String id = gradeIdVM.getId();
        LOG.debug("REST request to postpone Grade : {}", id);
        GradeDTO gradeDTO = gradeService.postpone(id);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createAlert(applicationName, "grade.postponed", id))
            .body(gradeDTO);
    }

    /**
     * {@code PATCH  /grades/resumed} : Resume the postponed ficha identified by {@code id}.
     * Its state is recomputed from its date range. The body must carry the ficha {@code id}.
     *
     * @param gradeIdVM the request body carrying the ficha id.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and body the updated
     *         ficha, {@code 400 (Bad Request)} if the id is invalid, no ficha matches it or
     *         the ficha is not postponed, or {@code 403 (Forbidden)} for non-admin users.
     */
    @PatchMapping("/resumed")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<GradeDTO> resumeGrade(@Valid @RequestBody GradeIdVM gradeIdVM) {
        String id = gradeIdVM.getId();
        LOG.debug("REST request to resume Grade : {}", id);
        GradeDTO gradeDTO = gradeService.resume(id);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createAlert(applicationName, "grade.resumed", id))
            .body(gradeDTO);
    }

    /**
     * {@code PATCH  /grades/cancelled} : Cancel the ficha identified by {@code id}. Any ficha
     * that is not already cancelled moves to CANCELADA, a definitive state. The body must
     * carry the ficha {@code id}.
     *
     * @param gradeIdVM the request body carrying the ficha id.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and body the updated
     *         ficha, {@code 400 (Bad Request)} if the id is invalid, no ficha matches it or
     *         the ficha is already cancelled, or {@code 403 (Forbidden)} for non-admin users.
     */
    @PatchMapping("/cancelled")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<GradeDTO> cancelGrade(@Valid @RequestBody GradeIdVM gradeIdVM) {
        String id = gradeIdVM.getId();
        LOG.debug("REST request to cancel Grade : {}", id);
        GradeDTO gradeDTO = gradeService.cancel(id);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createAlert(applicationName, "grade.cancelled", id))
            .body(gradeDTO);
    }

    /**
     * {@code GET  /grades} : get all the Grades.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Grades in body.
     */
    @GetMapping("")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<List<GradeDTO>> getAllGrades(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of Grades");
        Page<GradeDTO> page;
        if (eagerload) {
            page = gradeService.findAllWithEagerRelationships(pageable);
        } else {
            page = gradeService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /grades/active} : get all the active Grades.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of active Grades in body.
     */
    @GetMapping("/active")
    //@PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<List<GradeDTO>> getActiveGrades() {
        LOG.debug("REST request to get all active Grades");
        List<GradeDTO> activeGrades = gradeService.findActiveGrades();
        return ResponseEntity.ok().body(activeGrades);
    }

    /**
     * {@code GET  /grades/:id} : get the "id" grade.
     *
     * @param id the id of the gradeDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the gradeDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<GradeDTO> getGrade(@PathVariable("id") String id) {
        LOG.debug("REST request to get Grade : {}", id);
        Optional<GradeDTO> gradeDTO = gradeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(gradeDTO);
    }

    /**
     * {@code DELETE  /grades/:id} : delete the "id" grade.
     *
     * @param id the id of the gradeDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteGrade(@PathVariable("id") String id) {
        LOG.debug("REST request to delete Grade : {}", id);
        gradeService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id))
            .build();
    }
}
