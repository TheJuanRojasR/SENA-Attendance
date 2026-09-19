package com.mycompany.senaattendance.config.dbmigrations;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.CollationStrength;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Creates the unique, case-insensitive index that backs the name uniqueness of the catalogs whose
 * services validate it in the application layer (UC015, UC016, UC020, UC021 and UC022):
 * {@code modality}, {@code time_slot}, {@code document_type}, {@code justification_type} and
 * {@code program}. The application check cannot stop two concurrent creates, so the database must
 * enforce the same rule with a collated unique index.
 *
 * <p>The collation is {@code es} with {@link CollationStrength#SECONDARY}: names that only differ
 * in case collide (the rule {@code existsByNameIgnoreCase} validates), while accents remain
 * significant. {@code trimester} is not included: its service never validates the name as unique
 * (the invariant of that catalog is the non-overlapping date range).
 *
 * <p>Before creating an index the migration looks for existing duplicates under the same
 * collation. Duplicates are never deleted or rewritten: when any exists, no index is created and
 * the migration fails with a message listing the colliding names, so a human decides which
 * document to keep and can re-run the migration afterwards.
 */
@ChangeUnit(id = "catalog-name-indexes", order = "016")
public class MigrateCatalogNameIndexes {

    /**
     * Collections whose {@code name} uniqueness is validated by the application.
     */
    private static final List<String> CATALOG_COLLECTIONS = List.of(
        "modality",
        "time_slot",
        "document_type",
        "justification_type",
        "program"
    );

    /**
     * Collation shared by the duplicate detection and the indexes, so both apply the same
     * case-insensitive comparison.
     */
    private static final Collation NAME_COLLATION = Collation.builder().locale("es").collationStrength(CollationStrength.SECONDARY).build();

    private static final String DUPLICATES_MESSAGE_PREFIX =
        "Catalog name indexes were not created: the following collections already contain names that collide case-insensitively. " +
        "No data was modified; resolve the duplicates manually and run the migration again. Colliding names: ";

    private final MongoTemplate template;

    public MigrateCatalogNameIndexes(MongoTemplate template) {
        this.template = template;
    }

    /**
     * Fails before creating anything when a catalog already has case-insensitive duplicates;
     * otherwise creates the five unique name indexes with the shared collation.
     *
     * @throws IllegalStateException when any catalog already contains colliding names.
     */
    @Execution
    public void changeSet() {
        Map<String, List<String>> duplicates = findDuplicates();
        if (!duplicates.isEmpty()) {
            throw new IllegalStateException(DUPLICATES_MESSAGE_PREFIX + duplicates);
        }
        for (String collectionName : CATALOG_COLLECTIONS) {
            createNameIndex(collectionName);
        }
    }

    /**
     * Drops the five indexes created by the change set. Dropping an index does not touch the
     * catalog documents, so the change unit is invertible.
     */
    @RollbackExecution
    public void rollback() {
        for (String collectionName : CATALOG_COLLECTIONS) {
            if (!template.collectionExists(collectionName)) {
                continue;
            }
            MongoCollection<Document> collection = template.getCollection(collectionName);
            String indexName = indexName(collectionName);
            boolean exists = collection
                .listIndexes()
                .into(new ArrayList<>())
                .stream()
                .anyMatch(index -> indexName.equals(index.getString("name")));
            if (exists) {
                collection.dropIndex(indexName);
            }
        }
    }

    /**
     * Resolves, per collection, the names that collide under the shared collation (for example
     * {@code "A"} and {@code "a"}). A collection with no collision is omitted from the result.
     *
     * @return the colliding names by collection name, empty when every catalog is clean.
     */
    private Map<String, List<String>> findDuplicates() {
        Map<String, List<String>> duplicates = new LinkedHashMap<>();
        for (String collectionName : CATALOG_COLLECTIONS) {
            List<String> collidingNames = findCollidingNames(collectionName);
            if (!collidingNames.isEmpty()) {
                duplicates.put(collectionName, collidingNames);
            }
        }
        return duplicates;
    }

    /**
     * Groups the collection by {@code name} under the shared collation and returns every name of
     * the groups with more than one document. {@code $push} is used instead of {@code $addToSet}
     * so the colliding spellings are all reported.
     *
     * @param collectionName the collection to inspect.
     * @return the colliding names, possibly empty.
     */
    private List<String> findCollidingNames(String collectionName) {
        List<Document> groups = new ArrayList<>();
        template
            .getCollection(collectionName)
            .aggregate(
                List.of(
                    new Document(
                        "$group",
                        new Document("_id", "$name")
                            .append("names", new Document("$push", "$name"))
                            .append("count", new Document("$sum", 1))
                    ),
                    new Document("$match", new Document("count", new Document("$gt", 1)))
                )
            )
            .collation(NAME_COLLATION)
            .into(groups);

        return groups
            .stream()
            .flatMap(group -> ((List<?>) Objects.requireNonNull(group.get("names"))).stream())
            .map(String::valueOf)
            .distinct()
            .toList();
    }

    /**
     * Creates the unique name index of one catalog with the shared collation. The index name is
     * deterministic so the rollback can drop exactly what this migration created.
     *
     * @param collectionName the collection that receives the index.
     */
    private void createNameIndex(String collectionName) {
        template
            .getCollection(collectionName)
            .createIndex(
                Indexes.ascending("name"),
                new IndexOptions().unique(true).name(indexName(collectionName)).collation(NAME_COLLATION)
            );
    }

    /**
     * @param collectionName the catalog collection.
     * @return the deterministic name of the unique name index.
     */
    private static String indexName(String collectionName) {
        return "uk_" + collectionName + "_name_ci";
    }
}
