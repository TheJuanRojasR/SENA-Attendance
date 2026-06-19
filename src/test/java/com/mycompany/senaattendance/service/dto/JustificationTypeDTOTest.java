package com.mycompany.senaattendance.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class JustificationTypeDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(JustificationTypeDTO.class);
        JustificationTypeDTO justificationTypeDTO1 = new JustificationTypeDTO();
        justificationTypeDTO1.setId("id1");
        JustificationTypeDTO justificationTypeDTO2 = new JustificationTypeDTO();
        assertThat(justificationTypeDTO1).isNotEqualTo(justificationTypeDTO2);
        justificationTypeDTO2.setId(justificationTypeDTO1.getId());
        assertThat(justificationTypeDTO1).isEqualTo(justificationTypeDTO2);
        justificationTypeDTO2.setId("id2");
        assertThat(justificationTypeDTO1).isNotEqualTo(justificationTypeDTO2);
        justificationTypeDTO1.setId(null);
        assertThat(justificationTypeDTO1).isNotEqualTo(justificationTypeDTO2);
    }
}
