package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.enumeration.StateAttendance;
import java.time.LocalDate;

/**
 * Optional filters of the attendance history (A1). A {@code null} field means "no constraint" and
 * the present filters are combined with AND. The apprentice is identified by the same profile id
 * the session registration uses; the document number is not stored in the attendance record.
 *
 * @param classSectionId the materia to filter by.
 * @param date the session date to filter by.
 * @param studentId the apprentice profile id to filter by.
 * @param stateAttendance the state to filter by.
 */
public record AttendanceSearchCriteria(String classSectionId, LocalDate date, String studentId, StateAttendance stateAttendance) {}
