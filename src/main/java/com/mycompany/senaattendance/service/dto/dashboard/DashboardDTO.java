package com.mycompany.senaattendance.service.dto.dashboard;

/**
 * Base contract for all dashboard DTOs.
 */
public sealed interface DashboardDTO permits AdminDashboardDTO {
    String role();
}
