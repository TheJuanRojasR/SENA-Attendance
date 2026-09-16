package com.mycompany.senaattendance.service.dto.dashboard;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A concrete class session of the role dashboards (UC023): a scheduled session of a materia
 * expanded to the date it takes place, already discounted from the non-teaching exceptions.
 */
public record DashboardClassSessionDTO(
    String classSectionId,
    String subjectName,
    String gradeCode,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime
) {}
