package com.mycompany.senaattendance.service.dto.dashboard;

import java.util.List;

/**
 * The justifications of an apprentice grouped by state (UC023), highlighting the rejected parts
 * that are still inside their correction window.
 */
public record JustificationSummaryDTO(long pending, long approved, long rejected, List<JustificationDeadlineDTO> withinCorrectionWindow) {}
