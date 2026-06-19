package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.Program;
import com.mycompany.senaattendance.repository.ProgramRepository;
import com.mycompany.senaattendance.service.ProgramService;
import com.mycompany.senaattendance.service.dto.ProgramDTO;
import com.mycompany.senaattendance.service.mapper.ProgramMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.mycompany.senaattendance.domain.Program}.
 */
@Service
public class ProgramServiceImpl implements ProgramService {

    private static final Logger LOG = LoggerFactory.getLogger(ProgramServiceImpl.class);

    private final ProgramRepository programRepository;

    private final ProgramMapper programMapper;

    public ProgramServiceImpl(ProgramRepository programRepository, ProgramMapper programMapper) {
        this.programRepository = programRepository;
        this.programMapper = programMapper;
    }

    @Override
    public ProgramDTO save(ProgramDTO programDTO) {
        LOG.debug("Request to save Program : {}", programDTO);
        Program program = programMapper.toEntity(programDTO);
        program = programRepository.save(program);
        return programMapper.toDto(program);
    }

    @Override
    public ProgramDTO update(ProgramDTO programDTO) {
        LOG.debug("Request to update Program : {}", programDTO);
        Program program = programMapper.toEntity(programDTO);
        program = programRepository.save(program);
        return programMapper.toDto(program);
    }

    @Override
    public Optional<ProgramDTO> partialUpdate(ProgramDTO programDTO) {
        LOG.debug("Request to partially update Program : {}", programDTO);

        return programRepository
            .findById(programDTO.getId())
            .map(existingProgram -> {
                programMapper.partialUpdate(existingProgram, programDTO);

                return existingProgram;
            })
            .map(programRepository::save)
            .map(programMapper::toDto);
    }

    @Override
    public Page<ProgramDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Programs");
        return programRepository.findAll(pageable).map(programMapper::toDto);
    }

    @Override
    public Optional<ProgramDTO> findOne(String id) {
        LOG.debug("Request to get Program : {}", id);
        return programRepository.findById(id).map(programMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete Program : {}", id);
        programRepository.deleteById(id);
    }
}
