package com.mycompany.senaattendance.security;

/**
 * Constants for Spring Security authorities.
 */
public final class AuthoritiesConstants {

    public static final String ADMIN = "ROLE_ADMIN";

    public static final String USER = "ROLE_USER";

    public static final String ANONYMOUS = "ROLE_ANONYMOUS";

    // ------------------- New Roles -------------------
    public static final String COORDINATOR = "ROLE_COORDINATOR";

    public static final String INSTRUCTOR = "ROLE_INSTRUCTOR";

    public static final String APPRENTICE = "ROLE_APPRENTICE";

    private AuthoritiesConstants() {}
}
