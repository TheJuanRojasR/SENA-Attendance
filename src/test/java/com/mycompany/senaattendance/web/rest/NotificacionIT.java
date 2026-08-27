package com.mycompany.senaattendance.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.Notificacion;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.enumeration.NotificacionEstado;
import com.mycompany.senaattendance.domain.enumeration.NotificacionTipo;
import com.mycompany.senaattendance.repository.DocumentTypeRepository;
import com.mycompany.senaattendance.repository.NotificacionRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.MailService;
import com.mycompany.senaattendance.web.rest.vm.AdminCreateUserVM;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the E4 resilience path (creation email failure) on {@link UserResource#createUser}.
 */
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
@IntegrationTest
class NotificacionIT {

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MockMvc restUserMockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @MockitoBean
    private MailService mailService;

    @AfterEach
    void cleanupAndCheck() {
        notificacionRepository.deleteAll();
        userProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createUserWithFailedMailSendsNotification() throws Exception {
        doThrow(new MailSendException("SMTP connection refused")).when(mailService).sendCreationEmailSync(any(User.class));

        String documentNumber = "1000000001";
        String expectedLogin = documentNumber.toLowerCase().trim();
        AdminCreateUserVM userVM = new AdminCreateUserVM();
        userVM.setLogin(expectedLogin);
        userVM.setEmail("failed.mail@example.com");
        userVM.setPassword("Passw0rd!");
        userVM.setFirstName("John");
        userVM.setMiddleName("M");
        userVM.setFirstLastName("Doe");
        userVM.setSecondLastName("S");
        userVM.setDocumentNumber(documentNumber);
        userVM.setPhoneNumber("3001234567");
        userVM.setDocumentTypeId(documentTypeRepository.findAll().iterator().next().getId());
        userVM.setRole(AuthoritiesConstants.INSTRUCTOR);
        userVM.setActivated(true);
        userVM.setLangKey("en");

        restUserMockMvc
            .perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(userVM)))
            .andExpect(status().isCreated());

        // User + profile were still saved despite the mail failure
        User created = userRepository.findOneByLogin(expectedLogin).orElseThrow();
        assertThat(created.getEmail()).isEqualTo("failed.mail@example.com");
        assertThat(userProfileRepository.findByDocumentNumber(documentNumber)).isPresent();

        // A pending CREDENTIALS notification was persisted for the user
        List<Notificacion> notificaciones = notificacionRepository.findByUser(created, Pageable.unpaged()).getContent();
        assertThat(notificaciones).hasSize(1);
        assertThat(notificaciones.get(0).getTipo()).isEqualTo(NotificacionTipo.CREDENTIALS);
        assertThat(notificaciones.get(0).getEstado()).isEqualTo(NotificacionEstado.PENDIENTE);
    }
}
