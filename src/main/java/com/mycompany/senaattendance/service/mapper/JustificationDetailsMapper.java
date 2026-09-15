package com.mycompany.senaattendance.service.mapper;

import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Justification;
import com.mycompany.senaattendance.domain.JustificationDetails;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.service.dto.ClassSectionDTO;
import com.mycompany.senaattendance.service.dto.JustificationDTO;
import com.mycompany.senaattendance.service.dto.JustificationDetailsDTO;
import com.mycompany.senaattendance.service.dto.UserProfileDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link JustificationDetails} and its DTO {@link JustificationDetailsDTO}.
 *
 * <p>The nested justification is trimmed to what the apprentice flows (UC011) and the instructor
 * tray (UC010, A1) need to show: the period, the deadline mark, the request date and the
 * apprentice identity.
 */
@Mapper(componentModel = "spring")
public interface JustificationDetailsMapper extends EntityMapper<JustificationDetailsDTO, JustificationDetails> {
    @Mapping(target = "classSection", source = "classSection", qualifiedByName = "classSectionSubjectName")
    @Mapping(target = "justification", source = "justification", qualifiedByName = "justificationDescription")
    @Mapping(target = "requestDate", source = "justification.createdDate")
    JustificationDetailsDTO toDto(JustificationDetails s);

    /**
     * The reason of an out-of-time approval is server-owned: only the decision endpoint sets it,
     * so no client entity payload can inject it.
     */
    @Override
    @Mapping(target = "outOfTimeReason", ignore = true)
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
    @Mapping(target = "student", source = "student", qualifiedByName = "userProfileDocumentAndName")
    JustificationDTO toDtoJustificationDescription(Justification justification);

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
