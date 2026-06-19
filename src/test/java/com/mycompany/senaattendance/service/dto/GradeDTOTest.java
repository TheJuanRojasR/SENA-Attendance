package com.mycompany.senaattendance.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class GradeDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(GradeDTO.class);
        GradeDTO gradeDTO1 = new GradeDTO();
        gradeDTO1.setId("id1");
        GradeDTO gradeDTO2 = new GradeDTO();
        assertThat(gradeDTO1).isNotEqualTo(gradeDTO2);
        gradeDTO2.setId(gradeDTO1.getId());
        assertThat(gradeDTO1).isEqualTo(gradeDTO2);
        gradeDTO2.setId("id2");
        assertThat(gradeDTO1).isNotEqualTo(gradeDTO2);
        gradeDTO1.setId(null);
        assertThat(gradeDTO1).isNotEqualTo(gradeDTO2);
    }
}
