package com.mycompany.senaattendance.service.dto.dashboard;

/**
 * The unexcused failures of an apprentice in one ficha during the active trimester (UC023) and
 * the distance left to reach the accumulated alert threshold.
 */
public record GradeFailureDTO(String gradeId, String gradeCode, long unexcusedFailures, int threshold, long missingToThreshold) {}
