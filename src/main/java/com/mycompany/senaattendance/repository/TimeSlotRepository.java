package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.TimeSlot;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the TimeSlot entity.
 */
@Repository
public interface TimeSlotRepository extends MongoRepository<TimeSlot, String> {
    List<TimeSlot> findTimeSlotByIsActive(Boolean isActive);
}
