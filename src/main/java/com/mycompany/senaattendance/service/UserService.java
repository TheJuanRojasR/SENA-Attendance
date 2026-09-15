package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.config.Constants;
import com.mycompany.senaattendance.domain.Authority;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.DocumentType;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.StateGrade;
import com.mycompany.senaattendance.repository.AuthorityRepository;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.DocumentTypeRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.dto.AdminUserDTO;
import com.mycompany.senaattendance.service.dto.UserDTO;
import com.mycompany.senaattendance.service.dto.UserProfileDTO;
import com.mycompany.senaattendance.service.mapper.UserProfileMapper;
import com.mycompany.senaattendance.web.rest.errors.BadRequestAlertException;
import com.mycompany.senaattendance.web.rest.errors.DocumentNumberAlreadyUsedException;
import com.mycompany.senaattendance.web.rest.errors.DocumentTypeNotFoundException;
import com.mycompany.senaattendance.web.rest.errors.LoginAlreadyUsedException;
import com.mycompany.senaattendance.web.rest.vm.AccountUpdateVM;
import com.mycompany.senaattendance.web.rest.vm.AdminCreateUserVM;
import com.mycompany.senaattendance.web.rest.vm.AdminUpdateUserVM;
import com.mycompany.senaattendance.web.rest.vm.ManagedUserVM;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.security.RandomUtil;

/**
 * Service class for managing users.
 */
@Service
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,20}$");

    /**
     * Login of the protected super admin. This account can NEVER be deactivated (rule E6).
     */
    private static final String PROTECTED_ADMIN_LOGIN = "admin";

    /**
     * How long a password-reset link stays valid after it is requested.
     */
    private static final long RESET_KEY_VALIDITY_MINUTES = 30;

    /**
     * Roles that can be assigned to an account: Administrator, Instructor and Apprentice.
     */
    private static final Set<String> ASSIGNABLE_ROLES = Set.of(
        AuthoritiesConstants.ADMIN,
        AuthoritiesConstants.INSTRUCTOR,
        AuthoritiesConstants.APPRENTICE
    );

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthorityRepository authorityRepository;

    private final UserProfileRepository userProfileRepository;

    private final DocumentTypeRepository documentTypeRepository;

    private final ClassSectionRepository classSectionRepository;

    private final UserProfileMapper userProfileMapper;

    public UserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthorityRepository authorityRepository,
        UserProfileRepository userProfileRepository,
        DocumentTypeRepository documentTypeRepository,
        ClassSectionRepository classSectionRepository,
        UserProfileMapper userProfileMapper
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorityRepository = authorityRepository;
        this.userProfileRepository = userProfileRepository;
        this.documentTypeRepository = documentTypeRepository;
        this.classSectionRepository = classSectionRepository;
        this.userProfileMapper = userProfileMapper;
    }

    public Optional<User> activateRegistration(String key) {
        LOG.debug("Activating user for activation key {}", key);
        return userRepository.findOneByActivationKey(key).map(user -> {
            // activate given user for the registration key.
            user.setActivated(true);
            user.setActivationKey(null);
            userRepository.save(user);
            LOG.debug("Activated user: {}", user);
            return user;
        });
    }

    public User completePasswordReset(String newPassword, String key) {
        LOG.debug("Reset user password for reset key {}", key);
        // A missing key must resolve as an invalid link: findOneByResetKey(null) would otherwise
        // match users that never requested a reset and fail with IncorrectResultSizeDataAccessException.
        if (key == null || key.isBlank()) {
            throw new BadRequestAlertException("Reset link is not valid", "account", "resetlinkinvalid");
        }
        User user = userRepository
            .findOneByResetKey(key)
            .orElseThrow(() -> new BadRequestAlertException("Reset link is not valid", "account", "resetlinkinvalid"));

        // A found key with no reset date means the link was already consumed.
        if (user.getResetDate() == null) {
            throw new BadRequestAlertException("Reset link has already been used", "account", "resetlinkused");
        }

        if (!user.getResetDate().isAfter(Instant.now().minus(RESET_KEY_VALIDITY_MINUTES, ChronoUnit.MINUTES))) {
            throw new BadRequestAlertException("Reset link has expired", "account", "resetlinkexpired");
        }

        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            throw new InvalidPasswordException();
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        // The user chose their own password, so any forced-change flag no longer applies.
        user.setMustChangePassword(false);
        // Keep the key so a later reuse resolves to "already used" instead of "not valid".
        user.setResetDate(null);
        userRepository.save(user);
        return user;
    }

    public Optional<User> requestPasswordReset(String documentTypeId, String documentNumber) {
        return userProfileRepository
            .findByDocumentTypeAndDocumentNumber(documentTypeId, documentNumber)
            .map(UserProfile::getUser)
            .filter(User::isActivated)
            .map(user -> {
                assignResetKey(user);
                return userRepository.save(user);
            });
    }

    /**
     * Generates a fresh reset link for the user identified by the given document number and
     * marks the password change as mandatory, so the Administrator can resend the access after a
     * failed credentials email (UC006, E7). The notification lifecycle is owned by the caller.
     *
     * @param documentNumber the unique document number identifying the user profile.
     * @return the user with the new reset key and the forced password change.
     * @throws BadRequestAlertException if no profile or user matches the document number.
     */
    @Transactional
    public User resendCredentials(String documentNumber) {
        String normalized = StringUtils.trimToEmpty(documentNumber);

        UserProfile profile = userProfileRepository
            .findByDocumentNumber(normalized)
            .orElseThrow(() ->
                new BadRequestAlertException(
                    "No user profile found for document number: " + normalized,
                    "userProfile",
                    "documentNumberNotFound"
                )
            );

        User user = profile.getUser();
        if (user == null) {
            throw new BadRequestAlertException("No user found for document number: " + normalized, "userManagement", "userNotFound");
        }

        assignResetKey(user);
        user.setMustChangePassword(true);
        userRepository.save(user);
        LOG.debug("Resent credentials for User: {}", user.getLogin());
        return user;
    }

    /**
     * Generates a fresh reset key and stamps its request date, so a reset link stays valid for
     * {@link #RESET_KEY_VALIDITY_MINUTES} and the templates render a non-empty link. The caller
     * owns the write.
     *
     * @param user the user that receives the new reset key.
     */
    private void assignResetKey(User user) {
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(Instant.now());
    }

    public User registerUser(ManagedUserVM userVM, String password) {
        if (userVM.getEmail() == null || userVM.getEmail().isBlank()) {
            throw new BadRequestAlertException("Email is required", "userManagement", "emailrequired");
        }

        String documentNumber = userVM.getDocumentNumber().trim();

        // ------- RESOLVE DOCUMENT TYPE FIRST (login is derived from it) -------
        DocumentType documentType = documentTypeRepository
            .findById(userVM.getDocumentTypeId())
            .orElseThrow(() -> new DocumentTypeNotFoundException("Document type not found"));

        if (Boolean.FALSE.equals(documentType.getIsActive())) {
            throw new BadRequestAlertException("Document type is inactive", "userProfile", "documentTypeInactive");
        }

        String login = buildLogin(documentType, documentNumber);

        if (login.isEmpty()) {
            throw new IllegalArgumentException("Document number cannot be null or empty");
        }

        userRepository.findOneByLogin(login).ifPresent(existingUser -> {
            if (existingUser.isActivated()) {
                throw new DocumentNumberAlreadyUsedException("Document number is already in use");
            }
            throw new BadRequestAlertException(
                "Document number belongs to a deactivated account",
                "userManagement",
                "documentnumberinactive"
            );
        });

        userRepository.findOneByEmailIgnoreCase(userVM.getEmail()).ifPresent(existing -> {
            throw new EmailAlreadyUsedException();
        });

        if (userProfileRepository.findByDocumentTypeAndDocumentNumber(documentType.getId(), documentNumber).isPresent()) {
            throw new DocumentNumberAlreadyUsedException("Document number is already in use for this document type");
        }

        if (!password.matches(PASSWORD_PATTERN.pattern())) {
            throw new InvalidPasswordException();
        }

        User newUser = new User();

        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(login);

        newUser.setPassword(encryptedPassword);
        newUser.setEmail(userVM.getEmail().toLowerCase());
        newUser.setImageUrl(userVM.getImageUrl());

        newUser.setLangKey(userVM.getLangKey() != null ? userVM.getLangKey() : Constants.DEFAULT_LANGUAGE);

        newUser.setActivated(true);
        newUser.setMustChangePassword(false);
        newUser.setAuthorities(buildAuthorities(AuthoritiesConstants.APPRENTICE));
        userRepository.save(newUser);

        // ------- CREATE USER PROFILE -------
        UserProfile userProfile = new UserProfile();

        userProfile.setFirstName(userVM.getFirstName().trim());
        if (userVM.getMiddleName() != null) {
            userProfile.setMiddleName(userVM.getMiddleName().trim());
        }
        userProfile.setFirstLastName(userVM.getFirstLastName().trim());
        if (userVM.getSecondLastName() != null) {
            userProfile.setSecondLastName(userVM.getSecondLastName().trim());
        }
        userProfile.setDocumentNumber(documentNumber);
        userProfile.setPhoneNumber(userVM.getPhoneNumber().trim());

        userProfile.setUser(newUser);
        userProfile.setDocumentType(documentType);

        userProfileRepository.save(userProfile);

        LOG.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    @Transactional
    public User createUser(AdminCreateUserVM userVM) {
        // ------- RESOLVE DOCUMENT TYPE FIRST (login is derived from it) -------
        DocumentType documentType = resolveDocumentType(userVM.getDocumentTypeId());

        String login = buildLogin(documentType, userVM.getDocumentNumber());

        userRepository.findOneByLogin(login).ifPresent(existing -> {
            throw new LoginAlreadyUsedException();
        });

        userRepository.findOneByEmailIgnoreCase(userVM.getEmail()).ifPresent(existing -> {
            throw new EmailAlreadyUsedException();
        });

        userProfileRepository.findByDocumentTypeAndDocumentNumber(documentType.getId(), userVM.getDocumentNumber()).ifPresent(existing -> {
            throw new DocumentNumberAlreadyUsedException("Document number already in use");
        });

        if (!userVM.getPassword().matches(PASSWORD_PATTERN.pattern())) {
            throw new InvalidPasswordException();
        }

        // ------- CREATE USER -------
        User user = new User();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode(userVM.getPassword()));
        user.setEmail(userVM.getEmail().toLowerCase().trim());
        user.setImageUrl(null);
        user.setLangKey(userVM.getLangKey() != null ? userVM.getLangKey() : Constants.DEFAULT_LANGUAGE);
        user.setActivated(true);
        user.setMustChangePassword(true);
        // The creation email links to the reset page through the reset key (UC006, E7): without it
        // the template renders an empty link. The same key is refreshed when the admin resends.
        assignResetKey(user);

        user.setAuthorities(buildAuthorities(userVM.getRole()));

        userRepository.save(user);

        // ------- CREATE USER PROFILE -------
        UserProfile userProfile = new UserProfile();
        userProfile.setFirstName(userVM.getFirstName().trim());
        if (userVM.getMiddleName() != null) {
            userProfile.setMiddleName(userVM.getMiddleName().trim());
        }
        userProfile.setFirstLastName(userVM.getFirstLastName().trim());
        if (userVM.getSecondLastName() != null) {
            userProfile.setSecondLastName(userVM.getSecondLastName().trim());
        }
        userProfile.setDocumentNumber(userVM.getDocumentNumber().trim());
        userProfile.setPhoneNumber(userVM.getPhoneNumber().trim());
        userProfile.setUser(user);
        userProfile.setDocumentType(documentType);

        userProfileRepository.save(userProfile);
        LOG.debug("Created Information for User: {}", user);
        return user;
    }

    private DocumentType resolveDocumentType(String documentTypeId) {
        return documentTypeRepository
            .findById(documentTypeId)
            .orElseThrow(() -> new DocumentTypeNotFoundException("Document type not found"));
    }

    /**
     * Builds a unique login from the {@code (documentType, documentNumber)} pair.
     * Format: {@code <initials>_<documentNumber>} (lowercased and trimmed), e.g. {@code cc_12345678}.
     * When the type has no initials, the login falls back to just the document number.
     */
    private String buildLogin(DocumentType documentType, String documentNumber) {
        String typeCode = documentType != null && documentType.getInitials() != null ? documentType.getInitials() : "";
        return (typeCode + "_" + documentNumber).toLowerCase().trim();
    }

    /**
     * Builds the authority set for a user: always {@code ROLE_USER} plus exactly one
     * extra role. Throws {@link BadRequestAlertException} when a role cannot be resolved.
     *
     * @param role the extra role (e.g. ADMIN / INSTRUCTOR / APPRENTICE).
     * @return the immutable result set {@code {ROLE_USER, role}}.
     */
    private Set<Authority> buildAuthorities(String role) {
        if (role == null || !ASSIGNABLE_ROLES.contains(role)) {
            throw new BadRequestAlertException("Role not found", "userManagement", "rolenotfound");
        }
        Set<Authority> authorities = new HashSet<>();
        authorities.add(
            authorityRepository
                .findById(AuthoritiesConstants.USER)
                .orElseThrow(() -> new BadRequestAlertException("Role not found", "userManagement", "rolenotfound"))
        );
        authorities.add(
            authorityRepository
                .findById(role)
                .orElseThrow(() -> new BadRequestAlertException("Role not found", "userManagement", "rolenotfound"))
        );
        return authorities;
    }

    /**
     * Update all information for a specific user (User + UserProfile), and return the
     * modified user. Login is re-derived from {@link AdminUpdateUserVM#getDocumentNumber()};
     * {@code activated} and client authorities are IGNORED.
     *
     * @param vm the update view model.
     * @return the updated user, or {@link Optional#empty()} when the user does not exist.
     */
    @Transactional
    public Optional<AdminUserDTO> updateUser(AdminUpdateUserVM vm) {
        return Optional.of(userRepository.findById(vm.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(user -> {
                String documentNumber = null;
                String newLogin = null;
                String originalLogin = user.getLogin();

                // ----- USER PROFILE (fetch first so we can read the existing documentType for login) -----
                UserProfile userProfile = userProfileRepository
                    .findOneByUserId(user.getId())
                    .orElseThrow(() -> new BadRequestAlertException("UserProfile not found for current user", "userProfile", "notfound"));

                // The protected admin's identity document cannot change, because its login is the
                // stable identifier used to protect the account.
                if (
                    StringUtils.equals(originalLogin, PROTECTED_ADMIN_LOGIN) &&
                    (vm.getDocumentNumber() != null || vm.getDocumentTypeId() != null)
                ) {
                    throw new BadRequestAlertException(
                        "La cuenta admin está protegida y no puede modificarse su documento",
                        "userManagement",
                        "adminprotected"
                    );
                }

                // ----- CONDITIONAL document / login re-derivation -----
                // The login is derived from the whole (documentType, documentNumber) pair, so changing
                // either of them must re-derive it; the untouched half falls back to the stored value.
                if (vm.getDocumentNumber() != null || vm.getDocumentTypeId() != null) {
                    DocumentType effectiveType =
                        vm.getDocumentTypeId() != null ? resolveDocumentType(vm.getDocumentTypeId()) : userProfile.getDocumentType();
                    String effectiveNumber =
                        vm.getDocumentNumber() != null ? vm.getDocumentNumber().trim() : userProfile.getDocumentNumber();
                    documentNumber = effectiveNumber;
                    newLogin = buildLogin(effectiveType, effectiveNumber);

                    if (!newLogin.equals(user.getLogin())) {
                        // uniqueness excluding self
                        userRepository.findOneByLogin(newLogin).ifPresent(existing -> {
                            if (!existing.getId().equals(vm.getId())) {
                                throw new LoginAlreadyUsedException();
                            }
                        });
                    }

                    userProfileRepository
                        .findByDocumentTypeAndDocumentNumber(effectiveType != null ? effectiveType.getId() : null, effectiveNumber)
                        .ifPresent(existing -> {
                            if (!existing.getUser().getId().equals(vm.getId())) {
                                throw new DocumentNumberAlreadyUsedException("Document number is already in use");
                            }
                        });

                    user.setLogin(newLogin);
                }

                // ----- CONDITIONAL email -----
                if (vm.getEmail() != null) {
                    userRepository.findOneByEmailIgnoreCase(vm.getEmail()).ifPresent(existing -> {
                        if (!existing.getId().equals(vm.getId())) {
                            throw new EmailAlreadyUsedException();
                        }
                    });
                    user.setEmail(vm.getEmail().toLowerCase().trim());
                }

                // ----- CONDITIONAL imageUrl / langKey -----
                if (vm.getImageUrl() != null) {
                    user.setImageUrl(vm.getImageUrl());
                }
                if (vm.getLangKey() != null) {
                    user.setLangKey(vm.getLangKey());
                }

                // ----- CONDITIONAL role (authorities only rebuilt when provided) -----
                if (vm.getRole() != null) {
                    Set<Authority> newAuthorities = buildAuthorities(vm.getRole());
                    boolean losesAdmin =
                        hasAuthority(user.getAuthorities(), AuthoritiesConstants.ADMIN) &&
                        !hasAuthority(newAuthorities, AuthoritiesConstants.ADMIN);
                    boolean losesInstructor =
                        hasAuthority(user.getAuthorities(), AuthoritiesConstants.INSTRUCTOR) &&
                        !hasAuthority(newAuthorities, AuthoritiesConstants.INSTRUCTOR);
                    if (losesAdmin) {
                        validateProtectedAdmin(user, originalLogin);
                        validateLastActiveAdmin(user);
                    }
                    if (losesInstructor) {
                        validateLastInstructor(userProfile);
                    }
                    user.setAuthorities(newAuthorities);
                }

                // ----- USER PROFILE (only provided fields are touched) -----
                if (vm.getFirstName() != null) {
                    userProfile.setFirstName(vm.getFirstName().trim());
                }
                if (vm.getMiddleName() != null) {
                    userProfile.setMiddleName(vm.getMiddleName().trim());
                }
                if (vm.getFirstLastName() != null) {
                    userProfile.setFirstLastName(vm.getFirstLastName().trim());
                }
                if (vm.getSecondLastName() != null) {
                    userProfile.setSecondLastName(vm.getSecondLastName().trim());
                }
                if (vm.getPhoneNumber() != null) {
                    userProfile.setPhoneNumber(vm.getPhoneNumber().trim());
                }
                if (vm.getDocumentNumber() != null) {
                    userProfile.setDocumentNumber(documentNumber);
                }
                if (vm.getDocumentTypeId() != null) {
                    userProfile.setDocumentType(resolveDocumentType(vm.getDocumentTypeId()));
                }
                userProfile.setUser(user);

                userRepository.save(user);
                userProfileRepository.save(userProfile);
                LOG.debug("Changed Information for User: {}", user);
                return new AdminUserDTO(user);
            });
    }

    /**
     * Set the activation status of the user account associated with the given document number.
     *
     * Validates that a {@link UserProfile} exists for the document number and that it
     * points to an existing {@link User}; then sets {@code activated} to the requested value.
     * Idempotent: setting a value the user already has is a no-op on the resulting state.
     *
     * @param documentNumber the unique document number identifying the user profile.
     * @param activated      the target activation state (true = active, false = inactive).
     * @return the updated admin user DTO.
     * @throws BadRequestAlertException if no profile or user is found for the document number.
     */
    @Transactional
    public AdminUserDTO setUserActivated(String documentNumber, boolean activated) {
        String normalized = StringUtils.trimToEmpty(documentNumber);

        UserProfile profile = userProfileRepository
            .findByDocumentNumber(normalized)
            .orElseThrow(() ->
                new BadRequestAlertException(
                    "No user profile found for document number: " + normalized,
                    "userProfile",
                    "documentNumberNotFound"
                )
            );

        User user = profile.getUser();
        if (user == null) {
            throw new BadRequestAlertException("No user found for document number: " + normalized, "userManagement", "userNotFound");
        }

        if (!activated) {
            validateProtectedAdmin(user, user.getLogin());
            validateLastActiveAdmin(user);
            validateLastInstructor(profile);
        }

        user.setActivated(activated);
        userRepository.save(user);
        LOG.debug("Set activated={} for User: {}", activated, user.getLogin());
        return new AdminUserDTO(user);
    }

    /**
     * The protected super admin (login {@code "admin"}) is never deactivated nor demoted.
     *
     * @param user the user targeted by the operation.
     * @throws BadRequestAlertException with key {@code adminprotected} when the rule is violated.
     */
    private void validateProtectedAdmin(User user, String login) {
        if (StringUtils.equals(login, PROTECTED_ADMIN_LOGIN)) {
            throw new BadRequestAlertException(
                "La cuenta admin está protegida y no puede desactivarse",
                "userManagement",
                "adminprotected"
            );
        }
    }

    /**
     * Deactivating or demoting an active administrator must never leave the system without any
     * active administrator.
     *
     * @param user the user losing the administrator role.
     * @throws BadRequestAlertException with key {@code lastAdmin} when no active administrator would remain.
     */
    private void validateLastActiveAdmin(User user) {
        boolean targetIsActiveAdmin = user.isActivated() && hasAuthority(user.getAuthorities(), AuthoritiesConstants.ADMIN);
        if (!targetIsActiveAdmin) {
            return;
        }

        // Count active admins EXCLUDING the target. If none remain, block.
        long remainingActiveAdmins = userRepository.countByActivatedTrueAndAuthorities_Name(AuthoritiesConstants.ADMIN) - 1;
        if (remainingActiveAdmins == 0) {
            throw new BadRequestAlertException("Debe existir al menos un Administrador activo", "userManagement", "lastAdmin");
        }
    }

    private static boolean hasAuthority(Collection<Authority> authorities, String role) {
        return authorities.stream().anyMatch(a -> StringUtils.equals(a.getName(), role));
    }

    /**
     * An instructor who is the ONLY instructor of one or more operable class sections cannot be
     * deactivated or demoted, since those sections would be left without an instructor. A ficha
     * is operable when its state is {@code PENDIENTE} or {@code ACTIVA}
     * (see {@link StateGrade#isOperable()}).
     *
     * @param profile the user profile that identifies the instructor (its linked user carries the role).
     * @throws BadRequestAlertException with key {@code lastInstructor} when the rule is violated.
     */
    private void validateLastInstructor(UserProfile profile) {
        User user = profile.getUser();
        if (user == null) {
            return;
        }
        boolean isInstructor = user
            .getAuthorities()
            .stream()
            .anyMatch(a -> StringUtils.equals(a.getName(), AuthoritiesConstants.INSTRUCTOR));
        if (!isInstructor) {
            return;
        }

        // PENDIENTE and ACTIVA fichas are still operable and hold their instructor;
        // FINALIZADA, APLAZADA and CANCELADA do not.
        List<ClassSection> operationalSections = classSectionRepository
            .findByInstructorId(profile.getId())
            .stream()
            .filter(section -> section.getGrade() != null && section.getGrade().getState().isOperable())
            .toList();

        if (operationalSections.isEmpty()) {
            return;
        }

        List<String> affected = operationalSections
            .stream()
            .map(section -> {
                String subjectName = StringUtils.isBlank(section.getSubjectName()) ? "Materia sin nombre" : section.getSubjectName();
                String gradeCode = section.getGrade().getCode();
                return StringUtils.isBlank(gradeCode) ? subjectName : subjectName + " (ficha " + gradeCode + ")";
            })
            .distinct()
            .sorted()
            .toList();

        throw new BadRequestAlertException(
            "Debe existir al menos un instructor: " + String.join("; ", affected) + " quedaría sin instructor",
            "userManagement",
            "lastInstructor"
        );
    }

    public void changePassword(String currentClearTextPassword, String newPassword) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                String currentEncryptedPassword = user.getPassword();
                if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                    throw new BadRequestAlertException("Current password is incorrect", "userManagement", "currentpasswordinvalid");
                }
                if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
                    throw new InvalidPasswordException();
                }
                if (passwordEncoder.matches(newPassword, currentEncryptedPassword)) {
                    throw new BadRequestAlertException(
                        "New password must be different from the current one",
                        "userManagement",
                        "samepassword"
                    );
                }
                String encryptedPassword = passwordEncoder.encode(newPassword);
                user.setPassword(encryptedPassword);
                user.setMustChangePassword(false);
                userRepository.save(user);
                LOG.debug("Changed password for User: {}", user);
            });
    }

    public Page<AdminUserDTO> getAllManagedUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(AdminUserDTO::new);
    }

    public Page<UserDTO> getAllPublicUsers(Pageable pageable) {
        return userRepository.findAllByIdNotNullAndActivatedIsTrue(pageable).map(UserDTO::new);
    }

    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneByLogin(login);
    }

    public Optional<User> getUserWithAuthorities() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneByLogin);
    }

    /**
     * Gets the profile of the current authenticated user.
     *
     * @return the current user's profile, or empty when the user or their profile does not exist.
     */
    public Optional<UserProfileDTO> getCurrentUserProfile() {
        LOG.debug("Request to get current user's profile");
        return SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .flatMap(user -> userProfileRepository.findOneByUserId(user.getId()).map(userProfileMapper::toDto));
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired every day, at 01:00 (am).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        userRepository
            .findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS))
            .forEach(user -> {
                LOG.debug("Deleting not activated user {}", user.getLogin());
                userRepository.delete(user);
            });
    }

    /**
     * Gets a list of all the authorities.
     * @return a list of all the authorities.
     */
    public List<String> getAuthorities() {
        return authorityRepository.findAll().stream().map(Authority::getName).toList();
    }

    // ----- USER ACCOUNT UPDATE -----

    /**
     * Updates the current user's own account information:
     * email, langKey and the associated UserProfile names/phone.
     * Optionally changes the password when newPassword is provided.
     */
    public void updateOwnAccount(AccountUpdateVM accountUpdateVM) {
        LOG.debug("Updating current user's account information");

        User user = SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .orElseThrow(() -> new BadRequestAlertException("Current user not found", "user", "notfound"));

        if (accountUpdateVM.getDocumentTypeId() != null || accountUpdateVM.getDocumentNumber() != null) {
            throw new BadRequestAlertException("The document cannot be modified", "userProfile", "documentimmutable");
        }

        if (accountUpdateVM.getEmail() != null) {
            userRepository.findOneByEmailIgnoreCase(accountUpdateVM.getEmail()).ifPresent(existing -> {
                if (!existing.getLogin().equalsIgnoreCase(user.getLogin())) {
                    throw new EmailAlreadyUsedException();
                }
            });

            user.setEmail(accountUpdateVM.getEmail().toLowerCase().trim());
        }

        if (accountUpdateVM.getLangKey() != null) {
            user.setLangKey(accountUpdateVM.getLangKey());
        }

        if (StringUtils.isNotBlank(accountUpdateVM.getNewPassword())) {
            if (
                StringUtils.isBlank(accountUpdateVM.getCurrentPassword()) ||
                !passwordEncoder.matches(accountUpdateVM.getCurrentPassword(), user.getPassword())
            ) {
                throw new BadRequestAlertException("Current password is incorrect", "userManagement", "currentpasswordinvalid");
            }

            if (!PASSWORD_PATTERN.matcher(accountUpdateVM.getNewPassword()).matches()) {
                throw new InvalidPasswordException();
            }

            if (passwordEncoder.matches(accountUpdateVM.getNewPassword(), user.getPassword())) {
                throw new BadRequestAlertException("New password must be different from the current one", "userManagement", "samepassword");
            }

            user.setPassword(passwordEncoder.encode(accountUpdateVM.getNewPassword()));
            user.setMustChangePassword(false);
        }

        UserProfile userProfile = userProfileRepository
            .findOneByUserId(user.getId())
            .orElseThrow(() -> new BadRequestAlertException("UserProfile not found for current user", "userProfile", "notfound"));

        if (accountUpdateVM.getFirstName() != null) {
            userProfile.setFirstName(accountUpdateVM.getFirstName().trim());
        }

        if (accountUpdateVM.getFirstLastName() != null) {
            userProfile.setFirstLastName(accountUpdateVM.getFirstLastName().trim());
        }

        if (accountUpdateVM.getPhoneNumber() != null) {
            userProfile.setPhoneNumber(accountUpdateVM.getPhoneNumber().trim());
        }

        if (accountUpdateVM.getMiddleName() != null) {
            String middleName = accountUpdateVM.getMiddleName().trim();
            userProfile.setMiddleName(middleName.isEmpty() ? null : middleName);
        }

        if (accountUpdateVM.getSecondLastName() != null) {
            String secondLastName = accountUpdateVM.getSecondLastName().trim();
            userProfile.setSecondLastName(secondLastName.isEmpty() ? null : secondLastName);
        }

        userRepository.save(user);
        userProfileRepository.save(userProfile);
    }
}
