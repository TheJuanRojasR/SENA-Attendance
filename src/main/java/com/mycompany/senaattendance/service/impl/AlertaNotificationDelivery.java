package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.Alerta;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.Notificacion;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.AlertaType;
import com.mycompany.senaattendance.domain.enumeration.NotificacionEstado;
import com.mycompany.senaattendance.domain.enumeration.NotificacionTipo;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.NotificacionRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.service.AlertaNotificationPort;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Real delivery of the absence alert notifications (UC018): every alert event, generation or
 * automatic resolution, persists one in-app notification for the apprentice and for the
 * instructors related to the alert, unread, with {@code referenceType = "ALERT"} and the id of
 * the alert.
 *
 * <p>Recipients of a consecutive alert are the apprentice and the instructor of the materia; the
 * recipients of an accumulated alert are the apprentice and every instructor of the ficha, which
 * is the scope of that alert type (UC013, generation rule). The instructors are deduplicated by
 * user, so an instructor who teaches several materias of the ficha receives a single notification
 * per event.
 *
 * <p>The notifications are in-app, so they are persisted already delivered (the {@code PENDIENTE}
 * state of the inbox) and unread; only the email channel of the credentials flow can fail and be
 * retried.
 */
@Component
public class AlertaNotificationDelivery implements AlertaNotificationPort {

    private static final Logger LOG = LoggerFactory.getLogger(AlertaNotificationDelivery.class);

    private static final String REFERENCE_TYPE = "ALERT";

    private static final String APPRENTICE_GENERATED_CONSECUTIVE_MESSAGE =
        "Se generó una alerta por fallas consecutivas en una de tus materias.";

    private static final String APPRENTICE_GENERATED_ACCUMULATED_MESSAGE = "Se generó una alerta por fallas acumuladas en tu ficha.";

    private static final String APPRENTICE_RESOLVED_MESSAGE =
        "Tu alerta de inasistencia fue resuelta automáticamente: tus fallas bajaron del umbral.";

    private static final String INSTRUCTOR_GENERATED_CONSECUTIVE_MESSAGE = "Se generó una alerta de inasistencia en una de tus materias.";

    private static final String INSTRUCTOR_GENERATED_ACCUMULATED_MESSAGE = "Se generó una alerta de inasistencia en tu ficha.";

    private static final String INSTRUCTOR_RESOLVED_MESSAGE =
        "Una alerta de inasistencia de tu materia fue resuelta automáticamente: las fallas del aprendiz bajaron del umbral.";

    private static final String INSTRUCTOR_ACCUMULATED_RESOLVED_MESSAGE =
        "Una alerta de inasistencia de tu ficha fue resuelta automáticamente: las fallas del aprendiz bajaron del umbral.";

    private final NotificacionRepository notificacionRepository;

    private final UserProfileRepository userProfileRepository;

    private final ClassSectionRepository classSectionRepository;

    public AlertaNotificationDelivery(
        NotificacionRepository notificacionRepository,
        UserProfileRepository userProfileRepository,
        ClassSectionRepository classSectionRepository
    ) {
        this.notificacionRepository = notificacionRepository;
        this.userProfileRepository = userProfileRepository;
        this.classSectionRepository = classSectionRepository;
    }

    @Override
    public void generated(Alerta alerta) {
        if (alerta == null || alerta.getId() == null) {
            return;
        }
        notifyApprentice(alerta, apprenticeGeneratedMessage(alerta));
        notifyInstructors(alerta, instructorGeneratedMessage(alerta));
    }

    @Override
    public void resolved(Alerta alerta) {
        if (alerta == null || alerta.getId() == null) {
            return;
        }
        notifyApprentice(alerta, APPRENTICE_RESOLVED_MESSAGE);
        notifyInstructors(alerta, instructorResolvedMessage(alerta));
    }

    /**
     * @param alerta the generated alert.
     * @return the message the apprentice receives, which names the scope of the alert.
     */
    private static String apprenticeGeneratedMessage(Alerta alerta) {
        return alerta.getType() == AlertaType.ACUMULADAS
            ? APPRENTICE_GENERATED_ACCUMULATED_MESSAGE
            : APPRENTICE_GENERATED_CONSECUTIVE_MESSAGE;
    }

    /**
     * @param alerta the generated alert.
     * @return the message the instructors receive, which names the scope of the alert.
     */
    private static String instructorGeneratedMessage(Alerta alerta) {
        return alerta.getType() == AlertaType.ACUMULADAS
            ? INSTRUCTOR_GENERATED_ACCUMULATED_MESSAGE
            : INSTRUCTOR_GENERATED_CONSECUTIVE_MESSAGE;
    }

    /**
     * Persists the notification of the apprentice of the alert.
     *
     * @param alerta the alert whose state changed.
     * @param message the message of the apprentice.
     */
    private void notifyApprentice(Alerta alerta, String message) {
        User apprentice = userOf(alerta.getStudent());
        if (apprentice == null) {
            LOG.warn("The alert {} has no apprentice user to notify", alerta.getId());
            return;
        }
        saveNotification(apprentice, message, alerta.getId());
    }

    /**
     * Persists one notification per instructor related to the alert, deduplicated by user, so an
     * instructor who teaches several materias of the ficha receives a single notification.
     *
     * @param alerta the alert whose state changed.
     * @param message the message of the instructors.
     */
    private void notifyInstructors(Alerta alerta, String message) {
        Set<String> notifiedInstructors = new HashSet<>();
        for (UserProfile instructor : instructorsOf(alerta)) {
            User user = userOf(instructor);
            if (user == null || user.getId() == null || !notifiedInstructors.add(user.getId())) {
                continue;
            }
            saveNotification(user, message, alerta.getId());
        }
    }

    /**
     * Resolves the instructors related to the alert: the instructor of the materia in a
     * consecutive alert, and every instructor of the ficha in an accumulated one.
     *
     * @param alerta the alert whose state changed.
     * @return the related instructor profiles, possibly empty.
     */
    private List<UserProfile> instructorsOf(Alerta alerta) {
        if (alerta.getType() == AlertaType.CONSECUTIVAS) {
            ClassSection classSection = alerta.getClassSection();
            UserProfile instructor = classSection == null ? null : classSection.getInstructor();
            return instructor == null ? List.of() : List.of(instructor);
        }
        Grade grade = alerta.getGrade();
        if (grade == null || grade.getId() == null) {
            return List.of();
        }
        return classSectionRepository
            .findByGradeId(grade.getId())
            .stream()
            .map(ClassSection::getInstructor)
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * @param alerta the resolved alert.
     * @return the message the instructors receive, which names the scope of the alert.
     */
    private static String instructorResolvedMessage(Alerta alerta) {
        return alerta.getType() == AlertaType.ACUMULADAS ? INSTRUCTOR_ACCUMULATED_RESOLVED_MESSAGE : INSTRUCTOR_RESOLVED_MESSAGE;
    }

    /**
     * Persists one unread in-app notification of the alert.
     *
     * @param user the recipient.
     * @param message the message of the notification.
     * @param alertaId the id of the alert that originated it.
     */
    private void saveNotification(User user, String message, String alertaId) {
        Notificacion notificacion = new Notificacion()
            .user(user)
            .tipo(NotificacionTipo.ALERTA)
            .estado(NotificacionEstado.PENDIENTE)
            .read(false)
            .referenceType(REFERENCE_TYPE)
            .referenceId(alertaId)
            .mensaje(message);
        notificacionRepository.save(notificacion);
    }

    /**
     * Resolves the user behind a profile. A profile loaded from a payload may carry its id only,
     * so a partial in-memory reference is reloaded from the repository to reach the user.
     *
     * @param profile the profile that receives the notification, possibly {@code null}.
     * @return the user behind the profile, or {@code null} when the profile has no resolvable
     *         user.
     */
    private User userOf(UserProfile profile) {
        if (profile == null) {
            return null;
        }
        if (profile.getUser() != null) {
            return profile.getUser();
        }
        return profile.getId() == null ? null : userProfileRepository.findById(profile.getId()).map(UserProfile::getUser).orElse(null);
    }
}
