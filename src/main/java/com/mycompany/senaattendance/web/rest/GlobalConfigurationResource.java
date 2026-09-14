package com.mycompany.senaattendance.web.rest;

import com.mycompany.senaattendance.domain.GlobalConfiguration;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.GlobalConfigurationService;
import com.mycompany.senaattendance.service.dto.GlobalConfigurationDTO;
import com.mycompany.senaattendance.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
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
 * REST controller for the singleton {@link com.mycompany.senaattendance.domain.GlobalConfiguration}.
 */
@RestController
@RequestMapping("/api/global-configurations")
public class GlobalConfigurationResource {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalConfigurationResource.class);

    private static final String ENTITY_NAME = "globalConfiguration";

    @Value("${jhipster.clientApp.name:senaAttendance}")
    private String applicationName;

    private final GlobalConfigurationService globalConfigurationService;

    public GlobalConfigurationResource(GlobalConfigurationService globalConfigurationService) {
        this.globalConfigurationService = globalConfigurationService;
    }

    /**
     * {@code PATCH  /global-configurations} : Partially updates the global configuration. The id is optional in the
     * request body: the endpoint always targets the singleton, so the id may be omitted or, when present, it must be
     * the fixed singleton id.
     *
     * @param globalConfigurationDTO the globalConfigurationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated globalConfigurationDTO,
     * or with status {@code 400 (Bad Request)} if the globalConfigurationDTO is not valid or carries a foreign id,
     * or with status {@code 403 (Forbidden)} if the current user is not an admin,
     * or with status {@code 404 (Not Found)} if the globalConfigurationDTO is not found.
     */
    @PatchMapping(value = "", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<GlobalConfigurationDTO> partialUpdateGlobalConfiguration(
        @Valid @RequestBody GlobalConfigurationDTO globalConfigurationDTO
    ) {
        LOG.debug("REST request to partially update GlobalConfiguration : {}", globalConfigurationDTO);
        if (globalConfigurationDTO.getId() != null && !GlobalConfiguration.GLOBAL_CONFIGURATION_ID.equals(globalConfigurationDTO.getId())) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idinvalid");
        }

        Optional<GlobalConfigurationDTO> result = globalConfigurationService.partialUpdate(globalConfigurationDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, GlobalConfiguration.GLOBAL_CONFIGURATION_ID)
        );
    }

    /**
     * {@code GET  /global-configurations} : get the global configuration. Returns the singleton object directly,
     * re-seeding it with the default values when it is missing.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the globalConfiguration in body,
     * or with status {@code 403 (Forbidden)} if the current user is not an admin.
     */
    @GetMapping("")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public GlobalConfigurationDTO getAllGlobalConfigurations() {
        LOG.debug("REST request to get the GlobalConfiguration");
        return globalConfigurationService.get();
    }
}
