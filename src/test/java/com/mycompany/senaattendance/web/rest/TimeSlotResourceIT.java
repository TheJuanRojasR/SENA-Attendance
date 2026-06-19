package com.mycompany.senaattendance.web.rest;

import static com.mycompany.senaattendance.domain.TimeSlotAsserts.*;
import static com.mycompany.senaattendance.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.TimeSlot;
import com.mycompany.senaattendance.repository.TimeSlotRepository;
import com.mycompany.senaattendance.service.dto.TimeSlotDTO;
import com.mycompany.senaattendance.service.mapper.TimeSlotMapper;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
 * Integration tests for the {@link TimeSlotResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TimeSlotResourceIT {

    private static final DateTimeFormatter LOCAL_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Boolean DEFAULT_IS_ACTIVE = false;
    private static final Boolean UPDATED_IS_ACTIVE = true;

    private static final LocalTime DEFAULT_START_TIME = LocalTime.NOON;
    private static final LocalTime UPDATED_START_TIME = LocalTime.MAX.withNano(0);

    private static final LocalTime DEFAULT_END_TIME = LocalTime.NOON;
    private static final LocalTime UPDATED_END_TIME = LocalTime.MAX.withNano(0);

    private static final String ENTITY_API_URL = "/api/time-slots";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private TimeSlotMapper timeSlotMapper;

    @Autowired
    private MockMvc restTimeSlotMockMvc;

    private TimeSlot timeSlot;

    private TimeSlot insertedTimeSlot;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TimeSlot createEntity() {
        return new TimeSlot().name(DEFAULT_NAME).isActive(DEFAULT_IS_ACTIVE).startTime(DEFAULT_START_TIME).endTime(DEFAULT_END_TIME);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TimeSlot createUpdatedEntity() {
        return new TimeSlot().name(UPDATED_NAME).isActive(UPDATED_IS_ACTIVE).startTime(UPDATED_START_TIME).endTime(UPDATED_END_TIME);
    }

    @BeforeEach
    void initTest() {
        timeSlot = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedTimeSlot != null) {
            timeSlotRepository.delete(insertedTimeSlot);
            insertedTimeSlot = null;
        }
    }

    @Test
    void createTimeSlot() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the TimeSlot
        TimeSlotDTO timeSlotDTO = timeSlotMapper.toDto(timeSlot);
        var returnedTimeSlotDTO = om.readValue(
            restTimeSlotMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(timeSlotDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TimeSlotDTO.class
        );

        // Validate the TimeSlot in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedTimeSlot = timeSlotMapper.toEntity(returnedTimeSlotDTO);
        assertTimeSlotUpdatableFieldsEquals(returnedTimeSlot, getPersistedTimeSlot(returnedTimeSlot));

        insertedTimeSlot = returnedTimeSlot;
    }

    @Test
    void createTimeSlotWithExistingId() throws Exception {
        // Create the TimeSlot with an existing ID
        timeSlot.setId("existing_id");
        TimeSlotDTO timeSlotDTO = timeSlotMapper.toDto(timeSlot);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTimeSlotMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(timeSlotDTO)))
            .andExpect(status().isBadRequest());

        // Validate the TimeSlot in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        timeSlot.setName(null);

        // Create the TimeSlot, which fails.
        TimeSlotDTO timeSlotDTO = timeSlotMapper.toDto(timeSlot);

        restTimeSlotMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(timeSlotDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkIsActiveIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        timeSlot.setIsActive(null);

        // Create the TimeSlot, which fails.
        TimeSlotDTO timeSlotDTO = timeSlotMapper.toDto(timeSlot);

        restTimeSlotMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(timeSlotDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkStartTimeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        timeSlot.setStartTime(null);

        // Create the TimeSlot, which fails.
        TimeSlotDTO timeSlotDTO = timeSlotMapper.toDto(timeSlot);

        restTimeSlotMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(timeSlotDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkEndTimeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        timeSlot.setEndTime(null);

        // Create the TimeSlot, which fails.
        TimeSlotDTO timeSlotDTO = timeSlotMapper.toDto(timeSlot);

        restTimeSlotMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(timeSlotDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllTimeSlots() throws Exception {
        // Initialize the database
        insertedTimeSlot = timeSlotRepository.save(timeSlot);

        // Get all the timeSlotList
        restTimeSlotMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(timeSlot.getId())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].isActive").value(hasItem(DEFAULT_IS_ACTIVE)))
            .andExpect(jsonPath("$.[*].startTime").value(hasItem(DEFAULT_START_TIME.format(LOCAL_DATE_TIME_FORMAT))))
            .andExpect(jsonPath("$.[*].endTime").value(hasItem(DEFAULT_END_TIME.format(LOCAL_DATE_TIME_FORMAT))));
    }

    @Test
    void getTimeSlot() throws Exception {
        // Initialize the database
        insertedTimeSlot = timeSlotRepository.save(timeSlot);

        // Get the timeSlot
        restTimeSlotMockMvc
            .perform(get(ENTITY_API_URL_ID, timeSlot.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(timeSlot.getId()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.isActive").value(DEFAULT_IS_ACTIVE))
            .andExpect(jsonPath("$.startTime").value(DEFAULT_START_TIME.format(LOCAL_DATE_TIME_FORMAT)))
            .andExpect(jsonPath("$.endTime").value(DEFAULT_END_TIME.format(LOCAL_DATE_TIME_FORMAT)));
    }

    @Test
    void getNonExistingTimeSlot() throws Exception {
        // Get the timeSlot
        restTimeSlotMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingTimeSlot() throws Exception {
        // Initialize the database
        insertedTimeSlot = timeSlotRepository.save(timeSlot);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the timeSlot
        TimeSlot updatedTimeSlot = timeSlotRepository.findById(timeSlot.getId()).orElseThrow();
        updatedTimeSlot.name(UPDATED_NAME).isActive(UPDATED_IS_ACTIVE).startTime(UPDATED_START_TIME).endTime(UPDATED_END_TIME);
        TimeSlotDTO timeSlotDTO = timeSlotMapper.toDto(updatedTimeSlot);

        restTimeSlotMockMvc
            .perform(
                put(ENTITY_API_URL_ID, timeSlotDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(timeSlotDTO))
            )
            .andExpect(status().isOk());

        // Validate the TimeSlot in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTimeSlotToMatchAllProperties(updatedTimeSlot);
    }

    @Test
    void putNonExistingTimeSlot() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        timeSlot.setId(UUID.randomUUID().toString());

        // Create the TimeSlot
        TimeSlotDTO timeSlotDTO = timeSlotMapper.toDto(timeSlot);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTimeSlotMockMvc
            .perform(
                put(ENTITY_API_URL_ID, timeSlotDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(timeSlotDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TimeSlot in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchTimeSlot() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        timeSlot.setId(UUID.randomUUID().toString());

        // Create the TimeSlot
        TimeSlotDTO timeSlotDTO = timeSlotMapper.toDto(timeSlot);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTimeSlotMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(timeSlotDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TimeSlot in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamTimeSlot() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        timeSlot.setId(UUID.randomUUID().toString());

        // Create the TimeSlot
        TimeSlotDTO timeSlotDTO = timeSlotMapper.toDto(timeSlot);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTimeSlotMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(timeSlotDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TimeSlot in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateTimeSlotWithPatch() throws Exception {
        // Initialize the database
        insertedTimeSlot = timeSlotRepository.save(timeSlot);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the timeSlot using partial update
        TimeSlot partialUpdatedTimeSlot = new TimeSlot();
        partialUpdatedTimeSlot.setId(timeSlot.getId());

        partialUpdatedTimeSlot.name(UPDATED_NAME);

        restTimeSlotMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTimeSlot.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTimeSlot))
            )
            .andExpect(status().isOk());

        // Validate the TimeSlot in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTimeSlotUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedTimeSlot, timeSlot), getPersistedTimeSlot(timeSlot));
    }

    @Test
    void fullUpdateTimeSlotWithPatch() throws Exception {
        // Initialize the database
        insertedTimeSlot = timeSlotRepository.save(timeSlot);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the timeSlot using partial update
        TimeSlot partialUpdatedTimeSlot = new TimeSlot();
        partialUpdatedTimeSlot.setId(timeSlot.getId());

        partialUpdatedTimeSlot.name(UPDATED_NAME).isActive(UPDATED_IS_ACTIVE).startTime(UPDATED_START_TIME).endTime(UPDATED_END_TIME);

        restTimeSlotMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTimeSlot.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTimeSlot))
            )
            .andExpect(status().isOk());

        // Validate the TimeSlot in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTimeSlotUpdatableFieldsEquals(partialUpdatedTimeSlot, getPersistedTimeSlot(partialUpdatedTimeSlot));
    }

    @Test
    void patchNonExistingTimeSlot() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        timeSlot.setId(UUID.randomUUID().toString());

        // Create the TimeSlot
        TimeSlotDTO timeSlotDTO = timeSlotMapper.toDto(timeSlot);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTimeSlotMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, timeSlotDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(timeSlotDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TimeSlot in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchTimeSlot() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        timeSlot.setId(UUID.randomUUID().toString());

        // Create the TimeSlot
        TimeSlotDTO timeSlotDTO = timeSlotMapper.toDto(timeSlot);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTimeSlotMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(timeSlotDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TimeSlot in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamTimeSlot() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        timeSlot.setId(UUID.randomUUID().toString());

        // Create the TimeSlot
        TimeSlotDTO timeSlotDTO = timeSlotMapper.toDto(timeSlot);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTimeSlotMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(timeSlotDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TimeSlot in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteTimeSlot() throws Exception {
        // Initialize the database
        insertedTimeSlot = timeSlotRepository.save(timeSlot);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the timeSlot
        restTimeSlotMockMvc
            .perform(delete(ENTITY_API_URL_ID, timeSlot.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return timeSlotRepository.count();
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

    protected TimeSlot getPersistedTimeSlot(TimeSlot timeSlot) {
        return timeSlotRepository.findById(timeSlot.getId()).orElseThrow();
    }

    protected void assertPersistedTimeSlotToMatchAllProperties(TimeSlot expectedTimeSlot) {
        assertTimeSlotAllPropertiesEquals(expectedTimeSlot, getPersistedTimeSlot(expectedTimeSlot));
    }

    protected void assertPersistedTimeSlotToMatchUpdatableProperties(TimeSlot expectedTimeSlot) {
        assertTimeSlotAllUpdatablePropertiesEquals(expectedTimeSlot, getPersistedTimeSlot(expectedTimeSlot));
    }
}
