package com.mycompany.senaattendance.web.rest.vm;

import com.mycompany.senaattendance.domain.enumeration.StateAcademic;
import jakarta.validation.constraints.NotNull;

/**
 * View Model used to unlink an apprentice from a ficha with a reason (UC008, A1).
 *
 * <p>The enrollment is identified by its own {@code id}, not by the document number, because
 * the same document number can belong to profiles with different document types. The reason is
 * the academic state the record takes when the apprentice already has attendance in the ficha;
 * only the withdrawal reasons (Retiro voluntario, Aplazado, Cancelado) are accepted.
 */
public class UnlinkApprenticeVM {

    @NotNull
    private String id;

    @NotNull
    private StateAcademic reason;

    public UnlinkApprenticeVM() {
        // Empty constructor needed for Jackson.
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public StateAcademic getReason() {
        return reason;
    }

    public void setReason(StateAcademic reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "UnlinkApprenticeVM{id='" + id + "', reason=" + reason + "}";
    }
}
