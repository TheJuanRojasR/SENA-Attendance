package com.mycompany.senaattendance.service.mapper;

import com.mycompany.senaattendance.domain.TimeSlot;
import com.mycompany.senaattendance.service.dto.TimeSlotDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link TimeSlot} and its DTO {@link TimeSlotDTO}.
 */
@Mapper(componentModel = "spring")
public interface TimeSlotMapper extends EntityMapper<TimeSlotDTO, TimeSlot> {}
