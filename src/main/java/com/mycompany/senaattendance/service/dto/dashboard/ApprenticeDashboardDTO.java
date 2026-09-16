package com.mycompany.senaattendance.service.dto.dashboard;

import com.mycompany.senaattendance.security.AuthoritiesConstants;
import java.util.List;

/**
 * A Data Transfer Object (DTO) representing the apprentice dashboard (UC023). The trimester
 * dependent indicators ({@code attendance}, {@code failuresByGrade} and {@code upcomingClasses})
 * travel empty when there is no active trimester and {@code trimesterMessage} carries the reason.
 */
public record ApprenticeDashboardDTO(
    AttendanceSummaryDTO attendance,
    List<GradeFailureDTO> failuresByGrade,
    JustificationSummaryDTO justifications,
    List<EnrolledGradeDTO> grades,
    List<DashboardClassSessionDTO> upcomingClasses,
    long activeAlerts,
    String trimesterMessage
) implements DashboardDTO {
    @Override
    public String role() {
        return AuthoritiesConstants.APPRENTICE;
    }
}
