package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.service.dto.UserManagementDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Searches for users based on the provided term and returns the results in a paginated form according to the given paging configuration.
 *
 * @param searchTerm the text used to filter the users; may be empty or null
 * @param status     the activation status filter (true=active, false=inactive, null=all)
 * @param pageable   the pagination and sorting information for the result set
 * @return a paginated list of user management DTOs matching the search criteria
 */
public interface UserManagementService {
    Page<UserManagementDTO> searchUsers(String searchTerm, Boolean status, Pageable pageable);
}
