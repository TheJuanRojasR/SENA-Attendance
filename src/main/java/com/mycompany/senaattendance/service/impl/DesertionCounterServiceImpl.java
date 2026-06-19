package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.DesertionCounter;
import com.mycompany.senaattendance.repository.DesertionCounterRepository;
import com.mycompany.senaattendance.service.DesertionCounterService;
import com.mycompany.senaattendance.service.dto.DesertionCounterDTO;
import com.mycompany.senaattendance.service.mapper.DesertionCounterMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.mycompany.senaattendance.domain.DesertionCounter}.
 */
@Service
public class DesertionCounterServiceImpl implements DesertionCounterService {

    private static final Logger LOG = LoggerFactory.getLogger(DesertionCounterServiceImpl.class);

    private final DesertionCounterRepository desertionCounterRepository;

    private final DesertionCounterMapper desertionCounterMapper;

    public DesertionCounterServiceImpl(
        DesertionCounterRepository desertionCounterRepository,
        DesertionCounterMapper desertionCounterMapper
    ) {
        this.desertionCounterRepository = desertionCounterRepository;
        this.desertionCounterMapper = desertionCounterMapper;
    }

    @Override
    public DesertionCounterDTO save(DesertionCounterDTO desertionCounterDTO) {
        LOG.debug("Request to save DesertionCounter : {}", desertionCounterDTO);
        DesertionCounter desertionCounter = desertionCounterMapper.toEntity(desertionCounterDTO);
        desertionCounter = desertionCounterRepository.save(desertionCounter);
        return desertionCounterMapper.toDto(desertionCounter);
    }

    @Override
    public DesertionCounterDTO update(DesertionCounterDTO desertionCounterDTO) {
        LOG.debug("Request to update DesertionCounter : {}", desertionCounterDTO);
        DesertionCounter desertionCounter = desertionCounterMapper.toEntity(desertionCounterDTO);
        desertionCounter = desertionCounterRepository.save(desertionCounter);
        return desertionCounterMapper.toDto(desertionCounter);
    }

    @Override
    public Optional<DesertionCounterDTO> partialUpdate(DesertionCounterDTO desertionCounterDTO) {
        LOG.debug("Request to partially update DesertionCounter : {}", desertionCounterDTO);

        return desertionCounterRepository
            .findById(desertionCounterDTO.getId())
            .map(existingDesertionCounter -> {
                desertionCounterMapper.partialUpdate(existingDesertionCounter, desertionCounterDTO);

                return existingDesertionCounter;
            })
            .map(desertionCounterRepository::save)
            .map(desertionCounterMapper::toDto);
    }

    @Override
    public Page<DesertionCounterDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all DesertionCounters");
        return desertionCounterRepository.findAll(pageable).map(desertionCounterMapper::toDto);
    }

    public Page<DesertionCounterDTO> findAllWithEagerRelationships(Pageable pageable) {
        return desertionCounterRepository.findAllWithEagerRelationships(pageable).map(desertionCounterMapper::toDto);
    }

    @Override
    public Optional<DesertionCounterDTO> findOne(String id) {
        LOG.debug("Request to get DesertionCounter : {}", id);
        return desertionCounterRepository.findOneWithEagerRelationships(id).map(desertionCounterMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete DesertionCounter : {}", id);
        desertionCounterRepository.deleteById(id);
    }
}
