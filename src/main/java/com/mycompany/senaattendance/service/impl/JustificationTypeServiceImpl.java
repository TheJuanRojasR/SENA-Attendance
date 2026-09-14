package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.JustificationType;
import com.mycompany.senaattendance.domain.enumeration.Status;
import com.mycompany.senaattendance.repository.JustificationTypeRepository;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.JustificationTypeService;
import com.mycompany.senaattendance.service.dto.JustificationTypeDTO;
import com.mycompany.senaattendance.service.mapper.JustificationTypeMapper;
import com.mycompany.senaattendance.web.rest.errors.JustificationTypeNameAlreadyUsedException;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.mycompany.senaattendance.domain.JustificationType}.
 */
@Service
public class JustificationTypeServiceImpl implements JustificationTypeService {

    private static final Logger LOG = LoggerFactory.getLogger(JustificationTypeServiceImpl.class);

    private final JustificationTypeRepository justificationTypeRepository;

    private final JustificationTypeMapper justificationTypeMapper;

    public JustificationTypeServiceImpl(
        JustificationTypeRepository justificationTypeRepository,
        JustificationTypeMapper justificationTypeMapper
    ) {
        this.justificationTypeRepository = justificationTypeRepository;
        this.justificationTypeMapper = justificationTypeMapper;
    }

    @Override
    public JustificationTypeDTO save(JustificationTypeDTO justificationTypeDTO) {
        LOG.debug("Request to save JustificationType : {}", justificationTypeDTO);
        JustificationType justificationType = justificationTypeMapper.toEntity(justificationTypeDTO);
        validateAndNormalizeName(justificationType, null);

        // Los tipos nuevos nacen activos
        justificationType.setStatus(Status.ACTIVO);

        justificationType.setCreatedDate(Instant.now());
        Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
        if (currentUserLogin.isPresent()) {
            justificationType.setCreatedBy(currentUserLogin.get());
        }

        justificationType = justificationTypeRepository.save(justificationType);
        return justificationTypeMapper.toDto(justificationType);
    }

    @Override
    public JustificationTypeDTO update(JustificationTypeDTO justificationTypeDTO) {
        LOG.debug("Request to update JustificationType : {}", justificationTypeDTO);
        JustificationType justificationType = justificationTypeMapper.toEntity(justificationTypeDTO);
        validateAndNormalizeName(justificationType, justificationType.getId());

        Optional<JustificationType> optionalJustificationType = justificationTypeRepository.findById(justificationType.getId());
        if (optionalJustificationType.isPresent()) {
            JustificationType existingJustificationType = optionalJustificationType.get();
            justificationType.setCreatedBy(existingJustificationType.getCreatedBy());
            justificationType.setCreatedDate(existingJustificationType.getCreatedDate());
        } else {
            justificationType.setCreatedDate(Instant.now());
            Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
            if (currentUserLogin.isPresent()) {
                justificationType.setCreatedBy(currentUserLogin.get());
            }
        }

        // Un PUT sin status conserva el estado existente en lugar de borrarlo.
        if (justificationType.getStatus() == null) {
            justificationType.setStatus(optionalJustificationType.map(JustificationType::getStatus).orElse(Status.ACTIVO));
        }

        justificationType = justificationTypeRepository.save(justificationType);
        return justificationTypeMapper.toDto(justificationType);
    }

    @Override
    public Optional<JustificationTypeDTO> partialUpdate(JustificationTypeDTO justificationTypeDTO) {
        LOG.debug("Request to partially update JustificationType : {}", justificationTypeDTO);

        return justificationTypeRepository
            .findById(justificationTypeDTO.getId())
            .map(existingJustificationType -> {
                justificationTypeMapper.partialUpdate(existingJustificationType, justificationTypeDTO);
                validateAndNormalizeName(existingJustificationType, existingJustificationType.getId());

                return existingJustificationType;
            })
            .map(justificationTypeRepository::save)
            .map(justificationTypeMapper::toDto);
    }

    @Override
    public List<JustificationTypeDTO> findAll() {
        LOG.debug("Request to get all JustificationTypes");
        return justificationTypeRepository
            .findAll()
            .stream()
            .map(justificationTypeMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public Optional<JustificationTypeDTO> findOne(String id) {
        LOG.debug("Request to get JustificationType : {}", id);
        return justificationTypeRepository.findById(id).map(justificationTypeMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete JustificationType : {}", id);
        justificationTypeRepository.deleteById(id);
    }

    /**
     * Trims the justification type name and enforces its uniqueness case-insensitively.
     * <p>
     * When {@code excludeId} is not {@code null}, the justification type with that id is ignored so an
     * update that keeps the same name does not collide with itself. On create {@code excludeId} is
     * {@code null} and every existing justification type is considered. The trimmed name is written
     * back onto the entity so the stored value is consistent.
     *
     * @param justificationType the justification type whose name is normalized and validated.
     * @param excludeId the id to exclude from the uniqueness check, or {@code null} on create.
     * @throws JustificationTypeNameAlreadyUsedException if another justification type with the same name exists.
     */
    private void validateAndNormalizeName(JustificationType justificationType, String excludeId) {
        if (justificationType.getName() == null) {
            return;
        }
        String name = justificationType.getName().trim();
        justificationType.setName(name);
        boolean duplicate =
            excludeId == null
                ? justificationTypeRepository.existsByNameIgnoreCase(name)
                : justificationTypeRepository.existsByNameIgnoreCaseAndIdNot(name, excludeId);
        if (duplicate) {
            throw new JustificationTypeNameAlreadyUsedException();
        }
    }
}
