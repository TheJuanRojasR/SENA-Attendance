package com.mycompany.senaattendance.web.rest;

import com.mycompany.senaattendance.repository.JustificationDetailsRepository;
import com.mycompany.senaattendance.service.JustificationDetailsService;
import com.mycompany.senaattendance.service.dto.JustificationDetailsDTO;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.senaattendance.domain.JustificationDetails}.
 */
@RestController
@RequestMapping("/api/justification-details")
public class JustificationDetailsResource {

    private static final Logger LOG = LoggerFactory.getLogger(JustificationDetailsResource.class);

    private static final String ENTITY_NAME = "justificationDetails";

    @Value("${jhipster.clientApp.name:senaAttendance}")
    private String applicationName;

    private final JustificationDetailsService justificationDetailsService;

    private final JustificationDetailsRepository justificationDetailsRepository;

    public JustificationDetailsResource(
        JustificationDetailsService justificationDetailsService,
        JustificationDetailsRepository justificationDetailsRepository
    ) {
        this.justificationDetailsService = justificationDetailsService;
        this.justificationDetailsRepository = justificationDetailsRepository;
    }

    /**
     * {@code POST  /justification-details} : Create a new justificationDetails.
     *
     * @param justificationDetailsDTO the justificationDetailsDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new justificationDetailsDTO, or with status {@code 400 (Bad Request)} if the justificationDetails has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<JustificationDetailsDTO> createJustificationDetails(
        @Valid @RequestBody JustificationDetailsDTO justificationDetailsDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to save JustificationDetails : {}", justificationDetailsDTO);
        if (justificationDetailsDTO.getId() != null) {
            throw new BadRequestAlertException("A new justificationDetails cannot already have an ID", ENTITY_NAME, "idexists");
        }
        justificationDetailsDTO = justificationDetailsService.save(justificationDetailsDTO);
        return ResponseEntity.created(new URI("/api/justification-details/" + justificationDetailsDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, justificationDetailsDTO.getId()))
            .body(justificationDetailsDTO);
    }

    /**
     * {@code PUT  /justification-details/:id} : Updates an existing justificationDetails.
     *
     * @param id the id of the justificationDetailsDTO to save.
     * @param justificationDetailsDTO the justificationDetailsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated justificationDetailsDTO,
     * or with status {@code 400 (Bad Request)} if the justificationDetailsDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the justificationDetailsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<JustificationDetailsDTO> updateJustificationDetails(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody JustificationDetailsDTO justificationDetailsDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update JustificationDetails : {}, {}", id, justificationDetailsDTO);
        if (justificationDetailsDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, justificationDetailsDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!justificationDetailsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        justificationDetailsDTO = justificationDetailsService.update(justificationDetailsDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, justificationDetailsDTO.getId()))
            .body(justificationDetailsDTO);
    }

    /**
     * {@code PATCH  /justification-details/:id} : Partial updates given fields of an existing justificationDetails, field will ignore if it is null
     *
     * @param id the id of the justificationDetailsDTO to save.
     * @param justificationDetailsDTO the justificationDetailsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated justificationDetailsDTO,
     * or with status {@code 400 (Bad Request)} if the justificationDetailsDTO is not valid,
     * or with status {@code 404 (Not Found)} if the justificationDetailsDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the justificationDetailsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<JustificationDetailsDTO> partialUpdateJustificationDetails(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody JustificationDetailsDTO justificationDetailsDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update JustificationDetails partially : {}, {}", id, justificationDetailsDTO);
        if (justificationDetailsDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, justificationDetailsDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!justificationDetailsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<JustificationDetailsDTO> result = justificationDetailsService.partialUpdate(justificationDetailsDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, justificationDetailsDTO.getId())
        );
    }

    /**
     * {@code GET  /justification-details} : get all the Justification Details.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Justification Details in body.
     */
    @GetMapping("")
    public ResponseEntity<List<JustificationDetailsDTO>> getAllJustificationDetailses(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of JustificationDetailses");
        Page<JustificationDetailsDTO> page;
        if (eagerload) {
            page = justificationDetailsService.findAllWithEagerRelationships(pageable);
        } else {
            page = justificationDetailsService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /justification-details/:id} : get the "id" justificationDetails.
     *
     * @param id the id of the justificationDetailsDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the justificationDetailsDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<JustificationDetailsDTO> getJustificationDetails(@PathVariable("id") String id) {
        LOG.debug("REST request to get JustificationDetails : {}", id);
        Optional<JustificationDetailsDTO> justificationDetailsDTO = justificationDetailsService.findOne(id);
        return ResponseUtil.wrapOrNotFound(justificationDetailsDTO);
    }

    /**
     * {@code DELETE  /justification-details/:id} : delete the "id" justificationDetails.
     *
     * @param id the id of the justificationDetailsDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJustificationDetails(@PathVariable("id") String id) {
        LOG.debug("REST request to delete JustificationDetails : {}", id);
        justificationDetailsService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id))
            .build();
    }
}
