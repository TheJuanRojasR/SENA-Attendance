package com.mycompany.senaattendance.web.rest;

import static com.mycompany.senaattendance.domain.JustificationDetailsAsserts.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.Alerta;
import com.mycompany.senaattendance.domain.Attendance;
import com.mycompany.senaattendance.domain.AuditLog;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.Justification;
import com.mycompany.senaattendance.domain.JustificationDetails;
import com.mycompany.senaattendance.domain.JustificationType;
import com.mycompany.senaattendance.domain.Notificacion;
import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.AlertaState;
import com.mycompany.senaattendance.domain.enumeration.AlertaType;
import com.mycompany.senaattendance.domain.enumeration.NotificacionEstado;
import com.mycompany.senaattendance.domain.enumeration.NotificacionTipo;
import com.mycompany.senaattendance.domain.enumeration.StateAttendance;
import com.mycompany.senaattendance.domain.enumeration.StateJustification;
import com.mycompany.senaattendance.domain.enumeration.StateTrimester;
import com.mycompany.senaattendance.repository.AlertaRepository;
import com.mycompany.senaattendance.repository.AttendanceRepository;
import com.mycompany.senaattendance.repository.AuditLogRepository;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.repository.JustificationDetailsRepository;
import com.mycompany.senaattendance.repository.JustificationRepository;
import com.mycompany.senaattendance.repository.JustificationTypeRepository;
import com.mycompany.senaattendance.repository.NotificacionRepository;
import com.mycompany.senaattendance.repository.TrimesterRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.JustificationDetailsService;
import com.mycompany.senaattendance.service.JustificationNotificationPort;
import com.mycompany.senaattendance.service.dto.JustificationDetailsDTO;
import com.mycompany.senaattendance.service.mapper.JustificationDetailsMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Integration tests for the {@link JustificationDetailsResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class JustificationDetailsResourceIT {

    private static final StateJustification DEFAULT_STATE_JUSTIFICATION = StateJustification.ACEPTADA;
    private static final StateJustification UPDATED_STATE_JUSTIFICATION = StateJustification.RECHAZADA;

    private static final String DEFAULT_REJECTION_REASON = "AAAAAAAAAA";
    private static final String UPDATED_REJECTION_REASON = "BBBBBBBBBB";

    private static final String DEFAULT_CORRECTION_TEXT = "AAAAAAAAAA";
    private static final String UPDATED_CORRECTION_TEXT = "BBBBBBBBBB";

    private static final byte[] DEFAULT_CORRECTION_FILE_URL = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_CORRECTION_FILE_URL = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_CORRECTION_FILE_URL_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_CORRECTION_FILE_URL_CONTENT_TYPE = "image/png";

    private static final Instant DEFAULT_RESPONSE_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_RESPONSE_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final byte[] EVIDENCE = TestUtil.createByteArray(8, "1");
    private static final String EVIDENCE_CONTENT_TYPE = "application/pdf";

    private static final String ENTITY_API_URL = "/api/justification-details";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_DECISION_API_URL_ID = ENTITY_API_URL + "/{id}/decision";

    private static final String APPRENTICE_LOGIN = "justification_details_apprentice";
    private static final String OTHER_APPRENTICE_LOGIN = "other_justification_details_apprentice";
    private static final String INSTRUCTOR_LOGIN = "justification_details_instructor";
    private static final String OTHER_INSTRUCTOR_LOGIN = "other_justification_details_instructor";
    private static final String ADMIN_LOGIN = "justification_details_admin";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private Clock clock;

    @Autowired
    private JustificationDetailsRepository justificationDetailsRepository;

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private ClassSectionRepository classSectionRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private TrimesterRepository trimesterRepository;

    @Autowired
    private JustificationRepository justificationRepository;

    @Autowired
    private JustificationTypeRepository justificationTypeRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Mock
    private JustificationDetailsRepository justificationDetailsRepositoryMock;

    @Autowired
    private JustificationDetailsMapper justificationDetailsMapper;

    @Mock
    private JustificationDetailsService justificationDetailsServiceMock;

    @MockitoBean
    private JustificationNotificationPort justificationNotificationPort;

    @Autowired
    private MockMvc restJustificationDetailsMockMvc;

    private JustificationDetails justificationDetails;

    private JustificationDetails insertedJustificationDetails;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static JustificationDetails createEntity() {
        JustificationDetails justificationDetails = new JustificationDetails()
            .stateJustification(DEFAULT_STATE_JUSTIFICATION)
            .rejectionReason(DEFAULT_REJECTION_REASON)
            .correctionText(DEFAULT_CORRECTION_TEXT)
            .correctionFileUrl(DEFAULT_CORRECTION_FILE_URL)
            .correctionFileUrlContentType(DEFAULT_CORRECTION_FILE_URL_CONTENT_TYPE)
            .responseDate(DEFAULT_RESPONSE_DATE);
        // Add required entity
        ClassSection classSection;
        classSection = ClassSectionResourceIT.createEntity();
        classSection.setId("fixed-id-for-tests");
        justificationDetails.setClassSection(classSection);
        // Add required entity
        Justification justification;
        justification = JustificationResourceIT.createEntity();
        justification.setId("fixed-id-for-tests");
        justificationDetails.setJustification(justification);
        return justificationDetails;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static JustificationDetails createUpdatedEntity() {
        JustificationDetails updatedJustificationDetails = new JustificationDetails()
            .stateJustification(UPDATED_STATE_JUSTIFICATION)
            .rejectionReason(UPDATED_REJECTION_REASON)
            .correctionText(UPDATED_CORRECTION_TEXT)
            .correctionFileUrl(UPDATED_CORRECTION_FILE_URL)
            .correctionFileUrlContentType(UPDATED_CORRECTION_FILE_URL_CONTENT_TYPE)
            .responseDate(UPDATED_RESPONSE_DATE);
        // Add required entity
        ClassSection classSection;
        classSection = ClassSectionResourceIT.createUpdatedEntity();
        classSection.setId("fixed-id-for-tests");
        updatedJustificationDetails.setClassSection(classSection);
        // Add required entity
        Justification justification;
        justification = JustificationResourceIT.createUpdatedEntity();
        justification.setId("fixed-id-for-tests");
        updatedJustificationDetails.setJustification(justification);
        return updatedJustificationDetails;
    }

    @BeforeEach
    void initTest() {
        justificationDetails = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedJustificationDetails != null) {
            justificationDetailsRepository.delete(insertedJustificationDetails);
            insertedJustificationDetails = null;
        }
        // Remove the related documents persisted for the PUT tests and the scoping tests
        attendanceRepository.deleteAll();
        auditLogRepository.deleteAll();
        alertaRepository.deleteAll();
        notificacionRepository.deleteAll();
        justificationDetailsRepository.deleteAll();
        classSectionRepository.deleteAll();
        gradeRepository.deleteAll();
        trimesterRepository.deleteAll();
        justificationRepository.deleteAll();
        justificationTypeRepository.deleteAll();
        userProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createJustificationDetails() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the JustificationDetails
        JustificationDetailsDTO justificationDetailsDTO = justificationDetailsMapper.toDto(justificationDetails);
        var returnedJustificationDetailsDTO = om.readValue(
            restJustificationDetailsMockMvc
                .perform(
                    post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationDetailsDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            JustificationDetailsDTO.class
        );

        // Validate the JustificationDetails in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedJustificationDetails = justificationDetailsMapper.toEntity(returnedJustificationDetailsDTO);
        assertJustificationDetailsUpdatableFieldsEquals(
            returnedJustificationDetails,
            getPersistedJustificationDetails(returnedJustificationDetails)
        );

        insertedJustificationDetails = returnedJustificationDetails;
    }

    @Test
    void createJustificationDetailsWithExistingId() throws Exception {
        // Create the JustificationDetails with an existing ID
        justificationDetails.setId("existing_id");
        JustificationDetailsDTO justificationDetailsDTO = justificationDetailsMapper.toDto(justificationDetails);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restJustificationDetailsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationDetailsDTO)))
            .andExpect(status().isBadRequest());

        // Validate the JustificationDetails in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkRejectionReasonIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        justificationDetails.setRejectionReason(null);

        // Create the JustificationDetails, which fails.
        JustificationDetailsDTO justificationDetailsDTO = justificationDetailsMapper.toDto(justificationDetails);

        restJustificationDetailsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationDetailsDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkCorrectionTextIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        justificationDetails.setCorrectionText(null);

        // Create the JustificationDetails, which fails.
        JustificationDetailsDTO justificationDetailsDTO = justificationDetailsMapper.toDto(justificationDetails);

        restJustificationDetailsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationDetailsDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkResponseDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        justificationDetails.setResponseDate(null);

        // Create the JustificationDetails, which fails.
        JustificationDetailsDTO justificationDetailsDTO = justificationDetailsMapper.toDto(justificationDetails);

        restJustificationDetailsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationDetailsDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllJustificationDetailses() throws Exception {
        // Initialize the database
        insertedJustificationDetails = justificationDetailsRepository.save(justificationDetails);

        // Get all the justificationDetailsList
        restJustificationDetailsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(justificationDetails.getId())))
            .andExpect(jsonPath("$.[*].stateJustification").value(hasItem(DEFAULT_STATE_JUSTIFICATION.toString())))
            .andExpect(jsonPath("$.[*].rejectionReason").value(hasItem(DEFAULT_REJECTION_REASON)))
            .andExpect(jsonPath("$.[*].correctionText").value(hasItem(DEFAULT_CORRECTION_TEXT)))
            .andExpect(jsonPath("$.[*].correctionFileUrlContentType").value(hasItem(DEFAULT_CORRECTION_FILE_URL_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].correctionFileUrl").value(hasItem(Base64.getEncoder().encodeToString(DEFAULT_CORRECTION_FILE_URL))))
            .andExpect(jsonPath("$.[*].responseDate").value(hasItem(DEFAULT_RESPONSE_DATE.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllJustificationDetailsesWithEagerRelationshipsIsEnabled() throws Exception {
        when(justificationDetailsServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restJustificationDetailsMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(justificationDetailsServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllJustificationDetailsesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(justificationDetailsServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restJustificationDetailsMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(justificationDetailsRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void getJustificationDetails() throws Exception {
        // Initialize the database
        insertedJustificationDetails = justificationDetailsRepository.save(justificationDetails);

        // Get the justificationDetails
        restJustificationDetailsMockMvc
            .perform(get(ENTITY_API_URL_ID, justificationDetails.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(justificationDetails.getId()))
            .andExpect(jsonPath("$.stateJustification").value(DEFAULT_STATE_JUSTIFICATION.toString()))
            .andExpect(jsonPath("$.rejectionReason").value(DEFAULT_REJECTION_REASON))
            .andExpect(jsonPath("$.correctionText").value(DEFAULT_CORRECTION_TEXT))
            .andExpect(jsonPath("$.correctionFileUrlContentType").value(DEFAULT_CORRECTION_FILE_URL_CONTENT_TYPE))
            .andExpect(jsonPath("$.correctionFileUrl").value(Base64.getEncoder().encodeToString(DEFAULT_CORRECTION_FILE_URL)))
            .andExpect(jsonPath("$.responseDate").value(DEFAULT_RESPONSE_DATE.toString()));
    }

    @Test
    void getNonExistingJustificationDetails() throws Exception {
        // Get the justificationDetails
        restJustificationDetailsMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    // -----------------------------------------------------------------
    // UC011 — An apprentice only reaches the parts of their own justifications
    // -----------------------------------------------------------------

    @Test
    @WithMockUser(username = APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void getAllJustificationDetailsesAsApprenticeReturnsOnlyOwnRecords() throws Exception {
        UserProfile apprentice = persistApprentice(APPRENTICE_LOGIN);
        UserProfile otherApprentice = persistApprentice(OTHER_APPRENTICE_LOGIN);
        JustificationDetails ownDetails = persistJustificationDetails(persistJustification(apprentice));
        persistJustificationDetails(persistJustification(otherApprentice));

        restJustificationDetailsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "1"))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(ownDetails.getId()));
    }

    @Test
    @WithMockUser(username = APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void getOwnJustificationDetailsAsApprenticeReturnsOk() throws Exception {
        UserProfile apprentice = persistApprentice(APPRENTICE_LOGIN);
        JustificationDetails details = persistJustificationDetails(persistJustification(apprentice));

        restJustificationDetailsMockMvc
            .perform(get(ENTITY_API_URL_ID, details.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(details.getId()));
    }

    @Test
    @WithMockUser(username = APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void getJustificationDetailsOfAnotherApprenticeReturnsNotFound() throws Exception {
        persistApprentice(APPRENTICE_LOGIN);
        UserProfile otherApprentice = persistApprentice(OTHER_APPRENTICE_LOGIN);
        JustificationDetails otherDetails = persistJustificationDetails(persistJustification(otherApprentice));

        restJustificationDetailsMockMvc.perform(get(ENTITY_API_URL_ID, otherDetails.getId())).andExpect(status().isNotFound());
    }

    @Test
    void getJustificationDetailsOfAnotherApprenticeAsAdminReadsIt() throws Exception {
        UserProfile otherApprentice = persistApprentice(OTHER_APPRENTICE_LOGIN);
        JustificationDetails otherDetails = persistJustificationDetails(persistJustification(otherApprentice));

        restJustificationDetailsMockMvc
            .perform(get(ENTITY_API_URL_ID, otherDetails.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(otherDetails.getId()));
    }

    @Test
    @WithMockUser(username = APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void createJustificationDetailsAsApprenticeForOwnJustificationIsCreated() throws Exception {
        UserProfile apprentice = persistApprentice(APPRENTICE_LOGIN);
        JustificationDetailsDTO justificationDetailsDTO = justificationDetailsMapper.toDto(
            justificationDetailsFor(persistJustification(apprentice))
        );

        restJustificationDetailsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationDetailsDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.stateJustification").value(DEFAULT_STATE_JUSTIFICATION.toString()));
    }

    @Test
    @WithMockUser(username = APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void createJustificationDetailsAsApprenticeForAnotherJustificationReturnsBadRequest() throws Exception {
        persistApprentice(APPRENTICE_LOGIN);
        UserProfile otherApprentice = persistApprentice(OTHER_APPRENTICE_LOGIN);
        long databaseSizeBeforeCreate = getRepositoryCount();
        JustificationDetailsDTO justificationDetailsDTO = justificationDetailsMapper.toDto(
            justificationDetailsFor(persistJustification(otherApprentice))
        );

        restJustificationDetailsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationDetailsDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.notYourJustification"));

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @WithMockUser(username = APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void updateOwnJustificationDetailsAsApprenticeSucceeds() throws Exception {
        UserProfile apprentice = persistApprentice(APPRENTICE_LOGIN);
        Justification justification = persistJustification(apprentice);
        JustificationDetails insertedDetails = persistPendingJustificationDetails(justification);

        JustificationDetails payload = justificationDetailsFor(justification);
        payload.setId(insertedDetails.getId());
        payload.setCorrectionText(UPDATED_CORRECTION_TEXT);

        restJustificationDetailsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, insertedDetails.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(justificationDetailsMapper.toDto(payload)))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.correctionText").value(UPDATED_CORRECTION_TEXT));
    }

    @Test
    @WithMockUser(username = APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void updateJustificationDetailsOfAnotherApprenticeReturnsBadRequest() throws Exception {
        persistApprentice(APPRENTICE_LOGIN);
        UserProfile otherApprentice = persistApprentice(OTHER_APPRENTICE_LOGIN);
        Justification otherJustification = persistJustification(otherApprentice);
        JustificationDetails otherDetails = persistJustificationDetails(otherJustification);

        JustificationDetails payload = justificationDetailsFor(otherJustification);
        payload.setId(otherDetails.getId());
        payload.setCorrectionText(UPDATED_CORRECTION_TEXT);

        restJustificationDetailsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, otherDetails.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(justificationDetailsMapper.toDto(payload)))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.notYourJustification"));
    }

    @Test
    @WithMockUser(username = APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void patchJustificationDetailsOfAnotherApprenticeReturnsBadRequest() throws Exception {
        persistApprentice(APPRENTICE_LOGIN);
        UserProfile otherApprentice = persistApprentice(OTHER_APPRENTICE_LOGIN);
        JustificationDetails otherDetails = persistJustificationDetails(persistJustification(otherApprentice));

        JustificationDetails partialUpdatedDetails = new JustificationDetails();
        partialUpdatedDetails.setId(otherDetails.getId());
        partialUpdatedDetails.setCorrectionText(UPDATED_CORRECTION_TEXT);

        restJustificationDetailsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, otherDetails.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDetails))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.notYourJustification"));
    }

    @Test
    @WithMockUser(username = APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void deleteJustificationDetailsOfAnotherApprenticeReturnsBadRequest() throws Exception {
        persistApprentice(APPRENTICE_LOGIN);
        UserProfile otherApprentice = persistApprentice(OTHER_APPRENTICE_LOGIN);
        JustificationDetails otherDetails = persistJustificationDetails(persistJustification(otherApprentice));

        restJustificationDetailsMockMvc
            .perform(delete(ENTITY_API_URL_ID, otherDetails.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.notYourJustification"));

        assertThat(justificationDetailsRepository.existsById(otherDetails.getId())).isTrue();
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void getAllJustificationDetailsesAsInstructorReturnsForbidden() throws Exception {
        restJustificationDetailsMockMvc.perform(get(ENTITY_API_URL).accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.USER)
    void getJustificationDetailsWithoutAJustificationRoleReturnsForbidden() throws Exception {
        restJustificationDetailsMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isForbidden());
    }

    @Test
    void putExistingJustificationDetails() throws Exception {
        // Persist the @DBRef targets so they resolve on reload
        classSectionRepository.save(justificationDetails.getClassSection());
        justificationRepository.save(justificationDetails.getJustification());
        // A correction only starts from a pending part
        justificationDetails.setStateJustification(StateJustification.PENDIENTE);
        justificationDetails.setResponseDate(null);

        // Initialize the database
        insertedJustificationDetails = justificationDetailsRepository.save(justificationDetails);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // The payload corrects the part and, at the same time, tries to decide it: the state, the
        // rejection reason and the response date are server-owned.
        JustificationDetails updatedJustificationDetails = justificationDetailsRepository
            .findById(justificationDetails.getId())
            .orElseThrow();
        updatedJustificationDetails
            .stateJustification(UPDATED_STATE_JUSTIFICATION)
            .rejectionReason(UPDATED_REJECTION_REASON)
            .correctionText(UPDATED_CORRECTION_TEXT)
            .correctionFileUrl(UPDATED_CORRECTION_FILE_URL)
            .correctionFileUrlContentType(UPDATED_CORRECTION_FILE_URL_CONTENT_TYPE)
            .responseDate(UPDATED_RESPONSE_DATE);
        JustificationDetailsDTO justificationDetailsDTO = justificationDetailsMapper.toDto(updatedJustificationDetails);

        restJustificationDetailsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, justificationDetailsDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(justificationDetailsDTO))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stateJustification").value("PENDIENTE"))
            .andExpect(jsonPath("$.rejectionReason").value(DEFAULT_REJECTION_REASON))
            .andExpect(jsonPath("$.correctionText").value(UPDATED_CORRECTION_TEXT));

        // Validate the JustificationDetails in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        JustificationDetails reloaded = justificationDetailsRepository.findById(justificationDetails.getId()).orElseThrow();
        assertThat(reloaded.getStateJustification()).isEqualTo(StateJustification.PENDIENTE);
        assertThat(reloaded.getRejectionReason()).isEqualTo(DEFAULT_REJECTION_REASON);
        assertThat(reloaded.getResponseDate()).isNull();
        assertThat(reloaded.getCorrectionText()).isEqualTo(UPDATED_CORRECTION_TEXT);
        assertThat(reloaded.getCorrectionFileUrl()).isEqualTo(UPDATED_CORRECTION_FILE_URL);
        assertThat(reloaded.getCorrectionFileUrlContentType()).isEqualTo(UPDATED_CORRECTION_FILE_URL_CONTENT_TYPE);
    }

    @Test
    void putNonExistingJustificationDetails() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        justificationDetails.setId(UUID.randomUUID().toString());

        // Create the JustificationDetails
        JustificationDetailsDTO justificationDetailsDTO = justificationDetailsMapper.toDto(justificationDetails);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restJustificationDetailsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, justificationDetailsDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(justificationDetailsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the JustificationDetails in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchJustificationDetails() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        justificationDetails.setId(UUID.randomUUID().toString());

        // Create the JustificationDetails
        JustificationDetailsDTO justificationDetailsDTO = justificationDetailsMapper.toDto(justificationDetails);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restJustificationDetailsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(justificationDetailsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the JustificationDetails in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamJustificationDetails() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        justificationDetails.setId(UUID.randomUUID().toString());

        // Create the JustificationDetails
        JustificationDetailsDTO justificationDetailsDTO = justificationDetailsMapper.toDto(justificationDetails);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restJustificationDetailsMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationDetailsDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the JustificationDetails in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    // -----------------------------------------------------------------
    // UC011 — Subsanar una parte rechazada (use-cases.md:1003, A5, E3/E5)
    // -----------------------------------------------------------------

    @Test
    @WithMockUser(username = APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void correctRejectedPartWithinTheWindowReopensIt() throws Exception {
        UserProfile apprentice = persistApprentice(APPRENTICE_LOGIN);
        Justification justification = persistJustification(apprentice);
        JustificationDetails part = persistPart(justification, StateJustification.RECHAZADA, Instant.now(clock));

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", part.getId());
        payload.put("correctionText", UPDATED_CORRECTION_TEXT);
        payload.put("correctionFileUrl", UPDATED_CORRECTION_FILE_URL);
        payload.put("correctionFileUrlContentType", UPDATED_CORRECTION_FILE_URL_CONTENT_TYPE);
        // The client tries to decide the part: the state, the response date and the rejection
        // reason are server-owned and must be ignored.
        payload.put("stateJustification", StateJustification.ACEPTADA.toString());
        payload.put("responseDate", UPDATED_RESPONSE_DATE.toString());

        restJustificationDetailsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, part.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(payload))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stateJustification").value("PENDIENTE"))
            .andExpect(jsonPath("$.correctionText").value(UPDATED_CORRECTION_TEXT));

        JustificationDetails reloaded = justificationDetailsRepository.findById(part.getId()).orElseThrow();
        assertThat(reloaded.getStateJustification()).isEqualTo(StateJustification.PENDIENTE);
        assertThat(reloaded.getCorrectionText()).isEqualTo(UPDATED_CORRECTION_TEXT);
        assertThat(reloaded.getCorrectionFileUrl()).isEqualTo(UPDATED_CORRECTION_FILE_URL);
        assertThat(reloaded.getCorrectionFileUrlContentType()).isEqualTo(UPDATED_CORRECTION_FILE_URL_CONTENT_TYPE);
        assertThat(reloaded.getResponseDate()).isNull();
        // The rejection reason stays as the trace of why the part was rejected.
        assertThat(reloaded.getRejectionReason()).isEqualTo(DEFAULT_REJECTION_REASON);
    }

    @Test
    @WithMockUser(username = APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void correctRejectedPartOutsideTheWindowReturnsCorrectionExpired() throws Exception {
        UserProfile apprentice = persistApprentice(APPRENTICE_LOGIN);
        Justification justification = persistJustification(apprentice);
        JustificationDetails part = persistPart(justification, StateJustification.RECHAZADA, Instant.now(clock).minus(10, ChronoUnit.DAYS));

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", part.getId());
        payload.put("correctionText", UPDATED_CORRECTION_TEXT);

        restJustificationDetailsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, part.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(payload))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.correctionExpired"));

        JustificationDetails reloaded = justificationDetailsRepository.findById(part.getId()).orElseThrow();
        assertThat(reloaded.getStateJustification()).isEqualTo(StateJustification.RECHAZADA);
        assertThat(reloaded.getCorrectionText()).isEqualTo(DEFAULT_CORRECTION_TEXT);
    }

    @Test
    @WithMockUser(username = APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void correctAcceptedPartReturnsAlreadyProcessed() throws Exception {
        UserProfile apprentice = persistApprentice(APPRENTICE_LOGIN);
        Justification justification = persistJustification(apprentice);
        JustificationDetails part = persistPart(justification, StateJustification.ACEPTADA, Instant.now(clock));

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", part.getId());
        payload.put("correctionText", UPDATED_CORRECTION_TEXT);

        restJustificationDetailsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, part.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(payload))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.alreadyProcessed"));

        JustificationDetails reloaded = justificationDetailsRepository.findById(part.getId()).orElseThrow();
        assertThat(reloaded.getStateJustification()).isEqualTo(StateJustification.ACEPTADA);
        assertThat(reloaded.getCorrectionText()).isEqualTo(DEFAULT_CORRECTION_TEXT);
    }

    @Test
    @WithMockUser(username = APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void correctCancelledPartReturnsAlreadyProcessed() throws Exception {
        UserProfile apprentice = persistApprentice(APPRENTICE_LOGIN);
        Justification justification = persistJustification(apprentice);
        JustificationDetails part = persistPart(justification, StateJustification.CANCELADA, Instant.now(clock));

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", part.getId());
        payload.put("correctionText", UPDATED_CORRECTION_TEXT);

        restJustificationDetailsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, part.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(payload))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.alreadyProcessed"));

        JustificationDetails reloaded = justificationDetailsRepository.findById(part.getId()).orElseThrow();
        assertThat(reloaded.getStateJustification()).isEqualTo(StateJustification.CANCELADA);
        assertThat(reloaded.getCorrectionText()).isEqualTo(DEFAULT_CORRECTION_TEXT);
    }

    @Test
    @WithMockUser(username = APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void correctPendingPartOnlyUpdatesTheCorrectionFields() throws Exception {
        UserProfile apprentice = persistApprentice(APPRENTICE_LOGIN);
        Justification justification = persistJustification(apprentice);
        JustificationDetails part = persistPart(justification, StateJustification.PENDIENTE, null);

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", part.getId());
        payload.put("correctionText", UPDATED_CORRECTION_TEXT);
        payload.put("stateJustification", StateJustification.RECHAZADA.toString());
        payload.put("responseDate", UPDATED_RESPONSE_DATE.toString());

        restJustificationDetailsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, part.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(payload))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stateJustification").value("PENDIENTE"))
            .andExpect(jsonPath("$.correctionText").value(UPDATED_CORRECTION_TEXT));

        JustificationDetails reloaded = justificationDetailsRepository.findById(part.getId()).orElseThrow();
        assertThat(reloaded.getStateJustification()).isEqualTo(StateJustification.PENDIENTE);
        assertThat(reloaded.getResponseDate()).isNull();
    }

    // -----------------------------------------------------------------
    // UC010 — Bandeja de pendientes del instructor (use-cases.md:1081, A1)
    // -----------------------------------------------------------------

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void getPendingJustificationDetailsesAsInstructorReturnsOnlyTheirClassSections() throws Exception {
        UserProfile instructor = persistProfile(INSTRUCTOR_LOGIN, "3000000001");
        UserProfile otherInstructor = persistProfile(OTHER_INSTRUCTOR_LOGIN, "3000000002");
        UserProfile apprentice = persistApprentice(APPRENTICE_LOGIN);
        ClassSection ownClassSection = persistClassSection("Materia del instructor", instructor);
        ClassSection otherClassSection = persistClassSection("Materia de otro instructor", otherInstructor);
        LocalDate today = LocalDate.now(clock);
        Justification ownJustification = persistJustification(apprentice, today.minusDays(3), today, true);
        Justification otherJustification = persistJustification(apprentice, today.minusDays(3), today, true);
        JustificationDetails ownPart = persistPart(ownJustification, ownClassSection, StateJustification.PENDIENTE);
        persistPart(otherJustification, otherClassSection, StateJustification.PENDIENTE);

        restJustificationDetailsMockMvc
            .perform(get(ENTITY_API_URL + "/pending"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "1"))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(ownPart.getId()))
            .andExpect(jsonPath("$[0].stateJustification").value("PENDIENTE"))
            .andExpect(jsonPath("$[0].classSection.id").value(ownClassSection.getId()))
            .andExpect(jsonPath("$[0].classSection.subjectName").value("Materia del instructor"))
            .andExpect(jsonPath("$[0].justification.id").value(ownJustification.getId()))
            .andExpect(jsonPath("$[0].justification.student.documentNumber").value(apprentice.getDocumentNumber()))
            .andExpect(jsonPath("$[0].justification.startDate").value(today.minusDays(3).toString()))
            .andExpect(jsonPath("$[0].justification.endDate").value(today.toString()))
            .andExpect(jsonPath("$[0].justification.onTime").value(true))
            .andExpect(jsonPath("$[0].requestDate").exists());
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void getPendingJustificationDetailsesFiltersByState() throws Exception {
        UserProfile instructor = persistProfile(INSTRUCTOR_LOGIN, "3000000011");
        UserProfile apprentice = persistApprentice(APPRENTICE_LOGIN);
        ClassSection classSection = persistClassSection("Materia del instructor", instructor);
        LocalDate today = LocalDate.now(clock);
        Justification justification = persistJustification(apprentice, today.minusDays(3), today, true);
        persistPart(justification, classSection, StateJustification.PENDIENTE);
        JustificationDetails acceptedPart = persistPart(justification, classSection, StateJustification.ACEPTADA);

        restJustificationDetailsMockMvc
            .perform(get(ENTITY_API_URL + "/pending"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "1"))
            .andExpect(jsonPath("$[0].stateJustification").value("PENDIENTE"));

        restJustificationDetailsMockMvc
            .perform(get(ENTITY_API_URL + "/pending").param("stateJustification", "ACEPTADA"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "1"))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(acceptedPart.getId()))
            .andExpect(jsonPath("$[0].stateJustification").value("ACEPTADA"));
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void getPendingJustificationDetailsesAppliesTheOptionalFilters() throws Exception {
        UserProfile instructor = persistProfile(INSTRUCTOR_LOGIN, "3000000021");
        UserProfile apprentice = persistApprentice(APPRENTICE_LOGIN);
        ClassSection ownClassSection = persistClassSection("Materia del instructor", instructor);
        ClassSection otherOwnClassSection = persistClassSection("Otra materia del instructor", instructor);
        LocalDate today = LocalDate.now(clock);
        Justification justification = persistJustification(apprentice, today.minusDays(3), today, true);
        persistPart(justification, ownClassSection, StateJustification.PENDIENTE);
        persistPart(justification, otherOwnClassSection, StateJustification.PENDIENTE);

        restJustificationDetailsMockMvc
            .perform(get(ENTITY_API_URL + "/pending").param("classSectionId", ownClassSection.getId()))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "1"))
            .andExpect(jsonPath("$[0].classSection.id").value(ownClassSection.getId()));

        restJustificationDetailsMockMvc
            .perform(get(ENTITY_API_URL + "/pending").param("createdFrom", today.toString()))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "2"));

        restJustificationDetailsMockMvc
            .perform(get(ENTITY_API_URL + "/pending").param("createdTo", today.minusDays(1).toString()))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "0"))
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void getPendingJustificationDetailsesWithoutClassSectionsReturnsEmptyPage() throws Exception {
        persistProfile(INSTRUCTOR_LOGIN, "3000000031");
        UserProfile apprentice = persistApprentice(APPRENTICE_LOGIN);
        ClassSection classSection = persistClassSection("Materia de otro instructor", persistProfile(OTHER_INSTRUCTOR_LOGIN, "3000000032"));
        Justification justification = persistJustification(apprentice, LocalDate.now(clock), LocalDate.now(clock), true);
        persistPart(justification, classSection, StateJustification.PENDIENTE);

        restJustificationDetailsMockMvc
            .perform(get(ENTITY_API_URL + "/pending"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "0"))
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(username = APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void getPendingJustificationDetailsesAsApprenticeReturnsForbidden() throws Exception {
        restJustificationDetailsMockMvc.perform(get(ENTITY_API_URL + "/pending")).andExpect(status().isForbidden());
    }

    // -----------------------------------------------------------------
    // UC010 — Lectura del detalle de la parte por el instructor (use-cases.md:1064, paso 4)
    // -----------------------------------------------------------------

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void getPartOfOwnMateriaAsInstructorReadsTheEvidenceAndTheType() throws Exception {
        UserProfile instructor = persistProfile(INSTRUCTOR_LOGIN, "3200000001");
        UserProfile apprentice = persistApprentice(APPRENTICE_LOGIN);
        ClassSection classSection = persistClassSection("Materia del instructor", instructor);
        JustificationType type = justificationTypeRepository.save(JustificationTypeResourceIT.createEntity());
        LocalDate today = LocalDate.now(clock);
        Justification justification = persistJustification(apprentice, today.minusDays(3), today, true);
        justification.setJustificationType(type);
        justification.setEvidence(EVIDENCE);
        justification.setEvidenceContentType(EVIDENCE_CONTENT_TYPE);
        justification = justificationRepository.save(justification);
        JustificationDetails part = persistPart(justification, classSection, StateJustification.PENDIENTE);

        restJustificationDetailsMockMvc
            .perform(get(ENTITY_API_URL_ID, part.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(part.getId()))
            .andExpect(jsonPath("$.classSection.id").value(classSection.getId()))
            .andExpect(jsonPath("$.justification.student.documentNumber").value(apprentice.getDocumentNumber()))
            .andExpect(jsonPath("$.justification.onTime").value(true))
            .andExpect(jsonPath("$.justification.justificationType.name").value(type.getName()))
            .andExpect(jsonPath("$.justification.evidenceContentType").value(EVIDENCE_CONTENT_TYPE))
            .andExpect(jsonPath("$.justification.evidence").value(Base64.getEncoder().encodeToString(EVIDENCE)));
    }

    /**
     * The tray stays light: the file is only read from the detail, so a page of 20 parts never
     * carries 20 attachments.
     */
    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void getPendingJustificationDetailsesDoesNotCarryTheEvidence() throws Exception {
        UserProfile instructor = persistProfile(INSTRUCTOR_LOGIN, "3200000005");
        UserProfile apprentice = persistApprentice(APPRENTICE_LOGIN);
        ClassSection classSection = persistClassSection("Materia del instructor", instructor);
        LocalDate today = LocalDate.now(clock);
        Justification justification = persistJustification(apprentice, today.minusDays(3), today, true);
        justification.setEvidence(EVIDENCE);
        justification.setEvidenceContentType(EVIDENCE_CONTENT_TYPE);
        justificationRepository.save(justification);
        persistPart(justification, classSection, StateJustification.PENDIENTE);

        restJustificationDetailsMockMvc
            .perform(get(ENTITY_API_URL + "/pending"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "1"))
            .andExpect(jsonPath("$[0].justification.evidence").doesNotExist())
            .andExpect(jsonPath("$[0].justification.evidenceContentType").doesNotExist());
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void getPartOfAnotherInstructorsMateriaReturnsNotFound() throws Exception {
        persistProfile(INSTRUCTOR_LOGIN, "3200000002");
        UserProfile otherInstructor = persistProfile(OTHER_INSTRUCTOR_LOGIN, "3200000003");
        UserProfile apprentice = persistApprentice(APPRENTICE_LOGIN);
        ClassSection otherClassSection = persistClassSection("Materia de otro instructor", otherInstructor);
        JustificationDetails part = persistPart(persistJustification(apprentice), otherClassSection, StateJustification.PENDIENTE);

        restJustificationDetailsMockMvc.perform(get(ENTITY_API_URL_ID, part.getId())).andExpect(status().isNotFound());
    }

    @Test
    void getPartOfAnotherInstructorsMateriaAsAdminReadsItWithEvidence() throws Exception {
        UserProfile otherInstructor = persistProfile(OTHER_INSTRUCTOR_LOGIN, "3200000004");
        UserProfile apprentice = persistApprentice(APPRENTICE_LOGIN);
        ClassSection otherClassSection = persistClassSection("Materia de otro instructor", otherInstructor);
        LocalDate today = LocalDate.now(clock);
        Justification justification = persistJustification(apprentice, today.minusDays(3), today, true);
        justification.setEvidence(EVIDENCE);
        justification.setEvidenceContentType(EVIDENCE_CONTENT_TYPE);
        justificationRepository.save(justification);
        JustificationDetails part = persistPart(justification, otherClassSection, StateJustification.PENDIENTE);

        restJustificationDetailsMockMvc
            .perform(get(ENTITY_API_URL_ID, part.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(part.getId()))
            .andExpect(jsonPath("$.justification.evidence").value(Base64.getEncoder().encodeToString(EVIDENCE)));
    }

    // -----------------------------------------------------------------
    // UC010 — Decisión del instructor (use-cases.md:1052-1062, E1/E2/E4)
    // -----------------------------------------------------------------

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void decideApprovedPartMarksTheCoveredFailuresAsJustified() throws Exception {
        DecisionFixture fixture = persistDecisionFixture(true);
        ClassSection otherMateria = persistClassSection("Otra materia del instructor", fixture.instructor());
        LocalDate today = LocalDate.now(clock);
        Attendance coveredFailure = persistAttendance(
            fixture.classSection(),
            fixture.apprentice(),
            today.minusDays(3),
            StateAttendance.FALLA
        );
        Attendance outsideFailure = persistAttendance(
            fixture.classSection(),
            fixture.apprentice(),
            today.minusDays(10),
            StateAttendance.FALLA
        );
        Attendance presentRecord = persistAttendance(
            fixture.classSection(),
            fixture.apprentice(),
            today.minusDays(2),
            StateAttendance.PRESENTE
        );
        Attendance otherMateriaFailure = persistAttendance(otherMateria, fixture.apprentice(), today.minusDays(2), StateAttendance.FALLA);

        patchDecision(fixture.part().getId(), Map.of("stateJustification", "ACEPTADA"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stateJustification").value("ACEPTADA"))
            .andExpect(jsonPath("$.responseDate").exists());

        assertThat(justificationDetailsRepository.findById(fixture.part().getId()).orElseThrow().getStateJustification()).isEqualTo(
            StateJustification.ACEPTADA
        );
        // The deadline mark is server-owned: the decision never recalculates it
        assertThat(justificationRepository.findById(fixture.justification().getId()).orElseThrow().getOnTime()).isTrue();

        Attendance justified = attendanceRepository.findById(coveredFailure.getId()).orElseThrow();
        assertThat(justified.getStateAttendance()).isEqualTo(StateAttendance.JUSTIFICADA);
        assertThat(justified.getModifiedByJustification()).isNotNull();
        assertThat(justified.getModifiedByJustification().getId()).isEqualTo(fixture.justification().getId());
        assertThat(attendanceRepository.findById(outsideFailure.getId()).orElseThrow().getStateAttendance()).isEqualTo(
            StateAttendance.FALLA
        );
        assertThat(attendanceRepository.findById(presentRecord.getId()).orElseThrow().getStateAttendance()).isEqualTo(
            StateAttendance.PRESENTE
        );
        Attendance untouched = attendanceRepository.findById(otherMateriaFailure.getId()).orElseThrow();
        assertThat(untouched.getStateAttendance()).isEqualTo(StateAttendance.FALLA);
        assertThat(untouched.getModifiedByJustification()).isNull();

        List<AuditLog> audits = auditLogRepository.findAll();
        assertThat(audits).hasSize(1);
        assertThat(audits.getFirst().getPreviousState()).isEqualTo(StateAttendance.FALLA);
        assertThat(audits.getFirst().getNewState()).isEqualTo(StateAttendance.JUSTIFICADA);
        assertThat(audits.getFirst().getAttendance().getId()).isEqualTo(coveredFailure.getId());
        assertThat(audits.getFirst().getModifiedBy().getId()).isEqualTo(fixture.instructor().getId());
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void decideRejectedPartWithoutReasonReturnsRejectionReasonRequired() throws Exception {
        DecisionFixture fixture = persistDecisionFixture(true);

        patchDecision(fixture.part().getId(), Map.of("stateJustification", "RECHAZADA"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.rejectionReasonRequired"));

        JustificationDetails reloaded = justificationDetailsRepository.findById(fixture.part().getId()).orElseThrow();
        assertThat(reloaded.getStateJustification()).isEqualTo(StateJustification.PENDIENTE);
        assertThat(reloaded.getResponseDate()).isNull();
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void decideRejectedPartRegistersTheReasonAndKeepsTheAttendanceUntouched() throws Exception {
        DecisionFixture fixture = persistDecisionFixture(true);
        Attendance failure = persistAttendance(
            fixture.classSection(),
            fixture.apprentice(),
            LocalDate.now(clock).minusDays(3),
            StateAttendance.FALLA
        );

        patchDecision(fixture.part().getId(), Map.of("stateJustification", "RECHAZADA", "rejectionReason", "Soporte no legible"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stateJustification").value("RECHAZADA"))
            .andExpect(jsonPath("$.rejectionReason").value("Soporte no legible"))
            .andExpect(jsonPath("$.responseDate").exists());

        JustificationDetails reloaded = justificationDetailsRepository.findById(fixture.part().getId()).orElseThrow();
        assertThat(reloaded.getStateJustification()).isEqualTo(StateJustification.RECHAZADA);
        assertThat(reloaded.getRejectionReason()).isEqualTo("Soporte no legible");
        Attendance kept = attendanceRepository.findById(failure.getId()).orElseThrow();
        assertThat(kept.getStateAttendance()).isEqualTo(StateAttendance.FALLA);
        assertThat(kept.getModifiedByJustification()).isNull();
        assertThat(auditLogRepository.count()).isZero();
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void decideAnAlreadyDecidedPartReturnsAlreadyProcessed() throws Exception {
        DecisionFixture fixture = persistDecisionFixture(true, StateJustification.ACEPTADA);

        patchDecision(fixture.part().getId(), Map.of("stateJustification", "RECHAZADA", "rejectionReason", "Fuera de plazo"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.alreadyProcessed"));

        assertThat(justificationDetailsRepository.findById(fixture.part().getId()).orElseThrow().getStateJustification()).isEqualTo(
            StateJustification.ACEPTADA
        );
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void decidePartOfAnotherInstructorReturnsNotYourClassSection() throws Exception {
        UserProfile otherInstructor = persistProfile(OTHER_INSTRUCTOR_LOGIN, "3100000002");
        UserProfile apprentice = persistApprentice(APPRENTICE_LOGIN);
        ClassSection otherClassSection = persistClassSection("Materia de otro instructor", otherInstructor);
        Justification justification = persistJustification(apprentice, LocalDate.now(clock).minusDays(3), LocalDate.now(clock), true);
        JustificationDetails part = persistPart(justification, otherClassSection, StateJustification.PENDIENTE);

        patchDecision(part.getId(), Map.of("stateJustification", "ACEPTADA"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.notYourClassSection"));

        assertThat(justificationDetailsRepository.findById(part.getId()).orElseThrow().getStateJustification()).isEqualTo(
            StateJustification.PENDIENTE
        );
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void decideOutOfTimePartWithoutReasonReturnsOutOfTimeReasonRequired() throws Exception {
        DecisionFixture fixture = persistDecisionFixture(false);

        patchDecision(fixture.part().getId(), Map.of("stateJustification", "ACEPTADA"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.outOfTimeReasonRequired"));

        assertThat(justificationDetailsRepository.findById(fixture.part().getId()).orElseThrow().getStateJustification()).isEqualTo(
            StateJustification.PENDIENTE
        );
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void decideOutOfTimePartWithReasonApprovesIt() throws Exception {
        DecisionFixture fixture = persistDecisionFixture(false);
        Attendance failure = persistAttendance(
            fixture.classSection(),
            fixture.apprentice(),
            LocalDate.now(clock).minusDays(3),
            StateAttendance.FALLA
        );

        patchDecision(fixture.part().getId(), Map.of("stateJustification", "ACEPTADA", "outOfTimeReason", "Emergencia médica"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stateJustification").value("ACEPTADA"))
            .andExpect(jsonPath("$.outOfTimeReason").value("Emergencia médica"));

        JustificationDetails reloaded = justificationDetailsRepository.findById(fixture.part().getId()).orElseThrow();
        assertThat(reloaded.getStateJustification()).isEqualTo(StateJustification.ACEPTADA);
        assertThat(reloaded.getOutOfTimeReason()).isEqualTo("Emergencia médica");
        assertThat(attendanceRepository.findById(failure.getId()).orElseThrow().getStateAttendance()).isEqualTo(
            StateAttendance.JUSTIFICADA
        );
    }

    @Test
    @WithMockUser(username = ADMIN_LOGIN, authorities = AuthoritiesConstants.ADMIN)
    void decideAsAdminApprovesThePartOfAnyInstructor() throws Exception {
        UserProfile admin = persistProfile(ADMIN_LOGIN, "3100000003");
        UserProfile otherInstructor = persistProfile(OTHER_INSTRUCTOR_LOGIN, "3100000002");
        UserProfile apprentice = persistApprentice(APPRENTICE_LOGIN);
        ClassSection otherClassSection = persistClassSection("Materia de otro instructor", otherInstructor);
        LocalDate today = LocalDate.now(clock);
        Justification justification = persistJustification(apprentice, today.minusDays(3), today, true);
        JustificationDetails part = persistPart(justification, otherClassSection, StateJustification.PENDIENTE);
        Attendance failure = persistAttendance(otherClassSection, apprentice, today.minusDays(2), StateAttendance.FALLA);

        patchDecision(part.getId(), Map.of("stateJustification", "ACEPTADA"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stateJustification").value("ACEPTADA"));

        assertThat(attendanceRepository.findById(failure.getId()).orElseThrow().getStateAttendance()).isEqualTo(
            StateAttendance.JUSTIFICADA
        );
        assertThat(auditLogRepository.findAll().getFirst().getModifiedBy().getId()).isEqualTo(admin.getId());
    }

    @Test
    @WithMockUser(username = APPRENTICE_LOGIN, authorities = AuthoritiesConstants.APPRENTICE)
    void decideAsApprenticeReturnsForbidden() throws Exception {
        patchDecision(UUID.randomUUID().toString(), Map.of("stateJustification", "ACEPTADA")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void decideWithinTheResponseDeadlineIsNotMarkedLate() throws Exception {
        DecisionFixture fixture = persistDecisionFixture(true);

        patchDecision(fixture.part().getId(), Map.of("stateJustification", "ACEPTADA"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lateDecision").value(false));

        JustificationDetails reloaded = justificationDetailsRepository.findById(fixture.part().getId()).orElseThrow();
        assertThat(reloaded.getLateDecision()).isFalse();
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void decideAfterTheResponseDeadlineIsMarkedLate() throws Exception {
        DecisionFixture fixture = persistDecisionFixture(true);
        backdateRequestDate(fixture.justification(), 90);

        patchDecision(fixture.part().getId(), Map.of("stateJustification", "ACEPTADA"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lateDecision").value(true));

        JustificationDetails reloaded = justificationDetailsRepository.findById(fixture.part().getId()).orElseThrow();
        assertThat(reloaded.getLateDecision()).isTrue();
    }

    // -----------------------------------------------------------------
    // UC013 — La aprobación resuelve automáticamente las alertas
    // -----------------------------------------------------------------

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void decideApprovedPartResolvesTheActiveAlertBelowTheThreshold() throws Exception {
        UserProfile instructor = persistProfile(INSTRUCTOR_LOGIN, "3100000001");
        UserProfile apprentice = persistApprentice(APPRENTICE_LOGIN);
        LocalDate today = LocalDate.now(clock);
        Grade grade = gradeRepository.save(withUniqueCode(GradeResourceIT.createEntity()));
        ClassSection classSection = persistClassSection("Materia de la alerta", instructor, grade);
        Justification justification = persistJustification(apprentice, today.minusDays(5), today, true);
        JustificationDetails part = persistPart(justification, classSection, StateJustification.PENDIENTE);
        Trimester trimester = persistTrimester(today.minusDays(30), today.plusDays(30));
        Attendance coveredFailure = persistAttendance(classSection, apprentice, today.minusDays(3), StateAttendance.FALLA);
        Alerta activeAlert = persistConsecutiveAlert(apprentice, classSection, grade, trimester);

        patchDecision(part.getId(), Map.of("stateJustification", "ACEPTADA"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stateJustification").value("ACEPTADA"));

        // The approval converted the covered failure to JUSTIFICADA, so the failures of the
        // materia dropped below the threshold and the active alert was resolved automatically.
        assertThat(attendanceRepository.findById(coveredFailure.getId()).orElseThrow().getStateAttendance()).isEqualTo(
            StateAttendance.JUSTIFICADA
        );
        Alerta resolved = alertaRepository.findById(activeAlert.getId()).orElseThrow();
        assertThat(resolved.getState()).isEqualTo(AlertaState.RESUELTA_AUTOMATICAMENTE);
        assertThat(resolved.getResolvedAt()).isNotNull();
        assertThat(resolved.getObservation()).isNull();

        // The automatic resolution notifies the apprentice and the instructor of the materia.
        List<Notificacion> apprenticeNotifications = notificacionRepository
            .findByUser(apprentice.getUser(), Pageable.unpaged())
            .getContent();
        assertThat(apprenticeNotifications).hasSize(1);
        Notificacion apprenticeNotification = apprenticeNotifications.getFirst();
        assertThat(apprenticeNotification.getTipo()).isEqualTo(NotificacionTipo.ALERTA);
        assertThat(apprenticeNotification.getEstado()).isEqualTo(NotificacionEstado.PENDIENTE);
        assertThat(apprenticeNotification.getRead()).isFalse();
        assertThat(apprenticeNotification.getReferenceType()).isEqualTo("ALERT");
        assertThat(apprenticeNotification.getReferenceId()).isEqualTo(activeAlert.getId());

        List<Notificacion> instructorNotifications = notificacionRepository
            .findByUser(instructor.getUser(), Pageable.unpaged())
            .getContent();
        assertThat(instructorNotifications).hasSize(1);
        assertThat(instructorNotifications.getFirst().getReferenceId()).isEqualTo(activeAlert.getId());
    }

    // -----------------------------------------------------------------
    // UC010 — Una notificación por decisión (use-cases.md:1005, A2)
    // -----------------------------------------------------------------

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void decideAcceptedPartNotifiesTheStateChangeOnce() throws Exception {
        DecisionFixture fixture = persistDecisionFixture(true);

        patchDecision(fixture.part().getId(), Map.of("stateJustification", "ACEPTADA")).andExpect(status().isOk());

        ArgumentCaptor<Justification> notified = ArgumentCaptor.forClass(Justification.class);
        verify(justificationNotificationPort, times(1)).stateChanged(notified.capture(), eq(StateJustification.ACEPTADA));
        verifyNoMoreInteractions(justificationNotificationPort);
        assertThat(notified.getValue().getId()).isEqualTo(fixture.justification().getId());
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_LOGIN, authorities = AuthoritiesConstants.INSTRUCTOR)
    void decideRejectedPartNotifiesTheStateChangeOnce() throws Exception {
        DecisionFixture fixture = persistDecisionFixture(true);

        patchDecision(fixture.part().getId(), Map.of("stateJustification", "RECHAZADA", "rejectionReason", "Soporte no legible")).andExpect(
            status().isOk()
        );

        verify(justificationNotificationPort, times(1)).stateChanged(any(Justification.class), eq(StateJustification.RECHAZADA));
        verifyNoMoreInteractions(justificationNotificationPort);
    }

    @Test
    void patchNonExistingJustificationDetails() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        justificationDetails.setId(UUID.randomUUID().toString());

        // Create the JustificationDetails
        JustificationDetailsDTO justificationDetailsDTO = justificationDetailsMapper.toDto(justificationDetails);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restJustificationDetailsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, justificationDetailsDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(justificationDetailsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the JustificationDetails in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchJustificationDetails() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        justificationDetails.setId(UUID.randomUUID().toString());

        // Create the JustificationDetails
        JustificationDetailsDTO justificationDetailsDTO = justificationDetailsMapper.toDto(justificationDetails);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restJustificationDetailsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(justificationDetailsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the JustificationDetails in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamJustificationDetails() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        justificationDetails.setId(UUID.randomUUID().toString());

        // Create the JustificationDetails
        JustificationDetailsDTO justificationDetailsDTO = justificationDetailsMapper.toDto(justificationDetails);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restJustificationDetailsMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(justificationDetailsDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the JustificationDetails in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteJustificationDetails() throws Exception {
        // Initialize the database
        insertedJustificationDetails = justificationDetailsRepository.save(justificationDetails);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the justificationDetails
        restJustificationDetailsMockMvc
            .perform(delete(ENTITY_API_URL_ID, justificationDetails.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
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
        profile.setDocumentNumber("D" + UUID.randomUUID().toString().replace("-", "").substring(0, 13));
        profile.setUser(user);
        return userProfileRepository.save(profile);
    }

    private Justification persistJustification(UserProfile student) {
        Justification justification = JustificationResourceIT.createEntity();
        justification.setId(null);
        justification.setStudent(student);
        return justificationRepository.save(justification);
    }

    /**
     * Builds a part of the given justification, not persisted, so it can be sent as payload.
     */
    private static JustificationDetails justificationDetailsFor(Justification justification) {
        JustificationDetails justificationDetails = createEntity();
        justificationDetails.setId(null);
        justificationDetails.setJustification(justification);
        return justificationDetails;
    }

    private JustificationDetails persistJustificationDetails(Justification justification) {
        return justificationDetailsRepository.save(justificationDetailsFor(justification));
    }

    /**
     * Persists a pending part of the given justification, which is the state a correction can
     * start from.
     */
    private JustificationDetails persistPendingJustificationDetails(Justification justification) {
        JustificationDetails part = justificationDetailsFor(justification);
        part.setStateJustification(StateJustification.PENDIENTE);
        part.setResponseDate(null);
        return justificationDetailsRepository.save(part);
    }

    /**
     * Persists a part of the given justification in the requested state, with the response date
     * the correction window is computed from.
     */
    private JustificationDetails persistPart(Justification justification, StateJustification state, Instant responseDate) {
        JustificationDetails part = justificationDetailsFor(justification);
        part.setStateJustification(state);
        part.setResponseDate(responseDate);
        return justificationDetailsRepository.save(part);
    }

    /**
     * Persists a profile with a resolvable login and a fixed document number, so the service can
     * resolve it from the security context and the tests can assert the exposed identity.
     */
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

    /**
     * Persists a materia assigned to an instructor, which is the readable scope of the tray.
     */
    private ClassSection persistClassSection(String subjectName, UserProfile instructor) {
        ClassSection classSection = ClassSectionResourceIT.createEntity();
        classSection.setId(null);
        classSection.setSubjectName(subjectName);
        classSection.setInstructor(instructor);
        return classSectionRepository.save(classSection);
    }

    /**
     * Persists a materia of a real ficha, so the alert evaluation can resolve the ficha of the
     * materia from the database.
     */
    private ClassSection persistClassSection(String subjectName, UserProfile instructor, Grade grade) {
        ClassSection classSection = ClassSectionResourceIT.createEntity();
        classSection.setId(null);
        classSection.setSubjectName(subjectName);
        classSection.setInstructor(instructor);
        classSection.setGrade(grade);
        return classSectionRepository.save(classSection);
    }

    /**
     * Persists an active trimester, which is the window the alert evaluation counts over.
     */
    private Trimester persistTrimester(LocalDate startDate, LocalDate endDate) {
        return trimesterRepository.save(
            new Trimester().name("Trimestre de alertas").startDate(startDate).endDate(endDate).status(StateTrimester.ACTIVO)
        );
    }

    /**
     * Persists an active consecutive alert of one materia, which is the state an approved
     * justification that lowers the failures must resolve (UC013, A4).
     */
    private Alerta persistConsecutiveAlert(UserProfile apprentice, ClassSection classSection, Grade grade, Trimester trimester) {
        return alertaRepository.save(
            new Alerta()
                .student(apprentice)
                .classSection(classSection)
                .grade(grade)
                .trimester(trimester)
                .type(AlertaType.CONSECUTIVAS)
                .state(AlertaState.NO_LEIDA)
                .absenceCount(3)
                .threshold(3)
                .generatedAt(Instant.now(clock))
        );
    }

    /**
     * Stamps a unique code on a ficha built by the shared factory, so the unique code index does
     * not clash with a previous test run.
     */
    private static Grade withUniqueCode(Grade grade) {
        grade.setCode("ALR-" + UUID.randomUUID().toString().substring(0, 8));
        return grade;
    }

    /**
     * Persists a justification of the given apprentice over a period and with a deadline mark.
     */
    private Justification persistJustification(UserProfile student, LocalDate startDate, LocalDate endDate, Boolean onTime) {
        Justification justification = JustificationResourceIT.createEntity();
        justification.setId(null);
        justification.setStudent(student);
        justification.setStartDate(startDate);
        justification.setEndDate(endDate);
        justification.setOnTime(onTime);
        return justificationRepository.save(justification);
    }

    /**
     * Persists a part of the given justification in one materia with the requested decision state,
     * used to build the instructor tray and the decision scenarios of UC010.
     */
    private JustificationDetails persistPart(Justification justification, ClassSection classSection, StateJustification state) {
        JustificationDetails part = justificationDetailsFor(justification);
        part.setClassSection(classSection);
        part.setStateJustification(state);
        part.setResponseDate(null);
        return justificationDetailsRepository.save(part);
    }

    /**
     * Persists an attendance record of the apprentice in one materia on a date, used to prove the
     * conversion of approved parts.
     */
    private Attendance persistAttendance(ClassSection classSection, UserProfile student, LocalDate date, StateAttendance stateAttendance) {
        return attendanceRepository.save(
            new Attendance().date(date).stateAttendance(stateAttendance).classSection(classSection).student(student)
        );
    }

    /**
     * Moves the request date of the justification header back in time, so a decision taken now
     * falls outside the configured instructor response deadline.
     */
    private void backdateRequestDate(Justification justification, int days) {
        Justification persisted = justificationRepository.findById(justification.getId()).orElseThrow();
        persisted.setCreatedDate(Instant.now(clock).minus(days, ChronoUnit.DAYS));
        justificationRepository.save(persisted);
    }

    /**
     * Persists the graph a decision needs: the instructor assigned to one materia, the apprentice
     * and a part of a justification over the last five days with the requested deadline mark.
     */
    private DecisionFixture persistDecisionFixture(boolean onTime) {
        return persistDecisionFixture(onTime, StateJustification.PENDIENTE);
    }

    private DecisionFixture persistDecisionFixture(boolean onTime, StateJustification state) {
        UserProfile instructor = persistProfile(INSTRUCTOR_LOGIN, "3100000001");
        UserProfile apprentice = persistApprentice(APPRENTICE_LOGIN);
        ClassSection classSection = persistClassSection("Materia del instructor", instructor);
        LocalDate today = LocalDate.now(clock);
        Justification justification = persistJustification(apprentice, today.minusDays(5), today, onTime);
        JustificationDetails part = persistPart(justification, classSection, state);
        return new DecisionFixture(instructor, apprentice, classSection, justification, part);
    }

    /**
     * Sends the instructor decision over one part through the API.
     */
    private ResultActions patchDecision(String partId, Map<String, Object> payload) throws Exception {
        return restJustificationDetailsMockMvc.perform(
            patch(ENTITY_DECISION_API_URL_ID, partId).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(payload))
        );
    }

    /**
     * The persisted graph of a decision scenario: the instructor assigned to the materia, the
     * apprentice, and the header and part being decided.
     */
    private record DecisionFixture(
        UserProfile instructor,
        UserProfile apprentice,
        ClassSection classSection,
        Justification justification,
        JustificationDetails part
    ) {}

    protected long getRepositoryCount() {
        return justificationDetailsRepository.count();
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

    protected JustificationDetails getPersistedJustificationDetails(JustificationDetails justificationDetails) {
        return justificationDetailsRepository.findById(justificationDetails.getId()).orElseThrow();
    }

    protected void assertPersistedJustificationDetailsToMatchAllProperties(JustificationDetails expectedJustificationDetails) {
        assertJustificationDetailsAllPropertiesEquals(
            expectedJustificationDetails,
            getPersistedJustificationDetails(expectedJustificationDetails)
        );
    }

    protected void assertPersistedJustificationDetailsToMatchUpdatableProperties(JustificationDetails expectedJustificationDetails) {
        assertJustificationDetailsAllUpdatablePropertiesEquals(
            expectedJustificationDetails,
            getPersistedJustificationDetails(expectedJustificationDetails)
        );
    }
}
