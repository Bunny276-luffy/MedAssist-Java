package com.medassist.model;

import java.io.Serializable;

/**
 * Represents a caregiver associated with a {@link Patient} in the MedAssist system.
 *
 * <p>A caregiver receives escalation notifications (via email) whenever a critical
 * medication dose is missed by the patient. The {@code EscalationService} reads the
 * caregiver's contact information directly from this object.</p>
 *
 * <p>Implements {@link Serializable} so that it is persisted as part of the patient record.</p>
 *
 * @author MedAssist Team
 * @version 1.0
 */
public class Caregiver implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Full name of the caregiver. */
    private String name;

    /** Email address used for escalation notifications. */
    private String email;

    /** Phone number of the caregiver (for future SMS support). */
    private String phone;

    /** Relationship to the patient (e.g., "Son", "Daughter", "Nurse"). */
    private String relationship;

    /**
     * Constructs a fully-initialised {@code Caregiver} object.
     *
     * @param name         caregiver's full name
     * @param email        caregiver's email address
     * @param phone        caregiver's phone number
     * @param relationship relationship to the patient
     */
    public Caregiver(String name, String email, String phone, String relationship) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.relationship = relationship;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * Returns the caregiver's full name.
     *
     * @return name string
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the caregiver's email address.
     *
     * @return email string
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the caregiver's phone number.
     *
     * @return phone string
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Returns the relationship of the caregiver to the patient.
     *
     * @return relationship string
     */
    public String getRelationship() {
        return relationship;
    }

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    /**
     * Sets the caregiver's full name.
     *
     * @param name new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the caregiver's email address.
     *
     * @param email new email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets the caregiver's phone number.
     *
     * @param phone new phone number
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Sets the relationship of the caregiver to the patient.
     *
     * @param relationship new relationship string
     */
    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    /**
     * Returns a human-readable summary of this caregiver.
     *
     * @return formatted string representation
     */
    @Override
    public String toString() {
        return "Caregiver{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", relationship='" + relationship + '\'' +
                '}';
    }
}
