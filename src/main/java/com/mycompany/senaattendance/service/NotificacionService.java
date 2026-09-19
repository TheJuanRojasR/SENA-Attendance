package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.domain.enumeration.NotificacionTipo;
import com.mycompany.senaattendance.service.dto.NotificacionDTO;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing the inbox of the authenticated user (UC018).
 */
public interface NotificacionService {
    /**
     * Reads the inbox of the current user, ordered by the requested pageable (newest first by
     * default) and restricted to their own notifications.
     *
     * @param read the read state to filter by (optional).
     * @param tipo the notification type to filter by (optional).
     * @param from the first creation instant to include (optional).
     * @param to the last creation instant to include (optional).
     * @param pageable the pagination information.
     * @return the page of matching notifications; empty when the session has no resolvable user.
     */
    Page<NotificacionDTO> findInbox(Boolean read, NotificacionTipo tipo, Instant from, Instant to, Pageable pageable);

    /**
     * Marks one notification of the current user as read.
     *
     * @param id the notification id.
     * @return the updated notification, or empty when it does not exist or belongs to another
     *         user, so the caller answers {@code 404} without leaking foreign notifications.
     */
    Optional<NotificacionDTO> markAsRead(String id);

    /**
     * Marks every unread notification of the current user as read (A1).
     *
     * @return the number of notifications marked as read.
     */
    long markAllAsRead();

    /**
     * Counts the unread notifications of the current user, which feeds the unread indicator (A3).
     *
     * @return the number of unread notifications.
     */
    long countUnread();
}
