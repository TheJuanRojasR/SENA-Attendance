package com.mycompany.senaattendance.web.rest.errors;

public class DocumentNumberAlreadyUsedException extends BadRequestAlertException {

    public DocumentNumberAlreadyUsedException(String message) {
        super(ErrorConstants.DOCUMENT_NUMBER_ALREADY_USED, "Document number is already in use", "userManagement", "documentnumberexists");
    }
}
