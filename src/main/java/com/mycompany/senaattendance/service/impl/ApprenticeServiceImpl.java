package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.Apprentice;
import com.mycompany.senaattendance.repository.ApprenticeRepository;
import com.mycompany.senaattendance.service.ApprenticeService;
import com.mycompany.senaattendance.service.dto.ApprenticeDTO;
import com.mycompany.senaattendance.service.mapper.ApprenticeMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.mycompany.senaattendance.domain.Apprentice}.
 */
@Service
public class ApprenticeServiceImpl implements ApprenticeService {

    private static final Logger LOG = LoggerFactory.getLogger(ApprenticeServiceImpl.class);

    private final ApprenticeRepository apprenticeRepository;

    private final ApprenticeMapper apprenticeMapper;

    public ApprenticeServiceImpl(ApprenticeRepository apprenticeRepository, ApprenticeMapper apprenticeMapper) {
        this.apprenticeRepository = apprenticeRepository;
        this.apprenticeMapper = apprenticeMapper;
    }

    @Override
    public ApprenticeDTO save(ApprenticeDTO apprenticeDTO) {
        LOG.debug("Request to save Apprentice : {}", apprenticeDTO);
        Apprentice apprentice = apprenticeMapper.toEntity(apprenticeDTO);
        apprentice = apprenticeRepository.save(apprentice);
        return apprenticeMapper.toDto(apprentice);
    }

    @Override
    public ApprenticeDTO update(ApprenticeDTO apprenticeDTO) {
        LOG.debug("Request to update Apprentice : {}", apprenticeDTO);
        Apprentice apprentice = apprenticeMapper.toEntity(apprenticeDTO);
        apprentice = apprenticeRepository.save(apprentice);
        return apprenticeMapper.toDto(apprentice);
    }

    @Override
    public Optional<ApprenticeDTO> partialUpdate(ApprenticeDTO apprenticeDTO) {
        LOG.debug("Request to partially update Apprentice : {}", apprenticeDTO);

        return apprenticeRepository
            .findById(apprenticeDTO.getId())
            .map(existingApprentice -> {
                apprenticeMapper.partialUpdate(existingApprentice, apprenticeDTO);

                return existingApprentice;
            })
            .map(apprenticeRepository::save)
            .map(apprenticeMapper::toDto);
    }

    @Override
    public Page<ApprenticeDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Apprentices");
        return apprenticeRepository.findAll(pageable).map(apprenticeMapper::toDto);
    }

    public Page<ApprenticeDTO> findAllWithEagerRelationships(Pageable pageable) {
        return apprenticeRepository.findAllWithEagerRelationships(pageable).map(apprenticeMapper::toDto);
    }

    @Override
    public Optional<ApprenticeDTO> findOne(String id) {
        LOG.debug("Request to get Apprentice : {}", id);
        return apprenticeRepository.findOneWithEagerRelationships(id).map(apprenticeMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete Apprentice : {}", id);
        apprenticeRepository.deleteById(id);
    }
}
