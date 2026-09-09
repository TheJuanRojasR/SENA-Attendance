package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.config.Constants;
import com.mycompany.senaattendance.domain.GlobalConfiguration;
import com.mycompany.senaattendance.repository.GlobalConfigurationRepository;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.GlobalConfigurationService;
import com.mycompany.senaattendance.service.dto.GlobalConfigurationDTO;
import com.mycompany.senaattendance.service.mapper.GlobalConfigurationMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing the singleton {@link GlobalConfiguration}.
 *
 * <p>The configuration is a single row: reads re-seed it with the default values
 * when it is missing (defensive recovery), and updates are partial merges of the
 * two typed fields. Classification snapshots are never derived from live config.
 */
@Service
public class GlobalConfigurationServiceImpl implements GlobalConfigurationService {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalConfigurationServiceImpl.class);

    public static final Integer DEFAULT_STUDENT_JUSTIFICATION_DAYS = 5;
    public static final Integer DEFAULT_INSTRUCTOR_RESPONSE_DAYS = 2;

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
    public GlobalConfigurationDTO get() {
        LOG.debug("Request to get the GlobalConfiguration");
        return globalConfigurationMapper.toDto(getSingletonEntity());
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

    /**
     * Loads the single configuration row, re-seeding it with the default values
     * when it is missing.
     */
    private GlobalConfiguration getSingletonEntity() {
        List<GlobalConfiguration> configurations = globalConfigurationRepository.findAll();
        if (!configurations.isEmpty()) {
            return configurations.get(0);
        }

        LOG.warn("GlobalConfiguration row is missing, re-seeding it with the default values");
        GlobalConfiguration globalConfiguration = new GlobalConfiguration();
        globalConfiguration.setStudentJustificationDays(DEFAULT_STUDENT_JUSTIFICATION_DAYS);
        globalConfiguration.setInstructorResponseDays(DEFAULT_INSTRUCTOR_RESPONSE_DAYS);
        globalConfiguration.setCreatedBy(SecurityUtils.getCurrentUserLogin().orElse(Constants.SYSTEM));
        globalConfiguration.setCreatedDate(Instant.now());
        return globalConfigurationRepository.save(globalConfiguration);
    }
}
