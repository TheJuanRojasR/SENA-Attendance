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
 * Verifies that {@link MigrateCoordinatorRemoval} deletes the legacy {@code ROLE_COORDINATOR}
 * authority, the seeded {@code coordinator} user and its profile while leaving other documents
 * untouched.
 */
@IntegrationTest
class MigrateCoordinatorRemovalIT {

    private static final String AUTHORITY_COLLECTION = "authority";
    private static final String USER_COLLECTION = "user";
    private static final String USER_PROFILE_COLLECTION = "user_profile";
    private static final String COORDINATOR_AUTHORITY = "ROLE_COORDINATOR";
    private static final String COORDINATOR_LOGIN = "coordinator";
    private static final String COORDINATOR_USER_ID = "coordinator-migration-user";
    private static final String COORDINATOR_PROFILE_ID = "coordinator-migration-profile";
    private static final String KEPT_AUTHORITY = "ROLE_KEPT";
    private static final String KEPT_USER_ID = "coordinator-migration-kept-user";

    @Autowired
    private MongoTemplate template;

    @AfterEach
    void cleanup() {
        template.getCollection(USER_PROFILE_COLLECTION).deleteMany(new Document("_id", COORDINATOR_PROFILE_ID));
        template
            .getCollection(USER_COLLECTION)
            .deleteMany(new Document("_id", new Document("$in", List.of(COORDINATOR_USER_ID, KEPT_USER_ID))));
        template
            .getCollection(AUTHORITY_COLLECTION)
            .deleteMany(new Document("_id", new Document("$in", List.of(COORDINATOR_AUTHORITY, KEPT_AUTHORITY))));
    }

    @Test
    void changeSetRemovesCoordinatorAuthorityUserAndProfile() {
        template.getCollection(AUTHORITY_COLLECTION).insertOne(new Document("_id", COORDINATOR_AUTHORITY));
        template.getCollection(AUTHORITY_COLLECTION).insertOne(new Document("_id", KEPT_AUTHORITY));
        template.getCollection(USER_COLLECTION).insertOne(new Document("_id", COORDINATOR_USER_ID).append("login", COORDINATOR_LOGIN));
        template.getCollection(USER_COLLECTION).insertOne(new Document("_id", KEPT_USER_ID).append("login", "kept"));
        template
            .getCollection(USER_PROFILE_COLLECTION)
            .insertOne(
                new Document("_id", COORDINATOR_PROFILE_ID).append("user", new Document("$ref", "user").append("$id", COORDINATOR_USER_ID))
            );

        new MigrateCoordinatorRemoval(template).changeSet();

        assertThat(authorityExists(COORDINATOR_AUTHORITY)).isFalse();
        assertThat(authorityExists(KEPT_AUTHORITY)).isTrue();
        assertThat(userExists(COORDINATOR_USER_ID)).isFalse();
        assertThat(userExists(KEPT_USER_ID)).isTrue();
        assertThat(profileExists(COORDINATOR_PROFILE_ID)).isFalse();
    }

    private boolean authorityExists(String id) {
        return template.getCollection(AUTHORITY_COLLECTION).find(new Document("_id", id)).first() != null;
    }

    private boolean userExists(String id) {
        return template.getCollection(USER_COLLECTION).find(new Document("_id", id)).first() != null;
    }

    private boolean profileExists(String id) {
        return template.getCollection(USER_PROFILE_COLLECTION).find(new Document("_id", id)).first() != null;
    }
}
