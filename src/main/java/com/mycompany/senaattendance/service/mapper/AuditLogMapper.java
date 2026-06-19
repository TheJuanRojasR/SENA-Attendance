package com.mycompany.senaattendance.service.mapper;

import com.mycompany.senaattendance.domain.Attendance;
import com.mycompany.senaattendance.domain.AuditLog;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.service.dto.AttendanceDTO;
import com.mycompany.senaattendance.service.dto.AuditLogDTO;
import com.mycompany.senaattendance.service.dto.UserProfileDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link AuditLog} and its DTO {@link AuditLogDTO}.
 */
@Mapper(componentModel = "spring")
public interface AuditLogMapper extends EntityMapper<AuditLogDTO, AuditLog> {
    @Mapping(target = "modifiedBy", source = "modifiedBy", qualifiedByName = "userProfileDocumentNumber")
    @Mapping(target = "attendance", source = "attendance", qualifiedByName = "attendanceDate")
    AuditLogDTO toDto(AuditLog s);

    @Named("userProfileDocumentNumber")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "documentNumber", source = "documentNumber")
    UserProfileDTO toDtoUserProfileDocumentNumber(UserProfile userProfile);

    @Named("attendanceDate")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "date", source = "date")
    AttendanceDTO toDtoAttendanceDate(Attendance attendance);
}
