package com.mycompany.senaattendance.service.mapper;

import com.mycompany.senaattendance.domain.Attendance;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Justification;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.service.dto.AttendanceDTO;
import com.mycompany.senaattendance.service.dto.ClassSectionDTO;
import com.mycompany.senaattendance.service.dto.JustificationDTO;
import com.mycompany.senaattendance.service.dto.UserProfileDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Attendance} and its DTO {@link AttendanceDTO}.
 */
@Mapper(componentModel = "spring")
public interface AttendanceMapper extends EntityMapper<AttendanceDTO, Attendance> {
    @Mapping(target = "classSection", source = "classSection", qualifiedByName = "classSectionSubjectName")
    @Mapping(target = "student", source = "student", qualifiedByName = "userProfileSummary")
    @Mapping(target = "modifiedByJustification", source = "modifiedByJustification", qualifiedByName = "justificationId")
    AttendanceDTO toDto(Attendance s);

    @Named("classSectionSubjectName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "subjectName", source = "subjectName")
    ClassSectionDTO toDtoClassSectionSubjectName(ClassSection classSection);

    @Named("userProfileSummary")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "documentNumber", source = "documentNumber")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "firstLastName", source = "firstLastName")
    UserProfileDTO toDtoUserProfileSummary(UserProfile userProfile);

    @Named("justificationId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    JustificationDTO toDtoJustificationId(Justification justification);
}
