package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.Notificacion;
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
import org.springframework.data.mongodb.core.query.Update;

/**
 * Mongo implementation of the notification inbox (UC018). The owner is a DBRef, so it is matched
 * by its referenced id ({@code $id}) with explicit {@code ObjectId} values, the same way the
 * other DBRef searches of the project do.
 */
public class NotificacionRepositoryImpl implements NotificacionRepositoryCustom {

    private static final String ID = "_id";

    private static final String USER_ID = "user.$id";

    private static final String READ = "read";

    private static final String TIPO = "tipo";

    private static final String CREATED_DATE = "created_date";

    private final MongoTemplate mongoTemplate;

    public NotificacionRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<Notificacion> searchInbox(String userId, NotificacionSearchCriteria criteria, Pageable pageable) {
        Optional<ObjectId> ownerId = toObjectId(userId);
        if (ownerId.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Criteria> conditions = new ArrayList<>();
        conditions.add(Criteria.where(USER_ID).is(ownerId.get()));
        if (criteria.read() != null) {
            conditions.add(Criteria.where(READ).is(criteria.read()));
        }
        if (criteria.tipo() != null) {
            conditions.add(Criteria.where(TIPO).is(criteria.tipo()));
        }
        if (criteria.from() != null) {
            conditions.add(Criteria.where(CREATED_DATE).gte(criteria.from()));
        }
        if (criteria.to() != null) {
            conditions.add(Criteria.where(CREATED_DATE).lte(criteria.to()));
        }

        Query query = new Query(new Criteria().andOperator(conditions));
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Notificacion.class);
        return new PageImpl<>(mongoTemplate.find(query.with(pageable), Notificacion.class), pageable, total);
    }

    @Override
    public long countUnread(String userId) {
        Optional<ObjectId> ownerId = toObjectId(userId);
        if (ownerId.isEmpty()) {
            return 0L;
        }
        return mongoTemplate.count(unreadQuery(ownerId.get()), Notificacion.class);
    }

    @Override
    public long markAllAsRead(String userId) {
        Optional<ObjectId> ownerId = toObjectId(userId);
        if (ownerId.isEmpty()) {
            return 0L;
        }
        return mongoTemplate.updateMulti(unreadQuery(ownerId.get()), Update.update(READ, true), Notificacion.class).getModifiedCount();
    }

    @Override
    public boolean markAsRead(String id, String userId) {
        Optional<ObjectId> notificationId = toObjectId(id);
        Optional<ObjectId> ownerId = toObjectId(userId);
        if (notificationId.isEmpty() || ownerId.isEmpty()) {
            return false;
        }
        Query query = Query.query(Criteria.where(ID).is(notificationId.get()).and(USER_ID).is(ownerId.get()));
        return mongoTemplate.updateFirst(query, Update.update(READ, true), Notificacion.class).getMatchedCount() == 1;
    }

    private static Query unreadQuery(ObjectId ownerId) {
        return Query.query(Criteria.where(USER_ID).is(ownerId).and(READ).is(false));
    }

    /**
     * @param id the candidate id.
     * @return the id as an {@code ObjectId}, or empty when it cannot be one.
     */
    private static Optional<ObjectId> toObjectId(String id) {
        return id != null && ObjectId.isValid(id) ? Optional.of(new ObjectId(id)) : Optional.empty();
    }
}
