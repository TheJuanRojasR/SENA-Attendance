package com.mycompany.senaattendance.web.rest.errors;

import java.io.Serial;

@SuppressWarnings("java:S110") // Inheritance tree of classes should not be too deep
public class JustificationTypeNameAlreadyUsedException extends BadRequestAlertException {

    @Serial
    private static final long serialVersionUID = 1L;

    public JustificationTypeNameAlreadyUsedException() {
        super(
            ErrorConstants.JUSTIFICATION_TYPE_NAME_ALREADY_USED_TYPE,
            "Ya existe un tipo de justificación con este nombre",
            "justificationType",
            "justificationTypeNameAlreadyUsed"
        );
    }
}
