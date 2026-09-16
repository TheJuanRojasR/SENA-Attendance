package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.Notificacion;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.NotificacionEstado;
import com.mycompany.senaattendance.domain.enumeration.NotificacionTipo;
import com.mycompany.senaattendance.repository.NotificacionRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.service.CredentialsResendService;
import com.mycompany.senaattendance.service.MailService;
import com.mycompany.senaattendance.service.UserService;
import jakarta.mail.MessagingException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service Implementation of the credentials resend (UC006, E7).
 *
 * <p>The reset key generation and the forced password change live in {@link UserService}, so this
 * delivery only adds the email channel and the notification bookkeeping the two flows share: the
 * administrator resend closes the open credentials notification of the user, and the retry of the
 * scheduled job reports a delivery result without touching a notification of a different flow.
 */
@Service
public class CredentialsResendServiceImpl implements CredentialsResendService {

    private static final Logger LOG = LoggerFactory.getLogger(CredentialsResendServiceImpl.class);

    private final UserService userService;

    private final MailService mailService;

    private final UserProfileRepository userProfileRepository;

    private final NotificacionRepository notificacionRepository;

    public CredentialsResendServiceImpl(
        UserService userService,
        MailService mailService,
        UserProfileRepository userProfileRepository,
        NotificacionRepository notificacionRepository
    ) {
        this.userService = userService;
        this.mailService = mailService;
        this.userProfileRepository = userProfileRepository;
        this.notificacionRepository = notificacionRepository;
    }

    @Override
    public User resend(String documentNumber) {
        User user = userService.resendCredentials(documentNumber);
        boolean delivered = sendResetMail(user);
        notificacionRepository
            .findFirstByUserAndTipoAndEstadoInOrderByCreatedDateDesc(
                user,
                NotificacionTipo.CREDENTIALS,
                List.of(NotificacionEstado.PENDIENTE, NotificacionEstado.REINTENTAR)
            )
            .ifPresent(notification -> {
                notification.setEstado(delivered ? NotificacionEstado.ENVIADA : NotificacionEstado.REINTENTAR);
                notificacionRepository.save(notification);
            });
        return user;
    }

    @Override
    public boolean retry(Notificacion notification) {
        User user = notification == null ? null : notification.getUser();
        if (user == null || user.getId() == null) {
            LOG.warn("Cannot retry a credentials notification without a resolvable user");
            return false;
        }
        UserProfile profile = userProfileRepository.findOneByUserId(user.getId()).orElse(null);
        if (profile == null || profile.getDocumentNumber() == null) {
            LOG.warn("Cannot retry the credentials delivery: user '{}' has no resolvable profile", user.getLogin());
            return false;
        }
        return sendResetMail(userService.resendCredentials(profile.getDocumentNumber()));
    }

    /**
     * Sends the reset email synchronously. A delivery failure is reported as {@code false} so the
     * caller can keep the notification retryable (UC018, E3).
     *
     * @param user the user with the fresh reset key.
     * @return whether the email was delivered.
     */
    private boolean sendResetMail(User user) {
        try {
            return mailService.sendPasswordResetMailSync(user);
        } catch (MessagingException | RuntimeException e) {
            LOG.warn("Could not send the password reset email to user '{}'", user.getLogin(), e);
            return false;
        }
    }
}
