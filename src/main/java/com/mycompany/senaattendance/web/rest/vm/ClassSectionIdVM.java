package com.mycompany.senaattendance.web.rest.vm;

import jakarta.validation.constraints.NotNull;

/**
 * View Model used to reference a {@link com.mycompany.senaattendance.domain.ClassSection} by its
 * unique {@code id}, mirroring the nested reference shape of the attendance contract.
 */
public class ClassSectionIdVM {

    @NotNull
    private String id;

    public ClassSectionIdVM() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ClassSectionIdVM{id='" + id + "'}";
    }
}
