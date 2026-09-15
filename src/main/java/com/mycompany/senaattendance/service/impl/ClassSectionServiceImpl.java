package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.StateGrade;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.ClassSectionService;
import com.mycompany.senaattendance.service.dto.ClassSectionDTO;
import com.mycompany.senaattendance.service.dto.UserProfileDTO;
import com.mycompany.senaattendance.service.mapper.ClassSectionMapper;
import com.mycompany.senaattendance.web.rest.errors.BadRequestAlertException;
import com.mycompany.senaattendance.web.rest.errors.ClassSectionNameAlreadyUsedException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.mycompany.senaattendance.domain.ClassSection}.
 */
@Service
public class ClassSectionServiceImpl implements ClassSectionService {

    private static final Logger LOG = LoggerFactory.getLogger(ClassSectionServiceImpl.class);

    private static final String ENTITY_NAME = "classSection";

    private final ClassSectionRepository classSectionRepository;

    private final ClassSectionMapper classSectionMapper;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final GradeRepository gradeRepository;

    public ClassSectionServiceImpl(
        ClassSectionRepository classSectionRepository,
        ClassSectionMapper classSectionMapper,
        UserRepository userRepository,
        UserProfileRepository userProfileRepository,
        GradeRepository gradeRepository
    ) {
        this.classSectionRepository = classSectionRepository;
        this.classSectionMapper = classSectionMapper;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.gradeRepository = gradeRepository;
    }

    @Override
    public ClassSectionDTO save(ClassSectionDTO classSectionDTO) {
        LOG.debug("Request to save ClassSection : {}", classSectionDTO);
        ClassSection classSection = classSectionMapper.toEntity(classSectionDTO);
        validateGradeIsOperable(classSection);
        validateInstructor(classSectionDTO);
        validateAndNormalizeSubjectName(classSection, null);

        classSection.setCreatedDate(Instant.now());
        Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
        if (currentUserLogin.isPresent()) {
            classSection.setCreatedBy(currentUserLogin.get());
        }

        classSection = classSectionRepository.save(classSection);
        return classSectionMapper.toDto(classSection);
    }

    @Override
    public ClassSectionDTO update(ClassSectionDTO classSectionDTO) {
        LOG.debug("Request to update ClassSection : {}", classSectionDTO);
        ClassSection classSection = classSectionMapper.toEntity(classSectionDTO);
        validateGradeIsOperable(classSection);
        validateInstructor(classSectionDTO);
        validateAndNormalizeSubjectName(classSection, classSection.getId());

        Optional<ClassSection> optionalClassSection = classSectionRepository.findById(classSection.getId());
        if (optionalClassSection.isPresent()) {
            ClassSection existingClassSection = optionalClassSection.get();
            classSection.setCreatedBy(existingClassSection.getCreatedBy());
            classSection.setCreatedDate(existingClassSection.getCreatedDate());
        } else {
            classSection.setCreatedDate(Instant.now());
            Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
            if (currentUserLogin.isPresent()) {
                classSection.setCreatedBy(currentUserLogin.get());
            }
        }

        classSection = classSectionRepository.save(classSection);
        return classSectionMapper.toDto(classSection);
    }

    @Override
    public Optional<ClassSectionDTO> partialUpdate(ClassSectionDTO classSectionDTO) {
        LOG.debug("Request to partially update ClassSection : {}", classSectionDTO);

        return classSectionRepository
            .findById(classSectionDTO.getId())
            .map(existingClassSection -> {
                classSectionMapper.partialUpdate(existingClassSection, classSectionDTO);
                validateGradeIsOperable(existingClassSection);
                validateInstructor(classSectionDTO);
                validateAndNormalizeSubjectName(existingClassSection, existingClassSection.getId());

                return existingClassSection;
            })
            .map(classSectionRepository::save)
            .map(classSectionMapper::toDto);
    }

    @Override
    public Page<ClassSectionDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all ClassSections");
        return classSectionRepository.findAll(pageable).map(classSectionMapper::toDto);
    }

    public Page<ClassSectionDTO> findAllWithEagerRelationships(Pageable pageable) {
        return classSectionRepository.findAllWithEagerRelationships(pageable).map(classSectionMapper::toDto);
    }

    @Override
    public Optional<ClassSectionDTO> findOne(String id) {
        LOG.debug("Request to get ClassSection : {}", id);
        return classSectionRepository.findOneWithEagerRelationships(id).map(classSectionMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete ClassSection : {}", id);
        classSectionRepository.deleteById(id);
    }

    @Override
    public List<ClassSectionDTO> findAllForCurrentInstructor() {
        LOG.debug("Request to get all ClassSections for the current instructor");
        Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
        if (currentUserLogin.isEmpty()) {
            return Collections.emptyList();
        }

        User user = userRepository.findOneByLogin(currentUserLogin.get()).orElse(null);
        if (user == null) {
            return Collections.emptyList();
        }

        Optional<UserProfile> profileOpt = userProfileRepository.findOneByUserId(user.getId());
        if (profileOpt.isEmpty()) {
            return Collections.emptyList();
        }

        String profileId = profileOpt.get().getId();

        return classSectionRepository.findByInstructorId(profileId).stream().map(classSectionMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Rejects a write on a ficha that is not PENDIENTE or ACTIVA. The ficha is resolved by id
     * because the payload may only carry a reference to it; when the reference or the ficha
     * cannot be resolved, the check is skipped so the other validations report their own error.
     *
     * @param classSection the class section about to be persisted.
     * @throws BadRequestAlertException when the ficha exists and is not operable.
     */
    private void validateGradeIsOperable(ClassSection classSection) {
        Grade grade = classSection.getGrade();
        if (grade == null || grade.getId() == null) {
            return;
        }
        Grade persistedGrade = gradeRepository.findById(grade.getId()).orElse(null);
        if (persistedGrade == null) {
            return;
        }
        if (persistedGrade.getState() != StateGrade.PENDIENTE && persistedGrade.getState() != StateGrade.ACTIVA) {
            throw new BadRequestAlertException(
                "No se pueden crear ni modificar materias en una ficha en su estado actual",
                ENTITY_NAME,
                "gradeNotOperable"
            );
        }
    }

    /**
     * Validates the instructor of a class section payload when one is provided. The instructor is
     * optional, but an assigned one must still exist and belong to an activated account.
     *
     * @param classSectionDTO the payload to validate.
     * @throws BadRequestAlertException when the instructor does not exist or its account is inactive.
     */
    private void validateInstructor(ClassSectionDTO classSectionDTO) {
        UserProfileDTO instructor = classSectionDTO.getInstructor();
        if (instructor == null) {
            return;
        }

        User instructorUser = Optional.ofNullable(instructor.getId())
            .flatMap(userProfileRepository::findById)
            .map(UserProfile::getUser)
            .orElse(null);

        if (instructorUser == null || !instructorUser.isActivated()) {
            throw new BadRequestAlertException(
                "El instructor seleccionado ya no está disponible, selecciona otro",
                ENTITY_NAME,
                "instructorInactive"
            );
        }
    }

    /**
     * Trims the subject name and enforces its uniqueness inside the ficha (grade). When
     * {@code excludeId} is not {@code null}, the class section with that id is ignored so an
     * update that keeps the same name does not collide with itself; on create every class
     * section of the ficha is considered. The trimmed name is written back onto the entity so
     * the stored value is consistent.
     *
     * @param classSection the class section whose subject name is normalized and validated.
     * @param excludeId the id to exclude from the uniqueness check, or {@code null} on create.
     * @throws ClassSectionNameAlreadyUsedException if another class section in the same ficha uses the name.
     */
    private void validateAndNormalizeSubjectName(ClassSection classSection, String excludeId) {
        if (classSection.getSubjectName() == null) {
            return;
        }
        String subjectName = classSection.getSubjectName().trim();
        classSection.setSubjectName(subjectName);

        String gradeId = classSection.getGrade() != null ? classSection.getGrade().getId() : null;
        if (gradeId == null) {
            return;
        }

        boolean duplicate =
            excludeId == null
                ? classSectionRepository.existsBySubjectNameIgnoreCaseAndGradeId(subjectName, gradeId)
                : classSectionRepository.existsBySubjectNameIgnoreCaseAndGradeIdAndIdNot(subjectName, gradeId, excludeId);
        if (duplicate) {
            throw new ClassSectionNameAlreadyUsedException();
        }
    }
}
