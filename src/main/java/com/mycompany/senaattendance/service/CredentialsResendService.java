package com.mycompany.senaattendance.service;

import com.mycompany.senaattendance.domain.Notificacion;
import com.mycompany.senaattendance.domain.User;

/**
 * Service Interface for resending the access of a user through a fresh reset link (UC006, E7).
 *
 * <p>Both the administrator resend and the retry of a failed credentials delivery share the same
 * primitive: a new reset key that forces the password change and the synchronous reset email. The
 * caller owns the notification lifecycle of its flow.
 */
public interface CredentialsResendService {
    /**
     * Resends the access of the user behind a document number: generates a fresh reset key, forces
     * the password change, sends the reset email and closes the open credentials notification of
     * the user with the delivery result (E7, UC018 E3).
     *
     * @param documentNumber the unique document number identifying the user profile.
     * @return the user with the new reset key.
     */
    User resend(String documentNumber);

    /**
     * Retries the delivery of one credentials notification: generates a fresh reset link for its
     * user and sends the reset email. An unrecognizable user or profile resolves as a failed
     * delivery, so the caller keeps the notification retryable.
     *
     * @param notification the retryable credentials notification.
     * @return whether the reset email was delivered.
     */
    boolean retry(Notificacion notification);
}
