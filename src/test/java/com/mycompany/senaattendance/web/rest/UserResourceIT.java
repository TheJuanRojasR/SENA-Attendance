package com.mycompany.senaattendance.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.repository.DocumentTypeRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.dto.AdminUserDTO;
import com.mycompany.senaattendance.web.rest.vm.AdminCreateUserVM;
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
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the {@link UserResource} REST controller.
 */
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
@IntegrationTest
class UserResourceIT {

    private static final String DEFAULT_LOGIN = "johndoe";
    private static final String UPDATED_LOGIN = "jhipster";

    private static final String DEFAULT_ID = "id1";

    private static final String DEFAULT_EMAIL = "johndoe@example.com";
    private static final String UPDATED_EMAIL = "jhipster@example.com";

    private static final String DEFAULT_IMAGEURL = "http://placehold.it/50x50";
    private static final String UPDATED_IMAGEURL = "http://placehold.it/40x40";

    private static final String DEFAULT_LANGKEY = "en";
    private static final String UPDATED_LANGKEY = "fr";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private MockMvc restUserMockMvc;

    private User user;

    /**
     * Create a User.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which has a required relationship to the User entity.
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
        User persistUser = createEntity();
        return persistUser;
    }

    @BeforeEach
    void initTest() {
        user = initTestUser();
    }

    @AfterEach
    void cleanupAndCheck() {
        userProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createUser() throws Exception {
        // Create the User
        String documentNumber = "JDOC0001";
        String expectedLogin = documentNumber.toLowerCase().trim();
        AdminCreateUserVM userVM = new AdminCreateUserVM();
        userVM.setLogin(expectedLogin);
        userVM.setEmail(DEFAULT_EMAIL);
        userVM.setPassword("Passw0rd!");
        userVM.setFirstName("John");
        userVM.setMiddleName("M");
        userVM.setFirstLastName("Doe");
        userVM.setSecondLastName("S");
        userVM.setDocumentNumber(documentNumber);
        userVM.setPhoneNumber("3001234567");
        userVM.setDocumentTypeId(seededDocumentTypeId());
        userVM.setRole(AuthoritiesConstants.USER);
        userVM.setActivated(true);
        userVM.setLangKey(DEFAULT_LANGKEY);
        userVM.setAuthorities(Set.of(AuthoritiesConstants.USER));

        // Create the User
        restUserMockMvc
            .perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userVM)))
            .andExpect(status().isCreated());

        // Validate the persisted User (login is derived from the document number, imageUrl is forced null)
        User createdUser = userRepository.findOneByLogin(expectedLogin).orElseThrow();
        assertThat(createdUser.getLogin()).isEqualTo(expectedLogin);
        assertThat(createdUser.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(createdUser.getImageUrl()).isNull();
        assertThat(createdUser.getLangKey()).isEqualTo(DEFAULT_LANGKEY);
        assertThat(createdUser.isActivated()).isTrue();
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
        userVM.setRole(AuthoritiesConstants.USER);
        userVM.setActivated(true);
        userVM.setLangKey(DEFAULT_LANGKEY);
        userVM.setAuthorities(Set.of(AuthoritiesConstants.USER));

        // An entity with an existing ID cannot be created, so this API call must fail
        restUserMockMvc
            .perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userVM)))
            .andExpect(status().isBadRequest());

        // Validate the User in the database
        assertPersistedUsers(users -> assertThat(users).hasSize(databaseSizeBeforeCreate));
    }

    @Test
    void createUserWithExistingLogin() throws Exception {
        // Initialize the database
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
        userVM.setDocumentNumber(DEFAULT_LOGIN); // this derived login (johndoe) should already be used
        userVM.setPhoneNumber("3001234567");
        userVM.setDocumentTypeId(seededDocumentTypeId());
        userVM.setRole(AuthoritiesConstants.USER);
        userVM.setActivated(true);
        userVM.setLangKey(DEFAULT_LANGKEY);
        userVM.setAuthorities(Set.of(AuthoritiesConstants.USER));

        // Create the User
        restUserMockMvc
            .perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userVM)))
            .andExpect(status().isBadRequest());

        // Validate the User in the database
        assertPersistedUsers(users -> assertThat(users).hasSize(databaseSizeBeforeCreate));
    }

    @Test
    void createUserWithExistingEmail() throws Exception {
        // Initialize the database
        userRepository.save(user);
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        AdminCreateUserVM userVM = new AdminCreateUserVM();
        userVM.setLogin("jdoc0003");
        userVM.setEmail(DEFAULT_EMAIL); // this email should already be used
        userVM.setPassword("Passw0rd!");
        userVM.setFirstName("John");
        userVM.setMiddleName("M");
        userVM.setFirstLastName("Doe");
        userVM.setSecondLastName("S");
        userVM.setDocumentNumber("JDOC0003");
        userVM.setPhoneNumber("3001234567");
        userVM.setDocumentTypeId(seededDocumentTypeId());
        userVM.setRole(AuthoritiesConstants.USER);
        userVM.setActivated(true);
        userVM.setLangKey(DEFAULT_LANGKEY);
        userVM.setAuthorities(Set.of(AuthoritiesConstants.USER));

        // Create the User
        restUserMockMvc
            .perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userVM)))
            .andExpect(status().isBadRequest());

        // Validate the User in the database
        assertPersistedUsers(users -> assertThat(users).hasSize(databaseSizeBeforeCreate));
    }

    @Test
    void getAllUsers() throws Exception {
        // Initialize the database
        userRepository.save(user);

        // Get all the users
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
        // Initialize the database
        userRepository.save(user);

        // Get the user
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
        // Initialize the database
        userRepository.save(user);
        int databaseSizeBeforeUpdate = userRepository.findAll().size();

        // Update the user
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();

        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setId(updatedUser.getId());
        userDTO.setLogin(updatedUser.getLogin());
        userDTO.setEmail(UPDATED_EMAIL);
        userDTO.setActivated(updatedUser.isActivated());
        userDTO.setImageUrl(UPDATED_IMAGEURL);
        userDTO.setLangKey(UPDATED_LANGKEY);
        userDTO.setCreatedBy(updatedUser.getCreatedBy());
        userDTO.setCreatedDate(updatedUser.getCreatedDate());
        userDTO.setLastModifiedBy(updatedUser.getLastModifiedBy());
        userDTO.setLastModifiedDate(updatedUser.getLastModifiedDate());
        userDTO.setAuthorities(Set.of(AuthoritiesConstants.USER));

        restUserMockMvc
            .perform(put("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userDTO)))
            .andExpect(status().isOk());

        // Validate the User in the database
        assertPersistedUsers(users -> {
            assertThat(users).hasSize(databaseSizeBeforeUpdate);
            User testUser = users
                .stream()
                .filter(usr -> usr.getId().equals(updatedUser.getId()))
                .findFirst()
                .orElseThrow();
            assertThat(testUser.getEmail()).isEqualTo(UPDATED_EMAIL);
            assertThat(testUser.getImageUrl()).isEqualTo(UPDATED_IMAGEURL);
            assertThat(testUser.getLangKey()).isEqualTo(UPDATED_LANGKEY);
        });
    }

    @Test
    void updateUserLogin() throws Exception {
        // Initialize the database
        userRepository.save(user);
        int databaseSizeBeforeUpdate = userRepository.findAll().size();

        // Update the user
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();

        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setId(updatedUser.getId());
        userDTO.setLogin(UPDATED_LOGIN);
        userDTO.setEmail(UPDATED_EMAIL);
        userDTO.setActivated(updatedUser.isActivated());
        userDTO.setImageUrl(UPDATED_IMAGEURL);
        userDTO.setLangKey(UPDATED_LANGKEY);
        userDTO.setCreatedBy(updatedUser.getCreatedBy());
        userDTO.setCreatedDate(updatedUser.getCreatedDate());
        userDTO.setLastModifiedBy(updatedUser.getLastModifiedBy());
        userDTO.setLastModifiedDate(updatedUser.getLastModifiedDate());
        userDTO.setAuthorities(Set.of(AuthoritiesConstants.USER));

        restUserMockMvc
            .perform(put("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userDTO)))
            .andExpect(status().isOk());

        // Validate the User in the database
        assertPersistedUsers(users -> {
            assertThat(users).hasSize(databaseSizeBeforeUpdate);
            User testUser = users
                .stream()
                .filter(usr -> usr.getId().equals(updatedUser.getId()))
                .findFirst()
                .orElseThrow();
            assertThat(testUser.getLogin()).isEqualTo(UPDATED_LOGIN);
            assertThat(testUser.getEmail()).isEqualTo(UPDATED_EMAIL);
            assertThat(testUser.getImageUrl()).isEqualTo(UPDATED_IMAGEURL);
            assertThat(testUser.getLangKey()).isEqualTo(UPDATED_LANGKEY);
        });
    }

    @Test
    void updateUserExistingEmail() throws Exception {
        // Initialize the database with 2 users
        userRepository.save(user);

        User anotherUser = new User();
        anotherUser.setLogin("jhipster");
        anotherUser.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        anotherUser.setActivated(true);
        anotherUser.setEmail("jhipster@example.com");
        anotherUser.setImageUrl("");
        anotherUser.setLangKey("en");
        userRepository.save(anotherUser);

        // Update the user
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();

        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setId(updatedUser.getId());
        userDTO.setLogin(updatedUser.getLogin());
        userDTO.setEmail("jhipster@example.com"); // this email should already be used by anotherUser
        userDTO.setActivated(updatedUser.isActivated());
        userDTO.setImageUrl(updatedUser.getImageUrl());
        userDTO.setLangKey(updatedUser.getLangKey());
        userDTO.setCreatedBy(updatedUser.getCreatedBy());
        userDTO.setCreatedDate(updatedUser.getCreatedDate());
        userDTO.setLastModifiedBy(updatedUser.getLastModifiedBy());
        userDTO.setLastModifiedDate(updatedUser.getLastModifiedDate());
        userDTO.setAuthorities(Set.of(AuthoritiesConstants.USER));

        restUserMockMvc
            .perform(put("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userDTO)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateUserExistingLogin() throws Exception {
        // Initialize the database
        userRepository.save(user);

        User anotherUser = new User();
        anotherUser.setLogin("jhipster");
        anotherUser.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        anotherUser.setActivated(true);
        anotherUser.setEmail("jhipster@example.com");
        anotherUser.setImageUrl("");
        anotherUser.setLangKey("en");
        userRepository.save(anotherUser);

        // Update the user
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();

        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setId(updatedUser.getId());
        userDTO.setLogin("jhipster"); // this login should already be used by anotherUser
        userDTO.setEmail(updatedUser.getEmail());
        userDTO.setActivated(updatedUser.isActivated());
        userDTO.setImageUrl(updatedUser.getImageUrl());
        userDTO.setLangKey(updatedUser.getLangKey());
        userDTO.setCreatedBy(updatedUser.getCreatedBy());
        userDTO.setCreatedDate(updatedUser.getCreatedDate());
        userDTO.setLastModifiedBy(updatedUser.getLastModifiedBy());
        userDTO.setLastModifiedDate(updatedUser.getLastModifiedDate());
        userDTO.setAuthorities(Set.of(AuthoritiesConstants.USER));

        restUserMockMvc
            .perform(put("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userDTO)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUser() throws Exception {
        // Initialize the database
        userRepository.save(user);
        int databaseSizeBeforeDelete = userRepository.findAll().size();

        // Delete the user
        restUserMockMvc
            .perform(delete("/api/admin/users/{login}", user.getLogin()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database is empty
        assertPersistedUsers(users -> assertThat(users).hasSize(databaseSizeBeforeDelete - 1));
    }

    @Test
    void testUserEquals() throws Exception {
        TestUtil.equalsVerifier(User.class);
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

    private String seededDocumentTypeId() {
        return documentTypeRepository.findAll().iterator().next().getId();
    }

    private void assertPersistedUsers(Consumer<List<User>> userAssertion) {
        userAssertion.accept(userRepository.findAll());
    }
}
