package com.mycompany.senaattendance.web.rest;

import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.GradeService;
import com.mycompany.senaattendance.service.dto.GradeDTO;
import com.mycompany.senaattendance.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
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
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.COORDINATOR + "\")")
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
     * {@code PUT  /grades/:id} : Updates an existing grade.
     *
     * @param id the id of the gradeDTO to save.
     * @param gradeDTO the gradeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated gradeDTO,
     * or with status {@code 400 (Bad Request)} if the gradeDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the gradeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.COORDINATOR + "\")")
    public ResponseEntity<GradeDTO> updateGrade(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody GradeDTO gradeDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Grade : {}, {}", id, gradeDTO);
        if (gradeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, gradeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
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
     * {@code PATCH  /grades/:id} : Partial updates given fields of an existing grade, field will ignore if it is null
     *
     * @param id the id of the gradeDTO to save.
     * @param gradeDTO the gradeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated gradeDTO,
     * or with status {@code 400 (Bad Request)} if the gradeDTO is not valid,
     * or with status {@code 404 (Not Found)} if the gradeDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the gradeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.COORDINATOR + "\")")
    public ResponseEntity<GradeDTO> partialUpdateGrade(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody GradeDTO gradeDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Grade partially : {}, {}", id, gradeDTO);
        if (gradeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, gradeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
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
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.COORDINATOR + "\")")
    public ResponseEntity<Void> deleteGrade(@PathVariable("id") String id) {
        LOG.debug("REST request to delete Grade : {}", id);
        gradeService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id))
            .build();
    }
}
