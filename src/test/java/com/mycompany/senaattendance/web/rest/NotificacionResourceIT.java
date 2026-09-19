package com.mycompany.senaattendance.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.Notificacion;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.enumeration.NotificacionEstado;
import com.mycompany.senaattendance.domain.enumeration.NotificacionTipo;
import com.mycompany.senaattendance.repository.NotificacionRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import java.time.Instant;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the {@link NotificacionResource} inbox (UC018): scoping to the
 * authenticated user, newest-first order, the read state independent from the delivery one and
 * the optional filters with pagination.
 */
@AutoConfigureMockMvc
@IntegrationTest
class NotificacionResourceIT {

    private static final String ENTITY_API_URL = "/api/notifications";
    private static final String ENTITY_API_URL_ID_READ = ENTITY_API_URL + "/{id}/read";

    private static final String OWNER_LOGIN = "notifications_owner";
    private static final String OTHER_LOGIN = "notifications_other";

    private static final Instant OLDER_DATE = Instant.parse("2026-09-01T10:00:00Z");
    private static final Instant MIDDLE_DATE = Instant.parse("2026-09-05T10:00:00Z");
    private static final Instant NEWER_DATE = Instant.parse("2026-09-10T10:00:00Z");

    @Autowired
    private MockMvc restNotificacionMockMvc;

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanup() {
        notificacionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = OWNER_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void getInboxReturnsOnlyOwnNotificationsNewestFirst() throws Exception {
        User owner = persistUser(OWNER_LOGIN);
        User other = persistUser(OTHER_LOGIN);
        Notificacion older = persistNotification(
            owner,
            NotificacionTipo.CREDENTIALS,
            NotificacionEstado.PENDIENTE,
            false,
            OLDER_DATE,
            null,
            null,
            "Old own notification"
        );
        persistNotification(other, NotificacionTipo.CREDENTIALS, NotificacionEstado.PENDIENTE, false, NEWER_DATE, null, null, "Other");
        Notificacion newer = persistNotification(
            owner,
            NotificacionTipo.JUSTIFICACION,
            NotificacionEstado.PENDIENTE,
            false,
            NEWER_DATE,
            "JUSTIFICATION",
            "justification-1",
            "New own notification"
        );

        restNotificacionMockMvc
            .perform(get(ENTITY_API_URL))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "2"))
            .andExpect(header().string(NotificacionResource.UNREAD_COUNT_HEADER, "2"))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id").value(newer.getId()))
            .andExpect(jsonPath("$[0].tipo").value("JUSTIFICACION"))
            .andExpect(jsonPath("$[0].mensaje").value("New own notification"))
            .andExpect(jsonPath("$[0].estado").value("PENDIENTE"))
            .andExpect(jsonPath("$[0].read").value(false))
            .andExpect(jsonPath("$[0].referenceType").value("JUSTIFICATION"))
            .andExpect(jsonPath("$[0].referenceId").value("justification-1"))
            .andExpect(jsonPath("$[0].createdDate").value(NEWER_DATE.toString()))
            .andExpect(jsonPath("$[1].id").value(older.getId()));
    }

    @Test
    @WithMockUser(username = OWNER_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void getInboxWithoutNotificationsReturnsEmptyPage() throws Exception {
        persistUser(OWNER_LOGIN);

        restNotificacionMockMvc
            .perform(get(ENTITY_API_URL))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "0"))
            .andExpect(header().string(NotificacionResource.UNREAD_COUNT_HEADER, "0"))
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(username = OWNER_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void getInboxFiltersByReadState() throws Exception {
        User owner = persistUser(OWNER_LOGIN);
        Notificacion unread = persistNotification(
            owner,
            NotificacionTipo.CREDENTIALS,
            NotificacionEstado.PENDIENTE,
            false,
            OLDER_DATE,
            null,
            null,
            "Unread"
        );
        Notificacion read = persistNotification(
            owner,
            NotificacionTipo.CREDENTIALS,
            NotificacionEstado.PENDIENTE,
            true,
            NEWER_DATE,
            null,
            null,
            "Read"
        );

        restNotificacionMockMvc
            .perform(get(ENTITY_API_URL + "?read=false"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "1"))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(unread.getId()));

        restNotificacionMockMvc
            .perform(get(ENTITY_API_URL + "?read=true"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "1"))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(read.getId()));
    }

    @Test
    @WithMockUser(username = OWNER_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void getInboxFiltersByType() throws Exception {
        User owner = persistUser(OWNER_LOGIN);
        persistNotification(
            owner,
            NotificacionTipo.CREDENTIALS,
            NotificacionEstado.PENDIENTE,
            false,
            OLDER_DATE,
            null,
            null,
            "Credentials"
        );
        Notificacion justified = persistNotification(
            owner,
            NotificacionTipo.JUSTIFICACION,
            NotificacionEstado.PENDIENTE,
            false,
            NEWER_DATE,
            "JUSTIFICATION",
            "justification-2",
            "Justification"
        );

        restNotificacionMockMvc
            .perform(get(ENTITY_API_URL + "?type=JUSTIFICACION"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "1"))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(justified.getId()));
    }

    @Test
    @WithMockUser(username = OWNER_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void getInboxFiltersByCreationRange() throws Exception {
        User owner = persistUser(OWNER_LOGIN);
        persistNotification(
            owner,
            NotificacionTipo.CREDENTIALS,
            NotificacionEstado.PENDIENTE,
            false,
            OLDER_DATE,
            null,
            null,
            "Out of range"
        );
        Notificacion inRange = persistNotification(
            owner,
            NotificacionTipo.CREDENTIALS,
            NotificacionEstado.PENDIENTE,
            false,
            MIDDLE_DATE,
            null,
            null,
            "In range"
        );
        persistNotification(
            owner,
            NotificacionTipo.CREDENTIALS,
            NotificacionEstado.PENDIENTE,
            false,
            NEWER_DATE,
            null,
            null,
            "Out of range"
        );

        restNotificacionMockMvc
            .perform(get(ENTITY_API_URL + "?from=" + MIDDLE_DATE + "&to=" + MIDDLE_DATE))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "1"))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(inRange.getId()));
    }

    @Test
    @WithMockUser(username = OWNER_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void getInboxDefaultsToTwentyNotificationsPerPage() throws Exception {
        User owner = persistUser(OWNER_LOGIN);
        for (int index = 0; index < 21; index++) {
            persistNotification(
                owner,
                NotificacionTipo.CREDENTIALS,
                NotificacionEstado.PENDIENTE,
                false,
                OLDER_DATE.plusSeconds(index),
                null,
                null,
                "Notification " + index
            );
        }

        restNotificacionMockMvc
            .perform(get(ENTITY_API_URL))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "21"))
            .andExpect(header().exists("Link"))
            .andExpect(jsonPath("$", hasSize(20)));
    }

    @Test
    @WithMockUser(username = OWNER_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void markNotificationAsReadMarksOnlyThatNotification() throws Exception {
        User owner = persistUser(OWNER_LOGIN);
        User other = persistUser(OTHER_LOGIN);
        Notificacion target = persistNotification(
            owner,
            NotificacionTipo.CREDENTIALS,
            NotificacionEstado.PENDIENTE,
            false,
            NEWER_DATE,
            null,
            null,
            "Target"
        );
        Notificacion untouched = persistNotification(
            owner,
            NotificacionTipo.CREDENTIALS,
            NotificacionEstado.PENDIENTE,
            false,
            OLDER_DATE,
            null,
            null,
            "Untouched"
        );
        Notificacion foreign = persistNotification(
            other,
            NotificacionTipo.CREDENTIALS,
            NotificacionEstado.PENDIENTE,
            false,
            OLDER_DATE,
            null,
            null,
            "Foreign"
        );

        restNotificacionMockMvc
            .perform(patch(ENTITY_API_URL_ID_READ, target.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(target.getId()))
            .andExpect(jsonPath("$.read").value(true))
            .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        assertThat(reload(target).getRead()).isTrue();
        assertThat(reload(untouched).getRead()).isFalse();
        assertThat(reload(foreign).getRead()).isFalse();
    }

    @Test
    @WithMockUser(username = OWNER_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void markNotificationOfAnotherUserReturnsNotFound() throws Exception {
        persistUser(OWNER_LOGIN);
        User other = persistUser(OTHER_LOGIN);
        Notificacion foreign = persistNotification(
            other,
            NotificacionTipo.CREDENTIALS,
            NotificacionEstado.PENDIENTE,
            false,
            OLDER_DATE,
            null,
            null,
            "Foreign"
        );

        restNotificacionMockMvc.perform(patch(ENTITY_API_URL_ID_READ, foreign.getId())).andExpect(status().isNotFound());

        assertThat(reload(foreign).getRead()).isFalse();
    }

    @Test
    @WithMockUser(username = OWNER_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void markAllNotificationsAsReadMarksOnlyOwnNotifications() throws Exception {
        User owner = persistUser(OWNER_LOGIN);
        User other = persistUser(OTHER_LOGIN);
        Notificacion first = persistNotification(
            owner,
            NotificacionTipo.CREDENTIALS,
            NotificacionEstado.PENDIENTE,
            false,
            OLDER_DATE,
            null,
            null,
            "First"
        );
        Notificacion second = persistNotification(
            owner,
            NotificacionTipo.JUSTIFICACION,
            NotificacionEstado.PENDIENTE,
            false,
            NEWER_DATE,
            "JUSTIFICATION",
            "justification-3",
            "Second"
        );
        Notificacion foreign = persistNotification(
            other,
            NotificacionTipo.CREDENTIALS,
            NotificacionEstado.PENDIENTE,
            false,
            OLDER_DATE,
            null,
            null,
            "Foreign"
        );

        restNotificacionMockMvc.perform(patch(ENTITY_API_URL + "/read-all")).andExpect(status().isOk());

        assertThat(reload(first).getRead()).isTrue();
        assertThat(reload(second).getRead()).isTrue();
        assertThat(reload(foreign).getRead()).isFalse();

        restNotificacionMockMvc
            .perform(get(ENTITY_API_URL + "?read=false"))
            .andExpect(status().isOk())
            .andExpect(header().string(NotificacionResource.UNREAD_COUNT_HEADER, "0"));
    }

    /**
     * Persists a user with a resolvable login, so the service can resolve them from the security
     * context.
     */
    private User persistUser(String login) {
        User user = UserResourceIT.createEntity();
        user.setLogin(login);
        user.setEmail(login + "@example.com");
        user.setActivated(true);
        return userRepository.save(user);
    }

    /**
     * Persists a notification with a fixed creation date and a preset id, so the page order and
     * the date filters are deterministic beyond the auditing timestamps.
     */
    private Notificacion persistNotification(
        User owner,
        NotificacionTipo tipo,
        NotificacionEstado estado,
        Boolean read,
        Instant createdDate,
        String referenceType,
        String referenceId,
        String mensaje
    ) {
        Notificacion notificacion = new Notificacion()
            .id(new ObjectId().toHexString())
            .user(owner)
            .tipo(tipo)
            .estado(estado)
            .read(read)
            .referenceType(referenceType)
            .referenceId(referenceId)
            .mensaje(mensaje);
        notificacion.setCreatedDate(createdDate);
        return notificacionRepository.save(notificacion);
    }

    private Notificacion reload(Notificacion notificacion) {
        return notificacionRepository.findById(notificacion.getId()).orElseThrow();
    }
}
