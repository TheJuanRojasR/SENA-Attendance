package com.mycompany.senaattendance.web.rest;

import com.mycompany.senaattendance.repository.GlobalConfigurationRepository;
import com.mycompany.senaattendance.service.GlobalConfigurationService;
import com.mycompany.senaattendance.service.dto.GlobalConfigurationDTO;
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
 * REST controller for managing {@link com.mycompany.senaattendance.domain.GlobalConfiguration}.
 */
@RestController
@RequestMapping("/api/global-configurations")
public class GlobalConfigurationResource {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalConfigurationResource.class);

    private static final String ENTITY_NAME = "globalConfiguration";

    @Value("${jhipster.clientApp.name:senaAttendance}")
    private String applicationName;

    private final GlobalConfigurationService globalConfigurationService;

    private final GlobalConfigurationRepository globalConfigurationRepository;

    public GlobalConfigurationResource(
        GlobalConfigurationService globalConfigurationService,
        GlobalConfigurationRepository globalConfigurationRepository
    ) {
        this.globalConfigurationService = globalConfigurationService;
        this.globalConfigurationRepository = globalConfigurationRepository;
    }

    /**
     * {@code POST  /global-configurations} : Create a new globalConfiguration.
     *
     * @param globalConfigurationDTO the globalConfigurationDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new globalConfigurationDTO, or with status {@code 400 (Bad Request)} if the globalConfiguration has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<GlobalConfigurationDTO> createGlobalConfiguration(
        @Valid @RequestBody GlobalConfigurationDTO globalConfigurationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to save GlobalConfiguration : {}", globalConfigurationDTO);
        if (globalConfigurationDTO.getId() != null) {
            throw new BadRequestAlertException("A new globalConfiguration cannot already have an ID", ENTITY_NAME, "idexists");
        }
        globalConfigurationDTO = globalConfigurationService.save(globalConfigurationDTO);
        return ResponseEntity.created(new URI("/api/global-configurations/" + globalConfigurationDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, globalConfigurationDTO.getId()))
            .body(globalConfigurationDTO);
    }

    /**
     * {@code PUT  /global-configurations/:id} : Updates an existing globalConfiguration.
     *
     * @param id the id of the globalConfigurationDTO to save.
     * @param globalConfigurationDTO the globalConfigurationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated globalConfigurationDTO,
     * or with status {@code 400 (Bad Request)} if the globalConfigurationDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the globalConfigurationDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<GlobalConfigurationDTO> updateGlobalConfiguration(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody GlobalConfigurationDTO globalConfigurationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update GlobalConfiguration : {}, {}", id, globalConfigurationDTO);
        if (globalConfigurationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, globalConfigurationDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!globalConfigurationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        globalConfigurationDTO = globalConfigurationService.update(globalConfigurationDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, globalConfigurationDTO.getId()))
            .body(globalConfigurationDTO);
    }

    /**
     * {@code PATCH  /global-configurations/:id} : Partial updates given fields of an existing globalConfiguration, field will ignore if it is null
     *
     * @param id the id of the globalConfigurationDTO to save.
     * @param globalConfigurationDTO the globalConfigurationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated globalConfigurationDTO,
     * or with status {@code 400 (Bad Request)} if the globalConfigurationDTO is not valid,
     * or with status {@code 404 (Not Found)} if the globalConfigurationDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the globalConfigurationDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<GlobalConfigurationDTO> partialUpdateGlobalConfiguration(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody GlobalConfigurationDTO globalConfigurationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update GlobalConfiguration partially : {}, {}", id, globalConfigurationDTO);
        if (globalConfigurationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, globalConfigurationDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!globalConfigurationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<GlobalConfigurationDTO> result = globalConfigurationService.partialUpdate(globalConfigurationDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, globalConfigurationDTO.getId())
        );
    }

    /**
     * {@code GET  /global-configurations} : get all the Global Configurations.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Global Configurations in body.
     */
    @GetMapping("")
    public List<GlobalConfigurationDTO> getAllGlobalConfigurations() {
        LOG.debug("REST request to get all GlobalConfigurations");
        return globalConfigurationService.findAll();
    }

    /**
     * {@code GET  /global-configurations/:id} : get the "id" globalConfiguration.
     *
     * @param id the id of the globalConfigurationDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the globalConfigurationDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<GlobalConfigurationDTO> getGlobalConfiguration(@PathVariable("id") String id) {
        LOG.debug("REST request to get GlobalConfiguration : {}", id);
        Optional<GlobalConfigurationDTO> globalConfigurationDTO = globalConfigurationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(globalConfigurationDTO);
    }

    /**
     * {@code DELETE  /global-configurations/:id} : delete the "id" globalConfiguration.
     *
     * @param id the id of the globalConfigurationDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGlobalConfiguration(@PathVariable("id") String id) {
        LOG.debug("REST request to delete GlobalConfiguration : {}", id);
        globalConfigurationService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id))
            .build();
    }
}
