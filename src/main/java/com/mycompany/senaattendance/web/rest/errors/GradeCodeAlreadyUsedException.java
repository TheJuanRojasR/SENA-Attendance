package com.mycompany.senaattendance.web.rest.errors;

import java.io.Serial;

@SuppressWarnings("java:S110") // Inheritance tree of classes should not be too deep
public class GradeCodeAlreadyUsedException extends BadRequestAlertException {

    @Serial
    private static final long serialVersionUID = 1L;

    public GradeCodeAlreadyUsedException() {
        super(ErrorConstants.GRADE_CODE_ALREADY_USED_TYPE, "El código de ficha ya está en uso", "grade", "gradeCodeAlreadyUsed");
    }
}
