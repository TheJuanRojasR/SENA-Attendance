package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.Program;
import com.mycompany.senaattendance.domain.enumeration.StateGrade;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.repository.ProgramRepository;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.GradeService;
import com.mycompany.senaattendance.service.dto.GradeDTO;
import com.mycompany.senaattendance.service.mapper.GradeMapper;
import com.mycompany.senaattendance.web.rest.errors.BadRequestAlertException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.mycompany.senaattendance.domain.Grade}.
 */
@Service
public class GradeServiceImpl implements GradeService {

    private static final Logger LOG = LoggerFactory.getLogger(GradeServiceImpl.class);

    private final GradeRepository gradeRepository;

    private final GradeMapper gradeMapper;

    private final ProgramRepository programRepository;

    private final Clock clock;

    public GradeServiceImpl(GradeRepository gradeRepository, GradeMapper gradeMapper, ProgramRepository programRepository, Clock clock) {
        this.gradeRepository = gradeRepository;
        this.gradeMapper = gradeMapper;
        this.programRepository = programRepository;
        this.clock = clock;
    }

    @Override
    public GradeDTO save(GradeDTO gradeDTO) {
        LOG.debug("Request to save Grade : {}", gradeDTO);
        Grade grade = gradeMapper.toEntity(gradeDTO);

        validateProgramActiveForGrade(grade.getProgram());

        // The state is always derived from the dates; any state sent by the client is ignored.
        grade.setState(classifyState(LocalDate.now(clock), grade.getStartDate(), grade.getEndDate()));

        // Inserta fecha de creación
        grade.setCreatedDate(Instant.now());
        Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
        if (currentUserLogin.isPresent()) {
            // Inserta quien lo creo
            grade.setCreatedBy(currentUserLogin.get());
        }

        grade = gradeRepository.save(grade);
        return gradeMapper.toDto(grade);
    }

    @Override
    public GradeDTO update(GradeDTO gradeDTO) {
        LOG.debug("Request to update Grade : {}", gradeDTO);
        Grade grade = gradeMapper.toEntity(gradeDTO);

        Optional<Grade> optionalGrade = gradeRepository.findById(grade.getId());
        if (optionalGrade.isPresent()) {
            Grade existingGrade = optionalGrade.get();
            grade.setCreatedBy(existingGrade.getCreatedBy());
            grade.setCreatedDate(existingGrade.getCreatedDate());
            grade.setState(resolveState(existingGrade.getState(), LocalDate.now(clock), grade.getStartDate(), grade.getEndDate()));
        } else {
            grade.setState(classifyState(LocalDate.now(clock), grade.getStartDate(), grade.getEndDate()));
            grade.setCreatedDate(Instant.now());
            Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
            if (currentUserLogin.isPresent()) {
                grade.setCreatedBy(currentUserLogin.get());
            }
        }

        grade = gradeRepository.save(grade);
        return gradeMapper.toDto(grade);
    }

    @Override
    public Optional<GradeDTO> partialUpdate(GradeDTO gradeDTO) {
        LOG.debug("Request to partially update Grade : {}", gradeDTO);

        return gradeRepository
            .findById(gradeDTO.getId())
            .map(existingGrade -> {
                StateGrade currentState = existingGrade.getState();
                gradeMapper.partialUpdate(existingGrade, gradeDTO);
                existingGrade.setState(
                    resolveState(currentState, LocalDate.now(clock), existingGrade.getStartDate(), existingGrade.getEndDate())
                );

                return existingGrade;
            })
            .map(gradeRepository::save)
            .map(gradeMapper::toDto);
    }

    @Override
    public Page<GradeDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Grades");
        return gradeRepository.findAll(pageable).map(gradeMapper::toDto);
    }

    public Page<GradeDTO> findAllWithEagerRelationships(Pageable pageable) {
        return gradeRepository.findAllWithEagerRelationships(pageable).map(gradeMapper::toDto);
    }

    @Override
    public Optional<GradeDTO> findOne(String id) {
        LOG.debug("Request to get Grade : {}", id);
        return gradeRepository.findOneWithEagerRelationships(id).map(gradeMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete Grade : {}", id);
        gradeRepository.deleteById(id);
    }

    // --------------------------- New methods ---------------------------

    @Override
    public List<GradeDTO> findActiveGrades() {
        LOG.debug("Request to get all active Grades");

        // 1. Traer todos los grades
        List<Grade> allGrades = gradeRepository.findAllWithEagerRelationships();

        // 2. Filtrar por estado ACTIVA
        return allGrades
            .stream()
            .filter(grade -> grade.getState() != null && grade.getState().equals(StateGrade.ACTIVA))
            .map(gradeMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Daily job that keeps each ficha state in sync with today versus its
     * {@code [startDate, endDate]} range. Manually set states ({@code APLAZADA},
     * {@code CANCELADA}) are skipped; each loaded entity is saved as-is so its
     * {@code createdBy}/{@code createdDate} audit fields are preserved.
     */
    @Override
    @Scheduled(cron = "0 0 1 * * ?")
    public void syncStates() {
        LocalDate today = LocalDate.now(clock);
        gradeRepository.findAll().forEach(grade -> {
            if (isManualState(grade.getState())) {
                return;
            }
            StateGrade computed = classifyState(today, grade.getStartDate(), grade.getEndDate());
            if (grade.getState() != computed) {
                grade.setState(computed);
                gradeRepository.save(grade);
            }
        });
    }

    /**
     * Classifies a ficha as {@code PENDIENTE} ({@code start > today}), {@code FINALIZADA}
     * ({@code end < today}) or {@code ACTIVA} ({@code today ∈ [start, end]}).
     *
     * @param today the reference day.
     * @param start the ficha start date (inclusive).
     * @param end the ficha end date (inclusive).
     * @return the automatic state.
     */
    private StateGrade classifyState(LocalDate today, LocalDate start, LocalDate end) {
        if (start.isAfter(today)) {
            return StateGrade.PENDIENTE;
        }
        if (end.isBefore(today)) {
            return StateGrade.FINALIZADA;
        }
        return StateGrade.ACTIVA;
    }

    /**
     * Keeps a manually set state while the ficha is edited; any other state is re-derived
     * from the given date range.
     */
    private StateGrade resolveState(StateGrade currentState, LocalDate today, LocalDate start, LocalDate end) {
        if (isManualState(currentState)) {
            return currentState;
        }
        return classifyState(today, start, end);
    }

    /**
     * @param state a ficha state, possibly {@code null}.
     * @return whether the state is set by an administrator instead of derived from dates.
     */
    private static boolean isManualState(StateGrade state) {
        return state == StateGrade.APLAZADA || state == StateGrade.CANCELADA;
    }

    /**
     * A ficha (grade) can only be created for an ACTIVE program. If the referenced
     * program exists and is inactive, creating a new ficha under it is rejected (A2).
     * A reference to a program that is not persisted is left untouched.
     *
     * @param program the program referenced by the new grade.
     */
    private void validateProgramActiveForGrade(Program program) {
        if (program == null || program.getId() == null) {
            return;
        }
        programRepository.findById(program.getId()).ifPresent(existing -> {
            if (Boolean.FALSE.equals(existing.getStatus())) {
                throw new BadRequestAlertException("No se pueden crear fichas para un programa inactivo", "program", "programInactive");
            }
        });
    }
}
