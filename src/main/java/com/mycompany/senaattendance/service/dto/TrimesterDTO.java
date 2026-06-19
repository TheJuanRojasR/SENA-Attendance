package com.mycompany.senaattendance.service.dto;

import com.mycompany.senaattendance.domain.enumeration.State;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.senaattendance.domain.Trimester} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TrimesterDTO implements Serializable {

    private String id;

    @NotNull
    @Size(max = 30)
    private String name;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
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
        if (!(o instanceof TrimesterDTO)) {
            return false;
        }

        TrimesterDTO trimesterDTO = (TrimesterDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, trimesterDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TrimesterDTO{" +
            "id='" + getId() + "'" +
            ", name='" + getName() + "'" +
            ", startDate='" + getStartDate() + "'" +
            ", endDate='" + getEndDate() + "'" +
            ", state='" + getState() + "'" +
            "}";
    }
}
