package com.mycompany.senaattendance.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.Apprentice;
import com.mycompany.senaattendance.domain.DocumentType;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.StateAcademic;
import com.mycompany.senaattendance.domain.enumeration.StateGrade;
import com.mycompany.senaattendance.repository.ApprenticeRepository;
import com.mycompany.senaattendance.repository.AuthorityRepository;
import com.mycompany.senaattendance.repository.DocumentTypeRepository;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
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

    @AfterEach
    void cleanup() {
        // Sweep any enrollment created through the API for the seeded fichas, even when a test
        // failed before tracking it.
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
        UserProfile student = persistApprenticeProfile(DEFAULT_DOCUMENT_NUMBER, true);
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
            .andExpect(jsonPath("$.[*].grade.id").value(hasItem(grade.getId())));
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
}
