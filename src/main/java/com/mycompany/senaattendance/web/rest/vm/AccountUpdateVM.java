package com.mycompany.senaattendance.web.rest.vm;

import com.mycompany.senaattendance.config.Constants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AccountUpdateVM {

    // ----- USERPROFILE FIELDS -----
    @Size(min = 1, max = 30)
    private String firstName;

    @Size(min = 1, max = 30)
    private String middleName;

    @Size(min = 1, max = 30)
    private String firstLastName;

    @Size(min = 1, max = 30)
    private String secondLastName;

    @Size(min = 1, max = 30)
    private String phoneNumber;

    // ----- USER FIELDS -----
    @Email
    @Pattern(regexp = Constants.EMAIL_REGEX)
    @Size(min = 5, max = 254)
    private String email;

    @Size(min = 8, max = 20)
    private String currentPassword;

    @Size(min = 8, max = 20)
    private String newPassword;

    @Size(max = 256)
    private String imageUrl;

    @Size(min = 2, max = 10)
    private String langKey;

    // ------ EMPTY CONSTRUCTOR -----
    public AccountUpdateVM() {}

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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLangKey() {
        return langKey;
    }

    public void setLangKey(String langKey) {
        this.langKey = langKey;
    }
}
