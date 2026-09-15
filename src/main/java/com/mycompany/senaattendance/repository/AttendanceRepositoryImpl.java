package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.Attendance;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
 * Mongo implementation of the filtered attendance history. Attendance references the materia and
 * the apprentice through DBRefs, so both are matched by their referenced id ({@code $id}).
 */
public class AttendanceRepositoryImpl implements AttendanceRepositoryCustom {

    private static final String CLASS_SECTION_ID = "classSection.$id";

    private static final String STUDENT_ID = "student.$id";

    private final MongoTemplate mongoTemplate;

    public AttendanceRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<Attendance> searchAttendanceHistory(
        AttendanceSearchCriteria criteria,
        List<ObjectId> classSectionScope,
        Pageable pageable
    ) {
        List<Criteria> conditions = new ArrayList<>();
        if (classSectionScope != null) {
            if (classSectionScope.isEmpty()) {
                return Page.empty(pageable);
            }
            conditions.add(Criteria.where(CLASS_SECTION_ID).in(classSectionScope));
        }

        if (criteria.classSectionId() != null) {
            Optional<ObjectId> classSectionId = toObjectId(criteria.classSectionId());
            if (classSectionId.isEmpty()) {
                return Page.empty(pageable);
            }
            conditions.add(Criteria.where(CLASS_SECTION_ID).is(classSectionId.get()));
        }

        if (criteria.date() != null) {
            conditions.add(Criteria.where("date").is(criteria.date()));
        }

        if (criteria.studentId() != null) {
            Optional<ObjectId> studentId = toObjectId(criteria.studentId());
            if (studentId.isEmpty()) {
                return Page.empty(pageable);
            }
            conditions.add(Criteria.where(STUDENT_ID).is(studentId.get()));
        }

        if (criteria.stateAttendance() != null) {
            conditions.add(Criteria.where("state_attendance").is(criteria.stateAttendance()));
        }

        Query query = new Query();
        if (!conditions.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(conditions));
        }

        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Attendance.class);
        return new PageImpl<>(mongoTemplate.find(query.with(pageable), Attendance.class), pageable, total);
    }

    /**
     * @param id the candidate id.
     * @return the id as an {@code ObjectId}, or empty when it cannot be one.
     */
    private static Optional<ObjectId> toObjectId(String id) {
        return id != null && ObjectId.isValid(id) ? Optional.of(new ObjectId(id)) : Optional.empty();
    }
}
