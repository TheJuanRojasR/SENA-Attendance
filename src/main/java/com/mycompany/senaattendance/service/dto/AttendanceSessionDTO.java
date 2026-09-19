package com.mycompany.senaattendance.service.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * A DTO for the attendance session of a class section on a given date (UC009). It carries the
 * records persisted for that session and the counts behind {@code complete}: a session is
 * complete when the number of records equals the number of apprentices enrolled (Matriculado) in
 * the ficha, because the endpoint never fills the apprentices left out of the payload.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AttendanceSessionDTO implements Serializable {

    private ClassSectionDTO classSection;

    private LocalDate date;

    private List<AttendanceDTO> records;

    private boolean complete;

    private int enrolledCount;

    private int recordedCount;

    public ClassSectionDTO getClassSection() {
        return classSection;
    }

    public void setClassSection(ClassSectionDTO classSection) {
        this.classSection = classSection;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<AttendanceDTO> getRecords() {
        return records;
    }

    public void setRecords(List<AttendanceDTO> records) {
        this.records = records;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public int getEnrolledCount() {
        return enrolledCount;
    }

    public void setEnrolledCount(int enrolledCount) {
        this.enrolledCount = enrolledCount;
    }

    public int getRecordedCount() {
        return recordedCount;
    }

    public void setRecordedCount(int recordedCount) {
        this.recordedCount = recordedCount;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AttendanceSessionDTO{" +
            "classSection=" + getClassSection() +
            ", date='" + getDate() + "'" +
            ", records=" + getRecords() +
            ", complete=" + isComplete() +
            ", enrolledCount=" + getEnrolledCount() +
            ", recordedCount=" + getRecordedCount() +
            "}";
    }
}
