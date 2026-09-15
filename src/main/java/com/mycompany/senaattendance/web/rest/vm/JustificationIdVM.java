package com.mycompany.senaattendance.web.rest.vm;

import jakarta.validation.constraints.NotNull;

/**
 * View Model used by the justification lifecycle actions (cancel): carries the unique
 * {@code id} of the target {@link com.mycompany.senaattendance.domain.Justification}.
 */
public class JustificationIdVM {

    @NotNull
    private String id;

    public JustificationIdVM() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "JustificationIdVM{id='" + id + "'}";
    }
}
