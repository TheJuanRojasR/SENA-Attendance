package com.mycompany.senaattendance.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class JustificationTypeTestSamples {

    private static final Random random = new Random();
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static JustificationType getJustificationTypeSample1() {
        return new JustificationType().id("id1").name("name1").limitPerTrimester(1);
    }

    public static JustificationType getJustificationTypeSample2() {
        return new JustificationType().id("id2").name("name2").limitPerTrimester(2);
    }

    public static JustificationType getJustificationTypeRandomSampleGenerator() {
        return new JustificationType()
            .id(UUID.randomUUID().toString())
            .name(UUID.randomUUID().toString())
            .limitPerTrimester(intCount.incrementAndGet());
    }
}
