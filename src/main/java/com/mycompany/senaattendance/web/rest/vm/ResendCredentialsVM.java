package com.mycompany.senaattendance.web.rest.vm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * View Model used by the ADMIN to resend the access credentials of an existing user account
 * (UC006, E7).
 *
 * Carries the unique document number that identifies the
 * {@link com.mycompany.senaattendance.domain.UserProfile} whose owner receives a fresh reset
 * link, so they choose their own password.
 */
public class ResendCredentialsVM {

    @NotBlank
    @Size(min = 1, max = 15)
    private String documentNumber;

    public ResendCredentialsVM() {
        // Empty constructor needed for Jackson.
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    @Override
    public String toString() {
        return "ResendCredentialsVM{documentNumber='" + documentNumber + "'}";
    }
}
