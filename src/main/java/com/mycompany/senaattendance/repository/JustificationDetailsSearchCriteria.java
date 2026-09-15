package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.enumeration.StateJustification;
import java.util.List;
import org.bson.types.ObjectId;

/**
 * Optional filters of the instructor justification tray and history (UC010, A1). A {@code null}
 * field means "no constraint" and the present filters are combined with AND.
 *
 * @param stateJustification the state to include; the tray defaults to the pending parts.
 * @param classSectionId the materia to filter by.
 * @param justificationIds the ObjectId values of the justifications whose request date falls
 *        inside the requested range, or {@code null} when no date range was requested.
 */
public record JustificationDetailsSearchCriteria(
    StateJustification stateJustification,
    String classSectionId,
    List<ObjectId> justificationIds
) {}
