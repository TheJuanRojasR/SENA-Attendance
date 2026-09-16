package com.mycompany.senaattendance.service.dto.dashboard;

import java.time.LocalDate;

/**
 * A rejected justification part that can still be corrected (UC023, UC011 A5/E5): its deadline
 * and the business days left, where zero means the deadline is today.
 */
public record JustificationDeadlineDTO(String id, String subjectName, LocalDate deadline, long remainingBusinessDays) {}
