package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.TimeSlot;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.repository.TimeSlotRepository;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.TimeSlotService;
import com.mycompany.senaattendance.service.dto.TimeSlotDTO;
import com.mycompany.senaattendance.service.mapper.TimeSlotMapper;
import com.mycompany.senaattendance.web.rest.errors.BadRequestAlertException;
import com.mycompany.senaattendance.web.rest.errors.TimeSlotNameAlreadyUsedException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.mycompany.senaattendance.domain.TimeSlot}.
 */
@Service
public class TimeSlotServiceImpl implements TimeSlotService {

    private static final Logger LOG = LoggerFactory.getLogger(TimeSlotServiceImpl.class);

    private final TimeSlotRepository timeSlotRepository;

    private final TimeSlotMapper timeSlotMapper;

    private final GradeRepository gradeRepository;

    public TimeSlotServiceImpl(TimeSlotRepository timeSlotRepository, TimeSlotMapper timeSlotMapper, GradeRepository gradeRepository) {
        this.timeSlotRepository = timeSlotRepository;
        this.timeSlotMapper = timeSlotMapper;
        this.gradeRepository = gradeRepository;
    }

    @Override
    public TimeSlotDTO save(TimeSlotDTO timeSlotDTO) {
        LOG.debug("Request to save TimeSlot : {}", timeSlotDTO);
        TimeSlot timeSlot = timeSlotMapper.toEntity(timeSlotDTO);

        timeSlot.setIsActive(true);
        validateDifferentTimes(timeSlot);
        validateAndNormalizeName(timeSlot, null);

        // Insertar fecha de creación
        timeSlot.setCreatedDate(Instant.now());
        Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
        if (currentUserLogin.isPresent()) {
            // Insertar quien lo creo
            timeSlot.setCreatedBy(currentUserLogin.get());
        }

        timeSlot = timeSlotRepository.save(timeSlot);
        return timeSlotMapper.toDto(timeSlot);
    }

    @Override
    public TimeSlotDTO update(TimeSlotDTO timeSlotDTO) {
        LOG.debug("Request to update TimeSlot : {}", timeSlotDTO);
        TimeSlot timeSlot = timeSlotMapper.toEntity(timeSlotDTO);

        validateDifferentTimes(timeSlot);
        validateAndNormalizeName(timeSlot, timeSlot.getId());

        Optional<TimeSlot> optionalTimeSlot = timeSlotRepository.findById(timeSlot.getId());
        if (optionalTimeSlot.isPresent()) {
            TimeSlot existingTimeSlot = optionalTimeSlot.get();
            timeSlot.setCreatedBy(existingTimeSlot.getCreatedBy());
            timeSlot.setCreatedDate(existingTimeSlot.getCreatedDate());
        } else {
            timeSlot.setCreatedDate(Instant.now());
            Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
            if (currentUserLogin.isPresent()) {
                timeSlot.setCreatedBy(currentUserLogin.get());
            }
        }

        timeSlot = timeSlotRepository.save(timeSlot);
        return timeSlotMapper.toDto(timeSlot);
    }

    @Override
    public Optional<TimeSlotDTO> partialUpdate(TimeSlotDTO timeSlotDTO) {
        LOG.debug("Request to partially update TimeSlot : {}", timeSlotDTO);

        return timeSlotRepository
            .findById(timeSlotDTO.getId())
            .map(existingTimeSlot -> {
                timeSlotMapper.partialUpdate(existingTimeSlot, timeSlotDTO);
                validateDifferentTimes(existingTimeSlot);
                validateAndNormalizeName(existingTimeSlot, existingTimeSlot.getId());

                return existingTimeSlot;
            })
            .map(timeSlotRepository::save)
            .map(timeSlotMapper::toDto);
    }

    @Override
    public Page<TimeSlotDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all TimeSlots");
        return timeSlotRepository.findAll(pageable).map(timeSlotMapper::toDto);
    }

    @Override
    public Optional<TimeSlotDTO> findOne(String id) {
        LOG.debug("Request to get TimeSlot : {}", id);
        return timeSlotRepository.findById(id).map(timeSlotMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete TimeSlot : {}", id);
        if (gradeRepository.existsByTimeSlotId(id)) {
            throw new BadRequestAlertException("This jornada is assigned to fichas and cannot be deleted", "timeSlot", "timeSlotInUse");
        }
        timeSlotRepository.deleteById(id);
    }

    @Override
    public List<TimeSlotDTO> findByIsActiveTrue() {
        LOG.debug("Request to get all active TimeSlots");
        return timeSlotRepository.findTimeSlotByIsActive(true).stream().map(timeSlotMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Rejects a time slot whose start and end times are equal. Ranges that cross midnight
     * (an end time earlier than the start time) are valid and are not rejected here.
     *
     * @param timeSlot the time slot whose times are validated.
     * @throws BadRequestAlertException if both times are set and equal.
     */
    private void validateDifferentTimes(TimeSlot timeSlot) {
        if (timeSlot.getStartTime() != null && timeSlot.getStartTime().equals(timeSlot.getEndTime())) {
            throw new BadRequestAlertException("Start time and end time cannot be equal", "timeSlot", "timeSlotSameTime");
        }
    }

    /**
     * Trims the time slot name and enforces its uniqueness case-insensitively.
     * <p>
     * When {@code excludeId} is not {@code null}, the time slot with that id is ignored so an
     * update that keeps the same name does not collide with itself. On create {@code excludeId}
     * is {@code null} and every existing time slot is considered. The trimmed name is written
     * back onto the entity so the stored value is consistent.
     *
     * @param timeSlot the time slot whose name is normalized and validated.
     * @param excludeId the id to exclude from the uniqueness check, or {@code null} on create.
     * @throws TimeSlotNameAlreadyUsedException if another time slot with the same name exists.
     */
    private void validateAndNormalizeName(TimeSlot timeSlot, String excludeId) {
        if (timeSlot.getName() == null) {
            return;
        }
        String name = timeSlot.getName().trim();
        timeSlot.setName(name);
        boolean duplicate =
            excludeId == null
                ? timeSlotRepository.existsByNameIgnoreCase(name)
                : timeSlotRepository.existsByNameIgnoreCaseAndIdNot(name, excludeId);
        if (duplicate) {
            throw new TimeSlotNameAlreadyUsedException();
        }
    }
}
