package com.mycompany.senaattendance.domain;

import static com.mycompany.senaattendance.domain.ClassScheduleTestSamples.*;
import static com.mycompany.senaattendance.domain.ClassSectionTestSamples.*;
import static com.mycompany.senaattendance.domain.TrimesterTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ClassScheduleTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ClassSchedule.class);
        ClassSchedule classSchedule1 = getClassScheduleSample1();
        ClassSchedule classSchedule2 = new ClassSchedule();
        assertThat(classSchedule1).isNotEqualTo(classSchedule2);

        classSchedule2.setId(classSchedule1.getId());
        assertThat(classSchedule1).isEqualTo(classSchedule2);

        classSchedule2 = getClassScheduleSample2();
        assertThat(classSchedule1).isNotEqualTo(classSchedule2);
    }

    @Test
    void trimesterTest() {
        ClassSchedule classSchedule = getClassScheduleRandomSampleGenerator();
        Trimester trimesterBack = getTrimesterRandomSampleGenerator();

        classSchedule.setTrimester(trimesterBack);
        assertThat(classSchedule.getTrimester()).isEqualTo(trimesterBack);

        classSchedule.trimester(null);
        assertThat(classSchedule.getTrimester()).isNull();
    }

    @Test
    void classSectionTest() {
        ClassSchedule classSchedule = getClassScheduleRandomSampleGenerator();
        ClassSection classSectionBack = getClassSectionRandomSampleGenerator();

        classSchedule.setClassSection(classSectionBack);
        assertThat(classSchedule.getClassSection()).isEqualTo(classSectionBack);

        classSchedule.classSection(null);
        assertThat(classSchedule.getClassSection()).isNull();
    }
}
