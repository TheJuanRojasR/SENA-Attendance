package com.mycompany.senaattendance.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ApprenticeDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ApprenticeDTO.class);
        ApprenticeDTO apprenticeDTO1 = new ApprenticeDTO();
        apprenticeDTO1.setId("id1");
        ApprenticeDTO apprenticeDTO2 = new ApprenticeDTO();
        assertThat(apprenticeDTO1).isNotEqualTo(apprenticeDTO2);
        apprenticeDTO2.setId(apprenticeDTO1.getId());
        assertThat(apprenticeDTO1).isEqualTo(apprenticeDTO2);
        apprenticeDTO2.setId("id2");
        assertThat(apprenticeDTO1).isNotEqualTo(apprenticeDTO2);
        apprenticeDTO1.setId(null);
        assertThat(apprenticeDTO1).isNotEqualTo(apprenticeDTO2);
    }
}
