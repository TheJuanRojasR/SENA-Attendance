package com.mycompany.senaattendance.service.mapper;

import com.mycompany.senaattendance.domain.Notificacion;
import com.mycompany.senaattendance.service.dto.NotificacionDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity {@link Notificacion} and its DTO {@link NotificacionDTO}.
 */
@Mapper(componentModel = "spring")
public interface NotificacionMapper extends EntityMapper<NotificacionDTO, Notificacion> {}
