package com.mycompany.senaattendance.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.senaattendance.domain.ClassException} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ClassExceptionDTO implements Serializable {

    private String id;

    @NotNull
    private LocalDate date;

    @NotNull
    @Size(max = 200)
    private String reason;

    @NotNull
    private ClassSectionDTO classSection;

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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public ClassSectionDTO getClassSection() {
        return classSection;
    }

    public void setClassSection(ClassSectionDTO classSection) {
        this.classSection = classSection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClassExceptionDTO)) {
            return false;
        }

        ClassExceptionDTO classExceptionDTO = (ClassExceptionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, classExceptionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ClassExceptionDTO{" +
            "id='" + getId() + "'" +
            ", date='" + getDate() + "'" +
            ", reason='" + getReason() + "'" +
            ", classSection=" + getClassSection() +
            "}";
    }
}
