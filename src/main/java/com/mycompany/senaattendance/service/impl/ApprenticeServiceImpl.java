package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.Apprentice;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.StateAcademic;
import com.mycompany.senaattendance.domain.enumeration.StateGrade;
import com.mycompany.senaattendance.repository.ApprenticeRepository;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.ApprenticeService;
import com.mycompany.senaattendance.service.dto.ApprenticeDTO;
import com.mycompany.senaattendance.service.mapper.ApprenticeMapper;
import com.mycompany.senaattendance.web.rest.errors.BadRequestAlertException;
import com.mycompany.senaattendance.web.rest.vm.EnrollApprenticeVM;
import java.time.Instant;
import java.util.Optional;
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

    private final ApprenticeRepository apprenticeRepository;

    private final GradeRepository gradeRepository;

    private final UserProfileRepository userProfileRepository;

    private final ApprenticeMapper apprenticeMapper;

    public ApprenticeServiceImpl(
        ApprenticeRepository apprenticeRepository,
        GradeRepository gradeRepository,
        UserProfileRepository userProfileRepository,
        ApprenticeMapper apprenticeMapper
    ) {
        this.apprenticeRepository = apprenticeRepository;
        this.gradeRepository = gradeRepository;
        this.userProfileRepository = userProfileRepository;
        this.apprenticeMapper = apprenticeMapper;
    }

    /**
     * Enrolls the apprentice in the ficha (UC008). The academic state is set by the server as
     * {@code MATRICULADO}, so a state sent by the client is never persisted.
     *
     * <p>Rejections: a missing or deactivated apprentice account ({@code apprenticeInactive}),
     * an apprentice that already has a record in the ficha in any state
     * ({@code apprenticeAlreadyEnrolled}) and a ficha that is not {@code PENDIENTE} or
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

        UserProfile student = userProfileRepository
            .findByDocumentNumber(enrollApprenticeVM.getDocumentNumber().trim())
            .filter(profile -> profile.getUser() != null && profile.getUser().isActivated())
            .orElseThrow(() -> new BadRequestAlertException("Aprendiz no existe o no está activo", ENTITY_NAME, "apprenticeInactive"));

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
