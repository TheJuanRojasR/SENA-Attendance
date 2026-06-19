package com.mycompany.senaattendance.service.mapper;

import com.mycompany.senaattendance.domain.Modality;
import com.mycompany.senaattendance.service.dto.ModalityDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Modality} and its DTO {@link ModalityDTO}.
 */
@Mapper(componentModel = "spring")
public interface ModalityMapper extends EntityMapper<ModalityDTO, Modality> {}
