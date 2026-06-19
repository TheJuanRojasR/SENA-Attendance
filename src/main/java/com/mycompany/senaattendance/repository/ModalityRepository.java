package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.Modality;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the Modality entity.
 */
@Repository
public interface ModalityRepository extends MongoRepository<Modality, String> {}
