package com.mycompany.senaattendance.web.rest;

import static com.mycompany.senaattendance.domain.DesertionCounterAsserts.*;
import static com.mycompany.senaattendance.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.DesertionCounter;
import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.repository.DesertionCounterRepository;
import com.mycompany.senaattendance.service.DesertionCounterService;
import com.mycompany.senaattendance.service.dto.DesertionCounterDTO;
import com.mycompany.senaattendance.service.mapper.DesertionCounterMapper;
import java.util.ArrayList;
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
 * Integration tests for the {@link DesertionCounterResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class DesertionCounterResourceIT {

    private static final Integer DEFAULT_TOTAL_GLOBAL_ABSENCES = 1;
    private static final Integer UPDATED_TOTAL_GLOBAL_ABSENCES = 2;

    private static final Integer DEFAULT_ACCUMULATED_LATE_ARRIVALS = 1;
    private static final Integer UPDATED_ACCUMULATED_LATE_ARRIVALS = 2;

    private static final Integer DEFAULT_WORK_JUSTIFICATIONS_USED = 1;
    private static final Integer UPDATED_WORK_JUSTIFICATIONS_USED = 2;

    private static final Boolean DEFAULT_ALERTS_SENT = false;
    private static final Boolean UPDATED_ALERTS_SENT = true;

    private static final String ENTITY_API_URL = "/api/desertion-counters";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private DesertionCounterRepository desertionCounterRepository;

    @Mock
    private DesertionCounterRepository desertionCounterRepositoryMock;

    @Autowired
    private DesertionCounterMapper desertionCounterMapper;

    @Mock
    private DesertionCounterService desertionCounterServiceMock;

    @Autowired
    private MockMvc restDesertionCounterMockMvc;

    private DesertionCounter desertionCounter;

    private DesertionCounter insertedDesertionCounter;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DesertionCounter createEntity() {
        DesertionCounter desertionCounter = new DesertionCounter()
            .totalGlobalAbsences(DEFAULT_TOTAL_GLOBAL_ABSENCES)
            .accumulatedLateArrivals(DEFAULT_ACCUMULATED_LATE_ARRIVALS)
            .workJustificationsUsed(DEFAULT_WORK_JUSTIFICATIONS_USED)
            .alertsSent(DEFAULT_ALERTS_SENT);
        // Add required entity
        UserProfile userProfile;
        userProfile = UserProfileResourceIT.createEntity();
        userProfile.setId("fixed-id-for-tests");
        desertionCounter.setStudent(userProfile);
        // Add required entity
        Trimester trimester;
        trimester = TrimesterResourceIT.createEntity();
        trimester.setId("fixed-id-for-tests");
        desertionCounter.setTrimester(trimester);
        return desertionCounter;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DesertionCounter createUpdatedEntity() {
        DesertionCounter updatedDesertionCounter = new DesertionCounter()
            .totalGlobalAbsences(UPDATED_TOTAL_GLOBAL_ABSENCES)
            .accumulatedLateArrivals(UPDATED_ACCUMULATED_LATE_ARRIVALS)
            .workJustificationsUsed(UPDATED_WORK_JUSTIFICATIONS_USED)
            .alertsSent(UPDATED_ALERTS_SENT);
        // Add required entity
        UserProfile userProfile;
        userProfile = UserProfileResourceIT.createUpdatedEntity();
        userProfile.setId("fixed-id-for-tests");
        updatedDesertionCounter.setStudent(userProfile);
        // Add required entity
        Trimester trimester;
        trimester = TrimesterResourceIT.createUpdatedEntity();
        trimester.setId("fixed-id-for-tests");
        updatedDesertionCounter.setTrimester(trimester);
        return updatedDesertionCounter;
    }

    @BeforeEach
    void initTest() {
        desertionCounter = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedDesertionCounter != null) {
            desertionCounterRepository.delete(insertedDesertionCounter);
            insertedDesertionCounter = null;
        }
    }

    @Test
    void createDesertionCounter() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the DesertionCounter
        DesertionCounterDTO desertionCounterDTO = desertionCounterMapper.toDto(desertionCounter);
        var returnedDesertionCounterDTO = om.readValue(
            restDesertionCounterMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(desertionCounterDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            DesertionCounterDTO.class
        );

        // Validate the DesertionCounter in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedDesertionCounter = desertionCounterMapper.toEntity(returnedDesertionCounterDTO);
        assertDesertionCounterUpdatableFieldsEquals(returnedDesertionCounter, getPersistedDesertionCounter(returnedDesertionCounter));

        insertedDesertionCounter = returnedDesertionCounter;
    }

    @Test
    void createDesertionCounterWithExistingId() throws Exception {
        // Create the DesertionCounter with an existing ID
        desertionCounter.setId("existing_id");
        DesertionCounterDTO desertionCounterDTO = desertionCounterMapper.toDto(desertionCounter);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDesertionCounterMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(desertionCounterDTO)))
            .andExpect(status().isBadRequest());

        // Validate the DesertionCounter in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void getAllDesertionCounters() throws Exception {
        // Initialize the database
        insertedDesertionCounter = desertionCounterRepository.save(desertionCounter);

        // Get all the desertionCounterList
        restDesertionCounterMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(desertionCounter.getId())))
            .andExpect(jsonPath("$.[*].totalGlobalAbsences").value(hasItem(DEFAULT_TOTAL_GLOBAL_ABSENCES)))
            .andExpect(jsonPath("$.[*].accumulatedLateArrivals").value(hasItem(DEFAULT_ACCUMULATED_LATE_ARRIVALS)))
            .andExpect(jsonPath("$.[*].workJustificationsUsed").value(hasItem(DEFAULT_WORK_JUSTIFICATIONS_USED)))
            .andExpect(jsonPath("$.[*].alertsSent").value(hasItem(DEFAULT_ALERTS_SENT)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllDesertionCountersWithEagerRelationshipsIsEnabled() throws Exception {
        when(desertionCounterServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restDesertionCounterMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(desertionCounterServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllDesertionCountersWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(desertionCounterServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restDesertionCounterMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(desertionCounterRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void getDesertionCounter() throws Exception {
        // Initialize the database
        insertedDesertionCounter = desertionCounterRepository.save(desertionCounter);

        // Get the desertionCounter
        restDesertionCounterMockMvc
            .perform(get(ENTITY_API_URL_ID, desertionCounter.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(desertionCounter.getId()))
            .andExpect(jsonPath("$.totalGlobalAbsences").value(DEFAULT_TOTAL_GLOBAL_ABSENCES))
            .andExpect(jsonPath("$.accumulatedLateArrivals").value(DEFAULT_ACCUMULATED_LATE_ARRIVALS))
            .andExpect(jsonPath("$.workJustificationsUsed").value(DEFAULT_WORK_JUSTIFICATIONS_USED))
            .andExpect(jsonPath("$.alertsSent").value(DEFAULT_ALERTS_SENT));
    }

    @Test
    void getNonExistingDesertionCounter() throws Exception {
        // Get the desertionCounter
        restDesertionCounterMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingDesertionCounter() throws Exception {
        // Initialize the database
        insertedDesertionCounter = desertionCounterRepository.save(desertionCounter);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the desertionCounter
        DesertionCounter updatedDesertionCounter = desertionCounterRepository.findById(desertionCounter.getId()).orElseThrow();
        updatedDesertionCounter
            .totalGlobalAbsences(UPDATED_TOTAL_GLOBAL_ABSENCES)
            .accumulatedLateArrivals(UPDATED_ACCUMULATED_LATE_ARRIVALS)
            .workJustificationsUsed(UPDATED_WORK_JUSTIFICATIONS_USED)
            .alertsSent(UPDATED_ALERTS_SENT);
        DesertionCounterDTO desertionCounterDTO = desertionCounterMapper.toDto(updatedDesertionCounter);

        restDesertionCounterMockMvc
            .perform(
                put(ENTITY_API_URL_ID, desertionCounterDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(desertionCounterDTO))
            )
            .andExpect(status().isOk());

        // Validate the DesertionCounter in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedDesertionCounterToMatchAllProperties(updatedDesertionCounter);
    }

    @Test
    void putNonExistingDesertionCounter() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        desertionCounter.setId(UUID.randomUUID().toString());

        // Create the DesertionCounter
        DesertionCounterDTO desertionCounterDTO = desertionCounterMapper.toDto(desertionCounter);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDesertionCounterMockMvc
            .perform(
                put(ENTITY_API_URL_ID, desertionCounterDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(desertionCounterDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DesertionCounter in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchDesertionCounter() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        desertionCounter.setId(UUID.randomUUID().toString());

        // Create the DesertionCounter
        DesertionCounterDTO desertionCounterDTO = desertionCounterMapper.toDto(desertionCounter);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDesertionCounterMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(desertionCounterDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DesertionCounter in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamDesertionCounter() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        desertionCounter.setId(UUID.randomUUID().toString());

        // Create the DesertionCounter
        DesertionCounterDTO desertionCounterDTO = desertionCounterMapper.toDto(desertionCounter);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDesertionCounterMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(desertionCounterDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the DesertionCounter in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateDesertionCounterWithPatch() throws Exception {
        // Initialize the database
        insertedDesertionCounter = desertionCounterRepository.save(desertionCounter);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the desertionCounter using partial update
        DesertionCounter partialUpdatedDesertionCounter = new DesertionCounter();
        partialUpdatedDesertionCounter.setId(desertionCounter.getId());

        restDesertionCounterMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDesertionCounter.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDesertionCounter))
            )
            .andExpect(status().isOk());

        // Validate the DesertionCounter in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDesertionCounterUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedDesertionCounter, desertionCounter),
            getPersistedDesertionCounter(desertionCounter)
        );
    }

    @Test
    void fullUpdateDesertionCounterWithPatch() throws Exception {
        // Initialize the database
        insertedDesertionCounter = desertionCounterRepository.save(desertionCounter);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the desertionCounter using partial update
        DesertionCounter partialUpdatedDesertionCounter = new DesertionCounter();
        partialUpdatedDesertionCounter.setId(desertionCounter.getId());

        partialUpdatedDesertionCounter
            .totalGlobalAbsences(UPDATED_TOTAL_GLOBAL_ABSENCES)
            .accumulatedLateArrivals(UPDATED_ACCUMULATED_LATE_ARRIVALS)
            .workJustificationsUsed(UPDATED_WORK_JUSTIFICATIONS_USED)
            .alertsSent(UPDATED_ALERTS_SENT);

        restDesertionCounterMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDesertionCounter.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDesertionCounter))
            )
            .andExpect(status().isOk());

        // Validate the DesertionCounter in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDesertionCounterUpdatableFieldsEquals(
            partialUpdatedDesertionCounter,
            getPersistedDesertionCounter(partialUpdatedDesertionCounter)
        );
    }

    @Test
    void patchNonExistingDesertionCounter() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        desertionCounter.setId(UUID.randomUUID().toString());

        // Create the DesertionCounter
        DesertionCounterDTO desertionCounterDTO = desertionCounterMapper.toDto(desertionCounter);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDesertionCounterMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, desertionCounterDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(desertionCounterDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DesertionCounter in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchDesertionCounter() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        desertionCounter.setId(UUID.randomUUID().toString());

        // Create the DesertionCounter
        DesertionCounterDTO desertionCounterDTO = desertionCounterMapper.toDto(desertionCounter);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDesertionCounterMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(desertionCounterDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DesertionCounter in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamDesertionCounter() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        desertionCounter.setId(UUID.randomUUID().toString());

        // Create the DesertionCounter
        DesertionCounterDTO desertionCounterDTO = desertionCounterMapper.toDto(desertionCounter);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDesertionCounterMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(desertionCounterDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the DesertionCounter in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteDesertionCounter() throws Exception {
        // Initialize the database
        insertedDesertionCounter = desertionCounterRepository.save(desertionCounter);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the desertionCounter
        restDesertionCounterMockMvc
            .perform(delete(ENTITY_API_URL_ID, desertionCounter.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return desertionCounterRepository.count();
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

    protected DesertionCounter getPersistedDesertionCounter(DesertionCounter desertionCounter) {
        return desertionCounterRepository.findById(desertionCounter.getId()).orElseThrow();
    }

    protected void assertPersistedDesertionCounterToMatchAllProperties(DesertionCounter expectedDesertionCounter) {
        assertDesertionCounterAllPropertiesEquals(expectedDesertionCounter, getPersistedDesertionCounter(expectedDesertionCounter));
    }

    protected void assertPersistedDesertionCounterToMatchUpdatableProperties(DesertionCounter expectedDesertionCounter) {
        assertDesertionCounterAllUpdatablePropertiesEquals(
            expectedDesertionCounter,
            getPersistedDesertionCounter(expectedDesertionCounter)
        );
    }
}
