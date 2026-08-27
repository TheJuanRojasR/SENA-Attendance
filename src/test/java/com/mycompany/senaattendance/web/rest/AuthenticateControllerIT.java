package com.mycompany.senaattendance.web.rest;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.DocumentType;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.repository.DocumentTypeRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.web.rest.vm.LoginVM;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the {@link AuthenticateController} REST controller.
 *
 * <p>The controller authenticates by {@code documentTypeId + documentNumber} (looked up on a
 * {@link UserProfile}) and then checks the linked {@link User} password and activation state.
 */
@AutoConfigureMockMvc
@IntegrationTest
class AuthenticateControllerIT {

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void cleanup() {
        userProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testAuthorize() throws Exception {
        String suffix = uniqueSuffix();
        String login = "auth-ok-" + suffix;
        String documentNumber = "10" + suffix;

        User user = persistedUser(login, true);
        DocumentType documentType = seededDocumentType();
        userProfileRepository.save(persistedProfile(user, documentType, documentNumber));

        LoginVM loginVM = new LoginVM();
        loginVM.setDocumentTypeId(documentType.getId());
        loginVM.setDocumentNumber(documentNumber);
        loginVM.setPassword(VALID_PASSWORD);

        mockMvc
            .perform(post("/api/authenticate").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(loginVM)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id_token").isString())
            .andExpect(jsonPath("$.id_token").isNotEmpty())
            .andExpect(header().string("Authorization", not(nullValue())))
            .andExpect(header().string("Authorization", not(is(emptyString()))));
    }

    @Test
    void testAuthorizeWithRememberMe() throws Exception {
        String suffix = uniqueSuffix();
        String login = "auth-remember-" + suffix;
        String documentNumber = "10" + suffix;

        User user = persistedUser(login, true);
        DocumentType documentType = seededDocumentType();
        userProfileRepository.save(persistedProfile(user, documentType, documentNumber));

        LoginVM loginVM = new LoginVM();
        loginVM.setDocumentTypeId(documentType.getId());
        loginVM.setDocumentNumber(documentNumber);
        loginVM.setPassword(VALID_PASSWORD);
        loginVM.setRememberMe(true);

        mockMvc
            .perform(post("/api/authenticate").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(loginVM)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id_token").isString())
            .andExpect(jsonPath("$.id_token").isNotEmpty())
            .andExpect(header().string("Authorization", not(nullValue())))
            .andExpect(header().string("Authorization", not(is(emptyString()))));
    }

    @Test
    void testAuthorizeFails_wrongPassword() throws Exception {
        String suffix = uniqueSuffix();
        String login = "auth-pass-" + suffix;
        String documentNumber = "10" + suffix;

        User user = persistedUser(login, true);
        DocumentType documentType = seededDocumentType();
        userProfileRepository.save(persistedProfile(user, documentType, documentNumber));

        LoginVM loginVM = new LoginVM();
        loginVM.setDocumentTypeId(documentType.getId());
        loginVM.setDocumentNumber(documentNumber);
        loginVM.setPassword("WrongPass!");

        mockMvc
            .perform(post("/api/authenticate").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(loginVM)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.id_token").doesNotExist())
            .andExpect(header().doesNotExist("Authorization"));
    }

    @Test
    void testAuthorizeFails_unknownDocumentNumber() throws Exception {
        LoginVM loginVM = new LoginVM();
        loginVM.setDocumentTypeId(seededDocumentTypeId());
        loginVM.setDocumentNumber("999999999999");
        loginVM.setPassword(VALID_PASSWORD);

        mockMvc
            .perform(post("/api/authenticate").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(loginVM)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.id_token").doesNotExist())
            .andExpect(header().doesNotExist("Authorization"));
    }

    @Test
    void testAuthorizeFails_inactiveUser() throws Exception {
        String suffix = uniqueSuffix();
        String login = "auth-inactive-" + suffix;
        String documentNumber = "10" + suffix;

        User user = persistedUser(login, false);
        DocumentType documentType = seededDocumentType();
        userProfileRepository.save(persistedProfile(user, documentType, documentNumber));

        LoginVM loginVM = new LoginVM();
        loginVM.setDocumentTypeId(documentType.getId());
        loginVM.setDocumentNumber(documentNumber);
        loginVM.setPassword(VALID_PASSWORD);

        mockMvc
            .perform(post("/api/authenticate").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(loginVM)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.id_token").doesNotExist())
            .andExpect(header().doesNotExist("Authorization"));
    }

    @Test
    void testAuthorizeFails_documentTypeNotFound() throws Exception {
        LoginVM loginVM = new LoginVM();
        loginVM.setDocumentTypeId("000000000000000000000000");
        loginVM.setDocumentNumber("1012345678");
        loginVM.setPassword(VALID_PASSWORD);

        mockMvc
            .perform(post("/api/authenticate").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(loginVM)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.id_token").doesNotExist())
            .andExpect(header().doesNotExist("Authorization"));
    }

    @Test
    void testAuthorizeFails_missingRequiredFields() throws Exception {
        LoginVM loginVM = new LoginVM();
        // documentTypeId and documentNumber are left null on purpose -> @Valid must reject.
        loginVM.setPassword(VALID_PASSWORD);

        mockMvc
            .perform(post("/api/authenticate").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(loginVM)))
            .andExpect(status().isBadRequest());
    }

    private static String uniqueSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private DocumentType seededDocumentType() {
        return documentTypeRepository.findAll().iterator().next();
    }

    private String seededDocumentTypeId() {
        return seededDocumentType().getId();
    }

    private User persistedUser(String login, boolean activated) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(login + "@example.com");
        user.setPassword(passwordEncoder.encode(VALID_PASSWORD));
        user.setActivated(activated);
        user.setLangKey("es");
        return userRepository.save(user);
    }

    private UserProfile persistedProfile(User user, DocumentType documentType, String documentNumber) {
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
}
