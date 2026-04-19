package com.medassist.exception;

/**
 * Checked exception thrown when a requested medication cannot be found.
 *
 * <p>This exception is raised by service methods that search the patient's
 * medication list by drug name and fail to locate a matching entry.</p>
 *
 * @author MedAssist Team
 * @version 1.0
 */
public class MedicationNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code MedicationNotFoundException} with the specified detail message.
     *
     * @param message human-readable description of which medication was not found
     */
    public MedicationNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code MedicationNotFoundException} with a message and a cause.
     *
     * @param message human-readable description
     * @param cause   the underlying cause of this exception
     */
    public MedicationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
