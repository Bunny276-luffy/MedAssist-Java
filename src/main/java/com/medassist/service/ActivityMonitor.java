package com.medassist.service;

/**
 * Simulates activity detection for the patient in the MedAssist system.
 *
 * <p>In a production deployment, this class would integrate with platform-specific
 * sensors (motion, screen touch, microphone activity) to determine whether the
 * patient is currently active. For the current simulation, activity is determined
 * by a combination of the current time-of-day and a random probability factor.</p>
 *
 * <p>This class is stateless and cannot be instantiated.</p>
 *
 * @author MedAssist Team
 * @version 1.0
 * @see ReminderService
 */
public final class ActivityMonitor {

    /** Private constructor prevents instantiation. */
    private ActivityMonitor() {
        throw new UnsupportedOperationException("ActivityMonitor is a utility class.");
    }

    /**
     * Estimates whether the patient is currently active and likely to respond to a reminder.
     *
     * <p>The simulation logic applies two heuristics:
     * <ol>
     *   <li><b>Time-of-day check</b>: The system assumes the patient is inactive between
     *       22:00 (10 PM) and 06:00 (6 AM) — typical sleeping hours.</li>
     *   <li><b>Random probability</b>: During waking hours, there is a 70% chance that
     *       the patient is active. This simulates natural variability (e.g., out of room,
     *       napping).</li>
     * </ol>
     * </p>
     *
     * <p>A production implementation would replace this with real sensor/platform input.</p>
     *
     * @return {@code true} if the patient is estimated to be active, {@code false} otherwise
     */
    public static boolean isUserActive() {
        int hour = java.time.LocalTime.now().getHour();

        // Consider patient inactive during typical sleeping hours
        if (hour >= 22 || hour < 6) {
            System.out.println("[ActivityMonitor] Patient likely asleep (hour=" + hour + ").");
            return false;
        }

        // During waking hours, apply a 70% probability of being active
        boolean active = Math.random() < 0.70;
        System.out.println("[ActivityMonitor] Patient active=" + active
                + " (probabilistic check, hour=" + hour + ").");
        return active;
    }
}
