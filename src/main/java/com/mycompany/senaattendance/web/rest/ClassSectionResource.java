package com.mycompany.senaattendance.web.rest;

import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.ClassSectionService;
import com.mycompany.senaattendance.service.dto.ClassSectionDTO;
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
 * REST controller for managing {@link com.mycompany.senaattendance.domain.ClassSection}.
 */
@RestController
@RequestMapping("/api/class-sections")
public class ClassSectionResource {

    private static final Logger LOG = LoggerFactory.getLogger(ClassSectionResource.class);

    private static final String ENTITY_NAME = "classSection";

    @Value("${jhipster.clientApp.name:senaAttendance}")
    private String applicationName;

    private final ClassSectionService classSectionService;

    private final ClassSectionRepository classSectionRepository;

    public ClassSectionResource(ClassSectionService classSectionService, ClassSectionRepository classSectionRepository) {
        this.classSectionService = classSectionService;
        this.classSectionRepository = classSectionRepository;
    }

    /**
     * {@code POST  /class-sections} : Create a new classSection.
     *
     * @param classSectionDTO the classSectionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new classSectionDTO, or with status {@code 400 (Bad Request)} if the classSection has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.COORDINATOR + "\")")
    public ResponseEntity<ClassSectionDTO> createClassSection(@Valid @RequestBody ClassSectionDTO classSectionDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save ClassSection : {}", classSectionDTO);
        if (classSectionDTO.getId() != null) {
            throw new BadRequestAlertException("A new classSection cannot already have an ID", ENTITY_NAME, "idexists");
        }
        classSectionDTO = classSectionService.save(classSectionDTO);
        return ResponseEntity.created(new URI("/api/class-sections/" + classSectionDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, classSectionDTO.getId()))
            .body(classSectionDTO);
    }

    /**
     * {@code PUT  /class-sections/:id} : Updates an existing classSection.
     *
     * @param id the id of the classSectionDTO to save.
     * @param classSectionDTO the classSectionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated classSectionDTO,
     * or with status {@code 400 (Bad Request)} if the classSectionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the classSectionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.COORDINATOR + "\")")
    public ResponseEntity<ClassSectionDTO> updateClassSection(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody ClassSectionDTO classSectionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update ClassSection : {}, {}", id, classSectionDTO);
        if (classSectionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, classSectionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!classSectionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        classSectionDTO = classSectionService.update(classSectionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, classSectionDTO.getId()))
            .body(classSectionDTO);
    }

    /**
     * {@code PATCH  /class-sections/:id} : Partial updates given fields of an existing classSection, field will ignore if it is null
     *
     * @param id the id of the classSectionDTO to save.
     * @param classSectionDTO the classSectionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated classSectionDTO,
     * or with status {@code 400 (Bad Request)} if the classSectionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the classSectionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the classSectionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.COORDINATOR + "\")")
    public ResponseEntity<ClassSectionDTO> partialUpdateClassSection(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody ClassSectionDTO classSectionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update ClassSection partially : {}, {}", id, classSectionDTO);
        if (classSectionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, classSectionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!classSectionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ClassSectionDTO> result = classSectionService.partialUpdate(classSectionDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, classSectionDTO.getId())
        );
    }

    /**
     * {@code GET  /class-sections} : get all the Class Sections.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Class Sections in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ClassSectionDTO>> getAllClassSections(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of ClassSections");
        Page<ClassSectionDTO> page;
        if (eagerload) {
            page = classSectionService.findAllWithEagerRelationships(pageable);
        } else {
            page = classSectionService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /class-sections/:id} : get the "id" classSection.
     *
     * @param id the id of the classSectionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the classSectionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClassSectionDTO> getClassSection(@PathVariable("id") String id) {
        LOG.debug("REST request to get ClassSection : {}", id);
        Optional<ClassSectionDTO> classSectionDTO = classSectionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(classSectionDTO);
    }

    /**
     * {@code DELETE  /class-sections/:id} : delete the "id" classSection.
     *
     * @param id the id of the classSectionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.COORDINATOR + "\")")
    public ResponseEntity<Void> deleteClassSection(@PathVariable("id") String id) {
        LOG.debug("REST request to delete ClassSection : {}", id);
        classSectionService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id))
            .build();
    }
}
