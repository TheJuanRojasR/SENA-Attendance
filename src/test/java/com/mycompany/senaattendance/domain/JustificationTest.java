package com.mycompany.senaattendance.domain;

import static com.mycompany.senaattendance.domain.JustificationDetailsTestSamples.*;
import static com.mycompany.senaattendance.domain.JustificationTestSamples.*;
import static com.mycompany.senaattendance.domain.JustificationTypeTestSamples.*;
import static com.mycompany.senaattendance.domain.UserProfileTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class JustificationTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Justification.class);
        Justification justification1 = getJustificationSample1();
        Justification justification2 = new Justification();
        assertThat(justification1).isNotEqualTo(justification2);

        justification2.setId(justification1.getId());
        assertThat(justification1).isEqualTo(justification2);

        justification2 = getJustificationSample2();
        assertThat(justification1).isNotEqualTo(justification2);
    }

    @Test
    void detailsTest() {
        Justification justification = getJustificationRandomSampleGenerator();
        JustificationDetails justificationDetailsBack = getJustificationDetailsRandomSampleGenerator();

        justification.addDetails(justificationDetailsBack);
        assertThat(justification.getDetailses()).containsOnly(justificationDetailsBack);
        assertThat(justificationDetailsBack.getJustification()).isEqualTo(justification);

        justification.removeDetails(justificationDetailsBack);
        assertThat(justification.getDetailses()).doesNotContain(justificationDetailsBack);
        assertThat(justificationDetailsBack.getJustification()).isNull();

        justification.detailses(new HashSet<>(Set.of(justificationDetailsBack)));
        assertThat(justification.getDetailses()).containsOnly(justificationDetailsBack);
        assertThat(justificationDetailsBack.getJustification()).isEqualTo(justification);

        justification.setDetailses(new HashSet<>());
        assertThat(justification.getDetailses()).doesNotContain(justificationDetailsBack);
        assertThat(justificationDetailsBack.getJustification()).isNull();
    }

    @Test
    void justificationTypeTest() {
        Justification justification = getJustificationRandomSampleGenerator();
        JustificationType justificationTypeBack = getJustificationTypeRandomSampleGenerator();

        justification.setJustificationType(justificationTypeBack);
        assertThat(justification.getJustificationType()).isEqualTo(justificationTypeBack);

        justification.justificationType(null);
        assertThat(justification.getJustificationType()).isNull();
    }

    @Test
    void studentTest() {
        Justification justification = getJustificationRandomSampleGenerator();
        UserProfile userProfileBack = getUserProfileRandomSampleGenerator();

        justification.setStudent(userProfileBack);
        assertThat(justification.getStudent()).isEqualTo(userProfileBack);

        justification.student(null);
        assertThat(justification.getStudent()).isNull();
    }
}
