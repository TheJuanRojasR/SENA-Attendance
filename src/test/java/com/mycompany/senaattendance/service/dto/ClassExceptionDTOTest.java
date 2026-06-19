package com.mycompany.senaattendance.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ClassExceptionDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ClassExceptionDTO.class);
        ClassExceptionDTO classExceptionDTO1 = new ClassExceptionDTO();
        classExceptionDTO1.setId("id1");
        ClassExceptionDTO classExceptionDTO2 = new ClassExceptionDTO();
        assertThat(classExceptionDTO1).isNotEqualTo(classExceptionDTO2);
        classExceptionDTO2.setId(classExceptionDTO1.getId());
        assertThat(classExceptionDTO1).isEqualTo(classExceptionDTO2);
        classExceptionDTO2.setId("id2");
        assertThat(classExceptionDTO1).isNotEqualTo(classExceptionDTO2);
        classExceptionDTO1.setId(null);
        assertThat(classExceptionDTO1).isNotEqualTo(classExceptionDTO2);
    }
}
