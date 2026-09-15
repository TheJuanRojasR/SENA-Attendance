package com.mycompany.senaattendance.web.rest;

import static com.mycompany.senaattendance.domain.ClassSectionAsserts.*;
import static com.mycompany.senaattendance.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.Attendance;
import com.mycompany.senaattendance.domain.ClassException;
import com.mycompany.senaattendance.domain.ClassSchedule;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.DayOfWeek;
import com.mycompany.senaattendance.domain.enumeration.StateAttendance;
import com.mycompany.senaattendance.domain.enumeration.StateGrade;
import com.mycompany.senaattendance.repository.AttendanceRepository;
import com.mycompany.senaattendance.repository.ClassExceptionRepository;
import com.mycompany.senaattendance.repository.ClassScheduleRepository;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.ClassSectionService;
import com.mycompany.senaattendance.service.dto.ClassSectionDTO;
import com.mycompany.senaattendance.service.mapper.ClassSectionMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the {@link ClassSectionResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class ClassSectionResourceIT {

    private static final String DEFAULT_SUBJECT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_SUBJECT_NAME = "BBBBBBBBBB";

    private static final Boolean DEFAULT_IS_ACTIVE = false;
    private static final Boolean UPDATED_IS_ACTIVE = true;

    private static final String ACTIVE_INSTRUCTOR_ID = "active-instructor";
    private static final String INACTIVE_INSTRUCTOR_ID = "inactive-instructor";

    private static final String ENTITY_API_URL = "/api/class-sections";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ClassSectionRepository classSectionRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private ClassScheduleRepository classScheduleRepository;

    @Autowired
    private ClassExceptionRepository classExceptionRepository;

    @Mock
    private ClassSectionRepository classSectionRepositoryMock;

    @Autowired
    private ClassSectionMapper classSectionMapper;

    @Mock
    private ClassSectionService classSectionServiceMock;

    @Autowired
    private MockMvc restClassSectionMockMvc;

    private ClassSection classSection;

    private ClassSection insertedClassSection;

    private Attendance insertedAttendance;

    private ClassSchedule insertedSchedule;

    private ClassException insertedException;

    private final List<User> insertedInstructorUsers = new ArrayList<>();

    private final List<ClassSection> extraInsertedClassSections = new ArrayList<>();

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ClassSection createEntity() {
        ClassSection classSection = new ClassSection().subjectName(DEFAULT_SUBJECT_NAME).isActive(DEFAULT_IS_ACTIVE);
        // Add required entity
        Grade grade;
        grade = GradeResourceIT.createEntity();
        grade.setId("fixed-id-for-tests");
        classSection.setGrade(grade);
        return classSection;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ClassSection createUpdatedEntity() {
        ClassSection updatedClassSection = new ClassSection().subjectName(UPDATED_SUBJECT_NAME).isActive(UPDATED_IS_ACTIVE);
        // Add required entity
        Grade grade;
        grade = GradeResourceIT.createUpdatedEntity();
        grade.setId("fixed-id-for-tests");
        updatedClassSection.setGrade(grade);
        return updatedClassSection;
    }

    /**
     * Persists an instructor profile backed by a user account with the requested activation
     * state. The service validates that an assigned instructor exists and is active, so the
     * profile needs a real user document behind it.
     *
     * @param id the user and profile id to use.
     * @param activated whether the instructor account is active.
     * @return the persisted instructor profile.
     */
    private UserProfile persistInstructor(String id, boolean activated) {
        User user = UserResourceIT.createEntity();
        user.setId(id);
        user.setLogin("instructor_" + id);
        user.setEmail(id + "@example.com");
        user.setActivated(activated);
        insertedInstructorUsers.add(userRepository.save(user));

        UserProfile instructor = UserProfileResourceIT.createEntity();
        instructor.setId(id);
        instructor.setUser(user);
        // Keep the (documentType, documentNumber) compound index unique across seeded profiles.
        instructor.setDocumentNumber(UUID.randomUUID().toString().replace("-", "").substring(0, 15));
        return userProfileRepository.save(instructor);
    }

    /**
     * Builds an instructor profile that is never persisted, so it resolves as non-existing.
     *
     * @return the unattached instructor profile.
     */
    private UserProfile nonExistingInstructor() {
        UserProfile instructor = UserProfileResourceIT.createEntity();
        instructor.setId(UUID.randomUUID().toString());
        instructor.setDocumentNumber(UUID.randomUUID().toString().replace("-", "").substring(0, 15));
        return instructor;
    }

    /**
     * Persists a class section with the given name inside the given ficha and registers it for
     * cleanup. Used to seed duplicates without going through the REST validation.
     *
     * @param subjectName the subject name to store.
     * @param grade the ficha the class section belongs to.
     * @return the persisted class section.
     */
    private ClassSection persistClassSectionInGrade(String subjectName, Grade grade) {
        ClassSection persisted = classSectionRepository.save(
            new ClassSection().subjectName(subjectName).isActive(DEFAULT_IS_ACTIVE).grade(grade)
        );
        extraInsertedClassSections.add(persisted);
        return persisted;
    }

    /**
     * Persists a ficha in the given state, used to exercise the rule that only PENDIENTE and
     * ACTIVA fichas accept class section writes. The seeded ficha is removed by the grade
     * cleanup of {@link #cleanup()}.
     *
     * @param id the ficha id to use.
     * @param state the state to persist.
     * @return the persisted ficha.
     */
    private Grade persistGradeWithState(String id, StateGrade state) {
        Grade grade = GradeResourceIT.createEntity();
        grade.setId(id);
        grade.setState(state);
        return gradeRepository.save(grade);
    }

    /**
     * @param state a state that must reject class section writes.
     * @return a stable ficha id for the seeded state.
     */
    private String nonOperableGradeId(StateGrade state) {
        return "non-operable-" + state.name().toLowerCase(Locale.ROOT);
    }

    /**
     * @param state a state that must accept class section writes.
     * @return a stable ficha id for the seeded state.
     */
    private String operableGradeId(StateGrade state) {
        return "operable-" + state.name().toLowerCase(Locale.ROOT);
    }

    @BeforeEach
    void initTest() {
        classSection = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedClassSection != null) {
            classSectionRepository.delete(insertedClassSection);
            insertedClassSection = null;
        }
        // Remove the extra sections seeded by the uniqueness tests
        extraInsertedClassSections.forEach(classSectionRepository::delete);
        extraInsertedClassSections.clear();
        // Remove the documents seeded for the delete guard and cascade tests
        if (insertedAttendance != null) {
            attendanceRepository.delete(insertedAttendance);
            insertedAttendance = null;
        }
        if (insertedSchedule != null) {
            classScheduleRepository.delete(insertedSchedule);
            insertedSchedule = null;
        }
        if (insertedException != null) {
            classExceptionRepository.delete(insertedException);
            insertedException = null;
        }
        // Remove the related documents persisted for the PUT tests
        gradeRepository.deleteAll();
        userProfileRepository.deleteAll();
        // Remove the user accounts seeded as instructors
        insertedInstructorUsers.forEach(userRepository::delete);
        insertedInstructorUsers.clear();
    }

    @Test
    void createClassSectionWithoutInstructor() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ClassSection without an instructor, which is optional
        assertThat(classSection.getInstructor()).isNull();
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(classSection);
        var returnedClassSectionDTO = om.readValue(
            restClassSectionMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classSectionDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ClassSectionDTO.class
        );

        // Validate the ClassSection in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertThat(returnedClassSectionDTO.getInstructor()).isNull();
        var returnedClassSection = classSectionMapper.toEntity(returnedClassSectionDTO);
        assertClassSectionUpdatableFieldsEquals(returnedClassSection, getPersistedClassSection(returnedClassSection));
        assertThat(getPersistedClassSection(returnedClassSection).getInstructor()).isNull();

        insertedClassSection = returnedClassSection;
    }

    @Test
    void createClassSectionWithActiveInstructor() throws Exception {
        classSection.setInstructor(persistInstructor(ACTIVE_INSTRUCTOR_ID, true));

        long databaseSizeBeforeCreate = getRepositoryCount();

        // Create the ClassSection with an existing and active instructor
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(classSection);
        var returnedClassSectionDTO = om.readValue(
            restClassSectionMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classSectionDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ClassSectionDTO.class
        );

        // Validate the ClassSection in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertThat(returnedClassSectionDTO.getInstructor().getId()).isEqualTo(ACTIVE_INSTRUCTOR_ID);
        insertedClassSection = classSectionMapper.toEntity(returnedClassSectionDTO);
        assertThat(getPersistedClassSection(insertedClassSection).getInstructor().getId()).isEqualTo(ACTIVE_INSTRUCTOR_ID);
    }

    @Test
    void createClassSectionWithNonExistingInstructor() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        classSection.setInstructor(nonExistingInstructor());

        // Create the ClassSection, which fails because the instructor does not exist
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(classSection);

        restClassSectionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classSectionDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.instructorInactive"));

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void createClassSectionWithInactiveInstructor() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        classSection.setInstructor(persistInstructor(INACTIVE_INSTRUCTOR_ID, false));

        // Create the ClassSection, which fails because the instructor account is deactivated
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(classSection);

        restClassSectionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classSectionDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.instructorInactive"));

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void createClassSectionWithDuplicateNameInSameGrade() throws Exception {
        // Seed an existing section with the default name in the same ficha
        persistClassSectionInGrade(DEFAULT_SUBJECT_NAME, classSection.getGrade());

        long databaseSizeBeforeCreate = getRepositoryCount();

        // Create another section with the same name in the same ficha, which fails
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(classSection);

        restClassSectionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classSectionDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.classSectionNameAlreadyUsed"));

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void createClassSectionWithSameNameInAnotherGrade() throws Exception {
        // The name is taken in a different ficha, which must not block this create
        persistClassSectionInGrade(DEFAULT_SUBJECT_NAME, classSection.getGrade());

        Grade otherGrade = GradeResourceIT.createEntity();
        otherGrade.setId("other-fixed-id-for-tests");
        classSection.setGrade(otherGrade);

        long databaseSizeBeforeCreate = getRepositoryCount();

        var returnedClassSectionDTO = om.readValue(
            restClassSectionMockMvc
                .perform(
                    post(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(classSectionMapper.toDto(classSection)))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ClassSectionDTO.class
        );

        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        extraInsertedClassSections.add(classSectionMapper.toEntity(returnedClassSectionDTO));
    }

    @Test
    void createClassSectionWithDifferentCaseDuplicateNameInSameGrade() throws Exception {
        persistClassSectionInGrade(DEFAULT_SUBJECT_NAME, classSection.getGrade());
        classSection.setSubjectName(DEFAULT_SUBJECT_NAME.toLowerCase(Locale.ROOT));

        long databaseSizeBeforeCreate = getRepositoryCount();
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(classSection);

        restClassSectionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classSectionDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.classSectionNameAlreadyUsed"));

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void createClassSectionWithSurroundingSpacesDuplicateNameInSameGrade() throws Exception {
        persistClassSectionInGrade(DEFAULT_SUBJECT_NAME, classSection.getGrade());
        classSection.setSubjectName("  " + DEFAULT_SUBJECT_NAME + "  ");

        long databaseSizeBeforeCreate = getRepositoryCount();
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(classSection);

        restClassSectionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classSectionDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.classSectionNameAlreadyUsed"));

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void createClassSectionWithExistingId() throws Exception {
        // Create the ClassSection with an existing ID
        classSection.setId("existing_id");
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(classSection);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restClassSectionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classSectionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ClassSection in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkSubjectNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        classSection.setSubjectName(null);

        // Create the ClassSection, which fails.
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(classSection);

        restClassSectionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classSectionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkIsActiveIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        classSection.setIsActive(null);

        // Create the ClassSection, which fails.
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(classSection);

        restClassSectionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classSectionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllClassSections() throws Exception {
        // Initialize the database
        insertedClassSection = classSectionRepository.save(classSection);

        // Get all the classSectionList
        restClassSectionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(classSection.getId())))
            .andExpect(jsonPath("$.[*].subjectName").value(hasItem(DEFAULT_SUBJECT_NAME)))
            .andExpect(jsonPath("$.[*].isActive").value(hasItem(DEFAULT_IS_ACTIVE)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllClassSectionsWithEagerRelationshipsIsEnabled() throws Exception {
        when(classSectionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restClassSectionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(classSectionServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllClassSectionsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(classSectionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restClassSectionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(classSectionRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void getClassSection() throws Exception {
        // Initialize the database
        insertedClassSection = classSectionRepository.save(classSection);

        // Get the classSection
        restClassSectionMockMvc
            .perform(get(ENTITY_API_URL_ID, classSection.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(classSection.getId()))
            .andExpect(jsonPath("$.subjectName").value(DEFAULT_SUBJECT_NAME))
            .andExpect(jsonPath("$.isActive").value(DEFAULT_IS_ACTIVE));
    }

    @Test
    void getNonExistingClassSection() throws Exception {
        // Get the classSection
        restClassSectionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingClassSection() throws Exception {
        // Persist the @DBRef targets so they resolve on reload
        gradeRepository.save(classSection.getGrade());
        classSection.setInstructor(persistInstructor(ACTIVE_INSTRUCTOR_ID, true));

        // Initialize the database
        insertedClassSection = classSectionRepository.save(classSection);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the classSection
        ClassSection updatedClassSection = classSectionRepository.findById(classSection.getId()).orElseThrow();
        updatedClassSection.subjectName(UPDATED_SUBJECT_NAME).isActive(UPDATED_IS_ACTIVE);
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(updatedClassSection);

        restClassSectionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, classSectionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(classSectionDTO))
            )
            .andExpect(status().isOk());

        // Validate the ClassSection in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedClassSectionToMatchAllProperties(updatedClassSection);
    }

    @Test
    void putClassSectionKeepingItsOwnName() throws Exception {
        gradeRepository.save(classSection.getGrade());
        insertedClassSection = classSectionRepository.save(classSection);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Save the section again with its own name, which must not collide with itself
        ClassSection updatedClassSection = classSectionRepository.findById(classSection.getId()).orElseThrow();
        updatedClassSection.setIsActive(UPDATED_IS_ACTIVE);
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(updatedClassSection);

        restClassSectionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, classSectionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(classSectionDTO))
            )
            .andExpect(status().isOk());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putClassSectionWithDuplicateNameInSameGrade() throws Exception {
        gradeRepository.save(classSection.getGrade());
        insertedClassSection = classSectionRepository.save(classSection);
        ClassSection otherSection = persistClassSectionInGrade(UPDATED_SUBJECT_NAME, classSection.getGrade());

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Rename the other section to a name already used in the same ficha, which fails
        ClassSection updatedOtherSection = classSectionRepository.findById(otherSection.getId()).orElseThrow();
        updatedOtherSection.setSubjectName(DEFAULT_SUBJECT_NAME);
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(updatedOtherSection);

        restClassSectionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, classSectionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(classSectionDTO))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.classSectionNameAlreadyUsed"));

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putClassSectionWithNonExistingInstructor() throws Exception {
        gradeRepository.save(classSection.getGrade());
        insertedClassSection = classSectionRepository.save(classSection);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the classSection assigning an instructor that does not exist
        ClassSection updatedClassSection = classSectionRepository.findById(classSection.getId()).orElseThrow();
        updatedClassSection.setInstructor(nonExistingInstructor());
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(updatedClassSection);

        restClassSectionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, classSectionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(classSectionDTO))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.instructorInactive"));

        // Validate the ClassSection in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertThat(getPersistedClassSection(classSection).getInstructor()).isNull();
    }

    @Test
    void putClassSectionWithInactiveInstructor() throws Exception {
        gradeRepository.save(classSection.getGrade());
        insertedClassSection = classSectionRepository.save(classSection);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the classSection assigning an instructor whose account is deactivated
        ClassSection updatedClassSection = classSectionRepository.findById(classSection.getId()).orElseThrow();
        updatedClassSection.setInstructor(persistInstructor(INACTIVE_INSTRUCTOR_ID, false));
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(updatedClassSection);

        restClassSectionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, classSectionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(classSectionDTO))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.instructorInactive"));

        // Validate the ClassSection in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertThat(getPersistedClassSection(classSection).getInstructor()).isNull();
    }

    @Test
    void putNonExistingClassSection() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classSection.setId(UUID.randomUUID().toString());

        // Create the ClassSection
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(classSection);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restClassSectionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, classSectionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(classSectionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ClassSection in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchClassSection() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classSection.setId(UUID.randomUUID().toString());

        // Create the ClassSection
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(classSection);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restClassSectionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(classSectionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ClassSection in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamClassSection() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classSection.setId(UUID.randomUUID().toString());

        // Create the ClassSection
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(classSection);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restClassSectionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classSectionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ClassSection in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateClassSectionWithPatch() throws Exception {
        // Initialize the database
        insertedClassSection = classSectionRepository.save(classSection);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the classSection using partial update
        ClassSection partialUpdatedClassSection = new ClassSection();
        partialUpdatedClassSection.setId(classSection.getId());

        restClassSectionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedClassSection.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedClassSection))
            )
            .andExpect(status().isOk());

        // Validate the ClassSection in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertClassSectionUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedClassSection, classSection),
            getPersistedClassSection(classSection)
        );
    }

    @Test
    void fullUpdateClassSectionWithPatch() throws Exception {
        // Initialize the database
        insertedClassSection = classSectionRepository.save(classSection);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the classSection using partial update
        ClassSection partialUpdatedClassSection = new ClassSection();
        partialUpdatedClassSection.setId(classSection.getId());

        partialUpdatedClassSection.subjectName(UPDATED_SUBJECT_NAME).isActive(UPDATED_IS_ACTIVE);

        restClassSectionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedClassSection.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedClassSection))
            )
            .andExpect(status().isOk());

        // Validate the ClassSection in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertClassSectionUpdatableFieldsEquals(partialUpdatedClassSection, getPersistedClassSection(partialUpdatedClassSection));
    }

    @Test
    void patchClassSectionKeepingItsOwnName() throws Exception {
        gradeRepository.save(classSection.getGrade());
        insertedClassSection = classSectionRepository.save(classSection);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Patch the section with its own name, which must not collide with itself
        ClassSection partialUpdatedClassSection = new ClassSection();
        partialUpdatedClassSection.setId(classSection.getId());
        partialUpdatedClassSection.setSubjectName(DEFAULT_SUBJECT_NAME);

        restClassSectionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, classSection.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(classSectionMapper.toDto(partialUpdatedClassSection)))
            )
            .andExpect(status().isOk());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchClassSectionWithDuplicateNameInSameGrade() throws Exception {
        gradeRepository.save(classSection.getGrade());
        insertedClassSection = classSectionRepository.save(classSection);
        ClassSection otherSection = persistClassSectionInGrade(UPDATED_SUBJECT_NAME, classSection.getGrade());

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Rename the other section to a name already used in the same ficha, which fails
        ClassSection partialUpdatedClassSection = new ClassSection();
        partialUpdatedClassSection.setId(otherSection.getId());
        partialUpdatedClassSection.setSubjectName(DEFAULT_SUBJECT_NAME);

        restClassSectionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, otherSection.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(classSectionMapper.toDto(partialUpdatedClassSection)))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.classSectionNameAlreadyUsed"));

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateClassSectionWithActiveInstructor() throws Exception {
        // Initialize the database
        insertedClassSection = classSectionRepository.save(classSection);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Assign an active instructor through partial update
        ClassSection partialUpdatedClassSection = new ClassSection();
        partialUpdatedClassSection.setId(classSection.getId());
        partialUpdatedClassSection.setInstructor(persistInstructor(ACTIVE_INSTRUCTOR_ID, true));

        restClassSectionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedClassSection.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(classSectionMapper.toDto(partialUpdatedClassSection)))
            )
            .andExpect(status().isOk());

        // Validate the instructor was persisted
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertThat(getPersistedClassSection(classSection).getInstructor().getId()).isEqualTo(ACTIVE_INSTRUCTOR_ID);
    }

    @Test
    void partialUpdateClassSectionWithNonExistingInstructor() throws Exception {
        // Initialize the database
        insertedClassSection = classSectionRepository.save(classSection);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Assign an instructor that does not exist through partial update
        ClassSection partialUpdatedClassSection = new ClassSection();
        partialUpdatedClassSection.setId(classSection.getId());
        partialUpdatedClassSection.setInstructor(nonExistingInstructor());

        restClassSectionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedClassSection.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(classSectionMapper.toDto(partialUpdatedClassSection)))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.instructorInactive"));

        // Validate the ClassSection in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertThat(getPersistedClassSection(classSection).getInstructor()).isNull();
    }

    @Test
    void partialUpdateClassSectionWithInactiveInstructor() throws Exception {
        // Initialize the database
        insertedClassSection = classSectionRepository.save(classSection);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Assign an instructor whose account is deactivated through partial update
        ClassSection partialUpdatedClassSection = new ClassSection();
        partialUpdatedClassSection.setId(classSection.getId());
        partialUpdatedClassSection.setInstructor(persistInstructor(INACTIVE_INSTRUCTOR_ID, false));

        restClassSectionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedClassSection.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(classSectionMapper.toDto(partialUpdatedClassSection)))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.instructorInactive"));

        // Validate the ClassSection in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertThat(getPersistedClassSection(classSection).getInstructor()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = StateGrade.class, names = { "FINALIZADA", "APLAZADA", "CANCELADA" })
    void createClassSectionInNonOperableGradeIsRejected(StateGrade state) throws Exception {
        classSection.setGrade(persistGradeWithState(nonOperableGradeId(state), state));

        long databaseSizeBeforeCreate = getRepositoryCount();

        // Creating a subject in a ficha that is not PENDIENTE or ACTIVA fails
        restClassSectionMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(classSectionMapper.toDto(classSection)))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.gradeNotOperable"));

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @ParameterizedTest
    @EnumSource(value = StateGrade.class, names = { "FINALIZADA", "APLAZADA", "CANCELADA" })
    void putClassSectionInNonOperableGradeIsRejected(StateGrade state) throws Exception {
        classSection.setGrade(persistGradeWithState(nonOperableGradeId(state), state));
        insertedClassSection = classSectionRepository.save(classSection);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Modifying a subject of a ficha that is not PENDIENTE or ACTIVA fails
        ClassSection updatedClassSection = classSectionRepository.findById(classSection.getId()).orElseThrow();
        updatedClassSection.subjectName(UPDATED_SUBJECT_NAME);
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(updatedClassSection);

        restClassSectionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, classSectionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(classSectionDTO))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.gradeNotOperable"));

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @ParameterizedTest
    @EnumSource(value = StateGrade.class, names = { "FINALIZADA", "APLAZADA", "CANCELADA" })
    void patchClassSectionInNonOperableGradeIsRejected(StateGrade state) throws Exception {
        classSection.setGrade(persistGradeWithState(nonOperableGradeId(state), state));
        insertedClassSection = classSectionRepository.save(classSection);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Partially modifying a subject of a ficha that is not PENDIENTE or ACTIVA fails
        ClassSection partialUpdatedClassSection = new ClassSection();
        partialUpdatedClassSection.setId(classSection.getId());
        partialUpdatedClassSection.setSubjectName(UPDATED_SUBJECT_NAME);

        restClassSectionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, classSection.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(classSectionMapper.toDto(partialUpdatedClassSection)))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.gradeNotOperable"));

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @ParameterizedTest
    @EnumSource(value = StateGrade.class, names = { "FINALIZADA", "APLAZADA", "CANCELADA" })
    void reactivateClassSectionInNonOperableGradeIsRejected(StateGrade state) throws Exception {
        classSection.setGrade(persistGradeWithState(nonOperableGradeId(state), state));
        insertedClassSection = classSectionRepository.save(classSection);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Reactivating a deactivated subject is a modification, so it is rejected as well
        ClassSection partialUpdatedClassSection = new ClassSection();
        partialUpdatedClassSection.setId(classSection.getId());
        partialUpdatedClassSection.setIsActive(true);

        restClassSectionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, classSection.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(classSectionMapper.toDto(partialUpdatedClassSection)))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.gradeNotOperable"));

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertThat(getPersistedClassSection(classSection).getIsActive()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = StateGrade.class, names = { "PENDIENTE", "ACTIVA" })
    void createClassSectionInOperableGradeSucceeds(StateGrade state) throws Exception {
        classSection.setGrade(persistGradeWithState(operableGradeId(state), state));

        long databaseSizeBeforeCreate = getRepositoryCount();

        // Creating a subject in a PENDIENTE or ACTIVA ficha keeps working
        var returnedClassSectionDTO = om.readValue(
            restClassSectionMockMvc
                .perform(
                    post(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(classSectionMapper.toDto(classSection)))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ClassSectionDTO.class
        );

        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        extraInsertedClassSections.add(classSectionMapper.toEntity(returnedClassSectionDTO));
    }

    @ParameterizedTest
    @EnumSource(value = StateGrade.class, names = { "PENDIENTE", "ACTIVA" })
    void putClassSectionInOperableGradeSucceeds(StateGrade state) throws Exception {
        classSection.setGrade(persistGradeWithState(operableGradeId(state), state));
        insertedClassSection = classSectionRepository.save(classSection);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Modifying a subject of a PENDIENTE or ACTIVA ficha keeps working
        ClassSection updatedClassSection = classSectionRepository.findById(classSection.getId()).orElseThrow();
        updatedClassSection.subjectName(UPDATED_SUBJECT_NAME).isActive(UPDATED_IS_ACTIVE);

        restClassSectionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedClassSection.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(classSectionMapper.toDto(updatedClassSection)))
            )
            .andExpect(status().isOk());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedClassSectionToMatchAllProperties(updatedClassSection);
    }

    @ParameterizedTest
    @EnumSource(value = StateGrade.class, names = { "PENDIENTE", "ACTIVA" })
    void patchClassSectionInOperableGradeSucceeds(StateGrade state) throws Exception {
        classSection.setGrade(persistGradeWithState(operableGradeId(state), state));
        insertedClassSection = classSectionRepository.save(classSection);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Partially modifying a subject of a PENDIENTE or ACTIVA ficha keeps working
        ClassSection partialUpdatedClassSection = new ClassSection();
        partialUpdatedClassSection.setId(classSection.getId());
        partialUpdatedClassSection.setSubjectName(UPDATED_SUBJECT_NAME);

        restClassSectionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, classSection.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(classSectionMapper.toDto(partialUpdatedClassSection)))
            )
            .andExpect(status().isOk());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertThat(getPersistedClassSection(classSection).getSubjectName()).isEqualTo(UPDATED_SUBJECT_NAME);
    }

    @Test
    void patchNonExistingClassSection() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classSection.setId(UUID.randomUUID().toString());

        // Create the ClassSection
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(classSection);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restClassSectionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, classSectionDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(classSectionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ClassSection in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchClassSection() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classSection.setId(UUID.randomUUID().toString());

        // Create the ClassSection
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(classSection);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restClassSectionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(classSectionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ClassSection in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamClassSection() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classSection.setId(UUID.randomUUID().toString());

        // Create the ClassSection
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(classSection);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restClassSectionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(classSectionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ClassSection in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteClassSection() throws Exception {
        // Initialize the database
        insertedClassSection = classSectionRepository.save(classSection);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the classSection
        restClassSectionMockMvc
            .perform(delete(ENTITY_API_URL_ID, classSection.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    @Test
    void deleteNonExistingClassSectionIsASilentNoOp() throws Exception {
        long databaseSizeBeforeDelete = getRepositoryCount();

        restClassSectionMockMvc
            .perform(delete(ENTITY_API_URL_ID, UUID.randomUUID().toString()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        assertSameRepositoryCount(databaseSizeBeforeDelete);
    }

    @Test
    void deleteClassSectionWithAttendanceIsRejected() throws Exception {
        // Initialize the database
        insertedClassSection = classSectionRepository.save(classSection);
        insertedAttendance = attendanceRepository.save(
            new Attendance()
                .date(LocalDate.now(ZoneId.systemDefault()))
                .stateAttendance(StateAttendance.PRESENTE)
                .classSection(insertedClassSection)
        );

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Deleting a class section with attendance is rejected: it must be deactivated instead
        restClassSectionMockMvc
            .perform(delete(ENTITY_API_URL_ID, insertedClassSection.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.classSectionInUse"));

        // The class section and its attendance records are kept
        assertSameRepositoryCount(databaseSizeBeforeDelete);
        assertThat(classSectionRepository.existsById(insertedClassSection.getId())).isTrue();
        assertThat(attendanceRepository.existsById(insertedAttendance.getId())).isTrue();
    }

    @Test
    void deleteClassSectionCascadesSchedulesAndExceptions() throws Exception {
        // Initialize the database
        insertedClassSection = classSectionRepository.save(classSection);
        insertedSchedule = classScheduleRepository.save(
            new ClassSchedule()
                .dayOfWeek(DayOfWeek.LUNES)
                .startTime(LocalTime.of(7, 0))
                .endTime(LocalTime.of(9, 0))
                .classSection(insertedClassSection)
        );
        insertedException = classExceptionRepository.save(
            new ClassException().date(LocalDate.now(ZoneId.systemDefault())).reason("Test exception").classSection(insertedClassSection)
        );

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Deleting a class section without attendance removes it with its schedules and exceptions
        restClassSectionMockMvc
            .perform(delete(ENTITY_API_URL_ID, insertedClassSection.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        assertThat(classSectionRepository.findById(insertedClassSection.getId())).isEmpty();
        assertThat(classScheduleRepository.findById(insertedSchedule.getId())).isEmpty();
        assertThat(classExceptionRepository.findById(insertedException.getId())).isEmpty();
    }

    protected long getRepositoryCount() {
        return classSectionRepository.count();
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

    protected ClassSection getPersistedClassSection(ClassSection classSection) {
        return classSectionRepository.findById(classSection.getId()).orElseThrow();
    }

    protected void assertPersistedClassSectionToMatchAllProperties(ClassSection expectedClassSection) {
        assertClassSectionAllPropertiesEquals(expectedClassSection, getPersistedClassSection(expectedClassSection));
    }

    protected void assertPersistedClassSectionToMatchUpdatableProperties(ClassSection expectedClassSection) {
        assertClassSectionAllUpdatablePropertiesEquals(expectedClassSection, getPersistedClassSection(expectedClassSection));
    }
}
