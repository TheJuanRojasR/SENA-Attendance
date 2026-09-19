package com.mycompany.senaattendance.service.mapper;

import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Justification;
import com.mycompany.senaattendance.domain.JustificationDetails;
import com.mycompany.senaattendance.domain.JustificationType;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.service.dto.ClassSectionDTO;
import com.mycompany.senaattendance.service.dto.JustificationDTO;
import com.mycompany.senaattendance.service.dto.JustificationDetailsDTO;
import com.mycompany.senaattendance.service.dto.JustificationTypeDTO;
import com.mycompany.senaattendance.service.dto.UserProfileDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Justification} and its DTO {@link JustificationDTO}.
 */
@Mapper(componentModel = "spring")
public interface JustificationMapper extends EntityMapper<JustificationDTO, Justification> {
    @Override
    @Mapping(target = "justificationType", source = "justificationType", qualifiedByName = "justificationTypeName")
    @Mapping(target = "student", source = "student", qualifiedByName = "userProfileDocumentNumber")
    @Mapping(target = "detailses", source = "detailses", qualifiedByName = "justificationDetailsRef")
    JustificationDTO toDto(Justification s);

    @Override
    @Mapping(target = "detailses", ignore = true)
    Justification toEntity(JustificationDTO s);

    /**
     * The parts of a patch are used to validate the affected materias, never to replace the
     * persisted ones: the lifecycle of a part has its own service.
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "detailses", ignore = true)
    void partialUpdate(@MappingTarget Justification entity, JustificationDTO dto);

    @Named("justificationTypeName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    JustificationTypeDTO toDtoJustificationTypeName(JustificationType justificationType);

    @Named("userProfileDocumentNumber")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "documentNumber", source = "documentNumber")
    UserProfileDTO toDtoUserProfileDocumentNumber(UserProfile userProfile);

    @Named("justificationDetailsRef")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "stateJustification", source = "stateJustification")
    @Mapping(target = "rejectionReason", source = "rejectionReason")
    @Mapping(target = "correctionText", source = "correctionText")
    @Mapping(target = "correctionFileUrlContentType", source = "correctionFileUrlContentType")
    @Mapping(target = "responseDate", source = "responseDate")
    @Mapping(target = "classSection", source = "classSection", qualifiedByName = "classSectionRef")
    JustificationDetailsDTO toDtoJustificationDetailsRef(JustificationDetails details);

    @Named("classSectionRef")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "subjectName", source = "subjectName")
    ClassSectionDTO toDtoClassSectionRef(ClassSection classSection);
}
