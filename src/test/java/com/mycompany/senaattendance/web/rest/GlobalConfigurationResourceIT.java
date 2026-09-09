package com.mycompany.senaattendance.web.rest;

import static com.mycompany.senaattendance.domain.GlobalConfigurationAsserts.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.GlobalConfiguration;
import com.mycompany.senaattendance.repository.GlobalConfigurationRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.dto.GlobalConfigurationDTO;
import com.mycompany.senaattendance.service.impl.GlobalConfigurationServiceImpl;
import java.util.List;
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
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class GlobalConfigurationResourceIT {

    private static final Integer DEFAULT_STUDENT_JUSTIFICATION_DAYS = GlobalConfigurationServiceImpl.DEFAULT_STUDENT_JUSTIFICATION_DAYS;
    private static final Integer DEFAULT_INSTRUCTOR_RESPONSE_DAYS = GlobalConfigurationServiceImpl.DEFAULT_INSTRUCTOR_RESPONSE_DAYS;

    private static final Integer UPDATED_STUDENT_JUSTIFICATION_DAYS = 7;
    private static final Integer UPDATED_INSTRUCTOR_RESPONSE_DAYS = 3;

    private static final String ENTITY_API_URL = "/api/global-configurations";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private GlobalConfigurationRepository globalConfigurationRepository;

    @Autowired
    private MockMvc restGlobalConfigurationMockMvc;

    private GlobalConfiguration globalConfiguration;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static GlobalConfiguration createEntity() {
        return new GlobalConfiguration()
            .studentJustificationDays(DEFAULT_STUDENT_JUSTIFICATION_DAYS)
            .instructorResponseDays(DEFAULT_INSTRUCTOR_RESPONSE_DAYS);
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
            .instructorResponseDays(UPDATED_INSTRUCTOR_RESPONSE_DAYS);
    }

    @BeforeEach
    void initTest() {
        // The configuration is a singleton: start each test from a clean collection.
        globalConfigurationRepository.deleteAll();
        globalConfiguration = createEntity();
    }

    @AfterEach
    void cleanup() {
        globalConfigurationRepository.deleteAll();
    }

    private GlobalConfiguration saveSingleton() {
        List<GlobalConfiguration> configurations = globalConfigurationRepository.findAll();
        if (configurations.isEmpty()) {
            return globalConfigurationRepository.save(globalConfiguration);
        }
        GlobalConfiguration configuration = configurations.get(0);
        configuration.setStudentJustificationDays(globalConfiguration.getStudentJustificationDays());
        configuration.setInstructorResponseDays(globalConfiguration.getInstructorResponseDays());
        return globalConfigurationRepository.save(configuration);
    }

    @Test
    void getGlobalConfigurationReturnsSeededDefaults() throws Exception {
        // No configuration row: GET must re-seed it with the default values and return the singleton object.
        restGlobalConfigurationMockMvc
            .perform(get(ENTITY_API_URL))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.studentJustificationDays").value(DEFAULT_STUDENT_JUSTIFICATION_DAYS))
            .andExpect(jsonPath("$.instructorResponseDays").value(DEFAULT_INSTRUCTOR_RESPONSE_DAYS));

        assertThat(globalConfigurationRepository.count()).isEqualTo(1);
    }

    @Test
    void patchGlobalConfigurationWithAdmin() throws Exception {
        // Initialize the database
        globalConfiguration = saveSingleton();
        long databaseSizeBeforeUpdate = globalConfigurationRepository.count();

        // Update the globalConfiguration using patch
        GlobalConfigurationDTO updatedDTO = new GlobalConfigurationDTO();
        updatedDTO.setId(globalConfiguration.getId());
        updatedDTO.setStudentJustificationDays(UPDATED_STUDENT_JUSTIFICATION_DAYS);
        updatedDTO.setInstructorResponseDays(UPDATED_INSTRUCTOR_RESPONSE_DAYS);

        restGlobalConfigurationMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(updatedDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.studentJustificationDays").value(UPDATED_STUDENT_JUSTIFICATION_DAYS))
            .andExpect(jsonPath("$.instructorResponseDays").value(UPDATED_INSTRUCTOR_RESPONSE_DAYS));

        // Validate the GlobalConfiguration in the database
        assertThat(globalConfigurationRepository.count()).isEqualTo(databaseSizeBeforeUpdate);
        GlobalConfiguration persistedGlobalConfiguration = globalConfigurationRepository
            .findById(globalConfiguration.getId())
            .orElseThrow();
        assertGlobalConfigurationUpdatableFieldsEquals(createUpdatedEntity(), persistedGlobalConfiguration);
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.USER)
    void patchGlobalConfigurationForbiddenForNonAdmin() throws Exception {
        // Initialize the database
        globalConfiguration = saveSingleton();

        GlobalConfigurationDTO updatedDTO = new GlobalConfigurationDTO();
        updatedDTO.setId(globalConfiguration.getId());
        updatedDTO.setStudentJustificationDays(UPDATED_STUDENT_JUSTIFICATION_DAYS);
        updatedDTO.setInstructorResponseDays(UPDATED_INSTRUCTOR_RESPONSE_DAYS);

        restGlobalConfigurationMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(updatedDTO)))
            .andExpect(status().isForbidden());
    }

    @Test
    void patchGlobalConfigurationWithNullFieldMergesUnchangedField() throws Exception {
        // Initialize the database
        globalConfiguration = saveSingleton();

        GlobalConfigurationDTO updatedDTO = new GlobalConfigurationDTO();
        updatedDTO.setId(globalConfiguration.getId());
        updatedDTO.setStudentJustificationDays(null); // omitted/null -> left unchanged
        updatedDTO.setInstructorResponseDays(UPDATED_INSTRUCTOR_RESPONSE_DAYS);

        restGlobalConfigurationMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(updatedDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.studentJustificationDays").value(DEFAULT_STUDENT_JUSTIFICATION_DAYS))
            .andExpect(jsonPath("$.instructorResponseDays").value(UPDATED_INSTRUCTOR_RESPONSE_DAYS));

        GlobalConfiguration persistedGlobalConfiguration = globalConfigurationRepository
            .findById(globalConfiguration.getId())
            .orElseThrow();
        assertThat(persistedGlobalConfiguration.getStudentJustificationDays()).isEqualTo(DEFAULT_STUDENT_JUSTIFICATION_DAYS);
        assertThat(persistedGlobalConfiguration.getInstructorResponseDays()).isEqualTo(UPDATED_INSTRUCTOR_RESPONSE_DAYS);
    }

    @Test
    void patchGlobalConfigurationWithZeroValue() throws Exception {
        // Initialize the database
        globalConfiguration = saveSingleton();

        GlobalConfigurationDTO updatedDTO = new GlobalConfigurationDTO();
        updatedDTO.setId(globalConfiguration.getId());
        updatedDTO.setStudentJustificationDays(0);
        updatedDTO.setInstructorResponseDays(UPDATED_INSTRUCTOR_RESPONSE_DAYS);

        restGlobalConfigurationMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(updatedDTO)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void patchGlobalConfigurationWithNegativeValue() throws Exception {
        // Initialize the database
        globalConfiguration = saveSingleton();

        GlobalConfigurationDTO updatedDTO = new GlobalConfigurationDTO();
        updatedDTO.setId(globalConfiguration.getId());
        updatedDTO.setStudentJustificationDays(UPDATED_STUDENT_JUSTIFICATION_DAYS);
        updatedDTO.setInstructorResponseDays(-1);

        restGlobalConfigurationMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(updatedDTO)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void patchNonExistingGlobalConfiguration() throws Exception {
        long databaseSizeBeforeUpdate = globalConfigurationRepository.count();
        globalConfiguration.setId(UUID.randomUUID().toString());

        // Create the GlobalConfiguration
        GlobalConfigurationDTO globalConfigurationDTO = new GlobalConfigurationDTO();
        globalConfigurationDTO.setId(globalConfiguration.getId());
        globalConfigurationDTO.setStudentJustificationDays(UPDATED_STUDENT_JUSTIFICATION_DAYS);
        globalConfigurationDTO.setInstructorResponseDays(UPDATED_INSTRUCTOR_RESPONSE_DAYS);

        // If the entity doesn't exist, it will throw BadRequestAlertException
        restGlobalConfigurationMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(globalConfigurationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the GlobalConfiguration in the database
        assertThat(globalConfigurationRepository.count()).isEqualTo(databaseSizeBeforeUpdate);
    }
}
