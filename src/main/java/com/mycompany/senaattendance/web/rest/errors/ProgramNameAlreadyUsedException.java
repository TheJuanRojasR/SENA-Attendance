package com.mycompany.senaattendance.web.rest.errors;

import java.io.Serial;

@SuppressWarnings("java:S110") // Inheritance tree of classes should not be too deep
public class ProgramNameAlreadyUsedException extends BadRequestAlertException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ProgramNameAlreadyUsedException() {
        super(ErrorConstants.PROGRAM_NAME_ALREADY_USED_TYPE, "Ya existe un programa con este nombre", "program", "nameexists");
    }
}
