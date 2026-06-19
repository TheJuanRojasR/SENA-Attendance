package com.mycompany.senaattendance.service.mapper;

import static com.mycompany.senaattendance.domain.JustificationDetailsAsserts.*;
import static com.mycompany.senaattendance.domain.JustificationDetailsTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JustificationDetailsMapperTest {

    private JustificationDetailsMapper justificationDetailsMapper;

    @BeforeEach
    void setUp() {
        justificationDetailsMapper = new JustificationDetailsMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getJustificationDetailsSample1();
        var actual = justificationDetailsMapper.toEntity(justificationDetailsMapper.toDto(expected));
        assertJustificationDetailsAllPropertiesEquals(expected, actual);
    }
}
