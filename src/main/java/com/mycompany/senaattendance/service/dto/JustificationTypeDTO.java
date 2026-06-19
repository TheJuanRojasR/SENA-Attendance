package com.mycompany.senaattendance.service.dto;

import com.mycompany.senaattendance.domain.enumeration.State;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.senaattendance.domain.JustificationType} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class JustificationTypeDTO implements Serializable {

    private String id;

    @NotNull
    @Size(max = 100)
    private String name;

    private Integer limitPerTrimester;

    @NotNull
    private State state;

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

    public Integer getLimitPerTrimester() {
        return limitPerTrimester;
    }

    public void setLimitPerTrimester(Integer limitPerTrimester) {
        this.limitPerTrimester = limitPerTrimester;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JustificationTypeDTO)) {
            return false;
        }

        JustificationTypeDTO justificationTypeDTO = (JustificationTypeDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, justificationTypeDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "JustificationTypeDTO{" +
            "id='" + getId() + "'" +
            ", name='" + getName() + "'" +
            ", limitPerTrimester=" + getLimitPerTrimester() +
            ", state='" + getState() + "'" +
            "}";
    }
}
