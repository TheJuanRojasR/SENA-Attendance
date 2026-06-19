package com.mycompany.senaattendance.service.dto;

import com.mycompany.senaattendance.domain.enumeration.StateAttendance;
import com.mycompany.senaattendance.domain.enumeration.StateAttendance;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.senaattendance.domain.AuditLog} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AuditLogDTO implements Serializable {

    private String id;

    @NotNull
    private StateAttendance previousState;

    @NotNull
    private StateAttendance newState;

    @NotNull
    private Instant editDate;

    @NotNull
    private UserProfileDTO modifiedBy;

    @NotNull
    private AttendanceDTO attendance;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public StateAttendance getPreviousState() {
        return previousState;
    }

    public void setPreviousState(StateAttendance previousState) {
        this.previousState = previousState;
    }

    public StateAttendance getNewState() {
        return newState;
    }

    public void setNewState(StateAttendance newState) {
        this.newState = newState;
    }

    public Instant getEditDate() {
        return editDate;
    }

    public void setEditDate(Instant editDate) {
        this.editDate = editDate;
    }

    public UserProfileDTO getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(UserProfileDTO modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public AttendanceDTO getAttendance() {
        return attendance;
    }

    public void setAttendance(AttendanceDTO attendance) {
        this.attendance = attendance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuditLogDTO)) {
            return false;
        }

        AuditLogDTO auditLogDTO = (AuditLogDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, auditLogDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AuditLogDTO{" +
            "id='" + getId() + "'" +
            ", previousState='" + getPreviousState() + "'" +
            ", newState='" + getNewState() + "'" +
            ", editDate='" + getEditDate() + "'" +
            ", modifiedBy=" + getModifiedBy() +
            ", attendance=" + getAttendance() +
            "}";
    }
}
