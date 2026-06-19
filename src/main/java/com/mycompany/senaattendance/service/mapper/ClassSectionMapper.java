package com.mycompany.senaattendance.service.mapper;

import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.service.dto.ClassSectionDTO;
import com.mycompany.senaattendance.service.dto.GradeDTO;
import com.mycompany.senaattendance.service.dto.UserProfileDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ClassSection} and its DTO {@link ClassSectionDTO}.
 */
@Mapper(componentModel = "spring")
public interface ClassSectionMapper extends EntityMapper<ClassSectionDTO, ClassSection> {
    @Mapping(target = "instructor", source = "instructor", qualifiedByName = "userProfileDocumentNumber")
    @Mapping(target = "grade", source = "grade", qualifiedByName = "gradeCode")
    ClassSectionDTO toDto(ClassSection s);

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
