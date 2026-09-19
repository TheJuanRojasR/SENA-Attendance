package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.enumeration.NotificacionTipo;
import java.time.Instant;

/**
 * Optional filters of the notification inbox (UC018, A2). A {@code null} field means "no
 * constraint" and the present filters are combined with AND.
 *
 * @param read the read state to include; {@code null} includes read and unread notifications.
 * @param tipo the notification type to include.
 * @param from the first creation instant to include, inclusive.
 * @param to the last creation instant to include, inclusive.
 */
public record NotificacionSearchCriteria(Boolean read, NotificacionTipo tipo, Instant from, Instant to) {}
