package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.JustificationDetails;
import com.mycompany.senaattendance.repository.JustificationDetailsRepository;
import com.mycompany.senaattendance.service.JustificationDetailsService;
import com.mycompany.senaattendance.service.dto.JustificationDetailsDTO;
import com.mycompany.senaattendance.service.mapper.JustificationDetailsMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.mycompany.senaattendance.domain.JustificationDetails}.
 */
@Service
public class JustificationDetailsServiceImpl implements JustificationDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(JustificationDetailsServiceImpl.class);

    private final JustificationDetailsRepository justificationDetailsRepository;

    private final JustificationDetailsMapper justificationDetailsMapper;

    public JustificationDetailsServiceImpl(
        JustificationDetailsRepository justificationDetailsRepository,
        JustificationDetailsMapper justificationDetailsMapper
    ) {
        this.justificationDetailsRepository = justificationDetailsRepository;
        this.justificationDetailsMapper = justificationDetailsMapper;
    }

    @Override
    public JustificationDetailsDTO save(JustificationDetailsDTO justificationDetailsDTO) {
        LOG.debug("Request to save JustificationDetails : {}", justificationDetailsDTO);
        JustificationDetails justificationDetails = justificationDetailsMapper.toEntity(justificationDetailsDTO);
        justificationDetails = justificationDetailsRepository.save(justificationDetails);
        return justificationDetailsMapper.toDto(justificationDetails);
    }

    @Override
    public JustificationDetailsDTO update(JustificationDetailsDTO justificationDetailsDTO) {
        LOG.debug("Request to update JustificationDetails : {}", justificationDetailsDTO);
        JustificationDetails justificationDetails = justificationDetailsMapper.toEntity(justificationDetailsDTO);
        justificationDetails = justificationDetailsRepository.save(justificationDetails);
        return justificationDetailsMapper.toDto(justificationDetails);
    }

    @Override
    public Optional<JustificationDetailsDTO> partialUpdate(JustificationDetailsDTO justificationDetailsDTO) {
        LOG.debug("Request to partially update JustificationDetails : {}", justificationDetailsDTO);

        return justificationDetailsRepository
            .findById(justificationDetailsDTO.getId())
            .map(existingJustificationDetails -> {
                justificationDetailsMapper.partialUpdate(existingJustificationDetails, justificationDetailsDTO);

                return existingJustificationDetails;
            })
            .map(justificationDetailsRepository::save)
            .map(justificationDetailsMapper::toDto);
    }

    @Override
    public Page<JustificationDetailsDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all JustificationDetailses");
        return justificationDetailsRepository.findAll(pageable).map(justificationDetailsMapper::toDto);
    }

    public Page<JustificationDetailsDTO> findAllWithEagerRelationships(Pageable pageable) {
        return justificationDetailsRepository.findAllWithEagerRelationships(pageable).map(justificationDetailsMapper::toDto);
    }

    @Override
    public Optional<JustificationDetailsDTO> findOne(String id) {
        LOG.debug("Request to get JustificationDetails : {}", id);
        return justificationDetailsRepository.findOneWithEagerRelationships(id).map(justificationDetailsMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete JustificationDetails : {}", id);
        justificationDetailsRepository.deleteById(id);
    }
}
