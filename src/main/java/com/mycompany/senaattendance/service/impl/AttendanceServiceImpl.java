package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.Apprentice;
import com.mycompany.senaattendance.domain.Attendance;
import com.mycompany.senaattendance.domain.AuditLog;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.StateAcademic;
import com.mycompany.senaattendance.domain.enumeration.StateAttendance;
import com.mycompany.senaattendance.domain.enumeration.StateTrimester;
import com.mycompany.senaattendance.repository.ApprenticeRepository;
import com.mycompany.senaattendance.repository.AttendanceRepository;
import com.mycompany.senaattendance.repository.AttendanceSearchCriteria;
import com.mycompany.senaattendance.repository.AuditLogRepository;
import com.mycompany.senaattendance.repository.ClassExceptionRepository;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.TrimesterRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.AttendanceService;
import com.mycompany.senaattendance.service.TrimesterService;
import com.mycompany.senaattendance.service.dto.AttendanceDTO;
import com.mycompany.senaattendance.service.dto.AttendanceSessionDTO;
import com.mycompany.senaattendance.service.mapper.AttendanceMapper;
import com.mycompany.senaattendance.web.rest.errors.BadRequestAlertException;
import com.mycompany.senaattendance.web.rest.vm.AttendanceConfirmationVM;
import com.mycompany.senaattendance.web.rest.vm.AttendanceSessionVM;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.mycompany.senaattendance.domain.Attendance}.
 */
@Service
public class AttendanceServiceImpl implements AttendanceService {

    private static final Logger LOG = LoggerFactory.getLogger(AttendanceServiceImpl.class);

    private static final String ENTITY_NAME = "attendance";

    /**
     * Orders the records of a session by apprentice document number, so the response is stable
     * across calls. A record without a resolved student sorts first.
     */
    private static final Comparator<Attendance> RECORDS_BY_STUDENT_DOCUMENT = Comparator.comparing(attendance ->
        attendance.getStudent() != null && attendance.getStudent().getDocumentNumber() != null
            ? attendance.getStudent().getDocumentNumber()
            : ""
    );

    private final AttendanceRepository attendanceRepository;

    private final AttendanceMapper attendanceMapper;

    private final ClassSectionRepository classSectionRepository;

    private final ClassExceptionRepository classExceptionRepository;

    private final ApprenticeRepository apprenticeRepository;

    private final TrimesterRepository trimesterRepository;

    private final TrimesterService trimesterService;

    private final UserRepository userRepository;

    private final UserProfileRepository userProfileRepository;

    private final AuditLogRepository auditLogRepository;

    private final Clock clock;

    public AttendanceServiceImpl(
        AttendanceRepository attendanceRepository,
        AttendanceMapper attendanceMapper,
        ClassSectionRepository classSectionRepository,
        ClassExceptionRepository classExceptionRepository,
        ApprenticeRepository apprenticeRepository,
        TrimesterRepository trimesterRepository,
        TrimesterService trimesterService,
        UserRepository userRepository,
        UserProfileRepository userProfileRepository,
        AuditLogRepository auditLogRepository,
        Clock clock
    ) {
        this.attendanceRepository = attendanceRepository;
        this.attendanceMapper = attendanceMapper;
        this.classSectionRepository = classSectionRepository;
        this.classExceptionRepository = classExceptionRepository;
        this.apprenticeRepository = apprenticeRepository;
        this.trimesterRepository = trimesterRepository;
        this.trimesterService = trimesterService;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.auditLogRepository = auditLogRepository;
        this.clock = clock;
    }

    /**
     * Registers the attendance session of one class section on a session date (UC009). The
     * session is upserted mark by mark, keyed by materia, aprendiz and fecha; the apprentices
     * left out of the payload stay without a record, which is what makes a session incomplete
     * (A5). Every rule is evaluated before writing, so a rejected session persists nothing. A
     * mark that changes the state of an existing record is recorded in the audit log.
     *
     * @param attendanceSessionVM the materia, the session date and the confirmed marks.
     * @return the persisted session with its records and the derived completeness.
     * @throws BadRequestAlertException when the materia, the instructor, the date or a mark
     *         violates the UC009 rules.
     */
    @Override
    public AttendanceSessionDTO saveSession(AttendanceSessionVM attendanceSessionVM) {
        LOG.debug("Request to save an Attendance session : {}", attendanceSessionVM);

        ClassSection classSection = resolveClassSection(attendanceSessionVM);
        validateAssignedInstructor(classSection);
        validateSessionDate(attendanceSessionVM.getDate(), classSection);

        List<Apprentice> enrolledApprentices = findEnrolledApprentices(classSection);
        if (enrolledApprentices.isEmpty()) {
            throw new BadRequestAlertException("No hay aprendices activos en esta ficha", ENTITY_NAME, "noActiveApprentices");
        }

        Map<String, Apprentice> enrolledByStudentId = enrolledApprenticesByStudentId(enrolledApprentices);
        validateConfirmations(attendanceSessionVM.getAttendances(), enrolledByStudentId);
        upsertConfirmations(attendanceSessionVM, classSection, enrolledByStudentId);

        return buildSession(classSection, attendanceSessionVM.getDate(), enrolledApprentices.size());
    }

    /**
     * Edits the state of one attendance record (A2). The record must belong to a materia assigned
     * to the current instructor, the trimester of the session date must still be active, and only
     * Presente or Falla are accepted because Justificada arrives through an approved justification
     * (UC010). Everything but the state of the record is preserved, and a real change of state is
     * recorded in the audit log.
     *
     * @param id the id of the record to edit.
     * @param stateAttendance the new state.
     * @return the persisted record, or empty when it does not exist.
     * @throws BadRequestAlertException when the current user is not the assigned instructor, when
     *         the state is not editable or when the trimester is already closed.
     */
    @Override
    public Optional<AttendanceDTO> updateState(String id, StateAttendance stateAttendance) {
        LOG.debug("Request to edit the state of Attendance : {}, {}", id, stateAttendance);
        return attendanceRepository.findById(id).map(attendance -> {
            validateAssignedInstructor(attendance.getClassSection());
            validateEditableState(stateAttendance);
            validateActiveTrimester(attendance.getDate());

            StateAttendance previousState = attendance.getStateAttendance();
            attendance.setStateAttendance(stateAttendance);
            Attendance savedAttendance = attendanceRepository.save(attendance);
            recordStateChange(savedAttendance, previousState, stateAttendance, attendance.getClassSection().getInstructor());
            return attendanceMapper.toDto(savedAttendance);
        });
    }

    /**
     * Reads the attendance history (A1) with the optional filters combined into one query. An
     * administrator reads every record; an instructor reads only the records of the materias
     * assigned to them, and an apprentice reads only their own records. A filter outside the
     * readable scope resolves as an empty page.
     *
     * @param classSectionId the materia to filter by (may be null for every materia).
     * @param date the session date to filter by (may be null for every date).
     * @param studentId the apprentice profile id to filter by (may be null for every apprentice).
     * @param stateAttendance the state to filter by (may be null for every state).
     * @param pageable the pagination information.
     * @return the page of readable records matching the filters.
     */
    @Override
    public Page<AttendanceDTO> findAllForCurrentUser(
        String classSectionId,
        LocalDate date,
        String studentId,
        StateAttendance stateAttendance,
        Pageable pageable
    ) {
        LOG.debug("Request to get the page of Attendances the current user can read");
        List<ObjectId> classSectionScope = null;
        String studentScope = null;
        if (!isCurrentUserAdmin()) {
            if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.INSTRUCTOR)) {
                classSectionScope = currentInstructorClassSectionIds();
            } else {
                studentScope = currentUserProfileId();
                if (studentScope == null) {
                    return Page.empty(pageable);
                }
            }
        }
        AttendanceSearchCriteria criteria = new AttendanceSearchCriteria(classSectionId, date, studentId, stateAttendance);
        return attendanceRepository
            .searchAttendanceHistory(criteria, classSectionScope, studentScope, pageable)
            .map(attendanceMapper::toDto);
    }

    @Override
    public Optional<AttendanceDTO> findOneForCurrentUser(String id) {
        LOG.debug("Request to get Attendance : {}", id);
        return attendanceRepository.findOneWithEagerRelationships(id).filter(this::isReadableByCurrentUser).map(attendanceMapper::toDto);
    }

    /**
     * @return whether the current user is an administrator, who reads every attendance record.
     */
    private static boolean isCurrentUserAdmin() {
        return SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
    }

    /**
     * Resolves whether the current user can read the record: an administrator reads everything,
     * an apprentice only their own records, and an instructor only the records of the materias
     * assigned to them.
     *
     * @param attendance the record to read.
     * @return whether the record is inside the readable scope of the current user.
     */
    private boolean isReadableByCurrentUser(Attendance attendance) {
        if (isCurrentUserAdmin()) {
            return true;
        }

        String currentProfileId = currentUserProfileId();
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.APPRENTICE)) {
            return isOwnStudentRecord(attendance, currentProfileId);
        }

        ClassSection classSection = attendance.getClassSection();
        if (classSection == null || classSection.getInstructor() == null) {
            return false;
        }
        return currentProfileId != null && currentProfileId.equals(classSection.getInstructor().getId());
    }

    /**
     * @param attendance the record to check.
     * @param profileId the profile id of the current user, or {@code null}.
     * @return whether the record belongs to that apprentice profile.
     */
    private static boolean isOwnStudentRecord(Attendance attendance, String profileId) {
        return attendance.getStudent() != null && profileId != null && profileId.equals(attendance.getStudent().getId());
    }

    /**
     * Resolves the materias assigned to the current instructor. A user without a resolvable
     * profile or without assigned materias reads no records.
     *
     * @return the ObjectId values of the assigned materias, possibly empty.
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
            .filter(AttendanceServiceImpl::isObjectId)
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
     * Rejects a state the instructor cannot set on a record: it only exists as Presente or Falla,
     * and Justificada only arrives through an approved justification (UC010).
     *
     * @param stateAttendance the requested state.
     * @throws BadRequestAlertException when the state is not Presente or Falla.
     */
    private static void validateEditableState(StateAttendance stateAttendance) {
        if (stateAttendance != StateAttendance.PRESENTE && stateAttendance != StateAttendance.FALLA) {
            throw new BadRequestAlertException(
                "La asistencia solo se puede editar como Presente o Falla",
                ENTITY_NAME,
                "invalidAttendanceState"
            );
        }
    }

    /**
     * Resolves the materia of the session, which is the entry point of every other rule.
     *
     * @param attendanceSessionVM the requested session.
     * @return the persisted class section.
     * @throws BadRequestAlertException when no class section matches the requested id.
     */
    private ClassSection resolveClassSection(AttendanceSessionVM attendanceSessionVM) {
        String classSectionId = attendanceSessionVM.getClassSection().getId();
        return classSectionRepository
            .findById(classSectionId)
            .orElseThrow(() -> new BadRequestAlertException("La materia no existe", ENTITY_NAME, "idnotfound"));
    }

    /**
     * Rejects a session requested by anyone but the instructor assigned to the materia (E6).
     * A materia without instructor is not available for taking attendance, and an instructor
     * without a resolvable profile cannot be the assigned one.
     *
     * @param classSection the materia of the session.
     * @throws BadRequestAlertException when the materia has no instructor or the current user is
     *         not that instructor.
     */
    private void validateAssignedInstructor(ClassSection classSection) {
        UserProfile instructor = classSection.getInstructor();
        if (instructor == null) {
            throw new BadRequestAlertException(
                "Esta materia no tiene instructor asignado. Contacta al Administrador",
                ENTITY_NAME,
                "classSectionWithoutInstructor"
            );
        }

        String currentProfileId = currentUserProfileId();
        if (currentProfileId == null || !currentProfileId.equals(instructor.getId())) {
            throw new BadRequestAlertException(
                "Solo el instructor asignado a la materia puede registrar su asistencia",
                ENTITY_NAME,
                "notYourClassSection"
            );
        }
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

    /**
     * Validates the session date against the current day, the trimester that contains it, the
     * ficha range and the non-teaching exceptions of the materia. The order of the checks makes
     * the most specific situation win: a future date reports as future even when it also falls
     * outside the active trimester, and a closed trimester blocks its whole range before the
     * ficha range or the non-teaching exceptions are considered (E1).
     *
     * @param date the session date.
     * @param classSection the materia of the session.
     * @throws BadRequestAlertException with the key of the first rule the date violates.
     */
    private void validateSessionDate(LocalDate date, ClassSection classSection) {
        LocalDate today = LocalDate.now(clock);
        if (date.isAfter(today)) {
            throw new BadRequestAlertException("No puedes registrar asistencia para fechas futuras", ENTITY_NAME, "futureSessionDate");
        }

        validateActiveTrimester(date);

        Grade grade = classSection.getGrade();
        if (grade != null && (date.isBefore(grade.getStartDate()) || date.isAfter(grade.getEndDate()))) {
            throw new BadRequestAlertException("La fecha está fuera del rango de fechas de la ficha", ENTITY_NAME, "dateOutOfGradeRange");
        }

        if (classExceptionRepository.existsByClassSectionIdAndDate(classSection.getId(), date)) {
            throw new BadRequestAlertException(
                "Esta fecha está marcada como no lectiva, no se puede registrar asistencia",
                ENTITY_NAME,
                "nonTeachingDate"
            );
        }
    }

    /**
     * Resolves the trimester that contains the date and rejects anything but an active one. A
     * closed trimester blocks its whole range (E1), and a date outside every trimester or inside
     * a not-yet-started one is out of the current period.
     *
     * @param date the date to validate.
     * @throws BadRequestAlertException with the key of the rule the date violates.
     */
    private void validateActiveTrimester(LocalDate date) {
        Trimester trimester = trimesterRepository.findAllContaining(date).stream().findFirst().orElse(null);
        if (trimester == null) {
            throw new BadRequestAlertException("La fecha seleccionada está fuera del trimestre vigente", ENTITY_NAME, "dateOutOfTrimester");
        }

        StateTrimester trimesterState = trimesterService.classify(trimester);
        if (trimesterState == StateTrimester.CERRADO) {
            throw new BadRequestAlertException("No se puede modificar: el trimestre ya fue cerrado", ENTITY_NAME, "trimesterClosed");
        }
        if (trimesterState != StateTrimester.ACTIVO) {
            throw new BadRequestAlertException("La fecha seleccionada está fuera del trimestre vigente", ENTITY_NAME, "dateOutOfTrimester");
        }
    }

    /**
     * Resolves the apprentices that count for the session: those enrolled as Matriculado in the
     * ficha of the materia (E5). A materia whose ficha cannot be resolved yields no apprentices.
     *
     * @param classSection the materia of the session.
     * @return the enrolled apprentices, possibly empty.
     */
    private List<Apprentice> findEnrolledApprentices(ClassSection classSection) {
        Grade grade = classSection.getGrade();
        if (grade == null || grade.getId() == null) {
            return List.of();
        }
        return apprenticeRepository
            .findByGradeId(grade.getId())
            .stream()
            .filter(apprentice -> apprentice.getStateAcademic() == StateAcademic.MATRICULADO)
            .toList();
    }

    /**
     * Indexes the enrolled apprentices by profile id, which is the identity the payload uses.
     *
     * @param enrolledApprentices the apprentices enrolled in the ficha.
     * @return the apprentices by student profile id.
     */
    private static Map<String, Apprentice> enrolledApprenticesByStudentId(List<Apprentice> enrolledApprentices) {
        return enrolledApprentices
            .stream()
            .filter(apprentice -> apprentice.getStudent() != null && apprentice.getStudent().getId() != null)
            .collect(Collectors.toMap(apprentice -> apprentice.getStudent().getId(), Function.identity(), (first, second) -> first));
    }

    /**
     * Validates every confirmation before writing anything: the instructor only marks Presente or
     * Falla (Justificada arrives through UC010), and the apprentice must be enrolled in the ficha
     * as Matriculado.
     *
     * @param confirmations the marks sent by the client.
     * @param enrolledByStudentId the enrolled apprentices by profile id.
     * @throws BadRequestAlertException when a mark uses a forbidden state or an unenrolled student.
     */
    private static void validateConfirmations(List<AttendanceConfirmationVM> confirmations, Map<String, Apprentice> enrolledByStudentId) {
        for (AttendanceConfirmationVM confirmation : confirmations) {
            StateAttendance state = confirmation.getStateAttendance();
            if (state != StateAttendance.PRESENTE && state != StateAttendance.FALLA) {
                throw new BadRequestAlertException(
                    "El instructor solo puede registrar asistencia como Presente o Falla",
                    ENTITY_NAME,
                    "invalidAttendanceState"
                );
            }
            if (!enrolledByStudentId.containsKey(confirmation.getStudentId())) {
                throw new BadRequestAlertException("El aprendiz no está matriculado en esta ficha", ENTITY_NAME, "studentNotEnrolled");
            }
        }
    }

    /**
     * Creates or updates one record per confirmation, keyed by materia, aprendiz and fecha. Any
     * id sent in the payload is ignored, and an unchanged mark is rewritten, so saving the same
     * session twice is idempotent. Only a state that actually changes on an existing record is
     * audited: a new record has no previous value.
     *
     * @param attendanceSessionVM the requested session.
     * @param classSection the materia of the session.
     * @param enrolledByStudentId the enrolled apprentices by profile id.
     */
    private void upsertConfirmations(
        AttendanceSessionVM attendanceSessionVM,
        ClassSection classSection,
        Map<String, Apprentice> enrolledByStudentId
    ) {
        for (AttendanceConfirmationVM confirmation : attendanceSessionVM.getAttendances()) {
            UserProfile student = enrolledByStudentId.get(confirmation.getStudentId()).getStudent();
            Attendance attendance = attendanceRepository
                .findByClassSectionIdAndStudentIdAndDate(classSection.getId(), confirmation.getStudentId(), attendanceSessionVM.getDate())
                .orElseGet(() -> new Attendance().date(attendanceSessionVM.getDate()).classSection(classSection).student(student));
            StateAttendance previousState = attendance.getStateAttendance();
            StateAttendance newState = confirmation.getStateAttendance();
            attendance.setStateAttendance(newState);
            attendanceRepository.save(attendance);
            recordStateChange(attendance, previousState, newState, classSection.getInstructor());
        }
    }

    /**
     * Writes one audit entry when the state of an existing record really changes (UC009). A new
     * record has no previous state and saving the same state is not a change, so both cases are
     * left out of the audit log.
     *
     * @param attendance the persisted record whose state changed.
     * @param previousState the state before the change.
     * @param newState the state after the change.
     * @param modifiedBy the instructor profile that made the change.
     */
    private void recordStateChange(Attendance attendance, StateAttendance previousState, StateAttendance newState, UserProfile modifiedBy) {
        if (previousState == null || previousState == newState) {
            return;
        }
        AuditLog auditLog = new AuditLog()
            .previousState(previousState)
            .newState(newState)
            .editDate(Instant.now(clock))
            .modifiedBy(modifiedBy)
            .attendance(attendance);
        auditLogRepository.save(auditLog);
    }

    /**
     * Reads back the session and derives its completeness. The number of records counts every
     * record of that materia and date, and the session is complete when it matches the number of
     * apprentices enrolled in the ficha.
     *
     * @param classSection the materia of the session.
     * @param date the session date.
     * @param enrolledCount the number of apprentices enrolled in the ficha.
     * @return the persisted session.
     */
    private AttendanceSessionDTO buildSession(ClassSection classSection, LocalDate date, int enrolledCount) {
        List<Attendance> records = attendanceRepository.findByClassSectionIdAndDate(classSection.getId(), date);

        AttendanceSessionDTO sessionDTO = new AttendanceSessionDTO();
        sessionDTO.setClassSection(attendanceMapper.toDtoClassSectionSubjectName(classSection));
        sessionDTO.setDate(date);
        sessionDTO.setRecords(records.stream().sorted(RECORDS_BY_STUDENT_DOCUMENT).map(attendanceMapper::toDto).toList());
        sessionDTO.setEnrolledCount(enrolledCount);
        sessionDTO.setRecordedCount(records.size());
        sessionDTO.setComplete(records.size() == enrolledCount);
        return sessionDTO;
    }
}
