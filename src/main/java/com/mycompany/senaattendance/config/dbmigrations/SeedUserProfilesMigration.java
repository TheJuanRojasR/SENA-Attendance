package com.mycompany.senaattendance.config.dbmigrations;

import com.mycompany.senaattendance.config.Constants;
import com.mycompany.senaattendance.domain.DocumentType;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
 * Seeds the DocumentType catalog and attaches a UserProfile to the default users (admin,
 * instructor, apprentice) so they can authenticate by document type + document number.
 */
@ChangeUnit(id = "seed-user-profiles", order = "002")
public class SeedUserProfilesMigration {

    private static final String[] SEED_LOGINS = { "admin", "instructor", "apprentice" };

    private final MongoTemplate template;

    public SeedUserProfilesMigration(MongoTemplate template) {
        this.template = template;
    }

    @Execution
    public void changeSet() {
        seedDocumentTypes();
        attachUserProfiles();
    }

    @RollbackExecution
    public void rollback() {}

    private void seedDocumentTypes() {
        if (template.exists(new Query(), "document_type")) {
            return;
        }

        createDocumentType("Cédula de Ciudadanía", "CC");
        createDocumentType("Tarjeta de Identidad", "TI");
        createDocumentType("Cédula de Extranjería", "CE");
        createDocumentType("Pasaporte", "PA");
    }

    private void createDocumentType(String name, String initials) {
        DocumentType documentType = new DocumentType();
        documentType.setName(name);
        documentType.setInitials(initials);
        documentType.setCreatedBy(Constants.SYSTEM);
        documentType.setCreatedDate(Instant.now());
        template.save(documentType, "document_type");
    }

    private void attachUserProfiles() {
        for (String login : SEED_LOGINS) {
            User user = findUserByLogin(login);

            if (user == null || hasUserProfile(user)) {
                continue;
            }

            template.save(createPlaceholderProfile(user), "user_profile");
        }
    }

    private User findUserByLogin(String login) {
        Query query = Query.query(Criteria.where("login").is(login));
        List<User> users = template.find(query, User.class, "user");
        return users.stream().findFirst().orElse(null);
    }

    private boolean hasUserProfile(User user) {
        Query query = Query.query(Criteria.where("user.$id").is(user.getId()));
        return template.exists(query, "user_profile");
    }

    private UserProfile createPlaceholderProfile(User user) {
        String login = user.getLogin();
        String firstName = login.substring(0, 1).toUpperCase() + login.substring(1);

        UserProfile profile = new UserProfile();
        profile.setFirstName(firstName);
        profile.setFirstLastName("System");
        profile.setDocumentNumber(login);
        profile.setPhoneNumber("3000000000");
        profile.setUser(user);
        profile.setDocumentType(findDefaultDocumentType());
        profile.setCreatedBy(Constants.SYSTEM);
        profile.setCreatedDate(Instant.now());
        return profile;
    }

    private DocumentType findDefaultDocumentType() {
        Query query = new Query();
        if (!template.exists(query, "document_type")) {
            return null;
        }
        query.with(Sort.by(Sort.Direction.ASC, "_id"));
        List<DocumentType> types = template.find(query, DocumentType.class, "document_type");
        return types.stream().findFirst().orElse(null);
    }
}
