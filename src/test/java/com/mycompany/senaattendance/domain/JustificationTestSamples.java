package com.mycompany.senaattendance.domain;

import java.util.UUID;

public class JustificationTestSamples {

    public static Justification getJustificationSample1() {
        return new Justification().id("id1").description("description1");
    }

    public static Justification getJustificationSample2() {
        return new Justification().id("id2").description("description2");
    }

    public static Justification getJustificationRandomSampleGenerator() {
        return new Justification().id(UUID.randomUUID().toString()).description(UUID.randomUUID().toString());
    }
}
