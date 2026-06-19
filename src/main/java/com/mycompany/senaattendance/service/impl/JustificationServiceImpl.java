package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.Justification;
import com.mycompany.senaattendance.repository.JustificationRepository;
import com.mycompany.senaattendance.service.JustificationService;
import com.mycompany.senaattendance.service.dto.JustificationDTO;
import com.mycompany.senaattendance.service.mapper.JustificationMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.mycompany.senaattendance.domain.Justification}.
 */
@Service
public class JustificationServiceImpl implements JustificationService {

    private static final Logger LOG = LoggerFactory.getLogger(JustificationServiceImpl.class);

    private final JustificationRepository justificationRepository;

    private final JustificationMapper justificationMapper;

    public JustificationServiceImpl(JustificationRepository justificationRepository, JustificationMapper justificationMapper) {
        this.justificationRepository = justificationRepository;
        this.justificationMapper = justificationMapper;
    }

    @Override
    public JustificationDTO save(JustificationDTO justificationDTO) {
        LOG.debug("Request to save Justification : {}", justificationDTO);
        Justification justification = justificationMapper.toEntity(justificationDTO);
        justification = justificationRepository.save(justification);
        return justificationMapper.toDto(justification);
    }

    @Override
    public JustificationDTO update(JustificationDTO justificationDTO) {
        LOG.debug("Request to update Justification : {}", justificationDTO);
        Justification justification = justificationMapper.toEntity(justificationDTO);
        justification = justificationRepository.save(justification);
        return justificationMapper.toDto(justification);
    }

    @Override
    public Optional<JustificationDTO> partialUpdate(JustificationDTO justificationDTO) {
        LOG.debug("Request to partially update Justification : {}", justificationDTO);

        return justificationRepository
            .findById(justificationDTO.getId())
            .map(existingJustification -> {
                justificationMapper.partialUpdate(existingJustification, justificationDTO);

                return existingJustification;
            })
            .map(justificationRepository::save)
            .map(justificationMapper::toDto);
    }

    @Override
    public Page<JustificationDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Justifications");
        return justificationRepository.findAll(pageable).map(justificationMapper::toDto);
    }

    public Page<JustificationDTO> findAllWithEagerRelationships(Pageable pageable) {
        return justificationRepository.findAllWithEagerRelationships(pageable).map(justificationMapper::toDto);
    }

    @Override
    public Optional<JustificationDTO> findOne(String id) {
        LOG.debug("Request to get Justification : {}", id);
        return justificationRepository.findOneWithEagerRelationships(id).map(justificationMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete Justification : {}", id);
        justificationRepository.deleteById(id);
    }
}
