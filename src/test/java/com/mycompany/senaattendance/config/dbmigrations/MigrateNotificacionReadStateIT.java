package com.mycompany.senaattendance.config.dbmigrations;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.IntegrationTest;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Verifies that {@link MigrateNotificacionReadState} backfills the {@code read} field of legacy
 * {@code notificacion} documents as unread without touching the notifications that already carry
 * a read state.
 */
@IntegrationTest
class MigrateNotificacionReadStateIT {

    private static final String COLLECTION = "notificacion";
    private static final String LEGACY_ID = "legacy-notificacion-read-migration-test";
    private static final String READ_ID = "read-notificacion-read-migration-test";

    @Autowired
    private MongoTemplate template;

    @AfterEach
    void cleanup() {
        template.getCollection(COLLECTION).deleteMany(new Document("_id", new Document("$in", java.util.List.of(LEGACY_ID, READ_ID))));
    }

    @Test
    void changeSetBackfillsLegacyDocumentsAsUnread() {
        template.getCollection(COLLECTION).insertOne(new Document("_id", LEGACY_ID).append("mensaje", "Legacy notification"));
        template
            .getCollection(COLLECTION)
            .insertOne(new Document("_id", READ_ID).append("mensaje", "Already read notification").append("read", true));

        new MigrateNotificacionReadState(template).changeSet();

        Document migrated = template.getCollection(COLLECTION).find(new Document("_id", LEGACY_ID)).first();
        assertThat(migrated).isNotNull();
        assertThat(migrated.getBoolean("read")).isFalse();

        Document untouched = template.getCollection(COLLECTION).find(new Document("_id", READ_ID)).first();
        assertThat(untouched).isNotNull();
        assertThat(untouched.getBoolean("read")).isTrue();
    }
}
