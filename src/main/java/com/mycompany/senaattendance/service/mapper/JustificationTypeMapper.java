package com.mycompany.senaattendance.service.mapper;

import com.mycompany.senaattendance.domain.JustificationType;
import com.mycompany.senaattendance.service.dto.JustificationTypeDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link JustificationType} and its DTO {@link JustificationTypeDTO}.
 */
@Mapper(componentModel = "spring")
public interface JustificationTypeMapper extends EntityMapper<JustificationTypeDTO, JustificationType> {}
