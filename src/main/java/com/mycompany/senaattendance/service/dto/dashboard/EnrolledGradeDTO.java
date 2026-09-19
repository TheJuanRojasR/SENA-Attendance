package com.mycompany.senaattendance.service.dto.dashboard;

import java.util.List;

/**
 * One ficha the apprentice is enrolled in and its materias (UC023).
 */
public record EnrolledGradeDTO(String gradeId, String gradeCode, String programName, List<EnrolledSubjectDTO> subjects) {}
