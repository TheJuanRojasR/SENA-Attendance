package com.mycompany.senaattendance.web.rest.vm;

import com.mycompany.senaattendance.service.dto.AdminUserDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * View Model used by the ADMIN to create a complete user
 * (User + UserProfile) with a single role and a chosen password.
 * Kept separate from {@link ManagedUserVM} because the public
 * registration flow must not expose or accept a "role".
 */
public class AdminCreateUserVM extends AdminUserDTO {

    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 20;

    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
    private String password;

    @NotNull
    @Size(min = 1, max = 30)
    private String firstName;

    @Size(min = 1, max = 30)
    private String middleName;

    @NotNull
    @Size(min = 1, max = 30)
    private String firstLastName;

    @Size(min = 1, max = 30)
    private String secondLastName;

    @NotNull
    @Size(min = 1, max = 20)
    private String documentNumber;

    @NotNull
    @Size(min = 1, max = 20)
    private String phoneNumber;

    @NotNull
    @Size(min = 1)
    private String documentTypeId;

    @Size(min = 1, max = 50)
    private String role;

    public AdminCreateUserVM() {}

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

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
        return "AdminCreateUserVM{" + super.toString() + "}";
    }
}
