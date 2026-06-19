package com.mycompany.senaattendance.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ClassSectionDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ClassSectionDTO.class);
        ClassSectionDTO classSectionDTO1 = new ClassSectionDTO();
        classSectionDTO1.setId("id1");
        ClassSectionDTO classSectionDTO2 = new ClassSectionDTO();
        assertThat(classSectionDTO1).isNotEqualTo(classSectionDTO2);
        classSectionDTO2.setId(classSectionDTO1.getId());
        assertThat(classSectionDTO1).isEqualTo(classSectionDTO2);
        classSectionDTO2.setId("id2");
        assertThat(classSectionDTO1).isNotEqualTo(classSectionDTO2);
        classSectionDTO1.setId(null);
        assertThat(classSectionDTO1).isNotEqualTo(classSectionDTO2);
    }
}
