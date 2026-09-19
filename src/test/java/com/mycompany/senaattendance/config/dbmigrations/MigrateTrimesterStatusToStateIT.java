package com.mycompany.senaattendance.config.dbmigrations;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.IntegrationTest;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Verifies that {@link MigrateTrimesterStatusToState} rewrites the boolean {@code status} of a
 * {@code trimester} document into the {@code StateTrimester} value computed from its date range.
 */
@IntegrationTest
class MigrateTrimesterStatusToStateIT {

    private static final String COLLECTION = "trimester";
    private static final ObjectId CLOSED_ID = new ObjectId("64b7a1f2e4b0a1b2c3d4e571");
    private static final ObjectId ACTIVE_ID = new ObjectId("64b7a1f2e4b0a1b2c3d4e572");
    private static final ObjectId FUTURE_ID = new ObjectId("64b7a1f2e4b0a1b2c3d4e573");

    @Autowired
    private MongoTemplate template;

    @AfterEach
    void cleanup() {
        template.getCollection(COLLECTION).deleteMany(new Document("_id", new Document("$in", List.of(CLOSED_ID, ACTIVE_ID, FUTURE_ID))));
    }

    @Test
    void changeSetClassifiesEachLegacyStatusFromItsDateRange() {
        LocalDate today = LocalDate.now();
        insertLegacy(CLOSED_ID, today.minusDays(40), today.minusDays(10), true);
        insertLegacy(ACTIVE_ID, today.minusDays(10), today.plusDays(10), true);
        insertLegacy(FUTURE_ID, today.plusDays(10), today.plusDays(40), false);

        new MigrateTrimesterStatusToState(template).changeSet();

        assertThat(statusOf(CLOSED_ID)).isEqualTo("CERRADO");
        assertThat(statusOf(ACTIVE_ID)).isEqualTo("ACTIVO");
        assertThat(statusOf(FUTURE_ID)).isEqualTo("FUTURO");
    }

    private void insertLegacy(ObjectId id, LocalDate start, LocalDate end, boolean status) {
        template
            .getCollection(COLLECTION)
            .insertOne(
                new Document("_id", id)
                    .append("name", "Legacy trimester")
                    .append("start_date", toDate(start))
                    .append("end_date", toDate(end))
                    .append("status", status)
            );
    }

    private String statusOf(ObjectId id) {
        Document document = template.getCollection(COLLECTION).find(new Document("_id", id)).first();
        assertThat(document).isNotNull();
        return document.getString("status");
    }

    private static Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneOffset.UTC).toInstant());
    }
}
