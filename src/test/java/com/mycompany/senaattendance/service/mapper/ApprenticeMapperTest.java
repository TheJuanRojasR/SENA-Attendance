package com.mycompany.senaattendance.service.mapper;

import static com.mycompany.senaattendance.domain.ApprenticeAsserts.*;
import static com.mycompany.senaattendance.domain.ApprenticeTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApprenticeMapperTest {

    private ApprenticeMapper apprenticeMapper;

    @BeforeEach
    void setUp() {
        apprenticeMapper = new ApprenticeMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getApprenticeSample1();
        var actual = apprenticeMapper.toEntity(apprenticeMapper.toDto(expected));
        assertApprenticeAllPropertiesEquals(expected, actual);
    }
}
