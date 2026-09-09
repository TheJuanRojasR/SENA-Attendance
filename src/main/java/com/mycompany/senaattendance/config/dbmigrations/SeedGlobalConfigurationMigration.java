package com.mycompany.senaattendance.config.dbmigrations;

import com.mycompany.senaattendance.config.Constants;
import com.mycompany.senaattendance.domain.GlobalConfiguration;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/**
 * Enforces the singleton model for the {@code global_configuration} collection
 */
@ChangeUnit(id = "seed-global-configuration", order = "004")
public class SeedGlobalConfigurationMigration {

    private static final String COLLECTION = "global_configuration";

    private static final Integer DEFAULT_STUDENT_JUSTIFICATION_DAYS = 5;
    private static final Integer DEFAULT_INSTRUCTOR_RESPONSE_DAYS = 2;

    private final MongoTemplate template;

    public SeedGlobalConfigurationMigration(MongoTemplate template) {
        this.template = template;
    }

    @Execution
    public void changeSet() {
        removeDeprecatedFields();
        enforceSingletonRow();
    }

    @RollbackExecution
    public void rollback() {}

    private void removeDeprecatedFields() {
        Update unsetDeprecatedFields = new Update()
            .unset("late_arrivals_to_fail")
            .unset("max_postponement_justifications")
            .unset("standard_trimester_months");
        template.updateMulti(new Query(), unsetDeprecatedFields, COLLECTION);
    }

    private void enforceSingletonRow() {
        List<GlobalConfiguration> configurations = template.find(
            new Query().with(Sort.by(Sort.Direction.ASC, "_id")),
            GlobalConfiguration.class,
            COLLECTION
        );

        // Keep the oldest row and drop any extra duplicates left behind by previous multi-row writes.
        configurations
            .stream()
            .skip(1)
            .forEach(configuration -> template.remove(configuration, COLLECTION));

        if (configurations.isEmpty()) {
            template.save(createDefaultConfiguration(), COLLECTION);
        }
    }

    private GlobalConfiguration createDefaultConfiguration() {
        GlobalConfiguration configuration = new GlobalConfiguration();
        configuration.setStudentJustificationDays(DEFAULT_STUDENT_JUSTIFICATION_DAYS);
        configuration.setInstructorResponseDays(DEFAULT_INSTRUCTOR_RESPONSE_DAYS);
        configuration.setCreatedBy(Constants.SYSTEM);
        configuration.setCreatedDate(Instant.now());
        return configuration;
    }
}
