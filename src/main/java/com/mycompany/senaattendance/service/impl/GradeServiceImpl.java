package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.ClassException;
import com.mycompany.senaattendance.domain.ClassSchedule;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.Modality;
import com.mycompany.senaattendance.domain.Program;
import com.mycompany.senaattendance.domain.TimeSlot;
import com.mycompany.senaattendance.domain.enumeration.StateGrade;
import com.mycompany.senaattendance.repository.ApprenticeRepository;
import com.mycompany.senaattendance.repository.AttendanceRepository;
import com.mycompany.senaattendance.repository.ClassExceptionRepository;
import com.mycompany.senaattendance.repository.ClassScheduleRepository;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.repository.ModalityRepository;
import com.mycompany.senaattendance.repository.ProgramRepository;
import com.mycompany.senaattendance.repository.TimeSlotRepository;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.GradeService;
import com.mycompany.senaattendance.service.dto.GradeDTO;
import com.mycompany.senaattendance.service.mapper.GradeMapper;
import com.mycompany.senaattendance.web.rest.errors.BadRequestAlertException;
import com.mycompany.senaattendance.web.rest.errors.GradeCodeAlreadyUsedException;
import com.mycompany.senaattendance.web.rest.errors.GradeDatesOrderException;
import com.mycompany.senaattendance.web.rest.errors.GradeStartDateInPastException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.senaattendance.domain.Grade}.
 */
@Service
public class GradeServiceImpl implements GradeService {

    private static final Logger LOG = LoggerFactory.getLogger(GradeServiceImpl.class);

    private static final String ENTITY_NAME = "grade";

    // Fields an ACTIVA ficha accepts; any other editable field is locked by its state.
    private static final Set<String> ACTIVA_EDITABLE_FIELDS = Set.of("endDate", "program", "code");

    // Fields an APLAZADA ficha accepts; any other editable field is locked by its state.
    private static final Set<String> APLAZADA_EDITABLE_FIELDS = Set.of("endDate");

    private final GradeRepository gradeRepository;

    private final GradeMapper gradeMapper;

    private final ProgramRepository programRepository;

    private final ModalityRepository modalityRepository;

    private final TimeSlotRepository timeSlotRepository;

    private final ClassSectionRepository classSectionRepository;

    private final ApprenticeRepository apprenticeRepository;

    private final AttendanceRepository attendanceRepository;

    private final ClassScheduleRepository classScheduleRepository;

    private final ClassExceptionRepository classExceptionRepository;

    private final Clock clock;

    public GradeServiceImpl(
        GradeRepository gradeRepository,
        GradeMapper gradeMapper,
        ProgramRepository programRepository,
        ModalityRepository modalityRepository,
        TimeSlotRepository timeSlotRepository,
        ClassSectionRepository classSectionRepository,
        ApprenticeRepository apprenticeRepository,
        AttendanceRepository attendanceRepository,
        ClassScheduleRepository classScheduleRepository,
        ClassExceptionRepository classExceptionRepository,
        Clock clock
    ) {
        this.gradeRepository = gradeRepository;
        this.gradeMapper = gradeMapper;
        this.programRepository = programRepository;
        this.modalityRepository = modalityRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.classSectionRepository = classSectionRepository;
        this.apprenticeRepository = apprenticeRepository;
        this.attendanceRepository = attendanceRepository;
        this.classScheduleRepository = classScheduleRepository;
        this.classExceptionRepository = classExceptionRepository;
        this.clock = clock;
    }

    @Override
    public GradeDTO save(GradeDTO gradeDTO) {
        LOG.debug("Request to save Grade : {}", gradeDTO);
        Grade grade = gradeMapper.toEntity(gradeDTO);

        validateCode(grade.getCode(), null);
        validateDates(grade.getStartDate(), grade.getEndDate(), true);
        validateActiveCatalogs(grade.getProgram(), grade.getModality(), grade.getTimeSlot());

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
        // The persisted state decides which fields the update may touch, before any other rule.
        Set<String> changedFields = optionalGrade.map(existingGrade -> validateStateEditRules(existingGrade, gradeDTO)).orElseGet(Set::of);
        // When the ficha already exists, its own id is excluded from the uniqueness check.
        validateCode(grade.getCode(), optionalGrade.isPresent() ? grade.getId() : null);
        if (changedFields.contains("code")) {
            validateCodeLock(grade.getId());
        }
        // A ficha keeps its original start date when only other fields change, so the "not in
        // the past" rule applies only when the start date is actually being set or changed.
        LocalDate incomingStartDate = grade.getStartDate();
        boolean startDateChanged = optionalGrade
            .map(existingGrade -> !Objects.equals(existingGrade.getStartDate(), incomingStartDate))
            .orElse(true);
        validateDates(grade.getStartDate(), grade.getEndDate(), startDateChanged);
        validateActiveCatalogs(grade.getProgram(), grade.getModality(), grade.getTimeSlot());

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
                // Only the fields present in the payload are compared, so a PATCH that omits a
                // field never violates the state rules.
                Set<String> changedFields = validateStateEditRules(existingGrade, gradeDTO);
                validateCode(gradeDTO.getCode(), existingGrade.getId());
                if (changedFields.contains("code")) {
                    validateCodeLock(existingGrade.getId());
                }
                LocalDate persistedStartDate = existingGrade.getStartDate();
                StateGrade currentState = existingGrade.getState();
                gradeMapper.partialUpdate(existingGrade, gradeDTO);
                boolean startDateChanged = !Objects.equals(persistedStartDate, existingGrade.getStartDate());
                validateDates(existingGrade.getStartDate(), existingGrade.getEndDate(), startDateChanged);
                validateActiveCatalogs(existingGrade.getProgram(), existingGrade.getModality(), existingGrade.getTimeSlot());
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

    /**
     * Deletes a ficha only when it has neither apprentices nor attendance records; a ficha in
     * use must be cancelled instead. When the guard passes, the ficha disappears with all of
     * its information: its class sections are deleted together with their schedules and
     * exceptions. A missing ficha is still a silent no-op.
     */
    @Override
    @Transactional
    public void delete(String id) {
        LOG.debug("Request to delete Grade : {}", id);
        List<ClassSection> classSections = classSectionRepository.findByGradeId(id);
        if (!apprenticeRepository.findByGradeId(id).isEmpty() || hasAttendance(classSections)) {
            throw new BadRequestAlertException(
                "No es posible eliminar la ficha: tiene aprendices vinculados y/o registros de asistencia. Si desea retirarla de operación, use Cancelar ficha",
                ENTITY_NAME,
                "gradeInUse"
            );
        }
        cascadeDeleteClassSections(classSections);
        gradeRepository.deleteById(id);
    }

    /**
     * Deletes the ficha's class sections along with their schedules and exceptions.
     *
     * @param classSections the ficha's class sections.
     */
    private void cascadeDeleteClassSections(List<ClassSection> classSections) {
        classSections.forEach(classSection -> {
            classScheduleRepository.deleteAll(classScheduleRepository.findByClassSectionId(classSection.getId()));
            classExceptionRepository.deleteAll(classExceptionRepository.findByClassSectionId(classSection.getId()));
        });
        if (!classSections.isEmpty()) {
            classSectionRepository.deleteAll(classSections);
        }
    }

    /**
     * Resolves the multi-hop path from a ficha to its attendance records: the ficha's class
     * sections are mapped to {@link ObjectId} and counted, the same way
     * {@code TrimesterServiceImpl.hasAttendance} does for a trimester.
     *
     * @param classSections the ficha's class sections.
     * @return {@code true} when at least one attendance record exists in the class sections.
     */
    private boolean hasAttendance(List<ClassSection> classSections) {
        List<ObjectId> classSectionIds = classSections
            .stream()
            .map(ClassSection::getId)
            .filter(GradeServiceImpl::isObjectId)
            .map(ObjectId::new)
            .distinct()
            .toList();
        return !classSectionIds.isEmpty() && attendanceRepository.countByClassSection_IdIn(classSectionIds) > 0;
    }

    /**
     * @param id the candidate id string.
     * @return whether the string is a valid 24-hex {@code ObjectId} string.
     */
    private static boolean isObjectId(String id) {
        return id != null && ObjectId.isValid(id);
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
     * Postpones a ficha that has not started or is active. The transition is evaluated
     * against the persisted state, and no field edit rules or date/catalog validations run.
     */
    @Override
    @Transactional
    public GradeDTO postpone(String id) {
        LOG.debug("Request to postpone Grade : {}", id);
        Grade grade = findGradeForAction(id);
        if (grade.getState() != StateGrade.PENDIENTE && grade.getState() != StateGrade.ACTIVA) {
            throw new BadRequestAlertException("La ficha no se puede aplazar en su estado actual", ENTITY_NAME, "invalidtransition");
        }
        grade.setState(StateGrade.APLAZADA);
        return gradeMapper.toDto(gradeRepository.save(grade));
    }

    /**
     * Resumes a postponed ficha, recomputing its state from the date range the same way the
     * daily job does.
     */
    @Override
    @Transactional
    public GradeDTO resume(String id) {
        LOG.debug("Request to resume Grade : {}", id);
        Grade grade = findGradeForAction(id);
        if (grade.getState() != StateGrade.APLAZADA) {
            throw new BadRequestAlertException("La ficha no se puede reanudar en su estado actual", ENTITY_NAME, "invalidtransition");
        }
        grade.setState(classifyState(LocalDate.now(clock), grade.getStartDate(), grade.getEndDate()));
        return gradeMapper.toDto(gradeRepository.save(grade));
    }

    /**
     * Cancels a ficha that is not already cancelled. Cancellation is definitive and blocks
     * every later operation on the ficha.
     */
    @Override
    @Transactional
    public GradeDTO cancel(String id) {
        LOG.debug("Request to cancel Grade : {}", id);
        Grade grade = findGradeForAction(id);
        if (grade.getState() == StateGrade.CANCELADA) {
            throw new BadRequestAlertException("La ficha ya está cancelada", ENTITY_NAME, "invalidtransition");
        }
        grade.setState(StateGrade.CANCELADA);
        return gradeMapper.toDto(gradeRepository.save(grade));
    }

    /**
     * @param id the ficha id.
     * @return the persisted ficha.
     * @throws BadRequestAlertException with key {@code idnotfound} when no ficha matches the id.
     */
    private Grade findGradeForAction(String id) {
        return gradeRepository.findById(id).orElseThrow(() -> new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
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
     * Validates the ficha code: it must contain digits only and must not be used by
     * another ficha. A {@code null} code is skipped, so a PATCH that does not touch
     * the code is unaffected.
     *
     * @param code      the code to validate, possibly {@code null}.
     * @param excludeId the id excluded from the uniqueness check, or {@code null} when
     *                  no ficha can own the code yet (creation).
     * @throws BadRequestAlertException      with key {@code codenotnumeric} when the
     *                                       code contains non-digit characters.
     * @throws GradeCodeAlreadyUsedException if another ficha already uses the code.
     */
    private void validateCode(String code, String excludeId) {
        if (code == null) {
            return;
        }
        if (!code.matches("\\d+")) {
            throw new BadRequestAlertException("El código debe contener solo números", ENTITY_NAME, "codenotnumeric");
        }
        boolean duplicate = excludeId == null ? gradeRepository.existsByCode(code) : gradeRepository.existsByCodeAndIdNot(code, excludeId);
        if (duplicate) {
            throw new GradeCodeAlreadyUsedException();
        }
    }

    /**
     * Enforces the per-state ficha edit rules against the state the ficha has persisted. A
     * field counts as changed only when the payload carries a different value, so resending
     * an unchanged field is accepted even when the state locks it.
     *
     * @param existingGrade the persisted ficha whose state rules apply; the state sent by the
     *                      client is ignored.
     * @param gradeDTO      the incoming payload: on a PUT every field is present, on a PATCH
     *                      only the fields being modified.
     * @return the editable fields whose incoming value differs from the persisted one.
     * @throws BadRequestAlertException with key {@code noteditable} when a FINALIZADA ficha
     *                                  changes any field.
     * @throws BadRequestAlertException with key {@code fieldlocked} when an ACTIVA or
     *                                  APLAZADA ficha changes a field its state does not allow.
     */
    private Set<String> validateStateEditRules(Grade existingGrade, GradeDTO gradeDTO) {
        Set<String> changedFields = changedEditableFields(existingGrade, gradeDTO);
        switch (existingGrade.getState()) {
            case FINALIZADA -> {
                if (!changedFields.isEmpty()) {
                    throw new BadRequestAlertException("No se puede modificar una ficha finalizada", ENTITY_NAME, "noteditable");
                }
            }
            case ACTIVA -> {
                if (changedFields.stream().anyMatch(field -> !ACTIVA_EDITABLE_FIELDS.contains(field))) {
                    throw fieldLocked();
                }
            }
            case APLAZADA -> {
                if (changedFields.stream().anyMatch(field -> !APLAZADA_EDITABLE_FIELDS.contains(field))) {
                    throw fieldLocked();
                }
            }
            default -> {
                // PENDIENTE and CANCELADA accept changes on every editable field.
            }
        }
        return changedFields;
    }

    /**
     * @return the editable ficha fields whose incoming value differs from the persisted one.
     *         A field absent from the payload is never reported as changed.
     */
    private static Set<String> changedEditableFields(Grade existingGrade, GradeDTO gradeDTO) {
        Set<String> changedFields = new HashSet<>();
        if (gradeDTO.getCode() != null && !Objects.equals(existingGrade.getCode(), gradeDTO.getCode())) {
            changedFields.add("code");
        }
        if (gradeDTO.getStartDate() != null && !Objects.equals(existingGrade.getStartDate(), gradeDTO.getStartDate())) {
            changedFields.add("startDate");
        }
        if (gradeDTO.getEndDate() != null && !Objects.equals(existingGrade.getEndDate(), gradeDTO.getEndDate())) {
            changedFields.add("endDate");
        }
        if (gradeDTO.getProgram() != null && !Objects.equals(referenceId(existingGrade.getProgram()), gradeDTO.getProgram().getId())) {
            changedFields.add("program");
        }
        if (gradeDTO.getModality() != null && !Objects.equals(referenceId(existingGrade.getModality()), gradeDTO.getModality().getId())) {
            changedFields.add("modality");
        }
        if (gradeDTO.getTimeSlot() != null && !Objects.equals(referenceId(existingGrade.getTimeSlot()), gradeDTO.getTimeSlot().getId())) {
            changedFields.add("timeSlot");
        }
        return changedFields;
    }

    /**
     * The ficha code can only change while no class section nor apprentice references the
     * ficha, so existing academic records keep pointing at a stable code.
     *
     * @param gradeId the ficha whose code is being changed.
     * @throws BadRequestAlertException with key {@code gradeCodeLocked} when the ficha already
     *                                  has class sections or apprentices.
     */
    private void validateCodeLock(String gradeId) {
        boolean hasClassSections = !classSectionRepository.findByGradeId(gradeId).isEmpty();
        boolean hasApprentices = !apprenticeRepository.findByGradeId(gradeId).isEmpty();
        if (hasClassSections || hasApprentices) {
            throw new BadRequestAlertException(
                "El código solo puede cambiarse mientras la ficha no tenga materias ni aprendices",
                ENTITY_NAME,
                "gradeCodeLocked"
            );
        }
    }

    private static BadRequestAlertException fieldLocked() {
        return new BadRequestAlertException("El campo no se puede modificar en el estado actual de la ficha", ENTITY_NAME, "fieldlocked");
    }

    private static String referenceId(Program program) {
        return program == null ? null : program.getId();
    }

    private static String referenceId(Modality modality) {
        return modality == null ? null : modality.getId();
    }

    private static String referenceId(TimeSlot timeSlot) {
        return timeSlot == null ? null : timeSlot.getId();
    }

    /**
     * Validates the resulting date range of a ficha: the end date can never be before the
     * start date, and a start date in the past is rejected only when it is being set or
     * changed. That way an already started ficha can still be edited without moving its start.
     *
     * @param startDate        the resulting start date.
     * @param endDate          the resulting end date.
     * @param startDateChanged whether the start date differs from the persisted one (always {@code true} on creation).
     * @throws GradeDatesOrderException      if the end date is before the start date.
     * @throws GradeStartDateInPastException if the start date is being set to a past day.
     */
    private void validateDates(LocalDate startDate, LocalDate endDate, boolean startDateChanged) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new GradeDatesOrderException();
        }
        if (startDateChanged && startDate != null && startDate.isBefore(LocalDate.now(clock))) {
            throw new GradeStartDateInPastException();
        }
    }

    /**
     * Validates that the referenced program, modality and time slot are still active, so a
     * ficha cannot be created or updated against a catalog entry disabled in the meantime.
     */
    private void validateActiveCatalogs(Program program, Modality modality, TimeSlot timeSlot) {
        validateProgramActiveForGrade(program);
        validateModalityActiveForGrade(modality);
        validateTimeSlotActiveForGrade(timeSlot);
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

    /**
     * A ficha can only be saved for an ACTIVE modality. If the referenced modality exists
     * and is inactive, saving is rejected (E3). A reference to a modality that is not
     * persisted is left untouched.
     */
    private void validateModalityActiveForGrade(Modality modality) {
        if (modality == null || modality.getId() == null) {
            return;
        }
        modalityRepository.findById(modality.getId()).ifPresent(existing -> {
            if (Boolean.FALSE.equals(existing.getIsActive())) {
                throw new BadRequestAlertException("No se pueden crear fichas para una modalidad inactiva", "modality", "modalityInactive");
            }
        });
    }

    /**
     * A ficha can only be saved for an ACTIVE time slot. If the referenced time slot exists
     * and is inactive, saving is rejected (E3). A reference to a time slot that is not
     * persisted is left untouched.
     */
    private void validateTimeSlotActiveForGrade(TimeSlot timeSlot) {
        if (timeSlot == null || timeSlot.getId() == null) {
            return;
        }
        timeSlotRepository.findById(timeSlot.getId()).ifPresent(existing -> {
            if (Boolean.FALSE.equals(existing.getIsActive())) {
                throw new BadRequestAlertException("No se pueden crear fichas para una jornada inactiva", "timeSlot", "timeSlotInactive");
            }
        });
    }
}
