package com.mycompany.senaattendance.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.repository.TrimesterRepository;
import com.mycompany.senaattendance.service.dto.TrimesterDTO;
import com.mycompany.senaattendance.service.mapper.TrimesterMapper;
import com.mycompany.senaattendance.web.rest.errors.TrimesterDatesOrderException;
import com.mycompany.senaattendance.web.rest.errors.TrimesterDatesOverlapException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Unit tests for the {@link TrimesterServiceImpl#search} branch logic.
 *
 * <p>Each test drives one branch of the search decision tree and verifies the
 * correct repository method is invoked with the expected arguments.
 */
@ExtendWith(MockitoExtension.class)
class TrimesterServiceImplTest {

    private static final String NAME = "Primer Trimestre";
    private static final String NAME2 = "Segundo Trimestre";

    @Mock
    private TrimesterRepository trimesterRepository;

    @Mock
    private TrimesterMapper trimesterMapper;

    @Mock
    private Clock clock;

    @InjectMocks
    private TrimesterServiceImpl trimesterService;

    private Trimester trimester;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        trimester = new Trimester()
            .id("t-1")
            .name(NAME)
            .startDate(LocalDate.of(2025, 1, 1))
            .endDate(LocalDate.of(2025, 12, 31))
            .status(true);
        pageable = PageRequest.of(0, 10);
    }

    private TrimesterDTO toDto(Trimester t) {
        TrimesterDTO dto = new TrimesterDTO();
        dto.setId(t.getId());
        dto.setName(t.getName());
        dto.setStartDate(t.getStartDate());
        dto.setEndDate(t.getEndDate());
        dto.setStatus(t.getStatus());
        return dto;
    }

    private Page<Trimester> pageOf(Trimester t) {
        return new PageImpl<>(List.of(t), pageable, 1);
    }

    @Test
    void searchYearTermDelegatesToStartDateYear() {
        when(trimesterRepository.searchByStartDateYear(eq(2025), eq(pageable))).thenReturn(pageOf(trimester));
        when(trimesterMapper.toDto(trimester)).thenReturn(toDto(trimester));

        Page<TrimesterDTO> result = trimesterService.search("2025", null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo(NAME);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(trimesterRepository).searchByStartDateYear(2025, pageable);
        verify(trimesterRepository, never()).searchByName(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(trimesterRepository, never()).findAll(pageable);
    }

    @Test
    void searchYearTermWithStatusDelegatesToStartDateYearAndStatus() {
        when(trimesterRepository.searchByStartDateYearAndStatus(2025, true, pageable)).thenReturn(pageOf(trimester));
        when(trimesterMapper.toDto(trimester)).thenReturn(toDto(trimester));

        Page<TrimesterDTO> result = trimesterService.search("2025", true, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isTrue();
        verify(trimesterRepository).searchByStartDateYearAndStatus(2025, true, pageable);
        verify(trimesterRepository, never()).searchByNameAndStatus(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void searchNameTermDelegatesToName() {
        when(trimesterRepository.searchByName(NAME, pageable)).thenReturn(pageOf(trimester));
        when(trimesterMapper.toDto(trimester)).thenReturn(toDto(trimester));

        Page<TrimesterDTO> result = trimesterService.search(NAME, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo(NAME);
        verify(trimesterRepository).searchByName(NAME, pageable);
        verify(trimesterRepository, never()).searchByStartDateYear(anyInt(), org.mockito.ArgumentMatchers.any());
        verify(trimesterRepository, never()).findAll(pageable);
    }

    @Test
    void searchNameTermWithStatusDelegatesToNameAndStatus() {
        when(trimesterRepository.searchByNameAndStatus(NAME, false, pageable)).thenReturn(pageOf(trimester));
        when(trimesterMapper.toDto(trimester)).thenReturn(toDto(trimester));

        Page<TrimesterDTO> result = trimesterService.search(NAME, false, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo(NAME);
        verify(trimesterRepository).searchByNameAndStatus(NAME, false, pageable);
        verify(trimesterRepository, never()).findByStatus(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void searchBlankTermWithTrueStatusDelegatesToFindByStatus() {
        when(trimesterRepository.findByStatus(true, pageable)).thenReturn(pageOf(trimester));
        when(trimesterMapper.toDto(trimester)).thenReturn(toDto(trimester));

        Page<TrimesterDTO> result = trimesterService.search("", true, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isTrue();
        verify(trimesterRepository).findByStatus(true, pageable);
        verify(trimesterRepository, never()).searchByName(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(trimesterRepository, never()).findAll(pageable);
    }

    @Test
    void searchBlankTermWithFalseStatusDelegatesToFindByStatus() {
        Trimester inactive = new Trimester()
            .id("t-2")
            .name(NAME2)
            .startDate(LocalDate.of(2025, 1, 1))
            .endDate(LocalDate.of(2025, 12, 31))
            .status(false);
        when(trimesterRepository.findByStatus(false, pageable)).thenReturn(pageOf(inactive));
        when(trimesterMapper.toDto(inactive)).thenReturn(toDto(inactive));

        Page<TrimesterDTO> result = trimesterService.search("", false, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isFalse();
        verify(trimesterRepository).findByStatus(false, pageable);
        verify(trimesterRepository, never()).searchByName(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(trimesterRepository, never()).findAll(pageable);
    }

    @Test
    void searchBlankOrNullTermWithoutStatusDelegatesToFindAll() {
        when(trimesterRepository.findAll(pageable)).thenReturn(pageOf(trimester));
        when(trimesterMapper.toDto(trimester)).thenReturn(toDto(trimester));

        Page<TrimesterDTO> result = trimesterService.search("   ", null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(trimesterRepository).findAll(pageable);
        verify(trimesterRepository, never()).searchByName(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void searchTwoDigitTermIsTreatedAsNameNotYear() {
        when(trimesterRepository.searchByName("25", pageable)).thenReturn(pageOf(trimester));
        when(trimesterMapper.toDto(trimester)).thenReturn(toDto(trimester));

        Page<TrimesterDTO> result = trimesterService.search("25", null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo(NAME);
        verify(trimesterRepository).searchByName("25", pageable);
        verify(trimesterRepository, never()).searchByStartDateYear(anyInt(), org.mockito.ArgumentMatchers.any());
    }

    // -----------------------------------------------------------------
    // save() status computation
    // -----------------------------------------------------------------

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

    private void stubSaveSuccess(Trimester t, TrimesterDTO dto) {
        when(trimesterMapper.toEntity(dto)).thenReturn(t);
        when(trimesterRepository.findAllOverlapping(t.getStartDate(), t.getEndDate())).thenReturn(List.of());
        when(trimesterRepository.save(t)).thenReturn(t);
        when(trimesterMapper.toDto(t)).thenAnswer(invocation -> toDto(invocation.getArgument(0)));
    }

    @Test
    void saveComputesStatusFalseWhenStartIsInFuture() {
        LocalDate today = LocalDate.of(2026, 3, 10);
        mockClockAt(today);
        Trimester t = new Trimester().id("t-new").name(NAME).startDate(today.plusDays(10)).endDate(today.plusDays(40)).status(null);
        TrimesterDTO dto = toDto(t);
        stubSaveSuccess(t, dto);

        TrimesterDTO result = trimesterService.save(dto);

        assertThat(result.getStatus()).isFalse();
        assertThat(t.getStatus()).isFalse();
        verify(trimesterRepository).save(t);
    }

    @Test
    void saveComputesStatusTrueWhenTodayWithinRange() {
        LocalDate today = LocalDate.of(2026, 3, 10);
        mockClockAt(today);
        Trimester t = new Trimester().id("t-new").name(NAME).startDate(today).endDate(today.plusDays(30)).status(null);
        TrimesterDTO dto = toDto(t);
        stubSaveSuccess(t, dto);

        TrimesterDTO result = trimesterService.save(dto);

        assertThat(result.getStatus()).isTrue();
        assertThat(t.getStatus()).isTrue();
        verify(trimesterRepository).save(t);
    }

    @Test
    void saveComputesStatusFalseWhenEndIsInPast() {
        LocalDate today = LocalDate.of(2026, 3, 10);
        mockClockAt(today);
        Trimester t = new Trimester().id("t-new").name(NAME).startDate(today.minusDays(40)).endDate(today.minusDays(10)).status(null);
        TrimesterDTO dto = toDto(t);
        stubSaveSuccess(t, dto);

        TrimesterDTO result = trimesterService.save(dto);

        assertThat(result.getStatus()).isFalse();
        assertThat(t.getStatus()).isFalse();
        verify(trimesterRepository).save(t);
    }

    // -----------------------------------------------------------------
    // save() date-order validation (E2)
    // -----------------------------------------------------------------

    @Test
    void saveWithEqualDatesThrowsTrimesterDatesOrderException() {
        LocalDate today = LocalDate.of(2026, 3, 10);
        Trimester t = new Trimester().id("t-new").name(NAME).startDate(today).endDate(today).status(null);
        TrimesterDTO dto = toDto(t);
        when(trimesterMapper.toEntity(dto)).thenReturn(t);

        assertThatThrownBy(() -> trimesterService.save(dto)).isInstanceOf(TrimesterDatesOrderException.class);
        verify(trimesterRepository, never()).findAllOverlapping(any(), any());
        verify(trimesterRepository, never()).save(any());
    }

    @Test
    void saveWithReversedDatesThrowsTrimesterDatesOrderException() {
        LocalDate today = LocalDate.of(2026, 3, 10);
        Trimester t = new Trimester().id("t-new").name(NAME).startDate(today.plusDays(10)).endDate(today).status(null);
        TrimesterDTO dto = toDto(t);
        when(trimesterMapper.toEntity(dto)).thenReturn(t);

        assertThatThrownBy(() -> trimesterService.save(dto)).isInstanceOf(TrimesterDatesOrderException.class);
        verify(trimesterRepository, never()).findAllOverlapping(any(), any());
        verify(trimesterRepository, never()).save(any());
    }

    // -----------------------------------------------------------------
    // save() overlap validation (E1)
    // -----------------------------------------------------------------

    @Test
    void saveWithExactOverlapThrowsTrimesterDatesOverlapException() {
        LocalDate today = LocalDate.of(2026, 3, 10);
        Trimester existing = new Trimester().id("t-exists").name(NAME).startDate(today).endDate(today.plusDays(30)).status(true);
        Trimester t = new Trimester().id("t-new").name(NAME).startDate(today).endDate(today.plusDays(30)).status(null);
        TrimesterDTO dto = toDto(t);
        when(trimesterMapper.toEntity(dto)).thenReturn(t);
        when(trimesterRepository.findAllOverlapping(t.getStartDate(), t.getEndDate())).thenReturn(List.of(existing));

        assertThatThrownBy(() -> trimesterService.save(dto)).isInstanceOf(TrimesterDatesOverlapException.class);
        verify(trimesterRepository, never()).save(any());
    }

    @Test
    void saveWithContainedOverlapThrowsTrimesterDatesOverlapException() {
        LocalDate today = LocalDate.of(2026, 3, 10);
        Trimester existing = new Trimester().id("t-exists").name(NAME).startDate(today).endDate(today.plusDays(30)).status(true);
        Trimester t = new Trimester().id("t-new").name(NAME).startDate(today.plusDays(5)).endDate(today.plusDays(10)).status(null);
        TrimesterDTO dto = toDto(t);
        when(trimesterMapper.toEntity(dto)).thenReturn(t);
        when(trimesterRepository.findAllOverlapping(t.getStartDate(), t.getEndDate())).thenReturn(List.of(existing));

        assertThatThrownBy(() -> trimesterService.save(dto)).isInstanceOf(TrimesterDatesOverlapException.class);
        verify(trimesterRepository, never()).save(any());
    }

    @Test
    void saveWithAdjacentDatesIsAllowed() {
        LocalDate today = LocalDate.of(2026, 3, 10);
        mockClockAt(today);
        Trimester t = new Trimester().id("t-new").name(NAME).startDate(today.minusDays(30)).endDate(today.minusDays(1)).status(null);
        TrimesterDTO dto = toDto(t);
        when(trimesterMapper.toEntity(dto)).thenReturn(t);
        when(trimesterRepository.findAllOverlapping(t.getStartDate(), t.getEndDate())).thenReturn(List.of());
        when(trimesterRepository.save(t)).thenReturn(t);
        when(trimesterMapper.toDto(t)).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        TrimesterDTO result = trimesterService.save(dto);

        assertThat(result.getId()).isEqualTo("t-new");
        verify(trimesterRepository).save(t);
    }

    // -----------------------------------------------------------------
    // syncStatuses() daily job
    // -----------------------------------------------------------------

    @Test
    void syncStatusesActivatesTrimesterOnStartDate() {
        LocalDate today = LocalDate.of(2026, 3, 10);
        mockClockAt(today);
        Trimester t = new Trimester().id("t-1").name(NAME).startDate(today).endDate(today.plusDays(30)).status(false);
        when(trimesterRepository.findAll()).thenReturn(List.of(t));
        when(trimesterRepository.save(t)).thenReturn(t);

        trimesterService.syncStatuses();

        assertThat(t.getStatus()).isTrue();
        verify(trimesterRepository).save(t);
    }

    @Test
    void syncStatusesClosesTrimesterAfterEndDate() {
        LocalDate today = LocalDate.of(2026, 3, 10);
        mockClockAt(today);
        Trimester t = new Trimester().id("t-1").name(NAME).startDate(today.minusDays(40)).endDate(today.minusDays(10)).status(true);
        when(trimesterRepository.findAll()).thenReturn(List.of(t));
        when(trimesterRepository.save(t)).thenReturn(t);

        trimesterService.syncStatuses();

        assertThat(t.getStatus()).isFalse();
        verify(trimesterRepository).save(t);
    }

    @Test
    void syncStatusesDoesNotWriteWhenStatusUnchanged() {
        LocalDate today = LocalDate.of(2026, 3, 10);
        mockClockAt(today);
        Trimester active = new Trimester().id("t-1").name(NAME).startDate(today.minusDays(1)).endDate(today.plusDays(30)).status(true);
        Trimester inactive = new Trimester().id("t-2").name(NAME2).startDate(today.minusDays(40)).endDate(today.minusDays(1)).status(false);
        when(trimesterRepository.findAll()).thenReturn(List.of(active, inactive));

        trimesterService.syncStatuses();

        assertThat(active.getStatus()).isTrue();
        assertThat(inactive.getStatus()).isFalse();
        verify(trimesterRepository, never()).save(any());
    }
}
