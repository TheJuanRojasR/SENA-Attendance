package com.mycompany.senaattendance.service.dto.dashboard;

/**
 * Key Performance Indicators (KPIs) for the admin dashboard.
 */
public record DashboardKpisDTO(long totalUsers, long activeGrades, long totalPrograms, long totalModalities) {}
