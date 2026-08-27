package com.mycompany.senaattendance.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.config.Constants;
import com.mycompany.senaattendance.domain.Authority;
import com.mycompany.senaattendance.domain.DocumentType;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.repository.DocumentTypeRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.UserService;
import com.mycompany.senaattendance.service.dto.AdminUserDTO;
import com.mycompany.senaattendance.service.dto.PasswordChangeDTO;
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
        AdminCreateUserVM user = new AdminCreateUserVM();
        user.setLogin(TEST_USER_LOGIN);
        user.setPassword("Passw0rd!");
        user.setEmail("john.doe@jhipster.com");
        user.setFirstName("John");
        user.setMiddleName("Michael");
        user.setFirstLastName("Doe");
        user.setSecondLastName("Smith");
        user.setDocumentNumber(TEST_USER_LOGIN);
        user.setPhoneNumber("3000000000");
        user.setDocumentTypeId(validDocumentTypeId());
        user.setRole(AuthoritiesConstants.ADMIN);
        user.setLangKey("en");
        userService.createUser(user);

        restAccountMockMvc
            .perform(get("/api/account").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.login").value(TEST_USER_LOGIN))
            .andExpect(jsonPath("$.email").value("john.doe@jhipster.com"))
            .andExpect(jsonPath("$.langKey").value("en"))
            .andExpect(jsonPath("$.authorities").value(AuthoritiesConstants.ADMIN));

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
        assertThat(userRepository.findOneByLogin(documentNumber)).isEmpty();

        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(validUser)))
            .andExpect(status().isCreated());

        Optional<User> createdUser = userRepository.findOneByLogin(documentNumber);
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

        assertThat(userRepository.findOneByLogin("1000000003")).isEmpty();
    }

    @Test
    void testRegisterDuplicateLogin() throws Exception {
        String documentNumber = "1000000004";
        // First registration (unique document number, unique email)
        ManagedUserVM firstUser = validRegisterVM(documentNumber, "duplicate-login@example.com");

        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(firstUser)))
            .andExpect(status().isCreated());

        assertThat(userRepository.findOneByLogin(documentNumber)).isPresent();

        // Duplicate login (same document number -> same derived login), different email
        ManagedUserVM secondUser = validRegisterVM(documentNumber, "duplicate-login-2@example.com");

        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(secondUser)))
            .andExpect(status().is4xxClientError());

        Optional<User> testUser = userRepository.findOneByLogin(documentNumber);
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

        assertThat(userRepository.findOneByLogin("1000000005")).isPresent();

        // Duplicate email, different login (different document number)
        ManagedUserVM secondUser = validRegisterVM("1000000006", email);

        restAccountMockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(secondUser)))
            .andExpect(status().is4xxClientError());

        assertThat(userRepository.findOneByLogin("1000000006")).isEmpty();
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

        Optional<User> testUser = userRepository.findOneByLogin(documentNumber);
        assertThat(testUser).isPresent();
        assertThat(testUser.get().getEmail()).isEqualTo("duplicate-document@example.com");
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

        Optional<User> userDup = userRepository.findOneByLogin("1000000009");
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
    @WithMockUser("save-account")
    void testSaveAccount() throws Exception {
        User user = new User();
        user.setLogin("save-account");
        user.setEmail("save-account@example.com");
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(true);
        userRepository.save(user);

        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setLogin("not-used");
        userDTO.setEmail("save-account@example.com");
        userDTO.setActivated(false);
        userDTO.setImageUrl("http://placehold.it/50x50");
        userDTO.setLangKey(Constants.DEFAULT_LANGUAGE);
        userDTO.setAuthorities(Set.of(AuthoritiesConstants.ADMIN));

        restAccountMockMvc
            .perform(post("/api/account").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userDTO)))
            .andExpect(status().isOk());

        User updatedUser = userRepository.findOneByLogin(user.getLogin()).orElse(null);
        assertThat(updatedUser.getEmail()).isEqualTo(userDTO.getEmail());
        assertThat(updatedUser.getLangKey()).isEqualTo(userDTO.getLangKey());
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());
        assertThat(updatedUser.getImageUrl()).isEqualTo(userDTO.getImageUrl());
        assertThat(updatedUser.isActivated()).isTrue();
        assertThat(updatedUser.getAuthorities()).isEmpty();

        userService.deleteUser("save-account");
    }

    @Test
    @WithMockUser("save-invalid-email")
    void testSaveInvalidEmail() throws Exception {
        User user = new User();
        user.setLogin("save-invalid-email");
        user.setEmail("save-invalid-email@example.com");
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(true);

        userRepository.save(user);

        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setLogin("not-used");
        userDTO.setEmail("invalid email");
        userDTO.setActivated(false);
        userDTO.setImageUrl("http://placehold.it/50x50");
        userDTO.setLangKey(Constants.DEFAULT_LANGUAGE);
        userDTO.setAuthorities(Set.of(AuthoritiesConstants.ADMIN));

        restAccountMockMvc
            .perform(post("/api/account").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userDTO)))
            .andExpect(status().isBadRequest());

        assertThat(userRepository.findOneByEmailIgnoreCase("invalid email")).isNotPresent();

        userService.deleteUser("save-invalid-email");
    }

    @Test
    @WithMockUser("save-existing-email")
    void testSaveExistingEmail() throws Exception {
        User user = new User();
        user.setLogin("save-existing-email");
        user.setEmail("save-existing-email@example.com");
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(true);
        userRepository.save(user);

        User anotherUser = new User();
        anotherUser.setLogin("save-existing-email2");
        anotherUser.setEmail("save-existing-email2@example.com");
        anotherUser.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        anotherUser.setActivated(true);

        userRepository.save(anotherUser);

        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setLogin("not-used");
        userDTO.setEmail("save-existing-email2@example.com");
        userDTO.setActivated(false);
        userDTO.setImageUrl("http://placehold.it/50x50");
        userDTO.setLangKey(Constants.DEFAULT_LANGUAGE);
        userDTO.setAuthorities(Set.of(AuthoritiesConstants.ADMIN));

        restAccountMockMvc
            .perform(post("/api/account").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userDTO)))
            .andExpect(status().isBadRequest());

        User updatedUser = userRepository.findOneByLogin("save-existing-email").orElseThrow();
        assertThat(updatedUser.getEmail()).isEqualTo("save-existing-email@example.com");

        userService.deleteUser("save-existing-email");
        userService.deleteUser("save-existing-email2");
    }

    @Test
    @WithMockUser("save-existing-email-and-login")
    void testSaveExistingEmailAndLogin() throws Exception {
        User user = new User();
        user.setLogin("save-existing-email-and-login");
        user.setEmail("save-existing-email-and-login@example.com");
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(true);
        userRepository.save(user);

        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setLogin("not-used");
        userDTO.setEmail("save-existing-email-and-login@example.com");
        userDTO.setActivated(false);
        userDTO.setImageUrl("http://placehold.it/50x50");
        userDTO.setLangKey(Constants.DEFAULT_LANGUAGE);
        userDTO.setAuthorities(Set.of(AuthoritiesConstants.ADMIN));

        restAccountMockMvc
            .perform(post("/api/account").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userDTO)))
            .andExpect(status().isOk());

        User updatedUser = userRepository.findOneByLogin("save-existing-email-and-login").orElse(null);
        assertThat(updatedUser.getEmail()).isEqualTo("save-existing-email-and-login@example.com");

        userService.deleteUser("save-existing-email-and-login");
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
