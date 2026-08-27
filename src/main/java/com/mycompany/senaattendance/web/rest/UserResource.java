package com.mycompany.senaattendance.web.rest;

import com.mycompany.senaattendance.config.Constants;
import com.mycompany.senaattendance.domain.Notificacion;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.enumeration.NotificacionEstado;
import com.mycompany.senaattendance.domain.enumeration.NotificacionTipo;
import com.mycompany.senaattendance.repository.NotificacionRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.service.MailService;
import com.mycompany.senaattendance.service.UserService;
import com.mycompany.senaattendance.service.dto.AdminUserDTO;
import com.mycompany.senaattendance.web.rest.errors.*;
import com.mycompany.senaattendance.web.rest.vm.AdminCreateUserVM;
import com.mycompany.senaattendance.web.rest.vm.AdminUpdateUserVM;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing users.
 * <p>
 * This class accesses the {@link com.mycompany.senaattendance.domain.User} entity, and needs to fetch its collection of authorities.
 * <p>
 * For a normal use-case, it would be better to have an eager relationship between User and Authority,
 * and send everything to the client side: there would be no View Model and DTO, a lot less code, and an outer-join
 * which would be good for performance.
 * <p>
 * We use a View Model and a DTO for 3 reasons:
 * <ul>
 * <li>We want to keep a lazy association between the user and the authorities, because people will
 * quite often do relationships with the user, and we don't want them to get the authorities all
 * the time for nothing (for performance reasons). This is the #1 goal: we should not impact our users'
 * application because of this use-case.</li>
 * <li> Not having an outer join causes n+1 requests to the database. This is not a real issue as
 * we have by default a second-level cache. This means on the first HTTP call we do the n+1 requests,
 * but then all authorities come from the cache, so in fact it's much better than doing an outer join
 * (which will get lots of data from the database, for each HTTP call).</li>
 * <li> As this manages users, for security reasons, we'd rather have a DTO layer.</li>
 * </ul>
 * <p>
 * Another option would be to have a specific JPA entity graph to handle this case.
 */
@RestController
@RequestMapping("/api/admin")
public class UserResource {

    private static final List<String> ALLOWED_ORDERED_PROPERTIES = List.of(
        "id",
        "login",
        "email",
        "activated",
        "langKey",
        "createdBy",
        "createdDate",
        "lastModifiedBy",
        "lastModifiedDate"
    );

    private static final Logger LOG = LoggerFactory.getLogger(UserResource.class);

    @Value("${jhipster.clientApp.name:senaAttendance}")
    private String applicationName;

    private final UserService userService;

    private final MailService mailService;

    private final NotificacionRepository notificacionRepository;

    public UserResource(UserService userService, MailService mailService, NotificacionRepository notificacionRepository) {
        this.userService = userService;
        this.mailService = mailService;
        this.notificacionRepository = notificacionRepository;
    }

    /**
     * {@code POST  /admin/users}  : Creates a new user.
     * <p>
     * Creates a new user if the login and email are not already used, and sends a
     * mail with an activation link.
     * The user needs to be activated on creation.
     *
     * @param userVM the user to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new user, or with status {@code 400 (Bad Request)} if the login or email is already in use.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     * @throws BadRequestAlertException {@code 400 (Bad Request)} if the login or email is already in use.
     */
    @PostMapping("/users")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<User> createUser(@Valid @RequestBody AdminCreateUserVM userVM) throws URISyntaxException {
        LOG.debug("REST request to save User : {}", userVM);

        if (userVM.getId() != null) {
            throw new BadRequestAlertException("A new user cannot already have an ID", "userManagement", "idexists");
        } else if (isPasswordLengthInvalid(userVM.getPassword())) {
            throw new InvalidPasswordException();
        } else {
            try {
                User newUser = userService.createUser(userVM);
                try {
                    mailService.sendCreationEmailSync(newUser);
                } catch (MessagingException | RuntimeException e) {
                    LOG.warn("Could not send creation email to user '{}', saving pending notification", newUser.getLogin(), e);
                    saveNotificacion(newUser, e.getMessage());
                }
                return ResponseEntity.created(new URI("/api/admin/users/" + newUser.getLogin()))
                    .headers(HeaderUtil.createAlert(applicationName, "userManagement.created", newUser.getLogin()))
                    .body(newUser);
            } catch (DocumentTypeNotFoundException e) {
                throw new BadRequestAlertException(e.getMessage(), "userProfile", "documentTypeNotFound");
            }
        }
    }

    /**
     * {@code PUT /admin/users} : Updates an existing User.
     *
     * @param login the user login (ignored; identity is resolved from {@code userDTO.getId()}).
     * @param userDTO the user to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated user.
     * @throws BadRequestAlertException {@code 400 (Bad Request)} if the id is missing.
     */
    @PutMapping({ "/users", "/users/{login}" })
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AdminUserDTO> updateUser(
        @PathVariable(name = "login", required = false) String login,
        @Valid @RequestBody AdminUpdateUserVM userDTO
    ) {
        LOG.debug("REST request to update User : {}", userDTO);
        if (userDTO.getId() == null) {
            throw new BadRequestAlertException("A update user must have an ID", "userManagement", "idmissing");
        }
        Optional<AdminUserDTO> updatedUser;
        try {
            updatedUser = userService.updateUser(userDTO);
        } catch (DocumentTypeNotFoundException e) {
            throw new BadRequestAlertException(e.getMessage(), "userProfile", "documentTypeNotFound");
        }
        return ResponseUtil.wrapOrNotFound(
            updatedUser,
            HeaderUtil.createAlert(applicationName, "userManagement.updated", updatedUser.map(AdminUserDTO::getLogin).orElse(""))
        );
    }

    /**
     * Saves a pending {@link Notificacion} for the user so the admin can resend the
     * credentials email manually. Does NOT throw.
     */
    private void saveNotificacion(User user, String mensaje) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUser(user);
        notificacion.setTipo(NotificacionTipo.CREDENTIALS);
        notificacion.setEstado(NotificacionEstado.PENDIENTE);
        notificacion.setMensaje(mensaje);
        notificacionRepository.save(notificacion);
    }

    /**
     * {@code GET /admin/users} : get all users with all the details - calling this are only allowed for the administrators.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all users.
     */
    @GetMapping("/users")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<List<AdminUserDTO>> getAllUsers(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get all User for an admin");
        if (!onlyContainsAllowedProperties(pageable)) {
            return ResponseEntity.badRequest().build();
        }

        final Page<AdminUserDTO> page = userService.getAllManagedUsers(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    private boolean onlyContainsAllowedProperties(Pageable pageable) {
        return pageable.getSort().stream().map(Sort.Order::getProperty).allMatch(ALLOWED_ORDERED_PROPERTIES::contains);
    }

    /**
     * {@code GET /admin/users/:login} : get the "login" user.
     *
     * @param login the login of the user to find.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the "login" user, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/users/{login}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AdminUserDTO> getUser(@PathVariable("login") @Pattern(regexp = Constants.LOGIN_REGEX) String login) {
        LOG.debug("REST request to get User : {}", login);
        return ResponseUtil.wrapOrNotFound(userService.getUserWithAuthoritiesByLogin(login).map(AdminUserDTO::new));
    }

    /**
     * {@code DELETE /admin/users/:login} : delete the "login" User.
     *
     * @param login the login of the user to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/users/{login}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteUser(@PathVariable("login") @Pattern(regexp = Constants.LOGIN_REGEX) String login) {
        LOG.debug("REST request to delete User: {}", login);
        userService.deleteUser(login);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createAlert(applicationName, "userManagement.deleted", login))
            .build();
    }

    private static boolean isPasswordLengthInvalid(String password) {
        return (
            StringUtils.isEmpty(password) ||
            password.length() < AdminCreateUserVM.PASSWORD_MIN_LENGTH ||
            password.length() > AdminCreateUserVM.PASSWORD_MAX_LENGTH
        );
    }
}
