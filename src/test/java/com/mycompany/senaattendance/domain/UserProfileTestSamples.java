package com.mycompany.senaattendance.domain;

import java.util.UUID;

public class UserProfileTestSamples {

    public static UserProfile getUserProfileSample1() {
        return new UserProfile()
            .id("id1")
            .firstName("firstName1")
            .middleName("middleName1")
            .firstLastName("firstLastName1")
            .secondLastName("secondLastName1")
            .documentNumber("documentNumber1")
            .phoneNumber("phoneNumber1");
    }

    public static UserProfile getUserProfileSample2() {
        return new UserProfile()
            .id("id2")
            .firstName("firstName2")
            .middleName("middleName2")
            .firstLastName("firstLastName2")
            .secondLastName("secondLastName2")
            .documentNumber("documentNumber2")
            .phoneNumber("phoneNumber2");
    }

    public static UserProfile getUserProfileRandomSampleGenerator() {
        return new UserProfile()
            .id(UUID.randomUUID().toString())
            .firstName(UUID.randomUUID().toString())
            .middleName(UUID.randomUUID().toString())
            .firstLastName(UUID.randomUUID().toString())
            .secondLastName(UUID.randomUUID().toString())
            .documentNumber(UUID.randomUUID().toString())
            .phoneNumber(UUID.randomUUID().toString());
    }
}
