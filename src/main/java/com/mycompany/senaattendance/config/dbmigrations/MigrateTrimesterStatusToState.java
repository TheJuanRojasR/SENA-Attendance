package com.mycompany.senaattendance.config.dbmigrations;

import com.mongodb.client.MongoCollection;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Rewrites the boolean {@code status} of the {@code trimester} collection into the
 * {@code StateTrimester} value ({@code FUTURO}/{@code ACTIVO}/{@code CERRADO}) computed from
 * each document's {@code [start_date, end_date]} range against today.
 */
@ChangeUnit(id = "trimester-status-to-state", order = "009")
public class MigrateTrimesterStatusToState {

    private static final String COLLECTION = "trimester";

    private final MongoTemplate template;

    public MigrateTrimesterStatusToState(MongoTemplate template) {
        this.template = template;
    }

    @Execution
    public void changeSet() {
        LocalDate today = LocalDate.now();
        List<Document> documents = new ArrayList<>();
        MongoCollection<Document> collection = template.getCollection(COLLECTION);
        collection.find().into(documents);

        for (Document document : documents) {
            LocalDate start = toLocalDate(document.get("start_date"));
            LocalDate end = toLocalDate(document.get("end_date"));
            if (start == null || end == null) {
                continue;
            }
            collection.updateOne(
                new Document("_id", document.get("_id")),
                new Document("$set", new Document("status", classifyState(today, start, end)))
            );
        }
    }

    /**
     * The inverse cannot reconstruct the discarded date-based state: a boolean only expresses
     * whether the trimester was active, so {@code ACTIVO} becomes {@code true} while
     * {@code FUTURO} and {@code CERRADO} both collapse to {@code false}.
     */
    @RollbackExecution
    public void rollback() {
        List<Document> documents = new ArrayList<>();
        MongoCollection<Document> collection = template.getCollection(COLLECTION);
        collection.find().into(documents);

        for (Document document : documents) {
            Object status = document.get("status");
            if (!(status instanceof String state)) {
                continue;
            }
            collection.updateOne(
                new Document("_id", document.get("_id")),
                new Document("$set", new Document("status", "ACTIVO".equals(state)))
            );
        }
    }

    private static String classifyState(LocalDate today, LocalDate start, LocalDate end) {
        if (end.isBefore(today)) {
            return "CERRADO";
        }
        if (start.isAfter(today)) {
            return "FUTURO";
        }
        return "ACTIVO";
    }

    /**
     * Spring Data Mongo persists {@code LocalDate} as a BSON {@code Date} at UTC midnight, so the
     * driver returns a {@link Date} here.
     */
    private static LocalDate toLocalDate(Object value) {
        if (value instanceof Date date) {
            return date.toInstant().atOffset(ZoneOffset.UTC).toLocalDate();
        }
        return null;
    }
}
