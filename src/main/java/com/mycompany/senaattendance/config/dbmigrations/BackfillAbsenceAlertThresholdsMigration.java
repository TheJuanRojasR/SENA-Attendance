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
 * Backfills the absence alert thresholds of the {@code global_configuration} collection. Rows created
 * before these parameters existed have no value, so this migration sets the defaults
 * ({@code consecutiveAbsenceAlertThreshold = 3}, {@code accumulatedAbsenceAlertThreshold = 5}) to keep
 * the singleton valid for the {@code @NotNull} domain fields.
 */
@ChangeUnit(id = "backfill-absence-alert-thresholds", order = "006")
public class BackfillAbsenceAlertThresholdsMigration {

    private static final String COLLECTION = "global_configuration";

    private static final Integer DEFAULT_CONSECUTIVE_ABSENCE_ALERT_THRESHOLD = 3;
    private static final Integer DEFAULT_ACCUMULATED_ABSENCE_ALERT_THRESHOLD = 5;

    private final MongoTemplate template;

    public BackfillAbsenceAlertThresholdsMigration(MongoTemplate template) {
        this.template = template;
    }

    @Execution
    public void changeSet() {
        List<Document> documents = new ArrayList<>();
        MongoCollection<Document> collection = template.getCollection(COLLECTION);
        collection.find().into(documents);

        for (Document document : documents) {
            Document setFields = new Document();
            if (document.get("consecutive_absence_alert_threshold") == null) {
                setFields.put("consecutive_absence_alert_threshold", DEFAULT_CONSECUTIVE_ABSENCE_ALERT_THRESHOLD);
            }
            if (document.get("accumulated_absence_alert_threshold") == null) {
                setFields.put("accumulated_absence_alert_threshold", DEFAULT_ACCUMULATED_ABSENCE_ALERT_THRESHOLD);
            }
            if (!setFields.isEmpty()) {
                collection.updateOne(new Document("_id", document.get("_id")), new Document("$set", setFields));
            }
        }
    }

    @RollbackExecution
    public void rollback() {}
}
