package com.mycompany.senaattendance.web.rest;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.Modality;
import com.mycompany.senaattendance.domain.Program;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.StateGrade;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.repository.ModalityRepository;
import com.mycompany.senaattendance.repository.ProgramRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import java.time.Instant;
import java.time.LocalDate;
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

    @Autowired
    private MockMvc restDashboardMockMvc;

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

    // Track the entities seeded in the admin test so the shared DB can be cleaned up precisely.
    private Program seededProgram;
    private Modality seededModality;
    private UserProfile seededProfile;
    private Grade seededGrade;
    private ClassSection seededClassSection;

    @AfterEach
    void cleanup() {
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
}
