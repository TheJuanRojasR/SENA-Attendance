package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.DocumentType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the DocumentType entity.
 */
@Repository
public interface DocumentTypeRepository extends MongoRepository<DocumentType, String> {}
