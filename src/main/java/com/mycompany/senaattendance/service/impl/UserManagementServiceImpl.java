package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.service.UserManagementService;
import com.mycompany.senaattendance.service.dto.UserManagementDTO;
import com.mycompany.senaattendance.service.mapper.UserManagementMapper;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
    public Page<UserManagementDTO> searchUsers(String searchTerm, Boolean status, Pageable pageable) {
        List<User> usersByEmail = userRepository.findByEmailContaining(searchTerm);

        List<UserProfile> profilesByEmail = usersByEmail
            .stream()
            .map(user -> userProfileRepository.findOneByUserId(user.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

        Page<UserProfile> profilesByDocument = userProfileRepository.findByDocumentNumberContaining(searchTerm, pageable);

        Page<UserProfile> profilesByName = userProfileRepository.findByFirstNameContainingOrFirstLastNameContaining(searchTerm, pageable);

        Set<UserProfile> combinedResults = new LinkedHashSet<>();
        combinedResults.addAll(profilesByEmail);
        combinedResults.addAll(profilesByDocument.getContent());
        combinedResults.addAll(profilesByName.getContent());

        Set<UserProfile> filteredResults = combinedResults;
        if (status != null) {
            filteredResults = combinedResults
                .stream()
                .filter(profile -> {
                    User user = profile.getUser();
                    return user != null && user.isActivated() == status;
                })
                .collect(Collectors.toSet());
        }

        List<UserManagementDTO> dtos = filteredResults
            .stream()
            .map(profile -> userManagementMapper.toDto(profile, profile.getUser()))
            .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, dtos.size());
    }
}
