package com.mycompany.senaattendance.service.mapper;

import com.mycompany.senaattendance.domain.ClassException;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.service.dto.ClassExceptionDTO;
import com.mycompany.senaattendance.service.dto.ClassSectionDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ClassException} and its DTO {@link ClassExceptionDTO}.
 */
@Mapper(componentModel = "spring")
public interface ClassExceptionMapper extends EntityMapper<ClassExceptionDTO, ClassException> {
    @Mapping(target = "classSection", source = "classSection", qualifiedByName = "classSectionSubjectName")
    ClassExceptionDTO toDto(ClassException s);

    @Named("classSectionSubjectName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "subjectName", source = "subjectName")
    ClassSectionDTO toDtoClassSectionSubjectName(ClassSection classSection);
}
