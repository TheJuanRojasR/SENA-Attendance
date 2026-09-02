package com.mycompany.senaattendance.web.rest;

import static com.mycompany.senaattendance.domain.ProgramAsserts.*;
import static com.mycompany.senaattendance.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.Program;
import com.mycompany.senaattendance.repository.ProgramRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.dto.ProgramDTO;
import com.mycompany.senaattendance.service.mapper.ProgramMapper;
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
 * Integration tests for the {@link ProgramResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class ProgramResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_INITIALS = "AAAAAAAAAA";
    private static final String UPDATED_INITIALS = "BBBBBBBBBB";

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBB";

    private static final Integer DEFAULT_TRIMESTERS = 1;
    private static final Integer UPDATED_TRIMESTERS = 2;

    private static final Boolean DEFAULT_STATUS = true;
    private static final Boolean UPDATED_STATUS = false;

    private static final String ENTITY_API_URL = "/api/programs";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private ProgramMapper programMapper;

    @Autowired
    private MockMvc restProgramMockMvc;

    private Program program;

    private Program insertedProgram;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Program createEntity() {
        return new Program()
            .name(DEFAULT_NAME)
            .initials(DEFAULT_INITIALS)
            .code(DEFAULT_CODE)
            .trimesters(DEFAULT_TRIMESTERS)
            .status(DEFAULT_STATUS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Program createUpdatedEntity() {
        return new Program()
            .name(UPDATED_NAME)
            .initials(UPDATED_INITIALS)
            .code(UPDATED_CODE)
            .trimesters(UPDATED_TRIMESTERS)
            .status(UPDATED_STATUS);
    }

    @BeforeEach
    void initTest() {
        program = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedProgram != null) {
            programRepository.delete(insertedProgram);
            insertedProgram = null;
        }
    }

    @Test
    void createProgram() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Program
        ProgramDTO programDTO = programMapper.toDto(program);
        var returnedProgramDTO = om.readValue(
            restProgramMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(programDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ProgramDTO.class
        );

        // Validate the Program in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedProgram = programMapper.toEntity(returnedProgramDTO);
        assertProgramUpdatableFieldsEquals(returnedProgram, getPersistedProgram(returnedProgram));

        insertedProgram = returnedProgram;
    }

    @Test
    void createProgramWithExistingId() throws Exception {
        // Create the Program with an existing ID
        program.setId("existing_id");
        ProgramDTO programDTO = programMapper.toDto(program);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restProgramMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(programDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Program in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        program.setName(null);

        // Create the Program, which fails.
        ProgramDTO programDTO = programMapper.toDto(program);

        restProgramMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(programDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkInitialsIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        program.setInitials(null);

        // Create the Program, which fails.
        ProgramDTO programDTO = programMapper.toDto(program);

        restProgramMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(programDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkCodeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        program.setCode(null);

        // Create the Program, which fails.
        ProgramDTO programDTO = programMapper.toDto(program);

        restProgramMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(programDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkTrimestersIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        program.setTrimesters(null);

        // Create the Program, which fails.
        ProgramDTO programDTO = programMapper.toDto(program);

        restProgramMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(programDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void createProgramWithoutStatusDefaultsToTrue() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();

        // Create the Program without sending status (API tolerante)
        ProgramDTO programDTO = new ProgramDTO();
        programDTO.setName(DEFAULT_NAME);
        programDTO.setInitials(DEFAULT_INITIALS);
        programDTO.setCode(DEFAULT_CODE);
        programDTO.setTrimesters(DEFAULT_TRIMESTERS);

        var returnedProgramDTO = om.readValue(
            restProgramMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(programDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ProgramDTO.class
        );

        // Validate the Program in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedProgram = programMapper.toEntity(returnedProgramDTO);
        assertThat(returnedProgram.getStatus()).as("default status is true").isTrue();

        insertedProgram = returnedProgram;
    }

    @Test
    void createProgramWithDuplicateCodeReturnsBadRequest() throws Exception {
        // Persist a program with DEFAULT_CODE so the upcoming POST collides on code only
        programRepository.save(program);
        insertedProgram = program;
        long databaseSizeBeforeCreate = getRepositoryCount();

        ProgramDTO programDTO = buildProgramDTO("ZZZZZZZZZZ", "ZZZZZZZZZZ", DEFAULT_CODE, DEFAULT_TRIMESTERS);

        restProgramMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(programDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.codeexists"));

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void createProgramWithDuplicateInitialsReturnsBadRequest() throws Exception {
        // Persist a program with DEFAULT_INITIALS so the upcoming POST collides on initials only
        programRepository.save(program);
        insertedProgram = program;
        long databaseSizeBeforeCreate = getRepositoryCount();

        ProgramDTO programDTO = buildProgramDTO("ZZZZZZZZZZ", DEFAULT_INITIALS, "ZZZZZZZZZZ", DEFAULT_TRIMESTERS);

        restProgramMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(programDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.initialsexists"));

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void createProgramWithDuplicateNameReturnsBadRequest() throws Exception {
        // Persist a program with DEFAULT_NAME so the upcoming POST collides on name only
        programRepository.save(program);
        insertedProgram = program;
        long databaseSizeBeforeCreate = getRepositoryCount();

        ProgramDTO programDTO = buildProgramDTO(DEFAULT_NAME, "ZZZZZZZZZZ", "ZZZZZZZZZZ", DEFAULT_TRIMESTERS);

        restProgramMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(programDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.nameexists"));

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void createProgramWithTrimestersOutOfRangeReturnsBadRequest() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();

        // trimesters above the allowed maximum and below the minimum are both rejected
        restProgramMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(buildProgramDTO("NAME_13", "INI_13", "CODE_13", 13)))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors").isArray())
            .andExpect(jsonPath("$.fieldErrors[0].field").value("trimesters"));

        restProgramMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(buildProgramDTO("NAME_0", "INI_0", "CODE_0", 0)))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors").isArray())
            .andExpect(jsonPath("$.fieldErrors[0].field").value("trimesters"));

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void createProgramWithMaxTrimestersCreates() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();

        ProgramDTO programDTO = buildProgramDTO("NAME_12", "INI_12", "CODE_12", 12);

        var returnedProgramDTO = om.readValue(
            restProgramMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(programDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ProgramDTO.class
        );

        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        insertedProgram = programMapper.toEntity(returnedProgramDTO);
    }

    @Test
    void getAllPrograms() throws Exception {
        // Initialize the database
        insertedProgram = programRepository.save(program);

        // Get all the programList
        restProgramMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(program.getId())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].initials").value(hasItem(DEFAULT_INITIALS)))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].trimesters").value(hasItem(DEFAULT_TRIMESTERS)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)));
    }

    @Test
    void searchProgramsByCode() throws Exception {
        insertedProgram = programRepository.save(program);

        restProgramMockMvc
            .perform(get(ENTITY_API_URL + "/search").param("search", DEFAULT_CODE))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    @Test
    void searchProgramsByName() throws Exception {
        insertedProgram = programRepository.save(program);

        restProgramMockMvc
            .perform(get(ENTITY_API_URL + "/search").param("search", DEFAULT_NAME))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    @Test
    void searchProgramsByStatus() throws Exception {
        insertedProgram = programRepository.save(program);

        restProgramMockMvc
            .perform(get(ENTITY_API_URL + "/search").param("status", DEFAULT_STATUS.toString()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)));
    }

    @Test
    void searchProgramsEmptySearchReturnsAll() throws Exception {
        insertedProgram = programRepository.save(program);

        restProgramMockMvc
            .perform(get(ENTITY_API_URL + "/search"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    @Test
    void getProgram() throws Exception {
        // Initialize the database
        insertedProgram = programRepository.save(program);

        // Get the program
        restProgramMockMvc
            .perform(get(ENTITY_API_URL_ID, program.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(program.getId()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.initials").value(DEFAULT_INITIALS))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE))
            .andExpect(jsonPath("$.trimesters").value(DEFAULT_TRIMESTERS))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS));
    }

    @Test
    void getNonExistingProgram() throws Exception {
        // Get the program
        restProgramMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingProgram() throws Exception {
        // Initialize the database
        insertedProgram = programRepository.save(program);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the program
        Program updatedProgram = programRepository.findById(program.getId()).orElseThrow();
        updatedProgram
            .name(UPDATED_NAME)
            .initials(UPDATED_INITIALS)
            .code(UPDATED_CODE)
            .trimesters(UPDATED_TRIMESTERS)
            .status(UPDATED_STATUS);
        ProgramDTO programDTO = programMapper.toDto(updatedProgram);

        restProgramMockMvc
            .perform(
                put(ENTITY_API_URL_ID, programDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(programDTO))
            )
            .andExpect(status().isOk());

        // Validate the Program in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedProgramToMatchAllProperties(updatedProgram);
    }

    @Test
    void putNonExistingProgram() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        program.setId(UUID.randomUUID().toString());

        // Create the Program
        ProgramDTO programDTO = programMapper.toDto(program);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProgramMockMvc
            .perform(
                put(ENTITY_API_URL_ID, programDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(programDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Program in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchProgram() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        program.setId(UUID.randomUUID().toString());

        // Create the Program
        ProgramDTO programDTO = programMapper.toDto(program);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProgramMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(programDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Program in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamProgram() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        program.setId(UUID.randomUUID().toString());

        // Create the Program
        ProgramDTO programDTO = programMapper.toDto(program);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProgramMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(programDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Program in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateProgramWithPatch() throws Exception {
        // Initialize the database
        insertedProgram = programRepository.save(program);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the program using partial update
        Program partialUpdatedProgram = new Program();
        partialUpdatedProgram.setId(program.getId());

        partialUpdatedProgram.name(UPDATED_NAME).initials(UPDATED_INITIALS);

        restProgramMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(partialUpdatedProgram)))
            .andExpect(status().isOk());

        // Validate the Program in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertProgramUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedProgram, program), getPersistedProgram(program));
    }

    @Test
    void fullUpdateProgramWithPatch() throws Exception {
        // Initialize the database
        insertedProgram = programRepository.save(program);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the program using partial update
        Program partialUpdatedProgram = new Program();
        partialUpdatedProgram.setId(program.getId());

        partialUpdatedProgram
            .name(UPDATED_NAME)
            .initials(UPDATED_INITIALS)
            .code(UPDATED_CODE)
            .trimesters(UPDATED_TRIMESTERS)
            .status(UPDATED_STATUS);

        restProgramMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(partialUpdatedProgram)))
            .andExpect(status().isOk());

        // Validate the Program in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertProgramUpdatableFieldsEquals(partialUpdatedProgram, getPersistedProgram(partialUpdatedProgram));
    }

    @Test
    void patchProgramWithDuplicateCodeReturnsBadRequest() throws Exception {
        // A: already persisted with DEFAULT_CODE (the value we try to steal) -> cleaned by @AfterEach
        insertedProgram = programRepository.save(program);

        // B: the program we PATCH, trying to take A's code
        Program other = createEntity().name("OTHER_NAME").initials("OTHERIN").code("OTHERCODE").status(true);
        other = programRepository.save(other);

        try {
            ProgramDTO patchDto = new ProgramDTO();
            patchDto.setId(other.getId());
            patchDto.setCode(DEFAULT_CODE);
            patchDto.setInitials(other.getInitials());
            patchDto.setName(other.getName());

            restProgramMockMvc
                .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(patchDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error.codeexists"));

            assertThat(getPersistedProgram(other).getCode()).as("code unchanged after rejected patch").isEqualTo("OTHERCODE");
        } finally {
            programRepository.delete(other);
        }
    }

    @Test
    void patchProgramWithDuplicateInitialsReturnsBadRequest() throws Exception {
        insertedProgram = programRepository.save(program);

        Program other = createEntity().name("OTHER_NAME").initials("OTHERIN").code("OTHERCODE").status(true);
        other = programRepository.save(other);

        try {
            ProgramDTO patchDto = new ProgramDTO();
            patchDto.setId(other.getId());
            patchDto.setInitials(DEFAULT_INITIALS);
            patchDto.setCode(other.getCode());
            patchDto.setName(other.getName());

            restProgramMockMvc
                .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(patchDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error.initialsexists"));

            assertThat(getPersistedProgram(other).getInitials()).as("initials unchanged after rejected patch").isEqualTo("OTHERIN");
        } finally {
            programRepository.delete(other);
        }
    }

    @Test
    void patchProgramWithDuplicateNameReturnsBadRequest() throws Exception {
        insertedProgram = programRepository.save(program);

        Program other = createEntity().name("OTHER_NAME").initials("OTHERIN").code("OTHERCODE").status(true);
        other = programRepository.save(other);

        try {
            ProgramDTO patchDto = new ProgramDTO();
            patchDto.setId(other.getId());
            patchDto.setName(DEFAULT_NAME);
            patchDto.setCode(other.getCode());
            patchDto.setInitials(other.getInitials());

            restProgramMockMvc
                .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(patchDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error.nameexists"));

            assertThat(getPersistedProgram(other).getName()).as("name unchanged after rejected patch").isEqualTo("OTHER_NAME");
        } finally {
            programRepository.delete(other);
        }
    }

    @Test
    void patchProgramKeepingOwnUniqueValuesSucceeds() throws Exception {
        // Patching the SAME program keeping its own code/initials/name must NOT self-collide
        insertedProgram = programRepository.save(program);

        ProgramDTO patchDto = new ProgramDTO();
        patchDto.setId(program.getId());
        patchDto.setCode(program.getCode());
        patchDto.setInitials(program.getInitials());
        patchDto.setName(program.getName());

        restProgramMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(patchDto)))
            .andExpect(status().isOk());

        assertThat(getPersistedProgram(program).getCode()).isEqualTo(program.getCode());
    }

    @Test
    void patchProgramWithOnlyIdLeavesValuesUnchanged() throws Exception {
        insertedProgram = programRepository.save(program);

        ProgramDTO returnedProgram = om.readValue(
            restProgramMockMvc
                .perform(
                    patch(ENTITY_API_URL)
                        .contentType("application/merge-patch+json")
                        .content(om.writeValueAsBytes(om.createObjectNode().put("id", program.getId())))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ProgramDTO.class
        );

        assertThat(returnedProgram.getName()).isEqualTo(program.getName());
        assertThat(returnedProgram.getInitials()).isEqualTo(program.getInitials());
        assertThat(returnedProgram.getCode()).isEqualTo(program.getCode());
        assertThat(returnedProgram.getTrimesters()).isEqualTo(program.getTrimesters());
        assertThat(returnedProgram.getStatus()).isEqualTo(program.getStatus());
        assertProgramUpdatableFieldsEquals(program, getPersistedProgram(program));
    }

    @Test
    void patchProgramWithBlankValuesLeavesValuesUnchanged() throws Exception {
        insertedProgram = programRepository.save(program);

        var patchBody = om.createObjectNode().put("id", program.getId()).put("name", "").put("initials", "   ").put("code", " \t ");

        ProgramDTO returnedProgram = om.readValue(
            restProgramMockMvc
                .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(patchBody)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ProgramDTO.class
        );

        assertThat(returnedProgram.getName()).isEqualTo(program.getName());
        assertThat(returnedProgram.getInitials()).isEqualTo(program.getInitials());
        assertThat(returnedProgram.getCode()).isEqualTo(program.getCode());
        assertThat(returnedProgram.getTrimesters()).isEqualTo(program.getTrimesters());
        assertThat(returnedProgram.getStatus()).isEqualTo(program.getStatus());
        assertProgramUpdatableFieldsEquals(program, getPersistedProgram(program));
    }

    @Test
    void patchNonExistingProgram() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        program.setId(UUID.randomUUID().toString());

        // Create the Program
        ProgramDTO programDTO = programMapper.toDto(program);

        // If the body ID does not identify an existing program, it will throw BadRequestAlertException
        restProgramMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(programDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Program in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingBodyIdProgram() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();

        ProgramDTO programDTO = programMapper.toDto(program);

        // The resource identity must be supplied in the request body.
        restProgramMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(programDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.idnull"));

        // Validate the Program in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteProgram() throws Exception {
        // Initialize the database
        insertedProgram = programRepository.save(program);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the program
        restProgramMockMvc
            .perform(delete(ENTITY_API_URL_ID, program.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return programRepository.count();
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

    protected Program getPersistedProgram(Program program) {
        return programRepository.findById(program.getId()).orElseThrow();
    }

    protected void assertPersistedProgramToMatchAllProperties(Program expectedProgram) {
        assertProgramAllPropertiesEquals(expectedProgram, getPersistedProgram(expectedProgram));
    }

    protected void assertPersistedProgramToMatchUpdatableProperties(Program expectedProgram) {
        assertProgramAllUpdatablePropertiesEquals(expectedProgram, getPersistedProgram(expectedProgram));
    }

    private ProgramDTO buildProgramDTO(String name, String initials, String code, Integer trimesters) {
        ProgramDTO dto = new ProgramDTO();
        dto.setName(name);
        dto.setInitials(initials);
        dto.setCode(code);
        dto.setTrimesters(trimesters);
        return dto;
    }
}
