package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.GlobalConfiguration;
import com.mycompany.senaattendance.repository.GlobalConfigurationRepository;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.GlobalConfigurationService;
import com.mycompany.senaattendance.service.dto.GlobalConfigurationDTO;
import com.mycompany.senaattendance.service.mapper.GlobalConfigurationMapper;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.mycompany.senaattendance.domain.GlobalConfiguration}.
 */
@Service
public class GlobalConfigurationServiceImpl implements GlobalConfigurationService {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalConfigurationServiceImpl.class);

    private final GlobalConfigurationRepository globalConfigurationRepository;

    private final GlobalConfigurationMapper globalConfigurationMapper;

    public GlobalConfigurationServiceImpl(
        GlobalConfigurationRepository globalConfigurationRepository,
        GlobalConfigurationMapper globalConfigurationMapper
    ) {
        this.globalConfigurationRepository = globalConfigurationRepository;
        this.globalConfigurationMapper = globalConfigurationMapper;
    }

    @Override
    public GlobalConfigurationDTO save(GlobalConfigurationDTO globalConfigurationDTO) {
        LOG.debug("Request to save GlobalConfiguration : {}", globalConfigurationDTO);
        GlobalConfiguration globalConfiguration = globalConfigurationMapper.toEntity(globalConfigurationDTO);

        // Inserta fecha de creación
        globalConfiguration.setCreatedDate(Instant.now());
        Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
        if (currentUserLogin.isPresent()) {
            // Inserta quien lo creo
            globalConfiguration.setCreatedBy(currentUserLogin.get());
        }

        globalConfiguration = globalConfigurationRepository.save(globalConfiguration);
        return globalConfigurationMapper.toDto(globalConfiguration);
    }

    @Override
    public GlobalConfigurationDTO update(GlobalConfigurationDTO globalConfigurationDTO) {
        LOG.debug("Request to update GlobalConfiguration : {}", globalConfigurationDTO);
        GlobalConfiguration globalConfiguration = globalConfigurationMapper.toEntity(globalConfigurationDTO);

        Optional<GlobalConfiguration> optionalGlobalConfiguration = globalConfigurationRepository.findById(globalConfiguration.getId());
        if (optionalGlobalConfiguration.isPresent()) {
            GlobalConfiguration existingGlobalConfiguration = optionalGlobalConfiguration.get();
            globalConfiguration.setCreatedBy(existingGlobalConfiguration.getCreatedBy());
            globalConfiguration.setCreatedDate(existingGlobalConfiguration.getCreatedDate());
        } else {
            globalConfiguration.setCreatedDate(Instant.now());
            Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
            if (currentUserLogin.isPresent()) {
                globalConfiguration.setCreatedBy(currentUserLogin.get());
            }
        }

        globalConfiguration = globalConfigurationRepository.save(globalConfiguration);
        return globalConfigurationMapper.toDto(globalConfiguration);
    }

    @Override
    public Optional<GlobalConfigurationDTO> partialUpdate(GlobalConfigurationDTO globalConfigurationDTO) {
        LOG.debug("Request to partially update GlobalConfiguration : {}", globalConfigurationDTO);

        return globalConfigurationRepository
            .findById(globalConfigurationDTO.getId())
            .map(existingGlobalConfiguration -> {
                globalConfigurationMapper.partialUpdate(existingGlobalConfiguration, globalConfigurationDTO);

                return existingGlobalConfiguration;
            })
            .map(globalConfigurationRepository::save)
            .map(globalConfigurationMapper::toDto);
    }

    @Override
    public List<GlobalConfigurationDTO> findAll() {
        LOG.debug("Request to get all GlobalConfigurations");
        return globalConfigurationRepository
            .findAll()
            .stream()
            .map(globalConfigurationMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public Optional<GlobalConfigurationDTO> findOne(String id) {
        LOG.debug("Request to get GlobalConfiguration : {}", id);
        return globalConfigurationRepository.findById(id).map(globalConfigurationMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete GlobalConfiguration : {}", id);
        globalConfigurationRepository.deleteById(id);
    }
}
