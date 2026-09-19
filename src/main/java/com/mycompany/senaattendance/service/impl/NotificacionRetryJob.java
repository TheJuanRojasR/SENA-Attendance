package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.Notificacion;
import com.mycompany.senaattendance.domain.enumeration.NotificacionEstado;
import com.mycompany.senaattendance.domain.enumeration.NotificacionTipo;
import com.mycompany.senaattendance.repository.NotificacionRepository;
import com.mycompany.senaattendance.service.CredentialsResendService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Retries the failed deliveries of the notifications (UC018, E3).
 *
 * <p>Only the {@code CREDENTIALS} notifications can fail: they travel through the email channel,
 * which is the one that can reject a message. The {@code ALERTA} and {@code JUSTIFICACION}
 * notifications are in-app and are persisted already delivered (the {@code PENDIENTE} state of the
 * inbox feeds the unread indicator), so there is no external channel to retry and the job leaves
 * them untouched.
 *
 * <p>The retry reuses the administrator resend of UC006 (E7): a fresh reset key that forces the
 * password change and the synchronous reset email. A delivered email marks the notification as
 * {@code ENVIADA}; a failure keeps it in {@code REINTENTAR} for the next run.
 */
@Component
public class NotificacionRetryJob {

    private static final Logger LOG = LoggerFactory.getLogger(NotificacionRetryJob.class);

    private final NotificacionRepository notificacionRepository;

    private final CredentialsResendService credentialsResendService;

    public NotificacionRetryJob(NotificacionRepository notificacionRepository, CredentialsResendService credentialsResendService) {
        this.notificacionRepository = notificacionRepository;
        this.credentialsResendService = credentialsResendService;
    }

    /**
     * Retries every notification left in {@code REINTENTAR}. This is scheduled to get fired every
     * day, at 01:00 (am), the same window the other maintenance jobs of the project use.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void retryFailedDeliveries() {
        List<Notificacion> retryable = notificacionRepository.findByEstado(NotificacionEstado.REINTENTAR);
        if (retryable.isEmpty()) {
            return;
        }
        LOG.debug("Retrying {} notifications in state REINTENTAR", retryable.size());
        for (Notificacion notification : retryable) {
            if (notification.getTipo() != NotificacionTipo.CREDENTIALS) {
                // In-app notifications have no delivery channel that can fail.
                continue;
            }
            if (credentialsResendService.retry(notification)) {
                notification.setEstado(NotificacionEstado.ENVIADA);
                notificacionRepository.save(notification);
            }
        }
    }
}
