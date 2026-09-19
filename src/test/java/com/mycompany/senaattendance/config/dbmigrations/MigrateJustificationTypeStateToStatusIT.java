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
 * Verifies that {@link MigrateJustificationTypeStateToStatus} renames the {@code state} field of
 * legacy {@code justification_type} documents to {@code status} without losing its value.
 */
@IntegrationTest
class MigrateJustificationTypeStateToStatusIT {

    private static final String COLLECTION = "justification_type";
    private static final ObjectId LEGACY_ID = new ObjectId("64b7a1f2e4b0a1b2c3d4e551");

    @Autowired
    private MongoTemplate template;

    @AfterEach
    void cleanup() {
        template.getCollection(COLLECTION).deleteOne(new Document("_id", LEGACY_ID));
    }

    @Test
    void changeSetRenamesStateToStatusKeepingItsValue() {
        template
            .getCollection(COLLECTION)
            .insertOne(new Document("_id", LEGACY_ID).append("name", "Legacy type").append("state", "INACTIVO"));

        new MigrateJustificationTypeStateToStatus(template).changeSet();

        Document migrated = template.getCollection(COLLECTION).find(new Document("_id", LEGACY_ID)).first();
        assertThat(migrated).isNotNull();
        assertThat(migrated.getString("status")).isEqualTo("INACTIVO");
        assertThat(migrated).doesNotContainKey("state");
    }
}
