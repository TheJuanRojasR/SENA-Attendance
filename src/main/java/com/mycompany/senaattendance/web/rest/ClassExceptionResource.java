package com.mycompany.senaattendance.web.rest;

import com.mycompany.senaattendance.repository.ClassExceptionRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.ClassExceptionService;
import com.mycompany.senaattendance.service.dto.ClassExceptionDTO;
import com.mycompany.senaattendance.web.rest.errors.BadRequestAlertException;
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
 * REST controller for managing {@link com.mycompany.senaattendance.domain.ClassException}.
 */
@RestController
@RequestMapping("/api/class-exceptions")
public class ClassExceptionResource {

    private static final Logger LOG = LoggerFactory.getLogger(ClassExceptionResource.class);

    private static final String ENTITY_NAME = "classException";

    @Value("${jhipster.clientApp.name:senaAttendance}")
    private String applicationName;

    private final ClassExceptionService classExceptionService;

    private final ClassExceptionRepository classExceptionRepository;

    public ClassExceptionResource(ClassExceptionService classExceptionService, ClassExceptionRepository classExceptionRepository) {
        this.classExceptionService = classExceptionService;
        this.classExceptionRepository = classExceptionRepository;
    }

    /**
     * {@code POST  /class-exceptions} : Create a new classException. An administrator creates it
     * for any materia and an instructor only for a materia assigned to them.
     *
     * @param classExceptionDTO the classExceptionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new classExceptionDTO, or with status {@code 400 (Bad Request)} if the classException has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<ClassExceptionDTO> createClassException(@Valid @RequestBody ClassExceptionDTO classExceptionDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save ClassException : {}", classExceptionDTO);
        if (classExceptionDTO.getId() != null) {
            throw new BadRequestAlertException("A new classException cannot already have an ID", ENTITY_NAME, "idexists");
        }
        classExceptionDTO = classExceptionService.save(classExceptionDTO);
        return ResponseEntity.created(new URI("/api/class-exceptions/" + classExceptionDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, classExceptionDTO.getId()))
            .body(classExceptionDTO);
    }

    /**
     * {@code PUT  /class-exceptions} : Updates an existing classException; the id is taken from the request body.
     * An instructor can only update the exceptions of a materia assigned to them, and a past
     * non-teaching date only accepts a new reason.
     *
     * @param classExceptionDTO the classExceptionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated classExceptionDTO,
     * or with status {@code 400 (Bad Request)} if the classExceptionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the classExceptionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<ClassExceptionDTO> updateClassException(@Valid @RequestBody ClassExceptionDTO classExceptionDTO)
        throws URISyntaxException {
        String id = classExceptionDTO.getId();
        LOG.debug("REST request to update ClassException : {}", classExceptionDTO);
        if (id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        if (!classExceptionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        classExceptionDTO = classExceptionService.update(classExceptionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, classExceptionDTO.getId()))
            .body(classExceptionDTO);
    }

    /**
     * {@code PATCH  /class-exceptions} : Partial updates given fields of an existing classException, field will ignore if it is null.
     * An instructor can only update the exceptions of a materia assigned to them, and a past
     * non-teaching date only accepts a new reason.
     * The id is taken from the request body.
     *
     * @param classExceptionDTO the classExceptionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated classExceptionDTO,
     * or with status {@code 400 (Bad Request)} if the classExceptionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the classExceptionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the classExceptionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<ClassExceptionDTO> partialUpdateClassException(@NotNull @RequestBody ClassExceptionDTO classExceptionDTO)
        throws URISyntaxException {
        String id = classExceptionDTO.getId();
        LOG.debug("REST request to partial update ClassException partially : {}", classExceptionDTO);
        if (id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        if (!classExceptionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ClassExceptionDTO> result = classExceptionService.partialUpdate(classExceptionDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, classExceptionDTO.getId())
        );
    }

    /**
     * {@code GET  /class-exceptions} : get all the Class Exceptions the current user can read. An
     * administrator reads every exception and an instructor only the exceptions of the class
     * sections assigned to them.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Class Exceptions in body.
     */
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<List<ClassExceptionDTO>> getAllClassExceptions(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of ClassExceptions");
        Page<ClassExceptionDTO> page;
        if (eagerload) {
            page = classExceptionService.findAllWithEagerRelationshipsForCurrentUser(pageable);
        } else {
            page = classExceptionService.findAllForCurrentUser(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /class-exceptions/:id} : get the "id" classException when the current user can
     * read it. An administrator reads every exception and an instructor only the exceptions of the
     * class sections assigned to them, so an exception outside that scope resolves as not found.
     *
     * @param id the id of the classExceptionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the classExceptionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<ClassExceptionDTO> getClassException(@PathVariable("id") String id) {
        LOG.debug("REST request to get ClassException : {}", id);
        Optional<ClassExceptionDTO> classExceptionDTO = classExceptionService.findOneForCurrentUser(id);
        return ResponseUtil.wrapOrNotFound(classExceptionDTO);
    }

    /**
     * {@code DELETE  /class-exceptions/:id} : delete the "id" classException. An instructor can
     * only delete the exceptions of a materia assigned to them, and a past non-teaching date is a
     * precedent that is never removed.
     *
     * @param id the id of the classExceptionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Void> deleteClassException(@PathVariable("id") String id) {
        LOG.debug("REST request to delete ClassException : {}", id);
        classExceptionService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id))
            .build();
    }
}
