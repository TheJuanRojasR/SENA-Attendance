package com.mycompany.senaattendance.service.dto.dashboard;

import com.mycompany.senaattendance.security.AuthoritiesConstants;
import java.util.List;

/**
 * A Data Transfer Object (DTO) representing the instructor dashboard (UC023). The trimester
 * dependent lists travel empty when there is no active trimester and {@code trimesterMessage}
 * carries the reason.
 */
public record InstructorDashboardDTO(
    long pendingJustifications,
    long activeAlerts,
    long assignedSubjects,
    long assignedGrades,
    long assignedApprentices,
    List<DashboardClassSessionDTO> todayClasses,
    List<DashboardClassSessionDTO> upcomingClasses,
    String trimesterMessage
) implements DashboardDTO {
    @Override
    public String role() {
        return AuthoritiesConstants.INSTRUCTOR;
    }
}
