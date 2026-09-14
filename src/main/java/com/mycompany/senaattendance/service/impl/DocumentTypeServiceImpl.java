package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.DocumentType;
import com.mycompany.senaattendance.repository.DocumentTypeRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.DocumentTypeService;
import com.mycompany.senaattendance.service.dto.DocumentTypeDTO;
import com.mycompany.senaattendance.service.mapper.DocumentTypeMapper;
import com.mycompany.senaattendance.web.rest.errors.BadRequestAlertException;
import com.mycompany.senaattendance.web.rest.errors.DocumentTypeInitialsAlreadyUsedException;
import com.mycompany.senaattendance.web.rest.errors.DocumentTypeNameAlreadyUsedException;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for managing {@link com.mycompany.senaattendance.domain.DocumentType}.
 */
@Service
public class DocumentTypeServiceImpl implements DocumentTypeService {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentTypeServiceImpl.class);

    private final DocumentTypeRepository documentTypeRepository;

    private final DocumentTypeMapper documentTypeMapper;

    private final UserProfileRepository userProfileRepository;

    public DocumentTypeServiceImpl(
        DocumentTypeRepository documentTypeRepository,
        DocumentTypeMapper documentTypeMapper,
        UserProfileRepository userProfileRepository
    ) {
        this.documentTypeRepository = documentTypeRepository;
        this.documentTypeMapper = documentTypeMapper;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public DocumentTypeDTO save(DocumentTypeDTO documentTypeDTO) {
        LOG.debug("Request to save DocumentType : {}", documentTypeDTO);
        DocumentType documentType = documentTypeMapper.toEntity(documentTypeDTO);
        validateAndNormalizeName(documentType, null);
        validateAndNormalizeInitials(documentType, null);

        // Los tipos nuevos nacen activos
        if (documentType.getIsActive() == null) {
            documentType.setIsActive(Boolean.TRUE);
        }

        // Inserta la fecha de creación
        documentType.setCreatedDate(Instant.now());
        // Si existe el usuario se inserta quien lo creo
        Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
        if (currentUserLogin.isPresent()) {
            documentType.setCreatedBy(currentUserLogin.get());
        }

        documentType = documentTypeRepository.save(documentType);
        return documentTypeMapper.toDto(documentType);
    }

    @Override
    public DocumentTypeDTO update(DocumentTypeDTO documentTypeDTO) {
        LOG.debug("Request to update DocumentType : {}", documentTypeDTO);
        DocumentType documentType = documentTypeMapper.toEntity(documentTypeDTO);
        validateAndNormalizeName(documentType, documentType.getId());
        validateAndNormalizeInitials(documentType, documentType.getId());

        // Trae el documentType por ID
        Optional<DocumentType> optionalDocumentType = documentTypeRepository.findById(documentType.getId());
        if (optionalDocumentType.isPresent()) {
            DocumentType existingDocumentType = optionalDocumentType.get();
            validateInitialsChangeAllowed(documentType, existingDocumentType.getInitials());
            documentType.setCreatedBy(existingDocumentType.getCreatedBy());
            documentType.setCreatedDate(existingDocumentType.getCreatedDate());
        } else {
            documentType.setCreatedDate(Instant.now());
            Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
            if (currentUserLogin.isPresent()) {
                documentType.setCreatedBy(currentUserLogin.get());
            }
        }

        // El DTO puede no enviar isActive (por ejemplo, el PUT actual del admin):
        // conserva el estado existente o, si no existe, nace activo.
        if (documentType.getIsActive() == null) {
            documentType.setIsActive(optionalDocumentType.map(DocumentType::getIsActive).orElse(Boolean.TRUE));
        }

        documentType = documentTypeRepository.save(documentType);
        return documentTypeMapper.toDto(documentType);
    }

    @Override
    public Optional<DocumentTypeDTO> partialUpdate(DocumentTypeDTO documentTypeDTO) {
        LOG.debug("Request to partially update DocumentType : {}", documentTypeDTO);

        return documentTypeRepository
            .findById(documentTypeDTO.getId())
            .map(existingDocumentType -> {
                String originalInitials = existingDocumentType.getInitials();
                documentTypeMapper.partialUpdate(existingDocumentType, documentTypeDTO);
                validateAndNormalizeName(existingDocumentType, existingDocumentType.getId());
                validateAndNormalizeInitials(existingDocumentType, existingDocumentType.getId());
                validateInitialsChangeAllowed(existingDocumentType, originalInitials);

                return existingDocumentType;
            })
            .map(documentTypeRepository::save)
            .map(documentTypeMapper::toDto);
    }

    @Override
    public List<DocumentTypeDTO> findAll() {
        LOG.debug("Request to get all DocumentTypes");
        return documentTypeRepository.findAll().stream().map(documentTypeMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public Optional<DocumentTypeDTO> findOne(String id) {
        LOG.debug("Request to get DocumentType : {}", id);
        return documentTypeRepository.findById(id).map(documentTypeMapper::toDto);
    }

    @Override
    public void delete(String id) {
        LOG.debug("Request to delete DocumentType : {}", id);
        if (userProfileRepository.existsByDocumentTypeId(id)) {
            throw new BadRequestAlertException(
                "This document type is in use by users and cannot be deleted",
                "documentType",
                "documentTypeInUse"
            );
        }
        documentTypeRepository.deleteById(id);
    }

    /**
     * Trims the document type name and enforces its uniqueness case-insensitively.
     * <p>
     * When {@code excludeId} is not {@code null}, the document type with that id is ignored so an
     * update that keeps the same name does not collide with itself. On create {@code excludeId}
     * is {@code null} and every existing document type is considered. The trimmed name is written
     * back onto the entity so the stored value is consistent.
     *
     * @param documentType the document type whose name is normalized and validated.
     * @param excludeId the id to exclude from the uniqueness check, or {@code null} on create.
     * @throws DocumentTypeNameAlreadyUsedException if another document type with the same name exists.
     */
    private void validateAndNormalizeName(DocumentType documentType, String excludeId) {
        if (documentType.getName() == null) {
            return;
        }
        String name = documentType.getName().trim();
        documentType.setName(name);
        boolean duplicate =
            excludeId == null
                ? documentTypeRepository.existsByNameIgnoreCase(name)
                : documentTypeRepository.existsByNameIgnoreCaseAndIdNot(name, excludeId);
        if (duplicate) {
            throw new DocumentTypeNameAlreadyUsedException();
        }
    }

    /**
     * Trims and uppercases the document type initials and enforces their uniqueness case-insensitively.
     * <p>
     * Uppercasing keeps the stored initials canonical, since they build the derived login
     * {@code <initials>_<documentNumber>}. When {@code excludeId} is not {@code null}, the document
     * type with that id is ignored so an update that keeps the same initials does not collide with
     * itself. On create {@code excludeId} is {@code null} and every existing document type is considered.
     *
     * @param documentType the document type whose initials are normalized and validated.
     * @param excludeId the id to exclude from the uniqueness check, or {@code null} on create.
     * @throws DocumentTypeInitialsAlreadyUsedException if another document type with the same initials exists.
     */
    private void validateAndNormalizeInitials(DocumentType documentType, String excludeId) {
        if (documentType.getInitials() == null) {
            return;
        }
        String initials = documentType.getInitials().trim().toUpperCase();
        documentType.setInitials(initials);
        boolean duplicate =
            excludeId == null
                ? documentTypeRepository.existsByInitialsIgnoreCase(initials)
                : documentTypeRepository.existsByInitialsIgnoreCaseAndIdNot(initials, excludeId);
        if (duplicate) {
            throw new DocumentTypeInitialsAlreadyUsedException();
        }
    }

    /**
     * Blocks changing the initials of a document type that is still referenced by user profiles,
     * because those profiles derive their login from the current initials.
     * <p>
     * Initials are compared after normalizing, so an update that only rewrites the same initials
     * with a different casing is allowed. Renaming the document type is never blocked by this rule.
     *
     * @param documentType the document type holding the new initials.
     * @param originalInitials the initials currently persisted, or {@code null} if unknown.
     * @throws BadRequestAlertException if the initials changed and the document type is in use.
     */
    private void validateInitialsChangeAllowed(DocumentType documentType, String originalInitials) {
        String initials = documentType.getInitials();
        if (initials == null) {
            return;
        }
        String normalizedOriginal = originalInitials == null ? null : originalInitials.trim().toUpperCase();
        if (initials.equals(normalizedOriginal)) {
            return;
        }
        if (documentType.getId() != null && userProfileRepository.existsByDocumentTypeId(documentType.getId())) {
            throw new BadRequestAlertException(
                "The initials of a document type in use cannot be changed",
                "documentType",
                "documentTypeInitialsInUse"
            );
        }
    }
}
