package com.mycompany.senaattendance.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.mycompany.senaattendance.domain.Notificacion;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.enumeration.NotificacionEstado;
import com.mycompany.senaattendance.domain.enumeration.NotificacionTipo;
import com.mycompany.senaattendance.repository.NotificacionRepository;
import com.mycompany.senaattendance.service.CredentialsResendService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests of the retry of the failed deliveries (UC018, E3): a retried credentials notification
 * becomes sent on a delivered email and stays retryable when the delivery fails again, while the
 * in-app notifications are never touched because they have no external channel to retry.
 */
@ExtendWith(MockitoExtension.class)
class NotificacionRetryJobTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @Mock
    private CredentialsResendService credentialsResendService;

    @InjectMocks
    private NotificacionRetryJob notificacionRetryJob;

    @Test
    void deliveredRetryMarksTheCredentialsNotificationAsSent() {
        Notificacion notification = credentialNotification("notification-1");
        when(notificacionRepository.findByEstado(NotificacionEstado.REINTENTAR)).thenReturn(List.of(notification));
        when(credentialsResendService.retry(notification)).thenReturn(true);

        notificacionRetryJob.retryFailedDeliveries();

        assertThat(notification.getEstado()).isEqualTo(NotificacionEstado.ENVIADA);
        verify(notificacionRepository).save(notification);
    }

    @Test
    void failedRetryKeepsTheCredentialsNotificationRetryable() {
        Notificacion notification = credentialNotification("notification-2");
        when(notificacionRepository.findByEstado(NotificacionEstado.REINTENTAR)).thenReturn(List.of(notification));
        when(credentialsResendService.retry(notification)).thenReturn(false);

        notificacionRetryJob.retryFailedDeliveries();

        assertThat(notification.getEstado()).isEqualTo(NotificacionEstado.REINTENTAR);
        verify(notificacionRepository, never()).save(any());
    }

    @Test
    void inAppNotificationsAreNotRetried() {
        Notificacion alertNotification = new Notificacion()
            .id("notification-3")
            .user(user("user-1"))
            .tipo(NotificacionTipo.ALERTA)
            .estado(NotificacionEstado.REINTENTAR);
        Notificacion justificationNotification = new Notificacion()
            .id("notification-4")
            .user(user("user-2"))
            .tipo(NotificacionTipo.JUSTIFICACION)
            .estado(NotificacionEstado.REINTENTAR);
        when(notificacionRepository.findByEstado(NotificacionEstado.REINTENTAR)).thenReturn(
            List.of(alertNotification, justificationNotification)
        );

        notificacionRetryJob.retryFailedDeliveries();

        verifyNoInteractions(credentialsResendService);
        verify(notificacionRepository, never()).save(any());
        assertThat(alertNotification.getEstado()).isEqualTo(NotificacionEstado.REINTENTAR);
        assertThat(justificationNotification.getEstado()).isEqualTo(NotificacionEstado.REINTENTAR);
    }

    @Test
    void withoutRetryableNotificationsNothingHappens() {
        when(notificacionRepository.findByEstado(NotificacionEstado.REINTENTAR)).thenReturn(List.of());

        notificacionRetryJob.retryFailedDeliveries();

        verifyNoInteractions(credentialsResendService);
        verify(notificacionRepository, never()).save(any());
    }

    private static User user(String id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private static Notificacion credentialNotification(String id) {
        return new Notificacion()
            .id(id)
            .user(user("user-" + id))
            .tipo(NotificacionTipo.CREDENTIALS)
            .estado(NotificacionEstado.REINTENTAR);
    }
}
