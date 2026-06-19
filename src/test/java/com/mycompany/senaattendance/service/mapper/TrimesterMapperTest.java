package com.mycompany.senaattendance.service.mapper;

import static com.mycompany.senaattendance.domain.TrimesterAsserts.*;
import static com.mycompany.senaattendance.domain.TrimesterTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TrimesterMapperTest {

    private TrimesterMapper trimesterMapper;

    @BeforeEach
    void setUp() {
        trimesterMapper = new TrimesterMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getTrimesterSample1();
        var actual = trimesterMapper.toEntity(trimesterMapper.toDto(expected));
        assertTrimesterAllPropertiesEquals(expected, actual);
    }
}
