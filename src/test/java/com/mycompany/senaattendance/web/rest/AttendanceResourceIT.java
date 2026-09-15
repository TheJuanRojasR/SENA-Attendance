package com.mycompany.senaattendance.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.Apprentice;
import com.mycompany.senaattendance.domain.Attendance;
import com.mycompany.senaattendance.domain.ClassException;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.DocumentType;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.Modality;
import com.mycompany.senaattendance.domain.Program;
import com.mycompany.senaattendance.domain.TimeSlot;
import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.StateAcademic;
import com.mycompany.senaattendance.domain.enumeration.StateAttendance;
import com.mycompany.senaattendance.domain.enumeration.StateTrimester;
import com.mycompany.senaattendance.repository.ApprenticeRepository;
import com.mycompany.senaattendance.repository.AttendanceRepository;
import com.mycompany.senaattendance.repository.AuthorityRepository;
import com.mycompany.senaattendance.repository.ClassExceptionRepository;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.DocumentTypeRepository;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.repository.ModalityRepository;
import com.mycompany.senaattendance.repository.ProgramRepository;
import com.mycompany.senaattendance.repository.TimeSlotRepository;
import com.mycompany.senaattendance.repository.TrimesterRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the {@link AttendanceResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class AttendanceResourceIT {

    private static final LocalDate DEFAULT_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATE = LocalDate.now();

    private static final StateAttendance DEFAULT_STATE_ATTENDANCE = StateAttendance.PRESENTE;
    private static final StateAttendance UPDATED_STATE_ATTENDANCE = StateAttendance.FALLA;

    private static final String ENTITY_API_URL = "/api/attendances";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String SESSION_API_URL = ENTITY_API_URL + "/session";

    private static final String INSTRUCTOR_LOGIN = "session_instructor";
    private static final String OTHER_INSTRUCTOR_LOGIN = "other_session_instructor";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MockMvc restAttendanceMockMvc;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private ApprenticeRepository apprenticeRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private ClassExceptionRepository classExceptionRepository;

    @Autowired
    private ClassSectionRepository classSectionRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private ModalityRepository modalityRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private TrimesterRepository trimesterRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Clock clock;

    private final List<Attendance> insertedAttendances = new ArrayList<>();

    private final List<ClassException> insertedExceptions = new ArrayList<>();

    private final List<ClassSection> insertedClassSections = new ArrayList<>();

    private final List<Apprentice> insertedApprentices = new ArrayList<>();

    private final List<Grade> insertedGrades = new ArrayList<>();

    private final List<Trimester> insertedTrimesters = new ArrayList<>();

    private final List<Program> insertedPrograms = new ArrayList<>();

    private final List<Modality> insertedModalities = new ArrayList<>();

    private final List<TimeSlot> insertedTimeSlots = new ArrayList<>();

    private final List<UserProfile> insertedProfiles = new ArrayList<>();

    private final List<User> insertedUsers = new ArrayList<>();

    private final List<DocumentType> insertedDocumentTypes = new ArrayList<>();

    private DocumentType documentType;

    private UserProfile instructor;

    private UserProfile otherInstructor;

    private UserProfile firstStudent;

    private UserProfile secondStudent;

    private UserProfile unenrolledStudent;

    private Trimester trimester;

    private Grade grade;

    private ClassSection classSection;

    private ClassSection otherClassSection;

    private LocalDate sessionDate;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity. The referenced
     * entities are not persisted; tests that need them resolved persist their own
     * fixture graph.
     */
    public static Attendance createEntity() {
        Attendance attendance = new Attendance().date(DEFAULT_DATE).stateAttendance(DEFAULT_STATE_ATTENDANCE);
        // Add required entity
        ClassSection classSection;
        classSection = ClassSectionResourceIT.createEntity();
        classSection.setId("fixed-id-for-tests");
        attendance.setClassSection(classSection);
        // Add required entity
        UserProfile userProfile;
        userProfile = UserProfileResourceIT.createEntity();
        userProfile.setId("fixed-id-for-tests");
        attendance.setStudent(userProfile);
        return attendance;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Attendance createUpdatedEntity() {
        Attendance updatedAttendance = new Attendance().date(UPDATED_DATE).stateAttendance(UPDATED_STATE_ATTENDANCE);
        // Add required entity
        ClassSection classSection;
        classSection = ClassSectionResourceIT.createUpdatedEntity();
        classSection.setId("fixed-id-for-tests");
        updatedAttendance.setClassSection(classSection);
        // Add required entity
        UserProfile userProfile;
        userProfile = UserProfileResourceIT.createUpdatedEntity();
        userProfile.setId("fixed-id-for-tests");
        updatedAttendance.setStudent(userProfile);
        return updatedAttendance;
    }

    /**
     * Persists the fixture graph the session tests need: an active trimester, one operable ficha
     * with an assigned instructor and two enrolled apprentices, a second materia of the same ficha
     * assigned to another instructor, plus an extra profile without an enrollment.
     */
    @BeforeEach
    void initSessionFixture() {
        documentType = persistDocumentType();
        instructor = persistProfile(INSTRUCTOR_LOGIN, "1000000001", AuthoritiesConstants.INSTRUCTOR);
        otherInstructor = persistProfile(OTHER_INSTRUCTOR_LOGIN, "1000000002", AuthoritiesConstants.INSTRUCTOR);
        firstStudent = persistProfile("session_student_1", "2000000001", AuthoritiesConstants.APPRENTICE);
        secondStudent = persistProfile("session_student_2", "2000000002", AuthoritiesConstants.APPRENTICE);
        unenrolledStudent = persistProfile("session_student_3", "2000000003", AuthoritiesConstants.APPRENTICE);

        LocalDate today = LocalDate.now(clock);
        sessionDate = today;
        trimester = persistTrimester("Trimestre de sesión", today.minusDays(60), today.plusDays(60));
        grade = persistGrade("SES-001", today.minusDays(30), today.plusDays(30));
        classSection = persistClassSection("Materia de sesión", grade, instructor);
        otherClassSection = persistClassSection("Otra materia de sesión", grade, otherInstructor);
        persistEnrollment(firstStudent, grade, StateAcademic.MATRICULADO);
        persistEnrollment(secondStudent, grade, StateAcademic.MATRICULADO);
    }

    @AfterEach
    void cleanup() {
        // The session endpoint persists records the fixture cannot track, so sweep the collection.
        attendanceRepository.deleteAll();
        insertedAttendances.clear();
        insertedExceptions.forEach(classExceptionRepository::delete);
        insertedExceptions.clear();
        insertedClassSections.forEach(classSectionRepository::delete);
        insertedClassSections.clear();
        insertedApprentices.forEach(apprenticeRepository::delete);
        insertedApprentices.clear();
        insertedGrades.forEach(gradeRepository::delete);
        insertedGrades.clear();
        insertedTrimesters.forEach(trimesterRepository::delete);
        insertedTrimesters.clear();
        insertedProfiles.forEach(userProfileRepository::delete);
        insertedProfiles.clear();
        insertedDocumentTypes.forEach(documentTypeRepository::delete);
        insertedDocumentTypes.clear();
        insertedUsers.forEach(userRepository::delete);
        insertedUsers.clear();
        insertedTimeSlots.forEach(timeSlotRepository::delete);
        insertedTimeSlots.clear();
        insertedModalities.forEach(modalityRepository::delete);
        insertedModalities.clear();
        insertedPrograms.forEach(programRepository::delete);
        insertedPrograms.clear();
    }

    // -----------------------------------------------------------------
    // The generic CRUD is not part of the use case
    // -----------------------------------------------------------------

    @Test
    void createAttendanceIsNotAllowed() throws Exception {
        restAttendanceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void replaceAttendanceIsNotAllowed() throws Exception {
        restAttendanceMockMvc
            .perform(put(ENTITY_API_URL_ID, UUID.randomUUID().toString()).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void deleteAttendanceIsNotAllowed() throws Exception {
        restAttendanceMockMvc
            .perform(delete(ENTITY_API_URL_ID, UUID.randomUUID().toString()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isMethodNotAllowed());
    }

    // -----------------------------------------------------------------
    // A2 — Edit one record's state
    // -----------------------------------------------------------------

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void patchAttendanceStateAsAssignedInstructorUpdatesOnlyTheState() throws Exception {
        Attendance attendance = persistAttendance(classSection, firstStudent, sessionDate, StateAttendance.PRESENTE);

        Map<String, Object> payload = statePayload(attendance.getId(), StateAttendance.FALLA);
        // The payload may carry the materia, the apprentice and the date, but the edit ignores them.
        payload.put("date", sessionDate.minusDays(2).toString());
        payload.put("classSection", Map.of("id", otherClassSection.getId()));
        payload.put("student", Map.of("id", secondStudent.getId()));

        restAttendanceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, attendance.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(payload))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stateAttendance").value(StateAttendance.FALLA.toString()));

        Attendance reloaded = attendanceRepository.findById(attendance.getId()).orElseThrow();
        assertThat(reloaded.getStateAttendance()).isEqualTo(StateAttendance.FALLA);
        assertThat(reloaded.getDate()).isEqualTo(sessionDate);
        assertThat(reloaded.getClassSection().getId()).isEqualTo(classSection.getId());
        assertThat(reloaded.getStudent().getId()).isEqualTo(firstStudent.getId());
    }

    @Test
    @WithMockUser(username = OTHER_INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void patchAttendanceStateOfAnotherInstructorsRecordReturnsBadRequest() throws Exception {
        Attendance attendance = persistAttendance(classSection, firstStudent, sessionDate, StateAttendance.PRESENTE);

        restAttendanceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, attendance.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(statePayload(attendance.getId(), StateAttendance.FALLA)))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.notYourClassSection"));

        assertThat(attendanceRepository.findById(attendance.getId()).orElseThrow().getStateAttendance()).isEqualTo(
            StateAttendance.PRESENTE
        );
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void patchAttendanceStateToJustifiedReturnsBadRequest() throws Exception {
        Attendance attendance = persistAttendance(classSection, firstStudent, sessionDate, StateAttendance.PRESENTE);

        restAttendanceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, attendance.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(statePayload(attendance.getId(), StateAttendance.JUSTIFICADA)))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.invalidAttendanceState"));

        assertThat(attendanceRepository.findById(attendance.getId()).orElseThrow().getStateAttendance()).isEqualTo(
            StateAttendance.PRESENTE
        );
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void patchAttendanceStateInClosedTrimesterReturnsBadRequest() throws Exception {
        LocalDate today = LocalDate.now(clock);
        Attendance attendance = persistAttendance(classSection, firstStudent, today.minusDays(10), StateAttendance.PRESENTE);
        trimester.setEndDate(today.minusDays(5));
        trimesterRepository.save(trimester);

        restAttendanceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, attendance.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(statePayload(attendance.getId(), StateAttendance.FALLA)))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.trimesterClosed"));

        assertThat(attendanceRepository.findById(attendance.getId()).orElseThrow().getStateAttendance()).isEqualTo(
            StateAttendance.PRESENTE
        );
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void patchNonExistingAttendanceReturnsNotFound() throws Exception {
        String missingId = UUID.randomUUID().toString();

        restAttendanceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, missingId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(statePayload(missingId, StateAttendance.FALLA)))
            )
            .andExpect(status().isNotFound());
    }

    // -----------------------------------------------------------------
    // Scoped readings
    // -----------------------------------------------------------------

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void getAttendancesAsInstructorReturnsOnlyTheRecordsOfOwnClassSections() throws Exception {
        persistAttendance(classSection, firstStudent, sessionDate, StateAttendance.PRESENTE);
        persistAttendance(otherClassSection, secondStudent, sessionDate, StateAttendance.PRESENTE);

        restAttendanceMockMvc
            .perform(get(ENTITY_API_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "1"))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].classSection.id").value(classSection.getId()))
            .andExpect(jsonPath("$[0].student.documentNumber").value("2000000001"));
    }

    @Test
    void getAttendancesAsAdminReturnsEveryRecord() throws Exception {
        persistAttendance(classSection, firstStudent, sessionDate, StateAttendance.PRESENTE);
        persistAttendance(otherClassSection, secondStudent, sessionDate, StateAttendance.PRESENTE);

        restAttendanceMockMvc
            .perform(get(ENTITY_API_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "2"))
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getAttendanceAsAdminReadsAnyRecord() throws Exception {
        Attendance attendance = persistAttendance(otherClassSection, secondStudent, sessionDate, StateAttendance.FALLA);

        restAttendanceMockMvc
            .perform(get(ENTITY_API_URL_ID, attendance.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.date").value(sessionDate.toString()))
            .andExpect(jsonPath("$.stateAttendance").value(StateAttendance.FALLA.toString()));
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void getAttendanceOfAnotherInstructorsRecordReturnsNotFound() throws Exception {
        Attendance attendance = persistAttendance(otherClassSection, secondStudent, sessionDate, StateAttendance.FALLA);

        restAttendanceMockMvc.perform(get(ENTITY_API_URL_ID, attendance.getId())).andExpect(status().isNotFound());
    }

    @Test
    void getNonExistingAttendance() throws Exception {
        restAttendanceMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "attendance_apprentice", authorities = AuthoritiesConstants.APPRENTICE)
    void getAttendancesAsApprenticeReturnsForbidden() throws Exception {
        restAttendanceMockMvc.perform(get(ENTITY_API_URL).accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
        restAttendanceMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "attendance_coordinator", authorities = AuthoritiesConstants.COORDINATOR)
    void getAttendancesAsCoordinatorReturnsForbidden() throws Exception {
        restAttendanceMockMvc.perform(get(ENTITY_API_URL).accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    // -----------------------------------------------------------------
    // UC009 — Attendance session
    // -----------------------------------------------------------------

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void saveSessionWithAllApprenticesReturnsCompleteSession() throws Exception {
        Map<String, Object> payload = sessionPayload(
            classSection.getId(),
            sessionDate,
            List.of(
                confirmation(firstStudent.getId(), StateAttendance.PRESENTE),
                confirmation(secondStudent.getId(), StateAttendance.FALLA)
            )
        );

        restAttendanceMockMvc
            .perform(put(SESSION_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(payload)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.classSection.id").value(classSection.getId()))
            .andExpect(jsonPath("$.classSection.subjectName").value("Materia de sesión"))
            .andExpect(jsonPath("$.date").value(sessionDate.toString()))
            .andExpect(jsonPath("$.complete").value(true))
            .andExpect(jsonPath("$.enrolledCount").value(2))
            .andExpect(jsonPath("$.recordedCount").value(2))
            .andExpect(jsonPath("$.records.length()").value(2))
            .andExpect(jsonPath("$.records[*].student.documentNumber").value(containsInAnyOrder("2000000001", "2000000002")))
            .andExpect(jsonPath("$.records[*].stateAttendance").value(containsInAnyOrder("PRESENTE", "FALLA")));

        assertThat(attendanceRepository.findByClassSectionIdAndDate(classSection.getId(), sessionDate)).hasSize(2);
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void saveSessionWithPartialMarksLeavesUnconfirmedApprenticesWithoutRecord() throws Exception {
        Map<String, Object> payload = sessionPayload(
            classSection.getId(),
            sessionDate,
            List.of(confirmation(firstStudent.getId(), StateAttendance.PRESENTE))
        );

        restAttendanceMockMvc
            .perform(put(SESSION_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(payload)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.complete").value(false))
            .andExpect(jsonPath("$.enrolledCount").value(2))
            .andExpect(jsonPath("$.recordedCount").value(1))
            .andExpect(jsonPath("$.records.length()").value(1))
            .andExpect(jsonPath("$.records[0].student.documentNumber").value("2000000001"));

        assertThat(
            attendanceRepository.findByClassSectionIdAndStudentIdAndDate(classSection.getId(), firstStudent.getId(), sessionDate)
        ).isPresent();
        assertThat(
            attendanceRepository.findByClassSectionIdAndStudentIdAndDate(classSection.getId(), secondStudent.getId(), sessionDate)
        ).isEmpty();
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void saveSessionAgainUpdatesStatesWithoutDuplicatingRecords() throws Exception {
        saveSession(
            sessionPayload(
                classSection.getId(),
                sessionDate,
                List.of(
                    confirmation(firstStudent.getId(), StateAttendance.PRESENTE),
                    confirmation(secondStudent.getId(), StateAttendance.PRESENTE)
                )
            )
        );

        saveSession(
            sessionPayload(
                classSection.getId(),
                sessionDate,
                List.of(
                    confirmation(firstStudent.getId(), StateAttendance.FALLA),
                    confirmation(secondStudent.getId(), StateAttendance.FALLA)
                )
            )
        );

        assertThat(attendanceRepository.findByClassSectionIdAndDate(classSection.getId(), sessionDate)).hasSize(2);
        assertThat(
            attendanceRepository
                .findByClassSectionIdAndStudentIdAndDate(classSection.getId(), firstStudent.getId(), sessionDate)
                .orElseThrow()
                .getStateAttendance()
        ).isEqualTo(StateAttendance.FALLA);
        assertThat(
            attendanceRepository
                .findByClassSectionIdAndStudentIdAndDate(classSection.getId(), secondStudent.getId(), sessionDate)
                .orElseThrow()
                .getStateAttendance()
        ).isEqualTo(StateAttendance.FALLA);
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void saveSessionIgnoresTheIdSentInConfirmations() throws Exception {
        saveSession(
            sessionPayload(
                classSection.getId(),
                sessionDate,
                List.of(
                    confirmation(firstStudent.getId(), StateAttendance.PRESENTE),
                    confirmation(secondStudent.getId(), StateAttendance.PRESENTE)
                )
            )
        );
        String originalRecordId = attendanceRepository
            .findByClassSectionIdAndStudentIdAndDate(classSection.getId(), firstStudent.getId(), sessionDate)
            .orElseThrow()
            .getId();

        Map<String, Object> payload = sessionPayload(
            classSection.getId(),
            sessionDate,
            List.of(
                confirmationWithId(UUID.randomUUID().toString(), firstStudent.getId(), StateAttendance.FALLA),
                confirmation(secondStudent.getId(), StateAttendance.PRESENTE)
            )
        );
        saveSession(payload);

        Attendance reloaded = attendanceRepository
            .findByClassSectionIdAndStudentIdAndDate(classSection.getId(), firstStudent.getId(), sessionDate)
            .orElseThrow();
        assertThat(reloaded.getId()).isEqualTo(originalRecordId);
        assertThat(reloaded.getStateAttendance()).isEqualTo(StateAttendance.FALLA);
        assertThat(attendanceRepository.findByClassSectionIdAndDate(classSection.getId(), sessionDate)).hasSize(2);
    }

    @Test
    @WithMockUser(username = OTHER_INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void saveSessionForAnotherInstructorReturnsBadRequest() throws Exception {
        Map<String, Object> payload = sessionPayload(
            classSection.getId(),
            sessionDate,
            List.of(confirmation(firstStudent.getId(), StateAttendance.PRESENTE))
        );

        restAttendanceMockMvc
            .perform(put(SESSION_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(payload)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.notYourClassSection"));

        assertThat(attendanceRepository.findByClassSectionIdAndDate(classSection.getId(), sessionDate)).isEmpty();
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void saveSessionWithoutAssignedInstructorReturnsBadRequest() throws Exception {
        classSection.setInstructor(null);
        classSectionRepository.save(classSection);

        Map<String, Object> payload = sessionPayload(
            classSection.getId(),
            sessionDate,
            List.of(confirmation(firstStudent.getId(), StateAttendance.PRESENTE))
        );

        restAttendanceMockMvc
            .perform(put(SESSION_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(payload)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.classSectionWithoutInstructor"));

        assertThat(attendanceRepository.findByClassSectionIdAndDate(classSection.getId(), sessionDate)).isEmpty();
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void saveSessionWithJustifiedStateReturnsBadRequest() throws Exception {
        Map<String, Object> payload = sessionPayload(
            classSection.getId(),
            sessionDate,
            List.of(
                confirmation(firstStudent.getId(), StateAttendance.PRESENTE),
                confirmation(secondStudent.getId(), StateAttendance.JUSTIFICADA)
            )
        );

        restAttendanceMockMvc
            .perform(put(SESSION_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(payload)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.invalidAttendanceState"));

        assertThat(attendanceRepository.findByClassSectionIdAndDate(classSection.getId(), sessionDate)).isEmpty();
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void saveSessionWithNonEnrolledApprenticeReturnsBadRequest() throws Exception {
        Map<String, Object> payload = sessionPayload(
            classSection.getId(),
            sessionDate,
            List.of(confirmation(unenrolledStudent.getId(), StateAttendance.PRESENTE))
        );

        restAttendanceMockMvc
            .perform(put(SESSION_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(payload)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.studentNotEnrolled"));
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void saveSessionWithoutEnrolledApprenticesReturnsBadRequest() throws Exception {
        insertedApprentices.forEach(apprentice -> apprentice.setStateAcademic(StateAcademic.RETIRO_VOLUNTARIO));
        insertedApprentices.forEach(apprenticeRepository::save);

        Map<String, Object> payload = sessionPayload(
            classSection.getId(),
            sessionDate,
            List.of(confirmation(firstStudent.getId(), StateAttendance.PRESENTE))
        );

        restAttendanceMockMvc
            .perform(put(SESSION_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(payload)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.noActiveApprentices"));
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void saveSessionWithFutureDateReturnsBadRequest() throws Exception {
        Map<String, Object> payload = sessionPayload(
            classSection.getId(),
            sessionDate.plusDays(1),
            List.of(confirmation(firstStudent.getId(), StateAttendance.PRESENTE))
        );

        restAttendanceMockMvc
            .perform(put(SESSION_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(payload)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.futureSessionDate"));
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void saveSessionInClosedTrimesterReturnsBadRequest() throws Exception {
        LocalDate today = LocalDate.now(clock);
        trimester.setStartDate(today.minusDays(60));
        trimester.setEndDate(today.minusDays(5));
        trimesterRepository.save(trimester);

        Map<String, Object> payload = sessionPayload(
            classSection.getId(),
            today.minusDays(10),
            List.of(confirmation(firstStudent.getId(), StateAttendance.PRESENTE))
        );

        restAttendanceMockMvc
            .perform(put(SESSION_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(payload)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.trimesterClosed"));
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void saveSessionOutsideEveryTrimesterReturnsBadRequest() throws Exception {
        LocalDate today = LocalDate.now(clock);
        trimester.setStartDate(today.minusDays(60));
        trimester.setEndDate(today.minusDays(5));
        trimesterRepository.save(trimester);

        Map<String, Object> payload = sessionPayload(
            classSection.getId(),
            today.minusDays(3),
            List.of(confirmation(firstStudent.getId(), StateAttendance.PRESENTE))
        );

        restAttendanceMockMvc
            .perform(put(SESSION_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(payload)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.dateOutOfTrimester"));
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void saveSessionOutsideGradeRangeReturnsBadRequest() throws Exception {
        LocalDate today = LocalDate.now(clock);
        grade.setStartDate(today.minusDays(10));
        grade.setEndDate(today.plusDays(10));
        gradeRepository.save(grade);

        Map<String, Object> payload = sessionPayload(
            classSection.getId(),
            today.minusDays(20),
            List.of(confirmation(firstStudent.getId(), StateAttendance.PRESENTE))
        );

        restAttendanceMockMvc
            .perform(put(SESSION_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(payload)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.dateOutOfGradeRange"));
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void saveSessionOnNonTeachingDateReturnsBadRequest() throws Exception {
        insertedExceptions.add(
            classExceptionRepository.save(new ClassException().date(sessionDate).reason("Día festivo").classSection(classSection))
        );

        Map<String, Object> payload = sessionPayload(
            classSection.getId(),
            sessionDate,
            List.of(confirmation(firstStudent.getId(), StateAttendance.PRESENTE))
        );

        restAttendanceMockMvc
            .perform(put(SESSION_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(payload)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.nonTeachingDate"));
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void saveSessionWithUnknownClassSectionReturnsBadRequest() throws Exception {
        Map<String, Object> payload = sessionPayload(
            UUID.randomUUID().toString(),
            sessionDate,
            List.of(confirmation(firstStudent.getId(), StateAttendance.PRESENTE))
        );

        restAttendanceMockMvc
            .perform(put(SESSION_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(payload)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.idnotfound"));
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
    void saveSessionAsAdminReturnsForbidden() throws Exception {
        Map<String, Object> payload = sessionPayload(
            classSection.getId(),
            sessionDate,
            List.of(confirmation(firstStudent.getId(), StateAttendance.PRESENTE))
        );

        restAttendanceMockMvc
            .perform(put(SESSION_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(payload)))
            .andExpect(status().isForbidden());
    }

    // -----------------------------------------------------------------
    // Fixture helpers
    // -----------------------------------------------------------------

    private void saveSession(Map<String, Object> payload) throws Exception {
        restAttendanceMockMvc
            .perform(put(SESSION_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(payload)))
            .andExpect(status().isOk());
    }

    private static Map<String, Object> sessionPayload(String classSectionId, LocalDate date, List<Map<String, Object>> attendances) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("classSection", Map.of("id", classSectionId));
        payload.put("date", date.toString());
        payload.put("attendances", attendances);
        return payload;
    }

    private static Map<String, Object> confirmation(String studentId, StateAttendance stateAttendance) {
        Map<String, Object> confirmation = new HashMap<>();
        confirmation.put("studentId", studentId);
        confirmation.put("stateAttendance", stateAttendance.name());
        return confirmation;
    }

    private static Map<String, Object> confirmationWithId(String id, String studentId, StateAttendance stateAttendance) {
        Map<String, Object> confirmation = confirmation(studentId, stateAttendance);
        confirmation.put("id", id);
        return confirmation;
    }

    private static Map<String, Object> statePayload(String id, StateAttendance stateAttendance) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", id);
        payload.put("stateAttendance", stateAttendance.name());
        return payload;
    }

    private Attendance persistAttendance(ClassSection classSection, UserProfile student, LocalDate date, StateAttendance stateAttendance) {
        Attendance attendance = attendanceRepository.save(
            new Attendance().date(date).stateAttendance(stateAttendance).classSection(classSection).student(student)
        );
        insertedAttendances.add(attendance);
        return attendance;
    }

    private DocumentType persistDocumentType() {
        DocumentType persistedDocumentType = documentTypeRepository.save(DocumentTypeResourceIT.createEntity());
        insertedDocumentTypes.add(persistedDocumentType);
        return persistedDocumentType;
    }

    private UserProfile persistProfile(String login, String documentNumber, String authority) {
        User user = UserResourceIT.createEntity();
        user.setLogin(login);
        user.setEmail(login + "@example.com");
        user.setActivated(true);
        user.setAuthorities(new HashSet<>(Set.of(authorityRepository.findById(authority).orElseThrow())));
        insertedUsers.add(userRepository.save(user));

        UserProfile profile = UserProfileResourceIT.createEntity();
        profile.setDocumentNumber(documentNumber);
        profile.setDocumentType(documentType);
        profile.setUser(user);
        insertedProfiles.add(userProfileRepository.save(profile));
        return profile;
    }

    private Trimester persistTrimester(String name, LocalDate startDate, LocalDate endDate) {
        Trimester persistedTrimester = new Trimester().name(name).startDate(startDate).endDate(endDate).status(StateTrimester.ACTIVO);
        insertedTrimesters.add(trimesterRepository.save(persistedTrimester));
        return persistedTrimester;
    }

    private Grade persistGrade(String code, LocalDate startDate, LocalDate endDate) {
        Program program = programRepository.save(ProgramResourceIT.createEntity());
        insertedPrograms.add(program);
        Modality modality = modalityRepository.save(ModalityResourceIT.createEntity());
        insertedModalities.add(modality);
        TimeSlot timeSlot = timeSlotRepository.save(TimeSlotResourceIT.createEntity());
        insertedTimeSlots.add(timeSlot);

        Grade persistedGrade = GradeResourceIT.createEntity();
        persistedGrade.setCode(code);
        persistedGrade.setStartDate(startDate);
        persistedGrade.setEndDate(endDate);
        persistedGrade.setProgram(program);
        persistedGrade.setModality(modality);
        persistedGrade.setTimeSlot(timeSlot);
        insertedGrades.add(gradeRepository.save(persistedGrade));
        return persistedGrade;
    }

    private ClassSection persistClassSection(String subjectName, Grade grade, UserProfile instructor) {
        ClassSection persistedClassSection = new ClassSection().subjectName(subjectName).isActive(true).grade(grade).instructor(instructor);
        insertedClassSections.add(classSectionRepository.save(persistedClassSection));
        return persistedClassSection;
    }

    private Apprentice persistEnrollment(UserProfile student, Grade grade, StateAcademic stateAcademic) {
        Apprentice apprentice = new Apprentice().student(student).grade(grade).stateAcademic(stateAcademic);
        insertedApprentices.add(apprenticeRepository.save(apprentice));
        return apprentice;
    }
}
