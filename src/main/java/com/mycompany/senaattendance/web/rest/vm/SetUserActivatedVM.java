package com.mycompany.senaattendance.web.rest.vm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * View Model used by the ADMIN to set the activation status of an existing user
 * account (activate or deactivate).
 *
 * Carries the unique document number that identifies the
 * {@link com.mycompany.senaattendance.domain.UserProfile} and the target
 * {@code activated} value. The associated {@link com.mycompany.senaattendance.domain.User}
 * is set to that value.
 */
public class SetUserActivatedVM {

    @NotBlank
    @Size(min = 1, max = 15)
    private String documentNumber;

    @NotNull
    private Boolean activated;

    public SetUserActivatedVM() {
        // Empty constructor needed for Jackson.
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Boolean getActivated() {
        return activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    @Override
    public String toString() {
        return "SetUserActivatedVM{documentNumber='" + documentNumber + "', activated=" + activated + "}";
    }
}
