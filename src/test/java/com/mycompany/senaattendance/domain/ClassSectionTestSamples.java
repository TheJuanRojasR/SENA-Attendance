package com.mycompany.senaattendance.domain;

import java.util.UUID;

public class ClassSectionTestSamples {

    public static ClassSection getClassSectionSample1() {
        return new ClassSection().id("id1").subjectName("subjectName1");
    }

    public static ClassSection getClassSectionSample2() {
        return new ClassSection().id("id2").subjectName("subjectName2");
    }

    public static ClassSection getClassSectionRandomSampleGenerator() {
        return new ClassSection().id(UUID.randomUUID().toString()).subjectName(UUID.randomUUID().toString());
    }
}
