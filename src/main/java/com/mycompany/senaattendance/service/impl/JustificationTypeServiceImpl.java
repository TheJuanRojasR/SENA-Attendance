package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.JustificationType;
import com.mycompany.senaattendance.repository.JustificationTypeRepository;
import com.mycompany.senaattendance.service.JustificationTypeService;
import com.mycompany.senaattendance.service.dto.JustificationTypeDTO;
import com.mycompany.senaattendance.service.mapper.JustificationTypeMapper;
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
        justificationType = justificationTypeRepository.save(justificationType);
        return justificationTypeMapper.toDto(justificationType);
    }

    @Override
    public JustificationTypeDTO update(JustificationTypeDTO justificationTypeDTO) {
        LOG.debug("Request to update JustificationType : {}", justificationTypeDTO);
        JustificationType justificationType = justificationTypeMapper.toEntity(justificationTypeDTO);
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
}
