package com.mycompany.senaattendance.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.mycompany.senaattendance.domain.Trimester;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.domain.enumeration.StateTrimester;
import com.mycompany.senaattendance.repository.TrimesterRepository;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.repository.UserRepository;
import com.mycompany.senaattendance.service.TrimesterService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Unit tests for the shared {@link CurrentUserContext}: the resolution of the profile behind the
 * current login and of the trimester that covers today.
 */
@ExtendWith(MockitoExtension.class)
class CurrentUserContextTest {

    private static final String LOGIN = "ana.aprendiz";
    private static final String USER_ID = "65f1a2b3c4d5e6f7a8b9c0a1";
    private static final String PROFILE_ID = "65f1a2b3c4d5e6f7a8b9c0a2";
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 15);

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private TrimesterRepository trimesterRepository;

    @Mock
    private TrimesterService trimesterService;

    private CurrentUserContext currentUserContext;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-09-15T12:00:00Z"), ZoneOffset.UTC);
        currentUserContext = new CurrentUserContext(userRepository, userProfileRepository, trimesterRepository, trimesterService, clock);
        authenticate(LOGIN);
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void profileResolvesTheProfileOfTheCurrentLogin() {
        User user = new User();
        user.setId(USER_ID);
        UserProfile profile = new UserProfile();
        profile.setId(PROFILE_ID);
        when(userRepository.findOneByLogin(LOGIN)).thenReturn(Optional.of(user));
        when(userProfileRepository.findOneByUserId(USER_ID)).thenReturn(Optional.of(profile));

        assertThat(currentUserContext.profile()).contains(profile);
        assertThat(currentUserContext.profileId()).isEqualTo(PROFILE_ID);
    }

    @Test
    void profileIsEmptyWithoutAnAuthenticatedLogin() {
        SecurityContextHolder.clearContext();

        assertThat(currentUserContext.profile()).isEmpty();
        assertThat(currentUserContext.profileId()).isNull();
    }

    @Test
    void profileIsEmptyWhenTheAccountHasNoProfile() {
        User user = new User();
        user.setId(USER_ID);
        when(userRepository.findOneByLogin(LOGIN)).thenReturn(Optional.of(user));
        when(userProfileRepository.findOneByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThat(currentUserContext.profile()).isEmpty();
    }

    @Test
    void activeTrimesterIsTheContainingOneClassifiedAsActive() {
        Trimester trimester = new Trimester();
        when(trimesterRepository.findAllContaining(TODAY)).thenReturn(List.of(trimester));
        when(trimesterService.classify(trimester)).thenReturn(StateTrimester.ACTIVO);

        assertThat(currentUserContext.activeTrimester()).contains(trimester);
    }

    @Test
    void activeTrimesterIsEmptyWhenTheContainingOneIsClosed() {
        Trimester trimester = new Trimester();
        when(trimesterRepository.findAllContaining(TODAY)).thenReturn(List.of(trimester));
        when(trimesterService.classify(trimester)).thenReturn(StateTrimester.CERRADO);

        assertThat(currentUserContext.activeTrimester()).isEmpty();
    }

    @Test
    void activeTrimesterIsEmptyWhenNoTrimesterContainsToday() {
        when(trimesterRepository.findAllContaining(TODAY)).thenReturn(List.of());

        assertThat(currentUserContext.activeTrimester()).isEmpty();
    }

    @Test
    void todayUsesTheConfiguredClock() {
        assertThat(currentUserContext.today()).isEqualTo(TODAY);
    }

    private static void authenticate(String login) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(login, "n/a"));
        SecurityContextHolder.setContext(context);
    }
}
