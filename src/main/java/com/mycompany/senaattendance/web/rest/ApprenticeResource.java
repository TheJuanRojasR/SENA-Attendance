package com.mycompany.senaattendance.web.rest;

import com.mycompany.senaattendance.repository.ApprenticeRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.ApprenticeService;
import com.mycompany.senaattendance.service.dto.ApprenticeDTO;
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
 * REST controller for managing {@link com.mycompany.senaattendance.domain.Apprentice}.
 */
@RestController
@RequestMapping("/api/apprentices")
public class ApprenticeResource {

    private static final Logger LOG = LoggerFactory.getLogger(ApprenticeResource.class);

    private static final String ENTITY_NAME = "apprentice";

    @Value("${jhipster.clientApp.name:senaAttendance}")
    private String applicationName;

    private final ApprenticeService apprenticeService;

    private final ApprenticeRepository apprenticeRepository;

    public ApprenticeResource(ApprenticeService apprenticeService, ApprenticeRepository apprenticeRepository) {
        this.apprenticeService = apprenticeService;
        this.apprenticeRepository = apprenticeRepository;
    }

    /**
     * {@code POST  /apprentices} : Create a new apprentice.
     *
     * @param apprenticeDTO the apprenticeDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new apprenticeDTO, or with status {@code 400 (Bad Request)} if the apprentice has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    @PreAuthorize(
        "hasAuthority(\"" +
            AuthoritiesConstants.ADMIN +
            "\") or hasAuthority(\"" +
            AuthoritiesConstants.COORDINATOR +
            "\") or hasAuthority(\"" +
            AuthoritiesConstants.INSTRUCTOR +
            "\")"
    )
    public ResponseEntity<ApprenticeDTO> createApprentice(@Valid @RequestBody ApprenticeDTO apprenticeDTO) throws URISyntaxException {
        LOG.debug("REST request to save Apprentice : {}", apprenticeDTO);
        if (apprenticeDTO.getId() != null) {
            throw new BadRequestAlertException("A new apprentice cannot already have an ID", ENTITY_NAME, "idexists");
        }
        apprenticeDTO = apprenticeService.save(apprenticeDTO);
        return ResponseEntity.created(new URI("/api/apprentices/" + apprenticeDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, apprenticeDTO.getId()))
            .body(apprenticeDTO);
    }

    /**
     * {@code PUT  /apprentices/:id} : Updates an existing apprentice.
     *
     * @param id the id of the apprenticeDTO to save.
     * @param apprenticeDTO the apprenticeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated apprenticeDTO,
     * or with status {@code 400 (Bad Request)} if the apprenticeDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the apprenticeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    @PreAuthorize(
        "hasAuthority(\"" +
            AuthoritiesConstants.ADMIN +
            "\") or hasAuthority(\"" +
            AuthoritiesConstants.COORDINATOR +
            "\") or hasAuthority(\"" +
            AuthoritiesConstants.INSTRUCTOR +
            "\")"
    )
    public ResponseEntity<ApprenticeDTO> updateApprentice(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody ApprenticeDTO apprenticeDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Apprentice : {}, {}", id, apprenticeDTO);
        if (apprenticeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, apprenticeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!apprenticeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        apprenticeDTO = apprenticeService.update(apprenticeDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, apprenticeDTO.getId()))
            .body(apprenticeDTO);
    }

    /**
     * {@code PATCH  /apprentices/:id} : Partial updates given fields of an existing apprentice, field will ignore if it is null
     *
     * @param id the id of the apprenticeDTO to save.
     * @param apprenticeDTO the apprenticeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated apprenticeDTO,
     * or with status {@code 400 (Bad Request)} if the apprenticeDTO is not valid,
     * or with status {@code 404 (Not Found)} if the apprenticeDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the apprenticeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize(
        "hasAuthority(\"" +
            AuthoritiesConstants.ADMIN +
            "\") or hasAuthority(\"" +
            AuthoritiesConstants.COORDINATOR +
            "\") or hasAuthority(\"" +
            AuthoritiesConstants.INSTRUCTOR +
            "\")"
    )
    public ResponseEntity<ApprenticeDTO> partialUpdateApprentice(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody ApprenticeDTO apprenticeDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Apprentice partially : {}, {}", id, apprenticeDTO);
        if (apprenticeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, apprenticeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!apprenticeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ApprenticeDTO> result = apprenticeService.partialUpdate(apprenticeDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, apprenticeDTO.getId())
        );
    }

    /**
     * {@code GET  /apprentices} : get all the Apprentices.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Apprentices in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ApprenticeDTO>> getAllApprentices(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of Apprentices");
        Page<ApprenticeDTO> page;
        if (eagerload) {
            page = apprenticeService.findAllWithEagerRelationships(pageable);
        } else {
            page = apprenticeService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /apprentices/:id} : get the "id" apprentice.
     *
     * @param id the id of the apprenticeDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the apprenticeDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApprenticeDTO> getApprentice(@PathVariable("id") String id) {
        LOG.debug("REST request to get Apprentice : {}", id);
        Optional<ApprenticeDTO> apprenticeDTO = apprenticeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(apprenticeDTO);
    }

    /**
     * {@code DELETE  /apprentices/:id} : delete the "id" apprentice.
     *
     * @param id the id of the apprenticeDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize(
        "hasAuthority(\"" +
            AuthoritiesConstants.ADMIN +
            "\") or hasAuthority(\"" +
            AuthoritiesConstants.COORDINATOR +
            "\") or hasAuthority(\"" +
            AuthoritiesConstants.INSTRUCTOR +
            "\")"
    )
    public ResponseEntity<Void> deleteApprentice(@PathVariable("id") String id) {
        LOG.debug("REST request to delete Apprentice : {}", id);
        apprenticeService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id))
            .build();
    }
}
