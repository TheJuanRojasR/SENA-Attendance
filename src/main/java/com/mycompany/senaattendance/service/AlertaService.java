package com.mycompany.senaattendance.service;

import java.time.LocalDate;

/**
 * Service Interface for evaluating the absence alerts of an apprentice (UC013).
 */
public interface AlertaService {
    /**
     * Evaluates the absence alerts of one apprentice after a mark is registered or a
     * justification is approved, and generates the alerts whose threshold is reached.
     *
     * <p>The window is the trimester that contains {@code referenceDate}: every count starts at
     * its {@code startDate} and ends at {@code referenceDate}. The evaluation covers both alert
     * types:
     *
     * <ul>
     *     <li><b>Consecutivas</b> (per materia): the trailing streak of {@code FALLA} over the
     *     programmed sessions of the materia. A programmed session is a weekday of the materia
     *     schedule, and the dates marked as a non-teaching exception of the materia are removed
     *     from the sequence. A {@code PRESENTE} or a {@code JUSTIFICADA} cuts the streak; a
     *     session without a record neither cuts it nor adds to it.</li>
     *     <li><b>Acumuladas</b> (per ficha): the total {@code FALLA} of the apprentice in every
     *     materia of the ficha. A {@code JUSTIFICADA} does not count.</li>
     * </ul>
     *
     * <p>The thresholds come from {@link com.mycompany.senaattendance.domain.GlobalConfiguration}
     * and fall back to its defaults when missing (E4). A combination that already has an active
     * alert is not generated again (E1); an apprentice who is not matriculado in the ficha of the
     * materia generates no new alerts (E2).
     *
     * @param studentId the apprentice profile id.
     * @param classSectionId the materia whose session or justification triggered the evaluation.
     * @param referenceDate the day the evaluation is anchored to.
     */
    void evaluate(String studentId, String classSectionId, LocalDate referenceDate);

    /**
     * Re-evaluates the active alerts of one apprentice after an approved justification lowered
     * their failures, and resolves automatically the ones that stayed below their threshold
     * (UC013, A4). The resolved alert keeps its history with the resolution instant, and the
     * change is notified through the alert channel.
     *
     * <p>Only the alerts that are already active are revisited: an approval never generates a new
     * alert. The counts are measured over the same windows as {@link #evaluate}, anchored to
     * {@code referenceDate}; an apprentice who is no longer matriculado keeps their alerts (E2),
     * so the resolution does not depend on the enrollment.
     *
     * @param studentId the apprentice profile id.
     * @param classSectionId the materia whose justification was approved.
     * @param referenceDate the day the evaluation is anchored to.
     */
    void resolveBelowThreshold(String studentId, String classSectionId, LocalDate referenceDate);
}
