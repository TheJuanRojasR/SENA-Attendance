package com.mycompany.senaattendance.web.rest.errors;

import java.io.Serial;

@SuppressWarnings("java:S110") // Inheritance tree of classes should not be too deep
public class GradeStartDateInPastException extends BadRequestAlertException {

    @Serial
    private static final long serialVersionUID = 1L;

    public GradeStartDateInPastException() {
        super(ErrorConstants.GRADE_START_DATE_IN_PAST_TYPE, "La fecha de inicio no puede ser anterior a hoy", "grade", "startdateinpast");
    }
}
