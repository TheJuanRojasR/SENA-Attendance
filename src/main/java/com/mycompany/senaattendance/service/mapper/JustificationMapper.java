package com.mycompany.senaattendance.service.mapper;

import com.mycompany.senaattendance.domain.Justification;
import com.mycompany.senaattendance.domain.JustificationType;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.service.dto.JustificationDTO;
import com.mycompany.senaattendance.service.dto.JustificationTypeDTO;
import com.mycompany.senaattendance.service.dto.UserProfileDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Justification} and its DTO {@link JustificationDTO}.
 */
@Mapper(componentModel = "spring")
public interface JustificationMapper extends EntityMapper<JustificationDTO, Justification> {
    @Mapping(target = "justificationType", source = "justificationType", qualifiedByName = "justificationTypeName")
    @Mapping(target = "student", source = "student", qualifiedByName = "userProfileDocumentNumber")
    JustificationDTO toDto(Justification s);

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
}
