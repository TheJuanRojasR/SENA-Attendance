package com.mycompany.senaattendance.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mycompany.senaattendance.domain.enumeration.StateAcademic;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A Apprentice.
 */
@Document(collection = "apprentice")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Apprentice extends AbstractAuditingEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Field("state_academic")
    private StateAcademic stateAcademic;

    @DBRef
    @Field("student")
    @JsonIgnoreProperties(value = { "user", "documentType" }, allowSetters = true)
    private UserProfile student;

    @DBRef
    @Field("grade")
    @JsonIgnoreProperties(value = { "program", "modality", "timeSlot" }, allowSetters = true)
    private Grade grade;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getId() {
        return this.id;
    }

    public Apprentice id(String id) {
        this.setId(id);
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public StateAcademic getStateAcademic() {
        return this.stateAcademic;
    }

    public Apprentice stateAcademic(StateAcademic stateAcademic) {
        this.setStateAcademic(stateAcademic);
        return this;
    }

    public void setStateAcademic(StateAcademic stateAcademic) {
        this.stateAcademic = stateAcademic;
    }

    public UserProfile getStudent() {
        return this.student;
    }

    public void setStudent(UserProfile userProfile) {
        this.student = userProfile;
    }

    public Apprentice student(UserProfile userProfile) {
        this.setStudent(userProfile);
        return this;
    }

    public Grade getGrade() {
        return this.grade;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }

    public Apprentice grade(Grade grade) {
        this.setGrade(grade);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Apprentice)) {
            return false;
        }
        return getId() != null && getId().equals(((Apprentice) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Apprentice{" +
            "id=" + getId() +
            ", stateAcademic='" + getStateAcademic() + "'" +
            "}";
    }
}
