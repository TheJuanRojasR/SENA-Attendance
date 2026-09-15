package com.mycompany.senaattendance.config.dbmigrations;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Rewrites the removed {@code INACTIVA} ficha state into {@code APLAZADA}, the manual state
 * that keeps the document out of operation without altering its date range.
 */
@ChangeUnit(id = "grade-inactiva-to-aplazada", order = "010")
public class MigrateGradeInactivaToAplazada {

    private static final String COLLECTION = "grade";
    private static final String LEGACY_INACTIVA = "INACTIVA";
    private static final String APLAZADA = "APLAZADA";

    private final MongoTemplate template;

    public MigrateGradeInactivaToAplazada(MongoTemplate template) {
        this.template = template;
    }

    @Execution
    public void changeSet() {
        template
            .getCollection(COLLECTION)
            .updateMany(new Document("state", LEGACY_INACTIVA), new Document("$set", new Document("state", APLAZADA)));
    }

    /**
     * The inverse cannot tell a migrated {@code APLAZADA} apart from one an administrator paused
     * legitimately, so reverting would corrupt genuine states. It is intentionally a no-op.
     */
    @RollbackExecution
    public void rollback() {
        // Intentionally empty: see Javadoc.
    }
}
