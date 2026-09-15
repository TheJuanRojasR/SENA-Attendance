package com.mycompany.senaattendance.service.mapper;

import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.Program;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.service.dto.ClassSectionDTO;
import com.mycompany.senaattendance.service.dto.GradeDTO;
import com.mycompany.senaattendance.service.dto.ProgramDTO;
import com.mycompany.senaattendance.service.dto.UserProfileDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ClassSection} and its DTO {@link ClassSectionDTO}.
 */
@Mapper(componentModel = "spring")
public interface ClassSectionMapper extends EntityMapper<ClassSectionDTO, ClassSection> {
    @Mapping(target = "instructor", source = "instructor", qualifiedByName = "userProfileDocumentNumber")
    @Mapping(target = "grade", source = "grade", qualifiedByName = "gradeSummary")
    ClassSectionDTO toDto(ClassSection s);

    @Named("userProfileDocumentNumber")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "documentNumber", source = "documentNumber")
    UserProfileDTO toDtoUserProfileDocumentNumber(UserProfile userProfile);

    /**
     * Summarizes the ficha of a class section with what the instructor's own class section view
     * needs to show it: identifier, current state, dates and program. The state is exposed for
     * every ficha, including the non-operable ones, which are listed rather than filtered out.
     *
     * @param grade the ficha to summarize.
     * @return the ficha summary.
     */
    @Named("gradeSummary")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "state", source = "state")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    @Mapping(target = "program", source = "program", qualifiedByName = "programName")
    GradeDTO toDtoGradeSummary(Grade grade);

    /**
     * @param program the program of a ficha.
     * @return the program identified by id and name.
     */
    @Named("programName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    ProgramDTO toDtoProgramName(Program program);
}
