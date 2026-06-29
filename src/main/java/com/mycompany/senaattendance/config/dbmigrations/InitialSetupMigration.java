package com.mycompany.senaattendance.config.dbmigrations;

import com.mycompany.senaattendance.config.Constants;
import com.mycompany.senaattendance.domain.Authority;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import java.time.Instant;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Creates the initial database setup.
 */
@ChangeUnit(id = "users-initialization", order = "001")
public class InitialSetupMigration {

    private final MongoTemplate template;

    public InitialSetupMigration(MongoTemplate template) {
        this.template = template;
    }

    @Execution
    public void changeSet() {
        Authority userAuthority = createUserAuthority();
        userAuthority = template.save(userAuthority);
        Authority adminAuthority = createAdminAuthority();
        adminAuthority = template.save(adminAuthority);
        Authority instructorAuthority = createInstructorAuthority();
        instructorAuthority = template.save(instructorAuthority);
        Authority coordinatorAuthority = createCoordinatorAuthority();
        coordinatorAuthority = template.save(coordinatorAuthority);
        Authority apprenticeAuthority = createApprenticeAuthority();
        apprenticeAuthority = template.save(apprenticeAuthority);
        addUsers(userAuthority, adminAuthority, instructorAuthority, coordinatorAuthority, apprenticeAuthority);
    }

    @RollbackExecution
    public void rollback() {}

    private Authority createAuthority(String authority) {
        Authority adminAuthority = new Authority();
        adminAuthority.setName(authority);
        return adminAuthority;
    }

    private Authority createAdminAuthority() {
        Authority adminAuthority = createAuthority(AuthoritiesConstants.ADMIN);
        return adminAuthority;
    }

    private Authority createUserAuthority() {
        Authority userAuthority = createAuthority(AuthoritiesConstants.USER);
        return userAuthority;
    }

    private Authority createCoordinatorAuthority() {
        Authority coordinatorAuthority = createAuthority(AuthoritiesConstants.COORDINATOR);
        return coordinatorAuthority;
    }

    private Authority createInstructorAuthority() {
        Authority instructorAuthority = createAuthority(AuthoritiesConstants.INSTRUCTOR);
        return instructorAuthority;
    }

    private Authority createApprenticeAuthority() {
        Authority studentAuthority = createAuthority(AuthoritiesConstants.APPRENTICE);
        return studentAuthority;
    }

    private void addUsers(
        Authority userAuthority,
        Authority adminAuthority,
        Authority instructorAuthority,
        Authority coordinatorAuthority,
        Authority apprenticeAuthority
    ) {
        User user = createUser(userAuthority);
        template.save(user);
        User admin = createAdmin(adminAuthority, userAuthority);
        template.save(admin);
        User instructor = createInstructor(instructorAuthority, userAuthority);
        template.save(instructor);
        User coordinator = createCoordinator(coordinatorAuthority, userAuthority);
        template.save(coordinator);
        User apprentice = createApprentice(apprenticeAuthority, userAuthority);
        template.save(apprentice);
    }

    private User createUser(Authority userAuthority) {
        User userUser = new User();
        userUser.setLogin("user");
        userUser.setPassword("$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K");
        userUser.setEmail("user@localhost");
        userUser.setActivated(true);
        userUser.setLangKey("es");
        userUser.setCreatedBy(Constants.SYSTEM);
        userUser.setCreatedDate(Instant.now());
        userUser.getAuthorities().add(userAuthority);
        return userUser;
    }

    private User createAdmin(Authority adminAuthority, Authority userAuthority) {
        User adminUser = new User();
        adminUser.setLogin("admin");
        adminUser.setPassword("$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC");
        adminUser.setEmail("admin@localhost");
        adminUser.setActivated(true);
        adminUser.setLangKey("es");
        adminUser.setCreatedBy(Constants.SYSTEM);
        adminUser.setCreatedDate(Instant.now());
        adminUser.getAuthorities().add(adminAuthority);
        adminUser.getAuthorities().add(userAuthority);
        return adminUser;
    }

    private User createInstructor(Authority instructorAuthority, Authority userAuthority) {
        User instructorUser = new User();
        instructorUser.setLogin("instructor");
        instructorUser.setPassword("$2a$10$e8MSV72L8nRxanEtdgt4k.oKWSLiw7Jm4Jnh/FxZ1LsOa5GUbYd2u");
        instructorUser.setEmail("instructor@localhost");
        instructorUser.setActivated(true);
        instructorUser.setLangKey("es");
        instructorUser.setCreatedBy(Constants.SYSTEM);
        instructorUser.setCreatedDate(Instant.now());
        instructorUser.getAuthorities().add(instructorAuthority);
        instructorUser.getAuthorities().add(userAuthority);
        return instructorUser;
    }

    private User createCoordinator(Authority coordinatorAuthority, Authority userAuthority) {
        User coordinatorUser = new User();
        coordinatorUser.setLogin("coordinator");
        coordinatorUser.setPassword("$2a$10$qlNJJfrZe4UgUtvg88vf0O7cin4vRz/iBzG9io695hhhqF8.Kf3Hi");
        coordinatorUser.setEmail("Coordinator@localhost");
        coordinatorUser.setActivated(true);
        coordinatorUser.setLangKey("es");
        coordinatorUser.setCreatedBy(Constants.SYSTEM);
        coordinatorUser.setCreatedDate(Instant.now());
        coordinatorUser.getAuthorities().add(coordinatorAuthority);
        coordinatorUser.getAuthorities().add(userAuthority);
        return coordinatorUser;
    }

    private User createApprentice(Authority apprenticeAuthority, Authority userAuthority) {
        User apprenticeUser = new User();
        apprenticeUser.setLogin("apprentice");
        apprenticeUser.setPassword("$2a$10$Eo7PoD20ZiNqRi6uLfRjy.7HNlyYbJ43Hf8FEpNwgHUSsiZRdQv7y");
        apprenticeUser.setEmail("Apprentice@localhost");
        apprenticeUser.setActivated(true);
        apprenticeUser.setLangKey("es");
        apprenticeUser.setCreatedBy(Constants.SYSTEM);
        apprenticeUser.setCreatedDate(Instant.now());
        apprenticeUser.getAuthorities().add(apprenticeAuthority);
        apprenticeUser.getAuthorities().add(userAuthority);
        return apprenticeUser;
    }
}
