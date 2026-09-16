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
}
