package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.JustificationDetails;
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
 * Mongo implementation of the filtered instructor tray and history. A part references the materia
 * and the justification through DBRefs, so both are matched by their referenced id ({@code $id}).
 */
public class JustificationDetailsRepositoryImpl implements JustificationDetailsRepositoryCustom {

    private static final String CLASS_SECTION_ID = "classSection.$id";

    private static final String JUSTIFICATION_ID = "justification.$id";

    private static final String STATE_JUSTIFICATION = "state_justification";

    private final MongoTemplate mongoTemplate;

    public JustificationDetailsRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<JustificationDetails> searchParts(
        JustificationDetailsSearchCriteria criteria,
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

        if (criteria.stateJustification() != null) {
            conditions.add(Criteria.where(STATE_JUSTIFICATION).is(criteria.stateJustification()));
        }

        if (criteria.justificationIds() != null) {
            if (criteria.justificationIds().isEmpty()) {
                return Page.empty(pageable);
            }
            conditions.add(Criteria.where(JUSTIFICATION_ID).in(criteria.justificationIds()));
        }

        Query query = new Query();
        if (!conditions.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(conditions));
        }

        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), JustificationDetails.class);
        return new PageImpl<>(mongoTemplate.find(query.with(pageable), JustificationDetails.class), pageable, total);
    }

    /**
     * @param id the candidate id.
     * @return the id as an {@code ObjectId}, or empty when it cannot be one.
     */
    private static Optional<ObjectId> toObjectId(String id) {
        return id != null && ObjectId.isValid(id) ? Optional.of(new ObjectId(id)) : Optional.empty();
    }
}
