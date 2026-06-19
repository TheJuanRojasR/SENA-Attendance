package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.Program;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the Program entity.
 */
@Repository
public interface ProgramRepository extends MongoRepository<Program, String> {}
