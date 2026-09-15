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
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Justification;
import com.mycompany.senaattendance.domain.JustificationDetails;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.StateJustification;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.JustificationDetailsRepository;
import com.mycompany.senaattendance.repository.JustificationRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.JustificationDetailsService;
import com.mycompany.senaattendance.service.dto.JustificationDetailsDTO;
import com.mycompany.senaattendance.service.mapper.JustificationDetailsMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
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

    private static final String ENTITY_API_URL = "/api/justification-details";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final String APPRENTICE_LOGIN = "justification_details_apprentice";
    private static final String OTHER_APPRENTICE_LOGIN = "other_justification_details_apprentice";
    private static final String INSTRUCTOR_LOGIN = "justification_details_instructor";
    private static final String OTHER_INSTRUCTOR_LOGIN = "other_justification_details_instructor";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private Clock clock;

    @Autowired
    private JustificationDetailsRepository justificationDetailsRepository;

    @Autowired
    private ClassSectionRepository classSectionRepository;

    @Autowired
    private JustificationRepository justificationRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private JustificationDetailsRepository justificationDetailsRepositoryMock;

    @Autowired
    private JustificationDetailsMapper justificationDetailsMapper;

    @Mock
    private JustificationDetailsService justificationDetailsServiceMock;

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
        justificationDetailsRepository.deleteAll();
        classSectionRepository.deleteAll();
        justificationRepository.deleteAll();
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
    void getJustificationDetailsAsInstructorReturnsForbidden() throws Exception {
        restJustificationDetailsMockMvc.perform(get(ENTITY_API_URL).accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
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
