package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.domain.Justification;
import com.mycompany.senaattendance.domain.enumeration.StateJustification;

/**
 * Notification hook of the justification state changes (UC011, A2 and UC010: one notification per
 * change).
 *
 * <p>The delivery (UC018) persists one in-app notification for the apprentice that owns the
 * justification on every state change, and creating a justification also notifies the instructor
 * of each affected materia, so they know a new part is waiting for their decision (UC010, flow
 * step 1). Delivered notifications start unread and reference the justification that originated
 * them.
 *
 * <p>Call sites: creating a justification notifies {@code PENDIENTE}, cancelling it notifies
 * {@code CANCELADA} and deciding a part (UC010) notifies the resulting {@code ACEPTADA} or
 * {@code RECHAZADA} once per decided part.
 *
 * <p>The signature carries the header and the resulting state, not the recipient: the header
 * already resolves the apprentice of the change and the affected materias, so the delivery keeps
 * this contract.
 */
public interface JustificationNotificationPort {
    /**
     * Notifies one state change of a justification.
     *
     * @param justification the justification whose state changed.
     * @param newState the state the change produced.
     */
    void stateChanged(Justification justification, StateJustification newState);
}
