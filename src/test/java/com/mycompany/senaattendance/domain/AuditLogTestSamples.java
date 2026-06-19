package com.mycompany.senaattendance.domain;

import java.util.UUID;

public class AuditLogTestSamples {

    public static AuditLog getAuditLogSample1() {
        return new AuditLog().id("id1");
    }

    public static AuditLog getAuditLogSample2() {
        return new AuditLog().id("id2");
    }

    public static AuditLog getAuditLogRandomSampleGenerator() {
        return new AuditLog().id(UUID.randomUUID().toString());
    }
}
