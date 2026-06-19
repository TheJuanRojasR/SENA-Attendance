package com.mycompany.senaattendance.domain;

import java.util.UUID;

public class TrimesterTestSamples {

    public static Trimester getTrimesterSample1() {
        return new Trimester().id("id1").name("name1");
    }

    public static Trimester getTrimesterSample2() {
        return new Trimester().id("id2").name("name2");
    }

    public static Trimester getTrimesterRandomSampleGenerator() {
        return new Trimester().id(UUID.randomUUID().toString()).name(UUID.randomUUID().toString());
    }
}
