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
     * {@code POST  /class-exceptions} : Create a new classException.
     *
     * @param classExceptionDTO the classExceptionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new classExceptionDTO, or with status {@code 400 (Bad Request)} if the classException has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.COORDINATOR + "\")")
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
     * {@code PUT  /class-exceptions/:id} : Updates an existing classException.
     *
     * @param id the id of the classExceptionDTO to save.
     * @param classExceptionDTO the classExceptionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated classExceptionDTO,
     * or with status {@code 400 (Bad Request)} if the classExceptionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the classExceptionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.COORDINATOR + "\")")
    public ResponseEntity<ClassExceptionDTO> updateClassException(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody ClassExceptionDTO classExceptionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update ClassException : {}, {}", id, classExceptionDTO);
        if (classExceptionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, classExceptionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
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
     * {@code PATCH  /class-exceptions/:id} : Partial updates given fields of an existing classException, field will ignore if it is null
     *
     * @param id the id of the classExceptionDTO to save.
     * @param classExceptionDTO the classExceptionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated classExceptionDTO,
     * or with status {@code 400 (Bad Request)} if the classExceptionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the classExceptionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the classExceptionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.COORDINATOR + "\")")
    public ResponseEntity<ClassExceptionDTO> partialUpdateClassException(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody ClassExceptionDTO classExceptionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update ClassException partially : {}, {}", id, classExceptionDTO);
        if (classExceptionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, classExceptionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
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
     * {@code GET  /class-exceptions} : get all the Class Exceptions.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Class Exceptions in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ClassExceptionDTO>> getAllClassExceptions(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of ClassExceptions");
        Page<ClassExceptionDTO> page;
        if (eagerload) {
            page = classExceptionService.findAllWithEagerRelationships(pageable);
        } else {
            page = classExceptionService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /class-exceptions/:id} : get the "id" classException.
     *
     * @param id the id of the classExceptionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the classExceptionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClassExceptionDTO> getClassException(@PathVariable("id") String id) {
        LOG.debug("REST request to get ClassException : {}", id);
        Optional<ClassExceptionDTO> classExceptionDTO = classExceptionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(classExceptionDTO);
    }

    /**
     * {@code DELETE  /class-exceptions/:id} : delete the "id" classException.
     *
     * @param id the id of the classExceptionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.COORDINATOR + "\")")
    public ResponseEntity<Void> deleteClassException(@PathVariable("id") String id) {
        LOG.debug("REST request to delete ClassException : {}", id);
        classExceptionService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id))
            .build();
    }
}
