package com.mycompany.senaattendance.domain.enumeration;

/**
 * The StateGrade enumeration: the lifecycle states of a ficha (grade).
 *
 * <p>{@code PENDIENTE}, {@code ACTIVA} and {@code FINALIZADA} are derived from the ficha
 * date range; {@code APLAZADA} and {@code CANCELADA} are set manually by an administrator.
 */
public enum StateGrade {
    PENDIENTE,
    ACTIVA,
    FINALIZADA,
    APLAZADA,
    CANCELADA;

    /**
     * An operable ficha is one that still admits new materias (class sections) and whose assigned
     * instructor must keep holding it. This is the single source of truth for that criterion:
     * {@code PENDIENTE} (not started yet) and {@code ACTIVA} (running) are operable;
     * {@code FINALIZADA}, {@code APLAZADA} and {@code CANCELADA} are not.
     *
     * @return {@code true} when this state is {@code PENDIENTE} or {@code ACTIVA}.
     */
    public boolean isOperable() {
        return this == PENDIENTE || this == ACTIVA;
    }
}
