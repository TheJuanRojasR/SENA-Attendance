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
            "}";
    }
}
