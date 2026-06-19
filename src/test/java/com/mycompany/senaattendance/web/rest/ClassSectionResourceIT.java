package com.mycompany.senaattendance.web.rest;

import static com.mycompany.senaattendance.domain.ClassSectionAsserts.*;
import static com.mycompany.senaattendance.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.service.ClassSectionService;
import com.mycompany.senaattendance.service.dto.ClassSectionDTO;
import com.mycompany.senaattendance.service.mapper.ClassSectionMapper;
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
 * Integration tests for the {@link ClassSectionResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class ClassSectionResourceIT {

    private static final String DEFAULT_SUBJECT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_SUBJECT_NAME = "BBBBBBBBBB";

    private static final Boolean DEFAULT_IS_ACTIVE = false;
    private static final Boolean UPDATED_IS_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/class-sections";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ClassSectionRepository classSectionRepository;

    @Mock
    private ClassSectionRepository classSectionRepositoryMock;

    @Autowired
    private ClassSectionMapper classSectionMapper;

    @Mock
    private ClassSectionService classSectionServiceMock;

    @Autowired
    private MockMvc restClassSectionMockMvc;

    private ClassSection classSection;

    private ClassSection insertedClassSection;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ClassSection createEntity() {
        ClassSection classSection = new ClassSection().subjectName(DEFAULT_SUBJECT_NAME).isActive(DEFAULT_IS_ACTIVE);
        // Add required entity
        UserProfile userProfile;
        userProfile = UserProfileResourceIT.createEntity();
        userProfile.setId("fixed-id-for-tests");
        classSection.setInstructor(userProfile);
        // Add required entity
        Grade grade;
        grade = GradeResourceIT.createEntity();
        grade.setId("fixed-id-for-tests");
        classSection.setGrade(grade);
        return classSection;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ClassSection createUpdatedEntity() {
        ClassSection updatedClassSection = new ClassSection().subjectName(UPDATED_SUBJECT_NAME).isActive(UPDATED_IS_ACTIVE);
        // Add required entity
        UserProfile userProfile;
        userProfile = UserProfileResourceIT.createUpdatedEntity();
        userProfile.setId("fixed-id-for-tests");
        updatedClassSection.setInstructor(userProfile);
        // Add required entity
        Grade grade;
        grade = GradeResourceIT.createUpdatedEntity();
        grade.setId("fixed-id-for-tests");
        updatedClassSection.setGrade(grade);
        return updatedClassSection;
    }

    @BeforeEach
    void initTest() {
        classSection = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedClassSection != null) {
            classSectionRepository.delete(insertedClassSection);
            insertedClassSection = null;
        }
    }

    @Test
    void createClassSection() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ClassSection
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(classSection);
        var returnedClassSectionDTO = om.readValue(
            restClassSectionMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classSectionDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ClassSectionDTO.class
        );

        // Validate the ClassSection in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedClassSection = classSectionMapper.toEntity(returnedClassSectionDTO);
        assertClassSectionUpdatableFieldsEquals(returnedClassSection, getPersistedClassSection(returnedClassSection));

        insertedClassSection = returnedClassSection;
    }

    @Test
    void createClassSectionWithExistingId() throws Exception {
        // Create the ClassSection with an existing ID
        classSection.setId("existing_id");
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(classSection);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restClassSectionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classSectionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ClassSection in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkSubjectNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        classSection.setSubjectName(null);

        // Create the ClassSection, which fails.
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(classSection);

        restClassSectionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classSectionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkIsActiveIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        classSection.setIsActive(null);

        // Create the ClassSection, which fails.
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(classSection);

        restClassSectionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classSectionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllClassSections() throws Exception {
        // Initialize the database
        insertedClassSection = classSectionRepository.save(classSection);

        // Get all the classSectionList
        restClassSectionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(classSection.getId())))
            .andExpect(jsonPath("$.[*].subjectName").value(hasItem(DEFAULT_SUBJECT_NAME)))
            .andExpect(jsonPath("$.[*].isActive").value(hasItem(DEFAULT_IS_ACTIVE)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllClassSectionsWithEagerRelationshipsIsEnabled() throws Exception {
        when(classSectionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restClassSectionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(classSectionServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllClassSectionsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(classSectionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restClassSectionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(classSectionRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void getClassSection() throws Exception {
        // Initialize the database
        insertedClassSection = classSectionRepository.save(classSection);

        // Get the classSection
        restClassSectionMockMvc
            .perform(get(ENTITY_API_URL_ID, classSection.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(classSection.getId()))
            .andExpect(jsonPath("$.subjectName").value(DEFAULT_SUBJECT_NAME))
            .andExpect(jsonPath("$.isActive").value(DEFAULT_IS_ACTIVE));
    }

    @Test
    void getNonExistingClassSection() throws Exception {
        // Get the classSection
        restClassSectionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingClassSection() throws Exception {
        // Initialize the database
        insertedClassSection = classSectionRepository.save(classSection);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the classSection
        ClassSection updatedClassSection = classSectionRepository.findById(classSection.getId()).orElseThrow();
        updatedClassSection.subjectName(UPDATED_SUBJECT_NAME).isActive(UPDATED_IS_ACTIVE);
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(updatedClassSection);

        restClassSectionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, classSectionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(classSectionDTO))
            )
            .andExpect(status().isOk());

        // Validate the ClassSection in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedClassSectionToMatchAllProperties(updatedClassSection);
    }

    @Test
    void putNonExistingClassSection() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classSection.setId(UUID.randomUUID().toString());

        // Create the ClassSection
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(classSection);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restClassSectionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, classSectionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(classSectionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ClassSection in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchClassSection() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classSection.setId(UUID.randomUUID().toString());

        // Create the ClassSection
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(classSection);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restClassSectionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(classSectionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ClassSection in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamClassSection() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classSection.setId(UUID.randomUUID().toString());

        // Create the ClassSection
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(classSection);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restClassSectionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classSectionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ClassSection in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateClassSectionWithPatch() throws Exception {
        // Initialize the database
        insertedClassSection = classSectionRepository.save(classSection);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the classSection using partial update
        ClassSection partialUpdatedClassSection = new ClassSection();
        partialUpdatedClassSection.setId(classSection.getId());

        restClassSectionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedClassSection.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedClassSection))
            )
            .andExpect(status().isOk());

        // Validate the ClassSection in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertClassSectionUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedClassSection, classSection),
            getPersistedClassSection(classSection)
        );
    }

    @Test
    void fullUpdateClassSectionWithPatch() throws Exception {
        // Initialize the database
        insertedClassSection = classSectionRepository.save(classSection);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the classSection using partial update
        ClassSection partialUpdatedClassSection = new ClassSection();
        partialUpdatedClassSection.setId(classSection.getId());

        partialUpdatedClassSection.subjectName(UPDATED_SUBJECT_NAME).isActive(UPDATED_IS_ACTIVE);

        restClassSectionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedClassSection.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedClassSection))
            )
            .andExpect(status().isOk());

        // Validate the ClassSection in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertClassSectionUpdatableFieldsEquals(partialUpdatedClassSection, getPersistedClassSection(partialUpdatedClassSection));
    }

    @Test
    void patchNonExistingClassSection() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classSection.setId(UUID.randomUUID().toString());

        // Create the ClassSection
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(classSection);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restClassSectionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, classSectionDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(classSectionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ClassSection in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchClassSection() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classSection.setId(UUID.randomUUID().toString());

        // Create the ClassSection
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(classSection);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restClassSectionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(classSectionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ClassSection in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamClassSection() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classSection.setId(UUID.randomUUID().toString());

        // Create the ClassSection
        ClassSectionDTO classSectionDTO = classSectionMapper.toDto(classSection);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restClassSectionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(classSectionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ClassSection in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteClassSection() throws Exception {
        // Initialize the database
        insertedClassSection = classSectionRepository.save(classSection);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the classSection
        restClassSectionMockMvc
            .perform(delete(ENTITY_API_URL_ID, classSection.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return classSectionRepository.count();
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

    protected ClassSection getPersistedClassSection(ClassSection classSection) {
        return classSectionRepository.findById(classSection.getId()).orElseThrow();
    }

    protected void assertPersistedClassSectionToMatchAllProperties(ClassSection expectedClassSection) {
        assertClassSectionAllPropertiesEquals(expectedClassSection, getPersistedClassSection(expectedClassSection));
    }

    protected void assertPersistedClassSectionToMatchUpdatableProperties(ClassSection expectedClassSection) {
        assertClassSectionAllUpdatablePropertiesEquals(expectedClassSection, getPersistedClassSection(expectedClassSection));
    }
}
