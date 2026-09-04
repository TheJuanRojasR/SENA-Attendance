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
    void testRegisterInvalidPassword() throws Exception {
        ManagedUserVM invalidUser = validRegisterVM("1000000003", "register-invalid-password@example.com");
        invalidUser.setPassword("password"); // no uppercase / digit / symbol -> fails PASSWORD_PATTERN

        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(invalidUser)))
            .andExpect(status().isBadRequest());

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
            .andExpect(status().isBadRequest());

        Optional<User> testUser = userRepository.findOneByLogin(expectedLogin(documentNumber));
        assertThat(testUser).isPresent();
        assertThat(testUser.get().getEmail()).isEqualTo("duplicate-document@example.com");
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
        persistedAccountProfile(user, "SAVEPW1");

        AccountUpdateVM updateVM = new AccountUpdateVM();
        updateVM.setCurrentPassword(VALID_PASSWORD);
        updateVM.setNewPassword("NewPassw0rd!");

        restAccountMockMvc
            .perform(patch("/api/account").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(updateVM)))
            .andExpect(status().isOk());

        User updatedUser = userRepository.findOneByLogin("save-account-change-password").orElseThrow();
        assertThat(passwordEncoder.matches("NewPassw0rd!", updatedUser.getPassword())).isTrue();

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

        // wrong currentPassword -> InvalidPasswordException (400), password must not change
        restAccountMockMvc
            .perform(patch("/api/account").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(updateVM)))
            .andExpect(status().isBadRequest());

        User updatedUser = userRepository.findOneByLogin("save-account-change-password-wrong").orElseThrow();
        assertThat(passwordEncoder.matches("NewPassw0rd!", updatedUser.getPassword())).isFalse();
        assertThat(passwordEncoder.matches(VALID_PASSWORD, updatedUser.getPassword())).isTrue();

        userService.deleteUser("save-account-change-password-wrong");
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
            .andExpect(status().isBadRequest());

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
                    .content(om.writeValueAsBytes(new PasswordChangeDTO(currentPassword, "new password")))
            )
            .andExpect(status().isOk());

        User updatedUser = userRepository.findOneByLogin("change-password").orElse(null);
        assertThat(passwordEncoder.matches("new password", updatedUser.getPassword())).isTrue();

        userService.deleteUser("change-password");
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
        keyAndPassword.setNewPassword("new password");

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
