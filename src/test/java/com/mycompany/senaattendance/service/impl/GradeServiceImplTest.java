package com.mycompany.senaattendance.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.enumeration.StateGrade;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.repository.ProgramRepository;
import com.mycompany.senaattendance.service.dto.GradeDTO;
import com.mycompany.senaattendance.service.mapper.GradeMapper;
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
 * computation on creation, preservation of manual states on edition and the daily
 * {@code syncStates()} job.
 */
@ExtendWith(MockitoExtension.class)
class GradeServiceImplTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 3, 10);

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private GradeMapper gradeMapper;

    @Mock
    private ProgramRepository programRepository;

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
    void saveComputesFinalizadaWhenEndIsInPast() {
        mockClockAt(TODAY);
        Grade grade = grade("g-1", StateGrade.CANCELADA, TODAY.minusDays(40), TODAY.minusDays(10));
        GradeDTO dto = toDto(grade);
        when(gradeMapper.toEntity(dto)).thenReturn(grade);
        when(gradeRepository.save(grade)).thenReturn(grade);
        when(gradeMapper.toDto(grade)).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        GradeDTO result = gradeService.save(dto);

        assertThat(result.getState()).isEqualTo(StateGrade.FINALIZADA);
        assertThat(grade.getState()).isEqualTo(StateGrade.FINALIZADA);
    }

    // -----------------------------------------------------------------
    // update() manual state preservation
    // -----------------------------------------------------------------

    @Test
    void updatePreservesAplazadaManualState() {
        mockClockAt(TODAY);
        Grade existing = grade("g-1", StateGrade.APLAZADA, TODAY.minusDays(10), TODAY.plusDays(10));
        Grade incoming = grade("g-1", StateGrade.ACTIVA, TODAY.plusDays(5), TODAY.plusDays(30));
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
        Grade existing = grade("g-1", StateGrade.ACTIVA, TODAY.minusDays(10), TODAY.plusDays(10));
        Grade incoming = grade("g-1", StateGrade.CANCELADA, TODAY.plusDays(5), TODAY.plusDays(30));
        GradeDTO dto = toDto(incoming);
        when(gradeMapper.toEntity(dto)).thenReturn(incoming);
        when(gradeRepository.findById("g-1")).thenReturn(Optional.of(existing));
        when(gradeRepository.save(incoming)).thenReturn(incoming);
        when(gradeMapper.toDto(incoming)).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        GradeDTO result = gradeService.update(dto);

        assertThat(result.getState()).isEqualTo(StateGrade.PENDIENTE);
        assertThat(incoming.getState()).isEqualTo(StateGrade.PENDIENTE);
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
        dto.setStartDate(TODAY.plusDays(5));
        dto.setState(StateGrade.CANCELADA);

        GradeDTO result = gradeService.partialUpdate(dto).orElseThrow();

        assertThat(result.getState()).isEqualTo(StateGrade.PENDIENTE);
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
        return new Grade().id(id).code("FICHA").state(state).startDate(start).endDate(end);
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
