package com.mycompany.senaattendance.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.senaattendance.IntegrationTest;
import com.mycompany.senaattendance.domain.Apprentice;
import com.mycompany.senaattendance.domain.Attendance;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.GlobalConfiguration;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.Justification;
import com.mycompany.senaattendance.domain.JustificationDetails;
import com.mycompany.senaattendance.domain.JustificationType;
import com.mycompany.senaattendance.domain.Modality;
import com.mycompany.senaattendance.domain.Notificacion;
import com.mycompany.senaattendance.domain.Program;
import com.mycompany.senaattendance.domain.TimeSlot;
import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.NotificacionEstado;
import com.mycompany.senaattendance.domain.enumeration.NotificacionTipo;
import com.mycompany.senaattendance.domain.enumeration.StateAcademic;
import com.mycompany.senaattendance.domain.enumeration.StateAttendance;
import com.mycompany.senaattendance.domain.enumeration.StateTrimester;
import com.mycompany.senaattendance.repository.ApprenticeRepository;
import com.mycompany.senaattendance.repository.AttendanceRepository;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.DocumentTypeRepository;
import com.mycompany.senaattendance.repository.GlobalConfigurationRepository;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.repository.JustificationDetailsRepository;
import com.mycompany.senaattendance.repository.JustificationRepository;
import com.mycompany.senaattendance.repository.JustificationTypeRepository;
import com.mycompany.senaattendance.repository.ModalityRepository;
import com.mycompany.senaattendance.repository.NotificacionRepository;
import com.mycompany.senaattendance.repository.ProgramRepository;
import com.mycompany.senaattendance.repository.TimeSlotRepository;
import com.mycompany.senaattendance.repository.TrimesterRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.MailService;
import com.mycompany.senaattendance.service.dto.JustificationDTO;
import com.mycompany.senaattendance.web.rest.vm.AdminCreateUserVM;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Integration tests for the persistence of the notifications (UC018): the credential fallback
 * (UC006, E4) and the real delivery of the justification state changes, one notification per
 * apprentice and instructor recipient with the type and the origin reference.
 */
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
@IntegrationTest
class NotificacionIT {

    private static final String APPRENTICE_LOGIN = "notifications_apprentice";
    private static final String INSTRUCTOR_LOGIN = "notifications_instructor";

    private static final String RESEND_CREDENTIALS_URL = "/api/admin/users/resend-credentials";

    private static final String PENDING_MESSAGE = "Tu justificación quedó registrada y está pendiente de revisión.";
    private static final String CANCELLED_MESSAGE = "Tu justificación fue cancelada.";
    private static final String REJECTED_MESSAGE = "Tu justificación fue rechazada.";
    private static final String INSTRUCTOR_MESSAGE = "Recibiste una nueva justificación pendiente de revisión en una de tus materias.";

    private static final byte[] EVIDENCE = new byte[] { 1, 2, 3 };

    @Autowired
    private ObjectMapper om;

    @Autowired
    private Clock clock;

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

    @Autowired
    private JustificationRepository justificationRepository;

    @Autowired
    private JustificationDetailsRepository justificationDetailsRepository;

    @Autowired
    private JustificationTypeRepository justificationTypeRepository;

    @Autowired
    private ClassSectionRepository classSectionRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private ModalityRepository modalityRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private TrimesterRepository trimesterRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private ApprenticeRepository apprenticeRepository;

    @Autowired
    private GlobalConfigurationRepository globalConfigurationRepository;

    @MockitoBean
    private MailService mailService;

    private UserProfile apprentice;

    private UserProfile instructor;

    private ClassSection classSection;

    private JustificationType justificationType;

    @AfterEach
    void cleanupAndCheck() {
        notificacionRepository.deleteAll();
        justificationDetailsRepository.deleteAll();
        attendanceRepository.deleteAll();
        apprenticeRepository.deleteAll();
        justificationRepository.deleteAll();
        justificationTypeRepository.deleteAll();
        classSectionRepository.deleteAll();
        trimesterRepository.deleteAll();
        gradeRepository.deleteAll();
        programRepository.deleteAll();
        modalityRepository.deleteAll();
        timeSlotRepository.deleteAll();
        globalConfigurationRepository.deleteAll();
        userProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createUserWithFailedMailSendsNotification() throws Exception {
        doThrow(new MailSendException("SMTP connection refused")).when(mailService).sendCreationEmailSync(any(User.class));

        String documentNumber = "1000000001";
        String expectedLogin = derivedLogin(documentNumber);
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
        // The creation email links to the reset page, so the reset key must never be null
        assertThat(created.getResetKey()).isNotBlank();
        assertThat(userProfileRepository.findByDocumentNumber(documentNumber)).isPresent();

        // The user handed to the mocked mail carries the same reset key the template renders
        ArgumentCaptor<User> creationMailUser = ArgumentCaptor.forClass(User.class);
        verify(mailService).sendCreationEmailSync(creationMailUser.capture());
        assertThat(creationMailUser.getValue().getResetKey()).isNotBlank();

        // A pending CREDENTIALS notification was persisted for the user
        List<Notificacion> notificaciones = notificacionRepository.findByUser(created, Pageable.unpaged()).getContent();
        assertThat(notificaciones).hasSize(1);
        assertThat(notificaciones.get(0).getTipo()).isEqualTo(NotificacionTipo.CREDENTIALS);
        assertThat(notificaciones.get(0).getEstado()).isEqualTo(NotificacionEstado.PENDIENTE);
    }

    @Test
    void resendCredentialsSendsResetLinkAndMarksTheNotificationAsSent() throws Exception {
        UserProfile profile = persistProfile("notifications_resend_ok");
        Notificacion pending = persistCredentialNotification(profile.getUser(), NotificacionEstado.PENDIENTE);
        when(mailService.sendPasswordResetMailSync(any(User.class))).thenReturn(true);

        restUserMockMvc
            .perform(resendCredentials(profile.getDocumentNumber()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.login").value("notifications_resend_ok"))
            .andExpect(jsonPath("$.mustChangePassword").value(true));

        User resent = userRepository.findById(profile.getUser().getId()).orElseThrow();
        assertThat(resent.getResetKey()).isNotBlank();
        assertThat(resent.isMustChangePassword()).isTrue();
        assertThat(notificacionRepository.findById(pending.getId()).orElseThrow().getEstado()).isEqualTo(NotificacionEstado.ENVIADA);

        ArgumentCaptor<User> mailUser = ArgumentCaptor.forClass(User.class);
        verify(mailService).sendPasswordResetMailSync(mailUser.capture());
        assertThat(mailUser.getValue().getResetKey()).isEqualTo(resent.getResetKey());
    }

    @Test
    void resendCredentialsKeepsTheNotificationRetryableWhenTheMailFails() throws Exception {
        UserProfile profile = persistProfile("notifications_resend_fail");
        Notificacion pending = persistCredentialNotification(profile.getUser(), NotificacionEstado.PENDIENTE);
        when(mailService.sendPasswordResetMailSync(any(User.class))).thenThrow(new MailSendException("SMTP connection refused"));

        restUserMockMvc.perform(resendCredentials(profile.getDocumentNumber())).andExpect(status().isOk());

        assertThat(notificacionRepository.findById(pending.getId()).orElseThrow().getEstado()).isEqualTo(NotificacionEstado.REINTENTAR);
    }

    @Test
    void resendCredentialsWithoutOpenNotificationStillSendsTheMail() throws Exception {
        UserProfile profile = persistProfile("notifications_resend_none");
        when(mailService.sendPasswordResetMailSync(any(User.class))).thenReturn(true);

        restUserMockMvc.perform(resendCredentials(profile.getDocumentNumber())).andExpect(status().isOk());

        assertThat(notificationsOf(profile)).isEmpty();
        verify(mailService).sendPasswordResetMailSync(any(User.class));
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.APPRENTICE)
    void resendCredentialsIsForbiddenForNonAdmin() throws Exception {
        UserProfile profile = persistProfile("notifications_resend_forbidden");

        restUserMockMvc.perform(resendCredentials(profile.getDocumentNumber())).andExpect(status().isForbidden());

        verifyNoInteractions(mailService);
        assertThat(userRepository.findById(profile.getUser().getId()).orElseThrow().getResetKey()).isNull();
    }

    @Test
    void resendCredentialsWithUnknownDocumentReturnsDocumentNumberNotFound() throws Exception {
        restUserMockMvc
            .perform(resendCredentials("UNKNOWN0001"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.documentNumberNotFound"));

        verifyNoInteractions(mailService);
    }

    @Test
    void createJustificationPersistsNotificationForTheApprenticeAndTheInstructor() throws Exception {
        persistJustificationFixture();

        JustificationDTO created = createJustificationViaApi();

        Notificacion apprenticeNotification = singleNotificationOf(apprentice);
        assertThat(apprenticeNotification.getTipo()).isEqualTo(NotificacionTipo.JUSTIFICACION);
        assertThat(apprenticeNotification.getEstado()).isEqualTo(NotificacionEstado.PENDIENTE);
        assertThat(apprenticeNotification.getRead()).isFalse();
        assertThat(apprenticeNotification.getReferenceType()).isEqualTo("JUSTIFICATION");
        assertThat(apprenticeNotification.getReferenceId()).isEqualTo(created.getId());
        assertThat(apprenticeNotification.getMensaje()).isEqualTo(PENDING_MESSAGE);

        Notificacion instructorNotification = singleNotificationOf(instructor);
        assertThat(instructorNotification.getTipo()).isEqualTo(NotificacionTipo.JUSTIFICACION);
        assertThat(instructorNotification.getEstado()).isEqualTo(NotificacionEstado.PENDIENTE);
        assertThat(instructorNotification.getRead()).isFalse();
        assertThat(instructorNotification.getReferenceType()).isEqualTo("JUSTIFICATION");
        assertThat(instructorNotification.getReferenceId()).isEqualTo(created.getId());
        assertThat(instructorNotification.getMensaje()).isEqualTo(INSTRUCTOR_MESSAGE);
    }

    @Test
    void cancelJustificationPersistsCancelledNotificationForTheApprentice() throws Exception {
        persistJustificationFixture();
        JustificationDTO created = createJustificationViaApi();

        restUserMockMvc
            .perform(
                patch("/api/justifications/cancelled")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(Map.of("id", created.getId())))
            )
            .andExpect(status().isOk());

        List<Notificacion> apprenticeNotifications = notificationsOf(apprentice);
        assertThat(apprenticeNotifications).hasSize(2);
        assertThat(apprenticeNotifications)
            .extracting(Notificacion::getMensaje)
            .containsExactlyInAnyOrder(PENDING_MESSAGE, CANCELLED_MESSAGE);
        assertThat(apprenticeNotifications).allSatisfy(notification -> {
            assertThat(notification.getTipo()).isEqualTo(NotificacionTipo.JUSTIFICACION);
            assertThat(notification.getEstado()).isEqualTo(NotificacionEstado.PENDIENTE);
            assertThat(notification.getRead()).isFalse();
            assertThat(notification.getReferenceType()).isEqualTo("JUSTIFICATION");
            assertThat(notification.getReferenceId()).isEqualTo(created.getId());
        });

        // Cancelling only notifies the apprentice
        assertThat(notificationsOf(instructor)).hasSize(1);
    }

    @Test
    void decideJustificationPersistsDecisionNotificationForTheApprentice() throws Exception {
        persistJustificationFixture();
        JustificationDTO created = createJustificationViaApi();
        JustificationDetails part = partOf(created.getId());

        restUserMockMvc
            .perform(
                patch("/api/justification-details/{id}/decision", part.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(Map.of("stateJustification", "RECHAZADA", "rejectionReason", "Soporte no legible")))
            )
            .andExpect(status().isOk());

        List<Notificacion> apprenticeNotifications = notificationsOf(apprentice);
        assertThat(apprenticeNotifications).hasSize(2);
        assertThat(apprenticeNotifications)
            .extracting(Notificacion::getMensaje)
            .containsExactlyInAnyOrder(PENDING_MESSAGE, REJECTED_MESSAGE);
        assertThat(apprenticeNotifications).allSatisfy(notification -> {
            assertThat(notification.getTipo()).isEqualTo(NotificacionTipo.JUSTIFICACION);
            assertThat(notification.getEstado()).isEqualTo(NotificacionEstado.PENDIENTE);
            assertThat(notification.getRead()).isFalse();
            assertThat(notification.getReferenceType()).isEqualTo("JUSTIFICATION");
            assertThat(notification.getReferenceId()).isEqualTo(created.getId());
        });
    }

    /**
     * Derives the login that {@link com.mycompany.senaattendance.service.UserService#createUser}
     * now produces from the seeded {@link com.mycompany.senaattendance.domain.DocumentType}
     * initials: {@code <initials>_<documentNumber>}.
     */
    private String derivedLogin(String documentNumber) {
        com.mycompany.senaattendance.domain.DocumentType dt = documentTypeRepository.findAll().iterator().next();
        String typeCode = dt.getInitials() != null ? dt.getInitials() : "";
        return (typeCode + "_" + documentNumber).toLowerCase().trim();
    }

    /**
     * Persists the graph the justification creation rules run on: the apprentice and the
     * instructor with resolvable logins, one materia assigned to the instructor, their ficha with
     * an active trimester, the pending failure the justification covers, the type and the global
     * configuration.
     */
    private void persistJustificationFixture() {
        apprentice = persistProfile(APPRENTICE_LOGIN);
        instructor = persistProfile(INSTRUCTOR_LOGIN);
        LocalDate today = LocalDate.now(clock);

        globalConfigurationRepository.save(
            new GlobalConfiguration()
                .id(GlobalConfiguration.GLOBAL_CONFIGURATION_ID)
                .studentJustificationDays(3)
                .instructorResponseDays(2)
                .consecutiveAbsenceAlertThreshold(3)
                .accumulatedAbsenceAlertThreshold(5)
        );
        trimesterRepository.save(
            new Trimester()
                .name("Notification trimester")
                .startDate(today.minusDays(60))
                .endDate(today.plusDays(60))
                .status(StateTrimester.ACTIVO)
        );
        Grade grade = persistGrade("NOT-001", today.minusDays(30), today.plusDays(30));
        classSection = classSectionRepository.save(
            new ClassSection().subjectName("Notification subject").isActive(true).grade(grade).instructor(instructor)
        );
        apprenticeRepository.save(new Apprentice().student(apprentice).grade(grade).stateAcademic(StateAcademic.MATRICULADO));
        justificationType = justificationTypeRepository.save(JustificationTypeResourceIT.createEntity());
        attendanceRepository.save(
            new Attendance().date(today.minusDays(1)).stateAttendance(StateAttendance.FALLA).classSection(classSection).student(apprentice)
        );
    }

    /**
     * Creates a justification over the fixture through the API and returns its DTO.
     */
    private JustificationDTO createJustificationViaApi() throws Exception {
        LocalDate failure = LocalDate.now(clock).minusDays(1);
        Map<String, Object> payload = new HashMap<>();
        payload.put("description", "Notification justification");
        payload.put("startDate", failure.toString());
        payload.put("endDate", failure.toString());
        payload.put("evidenceContentType", "application/pdf");
        payload.put("evidence", Base64.getEncoder().encodeToString(EVIDENCE));
        payload.put("justificationType", Map.of("id", justificationType.getId()));
        payload.put("student", Map.of("id", apprentice.getId()));
        payload.put("detailses", List.of(Map.of("classSection", Map.of("id", classSection.getId()))));

        return om.readValue(
            restUserMockMvc
                .perform(post("/api/justifications").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(payload)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            JustificationDTO.class
        );
    }

    private JustificationDetails partOf(String justificationId) {
        return justificationDetailsRepository
            .findAll()
            .stream()
            .filter(part -> part.getJustification() != null && justificationId.equals(part.getJustification().getId()))
            .findFirst()
            .orElseThrow();
    }

    private List<Notificacion> notificationsOf(UserProfile profile) {
        return notificacionRepository.findByUser(profile.getUser(), Pageable.unpaged()).getContent();
    }

    private Notificacion singleNotificationOf(UserProfile profile) {
        List<Notificacion> notifications = notificationsOf(profile);
        assertThat(notifications).hasSize(1);
        return notifications.get(0);
    }

    /**
     * Builds the PATCH request the Administrator uses to resend the access credentials through a
     * fresh reset link (UC006, E7).
     */
    private MockHttpServletRequestBuilder resendCredentials(String documentNumber) throws Exception {
        return patch(RESEND_CREDENTIALS_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsBytes(Map.of("documentNumber", documentNumber)));
    }

    /**
     * Persists the open CREDENTIALS notification that the resend must close (UC006, E7).
     */
    private Notificacion persistCredentialNotification(User user, NotificacionEstado estado) {
        return notificacionRepository.save(
            new Notificacion().user(user).tipo(NotificacionTipo.CREDENTIALS).estado(estado).read(false).mensaje("SMTP connection refused")
        );
    }

    /**
     * Persists a profile with a resolvable login and a random document number, so the service can
     * resolve it from the security context and the unique document index does not clash.
     */
    private UserProfile persistProfile(String login) {
        User user = UserResourceIT.createEntity();
        user.setLogin(login);
        user.setEmail(login + "@example.com");
        user.setActivated(true);
        user = userRepository.save(user);

        UserProfile profile = UserProfileResourceIT.createEntity();
        profile.setDocumentNumber("N" + UUID.randomUUID().toString().replace("-", "").substring(0, 13));
        profile.setUser(user);
        return userProfileRepository.save(profile);
    }

    private Grade persistGrade(String code, LocalDate startDate, LocalDate endDate) {
        Program program = programRepository.save(ProgramResourceIT.createEntity());
        Modality modality = modalityRepository.save(ModalityResourceIT.createEntity());
        TimeSlot timeSlot = timeSlotRepository.save(TimeSlotResourceIT.createEntity());

        Grade grade = GradeResourceIT.createEntity();
        grade.setCode(code);
        grade.setStartDate(startDate);
        grade.setEndDate(endDate);
        grade.setProgram(program);
        grade.setModality(modality);
        grade.setTimeSlot(timeSlot);
        return gradeRepository.save(grade);
    }
}
