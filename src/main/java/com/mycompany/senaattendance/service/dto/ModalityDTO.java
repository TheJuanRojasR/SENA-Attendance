package com.mycompany.senaattendance.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.senaattendance.domain.Modality} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ModalityDTO implements Serializable {

    private String id;

    @NotNull
    @Size(max = 50)
    private String name;

    @NotNull
    private Boolean isActive;

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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ModalityDTO)) {
            return false;
        }

        ModalityDTO modalityDTO = (ModalityDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, modalityDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ModalityDTO{" +
            "id='" + getId() + "'" +
            ", name='" + getName() + "'" +
            ", isActive='" + getIsActive() + "'" +
            "}";
    }
}
