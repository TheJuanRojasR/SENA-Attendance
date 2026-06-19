package com.mycompany.senaattendance.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ModalityDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ModalityDTO.class);
        ModalityDTO modalityDTO1 = new ModalityDTO();
        modalityDTO1.setId("id1");
        ModalityDTO modalityDTO2 = new ModalityDTO();
        assertThat(modalityDTO1).isNotEqualTo(modalityDTO2);
        modalityDTO2.setId(modalityDTO1.getId());
        assertThat(modalityDTO1).isEqualTo(modalityDTO2);
        modalityDTO2.setId("id2");
        assertThat(modalityDTO1).isNotEqualTo(modalityDTO2);
        modalityDTO1.setId(null);
        assertThat(modalityDTO1).isNotEqualTo(modalityDTO2);
    }
}
