package com.mycompany.senaattendance.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mycompany.senaattendance.domain.enumeration.StateAttendance;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A Attendance.
 */
@Document(collection = "attendance")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Attendance implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Field("date")
    private LocalDate date;

    @NotNull
    @Field("state_attendance")
    private StateAttendance stateAttendance;

    @DBRef
    @Field("auditLogs")
    @JsonIgnoreProperties(value = { "modifiedBy", "attendance" }, allowSetters = true)
    private Set<AuditLog> auditLogses = new HashSet<>();

    @DBRef
    @Field("classSection")
    @JsonIgnoreProperties(value = { "scheduleses", "exceptionses", "instructor", "grade" }, allowSetters = true)
    private ClassSection classSection;

    @DBRef
    @Field("student")
    @JsonIgnoreProperties(value = { "user", "documentType" }, allowSetters = true)
    private UserProfile student;

    @DBRef
    @Field("modifiedByJustification")
    @JsonIgnoreProperties(value = { "detailses", "justificationType", "student" }, allowSetters = true)
    private Justification modifiedByJustification;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getId() {
        return this.id;
    }

    public Attendance id(String id) {
        this.setId(id);
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public Attendance date(LocalDate date) {
        this.setDate(date);
        return this;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public StateAttendance getStateAttendance() {
        return this.stateAttendance;
    }

    public Attendance stateAttendance(StateAttendance stateAttendance) {
        this.setStateAttendance(stateAttendance);
        return this;
    }

    public void setStateAttendance(StateAttendance stateAttendance) {
        this.stateAttendance = stateAttendance;
    }

    public Set<AuditLog> getAuditLogses() {
        return this.auditLogses;
    }

    public void setAuditLogses(Set<AuditLog> auditLogs) {
        if (this.auditLogses != null) {
            this.auditLogses.forEach(i -> i.setAttendance(null));
        }
        if (auditLogs != null) {
            auditLogs.forEach(i -> i.setAttendance(this));
        }
        this.auditLogses = auditLogs;
    }

    public Attendance auditLogses(Set<AuditLog> auditLogs) {
        this.setAuditLogses(auditLogs);
        return this;
    }

    public Attendance addAuditLogs(AuditLog auditLog) {
        this.auditLogses.add(auditLog);
        auditLog.setAttendance(this);
        return this;
    }

    public Attendance removeAuditLogs(AuditLog auditLog) {
        this.auditLogses.remove(auditLog);
        auditLog.setAttendance(null);
        return this;
    }

    public ClassSection getClassSection() {
        return this.classSection;
    }

    public void setClassSection(ClassSection classSection) {
        this.classSection = classSection;
    }

    public Attendance classSection(ClassSection classSection) {
        this.setClassSection(classSection);
        return this;
    }

    public UserProfile getStudent() {
        return this.student;
    }

    public void setStudent(UserProfile userProfile) {
        this.student = userProfile;
    }

    public Attendance student(UserProfile userProfile) {
        this.setStudent(userProfile);
        return this;
    }

    public Justification getModifiedByJustification() {
        return this.modifiedByJustification;
    }

    public void setModifiedByJustification(Justification justification) {
        this.modifiedByJustification = justification;
    }

    public Attendance modifiedByJustification(Justification justification) {
        this.setModifiedByJustification(justification);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Attendance)) {
            return false;
        }
        return getId() != null && getId().equals(((Attendance) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Attendance{" +
            "id=" + getId() +
            ", date='" + getDate() + "'" +
            ", stateAttendance='" + getStateAttendance() + "'" +
            "}";
    }
}
