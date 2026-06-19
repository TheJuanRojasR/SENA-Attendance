package com.mycompany.senaattendance.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.senaattendance.domain.ClassSection} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ClassSectionDTO implements Serializable {

    private String id;

    @NotNull
    @Size(max = 200)
    private String subjectName;

    @NotNull
    private Boolean isActive;

    @NotNull
    private UserProfileDTO instructor;

    @NotNull
    private GradeDTO grade;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public UserProfileDTO getInstructor() {
        return instructor;
    }

    public void setInstructor(UserProfileDTO instructor) {
        this.instructor = instructor;
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
        if (!(o instanceof ClassSectionDTO)) {
            return false;
        }

        ClassSectionDTO classSectionDTO = (ClassSectionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, classSectionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ClassSectionDTO{" +
            "id='" + getId() + "'" +
            ", subjectName='" + getSubjectName() + "'" +
            ", isActive='" + getIsActive() + "'" +
            ", instructor=" + getInstructor() +
            ", grade=" + getGrade() +
            "}";
    }
}
