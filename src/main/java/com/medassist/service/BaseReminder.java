package com.medassist.service;

import com.medassist.model.Medication;
import com.medassist.model.Patient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Abstract base class for all reminder scheduling components in MedAssist.
 *
 * <p>{@code BaseReminder} owns the shared {@link ScheduledExecutorService} used to
 * schedule time-based reminder tasks. Concrete subclasses (e.g., {@link ReminderService})
 * override {@link #fireAlert(Patient, Medication)} to define what happens when a
 * reminder fires (voice output, notification, escalation, etc.).</p>
 *
 * <p>Subclasses call {@link #scheduleReminder(Runnable, long, TimeUnit)} to register
 * a task, and {@link #cancelReminder()} to shut down the scheduler gracefully.</p>
 *
 * @author MedAssist Team
 * @version 1.0
 * @see ReminderService
 */
public abstract class BaseReminder {

    /**
     * Shared single-threaded scheduler for all reminder tasks.
     * A daemon-like executor that fires tasks at their configured delays.
     */
    protected final ScheduledExecutorService scheduler;

    /**
     * Holds a reference to the last scheduled future so it can be cancelled.
     */
    protected ScheduledFuture<?> scheduledFuture;

    /**
     * Initialises the {@code BaseReminder} with a cached scheduled thread pool.
     */
    protected BaseReminder() {
        this.scheduler = Executors.newScheduledThreadPool(4);
    }

    /**
     * Schedules a {@link Runnable} reminder task to execute after the given delay.
     *
     * <p>The returned {@link ScheduledFuture} is stored internally and can be
     * cancelled by calling {@link #cancelReminder()}.</p>
     *
     * @param task     the reminder task to run; must not be {@code null}
     * @param delay    the delay before execution
     * @param timeUnit the unit for the delay parameter
     */
    public void scheduleReminder(Runnable task, long delay, TimeUnit timeUnit) {
        scheduledFuture = scheduler.schedule(task, delay, timeUnit);
    }

    /**
     * Cancels the currently scheduled reminder and shuts down the scheduler.
     *
     * <p>Running tasks are allowed to complete; new tasks will not be accepted.
     * This method blocks for up to 5 seconds waiting for termination.</p>
     */
    public void cancelReminder() {
        if (scheduledFuture != null && !scheduledFuture.isDone()) {
            scheduledFuture.cancel(false);
        }
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
     * Fires a medication alert for the given patient and medication.
     *
     * <p>Subclasses must implement this method to define the alert behaviour —
     * for example, calling the voice engine, sending a push notification, or
     * updating the UI.</p>
     *
     * @param patient    the patient for whom the alert is firing; must not be {@code null}
     * @param medication the scheduled medication that triggered the alert; must not be {@code null}
     */
    public abstract void fireAlert(Patient patient, Medication medication);
}
