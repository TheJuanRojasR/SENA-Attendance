package com.mycompany.senaattendance.web.rest.vm;

import com.mycompany.senaattendance.domain.enumeration.StateJustification;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * View Model of the instructor decision over one part (UC010, flow step 5). The decision only
 * accepts {@code ACEPTADA} or {@code RECHAZADA}: a rejection always carries a reason (E1) and an
 * approval of a submission marked out of time carries the additional exception reason (A2). The
 * server owns the response date and the deadline mark, so the client never sends them.
 */
public class JustificationDecisionVM {

    @NotNull
    private StateJustification stateJustification;

    @Size(max = 300)
    private String rejectionReason;

    @Size(max = 300)
    private String outOfTimeReason;

    public StateJustification getStateJustification() {
        return stateJustification;
    }

    public void setStateJustification(StateJustification stateJustification) {
        this.stateJustification = stateJustification;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getOutOfTimeReason() {
        return outOfTimeReason;
    }

    public void setOutOfTimeReason(String outOfTimeReason) {
        this.outOfTimeReason = outOfTimeReason;
    }

    @Override
    public String toString() {
        return (
            "JustificationDecisionVM{" +
            "stateJustification=" +
            stateJustification +
            ", rejectionReason='" +
            rejectionReason +
            "'" +
            ", outOfTimeReason='" +
            outOfTimeReason +
            "'" +
            "}"
        );
    }
}
