package com.mycompany.senaattendance.service.dto.dashboard;

import com.mycompany.senaattendance.security.AuthoritiesConstants;
import java.util.List;

/**
 * A Data Transfer Object (DTO) representing the admin dashboard.
 */
public record AdminDashboardDTO(DashboardKpisDTO kpis, List<RecentGradeDTO> recentGrades) implements DashboardDTO {
    @Override
    public String role() {
        return AuthoritiesConstants.ADMIN;
    }
}
