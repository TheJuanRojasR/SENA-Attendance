package com.mycompany.senaattendance.web.rest.errors;

import java.io.Serial;

@SuppressWarnings("java:S110") // Inheritance tree of classes should not be too deep
public class TrimesterAttendanceStartDateException extends BadRequestAlertException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TrimesterAttendanceStartDateException() {
        super(
            ErrorConstants.TRIMESTER_ATTENDANCE_START_DATE_TYPE,
            "No se puede modificar la fecha de inicio porque el trimestre tiene registros de asistencia",
            "trimester",
            "attendancestartdate"
        );
    }
}
