package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.ClassException;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.repository.ClassExceptionRepository;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.ClassExceptionService;
import com.mycompany.senaattendance.service.dto.ClassExceptionDTO;
import com.mycompany.senaattendance.service.mapper.ClassExceptionMapper;
import com.mycompany.senaattendance.web.rest.errors.BadRequestAlertException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.mycompany.senaattendance.domain.ClassException}.
 *
 * <p>A non-teaching date is a precedent: once its date has passed, the exception stays and only
 * its reason can be edited (UC009 A4). An instructor manages only the exceptions of the materias
 * assigned to them, while an administrator manages every materia.
 */
@Service
public class ClassExceptionServiceImpl implements ClassExceptionService {

    private static final Logger LOG = LoggerFactory.getLogger(ClassExceptionServiceImpl.class);

    private static final String ENTITY_NAME = "classException";

    private final ClassExceptionRepository classExceptionRepository;

    private final ClassSectionRepository classSectionRepository;

    private final UserRepository userRepository;

    private final UserProfileRepository userProfileRepository;

    private final ClassExceptionMapper classExceptionMapper;

    private final Clock clock;

    public ClassExceptionServiceImpl(
        ClassExceptionRepository classExceptionRepository,
        ClassSectionRepository classSectionRepository,
        UserRepository userRepository,
        UserProfileRepository userProfileRepository,
        ClassExceptionMapper classExceptionMapper,
        Clock clock
    ) {
        this.classExceptionRepository = classExceptionRepository;
        this.classSectionRepository = classSectionRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.classExceptionMapper = classExceptionMapper;
        this.clock = clock;
    }

    /**
     * Marks a non-teaching date of a materia. The date cannot be created in the past: a precedent
     * is only born while the date has not passed yet.
     *
     * @param classExceptionDTO the entity to save.
     * @return the persisted entity.
     * @throws BadRequestAlertException when the materia does not exist, when the current instructor
     *         does not own it or when the date is already past.
     */
    @Override
    public ClassExceptionDTO save(ClassExceptionDTO classExceptionDTO) {
        LOG.debug("Request to save ClassException : {}", classExceptionDTO);
        ClassException classException = classExceptionMapper.toEntity(classExceptionDTO);

        ClassSection classSection = resolveClassSection(classExceptionDTO);
        validateOwnership(classSection);
        if (isPast(classException.getDate())) {
            throw pastExceptionLocked();
        }

        classException.setClassSection(classSection);
        classException.setCreatedDate(Instant.now());
        Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
        if (currentUserLogin.isPresent()) {
            classException.setCreatedBy(currentUserLogin.get());
        }

        classException = classExceptionRepository.save(classException);
        return classExceptionMapper.toDto(classException);
    }

    /**
     * Replaces a non-teaching date. A past exception keeps its date and materia: only its reason
     * can change, and no exception can be moved into the past.
     *
     * @param classExceptionDTO the entity to update.
     * @return the persisted entity.
     * @throws BadRequestAlertException when the materia does not exist, when the current instructor
     *         does not own it or when the edit violates the past exception lock.
     */
    @Override
    public ClassExceptionDTO update(ClassExceptionDTO classExceptionDTO) {
        LOG.debug("Request to update ClassException : {}", classExceptionDTO);
        ClassException classException = classExceptionMapper.toEntity(classExceptionDTO);

        ClassSection classSection = resolveClassSection(classExceptionDTO);
        validateOwnership(classSection);

        Optional<ClassException> optionalClassException = classExceptionRepository.findById(classException.getId());
        if (optionalClassException.isPresent()) {
            ClassException existingClassException = optionalClassException.get();
            validatePastExceptionChange(existingClassException, classException.getDate(), classSection.getId());
            classException.setCreatedBy(existingClassException.getCreatedBy());
            classException.setCreatedDate(existingClassException.getCreatedDate());
        } else {
            if (isPast(classException.getDate())) {
                throw pastExceptionLocked();
            }
            classException.setCreatedDate(Instant.now());
            Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
            if (currentUserLogin.isPresent()) {
                classException.setCreatedBy(currentUserLogin.get());
            }
        }

        classException.setClassSection(classSection);
        classException = classExceptionRepository.save(classException);
        return classExceptionMapper.toDto(classException);
    }

    /**
     * Partially updates a non-teaching date, applying the same past lock over the effective values
     * the record would end up with.
     *
     * @param classExceptionDTO the entity to update partially.
     * @return the persisted entity.
     */
    @Override
    public Optional<ClassExceptionDTO> partialUpdate(ClassExceptionDTO classExceptionDTO) {
        LOG.debug("Request to partially update ClassException : {}", classExceptionDTO);

        return classExceptionRepository
            .findById(classExceptionDTO.getId())
            .map(existingClassException -> {
                LocalDate effectiveDate =
                    classExceptionDTO.getDate() != null ? classExceptionDTO.getDate() : existingClassException.getDate();
                ClassSection effectiveClassSection =
                    classExceptionDTO.getClassSection() != null
                        ? resolveClassSection(classExceptionDTO)
                        : existingClassException.getClassSection();

                validateOwnership(effectiveClassSection);
                validatePastExceptionChange(existingClassException, effectiveDate, effectiveClassSection.getId());

                classExceptionMapper.partialUpdate(existingClassException, classExceptionDTO);

                return existingClassException;
            })
            .map(classExceptionRepository::save)
            .map(classExceptionMapper::toDto);
    }

    @Override
    public Page<ClassExceptionDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all ClassExceptions");
        return classExceptionRepository.findAll(pageable).map(classExceptionMapper::toDto);
    }

    public Page<ClassExceptionDTO> findAllWithEagerRelationships(Pageable pageable) {
        return classExceptionRepository.findAllWithEagerRelationships(pageable).map(classExceptionMapper::toDto);
    }

    @Override
    public Optional<ClassExceptionDTO> findOne(String id) {
        LOG.debug("Request to get ClassException : {}", id);
        return classExceptionRepository.findOneWithEagerRelationships(id).map(classExceptionMapper::toDto);
    }

    @Override
    public Page<ClassExceptionDTO> findAllForCurrentUser(Pageable pageable) {
        LOG.debug("Request to get the class exceptions the current user can read");
        return findPageForCurrentUser(pageable, false).map(classExceptionMapper::toDto);
    }

    @Override
    public Page<ClassExceptionDTO> findAllWithEagerRelationshipsForCurrentUser(Pageable pageable) {
        LOG.debug("Request to get the class exceptions the current user can read with eager relationships");
        return findPageForCurrentUser(pageable, true).map(classExceptionMapper::toDto);
    }

    @Override
    public Optional<ClassExceptionDTO> findOneForCurrentUser(String id) {
        LOG.debug("Request to get ClassException : {}", id);
        return classExceptionRepository
            .findOneWithEagerRelationships(id)
            .filter(this::isReadableByCurrentUser)
            .map(classExceptionMapper::toDto);
    }

    /**
     * Deletes a non-teaching date while it has not passed. A past exception is a precedent and is
     * never removed, not even by an administrator.
     *
     * @param id the id of the entity.
     * @throws BadRequestAlertException when the current instructor does not own the materia or when
     *         the date is already past.
     */
    @Override
    public void delete(String id) {
        LOG.debug("Request to delete ClassException : {}", id);
        classExceptionRepository.findById(id).ifPresent(existingClassException -> {
            validateOwnership(existingClassException.getClassSection());
            if (isPast(existingClassException.getDate())) {
                throw pastExceptionLocked();
            }
            classExceptionRepository.deleteById(id);
        });
    }

    /**
     * Resolves the page of exceptions the current user can read. An administrator reads every
     * exception; an instructor reads only the exceptions of the class sections assigned to them.
     *
     * @param pageable the pagination information.
     * @param eagerRelationships whether to load the related entities eagerly.
     * @return the page of readable exceptions.
     */
    private Page<ClassException> findPageForCurrentUser(Pageable pageable, boolean eagerRelationships) {
        if (isCurrentUserAdmin()) {
            return eagerRelationships
                ? classExceptionRepository.findAllWithEagerRelationships(pageable)
                : classExceptionRepository.findAll(pageable);
        }

        List<ObjectId> classSectionScope = currentInstructorClassSectionIds();
        if (classSectionScope.isEmpty()) {
            return Page.empty(pageable);
        }
        return classExceptionRepository.findByClassSectionIdIn(classSectionScope, pageable);
    }

    /**
     * @return whether the current user is an administrator, who reads and manages every exception.
     */
    private static boolean isCurrentUserAdmin() {
        return SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
    }

    /**
     * Resolves whether the current user can read the exception: an administrator reads everything,
     * and an instructor only the exceptions of the class sections assigned to them.
     *
     * @param classException the exception to read.
     * @return whether the exception is inside the readable scope of the current user.
     */
    private boolean isReadableByCurrentUser(ClassException classException) {
        if (isCurrentUserAdmin()) {
            return true;
        }

        ClassSection classSection = classException.getClassSection();
        if (classSection == null || classSection.getInstructor() == null) {
            return false;
        }
        String currentProfileId = currentUserProfileId();
        return currentProfileId != null && currentProfileId.equals(classSection.getInstructor().getId());
    }

    /**
     * Resolves the class sections assigned to the current instructor. A user without a resolvable
     * profile or without assigned class sections reads no exception.
     *
     * @return the ObjectId values of the assigned class sections, possibly empty.
     */
    private List<ObjectId> currentInstructorClassSectionIds() {
        String currentProfileId = currentUserProfileId();
        if (currentProfileId == null) {
            return List.of();
        }
        return classSectionRepository
            .findByInstructorId(currentProfileId)
            .stream()
            .map(ClassSection::getId)
            .filter(ClassExceptionServiceImpl::isObjectId)
            .map(ObjectId::new)
            .toList();
    }

    /**
     * @param id the candidate id.
     * @return whether the id is a 24-hex string convertible into an {@code ObjectId}.
     */
    private static boolean isObjectId(String id) {
        return id != null && id.length() == 24 && ObjectId.isValid(id);
    }

    /**
     * Resolves the materia of the exception, which is the entry point of the ownership check.
     *
     * @param classExceptionDTO the requested exception.
     * @return the persisted class section.
     * @throws BadRequestAlertException when no class section matches the requested id.
     */
    private ClassSection resolveClassSection(ClassExceptionDTO classExceptionDTO) {
        if (classExceptionDTO.getClassSection() == null || classExceptionDTO.getClassSection().getId() == null) {
            throw new BadRequestAlertException("La materia no existe", ENTITY_NAME, "idnotfound");
        }
        return classSectionRepository
            .findById(classExceptionDTO.getClassSection().getId())
            .orElseThrow(() -> new BadRequestAlertException("La materia no existe", ENTITY_NAME, "idnotfound"));
    }

    /**
     * Rejects a write over an exception of a materia that is not assigned to the current
     * instructor. An administrator manages the exceptions of every materia.
     *
     * @param classSection the materia of the exception.
     * @throws BadRequestAlertException when the current user is not the assigned instructor.
     */
    private void validateOwnership(ClassSection classSection) {
        if (isCurrentUserAdmin()) {
            return;
        }

        UserProfile instructor = classSection != null ? classSection.getInstructor() : null;
        String currentProfileId = currentUserProfileId();
        if (instructor == null || currentProfileId == null || !currentProfileId.equals(instructor.getId())) {
            throw new BadRequestAlertException(
                "Solo el instructor asignado a la materia puede gestionar sus fechas no lectivas",
                ENTITY_NAME,
                "notYourClassSection"
            );
        }
    }

    /**
     * Rejects an edit that would move a past exception or create one in the past. A past exception
     * can only change its reason, so its date and materia must stay untouched.
     *
     * @param existingClassException the persisted exception.
     * @param requestedDate the date the record would end up with.
     * @param requestedClassSectionId the class section id the record would end up with.
     * @throws BadRequestAlertException when the past precedent is being moved, recreated or removed.
     */
    private void validatePastExceptionChange(
        ClassException existingClassException,
        LocalDate requestedDate,
        String requestedClassSectionId
    ) {
        if (!isPast(existingClassException.getDate()) && !isPast(requestedDate)) {
            return;
        }

        boolean sameDate = Objects.equals(existingClassException.getDate(), requestedDate);
        boolean sameClassSection =
            existingClassException.getClassSection() != null &&
            Objects.equals(existingClassException.getClassSection().getId(), requestedClassSectionId);
        if (!sameDate || !sameClassSection) {
            throw pastExceptionLocked();
        }
    }

    /**
     * @param date the date to check.
     * @return whether the date is before today, the moment a non-teaching date becomes a precedent.
     */
    private boolean isPast(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now(clock));
    }

    /**
     * @return the error of an edit that would break the precedent of a past non-teaching date.
     */
    private static BadRequestAlertException pastExceptionLocked() {
        return new BadRequestAlertException(
            "Una fecha no lectiva pasada solo puede modificar su motivo: el precedente no se elimina",
            ENTITY_NAME,
            "pastExceptionLocked"
        );
    }

    /**
     * @return the profile id of the authenticated user, or {@code null} when the session has no
     *         login or the account has no profile.
     */
    private String currentUserProfileId() {
        return SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .map(User::getId)
            .flatMap(userProfileRepository::findOneByUserId)
            .map(UserProfile::getId)
            .orElse(null);
    }
}
