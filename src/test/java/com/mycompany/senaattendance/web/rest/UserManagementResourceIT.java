package com.mycompany.senaattendance.web.rest;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.Authority;
import com.mycompany.senaattendance.domain.DocumentType;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.repository.AuthorityRepository;
import com.mycompany.senaattendance.repository.DocumentTypeRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import java.util.ArrayList;
import java.util.List;
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
 * Integration tests for the {@link com.mycompany.senaattendance.web.rest.UserManagementResource#searchUsers(String, Boolean, Pageable)}
 * REST endpoint {@code GET /api/admin/users/search} (requires ROLE_ADMIN).
 */
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
@IntegrationTest
class UserManagementResourceIT {

    private static final String ENTITY_API_URL = "/api/admin/users/search";
    private static final String DEFAULT_PASSWORD = "Passw0rd!";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MockMvc restUserManagementMockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final List<User> createdUsers = new ArrayList<>();
    private final List<UserProfile> createdProfiles = new ArrayList<>();

    @AfterEach
    void cleanup() {
        // Profiles first (they @DBRef the users), then the users. Never deleteAll():
        // that would wipe the mongock-seeded catalog (users + profiles + document types).
        userProfileRepository.deleteAll(createdProfiles);
        userRepository.deleteAll(createdUsers);
        createdProfiles.clear();
        createdUsers.clear();
    }

    /**
     * Unique suffix so seeded data (admin, instructor, apprentice profiles; admin/user/etc. emails)
     * can never satisfy these searches and tests never collide with each other.
     */
    private static String uid() {
        return RandomStringUtils.insecure().nextAlphanumeric(8);
    }

    private DocumentType seededDocumentType() {
        return documentTypeRepository.findAll().iterator().next();
    }

    private User persistUser(String login, String email, boolean activated) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setActivated(activated);
        user.setLangKey("es");
        Authority userAuthority = authorityRepository.findById(AuthoritiesConstants.USER).orElseThrow();
        user.getAuthorities().add(userAuthority);
        User saved = userRepository.save(user);
        createdUsers.add(saved);
        return saved;
    }

    private UserProfile persistProfile(
        User user,
        String firstName,
        String firstLastName,
        String documentNumber,
        String phoneNumber,
        DocumentType documentType
    ) {
        UserProfile profile = new UserProfile()
            .firstName(firstName)
            .firstLastName(firstLastName)
            .documentNumber(documentNumber)
            .phoneNumber(phoneNumber)
            .user(user)
            .documentType(documentType);
        UserProfile saved = userProfileRepository.save(profile);
        createdProfiles.add(saved);
        return saved;
    }

    @Test
    void testSearchUsersByDocument() throws Exception {
        String documentNumber = "DOC" + uid();
        User user = persistUser("search-doc-" + uid(), "searchdoc-" + uid() + "@example.com", true);
        persistProfile(user, "Geralt", "Rivian", documentNumber, "3012345678", seededDocumentType());

        restUserManagementMockMvc
            .perform(get(ENTITY_API_URL).param("search", documentNumber).param("page", "0").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.[*].documentNumber").value(hasItem(documentNumber)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(user.getEmail())))
            .andExpect(jsonPath("$.[*].activated").value(hasItem(true)))
            .andExpect(jsonPath("$.[*].fullName").value(hasItem("Geralt Rivian")));
    }

    @Test
    void testSearchUsersByName() throws Exception {
        String nameTerm = "Zelda" + uid();
        String documentNumber = "DOC" + uid();
        User user = persistUser("search-name-" + uid(), "searchname-" + uid() + "@example.com", true);
        persistProfile(user, nameTerm, "Zoras", documentNumber, "3011111111", seededDocumentType());

        restUserManagementMockMvc
            .perform(get(ENTITY_API_URL).param("search", nameTerm).param("page", "0").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.[*].documentNumber").value(hasItem(documentNumber)))
            .andExpect(jsonPath("$.[*].fullName").value(hasItem(nameTerm + " Zoras")));
    }

    @Test
    void testSearchUsersByEmail() throws Exception {
        String emailLocal = "zelda-it-" + uid();
        String email = emailLocal + "@example.com";
        String documentNumber = "DOC" + uid();
        User user = persistUser("search-mail-" + uid(), email, true);
        persistProfile(user, "Navigator", "Hyrule", documentNumber, "3022222222", seededDocumentType());

        restUserManagementMockMvc
            .perform(get(ENTITY_API_URL).param("search", emailLocal).param("page", "0").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.[*].documentNumber").value(hasItem(documentNumber)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(email)));
    }

    @Test
    void testSearchUsersFiltersByInactiveStatus() throws Exception {
        String docInactive = "IDOC" + uid();
        String docActive = "ADOC" + uid();
        User inactiveUser = persistUser("search-inact-" + uid(), "qinact-" + uid() + "@example.com", false);
        User activeUser = persistUser("search-act-" + uid(), "qact-" + uid() + "@example.com", true);
        persistProfile(inactiveUser, "QuorumInactive", "Alpha", docInactive, "3033333333", seededDocumentType());
        persistProfile(activeUser, "QuorumActive", "Beta", docActive, "3044444444", seededDocumentType());

        restUserManagementMockMvc
            .perform(get(ENTITY_API_URL).param("search", "Quorum").param("status", "false").param("page", "0").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.[*].documentNumber").value(hasItem(docInactive)))
            .andExpect(jsonPath("$.[*].documentNumber").value(not(hasItem(docActive))));
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.USER)
    void testSearchUsersRequiresAdmin() throws Exception {
        restUserManagementMockMvc
            .perform(get(ENTITY_API_URL).param("search", "x").param("page", "0").param("size", "10"))
            .andExpect(status().isForbidden());
    }
}
