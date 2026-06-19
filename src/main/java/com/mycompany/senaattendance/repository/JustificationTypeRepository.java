package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.JustificationType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the JustificationType entity.
 */
@Repository
public interface JustificationTypeRepository extends MongoRepository<JustificationType, String> {}
