package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.Apprentice;
import com.mycompany.senaattendance.domain.Attendance;
import com.mycompany.senaattendance.domain.ClassException;
import com.mycompany.senaattendance.domain.ClassSchedule;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.GlobalConfiguration;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.Justification;
import com.mycompany.senaattendance.domain.JustificationDetails;
import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.StateAcademic;
import com.mycompany.senaattendance.domain.enumeration.StateAttendance;
import com.mycompany.senaattendance.domain.enumeration.StateGrade;
import com.mycompany.senaattendance.domain.enumeration.StateJustification;
import com.mycompany.senaattendance.repository.AlertaReadScope;
import com.mycompany.senaattendance.repository.AlertaRepository;
import com.mycompany.senaattendance.repository.ApprenticeRepository;
import com.mycompany.senaattendance.repository.AttendanceRepository;
import com.mycompany.senaattendance.repository.ClassExceptionRepository;
import com.mycompany.senaattendance.repository.ClassScheduleRepository;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.GlobalConfigurationRepository;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.repository.JustificationDetailsRepository;
import com.mycompany.senaattendance.repository.JustificationRepository;
import com.mycompany.senaattendance.repository.ModalityRepository;
import com.mycompany.senaattendance.repository.ProgramRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.DashboardService;
import com.mycompany.senaattendance.service.JustificationDetailsService;
import com.mycompany.senaattendance.service.dto.dashboard.AdminDashboardDTO;
import com.mycompany.senaattendance.service.dto.dashboard.ApprenticeDashboardDTO;
import com.mycompany.senaattendance.service.dto.dashboard.AttendanceSummaryDTO;
import com.mycompany.senaattendance.service.dto.dashboard.DashboardClassSessionDTO;
import com.mycompany.senaattendance.service.dto.dashboard.DashboardDTO;
import com.mycompany.senaattendance.service.dto.dashboard.DashboardKpisDTO;
import com.mycompany.senaattendance.service.dto.dashboard.EnrolledGradeDTO;
import com.mycompany.senaattendance.service.dto.dashboard.EnrolledSubjectDTO;
import com.mycompany.senaattendance.service.dto.dashboard.GradeFailureDTO;
import com.mycompany.senaattendance.service.dto.dashboard.InstructorDashboardDTO;
import com.mycompany.senaattendance.service.dto.dashboard.JustificationDeadlineDTO;
import com.mycompany.senaattendance.service.dto.dashboard.JustificationSummaryDTO;
import com.mycompany.senaattendance.service.dto.dashboard.RecentGradeDTO;
import com.mycompany.senaattendance.service.util.BusinessDays;
import com.mycompany.senaattendance.service.util.ClassSessions;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardServiceImpl.class);

    /**
     * Days a forward-looking panel looks ahead, so a long trimester does not expand an unbounded
     * number of session dates.
     */
    private static final int LOOKAHEAD_DAYS = 60;

    /**
     * Upper bound of the sessions of today the instructor panel lists.
     */
    private static final int TODAY_CLASSES_LIMIT = 10;

    /**
     * Upper bound of the upcoming sessions the role panels list.
     */
    private static final int UPCOMING_CLASSES_LIMIT = 5;

    private final UserProfileRepository userProfileRepository;
    private final GradeRepository gradeRepository;
    private final ProgramRepository programRepository;
    private final ModalityRepository modalityRepository;
    private final ClassSectionRepository classSectionRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final ClassExceptionRepository classExceptionRepository;
    private final ApprenticeRepository apprenticeRepository;
    private final AttendanceRepository attendanceRepository;
    private final AlertaRepository alertaRepository;
    private final JustificationRepository justificationRepository;
    private final JustificationDetailsRepository justificationDetailsRepository;
    private final GlobalConfigurationRepository globalConfigurationRepository;
    private final JustificationDetailsService justificationDetailsService;
    private final CurrentUserContext currentUserContext;
    private final Clock clock;

    public DashboardServiceImpl(
        UserProfileRepository userProfileRepository,
        GradeRepository gradeRepository,
        ProgramRepository programRepository,
        ModalityRepository modalityRepository,
        ClassSectionRepository classSectionRepository,
        ClassScheduleRepository classScheduleRepository,
        ClassExceptionRepository classExceptionRepository,
        ApprenticeRepository apprenticeRepository,
        AttendanceRepository attendanceRepository,
        AlertaRepository alertaRepository,
        JustificationRepository justificationRepository,
        JustificationDetailsRepository justificationDetailsRepository,
        GlobalConfigurationRepository globalConfigurationRepository,
        JustificationDetailsService justificationDetailsService,
        CurrentUserContext currentUserContext,
        Clock clock
    ) {
        this.userProfileRepository = userProfileRepository;
        this.gradeRepository = gradeRepository;
        this.programRepository = programRepository;
        this.modalityRepository = modalityRepository;
        this.classSectionRepository = classSectionRepository;
        this.classScheduleRepository = classScheduleRepository;
        this.classExceptionRepository = classExceptionRepository;
        this.apprenticeRepository = apprenticeRepository;
        this.attendanceRepository = attendanceRepository;
        this.alertaRepository = alertaRepository;
        this.justificationRepository = justificationRepository;
        this.justificationDetailsRepository = justificationDetailsRepository;
        this.globalConfigurationRepository = globalConfigurationRepository;
        this.justificationDetailsService = justificationDetailsService;
        this.currentUserContext = currentUserContext;
        this.clock = clock;
    }

    @Override
    public DashboardDTO getDashboardForCurrentUser() {
        LOG.debug("Request to get dashboard for current user");

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return buildAdminDashboard();
        }
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.INSTRUCTOR)) {
            return buildInstructorDashboard();
        }
        return buildApprenticeDashboard();
    }

    // ------- ADMIN DASHBOARD -------
    private AdminDashboardDTO buildAdminDashboard() {
        return new AdminDashboardDTO(buildAdminKpis(), buildRecentGrades());
    }

    private DashboardKpisDTO buildAdminKpis() {
        return new DashboardKpisDTO(
            userProfileRepository.count(),
            gradeRepository.countByState(StateGrade.ACTIVA),
            programRepository.count(),
            modalityRepository.count()
        );
    }

    private List<RecentGradeDTO> buildRecentGrades() {
        return gradeRepository.findTop5ByOrderByCreatedDateDesc().stream().map(this::toRecentGradeDTO).toList();
    }

    private RecentGradeDTO toRecentGradeDTO(Grade grade) {
        return new RecentGradeDTO(
            grade.getId(),
            grade.getCode(),
            grade.getProgram() != null ? grade.getProgram().getName() : null,
            resolveInstructorName(grade.getId()),
            grade.getState()
        );
    }

    private String resolveInstructorName(String gradeId) {
        return classSectionRepository
            .findByGradeId(gradeId)
            .stream()
            .map(ClassSection::getInstructor)
            .filter(Objects::nonNull)
            .findFirst()
            .map(instructor -> instructor.getFirstName() + " " + instructor.getFirstLastName())
            .orElse(null);
    }

    // ------- INSTRUCTOR DASHBOARD -------
    /**
     * Builds the instructor panel (UC023): the work left to decide, the active alerts of their
     * materias and fichas, their teaching load and the sessions of the active trimester. The
     * trimester dependent lists travel empty with the E2 message when no trimester is active; the
     * remaining indicators are trimester independent and are still computed.
     *
     * @return the instructor dashboard.
     */
    private InstructorDashboardDTO buildInstructorDashboard() {
        String profileId = currentUserContext.profileId();
        if (profileId == null) {
            return new InstructorDashboardDTO(0, 0, 0, 0, 0, List.of(), List.of(), null);
        }

        List<ClassSection> classSections = classSectionRepository.findByInstructorId(profileId);
        Set<String> gradeIds = classSections
            .stream()
            .map(ClassSection::getGrade)
            .filter(Objects::nonNull)
            .map(Grade::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        long pendingJustifications = justificationDetailsService
            .findPendingForCurrentUser(StateJustification.PENDIENTE, null, null, null, PageRequest.of(0, 1))
            .getTotalElements();
        long activeAlerts = alertaRepository.countActiveAlerts(scopeOf(classSections), null);

        Optional<Trimester> activeTrimester = currentUserContext.activeTrimester();
        List<DashboardClassSessionDTO> todayClasses = List.of();
        List<DashboardClassSessionDTO> upcomingClasses = List.of();
        String trimesterMessage = null;
        if (activeTrimester.isPresent()) {
            LocalDate today = currentUserContext.today();
            List<DashboardClassSessionDTO> sessions = classSessions(classSections, activeTrimester.get(), today);
            todayClasses = sessions
                .stream()
                .filter(session -> today.equals(session.date()))
                .limit(TODAY_CLASSES_LIMIT)
                .toList();
            upcomingClasses = sessions
                .stream()
                .filter(session -> session.date().isAfter(today))
                .limit(UPCOMING_CLASSES_LIMIT)
                .toList();
        } else {
            trimesterMessage = CurrentUserContext.NO_ACTIVE_TRIMESTER_MESSAGE;
        }

        return new InstructorDashboardDTO(
            pendingJustifications,
            activeAlerts,
            classSections.size(),
            gradeIds.size(),
            countEnrolledApprentices(gradeIds),
            todayClasses,
            upcomingClasses,
            trimesterMessage
        );
    }

    /**
     * Counts the apprentices matriculados in the given fichas, once per apprentice even when they
     * are enrolled in several of them.
     *
     * @param gradeIds the fichas where the instructor teaches.
     * @return the number of distinct enrolled apprentices.
     */
    private long countEnrolledApprentices(Set<String> gradeIds) {
        Set<String> studentIds = new HashSet<>();
        for (String gradeId : gradeIds) {
            apprenticeRepository
                .findByGradeId(gradeId)
                .stream()
                .filter(apprentice -> apprentice.getStateAcademic() == StateAcademic.MATRICULADO)
                .map(Apprentice::getStudent)
                .filter(Objects::nonNull)
                .map(UserProfile::getId)
                .filter(Objects::nonNull)
                .forEach(studentIds::add);
        }
        return studentIds.size();
    }

    // ------- APPRENTICE DASHBOARD -------
    /**
     * Builds the apprentice panel (UC023): the attendance of the active trimester, the failures
     * left to reach each ficha threshold, the justifications by state with the correction
     * deadlines, the enrollment and the sessions ahead. When no trimester is active the trimester
     * dependent indicators travel empty with the E2 message; the enrollment, the justifications
     * and the active alerts are trimester independent and are still computed.
     *
     * @return the apprentice dashboard.
     */
    private ApprenticeDashboardDTO buildApprenticeDashboard() {
        Optional<UserProfile> profile = currentUserContext.profile();
        if (profile.isEmpty() || profile.get().getId() == null) {
            return new ApprenticeDashboardDTO(null, List.of(), emptyJustifications(), List.of(), List.of(), 0, null);
        }
        String studentId = profile.get().getId();
        List<Apprentice> enrollments = apprenticeRepository
            .findByStudentId(studentId)
            .stream()
            .filter(apprentice -> apprentice.getStateAcademic() == StateAcademic.MATRICULADO)
            .toList();
        List<ClassSection> classSections = classSectionsOf(enrollments);

        long activeAlerts = alertaRepository.countActiveAlerts(null, studentId);
        JustificationSummaryDTO justifications = buildJustificationSummary(studentId);
        List<EnrolledGradeDTO> grades = buildEnrolledGrades(enrollments);

        Optional<Trimester> activeTrimester = currentUserContext.activeTrimester();
        if (activeTrimester.isEmpty()) {
            return new ApprenticeDashboardDTO(
                null,
                List.of(),
                justifications,
                grades,
                List.of(),
                activeAlerts,
                CurrentUserContext.NO_ACTIVE_TRIMESTER_MESSAGE
            );
        }

        Trimester trimester = activeTrimester.get();
        LocalDate today = currentUserContext.today();
        AttendanceSummaryDTO attendance = buildAttendanceSummary(studentId, classSections, trimester);
        List<GradeFailureDTO> failuresByGrade = buildFailuresByGrade(enrollments, studentId, trimester);
        List<DashboardClassSessionDTO> upcomingClasses = classSessions(classSections, trimester, today)
            .stream()
            .limit(UPCOMING_CLASSES_LIMIT)
            .toList();

        return new ApprenticeDashboardDTO(attendance, failuresByGrade, justifications, grades, upcomingClasses, activeAlerts, null);
    }

    /**
     * Resolves the materias of the enrolled fichas, once per materia.
     *
     * @param enrollments the enrollments of the apprentice.
     * @return the distinct materias of those fichas.
     */
    private List<ClassSection> classSectionsOf(List<Apprentice> enrollments) {
        Map<String, ClassSection> byId = new LinkedHashMap<>();
        for (Apprentice enrollment : enrollments) {
            Grade grade = enrollment.getGrade();
            if (grade == null || grade.getId() == null) {
                continue;
            }
            classSectionRepository.findByGradeId(grade.getId()).forEach(classSection -> {
                if (classSection.getId() != null) {
                    byId.putIfAbsent(classSection.getId(), classSection);
                }
            });
        }
        return new ArrayList<>(byId.values());
    }

    /**
     * Builds the "my fichas and materias" list of the apprentice.
     *
     * @param enrollments the enrollments of the apprentice.
     * @return the enrolled fichas with their materias.
     */
    private List<EnrolledGradeDTO> buildEnrolledGrades(List<Apprentice> enrollments) {
        List<EnrolledGradeDTO> grades = new ArrayList<>();
        for (Apprentice enrollment : enrollments) {
            Grade grade = enrollment.getGrade();
            if (grade == null || grade.getId() == null) {
                continue;
            }
            List<EnrolledSubjectDTO> subjects = classSectionRepository
                .findByGradeId(grade.getId())
                .stream()
                .map(classSection -> new EnrolledSubjectDTO(classSection.getId(), classSection.getSubjectName()))
                .toList();
            grades.add(
                new EnrolledGradeDTO(
                    grade.getId(),
                    grade.getCode(),
                    grade.getProgram() != null ? grade.getProgram().getName() : null,
                    subjects
                )
            );
        }
        return grades;
    }

    /**
     * Groups the parts of the apprentice justifications by state and lists the rejected ones that
     * can still be corrected, with the business days left.
     *
     * @param studentId the apprentice profile id.
     * @return the justification summary.
     */
    private JustificationSummaryDTO buildJustificationSummary(String studentId) {
        List<ObjectId> justificationIds = justificationRepository
            .findAllByStudentId(studentId)
            .stream()
            .map(Justification::getId)
            .filter(DashboardServiceImpl::isObjectId)
            .map(ObjectId::new)
            .toList();
        if (justificationIds.isEmpty()) {
            return emptyJustifications();
        }
        List<JustificationDetails> parts = justificationDetailsRepository.findAllByJustificationIdIn(justificationIds);
        long pending = parts
            .stream()
            .filter(part -> part.getStateJustification() == StateJustification.PENDIENTE)
            .count();
        long approved = parts
            .stream()
            .filter(part -> part.getStateJustification() == StateJustification.ACEPTADA)
            .count();
        long rejected = parts
            .stream()
            .filter(part -> part.getStateJustification() == StateJustification.RECHAZADA)
            .count();

        LocalDate today = currentUserContext.today();
        List<JustificationDeadlineDTO> withinCorrectionWindow = parts
            .stream()
            .filter(part -> part.getStateJustification() == StateJustification.RECHAZADA)
            .map(part -> correctionDeadline(part, today))
            .flatMap(Optional::stream)
            .sorted(Comparator.comparingLong(JustificationDeadlineDTO::remainingBusinessDays))
            .toList();
        return new JustificationSummaryDTO(pending, approved, rejected, withinCorrectionWindow);
    }

    /**
     * Derives the correction deadline of a rejected part: two business days counted from the day
     * after the rejection (UC011, A5/E5). A part whose window already closed has no deadline.
     *
     * @param part the rejected part.
     * @param today the current day.
     * @return the deadline with the business days left, or empty when the window closed.
     */
    private Optional<JustificationDeadlineDTO> correctionDeadline(JustificationDetails part, LocalDate today) {
        if (part.getResponseDate() == null) {
            return Optional.empty();
        }
        LocalDate rejectionDay = LocalDate.ofInstant(part.getResponseDate(), clock.getZone());
        LocalDate deadline = BusinessDays.plus(rejectionDay, BusinessDays.CORRECTION_BUSINESS_DAYS);
        if (today.isAfter(deadline)) {
            return Optional.empty();
        }
        ClassSection classSection = part.getClassSection();
        return Optional.of(
            new JustificationDeadlineDTO(
                part.getId(),
                classSection != null ? classSection.getSubjectName() : null,
                deadline,
                BusinessDays.businessDaysAfter(today, deadline)
            )
        );
    }

    /**
     * Counts the attendance records of the apprentice in the materias of the active trimester,
     * per state, and derives the attendance percentage over the recorded sessions.
     *
     * @param studentId the apprentice profile id.
     * @param classSections the materias of the apprentice.
     * @param trimester the active trimester.
     * @return the attendance summary.
     */
    private AttendanceSummaryDTO buildAttendanceSummary(String studentId, List<ClassSection> classSections, Trimester trimester) {
        List<ObjectId> classSectionIds = toObjectIds(classSections);
        long present = countAttendance(studentId, classSectionIds, trimester, StateAttendance.PRESENTE);
        long failure = countAttendance(studentId, classSectionIds, trimester, StateAttendance.FALLA);
        long justified = countAttendance(studentId, classSectionIds, trimester, StateAttendance.JUSTIFICADA);
        long total = present + failure + justified;
        double percentage = total == 0 ? 0.0 : Math.round((present * 10000.0) / total) / 100.0;
        return new AttendanceSummaryDTO(present, failure, justified, percentage);
    }

    /**
     * Counts the unexcused failures of the apprentice in each enrolled ficha during the active
     * trimester and the distance left to the accumulated alert threshold.
     *
     * @param enrollments the enrollments of the apprentice.
     * @param studentId the apprentice profile id.
     * @param trimester the active trimester.
     * @return one entry per enrolled ficha.
     */
    private List<GradeFailureDTO> buildFailuresByGrade(List<Apprentice> enrollments, String studentId, Trimester trimester) {
        int threshold = accumulatedThreshold();
        List<GradeFailureDTO> failuresByGrade = new ArrayList<>();
        for (Apprentice enrollment : enrollments) {
            Grade grade = enrollment.getGrade();
            if (grade == null || grade.getId() == null) {
                continue;
            }
            List<ObjectId> classSectionIds = toObjectIds(classSectionRepository.findByGradeId(grade.getId()));
            long failures = countAttendance(studentId, classSectionIds, trimester, StateAttendance.FALLA);
            failuresByGrade.add(
                new GradeFailureDTO(grade.getId(), grade.getCode(), failures, threshold, Math.max(0, threshold - failures))
            );
        }
        return failuresByGrade;
    }

    /**
     * Counts the attendance records of the apprentice in the given materias and state inside the
     * trimester.
     *
     * @param studentId the apprentice profile id.
     * @param classSectionIds the ObjectId values of the materias.
     * @param trimester the active trimester.
     * @param state the state to count.
     * @return the number of matching records.
     */
    private long countAttendance(String studentId, List<ObjectId> classSectionIds, Trimester trimester, StateAttendance state) {
        if (classSectionIds.isEmpty() || trimester.getStartDate() == null || trimester.getEndDate() == null) {
            return 0;
        }
        List<Attendance> records = attendanceRepository.findByStudentIdAndClassSectionIdInAndDateBetweenAndStateAttendance(
            studentId,
            classSectionIds,
            trimester.getStartDate(),
            trimester.getEndDate(),
            state
        );
        return records.size();
    }

    /**
     * @return the configured accumulated absence threshold, or its default when the singleton
     *         configuration is missing (E4).
     */
    private int accumulatedThreshold() {
        return globalConfigurationRepository
            .findById(GlobalConfiguration.GLOBAL_CONFIGURATION_ID)
            .map(GlobalConfiguration::getAccumulatedAbsenceAlertThreshold)
            .orElse(GlobalConfigurationServiceImpl.DEFAULT_ACCUMULATED_ABSENCE_ALERT_THRESHOLD);
    }

    private static JustificationSummaryDTO emptyJustifications() {
        return new JustificationSummaryDTO(0, 0, 0, List.of());
    }

    // ------- SHARED SESSION EXPANSION -------
    /**
     * Expands the weekly schedules of the materias into the concrete sessions from the given day
     * to the end of the trimester, discounted from the non-teaching exceptions. The window is
     * capped by {@link #LOOKAHEAD_DAYS} so a long trimester does not walk an unbounded range.
     *
     * @param classSections the materias whose schedules are expanded.
     * @param trimester the active trimester.
     * @param from the first day to expand, inclusive.
     * @return the sessions ordered by date and start time, possibly empty.
     */
    private List<DashboardClassSessionDTO> classSessions(List<ClassSection> classSections, Trimester trimester, LocalDate from) {
        if (classSections.isEmpty() || trimester == null || trimester.getEndDate() == null) {
            return List.of();
        }
        List<ObjectId> classSectionIds = toObjectIds(classSections);
        if (classSectionIds.isEmpty()) {
            return List.of();
        }

        LocalDate lookaheadEnd = from.plusDays(LOOKAHEAD_DAYS);
        LocalDate to = trimester.getEndDate().isBefore(lookaheadEnd) ? trimester.getEndDate() : lookaheadEnd;
        if (from.isAfter(to)) {
            return List.of();
        }

        Set<String> exceptionDates = exceptionDates(classSectionIds, from, to);
        List<ClassSchedule> schedules = classScheduleRepository.findByClassSectionIdInAndTrimesterId(classSectionIds, trimester.getId());
        List<DashboardClassSessionDTO> sessions = new ArrayList<>();
        for (ClassSchedule schedule : schedules) {
            ClassSection classSection = resolveScheduleClassSection(schedule, classSections);
            if (classSection == null) {
                continue;
            }
            for (LocalDate date : ClassSessions.datesInRange(List.of(schedule), from, to)) {
                if (exceptionDates.contains(exceptionKey(classSection.getId(), date))) {
                    continue;
                }
                sessions.add(
                    new DashboardClassSessionDTO(
                        classSection.getId(),
                        classSection.getSubjectName(),
                        classSection.getGrade() != null ? classSection.getGrade().getCode() : null,
                        date,
                        schedule.getStartTime(),
                        schedule.getEndTime()
                    )
                );
            }
        }
        sessions.sort(
            Comparator.comparing(DashboardClassSessionDTO::date).thenComparing(
                DashboardClassSessionDTO::startTime,
                Comparator.nullsLast(Comparator.naturalOrder())
            )
        );
        return sessions;
    }

    /**
     * Resolves the materia of a schedule from the sections the caller already loaded, because the
     * DBRef of the schedule only guarantees its id. A schedule of a materia outside that list is
     * dropped.
     *
     * @param schedule the schedule to resolve.
     * @param classSections the materias the caller loaded.
     * @return the matching materia, or {@code null} when it cannot be resolved.
     */
    private static ClassSection resolveScheduleClassSection(ClassSchedule schedule, List<ClassSection> classSections) {
        if (schedule.getClassSection() == null || schedule.getClassSection().getId() == null) {
            return null;
        }
        String classSectionId = schedule.getClassSection().getId();
        return classSections
            .stream()
            .filter(classSection -> classSectionId.equals(classSection.getId()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Reads the non-teaching exceptions of the given materias inside the window, as keys that
     * remove the session of that materia on that date.
     *
     * @param classSectionIds the materias to read.
     * @param from the first day of the window, inclusive.
     * @param to the last day of the window, inclusive.
     * @return the keys of the non-teaching dates.
     */
    private Set<String> exceptionDates(List<ObjectId> classSectionIds, LocalDate from, LocalDate to) {
        Set<String> keys = new HashSet<>();
        for (ObjectId classSectionId : classSectionIds) {
            classExceptionRepository
                .findByClassSectionIdAndDateBetween(classSectionId.toHexString(), from, to)
                .stream()
                .map(ClassException::getDate)
                .filter(Objects::nonNull)
                .map(date -> exceptionKey(classSectionId.toHexString(), date))
                .forEach(keys::add);
        }
        return keys;
    }

    private static String exceptionKey(String classSectionId, LocalDate date) {
        return classSectionId + "|" + date;
    }

    /**
     * @param classSections the materias of the instructor.
     * @return the readable alert scope of those materias and their fichas.
     */
    private static AlertaReadScope scopeOf(List<ClassSection> classSections) {
        Set<String> classSectionIds = classSections.stream().map(ClassSection::getId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<String> gradeIds = classSections
            .stream()
            .map(ClassSection::getGrade)
            .filter(Objects::nonNull)
            .map(Grade::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        return new AlertaReadScope(classSectionIds, gradeIds);
    }

    /**
     * @param classSections the materias to convert.
     * @return the usable {@code ObjectId} values of their ids.
     */
    private static List<ObjectId> toObjectIds(List<ClassSection> classSections) {
        return classSections
            .stream()
            .map(ClassSection::getId)
            .filter(DashboardServiceImpl::isObjectId)
            .map(ObjectId::new)
            .distinct()
            .toList();
    }

    /**
     * @param id the candidate id.
     * @return whether the id is a 24-hex string convertible into an {@code ObjectId}.
     */
    private static boolean isObjectId(String id) {
        return id != null && id.length() == 24 && ObjectId.isValid(id);
    }
}
