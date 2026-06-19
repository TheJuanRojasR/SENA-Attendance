package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.Attendance;
import com.mycompany.senaattendance.repository.AttendanceRepository;
import com.mycompany.senaattendance.service.AttendanceService;
import com.mycompany.senaattendance.service.dto.AttendanceDTO;
import com.mycompany.senaattendance.service.mapper.AttendanceMapper;
import java.util.Optional;
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

    private final AttendanceRepository attendanceRepository;

    private final AttendanceMapper attendanceMapper;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository, AttendanceMapper attendanceMapper) {
        this.attendanceRepository = attendanceRepository;
        this.attendanceMapper = attendanceMapper;
    }

    @Override
    public AttendanceDTO save(AttendanceDTO attendanceDTO) {
        LOG.debug("Request to save Attendance : {}", attendanceDTO);
        Attendance attendance = attendanceMapper.toEntity(attendanceDTO);
        attendance = attendanceRepository.save(attendance);
        return attendanceMapper.toDto(attendance);
    }

    @Override
    public AttendanceDTO update(AttendanceDTO attendanceDTO) {
        LOG.debug("Request to update Attendance : {}", attendanceDTO);
        Attendance attendance = attendanceMapper.toEntity(attendanceDTO);
        attendance = attendanceRepository.save(attendance);
        return attendanceMapper.toDto(attendance);
    }

    @Override
    public Optional<AttendanceDTO> partialUpdate(AttendanceDTO attendanceDTO) {
        LOG.debug("Request to partially update Attendance : {}", attendanceDTO);

        return attendanceRepository
            .findById(attendanceDTO.getId())
            .map(existingAttendance -> {
                attendanceMapper.partialUpdate(existingAttendance, attendanceDTO);

                return existingAttendance;
            })
            .map(attendanceRepository::save)
            .map(attendanceMapper::toDto);
    }

    @Override
    public Page<AttendanceDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Attendances");
        return attendanceRepository.findAll(pageable).map(attendanceMapper::toDto);
    }

    public Page<AttendanceDTO> findAllWithEagerRelationships(Pageable pageable) {
        return attendanceRepository.findAllWithEagerRelationships(pageable).map(attendanceMapper::toDto);
    }

    @Override
    public Optional<AttendanceDTO> findOne(String id) {
        LOG.debug("Request to get Attendance : {}", id);
        return attendanceRepository.findOneWithEagerRelationships(id).map(attendanceMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete Attendance : {}", id);
        attendanceRepository.deleteById(id);
    }
}
