package com.mycompany.senaattendance.web.rest.errors;

public class DocumentTypeNotFoundException extends RuntimeException {

    public DocumentTypeNotFoundException(String message) {
        super(message);
    }
}
