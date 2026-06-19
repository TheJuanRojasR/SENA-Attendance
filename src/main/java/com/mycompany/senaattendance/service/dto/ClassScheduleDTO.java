package com.mycompany.senaattendance.service.dto;

import com.mycompany.senaattendance.domain.enumeration.DayOfWeek;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.senaattendance.domain.ClassSchedule} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ClassScheduleDTO implements Serializable {

    private String id;

    private DayOfWeek dayOfWeek;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @NotNull
    private TrimesterDTO trimester;

    @NotNull
    private ClassSectionDTO classSection;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public TrimesterDTO getTrimester() {
        return trimester;
    }

    public void setTrimester(TrimesterDTO trimester) {
        this.trimester = trimester;
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
        if (!(o instanceof ClassScheduleDTO)) {
            return false;
        }

        ClassScheduleDTO classScheduleDTO = (ClassScheduleDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, classScheduleDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ClassScheduleDTO{" +
            "id='" + getId() + "'" +
            ", dayOfWeek='" + getDayOfWeek() + "'" +
            ", startTime='" + getStartTime() + "'" +
            ", endTime='" + getEndTime() + "'" +
            ", trimester=" + getTrimester() +
            ", classSection=" + getClassSection() +
            "}";
    }
}
