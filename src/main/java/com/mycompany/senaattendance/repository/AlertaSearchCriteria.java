package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.enumeration.AlertaState;
import com.mycompany.senaattendance.domain.enumeration.AlertaType;
import java.time.Instant;

/**
 * Optional filters of the alert inbox (UC013, A1). A {@code null} field means "no constraint" and
 * the present filters are combined with AND.
 *
 * @param type the alert type to include.
 * @param state the alert state to include.
 * @param gradeId the ficha to include.
 * @param studentId the apprentice profile to include.
 * @param from the first generation instant to include, inclusive.
 * @param to the last generation instant to include, inclusive.
 */
public record AlertaSearchCriteria(AlertaType type, AlertaState state, String gradeId, String studentId, Instant from, Instant to) {}
