package com.mycompany.senaattendance.service.mapper;

import com.mycompany.senaattendance.domain.GlobalConfiguration;
import com.mycompany.senaattendance.service.dto.GlobalConfigurationDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link GlobalConfiguration} and its DTO {@link GlobalConfigurationDTO}.
 */
@Mapper(componentModel = "spring")
public interface GlobalConfigurationMapper extends EntityMapper<GlobalConfigurationDTO, GlobalConfiguration> {}
