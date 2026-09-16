package com.mycompany.senaattendance.config.dbmigrations;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Drops the {@code desertion_counter} collection (UC013). The desertion counter is replaced by
 * the absence alerts, so its documents are no longer read or written by the application.
 */
@ChangeUnit(id = "desertion-counter-drop", order = "015")
public class MigrateDesertionCounterDrop {

    private static final String COLLECTION = "desertion_counter";

    private final MongoTemplate template;

    public MigrateDesertionCounterDrop(MongoTemplate template) {
        this.template = template;
    }

    @Execution
    public void changeSet() {
        if (template.collectionExists(COLLECTION)) {
            template.getCollection(COLLECTION).drop();
        }
    }

    /**
     * No-op: dropping a collection destroys its documents and the counters cannot be rebuilt from
     * the remaining data, so the migration is not invertible.
     */
    @RollbackExecution
    public void rollback() {}
}
