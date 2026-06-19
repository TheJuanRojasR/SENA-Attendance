package com.mycompany.senaattendance.domain;

import java.util.UUID;

public class ApprenticeTestSamples {

    public static Apprentice getApprenticeSample1() {
        return new Apprentice().id("id1");
    }

    public static Apprentice getApprenticeSample2() {
        return new Apprentice().id("id2");
    }

    public static Apprentice getApprenticeRandomSampleGenerator() {
        return new Apprentice().id(UUID.randomUUID().toString());
    }
}
