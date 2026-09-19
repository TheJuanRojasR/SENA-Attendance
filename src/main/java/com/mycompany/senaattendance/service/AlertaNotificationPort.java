package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.domain.Alerta;

/**
 * Notification hook of the absence alerts (UC013). The delivery (UC018) persists one in-app
 * notification per recipient on every alert event, so the alert lifecycle also reaches the
 * apprentice and the instructors through their inbox.
 *
 * <p>The justification flow keeps its own port: resolving an alert is a consequence of approving
 * a justification, not a state change of the justification itself, so it must not add a second
 * interaction to {@link JustificationNotificationPort}.
 */
public interface AlertaNotificationPort {
    /**
     * Notifies the generation of an alert (UC013, flow step 3): the apprentice and the instructors
     * related to the alert learn that a threshold was reached.
     *
     * @param alerta the generated alert.
     */
    void generated(Alerta alerta);

    /**
     * Notifies the automatic resolution of an alert (UC013, A4): the apprentice and the
     * instructors related to the alert learn that their failures dropped below the threshold.
     *
     * @param alerta the resolved alert.
     */
    void resolved(Alerta alerta);
}
