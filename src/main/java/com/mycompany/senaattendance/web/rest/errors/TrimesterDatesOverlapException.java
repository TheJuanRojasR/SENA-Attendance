package com.mycompany.senaattendance.web.rest.errors;

import java.io.Serial;

@SuppressWarnings("java:S110") // Inheritance tree of classes should not be too deep
public class TrimesterDatesOverlapException extends BadRequestAlertException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TrimesterDatesOverlapException() {
        super(ErrorConstants.TRIMESTER_DATES_OVERLAP_TYPE, "Las fechas se solapan con otro trimestre", "trimester", "datesoverlap");
    }
}
