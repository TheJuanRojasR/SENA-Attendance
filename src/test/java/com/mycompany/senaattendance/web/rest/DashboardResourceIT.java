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
import com.mycompany.senaattendance.domain.ClassException;
import com.mycompany.senaattendance.domain.ClassSchedule;
import com.mycompany.senaattendance.domain.ClassSection;
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
import com.mycompany.senaattendance.domain.enumeration.StateGrade;
import com.mycompany.senaattendance.domain.enumeration.StateJustification;
import com.mycompany.senaattendance.domain.enumeration.StateTrimester;
import com.mycompany.senaattendance.repository.AlertaRepository;
import com.mycompany.senaattendance.repository.ApprenticeRepository;
import com.mycompany.senaattendance.repository.ClassExceptionRepository;
import com.mycompany.senaattendance.repository.ClassScheduleRepository;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.repository.JustificationDetailsRepository;
import com.mycompany.senaattendance.repository.JustificationRepository;
import com.mycompany.senaattendance.repository.ModalityRepository;
import com.mycompany.senaattendance.repository.ProgramRepository;
import com.mycompany.senaattendance.repository.TrimesterRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
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

    @AfterEach
    void cleanup() {
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
        instructorScheduleTodayA = persistSchedule(instructorSectionA, today, "07:00", "09:00");
        instructorScheduleUpcomingA = persistSchedule(instructorSectionA, tomorrow, "07:00", "09:00");
        instructorExceptionTodayA = classExceptionRepository.save(
            new ClassException().date(today).reason("Jornada institucional").classSection(instructorSectionA)
        );
        // Materia B has a plain session today.
        instructorScheduleTodayB = persistSchedule(instructorSectionB, today, "10:00", "12:00");

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

    private ClassSchedule persistSchedule(ClassSection classSection, LocalDate date, String startTime, String endTime) {
        return classScheduleRepository.save(
            new ClassSchedule()
                .classSection(classSection)
                .trimester(instructorTrimester)
                .dayOfWeek(dayOfWeekOf(date))
                .startTime(LocalTime.parse(startTime))
                .endTime(LocalTime.parse(endTime))
        );
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
