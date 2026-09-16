package com.mycompany.senaattendance.service.impl;

import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.StateTrimester;
import com.mycompany.senaattendance.repository.TrimesterRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.security.SecurityUtils;
import com.mycompany.senaattendance.service.TrimesterService;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Shared context of the authenticated user for the services that scope their data by role: the
 * profile behind the current login and the active trimester of today. Both resolutions were
 * duplicated across services, so they live here once.
 */
@Component
public class CurrentUserContext {

    /**
     * Message the dashboards expose when no trimester covers today (UC023, E2).
     */
    public static final String NO_ACTIVE_TRIMESTER_MESSAGE = "No hay un trimestre activo";

    private final UserRepository userRepository;

    private final UserProfileRepository userProfileRepository;

    private final TrimesterRepository trimesterRepository;

    private final TrimesterService trimesterService;

    private final Clock clock;

    public CurrentUserContext(
        UserRepository userRepository,
        UserProfileRepository userProfileRepository,
        TrimesterRepository trimesterRepository,
        TrimesterService trimesterService,
        Clock clock
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.trimesterRepository = trimesterRepository;
        this.trimesterService = trimesterService;
        this.clock = clock;
    }

    /**
     * @return the profile of the authenticated user, or empty when the session has no login or
     *         the account has no profile.
     */
    public Optional<UserProfile> profile() {
        return SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .flatMap(user -> userProfileRepository.findOneByUserId(user.getId()));
    }

    /**
     * @return the id of the profile of the authenticated user, or {@code null} when it cannot be
     *         resolved.
     */
    public String profileId() {
        return profile().map(UserProfile::getId).orElse(null);
    }

    /**
     * @return the trimester that contains today and classifies as active, or empty when no
     *         trimester covers today.
     */
    public Optional<Trimester> activeTrimester() {
        LocalDate today = today();
        return trimesterRepository
            .findAllContaining(today)
            .stream()
            .filter(trimester -> trimesterService.classify(trimester) == StateTrimester.ACTIVO)
            .findFirst();
    }

    /**
     * @return today in the configured clock time zone.
     */
    public LocalDate today() {
        return LocalDate.now(clock);
    }
}
