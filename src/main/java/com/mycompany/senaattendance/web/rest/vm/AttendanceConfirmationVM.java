package com.mycompany.senaattendance.web.rest.vm;

import com.mycompany.senaattendance.domain.enumeration.StateAttendance;
import jakarta.validation.constraints.NotNull;

/**
 * View Model of one confirmed mark inside an attendance session (UC009): the apprentice and the
 * state the instructor saved. The optional {@code id} lets a client echo back a previously
 * returned record; the server ignores it because the session upserts by materia, aprendiz and
 * fecha, so a mark can neither move to another apprentice nor create a duplicate.
 */
public class AttendanceConfirmationVM {

    private String id;

    @NotNull
    private String studentId;

    @NotNull
    private StateAttendance stateAttendance;

    public AttendanceConfirmationVM() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public StateAttendance getStateAttendance() {
        return stateAttendance;
    }

    public void setStateAttendance(StateAttendance stateAttendance) {
        this.stateAttendance = stateAttendance;
    }

    @Override
    public String toString() {
        return "AttendanceConfirmationVM{studentId='" + studentId + "', stateAttendance=" + stateAttendance + "}";
    }
}
