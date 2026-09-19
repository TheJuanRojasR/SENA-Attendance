package com.mycompany.senaattendance.service.dto;

import com.mycompany.senaattendance.domain.enumeration.AlertaState;
import com.mycompany.senaattendance.domain.enumeration.AlertaType;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.senaattendance.domain.Alerta} entity (UC013). The apprentice
 * and the scope of the alert (the materia of a consecutive alert, the ficha always) travel as
 * references, and the state, the counts and the traceability fields are server-owned.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AlertaDTO implements Serializable {

    private String id;

    private UserProfileDTO student;

    private ClassSectionDTO classSection;

    private GradeDTO grade;

    private TrimesterDTO trimester;

    private AlertaType type;

    private AlertaState state;

    private Integer absenceCount;

    private Integer threshold;

    private Instant generatedAt;

    private Instant resolvedAt;

    @Size(max = 300)
    private String observation;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserProfileDTO getStudent() {
        return student;
    }

    public void setStudent(UserProfileDTO student) {
        this.student = student;
    }

    public ClassSectionDTO getClassSection() {
        return classSection;
    }

    public void setClassSection(ClassSectionDTO classSection) {
        this.classSection = classSection;
    }

    public GradeDTO getGrade() {
        return grade;
    }

    public void setGrade(GradeDTO grade) {
        this.grade = grade;
    }

    public TrimesterDTO getTrimester() {
        return trimester;
    }

    public void setTrimester(TrimesterDTO trimester) {
        this.trimester = trimester;
    }

    public AlertaType getType() {
        return type;
    }

    public void setType(AlertaType type) {
        this.type = type;
    }

    public AlertaState getState() {
        return state;
    }

    public void setState(AlertaState state) {
        this.state = state;
    }

    public Integer getAbsenceCount() {
        return absenceCount;
    }

    public void setAbsenceCount(Integer absenceCount) {
        this.absenceCount = absenceCount;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AlertaDTO)) {
            return false;
        }
        AlertaDTO alertaDTO = (AlertaDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, alertaDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AlertaDTO{" +
            "id='" + getId() + "'" +
            ", type='" + getType() + "'" +
            ", state='" + getState() + "'" +
            ", absenceCount=" + getAbsenceCount() +
            ", threshold=" + getThreshold() +
            ", generatedAt='" + getGeneratedAt() + "'" +
            ", resolvedAt='" + getResolvedAt() + "'" +
            ", observation='" + getObservation() + "'" +
            "}";
    }
}
