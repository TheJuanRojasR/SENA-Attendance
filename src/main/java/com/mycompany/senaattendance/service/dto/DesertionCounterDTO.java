package com.mycompany.senaattendance.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.senaattendance.domain.DesertionCounter} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DesertionCounterDTO implements Serializable {

    private String id;

    private Integer totalGlobalAbsences;

    private Integer accumulatedLateArrivals;

    private Integer workJustificationsUsed;

    private Boolean alertsSent;

    @NotNull
    private UserProfileDTO student;

    @NotNull
    private TrimesterDTO trimester;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getTotalGlobalAbsences() {
        return totalGlobalAbsences;
    }

    public void setTotalGlobalAbsences(Integer totalGlobalAbsences) {
        this.totalGlobalAbsences = totalGlobalAbsences;
    }

    public Integer getAccumulatedLateArrivals() {
        return accumulatedLateArrivals;
    }

    public void setAccumulatedLateArrivals(Integer accumulatedLateArrivals) {
        this.accumulatedLateArrivals = accumulatedLateArrivals;
    }

    public Integer getWorkJustificationsUsed() {
        return workJustificationsUsed;
    }

    public void setWorkJustificationsUsed(Integer workJustificationsUsed) {
        this.workJustificationsUsed = workJustificationsUsed;
    }

    public Boolean getAlertsSent() {
        return alertsSent;
    }

    public void setAlertsSent(Boolean alertsSent) {
        this.alertsSent = alertsSent;
    }

    public UserProfileDTO getStudent() {
        return student;
    }

    public void setStudent(UserProfileDTO student) {
        this.student = student;
    }

    public TrimesterDTO getTrimester() {
        return trimester;
    }

    public void setTrimester(TrimesterDTO trimester) {
        this.trimester = trimester;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DesertionCounterDTO)) {
            return false;
        }

        DesertionCounterDTO desertionCounterDTO = (DesertionCounterDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, desertionCounterDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DesertionCounterDTO{" +
            "id='" + getId() + "'" +
            ", totalGlobalAbsences=" + getTotalGlobalAbsences() +
            ", accumulatedLateArrivals=" + getAccumulatedLateArrivals() +
            ", workJustificationsUsed=" + getWorkJustificationsUsed() +
            ", alertsSent='" + getAlertsSent() + "'" +
            ", student=" + getStudent() +
            ", trimester=" + getTrimester() +
            "}";
    }
}
