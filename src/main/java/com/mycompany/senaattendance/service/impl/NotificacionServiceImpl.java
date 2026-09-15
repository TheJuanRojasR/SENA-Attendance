package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.Notificacion;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.enumeration.NotificacionTipo;
import com.mycompany.senaattendance.repository.NotificacionRepository;
import com.mycompany.senaattendance.repository.NotificacionSearchCriteria;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.NotificacionService;
import com.mycompany.senaattendance.service.dto.NotificacionDTO;
import com.mycompany.senaattendance.service.mapper.NotificacionMapper;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing the inbox of the authenticated user (UC018).
 *
 * <p>Every operation resolves the current user from the security context and scopes the query to
 * them: reading, marking one notification and marking every notification only reach the caller's
 * own records, and a foreign notification resolves as absent.
 */
@Service
public class NotificacionServiceImpl implements NotificacionService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificacionServiceImpl.class);

    private final NotificacionRepository notificacionRepository;

    private final UserRepository userRepository;

    private final NotificacionMapper notificacionMapper;

    public NotificacionServiceImpl(
        NotificacionRepository notificacionRepository,
        UserRepository userRepository,
        NotificacionMapper notificacionMapper
    ) {
        this.notificacionRepository = notificacionRepository;
        this.userRepository = userRepository;
        this.notificacionMapper = notificacionMapper;
    }

    @Override
    public Page<NotificacionDTO> findInbox(Boolean read, NotificacionTipo tipo, Instant from, Instant to, Pageable pageable) {
        LOG.debug("Request to get the notification inbox of the current user");
        return currentUser()
            .map(user ->
                notificacionRepository
                    .searchInbox(user.getId(), new NotificacionSearchCriteria(read, tipo, from, to), pageable)
                    .map(notificacionMapper::toDto)
            )
            .orElseGet(() -> Page.empty(pageable));
    }

    @Override
    public Optional<NotificacionDTO> markAsRead(String id) {
        LOG.debug("Request to mark Notification {} as read", id);
        return currentUser()
            .filter(user -> notificacionRepository.markAsRead(id, user.getId()))
            .flatMap(user -> notificacionRepository.findById(id))
            .map(notificacionMapper::toDto);
    }

    @Override
    public long markAllAsRead() {
        LOG.debug("Request to mark every unread Notification of the current user as read");
        return currentUser()
            .map(user -> notificacionRepository.markAllAsRead(user.getId()))
            .orElse(0L);
    }

    @Override
    public long countUnread() {
        return currentUser()
            .map(user -> notificacionRepository.countUnread(user.getId()))
            .orElse(0L);
    }

    /**
     * @return the authenticated user, or empty when the session has no login or the account does
     *         not exist.
     */
    private Optional<User> currentUser() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneByLogin);
    }
}
