package com.medassist.model;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * Records a single dose-taking event for a patient's medication.
 *
 * <p>A {@code DoseLog} entry is created each time a reminder fires. It captures
 * the scheduled time, the actual time the patient confirmed taking the dose (if any),
 * and the final {@link MedicationStatus}. These logs are persisted to a flat text
 * file by {@code FileStorageUtil.saveDoseLog()} for audit and reporting purposes.</p>
 *
 * @author MedAssist Team
 * @version 1.0
 * @see MedicationStatus
 */
public class DoseLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Name of the medication for which this log entry was created. */
    private String medicationName;

    /** The time this dose was originally scheduled. */
    private LocalTime scheduledTime;

    /** The time the patient actually took the dose ({@code null} if not taken). */
    private LocalTime actualTakenTime;

    /** The outcome status for this dose event. */
    private MedicationStatus status;

    /**
     * Constructs a new {@code DoseLog} entry.
     *
     * @param medicationName the drug name
     * @param scheduledTime  when the dose was scheduled
     * @param actualTakenTime when the dose was actually taken (may be {@code null})
     * @param status         the outcome status of this dose event
     */
    public DoseLog(String medicationName, LocalTime scheduledTime,
                   LocalTime actualTakenTime, MedicationStatus status) {
        this.medicationName = medicationName;
        this.scheduledTime = scheduledTime;
        this.actualTakenTime = actualTakenTime;
        this.status = status;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * Returns the medication name for this log entry.
     *
     * @return medication name string
     */
    public String getMedicationName() {
        return medicationName;
    }

    /**
     * Returns the scheduled time for this dose.
     *
     * @return scheduled {@link LocalTime}
     */
    public LocalTime getScheduledTime() {
        return scheduledTime;
    }

    /**
     * Returns the actual time the dose was taken, or {@code null} if not taken.
     *
     * @return actual taken {@link LocalTime}, or {@code null}
     */
    public LocalTime getActualTakenTime() {
        return actualTakenTime;
    }

    /**
     * Returns the adherence status for this dose event.
     *
     * @return {@link MedicationStatus} value
     */
    public MedicationStatus getStatus() {
        return status;
    }

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    /**
     * Sets the medication name.
     *
     * @param medicationName new medication name
     */
    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    /**
     * Sets the scheduled time.
     *
     * @param scheduledTime new scheduled time
     */
    public void setScheduledTime(LocalTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    /**
     * Sets the actual time the dose was taken.
     *
     * @param actualTakenTime time of dose intake, or {@code null}
     */
    public void setActualTakenTime(LocalTime actualTakenTime) {
        this.actualTakenTime = actualTakenTime;
    }

    /**
     * Sets the adherence status for this log entry.
     *
     * @param status new status
     */
    public void setStatus(MedicationStatus status) {
        this.status = status;
    }

    /**
     * Returns a formatted string representation of this dose log entry.
     *
     * @return formatted string
     */
    @Override
    public String toString() {
        return "DoseLog{" +
                "medicationName='" + medicationName + '\'' +
                ", scheduledTime=" + scheduledTime +
                ", actualTakenTime=" + actualTakenTime +
                ", status=" + status +
                '}';
    }
}
