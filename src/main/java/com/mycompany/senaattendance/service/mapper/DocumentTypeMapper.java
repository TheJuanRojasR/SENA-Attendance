package com.mycompany.senaattendance.service.mapper;

import com.mycompany.senaattendance.domain.DocumentType;
import com.mycompany.senaattendance.service.dto.DocumentTypeDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link DocumentType} and its DTO {@link DocumentTypeDTO}.
 */
@Mapper(componentModel = "spring")
public interface DocumentTypeMapper extends EntityMapper<DocumentTypeDTO, DocumentType> {}
