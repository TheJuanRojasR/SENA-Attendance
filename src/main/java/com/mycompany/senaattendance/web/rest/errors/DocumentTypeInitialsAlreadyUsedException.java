package com.mycompany.senaattendance.web.rest.errors;

import java.io.Serial;

@SuppressWarnings("java:S110") // Inheritance tree of classes should not be too deep
public class DocumentTypeInitialsAlreadyUsedException extends BadRequestAlertException {

    @Serial
    private static final long serialVersionUID = 1L;

    public DocumentTypeInitialsAlreadyUsedException() {
        super(
            ErrorConstants.DOCUMENT_TYPE_INITIALS_ALREADY_USED_TYPE,
            "Ya existe un tipo de documento con estas iniciales",
            "documentType",
            "documentTypeInitialsAlreadyUsed"
        );
    }
}
