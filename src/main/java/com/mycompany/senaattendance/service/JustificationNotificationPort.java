package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.domain.Justification;
import com.mycompany.senaattendance.domain.enumeration.StateJustification;

/**
 * Notification hook of the justification state changes (UC011, A2 and UC010: one notification per
 * change).
 *
 * <p>The delivery itself belongs to UC018 (notifications), which is not implemented yet: the only
 * implementation is a documented no-op, so every state change is recorded once and UC018 can
 * replace the bean without touching the justification flows.
 *
 * <p>Call sites: creating a justification notifies {@code PENDIENTE}, cancelling it notifies
 * {@code CANCELADA} and deciding a part (UC010) notifies the resulting {@code ACEPTADA} or
 * {@code RECHAZADA} once per decided part.
 *
 * <p>The signature carries the header and the resulting state, not the recipient: the header
 * already resolves the apprentice of the change, so UC018 can keep this contract. If the real
 * delivery also needs the deciding profile or the changed part, UC018 extends the signature with
 * those values.
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
