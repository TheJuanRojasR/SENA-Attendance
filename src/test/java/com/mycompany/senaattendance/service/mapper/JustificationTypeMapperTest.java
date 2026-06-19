package com.mycompany.senaattendance.service.mapper;

import static com.mycompany.senaattendance.domain.JustificationTypeAsserts.*;
import static com.mycompany.senaattendance.domain.JustificationTypeTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JustificationTypeMapperTest {

    private JustificationTypeMapper justificationTypeMapper;

    @BeforeEach
    void setUp() {
        justificationTypeMapper = new JustificationTypeMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getJustificationTypeSample1();
        var actual = justificationTypeMapper.toEntity(justificationTypeMapper.toDto(expected));
        assertJustificationTypeAllPropertiesEquals(expected, actual);
    }
}
