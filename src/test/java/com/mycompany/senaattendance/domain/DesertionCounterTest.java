package com.mycompany.senaattendance.domain;

import static com.mycompany.senaattendance.domain.DesertionCounterTestSamples.*;
import static com.mycompany.senaattendance.domain.TrimesterTestSamples.*;
import static com.mycompany.senaattendance.domain.UserProfileTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DesertionCounterTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(DesertionCounter.class);
        DesertionCounter desertionCounter1 = getDesertionCounterSample1();
        DesertionCounter desertionCounter2 = new DesertionCounter();
        assertThat(desertionCounter1).isNotEqualTo(desertionCounter2);

        desertionCounter2.setId(desertionCounter1.getId());
        assertThat(desertionCounter1).isEqualTo(desertionCounter2);

        desertionCounter2 = getDesertionCounterSample2();
        assertThat(desertionCounter1).isNotEqualTo(desertionCounter2);
    }

    @Test
    void studentTest() {
        DesertionCounter desertionCounter = getDesertionCounterRandomSampleGenerator();
        UserProfile userProfileBack = getUserProfileRandomSampleGenerator();

        desertionCounter.setStudent(userProfileBack);
        assertThat(desertionCounter.getStudent()).isEqualTo(userProfileBack);

        desertionCounter.student(null);
        assertThat(desertionCounter.getStudent()).isNull();
    }

    @Test
    void trimesterTest() {
        DesertionCounter desertionCounter = getDesertionCounterRandomSampleGenerator();
        Trimester trimesterBack = getTrimesterRandomSampleGenerator();

        desertionCounter.setTrimester(trimesterBack);
        assertThat(desertionCounter.getTrimester()).isEqualTo(trimesterBack);

        desertionCounter.trimester(null);
        assertThat(desertionCounter.getTrimester()).isNull();
    }
}
