package com.mycompany.senaattendance.service.mapper;

import static com.mycompany.senaattendance.domain.JustificationAsserts.*;
import static com.mycompany.senaattendance.domain.JustificationTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JustificationMapperTest {

    private JustificationMapper justificationMapper;

    @BeforeEach
    void setUp() {
        justificationMapper = new JustificationMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getJustificationSample1();
        var actual = justificationMapper.toEntity(justificationMapper.toDto(expected));
        assertJustificationAllPropertiesEquals(expected, actual);
    }
}
