package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.UserProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the UserProfile entity.
 */
@Repository
public interface UserProfileRepository extends MongoRepository<UserProfile, String> {
    @Query("{}")
    Page<UserProfile> findAllWithEagerRelationships(Pageable pageable);

    @Query("{}")
    List<UserProfile> findAllWithEagerRelationships();

    @Query("{'id': ?0}")
    Optional<UserProfile> findOneWithEagerRelationships(String id);
}
