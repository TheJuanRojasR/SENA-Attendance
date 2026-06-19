package com.mycompany.senaattendance.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class JustificationDetailsDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(JustificationDetailsDTO.class);
        JustificationDetailsDTO justificationDetailsDTO1 = new JustificationDetailsDTO();
        justificationDetailsDTO1.setId("id1");
        JustificationDetailsDTO justificationDetailsDTO2 = new JustificationDetailsDTO();
        assertThat(justificationDetailsDTO1).isNotEqualTo(justificationDetailsDTO2);
        justificationDetailsDTO2.setId(justificationDetailsDTO1.getId());
        assertThat(justificationDetailsDTO1).isEqualTo(justificationDetailsDTO2);
        justificationDetailsDTO2.setId("id2");
        assertThat(justificationDetailsDTO1).isNotEqualTo(justificationDetailsDTO2);
        justificationDetailsDTO1.setId(null);
        assertThat(justificationDetailsDTO1).isNotEqualTo(justificationDetailsDTO2);
    }
}
