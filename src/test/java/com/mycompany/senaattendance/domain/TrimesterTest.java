package com.mycompany.senaattendance.domain;

import static com.mycompany.senaattendance.domain.TrimesterTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TrimesterTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Trimester.class);
        Trimester trimester1 = getTrimesterSample1();
        Trimester trimester2 = new Trimester();
        assertThat(trimester1).isNotEqualTo(trimester2);

        trimester2.setId(trimester1.getId());
        assertThat(trimester1).isEqualTo(trimester2);

        trimester2 = getTrimesterSample2();
        assertThat(trimester1).isNotEqualTo(trimester2);
    }
}
