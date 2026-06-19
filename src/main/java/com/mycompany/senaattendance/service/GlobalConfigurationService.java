package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.service.dto.GlobalConfigurationDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.mycompany.senaattendance.domain.GlobalConfiguration}.
 */
public interface GlobalConfigurationService {
    /**
     * Save a globalConfiguration.
     *
     * @param globalConfigurationDTO the entity to save.
     * @return the persisted entity.
     */
    GlobalConfigurationDTO save(GlobalConfigurationDTO globalConfigurationDTO);

    /**
     * Updates a globalConfiguration.
     *
     * @param globalConfigurationDTO the entity to update.
     * @return the persisted entity.
     */
    GlobalConfigurationDTO update(GlobalConfigurationDTO globalConfigurationDTO);

    /**
     * Partially updates a globalConfiguration.
     *
     * @param globalConfigurationDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<GlobalConfigurationDTO> partialUpdate(GlobalConfigurationDTO globalConfigurationDTO);

    /**
     * Get all the globalConfigurations.
     *
     * @return the list of entities.
     */
    List<GlobalConfigurationDTO> findAll();

    /**
     * Get the "id" globalConfiguration.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<GlobalConfigurationDTO> findOne(String id);

    /**
     * Delete the "id" globalConfiguration.
     *
     * @param id the id of the entity.
     */
    void delete(String id);
}
