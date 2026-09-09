package com.mycompany.senaattendance.web.rest.errors;

import java.io.Serial;

@SuppressWarnings("java:S110") // Inheritance tree of classes should not be too deep
public class TrimesterNotEditableException extends BadRequestAlertException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TrimesterNotEditableException() {
        super(
            ErrorConstants.TRIMESTER_NOT_EDITABLE_TYPE,
            "No se puede editar un trimestre que ya ha finalizado",
            "trimester",
            "noteditable"
        );
    }
}
