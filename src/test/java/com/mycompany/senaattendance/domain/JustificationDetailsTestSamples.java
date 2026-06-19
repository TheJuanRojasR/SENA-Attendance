package com.mycompany.senaattendance.domain;

import java.util.UUID;

public class JustificationDetailsTestSamples {

    public static JustificationDetails getJustificationDetailsSample1() {
        return new JustificationDetails().id("id1").rejectionReason("rejectionReason1").correctionText("correctionText1");
    }

    public static JustificationDetails getJustificationDetailsSample2() {
        return new JustificationDetails().id("id2").rejectionReason("rejectionReason2").correctionText("correctionText2");
    }

    public static JustificationDetails getJustificationDetailsRandomSampleGenerator() {
        return new JustificationDetails()
            .id(UUID.randomUUID().toString())
            .rejectionReason(UUID.randomUUID().toString())
            .correctionText(UUID.randomUUID().toString());
    }
}
