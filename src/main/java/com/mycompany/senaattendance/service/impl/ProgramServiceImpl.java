package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.Program;
import com.mycompany.senaattendance.repository.ProgramRepository;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.ProgramService;
import com.mycompany.senaattendance.service.dto.ProgramDTO;
import com.mycompany.senaattendance.service.mapper.ProgramMapper;
import com.mycompany.senaattendance.web.rest.errors.ProgramCodeAlreadyUsedException;
import com.mycompany.senaattendance.web.rest.errors.ProgramInitialsAlreadyUsedException;
import com.mycompany.senaattendance.web.rest.errors.ProgramNameAlreadyUsedException;
import java.time.Instant;
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

        if (program.getStatus() == null) {
            program.setStatus(true);
        }

        validateAndNormalizeUniqueFields(program, null);

        // Inserta fecha de creación
        program.setCreatedDate(Instant.now());
        Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
        if (currentUserLogin.isPresent()) {
            // Inserta quien lo creo
            program.setCreatedBy(currentUserLogin.get());
        }

        program = programRepository.save(program);
        return programMapper.toDto(program);
    }

    @Override
    public ProgramDTO update(ProgramDTO programDTO) {
        LOG.debug("Request to update Program : {}", programDTO);
        Program program = programMapper.toEntity(programDTO);

        validateAndNormalizeUniqueFields(program, program.getId());

        Optional<Program> optionalProgram = programRepository.findById(program.getId());
        if (optionalProgram.isPresent()) {
            Program existingProgram = optionalProgram.get();
            program.setCreatedBy(existingProgram.getCreatedBy());
            program.setCreatedDate(existingProgram.getCreatedDate());
        } else {
            program.setCreatedDate(Instant.now());
            Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
            if (currentUserLogin.isPresent()) {
                program.setCreatedBy(currentUserLogin.get());
            }
        }

        program = programRepository.save(program);
        return programMapper.toDto(program);
    }

    @Override
    public Optional<ProgramDTO> partialUpdate(ProgramDTO programDTO) {
        LOG.debug("Request to partially update Program : {}", programDTO);

        programDTO.setStatus(null);
        sanitizeBlankPatchFields(programDTO);
        boolean hasFieldsToUpdate = hasPatchFields(programDTO);

        return programRepository
            .findById(programDTO.getId())
            .map(existingProgram -> {
                if (!hasFieldsToUpdate) {
                    return existingProgram;
                }

                programMapper.partialUpdate(existingProgram, programDTO);

                validateAndNormalizeUniqueFields(existingProgram, existingProgram.getId());

                return existingProgram;
            })
            .map(existingProgram -> hasFieldsToUpdate ? programRepository.save(existingProgram) : existingProgram)
            .map(programMapper::toDto);
    }

    @Override
    public Page<ProgramDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Programs");
        return programRepository.findAll(pageable).map(programMapper::toDto);
    }

    @Override
    public Page<ProgramDTO> search(String searchTerm, Boolean status, Pageable pageable) {
        LOG.debug("Request to search Programs with term: {}, status: {}", searchTerm, status);

        boolean hasTerm = searchTerm != null && !searchTerm.isBlank();

        Page<Program> page;
        if (hasTerm && status != null) {
            page = programRepository.searchByCodeOrNameAndStatus(searchTerm, status, pageable);
        } else if (hasTerm) {
            page = programRepository.searchByCodeOrName(searchTerm, pageable);
        } else if (status != null) {
            page = programRepository.findByStatus(status, pageable);
        } else {
            page = programRepository.findAll(pageable);
        }

        return page.map(programMapper::toDto);
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

    /**
     * Blank and whitespace-only values are treated as omitted for program PATCH requests.
     *
     * @param programDTO the incoming partial update
     */
    private void sanitizeBlankPatchFields(ProgramDTO programDTO) {
        if (programDTO.getName() != null && programDTO.getName().isBlank()) {
            programDTO.setName(null);
        }
        if (programDTO.getInitials() != null && programDTO.getInitials().isBlank()) {
            programDTO.setInitials(null);
        }
        if (programDTO.getCode() != null && programDTO.getCode().isBlank()) {
            programDTO.setCode(null);
        }
    }

    private boolean hasPatchFields(ProgramDTO programDTO) {
        return (
            programDTO.getName() != null ||
            programDTO.getInitials() != null ||
            programDTO.getCode() != null ||
            programDTO.getTrimesters() != null
        );
    }

    /**
     * Normalizes the program's unique fields and enforces their uniqueness.
     * <p>
     * {@code initials} and {@code code} are trimmed and converted to upper case (they are
     * siglas/codes such as "ADSO", "228118"); {@code name} is trimmed but keeps its original
     * case, and each value is compared case-insensitively against the repository.
     * <p>
     * When {@code excludeId} is not {@code null}, any program with that id is ignored so an
     * update that keeps the same code, initials or name does not collide with itself. On
     * create {@code excludeId} is {@code null} and every existing program is considered.
     * Non-null values are written back onto the entity so stored values are consistent.
     *
     * @param program   the program to normalize and validate (a partially updated entity may
     *                  carry {@code null} fields, which are skipped).
     * @param excludeId the id to exclude from the uniqueness checks, or {@code null} when no
     *                  exclusion is needed (e.g. on create).
     * @throws ProgramInitialsAlreadyUsedException if a program with the same initials exists.
     * @throws ProgramCodeAlreadyUsedException     if a program with the same code exists.
     * @throws ProgramNameAlreadyUsedException     if a program with the same name exists.
     */
    private void validateAndNormalizeUniqueFields(Program program, String excludeId) {
        if (program.getInitials() != null) {
            String initials = program.getInitials().trim().toUpperCase();
            program.setInitials(initials);
            boolean duplicate =
                excludeId == null
                    ? programRepository.existsByInitialsIgnoreCase(initials)
                    : programRepository.existsByInitialsIgnoreCaseAndIdNot(initials, excludeId);
            if (duplicate) {
                throw new ProgramInitialsAlreadyUsedException();
            }
        }

        if (program.getCode() != null) {
            String code = program.getCode().trim().toUpperCase();
            program.setCode(code);
            boolean duplicate =
                excludeId == null
                    ? programRepository.existsByCodeIgnoreCase(code)
                    : programRepository.existsByCodeIgnoreCaseAndIdNot(code, excludeId);
            if (duplicate) {
                throw new ProgramCodeAlreadyUsedException();
            }
        }

        if (program.getName() != null) {
            String name = program.getName().trim();
            program.setName(name);
            boolean duplicate =
                excludeId == null
                    ? programRepository.existsByNameIgnoreCase(name)
                    : programRepository.existsByNameIgnoreCaseAndIdNot(name, excludeId);
            if (duplicate) {
                throw new ProgramNameAlreadyUsedException();
            }
        }
    }
}
