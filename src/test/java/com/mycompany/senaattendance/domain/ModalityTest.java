package com.mycompany.senaattendance.domain;

import static com.mycompany.senaattendance.domain.ModalityTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ModalityTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Modality.class);
        Modality modality1 = getModalitySample1();
        Modality modality2 = new Modality();
        assertThat(modality1).isNotEqualTo(modality2);

        modality2.setId(modality1.getId());
        assertThat(modality1).isEqualTo(modality2);

        modality2 = getModalitySample2();
        assertThat(modality1).isNotEqualTo(modality2);
    }
}
