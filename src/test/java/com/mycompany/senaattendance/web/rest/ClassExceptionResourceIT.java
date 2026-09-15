package com.mycompany.senaattendance.web.rest;

import static com.mycompany.senaattendance.domain.ClassExceptionAsserts.*;
import static com.mycompany.senaattendance.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.ClassException;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.DocumentType;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.Modality;
import com.mycompany.senaattendance.domain.Program;
import com.mycompany.senaattendance.domain.TimeSlot;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.repository.AuthorityRepository;
import com.mycompany.senaattendance.repository.ClassExceptionRepository;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.DocumentTypeRepository;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.repository.ModalityRepository;
import com.mycompany.senaattendance.repository.ProgramRepository;
import com.mycompany.senaattendance.repository.TimeSlotRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.dto.ClassExceptionDTO;
import com.mycompany.senaattendance.service.mapper.ClassExceptionMapper;
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
 * Integration tests for the {@link ClassExceptionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class ClassExceptionResourceIT {

    private static final LocalDate DEFAULT_DATE = LocalDate.now();
    private static final LocalDate UPDATED_DATE = LocalDate.now().plusDays(1);
    private static final LocalDate PAST_DATE = LocalDate.now().minusDays(1);

    private static final String DEFAULT_REASON = "AAAAAAAAAA";
    private static final String UPDATED_REASON = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/class-exceptions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final String INSTRUCTOR_LOGIN = "exception_instructor";
    private static final String OTHER_INSTRUCTOR_LOGIN = "other_exception_instructor";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ClassExceptionRepository classExceptionRepository;

    @Autowired
    private ClassSectionRepository classSectionRepository;

    @Autowired
    private ClassExceptionMapper classExceptionMapper;

    @Autowired
    private MockMvc restClassExceptionMockMvc;

    @Autowired
    private AuthorityRepository authorityRepository;

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
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Clock clock;

    private final List<ClassSection> insertedClassSections = new ArrayList<>();

    private final List<Grade> insertedGrades = new ArrayList<>();

    private final List<Program> insertedPrograms = new ArrayList<>();

    private final List<Modality> insertedModalities = new ArrayList<>();

    private final List<TimeSlot> insertedTimeSlots = new ArrayList<>();

    private final List<UserProfile> insertedProfiles = new ArrayList<>();

    private final List<User> insertedUsers = new ArrayList<>();

    private final List<DocumentType> insertedDocumentTypes = new ArrayList<>();

    private DocumentType documentType;

    private UserProfile instructor;

    private UserProfile otherInstructor;

    private ClassSection classSection;

    private ClassSection otherClassSection;

    private ClassException classException;

    private ClassException insertedClassException;

    /**
     * Persists the fixture graph the tests need: two instructors, one ficha and one materia per
     * instructor, so the ownership and scope rules can be exercised.
     */
    @BeforeEach
    void initTest() {
        documentType = persistDocumentType();
        instructor = persistProfile(INSTRUCTOR_LOGIN, "1100000001", AuthoritiesConstants.INSTRUCTOR);
        otherInstructor = persistProfile(OTHER_INSTRUCTOR_LOGIN, "1100000002", AuthoritiesConstants.INSTRUCTOR);
        Grade grade = persistGrade("EXC-001");
        classSection = persistClassSection("Materia con excepciones", grade, instructor);
        otherClassSection = persistClassSection("Otra materia con excepciones", grade, otherInstructor);

        classException = new ClassException().date(DEFAULT_DATE).reason(DEFAULT_REASON).classSection(classSection);
    }

    @AfterEach
    void cleanup() {
        classExceptionRepository.deleteAll();
        insertedClassSections.forEach(classSectionRepository::delete);
        insertedClassSections.clear();
        insertedGrades.forEach(gradeRepository::delete);
        insertedGrades.clear();
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
        insertedClassException = null;
    }

    @Test
    void createClassException() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ClassException
        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(classException);
        var returnedClassExceptionDTO = om.readValue(
            restClassExceptionMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classExceptionDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ClassExceptionDTO.class
        );

        // Validate the ClassException in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedClassException = classExceptionMapper.toEntity(returnedClassExceptionDTO);
        assertClassExceptionUpdatableFieldsEquals(returnedClassException, getPersistedClassException(returnedClassException));

        insertedClassException = returnedClassException;
    }

    @Test
    void createClassExceptionWithExistingId() throws Exception {
        // Create the ClassException with an existing ID
        classException.setId("existing_id");
        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(classException);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restClassExceptionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ClassException in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        classException.setDate(null);

        // Create the ClassException, which fails.
        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(classException);

        restClassExceptionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkReasonIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        classException.setReason(null);

        // Create the ClassException, which fails.
        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(classException);

        restClassExceptionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllClassExceptions() throws Exception {
        // Initialize the database
        insertedClassException = classExceptionRepository.save(classException);

        // Get all the classExceptionList
        restClassExceptionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(classException.getId())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))
            .andExpect(jsonPath("$.[*].reason").value(hasItem(DEFAULT_REASON)));
    }

    @Test
    void getClassException() throws Exception {
        // Initialize the database
        insertedClassException = classExceptionRepository.save(classException);

        // Get the classException
        restClassExceptionMockMvc
            .perform(get(ENTITY_API_URL_ID, classException.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(classException.getId()))
            .andExpect(jsonPath("$.date").value(DEFAULT_DATE.toString()))
            .andExpect(jsonPath("$.reason").value(DEFAULT_REASON));
    }

    @Test
    void getNonExistingClassException() throws Exception {
        // Get the classException
        restClassExceptionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingClassException() throws Exception {
        // Initialize the database
        insertedClassException = classExceptionRepository.save(classException);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the classException
        ClassException updatedClassException = classExceptionRepository.findById(classException.getId()).orElseThrow();
        updatedClassException.date(UPDATED_DATE).reason(UPDATED_REASON);
        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(updatedClassException);

        restClassExceptionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isOk());

        // Validate the ClassException in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedClassExceptionToMatchAllProperties(updatedClassException);
    }

    @Test
    void putNonExistingClassException() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classException.setId(UUID.randomUUID().toString());

        // Create the ClassException
        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(classException);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restClassExceptionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ClassException in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putClassExceptionWithoutIdReturnsBadRequest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classException.setId(null);

        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(classException);

        restClassExceptionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.idnull"));

        // Validate the ClassException in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateClassExceptionWithPatch() throws Exception {
        // Initialize the database
        insertedClassException = classExceptionRepository.save(classException);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the classException using partial update
        ClassException partialUpdatedClassException = new ClassException();
        partialUpdatedClassException.setId(classException.getId());

        partialUpdatedClassException.reason(UPDATED_REASON);

        restClassExceptionMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedClassException))
            )
            .andExpect(status().isOk());

        // Validate the ClassException in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertClassExceptionUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedClassException, classException),
            getPersistedClassException(classException)
        );
    }

    @Test
    void fullUpdateClassExceptionWithPatch() throws Exception {
        // Initialize the database
        insertedClassException = classExceptionRepository.save(classException);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the classException using partial update
        ClassException partialUpdatedClassException = new ClassException();
        partialUpdatedClassException.setId(classException.getId());

        partialUpdatedClassException.date(UPDATED_DATE).reason(UPDATED_REASON);

        restClassExceptionMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedClassException))
            )
            .andExpect(status().isOk());

        // Validate the ClassException in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertClassExceptionUpdatableFieldsEquals(partialUpdatedClassException, getPersistedClassException(partialUpdatedClassException));
    }

    @Test
    void patchNonExistingClassException() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classException.setId(UUID.randomUUID().toString());

        // Create the ClassException
        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(classException);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restClassExceptionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ClassException in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchClassExceptionWithoutIdReturnsBadRequest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classException.setId(null);

        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(classException);

        restClassExceptionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.idnull"));

        // Validate the ClassException in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteClassException() throws Exception {
        // Initialize the database
        insertedClassException = classExceptionRepository.save(classException);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the classException
        restClassExceptionMockMvc
            .perform(delete(ENTITY_API_URL_ID, classException.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    // -----------------------------------------------------------------
    // Authorization: writes are restricted to admins and own instructors
    // -----------------------------------------------------------------

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.APPRENTICE)
    void createClassExceptionAsNonAdminReturnsForbidden() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(classException);

        restClassExceptionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isForbidden());

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.APPRENTICE)
    void updateClassExceptionAsNonAdminReturnsForbidden() throws Exception {
        insertedClassException = classExceptionRepository.save(classException);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(insertedClassException);

        restClassExceptionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isForbidden());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.APPRENTICE)
    void partialUpdateClassExceptionAsNonAdminReturnsForbidden() throws Exception {
        insertedClassException = classExceptionRepository.save(classException);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(insertedClassException);

        restClassExceptionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isForbidden());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.APPRENTICE)
    void deleteClassExceptionAsNonAdminReturnsForbidden() throws Exception {
        insertedClassException = classExceptionRepository.save(classException);

        long databaseSizeBeforeDelete = getRepositoryCount();

        restClassExceptionMockMvc
            .perform(delete(ENTITY_API_URL_ID, insertedClassException.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());

        assertSameRepositoryCount(databaseSizeBeforeDelete);
    }

    // -----------------------------------------------------------------
    // A4 — The instructor manages the non-teaching dates of their own class sections
    // -----------------------------------------------------------------

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void createClassExceptionAsInstructorOfTheClassSection() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(classException);

        restClassExceptionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.date").value(DEFAULT_DATE.toString()))
            .andExpect(jsonPath("$.classSection.id").value(classSection.getId()));

        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void createClassExceptionOfAnotherInstructorsClassSectionReturnsBadRequest() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();

        restClassExceptionMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(classExceptionPayload(otherClassSection.getId(), DEFAULT_DATE, DEFAULT_REASON)))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.notYourClassSection"));

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void createClassExceptionInThePastReturnsBadRequest() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();

        restClassExceptionMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(classExceptionPayload(classSection.getId(), PAST_DATE, DEFAULT_REASON)))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.pastExceptionLocked"));

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void updateClassExceptionAsInstructorOfTheClassSection() throws Exception {
        insertedClassException = classExceptionRepository.save(classException);
        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(insertedClassException);
        classExceptionDTO.setReason(UPDATED_REASON);

        restClassExceptionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reason").value(UPDATED_REASON));

        assertThat(classExceptionRepository.findById(insertedClassException.getId()).orElseThrow().getReason()).isEqualTo(UPDATED_REASON);
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void updateClassExceptionOfAnotherInstructorsClassSectionReturnsBadRequest() throws Exception {
        ClassException foreignClassException = classExceptionRepository.save(
            new ClassException().date(DEFAULT_DATE).reason(DEFAULT_REASON).classSection(otherClassSection)
        );
        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(foreignClassException);
        classExceptionDTO.setReason(UPDATED_REASON);

        restClassExceptionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.notYourClassSection"));

        assertThat(classExceptionRepository.findById(foreignClassException.getId()).orElseThrow().getReason()).isEqualTo(DEFAULT_REASON);
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void deleteClassExceptionAsInstructorOfTheClassSection() throws Exception {
        insertedClassException = classExceptionRepository.save(classException);

        restClassExceptionMockMvc
            .perform(delete(ENTITY_API_URL_ID, insertedClassException.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        assertThat(classExceptionRepository.findById(insertedClassException.getId())).isEmpty();
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void deleteClassExceptionOfAnotherInstructorsClassSectionReturnsBadRequest() throws Exception {
        ClassException foreignClassException = classExceptionRepository.save(
            new ClassException().date(DEFAULT_DATE).reason(DEFAULT_REASON).classSection(otherClassSection)
        );

        restClassExceptionMockMvc
            .perform(delete(ENTITY_API_URL_ID, foreignClassException.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.notYourClassSection"));

        assertThat(classExceptionRepository.findById(foreignClassException.getId())).isPresent();
    }

    // -----------------------------------------------------------------
    // A4 — A past non-teaching date only accepts a new reason
    // -----------------------------------------------------------------

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void updatePastClassExceptionReasonIsAllowed() throws Exception {
        ClassException pastClassException = persistPastClassException();
        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(pastClassException);
        classExceptionDTO.setReason(UPDATED_REASON);

        restClassExceptionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reason").value(UPDATED_REASON))
            .andExpect(jsonPath("$.date").value(PAST_DATE.toString()));

        ClassException reloaded = classExceptionRepository.findById(pastClassException.getId()).orElseThrow();
        assertThat(reloaded.getReason()).isEqualTo(UPDATED_REASON);
        assertThat(reloaded.getDate()).isEqualTo(PAST_DATE);
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void updatePastClassExceptionDateReturnsBadRequest() throws Exception {
        ClassException pastClassException = persistPastClassException();
        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(pastClassException);
        classExceptionDTO.setDate(DEFAULT_DATE);

        restClassExceptionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.pastExceptionLocked"));

        assertThat(classExceptionRepository.findById(pastClassException.getId()).orElseThrow().getDate()).isEqualTo(PAST_DATE);
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void partialUpdatePastClassExceptionDateReturnsBadRequest() throws Exception {
        ClassException pastClassException = persistPastClassException();

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", pastClassException.getId());
        payload.put("date", DEFAULT_DATE.toString());

        restClassExceptionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(payload)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.pastExceptionLocked"));

        assertThat(classExceptionRepository.findById(pastClassException.getId()).orElseThrow().getDate()).isEqualTo(PAST_DATE);
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void deletePastClassExceptionReturnsBadRequest() throws Exception {
        ClassException pastClassException = persistPastClassException();

        restClassExceptionMockMvc
            .perform(delete(ENTITY_API_URL_ID, pastClassException.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.pastExceptionLocked"));

        assertThat(classExceptionRepository.findById(pastClassException.getId())).isPresent();
    }

    @Test
    void deletePastClassExceptionAsAdminReturnsBadRequest() throws Exception {
        ClassException pastClassException = persistPastClassException();

        restClassExceptionMockMvc
            .perform(delete(ENTITY_API_URL_ID, pastClassException.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.pastExceptionLocked"));

        assertThat(classExceptionRepository.findById(pastClassException.getId())).isPresent();
    }

    // -----------------------------------------------------------------
    // A4 — Admin manages every class section, but the precedent still holds
    // -----------------------------------------------------------------

    @Test
    void createClassExceptionOfAnotherInstructorsClassSectionAsAdmin() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();

        restClassExceptionMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(classExceptionPayload(otherClassSection.getId(), DEFAULT_DATE, DEFAULT_REASON)))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.classSection.id").value(otherClassSection.getId()));

        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void getAllClassExceptionsAsAdminReturnsEveryException() throws Exception {
        classExceptionRepository.save(classException);
        classExceptionRepository.save(new ClassException().date(DEFAULT_DATE).reason(DEFAULT_REASON).classSection(otherClassSection));

        restClassExceptionMockMvc
            .perform(get(ENTITY_API_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "2"))
            .andExpect(jsonPath("$", hasSize(2)));
    }

    // -----------------------------------------------------------------
    // A4 — The readings are scoped to the class sections of the instructor
    // -----------------------------------------------------------------

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void getAllClassExceptionsAsInstructorReturnsOnlyOwnClassSectionExceptions() throws Exception {
        classExceptionRepository.save(classException);
        classExceptionRepository.save(new ClassException().date(DEFAULT_DATE).reason(DEFAULT_REASON).classSection(otherClassSection));

        restClassExceptionMockMvc
            .perform(get(ENTITY_API_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "1"))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].classSection.id").value(classSection.getId()));
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void getClassExceptionOfAnotherInstructorsClassSectionReturnsNotFound() throws Exception {
        ClassException foreignClassException = classExceptionRepository.save(
            new ClassException().date(DEFAULT_DATE).reason(DEFAULT_REASON).classSection(otherClassSection)
        );

        restClassExceptionMockMvc.perform(get(ENTITY_API_URL_ID, foreignClassException.getId())).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.APPRENTICE)
    void getAllClassExceptionsAsApprenticeReturnsForbidden() throws Exception {
        restClassExceptionMockMvc.perform(get(ENTITY_API_URL).accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.APPRENTICE)
    void getClassExceptionAsApprenticeReturnsForbidden() throws Exception {
        restClassExceptionMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isForbidden());
    }

    protected long getRepositoryCount() {
        return classExceptionRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected ClassException getPersistedClassException(ClassException classException) {
        return classExceptionRepository.findById(classException.getId()).orElseThrow();
    }

    protected void assertPersistedClassExceptionToMatchAllProperties(ClassException expectedClassException) {
        assertClassExceptionAllPropertiesEquals(expectedClassException, getPersistedClassException(expectedClassException));
    }

    protected void assertPersistedClassExceptionToMatchUpdatableProperties(ClassException expectedClassException) {
        assertClassExceptionAllUpdatablePropertiesEquals(expectedClassException, getPersistedClassException(expectedClassException));
    }

    // -----------------------------------------------------------------
    // Fixture helpers
    // -----------------------------------------------------------------

    private ClassException persistPastClassException() {
        return classExceptionRepository.save(new ClassException().date(PAST_DATE).reason(DEFAULT_REASON).classSection(classSection));
    }

    private static Map<String, Object> classExceptionPayload(String classSectionId, LocalDate date, String reason) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("date", date.toString());
        payload.put("reason", reason);
        payload.put("classSection", Map.of("id", classSectionId));
        return payload;
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

    private Grade persistGrade(String code) {
        Program program = programRepository.save(ProgramResourceIT.createEntity());
        insertedPrograms.add(program);
        Modality modality = modalityRepository.save(ModalityResourceIT.createEntity());
        insertedModalities.add(modality);
        TimeSlot timeSlot = timeSlotRepository.save(TimeSlotResourceIT.createEntity());
        insertedTimeSlots.add(timeSlot);

        Grade persistedGrade = GradeResourceIT.createEntity();
        persistedGrade.setCode(code);
        persistedGrade.setStartDate(LocalDate.now(clock).minusDays(30));
        persistedGrade.setEndDate(LocalDate.now(clock).plusDays(30));
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
}
