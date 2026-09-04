package com.mycompany.senaattendance.web.rest.errors;

import java.io.Serial;

@SuppressWarnings("java:S110") // Inheritance tree of classes should not be too deep
public class TrimesterStartDateMustBeFutureException extends BadRequestAlertException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TrimesterStartDateMustBeFutureException() {
        super(
            ErrorConstants.TRIMESTER_START_DATE_MUST_BE_FUTURE_TYPE,
            "La nueva fecha de inicio debe ser posterior a la fecha actual",
            "trimester",
            "startdatemustbefuture"
        );
    }
}
