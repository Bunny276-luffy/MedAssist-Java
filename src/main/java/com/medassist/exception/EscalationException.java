package com.medassist.exception;

/**
 * Checked exception thrown when an escalation alert cannot be delivered to the caregiver.
 *
 * <p>This exception is raised by {@code EscalationService} when the JavaMail API fails
 * to send an email (e.g., authentication error, network failure, invalid address).</p>
 *
 * @author MedAssist Team
 * @version 1.0
 * @see com.medassist.service.EscalationService
 */
public class EscalationException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code EscalationException} with the specified detail message.
     *
     * @param message human-readable description of the escalation failure
     */
    public EscalationException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code EscalationException} with a message and root cause.
     *
     * @param message human-readable description
     * @param cause   the underlying cause (e.g., {@code MessagingException})
     */
    public EscalationException(String message, Throwable cause) {
        super(message, cause);
    }
}
