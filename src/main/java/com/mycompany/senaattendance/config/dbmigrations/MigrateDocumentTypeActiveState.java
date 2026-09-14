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
 * Backfills the {@code is_active} field of the {@code document_type} collection. Types created before
 * the field existed have no value, and the registration flow treats those legacy documents as active,
 * so this migration makes that state explicit by setting {@code is_active} to {@code true}.
 */
@ChangeUnit(id = "document-type-active-state", order = "005")
public class MigrateDocumentTypeActiveState {

    private static final String COLLECTION = "document_type";

    private final MongoTemplate template;

    public MigrateDocumentTypeActiveState(MongoTemplate template) {
        this.template = template;
    }

    @Execution
    public void changeSet() {
        List<Document> documents = new ArrayList<>();
        MongoCollection<Document> collection = template.getCollection(COLLECTION);
        collection.find().into(documents);

        for (Document document : documents) {
            if (document.get("is_active") == null) {
                collection.updateOne(
                    new Document("_id", document.get("_id")),
                    new Document("$set", new Document("is_active", Boolean.TRUE))
                );
            }
        }
    }

    @RollbackExecution
    public void rollback() {}
}
