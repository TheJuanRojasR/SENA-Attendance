package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.Apprentice;
import com.mycompany.senaattendance.domain.ClassException;
import com.mycompany.senaattendance.domain.ClassSchedule;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.StateAcademic;
import com.mycompany.senaattendance.domain.enumeration.StateGrade;
import com.mycompany.senaattendance.domain.enumeration.StateJustification;
import com.mycompany.senaattendance.repository.AlertaReadScope;
import com.mycompany.senaattendance.repository.AlertaRepository;
import com.mycompany.senaattendance.repository.ApprenticeRepository;
import com.mycompany.senaattendance.repository.ClassExceptionRepository;
import com.mycompany.senaattendance.repository.ClassScheduleRepository;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.repository.ModalityRepository;
import com.mycompany.senaattendance.repository.ProgramRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.DashboardService;
import com.mycompany.senaattendance.service.JustificationDetailsService;
import com.mycompany.senaattendance.service.dto.dashboard.AdminDashboardDTO;
import com.mycompany.senaattendance.service.dto.dashboard.ApprenticeDashboardDTO;
import com.mycompany.senaattendance.service.dto.dashboard.DashboardClassSessionDTO;
import com.mycompany.senaattendance.service.dto.dashboard.DashboardDTO;
import com.mycompany.senaattendance.service.dto.dashboard.DashboardKpisDTO;
import com.mycompany.senaattendance.service.dto.dashboard.InstructorDashboardDTO;
import com.mycompany.senaattendance.service.dto.dashboard.RecentGradeDTO;
import com.mycompany.senaattendance.service.util.ClassSessions;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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
    private final AlertaRepository alertaRepository;
    private final JustificationDetailsService justificationDetailsService;
    private final CurrentUserContext currentUserContext;

    public DashboardServiceImpl(
        UserProfileRepository userProfileRepository,
        GradeRepository gradeRepository,
        ProgramRepository programRepository,
        ModalityRepository modalityRepository,
        ClassSectionRepository classSectionRepository,
        ClassScheduleRepository classScheduleRepository,
        ClassExceptionRepository classExceptionRepository,
        ApprenticeRepository apprenticeRepository,
        AlertaRepository alertaRepository,
        JustificationDetailsService justificationDetailsService,
        CurrentUserContext currentUserContext
    ) {
        this.userProfileRepository = userProfileRepository;
        this.gradeRepository = gradeRepository;
        this.programRepository = programRepository;
        this.modalityRepository = modalityRepository;
        this.classSectionRepository = classSectionRepository;
        this.classScheduleRepository = classScheduleRepository;
        this.classExceptionRepository = classExceptionRepository;
        this.apprenticeRepository = apprenticeRepository;
        this.alertaRepository = alertaRepository;
        this.justificationDetailsService = justificationDetailsService;
        this.currentUserContext = currentUserContext;
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
    private ApprenticeDashboardDTO buildApprenticeDashboard() {
        return new ApprenticeDashboardDTO(null, List.of(), null, List.of(), List.of(), 0, null);
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
        List<ObjectId> classSectionIds = classSections
            .stream()
            .map(ClassSection::getId)
            .filter(DashboardServiceImpl::isObjectId)
            .map(ObjectId::new)
            .distinct()
            .toList();
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
     * @param id the candidate id.
     * @return whether the id is a 24-hex string convertible into an {@code ObjectId}.
     */
    private static boolean isObjectId(String id) {
        return id != null && id.length() == 24 && ObjectId.isValid(id);
    }
}
