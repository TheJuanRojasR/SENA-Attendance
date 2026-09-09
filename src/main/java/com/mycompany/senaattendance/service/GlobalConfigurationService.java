package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.service.dto.GlobalConfigurationDTO;
import java.util.Optional;

/**
 * Service Interface for managing the singleton {@link com.mycompany.senaattendance.domain.GlobalConfiguration}.
 */
public interface GlobalConfigurationService {
    /**
     * Gets the global configuration.
     *
     * @return the global configuration.
     */
    GlobalConfigurationDTO get();

    /**
     * Partially updates the global configuration.
     *
     * @param globalConfigurationDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<GlobalConfigurationDTO> partialUpdate(GlobalConfigurationDTO globalConfigurationDTO);
}
