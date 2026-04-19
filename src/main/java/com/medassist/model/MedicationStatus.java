package com.medassist.model;

/**
 * Enum representing the possible statuses of a medication dose.
 *
 * <p>Each status describes the current state of a scheduled medication intake,
 * helping the system track adherence and trigger appropriate escalations.</p>
 *
 * @author MedAssist Team
 * @version 1.0
 */
public enum MedicationStatus {

    /**
     * The medication is scheduled but not yet taken or acted upon.
     */
    PENDING,

    /**
     * The patient has confirmed taking the medication.
     */
    TAKEN,

    /**
     * The scheduled time has passed without any confirmation from the patient.
     */
    MISSED,

    /**
     * The patient explicitly skipped the dose (e.g., by command or button).
     */
    SKIPPED,

    /**
     * A critical medication was missed, triggering caregiver escalation.
     */
    CRITICAL_MISSED
}
