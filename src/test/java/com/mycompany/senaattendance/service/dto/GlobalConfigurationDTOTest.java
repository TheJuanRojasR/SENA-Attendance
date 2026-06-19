package com.mycompany.senaattendance.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class GlobalConfigurationDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(GlobalConfigurationDTO.class);
        GlobalConfigurationDTO globalConfigurationDTO1 = new GlobalConfigurationDTO();
        globalConfigurationDTO1.setId("id1");
        GlobalConfigurationDTO globalConfigurationDTO2 = new GlobalConfigurationDTO();
        assertThat(globalConfigurationDTO1).isNotEqualTo(globalConfigurationDTO2);
        globalConfigurationDTO2.setId(globalConfigurationDTO1.getId());
        assertThat(globalConfigurationDTO1).isEqualTo(globalConfigurationDTO2);
        globalConfigurationDTO2.setId("id2");
        assertThat(globalConfigurationDTO1).isNotEqualTo(globalConfigurationDTO2);
        globalConfigurationDTO1.setId(null);
        assertThat(globalConfigurationDTO1).isNotEqualTo(globalConfigurationDTO2);
    }
}
