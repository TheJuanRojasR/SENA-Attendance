package com.mycompany.senaattendance.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TrimesterDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(TrimesterDTO.class);
        TrimesterDTO trimesterDTO1 = new TrimesterDTO();
        trimesterDTO1.setId("id1");
        TrimesterDTO trimesterDTO2 = new TrimesterDTO();
        assertThat(trimesterDTO1).isNotEqualTo(trimesterDTO2);
        trimesterDTO2.setId(trimesterDTO1.getId());
        assertThat(trimesterDTO1).isEqualTo(trimesterDTO2);
        trimesterDTO2.setId("id2");
        assertThat(trimesterDTO1).isNotEqualTo(trimesterDTO2);
        trimesterDTO1.setId(null);
        assertThat(trimesterDTO1).isNotEqualTo(trimesterDTO2);
    }
}
