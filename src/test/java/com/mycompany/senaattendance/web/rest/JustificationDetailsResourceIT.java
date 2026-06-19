package com.mycompany.senaattendance.web.rest;

import static com.mycompany.senaattendance.domain.JustificationDetailsAsserts.*;
import static com.mycompany.senaattendance.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Justification;
import com.mycompany.senaattendance.domain.JustificationDetails;
import com.mycompany.senaattendance.domain.enumeration.StateJustification;
import com.mycompany.senaattendance.repository.JustificationDetailsRepository;
import com.mycompany.senaattendance.service.JustificationDetailsService;
import com.mycompany.senaattendance.service.dto.JustificationDetailsDTO;
import com.mycompany.senaattendance.service.mapper.JustificationDetailsMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
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
@WithMockUser
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

    @Autowired
    private ObjectMapper om;

    @Autowired
    private JustificationDetailsRepository justificationDetailsRepository;

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

    @Test
    void putExistingJustificationDetails() throws Exception {
        // Initialize the database
        insertedJustificationDetails = justificationDetailsRepository.save(justificationDetails);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the justificationDetails
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
            .andExpect(status().isOk());

        // Validate the JustificationDetails in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedJustificationDetailsToMatchAllProperties(updatedJustificationDetails);
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

    @Test
    void partialUpdateJustificationDetailsWithPatch() throws Exception {
        // Initialize the database
        insertedJustificationDetails = justificationDetailsRepository.save(justificationDetails);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the justificationDetails using partial update
        JustificationDetails partialUpdatedJustificationDetails = new JustificationDetails();
        partialUpdatedJustificationDetails.setId(justificationDetails.getId());

        partialUpdatedJustificationDetails
            .stateJustification(UPDATED_STATE_JUSTIFICATION)
            .rejectionReason(UPDATED_REJECTION_REASON)
            .correctionText(UPDATED_CORRECTION_TEXT)
            .correctionFileUrl(UPDATED_CORRECTION_FILE_URL)
            .correctionFileUrlContentType(UPDATED_CORRECTION_FILE_URL_CONTENT_TYPE)
            .responseDate(UPDATED_RESPONSE_DATE);

        restJustificationDetailsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedJustificationDetails.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedJustificationDetails))
            )
            .andExpect(status().isOk());

        // Validate the JustificationDetails in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertJustificationDetailsUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedJustificationDetails, justificationDetails),
            getPersistedJustificationDetails(justificationDetails)
        );
    }

    @Test
    void fullUpdateJustificationDetailsWithPatch() throws Exception {
        // Initialize the database
        insertedJustificationDetails = justificationDetailsRepository.save(justificationDetails);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the justificationDetails using partial update
        JustificationDetails partialUpdatedJustificationDetails = new JustificationDetails();
        partialUpdatedJustificationDetails.setId(justificationDetails.getId());

        partialUpdatedJustificationDetails
            .stateJustification(UPDATED_STATE_JUSTIFICATION)
            .rejectionReason(UPDATED_REJECTION_REASON)
            .correctionText(UPDATED_CORRECTION_TEXT)
            .correctionFileUrl(UPDATED_CORRECTION_FILE_URL)
            .correctionFileUrlContentType(UPDATED_CORRECTION_FILE_URL_CONTENT_TYPE)
            .responseDate(UPDATED_RESPONSE_DATE);

        restJustificationDetailsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedJustificationDetails.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedJustificationDetails))
            )
            .andExpect(status().isOk());

        // Validate the JustificationDetails in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertJustificationDetailsUpdatableFieldsEquals(
            partialUpdatedJustificationDetails,
            getPersistedJustificationDetails(partialUpdatedJustificationDetails)
        );
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
