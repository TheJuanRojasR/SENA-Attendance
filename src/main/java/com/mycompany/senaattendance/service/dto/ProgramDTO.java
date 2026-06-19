package com.mycompany.senaattendance.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.senaattendance.domain.Program} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ProgramDTO implements Serializable {

    private String id;

    @NotNull
    @Size(max = 200)
    private String name;

    @NotNull
    @Size(max = 10)
    private String initials;

    @NotNull
    @Size(max = 30)
    private String code;

    @NotNull
    private Integer trimesters;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getTrimesters() {
        return trimesters;
    }

    public void setTrimesters(Integer trimesters) {
        this.trimesters = trimesters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProgramDTO)) {
            return false;
        }

        ProgramDTO programDTO = (ProgramDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, programDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ProgramDTO{" +
            "id='" + getId() + "'" +
            ", name='" + getName() + "'" +
            ", initials='" + getInitials() + "'" +
            ", code='" + getCode() + "'" +
            ", trimesters=" + getTrimesters() +
            "}";
    }
}
