package com.mycompany.senaattendance.config.dbmigrations;

import com.mongodb.client.MongoCollection;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Migrates the {@code trimester} collection from a string {@code state} field ({@code ACTIVO}/{@code INACTIVO})
 * to a boolean {@code status} field ({@code true} for active, {@code false} for inactive), dropping the old field.
 *
 * <p>The shared {@code State} enum is still used by {@code JustificationType}; this change only affects
 * documents in the {@code trimester} collection.
 */
@ChangeUnit(id = "trimester-state-to-status", order = "003")
public class MigrateTrimesterStateToStatus {

    private static final String COLLECTION = "trimester";

    private final MongoTemplate template;

    public MigrateTrimesterStateToStatus(MongoTemplate template) {
        this.template = template;
    }

    @Execution
    public void changeSet() {
        List<Document> documents = new ArrayList<>();
        MongoCollection<Document> collection = template.getCollection(COLLECTION);
        collection.find().into(documents);

        for (Document document : documents) {
            Boolean status = toStatus(document.get("state"));
            Document update = new Document();
            if (status != null) {
                update.append("$set", new Document("status", status));
            }
            update.append("$unset", new Document("state", ""));
            collection.updateOne(new Document("_id", document.get("_id")), update);
        }
    }

    @RollbackExecution
    public void rollback() {
        List<Document> documents = new ArrayList<>();
        MongoCollection<Document> collection = template.getCollection(COLLECTION);
        collection.find().into(documents);

        for (Document document : documents) {
            String state = toState(document.get("status"));
            if (state != null) {
                Document update = new Document();
                update.append("$set", new Document("state", state));
                update.append("$unset", new Document("status", ""));
                collection.updateOne(new Document("_id", document.get("_id")), update);
            }
        }
    }

    private static Boolean toStatus(Object state) {
        if ("ACTIVO".equals(state)) {
            return Boolean.TRUE;
        }
        if ("INACTIVO".equals(state)) {
            return Boolean.FALSE;
        }
        return null;
    }

    private static String toState(Object status) {
        if (Boolean.TRUE.equals(status)) {
            return "ACTIVO";
        }
        if (Boolean.FALSE.equals(status)) {
            return "INACTIVO";
        }
        return null;
    }
}
