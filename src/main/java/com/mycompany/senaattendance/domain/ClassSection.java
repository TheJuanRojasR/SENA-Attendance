package com.mycompany.senaattendance.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A ClassSection.
 */
@Document(collection = "class_section")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ClassSection extends AbstractAuditingEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Size(max = 200)
    @Field("subject_name")
    private String subjectName;

    @NotNull
    @Field("is_active")
    private Boolean isActive;

    @DBRef
    @Field("schedules")
    @JsonIgnoreProperties(value = { "trimester", "classSection" }, allowSetters = true)
    private Set<ClassSchedule> scheduleses = new HashSet<>();

    @DBRef
    @Field("exceptions")
    @JsonIgnoreProperties(value = { "classSection" }, allowSetters = true)
    private Set<ClassException> exceptionses = new HashSet<>();

    @DBRef
    @Field("instructor")
    @JsonIgnoreProperties(value = { "user", "documentType" }, allowSetters = true)
    private UserProfile instructor;

    @DBRef
    @Field("grade")
    @JsonIgnoreProperties(value = { "program", "modality", "timeSlot" }, allowSetters = true)
    private Grade grade;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getId() {
        return this.id;
    }

    public ClassSection id(String id) {
        this.setId(id);
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubjectName() {
        return this.subjectName;
    }

    public ClassSection subjectName(String subjectName) {
        this.setSubjectName(subjectName);
        return this;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Boolean getIsActive() {
        return this.isActive;
    }

    public ClassSection isActive(Boolean isActive) {
        this.setIsActive(isActive);
        return this;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Set<ClassSchedule> getScheduleses() {
        return this.scheduleses;
    }

    public void setScheduleses(Set<ClassSchedule> classSchedules) {
        if (this.scheduleses != null) {
            this.scheduleses.forEach(i -> i.setClassSection(null));
        }
        if (classSchedules != null) {
            classSchedules.forEach(i -> i.setClassSection(this));
        }
        this.scheduleses = classSchedules;
    }

    public ClassSection scheduleses(Set<ClassSchedule> classSchedules) {
        this.setScheduleses(classSchedules);
        return this;
    }

    public ClassSection addSchedules(ClassSchedule classSchedule) {
        this.scheduleses.add(classSchedule);
        classSchedule.setClassSection(this);
        return this;
    }

    public ClassSection removeSchedules(ClassSchedule classSchedule) {
        this.scheduleses.remove(classSchedule);
        classSchedule.setClassSection(null);
        return this;
    }

    public Set<ClassException> getExceptionses() {
        return this.exceptionses;
    }

    public void setExceptionses(Set<ClassException> classExceptions) {
        if (this.exceptionses != null) {
            this.exceptionses.forEach(i -> i.setClassSection(null));
        }
        if (classExceptions != null) {
            classExceptions.forEach(i -> i.setClassSection(this));
        }
        this.exceptionses = classExceptions;
    }

    public ClassSection exceptionses(Set<ClassException> classExceptions) {
        this.setExceptionses(classExceptions);
        return this;
    }

    public ClassSection addExceptions(ClassException classException) {
        this.exceptionses.add(classException);
        classException.setClassSection(this);
        return this;
    }

    public ClassSection removeExceptions(ClassException classException) {
        this.exceptionses.remove(classException);
        classException.setClassSection(null);
        return this;
    }

    public UserProfile getInstructor() {
        return this.instructor;
    }

    public void setInstructor(UserProfile userProfile) {
        this.instructor = userProfile;
    }

    public ClassSection instructor(UserProfile userProfile) {
        this.setInstructor(userProfile);
        return this;
    }

    public Grade getGrade() {
        return this.grade;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }

    public ClassSection grade(Grade grade) {
        this.setGrade(grade);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClassSection)) {
            return false;
        }
        return getId() != null && getId().equals(((ClassSection) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ClassSection{" +
            "id=" + getId() +
            ", subjectName='" + getSubjectName() + "'" +
            ", isActive='" + getIsActive() + "'" +
            "}";
    }
}
