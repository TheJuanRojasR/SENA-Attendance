package com.mycompany.senaattendance.web.rest.vm;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * View Model used to register an attendance session (UC009): one materia, one session date and
 * the marks the instructor confirmed. Only confirmed marks travel in {@code attendances}; an
 * apprentice left out keeps no record for that date and the session is reported as incomplete
 * (A5), so a client that lost marks must resend the missing ones.
 */
public class AttendanceSessionVM {

    @NotNull
    @Valid
    private ClassSectionIdVM classSection;

    @NotNull
    private LocalDate date;

    @NotNull
    @Valid
    private List<AttendanceConfirmationVM> attendances;

    public AttendanceSessionVM() {}

    public ClassSectionIdVM getClassSection() {
        return classSection;
    }

    public void setClassSection(ClassSectionIdVM classSection) {
        this.classSection = classSection;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<AttendanceConfirmationVM> getAttendances() {
        return attendances;
    }

    public void setAttendances(List<AttendanceConfirmationVM> attendances) {
        this.attendances = attendances;
    }

    @Override
    public String toString() {
        return "AttendanceSessionVM{classSection=" + classSection + ", date='" + date + "', attendances=" + attendances + "}";
    }
}
