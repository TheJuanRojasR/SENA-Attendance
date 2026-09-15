package com.mycompany.senaattendance.config.dbmigrations;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Rewrites the removed {@code TARDE} attendance state into {@code PRESENTE} inside the legacy
 * audit entries, so listing the audit log does not fail on a value the domain no longer defines.
 */
@ChangeUnit(id = "audit-log-tarde-to-presente", order = "012")
public class MigrateAuditLogTardeToPresente {

    private static final String COLLECTION = "audit_log";
    private static final String PREVIOUS_STATE = "previous_state";
    private static final String NEW_STATE = "new_state";
    private static final String LEGACY_TARDE = "TARDE";
    private static final String PRESENTE = "PRESENTE";

    private final MongoTemplate template;

    public MigrateAuditLogTardeToPresente(MongoTemplate template) {
        this.template = template;
    }

    @Execution
    public void changeSet() {
        for (String field : new String[] { PREVIOUS_STATE, NEW_STATE }) {
            template
                .getCollection(COLLECTION)
                .updateMany(new Document(field, LEGACY_TARDE), new Document("$set", new Document(field, PRESENTE)));
        }
    }

    /**
     * The inverse cannot tell a migrated {@code PRESENTE} apart from one recorded as a plain
     * presence, so reverting would corrupt genuine states. It is intentionally a no-op.
     */
    @RollbackExecution
    public void rollback() {
        // Intentionally empty: see Javadoc.
    }
}
