package com.mycompany.senaattendance.domain;

import java.util.UUID;

public class TimeSlotTestSamples {

    public static TimeSlot getTimeSlotSample1() {
        return new TimeSlot().id("id1").name("name1");
    }

    public static TimeSlot getTimeSlotSample2() {
        return new TimeSlot().id("id2").name("name2");
    }

    public static TimeSlot getTimeSlotRandomSampleGenerator() {
        return new TimeSlot().id(UUID.randomUUID().toString()).name(UUID.randomUUID().toString());
    }
}
