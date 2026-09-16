package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.Alerta;
import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.AlertaState;
import com.mycompany.senaattendance.domain.enumeration.AlertaType;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the {@link Alerta} entity.
 */
@Repository
public interface AlertaRepository extends MongoRepository<Alerta, String>, AlertaRepositoryCustom {
    /**
     * Finds the most recent alert of one apprentice for one materia in a trimester and with one
     * of the given states. Used to look for the active alert of the consecutive combination
     * (UC013, E1); the state collection excludes the resolved alerts, which are history.
     *
     * @param student the apprentice profile.
     * @param classSection the materia of the alert.
     * @param trimester the trimester the alert belongs to.
     * @param type the alert type.
     * @param states the states the alert must be in.
     * @return the matching alert, or empty when there is none.
     */
    Optional<Alerta> findFirstByStudentAndClassSectionAndTrimesterAndTypeAndStateInOrderByGeneratedAtDesc(
        UserProfile student,
        ClassSection classSection,
        Trimester trimester,
        AlertaType type,
        Collection<AlertaState> states
    );

    /**
     * Finds the most recent alert of one apprentice for one ficha in a trimester and with one of
     * the given states. Used to look for the active alert of the accumulated combination
     * (UC013, E1), whose scope is the whole ficha.
     *
     * @param student the apprentice profile.
     * @param grade the ficha of the alert.
     * @param trimester the trimester the alert belongs to.
     * @param type the alert type.
     * @param states the states the alert must be in.
     * @return the matching alert, or empty when there is none.
     */
    Optional<Alerta> findFirstByStudentAndGradeAndTrimesterAndTypeAndStateInOrderByGeneratedAtDesc(
        UserProfile student,
        Grade grade,
        Trimester trimester,
        AlertaType type,
        Collection<AlertaState> states
    );
}
