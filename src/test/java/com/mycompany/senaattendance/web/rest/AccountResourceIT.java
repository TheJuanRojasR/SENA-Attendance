package com.mycompany.senaattendance.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.config.Constants;
import com.mycompany.senaattendance.domain.Authority;
import com.mycompany.senaattendance.domain.DocumentType;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.repository.AuthorityRepository;
import com.mycompany.senaattendance.repository.DocumentTypeRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.UserService;
import com.mycompany.senaattendance.service.dto.PasswordChangeDTO;
import com.mycompany.senaattendance.web.rest.vm.AccountUpdateVM;
import com.mycompany.senaattendance.web.rest.vm.AdminCreateUserVM;
import com.mycompany.senaattendance.web.rest.vm.KeyAndPasswordVM;
import com.mycompany.senaattendance.web.rest.vm.ManagedUserVM;
import com.mycompany.senaattendance.web.rest.vm.PasswordResetRequestVM;
import java.time.Instant;
import java.util.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the {@link AccountResource} REST controller.
 */
@AutoConfigureMockMvc
@IntegrationTest
class AccountResourceIT {

    static final String TEST_USER_LOGIN = "test";

    private static final String VALID_PASSWORD = "Passw0rd!";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc restAccountMockMvc;

    @AfterEach
    void cleanupAndCheck() {
        userProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @WithUnauthenticatedMockUser
    void testNonAuthenticatedUser() throws Exception {
        restAccountMockMvc.perform(get("/api/authenticate")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(TEST_USER_LOGIN)
    void testAuthenticatedUser() throws Exception {
        restAccountMockMvc.perform(get("/api/authenticate").with(request -> request)).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(TEST_USER_LOGIN)
    void testGetExistingAccount() throws Exception {
        // The user's login is fixed (not derived) so the @WithMockUser principal matches.
        User accountUser = new User();
        accountUser.setLogin(TEST_USER_LOGIN);
        accountUser.setPassword(passwordEncoder.encode(VALID_PASSWORD));
        accountUser.setEmail("john.doe@jhipster.com");
        accountUser.setLangKey("en");
        accountUser.setActivated(true);
        accountUser.setAuthorities(
            new HashSet<>(
                Set.of(
                    authorityRepository.findById(AuthoritiesConstants.USER).orElseThrow(),
                    authorityRepository.findById(AuthoritiesConstants.ADMIN).orElseThrow()
                )
            )
        );
        userRepository.save(accountUser);

        userProfileRepository.save(
            new UserProfile()
                .firstName("John")
                .middleName("Michael")
                .firstLastName("Doe")
                .secondLastName("Smith")
                .documentNumber(TEST_USER_LOGIN)
                .phoneNumber("3000000000")
                .user(accountUser)
                .documentType(seededDocumentType())
        );

        restAccountMockMvc
            .perform(get("/api/account").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.login").value(TEST_USER_LOGIN))
            .andExpect(jsonPath("$.email").value("john.doe@jhipster.com"))
            .andExpect(jsonPath("$.langKey").value("en"))
            .andExpect(jsonPath("$.authorities").value(containsInAnyOrder(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN)));

        userService.deleteUser(TEST_USER_LOGIN);
    }

    @Test
    @WithMockUser(TEST_USER_LOGIN)
    void testGetCurrentUserProfile() throws Exception {
        // The user's login is fixed (not derived) so the @WithMockUser principal matches.
        User accountUser = new User();
        accountUser.setLogin(TEST_USER_LOGIN);
        accountUser.setPassword(passwordEncoder.encode(VALID_PASSWORD));
        accountUser.setEmail("john.doe@jhipster.com");
        accountUser.setLangKey("en");
        accountUser.setActivated(true);
        userRepository.save(accountUser);

        userProfileRepository.save(
            new UserProfile()
                .firstName("John")
                .middleName("Michael")
                .firstLastName("Doe")
                .secondLastName("Smith")
                .documentNumber(TEST_USER_LOGIN)
                .phoneNumber("3000000000")
                .user(accountUser)
                .documentType(seededDocumentType())
        );

        restAccountMockMvc
            .perform(get("/api/account/profile").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.firstLastName").value("Doe"))
            .andExpect(jsonPath("$.documentNumber").value(TEST_USER_LOGIN))
            .andExpect(jsonPath("$.phoneNumber").value("3000000000"))
            .andExpect(jsonPath("$.documentType.id").exists())
            .andExpect(jsonPath("$.user.login").value(TEST_USER_LOGIN))
            .andExpect(jsonPath("$.user.email").value("john.doe@jhipster.com"));
    }

    @Test
    @WithUnauthenticatedMockUser
    void testGetCurrentUserProfileUnauthenticated() throws Exception {
        restAccountMockMvc.perform(get("/api/account/profile").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
    }

    @Test
    void testGetUnknownAccount() throws Exception {
        restAccountMockMvc.perform(get("/api/account").accept(MediaType.APPLICATION_PROBLEM_JSON)).andExpect(status().isUnauthorized());
    }

    @Test
    void testRegisterValid() throws Exception {
        String documentNumber = "1000000001";
        String email = "test-register-valid@example.com";
        ManagedUserVM validUser = validRegisterVM(documentNumber, email);
        assertThat(userRepository.findOneByLogin(expectedLogin(documentNumber))).isEmpty();

        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(validUser)))
            .andExpect(status().isCreated());

        Optional<User> createdUser = userRepository.findOneByLogin(expectedLogin(documentNumber));
        assertThat(createdUser).isPresent();
        assertThat(createdUser.get().getPassword()).isNotEqualTo(VALID_PASSWORD);
        assertThat(passwordEncoder.matches(VALID_PASSWORD, createdUser.get().getPassword())).isTrue();
        assertThat(createdUser.get().isActivated()).isTrue();
        assertThat(createdUser.get().isMustChangePassword()).isFalse();
        assertThat(createdUser.get().getAuthorities().stream().map(Authority::getName)).containsExactlyInAnyOrder(
            AuthoritiesConstants.USER,
            AuthoritiesConstants.APPRENTICE
        );
        assertThat(userProfileRepository.findByDocumentNumber(documentNumber)).isPresent();
    }

    @Test
    void testRegisterDocumentTypeNotFound() throws Exception {
        ManagedUserVM invalidUser = validRegisterVM("1000000002", "register-doc-type@example.com");
        invalidUser.setDocumentTypeId("000000000000000000000000");

        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(invalidUser)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterInactiveDocumentType() throws Exception {
        String documentNumber = "1000000021";
        DocumentType inactiveType = new DocumentType();
        inactiveType.setName("Inactive Document Type");
        inactiveType.setInitials("IX");
        inactiveType.setIsActive(false);
        inactiveType = documentTypeRepository.save(inactiveType);

        try {
            ManagedUserVM invalidUser = validRegisterVM(documentNumber, "register-inactive-doc-type@example.com");
            invalidUser.setDocumentTypeId(inactiveType.getId());

            restAccountMockMvc
                .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error.documentTypeInactive"));

            assertThat(userRepository.findOneByLogin(loginFor(inactiveType.getId(), documentNumber))).isEmpty();
        } finally {
            documentTypeRepository.delete(inactiveType);
        }
    }

    @Test
    void testRegisterInvalidPassword() throws Exception {
        ManagedUserVM invalidUser = validRegisterVM("1000000003", "register-invalid-password@example.com");
        invalidUser.setPassword("password"); // no uppercase / digit / symbol -> fails PASSWORD_PATTERN

        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(invalidUser)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.invalidpassword"));

        assertThat(userRepository.findOneByLogin(expectedLogin("1000000003"))).isEmpty();
    }

    @Test
    void testRegisterDuplicateLogin() throws Exception {
        String documentNumber = "1000000004";
        // First registration (unique document number, unique email)
        ManagedUserVM firstUser = validRegisterVM(documentNumber, "duplicate-login@example.com");

        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(firstUser)))
            .andExpect(status().isCreated());

        assertThat(userRepository.findOneByLogin(expectedLogin(documentNumber))).isPresent();

        // Duplicate login (same document number -> same derived login), different email
        ManagedUserVM secondUser = validRegisterVM(documentNumber, "duplicate-login-2@example.com");

        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(secondUser)))
            .andExpect(status().is4xxClientError());

        Optional<User> testUser = userRepository.findOneByLogin(expectedLogin(documentNumber));
        assertThat(testUser).isPresent();
        assertThat(testUser.get().getEmail()).isEqualTo("duplicate-login@example.com");
    }

    @Test
    void testRegisterDuplicateEmail() throws Exception {
        String email = "duplicate-email@example.com";
        ManagedUserVM firstUser = validRegisterVM("1000000005", email);

        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(firstUser)))
            .andExpect(status().isCreated());

        assertThat(userRepository.findOneByLogin(expectedLogin("1000000005"))).isPresent();

        // Duplicate email, different login (different document number)
        ManagedUserVM secondUser = validRegisterVM("1000000006", email);

        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(secondUser)))
            .andExpect(status().is4xxClientError());

        assertThat(userRepository.findOneByLogin(expectedLogin("1000000006"))).isEmpty();
    }

    @Test
    void testRegisterDuplicateDocumentNumber() throws Exception {
        String documentNumber = "1000000007";
        ManagedUserVM firstUser = validRegisterVM(documentNumber, "duplicate-document@example.com");

        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(firstUser)))
            .andExpect(status().isCreated());

        // Same document number -> derived login already exists & activated -> 400
        ManagedUserVM secondUser = validRegisterVM(documentNumber, "duplicate-document-2@example.com");

        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(secondUser)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.documentnumberexists"));

        Optional<User> testUser = userRepository.findOneByLogin(expectedLogin(documentNumber));
        assertThat(testUser).isPresent();
        assertThat(testUser.get().getEmail()).isEqualTo("duplicate-document@example.com");
    }

    @Test
    void testRegisterSameDocumentDeactivatedAccountIsNotDeleted() throws Exception {
        String documentNumber = "1000000017";
        String login = expectedLogin(documentNumber);
        String deactivatedEmail = "deactivated-1000000017@example.com";

        // A deactivated account for the same document (type + number) already exists.
        User deactivatedUser = new User();
        deactivatedUser.setLogin(login);
        deactivatedUser.setPassword(passwordEncoder.encode(VALID_PASSWORD));
        deactivatedUser.setEmail(deactivatedEmail);
        deactivatedUser.setActivated(false);
        deactivatedUser.setAuthorities(
            new HashSet<>(
                Set.of(
                    authorityRepository.findById(AuthoritiesConstants.USER).orElseThrow(),
                    authorityRepository.findById(AuthoritiesConstants.APPRENTICE).orElseThrow()
                )
            )
        );
        userRepository.save(deactivatedUser);

        ManagedUserVM secondUser = validRegisterVM(documentNumber, "re-register-deactivated@example.com");

        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(secondUser)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.documentnumberinactive"));

        Optional<User> stillThere = userRepository.findOneByLogin(login);
        assertThat(stillThere).isPresent();
        assertThat(stillThere.get().isActivated()).isFalse();
        assertThat(stillThere.get().getEmail()).isEqualTo(deactivatedEmail);
    }

    @Test
    void testRegisterDuplicateEmailDeactivatedAccountIsNotDeleted() throws Exception {
        String deactivatedDocumentNumber = "1000000018";
        String deactivatedLogin = expectedLogin(deactivatedDocumentNumber);
        String sharedEmail = "deactivated-shared-email@example.com";

        User deactivatedUser = new User();
        deactivatedUser.setLogin(deactivatedLogin);
        deactivatedUser.setPassword(passwordEncoder.encode(VALID_PASSWORD));
        deactivatedUser.setEmail(sharedEmail);
        deactivatedUser.setActivated(false);
        deactivatedUser.setAuthorities(
            new HashSet<>(
                Set.of(
                    authorityRepository.findById(AuthoritiesConstants.USER).orElseThrow(),
                    authorityRepository.findById(AuthoritiesConstants.APPRENTICE).orElseThrow()
                )
            )
        );
        userRepository.save(deactivatedUser);

        // Different document (1000000019) but the email already belongs to the deactivated account.
        ManagedUserVM secondUser = validRegisterVM("1000000019", sharedEmail);

        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(secondUser)))
            .andExpect(status().isBadRequest());

        Optional<User> stillThere = userRepository.findOneByLogin(deactivatedLogin);
        assertThat(stillThere).isPresent();
        assertThat(stillThere.get().getEmail()).isEqualTo(sharedEmail);
        assertThat(userRepository.findOneByLogin(expectedLogin("1000000019"))).isEmpty();
    }

    @Test
    void testRegisterSameDocumentNumberDifferentDocumentType() throws Exception {
        // The same document number is allowed as long as the document type differs.
        String documentNumber = "1000000010";

        ManagedUserVM first = validRegisterVM(documentNumber, "same-doc-type-1@example.com");
        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(first)))
            .andExpect(status().isCreated());

        ManagedUserVM second = validRegisterVM(documentNumber, "same-doc-type-2@example.com");
        second.setDocumentTypeId(secondDocumentTypeId());
        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(second)))
            .andExpect(status().isCreated());

        // Both profiles exist under the same document number, but with distinct derived logins.
        assertThat(userProfileRepository.findByDocumentTypeAndDocumentNumber(validDocumentTypeId(), documentNumber)).isPresent();
        assertThat(userProfileRepository.findByDocumentTypeAndDocumentNumber(secondDocumentTypeId(), documentNumber)).isPresent();
        assertThat(userRepository.findOneByLogin(loginFor(validDocumentTypeId(), documentNumber))).isPresent();
        assertThat(userRepository.findOneByLogin(loginFor(secondDocumentTypeId(), documentNumber))).isPresent();
    }

    @Test
    void testRegisterDuplicateDocumentNumberFromProfileLeavesNoPartialUser() throws Exception {
        String conflictingDocumentNumber = "1000000020";
        String otherDocumentNumber = "1000000099";

        // Simulate inconsistent legacy data: a UserProfile already owns (valid type, conflictingDocumentNumber)
        // but it points to a DIFFERENT user whose login derives from another document number.
        User otherUser = new User();
        otherUser.setLogin(expectedLogin(otherDocumentNumber));
        otherUser.setPassword(passwordEncoder.encode(VALID_PASSWORD));
        otherUser.setEmail("other-partial-user@example.com");
        otherUser.setActivated(true);
        otherUser.setAuthorities(
            new HashSet<>(
                Set.of(
                    authorityRepository.findById(AuthoritiesConstants.USER).orElseThrow(),
                    authorityRepository.findById(AuthoritiesConstants.APPRENTICE).orElseThrow()
                )
            )
        );
        userRepository.save(otherUser);

        UserProfile existingProfile = userProfileRepository.save(
            new UserProfile()
                .firstName("Other")
                .firstLastName("User")
                .documentNumber(conflictingDocumentNumber)
                .phoneNumber("3001234567")
                .user(otherUser)
                .documentType(seededDocumentType())
        );

        ManagedUserVM conflictingUser = validRegisterVM(conflictingDocumentNumber, "no-partial-user@example.com");

        // The profile duplicate check must fire BEFORE any write, so registration fails with 400...
        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(conflictingUser)))
            .andExpect(status().isBadRequest());

        // ...and no orphan User is left behind for the requested document.
        assertThat(userRepository.findOneByLogin(expectedLogin(conflictingDocumentNumber))).isEmpty();

        // The pre-existing profile is untouched and still points to the other user.
        UserProfile unchangedProfile = userProfileRepository
            .findByDocumentTypeAndDocumentNumber(validDocumentTypeId(), conflictingDocumentNumber)
            .orElseThrow();
        assertThat(unchangedProfile.getId()).isEqualTo(existingProfile.getId());
        assertThat(unchangedProfile.getUser().getId()).isEqualTo(otherUser.getId());
    }

    @Test
    void testRegisterMissingRequiredFields() throws Exception {
        ManagedUserVM invalidUser = validRegisterVM("1000000008", "register-missing-fields@example.com");
        invalidUser.setFirstName(null); // @NotNull required field missing

        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(invalidUser)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterAdminIsIgnored() throws Exception {
        ManagedUserVM validUser = validRegisterVM("1000000009", "admin-ignored@example.com");
        validUser.setAuthorities(Set.of(AuthoritiesConstants.ADMIN));

        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(validUser)))
            .andExpect(status().isCreated());

        Optional<User> userDup = userRepository.findOneByLogin(expectedLogin("1000000009"));
        assertThat(userDup).isPresent();
        assertThat(userDup.orElseThrow().getAuthorities().stream().map(Authority::getName)).containsExactlyInAnyOrder(
            AuthoritiesConstants.USER,
            AuthoritiesConstants.APPRENTICE
        );
    }

    @Test
    void testRegisterDocumentNumberNotNumeric() throws Exception {
        ManagedUserVM invalidUser = validRegisterVM("10000000A1", "register-doc-nonnumeric@example.com");

        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(invalidUser)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterPhoneNumberInvalid() throws Exception {
        ManagedUserVM invalidUser = validRegisterVM("1000000011", "register-phone-invalid@example.com");
        invalidUser.setPhoneNumber("300123456"); // 9 digits -> fails @Pattern("\\d{10}")

        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(invalidUser)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterEmailMissing() throws Exception {
        ManagedUserVM invalidUser = validRegisterVM("1000000012", "register-email-missing@example.com");
        invalidUser.setEmail(null);

        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(invalidUser)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterLangKeyHonored() throws Exception {
        String documentNumber = "1000000013";
        ManagedUserVM validUser = validRegisterVM(documentNumber, "register-lang-key-honored@example.com");
        validUser.setLangKey("en");

        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(validUser)))
            .andExpect(status().isCreated());

        Optional<User> createdUser = userRepository.findOneByLogin(expectedLogin(documentNumber));
        assertThat(createdUser).isPresent();
        assertThat(createdUser.orElseThrow().getLangKey()).isEqualTo("en");
    }

    @Test
    void testRegisterLangKeyDefaultsWhenNull() throws Exception {
        String documentNumber = "1000000014";
        ManagedUserVM validUser = validRegisterVM(documentNumber, "register-lang-key-default@example.com");
        validUser.setLangKey(null);

        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(validUser)))
            .andExpect(status().isCreated());

        Optional<User> createdUser = userRepository.findOneByLogin(expectedLogin(documentNumber));
        assertThat(createdUser).isPresent();
        assertThat(createdUser.orElseThrow().getLangKey()).isEqualTo(Constants.DEFAULT_LANGUAGE);
    }

    @Test
    void testRegisterTrimsPersistedNames() throws Exception {
        String documentNumber = "1000000015";
        ManagedUserVM validUser = validRegisterVM(documentNumber, "register-trimmed-names@example.com");
        validUser.setFirstName("  Juan  ");
        validUser.setMiddleName("  Carlos ");
        validUser.setFirstLastName(" Perez  ");
        validUser.setSecondLastName("  Gomez  ");

        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(validUser)))
            .andExpect(status().isCreated());

        UserProfile createdProfile = userProfileRepository.findByDocumentNumber(documentNumber).orElseThrow();
        assertThat(createdProfile.getFirstName()).isEqualTo("Juan");
        assertThat(createdProfile.getMiddleName()).isEqualTo("Carlos");
        assertThat(createdProfile.getFirstLastName()).isEqualTo("Perez");
        assertThat(createdProfile.getSecondLastName()).isEqualTo("Gomez");
    }

    @Test
    void testRegisterFailsWhenApprenticeAuthorityMissing() throws Exception {
        String documentNumber = "1000000016";
        String email = "register-missing-apprentice@example.com";

        // The APPRENTICE authority is mandatory for a self-registration. Remove it to prove
        // registerUser fails fast instead of silently persisting a ROLE_USER-only user.
        Optional<Authority> apprenticeAuthority = authorityRepository.findById(AuthoritiesConstants.APPRENTICE);
        assertThat(apprenticeAuthority).isPresent();
        authorityRepository.deleteById(AuthoritiesConstants.APPRENTICE);

        try {
            ManagedUserVM validUser = validRegisterVM(documentNumber, email);

            restAccountMockMvc
                .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(validUser)))
                .andExpect(status().isBadRequest());

            assertThat(userRepository.findOneByLogin(expectedLogin(documentNumber))).isEmpty();
        } finally {
            // restore the authority: the @AfterEach of this class only deletes profiles and users
            authorityRepository.save(apprenticeAuthority.orElseThrow());
        }
    }

    private static ManagedUserVM createInvalidUser(String login, String password, String email, boolean activated) {
        ManagedUserVM invalidUser = new ManagedUserVM();
        invalidUser.setLogin(login);
        invalidUser.setPassword(password);
        invalidUser.setEmail(email);
        invalidUser.setActivated(activated);
        invalidUser.setImageUrl("http://placehold.it/50x50");
        invalidUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        invalidUser.setAuthorities(Set.of(AuthoritiesConstants.USER));
        return invalidUser;
    }

    private ManagedUserVM validRegisterVM(String documentNumber, String email) {
        ManagedUserVM user = new ManagedUserVM();
        user.setLogin(documentNumber);
        user.setPassword(VALID_PASSWORD);
        user.setEmail(email);
        user.setImageUrl("http://placehold.it/50x50");
        user.setLangKey(Constants.DEFAULT_LANGUAGE);
        user.setAuthorities(Set.of(AuthoritiesConstants.USER));
        user.setFirstName("Juan");
        user.setMiddleName("Carlos");
        user.setFirstLastName("Perez");
        user.setSecondLastName("Gomez");
        user.setDocumentNumber(documentNumber);
        user.setPhoneNumber("3001234567");
        user.setDocumentTypeId(validDocumentTypeId());
        return user;
    }

    private String validDocumentTypeId() {
        return documentTypeRepository.findAll().iterator().next().getId();
    }

    private DocumentType seededDocumentType() {
        return documentTypeRepository.findAll().iterator().next();
    }

    /**
     * Derives the login that {@link UserService#registerUser} / {@link UserService#createUser}
     * now produce from the seeded {@link DocumentType} initials: {@code <initials>_<documentNumber>}.
     */
    private String expectedLogin(String documentNumber) {
        return loginFor(validDocumentTypeId(), documentNumber);
    }

    /**
     * Derives the login for an arbitrary document type id, mirroring {@link UserService#buildLogin}.
     */
    private String loginFor(String documentTypeId, String documentNumber) {
        DocumentType dt = documentTypeRepository.findById(documentTypeId).orElseThrow();
        String typeCode = dt.getInitials() != null ? dt.getInitials() : "";
        return (typeCode + "_" + documentNumber).toLowerCase().trim();
    }

    private String secondDocumentTypeId() {
        List<DocumentType> types = documentTypeRepository.findAll();
        return types.size() > 1 ? types.get(1).getId() : types.get(0).getId();
    }

    private User persistedResetUser(String login) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(login + "@example.com");
        user.setPassword(passwordEncoder.encode(VALID_PASSWORD));
        user.setActivated(true);
        user.setLangKey("es");
        return userRepository.save(user);
    }

    private UserProfile persistedResetProfile(User user, DocumentType documentType, String documentNumber) {
        return new UserProfile()
            .firstName("Juan")
            .middleName("Carlos")
            .firstLastName("Perez")
            .secondLastName("Gomez")
            .documentNumber(documentNumber)
            .phoneNumber("3001234567")
            .user(user)
            .documentType(documentType);
    }

    private User persistedAccountUser(String login) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(login + "@example.com");
        user.setPassword(passwordEncoder.encode(VALID_PASSWORD));
        user.setActivated(true);
        user.setLangKey("es");
        return userRepository.save(user);
    }

    private UserProfile persistedAccountProfile(User user, String documentNumber) {
        return userProfileRepository.save(
            new UserProfile()
                .firstName("Juan")
                .middleName("Carlos")
                .firstLastName("Perez")
                .secondLastName("Gomez")
                .documentNumber(documentNumber)
                .phoneNumber("3001234567")
                .user(user)
                .documentType(seededDocumentType())
        );
    }

    @Test
    void testActivateAccount() throws Exception {
        final String activationKey = "some activation key";
        User user = new User();
        user.setLogin("activate-account");
        user.setEmail("activate-account@example.com");
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(false);
        user.setActivationKey(activationKey);

        userRepository.save(user);

        restAccountMockMvc.perform(get("/api/activate?key={activationKey}", activationKey)).andExpect(status().isOk());

        user = userRepository.findOneByLogin(user.getLogin()).orElse(null);
        assertThat(user.isActivated()).isTrue();

        userService.deleteUser("activate-account");
    }

    @Test
    void testActivateAccountWithWrongKey() throws Exception {
        restAccountMockMvc.perform(get("/api/activate?key=wrongActivationKey")).andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser("save-account-user")
    void testSaveAccount() throws Exception {
        User user = persistedAccountUser("save-account-user");
        persistedAccountProfile(user, "SAVEDAC1");

        AccountUpdateVM updateVM = new AccountUpdateVM();
        updateVM.setEmail("NEW-SAVE-ACCOUNT@example.com");
        updateVM.setFirstName("UpdatedFirst");
        updateVM.setPhoneNumber("3105551234");
        updateVM.setLangKey("en");

        restAccountMockMvc
            .perform(patch("/api/account").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(updateVM)))
            .andExpect(status().isOk());

        User updatedUser = userRepository.findOneByLogin("save-account-user").orElseThrow();
        // email is trimmed + lowercased by the service, so an uppercase input becomes lowercase
        assertThat(updatedUser.getEmail()).isEqualTo("new-save-account@example.com");
        assertThat(updatedUser.getLangKey()).isEqualTo("en");
        // no newPassword sent -> password must be unchanged
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());

        UserProfile updatedProfile = userProfileRepository.findOneByUserId(user.getId()).orElseThrow();
        assertThat(updatedProfile.getFirstName()).isEqualTo("UpdatedFirst");
        assertThat(updatedProfile.getPhoneNumber()).isEqualTo("3105551234");

        userService.deleteUser("save-account-user");
    }

    @Test
    @WithMockUser("save-account-image-url-ignored")
    void testSaveAccountIgnoresImageUrl() throws Exception {
        User user = persistedAccountUser("save-account-image-url-ignored");
        String originalImageUrl = "http://original.example.com/avatar.png";
        user.setImageUrl(originalImageUrl);
        userRepository.save(user);
        persistedAccountProfile(user, "SAVEIMG1");

        // imageUrl is no longer part of the self-service contract: it must be ignored by
        // deserialization and never reach the persisted User.imageUrl.
        restAccountMockMvc
            .perform(
                patch("/api/account")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"imageUrl\":\"http://attacker.example.com/avatar.png\"}")
            )
            .andExpect(status().isOk());

        User unchangedUser = userRepository.findOneByLogin("save-account-image-url-ignored").orElseThrow();
        assertThat(unchangedUser.getImageUrl()).isEqualTo(originalImageUrl);

        userService.deleteUser("save-account-image-url-ignored");
    }

    @Test
    @WithMockUser("save-account-document-number")
    void testSaveAccountDocumentNumberImmutable() throws Exception {
        User user = persistedAccountUser("save-account-document-number");
        UserProfile profile = persistedAccountProfile(user, "SAVEDOCN1");

        AccountUpdateVM updateVM = new AccountUpdateVM();
        updateVM.setFirstName("Changed");
        updateVM.setDocumentNumber("9999999999");

        // Manipulating the request with a document number must be rejected (E3).
        restAccountMockMvc
            .perform(patch("/api/account").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(updateVM)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.documentimmutable"));

        // The rejection happens before any write: document and other fields stay untouched.
        UserProfile unchangedProfile = userProfileRepository.findOneByUserId(user.getId()).orElseThrow();
        assertThat(unchangedProfile.getId()).isEqualTo(profile.getId());
        assertThat(unchangedProfile.getDocumentNumber()).isEqualTo("SAVEDOCN1");
        assertThat(unchangedProfile.getFirstName()).isEqualTo("Juan");

        userService.deleteUser("save-account-document-number");
    }

    @Test
    @WithMockUser("save-account-document-type")
    void testSaveAccountDocumentTypeImmutable() throws Exception {
        User user = persistedAccountUser("save-account-document-type");
        UserProfile profile = persistedAccountProfile(user, "SAVEDOCT1");
        String originalDocumentTypeId = profile.getDocumentType().getId();

        AccountUpdateVM updateVM = new AccountUpdateVM();
        updateVM.setFirstName("Changed");
        updateVM.setDocumentTypeId(secondDocumentTypeId());

        // Manipulating the request with a document type must be rejected (E3).
        restAccountMockMvc
            .perform(patch("/api/account").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(updateVM)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.documentimmutable"));

        // The rejection happens before any write: document and other fields stay untouched.
        UserProfile unchangedProfile = userProfileRepository.findOneByUserId(user.getId()).orElseThrow();
        assertThat(unchangedProfile.getId()).isEqualTo(profile.getId());
        assertThat(unchangedProfile.getDocumentType().getId()).isEqualTo(originalDocumentTypeId);
        assertThat(unchangedProfile.getFirstName()).isEqualTo("Juan");

        userService.deleteUser("save-account-document-type");
    }

    @Test
    @WithMockUser("save-account-valid-phone")
    void testSaveAccountValidPhone() throws Exception {
        User user = persistedAccountUser("save-account-valid-phone");
        persistedAccountProfile(user, "SAVEPH1");

        AccountUpdateVM updateVM = new AccountUpdateVM();
        updateVM.setPhoneNumber("3105551234"); // exactly 10 digits -> valid

        restAccountMockMvc
            .perform(patch("/api/account").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(updateVM)))
            .andExpect(status().isOk());

        UserProfile updatedProfile = userProfileRepository.findOneByUserId(user.getId()).orElseThrow();
        assertThat(updatedProfile.getPhoneNumber()).isEqualTo("3105551234");

        userService.deleteUser("save-account-valid-phone");
    }

    @Test
    @WithMockUser("save-account-short-phone")
    void testSaveAccountPhoneTooShort() throws Exception {
        User user = persistedAccountUser("save-account-short-phone");
        persistedAccountProfile(user, "SAVEPH2");

        AccountUpdateVM updateVM = new AccountUpdateVM();
        updateVM.setPhoneNumber("310555123"); // 9 digits -> fails @Pattern("\\d{10}")

        restAccountMockMvc
            .perform(patch("/api/account").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(updateVM)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.validation"))
            .andExpect(jsonPath("$.fieldErrors[0].field").value("phoneNumber"));

        // rejected before the service runs -> persisted value is untouched
        UserProfile unchangedProfile = userProfileRepository.findOneByUserId(user.getId()).orElseThrow();
        assertThat(unchangedProfile.getPhoneNumber()).isEqualTo("3001234567");

        userService.deleteUser("save-account-short-phone");
    }

    @Test
    @WithMockUser("save-account-nonnumeric-phone")
    void testSaveAccountPhoneNonNumeric() throws Exception {
        User user = persistedAccountUser("save-account-nonnumeric-phone");
        persistedAccountProfile(user, "SAVEPH3");

        AccountUpdateVM updateVM = new AccountUpdateVM();
        updateVM.setPhoneNumber("31055A1234"); // non-numeric -> fails @Pattern("\\d{10}")

        restAccountMockMvc
            .perform(patch("/api/account").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(updateVM)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.validation"))
            .andExpect(jsonPath("$.fieldErrors[0].field").value("phoneNumber"));

        UserProfile unchangedProfile = userProfileRepository.findOneByUserId(user.getId()).orElseThrow();
        assertThat(unchangedProfile.getPhoneNumber()).isEqualTo("3001234567");

        userService.deleteUser("save-account-nonnumeric-phone");
    }

    @Test
    @WithMockUser("save-account-clear-optional-names")
    void testSaveAccountClearOptionalNames() throws Exception {
        User user = persistedAccountUser("save-account-clear-optional-names");
        UserProfile profile = persistedAccountProfile(user, "SAVENA1");
        assertThat(profile.getMiddleName()).isEqualTo("Carlos");
        assertThat(profile.getSecondLastName()).isEqualTo("Gomez");

        AccountUpdateVM updateVM = new AccountUpdateVM();
        updateVM.setMiddleName("");
        updateVM.setSecondLastName("");

        restAccountMockMvc
            .perform(patch("/api/account").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(updateVM)))
            .andExpect(status().isOk());

        UserProfile updatedProfile = userProfileRepository.findOneByUserId(user.getId()).orElseThrow();
        assertThat(updatedProfile.getMiddleName()).isNull();
        assertThat(updatedProfile.getSecondLastName()).isNull();

        userService.deleteUser("save-account-clear-optional-names");
    }

    @Test
    @WithMockUser("save-invalid-email-user")
    void testSaveInvalidEmail() throws Exception {
        User user = persistedAccountUser("save-invalid-email-user");
        persistedAccountProfile(user, "SAVEDIC1");

        AccountUpdateVM updateVM = new AccountUpdateVM();
        updateVM.setEmail("invalid email");

        // @Email / @Pattern on AccountUpdateVM.email reject it before updateOwnAccount runs
        restAccountMockMvc
            .perform(patch("/api/account").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(updateVM)))
            .andExpect(status().isBadRequest());

        assertThat(userRepository.findOneByEmailIgnoreCase("invalid email")).isNotPresent();

        userService.deleteUser("save-invalid-email-user");
    }

    @Test
    @WithMockUser("save-existing-email-a")
    void testSaveExistingEmail() throws Exception {
        User userA = persistedAccountUser("save-existing-email-a");
        persistedAccountProfile(userA, "SAVEDEA1");
        User userB = persistedAccountUser("save-existing-email-b");
        persistedAccountProfile(userB, "SAVEDEB1");

        AccountUpdateVM updateVM = new AccountUpdateVM();
        updateVM.setEmail("save-existing-email-b@example.com");

        // the email belongs to userB (different login) -> EmailAlreadyUsedException (400)
        restAccountMockMvc
            .perform(patch("/api/account").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(updateVM)))
            .andExpect(status().isBadRequest());

        User updatedUser = userRepository.findOneByLogin("save-existing-email-a").orElseThrow();
        assertThat(updatedUser.getEmail()).isEqualTo("save-existing-email-a@example.com");

        userService.deleteUser("save-existing-email-a");
        userService.deleteUser("save-existing-email-b");
    }

    @Test
    @WithMockUser("save-existing-email-and-login")
    void testSaveExistingEmailAndLogin() throws Exception {
        User user = persistedAccountUser("save-existing-email-and-login");
        persistedAccountProfile(user, "SAVEDEL1");

        AccountUpdateVM updateVM = new AccountUpdateVM();
        updateVM.setEmail("save-existing-email-and-login@example.com");

        // re-setting the same email is allowed (the code only rejects emails of OTHER users)
        restAccountMockMvc
            .perform(patch("/api/account").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(updateVM)))
            .andExpect(status().isOk());

        User updatedUser = userRepository.findOneByLogin("save-existing-email-and-login").orElseThrow();
        assertThat(updatedUser.getEmail()).isEqualTo("save-existing-email-and-login@example.com");

        userService.deleteUser("save-existing-email-and-login");
    }

    @Test
    @WithMockUser("save-account-change-password")
    void testSaveAccountChangePassword() throws Exception {
        User user = persistedAccountUser("save-account-change-password");
        user.setMustChangePassword(true);
        userRepository.save(user);
        persistedAccountProfile(user, "SAVEPW1");

        AccountUpdateVM updateVM = new AccountUpdateVM();
        updateVM.setCurrentPassword(VALID_PASSWORD);
        updateVM.setNewPassword("NewPassw0rd!");

        restAccountMockMvc
            .perform(patch("/api/account").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(updateVM)))
            .andExpect(status().isOk());

        User updatedUser = userRepository.findOneByLogin("save-account-change-password").orElseThrow();
        assertThat(passwordEncoder.matches("NewPassw0rd!", updatedUser.getPassword())).isTrue();
        assertThat(updatedUser.isMustChangePassword()).isFalse();

        userService.deleteUser("save-account-change-password");
    }

    @Test
    @WithMockUser("save-account-change-password-wrong")
    void testSaveAccountChangePasswordWrongCurrent() throws Exception {
        User user = persistedAccountUser("save-account-change-password-wrong");
        persistedAccountProfile(user, "SAVEPW2");

        AccountUpdateVM updateVM = new AccountUpdateVM();
        updateVM.setCurrentPassword("WrongPassw0rd!");
        updateVM.setNewPassword("NewPassw0rd!");

        // wrong currentPassword -> BadRequestAlertException (400, error.currentpasswordinvalid), password must not change
        restAccountMockMvc
            .perform(patch("/api/account").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(updateVM)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.currentpasswordinvalid"));

        User updatedUser = userRepository.findOneByLogin("save-account-change-password-wrong").orElseThrow();
        assertThat(passwordEncoder.matches("NewPassw0rd!", updatedUser.getPassword())).isFalse();
        assertThat(passwordEncoder.matches(VALID_PASSWORD, updatedUser.getPassword())).isTrue();

        userService.deleteUser("save-account-change-password-wrong");
    }

    @Test
    @WithMockUser("save-account-change-password-same")
    void testSaveAccountChangePasswordSameAsCurrent() throws Exception {
        User user = persistedAccountUser("save-account-change-password-same");
        persistedAccountProfile(user, "SAVEPW3");

        AccountUpdateVM updateVM = new AccountUpdateVM();
        updateVM.setCurrentPassword(VALID_PASSWORD);
        updateVM.setNewPassword(VALID_PASSWORD);

        // new password equals the current one -> BadRequestAlertException (400, error.samepassword)
        restAccountMockMvc
            .perform(patch("/api/account").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(updateVM)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.samepassword"));

        User updatedUser = userRepository.findOneByLogin("save-account-change-password-same").orElseThrow();
        assertThat(passwordEncoder.matches(VALID_PASSWORD, updatedUser.getPassword())).isTrue();

        userService.deleteUser("save-account-change-password-same");
    }

    @Test
    @WithMockUser("save-account-policy-password")
    void testSaveAccountChangePasswordPolicyViolation() throws Exception {
        User user = persistedAccountUser("save-account-policy-password");
        persistedAccountProfile(user, "SAVEPW4");

        AccountUpdateVM updateVM = new AccountUpdateVM();
        updateVM.setCurrentPassword(VALID_PASSWORD);
        updateVM.setNewPassword("12345678"); // 8 chars (length-valid) but missing classes -> E5

        // the policy failure carries the dedicated business key instead of a generic error
        restAccountMockMvc
            .perform(patch("/api/account").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(updateVM)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.invalidpassword"));

        User updatedUser = userRepository.findOneByLogin("save-account-policy-password").orElseThrow();
        assertThat(passwordEncoder.matches("12345678", updatedUser.getPassword())).isFalse();
        assertThat(passwordEncoder.matches(VALID_PASSWORD, updatedUser.getPassword())).isTrue();

        userService.deleteUser("save-account-policy-password");
    }

    @Test
    @WithMockUser("save-account-short-current-password")
    void testSaveAccountChangePasswordShortCurrentReachesService() throws Exception {
        User user = persistedAccountUser("save-account-short-current-password");
        persistedAccountProfile(user, "SAVEPW5");

        AccountUpdateVM updateVM = new AccountUpdateVM();
        updateVM.setCurrentPassword("abc"); // below the old @Size(8) floor -> must reach the service
        updateVM.setNewPassword("NewPassw0rd!");

        // removing the VM-level @Size turns this into E4 (error.currentpasswordinvalid), not error.validation
        restAccountMockMvc
            .perform(patch("/api/account").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(updateVM)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.currentpasswordinvalid"));

        User updatedUser = userRepository.findOneByLogin("save-account-short-current-password").orElseThrow();
        assertThat(passwordEncoder.matches("NewPassw0rd!", updatedUser.getPassword())).isFalse();
        assertThat(passwordEncoder.matches(VALID_PASSWORD, updatedUser.getPassword())).isTrue();

        userService.deleteUser("save-account-short-current-password");
    }

    @Test
    @WithMockUser("change-password-wrong-existing-password")
    void testChangePasswordWrongExistingPassword() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.insecure().nextAlphanumeric(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password-wrong-existing-password");
        user.setEmail("change-password-wrong-existing-password@example.com");
        userRepository.save(user);

        restAccountMockMvc
            .perform(
                post("/api/account/change-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(new PasswordChangeDTO("1" + currentPassword, "new password")))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.currentpasswordinvalid"));

        User updatedUser = userRepository.findOneByLogin("change-password-wrong-existing-password").orElse(null);
        assertThat(passwordEncoder.matches("new password", updatedUser.getPassword())).isFalse();
        assertThat(passwordEncoder.matches(currentPassword, updatedUser.getPassword())).isTrue();

        userService.deleteUser("change-password-wrong-existing-password");
    }

    @Test
    @WithMockUser("change-password")
    void testChangePassword() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.insecure().nextAlphanumeric(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password");
        user.setEmail("change-password@example.com");
        userRepository.save(user);

        restAccountMockMvc
            .perform(
                post("/api/account/change-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(new PasswordChangeDTO(currentPassword, "NewPassw0rd!")))
            )
            .andExpect(status().isOk());

        User updatedUser = userRepository.findOneByLogin("change-password").orElse(null);
        assertThat(passwordEncoder.matches("NewPassw0rd!", updatedUser.getPassword())).isTrue();

        userService.deleteUser("change-password");
    }

    @Test
    @WithMockUser("change-password-policy")
    void testChangePasswordPolicyViolationLengthValid() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.insecure().nextAlphanumeric(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password-policy");
        user.setEmail("change-password-policy@example.com");
        userRepository.save(user);

        // 8 chars (length-valid) but missing uppercase/special chars -> policy violation (E5)
        restAccountMockMvc
            .perform(
                post("/api/account/change-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(new PasswordChangeDTO(currentPassword, "12345678")))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.invalidpassword"));

        User updatedUser = userRepository.findOneByLogin("change-password-policy").orElse(null);
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());

        userService.deleteUser("change-password-policy");
    }

    @Test
    @WithMockUser("change-password-same")
    void testChangePasswordSameAsCurrent() throws Exception {
        User user = new User();
        user.setPassword(passwordEncoder.encode(VALID_PASSWORD));
        user.setLogin("change-password-same");
        user.setEmail("change-password-same@example.com");
        userRepository.save(user);

        // new password equals the current one -> BadRequestAlertException (E6, error.samepassword)
        restAccountMockMvc
            .perform(
                post("/api/account/change-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(new PasswordChangeDTO(VALID_PASSWORD, VALID_PASSWORD)))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.samepassword"));

        User updatedUser = userRepository.findOneByLogin("change-password-same").orElse(null);
        assertThat(passwordEncoder.matches(VALID_PASSWORD, updatedUser.getPassword())).isTrue();

        userService.deleteUser("change-password-same");
    }

    @Test
    @WithMockUser("change-password-must-change")
    void testChangePasswordClearsMustChangePassword() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.insecure().nextAlphanumeric(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password-must-change");
        user.setEmail("change-password-must-change@example.com");
        user.setMustChangePassword(true);
        userRepository.save(user);

        restAccountMockMvc
            .perform(
                post("/api/account/change-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(new PasswordChangeDTO(currentPassword, "NewPassw0rd!")))
            )
            .andExpect(status().isOk());

        User updatedUser = userRepository.findOneByLogin("change-password-must-change").orElse(null);
        assertThat(updatedUser.isMustChangePassword()).isFalse();
        assertThat(passwordEncoder.matches("NewPassw0rd!", updatedUser.getPassword())).isTrue();

        userService.deleteUser("change-password-must-change");
    }

    @Test
    @WithMockUser("change-password-too-small")
    void testChangePasswordTooSmall() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.insecure().nextAlphanumeric(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password-too-small");
        user.setEmail("change-password-too-small@example.com");
        userRepository.save(user);

        String newPassword = RandomStringUtils.insecure().next(ManagedUserVM.PASSWORD_MIN_LENGTH - 1);

        restAccountMockMvc
            .perform(
                post("/api/account/change-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(new PasswordChangeDTO(currentPassword, newPassword)))
            )
            .andExpect(status().isBadRequest());

        User updatedUser = userRepository.findOneByLogin("change-password-too-small").orElse(null);
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());

        userService.deleteUser("change-password-too-small");
    }

    @Test
    @WithMockUser("change-password-too-long")
    void testChangePasswordTooLong() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.insecure().nextAlphanumeric(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password-too-long");
        user.setEmail("change-password-too-long@example.com");
        userRepository.save(user);

        String newPassword = RandomStringUtils.insecure().next(ManagedUserVM.PASSWORD_MAX_LENGTH + 1);

        restAccountMockMvc
            .perform(
                post("/api/account/change-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(new PasswordChangeDTO(currentPassword, newPassword)))
            )
            .andExpect(status().isBadRequest());

        User updatedUser = userRepository.findOneByLogin("change-password-too-long").orElse(null);
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());

        userService.deleteUser("change-password-too-long");
    }

    @Test
    @WithMockUser("change-password-empty")
    void testChangePasswordEmpty() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.insecure().nextAlphanumeric(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password-empty");
        user.setEmail("change-password-empty@example.com");
        userRepository.save(user);

        restAccountMockMvc
            .perform(
                post("/api/account/change-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(new PasswordChangeDTO(currentPassword, "")))
            )
            .andExpect(status().isBadRequest());

        User updatedUser = userRepository.findOneByLogin("change-password-empty").orElse(null);
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());

        userService.deleteUser("change-password-empty");
    }

    @Test
    void testRequestPasswordReset() throws Exception {
        String login = "reset-user";
        String documentNumber = "1010101010";
        User user = persistedResetUser(login);
        DocumentType documentType = seededDocumentType();
        userProfileRepository.save(persistedResetProfile(user, documentType, documentNumber));

        PasswordResetRequestVM request = new PasswordResetRequestVM();
        request.setDocumentTypeId(documentType.getId());
        request.setDocumentNumber(documentNumber);

        restAccountMockMvc
            .perform(
                post("/api/account/reset-password/init").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(request))
            )
            .andExpect(status().isOk());

        User updatedUser = userRepository.findOneByLogin(login).orElseThrow();
        assertThat(updatedUser.getResetKey()).isNotBlank();
        assertThat(updatedUser.getResetDate()).isNotNull();

        userService.deleteUser(login);
    }

    @Test
    void testRequestPasswordResetUnknownDocument() throws Exception {
        String login = "reset-unknown";
        String existingDocumentNumber = "1022222222";
        // A real activated user + profile exists, but it does NOT match the requested document number.
        User user = persistedResetUser(login);
        DocumentType documentType = seededDocumentType();
        userProfileRepository.save(persistedResetProfile(user, documentType, existingDocumentNumber));

        PasswordResetRequestVM request = new PasswordResetRequestVM();
        request.setDocumentTypeId(documentType.getId());
        request.setDocumentNumber("999999999999");

        restAccountMockMvc
            .perform(
                post("/api/account/reset-password/init").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(request))
            )
            .andExpect(status().isOk());

        User unchangedUser = userRepository.findOneByLogin(login).orElseThrow();
        assertThat(unchangedUser.getResetKey()).isNull();
        assertThat(unchangedUser.getResetDate()).isNull();

        userService.deleteUser(login);
    }

    @Test
    void testRequestPasswordResetMissingRequiredFields() throws Exception {
        PasswordResetRequestVM request = new PasswordResetRequestVM();
        // documentTypeId left null on purpose -> @Valid must reject.
        request.setDocumentNumber("1000000000");

        restAccountMockMvc
            .perform(
                post("/api/account/reset-password/init").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(request))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void testFinishPasswordReset() throws Exception {
        User user = new User();
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setLogin("finish-password-reset");
        user.setEmail("finish-password-reset@example.com");
        user.setResetDate(Instant.now().plusSeconds(60));
        user.setResetKey("reset key");
        userRepository.save(user);

        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey(user.getResetKey());
        keyAndPassword.setNewPassword(VALID_PASSWORD);

        restAccountMockMvc
            .perform(
                post("/api/account/reset-password/finish")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(keyAndPassword))
            )
            .andExpect(status().isOk());

        User updatedUser = userRepository.findOneByLogin(user.getLogin()).orElse(null);
        assertThat(passwordEncoder.matches(keyAndPassword.getNewPassword(), updatedUser.getPassword())).isTrue();

        userService.deleteUser("finish-password-reset");
    }

    @Test
    void testFinishPasswordResetWithWeakPassword() throws Exception {
        User user = new User();
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setLogin("finish-password-reset-weak");
        user.setEmail("finish-password-reset-weak@example.com");
        user.setResetDate(Instant.now().plusSeconds(60));
        user.setResetKey("reset key weak");
        userRepository.save(user);

        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey(user.getResetKey());
        // Length-valid (8) but class-invalid: no uppercase, lowercase or special character.
        keyAndPassword.setNewPassword("12345678");

        restAccountMockMvc
            .perform(
                post("/api/account/reset-password/finish")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(keyAndPassword))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.invalidpassword"));

        User updatedUser = userRepository.findOneByLogin(user.getLogin()).orElse(null);
        assertThat(passwordEncoder.matches(keyAndPassword.getNewPassword(), updatedUser.getPassword())).isFalse();

        userService.deleteUser("finish-password-reset-weak");
    }

    @Test
    void testFinishPasswordResetTooSmall() throws Exception {
        User user = new User();
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setLogin("finish-password-reset-too-small");
        user.setEmail("finish-password-reset-too-small@example.com");
        user.setResetDate(Instant.now().plusSeconds(60));
        user.setResetKey("reset key too small");
        userRepository.save(user);

        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey(user.getResetKey());
        keyAndPassword.setNewPassword("foo");

        restAccountMockMvc
            .perform(
                post("/api/account/reset-password/finish")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(keyAndPassword))
            )
            .andExpect(status().isBadRequest());

        User updatedUser = userRepository.findOneByLogin(user.getLogin()).orElse(null);
        assertThat(passwordEncoder.matches(keyAndPassword.getNewPassword(), updatedUser.getPassword())).isFalse();

        userService.deleteUser("finish-password-reset-too-small");
    }

    @Test
    void testFinishPasswordResetWrongKey() throws Exception {
        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey("wrong reset key");
        keyAndPassword.setNewPassword("new password");

        restAccountMockMvc
            .perform(
                post("/api/account/reset-password/finish")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(keyAndPassword))
            )
            .andExpect(status().isInternalServerError());
    }
}
