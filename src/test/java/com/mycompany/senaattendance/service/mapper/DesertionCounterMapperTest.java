package com.mycompany.senaattendance.service.mapper;

import static com.mycompany.senaattendance.domain.DesertionCounterAsserts.*;
import static com.mycompany.senaattendance.domain.DesertionCounterTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DesertionCounterMapperTest {

    private DesertionCounterMapper desertionCounterMapper;

    @BeforeEach
    void setUp() {
        desertionCounterMapper = new DesertionCounterMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getDesertionCounterSample1();
        var actual = desertionCounterMapper.toEntity(desertionCounterMapper.toDto(expected));
        assertDesertionCounterAllPropertiesEquals(expected, actual);
    }
}
