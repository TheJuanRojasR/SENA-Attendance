package com.mycompany.senaattendance.web.rest;

import static com.mycompany.senaattendance.domain.GlobalConfigurationAsserts.*;
import static com.mycompany.senaattendance.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.GlobalConfiguration;
import com.mycompany.senaattendance.repository.GlobalConfigurationRepository;
import com.mycompany.senaattendance.service.dto.GlobalConfigurationDTO;
import com.mycompany.senaattendance.service.mapper.GlobalConfigurationMapper;
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
 * Integration tests for the {@link GlobalConfigurationResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class GlobalConfigurationResourceIT {

    private static final Integer DEFAULT_STUDENT_JUSTIFICATION_DAYS = 1;
    private static final Integer UPDATED_STUDENT_JUSTIFICATION_DAYS = 2;

    private static final Integer DEFAULT_INSTRUCTOR_RESPONSE_DAYS = 1;
    private static final Integer UPDATED_INSTRUCTOR_RESPONSE_DAYS = 2;

    private static final Integer DEFAULT_LATE_ARRIVALS_TO_FAIL = 1;
    private static final Integer UPDATED_LATE_ARRIVALS_TO_FAIL = 2;

    private static final Integer DEFAULT_MAX_POSTPONEMENT_JUSTIFICATIONS = 1;
    private static final Integer UPDATED_MAX_POSTPONEMENT_JUSTIFICATIONS = 2;

    private static final Integer DEFAULT_STANDARD_TRIMESTER_MONTHS = 1;
    private static final Integer UPDATED_STANDARD_TRIMESTER_MONTHS = 2;

    private static final String ENTITY_API_URL = "/api/global-configurations";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private GlobalConfigurationRepository globalConfigurationRepository;

    @Autowired
    private GlobalConfigurationMapper globalConfigurationMapper;

    @Autowired
    private MockMvc restGlobalConfigurationMockMvc;

    private GlobalConfiguration globalConfiguration;

    private GlobalConfiguration insertedGlobalConfiguration;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static GlobalConfiguration createEntity() {
        return new GlobalConfiguration()
            .studentJustificationDays(DEFAULT_STUDENT_JUSTIFICATION_DAYS)
            .instructorResponseDays(DEFAULT_INSTRUCTOR_RESPONSE_DAYS)
            .lateArrivalsToFail(DEFAULT_LATE_ARRIVALS_TO_FAIL)
            .maxPostponementJustifications(DEFAULT_MAX_POSTPONEMENT_JUSTIFICATIONS)
            .standardTrimesterMonths(DEFAULT_STANDARD_TRIMESTER_MONTHS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static GlobalConfiguration createUpdatedEntity() {
        return new GlobalConfiguration()
            .studentJustificationDays(UPDATED_STUDENT_JUSTIFICATION_DAYS)
            .instructorResponseDays(UPDATED_INSTRUCTOR_RESPONSE_DAYS)
            .lateArrivalsToFail(UPDATED_LATE_ARRIVALS_TO_FAIL)
            .maxPostponementJustifications(UPDATED_MAX_POSTPONEMENT_JUSTIFICATIONS)
            .standardTrimesterMonths(UPDATED_STANDARD_TRIMESTER_MONTHS);
    }

    @BeforeEach
    void initTest() {
        globalConfiguration = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedGlobalConfiguration != null) {
            globalConfigurationRepository.delete(insertedGlobalConfiguration);
            insertedGlobalConfiguration = null;
        }
    }

    @Test
    void createGlobalConfiguration() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the GlobalConfiguration
        GlobalConfigurationDTO globalConfigurationDTO = globalConfigurationMapper.toDto(globalConfiguration);
        var returnedGlobalConfigurationDTO = om.readValue(
            restGlobalConfigurationMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(globalConfigurationDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            GlobalConfigurationDTO.class
        );

        // Validate the GlobalConfiguration in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedGlobalConfiguration = globalConfigurationMapper.toEntity(returnedGlobalConfigurationDTO);
        assertGlobalConfigurationUpdatableFieldsEquals(
            returnedGlobalConfiguration,
            getPersistedGlobalConfiguration(returnedGlobalConfiguration)
        );

        insertedGlobalConfiguration = returnedGlobalConfiguration;
    }

    @Test
    void createGlobalConfigurationWithExistingId() throws Exception {
        // Create the GlobalConfiguration with an existing ID
        globalConfiguration.setId("existing_id");
        GlobalConfigurationDTO globalConfigurationDTO = globalConfigurationMapper.toDto(globalConfiguration);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restGlobalConfigurationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(globalConfigurationDTO)))
            .andExpect(status().isBadRequest());

        // Validate the GlobalConfiguration in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkStudentJustificationDaysIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        globalConfiguration.setStudentJustificationDays(null);

        // Create the GlobalConfiguration, which fails.
        GlobalConfigurationDTO globalConfigurationDTO = globalConfigurationMapper.toDto(globalConfiguration);

        restGlobalConfigurationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(globalConfigurationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkInstructorResponseDaysIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        globalConfiguration.setInstructorResponseDays(null);

        // Create the GlobalConfiguration, which fails.
        GlobalConfigurationDTO globalConfigurationDTO = globalConfigurationMapper.toDto(globalConfiguration);

        restGlobalConfigurationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(globalConfigurationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkLateArrivalsToFailIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        globalConfiguration.setLateArrivalsToFail(null);

        // Create the GlobalConfiguration, which fails.
        GlobalConfigurationDTO globalConfigurationDTO = globalConfigurationMapper.toDto(globalConfiguration);

        restGlobalConfigurationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(globalConfigurationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkMaxPostponementJustificationsIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        globalConfiguration.setMaxPostponementJustifications(null);

        // Create the GlobalConfiguration, which fails.
        GlobalConfigurationDTO globalConfigurationDTO = globalConfigurationMapper.toDto(globalConfiguration);

        restGlobalConfigurationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(globalConfigurationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkStandardTrimesterMonthsIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        globalConfiguration.setStandardTrimesterMonths(null);

        // Create the GlobalConfiguration, which fails.
        GlobalConfigurationDTO globalConfigurationDTO = globalConfigurationMapper.toDto(globalConfiguration);

        restGlobalConfigurationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(globalConfigurationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllGlobalConfigurations() throws Exception {
        // Initialize the database
        insertedGlobalConfiguration = globalConfigurationRepository.save(globalConfiguration);

        // Get all the globalConfigurationList
        restGlobalConfigurationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(globalConfiguration.getId())))
            .andExpect(jsonPath("$.[*].studentJustificationDays").value(hasItem(DEFAULT_STUDENT_JUSTIFICATION_DAYS)))
            .andExpect(jsonPath("$.[*].instructorResponseDays").value(hasItem(DEFAULT_INSTRUCTOR_RESPONSE_DAYS)))
            .andExpect(jsonPath("$.[*].lateArrivalsToFail").value(hasItem(DEFAULT_LATE_ARRIVALS_TO_FAIL)))
            .andExpect(jsonPath("$.[*].maxPostponementJustifications").value(hasItem(DEFAULT_MAX_POSTPONEMENT_JUSTIFICATIONS)))
            .andExpect(jsonPath("$.[*].standardTrimesterMonths").value(hasItem(DEFAULT_STANDARD_TRIMESTER_MONTHS)));
    }

    @Test
    void getGlobalConfiguration() throws Exception {
        // Initialize the database
        insertedGlobalConfiguration = globalConfigurationRepository.save(globalConfiguration);

        // Get the globalConfiguration
        restGlobalConfigurationMockMvc
            .perform(get(ENTITY_API_URL_ID, globalConfiguration.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(globalConfiguration.getId()))
            .andExpect(jsonPath("$.studentJustificationDays").value(DEFAULT_STUDENT_JUSTIFICATION_DAYS))
            .andExpect(jsonPath("$.instructorResponseDays").value(DEFAULT_INSTRUCTOR_RESPONSE_DAYS))
            .andExpect(jsonPath("$.lateArrivalsToFail").value(DEFAULT_LATE_ARRIVALS_TO_FAIL))
            .andExpect(jsonPath("$.maxPostponementJustifications").value(DEFAULT_MAX_POSTPONEMENT_JUSTIFICATIONS))
            .andExpect(jsonPath("$.standardTrimesterMonths").value(DEFAULT_STANDARD_TRIMESTER_MONTHS));
    }

    @Test
    void getNonExistingGlobalConfiguration() throws Exception {
        // Get the globalConfiguration
        restGlobalConfigurationMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingGlobalConfiguration() throws Exception {
        // Initialize the database
        insertedGlobalConfiguration = globalConfigurationRepository.save(globalConfiguration);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the globalConfiguration
        GlobalConfiguration updatedGlobalConfiguration = globalConfigurationRepository.findById(globalConfiguration.getId()).orElseThrow();
        updatedGlobalConfiguration
            .studentJustificationDays(UPDATED_STUDENT_JUSTIFICATION_DAYS)
            .instructorResponseDays(UPDATED_INSTRUCTOR_RESPONSE_DAYS)
            .lateArrivalsToFail(UPDATED_LATE_ARRIVALS_TO_FAIL)
            .maxPostponementJustifications(UPDATED_MAX_POSTPONEMENT_JUSTIFICATIONS)
            .standardTrimesterMonths(UPDATED_STANDARD_TRIMESTER_MONTHS);
        GlobalConfigurationDTO globalConfigurationDTO = globalConfigurationMapper.toDto(updatedGlobalConfiguration);

        restGlobalConfigurationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, globalConfigurationDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(globalConfigurationDTO))
            )
            .andExpect(status().isOk());

        // Validate the GlobalConfiguration in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedGlobalConfigurationToMatchAllProperties(updatedGlobalConfiguration);
    }

    @Test
    void putNonExistingGlobalConfiguration() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        globalConfiguration.setId(UUID.randomUUID().toString());

        // Create the GlobalConfiguration
        GlobalConfigurationDTO globalConfigurationDTO = globalConfigurationMapper.toDto(globalConfiguration);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGlobalConfigurationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, globalConfigurationDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(globalConfigurationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the GlobalConfiguration in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchGlobalConfiguration() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        globalConfiguration.setId(UUID.randomUUID().toString());

        // Create the GlobalConfiguration
        GlobalConfigurationDTO globalConfigurationDTO = globalConfigurationMapper.toDto(globalConfiguration);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGlobalConfigurationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(globalConfigurationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the GlobalConfiguration in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamGlobalConfiguration() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        globalConfiguration.setId(UUID.randomUUID().toString());

        // Create the GlobalConfiguration
        GlobalConfigurationDTO globalConfigurationDTO = globalConfigurationMapper.toDto(globalConfiguration);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGlobalConfigurationMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(globalConfigurationDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the GlobalConfiguration in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateGlobalConfigurationWithPatch() throws Exception {
        // Initialize the database
        insertedGlobalConfiguration = globalConfigurationRepository.save(globalConfiguration);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the globalConfiguration using partial update
        GlobalConfiguration partialUpdatedGlobalConfiguration = new GlobalConfiguration();
        partialUpdatedGlobalConfiguration.setId(globalConfiguration.getId());

        restGlobalConfigurationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGlobalConfiguration.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGlobalConfiguration))
            )
            .andExpect(status().isOk());

        // Validate the GlobalConfiguration in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertGlobalConfigurationUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedGlobalConfiguration, globalConfiguration),
            getPersistedGlobalConfiguration(globalConfiguration)
        );
    }

    @Test
    void fullUpdateGlobalConfigurationWithPatch() throws Exception {
        // Initialize the database
        insertedGlobalConfiguration = globalConfigurationRepository.save(globalConfiguration);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the globalConfiguration using partial update
        GlobalConfiguration partialUpdatedGlobalConfiguration = new GlobalConfiguration();
        partialUpdatedGlobalConfiguration.setId(globalConfiguration.getId());

        partialUpdatedGlobalConfiguration
            .studentJustificationDays(UPDATED_STUDENT_JUSTIFICATION_DAYS)
            .instructorResponseDays(UPDATED_INSTRUCTOR_RESPONSE_DAYS)
            .lateArrivalsToFail(UPDATED_LATE_ARRIVALS_TO_FAIL)
            .maxPostponementJustifications(UPDATED_MAX_POSTPONEMENT_JUSTIFICATIONS)
            .standardTrimesterMonths(UPDATED_STANDARD_TRIMESTER_MONTHS);

        restGlobalConfigurationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGlobalConfiguration.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGlobalConfiguration))
            )
            .andExpect(status().isOk());

        // Validate the GlobalConfiguration in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertGlobalConfigurationUpdatableFieldsEquals(
            partialUpdatedGlobalConfiguration,
            getPersistedGlobalConfiguration(partialUpdatedGlobalConfiguration)
        );
    }

    @Test
    void patchNonExistingGlobalConfiguration() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        globalConfiguration.setId(UUID.randomUUID().toString());

        // Create the GlobalConfiguration
        GlobalConfigurationDTO globalConfigurationDTO = globalConfigurationMapper.toDto(globalConfiguration);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGlobalConfigurationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, globalConfigurationDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(globalConfigurationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the GlobalConfiguration in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchGlobalConfiguration() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        globalConfiguration.setId(UUID.randomUUID().toString());

        // Create the GlobalConfiguration
        GlobalConfigurationDTO globalConfigurationDTO = globalConfigurationMapper.toDto(globalConfiguration);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGlobalConfigurationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(globalConfigurationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the GlobalConfiguration in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamGlobalConfiguration() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        globalConfiguration.setId(UUID.randomUUID().toString());

        // Create the GlobalConfiguration
        GlobalConfigurationDTO globalConfigurationDTO = globalConfigurationMapper.toDto(globalConfiguration);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGlobalConfigurationMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(globalConfigurationDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the GlobalConfiguration in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteGlobalConfiguration() throws Exception {
        // Initialize the database
        insertedGlobalConfiguration = globalConfigurationRepository.save(globalConfiguration);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the globalConfiguration
        restGlobalConfigurationMockMvc
            .perform(delete(ENTITY_API_URL_ID, globalConfiguration.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return globalConfigurationRepository.count();
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

    protected GlobalConfiguration getPersistedGlobalConfiguration(GlobalConfiguration globalConfiguration) {
        return globalConfigurationRepository.findById(globalConfiguration.getId()).orElseThrow();
    }

    protected void assertPersistedGlobalConfigurationToMatchAllProperties(GlobalConfiguration expectedGlobalConfiguration) {
        assertGlobalConfigurationAllPropertiesEquals(
            expectedGlobalConfiguration,
            getPersistedGlobalConfiguration(expectedGlobalConfiguration)
        );
    }

    protected void assertPersistedGlobalConfigurationToMatchUpdatableProperties(GlobalConfiguration expectedGlobalConfiguration) {
        assertGlobalConfigurationAllUpdatablePropertiesEquals(
            expectedGlobalConfiguration,
            getPersistedGlobalConfiguration(expectedGlobalConfiguration)
        );
    }
}
