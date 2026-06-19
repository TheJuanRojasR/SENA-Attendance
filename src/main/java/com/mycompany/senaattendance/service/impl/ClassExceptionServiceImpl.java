package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.ClassException;
import com.mycompany.senaattendance.repository.ClassExceptionRepository;
import com.mycompany.senaattendance.service.ClassExceptionService;
import com.mycompany.senaattendance.service.dto.ClassExceptionDTO;
import com.mycompany.senaattendance.service.mapper.ClassExceptionMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.mycompany.senaattendance.domain.ClassException}.
 */
@Service
public class ClassExceptionServiceImpl implements ClassExceptionService {

    private static final Logger LOG = LoggerFactory.getLogger(ClassExceptionServiceImpl.class);

    private final ClassExceptionRepository classExceptionRepository;

    private final ClassExceptionMapper classExceptionMapper;

    public ClassExceptionServiceImpl(ClassExceptionRepository classExceptionRepository, ClassExceptionMapper classExceptionMapper) {
        this.classExceptionRepository = classExceptionRepository;
        this.classExceptionMapper = classExceptionMapper;
    }

    @Override
    public ClassExceptionDTO save(ClassExceptionDTO classExceptionDTO) {
        LOG.debug("Request to save ClassException : {}", classExceptionDTO);
        ClassException classException = classExceptionMapper.toEntity(classExceptionDTO);
        classException = classExceptionRepository.save(classException);
        return classExceptionMapper.toDto(classException);
    }

    @Override
    public ClassExceptionDTO update(ClassExceptionDTO classExceptionDTO) {
        LOG.debug("Request to update ClassException : {}", classExceptionDTO);
        ClassException classException = classExceptionMapper.toEntity(classExceptionDTO);
        classException = classExceptionRepository.save(classException);
        return classExceptionMapper.toDto(classException);
    }

    @Override
    public Optional<ClassExceptionDTO> partialUpdate(ClassExceptionDTO classExceptionDTO) {
        LOG.debug("Request to partially update ClassException : {}", classExceptionDTO);

        return classExceptionRepository
            .findById(classExceptionDTO.getId())
            .map(existingClassException -> {
                classExceptionMapper.partialUpdate(existingClassException, classExceptionDTO);

                return existingClassException;
            })
            .map(classExceptionRepository::save)
            .map(classExceptionMapper::toDto);
    }

    @Override
    public Page<ClassExceptionDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all ClassExceptions");
        return classExceptionRepository.findAll(pageable).map(classExceptionMapper::toDto);
    }

    public Page<ClassExceptionDTO> findAllWithEagerRelationships(Pageable pageable) {
        return classExceptionRepository.findAllWithEagerRelationships(pageable).map(classExceptionMapper::toDto);
    }

    @Override
    public Optional<ClassExceptionDTO> findOne(String id) {
        LOG.debug("Request to get ClassException : {}", id);
        return classExceptionRepository.findOneWithEagerRelationships(id).map(classExceptionMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete ClassException : {}", id);
        classExceptionRepository.deleteById(id);
    }
}
