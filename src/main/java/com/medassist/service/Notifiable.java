package com.medassist.service;

import com.medassist.model.Medication;
import com.medassist.model.Patient;

/**
 * Contract for notification-capable components within the MedAssist system.
 *
 * <p>Any service that wishes to send patient notifications or escalate alerts to a
 * caregiver must implement this interface. The polymorphic design allows the
 * {@code ReminderService} to maintain a {@code List<Notifiable>} and invoke both
 * simple notifications and full escalations through a uniform API.</p>
 *
 * <p>Implementations must handle all checked exceptions internally or wrap them in
 * appropriate runtime exceptions unless the spec requires propagation.</p>
 *
 * @author MedAssist Team
 * @version 1.0
 * @see com.medassist.service.ReminderService
 * @see com.medassist.service.EscalationService
 */
public interface Notifiable {

    /**
     * Sends a notification message to the patient or caregiver.
     *
     * <p>The delivery channel (voice, pop-up, email) depends on the implementing class.</p>
     *
     * @param message the human-readable notification content; must not be {@code null}
     */
    void sendNotification(String message);

    /**
     * Escalates a missed critical medication event to the patient's caregiver.
     *
     * <p>Implementations should record the escalation timestamp, send an email or SMS
     * to the caregiver, and update the medication's status to {@code CRITICAL_MISSED}.</p>
     *
     * @param patient    the patient who missed the dose; must not be {@code null}
     * @param medication the critical medication that was missed; must not be {@code null}
     */
    void escalate(Patient patient, Medication medication);
}
