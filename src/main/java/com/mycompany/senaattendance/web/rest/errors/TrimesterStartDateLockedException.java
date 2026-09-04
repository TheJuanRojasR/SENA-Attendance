package com.mycompany.senaattendance.web.rest.errors;

import java.io.Serial;

@SuppressWarnings("java:S110") // Inheritance tree of classes should not be too deep
public class TrimesterStartDateLockedException extends BadRequestAlertException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TrimesterStartDateLockedException() {
        super(
            ErrorConstants.TRIMESTER_START_DATE_LOCKED_TYPE,
            "No se puede modificar la fecha de inicio de un trimestre activo",
            "trimester",
            "startdatelocked"
        );
    }
}
