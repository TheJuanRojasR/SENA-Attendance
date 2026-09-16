package com.mycompany.senaattendance.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.mycompany.senaattendance.domain.Alerta;
import com.mycompany.senaattendance.domain.Attendance;
import com.mycompany.senaattendance.domain.ClassException;
import com.mycompany.senaattendance.domain.ClassSchedule;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.GlobalConfiguration;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.AlertaState;
import com.mycompany.senaattendance.domain.enumeration.AlertaType;
import com.mycompany.senaattendance.domain.enumeration.DayOfWeek;
import com.mycompany.senaattendance.domain.enumeration.StateAcademic;
import com.mycompany.senaattendance.domain.enumeration.StateAttendance;
import com.mycompany.senaattendance.repository.AlertaRepository;
import com.mycompany.senaattendance.repository.ApprenticeRepository;
import com.mycompany.senaattendance.repository.AttendanceRepository;
import com.mycompany.senaattendance.repository.ClassExceptionRepository;
import com.mycompany.senaattendance.repository.ClassScheduleRepository;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.GlobalConfigurationRepository;
import com.mycompany.senaattendance.repository.TrimesterRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for the absence alert engine (UC013): the consecutive streak over the programmed
 * sessions of a materia, the accumulated failures of the ficha, the deduplication of active
 * alerts, the matriculado guard (E2) and the default thresholds (E4). The clock is fixed and the
 * repositories are mocked, so every case is decided only by the dates and states of the fixture.
 */
@ExtendWith(MockitoExtension.class)
class AlertaServiceImplTest {

    private static final String STUDENT_ID = "65f1a2b3c4d5e6f7a8b9c0a1";

    private static final String CLASS_SECTION_ID = "65f1a2b3c4d5e6f7a8b9c0b1";

    private static final String OTHER_CLASS_SECTION_ID = "65f1a2b3c4d5e6f7a8b9c0b2";

    private static final String GRADE_ID = "65f1a2b3c4d5e6f7a8b9c0c1";

    private static final String TRIMESTER_ID = "65f1a2b3c4d5e6f7a8b9c0d1";

    private static final LocalDate TRIMESTER_START = LocalDate.of(2026, 3, 2);

    private static final LocalDate TRIMESTER_END = LocalDate.of(2026, 6, 19);

    /**
     * A Monday, so the programmed sessions of a Monday schedule are the five Mondays between the
     * trimester start and this date: 03-02, 03-09, 03-16, 03-23 and 03-30.
     */
    private static final LocalDate REFERENCE_DATE = LocalDate.of(2026, 3, 30);

    private static final Instant NOW = Instant.parse("2026-03-30T15:00:00Z");

    @Mock
    private AlertaRepository alertaRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private ClassScheduleRepository classScheduleRepository;

    @Mock
    private ClassExceptionRepository classExceptionRepository;

    @Mock
    private ClassSectionRepository classSectionRepository;

    @Mock
    private ApprenticeRepository apprenticeRepository;

    @Mock
    private TrimesterRepository trimesterRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private GlobalConfigurationRepository globalConfigurationRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private AlertaServiceImpl alertaService;

    private final UserProfile student = new UserProfile().id(STUDENT_ID);

    private final Grade grade = new Grade().id(GRADE_ID);

    private final ClassSection classSection = new ClassSection().id(CLASS_SECTION_ID).grade(grade);

    private final Trimester trimester = new Trimester().id(TRIMESTER_ID).startDate(TRIMESTER_START).endDate(TRIMESTER_END);

    // -----------------------------------------------------------------
    // Consecutive alerts
    // -----------------------------------------------------------------

    @Test
    void consecutiveStreakReachingTheThresholdGeneratesTheAlert() {
        mockEvaluationContext();
        mockThresholds(3, 5);
        mockMondaySessions();
        mockConsecutiveRecords(
            attendance(REFERENCE_DATE, StateAttendance.FALLA),
            attendance(LocalDate.of(2026, 3, 23), StateAttendance.FALLA),
            attendance(LocalDate.of(2026, 3, 16), StateAttendance.FALLA),
            attendance(LocalDate.of(2026, 3, 9), StateAttendance.FALLA)
        );
        mockClock();

        alertaService.evaluate(STUDENT_ID, CLASS_SECTION_ID, REFERENCE_DATE);

        Alerta saved = savedAlert();
        assertThat(saved.getType()).isEqualTo(AlertaType.CONSECUTIVAS);
        assertThat(saved.getState()).isEqualTo(AlertaState.NO_LEIDA);
        assertThat(saved.getAbsenceCount()).isEqualTo(4);
        assertThat(saved.getThreshold()).isEqualTo(3);
        assertThat(saved.getStudent()).isEqualTo(student);
        assertThat(saved.getClassSection()).isEqualTo(classSection);
        assertThat(saved.getGrade()).isEqualTo(grade);
        assertThat(saved.getTrimester()).isEqualTo(trimester);
        assertThat(saved.getGeneratedAt()).isEqualTo(NOW);
        assertThat(saved.getResolvedAt()).isNull();
        assertThat(saved.getObservation()).isNull();
    }

    @Test
    void consecutiveStreakBelowTheThresholdGeneratesNoAlert() {
        mockEvaluationContext();
        mockThresholds(3, 5);
        mockMondaySessions();
        mockConsecutiveRecords(attendance(LocalDate.of(2026, 3, 23), StateAttendance.FALLA));

        alertaService.evaluate(STUDENT_ID, CLASS_SECTION_ID, REFERENCE_DATE);

        verify(alertaRepository, never()).save(any());
    }

    @Test
    void presenteCutsTheStreak() {
        mockEvaluationContext();
        mockThresholds(3, 5);
        mockMondaySessions();
        mockConsecutiveRecords(
            attendance(LocalDate.of(2026, 3, 23), StateAttendance.PRESENTE),
            attendance(LocalDate.of(2026, 3, 16), StateAttendance.FALLA),
            attendance(LocalDate.of(2026, 3, 9), StateAttendance.FALLA)
        );

        alertaService.evaluate(STUDENT_ID, CLASS_SECTION_ID, REFERENCE_DATE);

        verify(alertaRepository, never()).save(any());
    }

    @Test
    void justificadaCutsTheStreak() {
        mockEvaluationContext();
        mockThresholds(3, 5);
        mockMondaySessions();
        mockConsecutiveRecords(
            attendance(LocalDate.of(2026, 3, 23), StateAttendance.JUSTIFICADA),
            attendance(LocalDate.of(2026, 3, 16), StateAttendance.FALLA),
            attendance(LocalDate.of(2026, 3, 9), StateAttendance.FALLA)
        );

        alertaService.evaluate(STUDENT_ID, CLASS_SECTION_ID, REFERENCE_DATE);

        verify(alertaRepository, never()).save(any());
    }

    @Test
    void nonTeachingDateIsSkippedWithoutCuttingNorAdding() {
        mockEvaluationContext();
        mockThresholds(3, 5);
        mockMondaySessions();
        mockConsecutiveRecords(
            attendance(REFERENCE_DATE, StateAttendance.FALLA),
            attendance(LocalDate.of(2026, 3, 16), StateAttendance.FALLA),
            attendance(LocalDate.of(2026, 3, 9), StateAttendance.FALLA)
        );
        when(classExceptionRepository.findByClassSectionIdAndDateBetween(CLASS_SECTION_ID, TRIMESTER_START, REFERENCE_DATE)).thenReturn(
            List.of(new ClassException().date(LocalDate.of(2026, 3, 23)))
        );
        mockClock();

        alertaService.evaluate(STUDENT_ID, CLASS_SECTION_ID, REFERENCE_DATE);

        // 03-30 and 03-16 and 03-09 are failures; the non-teaching 03-23 is removed from the
        // sequence instead of cutting it, so the streak is three, not one.
        assertThat(savedAlert().getAbsenceCount()).isEqualTo(3);
    }

    @Test
    void sessionWithoutARecordNeitherAddsNorCutsTheStreak() {
        mockEvaluationContext();
        mockThresholds(3, 5);
        mockMondaySessions();
        mockConsecutiveRecords(
            attendance(REFERENCE_DATE, StateAttendance.FALLA),
            attendance(LocalDate.of(2026, 3, 23), StateAttendance.FALLA),
            attendance(LocalDate.of(2026, 3, 9), StateAttendance.FALLA)
        );
        mockClock();

        alertaService.evaluate(STUDENT_ID, CLASS_SECTION_ID, REFERENCE_DATE);

        // 03-16 has no record; the failures of 03-09, 03-23 and 03-30 still add up to three.
        assertThat(savedAlert().getAbsenceCount()).isEqualTo(3);
    }

    @Test
    void missingConfigurationFallsBackToTheDefaultThresholds() {
        mockEvaluationContext();
        when(globalConfigurationRepository.findById(GlobalConfiguration.GLOBAL_CONFIGURATION_ID)).thenReturn(Optional.empty());
        mockMondaySessions();
        mockConsecutiveRecords(
            attendance(LocalDate.of(2026, 3, 23), StateAttendance.FALLA),
            attendance(LocalDate.of(2026, 3, 16), StateAttendance.FALLA),
            attendance(LocalDate.of(2026, 3, 9), StateAttendance.FALLA)
        );
        mockClock();

        alertaService.evaluate(STUDENT_ID, CLASS_SECTION_ID, REFERENCE_DATE);

        assertThat(savedAlert().getThreshold()).isEqualTo(3);
    }

    @Test
    @SuppressWarnings("unchecked")
    void activeConsecutiveAlertIsNotDuplicated() {
        mockEvaluationContext();
        mockThresholds(3, 5);
        mockMondaySessions();
        mockConsecutiveRecords(
            attendance(LocalDate.of(2026, 3, 23), StateAttendance.FALLA),
            attendance(LocalDate.of(2026, 3, 16), StateAttendance.FALLA),
            attendance(LocalDate.of(2026, 3, 9), StateAttendance.FALLA)
        );
        when(
            alertaRepository.findFirstByStudentAndClassSectionAndTrimesterAndTypeAndStateInOrderByGeneratedAtDesc(
                eq(student),
                eq(classSection),
                eq(trimester),
                eq(AlertaType.CONSECUTIVAS),
                anyCollection()
            )
        ).thenReturn(Optional.of(new Alerta().id("active-alert")));

        alertaService.evaluate(STUDENT_ID, CLASS_SECTION_ID, REFERENCE_DATE);

        verify(alertaRepository, never()).save(any());
        ArgumentCaptor<Collection<AlertaState>> statesCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(alertaRepository).findFirstByStudentAndClassSectionAndTrimesterAndTypeAndStateInOrderByGeneratedAtDesc(
            eq(student),
            eq(classSection),
            eq(trimester),
            eq(AlertaType.CONSECUTIVAS),
            statesCaptor.capture()
        );
        assertThat(statesCaptor.getValue()).containsExactlyInAnyOrder(AlertaState.NO_LEIDA, AlertaState.LEIDA, AlertaState.ATENDIDA);
    }

    // -----------------------------------------------------------------
    // Accumulated alerts
    // -----------------------------------------------------------------

    @Test
    void accumulatedFailuresReachingTheThresholdGenerateTheFichaAlert() {
        ClassSection otherClassSection = new ClassSection().id(OTHER_CLASS_SECTION_ID).grade(grade);
        mockEvaluationContext();
        mockThresholds(3, 5);
        when(classSectionRepository.findByGradeId(GRADE_ID)).thenReturn(List.of(classSection, otherClassSection));
        when(
            attendanceRepository.findByStudentIdAndClassSectionIdInAndDateBetweenAndStateAttendance(
                STUDENT_ID,
                List.of(new ObjectId(CLASS_SECTION_ID), new ObjectId(OTHER_CLASS_SECTION_ID)),
                TRIMESTER_START,
                REFERENCE_DATE,
                StateAttendance.FALLA
            )
        ).thenReturn(
            List.of(
                attendance(LocalDate.of(2026, 3, 2), StateAttendance.FALLA),
                attendance(LocalDate.of(2026, 3, 3), StateAttendance.FALLA),
                attendance(LocalDate.of(2026, 3, 4), StateAttendance.FALLA),
                attendance(LocalDate.of(2026, 3, 5), StateAttendance.FALLA),
                attendance(LocalDate.of(2026, 3, 6), StateAttendance.FALLA)
            )
        );
        mockClock();

        alertaService.evaluate(STUDENT_ID, CLASS_SECTION_ID, REFERENCE_DATE);

        Alerta saved = savedAlert();
        assertThat(saved.getType()).isEqualTo(AlertaType.ACUMULADAS);
        assertThat(saved.getState()).isEqualTo(AlertaState.NO_LEIDA);
        assertThat(saved.getAbsenceCount()).isEqualTo(5);
        assertThat(saved.getThreshold()).isEqualTo(5);
        assertThat(saved.getStudent()).isEqualTo(student);
        assertThat(saved.getClassSection()).isNull();
        assertThat(saved.getGrade()).isEqualTo(grade);
        assertThat(saved.getTrimester()).isEqualTo(trimester);
        assertThat(saved.getGeneratedAt()).isEqualTo(NOW);
    }

    @Test
    void accumulatedAlertBelowTheThresholdGeneratesNoAlert() {
        mockEvaluationContext();
        mockThresholds(3, 5);
        when(classSectionRepository.findByGradeId(GRADE_ID)).thenReturn(List.of(classSection));
        when(
            attendanceRepository.findByStudentIdAndClassSectionIdInAndDateBetweenAndStateAttendance(
                STUDENT_ID,
                List.of(new ObjectId(CLASS_SECTION_ID)),
                TRIMESTER_START,
                REFERENCE_DATE,
                StateAttendance.FALLA
            )
        ).thenReturn(List.of(attendance(LocalDate.of(2026, 3, 2), StateAttendance.FALLA)));

        alertaService.evaluate(STUDENT_ID, CLASS_SECTION_ID, REFERENCE_DATE);

        verify(alertaRepository, never()).save(any());
    }

    // -----------------------------------------------------------------
    // Guards
    // -----------------------------------------------------------------

    @Test
    void apprenticeWhoIsNotMatriculadoGeneratesNoAlerts() {
        when(classSectionRepository.findById(CLASS_SECTION_ID)).thenReturn(Optional.of(classSection));
        when(trimesterRepository.findAllContaining(REFERENCE_DATE)).thenReturn(List.of(trimester));
        when(userProfileRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));
        when(apprenticeRepository.existsByStudentIdAndGradeIdAndStateAcademic(STUDENT_ID, GRADE_ID, StateAcademic.MATRICULADO)).thenReturn(
            false
        );

        alertaService.evaluate(STUDENT_ID, CLASS_SECTION_ID, REFERENCE_DATE);

        verifyNoInteractions(alertaRepository, globalConfigurationRepository, attendanceRepository);
        verifyNoInteractions(classScheduleRepository, classExceptionRepository);
        verify(classSectionRepository, never()).findByGradeId(any());
    }

    @Test
    void dateOutsideEveryTrimesterGeneratesNoAlerts() {
        when(classSectionRepository.findById(CLASS_SECTION_ID)).thenReturn(Optional.of(classSection));
        when(trimesterRepository.findAllContaining(REFERENCE_DATE)).thenReturn(List.of());

        alertaService.evaluate(STUDENT_ID, CLASS_SECTION_ID, REFERENCE_DATE);

        verifyNoInteractions(userProfileRepository, apprenticeRepository, alertaRepository, attendanceRepository);
    }

    @Test
    void materiaWithoutFichaGeneratesNoAlerts() {
        ClassSection orphanMateria = new ClassSection().id(CLASS_SECTION_ID);
        when(classSectionRepository.findById(CLASS_SECTION_ID)).thenReturn(Optional.of(orphanMateria));

        alertaService.evaluate(STUDENT_ID, CLASS_SECTION_ID, REFERENCE_DATE);

        verifyNoInteractions(trimesterRepository, userProfileRepository, apprenticeRepository, alertaRepository);
    }

    // -----------------------------------------------------------------
    // Fixture helpers
    // -----------------------------------------------------------------

    private void mockEvaluationContext() {
        when(classSectionRepository.findById(CLASS_SECTION_ID)).thenReturn(Optional.of(classSection));
        when(trimesterRepository.findAllContaining(REFERENCE_DATE)).thenReturn(List.of(trimester));
        when(userProfileRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));
        when(apprenticeRepository.existsByStudentIdAndGradeIdAndStateAcademic(STUDENT_ID, GRADE_ID, StateAcademic.MATRICULADO)).thenReturn(
            true
        );
    }

    private void mockThresholds(int consecutive, int accumulated) {
        GlobalConfiguration configuration = new GlobalConfiguration();
        configuration.setConsecutiveAbsenceAlertThreshold(consecutive);
        configuration.setAccumulatedAbsenceAlertThreshold(accumulated);
        when(globalConfigurationRepository.findById(GlobalConfiguration.GLOBAL_CONFIGURATION_ID)).thenReturn(Optional.of(configuration));
    }

    private void mockMondaySessions() {
        when(classScheduleRepository.findByClassSectionIdAndTrimesterId(CLASS_SECTION_ID, TRIMESTER_ID)).thenReturn(
            List.of(new ClassSchedule().dayOfWeek(DayOfWeek.LUNES))
        );
    }

    private void mockConsecutiveRecords(Attendance... records) {
        when(
            attendanceRepository.findByStudentIdAndClassSectionIdAndDateBetween(
                STUDENT_ID,
                CLASS_SECTION_ID,
                TRIMESTER_START,
                REFERENCE_DATE
            )
        ).thenReturn(List.of(records));
    }

    private void mockClock() {
        when(clock.instant()).thenReturn(NOW);
    }

    private static Attendance attendance(LocalDate date, StateAttendance state) {
        return new Attendance().date(date).stateAttendance(state);
    }

    private Alerta savedAlert() {
        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaRepository).save(captor.capture());
        return captor.getValue();
    }
}
