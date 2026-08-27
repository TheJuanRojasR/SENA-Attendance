package com.mycompany.senaattendance.service.dto;

import java.util.Set;

public class UserManagementDTO {

    private String id;

    private String fullName;

    private String documentNumber;

    private String email;

    private Set<String> authorities;

    private boolean activated;

    public UserManagementDTO() {}

    public UserManagementDTO(String id, String fullName, String documentNumber, String email, Set<String> authorities, boolean activated) {
        this.id = id;
        this.fullName = fullName;
        this.documentNumber = documentNumber;
        this.email = email;
        this.authorities = authorities;
        this.activated = activated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }
}
