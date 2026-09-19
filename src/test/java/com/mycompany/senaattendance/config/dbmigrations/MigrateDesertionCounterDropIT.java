package com.mycompany.senaattendance.config.dbmigrations;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.IntegrationTest;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Verifies that {@link MigrateDesertionCounterDrop} drops the legacy {@code desertion_counter}
 * collection and that running the migration without the collection is harmless.
 */
@IntegrationTest
class MigrateDesertionCounterDropIT {

    private static final String COLLECTION = "desertion_counter";

    private static final ObjectId LEGACY_ID = new ObjectId("64b7a1f2e4b0a1b2c3d4e581");

    @Autowired
    private MongoTemplate template;

    @AfterEach
    void cleanup() {
        dropCollectionIfPresent();
    }

    @Test
    void changeSetDropsTheLegacyCollection() {
        template.getCollection(COLLECTION).insertOne(new Document("_id", LEGACY_ID).append("total_global_absences", 7));
        assertThat(template.collectionExists(COLLECTION)).isTrue();

        new MigrateDesertionCounterDrop(template).changeSet();

        assertThat(template.collectionExists(COLLECTION)).isFalse();
    }

    @Test
    void changeSetOnAMissingCollectionDoesNotFail() {
        dropCollectionIfPresent();

        new MigrateDesertionCounterDrop(template).changeSet();

        assertThat(template.collectionExists(COLLECTION)).isFalse();
    }

    private void dropCollectionIfPresent() {
        if (template.collectionExists(COLLECTION)) {
            template.getCollection(COLLECTION).drop();
        }
    }
}
