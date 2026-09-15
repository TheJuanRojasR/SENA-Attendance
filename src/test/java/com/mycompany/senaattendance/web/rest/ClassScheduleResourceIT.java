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
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.TimeSlot;
import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.domain.enumeration.DayOfWeek;
import com.mycompany.senaattendance.domain.enumeration.StateTrimester;
import com.mycompany.senaattendance.repository.ClassScheduleRepository;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.repository.TimeSlotRepository;
import com.mycompany.senaattendance.repository.TrimesterRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.ClassScheduleService;
import com.mycompany.senaattendance.service.dto.ClassScheduleDTO;
import com.mycompany.senaattendance.service.mapper.ClassScheduleMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class ClassScheduleResourceIT {

    private static final DateTimeFormatter LOCAL_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final DayOfWeek DEFAULT_DAY_OF_WEEK = DayOfWeek.LUNES;
    private static final DayOfWeek UPDATED_DAY_OF_WEEK = DayOfWeek.MARTES;

    // The ficha jornada seeded by TimeSlotResourceIT runs from NOON to 18:00, so every default
    // and updated schedule stays inside it.
    private static final LocalTime DEFAULT_START_TIME = LocalTime.NOON;
    private static final LocalTime DEFAULT_END_TIME = LocalTime.of(14, 0);

    private static final LocalTime UPDATED_START_TIME = LocalTime.of(12, 30);
    private static final LocalTime UPDATED_END_TIME = LocalTime.of(13, 30);

    // Date ranges anchored to the test day so the trimester classification is exercised by dates:
    // a closed trimester ends before today, an active one contains today and a future one starts
    // after today.
    private static final LocalDate TODAY = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate CLOSED_START_DATE = TODAY.minusDays(40);
    private static final LocalDate CLOSED_END_DATE = TODAY.minusDays(10);
    private static final LocalDate ACTIVE_START_DATE = TODAY.minusDays(10);
    private static final LocalDate ACTIVE_END_DATE = TODAY.plusDays(10);
    private static final LocalDate FUTURE_START_DATE = TODAY.plusDays(10);
    private static final LocalDate FUTURE_END_DATE = TODAY.plusDays(100);

    private static final String ENTITY_API_URL = "/api/class-schedules";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ClassScheduleRepository classScheduleRepository;

    @Autowired
    private ClassSectionRepository classSectionRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private TrimesterRepository trimesterRepository;

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

    private ClassSection insertedClassSection;

    private Grade insertedGrade;

    private TimeSlot insertedTimeSlot;

    private Trimester insertedTrimester;

    private final List<ClassSchedule> extraInsertedSchedules = new ArrayList<>();

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
        persistScheduleReferences();
    }

    @AfterEach
    void cleanup() {
        if (insertedClassSchedule != null) {
            classScheduleRepository.delete(insertedClassSchedule);
            insertedClassSchedule = null;
        }
        // Remove the schedules seeded by the overlap tests
        extraInsertedSchedules.forEach(classScheduleRepository::delete);
        extraInsertedSchedules.clear();
        // Remove the related documents persisted for the tests
        classSectionRepository.deleteAll();
        gradeRepository.deleteAll();
        trimesterRepository.deleteAll();
        timeSlotRepository.deleteAll();
    }

    /**
     * Persists the ficha chain (jornada, ficha, materia and trimester) the schedule references,
     * with ids generated by the database. This lets the service resolve the chain during
     * validation, mirroring the production flow.
     */
    private void persistScheduleReferences() {
        TimeSlot timeSlot = classSchedule.getClassSection().getGrade().getTimeSlot();
        timeSlot.setId(null);
        insertedTimeSlot = timeSlotRepository.save(timeSlot);

        Grade grade = classSchedule.getClassSection().getGrade();
        grade.setId(null);
        grade.setTimeSlot(insertedTimeSlot);
        insertedGrade = gradeRepository.save(grade);

        ClassSection classSection = classSchedule.getClassSection();
        classSection.setId(null);
        classSection.setGrade(insertedGrade);
        insertedClassSection = classSectionRepository.save(classSection);

        Trimester trimester = classSchedule.getTrimester();
        trimester.setId(null);
        insertedTrimester = trimesterRepository.save(trimester);

        classSchedule.setClassSection(insertedClassSection);
        classSchedule.setTrimester(insertedTrimester);
    }

    /**
     * Persists another subject inside the given ficha, bypassing the service so a test can
     * arrange the ficha subjects the overlap check must consider.
     *
     * @param subjectName the subject name to store.
     * @param grade the ficha the subject belongs to.
     * @return the persisted class section.
     */
    private ClassSection persistClassSectionInGrade(String subjectName, Grade grade) {
        return classSectionRepository.save(new ClassSection().subjectName(subjectName).isActive(true).grade(grade));
    }

    /**
     * Persists another ficha that shares the jornada under test.
     *
     * @return the persisted ficha.
     */
    private Grade persistGradeWithSharedJornada() {
        Grade grade = GradeResourceIT.createEntity();
        grade.setId(null);
        grade.setTimeSlot(insertedTimeSlot);
        return gradeRepository.save(grade);
    }

    /**
     * Persists another trimester under a distinct name.
     *
     * @param name the trimester name to store.
     * @return the persisted trimester.
     */
    private Trimester persistTrimester(String name) {
        Trimester trimester = TrimesterResourceIT.createEntity();
        trimester.setId(null);
        trimester.setName(name);
        return trimesterRepository.save(trimester);
    }

    /**
     * Persists a trimester with the given date range, so the schedule write rules can be exercised
     * against closed, active and future trimesters. The persisted status is left as the future one
     * of {@link TrimesterResourceIT#createEntity()} on purpose: the state must be classified from
     * the dates. The seeded trimester is removed by the trimester cleanup of {@link #cleanup()}.
     *
     * @param startDate the trimester start date (inclusive).
     * @param endDate the trimester end date (inclusive).
     * @return the persisted trimester.
     */
    private Trimester persistTrimester(LocalDate startDate, LocalDate endDate) {
        Trimester trimester = TrimesterResourceIT.createEntity();
        trimester.setId(null);
        trimester.setStartDate(startDate);
        trimester.setEndDate(endDate);
        return trimesterRepository.save(trimester);
    }

    /**
     * Persists a trimester whose date range classifies to the given state, used to exercise the
     * rule that a closed trimester freezes its schedules (E6).
     *
     * @param state the state the seeded date range must classify to.
     * @return the persisted trimester.
     */
    private Trimester persistTrimesterInState(StateTrimester state) {
        return switch (state) {
            case CERRADO -> persistTrimester(CLOSED_START_DATE, CLOSED_END_DATE);
            case ACTIVO -> persistTrimester(ACTIVE_START_DATE, ACTIVE_END_DATE);
            case FUTURO -> persistTrimester(FUTURE_START_DATE, FUTURE_END_DATE);
        };
    }

    /**
     * Seeds a schedule directly in the database, bypassing the service validation, so a test can
     * arrange an overlapping or adjacent session for the request under test.
     *
     * @param classSection the subject the seeded schedule belongs to.
     * @param trimester the trimester of the seeded schedule.
     * @param dayOfWeek the weekday of the seeded schedule.
     * @param startTime the start time of the seeded schedule.
     * @param endTime the end time of the seeded schedule.
     * @return the persisted schedule, registered for cleanup.
     */
    private ClassSchedule persistSchedule(
        ClassSection classSection,
        Trimester trimester,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
    ) {
        ClassSchedule persisted = classScheduleRepository.save(
            new ClassSchedule().dayOfWeek(dayOfWeek).startTime(startTime).endTime(endTime).classSection(classSection).trimester(trimester)
        );
        extraInsertedSchedules.add(persisted);
        return persisted;
    }

    /**
     * Posts the given payload expecting a 201 response and registers the created schedule for
     * cleanup.
     *
     * @param classScheduleDTO the payload to post.
     * @return the persisted schedule.
     */
    private ClassSchedule createScheduleExpectingCreated(ClassScheduleDTO classScheduleDTO) throws Exception {
        var returnedClassScheduleDTO = om.readValue(
            restClassScheduleMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classScheduleDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ClassScheduleDTO.class
        );
        insertedClassSchedule = classScheduleMapper.toEntity(returnedClassScheduleDTO);
        return insertedClassSchedule;
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
    void checkDayOfWeekIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        classSchedule.setDayOfWeek(null);

        // Create the ClassSchedule, which fails.
        ClassScheduleDTO classScheduleDTO = classScheduleMapper.toDto(classSchedule);

        restClassScheduleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classScheduleDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.validation"));

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void createClassScheduleWithEqualStartAndEndTimeReturnsBadRequest() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // An end time equal to the start time means the session does not advance in time
        classSchedule.setEndTime(DEFAULT_START_TIME);
        ClassScheduleDTO classScheduleDTO = classScheduleMapper.toDto(classSchedule);

        restClassScheduleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classScheduleDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.scheduleCrossesMidnight"));

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void createClassScheduleWithEndTimeBeforeStartTimeReturnsBadRequest() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // An end time before the start time would cross midnight
        classSchedule.startTime(DEFAULT_END_TIME).endTime(DEFAULT_START_TIME);
        ClassScheduleDTO classScheduleDTO = classScheduleMapper.toDto(classSchedule);

        restClassScheduleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classScheduleDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.scheduleCrossesMidnight"));

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void createClassScheduleStartingBeforeJornadaReturnsBadRequest() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // The seeded jornada starts at NOON, so one minute earlier is already outside it
        classSchedule.setStartTime(DEFAULT_START_TIME.minusMinutes(1));
        ClassScheduleDTO classScheduleDTO = classScheduleMapper.toDto(classSchedule);

        restClassScheduleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classScheduleDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.scheduleOutOfTimeSlot"));

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void createClassScheduleEndingAfterJornadaReturnsBadRequest() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // The seeded jornada ends at 18:00, so one minute later is already outside it
        classSchedule.setEndTime(LocalTime.of(18, 0).plusMinutes(1));
        ClassScheduleDTO classScheduleDTO = classScheduleMapper.toDto(classSchedule);

        restClassScheduleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classScheduleDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.scheduleOutOfTimeSlot"));

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void createClassScheduleOverlappingAnotherSubjectOfTheFichaReturnsBadRequest() throws Exception {
        // Another subject of the same ficha already occupies part of the same session
        ClassSection otherSection = persistClassSectionInGrade("Otra materia", insertedGrade);
        persistSchedule(otherSection, insertedTrimester, DEFAULT_DAY_OF_WEEK, LocalTime.of(13, 0), LocalTime.of(15, 0));
        long databaseSizeBeforeTest = getRepositoryCount();
        ClassScheduleDTO classScheduleDTO = classScheduleMapper.toDto(classSchedule);

        restClassScheduleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classScheduleDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.scheduleOverlap"));

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void createClassScheduleOverlappingTheSameSubjectReturnsBadRequest() throws Exception {
        // The schedule's own subject already has a session at the same time
        persistSchedule(insertedClassSection, insertedTrimester, DEFAULT_DAY_OF_WEEK, LocalTime.of(13, 0), LocalTime.of(15, 0));
        long databaseSizeBeforeTest = getRepositoryCount();
        ClassScheduleDTO classScheduleDTO = classScheduleMapper.toDto(classSchedule);

        restClassScheduleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classScheduleDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.scheduleOverlap"));

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void createClassScheduleAdjacentToAnotherSubjectSucceeds() throws Exception {
        // The seeded session ends exactly when the new one starts, which is not an overlap
        ClassSection otherSection = persistClassSectionInGrade("Otra materia", insertedGrade);
        persistSchedule(otherSection, insertedTrimester, DEFAULT_DAY_OF_WEEK, DEFAULT_START_TIME, DEFAULT_END_TIME);
        classSchedule.startTime(DEFAULT_END_TIME).endTime(LocalTime.of(16, 0));

        createScheduleExpectingCreated(classScheduleMapper.toDto(classSchedule));
    }

    @Test
    void createClassScheduleWithSameRangeInAnotherTrimesterSucceeds() throws Exception {
        // A conflicting session exists in the seeded trimester, but the new schedule belongs to another one
        ClassSection otherSection = persistClassSectionInGrade("Otra materia", insertedGrade);
        persistSchedule(otherSection, insertedTrimester, DEFAULT_DAY_OF_WEEK, DEFAULT_START_TIME, DEFAULT_END_TIME);
        classSchedule.setTrimester(persistTrimester("Otro trimestre"));

        createScheduleExpectingCreated(classScheduleMapper.toDto(classSchedule));
    }

    @Test
    void createClassScheduleWithSameRangeInAnotherFichaSucceeds() throws Exception {
        // A conflicting session exists in the ficha under test, but the new schedule belongs to another ficha
        ClassSection otherSection = persistClassSectionInGrade("Otra materia", insertedGrade);
        persistSchedule(otherSection, insertedTrimester, DEFAULT_DAY_OF_WEEK, DEFAULT_START_TIME, DEFAULT_END_TIME);
        Grade otherGrade = persistGradeWithSharedJornada();
        classSchedule.setClassSection(persistClassSectionInGrade("Materia de otra ficha", otherGrade));

        createScheduleExpectingCreated(classScheduleMapper.toDto(classSchedule));
    }

    @Test
    void createClassScheduleWithSameRangeOnAnotherDaySucceeds() throws Exception {
        // A conflicting session exists on LUNES, but the new schedule is on MARTES
        persistSchedule(insertedClassSection, insertedTrimester, DEFAULT_DAY_OF_WEEK, DEFAULT_START_TIME, DEFAULT_END_TIME);
        classSchedule.setDayOfWeek(UPDATED_DAY_OF_WEEK);

        createScheduleExpectingCreated(classScheduleMapper.toDto(classSchedule));
    }

    @Test
    void putClassScheduleKeepingItsOwnSlotSucceeds() throws Exception {
        insertedClassSchedule = classScheduleRepository.save(classSchedule);
        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Saving the schedule with the same slot must not collide with itself
        ClassScheduleDTO classScheduleDTO = classScheduleMapper.toDto(classSchedule);

        restClassScheduleMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classScheduleDTO)))
            .andExpect(status().isOk());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchClassScheduleKeepingItsOwnSlotSucceeds() throws Exception {
        insertedClassSchedule = classScheduleRepository.save(classSchedule);
        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Partially updating the schedule with its own slot must not collide with itself
        ClassSchedule partialUpdatedClassSchedule = new ClassSchedule();
        partialUpdatedClassSchedule.setId(classSchedule.getId());
        partialUpdatedClassSchedule.startTime(DEFAULT_START_TIME).endTime(DEFAULT_END_TIME);

        restClassScheduleMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(partialUpdatedClassSchedule))
            )
            .andExpect(status().isOk());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putClassScheduleOverlappingAnotherScheduleReturnsBadRequest() throws Exception {
        // The stored schedule is adjacent to the other one, so the update is what creates the overlap
        ClassSection otherSection = persistClassSectionInGrade("Otra materia", insertedGrade);
        persistSchedule(otherSection, insertedTrimester, DEFAULT_DAY_OF_WEEK, DEFAULT_END_TIME, LocalTime.of(16, 0));
        ClassSchedule stored = persistSchedule(
            insertedClassSection,
            insertedTrimester,
            DEFAULT_DAY_OF_WEEK,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME
        );
        long databaseSizeBeforeUpdate = getRepositoryCount();

        ClassScheduleDTO classScheduleDTO = classScheduleMapper.toDto(stored);
        classScheduleDTO.setStartTime(LocalTime.of(13, 0));
        classScheduleDTO.setEndTime(LocalTime.of(15, 0));

        restClassScheduleMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classScheduleDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.scheduleOverlap"));

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchClassScheduleOverlappingAnotherScheduleReturnsBadRequest() throws Exception {
        // The stored schedule is adjacent to the other one, so the update is what creates the overlap
        ClassSection otherSection = persistClassSectionInGrade("Otra materia", insertedGrade);
        persistSchedule(otherSection, insertedTrimester, DEFAULT_DAY_OF_WEEK, DEFAULT_END_TIME, LocalTime.of(16, 0));
        ClassSchedule stored = persistSchedule(
            insertedClassSection,
            insertedTrimester,
            DEFAULT_DAY_OF_WEEK,
            DEFAULT_START_TIME,
            DEFAULT_END_TIME
        );
        long databaseSizeBeforeUpdate = getRepositoryCount();

        ClassSchedule partialUpdatedClassSchedule = new ClassSchedule();
        partialUpdatedClassSchedule.setId(stored.getId());
        partialUpdatedClassSchedule.startTime(LocalTime.of(13, 0)).endTime(LocalTime.of(15, 0));

        restClassScheduleMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(partialUpdatedClassSchedule))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.scheduleOverlap"));

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    // -----------------------------------------------------------------
    // A closed trimester freezes its schedules (E6)
    // -----------------------------------------------------------------

    @Test
    void createClassScheduleInClosedTrimesterIsRejected() throws Exception {
        classSchedule.setTrimester(persistTrimesterInState(StateTrimester.CERRADO));
        long databaseSizeBeforeCreate = getRepositoryCount();

        // Creating a session in a closed trimester fails
        restClassScheduleMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(classScheduleMapper.toDto(classSchedule)))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.trimesterClosed"));

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void putClassScheduleInClosedTrimesterIsRejected() throws Exception {
        classSchedule.setTrimester(persistTrimesterInState(StateTrimester.CERRADO));
        insertedClassSchedule = classScheduleRepository.save(classSchedule);
        long databaseSizeBeforeUpdate = getRepositoryCount();

        ClassSchedule stored = classScheduleRepository.findById(classSchedule.getId()).orElseThrow();
        stored.dayOfWeek(UPDATED_DAY_OF_WEEK).startTime(UPDATED_START_TIME).endTime(UPDATED_END_TIME);
        ClassScheduleDTO classScheduleDTO = classScheduleMapper.toDto(stored);

        // Modifying a session of a closed trimester fails
        restClassScheduleMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classScheduleDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.trimesterClosed"));

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        ClassSchedule persisted = getPersistedClassSchedule(stored);
        assertThat(persisted.getDayOfWeek()).isEqualTo(DEFAULT_DAY_OF_WEEK);
        assertThat(persisted.getStartTime()).isEqualTo(DEFAULT_START_TIME);
        assertThat(persisted.getEndTime()).isEqualTo(DEFAULT_END_TIME);
    }

    @Test
    void patchClassScheduleInClosedTrimesterIsRejected() throws Exception {
        classSchedule.setTrimester(persistTrimesterInState(StateTrimester.CERRADO));
        insertedClassSchedule = classScheduleRepository.save(classSchedule);
        long databaseSizeBeforeUpdate = getRepositoryCount();

        ClassSchedule partialUpdatedClassSchedule = new ClassSchedule();
        partialUpdatedClassSchedule.setId(classSchedule.getId());
        partialUpdatedClassSchedule.dayOfWeek(UPDATED_DAY_OF_WEEK);

        // Partially modifying a session of a closed trimester fails
        restClassScheduleMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(partialUpdatedClassSchedule))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.trimesterClosed"));

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertThat(getPersistedClassSchedule(classSchedule).getDayOfWeek()).isEqualTo(DEFAULT_DAY_OF_WEEK);
    }

    @Test
    void deleteClassScheduleInClosedTrimesterIsRejected() throws Exception {
        classSchedule.setTrimester(persistTrimesterInState(StateTrimester.CERRADO));
        insertedClassSchedule = classScheduleRepository.save(classSchedule);
        long databaseSizeBeforeDelete = getRepositoryCount();

        // Deleting a session of a closed trimester fails
        restClassScheduleMockMvc
            .perform(delete(ENTITY_API_URL_ID, classSchedule.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.trimesterClosed"));

        assertSameRepositoryCount(databaseSizeBeforeDelete);
    }

    @ParameterizedTest
    @EnumSource(value = StateTrimester.class, names = { "FUTURO", "ACTIVO" })
    void createClassScheduleInOpenTrimesterSucceeds(StateTrimester state) throws Exception {
        classSchedule.setTrimester(persistTrimesterInState(state));

        // Creating a session in a future or active trimester keeps working
        createScheduleExpectingCreated(classScheduleMapper.toDto(classSchedule));
    }

    @ParameterizedTest
    @EnumSource(value = StateTrimester.class, names = { "FUTURO", "ACTIVO" })
    void putClassScheduleInOpenTrimesterSucceeds(StateTrimester state) throws Exception {
        classSchedule.setTrimester(persistTrimesterInState(state));
        insertedClassSchedule = classScheduleRepository.save(classSchedule);

        ClassSchedule updatedClassSchedule = classScheduleRepository.findById(classSchedule.getId()).orElseThrow();
        updatedClassSchedule.dayOfWeek(UPDATED_DAY_OF_WEEK).startTime(UPDATED_START_TIME).endTime(UPDATED_END_TIME);

        // Modifying a session of a future or active trimester keeps working
        restClassScheduleMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(classScheduleMapper.toDto(updatedClassSchedule)))
            )
            .andExpect(status().isOk());

        assertPersistedClassScheduleToMatchAllProperties(updatedClassSchedule);
    }

    @ParameterizedTest
    @EnumSource(value = StateTrimester.class, names = { "FUTURO", "ACTIVO" })
    void patchClassScheduleInOpenTrimesterSucceeds(StateTrimester state) throws Exception {
        classSchedule.setTrimester(persistTrimesterInState(state));
        insertedClassSchedule = classScheduleRepository.save(classSchedule);

        ClassSchedule partialUpdatedClassSchedule = new ClassSchedule();
        partialUpdatedClassSchedule.setId(classSchedule.getId());
        partialUpdatedClassSchedule.dayOfWeek(UPDATED_DAY_OF_WEEK);

        // Partially modifying a session of a future or active trimester keeps working
        restClassScheduleMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(partialUpdatedClassSchedule))
            )
            .andExpect(status().isOk());

        assertThat(getPersistedClassSchedule(classSchedule).getDayOfWeek()).isEqualTo(UPDATED_DAY_OF_WEEK);
    }

    @ParameterizedTest
    @EnumSource(value = StateTrimester.class, names = { "FUTURO", "ACTIVO" })
    void deleteClassScheduleInOpenTrimesterSucceeds(StateTrimester state) throws Exception {
        classSchedule.setTrimester(persistTrimesterInState(state));
        insertedClassSchedule = classScheduleRepository.save(classSchedule);
        long databaseSizeBeforeDelete = getRepositoryCount();

        // Deleting a session of a future or active trimester keeps working
        restClassScheduleMockMvc
            .perform(delete(ENTITY_API_URL_ID, classSchedule.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
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
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classScheduleDTO)))
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
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classScheduleDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ClassSchedule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putClassScheduleWithoutIdReturnsBadRequest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classSchedule.setId(null);

        ClassScheduleDTO classScheduleDTO = classScheduleMapper.toDto(classSchedule);

        restClassScheduleMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classScheduleDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.idnull"));

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
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(partialUpdatedClassSchedule))
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
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(partialUpdatedClassSchedule))
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
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(classScheduleDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ClassSchedule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchClassScheduleWithoutIdReturnsBadRequest() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        classSchedule.setId(null);

        ClassScheduleDTO classScheduleDTO = classScheduleMapper.toDto(classSchedule);

        restClassScheduleMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(classScheduleDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.idnull"));

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

    // -----------------------------------------------------------------
    // Authorization: writes are restricted to admins
    // -----------------------------------------------------------------

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.COORDINATOR)
    void createClassScheduleAsNonAdminReturnsForbidden() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        ClassScheduleDTO classScheduleDTO = classScheduleMapper.toDto(classSchedule);

        restClassScheduleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classScheduleDTO)))
            .andExpect(status().isForbidden());

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.COORDINATOR)
    void updateClassScheduleAsNonAdminReturnsForbidden() throws Exception {
        insertedClassSchedule = classScheduleRepository.save(classSchedule);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        ClassScheduleDTO classScheduleDTO = classScheduleMapper.toDto(insertedClassSchedule);

        restClassScheduleMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(classScheduleDTO)))
            .andExpect(status().isForbidden());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.COORDINATOR)
    void partialUpdateClassScheduleAsNonAdminReturnsForbidden() throws Exception {
        insertedClassSchedule = classScheduleRepository.save(classSchedule);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        ClassScheduleDTO classScheduleDTO = classScheduleMapper.toDto(insertedClassSchedule);

        restClassScheduleMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(classScheduleDTO)))
            .andExpect(status().isForbidden());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.COORDINATOR)
    void deleteClassScheduleAsNonAdminReturnsForbidden() throws Exception {
        insertedClassSchedule = classScheduleRepository.save(classSchedule);

        long databaseSizeBeforeDelete = getRepositoryCount();

        restClassScheduleMockMvc
            .perform(delete(ENTITY_API_URL_ID, insertedClassSchedule.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());

        assertSameRepositoryCount(databaseSizeBeforeDelete);
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
