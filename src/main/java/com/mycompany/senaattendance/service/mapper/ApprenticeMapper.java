package com.mycompany.senaattendance.service.mapper;

import com.mycompany.senaattendance.domain.Apprentice;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.service.dto.ApprenticeDTO;
import com.mycompany.senaattendance.service.dto.GradeDTO;
import com.mycompany.senaattendance.service.dto.UserProfileDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Apprentice} and its DTO {@link ApprenticeDTO}.
 */
@Mapper(componentModel = "spring")
public interface ApprenticeMapper extends EntityMapper<ApprenticeDTO, Apprentice> {
    @Mapping(target = "student", source = "student", qualifiedByName = "userProfileSummary")
    @Mapping(target = "grade", source = "grade", qualifiedByName = "gradeCode")
    ApprenticeDTO toDto(Apprentice s);

    /**
     * Summary of the student shown by the apprentice list (UC008, A2): the document number and
     * the name that identify the apprentice, plus the id of the profile.
     */
    @Named("userProfileSummary")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "documentNumber", source = "documentNumber")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "firstLastName", source = "firstLastName")
    UserProfileDTO toDtoUserProfileSummary(UserProfile userProfile);

    @Named("gradeCode")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    GradeDTO toDtoGradeCode(Grade grade);
}
