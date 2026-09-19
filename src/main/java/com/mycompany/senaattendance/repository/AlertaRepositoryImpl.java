package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.Alerta;
import com.mycompany.senaattendance.domain.enumeration.AlertaState;
import com.mycompany.senaattendance.domain.enumeration.AlertaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
 * Mongo implementation of the alert inbox (UC013, A1). The relationships are DBRefs, so they are
 * matched by their referenced id ({@code $id}) with explicit {@code ObjectId} values, the same way
 * the other DBRef searches of the project do.
 */
public class AlertaRepositoryImpl implements AlertaRepositoryCustom {

    private static final String STUDENT_ID = "student.$id";

    private static final String CLASS_SECTION_ID = "classSection.$id";

    private static final String GRADE_ID = "grade.$id";

    private static final String GENERATED_AT = "generated_at";

    private static final String TYPE = "type";

    private static final String STATE = "state";

    private final MongoTemplate mongoTemplate;

    public AlertaRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<Alerta> searchAlerts(AlertaSearchCriteria criteria, AlertaReadScope scope, Pageable pageable) {
        if (scope != null && scope.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Criteria> conditions = new ArrayList<>();
        if (criteria.type() != null) {
            conditions.add(Criteria.where(TYPE).is(criteria.type()));
        }
        if (criteria.state() != null) {
            conditions.add(Criteria.where(STATE).is(criteria.state()));
        }
        if (criteria.gradeId() != null) {
            Optional<ObjectId> gradeId = toObjectId(criteria.gradeId());
            if (gradeId.isEmpty()) {
                return Page.empty(pageable);
            }
            conditions.add(Criteria.where(GRADE_ID).is(gradeId.get()));
        }
        if (criteria.studentId() != null) {
            Optional<ObjectId> studentId = toObjectId(criteria.studentId());
            if (studentId.isEmpty()) {
                return Page.empty(pageable);
            }
            conditions.add(Criteria.where(STUDENT_ID).is(studentId.get()));
        }
        if (criteria.from() != null) {
            conditions.add(Criteria.where(GENERATED_AT).gte(criteria.from()));
        }
        if (criteria.to() != null) {
            conditions.add(Criteria.where(GENERATED_AT).lte(criteria.to()));
        }
        if (scope != null && !scope.isUnrestricted()) {
            conditions.add(readableScope(scope));
        }

        // An unrestricted read without filters has no condition at all: an empty $and is invalid.
        Query query = conditions.isEmpty() ? new Query() : new Query(new Criteria().andOperator(conditions));
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Alerta.class);
        return new PageImpl<>(mongoTemplate.find(query.with(pageable), Alerta.class), pageable, total);
    }

    @Override
    public long countActiveAlerts(AlertaReadScope scope, String studentId) {
        if (scope != null && scope.isEmpty()) {
            return 0;
        }

        List<Criteria> conditions = new ArrayList<>();
        conditions.add(Criteria.where(STATE).ne(AlertaState.RESUELTA_AUTOMATICAMENTE));
        if (studentId != null) {
            Optional<ObjectId> student = toObjectId(studentId);
            if (student.isEmpty()) {
                return 0;
            }
            conditions.add(Criteria.where(STUDENT_ID).is(student.get()));
        }
        if (scope != null && !scope.isUnrestricted()) {
            conditions.add(readableScope(scope));
        }

        return mongoTemplate.count(new Query(new Criteria().andOperator(conditions)), Alerta.class);
    }

    /**
     * Builds the readable scope of an instructor as a single clause: a consecutive alert of one of
     * their materias, or an accumulated alert of one of their fichas.
     *
     * @param scope the restricted scope of the current user.
     * @return the clause that keeps only the readable alerts.
     */
    private static Criteria readableScope(AlertaReadScope scope) {
        return new Criteria().orOperator(
            Criteria.where(TYPE).is(AlertaType.CONSECUTIVAS).and(CLASS_SECTION_ID).in(toObjectIds(scope.classSectionIds())),
            Criteria.where(TYPE).is(AlertaType.ACUMULADAS).and(GRADE_ID).in(toObjectIds(scope.gradeIds()))
        );
    }

    /**
     * @param ids the candidate id strings, possibly {@code null}.
     * @return the usable {@code ObjectId} values of the collection, possibly empty.
     */
    private static Set<ObjectId> toObjectIds(Set<String> ids) {
        if (ids == null) {
            return Set.of();
        }
        return ids.stream().map(AlertaRepositoryImpl::toObjectId).flatMap(Optional::stream).collect(Collectors.toSet());
    }

    /**
     * @param id the candidate id.
     * @return the id as an {@code ObjectId}, or empty when it cannot be one.
     */
    private static Optional<ObjectId> toObjectId(String id) {
        return id != null && ObjectId.isValid(id) ? Optional.of(new ObjectId(id)) : Optional.empty();
    }
}
