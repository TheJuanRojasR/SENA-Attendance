package com.mycompany.senaattendance.service.mapper;

import static com.mycompany.senaattendance.domain.ClassSectionAsserts.*;
import static com.mycompany.senaattendance.domain.ClassSectionTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClassSectionMapperTest {

    private ClassSectionMapper classSectionMapper;

    @BeforeEach
    void setUp() {
        classSectionMapper = new ClassSectionMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getClassSectionSample1();
        var actual = classSectionMapper.toEntity(classSectionMapper.toDto(expected));
        assertClassSectionAllPropertiesEquals(expected, actual);
    }
}
