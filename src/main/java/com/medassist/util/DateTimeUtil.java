package com.medassist.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class providing static helper methods for formatting {@link LocalTime}
 * and {@link LocalDate} values into human-readable display strings.
 *
 * <p>All methods in this class are stateless and thread-safe. This class cannot
 * be instantiated.</p>
 *
 * @author MedAssist Team
 * @version 1.0
 */
public final class DateTimeUtil {

    /** Formatter for displaying time in 12-hour format with AM/PM (e.g., "08:30 AM"). */
    private static final DateTimeFormatter TIME_DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("hh:mm a");

    /** Formatter for displaying dates in long human-readable format (e.g., "April 15, 2026"). */
    private static final DateTimeFormatter DATE_DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    /** Formatter for storing time in ISO-8601 format (e.g., "08:30"). */
    private static final DateTimeFormatter TIME_STORAGE_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    /** Private constructor prevents instantiation. */
    private DateTimeUtil() {
        throw new UnsupportedOperationException("DateTimeUtil is a utility class.");
    }

    /**
     * Formats a {@link LocalTime} for display using 12-hour notation with AM/PM.
     *
     * <p>Example: {@code LocalTime.of(8, 30)} → {@code "08:30 AM"}</p>
     *
     * @param time the time to format; must not be {@code null}
     * @return formatted time string in "hh:mm a" pattern
     */
    public static String formatTimeForDisplay(LocalTime time) {
        if (time == null) {
            return "N/A";
        }
        return time.format(TIME_DISPLAY_FORMATTER);
    }

    /**
     * Formats a {@link LocalDate} for display in long form.
     *
     * <p>Example: {@code LocalDate.of(2026, 4, 15)} → {@code "April 15, 2026"}</p>
     *
     * @param date the date to format; must not be {@code null}
     * @return formatted date string in "MMMM dd, yyyy" pattern
     */
    public static String formatDateForDisplay(LocalDate date) {
        if (date == null) {
            return "N/A";
        }
        return date.format(DATE_DISPLAY_FORMATTER);
    }

    /**
     * Formats a {@link LocalTime} for storage or log output using 24-hour (ISO) format.
     *
     * <p>Example: {@code LocalTime.of(14, 45)} → {@code "14:45"}</p>
     *
     * @param time the time to format; must not be {@code null}
     * @return formatted time string in "HH:mm" pattern
     */
    public static String formatTimeForStorage(LocalTime time) {
        if (time == null) {
            return "00:00";
        }
        return time.format(TIME_STORAGE_FORMATTER);
    }

    /**
     * Returns today's date as a display-friendly string.
     *
     * @return today's date formatted with {@link #formatDateForDisplay(LocalDate)}
     */
    public static String todayAsDisplayString() {
        return formatDateForDisplay(LocalDate.now());
    }

    /**
     * Calculates the number of seconds from now until the next occurrence of the given time.
     *
     * <p>If the scheduled time is in the past today, zero is returned (the reminder
     * fires immediately and the caller is expected to handle re-scheduling).</p>
     *
     * @param scheduledTime the target time today; must not be {@code null}
     * @return seconds until the scheduled time, or 0 if already past
     */
    public static long secondsUntil(LocalTime scheduledTime) {
        LocalTime now = LocalTime.now();
        if (scheduledTime.isBefore(now)) {
            return 0;
        }
        return java.time.Duration.between(now, scheduledTime).getSeconds();
    }
}
