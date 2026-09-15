package com.mycompany.senaattendance.web.rest;

import com.mycompany.senaattendance.domain.enumeration.NotificacionTipo;
import com.mycompany.senaattendance.service.NotificacionService;
import com.mycompany.senaattendance.service.dto.NotificacionDTO;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for the notification inbox of the authenticated user (UC018).
 *
 * <p>Every endpoint is scoped to the authenticated user: the inbox only returns their own
 * notifications, marking one reaches only their records and a notification of another user
 * resolves as {@code 404}, so the read never leaks its existence. The delivery state and the read
 * state are independent and the inbox is ordered from the newest to the oldest notification.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificacionResource {

    private static final Logger LOG = LoggerFactory.getLogger(NotificacionResource.class);

    /**
     * Header that carries the unread indicator of the authenticated user (UC018, A3).
     */
    public static final String UNREAD_COUNT_HEADER = "X-Unread-Count";

    private final NotificacionService notificacionService;

    public NotificacionResource(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    /**
     * {@code GET  /notifications} : get the inbox of the authenticated user, newest first. The
     * optional filters by read state, type and creation range are combined with AND, the results
     * are paginated (20 per page by default) and the response carries the total count, the
     * pagination links and the unread indicator (UC018, A2 and A3).
     *
     * @param pageable the pagination information.
     * @param read the read state to filter by (optional).
     * @param type the notification type to filter by (optional).
     * @param from the first creation instant to include (optional).
     * @param to the last creation instant to include (optional).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the page of
     *         notifications in body.
     */
    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificacionDTO>> getNotificationInbox(
        @org.springdoc.core.annotations.ParameterObject @PageableDefault(
            size = 20,
            sort = "createdDate",
            direction = Sort.Direction.DESC
        ) Pageable pageable,
        @RequestParam(name = "read", required = false) Boolean read,
        @RequestParam(name = "type", required = false) NotificacionTipo type,
        @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
        @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        LOG.debug("REST request to get the notification inbox of the current user");
        Page<NotificacionDTO> page = notificacionService.findInbox(read, type, from, to, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        headers.add(UNREAD_COUNT_HEADER, Long.toString(notificacionService.countUnread()));
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code PATCH  /notifications/:id/read} : mark one notification of the authenticated user as
     * read (UC018, flow step 3).
     *
     * @param id the id of the notification to mark as read.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the updated
     *         notification in body, or with status {@code 404 (Not Found)} when it does not exist
     *         or belongs to another user.
     */
    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificacionDTO> markNotificationAsRead(@PathVariable("id") String id) {
        LOG.debug("REST request to mark Notification {} as read", id);
        return ResponseUtil.wrapOrNotFound(notificacionService.markAsRead(id));
    }

    /**
     * {@code PATCH  /notifications/read-all} : mark every unread notification of the
     * authenticated user as read (UC018, A1).
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllNotificationsAsRead() {
        LOG.debug("REST request to mark every unread Notification of the current user as read");
        notificacionService.markAllAsRead();
        return ResponseEntity.ok().build();
    }
}
