package com.mycompany.senaattendance.web.rest.errors;

import java.io.Serial;

@SuppressWarnings("java:S110") // Inheritance tree of classes should not be too deep
public class ProgramInitialsAlreadyUsedException extends BadRequestAlertException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ProgramInitialsAlreadyUsedException() {
        super(ErrorConstants.PROGRAM_INITIALS_ALREADY_USED_TYPE, "Ya existe un programa con estas siglas", "program", "initialsexists");
    }
}
