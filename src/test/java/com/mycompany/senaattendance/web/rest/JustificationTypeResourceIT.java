package com.mycompany.senaattendance.web.rest;

import static com.mycompany.senaattendance.domain.JustificationTypeAsserts.*;
import static com.mycompany.senaattendance.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.Justification;
import com.mycompany.senaattendance.domain.JustificationType;
import com.mycompany.senaattendance.domain.enumeration.Status;
import com.mycompany.senaattendance.repository.JustificationRepository;
import com.mycompany.senaattendance.repository.JustificationTypeRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
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
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class JustificationTypeResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Integer DEFAULT_LIMIT_PER_TRIMESTER = 1;
    private static final Integer UPDATED_LIMIT_PER_TRIMESTER = 2;

    private static final Status DEFAULT_STATUS = Status.ACTIVO;
    private static final Status UPDATED_STATUS = Status.INACTIVO;

    private static final String ENTITY_API_URL = "/api/justification-types";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private JustificationTypeRepository justificationTypeRepository;

    @Autowired
    private JustificationRepository justificationRepository;

    @Autowired
    private JustificationTypeMapper justificationTypeMapper;

    @Autowired
    private MockMvc restJustificationTypeMockMvc;

    private JustificationType justificationType;

    private JustificationType insertedJustificationType;

    private Justification insertedJustification;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static JustificationType createEntity() {
        return new JustificationType().name(DEFAULT_NAME).limitPerTrimester(DEFAULT_LIMIT_PER_TRIMESTER).status(DEFAULT_STATUS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static JustificationType createUpdatedEntity() {
        return new JustificationType().name(UPDATED_NAME).limitPerTrimester(UPDATED_LIMIT_PER_TRIMESTER).status(UPDATED_STATUS);
    }

    @BeforeEach
    void initTest() {
        justificationType = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedJustification != null) {
            justificationRepository.delete(insertedJustification);
            insertedJustification = null;
        }
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
    void createJustificationTypeWithDuplicateNameReturnsBadRequest() throws Exception {
        // Persist a justification type with DEFAULT_NAME so the upcoming POST collides on name only
        insertedJustificationType = justificationTypeRepository.save(justificationType);
        long databaseSizeBeforeCreate = getRepositoryCount();

        JustificationTypeDTO justificationTypeDTO = justificationTypeMapper.toDto(justificationType);
        justificationTypeDTO.setId(null);
        justificationTypeDTO.setName("aaaaaaaaaa");
        justificationTypeDTO.setLimitPerTrimester(UPDATED_LIMIT_PER_TRIMESTER);

        restJustificationTypeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationTypeDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.justificationTypeNameAlreadyUsed"));

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void createJustificationTypeWithBlankNameReturnsBadRequest() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        justificationType.setName("   ");

        JustificationTypeDTO justificationTypeDTO = justificationTypeMapper.toDto(justificationType);

        restJustificationTypeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationTypeDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.validation"))
            .andExpect(jsonPath("$.fieldErrors").isArray())
            .andExpect(jsonPath("$.fieldErrors[0].field").value("name"));

        assertSameRepositoryCount(databaseSizeBeforeTest);
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
    void checkLimitPerTrimesterIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        justificationType.setLimitPerTrimester(null);

        JustificationTypeDTO justificationTypeDTO = justificationTypeMapper.toDto(justificationType);

        restJustificationTypeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationTypeDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.validation"))
            .andExpect(jsonPath("$.fieldErrors").isArray())
            .andExpect(jsonPath("$.fieldErrors[0].field").value("limitPerTrimester"));

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void createJustificationTypeWithZeroLimitReturnsBadRequest() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        justificationType.setLimitPerTrimester(0);

        JustificationTypeDTO justificationTypeDTO = justificationTypeMapper.toDto(justificationType);

        restJustificationTypeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationTypeDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.validation"))
            .andExpect(jsonPath("$.fieldErrors[0].field").value("limitPerTrimester"));

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void createJustificationTypeWithNegativeLimitReturnsBadRequest() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        justificationType.setLimitPerTrimester(-1);

        JustificationTypeDTO justificationTypeDTO = justificationTypeMapper.toDto(justificationType);

        restJustificationTypeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationTypeDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.validation"))
            .andExpect(jsonPath("$.fieldErrors[0].field").value("limitPerTrimester"));

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void createJustificationTypeWithoutStatusIsPersistedActive() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // set the field null so the backend applies its default of active
        justificationType.setStatus(null);

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

        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertThat(returnedJustificationTypeDTO.getStatus()).isEqualTo(Status.ACTIVO);

        var returnedJustificationType = justificationTypeMapper.toEntity(returnedJustificationTypeDTO);
        assertThat(getPersistedJustificationType(returnedJustificationType).getStatus()).isEqualTo(Status.ACTIVO);

        insertedJustificationType = returnedJustificationType;
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
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }

    @Test
    void getActiveJustificationTypes() throws Exception {
        insertedJustificationType = justificationTypeRepository.save(justificationType);
        JustificationType inactiveJustificationType = justificationTypeRepository.save(createUpdatedEntity());

        try {
            restJustificationTypeMockMvc
                .perform(get(ENTITY_API_URL + "/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(insertedJustificationType.getId())))
                .andExpect(jsonPath("$.[*].id").value(not(hasItem(inactiveJustificationType.getId()))));
        } finally {
            justificationTypeRepository.delete(inactiveJustificationType);
        }
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
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
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
        updatedJustificationType.name(UPDATED_NAME).limitPerTrimester(UPDATED_LIMIT_PER_TRIMESTER).status(UPDATED_STATUS);
        JustificationTypeDTO justificationTypeDTO = justificationTypeMapper.toDto(updatedJustificationType);

        restJustificationTypeMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationTypeDTO)))
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
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationTypeDTO)))
            .andExpect(status().isBadRequest());

        // Validate the JustificationType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putJustificationTypeWithoutIdReturnsBadRequest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        justificationType.setId(null);

        JustificationTypeDTO justificationTypeDTO = justificationTypeMapper.toDto(justificationType);

        restJustificationTypeMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationTypeDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.idnull"));

        // Validate the JustificationType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putJustificationTypeWithDuplicateNameReturnsBadRequest() throws Exception {
        insertedJustificationType = justificationTypeRepository.save(justificationType);

        JustificationType other = justificationTypeRepository.save(createUpdatedEntity());

        try {
            long databaseSizeBeforeUpdate = getRepositoryCount();
            JustificationTypeDTO justificationTypeDTO = justificationTypeMapper.toDto(other);
            justificationTypeDTO.setName(DEFAULT_NAME);

            restJustificationTypeMockMvc
                .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationTypeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error.justificationTypeNameAlreadyUsed"));

            assertSameRepositoryCount(databaseSizeBeforeUpdate);
            assertThat(getPersistedJustificationType(other).getName()).isEqualTo(UPDATED_NAME);
        } finally {
            justificationTypeRepository.delete(other);
        }
    }

    @Test
    void putJustificationTypeKeepingOwnNameSucceeds() throws Exception {
        insertedJustificationType = justificationTypeRepository.save(justificationType);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        JustificationTypeDTO justificationTypeDTO = justificationTypeMapper.toDto(insertedJustificationType);

        restJustificationTypeMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationTypeDTO)))
            .andExpect(status().isOk());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertThat(getPersistedJustificationType(insertedJustificationType).getName()).isEqualTo(DEFAULT_NAME);
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
                patch(ENTITY_API_URL)
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

        partialUpdatedJustificationType.name(UPDATED_NAME).limitPerTrimester(UPDATED_LIMIT_PER_TRIMESTER).status(UPDATED_STATUS);

        restJustificationTypeMockMvc
            .perform(
                patch(ENTITY_API_URL)
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
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(justificationTypeDTO)))
            .andExpect(status().isBadRequest());

        // Validate the JustificationType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchJustificationTypeWithoutIdReturnsBadRequest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        justificationType.setId(null);

        JustificationTypeDTO justificationTypeDTO = justificationTypeMapper.toDto(justificationType);

        restJustificationTypeMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(justificationTypeDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.idnull"));

        // Validate the JustificationType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchJustificationTypeWithDuplicateNameReturnsBadRequest() throws Exception {
        insertedJustificationType = justificationTypeRepository.save(justificationType);

        JustificationType other = justificationTypeRepository.save(createUpdatedEntity());

        try {
            long databaseSizeBeforeUpdate = getRepositoryCount();

            JustificationType partialUpdatedJustificationType = new JustificationType();
            partialUpdatedJustificationType.setId(other.getId());
            partialUpdatedJustificationType.setName(DEFAULT_NAME);

            restJustificationTypeMockMvc
                .perform(
                    patch(ENTITY_API_URL)
                        .contentType("application/merge-patch+json")
                        .content(om.writeValueAsBytes(partialUpdatedJustificationType))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error.justificationTypeNameAlreadyUsed"));

            assertSameRepositoryCount(databaseSizeBeforeUpdate);
            assertThat(getPersistedJustificationType(other).getName()).isEqualTo(UPDATED_NAME);
        } finally {
            justificationTypeRepository.delete(other);
        }
    }

    @Test
    void deleteJustificationTypeInUseReturnsBadRequest() throws Exception {
        insertedJustificationType = justificationTypeRepository.save(justificationType);

        Justification justification = JustificationResourceIT.createEntity();
        justification.setJustificationType(insertedJustificationType);
        insertedJustification = justificationRepository.save(justification);

        long databaseSizeBeforeDelete = getRepositoryCount();

        restJustificationTypeMockMvc
            .perform(delete(ENTITY_API_URL_ID, insertedJustificationType.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.justificationTypeInUse"));

        assertSameRepositoryCount(databaseSizeBeforeDelete);
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

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.COORDINATOR)
    void createJustificationTypeAsNonAdminReturnsForbidden() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        JustificationTypeDTO justificationTypeDTO = justificationTypeMapper.toDto(justificationType);

        restJustificationTypeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationTypeDTO)))
            .andExpect(status().isForbidden());

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.COORDINATOR)
    void updateJustificationTypeAsNonAdminReturnsForbidden() throws Exception {
        insertedJustificationType = justificationTypeRepository.save(justificationType);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        JustificationTypeDTO justificationTypeDTO = justificationTypeMapper.toDto(insertedJustificationType);

        restJustificationTypeMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(justificationTypeDTO)))
            .andExpect(status().isForbidden());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.COORDINATOR)
    void partialUpdateJustificationTypeAsNonAdminReturnsForbidden() throws Exception {
        insertedJustificationType = justificationTypeRepository.save(justificationType);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        JustificationTypeDTO justificationTypeDTO = justificationTypeMapper.toDto(insertedJustificationType);

        restJustificationTypeMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(justificationTypeDTO)))
            .andExpect(status().isForbidden());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.COORDINATOR)
    void deleteJustificationTypeAsNonAdminReturnsForbidden() throws Exception {
        insertedJustificationType = justificationTypeRepository.save(justificationType);

        long databaseSizeBeforeDelete = getRepositoryCount();

        restJustificationTypeMockMvc
            .perform(delete(ENTITY_API_URL_ID, insertedJustificationType.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());

        assertSameRepositoryCount(databaseSizeBeforeDelete);
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.INSTRUCTOR)
    void readJustificationTypesAsAuthenticatedNonAdminReturnsOk() throws Exception {
        insertedJustificationType = justificationTypeRepository.save(justificationType);

        restJustificationTypeMockMvc.perform(get(ENTITY_API_URL)).andExpect(status().isOk());
        restJustificationTypeMockMvc.perform(get(ENTITY_API_URL_ID, insertedJustificationType.getId())).andExpect(status().isOk());
        restJustificationTypeMockMvc.perform(get(ENTITY_API_URL + "/active")).andExpect(status().isOk());
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
