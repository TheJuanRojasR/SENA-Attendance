package com.mycompany.senaattendance.service.dto;

import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import java.io.Serial;

/**
 * A DTO representing a user detail: the account data of {@link AdminUserDTO} plus the profile
 * fields and the assignable role, so the admin edit form (UC006) can prefill every field.
 */
public class AdminUserDetailDTO extends AdminUserDTO {

    @Serial
    private static final long serialVersionUID = 1L;

    private String firstName;

    private String middleName;

    private String firstLastName;

    private String secondLastName;

    private String documentNumber;

    private String phoneNumber;

    private String documentTypeId;

    private String role;

    public AdminUserDetailDTO() {
        // Empty constructor needed for Jackson.
    }

    public AdminUserDetailDTO(User user, UserProfile profile, String role) {
        super(user);
        this.role = role;
        if (profile != null) {
            this.firstName = profile.getFirstName();
            this.middleName = profile.getMiddleName();
            this.firstLastName = profile.getFirstLastName();
            this.secondLastName = profile.getSecondLastName();
            this.documentNumber = profile.getDocumentNumber();
            this.phoneNumber = profile.getPhoneNumber();
            if (profile.getDocumentType() != null) {
                this.documentTypeId = profile.getDocumentType().getId();
            }
        }
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

    // prettier-ignore
    @Override
    public String toString() {
        return "AdminUserDetailDTO{" +
            super.toString() +
            ", firstName='" + firstName + '\'' +
            ", middleName='" + middleName + '\'' +
            ", firstLastName='" + firstLastName + '\'' +
            ", secondLastName='" + secondLastName + '\'' +
            ", documentNumber='" + documentNumber + '\'' +
            ", phoneNumber='" + phoneNumber + '\'' +
            ", documentTypeId='" + documentTypeId + '\'' +
            ", role='" + role + '\'' +
            "}";
    }
}
