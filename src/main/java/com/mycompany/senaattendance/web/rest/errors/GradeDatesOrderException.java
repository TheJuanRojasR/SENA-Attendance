package com.mycompany.senaattendance.web.rest.errors;

import java.io.Serial;

@SuppressWarnings("java:S110") // Inheritance tree of classes should not be too deep
public class GradeDatesOrderException extends BadRequestAlertException {

    @Serial
    private static final long serialVersionUID = 1L;

    public GradeDatesOrderException() {
        super(ErrorConstants.GRADE_DATES_ORDER_TYPE, "La fecha de fin no puede ser anterior a la fecha de inicio", "grade", "datesorder");
    }
}
