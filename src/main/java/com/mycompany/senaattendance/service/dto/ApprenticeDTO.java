package com.mycompany.senaattendance.service.dto;

import com.mycompany.senaattendance.domain.enumeration.StateAcademic;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.senaattendance.domain.Apprentice} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ApprenticeDTO implements Serializable {

    private String id;

    @NotNull
    private StateAcademic stateAcademic;

    @NotNull
    private UserProfileDTO student;

    @NotNull
    private GradeDTO grade;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public StateAcademic getStateAcademic() {
        return stateAcademic;
    }

    public void setStateAcademic(StateAcademic stateAcademic) {
        this.stateAcademic = stateAcademic;
    }

    public UserProfileDTO getStudent() {
        return student;
    }

    public void setStudent(UserProfileDTO student) {
        this.student = student;
    }

    public GradeDTO getGrade() {
        return grade;
    }

    public void setGrade(GradeDTO grade) {
        this.grade = grade;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ApprenticeDTO)) {
            return false;
        }

        ApprenticeDTO apprenticeDTO = (ApprenticeDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, apprenticeDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ApprenticeDTO{" +
            "id='" + getId() + "'" +
            ", stateAcademic='" + getStateAcademic() + "'" +
            ", student=" + getStudent() +
            ", grade=" + getGrade() +
            "}";
    }
}
