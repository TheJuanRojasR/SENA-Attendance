package com.mycompany.senaattendance.domain.enumeration;

/**
 * The AlertaState enumeration: the lifecycle states of an absence alert (UC013).
 *
 * <p>The instructor walks the alert from unread to attended; the system resolves it
 * automatically when an approved justification drops the count below the threshold. An active
 * alert blocks a new alert of the same combination, a resolved one does not (E1).
 */
public enum AlertaState {
    NO_LEIDA,
    LEIDA,
    ATENDIDA,
    RESUELTA_AUTOMATICAMENTE;

    /**
     * @return whether this state keeps the alert active. A resolved alert is history and does not
     *         block the generation of a new alert for the same combination (UC013, E1).
     */
    public boolean isActive() {
        return this != RESUELTA_AUTOMATICAMENTE;
    }
}
