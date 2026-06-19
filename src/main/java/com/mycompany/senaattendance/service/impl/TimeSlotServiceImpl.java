package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.TimeSlot;
import com.mycompany.senaattendance.repository.TimeSlotRepository;
import com.mycompany.senaattendance.service.TimeSlotService;
import com.mycompany.senaattendance.service.dto.TimeSlotDTO;
import com.mycompany.senaattendance.service.mapper.TimeSlotMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.mycompany.senaattendance.domain.TimeSlot}.
 */
@Service
public class TimeSlotServiceImpl implements TimeSlotService {

    private static final Logger LOG = LoggerFactory.getLogger(TimeSlotServiceImpl.class);

    private final TimeSlotRepository timeSlotRepository;

    private final TimeSlotMapper timeSlotMapper;

    public TimeSlotServiceImpl(TimeSlotRepository timeSlotRepository, TimeSlotMapper timeSlotMapper) {
        this.timeSlotRepository = timeSlotRepository;
        this.timeSlotMapper = timeSlotMapper;
    }

    @Override
    public TimeSlotDTO save(TimeSlotDTO timeSlotDTO) {
        LOG.debug("Request to save TimeSlot : {}", timeSlotDTO);
        TimeSlot timeSlot = timeSlotMapper.toEntity(timeSlotDTO);
        timeSlot = timeSlotRepository.save(timeSlot);
        return timeSlotMapper.toDto(timeSlot);
    }

    @Override
    public TimeSlotDTO update(TimeSlotDTO timeSlotDTO) {
        LOG.debug("Request to update TimeSlot : {}", timeSlotDTO);
        TimeSlot timeSlot = timeSlotMapper.toEntity(timeSlotDTO);
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

                return existingTimeSlot;
            })
            .map(timeSlotRepository::save)
            .map(timeSlotMapper::toDto);
    }

    @Override
    public List<TimeSlotDTO> findAll() {
        LOG.debug("Request to get all TimeSlots");
        return timeSlotRepository.findAll().stream().map(timeSlotMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public Optional<TimeSlotDTO> findOne(String id) {
        LOG.debug("Request to get TimeSlot : {}", id);
        return timeSlotRepository.findById(id).map(timeSlotMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete TimeSlot : {}", id);
        timeSlotRepository.deleteById(id);
    }
}
