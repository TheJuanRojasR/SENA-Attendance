package com.mycompany.senaattendance.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycompany.senaattendance.domain.Apprentice;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.Modality;
import com.mycompany.senaattendance.domain.Program;
import com.mycompany.senaattendance.domain.TimeSlot;
import com.mycompany.senaattendance.domain.enumeration.StateGrade;
import com.mycompany.senaattendance.repository.ApprenticeRepository;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.repository.ModalityRepository;
import com.mycompany.senaattendance.repository.ProgramRepository;
import com.mycompany.senaattendance.repository.TimeSlotRepository;
import com.mycompany.senaattendance.service.dto.GradeDTO;
import com.mycompany.senaattendance.service.dto.ModalityDTO;
import com.mycompany.senaattendance.service.dto.ProgramDTO;
import com.mycompany.senaattendance.service.dto.TimeSlotDTO;
import com.mycompany.senaattendance.service.mapper.GradeMapper;
import com.mycompany.senaattendance.web.rest.errors.BadRequestAlertException;
import com.mycompany.senaattendance.web.rest.errors.GradeCodeAlreadyUsedException;
import com.mycompany.senaattendance.web.rest.errors.GradeDatesOrderException;
import com.mycompany.senaattendance.web.rest.errors.GradeStartDateInPastException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for the date-driven ficha state lifecycle in {@link GradeServiceImpl}: state
 * computation on creation, preservation of manual states on edition, the daily
 * {@code syncStates()} job, the numeric/unique rules of the ficha code, the per-state edit
 * rules, the code lock, the date-range rules and the active catalog checks.
 */
@ExtendWith(MockitoExtension.class)
class GradeServiceImplTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 3, 10);

    private static final String DEFAULT_CODE = "1234567890";

    private static final String UPDATED_CODE = "9876543210";

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private GradeMapper gradeMapper;

    @Mock
    private ProgramRepository programRepository;

    @Mock
    private ModalityRepository modalityRepository;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private ClassSectionRepository classSectionRepository;

    @Mock
    private ApprenticeRepository apprenticeRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private GradeServiceImpl gradeService;

    // -----------------------------------------------------------------
    // save() state computation
    // -----------------------------------------------------------------

    @Test
    void saveComputesPendienteWhenStartIsInFutureAndIgnoresClientState() {
        mockClockAt(TODAY);
        Grade grade = grade("g-1", StateGrade.CANCELADA, TODAY.plusDays(10), TODAY.plusDays(40));
        GradeDTO dto = toDto(grade);
        when(gradeMapper.toEntity(dto)).thenReturn(grade);
        when(gradeRepository.save(grade)).thenReturn(grade);
        when(gradeMapper.toDto(grade)).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        GradeDTO result = gradeService.save(dto);

        assertThat(result.getState()).isEqualTo(StateGrade.PENDIENTE);
        assertThat(grade.getState()).isEqualTo(StateGrade.PENDIENTE);
        verify(gradeRepository).save(grade);
    }

    @Test
    void saveComputesActivaWhenTodayIsWithinTheRange() {
        mockClockAt(TODAY);
        Grade grade = grade("g-1", StateGrade.CANCELADA, TODAY, TODAY.plusDays(30));
        GradeDTO dto = toDto(grade);
        when(gradeMapper.toEntity(dto)).thenReturn(grade);
        when(gradeRepository.save(grade)).thenReturn(grade);
        when(gradeMapper.toDto(grade)).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        GradeDTO result = gradeService.save(dto);

        assertThat(result.getState()).isEqualTo(StateGrade.ACTIVA);
        assertThat(grade.getState()).isEqualTo(StateGrade.ACTIVA);
    }

    @Test
    void updateRecomputesFinalizadaWhenRangeIsInThePast() {
        mockClockAt(TODAY);
        // A ficha that already started keeps its past start date when other fields change;
        // its automatic state is re-derived from the resulting range.
        Grade existing = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(40), TODAY.minusDays(10));
        Grade incoming = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(40), TODAY.minusDays(10));
        GradeDTO dto = toDto(incoming);
        when(gradeMapper.toEntity(dto)).thenReturn(incoming);
        when(gradeRepository.findById("g-1")).thenReturn(Optional.of(existing));
        when(gradeRepository.save(incoming)).thenReturn(incoming);
        when(gradeMapper.toDto(incoming)).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        GradeDTO result = gradeService.update(dto);

        assertThat(result.getState()).isEqualTo(StateGrade.FINALIZADA);
        assertThat(incoming.getState()).isEqualTo(StateGrade.FINALIZADA);
    }

    // -----------------------------------------------------------------
    // update() manual state preservation
    // -----------------------------------------------------------------

    @Test
    void updatePreservesAplazadaManualState() {
        mockClockAt(TODAY);
        // A paused ficha only accepts end date changes, so its manual state must survive them.
        Grade existing = grade("g-1", StateGrade.APLAZADA, TODAY.minusDays(10), TODAY.plusDays(10));
        Grade incoming = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(30));
        GradeDTO dto = toDto(incoming);
        when(gradeMapper.toEntity(dto)).thenReturn(incoming);
        when(gradeRepository.findById("g-1")).thenReturn(Optional.of(existing));
        when(gradeRepository.save(incoming)).thenReturn(incoming);
        when(gradeMapper.toDto(incoming)).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        GradeDTO result = gradeService.update(dto);

        assertThat(result.getState()).isEqualTo(StateGrade.APLAZADA);
        assertThat(incoming.getState()).isEqualTo(StateGrade.APLAZADA);
    }

    @Test
    void updatePreservesCanceladaManualState() {
        mockClockAt(TODAY);
        Grade existing = grade("g-1", StateGrade.CANCELADA, TODAY.minusDays(10), TODAY.plusDays(10));
        Grade incoming = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(10));
        GradeDTO dto = toDto(incoming);
        when(gradeMapper.toEntity(dto)).thenReturn(incoming);
        when(gradeRepository.findById("g-1")).thenReturn(Optional.of(existing));
        when(gradeRepository.save(incoming)).thenReturn(incoming);
        when(gradeMapper.toDto(incoming)).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        GradeDTO result = gradeService.update(dto);

        assertThat(result.getState()).isEqualTo(StateGrade.CANCELADA);
        assertThat(incoming.getState()).isEqualTo(StateGrade.CANCELADA);
    }

    @Test
    void updateRecomputesStateWhenCurrentIsAutomatic() {
        mockClockAt(TODAY);
        // A PENDIENTE ficha accepts date changes, so the automatic state is re-derived.
        Grade existing = grade("g-1", StateGrade.PENDIENTE, TODAY.plusDays(10), TODAY.plusDays(40));
        Grade incoming = grade("g-1", StateGrade.CANCELADA, TODAY, TODAY.plusDays(30));
        GradeDTO dto = toDto(incoming);
        when(gradeMapper.toEntity(dto)).thenReturn(incoming);
        when(gradeRepository.findById("g-1")).thenReturn(Optional.of(existing));
        when(gradeRepository.save(incoming)).thenReturn(incoming);
        when(gradeMapper.toDto(incoming)).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        GradeDTO result = gradeService.update(dto);

        assertThat(result.getState()).isEqualTo(StateGrade.ACTIVA);
        assertThat(incoming.getState()).isEqualTo(StateGrade.ACTIVA);
    }

    // -----------------------------------------------------------------
    // partialUpdate() manual state preservation
    // -----------------------------------------------------------------

    @Test
    void partialUpdatePreservesAplazadaManualState() {
        mockClockAt(TODAY);
        Grade existing = grade("g-1", StateGrade.APLAZADA, TODAY.minusDays(10), TODAY.plusDays(10));
        stubFindById(existing);
        stubMapperMerge();
        stubSaveAndMap(existing);

        GradeDTO dto = new GradeDTO();
        dto.setId("g-1");
        dto.setEndDate(TODAY.plusDays(20));

        GradeDTO result = gradeService.partialUpdate(dto).orElseThrow();

        assertThat(result.getState()).isEqualTo(StateGrade.APLAZADA);
        verify(gradeRepository).save(existing);
    }

    @Test
    void partialUpdatePreservesCanceladaManualState() {
        mockClockAt(TODAY);
        Grade existing = grade("g-1", StateGrade.CANCELADA, TODAY.minusDays(10), TODAY.plusDays(10));
        stubFindById(existing);
        stubMapperMerge();
        stubSaveAndMap(existing);

        GradeDTO dto = new GradeDTO();
        dto.setId("g-1");
        dto.setEndDate(TODAY.plusDays(20));

        GradeDTO result = gradeService.partialUpdate(dto).orElseThrow();

        assertThat(result.getState()).isEqualTo(StateGrade.CANCELADA);
        verify(gradeRepository).save(existing);
    }

    @Test
    void partialUpdateRecomputesStateWhenCurrentIsAutomatic() {
        mockClockAt(TODAY);
        Grade existing = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(10));
        stubFindById(existing);
        stubMapperMerge();
        stubSaveAndMap(existing);

        GradeDTO dto = new GradeDTO();
        dto.setId("g-1");
        // An ACTIVA ficha accepts end date changes, so the automatic state is re-derived.
        dto.setEndDate(TODAY.minusDays(5));
        dto.setState(StateGrade.CANCELADA);

        GradeDTO result = gradeService.partialUpdate(dto).orElseThrow();

        assertThat(result.getState()).isEqualTo(StateGrade.FINALIZADA);
    }

    // -----------------------------------------------------------------
    // code validation: numeric and unique
    // -----------------------------------------------------------------

    @Test
    void saveRejectsNonNumericCode() {
        Grade grade = grade("g-1", StateGrade.ACTIVA, TODAY, TODAY.plusDays(10)).code("12A");
        GradeDTO dto = toDto(grade);
        when(gradeMapper.toEntity(dto)).thenReturn(grade);

        assertThatExceptionOfType(BadRequestAlertException.class)
            .isThrownBy(() -> gradeService.save(dto))
            .satisfies(ex -> assertThat(ex.getErrorKey()).isEqualTo("codenotnumeric"));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void saveRejectsDuplicateCode() {
        Grade grade = grade("g-1", StateGrade.ACTIVA, TODAY, TODAY.plusDays(10));
        GradeDTO dto = toDto(grade);
        when(gradeMapper.toEntity(dto)).thenReturn(grade);
        when(gradeRepository.existsByCode(DEFAULT_CODE)).thenReturn(true);

        assertThatExceptionOfType(GradeCodeAlreadyUsedException.class).isThrownBy(() -> gradeService.save(dto));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void updateRejectsNonNumericCode() {
        Grade existing = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(10));
        Grade incoming = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(10)).code("12A");
        GradeDTO dto = toDto(incoming);
        when(gradeMapper.toEntity(dto)).thenReturn(incoming);
        when(gradeRepository.findById("g-1")).thenReturn(Optional.of(existing));

        assertThatExceptionOfType(BadRequestAlertException.class)
            .isThrownBy(() -> gradeService.update(dto))
            .satisfies(ex -> assertThat(ex.getErrorKey()).isEqualTo("codenotnumeric"));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void updateRejectsCodeUsedByAnotherFicha() {
        Grade existing = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(10));
        Grade incoming = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(10));
        GradeDTO dto = toDto(incoming);
        when(gradeMapper.toEntity(dto)).thenReturn(incoming);
        when(gradeRepository.findById("g-1")).thenReturn(Optional.of(existing));
        when(gradeRepository.existsByCodeAndIdNot(DEFAULT_CODE, "g-1")).thenReturn(true);

        assertThatExceptionOfType(GradeCodeAlreadyUsedException.class).isThrownBy(() -> gradeService.update(dto));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void updateAllowsTheFichasOwnCode() {
        mockClockAt(TODAY);
        Grade existing = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(10));
        Grade incoming = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(10));
        GradeDTO dto = toDto(incoming);
        when(gradeMapper.toEntity(dto)).thenReturn(incoming);
        when(gradeRepository.findById("g-1")).thenReturn(Optional.of(existing));
        when(gradeRepository.existsByCodeAndIdNot(DEFAULT_CODE, "g-1")).thenReturn(false);
        when(gradeRepository.save(incoming)).thenReturn(incoming);
        when(gradeMapper.toDto(incoming)).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        GradeDTO result = gradeService.update(dto);

        assertThat(result.getCode()).isEqualTo(DEFAULT_CODE);
        verify(gradeRepository).existsByCodeAndIdNot(DEFAULT_CODE, "g-1");
    }

    @Test
    void partialUpdateRejectsNonNumericCode() {
        Grade existing = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(10));
        stubFindById(existing);

        GradeDTO dto = new GradeDTO();
        dto.setId("g-1");
        dto.setCode("12A");

        assertThatExceptionOfType(BadRequestAlertException.class)
            .isThrownBy(() -> gradeService.partialUpdate(dto))
            .satisfies(ex -> assertThat(ex.getErrorKey()).isEqualTo("codenotnumeric"));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void partialUpdateRejectsCodeUsedByAnotherFicha() {
        Grade existing = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(10));
        stubFindById(existing);
        when(gradeRepository.existsByCodeAndIdNot(DEFAULT_CODE, "g-1")).thenReturn(true);

        GradeDTO dto = new GradeDTO();
        dto.setId("g-1");
        dto.setCode(DEFAULT_CODE);

        assertThatExceptionOfType(GradeCodeAlreadyUsedException.class).isThrownBy(() -> gradeService.partialUpdate(dto));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void partialUpdateAllowsTheFichasOwnCode() {
        mockClockAt(TODAY);
        Grade existing = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(10));
        stubFindById(existing);
        stubMapperMerge();
        stubSaveAndMap(existing);
        when(gradeRepository.existsByCodeAndIdNot(DEFAULT_CODE, "g-1")).thenReturn(false);

        GradeDTO dto = new GradeDTO();
        dto.setId("g-1");
        dto.setCode(DEFAULT_CODE);

        GradeDTO result = gradeService.partialUpdate(dto).orElseThrow();

        assertThat(result.getCode()).isEqualTo(DEFAULT_CODE);
        verify(gradeRepository).existsByCodeAndIdNot(DEFAULT_CODE, "g-1");
    }

    // -----------------------------------------------------------------
    // code lock: a ficha with class sections or apprentices
    // -----------------------------------------------------------------

    @Test
    void updateRejectsCodeChangeWhenFichaHasClassSections() {
        Grade existing = grade("g-1", StateGrade.PENDIENTE, TODAY.plusDays(10), TODAY.plusDays(40));
        Grade incoming = grade("g-1", StateGrade.PENDIENTE, TODAY.plusDays(10), TODAY.plusDays(40)).code(UPDATED_CODE);
        GradeDTO dto = toDto(incoming);
        when(gradeMapper.toEntity(dto)).thenReturn(incoming);
        when(gradeRepository.findById("g-1")).thenReturn(Optional.of(existing));
        when(classSectionRepository.findByGradeId("g-1")).thenReturn(List.of(new ClassSection()));

        assertThatExceptionOfType(BadRequestAlertException.class)
            .isThrownBy(() -> gradeService.update(dto))
            .satisfies(ex -> assertThat(ex.getErrorKey()).isEqualTo("gradeCodeLocked"));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void updateRejectsCodeChangeWhenFichaHasApprentices() {
        Grade existing = grade("g-1", StateGrade.PENDIENTE, TODAY.plusDays(10), TODAY.plusDays(40));
        Grade incoming = grade("g-1", StateGrade.PENDIENTE, TODAY.plusDays(10), TODAY.plusDays(40)).code(UPDATED_CODE);
        GradeDTO dto = toDto(incoming);
        when(gradeMapper.toEntity(dto)).thenReturn(incoming);
        when(gradeRepository.findById("g-1")).thenReturn(Optional.of(existing));
        when(apprenticeRepository.findByGradeId("g-1")).thenReturn(List.of(new Apprentice()));

        assertThatExceptionOfType(BadRequestAlertException.class)
            .isThrownBy(() -> gradeService.update(dto))
            .satisfies(ex -> assertThat(ex.getErrorKey()).isEqualTo("gradeCodeLocked"));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void partialUpdateRejectsCodeChangeWhenFichaHasClassSections() {
        Grade existing = grade("g-1", StateGrade.PENDIENTE, TODAY.plusDays(10), TODAY.plusDays(40));
        stubFindById(existing);
        when(classSectionRepository.findByGradeId("g-1")).thenReturn(List.of(new ClassSection()));

        GradeDTO dto = new GradeDTO();
        dto.setId("g-1");
        dto.setCode(UPDATED_CODE);

        assertThatExceptionOfType(BadRequestAlertException.class)
            .isThrownBy(() -> gradeService.partialUpdate(dto))
            .satisfies(ex -> assertThat(ex.getErrorKey()).isEqualTo("gradeCodeLocked"));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void updateAllowsCodeChangeWhenFichaHasNoAssociations() {
        mockClockAt(TODAY);
        Grade existing = grade("g-1", StateGrade.PENDIENTE, TODAY.plusDays(10), TODAY.plusDays(40));
        Grade incoming = grade("g-1", StateGrade.PENDIENTE, TODAY.plusDays(10), TODAY.plusDays(40)).code(UPDATED_CODE);
        GradeDTO dto = toDto(incoming);
        when(gradeMapper.toEntity(dto)).thenReturn(incoming);
        when(gradeRepository.findById("g-1")).thenReturn(Optional.of(existing));
        when(gradeRepository.existsByCodeAndIdNot(UPDATED_CODE, "g-1")).thenReturn(false);
        when(classSectionRepository.findByGradeId("g-1")).thenReturn(List.of());
        when(apprenticeRepository.findByGradeId("g-1")).thenReturn(List.of());
        when(gradeRepository.save(incoming)).thenReturn(incoming);
        when(gradeMapper.toDto(incoming)).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        GradeDTO result = gradeService.update(dto);

        assertThat(result.getCode()).isEqualTo(UPDATED_CODE);
        verify(gradeRepository).save(incoming);
    }

    // -----------------------------------------------------------------
    // date validation: range order and past start date
    // -----------------------------------------------------------------

    @Test
    void saveRejectsEndDateBeforeStartDate() {
        Grade grade = grade("g-1", StateGrade.ACTIVA, TODAY.plusDays(10), TODAY.plusDays(5));
        GradeDTO dto = toDto(grade);
        when(gradeMapper.toEntity(dto)).thenReturn(grade);

        assertThatExceptionOfType(GradeDatesOrderException.class)
            .isThrownBy(() -> gradeService.save(dto))
            .satisfies(ex -> assertThat(ex.getErrorKey()).isEqualTo("datesorder"));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void saveRejectsStartDateInThePast() {
        mockClockAt(TODAY);
        Grade grade = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(1), TODAY.plusDays(10));
        GradeDTO dto = toDto(grade);
        when(gradeMapper.toEntity(dto)).thenReturn(grade);

        assertThatExceptionOfType(GradeStartDateInPastException.class)
            .isThrownBy(() -> gradeService.save(dto))
            .satisfies(ex -> assertThat(ex.getErrorKey()).isEqualTo("startdateinpast"));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void updateRejectsEndDateBeforeStartDate() {
        Grade existing = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(10));
        Grade incoming = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.minusDays(20));
        GradeDTO dto = toDto(incoming);
        when(gradeMapper.toEntity(dto)).thenReturn(incoming);
        when(gradeRepository.findById("g-1")).thenReturn(Optional.of(existing));

        assertThatExceptionOfType(GradeDatesOrderException.class).isThrownBy(() -> gradeService.update(dto));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void updateRejectsChangedStartDateInThePast() {
        mockClockAt(TODAY);
        // A PENDIENTE ficha allows moving its start date, so the past-date rule is reached.
        Grade existing = grade("g-1", StateGrade.PENDIENTE, TODAY.plusDays(10), TODAY.plusDays(40));
        Grade incoming = grade("g-1", StateGrade.PENDIENTE, TODAY.minusDays(5), TODAY.plusDays(10));
        GradeDTO dto = toDto(incoming);
        when(gradeMapper.toEntity(dto)).thenReturn(incoming);
        when(gradeRepository.findById("g-1")).thenReturn(Optional.of(existing));

        assertThatExceptionOfType(GradeStartDateInPastException.class).isThrownBy(() -> gradeService.update(dto));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void updateAllowsUnchangedStartDateInThePast() {
        mockClockAt(TODAY);
        Grade existing = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(10));
        Grade incoming = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(20));
        GradeDTO dto = toDto(incoming);
        when(gradeMapper.toEntity(dto)).thenReturn(incoming);
        when(gradeRepository.findById("g-1")).thenReturn(Optional.of(existing));
        when(gradeRepository.save(incoming)).thenReturn(incoming);
        when(gradeMapper.toDto(incoming)).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        GradeDTO result = gradeService.update(dto);

        assertThat(result.getStartDate()).isEqualTo(TODAY.minusDays(10));
        assertThat(result.getState()).isEqualTo(StateGrade.ACTIVA);
        verify(gradeRepository).save(incoming);
    }

    @Test
    void partialUpdateRejectsEndDateBeforeStartDate() {
        Grade existing = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(10));
        stubFindById(existing);
        stubMapperMerge();

        GradeDTO dto = new GradeDTO();
        dto.setId("g-1");
        dto.setEndDate(TODAY.minusDays(20));

        assertThatExceptionOfType(GradeDatesOrderException.class).isThrownBy(() -> gradeService.partialUpdate(dto));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void partialUpdateRejectsChangedStartDateInThePast() {
        mockClockAt(TODAY);
        // A PENDIENTE ficha allows moving its start date, so the past-date rule is reached.
        Grade existing = grade("g-1", StateGrade.PENDIENTE, TODAY.plusDays(10), TODAY.plusDays(40));
        stubFindById(existing);
        stubMapperMerge();

        GradeDTO dto = new GradeDTO();
        dto.setId("g-1");
        dto.setStartDate(TODAY.minusDays(5));

        assertThatExceptionOfType(GradeStartDateInPastException.class).isThrownBy(() -> gradeService.partialUpdate(dto));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void partialUpdateAllowsUnchangedStartDateInThePast() {
        mockClockAt(TODAY);
        Grade existing = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(10));
        stubFindById(existing);
        stubMapperMerge();
        stubSaveAndMap(existing);

        GradeDTO dto = new GradeDTO();
        dto.setId("g-1");
        dto.setEndDate(TODAY.plusDays(20));

        GradeDTO result = gradeService.partialUpdate(dto).orElseThrow();

        assertThat(result.getStartDate()).isEqualTo(TODAY.minusDays(10));
        verify(gradeRepository).save(existing);
    }

    // -----------------------------------------------------------------
    // per-state edit rules
    // -----------------------------------------------------------------

    @Test
    void updateRejectsEveryChangeOnFinalizada() {
        Grade existing = grade("g-1", StateGrade.FINALIZADA, TODAY.minusDays(40), TODAY.minusDays(10));
        Grade incoming = grade("g-1", StateGrade.FINALIZADA, TODAY.minusDays(40), TODAY.minusDays(5));
        GradeDTO dto = toDto(incoming);
        when(gradeMapper.toEntity(dto)).thenReturn(incoming);
        when(gradeRepository.findById("g-1")).thenReturn(Optional.of(existing));

        assertThatExceptionOfType(BadRequestAlertException.class)
            .isThrownBy(() -> gradeService.update(dto))
            .satisfies(ex -> assertThat(ex.getErrorKey()).isEqualTo("noteditable"));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void updateRejectsStartDateChangeOnActiva() {
        Grade existing = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(10));
        Grade incoming = grade("g-1", StateGrade.ACTIVA, TODAY.plusDays(5), TODAY.plusDays(10));
        GradeDTO dto = toDto(incoming);
        when(gradeMapper.toEntity(dto)).thenReturn(incoming);
        when(gradeRepository.findById("g-1")).thenReturn(Optional.of(existing));

        assertThatExceptionOfType(BadRequestAlertException.class)
            .isThrownBy(() -> gradeService.update(dto))
            .satisfies(ex -> assertThat(ex.getErrorKey()).isEqualTo("fieldlocked"));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void updateRejectsModalityChangeOnActiva() {
        Grade existing = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(10));
        Grade incoming = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(10));
        GradeDTO dto = toDto(incoming);
        dto.setModality(new ModalityDTO());
        dto.getModality().setId("m-2");
        when(gradeMapper.toEntity(dto)).thenReturn(incoming);
        when(gradeRepository.findById("g-1")).thenReturn(Optional.of(existing));

        assertThatExceptionOfType(BadRequestAlertException.class)
            .isThrownBy(() -> gradeService.update(dto))
            .satisfies(ex -> assertThat(ex.getErrorKey()).isEqualTo("fieldlocked"));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void updateAllowsEndDateAndProgramChangeOnActiva() {
        mockClockAt(TODAY);
        Grade existing = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(10));
        Grade incoming = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(30)).program(new Program().id("p-2"));
        GradeDTO dto = toDto(incoming);
        dto.setProgram(new ProgramDTO());
        dto.getProgram().setId("p-2");
        when(gradeMapper.toEntity(dto)).thenReturn(incoming);
        when(gradeRepository.findById("g-1")).thenReturn(Optional.of(existing));
        when(gradeRepository.save(incoming)).thenReturn(incoming);
        when(gradeMapper.toDto(incoming)).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        GradeDTO result = gradeService.update(dto);

        assertThat(result.getEndDate()).isEqualTo(TODAY.plusDays(30));
        assertThat(result.getState()).isEqualTo(StateGrade.ACTIVA);
        verify(gradeRepository).save(incoming);
    }

    @Test
    void updateRejectsCodeChangeOnAplazada() {
        Grade existing = grade("g-1", StateGrade.APLAZADA, TODAY.minusDays(10), TODAY.plusDays(10));
        Grade incoming = grade("g-1", StateGrade.APLAZADA, TODAY.minusDays(10), TODAY.plusDays(10)).code(UPDATED_CODE);
        GradeDTO dto = toDto(incoming);
        when(gradeMapper.toEntity(dto)).thenReturn(incoming);
        when(gradeRepository.findById("g-1")).thenReturn(Optional.of(existing));

        assertThatExceptionOfType(BadRequestAlertException.class)
            .isThrownBy(() -> gradeService.update(dto))
            .satisfies(ex -> assertThat(ex.getErrorKey()).isEqualTo("fieldlocked"));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void updateAllowsEndDateChangeOnAplazada() {
        mockClockAt(TODAY);
        Grade existing = grade("g-1", StateGrade.APLAZADA, TODAY.minusDays(10), TODAY.plusDays(10));
        Grade incoming = grade("g-1", StateGrade.APLAZADA, TODAY.minusDays(10), TODAY.plusDays(30));
        GradeDTO dto = toDto(incoming);
        when(gradeMapper.toEntity(dto)).thenReturn(incoming);
        when(gradeRepository.findById("g-1")).thenReturn(Optional.of(existing));
        when(gradeRepository.save(incoming)).thenReturn(incoming);
        when(gradeMapper.toDto(incoming)).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        GradeDTO result = gradeService.update(dto);

        assertThat(result.getEndDate()).isEqualTo(TODAY.plusDays(30));
        assertThat(result.getState()).isEqualTo(StateGrade.APLAZADA);
        verify(gradeRepository).save(incoming);
    }

    @Test
    void updateAllowsEveryChangeOnPendiente() {
        mockClockAt(TODAY);
        Grade existing = grade("g-1", StateGrade.PENDIENTE, TODAY.plusDays(10), TODAY.plusDays(40));
        Grade incoming = grade("g-1", StateGrade.PENDIENTE, TODAY.plusDays(5), TODAY.plusDays(60));
        GradeDTO dto = toDto(incoming);
        when(gradeMapper.toEntity(dto)).thenReturn(incoming);
        when(gradeRepository.findById("g-1")).thenReturn(Optional.of(existing));
        when(gradeRepository.save(incoming)).thenReturn(incoming);
        when(gradeMapper.toDto(incoming)).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        GradeDTO result = gradeService.update(dto);

        assertThat(result.getStartDate()).isEqualTo(TODAY.plusDays(5));
        assertThat(result.getEndDate()).isEqualTo(TODAY.plusDays(60));
        assertThat(result.getState()).isEqualTo(StateGrade.PENDIENTE);
        verify(gradeRepository).save(incoming);
    }

    @Test
    void partialUpdateRejectsEveryChangeOnFinalizada() {
        Grade existing = grade("g-1", StateGrade.FINALIZADA, TODAY.minusDays(40), TODAY.minusDays(10));
        stubFindById(existing);

        GradeDTO dto = new GradeDTO();
        dto.setId("g-1");
        dto.setEndDate(TODAY.minusDays(5));

        assertThatExceptionOfType(BadRequestAlertException.class)
            .isThrownBy(() -> gradeService.partialUpdate(dto))
            .satisfies(ex -> assertThat(ex.getErrorKey()).isEqualTo("noteditable"));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void partialUpdateRejectsStartDateChangeOnActiva() {
        Grade existing = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(10));
        stubFindById(existing);

        GradeDTO dto = new GradeDTO();
        dto.setId("g-1");
        dto.setStartDate(TODAY.plusDays(5));

        assertThatExceptionOfType(BadRequestAlertException.class)
            .isThrownBy(() -> gradeService.partialUpdate(dto))
            .satisfies(ex -> assertThat(ex.getErrorKey()).isEqualTo("fieldlocked"));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void partialUpdateRejectsForbiddenChangeOnAplazada() {
        Grade existing = grade("g-1", StateGrade.APLAZADA, TODAY.minusDays(10), TODAY.plusDays(10));
        stubFindById(existing);

        GradeDTO dto = new GradeDTO();
        dto.setId("g-1");
        dto.setTimeSlot(new TimeSlotDTO());
        dto.getTimeSlot().setId("t-2");

        assertThatExceptionOfType(BadRequestAlertException.class)
            .isThrownBy(() -> gradeService.partialUpdate(dto))
            .satisfies(ex -> assertThat(ex.getErrorKey()).isEqualTo("fieldlocked"));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void partialUpdateAllowsEndDateChangeOnAplazada() {
        mockClockAt(TODAY);
        Grade existing = grade("g-1", StateGrade.APLAZADA, TODAY.minusDays(10), TODAY.plusDays(10));
        stubFindById(existing);
        stubMapperMerge();
        stubSaveAndMap(existing);

        GradeDTO dto = new GradeDTO();
        dto.setId("g-1");
        dto.setEndDate(TODAY.plusDays(30));

        GradeDTO result = gradeService.partialUpdate(dto).orElseThrow();

        assertThat(result.getEndDate()).isEqualTo(TODAY.plusDays(30));
        assertThat(result.getState()).isEqualTo(StateGrade.APLAZADA);
        verify(gradeRepository).save(existing);
    }

    @Test
    void partialUpdateAllowsEveryChangeOnCancelada() {
        mockClockAt(TODAY);
        Grade existing = grade("g-1", StateGrade.CANCELADA, TODAY.minusDays(10), TODAY.plusDays(10));
        stubFindById(existing);
        stubMapperMerge();
        stubSaveAndMap(existing);
        when(gradeRepository.existsByCodeAndIdNot(UPDATED_CODE, "g-1")).thenReturn(false);

        GradeDTO dto = new GradeDTO();
        dto.setId("g-1");
        dto.setCode(UPDATED_CODE);
        dto.setStartDate(TODAY.plusDays(5));
        dto.setEndDate(TODAY.plusDays(40));

        GradeDTO result = gradeService.partialUpdate(dto).orElseThrow();

        assertThat(result.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(result.getState()).isEqualTo(StateGrade.CANCELADA);
        verify(gradeRepository).save(existing);
    }

    // -----------------------------------------------------------------
    // active catalog validation: program, modality and time slot
    // -----------------------------------------------------------------

    @Test
    void saveRejectsInactiveModality() {
        mockClockAt(TODAY);
        Grade grade = grade("g-1", StateGrade.ACTIVA, TODAY, TODAY.plusDays(10)).modality(new Modality().id("m-1"));
        GradeDTO dto = toDto(grade);
        when(gradeMapper.toEntity(dto)).thenReturn(grade);
        when(modalityRepository.findById("m-1")).thenReturn(Optional.of(new Modality().id("m-1").isActive(false)));

        assertThatExceptionOfType(BadRequestAlertException.class)
            .isThrownBy(() -> gradeService.save(dto))
            .satisfies(ex -> assertThat(ex.getErrorKey()).isEqualTo("modalityInactive"));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void saveRejectsInactiveTimeSlot() {
        mockClockAt(TODAY);
        Grade grade = grade("g-1", StateGrade.ACTIVA, TODAY, TODAY.plusDays(10)).timeSlot(new TimeSlot().id("t-1"));
        GradeDTO dto = toDto(grade);
        when(gradeMapper.toEntity(dto)).thenReturn(grade);
        when(timeSlotRepository.findById("t-1")).thenReturn(Optional.of(new TimeSlot().id("t-1").isActive(false)));

        assertThatExceptionOfType(BadRequestAlertException.class)
            .isThrownBy(() -> gradeService.save(dto))
            .satisfies(ex -> assertThat(ex.getErrorKey()).isEqualTo("timeSlotInactive"));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void updateRejectsInactiveModality() {
        // A PENDIENTE ficha allows changing the modality, so the inactive catalog is reached.
        Grade existing = grade("g-1", StateGrade.PENDIENTE, TODAY.plusDays(10), TODAY.plusDays(40));
        Grade incoming = grade("g-1", StateGrade.PENDIENTE, TODAY.plusDays(10), TODAY.plusDays(40)).modality(new Modality().id("m-1"));
        GradeDTO dto = toDto(incoming);
        dto.setModality(new ModalityDTO());
        dto.getModality().setId("m-1");
        when(gradeMapper.toEntity(dto)).thenReturn(incoming);
        when(gradeRepository.findById("g-1")).thenReturn(Optional.of(existing));
        when(modalityRepository.findById("m-1")).thenReturn(Optional.of(new Modality().id("m-1").isActive(false)));

        assertThatExceptionOfType(BadRequestAlertException.class)
            .isThrownBy(() -> gradeService.update(dto))
            .satisfies(ex -> assertThat(ex.getErrorKey()).isEqualTo("modalityInactive"));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void partialUpdateRejectsInactiveTimeSlot() {
        Grade existing = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(10)).timeSlot(new TimeSlot().id("t-1"));
        stubFindById(existing);
        stubMapperMerge();
        when(timeSlotRepository.findById("t-1")).thenReturn(Optional.of(new TimeSlot().id("t-1").isActive(false)));

        GradeDTO dto = new GradeDTO();
        dto.setId("g-1");
        dto.setEndDate(TODAY.plusDays(20));

        assertThatExceptionOfType(BadRequestAlertException.class)
            .isThrownBy(() -> gradeService.partialUpdate(dto))
            .satisfies(ex -> assertThat(ex.getErrorKey()).isEqualTo("timeSlotInactive"));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    // -----------------------------------------------------------------
    // syncStates() daily job
    // -----------------------------------------------------------------

    @Test
    void syncStatesReclassifiesOnlyNonManualFichas() {
        mockClockAt(TODAY);
        Grade started = grade("g-1", StateGrade.PENDIENTE, TODAY, TODAY.plusDays(30));
        Grade finished = grade("g-2", StateGrade.ACTIVA, TODAY.minusDays(40), TODAY.minusDays(10));
        Grade paused = grade("g-3", StateGrade.APLAZADA, TODAY.minusDays(40), TODAY.minusDays(10));
        Grade cancelled = grade("g-4", StateGrade.CANCELADA, TODAY.plusDays(5), TODAY.plusDays(30));
        when(gradeRepository.findAll()).thenReturn(List.of(started, finished, paused, cancelled));
        when(gradeRepository.save(any(Grade.class))).thenAnswer(invocation -> invocation.getArgument(0));

        gradeService.syncStates();

        assertThat(started.getState()).isEqualTo(StateGrade.ACTIVA);
        assertThat(finished.getState()).isEqualTo(StateGrade.FINALIZADA);
        assertThat(paused.getState()).isEqualTo(StateGrade.APLAZADA);
        assertThat(cancelled.getState()).isEqualTo(StateGrade.CANCELADA);
        verify(gradeRepository, times(2)).save(any(Grade.class));
    }

    @Test
    void syncStatesDoesNotWriteWhenStateUnchanged() {
        mockClockAt(TODAY);
        Grade active = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(10));
        when(gradeRepository.findAll()).thenReturn(List.of(active));

        gradeService.syncStates();

        assertThat(active.getState()).isEqualTo(StateGrade.ACTIVA);
        verify(gradeRepository, never()).save(any(Grade.class));
    }

    // -----------------------------------------------------------------
    // postpone() / resume() / cancel() lifecycle actions
    // -----------------------------------------------------------------

    @Test
    void postponeSetsAplazadaFromPendiente() {
        Grade grade = grade("g-1", StateGrade.PENDIENTE, TODAY.plusDays(10), TODAY.plusDays(40));
        stubFindById(grade);
        stubSaveAndMap(grade);

        GradeDTO result = gradeService.postpone("g-1");

        assertThat(result.getState()).isEqualTo(StateGrade.APLAZADA);
        assertThat(grade.getState()).isEqualTo(StateGrade.APLAZADA);
        verify(gradeRepository).save(grade);
    }

    @Test
    void postponeSetsAplazadaFromActiva() {
        Grade grade = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(10));
        stubFindById(grade);
        stubSaveAndMap(grade);

        GradeDTO result = gradeService.postpone("g-1");

        assertThat(result.getState()).isEqualTo(StateGrade.APLAZADA);
        assertThat(grade.getState()).isEqualTo(StateGrade.APLAZADA);
    }

    @Test
    void postponeRejectsAnyOtherState() {
        for (StateGrade state : List.of(StateGrade.APLAZADA, StateGrade.FINALIZADA, StateGrade.CANCELADA)) {
            Grade grade = grade("g-" + state, state, TODAY.minusDays(10), TODAY.plusDays(10));
            stubFindById(grade);

            assertThatExceptionOfType(BadRequestAlertException.class)
                .isThrownBy(() -> gradeService.postpone(grade.getId()))
                .satisfies(ex -> assertThat(ex.getErrorKey()).isEqualTo("invalidtransition"));
        }
        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void resumeReclassifiesPendienteForFutureRange() {
        mockClockAt(TODAY);
        Grade grade = grade("g-1", StateGrade.APLAZADA, TODAY.plusDays(10), TODAY.plusDays(40));
        stubFindById(grade);
        stubSaveAndMap(grade);

        GradeDTO result = gradeService.resume("g-1");

        assertThat(result.getState()).isEqualTo(StateGrade.PENDIENTE);
        assertThat(grade.getState()).isEqualTo(StateGrade.PENDIENTE);
        verify(gradeRepository).save(grade);
    }

    @Test
    void resumeReclassifiesActivaForCurrentRange() {
        mockClockAt(TODAY);
        Grade grade = grade("g-1", StateGrade.APLAZADA, TODAY.minusDays(10), TODAY.plusDays(10));
        stubFindById(grade);
        stubSaveAndMap(grade);

        GradeDTO result = gradeService.resume("g-1");

        assertThat(result.getState()).isEqualTo(StateGrade.ACTIVA);
    }

    @Test
    void resumeReclassifiesFinalizadaForPastRange() {
        mockClockAt(TODAY);
        Grade grade = grade("g-1", StateGrade.APLAZADA, TODAY.minusDays(40), TODAY.minusDays(10));
        stubFindById(grade);
        stubSaveAndMap(grade);

        GradeDTO result = gradeService.resume("g-1");

        assertThat(result.getState()).isEqualTo(StateGrade.FINALIZADA);
    }

    @Test
    void resumeRejectsNonPostponedStates() {
        for (StateGrade state : List.of(StateGrade.PENDIENTE, StateGrade.ACTIVA, StateGrade.FINALIZADA, StateGrade.CANCELADA)) {
            Grade grade = grade("g-" + state, state, TODAY.minusDays(10), TODAY.plusDays(10));
            stubFindById(grade);

            assertThatExceptionOfType(BadRequestAlertException.class)
                .isThrownBy(() -> gradeService.resume(grade.getId()))
                .satisfies(ex -> assertThat(ex.getErrorKey()).isEqualTo("invalidtransition"));
        }
        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void cancelSetsCanceladaFromEveryNonCancelledState() {
        for (StateGrade state : List.of(StateGrade.PENDIENTE, StateGrade.ACTIVA, StateGrade.FINALIZADA, StateGrade.APLAZADA)) {
            Grade grade = grade("g-" + state, state, TODAY.minusDays(10), TODAY.plusDays(10));
            stubFindById(grade);
            stubSaveAndMap(grade);

            assertThat(gradeService.cancel(grade.getId()).getState()).isEqualTo(StateGrade.CANCELADA);
            assertThat(grade.getState()).isEqualTo(StateGrade.CANCELADA);
        }
        verify(gradeRepository, times(4)).save(any(Grade.class));
    }

    @Test
    void cancelRejectsAlreadyCancelledState() {
        Grade grade = grade("g-1", StateGrade.CANCELADA, TODAY.minusDays(10), TODAY.plusDays(10));
        stubFindById(grade);

        assertThatExceptionOfType(BadRequestAlertException.class)
            .isThrownBy(() -> gradeService.cancel("g-1"))
            .satisfies(ex -> assertThat(ex.getErrorKey()).isEqualTo("invalidtransition"));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void lifecycleActionsRejectMissingGrade() {
        when(gradeRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatExceptionOfType(BadRequestAlertException.class)
            .isThrownBy(() -> gradeService.postpone("missing"))
            .satisfies(ex -> assertThat(ex.getErrorKey()).isEqualTo("idnotfound"));
        assertThatExceptionOfType(BadRequestAlertException.class)
            .isThrownBy(() -> gradeService.resume("missing"))
            .satisfies(ex -> assertThat(ex.getErrorKey()).isEqualTo("idnotfound"));
        assertThatExceptionOfType(BadRequestAlertException.class)
            .isThrownBy(() -> gradeService.cancel("missing"))
            .satisfies(ex -> assertThat(ex.getErrorKey()).isEqualTo("idnotfound"));

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    /**
     * Configures the mocked {@link Clock} so {@code LocalDate.now(clock)} returns
     * {@code today} in the system zone, matching the production
     * {@code Clock.systemDefaultZone()}.
     */
    private void mockClockAt(LocalDate today) {
        ZoneId zone = ZoneId.systemDefault();
        when(clock.instant()).thenReturn(today.atStartOfDay(zone).toInstant());
        when(clock.getZone()).thenReturn(zone);
    }

    private static Grade grade(String id, StateGrade state, LocalDate start, LocalDate end) {
        return new Grade().id(id).code(DEFAULT_CODE).state(state).startDate(start).endDate(end);
    }

    private static GradeDTO toDto(Grade grade) {
        GradeDTO dto = new GradeDTO();
        dto.setId(grade.getId());
        dto.setCode(grade.getCode());
        dto.setState(grade.getState());
        dto.setStartDate(grade.getStartDate());
        dto.setEndDate(grade.getEndDate());
        return dto;
    }

    private void stubFindById(Grade grade) {
        when(gradeRepository.findById(grade.getId())).thenReturn(Optional.of(grade));
    }

    private void stubSaveAndMap(Grade grade) {
        when(gradeRepository.save(grade)).thenReturn(grade);
        when(gradeMapper.toDto(grade)).thenAnswer(invocation -> toDto(invocation.getArgument(0)));
    }

    /**
     * Simulates the MapStruct merge semantics for a {@code partialUpdate}: non-null source
     * fields overwrite the target, null source fields are ignored.
     */
    private void stubMapperMerge() {
        doAnswer(invocation -> {
            Grade target = invocation.getArgument(0);
            GradeDTO source = invocation.getArgument(1);
            if (source.getCode() != null) {
                target.setCode(source.getCode());
            }
            if (source.getState() != null) {
                target.setState(source.getState());
            }
            if (source.getStartDate() != null) {
                target.setStartDate(source.getStartDate());
            }
            if (source.getEndDate() != null) {
                target.setEndDate(source.getEndDate());
            }
            return null;
        })
            .when(gradeMapper)
            .partialUpdate(any(Grade.class), any(GradeDTO.class));
    }
}
