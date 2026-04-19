package com.medassist.exception;

/**
 * Checked exception thrown when the OCR pipeline fails to extract prescription data.
 *
 * <p>This exception is raised by {@code OCRService.extractPrescription()} when
 * Tess4J cannot read the image, or when the regex parsers fail to identify any
 * recognisable drug name, dosage, or frequency in the OCR output.</p>
 *
 * @author MedAssist Team
 * @version 1.0
 * @see com.medassist.service.OCRService
 */
public class OCRFailureException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code OCRFailureException} with the specified detail message.
     *
     * @param message human-readable description of the OCR failure
     */
    public OCRFailureException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code OCRFailureException} with a message and root cause.
     *
     * @param message human-readable description
     * @param cause   the underlying cause (e.g., Tess4J exception or IOException)
     */
    public OCRFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
