package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.repository.TrimesterRepository;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.TrimesterService;
import com.mycompany.senaattendance.service.dto.TrimesterDTO;
import com.mycompany.senaattendance.service.mapper.TrimesterMapper;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.mycompany.senaattendance.domain.Trimester}.
 */
@Service
public class TrimesterServiceImpl implements TrimesterService {

    private static final Logger LOG = LoggerFactory.getLogger(TrimesterServiceImpl.class);

    private final TrimesterRepository trimesterRepository;

    private final TrimesterMapper trimesterMapper;

    public TrimesterServiceImpl(TrimesterRepository trimesterRepository, TrimesterMapper trimesterMapper) {
        this.trimesterRepository = trimesterRepository;
        this.trimesterMapper = trimesterMapper;
    }

    @Override
    public TrimesterDTO save(TrimesterDTO trimesterDTO) {
        LOG.debug("Request to save Trimester : {}", trimesterDTO);
        Trimester trimester = trimesterMapper.toEntity(trimesterDTO);

        trimester.setCreatedDate(Instant.now());
        Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
        if (currentUserLogin.isPresent()) {
            trimester.setCreatedBy(currentUserLogin.get());
        }

        trimester = trimesterRepository.save(trimester);
        return trimesterMapper.toDto(trimester);
    }

    @Override
    public TrimesterDTO update(TrimesterDTO trimesterDTO) {
        LOG.debug("Request to update Trimester : {}", trimesterDTO);
        Trimester trimester = trimesterMapper.toEntity(trimesterDTO);

        Optional<Trimester> optionalTrimester = trimesterRepository.findById(trimester.getId());
        if (optionalTrimester.isPresent()) {
            Trimester existingTrimester = optionalTrimester.get();
            trimester.setCreatedBy(existingTrimester.getCreatedBy());
            trimester.setCreatedDate(existingTrimester.getCreatedDate());
        } else {
            trimester.setCreatedDate(Instant.now());
            Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
            if (currentUserLogin.isPresent()) {
                trimester.setCreatedBy(currentUserLogin.get());
            }
        }

        trimester = trimesterRepository.save(trimester);
        return trimesterMapper.toDto(trimester);
    }

    @Override
    public Optional<TrimesterDTO> partialUpdate(TrimesterDTO trimesterDTO) {
        LOG.debug("Request to partially update Trimester : {}", trimesterDTO);

        return trimesterRepository
            .findById(trimesterDTO.getId())
            .map(existingTrimester -> {
                trimesterMapper.partialUpdate(existingTrimester, trimesterDTO);

                return existingTrimester;
            })
            .map(trimesterRepository::save)
            .map(trimesterMapper::toDto);
    }

    @Override
    public Page<TrimesterDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Trimesters");
        return trimesterRepository.findAll(pageable).map(trimesterMapper::toDto);
    }

    @Override
    public Optional<TrimesterDTO> findOne(String id) {
        LOG.debug("Request to get Trimester : {}", id);
        return trimesterRepository.findById(id).map(trimesterMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete Trimester : {}", id);
        trimesterRepository.deleteById(id);
    }
}
