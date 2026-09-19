package com.mycompany.senaattendance.config.dbmigrations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mycompany.senaattendance.IntegrationTest;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Verifies that {@link MigrateCatalogNameIndexes} creates the unique collated name indexes of the
 * catalogs, that a name which only differs in case collides once the index exists, that a legacy
 * database with duplicates fails with the list of colliding names without touching its data, and
 * that the rollback removes exactly the created indexes.
 */
@IntegrationTest
class MigrateCatalogNameIndexesIT {

    private static final List<String> CATALOG_COLLECTIONS = List.of(
        "modality",
        "time_slot",
        "document_type",
        "justification_type",
        "program"
    );

    private static final String COLLECTION = "modality";

    private static final String INDEX_NAME = "uk_modality_name_ci";

    private static final String FIRST_ID = "catalog-name-indexes-first";

    private static final String SECOND_ID = "catalog-name-indexes-second";

    private static final String FIRST_NAME = "Catalog index migration";

    private static final String SECOND_NAME = "catalog index migration";

    @Autowired
    private MongoTemplate template;

    @AfterEach
    void cleanup() {
        // Remove the seeded documents of every catalog, so the restored indexes stay unique
        for (String collectionName : CATALOG_COLLECTIONS) {
            template.getCollection(collectionName).deleteMany(new Document("_id", new Document("$in", List.of(FIRST_ID, SECOND_ID))));
        }
        // Recreate any index dropped by a test
        new MigrateCatalogNameIndexes(template).changeSet();
    }

    @Test
    void changeSetCreatesTheUniqueCollatedIndexForEveryCatalog() {
        new MigrateCatalogNameIndexes(template).rollback();
        assertThat(findIndex(COLLECTION, INDEX_NAME)).isNull();

        new MigrateCatalogNameIndexes(template).changeSet();

        for (String collectionName : CATALOG_COLLECTIONS) {
            Document index = findIndex(collectionName, indexName(collectionName));
            assertThat(index).as("unique name index of %s", collectionName).isNotNull();
            assertThat(index.getBoolean("unique")).isTrue();
            assertThat(index.get("key")).isEqualTo(new Document("name", 1));
            Document collation = index.get("collation", Document.class);
            assertThat(collation.getString("locale")).isEqualTo("es");
            assertThat(collation.getInteger("strength")).isEqualTo(2);
        }
    }

    @Test
    void changeSetRejectsANameThatOnlyDiffersInCase() {
        MongoCollection<Document> collection = template.getCollection(COLLECTION);
        collection.insertOne(new Document("_id", FIRST_ID).append("name", FIRST_NAME));

        assertThatThrownBy(() -> collection.insertOne(new Document("_id", SECOND_ID).append("name", SECOND_NAME))).isInstanceOf(
            MongoWriteException.class
        );
    }

    @Test
    void changeSetFailsListingLegacyDuplicatesWithoutTouchingTheData() {
        new MigrateCatalogNameIndexes(template).rollback();
        MongoCollection<Document> collection = template.getCollection(COLLECTION);
        collection.insertOne(new Document("_id", FIRST_ID).append("name", FIRST_NAME));
        collection.insertOne(new Document("_id", SECOND_ID).append("name", SECOND_NAME));

        assertThatThrownBy(() -> new MigrateCatalogNameIndexes(template).changeSet())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(COLLECTION)
            .hasMessageContaining(FIRST_NAME)
            .hasMessageContaining(SECOND_NAME);

        // No data was removed or rewritten and no index was created for any catalog
        assertThat(collection.find(new Document("_id", FIRST_ID)).first()).isNotNull();
        assertThat(collection.find(new Document("_id", SECOND_ID)).first()).isNotNull();
        for (String collectionName : CATALOG_COLLECTIONS) {
            assertThat(findIndex(collectionName, indexName(collectionName)))
                .as(collectionName)
                .isNull();
        }
    }

    @Test
    void rollbackDropsTheCreatedIndexes() {
        new MigrateCatalogNameIndexes(template).changeSet();

        new MigrateCatalogNameIndexes(template).rollback();

        for (String collectionName : CATALOG_COLLECTIONS) {
            assertThat(findIndex(collectionName, indexName(collectionName)))
                .as(collectionName)
                .isNull();
        }
    }

    private Document findIndex(String collectionName, String indexName) {
        for (Document index : template.getCollection(collectionName).listIndexes()) {
            if (indexName.equals(index.getString("name"))) {
                return index;
            }
        }
        return null;
    }

    private static String indexName(String collectionName) {
        return "uk_" + collectionName + "_name_ci";
    }
}
