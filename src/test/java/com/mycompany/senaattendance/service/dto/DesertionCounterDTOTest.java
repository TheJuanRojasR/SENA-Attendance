package com.mycompany.senaattendance.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DesertionCounterDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(DesertionCounterDTO.class);
        DesertionCounterDTO desertionCounterDTO1 = new DesertionCounterDTO();
        desertionCounterDTO1.setId("id1");
        DesertionCounterDTO desertionCounterDTO2 = new DesertionCounterDTO();
        assertThat(desertionCounterDTO1).isNotEqualTo(desertionCounterDTO2);
        desertionCounterDTO2.setId(desertionCounterDTO1.getId());
        assertThat(desertionCounterDTO1).isEqualTo(desertionCounterDTO2);
        desertionCounterDTO2.setId("id2");
        assertThat(desertionCounterDTO1).isNotEqualTo(desertionCounterDTO2);
        desertionCounterDTO1.setId(null);
        assertThat(desertionCounterDTO1).isNotEqualTo(desertionCounterDTO2);
    }
}
