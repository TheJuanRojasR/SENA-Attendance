package com.mycompany.senaattendance.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ProgramTestSamples {

    private static final Random random = new Random();
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Program getProgramSample1() {
        return new Program().id("id1").name("name1").initials("initials1").code("code1").trimesters(1);
    }

    public static Program getProgramSample2() {
        return new Program().id("id2").name("name2").initials("initials2").code("code2").trimesters(2);
    }

    public static Program getProgramRandomSampleGenerator() {
        return new Program()
            .id(UUID.randomUUID().toString())
            .name(UUID.randomUUID().toString())
            .initials(UUID.randomUUID().toString())
            .code(UUID.randomUUID().toString())
            .trimesters(intCount.incrementAndGet());
    }
}
