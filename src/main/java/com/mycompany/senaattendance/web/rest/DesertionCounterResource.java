package com.mycompany.senaattendance.web.rest;

import com.mycompany.senaattendance.repository.DesertionCounterRepository;
import com.mycompany.senaattendance.service.DesertionCounterService;
import com.mycompany.senaattendance.service.dto.DesertionCounterDTO;
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
 * REST controller for managing {@link com.mycompany.senaattendance.domain.DesertionCounter}.
 */
@RestController
@RequestMapping("/api/desertion-counters")
public class DesertionCounterResource {

    private static final Logger LOG = LoggerFactory.getLogger(DesertionCounterResource.class);

    private static final String ENTITY_NAME = "desertionCounter";

    @Value("${jhipster.clientApp.name:senaAttendance}")
    private String applicationName;

    private final DesertionCounterService desertionCounterService;

    private final DesertionCounterRepository desertionCounterRepository;

    public DesertionCounterResource(
        DesertionCounterService desertionCounterService,
        DesertionCounterRepository desertionCounterRepository
    ) {
        this.desertionCounterService = desertionCounterService;
        this.desertionCounterRepository = desertionCounterRepository;
    }

    /**
     * {@code POST  /desertion-counters} : Create a new desertionCounter.
     *
     * @param desertionCounterDTO the desertionCounterDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new desertionCounterDTO, or with status {@code 400 (Bad Request)} if the desertionCounter has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<DesertionCounterDTO> createDesertionCounter(@Valid @RequestBody DesertionCounterDTO desertionCounterDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save DesertionCounter : {}", desertionCounterDTO);
        if (desertionCounterDTO.getId() != null) {
            throw new BadRequestAlertException("A new desertionCounter cannot already have an ID", ENTITY_NAME, "idexists");
        }
        desertionCounterDTO = desertionCounterService.save(desertionCounterDTO);
        return ResponseEntity.created(new URI("/api/desertion-counters/" + desertionCounterDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, desertionCounterDTO.getId()))
            .body(desertionCounterDTO);
    }

    /**
     * {@code PUT  /desertion-counters/:id} : Updates an existing desertionCounter.
     *
     * @param id the id of the desertionCounterDTO to save.
     * @param desertionCounterDTO the desertionCounterDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated desertionCounterDTO,
     * or with status {@code 400 (Bad Request)} if the desertionCounterDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the desertionCounterDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DesertionCounterDTO> updateDesertionCounter(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody DesertionCounterDTO desertionCounterDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update DesertionCounter : {}, {}", id, desertionCounterDTO);
        if (desertionCounterDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, desertionCounterDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!desertionCounterRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        desertionCounterDTO = desertionCounterService.update(desertionCounterDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, desertionCounterDTO.getId()))
            .body(desertionCounterDTO);
    }

    /**
     * {@code PATCH  /desertion-counters/:id} : Partial updates given fields of an existing desertionCounter, field will ignore if it is null
     *
     * @param id the id of the desertionCounterDTO to save.
     * @param desertionCounterDTO the desertionCounterDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated desertionCounterDTO,
     * or with status {@code 400 (Bad Request)} if the desertionCounterDTO is not valid,
     * or with status {@code 404 (Not Found)} if the desertionCounterDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the desertionCounterDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<DesertionCounterDTO> partialUpdateDesertionCounter(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody DesertionCounterDTO desertionCounterDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update DesertionCounter partially : {}, {}", id, desertionCounterDTO);
        if (desertionCounterDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, desertionCounterDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!desertionCounterRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<DesertionCounterDTO> result = desertionCounterService.partialUpdate(desertionCounterDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, desertionCounterDTO.getId())
        );
    }

    /**
     * {@code GET  /desertion-counters} : get all the Desertion Counters.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Desertion Counters in body.
     */
    @GetMapping("")
    public ResponseEntity<List<DesertionCounterDTO>> getAllDesertionCounters(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of DesertionCounters");
        Page<DesertionCounterDTO> page;
        if (eagerload) {
            page = desertionCounterService.findAllWithEagerRelationships(pageable);
        } else {
            page = desertionCounterService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /desertion-counters/:id} : get the "id" desertionCounter.
     *
     * @param id the id of the desertionCounterDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the desertionCounterDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DesertionCounterDTO> getDesertionCounter(@PathVariable("id") String id) {
        LOG.debug("REST request to get DesertionCounter : {}", id);
        Optional<DesertionCounterDTO> desertionCounterDTO = desertionCounterService.findOne(id);
        return ResponseUtil.wrapOrNotFound(desertionCounterDTO);
    }

    /**
     * {@code DELETE  /desertion-counters/:id} : delete the "id" desertionCounter.
     *
     * @param id the id of the desertionCounterDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDesertionCounter(@PathVariable("id") String id) {
        LOG.debug("REST request to delete DesertionCounter : {}", id);
        desertionCounterService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id))
            .build();
    }
}
