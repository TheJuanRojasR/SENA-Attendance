package com.mycompany.senaattendance.web.rest;

import static com.mycompany.senaattendance.domain.GradeAsserts.*;
import static com.mycompany.senaattendance.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.Apprentice;
import com.mycompany.senaattendance.domain.Attendance;
import com.mycompany.senaattendance.domain.ClassException;
import com.mycompany.senaattendance.domain.ClassSchedule;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.Modality;
import com.mycompany.senaattendance.domain.Program;
import com.mycompany.senaattendance.domain.TimeSlot;
import com.mycompany.senaattendance.domain.enumeration.DayOfWeek;
import com.mycompany.senaattendance.domain.enumeration.StateAcademic;
import com.mycompany.senaattendance.domain.enumeration.StateAttendance;
import com.mycompany.senaattendance.domain.enumeration.StateGrade;
import com.mycompany.senaattendance.repository.ApprenticeRepository;
import com.mycompany.senaattendance.repository.AttendanceRepository;
import com.mycompany.senaattendance.repository.ClassExceptionRepository;
import com.mycompany.senaattendance.repository.ClassScheduleRepository;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.repository.ModalityRepository;
import com.mycompany.senaattendance.repository.ProgramRepository;
import com.mycompany.senaattendance.repository.TimeSlotRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.GradeService;
import com.mycompany.senaattendance.service.dto.GradeDTO;
import com.mycompany.senaattendance.service.dto.ModalityDTO;
import com.mycompany.senaattendance.service.dto.ProgramDTO;
import com.mycompany.senaattendance.service.dto.TimeSlotDTO;
import com.mycompany.senaattendance.service.mapper.GradeMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Integration tests for the {@link GradeResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class GradeResourceIT {

    private static final String DEFAULT_CODE = "1111111111";
    private static final String UPDATED_CODE = "2222222222";

    private static final StateGrade DEFAULT_STATE = StateGrade.ACTIVA;
    private static final StateGrade UPDATED_STATE = StateGrade.APLAZADA;

    private static final LocalDate DEFAULT_START_DATE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate UPDATED_START_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final LocalDate DEFAULT_END_DATE = LocalDate.now(ZoneId.systemDefault()).plusDays(30);
    private static final LocalDate UPDATED_END_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String ENTITY_API_URL = "/api/grades";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private GradeRepository gradeRepository;

    @Mock
    private GradeRepository gradeRepositoryMock;

    @Autowired
    private GradeMapper gradeMapper;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private ModalityRepository modalityRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private ClassSectionRepository classSectionRepository;

    @Autowired
    private ApprenticeRepository apprenticeRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private ClassScheduleRepository classScheduleRepository;

    @Autowired
    private ClassExceptionRepository classExceptionRepository;

    @Mock
    private GradeService gradeServiceMock;

    @Autowired
    private MockMvc restGradeMockMvc;

    private Grade grade;

    private Grade insertedGrade;

    private ClassSection insertedClassSection;

    private Apprentice insertedApprentice;

    private Attendance insertedAttendance;

    private ClassSchedule insertedSchedule;

    private ClassException insertedException;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Grade createEntity() {
        Grade grade = new Grade().code(DEFAULT_CODE).state(DEFAULT_STATE).startDate(DEFAULT_START_DATE).endDate(DEFAULT_END_DATE);
        // Add required entity
        Program program;
        program = ProgramResourceIT.createEntity();
        program.setId("fixed-id-for-tests");
        grade.setProgram(program);
        // Add required entity
        Modality modality;
        modality = ModalityResourceIT.createEntity();
        modality.setId("fixed-id-for-tests");
        // The ficha catalog references must be active for a valid create/update
        modality.setIsActive(true);
        grade.setModality(modality);
        // Add required entity
        TimeSlot timeSlot;
        timeSlot = TimeSlotResourceIT.createEntity();
        timeSlot.setId("fixed-id-for-tests");
        timeSlot.setIsActive(true);
        grade.setTimeSlot(timeSlot);
        return grade;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Grade createUpdatedEntity() {
        Grade updatedGrade = new Grade().code(UPDATED_CODE).state(UPDATED_STATE).startDate(UPDATED_START_DATE).endDate(UPDATED_END_DATE);
        // Add required entity
        Program program;
        program = ProgramResourceIT.createUpdatedEntity();
        program.setId("fixed-id-for-tests");
        updatedGrade.setProgram(program);
        // Add required entity
        Modality modality;
        modality = ModalityResourceIT.createUpdatedEntity();
        modality.setId("fixed-id-for-tests");
        updatedGrade.setModality(modality);
        // Add required entity
        TimeSlot timeSlot;
        timeSlot = TimeSlotResourceIT.createUpdatedEntity();
        timeSlot.setId("fixed-id-for-tests");
        updatedGrade.setTimeSlot(timeSlot);
        return updatedGrade;
    }

    @BeforeEach
    void initTest() {
        grade = createEntity();
    }

    @AfterEach
    void cleanup() {
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
        if (insertedClassSection != null) {
            classSectionRepository.delete(insertedClassSection);
            insertedClassSection = null;
        }
        if (insertedApprentice != null) {
            apprenticeRepository.delete(insertedApprentice);
            insertedApprentice = null;
        }
        if (insertedGrade != null) {
            gradeRepository.delete(insertedGrade);
            insertedGrade = null;
        }
        // Remove the related documents persisted for the PUT tests
        modalityRepository.deleteAll();
        programRepository.deleteAll();
        timeSlotRepository.deleteAll();
    }

    /**
     * Persists the @DBRef targets of the ficha under test so they resolve on reload.
     */
    private void persistCatalogRefs() {
        programRepository.save(grade.getProgram());
        modalityRepository.save(grade.getModality());
        timeSlotRepository.save(grade.getTimeSlot());
    }

    /**
     * Persists the ficha under test with the given state and date range, so the edit rules
     * evaluate the state the database actually holds.
     */
    private GradeDTO persistGrade(StateGrade state, LocalDate startDate, LocalDate endDate) {
        grade.state(state).startDate(startDate).endDate(endDate);
        insertedGrade = gradeRepository.save(grade);
        return gradeMapper.toDto(insertedGrade);
    }

    /**
     * Seeds a class section linked to the ficha, which locks the ficha code.
     */
    private ClassSection persistClassSection(Grade grade) {
        insertedClassSection = classSectionRepository.save(new ClassSection().subjectName("Test subject").isActive(true).grade(grade));
        return insertedClassSection;
    }

    /**
     * Seeds an apprentice linked to the ficha, which locks the ficha code.
     */
    private Apprentice persistApprentice(Grade grade) {
        insertedApprentice = apprenticeRepository.save(new Apprentice().stateAcademic(StateAcademic.MATRICULADO).grade(grade));
        return insertedApprentice;
    }

    @Test
    void createGrade() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Grade
        GradeDTO gradeDTO = gradeMapper.toDto(grade);
        var returnedGradeDTO = om.readValue(
            restGradeMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            GradeDTO.class
        );

        // Validate the Grade in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedGrade = gradeMapper.toEntity(returnedGradeDTO);
        assertGradeUpdatableFieldsEquals(returnedGrade, getPersistedGrade(returnedGrade));

        insertedGrade = returnedGrade;
    }

    @Test
    void createGradeWithInactiveProgramReturnsBadRequest() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();

        // The ficha references a program that is inactive (persisted in the DB)
        Program inactiveProgram = ProgramResourceIT.createEntity();
        inactiveProgram.setStatus(false);
        inactiveProgram = programRepository.save(inactiveProgram);

        try {
            GradeDTO gradeDTO = gradeMapper.toDto(createEntity());
            gradeDTO.getProgram().setId(inactiveProgram.getId());

            restGradeMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error.programInactive"));

            assertSameRepositoryCount(databaseSizeBeforeTest);
        } finally {
            programRepository.delete(inactiveProgram);
        }
    }

    @Test
    void createGradeWithEndDateBeforeStartDateReturnsBadRequest() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();

        GradeDTO gradeDTO = gradeMapper.toDto(createEntity());
        gradeDTO.setStartDate(DEFAULT_START_DATE.plusDays(10));
        gradeDTO.setEndDate(DEFAULT_START_DATE.plusDays(5));

        restGradeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.datesorder"));

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void createGradeWithStartDateInThePastReturnsBadRequest() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        LocalDate today = LocalDate.now(ZoneId.systemDefault());

        GradeDTO gradeDTO = gradeMapper.toDto(createEntity());
        gradeDTO.setStartDate(today.minusDays(1));
        gradeDTO.setEndDate(today.plusDays(30));

        restGradeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.startdateinpast"));

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void createGradeWithInactiveModalityReturnsBadRequest() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();

        Modality inactiveModality = ModalityResourceIT.createEntity();
        inactiveModality.setIsActive(false);
        inactiveModality = modalityRepository.save(inactiveModality);

        try {
            GradeDTO gradeDTO = gradeMapper.toDto(createEntity());
            gradeDTO.getModality().setId(inactiveModality.getId());

            restGradeMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error.modalityInactive"));

            assertSameRepositoryCount(databaseSizeBeforeTest);
        } finally {
            modalityRepository.delete(inactiveModality);
        }
    }

    @Test
    void createGradeWithInactiveTimeSlotReturnsBadRequest() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();

        TimeSlot inactiveTimeSlot = TimeSlotResourceIT.createEntity();
        inactiveTimeSlot.setIsActive(false);
        inactiveTimeSlot = timeSlotRepository.save(inactiveTimeSlot);

        try {
            GradeDTO gradeDTO = gradeMapper.toDto(createEntity());
            gradeDTO.getTimeSlot().setId(inactiveTimeSlot.getId());

            restGradeMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error.timeSlotInactive"));

            assertSameRepositoryCount(databaseSizeBeforeTest);
        } finally {
            timeSlotRepository.delete(inactiveTimeSlot);
        }
    }

    @Test
    void createGradeWithExistingId() throws Exception {
        // Create the Grade with an existing ID
        grade.setId("existing_id");
        GradeDTO gradeDTO = gradeMapper.toDto(grade);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restGradeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Grade in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkCodeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        grade.setCode(null);

        // Create the Grade, which fails.
        GradeDTO gradeDTO = gradeMapper.toDto(grade);

        restGradeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void createGradeWithNonNumericCodeReturnsBadRequest() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        grade.setCode("AB12");

        GradeDTO gradeDTO = gradeMapper.toDto(grade);

        restGradeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.validation"))
            .andExpect(jsonPath("$.fieldErrors").isArray())
            .andExpect(jsonPath("$.fieldErrors[0].field").value("code"));

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void createGradeWithDuplicateCodeReturnsBadRequest() throws Exception {
        // Persist a ficha with DEFAULT_CODE so the upcoming POST collides on code only
        insertedGrade = gradeRepository.save(grade);
        long databaseSizeBeforeCreate = getRepositoryCount();

        GradeDTO gradeDTO = gradeMapper.toDto(createEntity());

        restGradeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.gradeCodeAlreadyUsed"));

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void createGradeComputesStateFromDatesIgnoringClientState() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        LocalDate today = LocalDate.now(ZoneId.systemDefault());

        // The ficha starts in the future, so the server must persist PENDIENTE
        // regardless of the CANCELADA state sent by the client.
        GradeDTO gradeDTO = gradeMapper.toDto(createEntity());
        gradeDTO.setStartDate(today.plusDays(10));
        gradeDTO.setEndDate(today.plusDays(40));
        gradeDTO.setState(StateGrade.CANCELADA);

        var returnedGradeDTO = om.readValue(
            restGradeMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.state").value(StateGrade.PENDIENTE.toString()))
                .andReturn()
                .getResponse()
                .getContentAsString(),
            GradeDTO.class
        );

        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedGrade = gradeMapper.toEntity(returnedGradeDTO);
        assertThat(gradeRepository.findById(returnedGrade.getId()).orElseThrow().getState()).isEqualTo(StateGrade.PENDIENTE);

        insertedGrade = returnedGrade;
    }

    @Test
    void checkStartDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        grade.setStartDate(null);

        // Create the Grade, which fails.
        GradeDTO gradeDTO = gradeMapper.toDto(grade);

        restGradeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkEndDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        grade.setEndDate(null);

        // Create the Grade, which fails.
        GradeDTO gradeDTO = gradeMapper.toDto(grade);

        restGradeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllGrades() throws Exception {
        // Initialize the database
        insertedGrade = gradeRepository.save(grade);

        // Get all the gradeList
        restGradeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(grade.getId())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].state").value(hasItem(DEFAULT_STATE.toString())))
            .andExpect(jsonPath("$.[*].startDate").value(hasItem(DEFAULT_START_DATE.toString())))
            .andExpect(jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllGradesWithEagerRelationshipsIsEnabled() throws Exception {
        when(gradeServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restGradeMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(gradeServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllGradesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(gradeServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restGradeMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(gradeRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void getGrade() throws Exception {
        // Initialize the database
        insertedGrade = gradeRepository.save(grade);

        // Get the grade
        restGradeMockMvc
            .perform(get(ENTITY_API_URL_ID, grade.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(grade.getId()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE))
            .andExpect(jsonPath("$.state").value(DEFAULT_STATE.toString()))
            .andExpect(jsonPath("$.startDate").value(DEFAULT_START_DATE.toString()))
            .andExpect(jsonPath("$.endDate").value(DEFAULT_END_DATE.toString()));
    }

    @Test
    void getNonExistingGrade() throws Exception {
        // Get the grade
        restGradeMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingGrade() throws Exception {
        // Persist the @DBRef targets so they resolve on reload
        programRepository.save(grade.getProgram());
        modalityRepository.save(grade.getModality());
        timeSlotRepository.save(grade.getTimeSlot());

        // Initialize the database with a manually cancelled ficha, so the update must preserve
        // that state. A cancelled ficha accepts changes on every field.
        insertedGrade = gradeRepository.save(grade.state(StateGrade.CANCELADA));

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the grade
        Grade updatedGrade = gradeRepository.findById(grade.getId()).orElseThrow();
        updatedGrade.code(UPDATED_CODE).startDate(UPDATED_START_DATE).endDate(UPDATED_END_DATE);
        GradeDTO gradeDTO = gradeMapper.toDto(updatedGrade);

        restGradeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, gradeDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO))
            )
            .andExpect(status().isOk());

        // Validate the Grade in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedGradeToMatchAllProperties(updatedGrade);
    }

    @Test
    void putGradeWithNonNumericCodeReturnsBadRequest() throws Exception {
        // Persist the @DBRef targets so they resolve on reload
        programRepository.save(grade.getProgram());
        modalityRepository.save(grade.getModality());
        timeSlotRepository.save(grade.getTimeSlot());
        insertedGrade = gradeRepository.save(grade);

        GradeDTO gradeDTO = gradeMapper.toDto(gradeRepository.findById(grade.getId()).orElseThrow());
        gradeDTO.setCode("AB12");

        restGradeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, gradeDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.validation"))
            .andExpect(jsonPath("$.fieldErrors").isArray())
            .andExpect(jsonPath("$.fieldErrors[0].field").value("code"));

        assertThat(getPersistedGrade(grade).getCode()).isEqualTo(DEFAULT_CODE);
    }

    @Test
    void putGradeWithDuplicateCodeReturnsBadRequest() throws Exception {
        // The ficha that already owns DEFAULT_CODE
        programRepository.save(grade.getProgram());
        modalityRepository.save(grade.getModality());
        timeSlotRepository.save(grade.getTimeSlot());
        insertedGrade = gradeRepository.save(grade);

        // Another ficha that tries to take DEFAULT_CODE
        Grade other = createEntity().code(UPDATED_CODE);
        other = gradeRepository.save(other);
        try {
            // Reload the ficha so the payload references the same catalog ids the database holds.
            GradeDTO gradeDTO = gradeMapper.toDto(gradeRepository.findById(other.getId()).orElseThrow());
            gradeDTO.setCode(DEFAULT_CODE);

            restGradeMockMvc
                .perform(
                    put(ENTITY_API_URL_ID, gradeDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error.gradeCodeAlreadyUsed"));

            assertThat(getPersistedGrade(other).getCode()).isEqualTo(UPDATED_CODE);
        } finally {
            gradeRepository.delete(other);
        }
    }

    @Test
    void putGradeKeepingItsOwnCodeDoesNotReportDuplicate() throws Exception {
        // Persist the @DBRef targets so they resolve on reload
        programRepository.save(grade.getProgram());
        modalityRepository.save(grade.getModality());
        timeSlotRepository.save(grade.getTimeSlot());
        insertedGrade = gradeRepository.save(grade);

        GradeDTO gradeDTO = gradeMapper.toDto(gradeRepository.findById(grade.getId()).orElseThrow());
        gradeDTO.setCode(DEFAULT_CODE);
        gradeDTO.setEndDate(UPDATED_END_DATE);

        restGradeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, gradeDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO))
            )
            .andExpect(status().isOk());

        assertThat(getPersistedGrade(grade).getCode()).isEqualTo(DEFAULT_CODE);
    }

    @Test
    void putGradeWithEndDateBeforeStartDateReturnsBadRequest() throws Exception {
        // A PENDIENTE ficha accepts date changes, so the date order rule is reached.
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        GradeDTO gradeDTO = persistGrade(StateGrade.PENDIENTE, today.plusDays(10), today.plusDays(40));
        gradeDTO.setStartDate(today.plusDays(20));
        gradeDTO.setEndDate(today.plusDays(15));

        restGradeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, gradeDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.datesorder"));

        assertThat(getPersistedGrade(grade).getStartDate()).isEqualTo(today.plusDays(10));
    }

    @Test
    void putGradeWithChangedStartDateInThePastReturnsBadRequest() throws Exception {
        // A PENDIENTE ficha accepts moving its start date, so the past-date rule is reached.
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        GradeDTO gradeDTO = persistGrade(StateGrade.PENDIENTE, today.plusDays(10), today.plusDays(40));
        gradeDTO.setStartDate(today.minusDays(1));
        gradeDTO.setEndDate(today.plusDays(30));

        restGradeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, gradeDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.startdateinpast"));

        assertThat(getPersistedGrade(grade).getStartDate()).isEqualTo(today.plusDays(10));
    }

    @Test
    void putGradeWithInactiveModalityReturnsBadRequest() throws Exception {
        Modality inactiveModality = ModalityResourceIT.createEntity();
        inactiveModality.setIsActive(false);
        inactiveModality = modalityRepository.save(inactiveModality);

        try {
            // A PENDIENTE ficha accepts changing the modality, so the catalog rule is reached.
            LocalDate today = LocalDate.now(ZoneId.systemDefault());
            persistCatalogRefs();
            GradeDTO gradeDTO = persistGrade(StateGrade.PENDIENTE, today.plusDays(10), today.plusDays(40));
            gradeDTO.getModality().setId(inactiveModality.getId());

            restGradeMockMvc
                .perform(
                    put(ENTITY_API_URL_ID, gradeDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error.modalityInactive"));

            assertThat(getPersistedGrade(grade).getModality().getId()).isEqualTo("fixed-id-for-tests");
        } finally {
            modalityRepository.delete(inactiveModality);
        }
    }

    @Test
    void putGradeWithInactiveTimeSlotReturnsBadRequest() throws Exception {
        TimeSlot inactiveTimeSlot = TimeSlotResourceIT.createEntity();
        inactiveTimeSlot.setIsActive(false);
        inactiveTimeSlot = timeSlotRepository.save(inactiveTimeSlot);

        try {
            // A PENDIENTE ficha accepts changing the time slot, so the catalog rule is reached.
            LocalDate today = LocalDate.now(ZoneId.systemDefault());
            persistCatalogRefs();
            GradeDTO gradeDTO = persistGrade(StateGrade.PENDIENTE, today.plusDays(10), today.plusDays(40));
            gradeDTO.getTimeSlot().setId(inactiveTimeSlot.getId());

            restGradeMockMvc
                .perform(
                    put(ENTITY_API_URL_ID, gradeDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error.timeSlotInactive"));

            assertThat(getPersistedGrade(grade).getTimeSlot().getId()).isEqualTo("fixed-id-for-tests");
        } finally {
            timeSlotRepository.delete(inactiveTimeSlot);
        }
    }

    @Test
    void putFinalizadaGradeRejectsAnyChange() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        persistCatalogRefs();
        GradeDTO gradeDTO = persistGrade(StateGrade.FINALIZADA, today.minusDays(40), today.minusDays(10));
        gradeDTO.setEndDate(today.minusDays(5));

        restGradeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, gradeDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.noteditable"));

        assertThat(getPersistedGrade(grade).getEndDate()).isEqualTo(today.minusDays(10));
    }

    @Test
    void putActivaGradeRejectsStartDateChange() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        persistCatalogRefs();
        GradeDTO gradeDTO = persistGrade(StateGrade.ACTIVA, today.minusDays(10), today.plusDays(10));
        gradeDTO.setStartDate(today.plusDays(5));

        restGradeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, gradeDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.fieldlocked"));

        assertThat(getPersistedGrade(grade).getStartDate()).isEqualTo(today.minusDays(10));
    }

    @Test
    void putActivaGradeRejectsModalityChange() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        persistCatalogRefs();
        GradeDTO gradeDTO = persistGrade(StateGrade.ACTIVA, today.minusDays(10), today.plusDays(10));
        gradeDTO.setModality(new ModalityDTO());
        gradeDTO.getModality().setId("other-modality");

        restGradeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, gradeDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.fieldlocked"));

        assertThat(getPersistedGrade(grade).getModality().getId()).isEqualTo("fixed-id-for-tests");
    }

    @Test
    void putActivaGradeRejectsTimeSlotChange() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        persistCatalogRefs();
        GradeDTO gradeDTO = persistGrade(StateGrade.ACTIVA, today.minusDays(10), today.plusDays(10));
        gradeDTO.setTimeSlot(new TimeSlotDTO());
        gradeDTO.getTimeSlot().setId("other-time-slot");

        restGradeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, gradeDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.fieldlocked"));

        assertThat(getPersistedGrade(grade).getTimeSlot().getId()).isEqualTo("fixed-id-for-tests");
    }

    @Test
    void putActivaGradeAllowsEndDateAndProgramChange() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        persistCatalogRefs();
        GradeDTO gradeDTO = persistGrade(StateGrade.ACTIVA, today.minusDays(10), today.plusDays(10));
        gradeDTO.setEndDate(today.plusDays(45));
        gradeDTO.setProgram(new ProgramDTO());
        gradeDTO.getProgram().setId("other-program");

        restGradeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, gradeDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value(StateGrade.ACTIVA.toString()))
            .andExpect(jsonPath("$.endDate").value(today.plusDays(45).toString()))
            .andExpect(jsonPath("$.program.id").value("other-program"));

        assertThat(getPersistedGrade(grade).getEndDate()).isEqualTo(today.plusDays(45));
    }

    @Test
    void putAplazadaGradeRejectsStartDateChange() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        persistCatalogRefs();
        GradeDTO gradeDTO = persistGrade(StateGrade.APLAZADA, today.minusDays(10), today.plusDays(10));
        gradeDTO.setStartDate(today.plusDays(5));

        restGradeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, gradeDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.fieldlocked"));

        assertThat(getPersistedGrade(grade).getStartDate()).isEqualTo(today.minusDays(10));
    }

    @Test
    void putAplazadaGradeAllowsEndDateChange() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        persistCatalogRefs();
        GradeDTO gradeDTO = persistGrade(StateGrade.APLAZADA, today.minusDays(10), today.plusDays(10));
        gradeDTO.setEndDate(today.plusDays(60));

        restGradeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, gradeDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value(StateGrade.APLAZADA.toString()));

        assertThat(getPersistedGrade(grade).getEndDate()).isEqualTo(today.plusDays(60));
    }

    @Test
    void putPendienteGradeAllowsFieldChanges() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        persistCatalogRefs();
        GradeDTO gradeDTO = persistGrade(StateGrade.PENDIENTE, today.plusDays(10), today.plusDays(40));
        gradeDTO.setStartDate(today.plusDays(5));
        gradeDTO.setEndDate(today.plusDays(60));

        restGradeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, gradeDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value(StateGrade.PENDIENTE.toString()));

        assertThat(getPersistedGrade(grade).getStartDate()).isEqualTo(today.plusDays(5));
        assertThat(getPersistedGrade(grade).getEndDate()).isEqualTo(today.plusDays(60));
    }

    @Test
    void putCanceladaGradeAllowsFieldChanges() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        persistCatalogRefs();
        GradeDTO gradeDTO = persistGrade(StateGrade.CANCELADA, today.plusDays(10), today.plusDays(40));
        gradeDTO.setStartDate(today.plusDays(5));
        gradeDTO.setProgram(new ProgramDTO());
        gradeDTO.getProgram().setId("other-program");

        restGradeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, gradeDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value(StateGrade.CANCELADA.toString()))
            .andExpect(jsonPath("$.program.id").value("other-program"));

        assertThat(getPersistedGrade(grade).getStartDate()).isEqualTo(today.plusDays(5));
    }

    @Test
    void putGradeWithClassSectionsRejectsCodeChange() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        persistCatalogRefs();
        GradeDTO gradeDTO = persistGrade(StateGrade.PENDIENTE, today.plusDays(10), today.plusDays(40));
        persistClassSection(insertedGrade);
        gradeDTO.setCode(UPDATED_CODE);

        restGradeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, gradeDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.gradeCodeLocked"));

        assertThat(getPersistedGrade(grade).getCode()).isEqualTo(DEFAULT_CODE);
    }

    @Test
    void putGradeWithApprenticesRejectsCodeChange() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        persistCatalogRefs();
        GradeDTO gradeDTO = persistGrade(StateGrade.PENDIENTE, today.plusDays(10), today.plusDays(40));
        persistApprentice(insertedGrade);
        gradeDTO.setCode(UPDATED_CODE);

        restGradeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, gradeDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.gradeCodeLocked"));

        assertThat(getPersistedGrade(grade).getCode()).isEqualTo(DEFAULT_CODE);
    }

    @Test
    void putGradeWithoutAssociationsAllowsCodeChange() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        persistCatalogRefs();
        GradeDTO gradeDTO = persistGrade(StateGrade.PENDIENTE, today.plusDays(10), today.plusDays(40));
        gradeDTO.setCode(UPDATED_CODE);

        restGradeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, gradeDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO))
            )
            .andExpect(status().isOk());

        assertThat(getPersistedGrade(grade).getCode()).isEqualTo(UPDATED_CODE);
    }

    @Test
    void putNonExistingGrade() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        grade.setId(UUID.randomUUID().toString());

        // Create the Grade
        GradeDTO gradeDTO = gradeMapper.toDto(grade);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGradeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, gradeDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Grade in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchGrade() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        grade.setId(UUID.randomUUID().toString());

        // Create the Grade
        GradeDTO gradeDTO = gradeMapper.toDto(grade);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGradeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(gradeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Grade in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamGrade() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        grade.setId(UUID.randomUUID().toString());

        // Create the Grade
        GradeDTO gradeDTO = gradeMapper.toDto(grade);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGradeMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Grade in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateGradeWithPatch() throws Exception {
        // Initialize the database
        insertedGrade = gradeRepository.save(grade);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the grade using partial update
        Grade partialUpdatedGrade = new Grade();
        partialUpdatedGrade.setId(grade.getId());

        partialUpdatedGrade.endDate(UPDATED_END_DATE);

        restGradeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGrade.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGrade))
            )
            .andExpect(status().isOk());

        // Validate the Grade in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertGradeUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedGrade, grade), getPersistedGrade(grade));
    }

    @Test
    void fullUpdateGradeWithPatch() throws Exception {
        // Initialize the database with a manually cancelled ficha, so the patch must preserve
        // that state. A cancelled ficha accepts changes on every field.
        insertedGrade = gradeRepository.save(grade.state(StateGrade.CANCELADA));

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the grade using partial update
        Grade partialUpdatedGrade = new Grade();
        partialUpdatedGrade.setId(grade.getId());

        partialUpdatedGrade.code(UPDATED_CODE).state(StateGrade.CANCELADA).startDate(UPDATED_START_DATE).endDate(UPDATED_END_DATE);

        restGradeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGrade.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGrade))
            )
            .andExpect(status().isOk());

        // Validate the Grade in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertGradeUpdatableFieldsEquals(partialUpdatedGrade, getPersistedGrade(partialUpdatedGrade));
    }

    @Test
    void patchGradeWithNonNumericCodeReturnsBadRequest() throws Exception {
        insertedGrade = gradeRepository.save(grade);

        Grade partialUpdatedGrade = new Grade();
        partialUpdatedGrade.setId(grade.getId());
        partialUpdatedGrade.setCode("AB12");

        restGradeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGrade.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGrade))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.codenotnumeric"));

        assertThat(getPersistedGrade(grade).getCode()).isEqualTo(DEFAULT_CODE);
    }

    @Test
    void patchGradeWithDuplicateCodeReturnsBadRequest() throws Exception {
        // The ficha that already owns DEFAULT_CODE
        insertedGrade = gradeRepository.save(grade);

        // Another ficha that tries to take DEFAULT_CODE
        Grade other = createEntity().code(UPDATED_CODE);
        other = gradeRepository.save(other);
        try {
            Grade partialUpdatedGrade = new Grade();
            partialUpdatedGrade.setId(other.getId());
            partialUpdatedGrade.setCode(DEFAULT_CODE);

            restGradeMockMvc
                .perform(
                    patch(ENTITY_API_URL_ID, partialUpdatedGrade.getId())
                        .contentType("application/merge-patch+json")
                        .content(om.writeValueAsBytes(partialUpdatedGrade))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error.gradeCodeAlreadyUsed"));

            assertThat(getPersistedGrade(other).getCode()).isEqualTo(UPDATED_CODE);
        } finally {
            gradeRepository.delete(other);
        }
    }

    @Test
    void patchGradeKeepingItsOwnCodeDoesNotReportDuplicate() throws Exception {
        insertedGrade = gradeRepository.save(grade);

        Grade partialUpdatedGrade = new Grade();
        partialUpdatedGrade.setId(grade.getId());
        partialUpdatedGrade.setCode(DEFAULT_CODE);
        partialUpdatedGrade.setEndDate(UPDATED_END_DATE);

        restGradeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGrade.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGrade))
            )
            .andExpect(status().isOk());

        assertThat(getPersistedGrade(grade).getCode()).isEqualTo(DEFAULT_CODE);
    }

    @Test
    void patchGradeWithEndDateBeforeStartDateReturnsBadRequest() throws Exception {
        // A PENDIENTE ficha accepts date changes, so the date order rule is reached.
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        persistGrade(StateGrade.PENDIENTE, today.plusDays(10), today.plusDays(40));

        Grade partialUpdatedGrade = new Grade();
        partialUpdatedGrade.setId(grade.getId());
        partialUpdatedGrade.setStartDate(today.plusDays(20));
        partialUpdatedGrade.setEndDate(today.plusDays(15));

        restGradeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGrade.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGrade))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.datesorder"));

        assertThat(getPersistedGrade(grade).getStartDate()).isEqualTo(today.plusDays(10));
    }

    @Test
    void patchGradeWithChangedStartDateInThePastReturnsBadRequest() throws Exception {
        // A PENDIENTE ficha accepts moving its start date, so the past-date rule is reached.
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        persistGrade(StateGrade.PENDIENTE, today.plusDays(10), today.plusDays(40));

        Grade partialUpdatedGrade = new Grade();
        partialUpdatedGrade.setId(grade.getId());
        partialUpdatedGrade.setStartDate(today.minusDays(1));
        partialUpdatedGrade.setEndDate(today.plusDays(30));

        restGradeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGrade.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGrade))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.startdateinpast"));

        assertThat(getPersistedGrade(grade).getStartDate()).isEqualTo(today.plusDays(10));
    }

    @Test
    void patchGradeWithUnchangedStartDateInThePastIsAccepted() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        insertedGrade = gradeRepository.save(grade.startDate(today.minusDays(30)).endDate(today.plusDays(10)));

        Grade partialUpdatedGrade = new Grade();
        partialUpdatedGrade.setId(grade.getId());
        partialUpdatedGrade.setEndDate(today.plusDays(20));

        restGradeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGrade.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGrade))
            )
            .andExpect(status().isOk());

        assertThat(getPersistedGrade(grade).getStartDate()).isEqualTo(today.minusDays(30));
        assertThat(getPersistedGrade(grade).getEndDate()).isEqualTo(today.plusDays(20));
    }

    @Test
    void patchGradeWithInactiveModalityReturnsBadRequest() throws Exception {
        Modality inactiveModality = ModalityResourceIT.createEntity();
        inactiveModality.setIsActive(false);
        inactiveModality = modalityRepository.save(inactiveModality);

        try {
            // A PENDIENTE ficha accepts changing the modality, so the catalog rule is reached.
            LocalDate today = LocalDate.now(ZoneId.systemDefault());
            persistGrade(StateGrade.PENDIENTE, today.plusDays(10), today.plusDays(40));

            Grade partialUpdatedGrade = new Grade();
            partialUpdatedGrade.setId(grade.getId());
            partialUpdatedGrade.setModality(new Modality().id(inactiveModality.getId()));

            restGradeMockMvc
                .perform(
                    patch(ENTITY_API_URL_ID, partialUpdatedGrade.getId())
                        .contentType("application/merge-patch+json")
                        .content(om.writeValueAsBytes(partialUpdatedGrade))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error.modalityInactive"));

            assertThat(getPersistedGrade(grade).getCode()).isEqualTo(DEFAULT_CODE);
        } finally {
            modalityRepository.delete(inactiveModality);
        }
    }

    @Test
    void patchGradeWithInactiveTimeSlotReturnsBadRequest() throws Exception {
        TimeSlot inactiveTimeSlot = TimeSlotResourceIT.createEntity();
        inactiveTimeSlot.setIsActive(false);
        inactiveTimeSlot = timeSlotRepository.save(inactiveTimeSlot);

        try {
            // A PENDIENTE ficha accepts changing the time slot, so the catalog rule is reached.
            LocalDate today = LocalDate.now(ZoneId.systemDefault());
            persistGrade(StateGrade.PENDIENTE, today.plusDays(10), today.plusDays(40));

            Grade partialUpdatedGrade = new Grade();
            partialUpdatedGrade.setId(grade.getId());
            partialUpdatedGrade.setTimeSlot(new TimeSlot().id(inactiveTimeSlot.getId()));

            restGradeMockMvc
                .perform(
                    patch(ENTITY_API_URL_ID, partialUpdatedGrade.getId())
                        .contentType("application/merge-patch+json")
                        .content(om.writeValueAsBytes(partialUpdatedGrade))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error.timeSlotInactive"));

            assertThat(getPersistedGrade(grade).getCode()).isEqualTo(DEFAULT_CODE);
        } finally {
            timeSlotRepository.delete(inactiveTimeSlot);
        }
    }

    @Test
    void patchFinalizadaGradeRejectsAnyChange() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        persistGrade(StateGrade.FINALIZADA, today.minusDays(40), today.minusDays(10));

        Grade partialUpdatedGrade = new Grade();
        partialUpdatedGrade.setId(grade.getId());
        partialUpdatedGrade.setEndDate(today.minusDays(5));

        restGradeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGrade.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGrade))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.noteditable"));

        assertThat(getPersistedGrade(grade).getEndDate()).isEqualTo(today.minusDays(10));
    }

    @Test
    void patchActivaGradeRejectsStartDateChange() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        persistGrade(StateGrade.ACTIVA, today.minusDays(10), today.plusDays(10));

        Grade partialUpdatedGrade = new Grade();
        partialUpdatedGrade.setId(grade.getId());
        partialUpdatedGrade.setStartDate(today.plusDays(5));

        restGradeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGrade.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGrade))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.fieldlocked"));

        assertThat(getPersistedGrade(grade).getStartDate()).isEqualTo(today.minusDays(10));
    }

    @Test
    void patchActivaGradeAllowsEndDateChange() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        persistGrade(StateGrade.ACTIVA, today.minusDays(10), today.plusDays(10));

        Grade partialUpdatedGrade = new Grade();
        partialUpdatedGrade.setId(grade.getId());
        partialUpdatedGrade.setEndDate(today.plusDays(45));

        restGradeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGrade.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGrade))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value(StateGrade.ACTIVA.toString()))
            .andExpect(jsonPath("$.endDate").value(today.plusDays(45).toString()));

        assertThat(getPersistedGrade(grade).getEndDate()).isEqualTo(today.plusDays(45));
    }

    @Test
    void patchAplazadaGradeRejectsForbiddenChange() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        persistGrade(StateGrade.APLAZADA, today.minusDays(10), today.plusDays(10));

        Grade partialUpdatedGrade = new Grade();
        partialUpdatedGrade.setId(grade.getId());
        partialUpdatedGrade.setTimeSlot(new TimeSlot().id("other-time-slot"));

        restGradeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGrade.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGrade))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.fieldlocked"));

        assertThat(getPersistedGrade(grade).getEndDate()).isEqualTo(today.plusDays(10));
    }

    @Test
    void patchAplazadaGradeAllowsEndDateChange() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        persistGrade(StateGrade.APLAZADA, today.minusDays(10), today.plusDays(10));

        Grade partialUpdatedGrade = new Grade();
        partialUpdatedGrade.setId(grade.getId());
        partialUpdatedGrade.setEndDate(today.plusDays(60));

        restGradeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGrade.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGrade))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value(StateGrade.APLAZADA.toString()));

        assertThat(getPersistedGrade(grade).getEndDate()).isEqualTo(today.plusDays(60));
    }

    @Test
    void patchPendienteGradeAllowsStartDateChange() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        persistGrade(StateGrade.PENDIENTE, today.plusDays(10), today.plusDays(40));

        Grade partialUpdatedGrade = new Grade();
        partialUpdatedGrade.setId(grade.getId());
        partialUpdatedGrade.setStartDate(today.plusDays(5));

        restGradeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGrade.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGrade))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value(StateGrade.PENDIENTE.toString()));

        assertThat(getPersistedGrade(grade).getStartDate()).isEqualTo(today.plusDays(5));
    }

    @Test
    void patchCanceladaGradeAllowsStartDateChange() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        persistGrade(StateGrade.CANCELADA, today.plusDays(10), today.plusDays(40));

        Grade partialUpdatedGrade = new Grade();
        partialUpdatedGrade.setId(grade.getId());
        partialUpdatedGrade.setStartDate(today.plusDays(5));

        restGradeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGrade.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGrade))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value(StateGrade.CANCELADA.toString()));

        assertThat(getPersistedGrade(grade).getStartDate()).isEqualTo(today.plusDays(5));
    }

    @Test
    void patchGradeWithClassSectionsRejectsCodeChange() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        persistGrade(StateGrade.PENDIENTE, today.plusDays(10), today.plusDays(40));
        persistClassSection(insertedGrade);

        Grade partialUpdatedGrade = new Grade();
        partialUpdatedGrade.setId(grade.getId());
        partialUpdatedGrade.setCode(UPDATED_CODE);

        restGradeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGrade.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGrade))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.gradeCodeLocked"));

        assertThat(getPersistedGrade(grade).getCode()).isEqualTo(DEFAULT_CODE);
    }

    @Test
    void patchGradeWithApprenticesRejectsCodeChange() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        persistGrade(StateGrade.PENDIENTE, today.plusDays(10), today.plusDays(40));
        persistApprentice(insertedGrade);

        Grade partialUpdatedGrade = new Grade();
        partialUpdatedGrade.setId(grade.getId());
        partialUpdatedGrade.setCode(UPDATED_CODE);

        restGradeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGrade.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGrade))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.gradeCodeLocked"));

        assertThat(getPersistedGrade(grade).getCode()).isEqualTo(DEFAULT_CODE);
    }

    @Test
    void patchGradeWithoutAssociationsAllowsCodeChange() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        persistGrade(StateGrade.PENDIENTE, today.plusDays(10), today.plusDays(40));

        Grade partialUpdatedGrade = new Grade();
        partialUpdatedGrade.setId(grade.getId());
        partialUpdatedGrade.setCode(UPDATED_CODE);

        restGradeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGrade.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGrade))
            )
            .andExpect(status().isOk());

        assertThat(getPersistedGrade(grade).getCode()).isEqualTo(UPDATED_CODE);
    }

    @Test
    void patchNonExistingGrade() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        grade.setId(UUID.randomUUID().toString());

        // Create the Grade
        GradeDTO gradeDTO = gradeMapper.toDto(grade);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGradeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, gradeDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(gradeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Grade in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchGrade() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        grade.setId(UUID.randomUUID().toString());

        // Create the Grade
        GradeDTO gradeDTO = gradeMapper.toDto(grade);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGradeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(gradeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Grade in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamGrade() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        grade.setId(UUID.randomUUID().toString());

        // Create the Grade
        GradeDTO gradeDTO = gradeMapper.toDto(grade);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGradeMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(gradeDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Grade in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    // -----------------------------------------------------------------
    // Lifecycle actions: postpone / resume / cancel
    // -----------------------------------------------------------------

    @Test
    void postponeGradeFromPendienteSetsAplazada() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        GradeDTO gradeDTO = persistGrade(StateGrade.PENDIENTE, today.plusDays(10), today.plusDays(40));

        performLifecycleAction("postponed", gradeDTO.getId())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value(StateGrade.APLAZADA.toString()));

        assertThat(getPersistedGrade(grade).getState()).isEqualTo(StateGrade.APLAZADA);
    }

    @Test
    void postponeGradeFromActivaSetsAplazada() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        GradeDTO gradeDTO = persistGrade(StateGrade.ACTIVA, today.minusDays(10), today.plusDays(10));

        performLifecycleAction("postponed", gradeDTO.getId())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value(StateGrade.APLAZADA.toString()));

        assertThat(getPersistedGrade(grade).getState()).isEqualTo(StateGrade.APLAZADA);
    }

    @Test
    void postponeGradeFromFinalizadaReturnsBadRequest() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        GradeDTO gradeDTO = persistGrade(StateGrade.FINALIZADA, today.minusDays(40), today.minusDays(10));

        performLifecycleAction("postponed", gradeDTO.getId())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.invalidtransition"));

        assertThat(getPersistedGrade(grade).getState()).isEqualTo(StateGrade.FINALIZADA);
    }

    @Test
    void postponeGradeWithoutIdReturnsBadRequest() throws Exception {
        restGradeMockMvc
            .perform(
                patch(ENTITY_API_URL + "/postponed")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}")
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void postponeNonExistingGradeReturnsBadRequest() throws Exception {
        performLifecycleAction("postponed", UUID.randomUUID().toString())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.idnotfound"));
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.COORDINATOR)
    void postponeGradeAsNonAdminReturnsForbidden() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        GradeDTO gradeDTO = persistGrade(StateGrade.PENDIENTE, today.plusDays(10), today.plusDays(40));

        performLifecycleAction("postponed", gradeDTO.getId()).andExpect(status().isForbidden());

        assertThat(getPersistedGrade(grade).getState()).isEqualTo(StateGrade.PENDIENTE);
    }

    @Test
    void resumeGradeReclassifiesStateFromDates() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        GradeDTO gradeDTO = persistGrade(StateGrade.APLAZADA, today.plusDays(10), today.plusDays(40));

        // Future range: the ficha has not started yet.
        performLifecycleAction("resumed", gradeDTO.getId())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value(StateGrade.PENDIENTE.toString()));
        assertThat(getPersistedGrade(grade).getState()).isEqualTo(StateGrade.PENDIENTE);

        // Current range: the ficha is active today.
        reassignGradeAsAplazada(today.minusDays(10), today.plusDays(10));
        performLifecycleAction("resumed", grade.getId())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value(StateGrade.ACTIVA.toString()));
        assertThat(getPersistedGrade(grade).getState()).isEqualTo(StateGrade.ACTIVA);

        // Past range: the ficha has already finished.
        reassignGradeAsAplazada(today.minusDays(40), today.minusDays(10));
        performLifecycleAction("resumed", grade.getId())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value(StateGrade.FINALIZADA.toString()));
        assertThat(getPersistedGrade(grade).getState()).isEqualTo(StateGrade.FINALIZADA);
    }

    @Test
    void resumeGradeFromActivaReturnsBadRequest() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        GradeDTO gradeDTO = persistGrade(StateGrade.ACTIVA, today.minusDays(10), today.plusDays(10));

        performLifecycleAction("resumed", gradeDTO.getId())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.invalidtransition"));

        assertThat(getPersistedGrade(grade).getState()).isEqualTo(StateGrade.ACTIVA);
    }

    @Test
    void resumeNonExistingGradeReturnsBadRequest() throws Exception {
        performLifecycleAction("resumed", UUID.randomUUID().toString())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.idnotfound"));
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.COORDINATOR)
    void resumeGradeAsNonAdminReturnsForbidden() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        GradeDTO gradeDTO = persistGrade(StateGrade.APLAZADA, today.plusDays(10), today.plusDays(40));

        performLifecycleAction("resumed", gradeDTO.getId()).andExpect(status().isForbidden());

        assertThat(getPersistedGrade(grade).getState()).isEqualTo(StateGrade.APLAZADA);
    }

    @Test
    void cancelGradeFromActivaSetsCancelada() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        GradeDTO gradeDTO = persistGrade(StateGrade.ACTIVA, today.minusDays(10), today.plusDays(10));

        performLifecycleAction("cancelled", gradeDTO.getId())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value(StateGrade.CANCELADA.toString()));

        assertThat(getPersistedGrade(grade).getState()).isEqualTo(StateGrade.CANCELADA);
    }

    @Test
    void cancelGradeFromFinalizadaSetsCancelada() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        GradeDTO gradeDTO = persistGrade(StateGrade.FINALIZADA, today.minusDays(40), today.minusDays(10));

        performLifecycleAction("cancelled", gradeDTO.getId())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value(StateGrade.CANCELADA.toString()));

        assertThat(getPersistedGrade(grade).getState()).isEqualTo(StateGrade.CANCELADA);
    }

    @Test
    void cancelGradeFromCanceladaReturnsBadRequest() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        GradeDTO gradeDTO = persistGrade(StateGrade.CANCELADA, today.minusDays(10), today.plusDays(10));

        performLifecycleAction("cancelled", gradeDTO.getId())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.invalidtransition"));

        assertThat(getPersistedGrade(grade).getState()).isEqualTo(StateGrade.CANCELADA);
    }

    @Test
    void cancelNonExistingGradeReturnsBadRequest() throws Exception {
        performLifecycleAction("cancelled", UUID.randomUUID().toString())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.idnotfound"));
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.COORDINATOR)
    void cancelGradeAsNonAdminReturnsForbidden() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        GradeDTO gradeDTO = persistGrade(StateGrade.PENDIENTE, today.plusDays(10), today.plusDays(40));

        performLifecycleAction("cancelled", gradeDTO.getId()).andExpect(status().isForbidden());

        assertThat(getPersistedGrade(grade).getState()).isEqualTo(StateGrade.PENDIENTE);
    }

    /**
     * Performs one of the ficha lifecycle actions ({@code postponed}, {@code resumed},
     * {@code cancelled}) with the given ficha id in the request body.
     */
    private ResultActions performLifecycleAction(String action, String id) throws Exception {
        var body = om.createObjectNode().put("id", id);
        return restGradeMockMvc.perform(
            patch(ENTITY_API_URL + "/" + action)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(body))
        );
    }

    /**
     * Rewrites the persisted ficha under test as APLAZADA with the given range, so a resume
     * action can be exercised against another date window.
     */
    private void reassignGradeAsAplazada(LocalDate startDate, LocalDate endDate) {
        Grade persisted = getPersistedGrade(grade);
        persisted.setStartDate(startDate);
        persisted.setEndDate(endDate);
        persisted.setState(StateGrade.APLAZADA);
        insertedGrade = gradeRepository.save(persisted);
    }

    @Test
    void deleteGrade() throws Exception {
        // Initialize the database
        insertedGrade = gradeRepository.save(grade);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the grade
        restGradeMockMvc
            .perform(delete(ENTITY_API_URL_ID, grade.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    @Test
    void deleteNonExistingGradeIsASilentNoOp() throws Exception {
        long databaseSizeBeforeDelete = getRepositoryCount();

        restGradeMockMvc
            .perform(delete(ENTITY_API_URL_ID, UUID.randomUUID().toString()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        assertSameRepositoryCount(databaseSizeBeforeDelete);
    }

    // -----------------------------------------------------------------
    // Delete guard and cascade
    // -----------------------------------------------------------------

    @Test
    void deleteGradeWithApprenticesReturnsBadRequest() throws Exception {
        insertedGrade = gradeRepository.save(grade);
        persistApprentice(insertedGrade);

        long databaseSizeBeforeDelete = getRepositoryCount();

        restGradeMockMvc
            .perform(delete(ENTITY_API_URL_ID, grade.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.gradeInUse"));

        assertSameRepositoryCount(databaseSizeBeforeDelete);
        assertThat(gradeRepository.existsById(grade.getId())).isTrue();
    }

    @Test
    void deleteGradeWithAttendanceReturnsBadRequest() throws Exception {
        insertedGrade = gradeRepository.save(grade);
        ClassSection classSection = persistClassSection(insertedGrade);
        insertedAttendance = attendanceRepository.save(
            new Attendance()
                .date(LocalDate.now(ZoneId.systemDefault()))
                .stateAttendance(StateAttendance.PRESENTE)
                .classSection(classSection)
        );

        long databaseSizeBeforeDelete = getRepositoryCount();

        restGradeMockMvc
            .perform(delete(ENTITY_API_URL_ID, grade.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.gradeInUse"));

        assertSameRepositoryCount(databaseSizeBeforeDelete);
        assertThat(gradeRepository.existsById(grade.getId())).isTrue();
        assertThat(attendanceRepository.existsById(insertedAttendance.getId())).isTrue();
    }

    @Test
    void deleteGradeCascadesClassSectionsSchedulesAndExceptions() throws Exception {
        insertedGrade = gradeRepository.save(grade);
        ClassSection classSection = persistClassSection(insertedGrade);
        insertedSchedule = classScheduleRepository.save(
            new ClassSchedule()
                .dayOfWeek(DayOfWeek.LUNES)
                .startTime(LocalTime.of(7, 0))
                .endTime(LocalTime.of(9, 0))
                .classSection(classSection)
        );
        insertedException = classExceptionRepository.save(
            new ClassException().date(LocalDate.now(ZoneId.systemDefault())).reason("Test exception").classSection(classSection)
        );

        long databaseSizeBeforeDelete = getRepositoryCount();
        long classSectionsBeforeDelete = classSectionRepository.count();
        long schedulesBeforeDelete = classScheduleRepository.count();
        long exceptionsBeforeDelete = classExceptionRepository.count();

        restGradeMockMvc
            .perform(delete(ENTITY_API_URL_ID, grade.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        assertThat(gradeRepository.existsById(grade.getId())).isFalse();
        assertThat(classSectionRepository.count()).isEqualTo(classSectionsBeforeDelete - 1);
        assertThat(classScheduleRepository.count()).isEqualTo(schedulesBeforeDelete - 1);
        assertThat(classExceptionRepository.count()).isEqualTo(exceptionsBeforeDelete - 1);
        assertThat(classSectionRepository.findById(classSection.getId())).isEmpty();
        assertThat(classScheduleRepository.findById(insertedSchedule.getId())).isEmpty();
        assertThat(classExceptionRepository.findById(insertedException.getId())).isEmpty();
    }

    protected long getRepositoryCount() {
        return gradeRepository.count();
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

    protected Grade getPersistedGrade(Grade grade) {
        return gradeRepository.findById(grade.getId()).orElseThrow();
    }

    protected void assertPersistedGradeToMatchAllProperties(Grade expectedGrade) {
        assertGradeAllPropertiesEquals(expectedGrade, getPersistedGrade(expectedGrade));
    }

    protected void assertPersistedGradeToMatchUpdatableProperties(Grade expectedGrade) {
        assertGradeAllUpdatablePropertiesEquals(expectedGrade, getPersistedGrade(expectedGrade));
    }
}
