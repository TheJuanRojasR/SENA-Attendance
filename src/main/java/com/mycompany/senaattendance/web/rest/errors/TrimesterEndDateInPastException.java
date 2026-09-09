package com.mycompany.senaattendance.web.rest.errors;

import java.io.Serial;

@SuppressWarnings("java:S110") // Inheritance tree of classes should not be too deep
public class TrimesterEndDateInPastException extends BadRequestAlertException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TrimesterEndDateInPastException() {
        super(
            ErrorConstants.TRIMESTER_END_DATE_IN_PAST_TYPE,
            "La fecha de fin no puede ser anterior a la fecha actual",
            "trimester",
            "enddateinpast"
        );
    }
}
