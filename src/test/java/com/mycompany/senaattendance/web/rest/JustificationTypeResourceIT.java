package com.mycompany.senaattendance.web.rest;

import static com.mycompany.senaattendance.domain.JustificationTypeAsserts.*;
import static com.mycompany.senaattendance.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.JustificationType;
import com.mycompany.senaattendance.domain.enumeration.State;
import com.mycompany.senaattendance.repository.JustificationTypeRepository;
import com.mycompany.senaattendance.service.dto.JustificationTypeDTO;
import com.mycompany.senaattendance.service.mapper.JustificationTypeMapper;
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
 * Integration tests for the {@link JustificationTypeResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class JustificationTypeResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Integer DEFAULT_LIMIT_PER_TRIMESTER = 1;
    private static final Integer UPDATED_LIMIT_PER_TRIMESTER = 2;

    private static final State DEFAULT_STATE = State.ACTIVO;
    private static final State UPDATED_STATE = State.INACTIVO;

    private static final String ENTITY_API_URL = "/api/justification-types";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private JustificationTypeRepository justificationTypeRepository;

    @Autowired
    private JustificationTypeMapper justificationTypeMapper;

    @Autowired
    private MockMvc restJustificationTypeMockMvc;

    private JustificationType justificationType;

    private JustificationType insertedJustificationType;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static JustificationType createEntity() {
        return new JustificationType().name(DEFAULT_NAME).limitPerTrimester(DEFAULT_LIMIT_PER_TRIMESTER).state(DEFAULT_STATE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static JustificationType createUpdatedEntity() {
        return new JustificationType().name(UPDATED_NAME).limitPerTrimester(UPDATED_LIMIT_PER_TRIMESTER).state(UPDATED_STATE);
    }

    @BeforeEach
    void initTest() {
        justificationType = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedJustificationType != null) {
            justificationTypeRepository.delete(insertedJustificationType);
            insertedJustificationType = null;
        }
    }

    @Test
    void createJustificationType() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the JustificationType
        JustificationTypeDTO justificationTypeDTO = justificationTypeMapper.toDto(justificationType);
        var returnedJustificationTypeDTO = om.readValue(
            restJustificationTypeMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationTypeDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            JustificationTypeDTO.class
        );

        // Validate the JustificationType in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedJustificationType = justificationTypeMapper.toEntity(returnedJustificationTypeDTO);
        assertJustificationTypeUpdatableFieldsEquals(returnedJustificationType, getPersistedJustificationType(returnedJustificationType));

        insertedJustificationType = returnedJustificationType;
    }

    @Test
    void createJustificationTypeWithExistingId() throws Exception {
        // Create the JustificationType with an existing ID
        justificationType.setId("existing_id");
        JustificationTypeDTO justificationTypeDTO = justificationTypeMapper.toDto(justificationType);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restJustificationTypeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationTypeDTO)))
            .andExpect(status().isBadRequest());

        // Validate the JustificationType in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        justificationType.setName(null);

        // Create the JustificationType, which fails.
        JustificationTypeDTO justificationTypeDTO = justificationTypeMapper.toDto(justificationType);

        restJustificationTypeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationTypeDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkStateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        justificationType.setState(null);

        // Create the JustificationType, which fails.
        JustificationTypeDTO justificationTypeDTO = justificationTypeMapper.toDto(justificationType);

        restJustificationTypeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationTypeDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllJustificationTypes() throws Exception {
        // Initialize the database
        insertedJustificationType = justificationTypeRepository.save(justificationType);

        // Get all the justificationTypeList
        restJustificationTypeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(justificationType.getId())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].limitPerTrimester").value(hasItem(DEFAULT_LIMIT_PER_TRIMESTER)))
            .andExpect(jsonPath("$.[*].state").value(hasItem(DEFAULT_STATE.toString())));
    }

    @Test
    void getJustificationType() throws Exception {
        // Initialize the database
        insertedJustificationType = justificationTypeRepository.save(justificationType);

        // Get the justificationType
        restJustificationTypeMockMvc
            .perform(get(ENTITY_API_URL_ID, justificationType.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(justificationType.getId()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.limitPerTrimester").value(DEFAULT_LIMIT_PER_TRIMESTER))
            .andExpect(jsonPath("$.state").value(DEFAULT_STATE.toString()));
    }

    @Test
    void getNonExistingJustificationType() throws Exception {
        // Get the justificationType
        restJustificationTypeMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingJustificationType() throws Exception {
        // Initialize the database
        insertedJustificationType = justificationTypeRepository.save(justificationType);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the justificationType
        JustificationType updatedJustificationType = justificationTypeRepository.findById(justificationType.getId()).orElseThrow();
        updatedJustificationType.name(UPDATED_NAME).limitPerTrimester(UPDATED_LIMIT_PER_TRIMESTER).state(UPDATED_STATE);
        JustificationTypeDTO justificationTypeDTO = justificationTypeMapper.toDto(updatedJustificationType);

        restJustificationTypeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, justificationTypeDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(justificationTypeDTO))
            )
            .andExpect(status().isOk());

        // Validate the JustificationType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedJustificationTypeToMatchAllProperties(updatedJustificationType);
    }

    @Test
    void putNonExistingJustificationType() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        justificationType.setId(UUID.randomUUID().toString());

        // Create the JustificationType
        JustificationTypeDTO justificationTypeDTO = justificationTypeMapper.toDto(justificationType);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restJustificationTypeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, justificationTypeDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(justificationTypeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the JustificationType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchJustificationType() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        justificationType.setId(UUID.randomUUID().toString());

        // Create the JustificationType
        JustificationTypeDTO justificationTypeDTO = justificationTypeMapper.toDto(justificationType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restJustificationTypeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(justificationTypeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the JustificationType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamJustificationType() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        justificationType.setId(UUID.randomUUID().toString());

        // Create the JustificationType
        JustificationTypeDTO justificationTypeDTO = justificationTypeMapper.toDto(justificationType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restJustificationTypeMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationTypeDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the JustificationType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateJustificationTypeWithPatch() throws Exception {
        // Initialize the database
        insertedJustificationType = justificationTypeRepository.save(justificationType);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the justificationType using partial update
        JustificationType partialUpdatedJustificationType = new JustificationType();
        partialUpdatedJustificationType.setId(justificationType.getId());

        partialUpdatedJustificationType.name(UPDATED_NAME).limitPerTrimester(UPDATED_LIMIT_PER_TRIMESTER);

        restJustificationTypeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedJustificationType.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedJustificationType))
            )
            .andExpect(status().isOk());

        // Validate the JustificationType in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertJustificationTypeUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedJustificationType, justificationType),
            getPersistedJustificationType(justificationType)
        );
    }

    @Test
    void fullUpdateJustificationTypeWithPatch() throws Exception {
        // Initialize the database
        insertedJustificationType = justificationTypeRepository.save(justificationType);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the justificationType using partial update
        JustificationType partialUpdatedJustificationType = new JustificationType();
        partialUpdatedJustificationType.setId(justificationType.getId());

        partialUpdatedJustificationType.name(UPDATED_NAME).limitPerTrimester(UPDATED_LIMIT_PER_TRIMESTER).state(UPDATED_STATE);

        restJustificationTypeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedJustificationType.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedJustificationType))
            )
            .andExpect(status().isOk());

        // Validate the JustificationType in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertJustificationTypeUpdatableFieldsEquals(
            partialUpdatedJustificationType,
            getPersistedJustificationType(partialUpdatedJustificationType)
        );
    }

    @Test
    void patchNonExistingJustificationType() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        justificationType.setId(UUID.randomUUID().toString());

        // Create the JustificationType
        JustificationTypeDTO justificationTypeDTO = justificationTypeMapper.toDto(justificationType);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restJustificationTypeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, justificationTypeDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(justificationTypeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the JustificationType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchJustificationType() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        justificationType.setId(UUID.randomUUID().toString());

        // Create the JustificationType
        JustificationTypeDTO justificationTypeDTO = justificationTypeMapper.toDto(justificationType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restJustificationTypeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(justificationTypeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the JustificationType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamJustificationType() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        justificationType.setId(UUID.randomUUID().toString());

        // Create the JustificationType
        JustificationTypeDTO justificationTypeDTO = justificationTypeMapper.toDto(justificationType);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restJustificationTypeMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(justificationTypeDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the JustificationType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteJustificationType() throws Exception {
        // Initialize the database
        insertedJustificationType = justificationTypeRepository.save(justificationType);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the justificationType
        restJustificationTypeMockMvc
            .perform(delete(ENTITY_API_URL_ID, justificationType.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return justificationTypeRepository.count();
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

    protected JustificationType getPersistedJustificationType(JustificationType justificationType) {
        return justificationTypeRepository.findById(justificationType.getId()).orElseThrow();
    }

    protected void assertPersistedJustificationTypeToMatchAllProperties(JustificationType expectedJustificationType) {
        assertJustificationTypeAllPropertiesEquals(expectedJustificationType, getPersistedJustificationType(expectedJustificationType));
    }

    protected void assertPersistedJustificationTypeToMatchUpdatableProperties(JustificationType expectedJustificationType) {
        assertJustificationTypeAllUpdatablePropertiesEquals(
            expectedJustificationType,
            getPersistedJustificationType(expectedJustificationType)
        );
    }
}
