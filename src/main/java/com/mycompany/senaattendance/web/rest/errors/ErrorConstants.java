package com.mycompany.senaattendance.web.rest.errors;

import java.net.URI;

public final class ErrorConstants {

    public static final String ERR_CONCURRENCY_FAILURE = "error.concurrencyFailure";
    public static final String ERR_VALIDATION = "error.validation";
    public static final String PROBLEM_BASE_URL = "https://www.jhipster.tech/problem";
    public static final URI DEFAULT_TYPE = URI.create(PROBLEM_BASE_URL + "/problem-with-message");
    public static final URI CONSTRAINT_VIOLATION_TYPE = URI.create(PROBLEM_BASE_URL + "/constraint-violation");
    public static final URI INVALID_PASSWORD_TYPE = URI.create(PROBLEM_BASE_URL + "/invalid-password");
    public static final URI EMAIL_ALREADY_USED_TYPE = URI.create(PROBLEM_BASE_URL + "/email-already-used");
    public static final URI LOGIN_ALREADY_USED_TYPE = URI.create(PROBLEM_BASE_URL + "/login-already-used");
    public static final URI DOCUMENT_NUMBER_ALREADY_USED = URI.create(PROBLEM_BASE_URL + "/document-number-already-used");
    public static final URI PROGRAM_CODE_ALREADY_USED_TYPE = URI.create(PROBLEM_BASE_URL + "/program-code-already-used");
    public static final URI PROGRAM_INITIALS_ALREADY_USED_TYPE = URI.create(PROBLEM_BASE_URL + "/program-initials-already-used");
    public static final URI PROGRAM_NAME_ALREADY_USED_TYPE = URI.create(PROBLEM_BASE_URL + "/program-name-already-used");
    public static final URI TRIMESTER_DATES_ORDER_TYPE = URI.create(PROBLEM_BASE_URL + "/trimester-dates-order");
    public static final URI TRIMESTER_DATES_OVERLAP_TYPE = URI.create(PROBLEM_BASE_URL + "/trimester-dates-overlap");
    public static final URI TRIMESTER_NOT_EDITABLE_TYPE = URI.create(PROBLEM_BASE_URL + "/trimester-not-editable");
    public static final URI TRIMESTER_START_DATE_LOCKED_TYPE = URI.create(PROBLEM_BASE_URL + "/trimester-start-date-locked");
    public static final URI TRIMESTER_END_DATE_IN_PAST_TYPE = URI.create(PROBLEM_BASE_URL + "/trimester-end-date-in-past");
    public static final URI TRIMESTER_START_DATE_MUST_BE_FUTURE_TYPE = URI.create(
        PROBLEM_BASE_URL + "/trimester-start-date-must-be-future"
    );
    public static final URI TRIMESTER_ATTENDANCE_START_DATE_TYPE = URI.create(PROBLEM_BASE_URL + "/trimester-attendance-start-date");

    private ErrorConstants() {}
}
