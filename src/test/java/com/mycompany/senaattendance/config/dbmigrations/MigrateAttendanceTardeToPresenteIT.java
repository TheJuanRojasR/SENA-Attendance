package com.mycompany.senaattendance.config.dbmigrations;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.IntegrationTest;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Verifies that {@link MigrateAttendanceTardeToPresente} rewrites legacy {@code TARDE}
 * attendance states into {@code PRESENTE} while leaving other states untouched.
 */
@IntegrationTest
class MigrateAttendanceTardeToPresenteIT {

    private static final String COLLECTION = "attendance";
    private static final ObjectId LEGACY_ID = new ObjectId("64b7a1f2e4b0a1b2c3d4e521");
    private static final ObjectId FAILED_ID = new ObjectId("64b7a1f2e4b0a1b2c3d4e522");

    @Autowired
    private MongoTemplate template;

    @AfterEach
    void cleanup() {
        template.getCollection(COLLECTION).deleteMany(new Document("_id", new Document("$in", List.of(LEGACY_ID, FAILED_ID))));
    }

    @Test
    void changeSetRewritesTardeToPresente() {
        template.getCollection(COLLECTION).insertOne(new Document("_id", LEGACY_ID).append("state_attendance", "TARDE"));
        template.getCollection(COLLECTION).insertOne(new Document("_id", FAILED_ID).append("state_attendance", "FALLA"));

        new MigrateAttendanceTardeToPresente(template).changeSet();

        assertThat(stateOf(LEGACY_ID)).isEqualTo("PRESENTE");
        assertThat(stateOf(FAILED_ID)).isEqualTo("FALLA");
    }

    private String stateOf(ObjectId id) {
        Document document = template.getCollection(COLLECTION).find(new Document("_id", id)).first();
        assertThat(document).isNotNull();
        return document.getString("state_attendance");
    }
}
