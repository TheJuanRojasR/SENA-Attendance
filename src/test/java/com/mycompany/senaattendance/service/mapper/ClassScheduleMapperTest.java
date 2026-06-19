package com.mycompany.senaattendance.service.mapper;

import static com.mycompany.senaattendance.domain.ClassScheduleAsserts.*;
import static com.mycompany.senaattendance.domain.ClassScheduleTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClassScheduleMapperTest {

    private ClassScheduleMapper classScheduleMapper;

    @BeforeEach
    void setUp() {
        classScheduleMapper = new ClassScheduleMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getClassScheduleSample1();
        var actual = classScheduleMapper.toEntity(classScheduleMapper.toDto(expected));
        assertClassScheduleAllPropertiesEquals(expected, actual);
    }
}
