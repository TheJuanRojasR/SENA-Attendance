package com.mycompany.senaattendance.service.mapper;

import static com.mycompany.senaattendance.domain.TimeSlotAsserts.*;
import static com.mycompany.senaattendance.domain.TimeSlotTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TimeSlotMapperTest {

    private TimeSlotMapper timeSlotMapper;

    @BeforeEach
    void setUp() {
        timeSlotMapper = new TimeSlotMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getTimeSlotSample1();
        var actual = timeSlotMapper.toEntity(timeSlotMapper.toDto(expected));
        assertTimeSlotAllPropertiesEquals(expected, actual);
    }
}
