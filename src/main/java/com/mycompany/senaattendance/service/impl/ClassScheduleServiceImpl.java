package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.ClassSchedule;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.TimeSlot;
import com.mycompany.senaattendance.repository.ClassScheduleRepository;
import com.mycompany.senaattendance.repository.ClassSectionRepository;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.ClassScheduleService;
import com.mycompany.senaattendance.service.dto.ClassScheduleDTO;
import com.mycompany.senaattendance.service.mapper.ClassScheduleMapper;
import com.mycompany.senaattendance.web.rest.errors.BadRequestAlertException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.mycompany.senaattendance.domain.ClassSchedule}.
 */
@Service
public class ClassScheduleServiceImpl implements ClassScheduleService {

    private static final Logger LOG = LoggerFactory.getLogger(ClassScheduleServiceImpl.class);

    private static final String ENTITY_NAME = "classSchedule";

    private final ClassScheduleRepository classScheduleRepository;

    private final ClassScheduleMapper classScheduleMapper;

    private final ClassSectionRepository classSectionRepository;

    private final GradeRepository gradeRepository;

    public ClassScheduleServiceImpl(
        ClassScheduleRepository classScheduleRepository,
        ClassScheduleMapper classScheduleMapper,
        ClassSectionRepository classSectionRepository,
        GradeRepository gradeRepository
    ) {
        this.classScheduleRepository = classScheduleRepository;
        this.classScheduleMapper = classScheduleMapper;
        this.classSectionRepository = classSectionRepository;
        this.gradeRepository = gradeRepository;
    }

    @Override
    public ClassScheduleDTO save(ClassScheduleDTO classScheduleDTO) {
        LOG.debug("Request to save ClassSchedule : {}", classScheduleDTO);
        ClassSchedule classSchedule = classScheduleMapper.toEntity(classScheduleDTO);

        validateSchedule(classSchedule, null);

        classSchedule.setCreatedDate(Instant.now());
        Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
        if (currentUserLogin.isPresent()) {
            classSchedule.setCreatedBy(currentUserLogin.get());
        }

        classSchedule = classScheduleRepository.save(classSchedule);
        return classScheduleMapper.toDto(classSchedule);
    }

    @Override
    public ClassScheduleDTO update(ClassScheduleDTO classScheduleDTO) {
        LOG.debug("Request to update ClassSchedule : {}", classScheduleDTO);
        ClassSchedule classSchedule = classScheduleMapper.toEntity(classScheduleDTO);

        validateSchedule(classSchedule, classSchedule.getId());

        Optional<ClassSchedule> optionalClassSchedule = classScheduleRepository.findById(classSchedule.getId());
        if (optionalClassSchedule.isPresent()) {
            ClassSchedule existingClassSchedule = optionalClassSchedule.get();
            classSchedule.setCreatedBy(existingClassSchedule.getCreatedBy());
            classSchedule.setCreatedDate(existingClassSchedule.getCreatedDate());
        } else {
            classSchedule.setCreatedDate(Instant.now());
            Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
            if (currentUserLogin.isPresent()) {
                classSchedule.setCreatedBy(currentUserLogin.get());
            }
        }

        classSchedule = classScheduleRepository.save(classSchedule);
        return classScheduleMapper.toDto(classSchedule);
    }

    @Override
    public Optional<ClassScheduleDTO> partialUpdate(ClassScheduleDTO classScheduleDTO) {
        LOG.debug("Request to partially update ClassSchedule : {}", classScheduleDTO);

        return classScheduleRepository
            .findById(classScheduleDTO.getId())
            .map(existingClassSchedule -> {
                classScheduleMapper.partialUpdate(existingClassSchedule, classScheduleDTO);
                validateSchedule(existingClassSchedule, existingClassSchedule.getId());

                return existingClassSchedule;
            })
            .map(classScheduleRepository::save)
            .map(classScheduleMapper::toDto);
    }

    @Override
    public Page<ClassScheduleDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all ClassSchedules");
        return classScheduleRepository.findAll(pageable).map(classScheduleMapper::toDto);
    }

    public Page<ClassScheduleDTO> findAllWithEagerRelationships(Pageable pageable) {
        return classScheduleRepository.findAllWithEagerRelationships(pageable).map(classScheduleMapper::toDto);
    }

    @Override
    public Optional<ClassScheduleDTO> findOne(String id) {
        LOG.debug("Request to get ClassSchedule : {}", id);
        return classScheduleRepository.findOneWithEagerRelationships(id).map(classScheduleMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete ClassSchedule : {}", id);
        classScheduleRepository.deleteById(id);
    }

    /**
     * Validates the UC015 rules that apply to every class schedule write: the session must start
     * and end on the same day, it must fall inside the jornada (time slot) of its ficha, and it
     * must not overlap another schedule of the same ficha in the same trimester and weekday.
     *
     * @param classSchedule the schedule about to be persisted.
     * @param excludeId the id to exclude from the overlap check, or {@code null} on create.
     * @throws BadRequestAlertException when any of the rules is violated.
     */
    private void validateSchedule(ClassSchedule classSchedule, String excludeId) {
        validateSameDaySession(classSchedule);
        ClassSection classSection = resolveClassSection(classSchedule);
        validateWithinJornada(classSchedule, classSection);
        validateNoOverlap(classSchedule, classSection, excludeId);
    }

    /**
     * Rejects a session whose end time is not later than its start time. Such a range either has
     * zero length or crosses midnight, and the schedule only models a single day.
     *
     * @param classSchedule the schedule whose times are validated.
     * @throws BadRequestAlertException if both times are set and the end is not after the start.
     */
    private void validateSameDaySession(ClassSchedule classSchedule) {
        if (classSchedule.getStartTime() == null || classSchedule.getEndTime() == null) {
            return;
        }
        if (!classSchedule.getStartTime().isBefore(classSchedule.getEndTime())) {
            throw new BadRequestAlertException("La sesión debe iniciar y terminar el mismo día", ENTITY_NAME, "scheduleCrossesMidnight");
        }
    }

    /**
     * Loads the class section referenced by the schedule, which is the entry point of the ficha
     * chain ({@code classSection -> grade -> timeSlot}). Returns {@code null} when the reference
     * or the target document cannot be resolved, so the rules that depend on the ficha are skipped.
     *
     * @param classSchedule the schedule whose class section is resolved.
     * @return the persisted class section, or {@code null} if it cannot be resolved.
     */
    private ClassSection resolveClassSection(ClassSchedule classSchedule) {
        if (classSchedule.getClassSection() == null || classSchedule.getClassSection().getId() == null) {
            return null;
        }
        return classSectionRepository.findOneWithEagerRelationships(classSchedule.getClassSection().getId()).orElse(null);
    }

    /**
     * Resolves the jornada (time slot) of the ficha the class section belongs to.
     *
     * @param classSection the class section whose ficha jornada is resolved.
     * @return the time slot of the ficha, or {@code null} if the chain cannot be resolved.
     */
    private TimeSlot resolveJornada(ClassSection classSection) {
        if (classSection == null || classSection.getGrade() == null || classSection.getGrade().getId() == null) {
            return null;
        }
        return gradeRepository.findOneWithEagerRelationships(classSection.getGrade().getId()).map(Grade::getTimeSlot).orElse(null);
    }

    /**
     * Rejects a schedule that starts before or ends after the jornada of its ficha. Sessions that
     * touch a jornada boundary are valid.
     *
     * @param classSchedule the schedule whose times are validated.
     * @param classSection the class section that resolves the ficha jornada.
     * @throws BadRequestAlertException if the schedule falls outside the jornada.
     */
    private void validateWithinJornada(ClassSchedule classSchedule, ClassSection classSection) {
        TimeSlot jornada = resolveJornada(classSection);
        if (
            jornada == null ||
            jornada.getStartTime() == null ||
            jornada.getEndTime() == null ||
            classSchedule.getStartTime() == null ||
            classSchedule.getEndTime() == null
        ) {
            return;
        }
        if (classSchedule.getStartTime().isBefore(jornada.getStartTime()) || classSchedule.getEndTime().isAfter(jornada.getEndTime())) {
            throw new BadRequestAlertException(
                "El horario debe estar dentro de la jornada de la ficha",
                ENTITY_NAME,
                "scheduleOutOfTimeSlot"
            );
        }
    }

    /**
     * Rejects a schedule that overlaps another schedule of the same ficha in the same trimester and
     * weekday. Both the schedule's own subject and every other subject of the ficha are considered,
     * because the group of apprentices cannot attend two sessions at once. Adjacent ranges where one
     * ends exactly when the other starts do not overlap.
     *
     * @param classSchedule the schedule being validated.
     * @param classSection the class section that resolves the ficha and its subjects.
     * @param excludeId the id to exclude from the check, or {@code null} on create.
     * @throws BadRequestAlertException if an overlapping schedule is found.
     */
    private void validateNoOverlap(ClassSchedule classSchedule, ClassSection classSection, String excludeId) {
        if (
            classSection == null ||
            classSection.getGrade() == null ||
            classSection.getGrade().getId() == null ||
            classSchedule.getTrimester() == null ||
            classSchedule.getTrimester().getId() == null ||
            classSchedule.getDayOfWeek() == null ||
            classSchedule.getStartTime() == null ||
            classSchedule.getEndTime() == null
        ) {
            return;
        }

        String trimesterId = classSchedule.getTrimester().getId();
        List<ClassSection> classSections = classSectionRepository.findByGradeId(classSection.getGrade().getId());
        for (ClassSection candidateSection : classSections) {
            List<ClassSchedule> sameSlotSchedules = classScheduleRepository.findByClassSectionIdAndTrimesterIdAndDayOfWeek(
                candidateSection.getId(),
                trimesterId,
                classSchedule.getDayOfWeek()
            );
            for (ClassSchedule other : sameSlotSchedules) {
                if (other.getId() != null && other.getId().equals(excludeId)) {
                    continue;
                }
                if (overlaps(classSchedule, other)) {
                    throw new BadRequestAlertException(
                        "El horario se solapa con otro horario de la ficha en ese trimestre",
                        ENTITY_NAME,
                        "scheduleOverlap"
                    );
                }
            }
        }
    }

    /**
     * Returns whether two single-day ranges intersect. Ranges that only share a boundary
     * ({@code end == otherStart}) are not considered overlapping.
     *
     * @param classSchedule the candidate schedule.
     * @param other an existing schedule in the same trimester and weekday.
     * @return {@code true} if both ranges intersect.
     */
    private boolean overlaps(ClassSchedule classSchedule, ClassSchedule other) {
        return (classSchedule.getStartTime().isBefore(other.getEndTime()) && other.getStartTime().isBefore(classSchedule.getEndTime()));
    }
}
