package com.mycompany.senaattendance.web.rest;

import com.mycompany.senaattendance.repository.ModalityRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.ModalityService;
import com.mycompany.senaattendance.service.dto.ModalityDTO;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.senaattendance.domain.Modality}.
 */
@RestController
@RequestMapping("/api/modalities")
public class ModalityResource {

    private static final Logger LOG = LoggerFactory.getLogger(ModalityResource.class);

    private static final String ENTITY_NAME = "modality";

    @Value("${jhipster.clientApp.name:senaAttendance}")
    private String applicationName;

    private final ModalityService modalityService;

    private final ModalityRepository modalityRepository;

    public ModalityResource(ModalityService modalityService, ModalityRepository modalityRepository) {
        this.modalityService = modalityService;
        this.modalityRepository = modalityRepository;
    }

    /**
     * {@code POST  /modalities} : Create a new modality.
     *
     * @param modalityDTO the modalityDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new modalityDTO, or with status {@code 400 (Bad Request)} if the modality has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.COORDINATOR + "\")")
    public ResponseEntity<ModalityDTO> createModality(@Valid @RequestBody ModalityDTO modalityDTO) throws URISyntaxException {
        LOG.debug("REST request to save Modality : {}", modalityDTO);
        if (modalityDTO.getId() != null) {
            throw new BadRequestAlertException("A new modality cannot already have an ID", ENTITY_NAME, "idexists");
        }
        modalityDTO = modalityService.save(modalityDTO);
        return ResponseEntity.created(new URI("/api/modalities/" + modalityDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, modalityDTO.getId()))
            .body(modalityDTO);
    }

    /**
     * {@code PUT  /modalities/:id} : Updates an existing modality.
     *
     * @param id the id of the modalityDTO to save.
     * @param modalityDTO the modalityDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated modalityDTO,
     * or with status {@code 400 (Bad Request)} if the modalityDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the modalityDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.COORDINATOR + "\")")
    public ResponseEntity<ModalityDTO> updateModality(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody ModalityDTO modalityDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Modality : {}, {}", id, modalityDTO);
        if (modalityDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, modalityDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!modalityRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        modalityDTO = modalityService.update(modalityDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, modalityDTO.getId()))
            .body(modalityDTO);
    }

    /**
     * {@code PATCH  /modalities/:id} : Partial updates given fields of an existing modality, field will ignore if it is null
     *
     * @param id the id of the modalityDTO to save.
     * @param modalityDTO the modalityDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated modalityDTO,
     * or with status {@code 400 (Bad Request)} if the modalityDTO is not valid,
     * or with status {@code 404 (Not Found)} if the modalityDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the modalityDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.COORDINATOR + "\")")
    public ResponseEntity<ModalityDTO> partialUpdateModality(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody ModalityDTO modalityDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Modality partially : {}, {}", id, modalityDTO);
        if (modalityDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, modalityDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!modalityRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ModalityDTO> result = modalityService.partialUpdate(modalityDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, modalityDTO.getId())
        );
    }

    /**
     * {@code GET  /modalities} : get all the Modalities.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Modalities in body.
     */
    @GetMapping("")
    public List<ModalityDTO> getAllModalities() {
        LOG.debug("REST request to get all Modalities");
        return modalityService.findAll();
    }

    /**
     * {@code GET  /modalities/:id} : get the "id" modality.
     *
     * @param id the id of the modalityDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the modalityDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ModalityDTO> getModality(@PathVariable("id") String id) {
        LOG.debug("REST request to get Modality : {}", id);
        Optional<ModalityDTO> modalityDTO = modalityService.findOne(id);
        return ResponseUtil.wrapOrNotFound(modalityDTO);
    }

    /**
     * {@code DELETE  /modalities/:id} : delete the "id" modality.
     *
     * @param id the id of the modalityDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\") or hasAuthority(\"" + AuthoritiesConstants.COORDINATOR + "\")")
    public ResponseEntity<Void> deleteModality(@PathVariable("id") String id) {
        LOG.debug("REST request to delete Modality : {}", id);
        modalityService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id))
            .build();
    }

    @GetMapping("/active")
    public List<ModalityDTO> getActiveModalities() {
        LOG.debug("REST request to get all active Modalities");
        return modalityService.findActiveModalities();
    }
}
