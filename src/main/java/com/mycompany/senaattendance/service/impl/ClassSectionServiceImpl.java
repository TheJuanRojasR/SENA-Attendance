package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.ClassSectionService;
import com.mycompany.senaattendance.service.dto.ClassSectionDTO;
import com.mycompany.senaattendance.service.mapper.ClassSectionMapper;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.mycompany.senaattendance.domain.ClassSection}.
 */
@Service
public class ClassSectionServiceImpl implements ClassSectionService {

    private static final Logger LOG = LoggerFactory.getLogger(ClassSectionServiceImpl.class);

    private final ClassSectionRepository classSectionRepository;

    private final ClassSectionMapper classSectionMapper;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    public ClassSectionServiceImpl(
        ClassSectionRepository classSectionRepository,
        ClassSectionMapper classSectionMapper,
        UserRepository userRepository,
        UserProfileRepository userProfileRepository
    ) {
        this.classSectionRepository = classSectionRepository;
        this.classSectionMapper = classSectionMapper;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public ClassSectionDTO save(ClassSectionDTO classSectionDTO) {
        LOG.debug("Request to save ClassSection : {}", classSectionDTO);
        ClassSection classSection = classSectionMapper.toEntity(classSectionDTO);

        classSection.setCreatedDate(Instant.now());
        Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
        if (currentUserLogin.isPresent()) {
            classSection.setCreatedBy(currentUserLogin.get());
        }

        classSection = classSectionRepository.save(classSection);
        return classSectionMapper.toDto(classSection);
    }

    @Override
    public ClassSectionDTO update(ClassSectionDTO classSectionDTO) {
        LOG.debug("Request to update ClassSection : {}", classSectionDTO);
        ClassSection classSection = classSectionMapper.toEntity(classSectionDTO);

        Optional<ClassSection> optionalClassSection = classSectionRepository.findById(classSection.getId());
        if (optionalClassSection.isPresent()) {
            ClassSection existingClassSection = optionalClassSection.get();
            classSection.setCreatedBy(existingClassSection.getCreatedBy());
            classSection.setCreatedDate(existingClassSection.getCreatedDate());
        } else {
            classSection.setCreatedDate(Instant.now());
            Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
            if (currentUserLogin.isPresent()) {
                classSection.setCreatedBy(currentUserLogin.get());
            }
        }

        classSection = classSectionRepository.save(classSection);
        return classSectionMapper.toDto(classSection);
    }

    @Override
    public Optional<ClassSectionDTO> partialUpdate(ClassSectionDTO classSectionDTO) {
        LOG.debug("Request to partially update ClassSection : {}", classSectionDTO);

        return classSectionRepository
            .findById(classSectionDTO.getId())
            .map(existingClassSection -> {
                classSectionMapper.partialUpdate(existingClassSection, classSectionDTO);

                return existingClassSection;
            })
            .map(classSectionRepository::save)
            .map(classSectionMapper::toDto);
    }

    @Override
    public Page<ClassSectionDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all ClassSections");
        return classSectionRepository.findAll(pageable).map(classSectionMapper::toDto);
    }

    public Page<ClassSectionDTO> findAllWithEagerRelationships(Pageable pageable) {
        return classSectionRepository.findAllWithEagerRelationships(pageable).map(classSectionMapper::toDto);
    }

    @Override
    public Optional<ClassSectionDTO> findOne(String id) {
        LOG.debug("Request to get ClassSection : {}", id);
        return classSectionRepository.findOneWithEagerRelationships(id).map(classSectionMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete ClassSection : {}", id);
        classSectionRepository.deleteById(id);
    }

    @Override
    public List<ClassSectionDTO> findAllForCurrentInstructor() {
        LOG.debug("Request to get all ClassSections for the current instructor");
        Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
        if (currentUserLogin.isEmpty()) {
            return Collections.emptyList();
        }

        User user = userRepository.findOneByLogin(currentUserLogin.get()).orElse(null);
        if (user == null) {
            return Collections.emptyList();
        }

        Optional<UserProfile> profileOpt = userProfileRepository.findOneByUserId(user.getId());
        if (profileOpt.isEmpty()) {
            return Collections.emptyList();
        }

        String profileId = profileOpt.get().getId();

        return classSectionRepository.findByInstructorId(profileId).stream().map(classSectionMapper::toDto).collect(Collectors.toList());
    }
}
