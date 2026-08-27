package com.mycompany.senaattendance.web.rest;

import com.mycompany.senaattendance.service.UserManagementService;
import com.mycompany.senaattendance.service.dto.UserManagementDTO;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

@RestController
@RequestMapping("/api/admin")
public class UserManagementResource {

    private static final Logger LOG = LoggerFactory.getLogger(UserManagementResource.class);

    private final UserManagementService userManagementService;

    public UserManagementResource(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    /**
     * GET /api/admin/users/search : Search users by term
     *
     * Examples:
     * - GET /api/admin/users/search?search=carlos
     * - GET /api/admin/users/search?search=1029384756
     * - GET /api/admin/users/search?search=@sena.edu.co
     *
     * @param search   Search term
     * @param pageable Pagination information
     * @return List of users matching the search
     */
    @GetMapping("/users/search")
    @PreAuthorize("hasAuthority(\"ROLE_ADMIN\")")
    public ResponseEntity<List<UserManagementDTO>> searchUsers(
        @RequestParam String search,
        @RequestParam(required = false) Boolean status,
        @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to search users with term: {}, status: {}", search, status);

        Page<UserManagementDTO> page = userManagementService.searchUsers(search, status, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
