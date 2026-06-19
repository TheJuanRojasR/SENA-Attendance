package com.mycompany.senaattendance.domain;

import java.util.UUID;

public class DocumentTypeTestSamples {

    public static DocumentType getDocumentTypeSample1() {
        return new DocumentType().id("id1").name("name1").initials("initials1");
    }

    public static DocumentType getDocumentTypeSample2() {
        return new DocumentType().id("id2").name("name2").initials("initials2");
    }

    public static DocumentType getDocumentTypeRandomSampleGenerator() {
        return new DocumentType()
            .id(UUID.randomUUID().toString())
            .name(UUID.randomUUID().toString())
            .initials(UUID.randomUUID().toString());
    }
}
