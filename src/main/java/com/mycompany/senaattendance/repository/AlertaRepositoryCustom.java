package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.Alerta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Custom fragment of {@link AlertaRepository} that resolves the alert inbox (UC013, A1) in one
 * dynamic query, so the optional filters and the readable scope of the current user are combined
 * without one repository method per filter combination. The references are DBRefs, so they are
 * matched by their referenced id ({@code $id}) with explicit {@code ObjectId} values.
 */
public interface AlertaRepositoryCustom {
    /**
     * Searches the alerts by the optional filters and the readable scope.
     *
     * @param criteria the optional filters; a {@code null} field means no constraint.
     * @param scope the readable scope of the current user, or {@code null} for an unrestricted
     *              read.
     * @param pageable the pagination information.
     * @return the page of matching alerts, or an empty page when a filter or a scope id is not a
     *         usable {@code ObjectId}.
     */
    Page<Alerta> searchAlerts(AlertaSearchCriteria criteria, AlertaReadScope scope, Pageable pageable);

    /**
     * Counts the active alerts of a readable scope and, optionally, of one apprentice. An active
     * alert is any alert that is not {@code RESUELTA_AUTOMATICAMENTE}, because a resolved alert is
     * history (UC013, E1). Used by the role dashboards to report how many alerts affect the
     * current user (UC023).
     *
     * @param scope the readable scope of the current user, or {@code null} for an unrestricted
     *              read.
     * @param studentId the apprentice profile id to count for, or {@code null} for every
     *                  apprentice.
     * @return the number of matching active alerts, or zero when a scope or a student id is not
     *         a usable {@code ObjectId}.
     */
    long countActiveAlerts(AlertaReadScope scope, String studentId);
}
