package com.mycompany.senaattendance.config.dbmigrations;

import com.mongodb.client.MongoCollection;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Backfills the read state of the {@code notificacion} collection (UC018). Documents created
 * before the read state existed have no {@code read} field, so this migration sets {@code false}
 * (unread) to keep every notification readable by the inbox.
 */
@ChangeUnit(id = "notificacion-read-state", order = "014")
public class MigrateNotificacionReadState {

    private static final String COLLECTION = "notificacion";

    private static final String READ_FIELD = "read";

    private final MongoTemplate template;

    public MigrateNotificacionReadState(MongoTemplate template) {
        this.template = template;
    }

    @Execution
    public void changeSet() {
        MongoCollection<Document> collection = template.getCollection(COLLECTION);
        collection.updateMany(
            new Document(READ_FIELD, new Document("$exists", false)),
            new Document("$set", new Document(READ_FIELD, false))
        );
    }

    /**
     * No-op: the backfilled {@code false} cannot be told apart from a legitimate read=false set
     * by a user, so reverting would corrupt the read state of notifications created after the
     * migration.
     */
    @RollbackExecution
    public void rollback() {}
}
