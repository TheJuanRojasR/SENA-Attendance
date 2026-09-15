package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.JustificationDetails;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Custom fragment of {@link JustificationDetailsRepository} that resolves the instructor tray and
 * history (UC010, A1) in one dynamic query, so the optional filters and the readable scope are
 * combined without one repository method per filter combination.
 */
public interface JustificationDetailsRepositoryCustom {
    /**
     * Searches the parts by the optional filters, restricted to the readable class sections.
     *
     * @param criteria the optional filters; a {@code null} field means no constraint.
     * @param classSectionScope the ObjectId values of the readable class sections, or
     *        {@code null} to search the parts of every materia.
     * @param pageable the pagination information.
     * @return the page of matching parts.
     */
    Page<JustificationDetails> searchParts(
        JustificationDetailsSearchCriteria criteria,
        List<ObjectId> classSectionScope,
        Pageable pageable
    );
}
