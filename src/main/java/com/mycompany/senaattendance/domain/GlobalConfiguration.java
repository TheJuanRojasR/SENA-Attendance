package com.mycompany.senaattendance.domain;

import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A GlobalConfiguration.
 */
@Document(collection = "global_configuration")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class GlobalConfiguration extends AbstractAuditingEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Field("student_justification_days")
    @Min(value = 1)
    @Max(value = 30)
    private Integer studentJustificationDays;

    @NotNull
    @Field("instructor_response_days")
    @Min(value = 1)
    @Max(value = 30)
    private Integer instructorResponseDays;

    @NotNull
    @Field("late_arrivals_to_fail")
    @Min(value = 1)
    @Max(value = 30)
    private Integer lateArrivalsToFail;

    @NotNull
    @Field("max_postponement_justifications")
    @Min(value = 1)
    @Max(value = 30)
    private Integer maxPostponementJustifications;

    @NotNull
    @Field("standard_trimester_months")
    @Min(value = 1)
    @Max(value = 6)
    private Integer standardTrimesterMonths;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getId() {
        return this.id;
    }

    public GlobalConfiguration id(String id) {
        this.setId(id);
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getStudentJustificationDays() {
        return this.studentJustificationDays;
    }

    public GlobalConfiguration studentJustificationDays(Integer studentJustificationDays) {
        this.setStudentJustificationDays(studentJustificationDays);
        return this;
    }

    public void setStudentJustificationDays(Integer studentJustificationDays) {
        this.studentJustificationDays = studentJustificationDays;
    }

    public Integer getInstructorResponseDays() {
        return this.instructorResponseDays;
    }

    public GlobalConfiguration instructorResponseDays(Integer instructorResponseDays) {
        this.setInstructorResponseDays(instructorResponseDays);
        return this;
    }

    public void setInstructorResponseDays(Integer instructorResponseDays) {
        this.instructorResponseDays = instructorResponseDays;
    }

    public Integer getLateArrivalsToFail() {
        return this.lateArrivalsToFail;
    }

    public GlobalConfiguration lateArrivalsToFail(Integer lateArrivalsToFail) {
        this.setLateArrivalsToFail(lateArrivalsToFail);
        return this;
    }

    public void setLateArrivalsToFail(Integer lateArrivalsToFail) {
        this.lateArrivalsToFail = lateArrivalsToFail;
    }

    public Integer getMaxPostponementJustifications() {
        return this.maxPostponementJustifications;
    }

    public GlobalConfiguration maxPostponementJustifications(Integer maxPostponementJustifications) {
        this.setMaxPostponementJustifications(maxPostponementJustifications);
        return this;
    }

    public void setMaxPostponementJustifications(Integer maxPostponementJustifications) {
        this.maxPostponementJustifications = maxPostponementJustifications;
    }

    public Integer getStandardTrimesterMonths() {
        return this.standardTrimesterMonths;
    }

    public GlobalConfiguration standardTrimesterMonths(Integer standardTrimesterMonths) {
        this.setStandardTrimesterMonths(standardTrimesterMonths);
        return this;
    }

    public void setStandardTrimesterMonths(Integer standardTrimesterMonths) {
        this.standardTrimesterMonths = standardTrimesterMonths;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GlobalConfiguration)) {
            return false;
        }
        return getId() != null && getId().equals(((GlobalConfiguration) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "GlobalConfiguration{" +
            "id=" + getId() +
            ", studentJustificationDays=" + getStudentJustificationDays() +
            ", instructorResponseDays=" + getInstructorResponseDays() +
            ", lateArrivalsToFail=" + getLateArrivalsToFail() +
            ", maxPostponementJustifications=" + getMaxPostponementJustifications() +
            ", standardTrimesterMonths=" + getStandardTrimesterMonths() +
            "}";
    }
}
