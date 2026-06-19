package com.mycompany.senaattendance.web.rest;

import static com.mycompany.senaattendance.domain.JustificationAsserts.*;
import static com.mycompany.senaattendance.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.Justification;
import com.mycompany.senaattendance.domain.JustificationType;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.repository.JustificationRepository;
import com.mycompany.senaattendance.service.JustificationService;
import com.mycompany.senaattendance.service.dto.JustificationDTO;
import com.mycompany.senaattendance.service.mapper.JustificationMapper;
import java.time.LocalDate;
import java.time.ZoneId;
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
 * Integration tests for the {@link JustificationResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
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

    @Autowired
    private ObjectMapper om;

    @Autowired
    private JustificationRepository justificationRepository;

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

    @Test
    void putExistingJustification() throws Exception {
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
}
