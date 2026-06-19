package com.mycompany.senaattendance.domain;

import java.util.UUID;

public class ClassExceptionTestSamples {

    public static ClassException getClassExceptionSample1() {
        return new ClassException().id("id1").reason("reason1");
    }

    public static ClassException getClassExceptionSample2() {
        return new ClassException().id("id2").reason("reason2");
    }

    public static ClassException getClassExceptionRandomSampleGenerator() {
        return new ClassException().id(UUID.randomUUID().toString()).reason(UUID.randomUUID().toString());
    }
}
