package com.mycompany.senaattendance.service.dto.dashboard;

import com.mycompany.senaattendance.domain.enumeration.StateGrade;

/**
 * A Data Transfer Object (DTO) representing a recent grade in the dashboard.
 */
public record RecentGradeDTO(String id, String code, String programName, String instructorName, StateGrade state) {}
