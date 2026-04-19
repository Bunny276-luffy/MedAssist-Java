package com.medassist.service;

import com.medassist.exception.EscalationException;
import com.medassist.model.DoseLog;
import com.medassist.model.Medication;
import com.medassist.model.MedicationStatus;
import com.medassist.model.Patient;
import com.medassist.util.AppConstants;
import com.medassist.util.FileStorageUtil;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service that monitors the patient's dose history and escalates critical missed doses
 * to the associated caregiver via email.
 *
 * <p>{@code EscalationService} implements {@link Notifiable} and runs a background
 * {@link ScheduledExecutorService} that polls for {@code CRITICAL_MISSED} status entries
 * every {@link AppConstants#ESCALATION_INTERVAL_MINUTES} minutes. When found, it sends
 * an HTML email to the caregiver using the JavaMail API. After sending, the dose log
 * entry's status is internally updated to prevent duplicate alerts.</p>
 *
 * <p>Email credentials are read from {@link AppConstants}. Before production use,
 * replace the placeholder credentials with real SMTP settings.</p>
 *
 * @author MedAssist Team
 * @version 1.0
 * @see Notifiable
 * @see AppConstants
 */
public class EscalationService implements Notifiable {

    /** The patient whose dose log is monitored for escalation triggers. */
    private final Patient patient;

    /** Background scheduler that polls for CRITICAL_MISSED events. */
    private final ScheduledExecutorService scheduler;

    /**
     * In-memory dose log keyed by medication name, used to track escalation state.
     * In a full implementation this would be backed by the persisted dose log file.
     */
    private final HashMap<String, DoseLog> doseLogs;

    /** List of escalation alert messages displayed in the CaregiverPanel. */
    private final List<String> escalationHistory;

    /**
     * Constructs a new {@code EscalationService} for the given patient.
     *
     * @param patient the patient to monitor; must not be {@code null}
     */
    public EscalationService(Patient patient) {
        this.patient = patient;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.doseLogs = new HashMap<>();
        this.escalationHistory = new ArrayList<>();
    }

    /**
     * Starts the background escalation monitoring loop.
     *
     * <p>The loop fires at a fixed rate defined by {@link AppConstants#ESCALATION_INTERVAL_MINUTES}.
     * Each iteration scans the patient's medication list for any dose with
     * {@code CRITICAL_MISSED} status and triggers an escalation email to the caregiver.</p>
     */
    public void startMonitoring() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkAndEscalate();
            } catch (EscalationException e) {
                System.err.println("[EscalationService] Escalation failed: " + e.getMessage());
            }
        }, AppConstants.ESCALATION_INTERVAL_MINUTES,
                AppConstants.ESCALATION_INTERVAL_MINUTES,
                TimeUnit.MINUTES);

        System.out.println("[EscalationService] Monitoring started — polling every "
                + AppConstants.ESCALATION_INTERVAL_MINUTES + " minutes.");
    }

    /**
     * Stops the background monitoring scheduler gracefully.
     */
    public void stopMonitoring() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Checks the patient's medication list for any CRITICAL_MISSED entries and
     * sends an escalation email to the caregiver for each one found.
     *
     * @throws EscalationException if the email cannot be delivered
     */
    private void checkAndEscalate() throws EscalationException {
        for (Medication med : patient.getMedications()) {
            if (med.getStatus() == MedicationStatus.CRITICAL_MISSED) {
                escalate(patient, med);
            }
        }
    }

    /**
     * Triggers an immediate caregiver alert for a specific critical medication.
     *
     * <p>Sends an HTML email to the caregiver's registered email address, then
     * logs the alert to the escalation log file. After sending, the medication
     * status remains {@code CRITICAL_MISSED} to ensure auditability — re-alerting
     * is prevented by tracking sent alerts in the internal dose log map.</p>
     *
     * @param patient    the patient who missed the critical dose
     * @param medication the medication that was missed
     */
    @Override
    public void escalate(Patient patient, Medication medication) {
        String key = medication.getDrugName();

        // Prevent duplicate alert for the same medication within this session
        if (doseLogs.containsKey(key) &&
                doseLogs.get(key).getStatus() == MedicationStatus.CRITICAL_MISSED) {
            System.out.println("[EscalationService] Alert already sent for: " + key);
            return;
        }

        try {
            sendEscalationEmail(patient, medication);
            String logMsg = "ESCALATION sent to " + patient.getCaregiver().getEmail()
                    + " for " + medication.getDrugName()
                    + " at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            escalationHistory.add(logMsg);
            sendNotification(logMsg);
            FileStorageUtil.saveEscalationLog(logMsg);

            // Mark in internal log to prevent duplicate sends
            DoseLog log = new DoseLog(medication.getDrugName(),
                    medication.getScheduledTime(), null, MedicationStatus.CRITICAL_MISSED);
            doseLogs.put(key, log);

        } catch (EscalationException | IOException e) {
            System.err.println("[EscalationService] Failed to escalate: " + e.getMessage());
        }
    }

    /**
     * Sends a plain-text notification about the given message.
     *
     * <p>Currently prints to standard output and to the escalation history list.
     * In a production system, this would push a pop-up or system notification.</p>
     *
     * @param message the notification content
     */
    @Override
    public void sendNotification(String message) {
        System.out.println("[EscalationService] NOTIFICATION: " + message);
    }

    /**
     * Constructs and sends an HTML email to the caregiver using JavaMail API.
     *
     * @param patient    the patient who missed the dose
     * @param medication the missed critical medication
     * @throws EscalationException if the JavaMail session fails to send the message
     */
    private void sendEscalationEmail(Patient patient, Medication medication)
            throws EscalationException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", AppConstants.SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(AppConstants.SMTP_PORT));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        AppConstants.SENDER_EMAIL, AppConstants.SENDER_PASSWORD);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(AppConstants.SENDER_EMAIL));
            msg.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(patient.getCaregiver().getEmail()));
            msg.setSubject("⚠️ MedAssist ALERT: Critical Missed Dose — " + patient.getName());
            msg.setContent(buildEmailBody(patient, medication), "text/html; charset=utf-8");

            Transport.send(msg);
            System.out.println("[EscalationService] Email sent to: "
                    + patient.getCaregiver().getEmail());
        } catch (MessagingException e) {
            throw new EscalationException(
                    "Failed to send escalation email: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the HTML body for the escalation email.
     *
     * @param patient    patient information
     * @param medication missed medication information
     * @return HTML string for the email body
     */
    private String buildEmailBody(Patient patient, Medication medication) {
        return "<html><body style='font-family:Arial,sans-serif;'>"
                + "<h2 style='color:#c0392b;'>⚠️ Critical Medication Alert</h2>"
                + "<p>Dear " + patient.getCaregiver().getName() + ",</p>"
                + "<p>Your patient <strong>" + patient.getName()
                + "</strong> has <strong>missed a critical medication dose</strong>.</p>"
                + "<table border='1' cellpadding='8' style='border-collapse:collapse;'>"
                + "<tr><td><b>Medication</b></td><td>" + medication.getDrugName() + "</td></tr>"
                + "<tr><td><b>Dosage</b></td><td>" + medication.getDosage() + "</td></tr>"
                + "<tr><td><b>Scheduled Time</b></td><td>" + medication.getScheduledTime() + "</td></tr>"
                + "<tr><td><b>Status</b></td><td style='color:red;'>" + medication.getStatus() + "</td></tr>"
                + "</table>"
                + "<p>Please check on " + patient.getName() + " immediately.</p>"
                + "<p style='color:#7f8c8d;font-size:12px;'>This is an automated alert from MedAssist.</p>"
                + "</body></html>";
    }

    /**
     * Returns a defensive copy of the escalation history list.
     *
     * @return list of escalation alert messages sent during this session
     */
    public List<String> getEscalationHistory() {
        return new ArrayList<>(escalationHistory);
    }

    /**
     * Immediately triggers a test escalation alert for the first CRITICAL_MISSED medication,
     * or simulates one if none exists yet. Used from the CaregiverPanel's 'Test Alert' button.
     */
    public void triggerTestAlert() {
        Medication testMed = patient.getMedications().stream()
                .filter(m -> m.isCritical())
                .findFirst()
                .orElseGet(() -> {
                    Medication m = new Medication("Aspirin (TEST)", "81mg", "once daily",
                            java.time.LocalTime.now(), true);
                    m.setStatus(MedicationStatus.CRITICAL_MISSED);
                    return m;
                });
        testMed.setStatus(MedicationStatus.CRITICAL_MISSED);

        // Remove from prevention cache to allow test re-send
        doseLogs.remove(testMed.getDrugName());

        escalate(patient, testMed);
    }
}
