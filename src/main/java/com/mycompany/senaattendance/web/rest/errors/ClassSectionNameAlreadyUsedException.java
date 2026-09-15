package com.mycompany.senaattendance.web.rest.errors;

import java.io.Serial;

@SuppressWarnings("java:S110") // Inheritance tree of classes should not be too deep
public class ClassSectionNameAlreadyUsedException extends BadRequestAlertException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ClassSectionNameAlreadyUsedException() {
        super(
            ErrorConstants.CLASS_SECTION_NAME_ALREADY_USED_TYPE,
            "Ya existe una materia con este nombre en esta ficha",
            "classSection",
            "classSectionNameAlreadyUsed"
        );
    }
}
