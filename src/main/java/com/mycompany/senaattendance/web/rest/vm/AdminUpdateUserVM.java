package com.mycompany.senaattendance.web.rest.vm;

import com.mycompany.senaattendance.service.dto.AdminUserDTO;
import jakarta.validation.constraints.Size;

/**
 * View Model used by the ADMIN to update an existing user
 * (User + UserProfile). Carries the profile fields, the derived login
 * source (documentNumber) and a single role. All fields are OPTIONAL so the
 * client can send a partial {@code PATCH} payload: only the fields actually
 * present are updated. The inherited {@code login}, {@code activated} and
 * {@code authorities} fields are IGNORED server-side.
 * No password is accepted on edit.
 */
public class AdminUpdateUserVM extends AdminUserDTO {

    @Size(min = 1, max = 30)
    private String firstName;

    @Size(min = 1, max = 30)
    private String middleName;

    @Size(min = 1, max = 30)
    private String firstLastName;

    @Size(min = 1, max = 30)
    private String secondLastName;

    @Size(min = 1, max = 15)
    private String documentNumber;

    @Size(min = 1, max = 20)
    private String phoneNumber;

    @Size(min = 1)
    private String documentTypeId;

    @Size(min = 1, max = 50)
    private String role;

    public AdminUpdateUserVM() {}

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getFirstLastName() {
        return firstLastName;
    }

    public void setFirstLastName(String firstLastName) {
        this.firstLastName = firstLastName;
    }

    public String getSecondLastName() {
        return secondLastName;
    }

    public void setSecondLastName(String secondLastName) {
        this.secondLastName = secondLastName;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDocumentTypeId() {
        return documentTypeId;
    }

    public void setDocumentTypeId(String documentTypeId) {
        this.documentTypeId = documentTypeId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "AdminUpdateUserVM{" + super.toString() + "}";
    }
}
