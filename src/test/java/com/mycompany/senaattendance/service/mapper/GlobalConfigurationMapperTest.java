package com.mycompany.senaattendance.service.mapper;

import static com.mycompany.senaattendance.domain.GlobalConfigurationAsserts.*;
import static com.mycompany.senaattendance.domain.GlobalConfigurationTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GlobalConfigurationMapperTest {

    private GlobalConfigurationMapper globalConfigurationMapper;

    @BeforeEach
    void setUp() {
        globalConfigurationMapper = new GlobalConfigurationMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getGlobalConfigurationSample1();
        var actual = globalConfigurationMapper.toEntity(globalConfigurationMapper.toDto(expected));
        assertGlobalConfigurationAllPropertiesEquals(expected, actual);
    }
}
