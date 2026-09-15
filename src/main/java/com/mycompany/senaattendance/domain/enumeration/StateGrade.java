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
    CANCELADA,
}
