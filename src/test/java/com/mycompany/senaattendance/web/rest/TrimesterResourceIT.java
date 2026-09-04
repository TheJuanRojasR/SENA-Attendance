package com.mycompany.senaattendance.web.rest;

import static com.mycompany.senaattendance.domain.TrimesterAsserts.*;
import static com.mycompany.senaattendance.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.repository.TrimesterRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.dto.TrimesterDTO;
import com.mycompany.senaattendance.service.mapper.TrimesterMapper;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
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
 * Integration tests for the {@link TrimesterResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TrimesterResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_START_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_START_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final LocalDate DEFAULT_END_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_END_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final Boolean DEFAULT_STATUS = Boolean.TRUE;
    private static final Boolean UPDATED_STATUS = Boolean.FALSE;

    private static final String ENTITY_API_URL = "/api/trimesters";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TrimesterRepository trimesterRepository;

    @Autowired
    private TrimesterMapper trimesterMapper;

    @Autowired
    private MockMvc restTrimesterMockMvc;

    private Trimester trimester;

    private Trimester insertedTrimester;

    private final List<Trimester> insertedTrimesters = new ArrayList<>();

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Trimester createEntity() {
        return new Trimester().name(DEFAULT_NAME).startDate(DEFAULT_START_DATE).endDate(DEFAULT_END_DATE).status(DEFAULT_STATUS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Trimester createUpdatedEntity() {
        return new Trimester().name(UPDATED_NAME).startDate(UPDATED_START_DATE).endDate(UPDATED_END_DATE).status(UPDATED_STATUS);
    }

    @BeforeEach
    void initTest() {
        trimester = createEntity();
    }

    @AfterEach
    void cleanup() {
        insertedTrimesters.forEach(trimesterRepository::delete);
        insertedTrimesters.clear();
        if (insertedTrimester != null) {
            trimesterRepository.delete(insertedTrimester);
            insertedTrimester = null;
        }
    }

    @Test
    void createTrimester() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Trimester
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(trimester);
        var returnedTrimesterDTO = om.readValue(
            restTrimesterMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(trimesterDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TrimesterDTO.class
        );

        // Validate the Trimester in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedTrimester = trimesterMapper.toEntity(returnedTrimesterDTO);
        assertTrimesterUpdatableFieldsEquals(returnedTrimester, getPersistedTrimester(returnedTrimester));

        insertedTrimester = returnedTrimester;
    }

    @Test
    void createTrimesterWithExistingId() throws Exception {
        // Create the Trimester with an existing ID
        trimester.setId("existing_id");
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(trimester);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTrimesterMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(trimesterDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Trimester in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        trimester.setName(null);

        // Create the Trimester, which fails.
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(trimester);

        restTrimesterMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(trimesterDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkStartDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        trimester.setStartDate(null);

        // Create the Trimester, which fails.
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(trimester);

        restTrimesterMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(trimesterDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkEndDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        trimester.setEndDate(null);

        // Create the Trimester, which fails.
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(trimester);

        restTrimesterMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(trimesterDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.COORDINATOR)
    void createTrimesterComputesStatusFromDates() throws Exception {
        // status is a server-computed field: a null status in the request must be accepted
        // and replaced by the value derived from today versus the [startDate, endDate] range.
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(new Trimester().name("Activo Hoy").startDate(today).endDate(today.plusDays(30)));

        var returnedTrimesterDTO = om.readValue(
            restTrimesterMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(trimesterDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TrimesterDTO.class
        );

        assertThat(returnedTrimesterDTO.getStatus()).isTrue();
        insertedTrimesters.add(trimesterMapper.toEntity(returnedTrimesterDTO));
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.COORDINATOR)
    void createTrimesterWithEqualDatesReturns400() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(new Trimester().name("Iguales").startDate(today).endDate(today));

        restTrimesterMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(trimesterDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("https://www.jhipster.tech/problem/trimester-dates-order"));
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.COORDINATOR)
    void createTrimesterWithReversedDatesReturns400() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(new Trimester().name("Invertidas").startDate(today.plusDays(10)).endDate(today));

        restTrimesterMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(trimesterDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("https://www.jhipster.tech/problem/trimester-dates-order"));
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.COORDINATOR)
    void createTrimesterWithOverlapReturns400() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        Trimester existing = new Trimester().name("Existente").startDate(today).endDate(today.plusDays(30)).status(true);
        insertedTrimesters.add(trimesterRepository.save(existing));

        // exact overlap: identical [startDate, endDate] range
        TrimesterDTO exact = trimesterMapper.toDto(new Trimester().name("Exacta").startDate(today).endDate(today.plusDays(30)));
        restTrimesterMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(exact)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("https://www.jhipster.tech/problem/trimester-dates-overlap"));

        // contained overlap: new range fully inside the existing range
        TrimesterDTO contained = trimesterMapper.toDto(
            new Trimester().name("Contenida").startDate(today.plusDays(5)).endDate(today.plusDays(10))
        );
        restTrimesterMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(contained)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("https://www.jhipster.tech/problem/trimester-dates-overlap"));
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.COORDINATOR)
    void createTrimesterAdjacentIsAllowed() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        Trimester existing = new Trimester().name("Existente").startDate(today).endDate(today.plusDays(30)).status(true);
        insertedTrimesters.add(trimesterRepository.save(existing));

        // the new range ends the day before the existing range starts: a single-boundary touch,
        // which is NOT an overlap and must be accepted.
        TrimesterDTO adjacent = trimesterMapper.toDto(
            new Trimester().name("Adyacente").startDate(today.minusDays(30)).endDate(today.minusDays(1))
        );

        var returnedTrimesterDTO = om.readValue(
            restTrimesterMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(adjacent)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TrimesterDTO.class
        );

        assertThat(returnedTrimesterDTO.getId()).isNotNull();
        insertedTrimesters.add(trimesterMapper.toEntity(returnedTrimesterDTO));
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.COORDINATOR)
    void createTrimesterFutureStartIsInactive() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(
            new Trimester().name("Futuro").startDate(today.plusDays(10)).endDate(today.plusDays(40))
        );

        var returnedTrimesterDTO = om.readValue(
            restTrimesterMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(trimesterDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TrimesterDTO.class
        );

        assertThat(returnedTrimesterDTO.getStatus()).isFalse();
        insertedTrimesters.add(trimesterMapper.toEntity(returnedTrimesterDTO));
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.COORDINATOR)
    void createTrimesterPastEndIsInactive() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(
            new Trimester().name("Pasado").startDate(today.minusDays(40)).endDate(today.minusDays(10))
        );

        var returnedTrimesterDTO = om.readValue(
            restTrimesterMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(trimesterDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TrimesterDTO.class
        );

        assertThat(returnedTrimesterDTO.getStatus()).isFalse();
        insertedTrimesters.add(trimesterMapper.toEntity(returnedTrimesterDTO));
    }

    @Test
    void getAllTrimesters() throws Exception {
        // Initialize the database
        insertedTrimester = trimesterRepository.save(trimester);

        // Get all the trimesterList
        restTrimesterMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "1"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(trimester.getId())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].startDate").value(hasItem(DEFAULT_START_DATE.toString())))
            .andExpect(jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)));
    }

    @Test
    void searchTrimestersByYear() throws Exception {
        insertedTrimesters.add(trimesterRepository.save(newTrimester("Primer Trimestre 2025", LocalDate.of(2025, 1, 1), true)));
        insertedTrimesters.add(trimesterRepository.save(newTrimester("Segundo Trimestre 2026", LocalDate.of(2026, 1, 1), false)));

        restTrimesterMockMvc
            .perform(get(ENTITY_API_URL + "/search").param("search", "2025"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(header().string("X-Total-Count", "1"))
            .andExpect(jsonPath("$.[*].name").value(hasItem("Primer Trimestre 2025")));
    }

    @Test
    void searchTrimestersByName() throws Exception {
        insertedTrimesters.add(trimesterRepository.save(newTrimester("Primer Trimestre", LocalDate.of(2025, 1, 1), true)));
        insertedTrimesters.add(trimesterRepository.save(newTrimester("Segundo Trimestre", LocalDate.of(2025, 6, 1), false)));

        restTrimesterMockMvc
            .perform(get(ENTITY_API_URL + "/search").param("search", "Primer"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(header().string("X-Total-Count", "1"))
            .andExpect(jsonPath("$.[*].name").value(hasItem("Primer Trimestre")));
    }

    @Test
    void searchTrimestersByStatus() throws Exception {
        insertedTrimesters.add(trimesterRepository.save(newTrimester("Primer Trimestre", LocalDate.of(2025, 1, 1), true)));
        insertedTrimesters.add(trimesterRepository.save(newTrimester("Segundo Trimestre", LocalDate.of(2025, 6, 1), false)));

        restTrimesterMockMvc
            .perform(get(ENTITY_API_URL + "/search").param("status", "true"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(header().string("X-Total-Count", "1"))
            .andExpect(jsonPath("$.[*].status").value(hasItem(true)));
    }

    @Test
    void searchTrimestersByStatusFalse() throws Exception {
        insertedTrimesters.add(trimesterRepository.save(newTrimester("Primer Trimestre", LocalDate.of(2025, 1, 1), true)));
        insertedTrimesters.add(trimesterRepository.save(newTrimester("Segundo Trimestre", LocalDate.of(2025, 6, 1), false)));

        restTrimesterMockMvc
            .perform(get(ENTITY_API_URL + "/search").param("status", "false"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(header().string("X-Total-Count", "1"))
            .andExpect(jsonPath("$.[*].status").value(hasItem(false)));
    }

    @Test
    void searchTrimestersCombined() throws Exception {
        insertedTrimesters.add(trimesterRepository.save(newTrimester("Primer Trimestre", LocalDate.of(2025, 1, 1), true)));
        insertedTrimesters.add(trimesterRepository.save(newTrimester("Primer Trimestre Inactivo", LocalDate.of(2025, 6, 1), false)));

        restTrimesterMockMvc
            .perform(
                get(ENTITY_API_URL + "/search")
                    .param("search", "Primer")
                    .param("status", "true")
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(header().string("X-Total-Count", "1"))
            .andExpect(jsonPath("$.[*].status").value(hasItem(true)));
    }

    @Test
    void searchTrimestersNoResults() throws Exception {
        insertedTrimesters.add(trimesterRepository.save(newTrimester("Primer Trimestre", LocalDate.of(2025, 1, 1), true)));

        restTrimesterMockMvc
            .perform(get(ENTITY_API_URL + "/search").param("search", "noexiste"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(header().string("X-Total-Count", "0"))
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void searchTrimestersCaseInsensitive() throws Exception {
        insertedTrimesters.add(trimesterRepository.save(newTrimester("Primer Trimestre", LocalDate.of(2025, 1, 1), true)));
        insertedTrimesters.add(trimesterRepository.save(newTrimester("Segundo Trimestre", LocalDate.of(2025, 6, 1), false)));

        // Stored name is capitalized; the search term is lowercase. The repository
        // regex uses $options: 'i' so the match must be case-insensitive.
        restTrimesterMockMvc
            .perform(get(ENTITY_API_URL + "/search").param("search", "primer"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(header().string("X-Total-Count", "1"))
            .andExpect(jsonPath("$.[*].name").value(hasItem("Primer Trimestre")));
    }

    @Test
    void getAllTrimestersEmpty() throws Exception {
        // No trimester is saved in this test: initTest() only creates an in-memory
        // object, and the Mongo collection is fresh per-test. The list must be empty.
        restTrimesterMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(header().string("X-Total-Count", "0"))
            .andExpect(jsonPath("$").isEmpty());
    }

    private Trimester newTrimester(String name, LocalDate startDate, Boolean status) {
        return new Trimester().name(name).startDate(startDate).endDate(startDate.plusYears(1)).status(status);
    }

    @Test
    void getTrimester() throws Exception {
        // Initialize the database
        insertedTrimester = trimesterRepository.save(trimester);

        // Get the trimester
        restTrimesterMockMvc
            .perform(get(ENTITY_API_URL_ID, trimester.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(trimester.getId()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.startDate").value(DEFAULT_START_DATE.toString()))
            .andExpect(jsonPath("$.endDate").value(DEFAULT_END_DATE.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS));
    }

    @Test
    void getNonExistingTrimester() throws Exception {
        // Get the trimester
        restTrimesterMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingTrimester() throws Exception {
        // Initialize the database
        insertedTrimester = trimesterRepository.save(trimester);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the trimester
        Trimester updatedTrimester = trimesterRepository.findById(trimester.getId()).orElseThrow();
        updatedTrimester.name(UPDATED_NAME).startDate(UPDATED_START_DATE).endDate(UPDATED_END_DATE).status(UPDATED_STATUS);
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(updatedTrimester);

        restTrimesterMockMvc
            .perform(
                put(ENTITY_API_URL_ID, trimesterDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(trimesterDTO))
            )
            .andExpect(status().isOk());

        // Validate the Trimester in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTrimesterToMatchAllProperties(updatedTrimester);
    }

    @Test
    void putNonExistingTrimester() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        trimester.setId(UUID.randomUUID().toString());

        // Create the Trimester
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(trimester);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTrimesterMockMvc
            .perform(
                put(ENTITY_API_URL_ID, trimesterDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(trimesterDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Trimester in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchTrimester() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        trimester.setId(UUID.randomUUID().toString());

        // Create the Trimester
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(trimester);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTrimesterMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(trimesterDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Trimester in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamTrimester() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        trimester.setId(UUID.randomUUID().toString());

        // Create the Trimester
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(trimester);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTrimesterMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(trimesterDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Trimester in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateTrimesterWithPatch() throws Exception {
        // Initialize the database
        insertedTrimester = trimesterRepository.save(trimester);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the trimester using partial update
        Trimester partialUpdatedTrimester = new Trimester();
        partialUpdatedTrimester.setId(trimester.getId());

        partialUpdatedTrimester.endDate(UPDATED_END_DATE).status(UPDATED_STATUS);

        restTrimesterMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTrimester.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTrimester))
            )
            .andExpect(status().isOk());

        // Validate the Trimester in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTrimesterUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedTrimester, trimester),
            getPersistedTrimester(trimester)
        );
    }

    @Test
    void fullUpdateTrimesterWithPatch() throws Exception {
        // Initialize the database
        insertedTrimester = trimesterRepository.save(trimester);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the trimester using partial update
        Trimester partialUpdatedTrimester = new Trimester();
        partialUpdatedTrimester.setId(trimester.getId());

        partialUpdatedTrimester.name(UPDATED_NAME).startDate(UPDATED_START_DATE).endDate(UPDATED_END_DATE).status(UPDATED_STATUS);

        restTrimesterMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTrimester.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTrimester))
            )
            .andExpect(status().isOk());

        // Validate the Trimester in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTrimesterUpdatableFieldsEquals(partialUpdatedTrimester, getPersistedTrimester(partialUpdatedTrimester));
    }

    @Test
    void patchNonExistingTrimester() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        trimester.setId(UUID.randomUUID().toString());

        // Create the Trimester
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(trimester);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTrimesterMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, trimesterDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(trimesterDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Trimester in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchTrimester() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        trimester.setId(UUID.randomUUID().toString());

        // Create the Trimester
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(trimester);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTrimesterMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(trimesterDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Trimester in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamTrimester() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        trimester.setId(UUID.randomUUID().toString());

        // Create the Trimester
        TrimesterDTO trimesterDTO = trimesterMapper.toDto(trimester);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTrimesterMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(trimesterDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Trimester in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteTrimester() throws Exception {
        // Initialize the database
        insertedTrimester = trimesterRepository.save(trimester);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the trimester
        restTrimesterMockMvc
            .perform(delete(ENTITY_API_URL_ID, trimester.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return trimesterRepository.count();
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

    protected Trimester getPersistedTrimester(Trimester trimester) {
        return trimesterRepository.findById(trimester.getId()).orElseThrow();
    }

    protected void assertPersistedTrimesterToMatchAllProperties(Trimester expectedTrimester) {
        assertTrimesterAllPropertiesEquals(expectedTrimester, getPersistedTrimester(expectedTrimester));
    }

    protected void assertPersistedTrimesterToMatchUpdatableProperties(Trimester expectedTrimester) {
        assertTrimesterAllUpdatablePropertiesEquals(expectedTrimester, getPersistedTrimester(expectedTrimester));
    }
}
