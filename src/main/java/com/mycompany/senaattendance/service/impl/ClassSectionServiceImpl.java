package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.service.ClassSectionService;
import com.mycompany.senaattendance.service.dto.ClassSectionDTO;
import com.mycompany.senaattendance.service.mapper.ClassSectionMapper;
import java.util.Optional;
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

    public ClassSectionServiceImpl(ClassSectionRepository classSectionRepository, ClassSectionMapper classSectionMapper) {
        this.classSectionRepository = classSectionRepository;
        this.classSectionMapper = classSectionMapper;
    }

    @Override
    public ClassSectionDTO save(ClassSectionDTO classSectionDTO) {
        LOG.debug("Request to save ClassSection : {}", classSectionDTO);
        ClassSection classSection = classSectionMapper.toEntity(classSectionDTO);
        classSection = classSectionRepository.save(classSection);
        return classSectionMapper.toDto(classSection);
    }

    @Override
    public ClassSectionDTO update(ClassSectionDTO classSectionDTO) {
        LOG.debug("Request to update ClassSection : {}", classSectionDTO);
        ClassSection classSection = classSectionMapper.toEntity(classSectionDTO);
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
}
