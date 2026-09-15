package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.Notificacion;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.enumeration.NotificacionEstado;
import com.mycompany.senaattendance.domain.enumeration.NotificacionTipo;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the {@link Notificacion} entity.
 */
@Repository
public interface NotificacionRepository extends MongoRepository<Notificacion, String>, NotificacionRepositoryCustom {
    Page<Notificacion> findByUser(User user, Pageable pageable);

    /**
     * Returns the most recent notification of one user with one of the given delivery states.
     * Used to close the credential fallback of UC006 (E7) when the Administrator resends the
     * access: the open notification (pending or retryable) is the one the resend updates.
     *
     * @param user the owner of the notification.
     * @param tipo the notification type.
     * @param estados the delivery states to match.
     * @return the most recent matching notification, or empty when there is none.
     */
    Optional<Notificacion> findFirstByUserAndTipoAndEstadoInOrderByCreatedDateDesc(
        User user,
        NotificacionTipo tipo,
        Collection<NotificacionEstado> estados
    );
}
