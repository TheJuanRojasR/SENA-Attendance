package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.UserProfileService;
import com.mycompany.senaattendance.service.dto.UserProfileDTO;
import com.mycompany.senaattendance.service.mapper.UserProfileMapper;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.mycompany.senaattendance.domain.UserProfile}.
 */
@Service
public class UserProfileServiceImpl implements UserProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(UserProfileServiceImpl.class);

    private final UserProfileRepository userProfileRepository;

    private final UserProfileMapper userProfileMapper;

    public UserProfileServiceImpl(UserProfileRepository userProfileRepository, UserProfileMapper userProfileMapper) {
        this.userProfileRepository = userProfileRepository;
        this.userProfileMapper = userProfileMapper;
    }

    @Override
    public UserProfileDTO save(UserProfileDTO userProfileDTO) {
        LOG.debug("Request to save UserProfile : {}", userProfileDTO);
        UserProfile userProfile = userProfileMapper.toEntity(userProfileDTO);

        userProfile.setCreatedDate(Instant.now());
        Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
        if (currentUserLogin.isPresent()) {
            userProfile.setCreatedBy(currentUserLogin.get());
        }

        userProfile = userProfileRepository.save(userProfile);
        return userProfileMapper.toDto(userProfile);
    }

    @Override
    public UserProfileDTO update(UserProfileDTO userProfileDTO) {
        LOG.debug("Request to update UserProfile : {}", userProfileDTO);
        UserProfile userProfile = userProfileMapper.toEntity(userProfileDTO);

        Optional<UserProfile> optionalUserProfile = userProfileRepository.findById(userProfile.getId());
        if (optionalUserProfile.isPresent()) {
            UserProfile existingUserProfile = optionalUserProfile.get();
            userProfile.setCreatedBy(existingUserProfile.getCreatedBy());
            userProfile.setCreatedDate(existingUserProfile.getCreatedDate());
        } else {
            userProfile.setCreatedDate(Instant.now());
            Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
            if (currentUserLogin.isPresent()) {
                userProfile.setCreatedBy(currentUserLogin.get());
            }
        }

        userProfile = userProfileRepository.save(userProfile);
        return userProfileMapper.toDto(userProfile);
    }

    @Override
    public Optional<UserProfileDTO> partialUpdate(UserProfileDTO userProfileDTO) {
        LOG.debug("Request to partially update UserProfile : {}", userProfileDTO);

        return userProfileRepository
            .findById(userProfileDTO.getId())
            .map(existingUserProfile -> {
                userProfileMapper.partialUpdate(existingUserProfile, userProfileDTO);

                return existingUserProfile;
            })
            .map(userProfileRepository::save)
            .map(userProfileMapper::toDto);
    }

    @Override
    public Page<UserProfileDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all UserProfiles");
        return userProfileRepository.findAll(pageable).map(userProfileMapper::toDto);
    }

    public Page<UserProfileDTO> findAllWithEagerRelationships(Pageable pageable) {
        return userProfileRepository.findAllWithEagerRelationships(pageable).map(userProfileMapper::toDto);
    }

    @Override
    public Optional<UserProfileDTO> findOne(String id) {
        LOG.debug("Request to get UserProfile : {}", id);
        return userProfileRepository.findOneWithEagerRelationships(id).map(userProfileMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete UserProfile : {}", id);
        userProfileRepository.deleteById(id);
    }

    // --------------------------- New methods ---------------------------

    @Override
    public List<UserProfileDTO> findAllApprentices() {
        LOG.debug("Request to get all UserProfiles with APPRENTICE role");

        // 1. Trae todos los userProfiles
        List<UserProfile> allProfiles = userProfileRepository.findAllWithEagerRelationships();

        // 2. Filtra los userProfiles que tienen rol 'APPRENTICE'
        return allProfiles
            .stream()
            .filter(userProfile -> {
                User user = userProfile.getUser();

                if (user == null) {
                    return false;
                }

                return user
                    .getAuthorities()
                    .stream()
                    .anyMatch(authority -> authority.getName().equals(AuthoritiesConstants.APPRENTICE));
            })
            // 3. Convierte a DTO y colecta en una lista
            .map(userProfileMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public List<UserProfileDTO> findAllInstructors() {
        LOG.debug("Request to get all UserProfiles with INSTRUCTOR role");

        // 1. Trae todos los userProfiles
        List<UserProfile> allProfiles = userProfileRepository.findAllWithEagerRelationships();

        // 2. Filtra los userProfiles que tienen rol 'INSTRUCTOR'
        return allProfiles
            .stream()
            .filter(userProfile -> {
                User user = userProfile.getUser();

                if (user == null) {
                    return false;
                }

                return user
                    .getAuthorities()
                    .stream()
                    .anyMatch(authority -> authority.getName().equals(AuthoritiesConstants.INSTRUCTOR));
            })
            // 3. Convierte a DTO y colecta en una lista
            .map(userProfileMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }
}
