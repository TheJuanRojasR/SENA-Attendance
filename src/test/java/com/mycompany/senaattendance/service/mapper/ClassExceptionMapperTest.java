package com.mycompany.senaattendance.service.mapper;

import static com.mycompany.senaattendance.domain.ClassExceptionAsserts.*;
import static com.mycompany.senaattendance.domain.ClassExceptionTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClassExceptionMapperTest {

    private ClassExceptionMapper classExceptionMapper;

    @BeforeEach
    void setUp() {
        classExceptionMapper = new ClassExceptionMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getClassExceptionSample1();
        var actual = classExceptionMapper.toEntity(classExceptionMapper.toDto(expected));
        assertClassExceptionAllPropertiesEquals(expected, actual);
    }
}
