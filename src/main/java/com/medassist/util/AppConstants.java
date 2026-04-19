package com.medassist.util;

/**
 * Central repository of application-wide string constants for the MedAssist system.
 *
 * <p>All magic strings, file paths, and configurable time constants used across
 * services and UI panels are declared here. Using constants prevents typos and
 * makes global changes easy to manage from a single file.</p>
 *
 * @author MedAssist Team
 * @version 1.0
 */
public final class AppConstants {

    // -------------------------------------------------------------------------
    // Application Metadata
    // -------------------------------------------------------------------------

    /** Human-readable application name displayed in the title bar. */
    public static final String APP_NAME = "MedAssist — AI Medication Adherence System";

    /** Application version string. */
    public static final String APP_VERSION = "1.0.0";

    // -------------------------------------------------------------------------
    // File Paths
    // -------------------------------------------------------------------------

    /** Directory under the user's home folder where all MedAssist data files are stored. */
    public static final String DATA_DIR = System.getProperty("user.home") + "/MedAssist/data/";

    /** Full path to the serialized patient record file. */
    public static final String PATIENT_FILE_PATH = DATA_DIR + "patient.ser";

    /** Full path to the dose log text file. */
    public static final String DOSE_LOG_FILE_PATH = DATA_DIR + "dose_log.txt";

    /** Full path to the escalation log text file. */
    public static final String ESCALATION_LOG_FILE_PATH = DATA_DIR + "escalation_log.txt";

    // -------------------------------------------------------------------------
    // Timing Constants
    // -------------------------------------------------------------------------

    /**
     * Grace period in minutes before a missed dose is escalated.
     * The reminder may be re-fired after this interval if the user is inactive.
     */
    public static final int GRACE_PERIOD_MINUTES = 15;

    /**
     * Interval in minutes at which the EscalationService polls for CRITICAL_MISSED doses.
     */
    public static final int ESCALATION_INTERVAL_MINUTES = 5;

    /**
     * Delay in minutes to re-fire a reminder when the user is detected as inactive.
     */
    public static final int INACTIVE_DELAY_MINUTES = 2;

    // -------------------------------------------------------------------------
    // Email Configuration Placeholders
    // -------------------------------------------------------------------------

    /** SMTP host for outgoing escalation emails. */
    public static final String SMTP_HOST = "smtp.gmail.com";

    /** SMTP port for TLS-based mail submission. */
    public static final int SMTP_PORT = 587;

    /** Sender email address (must be configured before production use). */
    public static final String SENDER_EMAIL = "medassist.alerts@gmail.com";

    /** Sender email password / app-specific password. */
    public static final String SENDER_PASSWORD = "your_app_password_here";

    // -------------------------------------------------------------------------
    // UI Constants
    // -------------------------------------------------------------------------

    /** Default width of the main application window in pixels. */
    public static final int WINDOW_WIDTH = 1000;

    /** Default height of the main application window in pixels. */
    public static final int WINDOW_HEIGHT = 680;

    /** Minimum confidence threshold below which OCR results are flagged for review. */
    public static final double OCR_CONFIDENCE_THRESHOLD = 0.75;

    // -------------------------------------------------------------------------
    // Command Keywords
    // -------------------------------------------------------------------------

    /** Command type returned when 'taken' is detected in voice input. */
    public static final String CMD_TAKEN = "CMD_TAKEN";

    /** Command type returned when 'missed' is detected in voice input. */
    public static final String CMD_MISSED = "CMD_MISSED";

    /** Command type returned when 'add' is detected in voice input. */
    public static final String CMD_ADD = "CMD_ADD";

    /** Command type returned when 'schedule' is detected in voice input. */
    public static final String CMD_SCHEDULE = "CMD_SCHEDULE";

    /** Command type returned when 'scan' is detected in voice input. */
    public static final String CMD_SCAN = "CMD_SCAN";

    /** Command type returned when no keyword is matched. */
    public static final String CMD_UNKNOWN = "CMD_UNKNOWN";

    /** Private constructor prevents instantiation of this utility class. */
    private AppConstants() {
        throw new UnsupportedOperationException("AppConstants is a utility class.");
    }
}
