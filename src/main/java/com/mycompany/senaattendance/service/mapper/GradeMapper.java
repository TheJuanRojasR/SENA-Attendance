package com.mycompany.senaattendance.service.mapper;

import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.Modality;
import com.mycompany.senaattendance.domain.Program;
import com.mycompany.senaattendance.domain.TimeSlot;
import com.mycompany.senaattendance.service.dto.GradeDTO;
import com.mycompany.senaattendance.service.dto.ModalityDTO;
import com.mycompany.senaattendance.service.dto.ProgramDTO;
import com.mycompany.senaattendance.service.dto.TimeSlotDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Grade} and its DTO {@link GradeDTO}.
 */
@Mapper(componentModel = "spring")
public interface GradeMapper extends EntityMapper<GradeDTO, Grade> {
    @Mapping(target = "program", source = "program", qualifiedByName = "programName")
    @Mapping(target = "modality", source = "modality", qualifiedByName = "modalityName")
    @Mapping(target = "timeSlot", source = "timeSlot", qualifiedByName = "timeSlotName")
    GradeDTO toDto(Grade s);

    @Named("programName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    ProgramDTO toDtoProgramName(Program program);

    @Named("modalityName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    ModalityDTO toDtoModalityName(Modality modality);

    @Named("timeSlotName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    TimeSlotDTO toDtoTimeSlotName(TimeSlot timeSlot);
}
