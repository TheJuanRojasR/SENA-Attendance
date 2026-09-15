package com.mycompany.senaattendance.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.Apprentice;
import com.mycompany.senaattendance.domain.Attendance;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.DocumentType;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.StateAcademic;
import com.mycompany.senaattendance.domain.enumeration.StateAttendance;
import com.mycompany.senaattendance.domain.enumeration.StateGrade;
import com.mycompany.senaattendance.repository.ApprenticeRepository;
import com.mycompany.senaattendance.repository.AttendanceRepository;
import com.mycompany.senaattendance.repository.AuthorityRepository;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.DocumentTypeRepository;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the {@link ApprenticeResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class ApprenticeResourceIT {

    private static final String DEFAULT_DOCUMENT_NUMBER = "1000000001";
    private static final String UNKNOWN_DOCUMENT_NUMBER = "9999999999";
    private static final String INVALID_DOCUMENT_NUMBER = "100ABC";

    private static final String ENTITY_API_URL = "/api/apprentices";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ApprenticeRepository apprenticeRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private ClassSectionRepository classSectionRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private MockMvc restApprenticeMockMvc;

    private final List<User> insertedUsers = new ArrayList<>();

    private final List<UserProfile> insertedProfiles = new ArrayList<>();

    private final List<DocumentType> insertedDocumentTypes = new ArrayList<>();

    private final List<Grade> insertedGrades = new ArrayList<>();

    private final List<Apprentice> insertedApprentices = new ArrayList<>();

    private final List<ClassSection> insertedClassSections = new ArrayList<>();

    private final List<Attendance> insertedAttendances = new ArrayList<>();

    @AfterEach
    void cleanup() {
        // Sweep any enrollment created through the API for the seeded fichas, even when a test
        // failed before tracking it.
        insertedAttendances.forEach(attendanceRepository::delete);
        insertedAttendances.clear();
        insertedClassSections.forEach(classSectionRepository::delete);
        insertedClassSections.clear();
        insertedGrades.forEach(grade -> apprenticeRepository.deleteAll(apprenticeRepository.findByGradeId(grade.getId())));
        insertedApprentices.forEach(apprenticeRepository::delete);
        insertedApprentices.clear();
        insertedGrades.forEach(gradeRepository::delete);
        insertedGrades.clear();
        insertedProfiles.forEach(userProfileRepository::delete);
        insertedProfiles.clear();
        insertedDocumentTypes.forEach(documentTypeRepository::delete);
        insertedDocumentTypes.clear();
        insertedUsers.forEach(userRepository::delete);
        insertedUsers.clear();
    }

    /**
     * Persists a profile backed by a real user account, so the enrollment can resolve both the
     * profile by document number and the account activation state. The seeded user carries the
     * apprentice role assigned by self-registration (UC001).
     *
     * @param documentNumber the document number that identifies the apprentice.
     * @param activated whether the apprentice account is active.
     * @return the persisted apprentice profile.
     */
    private UserProfile persistApprenticeProfile(String documentNumber, boolean activated) {
        return persistApprenticeProfile(documentNumber, activated, UserProfileResourceIT.createEntity().getDocumentType());
    }

    /**
     * Persists a profile backed by a real user account with an explicit document type, so two
     * profiles can share a document number under the (documentType, documentNumber) unique key.
     *
     * @param documentNumber the document number that identifies the apprentice.
     * @param activated whether the apprentice account is active.
     * @param documentType the document type the profile is issued with.
     * @return the persisted apprentice profile.
     */
    private UserProfile persistApprenticeProfile(String documentNumber, boolean activated, DocumentType documentType) {
        String suffix = UUID.randomUUID().toString().replace("-", "");

        User user = UserResourceIT.createEntity();
        user.setLogin("apprentice_" + suffix);
        user.setEmail("apprentice_" + suffix + "@example.com");
        user.setActivated(activated);
        user.setAuthorities(new HashSet<>(Set.of(authorityRepository.findById(AuthoritiesConstants.APPRENTICE).orElseThrow())));
        insertedUsers.add(userRepository.save(user));

        UserProfile profile = UserProfileResourceIT.createEntity();
        profile.setDocumentType(documentType);
        profile.setDocumentNumber(documentNumber);
        profile.setUser(user);
        insertedProfiles.add(userProfileRepository.save(profile));
        return profile;
    }

    /**
     * Persists an activated apprentice profile with an explicit name, so the list text filters
     * can be exercised by name.
     *
     * @param documentNumber the document number that identifies the apprentice.
     * @param firstName the apprentice first name.
     * @param firstLastName the apprentice first last name.
     * @return the persisted apprentice profile.
     */
    private UserProfile persistApprenticeProfile(String documentNumber, String firstName, String firstLastName) {
        UserProfile profile = persistApprenticeProfile(documentNumber, true);
        profile.setFirstName(firstName);
        profile.setFirstLastName(firstLastName);
        return userProfileRepository.save(profile);
    }

    /**
     * Persists a document type so a profile can reference a real one.
     *
     * @return the persisted document type.
     */
    private DocumentType persistDocumentType() {
        DocumentType documentType = documentTypeRepository.save(DocumentTypeResourceIT.createEntity());
        insertedDocumentTypes.add(documentType);
        return documentType;
    }

    /**
     * Persists a ficha in the given state. The enrollment rules only accept PENDIENTE and ACTIVA.
     *
     * @param code the ficha code.
     * @param state the state to persist.
     * @return the persisted ficha.
     */
    private Grade persistGrade(String code, StateGrade state) {
        Grade grade = GradeResourceIT.createEntity();
        grade.setCode(code);
        grade.setState(state);
        insertedGrades.add(gradeRepository.save(grade));
        return grade;
    }

    /**
     * Persists an existing enrollment, used to exercise the no-reentry rule.
     *
     * @param student the enrolled apprentice.
     * @param grade the ficha.
     * @param state the academic state to persist.
     * @return the persisted enrollment.
     */
    private Apprentice persistEnrollment(UserProfile student, Grade grade, StateAcademic state) {
        Apprentice apprentice = new Apprentice().stateAcademic(state).student(student).grade(grade);
        insertedApprentices.add(apprenticeRepository.save(apprentice));
        return apprentice;
    }

    /**
     * Builds the enrollment request body, with the nested ficha shape already used by the
     * apprentices contract.
     *
     * @param documentNumber the apprentice document number.
     * @param gradeId the target ficha id.
     * @return the request body.
     */
    private Map<String, Object> enrollPayload(String documentNumber, String gradeId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("documentNumber", documentNumber);
        payload.put("grade", Map.of("id", gradeId));
        return payload;
    }

    /**
     * Persists a class section inside the ficha, the container of the attendance records that
     * decide whether the unlink deletes or keeps the enrollment.
     *
     * @param grade the ficha the class section belongs to.
     * @return the persisted class section.
     */
    private ClassSection persistClassSection(Grade grade) {
        ClassSection classSection = new ClassSection().subjectName("Materia de prueba").isActive(true).grade(grade);
        insertedClassSections.add(classSectionRepository.save(classSection));
        return classSection;
    }

    /**
     * Persists an attendance record linking the apprentice to a class section of the ficha.
     *
     * @param student the apprentice profile that attended.
     * @param classSection the class section where the attendance was recorded.
     * @return the persisted attendance.
     */
    private Attendance persistAttendance(UserProfile student, ClassSection classSection) {
        Attendance attendance = new Attendance()
            .date(LocalDate.now())
            .stateAttendance(StateAttendance.PRESENTE)
            .student(student)
            .classSection(classSection);
        insertedAttendances.add(attendanceRepository.save(attendance));
        return attendance;
    }

    /**
     * Builds the unlink request body, with the enrollment id and the withdrawal reason.
     *
     * @param apprenticeId the enrollment id.
     * @param reason the academic state chosen as the withdrawal reason.
     * @return the request body.
     */
    private Map<String, Object> unlinkPayload(String apprenticeId, StateAcademic reason) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", apprenticeId);
        payload.put("reason", reason.name());
        return payload;
    }

    @Test
    void enrollApprentice() throws Exception {
        UserProfile student = persistApprenticeProfile(DEFAULT_DOCUMENT_NUMBER, true);
        Grade grade = persistGrade("UC00801", StateGrade.ACTIVA);

        long databaseSizeBeforeCreate = apprenticeRepository.count();

        Map<String, Object> payload = enrollPayload(DEFAULT_DOCUMENT_NUMBER, grade.getId());
        // A state sent by the client is ignored: the server always enrolls as MATRICULADO.
        payload.put("stateAcademic", StateAcademic.CANCELADO.name());

        restApprenticeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(payload)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.stateAcademic").value(StateAcademic.MATRICULADO.name()))
            .andExpect(jsonPath("$.student.documentNumber").value(DEFAULT_DOCUMENT_NUMBER))
            .andExpect(jsonPath("$.grade.id").value(grade.getId()));

        assertThat(apprenticeRepository.count()).isEqualTo(databaseSizeBeforeCreate + 1);

        Apprentice persisted = apprenticeRepository.findByGradeId(grade.getId()).get(0);
        insertedApprentices.add(persisted);
        assertThat(persisted.getStateAcademic()).isEqualTo(StateAcademic.MATRICULADO);
        assertThat(persisted.getStudent().getId()).isEqualTo(student.getId());
        assertThat(persisted.getGrade().getId()).isEqualTo(grade.getId());
    }

    @Test
    void enrollNonExistingApprenticeIsRejected() throws Exception {
        Grade grade = persistGrade("UC00802", StateGrade.ACTIVA);

        long databaseSizeBeforeCreate = apprenticeRepository.count();

        restApprenticeMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(enrollPayload(UNKNOWN_DOCUMENT_NUMBER, grade.getId())))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.apprenticeInactive"));

        assertThat(apprenticeRepository.count()).isEqualTo(databaseSizeBeforeCreate);
    }

    @Test
    void enrollInactiveApprenticeIsRejected() throws Exception {
        persistApprenticeProfile(DEFAULT_DOCUMENT_NUMBER, false);
        Grade grade = persistGrade("UC00803", StateGrade.ACTIVA);

        long databaseSizeBeforeCreate = apprenticeRepository.count();

        restApprenticeMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(enrollPayload(DEFAULT_DOCUMENT_NUMBER, grade.getId())))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.apprenticeInactive"));

        assertThat(apprenticeRepository.count()).isEqualTo(databaseSizeBeforeCreate);
    }

    @Test
    void enrollWithAmbiguousDocumentNumberIsRejected() throws Exception {
        // The same number is valid across document types, so it does not identify a single
        // apprentice: the enrollment must reject it instead of failing with a server error.
        persistApprenticeProfile(DEFAULT_DOCUMENT_NUMBER, true, persistDocumentType());
        persistApprenticeProfile(DEFAULT_DOCUMENT_NUMBER, true, persistDocumentType());
        Grade grade = persistGrade("UC00810", StateGrade.ACTIVA);

        long databaseSizeBeforeCreate = apprenticeRepository.count();

        restApprenticeMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(enrollPayload(DEFAULT_DOCUMENT_NUMBER, grade.getId())))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.apprenticeInactive"));

        assertThat(apprenticeRepository.count()).isEqualTo(databaseSizeBeforeCreate);
    }

    @Test
    void enrollAlreadyMatriculadoApprenticeIsRejected() throws Exception {
        UserProfile student = persistApprenticeProfile(DEFAULT_DOCUMENT_NUMBER, true);
        Grade grade = persistGrade("UC00804", StateGrade.ACTIVA);
        persistEnrollment(student, grade, StateAcademic.MATRICULADO);

        long databaseSizeBeforeCreate = apprenticeRepository.count();

        restApprenticeMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(enrollPayload(DEFAULT_DOCUMENT_NUMBER, grade.getId())))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.apprenticeAlreadyEnrolled"));

        assertThat(apprenticeRepository.count()).isEqualTo(databaseSizeBeforeCreate);
    }

    @Test
    void enrollPreviouslyUnenrolledApprenticeIsRejected() throws Exception {
        UserProfile student = persistApprenticeProfile(DEFAULT_DOCUMENT_NUMBER, true);
        Grade grade = persistGrade("UC00805", StateGrade.ACTIVA);
        // A withdrawn apprentice cannot rejoin the same ficha, whatever the withdrawal reason.
        persistEnrollment(student, grade, StateAcademic.RETIRO_VOLUNTARIO);

        long databaseSizeBeforeCreate = apprenticeRepository.count();

        restApprenticeMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(enrollPayload(DEFAULT_DOCUMENT_NUMBER, grade.getId())))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.apprenticeAlreadyEnrolled"));

        assertThat(apprenticeRepository.count()).isEqualTo(databaseSizeBeforeCreate);
    }

    @ParameterizedTest
    @EnumSource(value = StateGrade.class, names = { "FINALIZADA", "APLAZADA", "CANCELADA" })
    void enrollInNonOperableGradeIsRejected(StateGrade state) throws Exception {
        persistApprenticeProfile(DEFAULT_DOCUMENT_NUMBER, true);
        Grade grade = persistGrade("UC00806", state);

        long databaseSizeBeforeCreate = apprenticeRepository.count();

        restApprenticeMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(enrollPayload(DEFAULT_DOCUMENT_NUMBER, grade.getId())))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.gradeNotOperable"));

        assertThat(apprenticeRepository.count()).isEqualTo(databaseSizeBeforeCreate);
    }

    @Test
    void enrollWithInvalidDocumentNumberIsRejected() throws Exception {
        Grade grade = persistGrade("UC00807", StateGrade.ACTIVA);

        long databaseSizeBeforeCreate = apprenticeRepository.count();

        restApprenticeMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(enrollPayload(INVALID_DOCUMENT_NUMBER, grade.getId())))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.validation"));

        assertThat(apprenticeRepository.count()).isEqualTo(databaseSizeBeforeCreate);
    }

    @Test
    void getAllApprentices() throws Exception {
        UserProfile student = persistApprenticeProfile(DEFAULT_DOCUMENT_NUMBER, "Ana Maria", "Gomez Ruiz");
        Grade grade = persistGrade("UC00808", StateGrade.ACTIVA);
        Apprentice apprentice = persistEnrollment(student, grade, StateAcademic.MATRICULADO);

        // Get all the apprenticeList
        restApprenticeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(apprentice.getId())))
            .andExpect(jsonPath("$.[*].stateAcademic").value(hasItem(StateAcademic.MATRICULADO.name())))
            .andExpect(jsonPath("$.[*].student.documentNumber").value(hasItem(DEFAULT_DOCUMENT_NUMBER)))
            .andExpect(jsonPath("$.[*].student.firstName").value(hasItem("Ana Maria")))
            .andExpect(jsonPath("$.[*].student.firstLastName").value(hasItem("Gomez Ruiz")))
            .andExpect(jsonPath("$.[*].grade.id").value(hasItem(grade.getId())));
    }

    @Test
    void getAllApprenticesFilteredByGradeId() throws Exception {
        Grade firstGrade = persistGrade("UC00820", StateGrade.ACTIVA);
        Grade secondGrade = persistGrade("UC00821", StateGrade.ACTIVA);
        UserProfile firstStudent = persistApprenticeProfile("2000000001", true);
        UserProfile secondStudent = persistApprenticeProfile("2000000002", true);
        Apprentice first = persistEnrollment(firstStudent, firstGrade, StateAcademic.MATRICULADO);
        persistEnrollment(secondStudent, secondGrade, StateAcademic.MATRICULADO);

        restApprenticeMockMvc
            .perform(get(ENTITY_API_URL).param("gradeId", firstGrade.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$.[*].id").value(hasItem(first.getId())))
            .andExpect(jsonPath("$.[*].grade.id").value(hasItem(firstGrade.getId())))
            .andExpect(header().string("X-Total-Count", "1"));
    }

    @Test
    void getAllApprenticesFilteredByDocumentNumber() throws Exception {
        UserProfile firstStudent = persistApprenticeProfile("2100000001", "Ana Maria", "Gomez");
        UserProfile secondStudent = persistApprenticeProfile("2100000002", "Carlos Andres", "Perez");
        Grade grade = persistGrade("UC00822", StateGrade.ACTIVA);
        Apprentice first = persistEnrollment(firstStudent, grade, StateAcademic.MATRICULADO);
        persistEnrollment(secondStudent, grade, StateAcademic.MATRICULADO);

        restApprenticeMockMvc
            .perform(get(ENTITY_API_URL).param("documentNumber", "2100000001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$.[*].id").value(hasItem(first.getId())))
            .andExpect(jsonPath("$.[*].student.documentNumber").value(hasItem("2100000001")))
            .andExpect(header().string("X-Total-Count", "1"));
    }

    @Test
    void getAllApprenticesFilteredByNameIsPartialAndCaseInsensitive() throws Exception {
        UserProfile firstStudent = persistApprenticeProfile("2200000001", "Maria Fernanda", "Gomez Ruiz");
        UserProfile secondStudent = persistApprenticeProfile("2200000002", "Carlos Andres", "Perez Mora");
        Grade grade = persistGrade("UC00823", StateGrade.ACTIVA);
        Apprentice first = persistEnrollment(firstStudent, grade, StateAcademic.MATRICULADO);
        persistEnrollment(secondStudent, grade, StateAcademic.MATRICULADO);

        restApprenticeMockMvc
            .perform(get(ENTITY_API_URL).param("name", "maria"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$.[*].id").value(hasItem(first.getId())))
            .andExpect(jsonPath("$.[*].student.firstName").value(hasItem("Maria Fernanda")))
            .andExpect(header().string("X-Total-Count", "1"));
    }

    @Test
    void getAllApprenticesFilteredByStateAcademic() throws Exception {
        UserProfile firstStudent = persistApprenticeProfile("2300000001", true);
        UserProfile secondStudent = persistApprenticeProfile("2300000002", true);
        Grade grade = persistGrade("UC00824", StateGrade.ACTIVA);
        Apprentice matriculado = persistEnrollment(firstStudent, grade, StateAcademic.MATRICULADO);
        persistEnrollment(secondStudent, grade, StateAcademic.RETIRO_VOLUNTARIO);

        restApprenticeMockMvc
            .perform(get(ENTITY_API_URL).param("stateAcademic", StateAcademic.MATRICULADO.name()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$.[*].id").value(hasItem(matriculado.getId())))
            .andExpect(jsonPath("$.[*].stateAcademic").value(hasItem(StateAcademic.MATRICULADO.name())))
            .andExpect(header().string("X-Total-Count", "1"));
    }

    @Test
    void getAllApprenticesFilteredByDocumentNumberAndNameCombinesBothFilters() throws Exception {
        // Both students share the name, so only the document drives them apart: a query that
        // ORs the filters would return both records.
        UserProfile firstStudent = persistApprenticeProfile("2400000001", "Pedro Jose", "Ruiz");
        UserProfile secondStudent = persistApprenticeProfile("2400000002", "Pedro Jose", "Ruiz");
        Grade grade = persistGrade("UC00825", StateGrade.ACTIVA);
        Apprentice first = persistEnrollment(firstStudent, grade, StateAcademic.MATRICULADO);
        persistEnrollment(secondStudent, grade, StateAcademic.MATRICULADO);

        restApprenticeMockMvc
            .perform(get(ENTITY_API_URL).param("documentNumber", "2400000001").param("name", "pedro"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$.[*].id").value(hasItem(first.getId())))
            .andExpect(header().string("X-Total-Count", "1"));
    }

    @Test
    void getAllApprenticesFilteredWithoutMatchesReturnsEmptyList() throws Exception {
        UserProfile student = persistApprenticeProfile("2500000001", "Maria Fernanda", "Gomez Ruiz");
        Grade grade = persistGrade("UC00826", StateGrade.ACTIVA);
        persistEnrollment(student, grade, StateAcademic.MATRICULADO);

        restApprenticeMockMvc
            .perform(get(ENTITY_API_URL).param("name", "nombre que no existe"))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"))
            .andExpect(header().string("X-Total-Count", "0"));
    }

    @Test
    void getAllApprenticesFilteredByNamePaginatesTheMatches() throws Exception {
        UserProfile firstStudent = persistApprenticeProfile("2600000001", "Paginado Uno", "Apellido");
        UserProfile secondStudent = persistApprenticeProfile("2600000002", "Paginado Dos", "Apellido");
        Grade grade = persistGrade("UC00827", StateGrade.ACTIVA);
        persistEnrollment(firstStudent, grade, StateAcademic.MATRICULADO);
        persistEnrollment(secondStudent, grade, StateAcademic.MATRICULADO);

        restApprenticeMockMvc
            .perform(get(ENTITY_API_URL).param("name", "paginado").param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(header().string("X-Total-Count", "2"));
    }

    @Test
    void getApprentice() throws Exception {
        UserProfile student = persistApprenticeProfile(DEFAULT_DOCUMENT_NUMBER, true);
        Grade grade = persistGrade("UC00809", StateGrade.ACTIVA);
        Apprentice apprentice = persistEnrollment(student, grade, StateAcademic.MATRICULADO);

        // Get the apprentice
        restApprenticeMockMvc
            .perform(get(ENTITY_API_URL_ID, apprentice.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(apprentice.getId()))
            .andExpect(jsonPath("$.stateAcademic").value(StateAcademic.MATRICULADO.name()))
            .andExpect(jsonPath("$.student.documentNumber").value(DEFAULT_DOCUMENT_NUMBER));
    }

    @Test
    void getNonExistingApprentice() throws Exception {
        // Get the apprentice
        restApprenticeMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    void unlinkApprenticeWithoutAttendanceDeletesTheRecord() throws Exception {
        UserProfile student = persistApprenticeProfile(DEFAULT_DOCUMENT_NUMBER, true);
        Grade grade = persistGrade("UC00811", StateGrade.ACTIVA);
        Apprentice apprentice = persistEnrollment(student, grade, StateAcademic.MATRICULADO);

        restApprenticeMockMvc
            .perform(
                patch(ENTITY_API_URL + "/unlinked")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(unlinkPayload(apprentice.getId(), StateAcademic.RETIRO_VOLUNTARIO)))
            )
            .andExpect(status().isNoContent());

        assertThat(apprenticeRepository.existsById(apprentice.getId())).isFalse();
    }

    @Test
    void unlinkApprenticeWithAttendanceKeepsTheRecordWithTheReason() throws Exception {
        UserProfile student = persistApprenticeProfile(DEFAULT_DOCUMENT_NUMBER, true);
        Grade grade = persistGrade("UC00812", StateGrade.ACTIVA);
        Apprentice apprentice = persistEnrollment(student, grade, StateAcademic.MATRICULADO);
        persistAttendance(student, persistClassSection(grade));

        restApprenticeMockMvc
            .perform(
                patch(ENTITY_API_URL + "/unlinked")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(unlinkPayload(apprentice.getId(), StateAcademic.APLAZADO)))
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(apprentice.getId()))
            .andExpect(jsonPath("$.stateAcademic").value(StateAcademic.APLAZADO.name()));

        Apprentice persisted = apprenticeRepository.findById(apprentice.getId()).orElseThrow();
        assertThat(persisted.getStateAcademic()).isEqualTo(StateAcademic.APLAZADO);
    }

    @Test
    void unlinkApprenticeWithInvalidReasonIsRejected() throws Exception {
        UserProfile student = persistApprenticeProfile(DEFAULT_DOCUMENT_NUMBER, true);
        Grade grade = persistGrade("UC00813", StateGrade.ACTIVA);
        Apprentice apprentice = persistEnrollment(student, grade, StateAcademic.MATRICULADO);

        restApprenticeMockMvc
            .perform(
                patch(ENTITY_API_URL + "/unlinked")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(unlinkPayload(apprentice.getId(), StateAcademic.MATRICULADO)))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.invalidunlinkreason"));

        Apprentice persisted = apprenticeRepository.findById(apprentice.getId()).orElseThrow();
        assertThat(persisted.getStateAcademic()).isEqualTo(StateAcademic.MATRICULADO);
    }

    @ParameterizedTest
    @EnumSource(value = StateGrade.class, names = { "FINALIZADA", "APLAZADA", "CANCELADA" })
    void unlinkApprenticeInNonOperableGradeIsRejected(StateGrade state) throws Exception {
        UserProfile student = persistApprenticeProfile(DEFAULT_DOCUMENT_NUMBER, true);
        Grade grade = persistGrade("UC00814", state);
        Apprentice apprentice = persistEnrollment(student, grade, StateAcademic.MATRICULADO);

        restApprenticeMockMvc
            .perform(
                patch(ENTITY_API_URL + "/unlinked")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(unlinkPayload(apprentice.getId(), StateAcademic.CANCELADO)))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.gradeNotOperable"));

        assertThat(apprenticeRepository.existsById(apprentice.getId())).isTrue();
    }

    @Test
    void unlinkNonExistingApprenticeIsRejected() throws Exception {
        restApprenticeMockMvc
            .perform(
                patch(ENTITY_API_URL + "/unlinked")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(unlinkPayload(UUID.randomUUID().toString(), StateAcademic.CANCELADO)))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.idnotfound"));
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.COORDINATOR)
    void unlinkWithNonAdminIsForbidden() throws Exception {
        restApprenticeMockMvc
            .perform(
                patch(ENTITY_API_URL + "/unlinked")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(unlinkPayload(UUID.randomUUID().toString(), StateAcademic.CANCELADO)))
            )
            .andExpect(status().isForbidden());
    }
}
