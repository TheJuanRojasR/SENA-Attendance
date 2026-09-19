package com.mycompany.senaattendance.web.rest;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.Alerta;
import com.mycompany.senaattendance.domain.Apprentice;
import com.mycompany.senaattendance.domain.Attendance;
import com.mycompany.senaattendance.domain.ClassException;
import com.mycompany.senaattendance.domain.ClassSchedule;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.GlobalConfiguration;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.Justification;
import com.mycompany.senaattendance.domain.JustificationDetails;
import com.mycompany.senaattendance.domain.Modality;
import com.mycompany.senaattendance.domain.Program;
import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.AlertaState;
import com.mycompany.senaattendance.domain.enumeration.AlertaType;
import com.mycompany.senaattendance.domain.enumeration.DayOfWeek;
import com.mycompany.senaattendance.domain.enumeration.StateAcademic;
import com.mycompany.senaattendance.domain.enumeration.StateAttendance;
import com.mycompany.senaattendance.domain.enumeration.StateGrade;
import com.mycompany.senaattendance.domain.enumeration.StateJustification;
import com.mycompany.senaattendance.domain.enumeration.StateTrimester;
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
import com.mycompany.senaattendance.repository.TrimesterRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.impl.GlobalConfigurationServiceImpl;
import com.mycompany.senaattendance.service.util.BusinessDays;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the {@link com.mycompany.senaattendance.web.rest.DashboardResource} REST controller.
 *
 * <p>The dashboard KPIs are computed with {@code count()} over the WHOLE shared MongoDB collections
 * (which include mongock-seeded data and leftovers from other IT classes). To stay deterministic we
 * capture a baseline <em>before</em> seeding and assert a relative increment, never an absolute count.
 * The role panels (UC023) are scoped to the seeded graph, so they assert absolute values over
 * fixtures that the {@code @AfterEach} removes.
 */
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
@IntegrationTest
class DashboardResourceIT {

    private static final String API_URL = "/api/dashboard";

    /**
     * Far-future created date so the grade we seed always lands at index {@code [0]} of
     * {@code findTop5ByOrderByCreatedDateDesc}, regardless of what other test data remains in the DB.
     */
    private static final Instant FUTURE_CREATED_DATE = Instant.parse("2099-12-31T23:59:59.999Z");

    private static final String INSTRUCTOR_LOGIN = "dashboard_instructor";

    private static final String INSTRUCTOR_WITHOUT_PROFILE_LOGIN = "dashboard_instructor_without_profile";

    private static final String APPRENTICE_LOGIN = "dashboard_apprentice";

    private static final String APPRENTICE_WITHOUT_TRIMESTER_LOGIN = "dashboard_apprentice_without_trimester";

    @Autowired
    private MockMvc restDashboardMockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private ModalityRepository modalityRepository;

    @Autowired
    private ClassSectionRepository classSectionRepository;

    @Autowired
    private ClassScheduleRepository classScheduleRepository;

    @Autowired
    private ClassExceptionRepository classExceptionRepository;

    @Autowired
    private ApprenticeRepository apprenticeRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private GlobalConfigurationRepository globalConfigurationRepository;

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private JustificationRepository justificationRepository;

    @Autowired
    private JustificationDetailsRepository justificationDetailsRepository;

    @Autowired
    private TrimesterRepository trimesterRepository;

    @Autowired
    private Clock clock;

    // Track the entities seeded in the admin test so the shared DB can be cleaned up precisely.
    private Program seededProgram;
    private Modality seededModality;
    private UserProfile seededProfile;
    private Grade seededGrade;
    private ClassSection seededClassSection;

    // Track the graph seeded by the instructor panel test.
    private User instructorUser;
    private UserProfile instructorProfile;
    private Grade instructorGradeA;
    private Grade instructorGradeB;
    private ClassSection instructorSectionA;
    private ClassSection instructorSectionB;
    private Trimester instructorTrimester;
    private ClassSchedule instructorScheduleTodayA;
    private ClassSchedule instructorScheduleTodayB;
    private ClassSchedule instructorScheduleUpcomingA;
    private ClassException instructorExceptionTodayA;
    private Apprentice instructorApprenticeInBothGrades;
    private Apprentice instructorApprenticeInGradeB;
    private Apprentice instructorApprenticeInGradeA;
    private Apprentice instructorRetiredApprentice;
    private UserProfile instructorStudentOne;
    private UserProfile instructorStudentTwo;
    private Alerta instructorActiveAlert;
    private Alerta instructorResolvedAlert;
    private Justification instructorJustification;
    private JustificationDetails instructorPendingPart;

    // Track the graph seeded by the apprentice panel tests.
    private User apprenticeUser;
    private UserProfile apprenticeProfile;
    private Program apprenticeProgram;
    private Grade apprenticeGrade;
    private ClassSection apprenticeSectionA;
    private ClassSection apprenticeSectionB;
    private Trimester apprenticeTrimester;
    private ClassSchedule apprenticeSchedule;
    private Apprentice apprenticeEnrollment;
    private final List<Attendance> apprenticeAttendances = new ArrayList<>();
    private Alerta apprenticeActiveAlert;
    private Alerta apprenticeResolvedAlert;
    private Justification apprenticeJustification;
    private final List<JustificationDetails> apprenticeParts = new ArrayList<>();

    @AfterEach
    void cleanup() {
        apprenticeParts.forEach(justificationDetailsRepository::delete);
        apprenticeParts.clear();
        if (apprenticeJustification != null) {
            justificationRepository.delete(apprenticeJustification);
            apprenticeJustification = null;
        }
        if (apprenticeActiveAlert != null) {
            alertaRepository.delete(apprenticeActiveAlert);
            apprenticeActiveAlert = null;
        }
        if (apprenticeResolvedAlert != null) {
            alertaRepository.delete(apprenticeResolvedAlert);
            apprenticeResolvedAlert = null;
        }
        if (apprenticeEnrollment != null) {
            apprenticeRepository.delete(apprenticeEnrollment);
            apprenticeEnrollment = null;
        }
        apprenticeAttendances.forEach(attendanceRepository::delete);
        apprenticeAttendances.clear();
        if (apprenticeSchedule != null) {
            classScheduleRepository.delete(apprenticeSchedule);
            apprenticeSchedule = null;
        }
        if (apprenticeSectionA != null) {
            classSectionRepository.delete(apprenticeSectionA);
            apprenticeSectionA = null;
        }
        if (apprenticeSectionB != null) {
            classSectionRepository.delete(apprenticeSectionB);
            apprenticeSectionB = null;
        }
        if (apprenticeGrade != null) {
            gradeRepository.delete(apprenticeGrade);
            apprenticeGrade = null;
        }
        if (apprenticeTrimester != null) {
            trimesterRepository.delete(apprenticeTrimester);
            apprenticeTrimester = null;
        }
        if (apprenticeProgram != null) {
            programRepository.delete(apprenticeProgram);
            apprenticeProgram = null;
        }
        if (apprenticeProfile != null) {
            userProfileRepository.delete(apprenticeProfile);
            apprenticeProfile = null;
        }
        if (apprenticeUser != null) {
            userRepository.delete(apprenticeUser);
            apprenticeUser = null;
        }
        if (instructorPendingPart != null) {
            justificationDetailsRepository.delete(instructorPendingPart);
            instructorPendingPart = null;
        }
        if (instructorJustification != null) {
            justificationRepository.delete(instructorJustification);
            instructorJustification = null;
        }
        if (instructorActiveAlert != null) {
            alertaRepository.delete(instructorActiveAlert);
            instructorActiveAlert = null;
        }
        if (instructorResolvedAlert != null) {
            alertaRepository.delete(instructorResolvedAlert);
            instructorResolvedAlert = null;
        }
        if (instructorApprenticeInBothGrades != null) {
            apprenticeRepository.delete(instructorApprenticeInBothGrades);
            instructorApprenticeInBothGrades = null;
        }
        if (instructorApprenticeInGradeB != null) {
            apprenticeRepository.delete(instructorApprenticeInGradeB);
            instructorApprenticeInGradeB = null;
        }
        if (instructorApprenticeInGradeA != null) {
            apprenticeRepository.delete(instructorApprenticeInGradeA);
            instructorApprenticeInGradeA = null;
        }
        if (instructorRetiredApprentice != null) {
            apprenticeRepository.delete(instructorRetiredApprentice);
            instructorRetiredApprentice = null;
        }
        if (instructorStudentOne != null) {
            userProfileRepository.delete(instructorStudentOne);
            instructorStudentOne = null;
        }
        if (instructorStudentTwo != null) {
            userProfileRepository.delete(instructorStudentTwo);
            instructorStudentTwo = null;
        }
        if (instructorScheduleTodayA != null) {
            classScheduleRepository.delete(instructorScheduleTodayA);
            instructorScheduleTodayA = null;
        }
        if (instructorScheduleTodayB != null) {
            classScheduleRepository.delete(instructorScheduleTodayB);
            instructorScheduleTodayB = null;
        }
        if (instructorScheduleUpcomingA != null) {
            classScheduleRepository.delete(instructorScheduleUpcomingA);
            instructorScheduleUpcomingA = null;
        }
        if (instructorExceptionTodayA != null) {
            classExceptionRepository.delete(instructorExceptionTodayA);
            instructorExceptionTodayA = null;
        }
        if (instructorSectionA != null) {
            classSectionRepository.delete(instructorSectionA);
            instructorSectionA = null;
        }
        if (instructorSectionB != null) {
            classSectionRepository.delete(instructorSectionB);
            instructorSectionB = null;
        }
        if (instructorGradeA != null) {
            gradeRepository.delete(instructorGradeA);
            instructorGradeA = null;
        }
        if (instructorGradeB != null) {
            gradeRepository.delete(instructorGradeB);
            instructorGradeB = null;
        }
        if (instructorTrimester != null) {
            trimesterRepository.delete(instructorTrimester);
            instructorTrimester = null;
        }
        if (instructorProfile != null) {
            userProfileRepository.delete(instructorProfile);
            instructorProfile = null;
        }
        if (instructorUser != null) {
            userRepository.delete(instructorUser);
            instructorUser = null;
        }
        if (seededClassSection != null) {
            classSectionRepository.delete(seededClassSection);
            seededClassSection = null;
        }
        if (seededGrade != null) {
            gradeRepository.delete(seededGrade);
            seededGrade = null;
        }
        if (seededProgram != null) {
            programRepository.delete(seededProgram);
            seededProgram = null;
        }
        if (seededModality != null) {
            modalityRepository.delete(seededModality);
            seededModality = null;
        }
        if (seededProfile != null) {
            userProfileRepository.delete(seededProfile);
            seededProfile = null;
        }
    }

    private String uniqueSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    @Test
    void testGetDashboardAsAdmin() throws Exception {
        // Capture KPI baselines BEFORE seeding (counts include mongock + leftover data from other ITs).
        long userCntBefore = userProfileRepository.count();
        long programCntBefore = programRepository.count();
        long modalityCntBefore = modalityRepository.count();
        long activeGradeCntBefore = gradeRepository.countByState(StateGrade.ACTIVA);

        String suffix = uniqueSuffix();

        // 1. Program (must be saved before any Grade that references it via @DBRef).
        seededProgram = programRepository.save(
            new Program()
                .name("Programa " + suffix)
                .initials("PG" + suffix.substring(0, 4))
                .code("PRG-" + suffix)
                .trimesters(3)
                .status(true)
        );

        // 2. Modality.
        seededModality = modalityRepository.save(new Modality().name("Modalidad " + suffix).isActive(true));

        // 3. A single UserProfile reused as both the counted user AND the course instructor,
        //    so totalUsers only increments by exactly 1. documentNumber must be unique and <= 15 chars.
        String documentNumber = "PROF" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        seededProfile = userProfileRepository.save(
            new UserProfile().firstName("Juan").firstLastName("Perez").documentNumber(documentNumber).phoneNumber("3000000000")
        );

        // 4. Grade linked to the Program, ACTIVA. Far-future created date keeps it at index [0] of the "recent" list.
        seededGrade = gradeRepository.save(
            new Grade()
                .code("GR-" + suffix)
                .state(StateGrade.ACTIVA)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .program(seededProgram)
        );
        seededGrade.setCreatedDate(FUTURE_CREATED_DATE);
        seededGrade = gradeRepository.save(seededGrade);

        // 5. ClassSection linking the grade to the instructor so resolveInstructorName() returns a name.
        seededClassSection = classSectionRepository.save(
            new ClassSection()
                .subjectName("Matemáticas " + suffix)
                .isActive(true)
                .instructor(seededProfile)
                .grade(seededGrade)
        );

        String expectedInstructorName = seededProfile.getFirstName() + " " + seededProfile.getFirstLastName();

        restDashboardMockMvc
            .perform(get(API_URL))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            // KPIs: relative increments over the baseline captured before seeding.
            .andExpect(jsonPath("$.kpis.totalUsers").value(userCntBefore + 1))
            .andExpect(jsonPath("$.kpis.totalPrograms").value(programCntBefore + 1))
            .andExpect(jsonPath("$.kpis.totalModalities").value(modalityCntBefore + 1))
            .andExpect(jsonPath("$.kpis.activeGrades").value(activeGradeCntBefore + 1))
            // recentGrades: the seeded grade is guaranteed to be at index [0] via the far-future created date.
            .andExpect(jsonPath("$.recentGrades").isArray())
            .andExpect(jsonPath("$.recentGrades[*].code").value(hasItem(seededGrade.getCode())))
            .andExpect(jsonPath("$.recentGrades[0].id").value(seededGrade.getId()))
            .andExpect(jsonPath("$.recentGrades[0].code").value(seededGrade.getCode()))
            .andExpect(jsonPath("$.recentGrades[0].programName").value(seededProgram.getName()))
            .andExpect(jsonPath("$.recentGrades[0].instructorName").value(expectedInstructorName))
            .andExpect(jsonPath("$.recentGrades[0].state").value(StateGrade.ACTIVA.toString()));
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.USER)
    void testGetDashboardForbiddenForUserRole() throws Exception {
        // ROLE_USER is NOT among the allowed authorities (ADMIN/INSTRUCTOR/APPRENTICE) -> 403 Forbidden.
        restDashboardMockMvc.perform(get(API_URL)).andExpect(status().isForbidden());
    }

    @Test
    @WithUnauthenticatedMockUser
    void testGetDashboardUnauthenticated() throws Exception {
        // No authentication context -> BearerTokenAuthenticationEntryPoint -> 401 Unauthorized.
        restDashboardMockMvc.perform(get(API_URL)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void testGetDashboardAsInstructorReturnsTheirOwnPanel() throws Exception {
        LocalDate today = LocalDate.now(clock);
        LocalDate tomorrow = today.plusDays(1);
        String suffix = uniqueSuffix();

        instructorUser = persistUser(INSTRUCTOR_LOGIN);
        instructorProfile = persistProfile("DI" + suffix, instructorUser);
        instructorStudentOne = persistProfile("DS1" + suffix, null);
        instructorStudentTwo = persistProfile("DS2" + suffix, null);
        UserProfile studentOne = instructorStudentOne;
        UserProfile studentTwo = instructorStudentTwo;

        instructorGradeA = persistGrade("DGA-" + suffix, today);
        instructorGradeB = persistGrade("DGB-" + suffix, today);
        instructorSectionA = persistClassSection("Materia A " + suffix, instructorGradeA, instructorProfile);
        instructorSectionB = persistClassSection("Materia B " + suffix, instructorGradeB, instructorProfile);
        instructorTrimester = trimesterRepository.save(
            new Trimester()
                .name("Trimestre dashboard " + suffix)
                .startDate(today.minusDays(30))
                .endDate(today.plusDays(30))
                .status(StateTrimester.ACTIVO)
        );

        // Materia A has a session today, but the date is non-teaching, so only its session tomorrow is shown.
        instructorScheduleTodayA = persistSchedule(instructorSectionA, instructorTrimester, today, "07:00", "09:00");
        instructorScheduleUpcomingA = persistSchedule(instructorSectionA, instructorTrimester, tomorrow, "07:00", "09:00");
        instructorExceptionTodayA = classExceptionRepository.save(
            new ClassException().date(today).reason("Jornada institucional").classSection(instructorSectionA)
        );
        // Materia B has a plain session today.
        instructorScheduleTodayB = persistSchedule(instructorSectionB, instructorTrimester, today, "10:00", "12:00");

        // Two distinct matriculados across the two fichas; the retired one must not count.
        instructorApprenticeInBothGrades = apprenticeRepository.save(
            new Apprentice().stateAcademic(StateAcademic.MATRICULADO).student(studentOne).grade(instructorGradeA)
        );
        instructorApprenticeInGradeB = apprenticeRepository.save(
            new Apprentice().stateAcademic(StateAcademic.MATRICULADO).student(studentOne).grade(instructorGradeB)
        );
        instructorApprenticeInGradeA = apprenticeRepository.save(
            new Apprentice().stateAcademic(StateAcademic.MATRICULADO).student(studentTwo).grade(instructorGradeA)
        );
        instructorRetiredApprentice = apprenticeRepository.save(
            new Apprentice().stateAcademic(StateAcademic.RETIRO_VOLUNTARIO).student(studentTwo).grade(instructorGradeB)
        );

        // One active alert of the own scope and one resolved one: only the active one counts.
        instructorActiveAlert = alertaRepository.save(
            new Alerta()
                .student(studentOne)
                .classSection(instructorSectionA)
                .grade(instructorGradeA)
                .trimester(instructorTrimester)
                .type(AlertaType.CONSECUTIVAS)
                .state(AlertaState.NO_LEIDA)
                .absenceCount(3)
                .threshold(3)
                .generatedAt(Instant.now(clock))
        );
        instructorResolvedAlert = alertaRepository.save(
            new Alerta()
                .student(studentTwo)
                .classSection(instructorSectionA)
                .grade(instructorGradeA)
                .trimester(instructorTrimester)
                .type(AlertaType.CONSECUTIVAS)
                .state(AlertaState.RESUELTA_AUTOMATICAMENTE)
                .absenceCount(3)
                .threshold(3)
                .generatedAt(Instant.now(clock))
        );

        // One pending part in the instructor materia, which is the work left to decide.
        instructorJustification = justificationRepository.save(
            new Justification()
                .description("Incapacidad")
                .startDate(today.minusDays(2))
                .endDate(today.minusDays(1))
                .evidenceContentType("application/pdf")
                .student(studentOne)
        );
        instructorPendingPart = justificationDetailsRepository.save(
            new JustificationDetails()
                .stateJustification(StateJustification.PENDIENTE)
                .rejectionReason("")
                .correctionText("")
                .correctionFileUrlContentType("application/pdf")
                .classSection(instructorSectionA)
                .justification(instructorJustification)
        );

        restDashboardMockMvc
            .perform(get(API_URL))
            .andExpect(status().isOk())
            // The instructor panel never carries the admin payload.
            .andExpect(jsonPath("$.kpis").doesNotExist())
            .andExpect(jsonPath("$.recentGrades").doesNotExist())
            .andExpect(jsonPath("$.pendingJustifications").value(1))
            .andExpect(jsonPath("$.activeAlerts").value(1))
            .andExpect(jsonPath("$.assignedSubjects").value(2))
            .andExpect(jsonPath("$.assignedGrades").value(2))
            .andExpect(jsonPath("$.assignedApprentices").value(2))
            // Only materia B has a session today: the materia A session is a non-teaching exception.
            .andExpect(jsonPath("$.todayClasses", hasSize(1)))
            .andExpect(jsonPath("$.todayClasses[0].classSectionId").value(instructorSectionB.getId()))
            .andExpect(jsonPath("$.todayClasses[0].date").value(today.toString()))
            .andExpect(jsonPath("$.todayClasses[0].subjectName").value(instructorSectionB.getSubjectName()))
            // The next session is the materia A one tomorrow.
            .andExpect(jsonPath("$.upcomingClasses[0].classSectionId").value(instructorSectionA.getId()))
            .andExpect(jsonPath("$.upcomingClasses[0].date").value(tomorrow.toString()))
            .andExpect(jsonPath("$.trimesterMessage").isEmpty());
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_WITHOUT_PROFILE_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void testGetDashboardAsInstructorWithoutProfileReturnsAnEmptyPanel() throws Exception {
        restDashboardMockMvc
            .perform(get(API_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pendingJustifications").value(0))
            .andExpect(jsonPath("$.activeAlerts").value(0))
            .andExpect(jsonPath("$.assignedSubjects").value(0))
            .andExpect(jsonPath("$.assignedGrades").value(0))
            .andExpect(jsonPath("$.assignedApprentices").value(0))
            .andExpect(jsonPath("$.todayClasses", hasSize(0)))
            .andExpect(jsonPath("$.upcomingClasses", hasSize(0)))
            .andExpect(jsonPath("$.kpis").doesNotExist());
    }

    @Test
    @WithMockUser(username = APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void testGetDashboardAsApprenticeReturnsTheirOwnPanel() throws Exception {
        LocalDate today = LocalDate.now(clock);
        LocalDate tomorrow = today.plusDays(1);
        String suffix = uniqueSuffix();

        apprenticeUser = persistUser(APPRENTICE_LOGIN);
        apprenticeProfile = persistProfile("DA" + suffix, apprenticeUser);
        apprenticeProgram = programRepository.save(
            new Program()
                .name("Programa aprendiz " + suffix)
                .initials("AP" + suffix.substring(0, 4))
                .code("APR-" + suffix)
                .trimesters(3)
                .status(true)
        );
        apprenticeGrade = gradeRepository.save(
            new Grade()
                .code("DGA-" + suffix)
                .state(StateGrade.ACTIVA)
                .startDate(today.minusDays(30))
                .endDate(today.plusDays(30))
                .program(apprenticeProgram)
        );
        apprenticeSectionA = persistClassSection("Materia aprendiz A " + suffix, apprenticeGrade, null);
        apprenticeSectionB = persistClassSection("Materia aprendiz B " + suffix, apprenticeGrade, null);
        apprenticeTrimester = trimesterRepository.save(
            new Trimester()
                .name("Trimestre aprendiz " + suffix)
                .startDate(today.minusDays(30))
                .endDate(today.plusDays(30))
                .status(StateTrimester.ACTIVO)
        );
        apprenticeSchedule = persistSchedule(apprenticeSectionA, apprenticeTrimester, tomorrow, "07:00", "09:00");
        apprenticeEnrollment = apprenticeRepository.save(
            new Apprentice().stateAcademic(StateAcademic.MATRICULADO).student(apprenticeProfile).grade(apprenticeGrade)
        );

        // One session per state: the percentage is 1 of 3.
        apprenticeAttendances.add(persistAttendance(apprenticeSectionA, today.minusDays(3), StateAttendance.PRESENTE));
        apprenticeAttendances.add(persistAttendance(apprenticeSectionB, today.minusDays(2), StateAttendance.FALLA));
        apprenticeAttendances.add(persistAttendance(apprenticeSectionB, today.minusDays(1), StateAttendance.JUSTIFICADA));

        // One active alert of the apprentice and one already resolved: only the active one counts.
        apprenticeActiveAlert = alertaRepository.save(
            new Alerta()
                .student(apprenticeProfile)
                .classSection(apprenticeSectionA)
                .grade(apprenticeGrade)
                .trimester(apprenticeTrimester)
                .type(AlertaType.CONSECUTIVAS)
                .state(AlertaState.NO_LEIDA)
                .absenceCount(3)
                .threshold(3)
                .generatedAt(Instant.now(clock))
        );
        apprenticeResolvedAlert = alertaRepository.save(
            new Alerta()
                .student(apprenticeProfile)
                .classSection(apprenticeSectionA)
                .grade(apprenticeGrade)
                .trimester(apprenticeTrimester)
                .type(AlertaType.CONSECUTIVAS)
                .state(AlertaState.RESUELTA_AUTOMATICAMENTE)
                .absenceCount(3)
                .threshold(3)
                .generatedAt(Instant.now(clock))
        );

        // One header with one part per state; only the rejection of today is still correctable.
        apprenticeJustification = justificationRepository.save(
            new Justification()
                .description("Incapacidad")
                .startDate(today.minusDays(2))
                .endDate(today.minusDays(1))
                .evidenceContentType("application/pdf")
                .student(apprenticeProfile)
        );
        apprenticeParts.add(persistPart(apprenticeSectionA, StateJustification.PENDIENTE, null));
        apprenticeParts.add(persistPart(apprenticeSectionA, StateJustification.ACEPTADA, Instant.now(clock)));
        apprenticeParts.add(persistPart(apprenticeSectionA, StateJustification.RECHAZADA, Instant.now(clock)));
        apprenticeParts.add(persistPart(apprenticeSectionA, StateJustification.RECHAZADA, Instant.now(clock).minus(10, ChronoUnit.DAYS)));

        long threshold = expectedAccumulatedThreshold();

        restDashboardMockMvc
            .perform(get(API_URL))
            .andExpect(status().isOk())
            // The apprentice panel never carries the admin payload.
            .andExpect(jsonPath("$.kpis").doesNotExist())
            .andExpect(jsonPath("$.recentGrades").doesNotExist())
            .andExpect(jsonPath("$.attendance.present").value(1))
            .andExpect(jsonPath("$.attendance.failure").value(1))
            .andExpect(jsonPath("$.attendance.justified").value(1))
            .andExpect(jsonPath("$.attendance.percentage").value(33.33))
            .andExpect(jsonPath("$.failuresByGrade", hasSize(1)))
            .andExpect(jsonPath("$.failuresByGrade[0].gradeId").value(apprenticeGrade.getId()))
            .andExpect(jsonPath("$.failuresByGrade[0].unexcusedFailures").value(1))
            .andExpect(jsonPath("$.failuresByGrade[0].threshold").value(threshold))
            .andExpect(jsonPath("$.failuresByGrade[0].missingToThreshold").value(threshold - 1))
            .andExpect(jsonPath("$.justifications.pending").value(1))
            .andExpect(jsonPath("$.justifications.approved").value(1))
            .andExpect(jsonPath("$.justifications.rejected").value(2))
            .andExpect(jsonPath("$.justifications.withinCorrectionWindow", hasSize(1)))
            .andExpect(jsonPath("$.justifications.withinCorrectionWindow[0].remainingBusinessDays").value(2))
            .andExpect(jsonPath("$.justifications.withinCorrectionWindow[0].deadline").value(BusinessDays.plus(today, 2).toString()))
            .andExpect(jsonPath("$.grades", hasSize(1)))
            .andExpect(jsonPath("$.grades[0].gradeId").value(apprenticeGrade.getId()))
            .andExpect(jsonPath("$.grades[0].gradeCode").value(apprenticeGrade.getCode()))
            .andExpect(jsonPath("$.grades[0].programName").value(apprenticeProgram.getName()))
            .andExpect(jsonPath("$.grades[0].subjects[*].subjectName", hasItem(apprenticeSectionA.getSubjectName())))
            .andExpect(jsonPath("$.grades[0].subjects[*].subjectName", hasItem(apprenticeSectionB.getSubjectName())))
            .andExpect(jsonPath("$.upcomingClasses[0].classSectionId").value(apprenticeSectionA.getId()))
            .andExpect(jsonPath("$.upcomingClasses[0].date").value(tomorrow.toString()))
            .andExpect(jsonPath("$.activeAlerts").value(1))
            .andExpect(jsonPath("$.trimesterMessage").isEmpty());
    }

    @Test
    @WithMockUser(username = APPRENTICE_WITHOUT_TRIMESTER_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void testGetDashboardAsApprenticeWithoutActiveTrimesterReturnsEmptyTrimesterIndicators() throws Exception {
        apprenticeUser = persistUser(APPRENTICE_WITHOUT_TRIMESTER_LOGIN);
        apprenticeProfile = persistProfile("DX" + uniqueSuffix(), apprenticeUser);

        restDashboardMockMvc
            .perform(get(API_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.kpis").doesNotExist())
            // E2: the trimester dependent indicators travel empty with the message.
            .andExpect(jsonPath("$.attendance").isEmpty())
            .andExpect(jsonPath("$.failuresByGrade", hasSize(0)))
            .andExpect(jsonPath("$.upcomingClasses", hasSize(0)))
            .andExpect(jsonPath("$.trimesterMessage").value("No hay un trimestre activo"))
            // The trimester independent indicators are still computed.
            .andExpect(jsonPath("$.justifications.pending").value(0))
            .andExpect(jsonPath("$.grades", hasSize(0)))
            .andExpect(jsonPath("$.activeAlerts").value(0));
    }

    // -----------------------------------------------------------------
    // Fixture helpers
    // -----------------------------------------------------------------

    /**
     * Persists a profile with a resolvable login and document number, so the service can resolve
     * it from the security context.
     */
    private UserProfile persistProfile(String documentNumber, User user) {
        UserProfile profile = UserProfileResourceIT.createEntity();
        profile.setDocumentNumber(documentNumber);
        profile.setUser(user);
        profile.setDocumentType(null);
        return userProfileRepository.save(profile);
    }

    private User persistUser(String login) {
        User user = UserResourceIT.createEntity();
        user.setLogin(login);
        user.setEmail(login + "@example.com");
        user.setActivated(true);
        return userRepository.save(user);
    }

    private Grade persistGrade(String code, LocalDate today) {
        Grade grade = GradeResourceIT.createEntity();
        grade.setCode(code);
        grade.setState(StateGrade.ACTIVA);
        grade.setStartDate(today.minusDays(30));
        grade.setEndDate(today.plusDays(30));
        return gradeRepository.save(grade);
    }

    private ClassSection persistClassSection(String subjectName, Grade grade, UserProfile instructor) {
        return classSectionRepository.save(new ClassSection().subjectName(subjectName).isActive(true).grade(grade).instructor(instructor));
    }

    private ClassSchedule persistSchedule(
        ClassSection classSection,
        Trimester trimester,
        LocalDate date,
        String startTime,
        String endTime
    ) {
        return classScheduleRepository.save(
            new ClassSchedule()
                .classSection(classSection)
                .trimester(trimester)
                .dayOfWeek(dayOfWeekOf(date))
                .startTime(LocalTime.parse(startTime))
                .endTime(LocalTime.parse(endTime))
        );
    }

    private Attendance persistAttendance(ClassSection classSection, LocalDate date, StateAttendance state) {
        return attendanceRepository.save(
            new Attendance().classSection(classSection).student(apprenticeProfile).date(date).stateAttendance(state)
        );
    }

    private JustificationDetails persistPart(ClassSection classSection, StateJustification state, Instant responseDate) {
        return justificationDetailsRepository.save(
            new JustificationDetails()
                .stateJustification(state)
                .rejectionReason("")
                .correctionText("")
                .correctionFileUrlContentType("application/pdf")
                .responseDate(responseDate)
                .classSection(classSection)
                .justification(apprenticeJustification)
        );
    }

    private long expectedAccumulatedThreshold() {
        return globalConfigurationRepository
            .findById(GlobalConfiguration.GLOBAL_CONFIGURATION_ID)
            .map(GlobalConfiguration::getAccumulatedAbsenceAlertThreshold)
            .map(Integer::longValue)
            .orElse(GlobalConfigurationServiceImpl.DEFAULT_ACCUMULATED_ABSENCE_ALERT_THRESHOLD.longValue());
    }

    private static DayOfWeek dayOfWeekOf(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> DayOfWeek.LUNES;
            case TUESDAY -> DayOfWeek.MARTES;
            case WEDNESDAY -> DayOfWeek.MIERCOLES;
            case THURSDAY -> DayOfWeek.JUEVES;
            case FRIDAY -> DayOfWeek.VIERNES;
            case SATURDAY -> DayOfWeek.SABADO;
            case SUNDAY -> DayOfWeek.DOMINGO;
        };
    }
}
