package com.mycompany.senaattendance.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mycompany.senaattendance.domain.enumeration.StateAttendance;
import com.mycompany.senaattendance.domain.enumeration.StateAttendance;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A AuditLog.
 */
@Document(collection = "audit_log")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AuditLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Field("previous_state")
    private StateAttendance previousState;

    @NotNull
    @Field("new_state")
    private StateAttendance newState;

    @NotNull
    @Field("edit_date")
    private Instant editDate;

    @DBRef
    @Field("modifiedBy")
    @JsonIgnoreProperties(value = { "user", "documentType" }, allowSetters = true)
    private UserProfile modifiedBy;

    @DBRef
    @Field("attendance")
    @JsonIgnoreProperties(value = { "auditLogses", "classSection", "student", "modifiedByJustification" }, allowSetters = true)
    private Attendance attendance;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getId() {
        return this.id;
    }

    public AuditLog id(String id) {
        this.setId(id);
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public StateAttendance getPreviousState() {
        return this.previousState;
    }

    public AuditLog previousState(StateAttendance previousState) {
        this.setPreviousState(previousState);
        return this;
    }

    public void setPreviousState(StateAttendance previousState) {
        this.previousState = previousState;
    }

    public StateAttendance getNewState() {
        return this.newState;
    }

    public AuditLog newState(StateAttendance newState) {
        this.setNewState(newState);
        return this;
    }

    public void setNewState(StateAttendance newState) {
        this.newState = newState;
    }

    public Instant getEditDate() {
        return this.editDate;
    }

    public AuditLog editDate(Instant editDate) {
        this.setEditDate(editDate);
        return this;
    }

    public void setEditDate(Instant editDate) {
        this.editDate = editDate;
    }

    public UserProfile getModifiedBy() {
        return this.modifiedBy;
    }

    public void setModifiedBy(UserProfile userProfile) {
        this.modifiedBy = userProfile;
    }

    public AuditLog modifiedBy(UserProfile userProfile) {
        this.setModifiedBy(userProfile);
        return this;
    }

    public Attendance getAttendance() {
        return this.attendance;
    }

    public void setAttendance(Attendance attendance) {
        this.attendance = attendance;
    }

    public AuditLog attendance(Attendance attendance) {
        this.setAttendance(attendance);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuditLog)) {
            return false;
        }
        return getId() != null && getId().equals(((AuditLog) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AuditLog{" +
            "id=" + getId() +
            ", previousState='" + getPreviousState() + "'" +
            ", newState='" + getNewState() + "'" +
            ", editDate='" + getEditDate() + "'" +
            "}";
    }
}
