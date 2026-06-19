package com.mycompany.senaattendance.service.mapper;

import com.mycompany.senaattendance.domain.DesertionCounter;
import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.service.dto.DesertionCounterDTO;
import com.mycompany.senaattendance.service.dto.TrimesterDTO;
import com.mycompany.senaattendance.service.dto.UserProfileDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link DesertionCounter} and its DTO {@link DesertionCounterDTO}.
 */
@Mapper(componentModel = "spring")
public interface DesertionCounterMapper extends EntityMapper<DesertionCounterDTO, DesertionCounter> {
    @Mapping(target = "student", source = "student", qualifiedByName = "userProfileDocumentNumber")
    @Mapping(target = "trimester", source = "trimester", qualifiedByName = "trimesterName")
    DesertionCounterDTO toDto(DesertionCounter s);

    @Named("userProfileDocumentNumber")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "documentNumber", source = "documentNumber")
    UserProfileDTO toDtoUserProfileDocumentNumber(UserProfile userProfile);

    @Named("trimesterName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    TrimesterDTO toDtoTrimesterName(Trimester trimester);
}
