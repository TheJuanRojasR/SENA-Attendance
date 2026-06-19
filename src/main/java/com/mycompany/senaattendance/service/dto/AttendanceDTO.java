package com.mycompany.senaattendance.service.dto;

import com.mycompany.senaattendance.domain.enumeration.StateAttendance;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.senaattendance.domain.Attendance} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AttendanceDTO implements Serializable {

    private String id;

    @NotNull
    private LocalDate date;

    @NotNull
    private StateAttendance stateAttendance;

    @NotNull
    private ClassSectionDTO classSection;

    @NotNull
    private UserProfileDTO student;

    private JustificationDTO modifiedByJustification;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public StateAttendance getStateAttendance() {
        return stateAttendance;
    }

    public void setStateAttendance(StateAttendance stateAttendance) {
        this.stateAttendance = stateAttendance;
    }

    public ClassSectionDTO getClassSection() {
        return classSection;
    }

    public void setClassSection(ClassSectionDTO classSection) {
        this.classSection = classSection;
    }

    public UserProfileDTO getStudent() {
        return student;
    }

    public void setStudent(UserProfileDTO student) {
        this.student = student;
    }

    public JustificationDTO getModifiedByJustification() {
        return modifiedByJustification;
    }

    public void setModifiedByJustification(JustificationDTO modifiedByJustification) {
        this.modifiedByJustification = modifiedByJustification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AttendanceDTO)) {
            return false;
        }

        AttendanceDTO attendanceDTO = (AttendanceDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, attendanceDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AttendanceDTO{" +
            "id='" + getId() + "'" +
            ", date='" + getDate() + "'" +
            ", stateAttendance='" + getStateAttendance() + "'" +
            ", classSection=" + getClassSection() +
            ", student=" + getStudent() +
            ", modifiedByJustification=" + getModifiedByJustification() +
            "}";
    }
}
