package com.mycompany.senaattendance.web.rest;

import static com.mycompany.senaattendance.domain.JustificationAsserts.*;
import static com.mycompany.senaattendance.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.Apprentice;
import com.mycompany.senaattendance.domain.Attendance;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.GlobalConfiguration;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.Justification;
import com.mycompany.senaattendance.domain.JustificationDetails;
import com.mycompany.senaattendance.domain.JustificationType;
import com.mycompany.senaattendance.domain.Modality;
import com.mycompany.senaattendance.domain.Program;
import com.mycompany.senaattendance.domain.TimeSlot;
import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.StateAcademic;
import com.mycompany.senaattendance.domain.enumeration.StateAttendance;
import com.mycompany.senaattendance.domain.enumeration.StateJustification;
import com.mycompany.senaattendance.domain.enumeration.StateTrimester;
import com.mycompany.senaattendance.repository.ApprenticeRepository;
import com.mycompany.senaattendance.repository.AttendanceRepository;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.GlobalConfigurationRepository;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.repository.JustificationDetailsRepository;
import com.mycompany.senaattendance.repository.JustificationRepository;
import com.mycompany.senaattendance.repository.JustificationTypeRepository;
import com.mycompany.senaattendance.repository.ModalityRepository;
import com.mycompany.senaattendance.repository.ProgramRepository;
import com.mycompany.senaattendance.repository.TimeSlotRepository;
import com.mycompany.senaattendance.repository.TrimesterRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.JustificationService;
import com.mycompany.senaattendance.service.dto.JustificationDTO;
import com.mycompany.senaattendance.service.mapper.JustificationMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * Integration tests for the {@link JustificationResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class JustificationResourceIT {

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_START_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_START_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final LocalDate DEFAULT_END_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_END_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final byte[] DEFAULT_EVIDENCE = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_EVIDENCE = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_EVIDENCE_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_EVIDENCE_CONTENT_TYPE = "image/png";

    private static final String ENTITY_API_URL = "/api/justifications";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final String APPRENTICE_LOGIN = "justifications_apprentice";
    private static final String OTHER_APPRENTICE_LOGIN = "other_justifications_apprentice";
    private static final String RULES_APPRENTICE_LOGIN = "justification_rules_apprentice";
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private Clock clock;

    @Autowired
    private JustificationRepository justificationRepository;

    @Autowired
    private JustificationDetailsRepository justificationDetailsRepository;

    @Autowired
    private JustificationTypeRepository justificationTypeRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private ApprenticeRepository apprenticeRepository;

    @Autowired
    private ClassSectionRepository classSectionRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private GlobalConfigurationRepository globalConfigurationRepository;

    @Autowired
    private ModalityRepository modalityRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private TrimesterRepository trimesterRepository;

    @Mock
    private JustificationRepository justificationRepositoryMock;

    @Autowired
    private JustificationMapper justificationMapper;

    @Mock
    private JustificationService justificationServiceMock;

    @Autowired
    private MockMvc restJustificationMockMvc;

    private Justification justification;

    private Justification insertedJustification;

    private UserProfile rulesApprentice;

    private ClassSection rulesClassSection;

    private JustificationType rulesJustificationType;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Justification createEntity() {
        Justification justification = new Justification()
            .description(DEFAULT_DESCRIPTION)
            .startDate(DEFAULT_START_DATE)
            .endDate(DEFAULT_END_DATE)
            .evidence(DEFAULT_EVIDENCE)
            .evidenceContentType(DEFAULT_EVIDENCE_CONTENT_TYPE);
        // Add required entity
        JustificationType justificationType;
        justificationType = JustificationTypeResourceIT.createEntity();
        justificationType.setId("fixed-id-for-tests");
        justification.setJustificationType(justificationType);
        // Add required entity
        UserProfile userProfile;
        userProfile = UserProfileResourceIT.createEntity();
        userProfile.setId("fixed-id-for-tests");
        justification.setStudent(userProfile);
        return justification;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Justification createUpdatedEntity() {
        Justification updatedJustification = new Justification()
            .description(UPDATED_DESCRIPTION)
            .startDate(UPDATED_START_DATE)
            .endDate(UPDATED_END_DATE)
            .evidence(UPDATED_EVIDENCE)
            .evidenceContentType(UPDATED_EVIDENCE_CONTENT_TYPE);
        // Add required entity
        JustificationType justificationType;
        justificationType = JustificationTypeResourceIT.createUpdatedEntity();
        justificationType.setId("fixed-id-for-tests");
        updatedJustification.setJustificationType(justificationType);
        // Add required entity
        UserProfile userProfile;
        userProfile = UserProfileResourceIT.createUpdatedEntity();
        userProfile.setId("fixed-id-for-tests");
        updatedJustification.setStudent(userProfile);
        return updatedJustification;
    }

    @BeforeEach
    void initTest() {
        justification = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedJustification != null) {
            justificationRepository.delete(insertedJustification);
            insertedJustification = null;
        }
        // Remove the related documents persisted for the PUT tests and the scoping tests
        justificationRepository.deleteAll();
        justificationDetailsRepository.deleteAll();
        justificationTypeRepository.deleteAll();
        attendanceRepository.deleteAll();
        apprenticeRepository.deleteAll();
        classSectionRepository.deleteAll();
        gradeRepository.deleteAll();
        globalConfigurationRepository.deleteAll();
        modalityRepository.deleteAll();
        programRepository.deleteAll();
        timeSlotRepository.deleteAll();
        trimesterRepository.deleteAll();
        userProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createJustification() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Justification
        JustificationDTO justificationDTO = justificationMapper.toDto(justification);
        var returnedJustificationDTO = om.readValue(
            restJustificationMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            JustificationDTO.class
        );

        // Validate the Justification in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedJustification = justificationMapper.toEntity(returnedJustificationDTO);
        assertJustificationUpdatableFieldsEquals(returnedJustification, getPersistedJustification(returnedJustification));

        insertedJustification = returnedJustification;
    }

    @Test
    void createJustificationWithExistingId() throws Exception {
        // Create the Justification with an existing ID
        justification.setId("existing_id");
        JustificationDTO justificationDTO = justificationMapper.toDto(justification);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restJustificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Justification in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkDescriptionIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        justification.setDescription(null);

        // Create the Justification, which fails.
        JustificationDTO justificationDTO = justificationMapper.toDto(justification);

        restJustificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkStartDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        justification.setStartDate(null);

        // Create the Justification, which fails.
        JustificationDTO justificationDTO = justificationMapper.toDto(justification);

        restJustificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkEvidenceContentTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        justification.setEvidenceContentType(null);

        // Create the Justification, which fails.
        JustificationDTO justificationDTO = justificationMapper.toDto(justification);

        restJustificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkEndDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        justification.setEndDate(null);

        // Create the Justification, which fails.
        JustificationDTO justificationDTO = justificationMapper.toDto(justification);

        restJustificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllJustifications() throws Exception {
        // Initialize the database
        insertedJustification = justificationRepository.save(justification);

        // Get all the justificationList
        restJustificationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(justification.getId())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].startDate").value(hasItem(DEFAULT_START_DATE.toString())))
            .andExpect(jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE.toString())))
            .andExpect(jsonPath("$.[*].evidenceContentType").value(hasItem(DEFAULT_EVIDENCE_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].evidence").value(hasItem(Base64.getEncoder().encodeToString(DEFAULT_EVIDENCE))));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllJustificationsWithEagerRelationshipsIsEnabled() throws Exception {
        when(justificationServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restJustificationMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(justificationServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllJustificationsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(justificationServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restJustificationMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(justificationRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void getJustification() throws Exception {
        // Initialize the database
        insertedJustification = justificationRepository.save(justification);

        // Get the justification
        restJustificationMockMvc
            .perform(get(ENTITY_API_URL_ID, justification.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(justification.getId()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.startDate").value(DEFAULT_START_DATE.toString()))
            .andExpect(jsonPath("$.endDate").value(DEFAULT_END_DATE.toString()))
            .andExpect(jsonPath("$.evidenceContentType").value(DEFAULT_EVIDENCE_CONTENT_TYPE))
            .andExpect(jsonPath("$.evidence").value(Base64.getEncoder().encodeToString(DEFAULT_EVIDENCE)));
    }

    @Test
    void getNonExistingJustification() throws Exception {
        // Get the justification
        restJustificationMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    // -----------------------------------------------------------------
    // UC011 — An apprentice only reaches their own justifications
    // -----------------------------------------------------------------

    @Test
    @WithMockUser(username = APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void getAllJustificationsAsApprenticeReturnsOnlyOwnRecords() throws Exception {
        UserProfile apprentice = persistApprentice(APPRENTICE_LOGIN);
        UserProfile otherApprentice = persistApprentice(OTHER_APPRENTICE_LOGIN);
        JustificationType justificationType = persistJustificationType();
        Justification ownJustification = persistJustification(apprentice, justificationType);
        persistJustification(otherApprentice, justificationType);

        restJustificationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "1"))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(ownJustification.getId()));
    }

    @Test
    @WithMockUser(username = APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void getOwnJustificationAsApprenticeReturnsOk() throws Exception {
        UserProfile apprentice = persistApprentice(APPRENTICE_LOGIN);
        Justification justification = persistJustification(apprentice, persistJustificationType());

        restJustificationMockMvc
            .perform(get(ENTITY_API_URL_ID, justification.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(justification.getId()));
    }

    @Test
    @WithMockUser(username = APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void getJustificationOfAnotherApprenticeReturnsNotFound() throws Exception {
        persistApprentice(APPRENTICE_LOGIN);
        UserProfile otherApprentice = persistApprentice(OTHER_APPRENTICE_LOGIN);
        Justification otherJustification = persistJustification(otherApprentice, persistJustificationType());

        restJustificationMockMvc.perform(get(ENTITY_API_URL_ID, otherJustification.getId())).andExpect(status().isNotFound());
    }

    @Test
    void getJustificationOfAnotherApprenticeAsAdminReadsIt() throws Exception {
        UserProfile otherApprentice = persistApprentice(OTHER_APPRENTICE_LOGIN);
        Justification otherJustification = persistJustification(otherApprentice, persistJustificationType());

        restJustificationMockMvc
            .perform(get(ENTITY_API_URL_ID, otherJustification.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(otherJustification.getId()));
    }

    @Test
    @WithMockUser(username = APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void createJustificationAsApprenticeForOwnProfileIsCreated() throws Exception {
        UserProfile apprentice = persistApprentice(APPRENTICE_LOGIN);
        JustificationDTO justificationDTO = justificationMapper.toDto(justificationFor(apprentice, persistJustificationType()));

        restJustificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.student.id").value(apprentice.getId()));

        assertThat(getRepositoryCount()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void createJustificationAsApprenticeForAnotherStudentReturnsBadRequest() throws Exception {
        persistApprentice(APPRENTICE_LOGIN);
        UserProfile otherApprentice = persistApprentice(OTHER_APPRENTICE_LOGIN);
        long databaseSizeBeforeCreate = getRepositoryCount();
        JustificationDTO justificationDTO = justificationMapper.toDto(justificationFor(otherApprentice, persistJustificationType()));

        restJustificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.notYourJustification"));

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @WithMockUser(username = APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void updateOwnJustificationAsApprenticeSucceeds() throws Exception {
        UserProfile apprentice = persistApprentice(APPRENTICE_LOGIN);
        Justification justification = persistJustification(apprentice, persistJustificationType());

        Justification updatedJustification = justificationRepository.findById(justification.getId()).orElseThrow();
        updatedJustification.setDescription(UPDATED_DESCRIPTION);

        restJustificationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedJustification.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(justificationMapper.toDto(updatedJustification)))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value(UPDATED_DESCRIPTION));
    }

    @Test
    @WithMockUser(username = APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void updateJustificationOfAnotherApprenticeReturnsBadRequest() throws Exception {
        UserProfile apprentice = persistApprentice(APPRENTICE_LOGIN);
        UserProfile otherApprentice = persistApprentice(OTHER_APPRENTICE_LOGIN);
        JustificationType justificationType = persistJustificationType();
        persistJustification(apprentice, justificationType);
        Justification otherJustification = persistJustification(otherApprentice, justificationType);

        // The payload claims the current apprentice as student, but the persisted row is not theirs.
        JustificationDTO justificationDTO = justificationMapper.toDto(justificationFor(apprentice, justificationType));
        justificationDTO.setId(otherJustification.getId());

        restJustificationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, otherJustification.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(justificationDTO))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.notYourJustification"));

        assertThat(justificationRepository.findById(otherJustification.getId()).orElseThrow().getStudent().getId()).isEqualTo(
            otherApprentice.getId()
        );
    }

    @Test
    @WithMockUser(username = APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void patchJustificationOfAnotherApprenticeReturnsBadRequest() throws Exception {
        persistApprentice(APPRENTICE_LOGIN);
        UserProfile otherApprentice = persistApprentice(OTHER_APPRENTICE_LOGIN);
        Justification otherJustification = persistJustification(otherApprentice, persistJustificationType());

        Justification partialUpdatedJustification = new Justification();
        partialUpdatedJustification.setId(otherJustification.getId());
        partialUpdatedJustification.setDescription(UPDATED_DESCRIPTION);

        restJustificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, otherJustification.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedJustification))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.notYourJustification"));
    }

    @Test
    @WithMockUser(username = APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void deleteJustificationOfAnotherApprenticeReturnsBadRequest() throws Exception {
        persistApprentice(APPRENTICE_LOGIN);
        UserProfile otherApprentice = persistApprentice(OTHER_APPRENTICE_LOGIN);
        Justification otherJustification = persistJustification(otherApprentice, persistJustificationType());

        restJustificationMockMvc
            .perform(delete(ENTITY_API_URL_ID, otherJustification.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.notYourJustification"));

        assertThat(justificationRepository.existsById(otherJustification.getId())).isTrue();
    }

    @Test
    @WithMockUser(username = "justifications_instructor", authorities = AuthoritiesConstants.INSTRUCTOR)
    void getJustificationsAsInstructorReturnsForbidden() throws Exception {
        restJustificationMockMvc.perform(get(ENTITY_API_URL).accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
        restJustificationMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isForbidden());
    }

    @Test
    void putExistingJustification() throws Exception {
        // Persist the @DBRef targets so they resolve on reload
        justificationTypeRepository.save(justification.getJustificationType());
        userProfileRepository.save(justification.getStudent());

        // Initialize the database
        insertedJustification = justificationRepository.save(justification);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the justification
        Justification updatedJustification = justificationRepository.findById(justification.getId()).orElseThrow();
        updatedJustification
            .description(UPDATED_DESCRIPTION)
            .startDate(UPDATED_START_DATE)
            .endDate(UPDATED_END_DATE)
            .evidence(UPDATED_EVIDENCE)
            .evidenceContentType(UPDATED_EVIDENCE_CONTENT_TYPE);
        JustificationDTO justificationDTO = justificationMapper.toDto(updatedJustification);

        restJustificationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, justificationDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(justificationDTO))
            )
            .andExpect(status().isOk());

        // Validate the Justification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedJustificationToMatchAllProperties(updatedJustification);
    }

    @Test
    void putNonExistingJustification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        justification.setId(UUID.randomUUID().toString());

        // Create the Justification
        JustificationDTO justificationDTO = justificationMapper.toDto(justification);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restJustificationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, justificationDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(justificationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Justification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchJustification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        justification.setId(UUID.randomUUID().toString());

        // Create the Justification
        JustificationDTO justificationDTO = justificationMapper.toDto(justification);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restJustificationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(justificationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Justification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamJustification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        justification.setId(UUID.randomUUID().toString());

        // Create the Justification
        JustificationDTO justificationDTO = justificationMapper.toDto(justification);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restJustificationMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Justification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateJustificationWithPatch() throws Exception {
        // Initialize the database
        insertedJustification = justificationRepository.save(justification);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the justification using partial update
        Justification partialUpdatedJustification = new Justification();
        partialUpdatedJustification.setId(justification.getId());

        partialUpdatedJustification.description(UPDATED_DESCRIPTION).endDate(UPDATED_END_DATE);

        restJustificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedJustification.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedJustification))
            )
            .andExpect(status().isOk());

        // Validate the Justification in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertJustificationUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedJustification, justification),
            getPersistedJustification(justification)
        );
    }

    @Test
    void fullUpdateJustificationWithPatch() throws Exception {
        // Initialize the database
        insertedJustification = justificationRepository.save(justification);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the justification using partial update
        Justification partialUpdatedJustification = new Justification();
        partialUpdatedJustification.setId(justification.getId());

        partialUpdatedJustification
            .description(UPDATED_DESCRIPTION)
            .startDate(UPDATED_START_DATE)
            .endDate(UPDATED_END_DATE)
            .evidence(UPDATED_EVIDENCE)
            .evidenceContentType(UPDATED_EVIDENCE_CONTENT_TYPE);

        restJustificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedJustification.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedJustification))
            )
            .andExpect(status().isOk());

        // Validate the Justification in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertJustificationUpdatableFieldsEquals(partialUpdatedJustification, getPersistedJustification(partialUpdatedJustification));
    }

    @Test
    void patchNonExistingJustification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        justification.setId(UUID.randomUUID().toString());

        // Create the Justification
        JustificationDTO justificationDTO = justificationMapper.toDto(justification);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restJustificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, justificationDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(justificationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Justification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchJustification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        justification.setId(UUID.randomUUID().toString());

        // Create the Justification
        JustificationDTO justificationDTO = justificationMapper.toDto(justification);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restJustificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(justificationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Justification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamJustification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        justification.setId(UUID.randomUUID().toString());

        // Create the Justification
        JustificationDTO justificationDTO = justificationMapper.toDto(justification);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restJustificationMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(justificationDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Justification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteJustification() throws Exception {
        // Initialize the database
        insertedJustification = justificationRepository.save(justification);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the justification
        restJustificationMockMvc
            .perform(delete(ENTITY_API_URL_ID, justification.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    // -----------------------------------------------------------------
    // UC011 — Deadline mark (use-cases.md:999)
    // -----------------------------------------------------------------

    @Test
    @WithMockUser(username = RULES_APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void createJustificationWithinDeadlineIsOnTime() throws Exception {
        LocalDate today = LocalDate.now(clock);
        LocalDate firstFailure = today.minusDays(20);
        LocalDate lastFailure = today.minusDays(1);
        persistRulesFixture(5, 3, firstFailure, lastFailure);

        // The mark is counted from the last failure of the range, not from the first one.
        postJustification(justificationPayload(firstFailure, lastFailure, rulesClassSection.getId()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.onTime").value(true));

        assertThat(justificationDetailsRepository.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = RULES_APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void createJustificationAfterDeadlineIsOutOfTime() throws Exception {
        LocalDate today = LocalDate.now(clock);
        LocalDate failure = today.minusDays(10);
        persistRulesFixture(5, 3, failure);

        // The deadline does not block the submission: the justification is created out of time.
        postJustification(justificationPayload(failure, failure, rulesClassSection.getId()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.onTime").value(false));

        assertThat(justificationDetailsRepository.count()).isEqualTo(1);
    }

    // -----------------------------------------------------------------
    // UC011 — Per-type quota (use-cases.md:1000, E6)
    // -----------------------------------------------------------------

    @Test
    @WithMockUser(username = RULES_APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void createJustificationWithinQuotaCreatesIt() throws Exception {
        LocalDate today = LocalDate.now(clock);
        LocalDate firstFailure = today.minusDays(20);
        persistRulesFixture(2, 5, firstFailure);

        postJustification(justificationPayload(firstFailure, firstFailure, rulesClassSection.getId())).andExpect(status().isCreated());

        assertThat(justificationDetailsRepository.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = RULES_APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void createJustificationUpToTheQuotaLimitCreatesIt() throws Exception {
        LocalDate today = LocalDate.now(clock);
        LocalDate firstFailure = today.minusDays(20);
        LocalDate secondFailure = today.minusDays(13);
        persistRulesFixture(2, 5, firstFailure, secondFailure);

        postJustification(justificationPayload(firstFailure, firstFailure, rulesClassSection.getId())).andExpect(status().isCreated());

        // The second day reaches the limit without exceeding it.
        postJustification(justificationPayload(secondFailure, secondFailure, rulesClassSection.getId())).andExpect(status().isCreated());

        assertThat(justificationDetailsRepository.count()).isEqualTo(2);
    }

    @Test
    @WithMockUser(username = RULES_APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void createJustificationAboveTheQuotaReturnsQuotaExceeded() throws Exception {
        LocalDate today = LocalDate.now(clock);
        LocalDate firstFailure = today.minusDays(20);
        LocalDate secondFailure = today.minusDays(13);
        LocalDate thirdFailure = today.minusDays(6);
        persistRulesFixture(2, 5, firstFailure, secondFailure, thirdFailure);

        postJustification(justificationPayload(firstFailure, firstFailure, rulesClassSection.getId())).andExpect(status().isCreated());

        // One day is already covered, so only one of the two new days fits the quota.
        postJustification(justificationPayload(secondFailure, thirdFailure, rulesClassSection.getId()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.quotaExceeded"))
            .andExpect(jsonPath("$.detail").value(containsString("Te quedan 1 días disponibles")));

        assertThat(justificationRepository.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = RULES_APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void createJustificationWithARejectedPartReleasesQuota() throws Exception {
        LocalDate today = LocalDate.now(clock);
        LocalDate firstFailure = today.minusDays(20);
        LocalDate secondFailure = today.minusDays(13);
        persistRulesFixture(2, 5, firstFailure, secondFailure);
        persistRejectedJustificationCovering(firstFailure);

        postJustification(justificationPayload(firstFailure, secondFailure, rulesClassSection.getId())).andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = RULES_APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void createJustificationWithARepeatedDateDoesNotCountItTwice() throws Exception {
        LocalDate today = LocalDate.now(clock);
        LocalDate firstFailure = today.minusDays(20);
        LocalDate secondFailure = today.minusDays(13);
        persistRulesFixture(2, 5, firstFailure, secondFailure);

        postJustification(justificationPayload(firstFailure, secondFailure, rulesClassSection.getId())).andExpect(status().isCreated());

        // Repeating an already covered date does not consume the quota twice.
        postJustification(justificationPayload(firstFailure, firstFailure, rulesClassSection.getId())).andExpect(status().isCreated());
    }

    /**
     * Persists an apprentice with a resolvable login, so the service can resolve their profile
     * from the security context.
     */
    private UserProfile persistApprentice(String login) {
        User user = UserResourceIT.createEntity();
        user.setLogin(login);
        user.setEmail(login + "@example.com");
        user.setActivated(true);
        user = userRepository.save(user);

        UserProfile profile = UserProfileResourceIT.createEntity();
        profile.setDocumentNumber("J" + UUID.randomUUID().toString().replace("-", "").substring(0, 13));
        profile.setUser(user);
        return userProfileRepository.save(profile);
    }

    private JustificationType persistJustificationType() {
        return justificationTypeRepository.save(JustificationTypeResourceIT.createEntity());
    }

    /**
     * Builds a justification of the given apprentice, not persisted, so it can be sent as payload.
     */
    private static Justification justificationFor(UserProfile student, JustificationType justificationType) {
        return new Justification()
            .description(DEFAULT_DESCRIPTION)
            .startDate(DEFAULT_START_DATE)
            .endDate(DEFAULT_END_DATE)
            .evidence(DEFAULT_EVIDENCE)
            .evidenceContentType(DEFAULT_EVIDENCE_CONTENT_TYPE)
            .justificationType(justificationType)
            .student(student);
    }

    private Justification persistJustification(UserProfile student, JustificationType justificationType) {
        return justificationRepository.save(justificationFor(student, justificationType));
    }

    protected long getRepositoryCount() {
        return justificationRepository.count();
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

    protected Justification getPersistedJustification(Justification justification) {
        return justificationRepository.findById(justification.getId()).orElseThrow();
    }

    protected void assertPersistedJustificationToMatchAllProperties(Justification expectedJustification) {
        assertJustificationAllPropertiesEquals(expectedJustification, getPersistedJustification(expectedJustification));
    }

    protected void assertPersistedJustificationToMatchUpdatableProperties(Justification expectedJustification) {
        assertJustificationAllUpdatablePropertiesEquals(expectedJustification, getPersistedJustification(expectedJustification));
    }

    // -----------------------------------------------------------------
    // Fixture helpers for the UC011 creation rules
    // -----------------------------------------------------------------

    /**
     * Persists the graph the creation rules run on: the apprentice with an active ficha where they
     * are matriculado, one materia, an active trimester, the justification type and the real
     * {@code FALLA} attendance of the given days.
     */
    private void persistRulesFixture(int limitPerTrimester, int justificationDays, LocalDate... failureDates) {
        rulesApprentice = persistApprentice(RULES_APPRENTICE_LOGIN);
        persistGlobalConfiguration(justificationDays);
        LocalDate today = LocalDate.now(clock);
        persistTrimester(today.minusDays(60), today.plusDays(60));
        Grade grade = persistGrade("JUS-001", today.minusDays(30), today.plusDays(30));
        rulesClassSection = persistClassSection("Materia de justificaciones", grade);
        persistEnrollment(rulesApprentice, grade, StateAcademic.MATRICULADO);
        rulesJustificationType = createJustificationType(limitPerTrimester);
        for (LocalDate failureDate : failureDates) {
            persistAttendance(rulesClassSection, rulesApprentice, failureDate, StateAttendance.FALLA);
        }
    }

    private void persistGlobalConfiguration(int studentJustificationDays) {
        globalConfigurationRepository.save(
            new GlobalConfiguration()
                .id(GlobalConfiguration.GLOBAL_CONFIGURATION_ID)
                .studentJustificationDays(studentJustificationDays)
                .instructorResponseDays(2)
                .consecutiveAbsenceAlertThreshold(3)
                .accumulatedAbsenceAlertThreshold(5)
        );
    }

    private void persistTrimester(LocalDate startDate, LocalDate endDate) {
        trimesterRepository.save(
            new Trimester().name("Trimestre de justificaciones").startDate(startDate).endDate(endDate).status(StateTrimester.ACTIVO)
        );
    }

    private Grade persistGrade(String code, LocalDate startDate, LocalDate endDate) {
        Program program = programRepository.save(ProgramResourceIT.createEntity());
        Modality modality = modalityRepository.save(ModalityResourceIT.createEntity());
        TimeSlot timeSlot = timeSlotRepository.save(TimeSlotResourceIT.createEntity());

        Grade grade = GradeResourceIT.createEntity();
        grade.setCode(code);
        grade.setStartDate(startDate);
        grade.setEndDate(endDate);
        grade.setProgram(program);
        grade.setModality(modality);
        grade.setTimeSlot(timeSlot);
        return gradeRepository.save(grade);
    }

    private ClassSection persistClassSection(String subjectName, Grade grade) {
        return classSectionRepository.save(new ClassSection().subjectName(subjectName).isActive(true).grade(grade));
    }

    private void persistEnrollment(UserProfile student, Grade grade, StateAcademic stateAcademic) {
        apprenticeRepository.save(new Apprentice().student(student).grade(grade).stateAcademic(stateAcademic));
    }

    private void persistAttendance(ClassSection classSection, UserProfile student, LocalDate date, StateAttendance stateAttendance) {
        attendanceRepository.save(new Attendance().date(date).stateAttendance(stateAttendance).classSection(classSection).student(student));
    }

    private JustificationType createJustificationType(int limitPerTrimester) {
        JustificationType justificationType = JustificationTypeResourceIT.createEntity();
        justificationType.setLimitPerTrimester(limitPerTrimester);
        return justificationTypeRepository.save(justificationType);
    }

    /**
     * Persists a justification of the fixture apprentice with one rejected part covering the given
     * day, so the quota test can prove that a rejected part releases its reserved days.
     */
    private void persistRejectedJustificationCovering(LocalDate failureDate) {
        Justification header = new Justification()
            .description(DEFAULT_DESCRIPTION)
            .startDate(failureDate)
            .endDate(failureDate)
            .evidence(DEFAULT_EVIDENCE)
            .evidenceContentType(DEFAULT_EVIDENCE_CONTENT_TYPE)
            .justificationType(rulesJustificationType)
            .student(rulesApprentice);
        header = justificationRepository.save(header);

        justificationDetailsRepository.save(
            new JustificationDetails()
                .stateJustification(StateJustification.RECHAZADA)
                .rejectionReason("Soporte no legible")
                .correctionText("")
                .correctionFileUrlContentType("")
                .responseDate(Instant.now())
                .classSection(rulesClassSection)
                .justification(header)
        );
    }

    private Map<String, Object> justificationPayload(LocalDate startDate, LocalDate endDate, String classSectionId) {
        return justificationPayload(
            rulesJustificationType.getId(),
            rulesApprentice.getId(),
            startDate,
            endDate,
            PDF_CONTENT_TYPE,
            DEFAULT_EVIDENCE,
            List.of(classSectionId)
        );
    }

    private static Map<String, Object> justificationPayload(
        String justificationTypeId,
        String studentId,
        LocalDate startDate,
        LocalDate endDate,
        String evidenceContentType,
        byte[] evidence,
        List<String> classSectionIds
    ) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("description", DEFAULT_DESCRIPTION);
        payload.put("startDate", startDate.toString());
        payload.put("endDate", endDate.toString());
        payload.put("evidenceContentType", evidenceContentType);
        payload.put("evidence", Base64.getEncoder().encodeToString(evidence));
        payload.put("justificationType", Map.of("id", justificationTypeId));
        payload.put("student", Map.of("id", studentId));
        payload.put(
            "detailses",
            classSectionIds
                .stream()
                .map(id -> Map.of("classSection", Map.of("id", id)))
                .toList()
        );
        return payload;
    }

    private ResultActions postJustification(Map<String, Object> payload) throws Exception {
        return restJustificationMockMvc.perform(
            post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(payload))
        );
    }
}
