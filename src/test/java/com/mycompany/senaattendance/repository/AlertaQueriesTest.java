package com.mycompany.senaattendance.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.config.MongoDbTestContainer;
import com.mycompany.senaattendance.domain.Alerta;
import com.mycompany.senaattendance.domain.Attendance;
import com.mycompany.senaattendance.domain.ClassException;
import com.mycompany.senaattendance.domain.ClassSchedule;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.AlertaState;
import com.mycompany.senaattendance.domain.enumeration.AlertaType;
import com.mycompany.senaattendance.domain.enumeration.DayOfWeek;
import com.mycompany.senaattendance.domain.enumeration.StateAttendance;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

/**
 * Data-layer tests of the absence alert queries (UC013) against a real MongoDB container. They
 * pin the DBRef matching a query written by hand can get wrong: the scalar {@code _id} lookups
 * resolve a String into the referenced id, while the {@code $in} lookups compare explicit
 * ObjectIds against {@code $id}.
 */
@DataMongoTest
@ImportTestcontainers(MongoDbTestContainer.class)
class AlertaQueriesTest {

    private static final LocalDate TRIMESTER_START = LocalDate.of(2026, 3, 2);

    private static final LocalDate REFERENCE_DATE = LocalDate.of(2026, 3, 30);

    private static final LocalDate SESSION_DATE = LocalDate.of(2026, 3, 9);

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private ClassScheduleRepository classScheduleRepository;

    @Autowired
    private ClassExceptionRepository classExceptionRepository;

    @Autowired
    private ClassSectionRepository classSectionRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private TrimesterRepository trimesterRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private AlertaRepository alertaRepository;

    @AfterEach
    void cleanup() {
        attendanceRepository.deleteAll();
        classScheduleRepository.deleteAll();
        classExceptionRepository.deleteAll();
        alertaRepository.deleteAll();
        classSectionRepository.deleteAll();
        gradeRepository.deleteAll();
        trimesterRepository.deleteAll();
        userProfileRepository.deleteAll();
    }

    @Test
    void attendanceLookupByStudentClassSectionAndDateRangeMatchesTheScalarRefs() {
        Fixture fixture = newFixture();
        UserProfile otherStudent = userProfileRepository.save(new UserProfile());

        persistAttendance(fixture, SESSION_DATE, StateAttendance.FALLA);
        persistAttendance(fixture, REFERENCE_DATE, StateAttendance.PRESENTE);
        persistAttendance(fixture, TRIMESTER_START.minusDays(1), StateAttendance.FALLA);
        persistAttendance(fixture.classSection(), otherStudent, SESSION_DATE, StateAttendance.FALLA);
        persistAttendance(fixture.otherClassSection(), fixture.student(), SESSION_DATE, StateAttendance.FALLA);

        List<Attendance> records = attendanceRepository.findByStudentIdAndClassSectionIdAndDateBetween(
            fixture.student().getId(),
            fixture.classSection().getId(),
            TRIMESTER_START,
            REFERENCE_DATE
        );

        assertThat(records).extracting(Attendance::getDate).containsExactlyInAnyOrder(SESSION_DATE, REFERENCE_DATE);
    }

    @Test
    void accumulatedFailuresLookupNeedsExplicitObjectIds() {
        Fixture fixture = newFixture();
        UserProfile otherStudent = userProfileRepository.save(new UserProfile());

        persistAttendance(fixture, SESSION_DATE, StateAttendance.FALLA);
        persistAttendance(fixture, REFERENCE_DATE, StateAttendance.FALLA);
        persistAttendance(fixture, LocalDate.of(2026, 3, 16), StateAttendance.JUSTIFICADA);
        persistAttendance(fixture, TRIMESTER_START.minusDays(1), StateAttendance.FALLA);
        persistAttendance(fixture.otherClassSection(), fixture.student(), SESSION_DATE, StateAttendance.FALLA);
        persistAttendance(fixture.classSection(), otherStudent, SESSION_DATE, StateAttendance.FALLA);

        List<Attendance> failures = attendanceRepository.findByStudentIdAndClassSectionIdInAndDateBetweenAndStateAttendance(
            fixture.student().getId(),
            List.of(new ObjectId(fixture.classSection().getId()), new ObjectId(fixture.otherClassSection().getId())),
            TRIMESTER_START,
            REFERENCE_DATE,
            StateAttendance.FALLA
        );

        assertThat(failures).hasSize(3);
    }

    @Test
    void scheduleLookupByClassSectionAndTrimesterMatchesBothRefs() {
        Fixture fixture = newFixture();
        Trimester otherTrimester = trimesterRepository.save(
            new Trimester().startDate(LocalDate.of(2026, 7, 6)).endDate(LocalDate.of(2026, 9, 18))
        );

        ClassSchedule schedule = classScheduleRepository.save(mondaySchedule(fixture.classSection(), fixture.trimester()));
        classScheduleRepository.save(mondaySchedule(fixture.otherClassSection(), fixture.trimester()));
        classScheduleRepository.save(mondaySchedule(fixture.classSection(), otherTrimester));

        List<ClassSchedule> schedules = classScheduleRepository.findByClassSectionIdAndTrimesterId(
            fixture.classSection().getId(),
            fixture.trimester().getId()
        );

        assertThat(schedules).extracting(ClassSchedule::getId).containsExactly(schedule.getId());
    }

    @Test
    void exceptionLookupByClassSectionAndDateRangeMatchesTheScalarRef() {
        Fixture fixture = newFixture();
        ClassException inRange = classExceptionRepository.save(
            new ClassException()
                .date(LocalDate.of(2026, 3, 23))
                .reason("Jornada institucional")
                .classSection(fixture.classSection())
        );
        classExceptionRepository.save(
            new ClassException().date(TRIMESTER_START.minusDays(5)).reason("Semana de receso").classSection(fixture.classSection())
        );
        classExceptionRepository.save(
            new ClassException()
                .date(LocalDate.of(2026, 3, 23))
                .reason("Jornada institucional")
                .classSection(fixture.otherClassSection())
        );

        List<ClassException> exceptions = classExceptionRepository.findByClassSectionIdAndDateBetween(
            fixture.classSection().getId(),
            TRIMESTER_START,
            REFERENCE_DATE
        );

        assertThat(exceptions).extracting(ClassException::getId).containsExactly(inRange.getId());
    }

    @Test
    void activeAlertLookupExcludesTheResolvedHistory() {
        Fixture fixture = newFixture();
        Alerta active = alertaRepository.save(consecutiveAlert(fixture, AlertaState.LEIDA, "2026-03-23T10:00:00Z", 3));
        alertaRepository.save(consecutiveAlert(fixture, AlertaState.RESUELTA_AUTOMATICAMENTE, "2026-03-30T10:00:00Z", 3));
        Alerta accumulated = alertaRepository.save(
            new Alerta()
                .student(fixture.student())
                .grade(fixture.grade())
                .trimester(fixture.trimester())
                .type(AlertaType.ACUMULADAS)
                .state(AlertaState.NO_LEIDA)
                .absenceCount(5)
                .threshold(5)
                .generatedAt(Instant.parse("2026-03-30T11:00:00Z"))
        );

        List<AlertaState> activeStates = List.of(AlertaState.NO_LEIDA, AlertaState.LEIDA, AlertaState.ATENDIDA);

        assertThat(
            alertaRepository.findFirstByStudentAndClassSectionAndTrimesterAndTypeAndStateInOrderByGeneratedAtDesc(
                fixture.student(),
                fixture.classSection(),
                fixture.trimester(),
                AlertaType.CONSECUTIVAS,
                activeStates
            )
        ).contains(active);
        assertThat(
            alertaRepository.findFirstByStudentAndGradeAndTrimesterAndTypeAndStateInOrderByGeneratedAtDesc(
                fixture.student(),
                fixture.grade(),
                fixture.trimester(),
                AlertaType.ACUMULADAS,
                activeStates
            )
        ).contains(accumulated);
    }

    private Fixture newFixture() {
        UserProfile student = userProfileRepository.save(new UserProfile());
        Grade grade = gradeRepository.save(new Grade());
        Trimester trimester = trimesterRepository.save(new Trimester().startDate(TRIMESTER_START).endDate(LocalDate.of(2026, 6, 19)));
        ClassSection classSection = classSectionRepository.save(new ClassSection().grade(grade));
        ClassSection otherClassSection = classSectionRepository.save(new ClassSection().grade(grade));
        return new Fixture(student, grade, trimester, classSection, otherClassSection);
    }

    private void persistAttendance(Fixture fixture, LocalDate date, StateAttendance state) {
        persistAttendance(fixture.classSection(), fixture.student(), date, state);
    }

    private void persistAttendance(ClassSection classSection, UserProfile student, LocalDate date, StateAttendance state) {
        attendanceRepository.save(new Attendance().date(date).stateAttendance(state).classSection(classSection).student(student));
    }

    private static ClassSchedule mondaySchedule(ClassSection classSection, Trimester trimester) {
        return new ClassSchedule()
            .dayOfWeek(DayOfWeek.LUNES)
            .startTime(LocalTime.of(7, 0))
            .endTime(LocalTime.of(9, 0))
            .classSection(classSection)
            .trimester(trimester);
    }

    private Alerta consecutiveAlert(Fixture fixture, AlertaState state, String generatedAt, int count) {
        return new Alerta()
            .student(fixture.student())
            .classSection(fixture.classSection())
            .grade(fixture.grade())
            .trimester(fixture.trimester())
            .type(AlertaType.CONSECUTIVAS)
            .state(state)
            .absenceCount(count)
            .threshold(3)
            .generatedAt(Instant.parse(generatedAt));
    }

    /**
     * The persisted documents of one test: the apprentice, the ficha, the trimester and two
     * materias of the ficha.
     */
    private record Fixture(
        UserProfile student,
        Grade grade,
        Trimester trimester,
        ClassSection classSection,
        ClassSection otherClassSection
    ) {}
}
