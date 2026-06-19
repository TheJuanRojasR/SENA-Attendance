package com.mycompany.senaattendance.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class DesertionCounterTestSamples {

    private static final Random random = new Random();
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static DesertionCounter getDesertionCounterSample1() {
        return new DesertionCounter().id("id1").totalGlobalAbsences(1).accumulatedLateArrivals(1).workJustificationsUsed(1);
    }

    public static DesertionCounter getDesertionCounterSample2() {
        return new DesertionCounter().id("id2").totalGlobalAbsences(2).accumulatedLateArrivals(2).workJustificationsUsed(2);
    }

    public static DesertionCounter getDesertionCounterRandomSampleGenerator() {
        return new DesertionCounter()
            .id(UUID.randomUUID().toString())
            .totalGlobalAbsences(intCount.incrementAndGet())
            .accumulatedLateArrivals(intCount.incrementAndGet())
            .workJustificationsUsed(intCount.incrementAndGet());
    }
}
