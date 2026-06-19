package com.mycompany.senaattendance.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mycompany.senaattendance.domain.enumeration.DayOfWeek;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A ClassSchedule.
 */
@Document(collection = "class_schedule")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ClassSchedule implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Field("day_of_week")
    private DayOfWeek dayOfWeek;

    @NotNull
    @Field("start_time")
    private LocalTime startTime;

    @NotNull
    @Field("end_time")
    private LocalTime endTime;

    @DBRef
    @Field("trimester")
    private Trimester trimester;

    @DBRef
    @Field("classSection")
    @JsonIgnoreProperties(value = { "scheduleses", "exceptionses", "instructor", "grade" }, allowSetters = true)
    private ClassSection classSection;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getId() {
        return this.id;
    }

    public ClassSchedule id(String id) {
        this.setId(id);
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DayOfWeek getDayOfWeek() {
        return this.dayOfWeek;
    }

    public ClassSchedule dayOfWeek(DayOfWeek dayOfWeek) {
        this.setDayOfWeek(dayOfWeek);
        return this;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return this.startTime;
    }

    public ClassSchedule startTime(LocalTime startTime) {
        this.setStartTime(startTime);
        return this;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return this.endTime;
    }

    public ClassSchedule endTime(LocalTime endTime) {
        this.setEndTime(endTime);
        return this;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Trimester getTrimester() {
        return this.trimester;
    }

    public void setTrimester(Trimester trimester) {
        this.trimester = trimester;
    }

    public ClassSchedule trimester(Trimester trimester) {
        this.setTrimester(trimester);
        return this;
    }

    public ClassSection getClassSection() {
        return this.classSection;
    }

    public void setClassSection(ClassSection classSection) {
        this.classSection = classSection;
    }

    public ClassSchedule classSection(ClassSection classSection) {
        this.setClassSection(classSection);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClassSchedule)) {
            return false;
        }
        return getId() != null && getId().equals(((ClassSchedule) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ClassSchedule{" +
            "id=" + getId() +
            ", dayOfWeek='" + getDayOfWeek() + "'" +
            ", startTime='" + getStartTime() + "'" +
            ", endTime='" + getEndTime() + "'" +
            "}";
    }
}
