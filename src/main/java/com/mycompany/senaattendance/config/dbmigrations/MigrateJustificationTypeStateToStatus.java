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
 * Renames the {@code state} field of the {@code justification_type} collection to {@code status},
 * keeping its enum value ({@code ACTIVO}/{@code INACTIVO}). The entity and DTO expose the property
 * as {@code status} now.
 */
@ChangeUnit(id = "justification-type-state-to-status", order = "008")
public class MigrateJustificationTypeStateToStatus {

    private static final String COLLECTION = "justification_type";

    private final MongoTemplate template;

    public MigrateJustificationTypeStateToStatus(MongoTemplate template) {
        this.template = template;
    }

    @Execution
    public void changeSet() {
        renameField("state", "status");
    }

    @RollbackExecution
    public void rollback() {
        renameField("status", "state");
    }

    private void renameField(String from, String to) {
        List<Document> documents = new ArrayList<>();
        MongoCollection<Document> collection = template.getCollection(COLLECTION);
        collection.find(new Document(from, new Document("$exists", true))).into(documents);

        for (Document document : documents) {
            collection.updateOne(
                new Document("_id", document.get("_id")),
                new Document("$set", new Document(to, document.get(from))).append("$unset", new Document(from, ""))
            );
        }
    }
}
