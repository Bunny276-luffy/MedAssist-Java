package com.medassist.service;

import java.awt.MouseInfo;
import java.awt.Point;
import java.time.LocalTime;

/**
 * Detects whether the patient is currently active by monitoring real system input.
 *
 * <p>Uses {@link MouseInfo} from the AWT toolkit to sample the mouse cursor position
 * at two points in time (separated by {@value #POLL_INTERVAL_MS} ms). If the position
 * has not changed, the system is considered idle and the patient likely inactive.</p>
 *
 * <p>A time-of-day guard is also applied: between 22:00 and 06:00 the patient is
 * always treated as inactive (sleep hours), regardless of mouse movement.</p>
 *
 * <p>This class is stateless and cannot be instantiated.</p>
 *
 * @author MedAssist Team
 * @version 2.0
 * @see ReminderService
 */
public final class ActivityMonitor {

    /**
     * How long (ms) to wait between the two mouse-position samples.
     * 30 seconds gives a realistic "idle" detection window without blocking the caller
     * too long — callers should invoke this from a background thread.
     */
    private static final long POLL_INTERVAL_MS = 30_000L;

    /** Private constructor prevents instantiation. */
    private ActivityMonitor() {
        throw new UnsupportedOperationException("ActivityMonitor is a utility class.");
    }

    /**
     * Determines whether the patient is currently active by comparing two mouse
     * cursor positions sampled {@value #POLL_INTERVAL_MS} milliseconds apart.
     *
     * <p>Decision logic:
     * <ol>
     *   <li><b>Sleep-hour guard</b>: returns {@code false} immediately if the current
     *       hour is between 22:00 and 06:00.</li>
     *   <li><b>Mouse-idle check</b>: takes an initial cursor reading, sleeps for
     *       {@value #POLL_INTERVAL_MS} ms, then takes a second reading.
     *       If the cursor coordinates are identical, the patient is considered inactive.</li>
     *   <li><b>Fallback</b>: if {@link MouseInfo#getPointerInfo()} returns {@code null}
     *       (headless environment), falls back to a 70 % probability heuristic.</li>
     * </ol>
     * </p>
     *
     * @return {@code true} if mouse movement was detected (patient active),
     *         {@code false} if the cursor was stationary or it is a sleep hour
     */
    public static boolean isUserActive() {
        int hour = LocalTime.now().getHour();

        // ── 1. Sleep-hour guard ──────────────────────────────────────────────
        if (hour >= 22 || hour < 6) {
            System.out.println("[ActivityMonitor] Sleep hours — patient inactive (hour=" + hour + ").");
            return false;
        }

        // ── 2. Mouse idle detection ──────────────────────────────────────────
        try {
            java.awt.PointerInfo pi1 = MouseInfo.getPointerInfo();
            if (pi1 == null) {
                // Headless environment — fall back to probabilistic check
                return probabilisticFallback(hour);
            }

            Point pos1 = pi1.getLocation();
            System.out.println("[ActivityMonitor] Mouse at (" + pos1.x + "," + pos1.y
                    + ") — waiting " + (POLL_INTERVAL_MS / 1000) + "s to detect movement…");

            Thread.sleep(POLL_INTERVAL_MS);

            java.awt.PointerInfo pi2 = MouseInfo.getPointerInfo();
            if (pi2 == null) {
                return probabilisticFallback(hour);
            }

            Point pos2 = pi2.getLocation();
            boolean moved = !pos1.equals(pos2);

            System.out.println("[ActivityMonitor] Mouse was " + (moved ? "MOVED" : "STATIONARY")
                    + " (" + pos2.x + "," + pos2.y + ") — patient " + (moved ? "ACTIVE" : "INACTIVE") + ".");
            return moved;

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            System.out.println("[ActivityMonitor] Interrupted during idle check — assuming active.");
            return true;
        } catch (Exception e) {
            System.err.println("[ActivityMonitor] Mouse detection error: " + e.getMessage()
                    + " — using fallback.");
            return probabilisticFallback(hour);
        }
    }

    /**
     * Probabilistic fallback used when mouse detection is unavailable.
     * Returns {@code true} with 70 % probability during waking hours.
     *
     * @param hour current hour of day
     * @return {@code true} if randomly determined to be active
     */
    private static boolean probabilisticFallback(int hour) {
        boolean active = Math.random() < 0.70;
        System.out.println("[ActivityMonitor] Fallback — patient active=" + active
                + " (probabilistic, hour=" + hour + ").");
        return active;
    }
}
