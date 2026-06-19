package com.mycompany.senaattendance.web.rest;

import static com.mycompany.senaattendance.domain.ModalityAsserts.*;
import static com.mycompany.senaattendance.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.Modality;
import com.mycompany.senaattendance.repository.ModalityRepository;
import com.mycompany.senaattendance.service.dto.ModalityDTO;
import com.mycompany.senaattendance.service.mapper.ModalityMapper;
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
 * Integration tests for the {@link ModalityResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ModalityResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Boolean DEFAULT_IS_ACTIVE = false;
    private static final Boolean UPDATED_IS_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/modalities";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ModalityRepository modalityRepository;

    @Autowired
    private ModalityMapper modalityMapper;

    @Autowired
    private MockMvc restModalityMockMvc;

    private Modality modality;

    private Modality insertedModality;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Modality createEntity() {
        return new Modality().name(DEFAULT_NAME).isActive(DEFAULT_IS_ACTIVE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Modality createUpdatedEntity() {
        return new Modality().name(UPDATED_NAME).isActive(UPDATED_IS_ACTIVE);
    }

    @BeforeEach
    void initTest() {
        modality = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedModality != null) {
            modalityRepository.delete(insertedModality);
            insertedModality = null;
        }
    }

    @Test
    void createModality() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Modality
        ModalityDTO modalityDTO = modalityMapper.toDto(modality);
        var returnedModalityDTO = om.readValue(
            restModalityMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(modalityDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ModalityDTO.class
        );

        // Validate the Modality in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedModality = modalityMapper.toEntity(returnedModalityDTO);
        assertModalityUpdatableFieldsEquals(returnedModality, getPersistedModality(returnedModality));

        insertedModality = returnedModality;
    }

    @Test
    void createModalityWithExistingId() throws Exception {
        // Create the Modality with an existing ID
        modality.setId("existing_id");
        ModalityDTO modalityDTO = modalityMapper.toDto(modality);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restModalityMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(modalityDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Modality in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        modality.setName(null);

        // Create the Modality, which fails.
        ModalityDTO modalityDTO = modalityMapper.toDto(modality);

        restModalityMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(modalityDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkIsActiveIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        modality.setIsActive(null);

        // Create the Modality, which fails.
        ModalityDTO modalityDTO = modalityMapper.toDto(modality);

        restModalityMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(modalityDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllModalities() throws Exception {
        // Initialize the database
        insertedModality = modalityRepository.save(modality);

        // Get all the modalityList
        restModalityMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(modality.getId())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].isActive").value(hasItem(DEFAULT_IS_ACTIVE)));
    }

    @Test
    void getModality() throws Exception {
        // Initialize the database
        insertedModality = modalityRepository.save(modality);

        // Get the modality
        restModalityMockMvc
            .perform(get(ENTITY_API_URL_ID, modality.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(modality.getId()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.isActive").value(DEFAULT_IS_ACTIVE));
    }

    @Test
    void getNonExistingModality() throws Exception {
        // Get the modality
        restModalityMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingModality() throws Exception {
        // Initialize the database
        insertedModality = modalityRepository.save(modality);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the modality
        Modality updatedModality = modalityRepository.findById(modality.getId()).orElseThrow();
        updatedModality.name(UPDATED_NAME).isActive(UPDATED_IS_ACTIVE);
        ModalityDTO modalityDTO = modalityMapper.toDto(updatedModality);

        restModalityMockMvc
            .perform(
                put(ENTITY_API_URL_ID, modalityDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(modalityDTO))
            )
            .andExpect(status().isOk());

        // Validate the Modality in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedModalityToMatchAllProperties(updatedModality);
    }

    @Test
    void putNonExistingModality() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        modality.setId(UUID.randomUUID().toString());

        // Create the Modality
        ModalityDTO modalityDTO = modalityMapper.toDto(modality);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restModalityMockMvc
            .perform(
                put(ENTITY_API_URL_ID, modalityDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(modalityDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Modality in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchModality() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        modality.setId(UUID.randomUUID().toString());

        // Create the Modality
        ModalityDTO modalityDTO = modalityMapper.toDto(modality);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restModalityMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(modalityDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Modality in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamModality() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        modality.setId(UUID.randomUUID().toString());

        // Create the Modality
        ModalityDTO modalityDTO = modalityMapper.toDto(modality);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restModalityMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(modalityDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Modality in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateModalityWithPatch() throws Exception {
        // Initialize the database
        insertedModality = modalityRepository.save(modality);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the modality using partial update
        Modality partialUpdatedModality = new Modality();
        partialUpdatedModality.setId(modality.getId());

        partialUpdatedModality.name(UPDATED_NAME).isActive(UPDATED_IS_ACTIVE);

        restModalityMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedModality.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedModality))
            )
            .andExpect(status().isOk());

        // Validate the Modality in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertModalityUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedModality, modality), getPersistedModality(modality));
    }

    @Test
    void fullUpdateModalityWithPatch() throws Exception {
        // Initialize the database
        insertedModality = modalityRepository.save(modality);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the modality using partial update
        Modality partialUpdatedModality = new Modality();
        partialUpdatedModality.setId(modality.getId());

        partialUpdatedModality.name(UPDATED_NAME).isActive(UPDATED_IS_ACTIVE);

        restModalityMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedModality.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedModality))
            )
            .andExpect(status().isOk());

        // Validate the Modality in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertModalityUpdatableFieldsEquals(partialUpdatedModality, getPersistedModality(partialUpdatedModality));
    }

    @Test
    void patchNonExistingModality() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        modality.setId(UUID.randomUUID().toString());

        // Create the Modality
        ModalityDTO modalityDTO = modalityMapper.toDto(modality);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restModalityMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, modalityDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(modalityDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Modality in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchModality() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        modality.setId(UUID.randomUUID().toString());

        // Create the Modality
        ModalityDTO modalityDTO = modalityMapper.toDto(modality);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restModalityMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(modalityDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Modality in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamModality() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        modality.setId(UUID.randomUUID().toString());

        // Create the Modality
        ModalityDTO modalityDTO = modalityMapper.toDto(modality);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restModalityMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(modalityDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Modality in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteModality() throws Exception {
        // Initialize the database
        insertedModality = modalityRepository.save(modality);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the modality
        restModalityMockMvc
            .perform(delete(ENTITY_API_URL_ID, modality.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return modalityRepository.count();
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

    protected Modality getPersistedModality(Modality modality) {
        return modalityRepository.findById(modality.getId()).orElseThrow();
    }

    protected void assertPersistedModalityToMatchAllProperties(Modality expectedModality) {
        assertModalityAllPropertiesEquals(expectedModality, getPersistedModality(expectedModality));
    }

    protected void assertPersistedModalityToMatchUpdatableProperties(Modality expectedModality) {
        assertModalityAllUpdatablePropertiesEquals(expectedModality, getPersistedModality(expectedModality));
    }
}
