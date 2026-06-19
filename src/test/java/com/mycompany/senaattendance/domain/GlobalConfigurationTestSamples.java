package com.mycompany.senaattendance.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class GlobalConfigurationTestSamples {

    private static final Random random = new Random();
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static GlobalConfiguration getGlobalConfigurationSample1() {
        return new GlobalConfiguration()
            .id("id1")
            .studentJustificationDays(1)
            .instructorResponseDays(1)
            .lateArrivalsToFail(1)
            .maxPostponementJustifications(1)
            .standardTrimesterMonths(1);
    }

    public static GlobalConfiguration getGlobalConfigurationSample2() {
        return new GlobalConfiguration()
            .id("id2")
            .studentJustificationDays(2)
            .instructorResponseDays(2)
            .lateArrivalsToFail(2)
            .maxPostponementJustifications(2)
            .standardTrimesterMonths(2);
    }

    public static GlobalConfiguration getGlobalConfigurationRandomSampleGenerator() {
        return new GlobalConfiguration()
            .id(UUID.randomUUID().toString())
            .studentJustificationDays(intCount.incrementAndGet())
            .instructorResponseDays(intCount.incrementAndGet())
            .lateArrivalsToFail(intCount.incrementAndGet())
            .maxPostponementJustifications(intCount.incrementAndGet())
            .standardTrimesterMonths(intCount.incrementAndGet());
    }
}
