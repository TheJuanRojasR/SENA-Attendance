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

    @NotNull
    private Integer studentJustificationDays;

    @NotNull
    private Integer instructorResponseDays;

    @NotNull
    private Integer lateArrivalsToFail;

    @NotNull
    private Integer maxPostponementJustifications;

    @NotNull
    private Integer standardTrimesterMonths;

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

    public Integer getLateArrivalsToFail() {
        return lateArrivalsToFail;
    }

    public void setLateArrivalsToFail(Integer lateArrivalsToFail) {
        this.lateArrivalsToFail = lateArrivalsToFail;
    }

    public Integer getMaxPostponementJustifications() {
        return maxPostponementJustifications;
    }

    public void setMaxPostponementJustifications(Integer maxPostponementJustifications) {
        this.maxPostponementJustifications = maxPostponementJustifications;
    }

    public Integer getStandardTrimesterMonths() {
        return standardTrimesterMonths;
    }

    public void setStandardTrimesterMonths(Integer standardTrimesterMonths) {
        this.standardTrimesterMonths = standardTrimesterMonths;
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
            ", lateArrivalsToFail=" + getLateArrivalsToFail() +
            ", maxPostponementJustifications=" + getMaxPostponementJustifications() +
            ", standardTrimesterMonths=" + getStandardTrimesterMonths() +
            "}";
    }
}
