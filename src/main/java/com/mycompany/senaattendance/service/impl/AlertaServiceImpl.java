package com.mycompany.senaattendance.service.impl;

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
import com.mycompany.senaattendance.service.AlertaService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for evaluating the absence alerts (UC013).
 *
 * <p>The evaluation is read-only until it decides to generate an alert: it resolves the trimester
 * of the reference date, rebuilds the failures of the apprentice and asks the repository for an
 * active alert of the same combination. Only the resolved alerts are excluded from that lookup,
 * because a resolved combination admits a new alert while the previous one stays as history.
 */
@Service
public class AlertaServiceImpl implements AlertaService {

    private static final Logger LOG = LoggerFactory.getLogger(AlertaServiceImpl.class);

    /**
     * The states that keep an alert active, derived from {@link AlertaState#isActive()} so the
     * criterion lives in the enum only.
     */
    private static final List<AlertaState> ACTIVE_STATES = Stream.of(AlertaState.values()).filter(AlertaState::isActive).toList();

    private final AlertaRepository alertaRepository;

    private final AttendanceRepository attendanceRepository;

    private final ClassScheduleRepository classScheduleRepository;

    private final ClassExceptionRepository classExceptionRepository;

    private final ClassSectionRepository classSectionRepository;

    private final ApprenticeRepository apprenticeRepository;

    private final TrimesterRepository trimesterRepository;

    private final UserProfileRepository userProfileRepository;

    private final GlobalConfigurationRepository globalConfigurationRepository;

    private final Clock clock;

    public AlertaServiceImpl(
        AlertaRepository alertaRepository,
        AttendanceRepository attendanceRepository,
        ClassScheduleRepository classScheduleRepository,
        ClassExceptionRepository classExceptionRepository,
        ClassSectionRepository classSectionRepository,
        ApprenticeRepository apprenticeRepository,
        TrimesterRepository trimesterRepository,
        UserProfileRepository userProfileRepository,
        GlobalConfigurationRepository globalConfigurationRepository,
        Clock clock
    ) {
        this.alertaRepository = alertaRepository;
        this.attendanceRepository = attendanceRepository;
        this.classScheduleRepository = classScheduleRepository;
        this.classExceptionRepository = classExceptionRepository;
        this.classSectionRepository = classSectionRepository;
        this.apprenticeRepository = apprenticeRepository;
        this.trimesterRepository = trimesterRepository;
        this.userProfileRepository = userProfileRepository;
        this.globalConfigurationRepository = globalConfigurationRepository;
        this.clock = clock;
    }

    /**
     * {@inheritDoc}
     *
     * <p>A materia without a ficha, a date outside every trimester, an unknown apprentice and an
     * apprentice who is not matriculado in the ficha are resolved without generating alerts.
     */
    @Override
    public void evaluate(String studentId, String classSectionId, LocalDate referenceDate) {
        if (studentId == null || classSectionId == null || referenceDate == null) {
            return;
        }
        LOG.debug("Request to evaluate the absence alerts of apprentice {} in materia {} at {}", studentId, classSectionId, referenceDate);

        ClassSection classSection = classSectionRepository.findById(classSectionId).orElse(null);
        if (classSection == null || classSection.getGrade() == null) {
            return;
        }
        Trimester trimester = trimesterRepository.findAllContaining(referenceDate).stream().findFirst().orElse(null);
        if (trimester == null) {
            return;
        }
        UserProfile student = userProfileRepository.findById(studentId).orElse(null);
        if (student == null) {
            return;
        }
        Grade grade = classSection.getGrade();
        if (!apprenticeRepository.existsByStudentIdAndGradeIdAndStateAcademic(studentId, grade.getId(), StateAcademic.MATRICULADO)) {
            // E2: an apprentice desvinculado from the ficha generates no new alerts.
            return;
        }

        GlobalConfiguration configuration = globalConfigurationRepository
            .findById(GlobalConfiguration.GLOBAL_CONFIGURATION_ID)
            .orElse(null);
        evaluateConsecutive(student, classSection, grade, trimester, referenceDate, consecutiveThreshold(configuration));
        evaluateAccumulated(student, studentId, grade, trimester, referenceDate, accumulatedThreshold(configuration));
    }

    /**
     * Generates the consecutive alert of the materia when its trailing streak reaches the
     * threshold and no active alert of the same combination exists (E1).
     *
     * @param student the apprentice profile.
     * @param classSection the materia of the session.
     * @param grade the ficha of the materia.
     * @param trimester the trimester of the reference date.
     * @param referenceDate the last day of the window.
     * @param threshold the configured consecutive threshold.
     */
    private void evaluateConsecutive(
        UserProfile student,
        ClassSection classSection,
        Grade grade,
        Trimester trimester,
        LocalDate referenceDate,
        int threshold
    ) {
        int streak = trailingFailures(student.getId(), classSection, trimester, referenceDate);
        if (streak < threshold) {
            return;
        }
        boolean activeAlertExists = alertaRepository
            .findFirstByStudentAndClassSectionAndTrimesterAndTypeAndStateInOrderByGeneratedAtDesc(
                student,
                classSection,
                trimester,
                AlertaType.CONSECUTIVAS,
                ACTIVE_STATES
            )
            .isPresent();
        if (activeAlertExists) {
            LOG.debug("The consecutive alert of apprentice {} in materia {} is already active", student.getId(), classSection.getId());
            return;
        }
        generate(student, classSection, grade, trimester, AlertaType.CONSECUTIVAS, streak, threshold);
    }

    /**
     * Generates the accumulated alert of the ficha when the total failures of the apprentice
     * reach the threshold and no active alert of the same combination exists (E1).
     *
     * @param student the apprentice profile.
     * @param studentId the apprentice profile id.
     * @param grade the ficha of the materia.
     * @param trimester the trimester of the reference date.
     * @param referenceDate the last day of the window.
     * @param threshold the configured accumulated threshold.
     */
    private void evaluateAccumulated(
        UserProfile student,
        String studentId,
        Grade grade,
        Trimester trimester,
        LocalDate referenceDate,
        int threshold
    ) {
        int failures = accumulatedFailures(studentId, grade, trimester, referenceDate);
        if (failures < threshold) {
            return;
        }
        boolean activeAlertExists = alertaRepository
            .findFirstByStudentAndGradeAndTrimesterAndTypeAndStateInOrderByGeneratedAtDesc(
                student,
                grade,
                trimester,
                AlertaType.ACUMULADAS,
                ACTIVE_STATES
            )
            .isPresent();
        if (activeAlertExists) {
            LOG.debug("The accumulated alert of apprentice {} in ficha {} is already active", student.getId(), grade.getId());
            return;
        }
        generate(student, null, grade, trimester, AlertaType.ACUMULADAS, failures, threshold);
    }

    /**
     * Measures the trailing streak of failures of one materia. The sequence is walked from the
     * most recent programmed session backwards: a non-teaching exception and a session without a
     * record are skipped without cutting nor adding, a failure adds one and any other recorded
     * state cuts the streak.
     *
     * @param studentId the apprentice profile id.
     * @param classSection the materia of the sessions.
     * @param trimester the trimester of the window.
     * @param referenceDate the last day of the window.
     * @return the trailing streak of failures, zero when the latest session is not a failure.
     */
    private int trailingFailures(String studentId, ClassSection classSection, Trimester trimester, LocalDate referenceDate) {
        List<ClassSchedule> schedules = classScheduleRepository.findByClassSectionIdAndTrimesterId(classSection.getId(), trimester.getId());
        Map<LocalDate, StateAttendance> stateByDate = attendanceRepository
            .findByStudentIdAndClassSectionIdAndDateBetween(studentId, classSection.getId(), trimester.getStartDate(), referenceDate)
            .stream()
            .filter(attendance -> attendance.getDate() != null && attendance.getStateAttendance() != null)
            .collect(Collectors.toMap(Attendance::getDate, Attendance::getStateAttendance, (first, second) -> second));
        Set<LocalDate> exceptionDates = classExceptionRepository
            .findByClassSectionIdAndDateBetween(classSection.getId(), trimester.getStartDate(), referenceDate)
            .stream()
            .map(ClassException::getDate)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        int streak = 0;
        for (LocalDate date : programmedSessions(schedules, trimester.getStartDate(), referenceDate)) {
            if (exceptionDates.contains(date)) {
                continue;
            }
            StateAttendance state = stateByDate.get(date);
            if (state == null) {
                continue;
            }
            if (state != StateAttendance.FALLA) {
                break;
            }
            streak++;
        }
        return streak;
    }

    /**
     * Measures the failures of the apprentice in every materia of the ficha inside the window.
     * The class sections are matched through explicit {@link ObjectId} values, because the
     * repository compares the {@code $in} against the DBRef ids.
     *
     * @param studentId the apprentice profile id.
     * @param grade the ficha of the apprentice.
     * @param trimester the trimester of the window.
     * @param referenceDate the last day of the window.
     * @return the count of failures, zero when the ficha has no usable materia.
     */
    private int accumulatedFailures(String studentId, Grade grade, Trimester trimester, LocalDate referenceDate) {
        List<ObjectId> classSectionIds = classSectionRepository
            .findByGradeId(grade.getId())
            .stream()
            .map(ClassSection::getId)
            .filter(Objects::nonNull)
            .filter(AlertaServiceImpl::isObjectId)
            .map(ObjectId::new)
            .distinct()
            .toList();
        if (classSectionIds.isEmpty()) {
            return 0;
        }
        return attendanceRepository
            .findByStudentIdAndClassSectionIdInAndDateBetweenAndStateAttendance(
                studentId,
                classSectionIds,
                trimester.getStartDate(),
                referenceDate,
                StateAttendance.FALLA
            )
            .size();
    }

    /**
     * Expands the weekday schedules of a materia into the concrete dates of the window,
     * most recent first. The dates marked as a non-teaching exception are removed by the caller.
     *
     * @param schedules the schedules of the materia in the trimester.
     * @param start the first day of the window.
     * @param end the last day of the window.
     * @return the programmed session dates in descending order, possibly empty.
     */
    private static List<LocalDate> programmedSessions(List<ClassSchedule> schedules, LocalDate start, LocalDate end) {
        if (start == null || end == null || start.isAfter(end)) {
            return List.of();
        }
        Set<LocalDate> dates = new HashSet<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            for (ClassSchedule schedule : schedules) {
                if (matches(date, schedule.getDayOfWeek())) {
                    dates.add(date);
                    break;
                }
            }
        }
        return dates.stream().sorted(Comparator.reverseOrder()).toList();
    }

    /**
     * @param date the candidate session date.
     * @param scheduleDay the weekday of the schedule.
     * @return whether the date falls on that weekday.
     */
    private static boolean matches(LocalDate date, DayOfWeek scheduleDay) {
        if (scheduleDay == null) {
            return false;
        }
        return switch (scheduleDay) {
            case LUNES -> date.getDayOfWeek() == java.time.DayOfWeek.MONDAY;
            case MARTES -> date.getDayOfWeek() == java.time.DayOfWeek.TUESDAY;
            case MIERCOLES -> date.getDayOfWeek() == java.time.DayOfWeek.WEDNESDAY;
            case JUEVES -> date.getDayOfWeek() == java.time.DayOfWeek.THURSDAY;
            case VIERNES -> date.getDayOfWeek() == java.time.DayOfWeek.FRIDAY;
            case SABADO -> date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY;
            case DOMINGO -> date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY;
        };
    }

    /**
     * Persists the new alert in its initial state, with the count that triggered it.
     *
     * @param student the apprentice profile.
     * @param classSection the materia of a consecutive alert, or {@code null} in an accumulated one.
     * @param grade the ficha of the alert.
     * @param trimester the trimester of the alert.
     * @param type the alert type.
     * @param count the count that reached the threshold.
     * @param threshold the threshold that was reached.
     */
    private void generate(
        UserProfile student,
        ClassSection classSection,
        Grade grade,
        Trimester trimester,
        AlertaType type,
        int count,
        int threshold
    ) {
        Alerta alerta = new Alerta()
            .student(student)
            .classSection(classSection)
            .grade(grade)
            .trimester(trimester)
            .type(type)
            .state(AlertaState.NO_LEIDA)
            .absenceCount(count)
            .threshold(threshold)
            .generatedAt(Instant.now(clock));
        alertaRepository.save(alerta);
        LOG.debug("Generated a {} alert for apprentice {} with count {} at threshold {}", type, student.getId(), count, threshold);
    }

    /**
     * @param configuration the singleton configuration, possibly {@code null}.
     * @return the configured consecutive threshold, or its default when missing (E4).
     */
    private static int consecutiveThreshold(GlobalConfiguration configuration) {
        return Optional.ofNullable(configuration)
            .map(GlobalConfiguration::getConsecutiveAbsenceAlertThreshold)
            .filter(Objects::nonNull)
            .orElse(GlobalConfigurationServiceImpl.DEFAULT_CONSECUTIVE_ABSENCE_ALERT_THRESHOLD);
    }

    /**
     * @param configuration the singleton configuration, possibly {@code null}.
     * @return the configured accumulated threshold, or its default when missing (E4).
     */
    private static int accumulatedThreshold(GlobalConfiguration configuration) {
        return Optional.ofNullable(configuration)
            .map(GlobalConfiguration::getAccumulatedAbsenceAlertThreshold)
            .filter(Objects::nonNull)
            .orElse(GlobalConfigurationServiceImpl.DEFAULT_ACCUMULATED_ABSENCE_ALERT_THRESHOLD);
    }

    /**
     * @param id the candidate id.
     * @return whether the id is a usable {@code ObjectId} string.
     */
    private static boolean isObjectId(String id) {
        return id != null && ObjectId.isValid(id);
    }
}
