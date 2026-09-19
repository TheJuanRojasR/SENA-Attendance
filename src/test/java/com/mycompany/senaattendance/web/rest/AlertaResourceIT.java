package com.mycompany.senaattendance.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.Alerta;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.AlertaState;
import com.mycompany.senaattendance.domain.enumeration.AlertaType;
import com.mycompany.senaattendance.domain.enumeration.StateTrimester;
import com.mycompany.senaattendance.repository.AlertaRepository;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.repository.TrimesterRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the {@link AlertaResource} inbox (UC013): the readable scope of the
 * instructor against the whole view of the administrator, the optional filters with pagination,
 * the read and attend transitions with their observation and the history of one apprentice.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class AlertaResourceIT {

    private static final String ENTITY_API_URL = "/api/alerts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_READ_API_URL_ID = ENTITY_API_URL + "/{id}/read";
    private static final String ENTITY_ATTEND_API_URL_ID = ENTITY_API_URL + "/{id}/attend";
    private static final String ENTITY_STUDENT_API_URL = ENTITY_API_URL + "/students/{studentId}";

    private static final String INSTRUCTOR_LOGIN = "alerts_instructor";
    private static final String OTHER_INSTRUCTOR_LOGIN = "other_alerts_instructor";
    private static final String FIRST_STUDENT_LOGIN = "alerts_student_1";
    private static final String SECOND_STUDENT_LOGIN = "alerts_student_2";

    private static final Instant OLDER_GENERATED_AT = Instant.parse("2026-09-01T10:00:00Z");
    private static final Instant MIDDLE_GENERATED_AT = Instant.parse("2026-09-05T10:00:00Z");
    private static final Instant HISTORY_GENERATED_AT = Instant.parse("2026-09-08T10:00:00Z");
    private static final Instant OTHER_GENERATED_AT = Instant.parse("2026-09-10T10:00:00Z");
    private static final Instant OTHER_ACCUMULATED_GENERATED_AT = Instant.parse("2026-09-12T10:00:00Z");
    private static final Instant RESOLVED_GENERATED_AT = Instant.parse("2026-09-14T10:00:00Z");

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MockMvc restAlertaMockMvc;

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private ClassSectionRepository classSectionRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private TrimesterRepository trimesterRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Clock clock;

    private UserProfile instructor;

    private UserProfile otherInstructor;

    private UserProfile firstStudent;

    private UserProfile secondStudent;

    private Grade ownGrade;

    private Grade otherGrade;

    private ClassSection ownClassSection;

    private ClassSection otherClassSection;

    private Trimester trimester;

    private Alerta ownAlert;

    private Alerta ownAccumulatedAlert;

    private Alerta ownHistoryAlert;

    private Alerta otherAlert;

    private Alerta otherAccumulatedAlert;

    private Alerta ownResolvedAlert;

    /**
     * Persists the graph the inbox tests read: two instructors, two apprentices, the ficha of each
     * instructor with one materia and an active trimester. The alerts are persisted per test so the
     * generated dates pin the ordering and the filters.
     */
    @BeforeEach
    void initAlertsFixture() {
        instructor = persistProfile(INSTRUCTOR_LOGIN, "4100000001");
        otherInstructor = persistProfile(OTHER_INSTRUCTOR_LOGIN, "4100000002");
        firstStudent = persistProfile(FIRST_STUDENT_LOGIN, "4200000001");
        secondStudent = persistProfile(SECOND_STUDENT_LOGIN, "4200000002");

        LocalDate today = LocalDate.now(clock);
        ownGrade = persistGrade("ALR-A", today);
        otherGrade = persistGrade("ALR-B", today);
        ownClassSection = persistClassSection("Materia del instructor", ownGrade, instructor);
        otherClassSection = persistClassSection("Materia del otro instructor", otherGrade, otherInstructor);
        trimester = trimesterRepository.save(
            new Trimester()
                .name("Trimestre de alertas")
                .startDate(today.minusDays(60))
                .endDate(today.plusDays(60))
                .status(StateTrimester.ACTIVO)
        );

        ownAlert = persistAlert(firstStudent, ownClassSection, ownGrade, AlertaType.CONSECUTIVAS, AlertaState.NO_LEIDA, OLDER_GENERATED_AT);
        ownAccumulatedAlert = persistAlert(firstStudent, null, ownGrade, AlertaType.ACUMULADAS, AlertaState.LEIDA, MIDDLE_GENERATED_AT);
        ownHistoryAlert = persistAlert(
            secondStudent,
            ownClassSection,
            ownGrade,
            AlertaType.CONSECUTIVAS,
            AlertaState.ATENDIDA,
            HISTORY_GENERATED_AT
        );
        otherAlert = persistAlert(
            firstStudent,
            otherClassSection,
            otherGrade,
            AlertaType.CONSECUTIVAS,
            AlertaState.NO_LEIDA,
            OTHER_GENERATED_AT
        );
        otherAccumulatedAlert = persistAlert(
            secondStudent,
            null,
            otherGrade,
            AlertaType.ACUMULADAS,
            AlertaState.NO_LEIDA,
            OTHER_ACCUMULATED_GENERATED_AT
        );
        ownResolvedAlert = persistAlert(
            firstStudent,
            ownClassSection,
            ownGrade,
            AlertaType.CONSECUTIVAS,
            AlertaState.RESUELTA_AUTOMATICAMENTE,
            RESOLVED_GENERATED_AT
        );
        ownResolvedAlert.setResolvedAt(RESOLVED_GENERATED_AT);
        ownResolvedAlert = alertaRepository.save(ownResolvedAlert);
    }

    @AfterEach
    void cleanup() {
        alertaRepository.deleteAll();
        classSectionRepository.deleteAll();
        gradeRepository.deleteAll();
        trimesterRepository.deleteAll();
        userProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    // -----------------------------------------------------------------
    // A1 — Consultar alertas con scoping
    // -----------------------------------------------------------------

    @Test
    void getAlertsAsAdminReturnsEveryAlertNewestFirst() throws Exception {
        restAlertaMockMvc
            .perform(get(ENTITY_API_URL))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "6"))
            .andExpect(jsonPath("$", hasSize(6)))
            .andExpect(jsonPath("$[0].id").value(ownResolvedAlert.getId()))
            .andExpect(jsonPath("$[5].id").value(ownAlert.getId()));
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void getAlertsAsInstructorReturnsOnlyTheAlertsOfOwnMateriasAndFichas() throws Exception {
        restAlertaMockMvc
            .perform(get(ENTITY_API_URL))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "4"))
            .andExpect(jsonPath("$", hasSize(4)))
            .andExpect(jsonPath("$[0].id").value(ownResolvedAlert.getId()))
            .andExpect(jsonPath("$[1].id").value(ownHistoryAlert.getId()))
            .andExpect(jsonPath("$[2].id").value(ownAccumulatedAlert.getId()))
            .andExpect(jsonPath("$[3].id").value(ownAlert.getId()));
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void getAlertsOfAnotherInstructorsScopeReturnsEmptyPage() throws Exception {
        restAlertaMockMvc
            .perform(get(ENTITY_API_URL).param("gradeId", otherGrade.getId()))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "0"))
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAlertsFiltersByType() throws Exception {
        restAlertaMockMvc
            .perform(get(ENTITY_API_URL).param("type", AlertaType.CONSECUTIVAS.name()))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "4"))
            .andExpect(jsonPath("$", hasSize(4)))
            .andExpect(jsonPath("$[*].type", everyItem(is("CONSECUTIVAS"))));
    }

    @Test
    void getAlertsFiltersByState() throws Exception {
        restAlertaMockMvc
            .perform(get(ENTITY_API_URL).param("state", AlertaState.NO_LEIDA.name()))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "3"))
            .andExpect(jsonPath("$[0].id").value(otherAccumulatedAlert.getId()))
            .andExpect(jsonPath("$[2].id").value(ownAlert.getId()));
    }

    @Test
    void getAlertsFiltersByGrade() throws Exception {
        restAlertaMockMvc
            .perform(get(ENTITY_API_URL).param("gradeId", otherGrade.getId()))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "2"))
            .andExpect(jsonPath("$[0].id").value(otherAccumulatedAlert.getId()))
            .andExpect(jsonPath("$[1].id").value(otherAlert.getId()));
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void getAlertsFiltersByStudentInsideTheReadableScope() throws Exception {
        restAlertaMockMvc
            .perform(get(ENTITY_API_URL).param("studentId", secondStudent.getId()))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "1"))
            .andExpect(jsonPath("$[0].id").value(ownHistoryAlert.getId()));
    }

    @Test
    void getAlertsFiltersByGenerationRange() throws Exception {
        restAlertaMockMvc
            .perform(get(ENTITY_API_URL).param("from", "2026-09-06T00:00:00Z").param("to", "2026-09-11T00:00:00Z"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "2"))
            .andExpect(jsonPath("$[0].id").value(otherAlert.getId()))
            .andExpect(jsonPath("$[1].id").value(ownHistoryAlert.getId()));
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void getOwnAlertReadsItWithItsScope() throws Exception {
        restAlertaMockMvc
            .perform(get(ENTITY_API_URL_ID, ownAlert.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("CONSECUTIVAS"))
            .andExpect(jsonPath("$.state").value("NO_LEIDA"))
            .andExpect(jsonPath("$.absenceCount").value(4))
            .andExpect(jsonPath("$.threshold").value(3))
            .andExpect(jsonPath("$.student.documentNumber").value("4200000001"))
            .andExpect(jsonPath("$.classSection.subjectName").value("Materia del instructor"))
            .andExpect(jsonPath("$.grade.code").value("ALR-A"))
            .andExpect(jsonPath("$.trimester.name").value("Trimestre de alertas"));
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void getAlertOfAnotherInstructorsScopeReturnsNotFound() throws Exception {
        restAlertaMockMvc.perform(get(ENTITY_API_URL_ID, otherAlert.getId())).andExpect(status().isNotFound());
        restAlertaMockMvc.perform(get(ENTITY_API_URL_ID, otherAccumulatedAlert.getId())).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = FIRST_STUDENT_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void getAlertsAsApprenticeReturnsForbidden() throws Exception {
        restAlertaMockMvc.perform(get(ENTITY_API_URL)).andExpect(status().isForbidden());
    }

    // -----------------------------------------------------------------
    // A2 — Marcar leída
    // -----------------------------------------------------------------

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void markAlertAsReadMovesItToRead() throws Exception {
        restAlertaMockMvc
            .perform(patch(ENTITY_READ_API_URL_ID, ownAlert.getId()).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value("LEIDA"));

        assertThat(alertaRepository.findById(ownAlert.getId()).orElseThrow().getState()).isEqualTo(AlertaState.LEIDA);
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void markAlertAsReadIsIdempotent() throws Exception {
        restAlertaMockMvc
            .perform(patch(ENTITY_READ_API_URL_ID, ownAccumulatedAlert.getId()).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value("LEIDA"));
        // A resolved alert keeps its final state: marking it as read never rewrites history.
        restAlertaMockMvc
            .perform(patch(ENTITY_READ_API_URL_ID, ownResolvedAlert.getId()).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value("RESUELTA_AUTOMATICAMENTE"));
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void markAlertOfAnotherInstructorsScopeAsReadReturnsNotFound() throws Exception {
        restAlertaMockMvc
            .perform(patch(ENTITY_READ_API_URL_ID, otherAlert.getId()).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        assertThat(alertaRepository.findById(otherAlert.getId()).orElseThrow().getState()).isEqualTo(AlertaState.NO_LEIDA);
    }

    // -----------------------------------------------------------------
    // A3 — Marcar atendida con observación
    // -----------------------------------------------------------------

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void attendAlertSavesTheObservation() throws Exception {
        restAlertaMockMvc
            .perform(
                patch(ENTITY_ATTEND_API_URL_ID, ownAlert.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(Map.of("observation", "Se contactó al aprendiz")))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value("ATENDIDA"))
            .andExpect(jsonPath("$.observation").value("Se contactó al aprendiz"));

        Alerta attended = alertaRepository.findById(ownAlert.getId()).orElseThrow();
        assertThat(attended.getState()).isEqualTo(AlertaState.ATENDIDA);
        assertThat(attended.getObservation()).isEqualTo("Se contactó al aprendiz");
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void attendAlertWithAMissingObservationReturnsBadRequest() throws Exception {
        restAlertaMockMvc
            .perform(
                patch(ENTITY_ATTEND_API_URL_ID, ownAlert.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(Map.of()))
            )
            .andExpect(status().isBadRequest());

        assertThat(alertaRepository.findById(ownAlert.getId()).orElseThrow().getState()).isEqualTo(AlertaState.NO_LEIDA);
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void attendAlertWithAnObservationLongerThanTheLimitReturnsBadRequest() throws Exception {
        restAlertaMockMvc
            .perform(
                patch(ENTITY_ATTEND_API_URL_ID, ownAlert.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(Map.of("observation", "x".repeat(301))))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void attendAlertOfAnotherInstructorsScopeReturnsNotFound() throws Exception {
        restAlertaMockMvc
            .perform(
                patch(ENTITY_ATTEND_API_URL_ID, otherAlert.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(Map.of("observation", "Seguimiento")))
            )
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void attendAResolvedAlertReturnsBadRequest() throws Exception {
        restAlertaMockMvc
            .perform(
                patch(ENTITY_ATTEND_API_URL_ID, ownResolvedAlert.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(Map.of("observation", "Seguimiento tardío")))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.alertAlreadyResolved"));
    }

    // -----------------------------------------------------------------
    // A5 — Historial por aprendiz
    // -----------------------------------------------------------------

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void getStudentHistoryReturnsOnlyTheReadableAlertsOfTheApprentice() throws Exception {
        restAlertaMockMvc
            .perform(get(ENTITY_STUDENT_API_URL, firstStudent.getId()))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "3"))
            .andExpect(jsonPath("$[0].id").value(ownResolvedAlert.getId()))
            .andExpect(jsonPath("$[2].id").value(ownAlert.getId()));

        restAlertaMockMvc
            .perform(get(ENTITY_STUDENT_API_URL, secondStudent.getId()))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "1"))
            .andExpect(jsonPath("$[0].id").value(ownHistoryAlert.getId()));
    }

    @Test
    void getStudentHistoryAsAdminReturnsEveryAlertOfTheApprentice() throws Exception {
        restAlertaMockMvc
            .perform(get(ENTITY_STUDENT_API_URL, secondStudent.getId()))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "2"))
            .andExpect(jsonPath("$[0].id").value(otherAccumulatedAlert.getId()))
            .andExpect(jsonPath("$[1].id").value(ownHistoryAlert.getId()));
    }

    // -----------------------------------------------------------------
    // Fixture helpers
    // -----------------------------------------------------------------

    private Alerta persistAlert(
        UserProfile student,
        ClassSection classSection,
        Grade grade,
        AlertaType type,
        AlertaState state,
        Instant generatedAt
    ) {
        return alertaRepository.save(
            new Alerta()
                .student(student)
                .classSection(classSection)
                .grade(grade)
                .trimester(trimester)
                .type(type)
                .state(state)
                .absenceCount(4)
                .threshold(3)
                .generatedAt(generatedAt)
        );
    }

    private UserProfile persistProfile(String login, String documentNumber) {
        User user = UserResourceIT.createEntity();
        user.setLogin(login);
        user.setEmail(login + "@example.com");
        user.setActivated(true);
        user = userRepository.save(user);

        UserProfile profile = UserProfileResourceIT.createEntity();
        profile.setDocumentNumber(documentNumber);
        profile.setUser(user);
        return userProfileRepository.save(profile);
    }

    private Grade persistGrade(String code, LocalDate today) {
        Grade grade = GradeResourceIT.createEntity();
        grade.setCode(code);
        grade.setStartDate(today.minusDays(30));
        grade.setEndDate(today.plusDays(30));
        return gradeRepository.save(grade);
    }

    private ClassSection persistClassSection(String subjectName, Grade grade, UserProfile instructor) {
        return classSectionRepository.save(new ClassSection().subjectName(subjectName).isActive(true).grade(grade).instructor(instructor));
    }
}
