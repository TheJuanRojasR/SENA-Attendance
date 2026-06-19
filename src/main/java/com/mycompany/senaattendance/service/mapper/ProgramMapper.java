package com.mycompany.senaattendance.service.mapper;

import com.mycompany.senaattendance.domain.Program;
import com.mycompany.senaattendance.service.dto.ProgramDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Program} and its DTO {@link ProgramDTO}.
 */
@Mapper(componentModel = "spring")
public interface ProgramMapper extends EntityMapper<ProgramDTO, Program> {}
