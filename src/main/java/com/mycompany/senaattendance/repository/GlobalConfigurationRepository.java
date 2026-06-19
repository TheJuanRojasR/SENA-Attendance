package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.GlobalConfiguration;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the GlobalConfiguration entity.
 */
@Repository
public interface GlobalConfigurationRepository extends MongoRepository<GlobalConfiguration, String> {}
