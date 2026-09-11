package com.mycompany.senaattendance.web.rest;

import static com.mycompany.senaattendance.security.SecurityUtils.AUTHORITIES_CLAIM;
import static com.mycompany.senaattendance.security.SecurityUtils.JWT_ALGORITHM;
import static com.mycompany.senaattendance.security.SecurityUtils.USER_ID_CLAIM;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mycompany.senaattendance.domain.User;
import com.mycompany.senaattendance.domain.UserProfile;
import com.mycompany.senaattendance.repository.UserProfileRepository;
import com.mycompany.senaattendance.security.DomainUserDetailsService;
import com.mycompany.senaattendance.security.DomainUserDetailsService.UserWithId;
import com.mycompany.senaattendance.web.rest.vm.LoginVM;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

/**
 * Controller to authenticate users.
 */
@RestController
@RequestMapping("/api")
public class AuthenticateController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticateController.class);

    private final JwtEncoder jwtEncoder;
    private final UserProfileRepository userProfileRepository;

    @Value("${jhipster.security.authentication.jwt.token-validity-in-seconds:0}")
    private long tokenValidityInSeconds;

    @Value("${jhipster.security.authentication.jwt.token-validity-in-seconds-for-remember-me:0}")
    private long tokenValidityInSecondsForRememberMe;

    private final PasswordEncoder passwordEncoder;

    public AuthenticateController(JwtEncoder jwtEncoder, UserProfileRepository userProfileRepository, PasswordEncoder passwordEncoder) {
        this.jwtEncoder = jwtEncoder;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authorize(@Valid @RequestBody LoginVM loginVM) {
        var profileOpt = userProfileRepository.findByDocumentTypeAndDocumentNumber(
            loginVM.getDocumentTypeId(),
            loginVM.getDocumentNumber()
        );

        if (profileOpt.isEmpty()) {
            return unauthorized("badcredentials");
        }

        UserProfile profile = profileOpt.get();
        User user = profile.getUser();

        if (user == null) {
            return unauthorized("badcredentials");
        }

        boolean passwordMatches = passwordEncoder.matches(loginVM.getPassword(), user.getPassword());
        if (!passwordMatches) {
            return unauthorized("badcredentials");
        }

        if (!user.isActivated()) {
            return unauthorized("accountinactive");
        }

        var userDetails = DomainUserDetailsService.UserWithId.fromUser(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = this.createToken(authentication, loginVM.isRememberMe());
        var httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(jwt);

        return new ResponseEntity<>(new JWTToken(jwt, user.isMustChangePassword()), httpHeaders, HttpStatus.OK);
    }

    /**
     * Builds the {@code 401 Unauthorized} response for a failed login, carrying the business
     * error key in the body as {@code message: error.<key>} so the client can tell an invalid
     * credential from an inactive account. The same {@code badcredentials} key is used for an
     * unknown document, a missing user relation and a wrong password, so the response never
     * reveals which part of the credential failed.
     *
     * @param errorKey the business error key, without the {@code error.} prefix.
     * @return the {@link ResponseEntity} with status {@code 401 (Unauthorized)} and the error key.
     */
    private ResponseEntity<Object> unauthorized(String errorKey) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("message", "error." + errorKey));
    }

    /**
     * {@code GET /authenticate} : check if the user is authenticated.
     *
     * @return the {@link ResponseEntity} with status {@code 204 (No Content)},
     * or with status {@code 401 (Unauthorized)} if not authenticated.
     */
    @GetMapping("/authenticate")
    public ResponseEntity<Void> isAuthenticated(Principal principal) {
        LOG.debug("REST request to check if the current user is authenticated");
        return ResponseEntity.status(principal == null ? HttpStatus.UNAUTHORIZED : HttpStatus.NO_CONTENT).build();
    }

    public String createToken(Authentication authentication, boolean rememberMe) {
        String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(" "));

        var now = Instant.now();
        Instant validity;
        if (rememberMe) {
            validity = now.plus(this.tokenValidityInSecondsForRememberMe, ChronoUnit.SECONDS);
        } else {
            validity = now.plus(this.tokenValidityInSeconds, ChronoUnit.SECONDS);
        }

        // @formatter:off
        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(validity)
            .subject(authentication.getName())
            .claim(AUTHORITIES_CLAIM, authorities);
        if (authentication.getPrincipal() instanceof UserWithId user) {
            builder.claim(USER_ID_CLAIM, user.getId());
        }

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, builder.build())).getTokenValue();
    }

    /**
     * Object to return as body in JWT Authentication.
     */
    static class JWTToken {

        private String idToken;

        private boolean mustChangePassword;

        JWTToken(String idToken, boolean mustChangePassword) {
            this.idToken = idToken;
            this.mustChangePassword = mustChangePassword;
        }

        @JsonProperty("id_token")
        String getIdToken() {
            return idToken;
        }

        void setIdToken(String idToken) {
            this.idToken = idToken;
        }

        @JsonProperty("mustChangePassword")
        boolean isMustChangePassword() {
            return mustChangePassword;
        }

        void setMustChangePassword(boolean mustChangePassword) {
            this.mustChangePassword = mustChangePassword;
        }
    }
}
