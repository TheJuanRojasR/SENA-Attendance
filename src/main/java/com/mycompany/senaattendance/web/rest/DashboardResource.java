package com.mycompany.senaattendance.web.rest;

import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.DashboardService;
import com.mycompany.senaattendance.service.dto.dashboard.DashboardDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the role-based dashboard.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardResource {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardResource.class);

    private final DashboardService dashboardService;

    public DashboardResource(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * {@code GET /dashboard} : get the dashboard data for the current user's role.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the dashboard payload in body.
     */
    @GetMapping("")
    @PreAuthorize(
        "hasAuthority(\"" +
            AuthoritiesConstants.ADMIN +
            "\") " +
            "or hasAuthority(\"" +
            AuthoritiesConstants.INSTRUCTOR +
            "\") " +
            "or hasAuthority(\"" +
            AuthoritiesConstants.APPRENTICE +
            "\")"
    )
    public ResponseEntity<DashboardDTO> getDashboard() {
        LOG.debug("REST request to get Dashboard for current user");

        DashboardDTO dashboard = dashboardService.getDashboardForCurrentUser();
        return ResponseEntity.ok().body(dashboard);
    }
}
