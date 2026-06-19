package com.mycompany.senaattendance.service.mapper;

import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.service.dto.TrimesterDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Trimester} and its DTO {@link TrimesterDTO}.
 */
@Mapper(componentModel = "spring")
public interface TrimesterMapper extends EntityMapper<TrimesterDTO, Trimester> {}
