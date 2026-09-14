package com.mycompany.senaattendance.web.rest.errors;

import java.io.Serial;

@SuppressWarnings("java:S110") // Inheritance tree of classes should not be too deep
public class ModalityNameAlreadyUsedException extends BadRequestAlertException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ModalityNameAlreadyUsedException() {
        super(
            ErrorConstants.MODALITY_NAME_ALREADY_USED_TYPE,
            "Ya existe una modalidad con este nombre",
            "modality",
            "modalityNameAlreadyUsed"
        );
    }
}
