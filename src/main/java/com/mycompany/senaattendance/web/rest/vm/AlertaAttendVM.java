package com.mycompany.senaattendance.web.rest.vm;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * View Model of the follow-up an instructor registers when attending an alert (UC013, A3). The
 * observation is the only client-owned field: the state, the dates and the relationships of the
 * alert stay server-owned.
 */
public class AlertaAttendVM {

    @NotNull
    @Size(max = 300)
    private String observation;

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    @Override
    public String toString() {
        return "AlertaAttendVM{" + "observation='" + observation + "'" + "}";
    }
}
