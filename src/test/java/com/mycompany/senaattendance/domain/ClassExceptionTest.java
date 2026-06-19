package com.mycompany.senaattendance.domain;

import static com.mycompany.senaattendance.domain.ClassExceptionTestSamples.*;
import static com.mycompany.senaattendance.domain.ClassSectionTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ClassExceptionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ClassException.class);
        ClassException classException1 = getClassExceptionSample1();
        ClassException classException2 = new ClassException();
        assertThat(classException1).isNotEqualTo(classException2);

        classException2.setId(classException1.getId());
        assertThat(classException1).isEqualTo(classException2);

        classException2 = getClassExceptionSample2();
        assertThat(classException1).isNotEqualTo(classException2);
    }

    @Test
    void classSectionTest() {
        ClassException classException = getClassExceptionRandomSampleGenerator();
        ClassSection classSectionBack = getClassSectionRandomSampleGenerator();

        classException.setClassSection(classSectionBack);
        assertThat(classException.getClassSection()).isEqualTo(classSectionBack);

        classException.classSection(null);
        assertThat(classException.getClassSection()).isNull();
    }
}
