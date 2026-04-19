package com.medassist.util;

import com.medassist.model.DoseLog;
import com.medassist.model.Patient;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

/**
 * Provides static utility methods for persisting and loading MedAssist data.
 *
 * <p>Patient records are serialized to a binary {@code .ser} file using Java's
 * built-in serialization framework. Dose log entries are appended to a human-readable
 * {@code .txt} file for audit purposes.</p>
 *
 * <p>All methods create the target directory automatically if it does not exist.
 * This class is stateless and cannot be instantiated.</p>
 *
 * @author MedAssist Team
 * @version 1.0
 * @see AppConstants#PATIENT_FILE_PATH
 * @see AppConstants#DOSE_LOG_FILE_PATH
 */
public final class FileStorageUtil {

    /** Private constructor prevents instantiation. */
    private FileStorageUtil() {
        throw new UnsupportedOperationException("FileStorageUtil is a utility class.");
    }

    /**
     * Serializes the given {@link Patient} object to the configured patient data file.
     *
     * <p>The target directory is created if it does not exist. An existing file is
     * overwritten on each call.</p>
     *
     * @param patient the patient to persist; must not be {@code null}
     * @throws IOException if the file cannot be written
     */
    public static void savePatient(Patient patient) throws IOException {
        ensureDataDirectory();
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(AppConstants.PATIENT_FILE_PATH))) {
            oos.writeObject(patient);
            System.out.println("[FileStorageUtil] Patient saved: " + patient.getName());
        }
    }

    /**
     * Deserializes and returns the {@link Patient} object from the patient data file.
     *
     * <p>Returns {@code null} if the file does not exist (first-run scenario).</p>
     *
     * @return the deserialized {@link Patient}, or {@code null} if no file is found
     * @throws IOException            if the file cannot be read
     * @throws ClassNotFoundException if the deserialized class is not on the classpath
     */
    public static Patient loadPatient() throws IOException, ClassNotFoundException {
        File file = new File(AppConstants.PATIENT_FILE_PATH);
        if (!file.exists()) {
            System.out.println("[FileStorageUtil] No patient file found — first run.");
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Patient patient = (Patient) ois.readObject();
            System.out.println("[FileStorageUtil] Patient loaded: " + patient.getName());
            return patient;
        }
    }

    /**
     * Appends a {@link DoseLog} entry to the dose log text file.
     *
     * <p>Each line in the log file contains a timestamp, medication name, scheduled time,
     * actual taken time, and status — separated by pipe ({@code |}) characters for easy
     * parsing.</p>
     *
     * @param log the dose log entry to append; must not be {@code null}
     * @throws IOException if the file cannot be written
     */
    public static void saveDoseLog(DoseLog log) throws IOException {
        ensureDataDirectory();
        String entry = LocalDateTime.now()
                + " | " + log.getMedicationName()
                + " | Scheduled: " + log.getScheduledTime()
                + " | Taken: " + (log.getActualTakenTime() != null ? log.getActualTakenTime() : "N/A")
                + " | Status: " + log.getStatus()
                + System.lineSeparator();

        Path path = Paths.get(AppConstants.DOSE_LOG_FILE_PATH);
        Files.writeString(path, entry,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
        System.out.println("[FileStorageUtil] DoseLog saved: " + log.getMedicationName());
    }

    /**
     * Appends an escalation alert message to the escalation log text file.
     *
     * @param message the escalation message to log; must not be {@code null}
     * @throws IOException if the file cannot be written
     */
    public static void saveEscalationLog(String message) throws IOException {
        ensureDataDirectory();
        String entry = LocalDateTime.now() + " | " + message + System.lineSeparator();
        Path path = Paths.get(AppConstants.ESCALATION_LOG_FILE_PATH);
        Files.writeString(path, entry,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }

    /**
     * Reads all lines from the escalation log file and returns them as a single string.
     *
     * @return the full contents of the escalation log, or an empty string if not found
     */
    public static String readEscalationLog() {
        try {
            Path path = Paths.get(AppConstants.ESCALATION_LOG_FILE_PATH);
            if (!Files.exists(path)) {
                return "";
            }
            return Files.readString(path);
        } catch (IOException e) {
            return "Could not read escalation log: " + e.getMessage();
        }
    }

    /**
     * Ensures that the data directory defined by {@link AppConstants#DATA_DIR} exists,
     * creating it (and any parent directories) if necessary.
     *
     * @throws IOException if the directory cannot be created
     */
    private static void ensureDataDirectory() throws IOException {
        Path dir = Paths.get(AppConstants.DATA_DIR);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }
}
