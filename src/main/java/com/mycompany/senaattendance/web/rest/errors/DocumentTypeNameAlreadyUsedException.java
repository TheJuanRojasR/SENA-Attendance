package com.mycompany.senaattendance.web.rest.errors;

import java.io.Serial;

@SuppressWarnings("java:S110") // Inheritance tree of classes should not be too deep
public class DocumentTypeNameAlreadyUsedException extends BadRequestAlertException {

    @Serial
    private static final long serialVersionUID = 1L;

    public DocumentTypeNameAlreadyUsedException() {
        super(
            ErrorConstants.DOCUMENT_TYPE_NAME_ALREADY_USED_TYPE,
            "Ya existe un tipo de documento con este nombre",
            "documentType",
            "documentTypeNameAlreadyUsed"
        );
    }
}
