package com.mycompany.senaattendance.config.dbmigrations;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Rewrites the removed {@code TARDE} attendance state into {@code PRESENTE}, the state that
 * keeps the attendance counted as a presence now that the domain only defines
 * {@code PRESENTE}, {@code FALLA} and {@code JUSTIFICADA}.
 */
@ChangeUnit(id = "attendance-tarde-to-presente", order = "011")
public class MigrateAttendanceTardeToPresente {

    private static final String COLLECTION = "attendance";
    private static final String LEGACY_TARDE = "TARDE";
    private static final String PRESENTE = "PRESENTE";

    private final MongoTemplate template;

    public MigrateAttendanceTardeToPresente(MongoTemplate template) {
        this.template = template;
    }

    @Execution
    public void changeSet() {
        template
            .getCollection(COLLECTION)
            .updateMany(new Document("state_attendance", LEGACY_TARDE), new Document("$set", new Document("state_attendance", PRESENTE)));
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
