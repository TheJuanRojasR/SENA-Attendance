package com.mycompany.senaattendance.service.mapper;

import com.mycompany.senaattendance.domain.Alerta;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.service.dto.AlertaDTO;
import com.mycompany.senaattendance.service.dto.ClassSectionDTO;
import com.mycompany.senaattendance.service.dto.GradeDTO;
import com.mycompany.senaattendance.service.dto.TrimesterDTO;
import com.mycompany.senaattendance.service.dto.UserProfileDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper for the entity {@link Alerta} and its DTO {@link AlertaDTO}. The references travel as
 * compact DTOs with the identity the inbox shows, so the whole chain of the alert is not
 * serialized.
 */
@Mapper(componentModel = "spring")
public interface AlertaMapper extends EntityMapper<AlertaDTO, Alerta> {
    @Override
    @Mapping(target = "student", source = "student", qualifiedByName = "userProfileIdentity")
    @Mapping(target = "classSection", source = "classSection", qualifiedByName = "classSectionIdentity")
    @Mapping(target = "grade", source = "grade", qualifiedByName = "gradeIdentity")
    @Mapping(target = "trimester", source = "trimester", qualifiedByName = "trimesterIdentity")
    AlertaDTO toDto(Alerta s);

    /**
     * The alert is server-owned: a payload never replaces its relationships, so only its scalar
     * fields could be copied through this mapper.
     */
    @Override
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "classSection", ignore = true)
    @Mapping(target = "grade", ignore = true)
    @Mapping(target = "trimester", ignore = true)
    Alerta toEntity(AlertaDTO s);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "classSection", ignore = true)
    @Mapping(target = "grade", ignore = true)
    @Mapping(target = "trimester", ignore = true)
    void partialUpdate(@MappingTarget Alerta entity, AlertaDTO dto);

    @Named("userProfileIdentity")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "documentNumber", source = "documentNumber")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "firstLastName", source = "firstLastName")
    UserProfileDTO toDtoUserProfileIdentity(UserProfile userProfile);

    @Named("classSectionIdentity")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "subjectName", source = "subjectName")
    ClassSectionDTO toDtoClassSectionIdentity(ClassSection classSection);

    @Named("gradeIdentity")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    GradeDTO toDtoGradeIdentity(Grade grade);

    @Named("trimesterIdentity")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    TrimesterDTO toDtoTrimesterIdentity(Trimester trimester);
}
