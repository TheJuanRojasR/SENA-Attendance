package com.mycompany.senaattendance.web.rest;

import com.mycompany.senaattendance.repository.TrimesterRepository;
import com.mycompany.senaattendance.service.TrimesterService;
import com.mycompany.senaattendance.service.dto.TrimesterDTO;
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
 * REST controller for managing {@link com.mycompany.senaattendance.domain.Trimester}.
 */
@RestController
@RequestMapping("/api/trimesters")
public class TrimesterResource {

    private static final Logger LOG = LoggerFactory.getLogger(TrimesterResource.class);

    private static final String ENTITY_NAME = "trimester";

    @Value("${jhipster.clientApp.name:senaAttendance}")
    private String applicationName;

    private final TrimesterService trimesterService;

    private final TrimesterRepository trimesterRepository;

    public TrimesterResource(TrimesterService trimesterService, TrimesterRepository trimesterRepository) {
        this.trimesterService = trimesterService;
        this.trimesterRepository = trimesterRepository;
    }

    /**
     * {@code POST  /trimesters} : Create a new trimester.
     *
     * @param trimesterDTO the trimesterDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new trimesterDTO, or with status {@code 400 (Bad Request)} if the trimester has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<TrimesterDTO> createTrimester(@Valid @RequestBody TrimesterDTO trimesterDTO) throws URISyntaxException {
        LOG.debug("REST request to save Trimester : {}", trimesterDTO);
        if (trimesterDTO.getId() != null) {
            throw new BadRequestAlertException("A new trimester cannot already have an ID", ENTITY_NAME, "idexists");
        }
        trimesterDTO = trimesterService.save(trimesterDTO);
        return ResponseEntity.created(new URI("/api/trimesters/" + trimesterDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, trimesterDTO.getId()))
            .body(trimesterDTO);
    }

    /**
     * {@code PUT  /trimesters/:id} : Updates an existing trimester.
     *
     * @param id the id of the trimesterDTO to save.
     * @param trimesterDTO the trimesterDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated trimesterDTO,
     * or with status {@code 400 (Bad Request)} if the trimesterDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the trimesterDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TrimesterDTO> updateTrimester(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody TrimesterDTO trimesterDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Trimester : {}, {}", id, trimesterDTO);
        if (trimesterDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, trimesterDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!trimesterRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        trimesterDTO = trimesterService.update(trimesterDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, trimesterDTO.getId()))
            .body(trimesterDTO);
    }

    /**
     * {@code PATCH  /trimesters/:id} : Partial updates given fields of an existing trimester, field will ignore if it is null
     *
     * @param id the id of the trimesterDTO to save.
     * @param trimesterDTO the trimesterDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated trimesterDTO,
     * or with status {@code 400 (Bad Request)} if the trimesterDTO is not valid,
     * or with status {@code 404 (Not Found)} if the trimesterDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the trimesterDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<TrimesterDTO> partialUpdateTrimester(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody TrimesterDTO trimesterDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Trimester partially : {}, {}", id, trimesterDTO);
        if (trimesterDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, trimesterDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!trimesterRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<TrimesterDTO> result = trimesterService.partialUpdate(trimesterDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, trimesterDTO.getId())
        );
    }

    /**
     * {@code GET  /trimesters} : get all the Trimesters.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Trimesters in body.
     */
    @GetMapping("")
    public ResponseEntity<List<TrimesterDTO>> getAllTrimesters(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of Trimesters");
        Page<TrimesterDTO> page = trimesterService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /trimesters/:id} : get the "id" trimester.
     *
     * @param id the id of the trimesterDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the trimesterDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TrimesterDTO> getTrimester(@PathVariable("id") String id) {
        LOG.debug("REST request to get Trimester : {}", id);
        Optional<TrimesterDTO> trimesterDTO = trimesterService.findOne(id);
        return ResponseUtil.wrapOrNotFound(trimesterDTO);
    }

    /**
     * {@code DELETE  /trimesters/:id} : delete the "id" trimester.
     *
     * @param id the id of the trimesterDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrimester(@PathVariable("id") String id) {
        LOG.debug("REST request to delete Trimester : {}", id);
        trimesterService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id))
            .build();
    }
}
