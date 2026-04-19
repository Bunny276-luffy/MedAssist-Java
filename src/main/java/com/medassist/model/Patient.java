package com.medassist.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents a patient registered in the MedAssist system.
 *
 * <p>A {@code Patient} holds personal identification details, preferred language,
 * an ordered list of {@link Medication} objects, and an associated {@link Caregiver}.
 * Patient objects are serialized to disk so that the system persists state across
 * application restarts.</p>
 *
 * @author MedAssist Team
 * @version 1.0
 * @see Medication
 * @see Caregiver
 * @see Language
 */
public class Patient implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unique identifier for the patient (e.g., UUID or hospital ID). */
    private String patientId;

    /** Full name of the patient. */
    private String name;

    /** Preferred interaction language. */
    private Language language;

    /** Ordered list of medications prescribed to this patient. */
    private ArrayList<Medication> medications;

    /** The caregiver responsible for receiving escalation alerts. */
    private Caregiver caregiver;

    /**
     * Constructs a fully-initialised {@code Patient} with an empty medication list.
     *
     * @param patientId unique patient identifier
     * @param name      full name of the patient
     * @param language  preferred language for voice interactions
     * @param caregiver associated caregiver
     */
    public Patient(String patientId, String name, Language language, Caregiver caregiver) {
        this.patientId = patientId;
        this.name = name;
        this.language = language;
        this.caregiver = caregiver;
        this.medications = new ArrayList<>();
    }

    // -------------------------------------------------------------------------
    // Business Methods
    // -------------------------------------------------------------------------

    /**
     * Adds a medication to this patient's medication list.
     *
     * @param medication the {@link Medication} to add; must not be {@code null}
     */
    public void addMedication(Medication medication) {
        if (medication != null) {
            medications.add(medication);
        }
    }

    /**
     * Removes a medication from this patient's list by drug name.
     *
     * @param drugName the drug name to search for and remove
     * @return {@code true} if a medication was removed
     */
    public boolean removeMedication(String drugName) {
        return medications.removeIf(m -> m.getDrugName().equalsIgnoreCase(drugName));
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * Returns the patient's unique identifier.
     *
     * @return patient ID string
     */
    public String getPatientId() {
        return patientId;
    }

    /**
     * Returns the patient's full name.
     *
     * @return name string
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the patient's preferred language.
     *
     * @return {@link Language} enum value
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * Returns the list of medications for this patient.
     *
     * @return {@link ArrayList} of {@link Medication} objects
     */
    public ArrayList<Medication> getMedications() {
        return medications;
    }

    /**
     * Returns the caregiver associated with this patient.
     *
     * @return {@link Caregiver} object
     */
    public Caregiver getCaregiver() {
        return caregiver;
    }

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    /**
     * Sets the patient's unique identifier.
     *
     * @param patientId new patient ID
     */
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    /**
     * Sets the patient's full name.
     *
     * @param name new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the patient's preferred language.
     *
     * @param language new language preference
     */
    public void setLanguage(Language language) {
        this.language = language;
    }

    /**
     * Replaces the entire medication list for this patient.
     *
     * @param medications new list of medications
     */
    public void setMedications(ArrayList<Medication> medications) {
        this.medications = medications;
    }

    /**
     * Sets the caregiver for this patient.
     *
     * @param caregiver new caregiver
     */
    public void setCaregiver(Caregiver caregiver) {
        this.caregiver = caregiver;
    }

    /**
     * Returns a human-readable summary of this patient.
     *
     * @return formatted string representation
     */
    @Override
    public String toString() {
        return "Patient{" +
                "patientId='" + patientId + '\'' +
                ", name='" + name + '\'' +
                ", language=" + language +
                ", medications=" + medications +
                ", caregiver=" + caregiver +
                '}';
    }
}
