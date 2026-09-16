package com.mycompany.senaattendance.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mycompany.senaattendance.domain.enumeration.AlertaState;
import com.mycompany.senaattendance.domain.enumeration.AlertaType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * An absence alert (UC013). The system generates it when the failures of an apprentice reach one
 * of the configured thresholds, and the instructor walks it from unread to attended. The
 * combination of apprentice, scope (materia or ficha), trimester and type admits only one active
 * alert; every resolved alert stays as history.
 */
@Document(collection = "alerta")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Alerta extends AbstractAuditingEntity<String> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @DBRef
    @Field("student")
    @JsonIgnoreProperties(value = { "user", "documentType" }, allowSetters = true)
    private UserProfile student;

    /**
     * The materia of a consecutive alert. It is {@code null} in the accumulated alerts, whose
     * scope is the whole ficha.
     */
    @DBRef
    @Field("classSection")
    @JsonIgnoreProperties(value = { "scheduleses", "exceptionses", "instructor", "grade" }, allowSetters = true)
    private ClassSection classSection;

    @NotNull
    @DBRef
    @Field("grade")
    @JsonIgnoreProperties(value = { "program", "modality", "timeSlot" }, allowSetters = true)
    private Grade grade;

    @NotNull
    @DBRef
    @Field("trimester")
    private Trimester trimester;

    @NotNull
    @Field("type")
    private AlertaType type;

    @NotNull
    @Field("state")
    private AlertaState state;

    /**
     * The count that triggered the alert: the trailing streak of the materia in a consecutive
     * alert, the total failures of the ficha in an accumulated one.
     */
    @NotNull
    @Field("absence_count")
    private Integer absenceCount;

    @NotNull
    @Field("threshold")
    private Integer threshold;

    @NotNull
    @Field("generated_at")
    private Instant generatedAt;

    @Field("resolved_at")
    private Instant resolvedAt;

    /**
     * The note the instructor registers when attending the alert (A3).
     */
    @Size(max = 300)
    @Field("observation")
    private String observation;

    public String getId() {
        return this.id;
    }

    public Alerta id(String id) {
        this.setId(id);
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserProfile getStudent() {
        return this.student;
    }

    public Alerta student(UserProfile userProfile) {
        this.setStudent(userProfile);
        return this;
    }

    public void setStudent(UserProfile userProfile) {
        this.student = userProfile;
    }

    public ClassSection getClassSection() {
        return this.classSection;
    }

    public Alerta classSection(ClassSection classSection) {
        this.setClassSection(classSection);
        return this;
    }

    public void setClassSection(ClassSection classSection) {
        this.classSection = classSection;
    }

    public Grade getGrade() {
        return this.grade;
    }

    public Alerta grade(Grade grade) {
        this.setGrade(grade);
        return this;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }

    public Trimester getTrimester() {
        return this.trimester;
    }

    public Alerta trimester(Trimester trimester) {
        this.setTrimester(trimester);
        return this;
    }

    public void setTrimester(Trimester trimester) {
        this.trimester = trimester;
    }

    public AlertaType getType() {
        return this.type;
    }

    public Alerta type(AlertaType type) {
        this.setType(type);
        return this;
    }

    public void setType(AlertaType type) {
        this.type = type;
    }

    public AlertaState getState() {
        return this.state;
    }

    public Alerta state(AlertaState state) {
        this.setState(state);
        return this;
    }

    public void setState(AlertaState state) {
        this.state = state;
    }

    public Integer getAbsenceCount() {
        return this.absenceCount;
    }

    public Alerta absenceCount(Integer absenceCount) {
        this.setAbsenceCount(absenceCount);
        return this;
    }

    public void setAbsenceCount(Integer absenceCount) {
        this.absenceCount = absenceCount;
    }

    public Integer getThreshold() {
        return this.threshold;
    }

    public Alerta threshold(Integer threshold) {
        this.setThreshold(threshold);
        return this;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public Instant getGeneratedAt() {
        return this.generatedAt;
    }

    public Alerta generatedAt(Instant generatedAt) {
        this.setGeneratedAt(generatedAt);
        return this;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }

    public Instant getResolvedAt() {
        return this.resolvedAt;
    }

    public Alerta resolvedAt(Instant resolvedAt) {
        this.setResolvedAt(resolvedAt);
        return this;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getObservation() {
        return this.observation;
    }

    public Alerta observation(String observation) {
        this.setObservation(observation);
        return this;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Alerta)) {
            return false;
        }
        return getId() != null && getId().equals(((Alerta) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Alerta{" +
            "id=" + getId() +
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
