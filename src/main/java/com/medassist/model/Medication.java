package com.medassist.model;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * Represents a single medication prescribed to a patient in the MedAssist system.
 *
 * <p>Each {@code Medication} instance encapsulates the drug name, dosage instructions,
 * frequency of administration, the scheduled time for intake, a criticality flag that
 * triggers caregiver escalation on missed doses, and the current adherence status.</p>
 *
 * <p>This class implements {@link Serializable} so that patient records, which contain
 * medication lists, can be persisted to disk via {@code FileStorageUtil}.</p>
 *
 * @author MedAssist Team
 * @version 1.0
 * @see MedicationStatus
 */
public class Medication implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Name of the drug (e.g., "Metformin 500mg"). */
    private String drugName;

    /** Dosage instruction (e.g., "1 tablet"). */
    private String dosage;

    /** How often the medication should be taken (e.g., "twice daily"). */
    private String frequency;

    /** The local time at which the patient should take this medication. */
    private LocalTime scheduledTime;

    /** If {@code true}, a missed dose triggers immediate caregiver escalation. */
    private boolean isCritical;

    /** The current adherence status of this medication dose. */
    private MedicationStatus status;

    /**
     * Constructs a fully-initialised {@code Medication} object.
     *
     * @param drugName      name of the drug
     * @param dosage        dosage instruction
     * @param frequency     how often the drug should be taken
     * @param scheduledTime time at which the dose should be taken
     * @param isCritical    {@code true} if missed dose triggers escalation
     */
    public Medication(String drugName, String dosage, String frequency,
                      LocalTime scheduledTime, boolean isCritical) {
        this.drugName = drugName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.scheduledTime = scheduledTime;
        this.isCritical = isCritical;
        this.status = MedicationStatus.PENDING;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * Returns the drug name.
     *
     * @return drug name string
     */
    public String getDrugName() {
        return drugName;
    }

    /**
     * Returns the dosage instruction.
     *
     * @return dosage string
     */
    public String getDosage() {
        return dosage;
    }

    /**
     * Returns the frequency of administration.
     *
     * @return frequency string
     */
    public String getFrequency() {
        return frequency;
    }

    /**
     * Returns the scheduled time for this medication dose.
     *
     * @return scheduled {@link LocalTime}
     */
    public LocalTime getScheduledTime() {
        return scheduledTime;
    }

    /**
     * Returns whether this medication is marked as critical.
     *
     * @return {@code true} if critical
     */
    public boolean isCritical() {
        return isCritical;
    }

    /**
     * Returns the current adherence status.
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
     * Sets the drug name.
     *
     * @param drugName new drug name
     */
    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    /**
     * Sets the dosage instruction.
     *
     * @param dosage new dosage string
     */
    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    /**
     * Sets the frequency of administration.
     *
     * @param frequency new frequency string
     */
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    /**
     * Sets the scheduled time for this dose.
     *
     * @param scheduledTime new scheduled time
     */
    public void setScheduledTime(LocalTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    /**
     * Sets the criticality flag.
     *
     * @param critical {@code true} to mark as critical
     */
    public void setCritical(boolean critical) {
        isCritical = critical;
    }

    /**
     * Sets the adherence status of this medication.
     *
     * @param status new {@link MedicationStatus}
     */
    public void setStatus(MedicationStatus status) {
        this.status = status;
    }

    /**
     * Returns a human-readable summary of this medication.
     *
     * @return formatted string representation
     */
    @Override
    public String toString() {
        return "Medication{" +
                "drugName='" + drugName + '\'' +
                ", dosage='" + dosage + '\'' +
                ", frequency='" + frequency + '\'' +
                ", scheduledTime=" + scheduledTime +
                ", isCritical=" + isCritical +
                ", status=" + status +
                '}';
    }
}
