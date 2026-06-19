package com.mycompany.senaattendance.service.mapper;

import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Justification;
import com.mycompany.senaattendance.domain.JustificationDetails;
import com.mycompany.senaattendance.service.dto.ClassSectionDTO;
import com.mycompany.senaattendance.service.dto.JustificationDTO;
import com.mycompany.senaattendance.service.dto.JustificationDetailsDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link JustificationDetails} and its DTO {@link JustificationDetailsDTO}.
 */
@Mapper(componentModel = "spring")
public interface JustificationDetailsMapper extends EntityMapper<JustificationDetailsDTO, JustificationDetails> {
    @Mapping(target = "classSection", source = "classSection", qualifiedByName = "classSectionSubjectName")
    @Mapping(target = "justification", source = "justification", qualifiedByName = "justificationDescription")
    JustificationDetailsDTO toDto(JustificationDetails s);

    @Named("classSectionSubjectName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "subjectName", source = "subjectName")
    ClassSectionDTO toDtoClassSectionSubjectName(ClassSection classSection);

    @Named("justificationDescription")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "description", source = "description")
    JustificationDTO toDtoJustificationDescription(Justification justification);
}
