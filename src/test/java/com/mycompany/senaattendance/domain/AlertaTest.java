package com.mycompany.senaattendance.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.domain.enumeration.AlertaState;
import com.mycompany.senaattendance.domain.enumeration.AlertaType;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Alerta} domain model: the fields of the alert, the nullable scope of
 * the consecutive type and the states that keep an alert active (UC013).
 */
class AlertaTest {

    @Test
    void equalsVerifier() {
        Alerta alerta1 = new Alerta();
        alerta1.setId("id1");
        Alerta alerta2 = new Alerta();
        alerta2.setId("id1");
        assertThat(alerta1).isEqualTo(alerta2);
        assertThat(alerta1.hashCode()).isEqualTo(alerta2.hashCode());

        alerta2.setId("id2");
        assertThat(alerta1).isNotEqualTo(alerta2);
    }

    @Test
    void traceabilityFieldsAssigned() {
        UserProfile student = new UserProfile();
        ClassSection classSection = new ClassSection();
        Grade grade = new Grade();
        Trimester trimester = new Trimester();
        Instant generatedAt = Instant.parse("2026-03-10T12:00:00Z");

        Alerta alerta = new Alerta()
            .id("alerta-1")
            .student(student)
            .classSection(classSection)
            .grade(grade)
            .trimester(trimester)
            .type(AlertaType.CONSECUTIVAS)
            .state(AlertaState.NO_LEIDA)
            .absenceCount(4)
            .threshold(3)
            .generatedAt(generatedAt);

        assertThat(alerta.getId()).isEqualTo("alerta-1");
        assertThat(alerta.getStudent()).isEqualTo(student);
        assertThat(alerta.getClassSection()).isEqualTo(classSection);
        assertThat(alerta.getGrade()).isEqualTo(grade);
        assertThat(alerta.getTrimester()).isEqualTo(trimester);
        assertThat(alerta.getType()).isEqualTo(AlertaType.CONSECUTIVAS);
        assertThat(alerta.getState()).isEqualTo(AlertaState.NO_LEIDA);
        assertThat(alerta.getAbsenceCount()).isEqualTo(4);
        assertThat(alerta.getThreshold()).isEqualTo(3);
        assertThat(alerta.getGeneratedAt()).isEqualTo(generatedAt);
        assertThat(alerta.getResolvedAt()).isNull();
        assertThat(alerta.getObservation()).isNull();
    }

    @Test
    void accumulatedAlertHasNoMateriaAndCanBeAttended() {
        Alerta alerta = new Alerta()
            .type(AlertaType.ACUMULADAS)
            .state(AlertaState.ATENDIDA)
            .absenceCount(5)
            .threshold(5)
            .observation("Se contactó al aprendiz y se citó al acudiente")
            .resolvedAt(Instant.parse("2026-03-11T08:30:00Z"));

        assertThat(alerta.getClassSection()).isNull();
        assertThat(alerta.getState()).isEqualTo(AlertaState.ATENDIDA);
        assertThat(alerta.getObservation()).isEqualTo("Se contactó al aprendiz y se citó al acudiente");
        assertThat(alerta.getResolvedAt()).isEqualTo(Instant.parse("2026-03-11T08:30:00Z"));
    }

    @Test
    void onlyResolvedAlertsAreInactive() {
        assertThat(AlertaState.NO_LEIDA.isActive()).isTrue();
        assertThat(AlertaState.LEIDA.isActive()).isTrue();
        assertThat(AlertaState.ATENDIDA.isActive()).isTrue();
        assertThat(AlertaState.RESUELTA_AUTOMATICAMENTE.isActive()).isFalse();
    }
}
