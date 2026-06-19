package com.mycompany.senaattendance.web.rest;

import static com.mycompany.senaattendance.domain.ClassScheduleAsserts.*;
import static com.mycompany.senaattendance.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.ClassSchedule;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.domain.enumeration.DayOfWeek;
import com.mycompany.senaattendance.repository.ClassScheduleRepository;
import com.mycompany.senaattendance.service.ClassScheduleService;
import com.mycompany.senaattendance.service.dto.ClassScheduleDTO;
import com.mycompany.senaattendance.service.mapper.ClassScheduleMapper;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
 * Integration tests for the {@link ClassScheduleResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class ClassScheduleResourceIT {

    private static final DateTimeFormatter LOCAL_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final DayOfWeek DEFAULT_DAY_OF_WEEK = DayOfWeek.LUNES;
    private static final DayOfWeek UPDATED_DAY_OF_WEEK = DayOfWeek.MARTES;

    private static final LocalTime DEFAULT_START_TIME = LocalTime.NOON;
    private static final LocalTime UPDATED_START_TIME = LocalTime.MAX.withNano(0);

    private static final LocalTime DEFAULT_END_TIME = LocalTime.NOON;
    private static final LocalTime UPDATED_END_TIME = LocalTime.MAX.withNano(0);

    private static final String ENTITY_API_URL = "/api/class-schedules";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ClassScheduleRepository classScheduleRepository;

    @Mock
    private ClassScheduleRepository classScheduleRepositoryMock;

    @Autowired
    private ClassScheduleMapper classScheduleMapper;

    @Mock
    private ClassScheduleService classScheduleServiceMock;

    @Autowired
    private MockMvc restClassScheduleMockMvc;

    private ClassSchedule classSchedule;

    private ClassSchedule insertedClassSchedule;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ClassSchedule createEntity() {
        ClassSchedule classSchedule = new ClassSchedule()
            .dayOfWeek(DEFAULT_DAY_OF_WEEK)
            .startTime(DEFAULT_START_TIME)
            .endTime(DEFAULT_END_TIME);
        // Add required entity
        Trimester trimester;
        trimester = TrimesterResourceIT.createEntity();
        trimester.setId("fixed-id-for-tests");
        classSchedule.setTrimester(trimester);
        // Add required entity
        ClassSection classSection;
        classSection = ClassSectionResourceIT.createEntity();
        classSection.setId("fixed-id-for-tests");
        classSchedule.setClassSection(classSection);
        return classSchedule;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ClassSchedule createUpdatedEntity() {
        ClassSchedule updatedClassSchedule = new ClassSchedule()
            .dayOfWeek(UPDATED_DAY_OF_WEEK)
            .startTime(UPDATED_START_TIME)
            .endTime(UPDATED_END_TIME);
        // Add required entity
        Trimester trimester;
        trimester = TrimesterResourceIT.createUpdatedEntity();
        trimester.setId("fixed-id-for-tests");
        updatedClassSchedule.setTrimester(trimester);
        // Add required entity
        ClassSection classSection;
        classSection = ClassSectionResourceIT.createUpdatedEntity();
        classSection.setId("fixed-id-for-tests");
        updatedClassSchedule.setClassSection(classSection);
        return updatedClassSchedule;
    }

    @BeforeEach
    void initTest() {
        classSchedule = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedClassSchedule != null) {
            classScheduleRepository.delete(insertedClassSchedule);
            insertedClassSchedule = null;
        }
    }

    @Test
    void createClassSchedule() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ClassSchedule
        ClassScheduleDTO classScheduleDTO = classScheduleMapper.toDto(classSchedule);
        var returnedClassScheduleDTO = om.readValue(
            restClassScheduleMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classScheduleDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ClassScheduleDTO.class
        );

        // Validate the ClassSchedule in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedClassSchedule = classScheduleMapper.toEntity(returnedClassScheduleDTO);
        assertClassScheduleUpdatableFieldsEquals(returnedClassSchedule, getPersistedClassSchedule(returnedClassSchedule));

        insertedClassSchedule = returnedClassSchedule;
    }

    @Test
    void createClassScheduleWithExistingId() throws Exception {
        // Create the ClassSchedule with an existing ID
        classSchedule.setId("existing_id");
        ClassScheduleDTO classScheduleDTO = classScheduleMapper.toDto(classSchedule);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restClassScheduleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classScheduleDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ClassSchedule in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkStartTimeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        classSchedule.setStartTime(null);

        // Create the ClassSchedule, which fails.
        ClassScheduleDTO classScheduleDTO = classScheduleMapper.toDto(classSchedule);

        restClassScheduleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classScheduleDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkEndTimeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        classSchedule.setEndTime(null);

        // Create the ClassSchedule, which fails.
        ClassScheduleDTO classScheduleDTO = classScheduleMapper.toDto(classSchedule);

        restClassScheduleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classScheduleDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllClassSchedules() throws Exception {
        // Initialize the database
        insertedClassSchedule = classScheduleRepository.save(classSchedule);

        // Get all the classScheduleList
        restClassScheduleMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(classSchedule.getId())))
            .andExpect(jsonPath("$.[*].dayOfWeek").value(hasItem(DEFAULT_DAY_OF_WEEK.toString())))
            .andExpect(jsonPath("$.[*].startTime").value(hasItem(DEFAULT_START_TIME.format(LOCAL_DATE_TIME_FORMAT))))
            .andExpect(jsonPath("$.[*].endTime").value(hasItem(DEFAULT_END_TIME.format(LOCAL_DATE_TIME_FORMAT))));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllClassSchedulesWithEagerRelationshipsIsEnabled() throws Exception {
        when(classScheduleServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restClassScheduleMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(classScheduleServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllClassSchedulesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(classScheduleServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restClassScheduleMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(classScheduleRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void getClassSchedule() throws Exception {
        // Initialize the database
        insertedClassSchedule = classScheduleRepository.save(classSchedule);

        // Get the classSchedule
        restClassScheduleMockMvc
            .perform(get(ENTITY_API_URL_ID, classSchedule.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(classSchedule.getId()))
            .andExpect(jsonPath("$.dayOfWeek").value(DEFAULT_DAY_OF_WEEK.toString()))
            .andExpect(jsonPath("$.startTime").value(DEFAULT_START_TIME.format(LOCAL_DATE_TIME_FORMAT)))
            .andExpect(jsonPath("$.endTime").value(DEFAULT_END_TIME.format(LOCAL_DATE_TIME_FORMAT)));
    }

    @Test
    void getNonExistingClassSchedule() throws Exception {
        // Get the classSchedule
        restClassScheduleMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingClassSchedule() throws Exception {
        // Initialize the database
        insertedClassSchedule = classScheduleRepository.save(classSchedule);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the classSchedule
        ClassSchedule updatedClassSchedule = classScheduleRepository.findById(classSchedule.getId()).orElseThrow();
        updatedClassSchedule.dayOfWeek(UPDATED_DAY_OF_WEEK).startTime(UPDATED_START_TIME).endTime(UPDATED_END_TIME);
        ClassScheduleDTO classScheduleDTO = classScheduleMapper.toDto(updatedClassSchedule);

        restClassScheduleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, classScheduleDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(classScheduleDTO))
            )
            .andExpect(status().isOk());

        // Validate the ClassSchedule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedClassScheduleToMatchAllProperties(updatedClassSchedule);
    }

    @Test
    void putNonExistingClassSchedule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classSchedule.setId(UUID.randomUUID().toString());

        // Create the ClassSchedule
        ClassScheduleDTO classScheduleDTO = classScheduleMapper.toDto(classSchedule);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restClassScheduleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, classScheduleDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(classScheduleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ClassSchedule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchClassSchedule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classSchedule.setId(UUID.randomUUID().toString());

        // Create the ClassSchedule
        ClassScheduleDTO classScheduleDTO = classScheduleMapper.toDto(classSchedule);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restClassScheduleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(classScheduleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ClassSchedule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamClassSchedule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classSchedule.setId(UUID.randomUUID().toString());

        // Create the ClassSchedule
        ClassScheduleDTO classScheduleDTO = classScheduleMapper.toDto(classSchedule);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restClassScheduleMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classScheduleDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ClassSchedule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateClassScheduleWithPatch() throws Exception {
        // Initialize the database
        insertedClassSchedule = classScheduleRepository.save(classSchedule);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the classSchedule using partial update
        ClassSchedule partialUpdatedClassSchedule = new ClassSchedule();
        partialUpdatedClassSchedule.setId(classSchedule.getId());

        partialUpdatedClassSchedule.dayOfWeek(UPDATED_DAY_OF_WEEK);

        restClassScheduleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedClassSchedule.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedClassSchedule))
            )
            .andExpect(status().isOk());

        // Validate the ClassSchedule in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertClassScheduleUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedClassSchedule, classSchedule),
            getPersistedClassSchedule(classSchedule)
        );
    }

    @Test
    void fullUpdateClassScheduleWithPatch() throws Exception {
        // Initialize the database
        insertedClassSchedule = classScheduleRepository.save(classSchedule);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the classSchedule using partial update
        ClassSchedule partialUpdatedClassSchedule = new ClassSchedule();
        partialUpdatedClassSchedule.setId(classSchedule.getId());

        partialUpdatedClassSchedule.dayOfWeek(UPDATED_DAY_OF_WEEK).startTime(UPDATED_START_TIME).endTime(UPDATED_END_TIME);

        restClassScheduleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedClassSchedule.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedClassSchedule))
            )
            .andExpect(status().isOk());

        // Validate the ClassSchedule in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertClassScheduleUpdatableFieldsEquals(partialUpdatedClassSchedule, getPersistedClassSchedule(partialUpdatedClassSchedule));
    }

    @Test
    void patchNonExistingClassSchedule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classSchedule.setId(UUID.randomUUID().toString());

        // Create the ClassSchedule
        ClassScheduleDTO classScheduleDTO = classScheduleMapper.toDto(classSchedule);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restClassScheduleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, classScheduleDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(classScheduleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ClassSchedule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchClassSchedule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classSchedule.setId(UUID.randomUUID().toString());

        // Create the ClassSchedule
        ClassScheduleDTO classScheduleDTO = classScheduleMapper.toDto(classSchedule);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restClassScheduleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(classScheduleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ClassSchedule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamClassSchedule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classSchedule.setId(UUID.randomUUID().toString());

        // Create the ClassSchedule
        ClassScheduleDTO classScheduleDTO = classScheduleMapper.toDto(classSchedule);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restClassScheduleMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(classScheduleDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ClassSchedule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteClassSchedule() throws Exception {
        // Initialize the database
        insertedClassSchedule = classScheduleRepository.save(classSchedule);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the classSchedule
        restClassScheduleMockMvc
            .perform(delete(ENTITY_API_URL_ID, classSchedule.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return classScheduleRepository.count();
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

    protected ClassSchedule getPersistedClassSchedule(ClassSchedule classSchedule) {
        return classScheduleRepository.findById(classSchedule.getId()).orElseThrow();
    }

    protected void assertPersistedClassScheduleToMatchAllProperties(ClassSchedule expectedClassSchedule) {
        assertClassScheduleAllPropertiesEquals(expectedClassSchedule, getPersistedClassSchedule(expectedClassSchedule));
    }

    protected void assertPersistedClassScheduleToMatchUpdatableProperties(ClassSchedule expectedClassSchedule) {
        assertClassScheduleAllUpdatablePropertiesEquals(expectedClassSchedule, getPersistedClassSchedule(expectedClassSchedule));
    }
}
