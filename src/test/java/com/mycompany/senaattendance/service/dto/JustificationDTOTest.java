package com.mycompany.senaattendance.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class JustificationDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(JustificationDTO.class);
        JustificationDTO justificationDTO1 = new JustificationDTO();
        justificationDTO1.setId("id1");
        JustificationDTO justificationDTO2 = new JustificationDTO();
        assertThat(justificationDTO1).isNotEqualTo(justificationDTO2);
        justificationDTO2.setId(justificationDTO1.getId());
        assertThat(justificationDTO1).isEqualTo(justificationDTO2);
        justificationDTO2.setId("id2");
        assertThat(justificationDTO1).isNotEqualTo(justificationDTO2);
        justificationDTO1.setId(null);
        assertThat(justificationDTO1).isNotEqualTo(justificationDTO2);
    }
}
