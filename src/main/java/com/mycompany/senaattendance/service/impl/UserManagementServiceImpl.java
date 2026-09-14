package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.service.UserManagementService;
import com.mycompany.senaattendance.service.dto.UserManagementDTO;
import com.mycompany.senaattendance.service.mapper.UserManagementMapper;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserManagementMapper userManagementMapper;

    public UserManagementServiceImpl(
        UserRepository userRepository,
        UserProfileRepository userProfileRepository,
        UserManagementMapper userManagementMapper
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.userManagementMapper = userManagementMapper;
    }

    @Override
    public Page<UserManagementDTO> searchUsers(String searchTerm, Boolean status, String role, Pageable pageable) {
        String term = StringUtils.trimToEmpty(searchTerm).toLowerCase();

        // The match spans profile fields and the user email, so it is resolved against the whole
        // collection and then sliced for the requested page. This keeps the total count accurate
        // for the applied filters instead of being limited by a pre-paged sub-query.
        List<UserManagementDTO> matches = userProfileRepository
            .findAll()
            .stream()
            .filter(profile -> matchesTerm(profile, term))
            .filter(profile -> matchesStatus(profile, status))
            .filter(profile -> matchesRole(profile, role))
            .map(profile -> userManagementMapper.toDto(profile, profile.getUser()))
            .toList();

        long total = matches.size();
        int from = (int) pageable.getOffset();
        if (from >= total) {
            return new PageImpl<>(List.of(), pageable, total);
        }
        int to = Math.min(from + pageable.getPageSize(), (int) total);
        return new PageImpl<>(matches.subList(from, to), pageable, total);
    }

    private boolean matchesTerm(UserProfile profile, String term) {
        if (term.isEmpty()) {
            return true;
        }
        User user = profile.getUser();
        return (
            containsIgnoreCase(profile.getFirstName(), term) ||
            containsIgnoreCase(profile.getFirstLastName(), term) ||
            containsIgnoreCase(profile.getDocumentNumber(), term) ||
            (user != null && containsIgnoreCase(user.getEmail(), term))
        );
    }

    private boolean matchesStatus(UserProfile profile, Boolean status) {
        User user = profile.getUser();
        return status == null || (user != null && user.isActivated() == status);
    }

    private boolean matchesRole(UserProfile profile, String role) {
        if (role == null) {
            return true;
        }
        User user = profile.getUser();
        return (
            user != null &&
            user
                .getAuthorities()
                .stream()
                .anyMatch(authority -> StringUtils.equals(authority.getName(), role))
        );
    }

    private static boolean containsIgnoreCase(String value, String term) {
        return value != null && value.toLowerCase().contains(term);
    }
}
