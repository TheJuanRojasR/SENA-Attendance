package com.mycompany.senaattendance.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.Authority;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.DocumentType;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.repository.AuthorityRepository;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.DocumentTypeRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.MailService;
import com.mycompany.senaattendance.service.dto.AdminUserDTO;
import com.mycompany.senaattendance.web.rest.vm.AdminCreateUserVM;
import com.mycompany.senaattendance.web.rest.vm.AdminUpdateUserVM;
import com.mycompany.senaattendance.web.rest.vm.SetUserActivatedVM;
import java.util.*;
import java.util.function.Consumer;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the {@link UserResource} REST controller.
 */
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
@IntegrationTest
class UserResourceIT {

    private static final String DEFAULT_LOGIN = "johndoe";
    private static final String DEFAULT_ID = "id1";

    private static final String DEFAULT_EMAIL = "johndoe@example.com";
    private static final String UPDATED_EMAIL = "jhipster@example.com";

    private static final String DEFAULT_IMAGEURL = "http://placehold.it/50x50";
    private static final String UPDATED_IMAGEURL = "http://placehold.it/40x40";

    private static final String DEFAULT_LANGKEY = "en";
    private static final String UPDATED_LANGKEY = "fr";

    private static final String DEFAULT_DOCUMENT = "JDOC0001";
    private static final String UPDATED_DOCUMENT = "JDOC0002";

    private static final String DEFAULT_PHONE = "3001234567";
    private static final String UPDATED_PHONE = "3007654321";

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
    private ClassSectionRepository classSectionRepository;

    @MockitoBean
    private MailService mailService;

    @Autowired
    private MockMvc restUserMockMvc;

    private User user;

    /**
     * Create a User.
     */
    public static User createEntity() {
        User persistUser = new User();
        persistUser.setLogin(DEFAULT_LOGIN);
        persistUser.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        persistUser.setActivated(true);
        persistUser.setEmail(DEFAULT_EMAIL);
        persistUser.setImageUrl(DEFAULT_IMAGEURL);
        persistUser.setLangKey(DEFAULT_LANGKEY);
        return persistUser;
    }

    /**
     * Setups the database with one user.
     */
    public static User initTestUser() {
        return createEntity();
    }

    @BeforeEach
    void initTest() {
        user = initTestUser();
    }

    @AfterEach
    void cleanupAndCheck() {
        classSectionRepository.deleteAll();
        userProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String seededDocumentTypeId() {
        return documentTypeRepository.findAll().iterator().next().getId();
    }

    /**
     * Derives the login that {@link com.mycompany.senaattendance.service.UserService#createUser} /
     * {@link com.mycompany.senaattendance.service.UserService#updateUser} now produce from the
     * seeded {@link DocumentType} initials: {@code <initials>_<documentNumber>}.
     */
    private String derivedLogin(String documentNumber) {
        DocumentType dt = documentTypeRepository.findById(seededDocumentTypeId()).orElseThrow();
        String typeCode = dt.getInitials() != null ? dt.getInitials() : "";
        return (typeCode + "_" + documentNumber).toLowerCase().trim();
    }

    private User persistedUser(String login, String email) {
        User u = new User();
        u.setLogin(login);
        u.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        u.setActivated(true);
        u.setEmail(email);
        u.setImageUrl(DEFAULT_IMAGEURL);
        u.setLangKey(DEFAULT_LANGKEY);
        return userRepository.save(u);
    }

    private UserProfile persistedProfile(User user, String documentNumber) {
        return userProfileRepository.save(
            new UserProfile()
                .firstName("John")
                .middleName("M")
                .firstLastName("Doe")
                .secondLastName("S")
                .documentNumber(documentNumber)
                .phoneNumber(DEFAULT_PHONE)
                .user(user)
                .documentType(documentTypeRepository.findById(seededDocumentTypeId()).orElseThrow())
        );
    }

    private User persistedUserWithProfile(String documentNumber, String email) {
        User u = persistedUser(documentNumber.toLowerCase(), email);
        persistedProfile(u, documentNumber);
        return u;
    }

    private boolean hasAuthority(User u, String authority) {
        return u.getAuthorities().stream().map(Authority::getName).anyMatch(authority::equals);
    }

    private Set<Authority> authoritySet(String... names) {
        Set<Authority> authorities = new HashSet<>();
        for (String name : names) {
            authorities.add(authorityRepository.findById(name).orElseThrow());
        }
        return authorities;
    }

    /**
     * Returns the protected super admin (login "admin"). If the mongock seed is still present we reuse it;
     * otherwise we create a fresh ADMIN user with that login, so the E3 protected-admin rule is deterministic.
     */
    private User protectedAdmin() {
        return userRepository.findOneByLogin("admin").orElseGet(() -> {
            User u = persistedUser("admin", "protected.admin@example.com");
            u.setAuthorities(authoritySet(AuthoritiesConstants.ADMIN, AuthoritiesConstants.USER));
            return userRepository.save(u);
        });
    }

    private User freshAdmin(String login, String email) {
        User u = persistedUser(login, email);
        u.setAuthorities(authoritySet(AuthoritiesConstants.ADMIN, AuthoritiesConstants.USER));
        return userRepository.save(u);
    }

    private User instructorUser(String login, String email) {
        User u = persistedUser(login, email);
        u.setAuthorities(authoritySet(AuthoritiesConstants.INSTRUCTOR, AuthoritiesConstants.USER));
        return userRepository.save(u);
    }

    private AdminUpdateUserVM buildUpdateVM(String id, String documentNumber, String email, String role, String langKey, String imageUrl) {
        AdminUpdateUserVM vm = new AdminUpdateUserVM();
        vm.setId(id);
        vm.setEmail(email);
        vm.setFirstName("Ana");
        vm.setMiddleName("Maria");
        vm.setFirstLastName("Gomez");
        vm.setSecondLastName("Rodriguez");
        vm.setDocumentNumber(documentNumber);
        vm.setPhoneNumber(UPDATED_PHONE);
        vm.setDocumentTypeId(seededDocumentTypeId());
        vm.setRole(role);
        vm.setLangKey(langKey);
        vm.setImageUrl(imageUrl);
        return vm;
    }

    @Test
    void createUser() throws Exception {
        String documentNumber = "JDOC0001";
        String expectedLogin = derivedLogin(documentNumber);
        AdminCreateUserVM userVM = new AdminCreateUserVM();
        userVM.setLogin("ignored.create.login");
        userVM.setEmail(DEFAULT_EMAIL);
        userVM.setPassword("Passw0rd!");
        userVM.setFirstName("John");
        userVM.setMiddleName("M");
        userVM.setFirstLastName("Doe");
        userVM.setSecondLastName("S");
        userVM.setDocumentNumber(documentNumber);
        userVM.setPhoneNumber("3001234567");
        userVM.setDocumentTypeId(seededDocumentTypeId());
        userVM.setRole(AuthoritiesConstants.INSTRUCTOR);
        userVM.setActivated(true);
        userVM.setLangKey(DEFAULT_LANGKEY);
        // client authorities are ignored: the role field drives the authority set
        userVM.setAuthorities(Set.of(AuthoritiesConstants.ADMIN));

        restUserMockMvc
            .perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userVM)))
            .andExpect(status().isCreated());

        User createdUser = userRepository.findOneByLogin(expectedLogin).orElseThrow();
        assertThat(createdUser.getLogin()).isEqualTo(expectedLogin);
        assertThat(createdUser.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(createdUser.getImageUrl()).isNull();
        assertThat(createdUser.getLangKey()).isEqualTo(DEFAULT_LANGKEY);
        assertThat(createdUser.isActivated()).isTrue();
        // role field produces {ROLE_USER, ROLE_INSTRUCTOR}
        assertThat(createdUser.getAuthorities().stream().map(Authority::getName)).containsExactlyInAnyOrder(
            AuthoritiesConstants.USER,
            AuthoritiesConstants.INSTRUCTOR
        );
    }

    @Test
    void createUserWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        AdminCreateUserVM userVM = new AdminCreateUserVM();
        userVM.setId("1L");
        userVM.setLogin("jdoc0002");
        userVM.setEmail("johndoe2@example.com");
        userVM.setPassword("Passw0rd!");
        userVM.setFirstName("John");
        userVM.setMiddleName("M");
        userVM.setFirstLastName("Doe");
        userVM.setSecondLastName("S");
        userVM.setDocumentNumber("JDOC0002");
        userVM.setPhoneNumber("3001234567");
        userVM.setDocumentTypeId(seededDocumentTypeId());
        userVM.setRole(AuthoritiesConstants.INSTRUCTOR);
        userVM.setActivated(true);
        userVM.setLangKey(DEFAULT_LANGKEY);
        userVM.setAuthorities(Set.of(AuthoritiesConstants.INSTRUCTOR));

        restUserMockMvc
            .perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userVM)))
            .andExpect(status().isBadRequest());

        assertPersistedUsers(users -> assertThat(users).hasSize(databaseSizeBeforeCreate));
    }

    @Test
    void createUserWithExistingLogin() throws Exception {
        // the existing user's login must equal the derived login (cc_johndoe) so the collision triggers
        user.setLogin(derivedLogin(DEFAULT_LOGIN));
        userRepository.save(user);
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        AdminCreateUserVM userVM = new AdminCreateUserVM();
        userVM.setLogin(DEFAULT_LOGIN);
        userVM.setEmail("anothermail@example.com");
        userVM.setPassword("Passw0rd!");
        userVM.setFirstName("John");
        userVM.setMiddleName("M");
        userVM.setFirstLastName("Doe");
        userVM.setSecondLastName("S");
        userVM.setDocumentNumber(DEFAULT_LOGIN); // derived login (cc_johndoe) already used
        userVM.setPhoneNumber("3001234567");
        userVM.setDocumentTypeId(seededDocumentTypeId());
        userVM.setRole(AuthoritiesConstants.INSTRUCTOR);
        userVM.setActivated(true);
        userVM.setLangKey(DEFAULT_LANGKEY);
        userVM.setAuthorities(Set.of(AuthoritiesConstants.INSTRUCTOR));

        restUserMockMvc
            .perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userVM)))
            .andExpect(status().isBadRequest());

        assertPersistedUsers(users -> assertThat(users).hasSize(databaseSizeBeforeCreate));
    }

    @Test
    void createUserWithExistingEmail() throws Exception {
        userRepository.save(user);
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        AdminCreateUserVM userVM = new AdminCreateUserVM();
        userVM.setLogin("jdoc0003");
        userVM.setEmail(DEFAULT_EMAIL); // already used
        userVM.setPassword("Passw0rd!");
        userVM.setFirstName("John");
        userVM.setMiddleName("M");
        userVM.setFirstLastName("Doe");
        userVM.setSecondLastName("S");
        userVM.setDocumentNumber("JDOC0003");
        userVM.setPhoneNumber("3001234567");
        userVM.setDocumentTypeId(seededDocumentTypeId());
        userVM.setRole(AuthoritiesConstants.INSTRUCTOR);
        userVM.setActivated(true);
        userVM.setLangKey(DEFAULT_LANGKEY);
        userVM.setAuthorities(Set.of(AuthoritiesConstants.INSTRUCTOR));

        restUserMockMvc
            .perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userVM)))
            .andExpect(status().isBadRequest());

        assertPersistedUsers(users -> assertThat(users).hasSize(databaseSizeBeforeCreate));
    }

    @Test
    void getAllUsers() throws Exception {
        userRepository.save(user);

        restUserMockMvc
            .perform(get("/api/admin/users").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].login").value(hasItem(DEFAULT_LOGIN)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].imageUrl").value(hasItem(DEFAULT_IMAGEURL)))
            .andExpect(jsonPath("$.[*].langKey").value(hasItem(DEFAULT_LANGKEY)));
    }

    @Test
    void getUser() throws Exception {
        userRepository.save(user);

        restUserMockMvc
            .perform(get("/api/admin/users/{login}", user.getLogin()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.login").value(user.getLogin()))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.imageUrl").value(DEFAULT_IMAGEURL))
            .andExpect(jsonPath("$.langKey").value(DEFAULT_LANGKEY));
    }

    @Test
    void getNonExistingUser() throws Exception {
        restUserMockMvc.perform(get("/api/admin/users/unknown")).andExpect(status().isNotFound());
    }

    @Test
    void updateUser() throws Exception {
        User user = persistedUserWithProfile(DEFAULT_DOCUMENT, DEFAULT_EMAIL);
        int databaseSizeBeforeUpdate = userRepository.findAll().size();

        AdminUpdateUserVM vm = buildUpdateVM(
            user.getId(),
            DEFAULT_DOCUMENT,
            UPDATED_EMAIL,
            AuthoritiesConstants.INSTRUCTOR,
            UPDATED_LANGKEY,
            UPDATED_IMAGEURL
        );
        vm.setLogin("ignored.login"); // login is derived from documentNumber

        restUserMockMvc
            .perform(patch("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vm)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.login").value(derivedLogin(DEFAULT_DOCUMENT)))
            .andExpect(jsonPath("$.email").value(UPDATED_EMAIL))
            .andExpect(jsonPath("$.langKey").value(UPDATED_LANGKEY))
            .andExpect(jsonPath("$.activated").value(true))
            .andExpect(jsonPath("$.authorities").value(containsInAnyOrder(AuthoritiesConstants.USER, AuthoritiesConstants.INSTRUCTOR)));

        assertPersistedUsers(users -> {
            assertThat(users).hasSize(databaseSizeBeforeUpdate);
            User testUser = users
                .stream()
                .filter(usr -> usr.getId().equals(user.getId()))
                .findFirst()
                .orElseThrow();
            assertThat(testUser.getLogin()).isEqualTo(derivedLogin(DEFAULT_DOCUMENT));
            assertThat(testUser.getEmail()).isEqualTo(UPDATED_EMAIL);
            assertThat(testUser.getImageUrl()).isEqualTo(UPDATED_IMAGEURL);
            assertThat(testUser.getLangKey()).isEqualTo(UPDATED_LANGKEY);
            assertThat(testUser.isActivated()).isTrue();
            assertThat(testUser.getAuthorities().stream().map(Authority::getName)).containsExactlyInAnyOrder(
                AuthoritiesConstants.USER,
                AuthoritiesConstants.INSTRUCTOR
            );
        });

        UserProfile updatedProfile = userProfileRepository.findOneByUserId(user.getId()).orElseThrow();
        assertThat(updatedProfile.getFirstName()).isEqualTo("Ana");
        assertThat(updatedProfile.getMiddleName()).isEqualTo("Maria");
        assertThat(updatedProfile.getFirstLastName()).isEqualTo("Gomez");
        assertThat(updatedProfile.getSecondLastName()).isEqualTo("Rodriguez");
        assertThat(updatedProfile.getDocumentNumber()).isEqualTo(DEFAULT_DOCUMENT);
        assertThat(updatedProfile.getPhoneNumber()).isEqualTo(UPDATED_PHONE);
        assertThat(updatedProfile.getDocumentType().getId()).isEqualTo(seededDocumentTypeId());
    }

    @Test
    void updateUserRedirectsLogin() throws Exception {
        User user = persistedUserWithProfile(DEFAULT_DOCUMENT, DEFAULT_EMAIL);
        int databaseSizeBeforeUpdate = userRepository.findAll().size();

        AdminUpdateUserVM vm = buildUpdateVM(
            user.getId(),
            UPDATED_DOCUMENT,
            DEFAULT_EMAIL,
            AuthoritiesConstants.INSTRUCTOR,
            DEFAULT_LANGKEY,
            DEFAULT_IMAGEURL
        );

        restUserMockMvc
            .perform(patch("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vm)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.login").value(derivedLogin(UPDATED_DOCUMENT)));

        assertThat(userRepository.findOneByLogin(derivedLogin(DEFAULT_DOCUMENT))).isEmpty();
        assertThat(userRepository.findOneByLogin(derivedLogin(UPDATED_DOCUMENT))).isPresent();

        assertPersistedUsers(users -> assertThat(users).hasSize(databaseSizeBeforeUpdate));
        UserProfile updatedProfile = userProfileRepository.findOneByUserId(user.getId()).orElseThrow();
        assertThat(updatedProfile.getDocumentNumber()).isEqualTo(UPDATED_DOCUMENT);
    }

    @Test
    void updateUserRoleChangesAuthorities() throws Exception {
        User user = persistedUserWithProfile(DEFAULT_DOCUMENT, DEFAULT_EMAIL);
        user.setAuthorities(
            new HashSet<>(
                Set.of(
                    authorityRepository.findById(AuthoritiesConstants.USER).orElseThrow(),
                    authorityRepository.findById(AuthoritiesConstants.ADMIN).orElseThrow()
                )
            )
        );
        userRepository.save(user);

        AdminUpdateUserVM vm = buildUpdateVM(
            user.getId(),
            DEFAULT_DOCUMENT,
            DEFAULT_EMAIL,
            AuthoritiesConstants.INSTRUCTOR,
            DEFAULT_LANGKEY,
            DEFAULT_IMAGEURL
        );

        restUserMockMvc
            .perform(patch("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vm)))
            .andExpect(status().isOk());

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getAuthorities().stream().map(Authority::getName)).containsExactlyInAnyOrder(
            AuthoritiesConstants.USER,
            AuthoritiesConstants.INSTRUCTOR
        );
    }

    @Test
    void updateUserPartialFirstName() throws Exception {
        User user = persistedUserWithProfile(DEFAULT_DOCUMENT, DEFAULT_EMAIL);

        // firstName is now optional: a PATCH that only carries firstName updates just that field
        AdminUpdateUserVM vm = new AdminUpdateUserVM();
        vm.setId(user.getId());
        vm.setFirstName("Ana");

        restUserMockMvc
            .perform(patch("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vm)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.login").value(DEFAULT_DOCUMENT.toLowerCase()));

        UserProfile profile = userProfileRepository.findOneByUserId(user.getId()).orElseThrow();
        assertThat(profile.getFirstName()).isEqualTo("Ana");
        // untouched profile fields remain
        assertThat(profile.getFirstLastName()).isEqualTo("Doe");
        assertThat(profile.getDocumentNumber()).isEqualTo(DEFAULT_DOCUMENT);
        assertThat(profile.getPhoneNumber()).isEqualTo(DEFAULT_PHONE);
        User unchanged = userRepository.findById(user.getId()).orElseThrow();
        assertThat(unchanged.getLogin()).isEqualTo(DEFAULT_DOCUMENT.toLowerCase());
        assertThat(unchanged.getEmail()).isEqualTo(DEFAULT_EMAIL);
    }

    @Test
    void updateUserPartialEmail() throws Exception {
        User user = persistedUserWithProfile(DEFAULT_DOCUMENT, DEFAULT_EMAIL);

        // PATCH carrying ONLY email: login and every other field stay untouched
        AdminUpdateUserVM vm = new AdminUpdateUserVM();
        vm.setId(user.getId());
        vm.setEmail(UPDATED_EMAIL);

        restUserMockMvc
            .perform(patch("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vm)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.login").value(DEFAULT_DOCUMENT.toLowerCase()))
            .andExpect(jsonPath("$.email").value(UPDATED_EMAIL));

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getLogin()).isEqualTo(DEFAULT_DOCUMENT.toLowerCase());
        assertThat(updated.getEmail()).isEqualTo(UPDATED_EMAIL);
        // untouched fields preserved
        assertThat(updated.getImageUrl()).isEqualTo(DEFAULT_IMAGEURL);
        assertThat(updated.getLangKey()).isEqualTo(DEFAULT_LANGKEY);
        assertThat(updated.isActivated()).isTrue();
        UserProfile profile = userProfileRepository.findOneByUserId(user.getId()).orElseThrow();
        assertThat(profile.getDocumentNumber()).isEqualTo(DEFAULT_DOCUMENT);
        assertThat(profile.getFirstName()).isEqualTo("John");
    }

    @Test
    void updateUserPartialRole() throws Exception {
        User user = persistedUserWithProfile(DEFAULT_DOCUMENT, DEFAULT_EMAIL);
        user.setAuthorities(
            new HashSet<>(
                Set.of(
                    authorityRepository.findById(AuthoritiesConstants.USER).orElseThrow(),
                    authorityRepository.findById(AuthoritiesConstants.ADMIN).orElseThrow()
                )
            )
        );
        userRepository.save(user);

        // PATCH carrying ONLY role: authorities must change, login stays
        AdminUpdateUserVM vm = new AdminUpdateUserVM();
        vm.setId(user.getId());
        vm.setRole(AuthoritiesConstants.INSTRUCTOR);

        restUserMockMvc
            .perform(patch("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vm)))
            .andExpect(status().isOk());

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getAuthorities().stream().map(Authority::getName)).containsExactlyInAnyOrder(
            AuthoritiesConstants.USER,
            AuthoritiesConstants.INSTRUCTOR
        );
        assertThat(updated.getLogin()).isEqualTo(DEFAULT_DOCUMENT.toLowerCase());
    }

    @Test
    void updateUserEmptyPatch() throws Exception {
        User user = persistedUserWithProfile(DEFAULT_DOCUMENT, DEFAULT_EMAIL);

        // an empty PATCH (only id) is a no-op that still returns the user with 200
        AdminUpdateUserVM vm = new AdminUpdateUserVM();
        vm.setId(user.getId());

        restUserMockMvc
            .perform(patch("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vm)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.login").value(DEFAULT_DOCUMENT.toLowerCase()))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL));

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getLogin()).isEqualTo(DEFAULT_DOCUMENT.toLowerCase());
        assertThat(updated.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(updated.getImageUrl()).isEqualTo(DEFAULT_IMAGEURL);
        assertThat(updated.getLangKey()).isEqualTo(DEFAULT_LANGKEY);
        UserProfile profile = userProfileRepository.findOneByUserId(user.getId()).orElseThrow();
        assertThat(profile.getFirstName()).isEqualTo("John");
        assertThat(profile.getDocumentNumber()).isEqualTo(DEFAULT_DOCUMENT);
    }

    @Test
    void updateUserDocumentNumberAlreadyUsed() throws Exception {
        User userA = persistedUserWithProfile("JDOCA", "usera@example.com");
        // userB has a profile whose documentNumber (JDOCB) is taken by another user (login not doc-derived)
        persistedUser("some.other.login", "userb@example.com");
        persistedProfile(userRepository.findOneByLogin("some.other.login").orElseThrow(), "JDOCB");
        int databaseSizeBeforeUpdate = userRepository.findAll().size();

        AdminUpdateUserVM vm = buildUpdateVM(
            userA.getId(),
            "JDOCB",
            "usera@example.com",
            AuthoritiesConstants.INSTRUCTOR,
            DEFAULT_LANGKEY,
            DEFAULT_IMAGEURL
        );

        restUserMockMvc
            .perform(patch("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vm)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.documentnumberexists"));

        assertPersistedUsers(users -> assertThat(users).hasSize(databaseSizeBeforeUpdate));
        assertThat(userProfileRepository.findByDocumentNumber("JDOCA")).isPresent();
    }

    @Test
    void updateUserDocumentTypeNotFound() throws Exception {
        User user = persistedUserWithProfile(DEFAULT_DOCUMENT, DEFAULT_EMAIL);

        AdminUpdateUserVM vm = buildUpdateVM(
            user.getId(),
            DEFAULT_DOCUMENT,
            DEFAULT_EMAIL,
            AuthoritiesConstants.INSTRUCTOR,
            DEFAULT_LANGKEY,
            DEFAULT_IMAGEURL
        );
        vm.setDocumentTypeId("000000000000000000000000"); // not found

        restUserMockMvc
            .perform(patch("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vm)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.documentTypeNotFound"));
    }

    @Test
    void updateUserRoleNotFound() throws Exception {
        User user = persistedUserWithProfile(DEFAULT_DOCUMENT, DEFAULT_EMAIL);

        AdminUpdateUserVM vm = buildUpdateVM(
            user.getId(),
            DEFAULT_DOCUMENT,
            DEFAULT_EMAIL,
            "ROLE_NONEXISTENT",
            DEFAULT_LANGKEY,
            DEFAULT_IMAGEURL
        );

        restUserMockMvc
            .perform(patch("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vm)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.rolenotfound"));
    }

    @Test
    void updateUserEmailAlreadyUsed() throws Exception {
        User userA = persistedUserWithProfile("JDOCA", "usera@example.com");
        persistedUser("userb.login", "userb@example.com");
        persistedProfile(userRepository.findOneByLogin("userb.login").orElseThrow(), "JDOCB");
        int databaseSizeBeforeUpdate = userRepository.findAll().size();

        AdminUpdateUserVM vm = buildUpdateVM(
            userA.getId(),
            "JDOCA",
            "userb@example.com", // used by another user
            AuthoritiesConstants.INSTRUCTOR,
            DEFAULT_LANGKEY,
            DEFAULT_IMAGEURL
        );

        restUserMockMvc
            .perform(patch("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vm)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.emailexists"));

        assertPersistedUsers(users -> assertThat(users).hasSize(databaseSizeBeforeUpdate));
        assertThat(userRepository.findById(userA.getId()).orElseThrow().getEmail()).isEqualTo("usera@example.com");
    }

    @Test
    void updateUserNotFound() throws Exception {
        AdminUpdateUserVM vm = buildUpdateVM(
            "nonexistent-id",
            DEFAULT_DOCUMENT,
            DEFAULT_EMAIL,
            AuthoritiesConstants.INSTRUCTOR,
            DEFAULT_LANGKEY,
            DEFAULT_IMAGEURL
        );

        restUserMockMvc
            .perform(patch("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vm)))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser() throws Exception {
        userRepository.save(user);
        int databaseSizeBeforeDelete = userRepository.findAll().size();

        restUserMockMvc
            .perform(delete("/api/admin/users/{login}", user.getLogin()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        assertPersistedUsers(users -> assertThat(users).hasSize(databaseSizeBeforeDelete - 1));
    }

    @Test
    void setUserActivatedDeactivate() throws Exception {
        persistedUserWithProfile(DEFAULT_DOCUMENT, DEFAULT_EMAIL);

        SetUserActivatedVM vm = new SetUserActivatedVM();
        vm.setDocumentNumber(DEFAULT_DOCUMENT);
        vm.setActivated(false);

        restUserMockMvc
            .perform(patch("/api/admin/users/activated").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vm)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.login").value(DEFAULT_DOCUMENT.toLowerCase()))
            .andExpect(jsonPath("$.activated").value(false));

        UserProfile profile = userProfileRepository.findByDocumentNumber(DEFAULT_DOCUMENT).orElseThrow();
        User deactivated = userRepository.findById(profile.getUser().getId()).orElseThrow();
        assertThat(deactivated.isActivated()).isFalse();
        // the rest of the profile stays untouched
        assertThat(profile.getFirstName()).isEqualTo("John");
        assertThat(profile.getDocumentNumber()).isEqualTo(DEFAULT_DOCUMENT);
    }

    @Test
    void setUserActivatedActivate() throws Exception {
        User user = persistedUserWithProfile(DEFAULT_DOCUMENT, DEFAULT_EMAIL);
        user.setActivated(false);
        userRepository.save(user);

        SetUserActivatedVM vm = new SetUserActivatedVM();
        vm.setDocumentNumber(DEFAULT_DOCUMENT);
        vm.setActivated(true);

        restUserMockMvc
            .perform(patch("/api/admin/users/activated").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vm)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.login").value(DEFAULT_DOCUMENT.toLowerCase()))
            .andExpect(jsonPath("$.activated").value(true));

        UserProfile profile = userProfileRepository.findByDocumentNumber(DEFAULT_DOCUMENT).orElseThrow();
        User activated = userRepository.findById(profile.getUser().getId()).orElseThrow();
        assertThat(activated.isActivated()).isTrue();
    }

    @Test
    void setUserActivatedNonExistingUser() throws Exception {
        SetUserActivatedVM vm = new SetUserActivatedVM();
        vm.setDocumentNumber("UNKNOWN0001");
        vm.setActivated(false);

        restUserMockMvc
            .perform(patch("/api/admin/users/activated").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vm)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.documentNumberNotFound"));
    }

    @Test
    void setUserActivatedProtectedAdminBlocked() throws Exception {
        // The protected super admin (login "admin") can NEVER be deactivated. We reuse the seed admin if
        // present, or build it fresh, so the test is deterministic regardless of test ordering.
        User adminUser = protectedAdmin();
        persistedProfile(adminUser, "E3PROT01");

        SetUserActivatedVM vm = new SetUserActivatedVM();
        vm.setDocumentNumber("E3PROT01");
        vm.setActivated(false);

        restUserMockMvc
            .perform(patch("/api/admin/users/activated").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vm)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.lastAdmin"));

        assertThat(userRepository.findById(adminUser.getId()).orElseThrow().isActivated()).isTrue();
    }

    @Test
    void setUserActivatedLastAdminBlocked() throws Exception {
        // Make the target the ONLY active admin by deactivating every other active admin first.
        User target = freshAdmin("last.admin", "last.admin@example.com");
        persistedProfile(target, "E3LAST01");

        for (User u : userRepository.findAll()) {
            if (u.getId().equals(target.getId()) || !u.isActivated()) {
                continue;
            }
            if (hasAuthority(u, AuthoritiesConstants.ADMIN)) {
                u.setActivated(false);
                userRepository.save(u);
            }
        }

        SetUserActivatedVM vm = new SetUserActivatedVM();
        vm.setDocumentNumber("E3LAST01");
        vm.setActivated(false);

        restUserMockMvc
            .perform(patch("/api/admin/users/activated").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vm)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.lastAdmin"));

        assertThat(userRepository.findById(target.getId()).orElseThrow().isActivated()).isTrue();
    }

    @Test
    void setUserActivatedOnlyInstructorBlocked() throws Exception {
        User instructor = instructorUser("only.instr", "only.instr@example.com");
        UserProfile profile = persistedProfile(instructor, "E5INSTR01");

        ClassSection activeSection = new ClassSection().subjectName("E5 Ficha Activa").isActive(true).instructor(profile);
        classSectionRepository.save(activeSection);

        SetUserActivatedVM vm = new SetUserActivatedVM();
        vm.setDocumentNumber("E5INSTR01");
        vm.setActivated(false);

        restUserMockMvc
            .perform(patch("/api/admin/users/activated").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vm)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.lastInstructor"));

        assertThat(userRepository.findById(instructor.getId()).orElseThrow().isActivated()).isTrue();
    }

    @Test
    void setUserActivatedInstructorNoActiveSectionsOk() throws Exception {
        // Instructor has NO active class section (or only inactive ones): deactivation must succeed.
        User instructor = instructorUser("noact.instr", "noact.instr@example.com");
        UserProfile profile = persistedProfile(instructor, "E5OK01");

        // A single INACTIVE section does not trigger the E5 rule.
        ClassSection inactiveSection = new ClassSection().subjectName("E5 Ficha Inactiva").isActive(false).instructor(profile);
        classSectionRepository.save(inactiveSection);

        SetUserActivatedVM vm = new SetUserActivatedVM();
        vm.setDocumentNumber("E5OK01");
        vm.setActivated(false);

        restUserMockMvc
            .perform(patch("/api/admin/users/activated").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vm)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.login").value("noact.instr"))
            .andExpect(jsonPath("$.activated").value(false));

        assertThat(userRepository.findById(instructor.getId()).orElseThrow().isActivated()).isFalse();
    }

    @Test
    void testUserEquals() throws Exception {
        com.mycompany.senaattendance.web.rest.TestUtil.equalsVerifier(User.class);
        User user1 = new User();
        user1.setId(DEFAULT_ID);
        User user2 = new User();
        user2.setId(user1.getId());
        assertThat(user1).isEqualTo(user2);
        user2.setId("id2");
        assertThat(user1).isNotEqualTo(user2);
        user1.setId(null);
        assertThat(user1).isNotEqualTo(user2);
    }

    private void assertPersistedUsers(Consumer<List<User>> userAssertion) {
        userAssertion.accept(userRepository.findAll());
    }
}
