package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.Justification;
import com.mycompany.senaattendance.domain.enumeration.StateJustification;
import com.mycompany.senaattendance.service.JustificationNotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * No-op {@link JustificationNotificationPort}: the notification delivery belongs to UC018, which
 * is not implemented yet, so every state change is only traced at debug level. UC018 replaces this
 * bean with the real delivery (for example one {@code Notificacion} per apprentice) without
 * touching the justification flows.
 */
@Component
public class NoOpJustificationNotificationPort implements JustificationNotificationPort {

    private static final Logger LOG = LoggerFactory.getLogger(NoOpJustificationNotificationPort.class);

    @Override
    public void stateChanged(Justification justification, StateJustification newState) {
        LOG.debug(
            "Justification {} changed to {}; the UC018 notification delivery is not implemented yet",
            justification.getId(),
            newState
        );
    }
}
