package com.mycompany.senaattendance.web.rest;

import static com.mycompany.senaattendance.domain.TrimesterAsserts.*;
import static com.mycompany.senaattendance.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.domain.enumeration.State;
import com.mycompany.senaattendance.repository.TrimesterRepository;
import com.mycompany.senaattendance.service.dto.TrimesterDTO;
import com.mycompany.senaattendance.service.mapper.TrimesterMapper;
import java.time.LocalDate;
import java.time.ZoneId;
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
 * Integration tests for the {@link TrimesterResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TrimesterResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_START_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_START_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final LocalDate DEFAULT_END_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_END_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final State DEFAULT_STATE = State.ACTIVO;
    private static final State UPDATED_STATE = State.INACTIVO;

    private static final String ENTITY_API_URL = "/api/trimesters";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TrimesterRepository trimesterRepository;

    @Autowired
    private TrimesterMapper trimesterMapper;

    @Autowired
    private MockMvc restTrimesterMockMvc;

    private Trimester trimester;

    private Trimester insertedTrimester;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Trimester createEntity() {
        return new Trimester().name(DEFAULT_NAME).startDate(DEFAULT_START_DATE).endDate(DEFAULT_END_DATE).state(DEFAULT_STATE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Trimester createUpdatedEntity() {
        return new Trimester().name(UPDATED_NAME).startDate(UPDATED_START_DATE).endDate(UPDATED_END_DATE).state(UPDATED_STATE);
    }

    @BeforeEach
    void initTest() {
        trimester = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedTrimester != null) {
            trimesterRepository.delete(insertedTrimester);
            insertedTrimester = null;
        }
    }

    @Test
    void createTrimester() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Trimester
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(trimester);
        var returnedTrimesterDTO = om.readValue(
            restTrimesterMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(trimesterDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TrimesterDTO.class
        );

        // Validate the Trimester in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedTrimester = trimesterMapper.toEntity(returnedTrimesterDTO);
        assertTrimesterUpdatableFieldsEquals(returnedTrimester, getPersistedTrimester(returnedTrimester));

        insertedTrimester = returnedTrimester;
    }

    @Test
    void createTrimesterWithExistingId() throws Exception {
        // Create the Trimester with an existing ID
        trimester.setId("existing_id");
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(trimester);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTrimesterMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(trimesterDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Trimester in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        trimester.setName(null);

        // Create the Trimester, which fails.
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(trimester);

        restTrimesterMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(trimesterDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkStartDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        trimester.setStartDate(null);

        // Create the Trimester, which fails.
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(trimester);

        restTrimesterMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(trimesterDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkEndDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        trimester.setEndDate(null);

        // Create the Trimester, which fails.
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(trimester);

        restTrimesterMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(trimesterDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkStateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        trimester.setState(null);

        // Create the Trimester, which fails.
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(trimester);

        restTrimesterMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(trimesterDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllTrimesters() throws Exception {
        // Initialize the database
        insertedTrimester = trimesterRepository.save(trimester);

        // Get all the trimesterList
        restTrimesterMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(trimester.getId())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].startDate").value(hasItem(DEFAULT_START_DATE.toString())))
            .andExpect(jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE.toString())))
            .andExpect(jsonPath("$.[*].state").value(hasItem(DEFAULT_STATE.toString())));
    }

    @Test
    void getTrimester() throws Exception {
        // Initialize the database
        insertedTrimester = trimesterRepository.save(trimester);

        // Get the trimester
        restTrimesterMockMvc
            .perform(get(ENTITY_API_URL_ID, trimester.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(trimester.getId()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.startDate").value(DEFAULT_START_DATE.toString()))
            .andExpect(jsonPath("$.endDate").value(DEFAULT_END_DATE.toString()))
            .andExpect(jsonPath("$.state").value(DEFAULT_STATE.toString()));
    }

    @Test
    void getNonExistingTrimester() throws Exception {
        // Get the trimester
        restTrimesterMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingTrimester() throws Exception {
        // Initialize the database
        insertedTrimester = trimesterRepository.save(trimester);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the trimester
        Trimester updatedTrimester = trimesterRepository.findById(trimester.getId()).orElseThrow();
        updatedTrimester.name(UPDATED_NAME).startDate(UPDATED_START_DATE).endDate(UPDATED_END_DATE).state(UPDATED_STATE);
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(updatedTrimester);

        restTrimesterMockMvc
            .perform(
                put(ENTITY_API_URL_ID, trimesterDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(trimesterDTO))
            )
            .andExpect(status().isOk());

        // Validate the Trimester in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTrimesterToMatchAllProperties(updatedTrimester);
    }

    @Test
    void putNonExistingTrimester() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        trimester.setId(UUID.randomUUID().toString());

        // Create the Trimester
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(trimester);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTrimesterMockMvc
            .perform(
                put(ENTITY_API_URL_ID, trimesterDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(trimesterDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Trimester in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchTrimester() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        trimester.setId(UUID.randomUUID().toString());

        // Create the Trimester
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(trimester);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTrimesterMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(trimesterDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Trimester in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamTrimester() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        trimester.setId(UUID.randomUUID().toString());

        // Create the Trimester
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(trimester);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTrimesterMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(trimesterDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Trimester in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateTrimesterWithPatch() throws Exception {
        // Initialize the database
        insertedTrimester = trimesterRepository.save(trimester);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the trimester using partial update
        Trimester partialUpdatedTrimester = new Trimester();
        partialUpdatedTrimester.setId(trimester.getId());

        partialUpdatedTrimester.endDate(UPDATED_END_DATE).state(UPDATED_STATE);

        restTrimesterMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTrimester.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTrimester))
            )
            .andExpect(status().isOk());

        // Validate the Trimester in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTrimesterUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedTrimester, trimester),
            getPersistedTrimester(trimester)
        );
    }

    @Test
    void fullUpdateTrimesterWithPatch() throws Exception {
        // Initialize the database
        insertedTrimester = trimesterRepository.save(trimester);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the trimester using partial update
        Trimester partialUpdatedTrimester = new Trimester();
        partialUpdatedTrimester.setId(trimester.getId());

        partialUpdatedTrimester.name(UPDATED_NAME).startDate(UPDATED_START_DATE).endDate(UPDATED_END_DATE).state(UPDATED_STATE);

        restTrimesterMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTrimester.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTrimester))
            )
            .andExpect(status().isOk());

        // Validate the Trimester in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTrimesterUpdatableFieldsEquals(partialUpdatedTrimester, getPersistedTrimester(partialUpdatedTrimester));
    }

    @Test
    void patchNonExistingTrimester() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        trimester.setId(UUID.randomUUID().toString());

        // Create the Trimester
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(trimester);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTrimesterMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, trimesterDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(trimesterDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Trimester in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchTrimester() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        trimester.setId(UUID.randomUUID().toString());

        // Create the Trimester
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(trimester);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTrimesterMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(trimesterDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Trimester in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamTrimester() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        trimester.setId(UUID.randomUUID().toString());

        // Create the Trimester
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(trimester);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTrimesterMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(trimesterDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Trimester in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteTrimester() throws Exception {
        // Initialize the database
        insertedTrimester = trimesterRepository.save(trimester);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the trimester
        restTrimesterMockMvc
            .perform(delete(ENTITY_API_URL_ID, trimester.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return trimesterRepository.count();
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

    protected Trimester getPersistedTrimester(Trimester trimester) {
        return trimesterRepository.findById(trimester.getId()).orElseThrow();
    }

    protected void assertPersistedTrimesterToMatchAllProperties(Trimester expectedTrimester) {
        assertTrimesterAllPropertiesEquals(expectedTrimester, getPersistedTrimester(expectedTrimester));
    }

    protected void assertPersistedTrimesterToMatchUpdatableProperties(Trimester expectedTrimester) {
        assertTrimesterAllUpdatablePropertiesEquals(expectedTrimester, getPersistedTrimester(expectedTrimester));
    }
}
