package com.mycompany.senaattendance.config.dbmigrations;

import com.mongodb.client.MongoCollection;
import com.mycompany.senaattendance.domain.GlobalConfiguration;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Consolidates the {@code global_configuration} collection into the singleton document with the fixed id
 * {@link GlobalConfiguration#GLOBAL_CONFIGURATION_ID}.
 *
 * <p>Databases created before the singleton was a hard guarantee may hold a row with a generated id, or even
 * several rows left behind by previous multi-row writes. This migration picks the existing singleton when present,
 * otherwise the earliest row, copies its fields into the fixed-id document and drops every other row.
 */
@ChangeUnit(id = "consolidate-global-configuration-singleton", order = "007")
public class ConsolidateGlobalConfigurationSingletonMigration {

    private static final String COLLECTION = "global_configuration";

    private final MongoTemplate template;

    public ConsolidateGlobalConfigurationSingletonMigration(MongoTemplate template) {
        this.template = template;
    }

    @Execution
    public void changeSet() {
        MongoCollection<Document> collection = template.getCollection(COLLECTION);
        List<Document> documents = new ArrayList<>();
        collection.find().sort(new Document("_id", 1)).into(documents);

        if (documents.isEmpty()) {
            return;
        }

        // Keep the existing singleton when present, otherwise the earliest row.
        Document singleton = documents
            .stream()
            .filter(document -> GlobalConfiguration.GLOBAL_CONFIGURATION_ID.equals(document.get("_id")))
            .findFirst()
            .orElse(documents.get(0));

        if (!GlobalConfiguration.GLOBAL_CONFIGURATION_ID.equals(singleton.get("_id"))) {
            Document consolidated = new Document(singleton);
            consolidated.put("_id", GlobalConfiguration.GLOBAL_CONFIGURATION_ID);
            collection.insertOne(consolidated);
        }

        collection.deleteMany(new Document("_id", new Document("$ne", GlobalConfiguration.GLOBAL_CONFIGURATION_ID)));
    }

    @RollbackExecution
    public void rollback() {}
}
