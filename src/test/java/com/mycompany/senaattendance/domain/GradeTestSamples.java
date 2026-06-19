package com.mycompany.senaattendance.domain;

import java.util.UUID;

public class GradeTestSamples {

    public static Grade getGradeSample1() {
        return new Grade().id("id1").code("code1");
    }

    public static Grade getGradeSample2() {
        return new Grade().id("id2").code("code2");
    }

    public static Grade getGradeRandomSampleGenerator() {
        return new Grade().id(UUID.randomUUID().toString()).code(UUID.randomUUID().toString());
    }
}
