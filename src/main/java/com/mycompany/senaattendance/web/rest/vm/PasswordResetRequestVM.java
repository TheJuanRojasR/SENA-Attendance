package com.mycompany.senaattendance.web.rest.vm;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * View Model object for storing the user's key and password.
 */
public class PasswordResetRequestVM {

    @NotNull
    @Size(min = 1, max = 254)
    private String documentTypeId;

    @NotNull
    @Size(min = 1, max = 254)
    private String documentNumber;

    public String getDocumentTypeId() {
        return documentTypeId;
    }

    public void setDocumentTypeId(String documentTypeId) {
        this.documentTypeId = documentTypeId;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }
}
