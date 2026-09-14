package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.Modality;
import com.mycompany.senaattendance.repository.GradeRepository;
import com.mycompany.senaattendance.repository.ModalityRepository;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.ModalityService;
import com.mycompany.senaattendance.service.dto.ModalityDTO;
import com.mycompany.senaattendance.service.mapper.ModalityMapper;
import com.mycompany.senaattendance.web.rest.errors.BadRequestAlertException;
import com.mycompany.senaattendance.web.rest.errors.ModalityNameAlreadyUsedException;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.mycompany.senaattendance.domain.Modality}.
 */
@Service
public class ModalityServiceImpl implements ModalityService {

    private static final Logger LOG = LoggerFactory.getLogger(ModalityServiceImpl.class);

    private final ModalityRepository modalityRepository;

    private final ModalityMapper modalityMapper;

    private final GradeRepository gradeRepository;

    public ModalityServiceImpl(ModalityRepository modalityRepository, ModalityMapper modalityMapper, GradeRepository gradeRepository) {
        this.modalityRepository = modalityRepository;
        this.modalityMapper = modalityMapper;
        this.gradeRepository = gradeRepository;
    }

    @Override
    public ModalityDTO save(ModalityDTO modalityDTO) {
        LOG.debug("Request to save Modality : {}", modalityDTO);
        Modality modality = modalityMapper.toEntity(modalityDTO);

        modality.setIsActive(true);
        validateAndNormalizeName(modality, null);

        // Inserta fecha de creación
        modality.setCreatedDate(Instant.now());
        Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
        if (currentUserLogin.isPresent()) {
            // Inserta quien lo creo
            modality.setCreatedBy(currentUserLogin.get());
        }

        modality = modalityRepository.save(modality);
        return modalityMapper.toDto(modality);
    }

    @Override
    public ModalityDTO update(ModalityDTO modalityDTO) {
        LOG.debug("Request to update Modality : {}", modalityDTO);
        Modality modality = modalityMapper.toEntity(modalityDTO);

        validateAndNormalizeName(modality, modality.getId());

        Optional<Modality> optionalModality = modalityRepository.findById(modality.getId());
        if (optionalModality.isPresent()) {
            Modality existingModality = optionalModality.get();
            modality.setCreatedBy(existingModality.getCreatedBy());
            modality.setCreatedDate(existingModality.getCreatedDate());
        } else {
            modality.setCreatedDate(Instant.now());
            Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
            if (currentUserLogin.isPresent()) {
                modality.setCreatedBy(currentUserLogin.get());
            }
        }

        modality = modalityRepository.save(modality);
        return modalityMapper.toDto(modality);
    }

    @Override
    public Optional<ModalityDTO> partialUpdate(ModalityDTO modalityDTO) {
        LOG.debug("Request to partially update Modality : {}", modalityDTO);

        return modalityRepository
            .findById(modalityDTO.getId())
            .map(existingModality -> {
                modalityMapper.partialUpdate(existingModality, modalityDTO);
                validateAndNormalizeName(existingModality, existingModality.getId());

                return existingModality;
            })
            .map(modalityRepository::save)
            .map(modalityMapper::toDto);
    }

    @Override
    public List<ModalityDTO> findAll() {
        LOG.debug("Request to get all Modalities");
        return modalityRepository.findAll().stream().map(modalityMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public Optional<ModalityDTO> findOne(String id) {
        LOG.debug("Request to get Modality : {}", id);
        return modalityRepository.findById(id).map(modalityMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete Modality : {}", id);
        if (gradeRepository.existsByModalityId(id)) {
            throw new BadRequestAlertException("This modality is assigned to fichas and cannot be deleted", "modality", "modalityInUse");
        }
        modalityRepository.deleteById(id);
    }

    @Override
    public List<ModalityDTO> findActiveModalities() {
        LOG.debug("Request to get all active Modalities");
        return modalityRepository
            .findModalityByIsActive(true)
            .stream()
            .map(modalityMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Trims the modality name and enforces its uniqueness case-insensitively.
     * <p>
     * When {@code excludeId} is not {@code null}, the modality with that id is ignored so an
     * update that keeps the same name does not collide with itself. On create {@code excludeId}
     * is {@code null} and every existing modality is considered. The trimmed name is written
     * back onto the entity so the stored value is consistent.
     *
     * @param modality the modality whose name is normalized and validated.
     * @param excludeId the id to exclude from the uniqueness check, or {@code null} on create.
     * @throws ModalityNameAlreadyUsedException if another modality with the same name exists.
     */
    private void validateAndNormalizeName(Modality modality, String excludeId) {
        if (modality.getName() == null) {
            return;
        }
        String name = modality.getName().trim();
        modality.setName(name);
        boolean duplicate =
            excludeId == null
                ? modalityRepository.existsByNameIgnoreCase(name)
                : modalityRepository.existsByNameIgnoreCaseAndIdNot(name, excludeId);
        if (duplicate) {
            throw new ModalityNameAlreadyUsedException();
        }
    }
}
