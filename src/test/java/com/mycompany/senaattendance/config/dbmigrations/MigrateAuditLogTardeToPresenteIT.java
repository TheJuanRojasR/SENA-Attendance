package com.mycompany.senaattendance.config.dbmigrations;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.IntegrationTest;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Verifies that {@link MigrateAuditLogTardeToPresente} rewrites legacy {@code TARDE} audit
 * states into {@code PRESENTE} on both the previous and the new state while leaving other
 * states untouched.
 */
@IntegrationTest
class MigrateAuditLogTardeToPresenteIT {

    private static final String COLLECTION = "audit_log";
    private static final ObjectId LEGACY_ID = new ObjectId("64b7a1f2e4b0a1b2c3d4e531");
    private static final ObjectId FAILED_ID = new ObjectId("64b7a1f2e4b0a1b2c3d4e532");

    @Autowired
    private MongoTemplate template;

    @AfterEach
    void cleanup() {
        template.getCollection(COLLECTION).deleteMany(new Document("_id", new Document("$in", List.of(LEGACY_ID, FAILED_ID))));
    }

    @Test
    void changeSetRewritesTardeToPresenteInBothStates() {
        template.getCollection(COLLECTION).insertOne(
            new Document("_id", LEGACY_ID)
                .append("previous_state", "TARDE")
                .append("new_state", "TARDE")
                .append("edit_date", Date.from(Instant.parse("2026-01-01T00:00:00Z")))
        );
        template
            .getCollection(COLLECTION)
            .insertOne(new Document("_id", FAILED_ID).append("previous_state", "PRESENTE").append("new_state", "FALLA"));

        new MigrateAuditLogTardeToPresente(template).changeSet();

        assertThat(stateOf(LEGACY_ID, "previous_state")).isEqualTo("PRESENTE");
        assertThat(stateOf(LEGACY_ID, "new_state")).isEqualTo("PRESENTE");
        assertThat(stateOf(FAILED_ID, "previous_state")).isEqualTo("PRESENTE");
        assertThat(stateOf(FAILED_ID, "new_state")).isEqualTo("FALLA");
    }

    private String stateOf(ObjectId id, String field) {
        Document document = template.getCollection(COLLECTION).find(new Document("_id", id)).first();
        assertThat(document).isNotNull();
        return document.getString(field);
    }
}
