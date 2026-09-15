package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.domain.Justification;
import com.mycompany.senaattendance.domain.enumeration.StateJustification;

/**
 * Notification hook of the justification state changes (UC011, A2: one notification per change).
 *
 * <p>The delivery itself belongs to UC018 (notifications), which is not implemented yet: the only
 * implementation is a documented no-op, so every state change is recorded once and UC018 can
 * replace the bean without touching the justification flows.
 *
 * <p>Call sites: creating a justification notifies {@code PENDIENTE} and cancelling it notifies
 * {@code CANCELADA}. The instructor decision (UC010, not implemented yet) must notify the
 * resulting {@code ACEPTADA}/{@code RECHAZADA} once per decided part.
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
