package com.mycompany.senaattendance.service.mapper;

import com.mycompany.senaattendance.domain.ClassSchedule;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.service.dto.ClassScheduleDTO;
import com.mycompany.senaattendance.service.dto.ClassSectionDTO;
import com.mycompany.senaattendance.service.dto.TrimesterDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ClassSchedule} and its DTO {@link ClassScheduleDTO}.
 */
@Mapper(componentModel = "spring")
public interface ClassScheduleMapper extends EntityMapper<ClassScheduleDTO, ClassSchedule> {
    @Mapping(target = "trimester", source = "trimester", qualifiedByName = "trimesterName")
    @Mapping(target = "classSection", source = "classSection", qualifiedByName = "classSectionSubjectName")
    ClassScheduleDTO toDto(ClassSchedule s);

    @Named("trimesterName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    TrimesterDTO toDtoTrimesterName(Trimester trimester);

    @Named("classSectionSubjectName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "subjectName", source = "subjectName")
    ClassSectionDTO toDtoClassSectionSubjectName(ClassSection classSection);
}
