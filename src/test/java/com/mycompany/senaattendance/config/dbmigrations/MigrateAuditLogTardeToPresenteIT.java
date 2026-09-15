package com.mycompany.senaattendance.config.dbmigrations;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.IntegrationTest;
import java.util.List;
import org.bson.Document;
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
    private static final String LEGACY_ID = "legacy-audit-log-migration-tarde";
    private static final String FAILED_ID = "legacy-audit-log-migration-falla";

    @Autowired
    private MongoTemplate template;

    @AfterEach
    void cleanup() {
        template.getCollection(COLLECTION).deleteMany(new Document("_id", new Document("$in", List.of(LEGACY_ID, FAILED_ID))));
    }

    @Test
    void changeSetRewritesTardeToPresenteInBothStates() {
        template
            .getCollection(COLLECTION)
            .insertOne(
                new Document("_id", LEGACY_ID)
                    .append("previous_state", "TARDE")
                    .append("new_state", "TARDE")
                    .append("edit_date", "2026-01-01T00:00:00Z")
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

    private String stateOf(String id, String field) {
        Document document = template.getCollection(COLLECTION).find(new Document("_id", id)).first();
        assertThat(document).isNotNull();
        return document.getString(field);
    }
}
