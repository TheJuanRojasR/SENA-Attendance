package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.enumeration.AlertaType;
import java.util.Set;

/**
 * The readable scope of the alert inbox (UC013, A1): an administrator reads every alert, while an
 * instructor reads the consecutive alerts of their materias and the accumulated alerts of their
 * fichas (the scope the alert type was generated for). A restricted scope without ids matches
 * nothing.
 *
 * @param classSectionIds the materias of the instructor, or {@code null} for an unrestricted read.
 * @param gradeIds the fichas of the instructor, or {@code null} for an unrestricted read.
 */
public record AlertaReadScope(Set<String> classSectionIds, Set<String> gradeIds) {
    /**
     * @return the scope of an administrator, who reads every alert.
     */
    public static AlertaReadScope unrestricted() {
        return new AlertaReadScope(null, null);
    }

    /**
     * @return whether the scope places no restriction on the query.
     */
    public boolean isUnrestricted() {
        return classSectionIds == null || gradeIds == null;
    }

    /**
     * @return whether the scope is restricted and holds no readable materia nor ficha.
     */
    public boolean isEmpty() {
        return !isUnrestricted() && classSectionIds.isEmpty() && gradeIds.isEmpty();
    }

    /**
     * @param alertType the type of the alert being read.
     * @param classSectionId the materia of the alert, or {@code null} in an accumulated one.
     * @param gradeId the ficha of the alert.
     * @return whether an alert of that scope is readable.
     */
    public boolean contains(AlertaType alertType, String classSectionId, String gradeId) {
        if (isUnrestricted()) {
            return true;
        }
        if (alertType == AlertaType.CONSECUTIVAS) {
            return classSectionId != null && classSectionIds.contains(classSectionId);
        }
        return gradeId != null && gradeIds.contains(gradeId);
    }
}
