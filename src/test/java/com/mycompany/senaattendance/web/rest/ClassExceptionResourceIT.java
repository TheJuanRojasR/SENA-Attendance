package com.mycompany.senaattendance.web.rest;

import static com.mycompany.senaattendance.domain.ClassExceptionAsserts.*;
import static com.mycompany.senaattendance.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.ClassException;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.repository.ClassExceptionRepository;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.dto.ClassExceptionDTO;
import com.mycompany.senaattendance.service.mapper.ClassExceptionMapper;
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
 * Integration tests for the {@link ClassExceptionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class ClassExceptionResourceIT {

    private static final LocalDate DEFAULT_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_REASON = "AAAAAAAAAA";
    private static final String UPDATED_REASON = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/class-exceptions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ClassExceptionRepository classExceptionRepository;

    @Autowired
    private ClassSectionRepository classSectionRepository;

    @Autowired
    private ClassExceptionMapper classExceptionMapper;

    @Autowired
    private MockMvc restClassExceptionMockMvc;

    private ClassException classException;

    private ClassException insertedClassException;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ClassException createEntity() {
        ClassException classException = new ClassException().date(DEFAULT_DATE).reason(DEFAULT_REASON);
        // Add required entity
        ClassSection classSection;
        classSection = ClassSectionResourceIT.createEntity();
        classSection.setId("fixed-id-for-tests");
        classException.setClassSection(classSection);
        return classException;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ClassException createUpdatedEntity() {
        ClassException updatedClassException = new ClassException().date(UPDATED_DATE).reason(UPDATED_REASON);
        // Add required entity
        ClassSection classSection;
        classSection = ClassSectionResourceIT.createUpdatedEntity();
        classSection.setId("fixed-id-for-tests");
        updatedClassException.setClassSection(classSection);
        return updatedClassException;
    }

    @BeforeEach
    void initTest() {
        classException = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedClassException != null) {
            classExceptionRepository.delete(insertedClassException);
            insertedClassException = null;
        }
        // Remove the related document persisted for the PUT tests
        classSectionRepository.deleteAll();
    }

    @Test
    void createClassException() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ClassException
        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(classException);
        var returnedClassExceptionDTO = om.readValue(
            restClassExceptionMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classExceptionDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ClassExceptionDTO.class
        );

        // Validate the ClassException in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedClassException = classExceptionMapper.toEntity(returnedClassExceptionDTO);
        assertClassExceptionUpdatableFieldsEquals(returnedClassException, getPersistedClassException(returnedClassException));

        insertedClassException = returnedClassException;
    }

    @Test
    void createClassExceptionWithExistingId() throws Exception {
        // Create the ClassException with an existing ID
        classException.setId("existing_id");
        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(classException);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restClassExceptionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ClassException in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        classException.setDate(null);

        // Create the ClassException, which fails.
        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(classException);

        restClassExceptionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkReasonIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        classException.setReason(null);

        // Create the ClassException, which fails.
        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(classException);

        restClassExceptionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllClassExceptions() throws Exception {
        // Initialize the database
        insertedClassException = classExceptionRepository.save(classException);

        // Get all the classExceptionList
        restClassExceptionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(classException.getId())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))
            .andExpect(jsonPath("$.[*].reason").value(hasItem(DEFAULT_REASON)));
    }

    @Test
    void getClassException() throws Exception {
        // Initialize the database
        insertedClassException = classExceptionRepository.save(classException);

        // Get the classException
        restClassExceptionMockMvc
            .perform(get(ENTITY_API_URL_ID, classException.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(classException.getId()))
            .andExpect(jsonPath("$.date").value(DEFAULT_DATE.toString()))
            .andExpect(jsonPath("$.reason").value(DEFAULT_REASON));
    }

    @Test
    void getNonExistingClassException() throws Exception {
        // Get the classException
        restClassExceptionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingClassException() throws Exception {
        // Persist the @DBRef targets so they resolve on reload
        classSectionRepository.save(classException.getClassSection());

        // Initialize the database
        insertedClassException = classExceptionRepository.save(classException);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the classException
        ClassException updatedClassException = classExceptionRepository.findById(classException.getId()).orElseThrow();
        updatedClassException.date(UPDATED_DATE).reason(UPDATED_REASON);
        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(updatedClassException);

        restClassExceptionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isOk());

        // Validate the ClassException in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedClassExceptionToMatchAllProperties(updatedClassException);
    }

    @Test
    void putNonExistingClassException() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classException.setId(UUID.randomUUID().toString());

        // Create the ClassException
        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(classException);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restClassExceptionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ClassException in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putClassExceptionWithoutIdReturnsBadRequest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classException.setId(null);

        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(classException);

        restClassExceptionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.idnull"));

        // Validate the ClassException in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateClassExceptionWithPatch() throws Exception {
        // Initialize the database
        insertedClassException = classExceptionRepository.save(classException);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the classException using partial update
        ClassException partialUpdatedClassException = new ClassException();
        partialUpdatedClassException.setId(classException.getId());

        partialUpdatedClassException.reason(UPDATED_REASON);

        restClassExceptionMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedClassException))
            )
            .andExpect(status().isOk());

        // Validate the ClassException in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertClassExceptionUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedClassException, classException),
            getPersistedClassException(classException)
        );
    }

    @Test
    void fullUpdateClassExceptionWithPatch() throws Exception {
        // Initialize the database
        insertedClassException = classExceptionRepository.save(classException);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the classException using partial update
        ClassException partialUpdatedClassException = new ClassException();
        partialUpdatedClassException.setId(classException.getId());

        partialUpdatedClassException.date(UPDATED_DATE).reason(UPDATED_REASON);

        restClassExceptionMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedClassException))
            )
            .andExpect(status().isOk());

        // Validate the ClassException in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertClassExceptionUpdatableFieldsEquals(partialUpdatedClassException, getPersistedClassException(partialUpdatedClassException));
    }

    @Test
    void patchNonExistingClassException() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classException.setId(UUID.randomUUID().toString());

        // Create the ClassException
        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(classException);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restClassExceptionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ClassException in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchClassExceptionWithoutIdReturnsBadRequest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classException.setId(null);

        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(classException);

        restClassExceptionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.idnull"));

        // Validate the ClassException in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteClassException() throws Exception {
        // Initialize the database
        insertedClassException = classExceptionRepository.save(classException);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the classException
        restClassExceptionMockMvc
            .perform(delete(ENTITY_API_URL_ID, classException.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    // -----------------------------------------------------------------
    // Authorization: writes are restricted to admins
    // -----------------------------------------------------------------

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.COORDINATOR)
    void createClassExceptionAsNonAdminReturnsForbidden() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(classException);

        restClassExceptionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isForbidden());

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.COORDINATOR)
    void updateClassExceptionAsNonAdminReturnsForbidden() throws Exception {
        insertedClassException = classExceptionRepository.save(classException);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(insertedClassException);

        restClassExceptionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isForbidden());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.COORDINATOR)
    void partialUpdateClassExceptionAsNonAdminReturnsForbidden() throws Exception {
        insertedClassException = classExceptionRepository.save(classException);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        ClassExceptionDTO classExceptionDTO = classExceptionMapper.toDto(insertedClassException);

        restClassExceptionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(classExceptionDTO)))
            .andExpect(status().isForbidden());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.COORDINATOR)
    void deleteClassExceptionAsNonAdminReturnsForbidden() throws Exception {
        insertedClassException = classExceptionRepository.save(classException);

        long databaseSizeBeforeDelete = getRepositoryCount();

        restClassExceptionMockMvc
            .perform(delete(ENTITY_API_URL_ID, insertedClassException.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());

        assertSameRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return classExceptionRepository.count();
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

    protected ClassException getPersistedClassException(ClassException classException) {
        return classExceptionRepository.findById(classException.getId()).orElseThrow();
    }

    protected void assertPersistedClassExceptionToMatchAllProperties(ClassException expectedClassException) {
        assertClassExceptionAllPropertiesEquals(expectedClassException, getPersistedClassException(expectedClassException));
    }

    protected void assertPersistedClassExceptionToMatchUpdatableProperties(ClassException expectedClassException) {
        assertClassExceptionAllUpdatablePropertiesEquals(expectedClassException, getPersistedClassException(expectedClassException));
    }
}
