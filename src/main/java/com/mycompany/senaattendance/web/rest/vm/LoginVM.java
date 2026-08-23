package com.mycompany.senaattendance.web.rest.vm;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * View Model object for storing a user's credentials.
 */
public class LoginVM {

    @NotNull
    @Size(min = 1, max = 254)
    private String documentTypeId;

    @NotNull
    @Size(min = 1, max = 254)
    private String documentNumber;

    @NotNull
    @Size(min = 4, max = 100)
    private String password;

    private boolean rememberMe;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "LoginVM{" +
            "documentTypeId='" + documentTypeId + '\'' +
            ", documentNumber='" + documentNumber + '\'' +
            ", rememberMe=" + rememberMe +
            '}';
    }
}
