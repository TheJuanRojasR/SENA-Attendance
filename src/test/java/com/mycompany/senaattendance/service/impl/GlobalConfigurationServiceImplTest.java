package com.mycompany.senaattendance.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycompany.senaattendance.domain.GlobalConfiguration;
import com.mycompany.senaattendance.repository.GlobalConfigurationRepository;
import com.mycompany.senaattendance.service.dto.GlobalConfigurationDTO;
import com.mycompany.senaattendance.service.mapper.GlobalConfigurationMapperImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Unit tests for the singleton read behavior of {@link GlobalConfigurationServiceImpl},
 * in particular the defensive re-seed when the configuration row is missing.
 */
@ExtendWith(MockitoExtension.class)
class GlobalConfigurationServiceImplTest {

    private static final String CONFIG_ID = GlobalConfiguration.GLOBAL_CONFIGURATION_ID;

    @Mock
    private GlobalConfigurationRepository globalConfigurationRepository;

    private GlobalConfigurationServiceImpl globalConfigurationService;

    @BeforeEach
    void setUp() {
        globalConfigurationService = new GlobalConfigurationServiceImpl(globalConfigurationRepository, new GlobalConfigurationMapperImpl());
        SecurityContextHolder.clearContext();
    }

    private GlobalConfiguration existingConfiguration() {
        return new GlobalConfiguration()
            .id(CONFIG_ID)
            .studentJustificationDays(10)
            .instructorResponseDays(4)
            .consecutiveAbsenceAlertThreshold(8)
            .accumulatedAbsenceAlertThreshold(9);
    }

    @Test
    void getShouldReturnSingletonRowWhenPresent() {
        GlobalConfiguration configuration = existingConfiguration();
        when(globalConfigurationRepository.findById(CONFIG_ID)).thenReturn(Optional.of(configuration));

        GlobalConfigurationDTO result = globalConfigurationService.get();

        assertThat(result.getId()).isEqualTo(CONFIG_ID);
        assertThat(result.getStudentJustificationDays()).isEqualTo(10);
        assertThat(result.getInstructorResponseDays()).isEqualTo(4);
        assertThat(result.getConsecutiveAbsenceAlertThreshold()).isEqualTo(8);
        assertThat(result.getAccumulatedAbsenceAlertThreshold()).isEqualTo(9);
        verify(globalConfigurationRepository, never()).save(any());
    }

    @Test
    void getShouldFillMissingFieldsWithDefaultsOnLegacyRow() {
        GlobalConfiguration legacy = new GlobalConfiguration().id(CONFIG_ID).studentJustificationDays(10).instructorResponseDays(4);
        when(globalConfigurationRepository.findById(CONFIG_ID)).thenReturn(Optional.of(legacy));
        when(globalConfigurationRepository.save(any(GlobalConfiguration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GlobalConfigurationDTO result = globalConfigurationService.get();

        assertThat(result.getStudentJustificationDays()).isEqualTo(10);
        assertThat(result.getInstructorResponseDays()).isEqualTo(4);
        assertThat(result.getConsecutiveAbsenceAlertThreshold()).isEqualTo(
            GlobalConfigurationServiceImpl.DEFAULT_CONSECUTIVE_ABSENCE_ALERT_THRESHOLD
        );
        assertThat(result.getAccumulatedAbsenceAlertThreshold()).isEqualTo(
            GlobalConfigurationServiceImpl.DEFAULT_ACCUMULATED_ABSENCE_ALERT_THRESHOLD
        );

        ArgumentCaptor<GlobalConfiguration> captor = ArgumentCaptor.forClass(GlobalConfiguration.class);
        verify(globalConfigurationRepository).save(captor.capture());
        assertThat(captor.getValue().getConsecutiveAbsenceAlertThreshold()).isEqualTo(
            GlobalConfigurationServiceImpl.DEFAULT_CONSECUTIVE_ABSENCE_ALERT_THRESHOLD
        );
        assertThat(captor.getValue().getAccumulatedAbsenceAlertThreshold()).isEqualTo(
            GlobalConfigurationServiceImpl.DEFAULT_ACCUMULATED_ABSENCE_ALERT_THRESHOLD
        );
    }

    @Test
    void getShouldReseedWithDefaultsWhenMissing() {
        when(globalConfigurationRepository.findById(CONFIG_ID)).thenReturn(Optional.empty());
        when(globalConfigurationRepository.save(any(GlobalConfiguration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GlobalConfigurationDTO result = globalConfigurationService.get();

        assertThat(result.getId()).isEqualTo(CONFIG_ID);
        assertThat(result.getStudentJustificationDays()).isEqualTo(GlobalConfigurationServiceImpl.DEFAULT_STUDENT_JUSTIFICATION_DAYS);
        assertThat(result.getInstructorResponseDays()).isEqualTo(GlobalConfigurationServiceImpl.DEFAULT_INSTRUCTOR_RESPONSE_DAYS);
        assertThat(result.getConsecutiveAbsenceAlertThreshold()).isEqualTo(
            GlobalConfigurationServiceImpl.DEFAULT_CONSECUTIVE_ABSENCE_ALERT_THRESHOLD
        );
        assertThat(result.getAccumulatedAbsenceAlertThreshold()).isEqualTo(
            GlobalConfigurationServiceImpl.DEFAULT_ACCUMULATED_ABSENCE_ALERT_THRESHOLD
        );

        ArgumentCaptor<GlobalConfiguration> captor = ArgumentCaptor.forClass(GlobalConfiguration.class);
        verify(globalConfigurationRepository).save(captor.capture());
        assertThat(captor.getValue().getStudentJustificationDays()).isEqualTo(
            GlobalConfigurationServiceImpl.DEFAULT_STUDENT_JUSTIFICATION_DAYS
        );
        assertThat(captor.getValue().getInstructorResponseDays()).isEqualTo(
            GlobalConfigurationServiceImpl.DEFAULT_INSTRUCTOR_RESPONSE_DAYS
        );
        assertThat(captor.getValue().getConsecutiveAbsenceAlertThreshold()).isEqualTo(
            GlobalConfigurationServiceImpl.DEFAULT_CONSECUTIVE_ABSENCE_ALERT_THRESHOLD
        );
        assertThat(captor.getValue().getAccumulatedAbsenceAlertThreshold()).isEqualTo(
            GlobalConfigurationServiceImpl.DEFAULT_ACCUMULATED_ABSENCE_ALERT_THRESHOLD
        );
        assertThat(captor.getValue().getCreatedBy()).isEqualTo("system");
    }

    @Test
    void partialUpdateShouldLoadSingletonByIdAndKeepFixedIdentity() {
        GlobalConfiguration existing = existingConfiguration();
        when(globalConfigurationRepository.findById(CONFIG_ID)).thenReturn(Optional.of(existing));
        when(globalConfigurationRepository.save(any(GlobalConfiguration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GlobalConfigurationDTO dto = new GlobalConfigurationDTO();
        dto.setId("some-foreign-id");
        dto.setStudentJustificationDays(7);

        Optional<GlobalConfigurationDTO> result = globalConfigurationService.partialUpdate(dto);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(CONFIG_ID);
        assertThat(result.get().getStudentJustificationDays()).isEqualTo(7);
        // Omitted fields are left unchanged.
        assertThat(result.get().getInstructorResponseDays()).isEqualTo(4);
        verify(globalConfigurationRepository).findById(CONFIG_ID);
    }
}
