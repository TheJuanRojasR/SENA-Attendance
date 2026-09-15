package com.mycompany.senaattendance.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.domain.enumeration.NotificacionEstado;
import com.mycompany.senaattendance.domain.enumeration.NotificacionTipo;
import org.junit.jupiter.api.Test;

class NotificacionTest {

    @Test
    void equalsVerifier() {
        Notificacion notificacion1 = new Notificacion();
        notificacion1.setId("id1");
        Notificacion notificacion2 = new Notificacion();
        notificacion2.setId("id1");
        assertThat(notificacion1).isEqualTo(notificacion2);
        assertThat(notificacion1.hashCode()).isEqualTo(notificacion2.hashCode());

        notificacion2.setId("id2");
        assertThat(notificacion1).isNotEqualTo(notificacion2);
    }

    @Test
    void tipoAndEstadoAssigned() {
        User user = new User();
        user.setLogin("johndoe");

        Notificacion notificacion = new Notificacion();
        notificacion.setUser(user);
        notificacion.setTipo(NotificacionTipo.CREDENTIALS);
        notificacion.setEstado(NotificacionEstado.PENDIENTE);
        notificacion.setMensaje("Could not send credentials email");

        assertThat(notificacion.getUser()).isEqualTo(user);
        assertThat(notificacion.getTipo()).isEqualTo(NotificacionTipo.CREDENTIALS);
        assertThat(notificacion.getEstado()).isEqualTo(NotificacionEstado.PENDIENTE);
        assertThat(notificacion.getMensaje()).isEqualTo("Could not send credentials email");
    }

    @Test
    void readStateAndOriginReferenceAssigned() {
        Notificacion notificacion = new Notificacion().read(false).referenceType("JUSTIFICATION").referenceId("justification-1");

        assertThat(notificacion.getRead()).isFalse();
        assertThat(notificacion.getReferenceType()).isEqualTo("JUSTIFICATION");
        assertThat(notificacion.getReferenceId()).isEqualTo("justification-1");

        notificacion.setRead(true);
        assertThat(notificacion.getRead()).isTrue();
    }
}
