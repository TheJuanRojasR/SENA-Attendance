package com.mycompany.senaattendance.service.mapper;

import com.mycompany.senaattendance.domain.Authority;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.service.dto.UserManagementDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserManagementMapper {
    @Mapping(target = "id", source = "userProfile.id")
    @Mapping(target = "fullName", expression = "java(userProfile.getFirstName() + ' ' + userProfile.getFirstLastName())")
    @Mapping(target = "authorities", source = "user.authorities")
    UserManagementDTO toDto(UserProfile userProfile, User user);

    default Set<String> mapAuthorities(Set<Authority> authorities) {
        if (authorities == null) {
            return null;
        }
        return authorities.stream().map(Authority::getName).collect(Collectors.toSet());
    }
}
