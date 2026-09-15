package com.mycompany.senaattendance.web.rest;

import static com.mycompany.senaattendance.domain.DocumentTypeAsserts.*;
import static com.mycompany.senaattendance.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.DocumentType;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.repository.DocumentTypeRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.dto.DocumentTypeDTO;
import com.mycompany.senaattendance.service.mapper.DocumentTypeMapper;
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
 * Integration tests for the {@link DocumentTypeResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class DocumentTypeResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_INITIALS = "AAAAAAAAAA";
    private static final String UPDATED_INITIALS = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/document-types";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private DocumentTypeMapper documentTypeMapper;

    @Autowired
    private MockMvc restDocumentTypeMockMvc;

    private DocumentType documentType;

    private DocumentType insertedDocumentType;

    private UserProfile insertedUserProfile;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DocumentType createEntity() {
        return new DocumentType().name(DEFAULT_NAME).initials(DEFAULT_INITIALS).isActive(true);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DocumentType createUpdatedEntity() {
        return new DocumentType().name(UPDATED_NAME).initials(UPDATED_INITIALS);
    }

    @BeforeEach
    void initTest() {
        documentType = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedUserProfile != null) {
            userProfileRepository.delete(insertedUserProfile);
            insertedUserProfile = null;
        }
        if (insertedDocumentType != null) {
            documentTypeRepository.delete(insertedDocumentType);
            insertedDocumentType = null;
        }
    }

    /**
     * Builds a minimal user profile that references the given document type, so tests can assert
     * the "document type is in use" rules. The random document number avoids the compound unique
     * index on (documentType, documentNumber).
     */
    private UserProfile createProfileUsing(DocumentType documentType) {
        UserProfile profile = new UserProfile()
            .firstName("AAAAAAAAAA")
            .firstLastName("AAAAAAAAAA")
            .documentNumber("D" + UUID.randomUUID().toString().replace("-", "").substring(0, 12))
            .phoneNumber("3000000000");
        profile.setDocumentType(documentType);
        return profile;
    }

    @Test
    void createDocumentType() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the DocumentType
        DocumentTypeDTO documentTypeDTO = documentTypeMapper.toDto(documentType);
        var returnedDocumentTypeDTO = om.readValue(
            restDocumentTypeMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentTypeDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            DocumentTypeDTO.class
        );

        // Validate the DocumentType in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedDocumentType = documentTypeMapper.toEntity(returnedDocumentTypeDTO);
        assertDocumentTypeUpdatableFieldsEquals(returnedDocumentType, getPersistedDocumentType(returnedDocumentType));

        insertedDocumentType = returnedDocumentType;
    }

    @Test
    void createDocumentTypeWithoutIsActiveIsPersistedActive() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // set the field null so the backend applies its default of active
        documentType.setIsActive(null);

        DocumentTypeDTO documentTypeDTO = documentTypeMapper.toDto(documentType);
        var returnedDocumentTypeDTO = om.readValue(
            restDocumentTypeMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentTypeDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            DocumentTypeDTO.class
        );

        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertThat(returnedDocumentTypeDTO.getIsActive()).isTrue();

        var returnedDocumentType = documentTypeMapper.toEntity(returnedDocumentTypeDTO);
        assertThat(getPersistedDocumentType(returnedDocumentType).getIsActive()).isTrue();

        insertedDocumentType = returnedDocumentType;
    }

    @Test
    void createDocumentTypeWithExistingId() throws Exception {
        // Create the DocumentType with an existing ID
        documentType.setId("existing_id");
        DocumentTypeDTO documentTypeDTO = documentTypeMapper.toDto(documentType);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDocumentTypeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentTypeDTO)))
            .andExpect(status().isBadRequest());

        // Validate the DocumentType in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void createDocumentTypeWithDuplicateNameReturnsBadRequest() throws Exception {
        // Persist a document type with DEFAULT_NAME so the upcoming POST collides on name only
        insertedDocumentType = documentTypeRepository.save(documentType);
        long databaseSizeBeforeCreate = getRepositoryCount();

        DocumentTypeDTO documentTypeDTO = documentTypeMapper.toDto(documentType);
        documentTypeDTO.setId(null);
        documentTypeDTO.setName("aaaaaaaaaa");
        documentTypeDTO.setInitials("ZZ");

        restDocumentTypeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentTypeDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.documentTypeNameAlreadyUsed"));

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void createDocumentTypeWithBlankNameReturnsBadRequest() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        documentType.setName("   ");

        DocumentTypeDTO documentTypeDTO = documentTypeMapper.toDto(documentType);

        restDocumentTypeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentTypeDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.validation"))
            .andExpect(jsonPath("$.fieldErrors").isArray())
            .andExpect(jsonPath("$.fieldErrors[0].field").value("name"));

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void createDocumentTypeNormalizesInitialsToUpperCase() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        documentType.setName("Tipo Normalizado");
        documentType.setInitials("qz");

        DocumentTypeDTO documentTypeDTO = documentTypeMapper.toDto(documentType);
        var returnedDocumentTypeDTO = om.readValue(
            restDocumentTypeMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentTypeDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            DocumentTypeDTO.class
        );

        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertThat(returnedDocumentTypeDTO.getInitials()).isEqualTo("QZ");

        var returnedDocumentType = documentTypeMapper.toEntity(returnedDocumentTypeDTO);
        assertThat(getPersistedDocumentType(returnedDocumentType).getInitials()).isEqualTo("QZ");

        insertedDocumentType = returnedDocumentType;
    }

    @Test
    void createDocumentTypeWithDuplicateInitialsReturnsBadRequest() throws Exception {
        // Persist a document type with DEFAULT_INITIALS so the upcoming POST collides on initials only
        insertedDocumentType = documentTypeRepository.save(documentType);
        long databaseSizeBeforeCreate = getRepositoryCount();

        DocumentTypeDTO documentTypeDTO = documentTypeMapper.toDto(documentType);
        documentTypeDTO.setId(null);
        documentTypeDTO.setName(UPDATED_NAME);
        documentTypeDTO.setInitials("aaaaaaaaaa");

        restDocumentTypeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentTypeDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.documentTypeInitialsAlreadyUsed"));

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void createDocumentTypeWithBlankInitialsReturnsBadRequest() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        documentType.setInitials("   ");

        DocumentTypeDTO documentTypeDTO = documentTypeMapper.toDto(documentType);

        restDocumentTypeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentTypeDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.validation"))
            .andExpect(jsonPath("$.fieldErrors").isArray())
            .andExpect(jsonPath("$.fieldErrors[0].field").value("initials"));

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        documentType.setName(null);

        // Create the DocumentType, which fails.
        DocumentTypeDTO documentTypeDTO = documentTypeMapper.toDto(documentType);

        restDocumentTypeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentTypeDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkInitialsIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        documentType.setInitials(null);

        // Create the DocumentType, which fails.
        DocumentTypeDTO documentTypeDTO = documentTypeMapper.toDto(documentType);

        restDocumentTypeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentTypeDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllDocumentTypes() throws Exception {
        // Initialize the database
        insertedDocumentType = documentTypeRepository.save(documentType);

        // Get all the documentTypeList
        restDocumentTypeMockMvc
            .perform(get(ENTITY_API_URL + "?page=0&size=20&sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", String.valueOf(getRepositoryCount())))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(documentType.getId())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].initials").value(hasItem(DEFAULT_INITIALS)));
    }

    @Test
    void getAllDocumentTypesWithSizeOneReturnsOnlyOneElementWithTotalCount() throws Exception {
        insertedDocumentType = documentTypeRepository.save(documentType);
        DocumentType other = documentTypeRepository.save(createUpdatedEntity());

        try {
            restDocumentTypeMockMvc
                .perform(get(ENTITY_API_URL + "?page=0&size=1"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", String.valueOf(getRepositoryCount())))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(1)));
        } finally {
            documentTypeRepository.delete(other);
        }
    }

    @Test
    void getActiveDocumentTypes() throws Exception {
        insertedDocumentType = documentTypeRepository.save(documentType);
        DocumentType inactiveDocumentType = documentTypeRepository.save(createUpdatedEntity().isActive(false));

        try {
            restDocumentTypeMockMvc
                .perform(get(ENTITY_API_URL + "/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(insertedDocumentType.getId())))
                .andExpect(jsonPath("$.[*].id").value(not(hasItem(inactiveDocumentType.getId()))));
        } finally {
            documentTypeRepository.delete(inactiveDocumentType);
        }
    }

    @Test
    void getDocumentType() throws Exception {
        // Initialize the database
        insertedDocumentType = documentTypeRepository.save(documentType);

        // Get the documentType
        restDocumentTypeMockMvc
            .perform(get(ENTITY_API_URL_ID, documentType.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(documentType.getId()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.initials").value(DEFAULT_INITIALS));
    }

    @Test
    void getNonExistingDocumentType() throws Exception {
        // Get the documentType
        restDocumentTypeMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingDocumentType() throws Exception {
        // Initialize the database
        insertedDocumentType = documentTypeRepository.save(documentType);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the documentType
        DocumentType updatedDocumentType = documentTypeRepository.findById(documentType.getId()).orElseThrow();
        updatedDocumentType.name(UPDATED_NAME).initials(UPDATED_INITIALS);
        DocumentTypeDTO documentTypeDTO = documentTypeMapper.toDto(updatedDocumentType);

        restDocumentTypeMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentTypeDTO)))
            .andExpect(status().isOk());

        // Validate the DocumentType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedDocumentTypeToMatchAllProperties(updatedDocumentType);
    }

    @Test
    void putNonExistingDocumentType() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        documentType.setId(UUID.randomUUID().toString());

        // Create the DocumentType
        DocumentTypeDTO documentTypeDTO = documentTypeMapper.toDto(documentType);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDocumentTypeMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentTypeDTO)))
            .andExpect(status().isBadRequest());

        // Validate the DocumentType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putDocumentTypeWithoutIdReturnsBadRequest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        documentType.setId(null);

        DocumentTypeDTO documentTypeDTO = documentTypeMapper.toDto(documentType);

        restDocumentTypeMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentTypeDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.idnull"));

        // Validate the DocumentType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putDocumentTypeWithDuplicateNameReturnsBadRequest() throws Exception {
        insertedDocumentType = documentTypeRepository.save(documentType);

        DocumentType other = documentTypeRepository.save(createUpdatedEntity());

        try {
            long databaseSizeBeforeUpdate = getRepositoryCount();
            DocumentTypeDTO documentTypeDTO = documentTypeMapper.toDto(other);
            documentTypeDTO.setName(DEFAULT_NAME);

            restDocumentTypeMockMvc
                .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentTypeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error.documentTypeNameAlreadyUsed"));

            assertSameRepositoryCount(databaseSizeBeforeUpdate);
            assertThat(getPersistedDocumentType(other).getName()).isEqualTo(UPDATED_NAME);
        } finally {
            documentTypeRepository.delete(other);
        }
    }

    @Test
    void putDocumentTypeKeepingOwnNameSucceeds() throws Exception {
        insertedDocumentType = documentTypeRepository.save(documentType);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        DocumentTypeDTO documentTypeDTO = documentTypeMapper.toDto(insertedDocumentType);

        restDocumentTypeMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentTypeDTO)))
            .andExpect(status().isOk());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertThat(getPersistedDocumentType(insertedDocumentType).getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    void putDocumentTypeWithDuplicateInitialsReturnsBadRequest() throws Exception {
        insertedDocumentType = documentTypeRepository.save(documentType);

        DocumentType other = documentTypeRepository.save(createUpdatedEntity());

        try {
            long databaseSizeBeforeUpdate = getRepositoryCount();
            DocumentTypeDTO documentTypeDTO = documentTypeMapper.toDto(other);
            documentTypeDTO.setInitials(DEFAULT_INITIALS);

            restDocumentTypeMockMvc
                .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentTypeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error.documentTypeInitialsAlreadyUsed"));

            assertSameRepositoryCount(databaseSizeBeforeUpdate);
            assertThat(getPersistedDocumentType(other).getInitials()).isEqualTo(UPDATED_INITIALS);
        } finally {
            documentTypeRepository.delete(other);
        }
    }

    @Test
    void putDocumentTypeInitialsOfInUseReturnsBadRequest() throws Exception {
        insertedDocumentType = documentTypeRepository.save(documentType);
        insertedUserProfile = userProfileRepository.save(createProfileUsing(insertedDocumentType));

        DocumentType updatedDocumentType = documentTypeRepository.findById(insertedDocumentType.getId()).orElseThrow();
        updatedDocumentType.setInitials(UPDATED_INITIALS);
        DocumentTypeDTO documentTypeDTO = documentTypeMapper.toDto(updatedDocumentType);

        restDocumentTypeMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentTypeDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.documentTypeInitialsInUse"));

        assertThat(getPersistedDocumentType(insertedDocumentType).getInitials()).isEqualTo(DEFAULT_INITIALS);
    }

    @Test
    void putDocumentTypeNameOfInUseSucceeds() throws Exception {
        insertedDocumentType = documentTypeRepository.save(documentType);
        insertedUserProfile = userProfileRepository.save(createProfileUsing(insertedDocumentType));

        DocumentType updatedDocumentType = documentTypeRepository.findById(insertedDocumentType.getId()).orElseThrow();
        updatedDocumentType.setName(UPDATED_NAME);
        DocumentTypeDTO documentTypeDTO = documentTypeMapper.toDto(updatedDocumentType);

        restDocumentTypeMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentTypeDTO)))
            .andExpect(status().isOk());

        assertThat(getPersistedDocumentType(insertedDocumentType).getName()).isEqualTo(UPDATED_NAME);
        assertThat(getPersistedDocumentType(insertedDocumentType).getInitials()).isEqualTo(DEFAULT_INITIALS);
    }

    @Test
    void patchDocumentTypeInitialsOfInUseReturnsBadRequest() throws Exception {
        insertedDocumentType = documentTypeRepository.save(documentType);
        insertedUserProfile = userProfileRepository.save(createProfileUsing(insertedDocumentType));

        DocumentType partialUpdatedDocumentType = new DocumentType();
        partialUpdatedDocumentType.setId(insertedDocumentType.getId());
        partialUpdatedDocumentType.setInitials(UPDATED_INITIALS);

        restDocumentTypeMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(partialUpdatedDocumentType))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.documentTypeInitialsInUse"));

        assertThat(getPersistedDocumentType(insertedDocumentType).getInitials()).isEqualTo(DEFAULT_INITIALS);
    }

    @Test
    void partialUpdateDocumentTypeWithPatch() throws Exception {
        // Initialize the database
        insertedDocumentType = documentTypeRepository.save(documentType);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the documentType using partial update
        DocumentType partialUpdatedDocumentType = new DocumentType();
        partialUpdatedDocumentType.setId(documentType.getId());

        partialUpdatedDocumentType.name(UPDATED_NAME);

        restDocumentTypeMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(partialUpdatedDocumentType))
            )
            .andExpect(status().isOk());

        // Validate the DocumentType in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDocumentTypeUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedDocumentType, documentType),
            getPersistedDocumentType(documentType)
        );
    }

    @Test
    void fullUpdateDocumentTypeWithPatch() throws Exception {
        // Initialize the database
        insertedDocumentType = documentTypeRepository.save(documentType);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the documentType using partial update
        DocumentType partialUpdatedDocumentType = new DocumentType();
        partialUpdatedDocumentType.setId(documentType.getId());

        partialUpdatedDocumentType.name(UPDATED_NAME).initials(UPDATED_INITIALS).isActive(true);

        restDocumentTypeMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(partialUpdatedDocumentType))
            )
            .andExpect(status().isOk());

        // Validate the DocumentType in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDocumentTypeUpdatableFieldsEquals(partialUpdatedDocumentType, getPersistedDocumentType(partialUpdatedDocumentType));
    }

    @Test
    void patchNonExistingDocumentType() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        documentType.setId(UUID.randomUUID().toString());

        // Create the DocumentType
        DocumentTypeDTO documentTypeDTO = documentTypeMapper.toDto(documentType);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDocumentTypeMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(documentTypeDTO)))
            .andExpect(status().isBadRequest());

        // Validate the DocumentType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchDocumentTypeWithoutIdReturnsBadRequest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        documentType.setId(null);

        DocumentTypeDTO documentTypeDTO = documentTypeMapper.toDto(documentType);

        restDocumentTypeMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(documentTypeDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.idnull"));

        // Validate the DocumentType in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchDocumentTypeWithDuplicateNameReturnsBadRequest() throws Exception {
        insertedDocumentType = documentTypeRepository.save(documentType);

        DocumentType other = documentTypeRepository.save(createUpdatedEntity());

        try {
            long databaseSizeBeforeUpdate = getRepositoryCount();

            DocumentType partialUpdatedDocumentType = new DocumentType();
            partialUpdatedDocumentType.setId(other.getId());
            partialUpdatedDocumentType.setName(DEFAULT_NAME);

            restDocumentTypeMockMvc
                .perform(
                    patch(ENTITY_API_URL)
                        .contentType("application/merge-patch+json")
                        .content(om.writeValueAsBytes(partialUpdatedDocumentType))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error.documentTypeNameAlreadyUsed"));

            assertSameRepositoryCount(databaseSizeBeforeUpdate);
            assertThat(getPersistedDocumentType(other).getName()).isEqualTo(UPDATED_NAME);
        } finally {
            documentTypeRepository.delete(other);
        }
    }

    @Test
    void patchDocumentTypeWithDuplicateInitialsReturnsBadRequest() throws Exception {
        insertedDocumentType = documentTypeRepository.save(documentType);

        DocumentType other = documentTypeRepository.save(createUpdatedEntity());

        try {
            long databaseSizeBeforeUpdate = getRepositoryCount();

            DocumentType partialUpdatedDocumentType = new DocumentType();
            partialUpdatedDocumentType.setId(other.getId());
            partialUpdatedDocumentType.setInitials(DEFAULT_INITIALS);

            restDocumentTypeMockMvc
                .perform(
                    patch(ENTITY_API_URL)
                        .contentType("application/merge-patch+json")
                        .content(om.writeValueAsBytes(partialUpdatedDocumentType))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error.documentTypeInitialsAlreadyUsed"));

            assertSameRepositoryCount(databaseSizeBeforeUpdate);
            assertThat(getPersistedDocumentType(other).getInitials()).isEqualTo(UPDATED_INITIALS);
        } finally {
            documentTypeRepository.delete(other);
        }
    }

    @Test
    void deleteDocumentTypeInUseReturnsBadRequest() throws Exception {
        insertedDocumentType = documentTypeRepository.save(documentType);
        insertedUserProfile = userProfileRepository.save(createProfileUsing(insertedDocumentType));

        long databaseSizeBeforeDelete = getRepositoryCount();

        restDocumentTypeMockMvc
            .perform(delete(ENTITY_API_URL_ID, insertedDocumentType.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.documentTypeInUse"));

        assertSameRepositoryCount(databaseSizeBeforeDelete);
    }

    @Test
    void deleteDocumentType() throws Exception {
        // Initialize the database
        insertedDocumentType = documentTypeRepository.save(documentType);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the documentType
        restDocumentTypeMockMvc
            .perform(delete(ENTITY_API_URL_ID, documentType.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.APPRENTICE)
    void createDocumentTypeAsNonAdminReturnsForbidden() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        DocumentTypeDTO documentTypeDTO = documentTypeMapper.toDto(documentType);

        restDocumentTypeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentTypeDTO)))
            .andExpect(status().isForbidden());

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.APPRENTICE)
    void updateDocumentTypeAsNonAdminReturnsForbidden() throws Exception {
        insertedDocumentType = documentTypeRepository.save(documentType);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        DocumentTypeDTO documentTypeDTO = documentTypeMapper.toDto(insertedDocumentType);

        restDocumentTypeMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentTypeDTO)))
            .andExpect(status().isForbidden());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.APPRENTICE)
    void partialUpdateDocumentTypeAsNonAdminReturnsForbidden() throws Exception {
        insertedDocumentType = documentTypeRepository.save(documentType);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        DocumentTypeDTO documentTypeDTO = documentTypeMapper.toDto(insertedDocumentType);

        restDocumentTypeMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(documentTypeDTO)))
            .andExpect(status().isForbidden());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.APPRENTICE)
    void deleteDocumentTypeAsNonAdminReturnsForbidden() throws Exception {
        insertedDocumentType = documentTypeRepository.save(documentType);

        long databaseSizeBeforeDelete = getRepositoryCount();

        restDocumentTypeMockMvc
            .perform(delete(ENTITY_API_URL_ID, insertedDocumentType.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());

        assertSameRepositoryCount(databaseSizeBeforeDelete);
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.INSTRUCTOR)
    void readDocumentTypesAsAuthenticatedNonAdminReturnsOk() throws Exception {
        insertedDocumentType = documentTypeRepository.save(documentType);

        restDocumentTypeMockMvc.perform(get(ENTITY_API_URL)).andExpect(status().isOk());
        restDocumentTypeMockMvc.perform(get(ENTITY_API_URL_ID, insertedDocumentType.getId())).andExpect(status().isOk());
        restDocumentTypeMockMvc.perform(get(ENTITY_API_URL + "/active")).andExpect(status().isOk());
    }

    protected long getRepositoryCount() {
        return documentTypeRepository.count();
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

    protected DocumentType getPersistedDocumentType(DocumentType documentType) {
        return documentTypeRepository.findById(documentType.getId()).orElseThrow();
    }

    protected void assertPersistedDocumentTypeToMatchAllProperties(DocumentType expectedDocumentType) {
        assertDocumentTypeAllPropertiesEquals(expectedDocumentType, getPersistedDocumentType(expectedDocumentType));
    }

    protected void assertPersistedDocumentTypeToMatchUpdatableProperties(DocumentType expectedDocumentType) {
        assertDocumentTypeAllUpdatablePropertiesEquals(expectedDocumentType, getPersistedDocumentType(expectedDocumentType));
    }
}
