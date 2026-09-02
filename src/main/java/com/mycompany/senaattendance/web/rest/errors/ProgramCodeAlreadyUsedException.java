package com.mycompany.senaattendance.web.rest.errors;

import java.io.Serial;

@SuppressWarnings("java:S110") // Inheritance tree of classes should not be too deep
public class ProgramCodeAlreadyUsedException extends BadRequestAlertException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ProgramCodeAlreadyUsedException() {
        super(ErrorConstants.PROGRAM_CODE_ALREADY_USED_TYPE, "Ya existe un programa con este código", "program", "codeexists");
    }
}
