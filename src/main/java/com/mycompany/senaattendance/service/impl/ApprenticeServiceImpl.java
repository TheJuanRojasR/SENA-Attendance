package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.Apprentice;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.StateAcademic;
import com.mycompany.senaattendance.domain.enumeration.StateGrade;
import com.mycompany.senaattendance.repository.ApprenticeRepository;
import com.mycompany.senaattendance.repository.AttendanceRepository;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.ApprenticeService;
import com.mycompany.senaattendance.service.dto.ApprenticeDTO;
import com.mycompany.senaattendance.service.mapper.ApprenticeMapper;
import com.mycompany.senaattendance.web.rest.errors.BadRequestAlertException;
import com.mycompany.senaattendance.web.rest.vm.EnrollApprenticeVM;
import com.mycompany.senaattendance.web.rest.vm.UnlinkApprenticeVM;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.mycompany.senaattendance.domain.Apprentice}.
 */
@Service
public class ApprenticeServiceImpl implements ApprenticeService {

    private static final Logger LOG = LoggerFactory.getLogger(ApprenticeServiceImpl.class);

    private static final String ENTITY_NAME = "apprentice";

    /**
     * The withdrawal reasons accepted by the unlink operation (UC008, A1). Enrolling or leaving
     * an apprentice inactive are not valid ways to unlink it.
     */
    private static final Set<StateAcademic> UNLINK_REASONS = Set.of(
        StateAcademic.RETIRO_VOLUNTARIO,
        StateAcademic.APLAZADO,
        StateAcademic.CANCELADO
    );

    private final ApprenticeRepository apprenticeRepository;

    private final GradeRepository gradeRepository;

    private final UserProfileRepository userProfileRepository;

    private final ClassSectionRepository classSectionRepository;

    private final AttendanceRepository attendanceRepository;

    private final ApprenticeMapper apprenticeMapper;

    public ApprenticeServiceImpl(
        ApprenticeRepository apprenticeRepository,
        GradeRepository gradeRepository,
        UserProfileRepository userProfileRepository,
        ClassSectionRepository classSectionRepository,
        AttendanceRepository attendanceRepository,
        ApprenticeMapper apprenticeMapper
    ) {
        this.apprenticeRepository = apprenticeRepository;
        this.gradeRepository = gradeRepository;
        this.userProfileRepository = userProfileRepository;
        this.classSectionRepository = classSectionRepository;
        this.attendanceRepository = attendanceRepository;
        this.apprenticeMapper = apprenticeMapper;
    }

    /**
     * Enrolls the apprentice in the ficha (UC008). The academic state is set by the server as
     * {@code MATRICULADO}, so a state sent by the client is never persisted.
     *
     * <p>The document number is not unique on its own: the unique key is the
     * (documentType, documentNumber) pair, so a number shared by more than one profile does not
     * identify a student and is rejected as {@code apprenticeInactive}.
     *
     * <p>Rejections: a missing, deactivated or ambiguous apprentice account
     * ({@code apprenticeInactive}), an apprentice that already has a record in the ficha in any
     * state ({@code apprenticeAlreadyEnrolled}) and a ficha that is not {@code PENDIENTE} or
     * {@code ACTIVA} ({@code gradeNotOperable}).
     */
    @Override
    public ApprenticeDTO enroll(EnrollApprenticeVM enrollApprenticeVM) {
        LOG.debug("Request to enroll Apprentice by document number : {}", enrollApprenticeVM.getDocumentNumber());

        Grade grade = gradeRepository
            .findById(enrollApprenticeVM.getGrade().getId())
            .orElseThrow(() -> new BadRequestAlertException("La ficha no existe", "grade", "idnotfound"));

        if (grade.getState() != StateGrade.PENDIENTE && grade.getState() != StateGrade.ACTIVA) {
            throw new BadRequestAlertException("La ficha no permite esta operación en su estado actual", ENTITY_NAME, "gradeNotOperable");
        }

        List<UserProfile> documentProfiles = userProfileRepository.findAllByDocumentNumber(enrollApprenticeVM.getDocumentNumber().trim());
        if (documentProfiles.size() != 1) {
            throw new BadRequestAlertException(
                "El número de documento no identifica a un único aprendiz",
                ENTITY_NAME,
                "apprenticeInactive"
            );
        }

        UserProfile student = documentProfiles.get(0);
        if (student.getUser() == null || !student.getUser().isActivated()) {
            throw new BadRequestAlertException("Aprendiz no existe o no está activo", ENTITY_NAME, "apprenticeInactive");
        }

        if (apprenticeRepository.existsByStudentIdAndGradeId(student.getId(), grade.getId())) {
            throw new BadRequestAlertException(
                "Este aprendiz ya tiene un registro en esta ficha",
                ENTITY_NAME,
                "apprenticeAlreadyEnrolled"
            );
        }

        Apprentice apprentice = new Apprentice().stateAcademic(StateAcademic.MATRICULADO).student(student).grade(grade);
        apprentice.setCreatedDate(Instant.now());
        Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
        if (currentUserLogin.isPresent()) {
            apprentice.setCreatedBy(currentUserLogin.get());
        }

        return apprenticeMapper.toDto(apprenticeRepository.save(apprentice));
    }

    /**
     * Unlinks the enrollment with the chosen reason (UC008, A1). The record is deleted only when
     * the apprentice has no attendance in the ficha; when attendance exists the record is kept
     * and its academic state becomes the reason, so the history is preserved.
     *
     * <p>Rejections: a missing record ({@code idnotfound}), a ficha that is not {@code PENDIENTE}
     * or {@code ACTIVA} ({@code gradeNotOperable}) and a reason that is not one of the withdrawal
     * reasons ({@code invalidunlinkreason}).
     */
    @Override
    public Optional<ApprenticeDTO> unlink(UnlinkApprenticeVM unlinkApprenticeVM) {
        LOG.debug("Request to unlink Apprentice : {} with reason {}", unlinkApprenticeVM.getId(), unlinkApprenticeVM.getReason());

        Apprentice apprentice = apprenticeRepository
            .findById(unlinkApprenticeVM.getId())
            .orElseThrow(() -> new BadRequestAlertException("El registro del aprendiz no existe", ENTITY_NAME, "idnotfound"));

        Grade grade = apprentice.getGrade();
        if (grade.getState() != StateGrade.PENDIENTE && grade.getState() != StateGrade.ACTIVA) {
            throw new BadRequestAlertException("La ficha no permite esta operación en su estado actual", ENTITY_NAME, "gradeNotOperable");
        }

        if (!UNLINK_REASONS.contains(unlinkApprenticeVM.getReason())) {
            throw new BadRequestAlertException("El motivo de desvinculación no es válido", ENTITY_NAME, "invalidunlinkreason");
        }

        if (!hasAttendanceInGrade(apprentice)) {
            apprenticeRepository.delete(apprentice);
            return Optional.empty();
        }

        apprentice.setStateAcademic(unlinkApprenticeVM.getReason());
        return Optional.of(apprenticeMapper.toDto(apprenticeRepository.save(apprentice)));
    }

    /**
     * Resolves whether the apprentice already has attendance in the ficha. The attendance records
     * live in the ficha's class sections, so the lookup goes from the ficha to its class sections
     * and then matches the student against them.
     *
     * @param apprentice the enrollment to check.
     * @return {@code true} when at least one attendance record links the apprentice to a class section of the ficha.
     */
    private boolean hasAttendanceInGrade(Apprentice apprentice) {
        UserProfile student = apprentice.getStudent();
        if (student == null) {
            return false;
        }

        List<ObjectId> classSectionIds = classSectionRepository
            .findByGradeId(apprentice.getGrade().getId())
            .stream()
            .map(ClassSection::getId)
            .filter(ApprenticeServiceImpl::isObjectId)
            .map(ObjectId::new)
            .distinct()
            .toList();

        return !classSectionIds.isEmpty() && attendanceRepository.existsByStudentIdAndClassSectionIdIn(student.getId(), classSectionIds);
    }

    /**
     * @param id the candidate id.
     * @return whether the id is a 24-hex string convertible into an {@code ObjectId}.
     */
    private static boolean isObjectId(String id) {
        return id != null && id.length() == 24 && ObjectId.isValid(id);
    }

    @Override
    public Page<ApprenticeDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Apprentices");
        return apprenticeRepository.findAll(pageable).map(apprenticeMapper::toDto);
    }

    public Page<ApprenticeDTO> findAllWithEagerRelationships(Pageable pageable) {
        return apprenticeRepository.findAllWithEagerRelationships(pageable).map(apprenticeMapper::toDto);
    }

    @Override
    public Optional<ApprenticeDTO> findOne(String id) {
        LOG.debug("Request to get Apprentice : {}", id);
        return apprenticeRepository.findOneWithEagerRelationships(id).map(apprenticeMapper::toDto);
    }
}
