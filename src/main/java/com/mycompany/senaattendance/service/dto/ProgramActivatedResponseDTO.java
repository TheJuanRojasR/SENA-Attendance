package com.mycompany.senaattendance.service.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * Response DTO for {@code PATCH /api/programs/activated}.
 *
 * <p>Carries the updated {@link ProgramDTO program} plus, when the program is being
 * deactivated and still has active fichas (grades), a dynamic {@code warning} and the
 * count of those active fichas so the frontend can surface the E2 advisory without
 * extra requests.
 */
public class ProgramActivatedResponseDTO implements Serializable {

    private ProgramDTO program;

    private int activeFichasCount;

    private String warning;

    public ProgramActivatedResponseDTO() {}

    public ProgramDTO getProgram() {
        return program;
    }

    public void setProgram(ProgramDTO program) {
        this.program = program;
    }

    public int getActiveFichasCount() {
        return activeFichasCount;
    }

    public void setActiveFichasCount(int activeFichasCount) {
        this.activeFichasCount = activeFichasCount;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProgramActivatedResponseDTO that)) {
            return false;
        }
        return (
            activeFichasCount == that.activeFichasCount && Objects.equals(program, that.program) && Objects.equals(warning, that.warning)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(program, activeFichasCount, warning);
    }

    @Override
    public String toString() {
        return (
            "ProgramActivatedResponseDTO{" +
            "program=" +
            program +
            ", activeFichasCount=" +
            activeFichasCount +
            ", warning='" +
            warning +
            "'}"
        );
    }
}
