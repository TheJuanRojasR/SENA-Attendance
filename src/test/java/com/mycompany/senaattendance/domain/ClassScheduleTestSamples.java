package com.mycompany.senaattendance.domain;

import java.util.UUID;

public class ClassScheduleTestSamples {

    public static ClassSchedule getClassScheduleSample1() {
        return new ClassSchedule().id("id1");
    }

    public static ClassSchedule getClassScheduleSample2() {
        return new ClassSchedule().id("id2");
    }

    public static ClassSchedule getClassScheduleRandomSampleGenerator() {
        return new ClassSchedule().id(UUID.randomUUID().toString());
    }
}
