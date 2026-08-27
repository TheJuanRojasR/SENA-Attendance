package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.ClassSection;
import com.mycompany.senaattendance.domain.Grade;
import com.mycompany.senaattendance.domain.enumeration.StateGrade;
import com.mycompany.senaattendance.repository.*;
import com.mycompany.senaattendance.security.AuthoritiesConstants;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.DashboardService;
import com.mycompany.senaattendance.service.dto.dashboard.AdminDashboardDTO;
import com.mycompany.senaattendance.service.dto.dashboard.DashboardDTO;
import com.mycompany.senaattendance.service.dto.dashboard.DashboardKpisDTO;
import com.mycompany.senaattendance.service.dto.dashboard.RecentGradeDTO;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardServiceImpl.class);

    private final UserProfileRepository userProfileRepository;
    private final GradeRepository gradeRepository;
    private final ProgramRepository programRepository;
    private final ModalityRepository modalityRepository;
    private final ClassSectionRepository classSectionRepository;

    public DashboardServiceImpl(
        UserProfileRepository userProfileRepository,
        GradeRepository gradeRepository,
        ProgramRepository programRepository,
        ModalityRepository modalityRepository,
        ClassSectionRepository classSectionRepository
    ) {
        this.userProfileRepository = userProfileRepository;
        this.gradeRepository = gradeRepository;
        this.programRepository = programRepository;
        this.modalityRepository = modalityRepository;
        this.classSectionRepository = classSectionRepository;
    }

    @Override
    public DashboardDTO getDashboardForCurrentUser() {
        LOG.debug("Request to get dashboard for current user");

        if (SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.ADMIN)) {
            return buildAdminDashboard();
        }

        // if (SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.INSTRUCTOR)) {
        //     return buildInstructorDashboard();   // futuro
        // }
        // if (SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.APPRENTICE)) {
        //     return buildApprenticeDashboard();   // futuro
        // }

        return new AdminDashboardDTO(buildAdminKpis(), buildRecentGrades()); // Temporal
    }

    // ------- ADMIN DASHBOARD -------
    private AdminDashboardDTO buildAdminDashboard() {
        return new AdminDashboardDTO(buildAdminKpis(), buildRecentGrades());
    }

    private DashboardKpisDTO buildAdminKpis() {
        return new DashboardKpisDTO(
            userProfileRepository.count(),
            gradeRepository.countByState(StateGrade.ACTIVA),
            programRepository.count(),
            modalityRepository.count()
        );
    }

    private List<RecentGradeDTO> buildRecentGrades() {
        return gradeRepository.findTop5ByOrderByCreatedDateDesc().stream().map(this::toRecentGradeDTO).toList();
    }

    private RecentGradeDTO toRecentGradeDTO(Grade grade) {
        return new RecentGradeDTO(
            grade.getId(),
            grade.getCode(),
            grade.getProgram() != null ? grade.getProgram().getName() : null,
            resolveInstructorName(grade.getId()),
            grade.getState()
        );
    }

    private String resolveInstructorName(String gradeId) {
        return classSectionRepository
            .findByGradeId(gradeId)
            .stream()
            .map(ClassSection::getInstructor)
            .filter(java.util.Objects::nonNull)
            .findFirst()
            .map(instructor -> instructor.getFirstName() + " " + instructor.getFirstLastName())
            .orElse(null);
    }

    // ------- INSTRUCTOR DASHBOARD -------
}
