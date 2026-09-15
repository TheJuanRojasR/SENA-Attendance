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
 * Mapper for the entity {@link JustificationDetails} and its DTO {@link JustificationDetailsDTO}.
 *
 * <p>The nested justification is trimmed to what the apprentice flows (UC011) and the instructor
 * tray (UC010, A1) need to show: the period, the deadline mark, the request date, the type and
 * the apprentice identity.
 */
@Mapper(componentModel = "spring")
public interface JustificationDetailsMapper extends EntityMapper<JustificationDetailsDTO, JustificationDetails> {
    @Mapping(target = "classSection", source = "classSection", qualifiedByName = "classSectionSubjectName")
    @Mapping(target = "justification", source = "justification", qualifiedByName = "justificationDescription")
    @Mapping(target = "requestDate", source = "justification.createdDate")
    JustificationDetailsDTO toDto(JustificationDetails s);

    /**
     * Detail read of one part (UC010, flow step 4): the instructor reviews the attached support
     * before deciding, so the header of the detail carries the evidence and its content type. The
     * tray keeps the light header and the file is only read from the detail.
     */
    @Named("withEvidence")
    @Mapping(target = "classSection", source = "classSection", qualifiedByName = "classSectionSubjectName")
    @Mapping(target = "justification", source = "justification", qualifiedByName = "justificationDescriptionWithEvidence")
    @Mapping(target = "requestDate", source = "justification.createdDate")
    JustificationDetailsDTO toDtoWithEvidence(JustificationDetails s);

    /**
     * The reason of an out-of-time approval and the late-decision mark are server-owned: only the
     * decision endpoint sets them, so no client entity payload can inject them.
     */
    @Override
    @Mapping(target = "outOfTimeReason", ignore = true)
    @Mapping(target = "lateDecision", ignore = true)
    JustificationDetails toEntity(JustificationDetailsDTO s);

    @Named("classSectionSubjectName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "subjectName", source = "subjectName")
    ClassSectionDTO toDtoClassSectionSubjectName(ClassSection classSection);

    @Named("justificationDescription")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    @Mapping(target = "onTime", source = "onTime")
    @Mapping(target = "justificationType", source = "justificationType", qualifiedByName = "justificationTypeName")
    @Mapping(target = "student", source = "student", qualifiedByName = "userProfileDocumentAndName")
    JustificationDTO toDtoJustificationDescription(Justification justification);

    @Named("justificationDescriptionWithEvidence")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    @Mapping(target = "evidence", source = "evidence")
    @Mapping(target = "evidenceContentType", source = "evidenceContentType")
    @Mapping(target = "onTime", source = "onTime")
    @Mapping(target = "justificationType", source = "justificationType", qualifiedByName = "justificationTypeName")
    @Mapping(target = "student", source = "student", qualifiedByName = "userProfileDocumentAndName")
    JustificationDTO toDtoJustificationDescriptionWithEvidence(Justification justification);

    @Named("justificationTypeName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    JustificationTypeDTO toDtoJustificationTypeName(JustificationType justificationType);

    @Named("userProfileDocumentAndName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "documentNumber", source = "documentNumber")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "middleName", source = "middleName")
    @Mapping(target = "firstLastName", source = "firstLastName")
    @Mapping(target = "secondLastName", source = "secondLastName")
    UserProfileDTO toDtoUserProfileDocumentAndName(UserProfile userProfile);
}
