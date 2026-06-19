package com.mycompany.senaattendance.service.mapper;

import static com.mycompany.senaattendance.domain.ModalityAsserts.*;
import static com.mycompany.senaattendance.domain.ModalityTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ModalityMapperTest {

    private ModalityMapper modalityMapper;

    @BeforeEach
    void setUp() {
        modalityMapper = new ModalityMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getModalitySample1();
        var actual = modalityMapper.toEntity(modalityMapper.toDto(expected));
        assertModalityAllPropertiesEquals(expected, actual);
    }
}
