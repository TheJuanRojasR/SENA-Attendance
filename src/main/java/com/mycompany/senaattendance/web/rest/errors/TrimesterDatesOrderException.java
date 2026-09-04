package com.mycompany.senaattendance.web.rest.errors;

import java.io.Serial;

@SuppressWarnings("java:S110") // Inheritance tree of classes should not be too deep
public class TrimesterDatesOrderException extends BadRequestAlertException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TrimesterDatesOrderException() {
        super(
            ErrorConstants.TRIMESTER_DATES_ORDER_TYPE,
            "La fecha de inicio debe ser anterior a la fecha de fin",
            "trimester",
            "datesorder"
        );
    }
}
