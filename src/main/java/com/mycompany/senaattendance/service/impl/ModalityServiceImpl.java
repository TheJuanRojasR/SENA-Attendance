package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.Modality;
import com.mycompany.senaattendance.repository.ModalityRepository;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.ModalityService;
import com.mycompany.senaattendance.service.dto.ModalityDTO;
import com.mycompany.senaattendance.service.mapper.ModalityMapper;
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

    public ModalityServiceImpl(ModalityRepository modalityRepository, ModalityMapper modalityMapper) {
        this.modalityRepository = modalityRepository;
        this.modalityMapper = modalityMapper;
    }

    @Override
    public ModalityDTO save(ModalityDTO modalityDTO) {
        LOG.debug("Request to save Modality : {}", modalityDTO);
        Modality modality = modalityMapper.toEntity(modalityDTO);

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
        modalityRepository.deleteById(id);
    }
}
