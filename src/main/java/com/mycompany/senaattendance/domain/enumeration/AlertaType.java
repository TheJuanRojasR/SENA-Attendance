package com.mycompany.senaattendance.domain.enumeration;

/**
 * The AlertaType enumeration: the two ways an absence alert is triggered (UC013).
 *
 * <p>{@code CONSECUTIVAS} measures the trailing streak of failures of one materia, so its alert
 * always carries the materia ({@code classSection}); {@code ACUMULADAS} measures the total
 * failures of the apprentice across every materia of the ficha, so its alert carries the ficha
 * ({@code grade}) and no materia.
 */
public enum AlertaType {
    CONSECUTIVAS,
    ACUMULADAS,
}
