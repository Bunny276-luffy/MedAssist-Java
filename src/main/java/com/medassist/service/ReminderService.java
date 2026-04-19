package com.medassist.service;

import com.medassist.model.DoseLog;
import com.medassist.model.Medication;
import com.medassist.model.MedicationStatus;
import com.medassist.model.Patient;
import com.medassist.util.AppConstants;
import com.medassist.util.DateTimeUtil;
import com.medassist.util.FileStorageUtil;
import com.medassist.voice.VoiceEngine;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Core reminder scheduling service for the MedAssist system.
 *
 * <p>{@code ReminderService} extends {@link BaseReminder} (gaining access to a shared
 * {@link java.util.concurrent.ScheduledExecutorService}) and implements {@link Notifiable}
 * to participate in the polymorphic notification pipeline. It schedules one reminder task
 * per medication in the patient's list, calculates the delay to each {@code scheduledTime},
 * and fires an alert at the correct moment using {@link VoiceEngine#speak}.</p>
 *
 * <p>Before firing, each task checks {@link ActivityMonitor#isUserActive()}. If the patient
 * is inactive, the reminder is re-scheduled after a {@link AppConstants#INACTIVE_DELAY_MINUTES}
 * minute delay.</p>
 *
 * <p>All dose events are persisted via {@link FileStorageUtil#saveDoseLog}.</p>
 *
 * @author MedAssist Team
 * @version 1.0
 * @see BaseReminder
 * @see Notifiable
 * @see ActivityMonitor
 */
public class ReminderService extends BaseReminder implements Notifiable {

    /** The patient for whom reminders are being scheduled. */
    private final Patient patient;

    /**
     * Polymorphic list of notification handlers. Any additional {@link Notifiable}
     * implementations (e.g., SMS, push notification) can be added here.
     */
    private final List<Notifiable> notifiables;

    /** Voice engine used to speak the reminder aloud. */
    private final VoiceEngine voiceEngine;

    /**
     * Constructs a {@code ReminderService} for the given patient.
     *
     * @param patient     the patient whose medication schedule will be managed;
     *                    must not be {@code null}
     * @param voiceEngine the voice engine for speech output; must not be {@code null}
     */
    public ReminderService(Patient patient, VoiceEngine voiceEngine) {
        super();
        this.patient = patient;
        this.voiceEngine = voiceEngine;
        this.notifiables = new ArrayList<>();
        this.notifiables.add(this); // self-register as Notifiable
    }

    /**
     * Adds an additional {@link Notifiable} handler to the notification chain.
     *
     * <p>This demonstrates the polymorphism requirement: any number of notification
     * strategies can be chained at runtime.</p>
     *
     * @param notifiable the handler to add; must not be {@code null}
     */
    public void addNotifiable(Notifiable notifiable) {
        if (notifiable != null) {
            notifiables.add(notifiable);
        }
    }

    /**
     * Schedules a reminder for every medication in the patient's list.
     *
     * <p>For each medication, the delay (in seconds) from now to the
     * {@code scheduledTime} is calculated. If the time is in the past today,
     * the reminder fires after a 5-second delay to handle startup loading.</p>
     */
    public void scheduleAllMedications() {
        if (patient.getMedications().isEmpty()) {
            System.out.println("[ReminderService] No medications to schedule.");
            return;
        }

        for (Medication med : patient.getMedications()) {
            long delay = DateTimeUtil.secondsUntil(med.getScheduledTime());
            if (delay <= 0) {
                delay = 5; // fire shortly if time already passed (startup/demo)
            }

            final Medication medication = med;
            final long fireDelay = delay;

            scheduler.schedule(() -> handleReminder(patient, medication),
                    fireDelay, TimeUnit.SECONDS);

            System.out.println("[ReminderService] Scheduled '" + med.getDrugName()
                    + "' in " + fireDelay + "s (at " + med.getScheduledTime() + ").");
        }
    }

    /**
     * Handles the firing of a reminder for a specific medication.
     *
     * <p>If the patient is detected as inactive, the reminder is re-scheduled
     * after {@link AppConstants#INACTIVE_DELAY_MINUTES} minutes. Otherwise,
     * {@link #fireAlert(Patient, Medication)} is called immediately.</p>
     *
     * @param patient    the patient being reminded
     * @param medication the medication due now
     */
    private void handleReminder(Patient patient, Medication medication) {
        if (!ActivityMonitor.isUserActive()) {
            System.out.println("[ReminderService] Patient inactive — delaying '"
                    + medication.getDrugName() + "' by "
                    + AppConstants.INACTIVE_DELAY_MINUTES + " minutes.");
            scheduler.schedule(() -> fireAlert(patient, medication),
                    AppConstants.INACTIVE_DELAY_MINUTES, TimeUnit.MINUTES);
        } else {
            fireAlert(patient, medication);
        }
    }

    /**
     * Fires the medication alert by speaking the reminder and notifying all handlers.
     *
     * <p>After speaking, the medication status is set to {@code PENDING} to await
     * patient confirmation. The dose event is persisted via {@link FileStorageUtil}.</p>
     *
     * @param patient    the patient for whom the alert is firing
     * @param medication the medication that triggered the alert
     */
    @Override
    public void fireAlert(Patient patient, Medication medication) {
        String message = "Time to take your medication: " + medication.getDrugName()
                + ", " + medication.getDosage()
                + ". Frequency: " + medication.getFrequency() + ".";

        // Notify all registered handlers
        for (Notifiable n : notifiables) {
            n.sendNotification(message);
        }

        // Speak via voice engine
        voiceEngine.speak(message, patient.getLanguage());

        // Persist a PENDING dose log entry
        DoseLog log = new DoseLog(medication.getDrugName(),
                medication.getScheduledTime(), null, MedicationStatus.PENDING);
        try {
            FileStorageUtil.saveDoseLog(log);
        } catch (IOException e) {
            System.err.println("[ReminderService] Failed to save dose log: " + e.getMessage());
        }

        // If critical and no confirmation after grace period, mark CRITICAL_MISSED
        if (medication.isCritical()) {
            scheduler.schedule(() -> {
                if (medication.getStatus() == MedicationStatus.PENDING) {
                    medication.setStatus(MedicationStatus.CRITICAL_MISSED);
                    String alertMsg = "CRITICAL MISSED: " + medication.getDrugName()
                            + " for patient " + patient.getName();
                    sendNotification(alertMsg);
                    System.err.println("[ReminderService] " + alertMsg);
                }
            }, AppConstants.GRACE_PERIOD_MINUTES, TimeUnit.MINUTES);
        }
    }

    /**
     * Sends a notification to standard output (and to the Swing UI via event bus,
     * simulated here with a console print).
     *
     * @param message the notification message
     */
    @Override
    public void sendNotification(String message) {
        System.out.println("[ReminderService] NOTIFICATION: " + message);
    }

    /**
     * Escalates a critical missed medication to the caregiver (delegates to EscalationService
     * in the full composition; here it logs and marks status).
     *
     * @param patient    the affected patient
     * @param medication the missed critical medication
     */
    @Override
    public void escalate(Patient patient, Medication medication) {
        medication.setStatus(MedicationStatus.CRITICAL_MISSED);
        sendNotification("Escalating critical missed dose: " + medication.getDrugName()
                + " for " + patient.getName());
    }

    /**
     * Returns the patient associated with this reminder service.
     *
     * @return the {@link Patient} object
     */
    public Patient getPatient() {
        return patient;
    }
}
