package com.mycompany.senaattendance.service;

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
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.dto.AdminUserDTO;
import com.mycompany.senaattendance.service.dto.UserDTO;
import com.mycompany.senaattendance.web.rest.errors.BadRequestAlertException;
import com.mycompany.senaattendance.web.rest.errors.DocumentNumberAlreadyUsedException;
import com.mycompany.senaattendance.web.rest.errors.DocumentTypeNotFoundException;
import com.mycompany.senaattendance.web.rest.errors.LoginAlreadyUsedException;
import com.mycompany.senaattendance.web.rest.vm.AccountUpdateVM;
import com.mycompany.senaattendance.web.rest.vm.AdminCreateUserVM;
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

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthorityRepository authorityRepository;

    private final UserProfileRepository userProfileRepository;

    private final DocumentTypeRepository documentTypeRepository;

    public UserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthorityRepository authorityRepository,
        UserProfileRepository userProfileRepository,
        DocumentTypeRepository documentTypeRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorityRepository = authorityRepository;
        this.userProfileRepository = userProfileRepository;
        this.documentTypeRepository = documentTypeRepository;
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

    public Optional<User> completePasswordReset(String newPassword, String key) {
        LOG.debug("Reset user password for reset key {}", key);
        return userRepository
            .findOneByResetKey(key)
            .filter(user -> user.getResetDate().isAfter(Instant.now().minus(1, ChronoUnit.DAYS)))
            .map(user -> {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetKey(null);
                user.setResetDate(null);
                userRepository.save(user);
                return user;
            });
    }

    public Optional<User> requestPasswordReset(String documentTypeId, String documentNumber) {
        return userProfileRepository
            .findByDocumentTypeAndDocumentNumber(documentTypeId, documentNumber)
            .map(UserProfile::getUser)
            .filter(User::isActivated)
            .map(user -> {
                user.setResetKey(RandomUtil.generateResetKey());
                user.setResetDate(Instant.now());
                userRepository.save(user);
                return user;
            });
    }

    public User registerUser(ManagedUserVM userVM, String password) {
        String login = userVM.getDocumentNumber().toLowerCase().trim();

        if (login.isEmpty()) {
            throw new IllegalArgumentException("Document number cannot be null or empty");
        }

        userRepository.findOneByLogin(login).ifPresent(existingUser -> {
            boolean removed = removeNonActivatedUser(existingUser);
            if (!removed) {
                throw new UsernameAlreadyUsedException();
            }
        });

        userRepository.findOneByEmailIgnoreCase(userVM.getEmail()).ifPresent(existingUser -> {
            boolean removed = removeNonActivatedUser(existingUser);
            if (!removed) {
                throw new EmailAlreadyUsedException();
            }
        });

        User newUser = new User();

        if (!password.matches(PASSWORD_PATTERN.pattern())) {
            throw new InvalidPasswordException();
        }

        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(login);

        newUser.setPassword(encryptedPassword);
        if (userVM.getEmail() != null) {
            newUser.setEmail(userVM.getEmail().toLowerCase());
        }
        newUser.setImageUrl(userVM.getImageUrl());

        if (userVM.getLangKey() != null) {
            newUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        } else {
            newUser.setLangKey(userVM.getLangKey());
        }

        newUser.setActivated(true);
        Set<Authority> authorities = new HashSet<>();
        authorityRepository.findById(AuthoritiesConstants.USER).ifPresent(authorities::add);
        authorityRepository.findById(AuthoritiesConstants.APPRENTICE).ifPresent(authorities::add);
        newUser.setAuthorities(authorities);
        userRepository.save(newUser);

        // ------- SEARCH DOCUMENT TYPE -------
        DocumentType documentType = documentTypeRepository
            .findById(userVM.getDocumentTypeId())
            .orElseThrow(() -> new DocumentTypeNotFoundException("Document type not found"));

        if (documentType == null) {
            throw new DocumentTypeNotFoundException("Document type not found");
        }

        // ------- CREATE USER PROFILE -------
        UserProfile userProfile = new UserProfile();

        if (userProfileRepository.findByDocumentNumber(userVM.getDocumentNumber()).isPresent()) {
            throw new DocumentNumberAlreadyUsedException("Document number is already in use");
        }

        userProfile.setFirstName(userVM.getFirstName());
        userProfile.setMiddleName(userVM.getMiddleName());
        userProfile.setFirstLastName(userVM.getFirstLastName());
        userProfile.setSecondLastName(userVM.getSecondLastName());
        userProfile.setDocumentNumber(userVM.getDocumentNumber());
        userProfile.setPhoneNumber(userVM.getPhoneNumber());

        userProfile.setUser(newUser);
        userProfile.setDocumentType(documentType);

        userProfileRepository.save(userProfile);

        LOG.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    private boolean removeNonActivatedUser(User existingUser) {
        if (existingUser.isActivated()) {
            return false;
        }
        userRepository.delete(existingUser);
        return true;
    }

    @Transactional
    public User createUser(AdminCreateUserVM userVM) {
        String login = userVM.getDocumentNumber().toLowerCase().trim();

        userRepository.findOneByLogin(login).ifPresent(existing -> {
            throw new LoginAlreadyUsedException();
        });

        userRepository.findOneByEmailIgnoreCase(userVM.getEmail()).ifPresent(existing -> {
            throw new EmailAlreadyUsedException();
        });

        userProfileRepository.findByDocumentNumber((userVM.getDocumentNumber())).ifPresent(existing -> {
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

        Set<Authority> authorities = new HashSet<>();

        authorityRepository.findById(userVM.getRole()).ifPresent(authorities::add);

        if (authorities.isEmpty()) {
            throw new BadRequestAlertException("Role not found", "userManagement", "rolenotfound");
        }

        user.setAuthorities(authorities);

        userRepository.save(user);

        // ------- CREATE USER PROFILE -------
        UserProfile userProfile = new UserProfile();
        userProfile.setFirstName(userVM.getFirstName().trim());
        userProfile.setMiddleName(userVM.getMiddleName().trim());
        userProfile.setFirstLastName(userVM.getFirstLastName().trim());
        userProfile.setSecondLastName(userVM.getSecondLastName().trim());
        userProfile.setDocumentNumber(userVM.getDocumentNumber().trim());
        userProfile.setPhoneNumber(userVM.getPhoneNumber().trim());
        userProfile.setUser(user);
        userProfile.setDocumentType(resolveDocumentType(userVM.getDocumentTypeId()));

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
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update.
     * @return updated user.
     */
    public Optional<AdminUserDTO> updateUser(AdminUserDTO userDTO) {
        return Optional.of(userRepository.findById(userDTO.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(user -> {
                user.setLogin(userDTO.getLogin().toLowerCase());
                if (userDTO.getEmail() != null) {
                    user.setEmail(userDTO.getEmail().toLowerCase());
                }
                user.setImageUrl(userDTO.getImageUrl());
                user.setActivated(userDTO.isActivated());
                user.setLangKey(userDTO.getLangKey());
                Set<Authority> managedAuthorities = user.getAuthorities();
                managedAuthorities.clear();
                userDTO
                    .getAuthorities()
                    .stream()
                    .map(authorityRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(managedAuthorities::add);
                userRepository.save(user);
                LOG.debug("Changed Information for User: {}", user);
                return user;
            })
            .map(AdminUserDTO::new);
    }

    public void deleteUser(String login) {
        userRepository.findOneByLogin(login).ifPresent(user -> {
            userRepository.delete(user);
            LOG.debug("Deleted User: {}", user);
        });
    }

    /**
     * Update basic information (email, language) for the current user.
     *
     * @param email     email id of user.
     * @param langKey   language key.
     * @param imageUrl  image URL of user.
     */
    public void updateUser(String email, String langKey, String imageUrl) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                if (email != null) {
                    user.setEmail(email.toLowerCase());
                }
                user.setLangKey(langKey);
                user.setImageUrl(imageUrl);
                userRepository.save(user);
                LOG.debug("Changed Information for User: {}", user);
            });
    }

    public void changePassword(String currentClearTextPassword, String newPassword) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                String currentEncryptedPassword = user.getPassword();
                if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                    throw new InvalidPasswordException();
                }
                String encryptedPassword = passwordEncoder.encode(newPassword);
                user.setPassword(encryptedPassword);
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
     * email, langKey, imageUrl and the associated UserProfile names/phone.
     * Optionally changes the password when newPassword is provided.
     */
    public void updateOwnAccount(AccountUpdateVM accountUpdateVM) {
        LOG.debug("Updating current user's account information");

        User user = SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .orElseThrow(() -> new BadRequestAlertException("Current user not found", "user", "notfound"));

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

        if (accountUpdateVM.getImageUrl() != null) {
            user.setImageUrl(accountUpdateVM.getImageUrl());
        }

        if (StringUtils.isNotBlank(accountUpdateVM.getNewPassword())) {
            if (
                StringUtils.isBlank(accountUpdateVM.getCurrentPassword()) ||
                !passwordEncoder.matches(accountUpdateVM.getCurrentPassword(), user.getPassword())
            ) {
                throw new InvalidPasswordException();
            }

            if (!PASSWORD_PATTERN.matcher(accountUpdateVM.getNewPassword()).matches()) {
                throw new InvalidPasswordException();
            }

            user.setPassword(passwordEncoder.encode(accountUpdateVM.getNewPassword()));
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
            userProfile.setMiddleName(accountUpdateVM.getMiddleName().trim());
        }

        if (accountUpdateVM.getSecondLastName() != null) {
            userProfile.setSecondLastName(accountUpdateVM.getSecondLastName().trim());
        }

        userRepository.save(user);
        userProfileRepository.save(userProfile);
    }
}
