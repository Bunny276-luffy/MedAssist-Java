package com.medassist.model;

import java.io.Serializable;

/**
 * Represents a prescription extracted from a scanned image via the OCR pipeline.
 *
 * <p>A {@code Prescription} holds the raw data parsed from a doctor's handwritten
 * or printed prescription. The {@code confidence} score (0.0–1.0) indicates how
 * reliably the OCR engine extracted each field — values below 0.75 are flagged for
 * manual review in the UI.</p>
 *
 * @author MedAssist Team
 * @version 1.0
 * @see com.medassist.service.OCRService
 */
public class Prescription implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Drug name extracted from the prescription text. */
    private String drugName;

    /** Dosage instruction extracted from the prescription text. */
    private String dosage;

    /** Frequency instruction extracted from the prescription text. */
    private String frequency;

    /** Duration of the prescription (e.g., "30 days"). */
    private String duration;

    /** OCR confidence score from 0.0 (low) to 1.0 (high). */
    private double confidence;

    /**
     * Constructs a fully-initialised {@code Prescription} object.
     *
     * @param drugName   extracted drug name
     * @param dosage     extracted dosage
     * @param frequency  extracted frequency
     * @param duration   extracted duration
     * @param confidence OCR confidence score (0.0–1.0)
     */
    public Prescription(String drugName, String dosage, String frequency,
                        String duration, double confidence) {
        this.drugName = drugName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
        this.confidence = confidence;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * Returns the extracted drug name.
     *
     * @return drug name string
     */
    public String getDrugName() {
        return drugName;
    }

    /**
     * Returns the extracted dosage instruction.
     *
     * @return dosage string
     */
    public String getDosage() {
        return dosage;
    }

    /**
     * Returns the extracted frequency instruction.
     *
     * @return frequency string
     */
    public String getFrequency() {
        return frequency;
    }

    /**
     * Returns the duration of the prescription.
     *
     * @return duration string
     */
    public String getDuration() {
        return duration;
    }

    /**
     * Returns the OCR confidence score.
     *
     * @return confidence value between 0.0 and 1.0
     */
    public double getConfidence() {
        return confidence;
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
     * @param dosage new dosage
     */
    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    /**
     * Sets the frequency instruction.
     *
     * @param frequency new frequency
     */
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    /**
     * Sets the prescription duration.
     *
     * @param duration new duration
     */
    public void setDuration(String duration) {
        this.duration = duration;
    }

    /**
     * Sets the OCR confidence score.
     *
     * @param confidence new confidence (0.0–1.0)
     */
    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    /**
     * Returns a human-readable summary of this prescription.
     *
     * @return formatted string representation
     */
    @Override
    public String toString() {
        return "Prescription{" +
                "drugName='" + drugName + '\'' +
                ", dosage='" + dosage + '\'' +
                ", frequency='" + frequency + '\'' +
                ", duration='" + duration + '\'' +
                ", confidence=" + String.format("%.2f", confidence) +
                '}';
    }
}
