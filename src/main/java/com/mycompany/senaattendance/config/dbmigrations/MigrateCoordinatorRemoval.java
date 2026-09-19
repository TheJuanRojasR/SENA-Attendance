package com.mycompany.senaattendance.config.dbmigrations;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Removes the unused coordinator role from databases created before it was dropped: the
 * {@code ROLE_COORDINATOR} authority document, the seeded {@code coordinator} user and the
 * profile attached to that user, so no orphan document is left behind.
 */
@ChangeUnit(id = "coordinator-role-removal", order = "013")
public class MigrateCoordinatorRemoval {

    private static final String AUTHORITY_COLLECTION = "authority";
    private static final String USER_COLLECTION = "user";
    private static final String USER_PROFILE_COLLECTION = "user_profile";
    private static final String COORDINATOR_AUTHORITY = "ROLE_COORDINATOR";
    private static final String COORDINATOR_LOGIN = "coordinator";
    private static final String ID_FIELD = "_id";

    private final MongoTemplate template;

    public MigrateCoordinatorRemoval(MongoTemplate template) {
        this.template = template;
    }

    @Execution
    public void changeSet() {
        // The ids are read as raw BSON values: persisted documents use ObjectId, so casting to String breaks.
        List<Object> coordinatorIds = template
            .getCollection(USER_COLLECTION)
            .find(new Document("login", COORDINATOR_LOGIN))
            .map(document -> document.get(ID_FIELD))
            .into(new ArrayList<>());

        if (!coordinatorIds.isEmpty()) {
            template.getCollection(USER_PROFILE_COLLECTION).deleteMany(new Document("user.$id", new Document("$in", coordinatorIds)));
            template.getCollection(USER_COLLECTION).deleteMany(new Document(ID_FIELD, new Document("$in", coordinatorIds)));
        }

        template.getCollection(AUTHORITY_COLLECTION).deleteMany(new Document(ID_FIELD, COORDINATOR_AUTHORITY));
    }

    /**
     * The deleted authority, user and profile cannot be restored with their original ids or audit
     * fields, so the rollback is intentionally a no-op.
     */
    @RollbackExecution
    public void rollback() {
        // Intentionally empty: see Javadoc.
    }
}
