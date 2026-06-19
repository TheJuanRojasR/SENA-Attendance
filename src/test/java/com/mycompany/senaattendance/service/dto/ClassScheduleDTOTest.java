package com.mycompany.senaattendance.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ClassScheduleDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ClassScheduleDTO.class);
        ClassScheduleDTO classScheduleDTO1 = new ClassScheduleDTO();
        classScheduleDTO1.setId("id1");
        ClassScheduleDTO classScheduleDTO2 = new ClassScheduleDTO();
        assertThat(classScheduleDTO1).isNotEqualTo(classScheduleDTO2);
        classScheduleDTO2.setId(classScheduleDTO1.getId());
        assertThat(classScheduleDTO1).isEqualTo(classScheduleDTO2);
        classScheduleDTO2.setId("id2");
        assertThat(classScheduleDTO1).isNotEqualTo(classScheduleDTO2);
        classScheduleDTO1.setId(null);
        assertThat(classScheduleDTO1).isNotEqualTo(classScheduleDTO2);
    }
}
