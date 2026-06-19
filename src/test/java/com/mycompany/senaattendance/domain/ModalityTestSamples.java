package com.mycompany.senaattendance.domain;

import java.util.UUID;

public class ModalityTestSamples {

    public static Modality getModalitySample1() {
        return new Modality().id("id1").name("name1");
    }

    public static Modality getModalitySample2() {
        return new Modality().id("id2").name("name2");
    }

    public static Modality getModalityRandomSampleGenerator() {
        return new Modality().id(UUID.randomUUID().toString()).name(UUID.randomUUID().toString());
    }
}
