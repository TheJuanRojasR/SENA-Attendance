package com.mycompany.senaattendance.web.rest;

import com.mycompany.senaattendance.repository.JustificationRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.JustificationService;
import com.mycompany.senaattendance.service.dto.JustificationDTO;
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
 * REST controller for managing {@link com.mycompany.senaattendance.domain.Justification}.
 */
@RestController
@RequestMapping("/api/justifications")
public class JustificationResource {

    private static final Logger LOG = LoggerFactory.getLogger(JustificationResource.class);

    private static final String ENTITY_NAME = "justification";

    @Value("${jhipster.clientApp.name:senaAttendance}")
    private String applicationName;

    private final JustificationService justificationService;

    private final JustificationRepository justificationRepository;

    public JustificationResource(JustificationService justificationService, JustificationRepository justificationRepository) {
        this.justificationService = justificationService;
        this.justificationRepository = justificationRepository;
    }

    /**
     * {@code POST  /justifications} : Create a new justification.
     *
     * @param justificationDTO the justificationDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new justificationDTO, or with status {@code 400 (Bad Request)} if the justification has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    @PreAuthorize(
        "hasAuthority(\"" +
            AuthoritiesConstants.ADMIN +
            "\") or hasAuthority(\"" +
            AuthoritiesConstants.COORDINATOR +
            "\") or hasAuthority(\"" +
            AuthoritiesConstants.APPRENTICE +
            "\")"
    )
    public ResponseEntity<JustificationDTO> createJustification(@Valid @RequestBody JustificationDTO justificationDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save Justification : {}", justificationDTO);
        if (justificationDTO.getId() != null) {
            throw new BadRequestAlertException("A new justification cannot already have an ID", ENTITY_NAME, "idexists");
        }
        justificationDTO = justificationService.save(justificationDTO);
        return ResponseEntity.created(new URI("/api/justifications/" + justificationDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, justificationDTO.getId()))
            .body(justificationDTO);
    }

    /**
     * {@code PUT  /justifications/:id} : Updates an existing justification.
     *
     * @param id the id of the justificationDTO to save.
     * @param justificationDTO the justificationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated justificationDTO,
     * or with status {@code 400 (Bad Request)} if the justificationDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the justificationDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    @PreAuthorize(
        "hasAuthority(\"" +
            AuthoritiesConstants.ADMIN +
            "\") or hasAuthority(\"" +
            AuthoritiesConstants.COORDINATOR +
            "\") or hasAuthority(\"" +
            AuthoritiesConstants.APPRENTICE +
            "\")"
    )
    public ResponseEntity<JustificationDTO> updateJustification(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody JustificationDTO justificationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Justification : {}, {}", id, justificationDTO);
        if (justificationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, justificationDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!justificationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        justificationDTO = justificationService.update(justificationDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, justificationDTO.getId()))
            .body(justificationDTO);
    }

    /**
     * {@code PATCH  /justifications/:id} : Partial updates given fields of an existing justification, field will ignore if it is null
     *
     * @param id the id of the justificationDTO to save.
     * @param justificationDTO the justificationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated justificationDTO,
     * or with status {@code 400 (Bad Request)} if the justificationDTO is not valid,
     * or with status {@code 404 (Not Found)} if the justificationDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the justificationDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize(
        "hasAuthority(\"" +
            AuthoritiesConstants.ADMIN +
            "\") or hasAuthority(\"" +
            AuthoritiesConstants.COORDINATOR +
            "\") or hasAuthority(\"" +
            AuthoritiesConstants.APPRENTICE +
            "\")"
    )
    public ResponseEntity<JustificationDTO> partialUpdateJustification(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody JustificationDTO justificationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Justification partially : {}, {}", id, justificationDTO);
        if (justificationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, justificationDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!justificationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<JustificationDTO> result = justificationService.partialUpdate(justificationDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, justificationDTO.getId())
        );
    }

    /**
     * {@code GET  /justifications} : get all the Justifications.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Justifications in body.
     */
    @GetMapping("")
    public ResponseEntity<List<JustificationDTO>> getAllJustifications(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of Justifications");
        Page<JustificationDTO> page;
        if (eagerload) {
            page = justificationService.findAllWithEagerRelationships(pageable);
        } else {
            page = justificationService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /justifications/:id} : get the "id" justification.
     *
     * @param id the id of the justificationDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the justificationDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<JustificationDTO> getJustification(@PathVariable("id") String id) {
        LOG.debug("REST request to get Justification : {}", id);
        Optional<JustificationDTO> justificationDTO = justificationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(justificationDTO);
    }

    /**
     * {@code DELETE  /justifications/:id} : delete the "id" justification.
     *
     * @param id the id of the justificationDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize(
        "hasAuthority(\"" +
            AuthoritiesConstants.ADMIN +
            "\") or hasAuthority(\"" +
            AuthoritiesConstants.COORDINATOR +
            "\") or hasAuthority(\"" +
            AuthoritiesConstants.APPRENTICE +
            "\")"
    )
    public ResponseEntity<Void> deleteJustification(@PathVariable("id") String id) {
        LOG.debug("REST request to delete Justification : {}", id);
        justificationService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id))
            .build();
    }
}
