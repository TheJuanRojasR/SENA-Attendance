package com.mycompany.senaattendance.web.rest;

import static com.mycompany.senaattendance.domain.AttendanceAsserts.*;
import static com.mycompany.senaattendance.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.Attendance;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.StateAttendance;
import com.mycompany.senaattendance.repository.AttendanceRepository;
import com.mycompany.senaattendance.service.AttendanceService;
import com.mycompany.senaattendance.service.dto.AttendanceDTO;
import com.mycompany.senaattendance.service.mapper.AttendanceMapper;
import java.time.LocalDate;
import java.time.ZoneId;
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
 * Integration tests for the {@link AttendanceResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class AttendanceResourceIT {

    private static final LocalDate DEFAULT_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final StateAttendance DEFAULT_STATE_ATTENDANCE = StateAttendance.PRESENTE;
    private static final StateAttendance UPDATED_STATE_ATTENDANCE = StateAttendance.FALLA;

    private static final String ENTITY_API_URL = "/api/attendances";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Mock
    private AttendanceRepository attendanceRepositoryMock;

    @Autowired
    private AttendanceMapper attendanceMapper;

    @Mock
    private AttendanceService attendanceServiceMock;

    @Autowired
    private MockMvc restAttendanceMockMvc;

    private Attendance attendance;

    private Attendance insertedAttendance;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Attendance createEntity() {
        Attendance attendance = new Attendance().date(DEFAULT_DATE).stateAttendance(DEFAULT_STATE_ATTENDANCE);
        // Add required entity
        ClassSection classSection;
        classSection = ClassSectionResourceIT.createEntity();
        classSection.setId("fixed-id-for-tests");
        attendance.setClassSection(classSection);
        // Add required entity
        UserProfile userProfile;
        userProfile = UserProfileResourceIT.createEntity();
        userProfile.setId("fixed-id-for-tests");
        attendance.setStudent(userProfile);
        return attendance;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Attendance createUpdatedEntity() {
        Attendance updatedAttendance = new Attendance().date(UPDATED_DATE).stateAttendance(UPDATED_STATE_ATTENDANCE);
        // Add required entity
        ClassSection classSection;
        classSection = ClassSectionResourceIT.createUpdatedEntity();
        classSection.setId("fixed-id-for-tests");
        updatedAttendance.setClassSection(classSection);
        // Add required entity
        UserProfile userProfile;
        userProfile = UserProfileResourceIT.createUpdatedEntity();
        userProfile.setId("fixed-id-for-tests");
        updatedAttendance.setStudent(userProfile);
        return updatedAttendance;
    }

    @BeforeEach
    void initTest() {
        attendance = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedAttendance != null) {
            attendanceRepository.delete(insertedAttendance);
            insertedAttendance = null;
        }
    }

    @Test
    void createAttendance() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Attendance
        AttendanceDTO attendanceDTO = attendanceMapper.toDto(attendance);
        var returnedAttendanceDTO = om.readValue(
            restAttendanceMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(attendanceDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AttendanceDTO.class
        );

        // Validate the Attendance in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedAttendance = attendanceMapper.toEntity(returnedAttendanceDTO);
        assertAttendanceUpdatableFieldsEquals(returnedAttendance, getPersistedAttendance(returnedAttendance));

        insertedAttendance = returnedAttendance;
    }

    @Test
    void createAttendanceWithExistingId() throws Exception {
        // Create the Attendance with an existing ID
        attendance.setId("existing_id");
        AttendanceDTO attendanceDTO = attendanceMapper.toDto(attendance);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAttendanceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(attendanceDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Attendance in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        attendance.setDate(null);

        // Create the Attendance, which fails.
        AttendanceDTO attendanceDTO = attendanceMapper.toDto(attendance);

        restAttendanceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(attendanceDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkStateAttendanceIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        attendance.setStateAttendance(null);

        // Create the Attendance, which fails.
        AttendanceDTO attendanceDTO = attendanceMapper.toDto(attendance);

        restAttendanceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(attendanceDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllAttendances() throws Exception {
        // Initialize the database
        insertedAttendance = attendanceRepository.save(attendance);

        // Get all the attendanceList
        restAttendanceMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(attendance.getId())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))
            .andExpect(jsonPath("$.[*].stateAttendance").value(hasItem(DEFAULT_STATE_ATTENDANCE.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAttendancesWithEagerRelationshipsIsEnabled() throws Exception {
        when(attendanceServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAttendanceMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(attendanceServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAttendancesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(attendanceServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAttendanceMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(attendanceRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void getAttendance() throws Exception {
        // Initialize the database
        insertedAttendance = attendanceRepository.save(attendance);

        // Get the attendance
        restAttendanceMockMvc
            .perform(get(ENTITY_API_URL_ID, attendance.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(attendance.getId()))
            .andExpect(jsonPath("$.date").value(DEFAULT_DATE.toString()))
            .andExpect(jsonPath("$.stateAttendance").value(DEFAULT_STATE_ATTENDANCE.toString()));
    }

    @Test
    void getNonExistingAttendance() throws Exception {
        // Get the attendance
        restAttendanceMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingAttendance() throws Exception {
        // Initialize the database
        insertedAttendance = attendanceRepository.save(attendance);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the attendance
        Attendance updatedAttendance = attendanceRepository.findById(attendance.getId()).orElseThrow();
        updatedAttendance.date(UPDATED_DATE).stateAttendance(UPDATED_STATE_ATTENDANCE);
        AttendanceDTO attendanceDTO = attendanceMapper.toDto(updatedAttendance);

        restAttendanceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, attendanceDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(attendanceDTO))
            )
            .andExpect(status().isOk());

        // Validate the Attendance in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAttendanceToMatchAllProperties(updatedAttendance);
    }

    @Test
    void putNonExistingAttendance() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        attendance.setId(UUID.randomUUID().toString());

        // Create the Attendance
        AttendanceDTO attendanceDTO = attendanceMapper.toDto(attendance);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAttendanceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, attendanceDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(attendanceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Attendance in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchAttendance() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        attendance.setId(UUID.randomUUID().toString());

        // Create the Attendance
        AttendanceDTO attendanceDTO = attendanceMapper.toDto(attendance);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAttendanceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(attendanceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Attendance in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamAttendance() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        attendance.setId(UUID.randomUUID().toString());

        // Create the Attendance
        AttendanceDTO attendanceDTO = attendanceMapper.toDto(attendance);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAttendanceMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(attendanceDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Attendance in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateAttendanceWithPatch() throws Exception {
        // Initialize the database
        insertedAttendance = attendanceRepository.save(attendance);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the attendance using partial update
        Attendance partialUpdatedAttendance = new Attendance();
        partialUpdatedAttendance.setId(attendance.getId());

        partialUpdatedAttendance.date(UPDATED_DATE);

        restAttendanceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAttendance.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAttendance))
            )
            .andExpect(status().isOk());

        // Validate the Attendance in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAttendanceUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedAttendance, attendance),
            getPersistedAttendance(attendance)
        );
    }

    @Test
    void fullUpdateAttendanceWithPatch() throws Exception {
        // Initialize the database
        insertedAttendance = attendanceRepository.save(attendance);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the attendance using partial update
        Attendance partialUpdatedAttendance = new Attendance();
        partialUpdatedAttendance.setId(attendance.getId());

        partialUpdatedAttendance.date(UPDATED_DATE).stateAttendance(UPDATED_STATE_ATTENDANCE);

        restAttendanceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAttendance.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAttendance))
            )
            .andExpect(status().isOk());

        // Validate the Attendance in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAttendanceUpdatableFieldsEquals(partialUpdatedAttendance, getPersistedAttendance(partialUpdatedAttendance));
    }

    @Test
    void patchNonExistingAttendance() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        attendance.setId(UUID.randomUUID().toString());

        // Create the Attendance
        AttendanceDTO attendanceDTO = attendanceMapper.toDto(attendance);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAttendanceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, attendanceDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(attendanceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Attendance in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchAttendance() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        attendance.setId(UUID.randomUUID().toString());

        // Create the Attendance
        AttendanceDTO attendanceDTO = attendanceMapper.toDto(attendance);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAttendanceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(attendanceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Attendance in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamAttendance() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        attendance.setId(UUID.randomUUID().toString());

        // Create the Attendance
        AttendanceDTO attendanceDTO = attendanceMapper.toDto(attendance);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAttendanceMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(attendanceDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Attendance in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteAttendance() throws Exception {
        // Initialize the database
        insertedAttendance = attendanceRepository.save(attendance);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the attendance
        restAttendanceMockMvc
            .perform(delete(ENTITY_API_URL_ID, attendance.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return attendanceRepository.count();
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

    protected Attendance getPersistedAttendance(Attendance attendance) {
        return attendanceRepository.findById(attendance.getId()).orElseThrow();
    }

    protected void assertPersistedAttendanceToMatchAllProperties(Attendance expectedAttendance) {
        assertAttendanceAllPropertiesEquals(expectedAttendance, getPersistedAttendance(expectedAttendance));
    }

    protected void assertPersistedAttendanceToMatchUpdatableProperties(Attendance expectedAttendance) {
        assertAttendanceAllUpdatablePropertiesEquals(expectedAttendance, getPersistedAttendance(expectedAttendance));
    }
}
