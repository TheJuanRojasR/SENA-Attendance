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
import com.mycompany.senaattendance.service.AlertaNotificationPort;
import com.mycompany.senaattendance.service.AlertaService;
import com.mycompany.senaattendance.service.util.ClassSessions;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
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
 * <p>The evaluation is read-only until it decides to write: it resolves the trimester of the
 * reference date, rebuilds the failures of the apprentice and asks the repository for an active
 * alert of the same combination. Only the resolved alerts are excluded from that lookup, because
 * a resolved combination admits a new alert while the previous one stays as history.
 *
 * <p>The evaluation generates an alert when a threshold is reached, and the resolution flow
 * resolves it automatically when an approved justification drops the count below the threshold
 * (A4). Every write is announced through {@link AlertaNotificationPort}, so the alert lifecycle
 * reaches the inbox (UC018).
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

    private final AlertaNotificationPort alertaNotificationPort;

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
        AlertaNotificationPort alertaNotificationPort,
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
        this.alertaNotificationPort = alertaNotificationPort;
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

        AlertContext context = resolveContext(studentId, classSectionId, referenceDate);
        if (context == null) {
            return;
        }
        if (
            !apprenticeRepository.existsByStudentIdAndGradeIdAndStateAcademic(studentId, context.grade().getId(), StateAcademic.MATRICULADO)
        ) {
            // E2: an apprentice desvinculado from the ficha generates no new alerts.
            return;
        }

        GlobalConfiguration configuration = globalConfigurationRepository
            .findById(GlobalConfiguration.GLOBAL_CONFIGURATION_ID)
            .orElse(null);
        evaluateConsecutive(
            context.student(),
            context.classSection(),
            context.grade(),
            context.trimester(),
            referenceDate,
            consecutiveThreshold(configuration)
        );
        evaluateAccumulated(
            context.student(),
            studentId,
            context.grade(),
            context.trimester(),
            referenceDate,
            accumulatedThreshold(configuration)
        );
    }

    /**
     * {@inheritDoc}
     *
     * <p>The context is resolved the same way as in {@link #evaluate}, but the enrollment (E2) is
     * not required: the approved justification may cover an apprentice who already left the ficha,
     * and the alert that is already active must be able to reach its final state.
     */
    @Override
    public void resolveBelowThreshold(String studentId, String classSectionId, LocalDate referenceDate) {
        if (studentId == null || classSectionId == null || referenceDate == null) {
            return;
        }
        LOG.debug(
            "Request to resolve the absence alerts below the threshold of apprentice {} in materia {} at {}",
            studentId,
            classSectionId,
            referenceDate
        );

        AlertContext context = resolveContext(studentId, classSectionId, referenceDate);
        if (context == null) {
            return;
        }
        GlobalConfiguration configuration = globalConfigurationRepository
            .findById(GlobalConfiguration.GLOBAL_CONFIGURATION_ID)
            .orElse(null);
        resolveConsecutiveBelowThreshold(context, referenceDate, consecutiveThreshold(configuration));
        resolveAccumulatedBelowThreshold(context, studentId, referenceDate, accumulatedThreshold(configuration));
    }

    /**
     * Resolves the active consecutive alert of the materia when the approved justification left
     * the trailing streak below the threshold. A materia without an active alert has nothing to
     * resolve, and a streak that still reaches the threshold keeps the alert active.
     *
     * @param context the resolved evaluation context.
     * @param referenceDate the last day of the window.
     * @param threshold the configured consecutive threshold.
     */
    private void resolveConsecutiveBelowThreshold(AlertContext context, LocalDate referenceDate, int threshold) {
        Optional<Alerta> activeAlert =
            alertaRepository.findFirstByStudentAndClassSectionAndTrimesterAndTypeAndStateInOrderByGeneratedAtDesc(
                context.student(),
                context.classSection(),
                context.trimester(),
                AlertaType.CONSECUTIVAS,
                ACTIVE_STATES
            );
        if (activeAlert.isEmpty()) {
            return;
        }
        int streak = trailingFailures(context.student().getId(), context.classSection(), context.trimester(), referenceDate);
        if (streak >= threshold) {
            return;
        }
        resolve(activeAlert.get());
    }

    /**
     * Resolves the active accumulated alert of the ficha when the approved justification left the
     * total failures below the threshold. The alert has no materia, so it is looked up by the
     * ficha of the materia that triggered the resolution.
     *
     * @param context the resolved evaluation context.
     * @param studentId the apprentice profile id.
     * @param referenceDate the last day of the window.
     * @param threshold the configured accumulated threshold.
     */
    private void resolveAccumulatedBelowThreshold(AlertContext context, String studentId, LocalDate referenceDate, int threshold) {
        Optional<Alerta> activeAlert = alertaRepository.findFirstByStudentAndGradeAndTrimesterAndTypeAndStateInOrderByGeneratedAtDesc(
            context.student(),
            context.grade(),
            context.trimester(),
            AlertaType.ACUMULADAS,
            ACTIVE_STATES
        );
        if (activeAlert.isEmpty()) {
            return;
        }
        int failures = accumulatedFailures(studentId, context.grade(), context.trimester(), referenceDate);
        if (failures >= threshold) {
            return;
        }
        resolve(activeAlert.get());
    }

    /**
     * Persists the automatic resolution of an alert (A4). The alert keeps its history with the
     * instant of the resolution, and the change is announced through the alert channel.
     *
     * @param alerta the alert to resolve.
     */
    private void resolve(Alerta alerta) {
        alerta.setState(AlertaState.RESUELTA_AUTOMATICAMENTE);
        alerta.setResolvedAt(Instant.now(clock));
        alertaRepository.save(alerta);
        LOG.debug("Resolved automatically the {} alert of apprentice {}", alerta.getType(), alerta.getStudent().getId());
        alertaNotificationPort.resolved(alerta);
    }

    /**
     * Resolves the apprentice, the materia, the ficha and the trimester of an evaluation. A
     * materia without a ficha, a date outside every trimester and an unknown apprentice have no
     * context to evaluate.
     *
     * @param studentId the apprentice profile id.
     * @param classSectionId the materia whose session or justification triggered the evaluation.
     * @param referenceDate the day the evaluation is anchored to.
     * @return the resolved context, or {@code null} when any link is missing.
     */
    private AlertContext resolveContext(String studentId, String classSectionId, LocalDate referenceDate) {
        ClassSection classSection = classSectionRepository.findById(classSectionId).orElse(null);
        if (classSection == null || classSection.getGrade() == null) {
            return null;
        }
        Trimester trimester = trimesterRepository.findAllContaining(referenceDate).stream().findFirst().orElse(null);
        if (trimester == null) {
            return null;
        }
        UserProfile student = userProfileRepository.findById(studentId).orElse(null);
        if (student == null) {
            return null;
        }
        return new AlertContext(student, classSection, classSection.getGrade(), trimester);
    }

    /**
     * The resolved context of an evaluation: the apprentice, the materia that triggered it, the
     * ficha of that materia and the trimester of the reference date.
     *
     * @param student the apprentice profile.
     * @param classSection the materia of the evaluation.
     * @param grade the ficha of the materia.
     * @param trimester the trimester of the reference date.
     */
    private record AlertContext(UserProfile student, ClassSection classSection, Grade grade, Trimester trimester) {}

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
        for (LocalDate date : ClassSessions.programmedSessions(schedules, trimester.getStartDate(), referenceDate)) {
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
     * Persists the new alert in its initial state, with the count that triggered it, and
     * announces it through the alert channel (UC018).
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
        alertaNotificationPort.generated(alerta);
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
