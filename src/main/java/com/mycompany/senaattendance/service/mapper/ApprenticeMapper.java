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
    @Mapping(target = "student", source = "student", qualifiedByName = "userProfileDocumentNumber")
    @Mapping(target = "grade", source = "grade", qualifiedByName = "gradeCode")
    ApprenticeDTO toDto(Apprentice s);

    @Named("userProfileDocumentNumber")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "documentNumber", source = "documentNumber")
    UserProfileDTO toDtoUserProfileDocumentNumber(UserProfile userProfile);

    @Named("gradeCode")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    GradeDTO toDtoGradeCode(Grade grade);
}
