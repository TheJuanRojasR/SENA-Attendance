package com.mycompany.senaattendance.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.repository.TrimesterRepository;
import com.mycompany.senaattendance.service.dto.TrimesterDTO;
import com.mycompany.senaattendance.service.mapper.TrimesterMapper;
import java.time.LocalDate;
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
}
