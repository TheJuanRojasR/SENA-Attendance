package com.mycompany.senaattendance.config.dbmigrations;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.IntegrationTest;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Verifies that {@link MigrateGradeInactivaToAplazada} rewrites legacy {@code INACTIVA}
 * states into {@code APLAZADA} while leaving other documents untouched.
 */
@IntegrationTest
class MigrateGradeInactivaToAplazadaIT {

    private static final String COLLECTION = "grade";
    private static final String LEGACY_ID = "legacy-grade-migration-inactiva";
    private static final String PAUSED_ID = "legacy-grade-migration-aplazada";

    @Autowired
    private MongoTemplate template;

    @AfterEach
    void cleanup() {
        template.getCollection(COLLECTION).deleteMany(new Document("_id", new Document("$in", List.of(LEGACY_ID, PAUSED_ID))));
    }

    @Test
    void changeSetRewritesInactivaToAplazada() {
        template.getCollection(COLLECTION).insertOne(new Document("_id", LEGACY_ID).append("code", "LEGACY").append("state", "INACTIVA"));
        template.getCollection(COLLECTION).insertOne(new Document("_id", PAUSED_ID).append("code", "PAUSED").append("state", "APLAZADA"));

        new MigrateGradeInactivaToAplazada(template).changeSet();

        assertThat(stateOf(LEGACY_ID)).isEqualTo("APLAZADA");
        assertThat(stateOf(PAUSED_ID)).isEqualTo("APLAZADA");
    }

    private String stateOf(String id) {
        Document document = template.getCollection(COLLECTION).find(new Document("_id", id)).first();
        assertThat(document).isNotNull();
        return document.getString("state");
    }
}
