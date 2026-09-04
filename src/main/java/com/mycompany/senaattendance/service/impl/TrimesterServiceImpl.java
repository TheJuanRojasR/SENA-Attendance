package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.ClassSchedule;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.repository.AttendanceRepository;
import com.mycompany.senaattendance.repository.ClassScheduleRepository;
import com.mycompany.senaattendance.repository.TrimesterRepository;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.TrimesterService;
import com.mycompany.senaattendance.service.dto.TrimesterDTO;
import com.mycompany.senaattendance.service.mapper.TrimesterMapper;
import com.mycompany.senaattendance.web.rest.errors.TrimesterAttendanceStartDateException;
import com.mycompany.senaattendance.web.rest.errors.TrimesterDatesOrderException;
import com.mycompany.senaattendance.web.rest.errors.TrimesterDatesOverlapException;
import com.mycompany.senaattendance.web.rest.errors.TrimesterEndDateInPastException;
import com.mycompany.senaattendance.web.rest.errors.TrimesterNotEditableException;
import com.mycompany.senaattendance.web.rest.errors.TrimesterStartDateLockedException;
import com.mycompany.senaattendance.web.rest.errors.TrimesterStartDateMustBeFutureException;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.mycompany.senaattendance.domain.Trimester}.
 */
@Service
public class TrimesterServiceImpl implements TrimesterService {

    private static final Logger LOG = LoggerFactory.getLogger(TrimesterServiceImpl.class);

    private static final String ENTITY_NAME = "trimester";

    private final TrimesterRepository trimesterRepository;

    private final TrimesterMapper trimesterMapper;

    private final Clock clock;

    private final ClassScheduleRepository classScheduleRepository;

    private final AttendanceRepository attendanceRepository;

    public TrimesterServiceImpl(
        TrimesterRepository trimesterRepository,
        TrimesterMapper trimesterMapper,
        Clock clock,
        ClassScheduleRepository classScheduleRepository,
        AttendanceRepository attendanceRepository
    ) {
        this.trimesterRepository = trimesterRepository;
        this.trimesterMapper = trimesterMapper;
        this.clock = clock;
        this.classScheduleRepository = classScheduleRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Override
    public TrimesterDTO save(TrimesterDTO trimesterDTO) {
        LOG.debug("Request to save Trimester : {}", trimesterDTO);
        Trimester trimester = trimesterMapper.toEntity(trimesterDTO);

        validateDatesAndOverlap(trimester);
        trimester.setStatus(computeStatus(LocalDate.now(clock), trimester.getStartDate(), trimester.getEndDate()));

        trimester.setCreatedDate(Instant.now());
        Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
        if (currentUserLogin.isPresent()) {
            trimester.setCreatedBy(currentUserLogin.get());
        }

        trimester = trimesterRepository.save(trimester);
        return trimesterMapper.toDto(trimester);
    }

    @Override
    public TrimesterDTO update(TrimesterDTO trimesterDTO) {
        LOG.debug("Request to update Trimester : {}", trimesterDTO);
        Trimester trimester = trimesterMapper.toEntity(trimesterDTO);

        Optional<Trimester> optionalTrimester = trimesterRepository.findById(trimester.getId());
        if (optionalTrimester.isPresent()) {
            Trimester existingTrimester = optionalTrimester.get();
            trimester.setCreatedBy(existingTrimester.getCreatedBy());
            trimester.setCreatedDate(existingTrimester.getCreatedDate());
        } else {
            trimester.setCreatedDate(Instant.now());
            Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
            if (currentUserLogin.isPresent()) {
                trimester.setCreatedBy(currentUserLogin.get());
            }
        }

        trimester = trimesterRepository.save(trimester);
        return trimesterMapper.toDto(trimester);
    }

    @Override
    public Optional<TrimesterDTO> partialUpdate(TrimesterDTO trimesterDTO) {
        LOG.debug("Request to partially update Trimester : {}", trimesterDTO);

        trimesterDTO.setStatus(null);
        sanitizeBlankPatchFields(trimesterDTO);
        boolean hasFields = hasPatchFields(trimesterDTO);

        return trimesterRepository
            .findById(trimesterDTO.getId())
            .map(existingTrimester -> {
                if (!hasFields) {
                    return existingTrimester;
                }
                LocalDate today = LocalDate.now(clock);
                TrimesterState state = classifyState(today, existingTrimester.getStartDate(), existingTrimester.getEndDate());
                if (state == TrimesterState.CLOSED) {
                    throw new TrimesterNotEditableException();
                }
                boolean startChanged =
                    trimesterDTO.getStartDate() != null && !trimesterDTO.getStartDate().equals(existingTrimester.getStartDate());
                boolean endChanged = trimesterDTO.getEndDate() != null && !trimesterDTO.getEndDate().equals(existingTrimester.getEndDate());
                if (state == TrimesterState.ACTIVE) {
                    if (startChanged) {
                        throw new TrimesterStartDateLockedException();
                    }
                    if (endChanged && trimesterDTO.getEndDate().isBefore(today)) {
                        throw new TrimesterEndDateInPastException();
                    }
                } else if (state == TrimesterState.FUTURE) {
                    if (startChanged && !trimesterDTO.getStartDate().isAfter(today)) {
                        throw new TrimesterStartDateMustBeFutureException();
                    }
                }
                if (startChanged && hasAttendance(existingTrimester.getId())) {
                    throw new TrimesterAttendanceStartDateException();
                }
                trimesterMapper.partialUpdate(existingTrimester, trimesterDTO);
                validateDatesAndOverlapExcludingSelf(existingTrimester);
                if (startChanged || endChanged) {
                    existingTrimester.setStatus(computeStatus(today, existingTrimester.getStartDate(), existingTrimester.getEndDate()));
                }
                return existingTrimester;
            })
            .map(existingTrimester -> hasFields ? trimesterRepository.save(existingTrimester) : existingTrimester)
            .map(trimesterMapper::toDto);
    }

    @Override
    public Page<TrimesterDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Trimesters");
        return trimesterRepository.findAll(pageable).map(trimesterMapper::toDto);
    }

    @Override
    public Page<TrimesterDTO> search(String searchTerm, Boolean status, Pageable pageable) {
        LOG.debug("Request to search Trimesters with term: {}, status: {}", searchTerm, status);

        boolean hasTerm = searchTerm != null && !searchTerm.isBlank();
        boolean year = hasTerm && searchTerm.matches("\\d{4}");

        Page<Trimester> page;
        if (year && status != null) {
            page = trimesterRepository.searchByStartDateYearAndStatus(Integer.valueOf(searchTerm), status, pageable);
        } else if (year) {
            page = trimesterRepository.searchByStartDateYear(Integer.valueOf(searchTerm), pageable);
        } else if (hasTerm && status != null) {
            page = trimesterRepository.searchByNameAndStatus(searchTerm, status, pageable);
        } else if (hasTerm) {
            page = trimesterRepository.searchByName(searchTerm, pageable);
        } else if (status != null) {
            page = trimesterRepository.findByStatus(status, pageable);
        } else {
            page = trimesterRepository.findAll(pageable);
        }

        return page.map(trimesterMapper::toDto);
    }

    @Override
    public Optional<TrimesterDTO> findOne(String id) {
        LOG.debug("Request to get Trimester : {}", id);
        return trimesterRepository.findById(id).map(trimesterMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete Trimester : {}", id);
        trimesterRepository.deleteById(id);
    }

    /**
     * Computes whether a trimester is active on {@code today}, defined as today falling
     * within the inclusive {@code [start, end]} range.
     *
     * @param today the reference day.
     * @param start the trimester start date (inclusive).
     * @param end the trimester end date (inclusive).
     * @return {@code true} when {@code start <= today <= end}.
     */
    private boolean computeStatus(LocalDate today, LocalDate start, LocalDate end) {
        return !today.isBefore(start) && !today.isAfter(end);
    }

    /**
     * Validates the date order (E2) and the no-overlap rule (E1) for a trimester being
     * created, throwing a {@code BadRequestAlertException} subclass on failure. Date order
     * is checked first so a malformed range fails before any repository query.
     *
     * @param trimester the trimester to validate.
     * @throws TrimesterDatesOrderException if {@code startDate >= endDate}.
     * @throws TrimesterDatesOverlapException if the range overlaps an existing trimester.
     */
    private void validateDatesAndOverlap(Trimester trimester) {
        LocalDate start = trimester.getStartDate();
        LocalDate end = trimester.getEndDate();
        if (!start.isBefore(end)) {
            throw new TrimesterDatesOrderException();
        }
        if (!trimesterRepository.findAllOverlapping(start, end).isEmpty()) {
            throw new TrimesterDatesOverlapException();
        }
    }

    /**
     * Daily job that keeps each trimester's {@code status} in sync with today versus its
     * {@code [startDate, endDate]} range. Only trimesters whose computed status differs
     * from the persisted status are rewritten; each loaded entity is saved as-is so its
     * {@code createdBy}/{@code createdDate} audit fields are preserved and the auditing
     * listener fills {@code lastModifiedDate}.
     */
    @Override
    @Scheduled(cron = "0 0 1 * * ?")
    public void syncStatuses() {
        LocalDate today = LocalDate.now(clock);
        trimesterRepository.findAll().forEach(trimester -> {
            boolean computed = computeStatus(today, trimester.getStartDate(), trimester.getEndDate());
            if (trimester.getStatus() == null || trimester.getStatus() != computed) {
                trimester.setStatus(computed);
                trimesterRepository.save(trimester);
            }
        });
    }

    /**
     * Lifecycle state of a trimester relative to {@code today}, classified from its date range.
     * Mirrors {@link #computeStatus} so the inclusive active bounds stay consistent.
     */
    private enum TrimesterState {
        CLOSED,
        ACTIVE,
        FUTURE,
    }

    /**
     * Classifies a trimester as {@code CLOSED} ({@code end < today}), {@code ACTIVE}
     * ({@code today ∈ [start, end]}) or {@code FUTURE} ({@code start > today}).
     *
     * @param today the reference day.
     * @param start the trimester start date (inclusive).
     * @param end the trimester end date (inclusive).
     * @return the lifecycle state.
     */
    private TrimesterState classifyState(LocalDate today, LocalDate start, LocalDate end) {
        if (end.isBefore(today)) {
            return TrimesterState.CLOSED;
        }
        if (start.isAfter(today)) {
            return TrimesterState.FUTURE;
        }
        return TrimesterState.ACTIVE;
    }

    /**
     * Blank and whitespace-only names are treated as omitted for trimester PATCH requests,
     * matching {@code ProgramServiceImpl}.
     *
     * @param trimesterDTO the incoming partial update.
     */
    private void sanitizeBlankPatchFields(TrimesterDTO trimesterDTO) {
        if (trimesterDTO.getName() != null && trimesterDTO.getName().isBlank()) {
            trimesterDTO.setName(null);
        }
    }

    /**
     * @param trimesterDTO the incoming partial update.
     * @return whether the DTO carries at least one editable field to apply.
     */
    private boolean hasPatchFields(TrimesterDTO trimesterDTO) {
        return (trimesterDTO.getName() != null || trimesterDTO.getStartDate() != null || trimesterDTO.getEndDate() != null);
    }

    /**
     * Detects whether the trimester has attendance records. The relationship is a multi-hop
     * chain: schedules reference the trimester, schedules reference a {@code classSection},
     * and attendance records reference that {@code classSection}. Only non-null class sections
     * are considered, so a schedule without a section cannot create a false positive.
     * <p>
     * The class section ids are matched as {@code ObjectId}s because MongoDB persists
     * auto-generated {@code @Id} values (and the {@code $id} of a {@code @DBRef}) as
     * {@code ObjectId}; a string comparison would never match them.
     *
     * @param trimesterId the trimester id.
     * @return {@code true} when at least one attendance record exists in the trimester's class sections.
     */
    private boolean hasAttendance(String trimesterId) {
        List<ObjectId> classSectionIds = classScheduleRepository
            .findByTrimesterId(trimesterId)
            .stream()
            .map(ClassSchedule::getClassSection)
            .filter(Objects::nonNull)
            .map(ClassSection::getId)
            .filter(TrimesterServiceImpl::isObjectId)
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

    /**
     * Validates the date order (E2) and the no-overlap rule (E1, excluding the edited
     * trimester itself) for a trimester being partially updated, throwing a
     * {@code BadRequestAlertException} subclass on failure. Date order is checked first so a
     * malformed range fails before any repository query.
     *
     * @param trimester the trimester to validate.
     * @throws TrimesterDatesOrderException if {@code startDate >= endDate}.
     * @throws TrimesterDatesOverlapException if the range overlaps another trimester.
     */
    private void validateDatesAndOverlapExcludingSelf(Trimester trimester) {
        LocalDate start = trimester.getStartDate();
        LocalDate end = trimester.getEndDate();
        if (!start.isBefore(end)) {
            throw new TrimesterDatesOrderException();
        }
        if (!trimesterRepository.findAllOverlappingExcluding(start, end, trimester.getId()).isEmpty()) {
            throw new TrimesterDatesOverlapException();
        }
    }
}
