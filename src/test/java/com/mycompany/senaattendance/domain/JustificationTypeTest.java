package com.mycompany.senaattendance.domain;

import static com.mycompany.senaattendance.domain.JustificationTypeTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class JustificationTypeTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(JustificationType.class);
        JustificationType justificationType1 = getJustificationTypeSample1();
        JustificationType justificationType2 = new JustificationType();
        assertThat(justificationType1).isNotEqualTo(justificationType2);

        justificationType2.setId(justificationType1.getId());
        assertThat(justificationType1).isEqualTo(justificationType2);

        justificationType2 = getJustificationTypeSample2();
        assertThat(justificationType1).isNotEqualTo(justificationType2);
    }
}
