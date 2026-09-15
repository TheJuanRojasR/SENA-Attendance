package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Justification;
import com.mycompany.senaattendance.domain.JustificationDetails;
import com.mycompany.senaattendance.domain.Notificacion;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.NotificacionEstado;
import com.mycompany.senaattendance.domain.enumeration.NotificacionTipo;
import com.mycompany.senaattendance.domain.enumeration.StateJustification;
import com.mycompany.senaattendance.repository.NotificacionRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.service.JustificationNotificationPort;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Real delivery of the justification notifications (UC018): every state change persists one
 * in-app notification for the apprentice that owns the justification, and creating a
 * justification also notifies the instructor of each affected materia, so they know a new part is
 * waiting for their decision (UC010, flow step 1).
 *
 * <p>The notifications are in-app, so they are persisted as {@code PENDIENTE} (delivery to the
 * email channel uses {@code ENVIADA}) and unread. Every notification references its origin with
 * {@code referenceType = "JUSTIFICATION"} and the id of the justification.
 *
 * <p>Recipients per event: {@code PENDIENTE} notifies the apprentice and the instructors of the
 * affected materias (once per instructor); {@code ACEPTADA}, {@code RECHAZADA} and
 * {@code CANCELADA} notify the apprentice. The rejection reason is not part of this contract, so
 * the message only reports the resulting state.
 */
@Component
public class JustificationNotificationDelivery implements JustificationNotificationPort {

    private static final Logger LOG = LoggerFactory.getLogger(JustificationNotificationDelivery.class);

    private static final String REFERENCE_TYPE = "JUSTIFICATION";

    private static final String APPRENTICE_PENDING_MESSAGE = "Tu justificación quedó registrada y está pendiente de revisión.";

    private static final String APPRENTICE_ACCEPTED_MESSAGE = "Tu justificación fue aprobada.";

    private static final String APPRENTICE_REJECTED_MESSAGE = "Tu justificación fue rechazada.";

    private static final String APPRENTICE_CANCELLED_MESSAGE = "Tu justificación fue cancelada.";

    private static final String INSTRUCTOR_MESSAGE = "Recibiste una nueva justificación pendiente de revisión en una de tus materias.";

    private final NotificacionRepository notificacionRepository;

    private final UserProfileRepository userProfileRepository;

    public JustificationNotificationDelivery(NotificacionRepository notificacionRepository, UserProfileRepository userProfileRepository) {
        this.notificacionRepository = notificacionRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public void stateChanged(Justification justification, StateJustification newState) {
        if (justification == null || justification.getId() == null) {
            return;
        }
        notifyApprentice(justification, newState);
        if (newState == StateJustification.PENDIENTE) {
            notifyInstructors(justification);
        }
    }

    /**
     * Persists the notification of the apprentice that owns the justification.
     *
     * @param justification the justification whose state changed.
     * @param newState the state the change produced.
     */
    private void notifyApprentice(Justification justification, StateJustification newState) {
        User apprentice = userOf(justification.getStudent());
        if (apprentice == null) {
            LOG.warn("Justification {} has no apprentice user to notify", justification.getId());
            return;
        }
        saveNotification(apprentice, apprenticeMessage(newState), justification.getId());
    }

    /**
     * Persists one notification per instructor of the affected materias, deduplicated so an
     * instructor that teaches several of them receives a single notification.
     *
     * @param justification the created justification.
     */
    private void notifyInstructors(Justification justification) {
        Set<JustificationDetails> parts = justification.getDetailses();
        if (parts == null) {
            return;
        }
        Set<String> notifiedInstructors = new HashSet<>();
        for (JustificationDetails part : parts) {
            User instructor = instructorOf(part);
            if (instructor == null || instructor.getId() == null || !notifiedInstructors.add(instructor.getId())) {
                continue;
            }
            saveNotification(instructor, INSTRUCTOR_MESSAGE, justification.getId());
        }
    }

    private void saveNotification(User user, String mensaje, String justificationId) {
        Notificacion notificacion = new Notificacion()
            .user(user)
            .tipo(NotificacionTipo.JUSTIFICACION)
            .estado(NotificacionEstado.PENDIENTE)
            .read(false)
            .referenceType(REFERENCE_TYPE)
            .referenceId(justificationId)
            .mensaje(mensaje);
        notificacionRepository.save(notificacion);
    }

    /**
     * @param state the resulting state of the change.
     * @return the message the apprentice receives for that state.
     */
    private static String apprenticeMessage(StateJustification state) {
        return switch (state) {
            case PENDIENTE -> APPRENTICE_PENDING_MESSAGE;
            case ACEPTADA -> APPRENTICE_ACCEPTED_MESSAGE;
            case RECHAZADA -> APPRENTICE_REJECTED_MESSAGE;
            case CANCELADA -> APPRENTICE_CANCELLED_MESSAGE;
        };
    }

    /**
     * @param part a part of the justification, possibly {@code null}.
     * @return the user of the instructor assigned to the materia of the part, or {@code null}
     *         when any link of the chain is missing.
     */
    private User instructorOf(JustificationDetails part) {
        if (part == null) {
            return null;
        }
        ClassSection classSection = part.getClassSection();
        return classSection == null ? null : userOf(classSection.getInstructor());
    }

    /**
     * Resolves the user behind a profile. A header built from a create payload carries the
     * apprentice profile with its id only, so a partial in-memory reference is reloaded from the
     * repository to reach the user.
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
