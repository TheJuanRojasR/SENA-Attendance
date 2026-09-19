package com.mycompany.senaattendance.web.rest.vm;

import jakarta.validation.constraints.NotNull;

/**
 * View Model used by the ficha lifecycle actions (postpone, resume, cancel):
 * carries the unique {@code id} of the target {@link com.mycompany.senaattendance.domain.Grade}.
 */
public class GradeIdVM {

    @NotNull
    private String id;

    public GradeIdVM() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "GradeIdVM{id='" + id + "'}";
    }
}
