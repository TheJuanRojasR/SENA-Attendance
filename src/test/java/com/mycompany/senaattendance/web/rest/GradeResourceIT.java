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
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.Modality;
import com.mycompany.senaattendance.domain.Program;
import com.mycompany.senaattendance.domain.TimeSlot;
import com.mycompany.senaattendance.domain.enumeration.StateGrade;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.repository.ModalityRepository;
import com.mycompany.senaattendance.repository.ProgramRepository;
import com.mycompany.senaattendance.repository.TimeSlotRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.GradeService;
import com.mycompany.senaattendance.service.dto.GradeDTO;
import com.mycompany.senaattendance.service.dto.ProgramDTO;
import com.mycompany.senaattendance.service.mapper.GradeMapper;
import java.time.LocalDate;
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

    @Mock
    private GradeService gradeServiceMock;

    @Autowired
    private MockMvc restGradeMockMvc;

    private Grade grade;

    private Grade insertedGrade;

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
        if (insertedGrade != null) {
            gradeRepository.delete(insertedGrade);
            insertedGrade = null;
        }
        // Remove the related documents persisted for the PUT tests
        modalityRepository.deleteAll();
        programRepository.deleteAll();
        timeSlotRepository.deleteAll();
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

        // Initialize the database with a manually paused ficha, so the update must preserve that state
        insertedGrade = gradeRepository.save(grade.state(UPDATED_STATE));

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the grade
        Grade updatedGrade = gradeRepository.findById(grade.getId()).orElseThrow();
        updatedGrade.code(UPDATED_CODE).state(UPDATED_STATE).startDate(UPDATED_START_DATE).endDate(UPDATED_END_DATE);
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
            GradeDTO gradeDTO = gradeMapper.toDto(other);
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
        // Persist the @DBRef targets so they resolve on reload
        programRepository.save(grade.getProgram());
        modalityRepository.save(grade.getModality());
        timeSlotRepository.save(grade.getTimeSlot());
        insertedGrade = gradeRepository.save(grade);

        GradeDTO gradeDTO = gradeMapper.toDto(gradeRepository.findById(grade.getId()).orElseThrow());
        gradeDTO.setStartDate(DEFAULT_START_DATE.plusDays(10));
        gradeDTO.setEndDate(DEFAULT_START_DATE.plusDays(5));

        restGradeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, gradeDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.datesorder"));

        assertThat(getPersistedGrade(grade).getStartDate()).isEqualTo(DEFAULT_START_DATE);
    }

    @Test
    void putGradeWithChangedStartDateInThePastReturnsBadRequest() throws Exception {
        // Persist the @DBRef targets so they resolve on reload
        programRepository.save(grade.getProgram());
        modalityRepository.save(grade.getModality());
        timeSlotRepository.save(grade.getTimeSlot());
        insertedGrade = gradeRepository.save(grade);

        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        GradeDTO gradeDTO = gradeMapper.toDto(gradeRepository.findById(grade.getId()).orElseThrow());
        gradeDTO.setStartDate(today.minusDays(1));
        gradeDTO.setEndDate(today.plusDays(30));

        restGradeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, gradeDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(gradeDTO))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.startdateinpast"));

        assertThat(getPersistedGrade(grade).getStartDate()).isEqualTo(DEFAULT_START_DATE);
    }

    @Test
    void putGradeWithInactiveModalityReturnsBadRequest() throws Exception {
        Modality inactiveModality = ModalityResourceIT.createEntity();
        inactiveModality.setIsActive(false);
        inactiveModality = modalityRepository.save(inactiveModality);

        try {
            // Persist the @DBRef targets so they resolve on reload
            programRepository.save(grade.getProgram());
            modalityRepository.save(grade.getModality());
            timeSlotRepository.save(grade.getTimeSlot());
            insertedGrade = gradeRepository.save(grade);

            GradeDTO gradeDTO = gradeMapper.toDto(gradeRepository.findById(grade.getId()).orElseThrow());
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
            // Persist the @DBRef targets so they resolve on reload
            programRepository.save(grade.getProgram());
            modalityRepository.save(grade.getModality());
            timeSlotRepository.save(grade.getTimeSlot());
            insertedGrade = gradeRepository.save(grade);

            GradeDTO gradeDTO = gradeMapper.toDto(gradeRepository.findById(grade.getId()).orElseThrow());
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
        // Initialize the database with a manually paused ficha, so the patch must preserve that state
        insertedGrade = gradeRepository.save(grade.state(UPDATED_STATE));

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the grade using partial update
        Grade partialUpdatedGrade = new Grade();
        partialUpdatedGrade.setId(grade.getId());

        partialUpdatedGrade.code(UPDATED_CODE).state(UPDATED_STATE).startDate(UPDATED_START_DATE).endDate(UPDATED_END_DATE);

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
        insertedGrade = gradeRepository.save(grade);

        Grade partialUpdatedGrade = new Grade();
        partialUpdatedGrade.setId(grade.getId());
        partialUpdatedGrade.setStartDate(DEFAULT_START_DATE.plusDays(10));
        partialUpdatedGrade.setEndDate(DEFAULT_START_DATE.plusDays(5));

        restGradeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGrade.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGrade))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.datesorder"));

        assertThat(getPersistedGrade(grade).getStartDate()).isEqualTo(DEFAULT_START_DATE);
    }

    @Test
    void patchGradeWithChangedStartDateInThePastReturnsBadRequest() throws Exception {
        insertedGrade = gradeRepository.save(grade);

        LocalDate today = LocalDate.now(ZoneId.systemDefault());
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

        assertThat(getPersistedGrade(grade).getStartDate()).isEqualTo(DEFAULT_START_DATE);
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
            insertedGrade = gradeRepository.save(grade);

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
            insertedGrade = gradeRepository.save(grade);

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
