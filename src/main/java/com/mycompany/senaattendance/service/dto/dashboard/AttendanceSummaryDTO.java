package com.mycompany.senaattendance.service.dto.dashboard;

/**
 * The attendance summary of an apprentice in the active trimester (UC023): the totals of the
 * recorded sessions per state (A, F, J) and the percentage of attendance over that total.
 */
public record AttendanceSummaryDTO(long present, long failure, long justified, double percentage) {}
