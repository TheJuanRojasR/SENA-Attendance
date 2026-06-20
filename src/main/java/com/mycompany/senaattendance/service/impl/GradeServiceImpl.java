package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.GradeService;
import com.mycompany.senaattendance.service.dto.GradeDTO;
import com.mycompany.senaattendance.service.mapper.GradeMapper;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.mycompany.senaattendance.domain.Grade}.
 */
@Service
public class GradeServiceImpl implements GradeService {

    private static final Logger LOG = LoggerFactory.getLogger(GradeServiceImpl.class);

    private final GradeRepository gradeRepository;

    private final GradeMapper gradeMapper;

    public GradeServiceImpl(GradeRepository gradeRepository, GradeMapper gradeMapper) {
        this.gradeRepository = gradeRepository;
        this.gradeMapper = gradeMapper;
    }

    @Override
    public GradeDTO save(GradeDTO gradeDTO) {
        LOG.debug("Request to save Grade : {}", gradeDTO);
        Grade grade = gradeMapper.toEntity(gradeDTO);

        // Inserta fecha de creación
        grade.setCreatedDate(Instant.now());
        Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
        if (currentUserLogin.isPresent()) {
            // Inserta quien lo creo
            grade.setCreatedBy(currentUserLogin.get());
        }

        grade = gradeRepository.save(grade);
        return gradeMapper.toDto(grade);
    }

    @Override
    public GradeDTO update(GradeDTO gradeDTO) {
        LOG.debug("Request to update Grade : {}", gradeDTO);
        Grade grade = gradeMapper.toEntity(gradeDTO);

        Optional<Grade> optionalGrade = gradeRepository.findById(grade.getId());
        if (optionalGrade.isPresent()) {
            Grade existingGrade = optionalGrade.get();
            grade.setCreatedBy(existingGrade.getCreatedBy());
            grade.setCreatedDate(existingGrade.getCreatedDate());
        } else {
            grade.setCreatedDate(Instant.now());
            Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
            if (currentUserLogin.isPresent()) {
                grade.setCreatedBy(currentUserLogin.get());
            }
        }

        grade = gradeRepository.save(grade);
        return gradeMapper.toDto(grade);
    }

    @Override
    public Optional<GradeDTO> partialUpdate(GradeDTO gradeDTO) {
        LOG.debug("Request to partially update Grade : {}", gradeDTO);

        return gradeRepository
            .findById(gradeDTO.getId())
            .map(existingGrade -> {
                gradeMapper.partialUpdate(existingGrade, gradeDTO);

                return existingGrade;
            })
            .map(gradeRepository::save)
            .map(gradeMapper::toDto);
    }

    @Override
    public Page<GradeDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Grades");
        return gradeRepository.findAll(pageable).map(gradeMapper::toDto);
    }

    public Page<GradeDTO> findAllWithEagerRelationships(Pageable pageable) {
        return gradeRepository.findAllWithEagerRelationships(pageable).map(gradeMapper::toDto);
    }

    @Override
    public Optional<GradeDTO> findOne(String id) {
        LOG.debug("Request to get Grade : {}", id);
        return gradeRepository.findOneWithEagerRelationships(id).map(gradeMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete Grade : {}", id);
        gradeRepository.deleteById(id);
    }
}
