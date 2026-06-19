package com.mycompany.senaattendance.domain;

import static com.mycompany.senaattendance.domain.ClassSectionTestSamples.*;
import static com.mycompany.senaattendance.domain.JustificationDetailsTestSamples.*;
import static com.mycompany.senaattendance.domain.JustificationTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class JustificationDetailsTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(JustificationDetails.class);
        JustificationDetails justificationDetails1 = getJustificationDetailsSample1();
        JustificationDetails justificationDetails2 = new JustificationDetails();
        assertThat(justificationDetails1).isNotEqualTo(justificationDetails2);

        justificationDetails2.setId(justificationDetails1.getId());
        assertThat(justificationDetails1).isEqualTo(justificationDetails2);

        justificationDetails2 = getJustificationDetailsSample2();
        assertThat(justificationDetails1).isNotEqualTo(justificationDetails2);
    }

    @Test
    void classSectionTest() {
        JustificationDetails justificationDetails = getJustificationDetailsRandomSampleGenerator();
        ClassSection classSectionBack = getClassSectionRandomSampleGenerator();

        justificationDetails.setClassSection(classSectionBack);
        assertThat(justificationDetails.getClassSection()).isEqualTo(classSectionBack);

        justificationDetails.classSection(null);
        assertThat(justificationDetails.getClassSection()).isNull();
    }

    @Test
    void justificationTest() {
        JustificationDetails justificationDetails = getJustificationDetailsRandomSampleGenerator();
        Justification justificationBack = getJustificationRandomSampleGenerator();

        justificationDetails.setJustification(justificationBack);
        assertThat(justificationDetails.getJustification()).isEqualTo(justificationBack);

        justificationDetails.justification(null);
        assertThat(justificationDetails.getJustification()).isNull();
    }
}
