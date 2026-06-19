package com.mycompany.senaattendance.domain;

import static com.mycompany.senaattendance.domain.GlobalConfigurationTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class GlobalConfigurationTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(GlobalConfiguration.class);
        GlobalConfiguration globalConfiguration1 = getGlobalConfigurationSample1();
        GlobalConfiguration globalConfiguration2 = new GlobalConfiguration();
        assertThat(globalConfiguration1).isNotEqualTo(globalConfiguration2);

        globalConfiguration2.setId(globalConfiguration1.getId());
        assertThat(globalConfiguration1).isEqualTo(globalConfiguration2);

        globalConfiguration2 = getGlobalConfigurationSample2();
        assertThat(globalConfiguration1).isNotEqualTo(globalConfiguration2);
    }
}
