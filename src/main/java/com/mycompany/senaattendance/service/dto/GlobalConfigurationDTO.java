package com.mycompany.senaattendance.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.senaattendance.domain.GlobalConfiguration} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class GlobalConfigurationDTO implements Serializable {

    private String id;

    @Min(1)
    private Integer studentJustificationDays;

    @Min(1)
    private Integer instructorResponseDays;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getStudentJustificationDays() {
        return studentJustificationDays;
    }

    public void setStudentJustificationDays(Integer studentJustificationDays) {
        this.studentJustificationDays = studentJustificationDays;
    }

    public Integer getInstructorResponseDays() {
        return instructorResponseDays;
    }

    public void setInstructorResponseDays(Integer instructorResponseDays) {
        this.instructorResponseDays = instructorResponseDays;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GlobalConfigurationDTO)) {
            return false;
        }

        GlobalConfigurationDTO globalConfigurationDTO = (GlobalConfigurationDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, globalConfigurationDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "GlobalConfigurationDTO{" +
            "id='" + getId() + "'" +
            ", studentJustificationDays=" + getStudentJustificationDays() +
            ", instructorResponseDays=" + getInstructorResponseDays() +
            "}";
    }
}
