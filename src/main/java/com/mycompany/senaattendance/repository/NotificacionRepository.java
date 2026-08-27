package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.Notificacion;
import com.mycompany.senaattendance.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the {@link Notificacion} entity.
 */
@Repository
public interface NotificacionRepository extends MongoRepository<Notificacion, String> {
    Page<Notificacion> findByUser(User user, Pageable pageable);
}
