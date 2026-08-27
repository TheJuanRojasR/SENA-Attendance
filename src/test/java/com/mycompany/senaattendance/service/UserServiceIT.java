package com.mycompany.senaattendance.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.DocumentType;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.repository.DocumentTypeRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tech.jhipster.security.RandomUtil;

/**
 * Integration tests for {@link UserService}.
 */
@IntegrationTest
class UserServiceIT {

    private static final String DEFAULT_LOGIN = "johndoe_service";

    private static final String DEFAULT_EMAIL = "johndoe_service@example.com";

    private static final String DEFAULT_IMAGEURL = "http://placehold.it/50x50";

    private static final String DEFAULT_LANGKEY = "dummy";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private UserService userService;

    private User user;

    @BeforeEach
    void init() {
        user = new User();
        user.setLogin(DEFAULT_LOGIN);
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(true);
        user.setEmail(DEFAULT_EMAIL);
        user.setImageUrl(DEFAULT_IMAGEURL);
        user.setLangKey(DEFAULT_LANGKEY);
    }

    @AfterEach
    void cleanupAndCheck() {
        userProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void assertThatUserAndProfileMustExistToResetPassword() {
        String documentTypeId = seededDocumentTypeId();

        // Build an activated user and link it to a UserProfile against a real seeded DocumentType
        User matchingUser = new User();
        matchingUser.setLogin("reset-activated-login");
        matchingUser.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        matchingUser.setActivated(true);
        matchingUser.setEmail("reset-activated@example.com");
        matchingUser.setImageUrl(DEFAULT_IMAGEURL);
        matchingUser.setLangKey(DEFAULT_LANGKEY);
        userRepository.save(matchingUser);

        UserProfile profile = new UserProfile()
            .firstName("Reset")
            .firstLastName("User")
            .documentNumber("RESET-DOC-0001")
            .phoneNumber("3000000001")
            .user(matchingUser)
            .documentType(documentTypeRepository.findById(documentTypeId).orElseThrow());
        userProfileRepository.save(profile);

        // Case A: no profile matches the document number -> empty
        Optional<User> maybeUser = userService.requestPasswordReset(documentTypeId, "NONEXISTENT-DOC-NUMBER");
        assertThat(maybeUser).isNotPresent();

        // Case B: valid activated profile matches -> reset key and date are set
        maybeUser = userService.requestPasswordReset(documentTypeId, profile.getDocumentNumber());
        assertThat(maybeUser).isPresent();
        assertThat(maybeUser.orElse(null).getEmail()).isEqualTo(matchingUser.getEmail());
        assertThat(maybeUser.orElse(null).getResetDate()).isNotNull();
        assertThat(maybeUser.orElse(null).getResetKey()).isNotNull();
    }

    @Test
    void assertThatOnlyActivatedUserCanRequestPasswordReset() {
        String documentTypeId = seededDocumentTypeId();

        // Build a NON-activated user and link it to a UserProfile against a real seeded DocumentType
        User inactiveUser = new User();
        inactiveUser.setLogin("reset-inactive-login");
        inactiveUser.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        inactiveUser.setActivated(false);
        inactiveUser.setEmail("reset-inactive@example.com");
        inactiveUser.setImageUrl(DEFAULT_IMAGEURL);
        inactiveUser.setLangKey(DEFAULT_LANGKEY);
        userRepository.save(inactiveUser);

        UserProfile profile = new UserProfile()
            .firstName("Inactive")
            .firstLastName("User")
            .documentNumber("RESET-DOC-0002")
            .phoneNumber("3000000002")
            .user(inactiveUser)
            .documentType(documentTypeRepository.findById(documentTypeId).orElseThrow());
        userProfileRepository.save(profile);

        // The linked user is not activated -> the reset must not be granted
        Optional<User> maybeUser = userService.requestPasswordReset(documentTypeId, profile.getDocumentNumber());
        assertThat(maybeUser).isNotPresent();
    }

    @Test
    void assertThatResetKeyMustNotBeOlderThan24Hours() {
        Instant daysAgo = Instant.now().minus(25, ChronoUnit.HOURS);
        String resetKey = RandomUtil.generateResetKey();
        user.setActivated(true);
        user.setResetDate(daysAgo);
        user.setResetKey(resetKey);
        userRepository.save(user);

        Optional<User> maybeUser = userService.completePasswordReset("johndoe2", user.getResetKey());
        assertThat(maybeUser).isNotPresent();
        userRepository.delete(user);
    }

    @Test
    void assertThatResetKeyMustBeValid() {
        Instant daysAgo = Instant.now().minus(25, ChronoUnit.HOURS);
        user.setActivated(true);
        user.setResetDate(daysAgo);
        user.setResetKey("1234");
        userRepository.save(user);

        Optional<User> maybeUser = userService.completePasswordReset("johndoe2", user.getResetKey());
        assertThat(maybeUser).isNotPresent();
        userRepository.delete(user);
    }

    @Test
    void assertThatUserCanResetPassword() {
        String oldPassword = user.getPassword();
        Instant daysAgo = Instant.now().minus(2, ChronoUnit.HOURS);
        String resetKey = RandomUtil.generateResetKey();
        user.setActivated(true);
        user.setResetDate(daysAgo);
        user.setResetKey(resetKey);
        userRepository.save(user);

        Optional<User> maybeUser = userService.completePasswordReset("johndoe2", user.getResetKey());
        assertThat(maybeUser).isPresent();
        assertThat(maybeUser.orElse(null).getResetDate()).isNull();
        assertThat(maybeUser.orElse(null).getResetKey()).isNull();
        assertThat(maybeUser.orElse(null).getPassword()).isNotEqualTo(oldPassword);

        userRepository.delete(user);
    }

    @Test
    void assertThatNotActivatedUsersWithNotNullActivationKeyCreatedBefore3DaysAreDeleted() {
        Instant now = Instant.now();
        user.setActivated(false);
        user.setActivationKey(RandomStringUtils.insecure().next(20));
        User dbUser = userRepository.save(user);
        dbUser.setCreatedDate(now.minus(4, ChronoUnit.DAYS));
        userRepository.save(user);
        Instant threeDaysAgo = now.minus(3, ChronoUnit.DAYS);
        List<User> users = userRepository.findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(threeDaysAgo);
        assertThat(users).isNotEmpty();
        userService.removeNotActivatedUsers();
        users = userRepository.findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(threeDaysAgo);
        assertThat(users).isEmpty();
    }

    @Test
    void assertThatNotActivatedUsersWithNullActivationKeyCreatedBefore3DaysAreNotDeleted() {
        Instant now = Instant.now();
        user.setActivated(false);
        User dbUser = userRepository.save(user);
        dbUser.setCreatedDate(now.minus(4, ChronoUnit.DAYS));
        userRepository.save(user);
        Instant threeDaysAgo = now.minus(3, ChronoUnit.DAYS);
        List<User> users = userRepository.findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(threeDaysAgo);
        assertThat(users).isEmpty();
        userService.removeNotActivatedUsers();
        Optional<User> maybeDbUser = userRepository.findById(dbUser.getId());
        assertThat(maybeDbUser).contains(dbUser);
    }

    /**
     * Resolves a real seeded {@link DocumentType} id (CC/TI/CE/PA are loaded at startup).
     */
    private String seededDocumentTypeId() {
        return documentTypeRepository.findAll().iterator().next().getId();
    }
}
