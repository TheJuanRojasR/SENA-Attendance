package com.mycompany.senaattendance.web.rest;

import com.mycompany.senaattendance.repository.JustificationTypeRepository;
import com.mycompany.senaattendance.service.JustificationTypeService;
import com.mycompany.senaattendance.service.dto.JustificationTypeDTO;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.senaattendance.domain.JustificationType}.
 */
@RestController
@RequestMapping("/api/justification-types")
public class JustificationTypeResource {

    private static final Logger LOG = LoggerFactory.getLogger(JustificationTypeResource.class);

    private static final String ENTITY_NAME = "justificationType";

    @Value("${jhipster.clientApp.name:senaAttendance}")
    private String applicationName;

    private final JustificationTypeService justificationTypeService;

    private final JustificationTypeRepository justificationTypeRepository;

    public JustificationTypeResource(
        JustificationTypeService justificationTypeService,
        JustificationTypeRepository justificationTypeRepository
    ) {
        this.justificationTypeService = justificationTypeService;
        this.justificationTypeRepository = justificationTypeRepository;
    }

    /**
     * {@code POST  /justification-types} : Create a new justificationType.
     *
     * @param justificationTypeDTO the justificationTypeDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new justificationTypeDTO, or with status {@code 400 (Bad Request)} if the justificationType has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<JustificationTypeDTO> createJustificationType(@Valid @RequestBody JustificationTypeDTO justificationTypeDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save JustificationType : {}", justificationTypeDTO);
        if (justificationTypeDTO.getId() != null) {
            throw new BadRequestAlertException("A new justificationType cannot already have an ID", ENTITY_NAME, "idexists");
        }
        justificationTypeDTO = justificationTypeService.save(justificationTypeDTO);
        return ResponseEntity.created(new URI("/api/justification-types/" + justificationTypeDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, justificationTypeDTO.getId()))
            .body(justificationTypeDTO);
    }

    /**
     * {@code PUT  /justification-types/:id} : Updates an existing justificationType.
     *
     * @param id the id of the justificationTypeDTO to save.
     * @param justificationTypeDTO the justificationTypeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated justificationTypeDTO,
     * or with status {@code 400 (Bad Request)} if the justificationTypeDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the justificationTypeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<JustificationTypeDTO> updateJustificationType(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody JustificationTypeDTO justificationTypeDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update JustificationType : {}, {}", id, justificationTypeDTO);
        if (justificationTypeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, justificationTypeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!justificationTypeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        justificationTypeDTO = justificationTypeService.update(justificationTypeDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, justificationTypeDTO.getId()))
            .body(justificationTypeDTO);
    }

    /**
     * {@code PATCH  /justification-types/:id} : Partial updates given fields of an existing justificationType, field will ignore if it is null
     *
     * @param id the id of the justificationTypeDTO to save.
     * @param justificationTypeDTO the justificationTypeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated justificationTypeDTO,
     * or with status {@code 400 (Bad Request)} if the justificationTypeDTO is not valid,
     * or with status {@code 404 (Not Found)} if the justificationTypeDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the justificationTypeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<JustificationTypeDTO> partialUpdateJustificationType(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody JustificationTypeDTO justificationTypeDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update JustificationType partially : {}, {}", id, justificationTypeDTO);
        if (justificationTypeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, justificationTypeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!justificationTypeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<JustificationTypeDTO> result = justificationTypeService.partialUpdate(justificationTypeDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, justificationTypeDTO.getId())
        );
    }

    /**
     * {@code GET  /justification-types} : get all the Justification Types.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Justification Types in body.
     */
    @GetMapping("")
    public List<JustificationTypeDTO> getAllJustificationTypes() {
        LOG.debug("REST request to get all JustificationTypes");
        return justificationTypeService.findAll();
    }

    /**
     * {@code GET  /justification-types/:id} : get the "id" justificationType.
     *
     * @param id the id of the justificationTypeDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the justificationTypeDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<JustificationTypeDTO> getJustificationType(@PathVariable("id") String id) {
        LOG.debug("REST request to get JustificationType : {}", id);
        Optional<JustificationTypeDTO> justificationTypeDTO = justificationTypeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(justificationTypeDTO);
    }

    /**
     * {@code DELETE  /justification-types/:id} : delete the "id" justificationType.
     *
     * @param id the id of the justificationTypeDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJustificationType(@PathVariable("id") String id) {
        LOG.debug("REST request to delete JustificationType : {}", id);
        justificationTypeService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id))
            .build();
    }
}
