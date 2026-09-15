package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.Notificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Custom fragment of {@link NotificacionRepository} that resolves the notification inbox of one
 * user (UC018) in one dynamic query, so the optional filters are combined without one repository
 * method per filter combination. The owner is a DBRef, so it is matched by its referenced id
 * ({@code $id}) with an explicit {@code ObjectId}.
 */
public interface NotificacionRepositoryCustom {
    /**
     * Searches the notifications of one user by the optional filters.
     *
     * @param userId the id of the owner whose inbox is read.
     * @param criteria the optional filters; a {@code null} field means no constraint.
     * @param pageable the pagination information.
     * @return the page of matching notifications, or an empty page when the user id is invalid.
     */
    Page<Notificacion> searchInbox(String userId, NotificacionSearchCriteria criteria, Pageable pageable);

    /**
     * Counts the unread notifications of one user, which feeds the unread indicator (UC018, A3).
     *
     * @param userId the id of the owner whose unread notifications are counted.
     * @return the number of unread notifications, or {@code 0} when the user id is invalid.
     */
    long countUnread(String userId);

    /**
     * Marks every unread notification of one user as read (UC018, A1).
     *
     * @param userId the id of the owner whose inbox is updated.
     * @return the number of notifications marked as read, or {@code 0} when the user id is
     *         invalid.
     */
    long markAllAsRead(String userId);

    /**
     * Marks one notification of one user as read. Already read notifications match without
     * changing, so the operation is idempotent.
     *
     * @param id the notification id.
     * @param userId the id of the owner the notification must belong to.
     * @return whether a notification of that user matched the id.
     */
    boolean markAsRead(String id, String userId);
}
