package com.medassist.voice;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Uses Windows' built-in {@code System.Speech.Recognition} engine (via PowerShell)
 * to capture microphone audio and return a transcribed text string.
 *
 * <p>No additional downloads, API keys, or dependencies are required — the
 * {@code System.Speech} assembly ships with every Windows installation that
 * has .NET Framework 3.5+ (Windows 7 and later).</p>
 *
 * <p><b>How it works:</b>
 * <ol>
 *   <li>A {@code ProcessBuilder} spawns {@code powershell.exe} with an inline script.</li>
 *   <li>The script creates a {@code SpeechRecognitionEngine} with a dictation grammar.</li>
 *   <li>It listens on the default audio device for up to {@link #TIMEOUT_SECONDS} seconds.</li>
 *   <li>The recognised text (or an empty string on timeout) is written to stdout.</li>
 *   <li>Java reads stdout and delivers the result via the {@code Consumer<String>} callback.</li>
 * </ol>
 * </p>
 *
 * @author MedAssist Team
 * @version 1.0
 */
public class WindowsSpeechRecognizer {

    /** Maximum seconds to wait for a recognition result before giving up. */
    private static final int TIMEOUT_SECONDS = 10;

    /**
     * Improved PowerShell script with:
     * - 2-second end-silence timeout so it doesn't cut off too early
     * - 6-second initial silence timeout so it waits for you to start speaking
     * - BabbleTimeout disabled (handles background noise better)
     * - Multiple alternates returned, ranked by confidence, newline-separated
     *   so VoicePanel can show the top results and let the user pick/edit
     */
    private static final String PS_SCRIPT =
            "Add-Type -AssemblyName System.Speech; " +
            "$r = New-Object System.Speech.Recognition.SpeechRecognitionEngine; " +
            "$g = New-Object System.Speech.Recognition.DictationGrammar; " +
            "$r.LoadGrammar($g); " +
            "$r.SetInputToDefaultAudioDevice(); " +
            "$r.InitialSilenceTimeout = [TimeSpan]::FromSeconds(6); " +
            "$r.EndSilenceTimeout     = [TimeSpan]::FromSeconds(2); " +
            "$r.BabbleTimeout         = [TimeSpan]::FromSeconds(0); " +
            "$timeout = [TimeSpan]::FromSeconds(" + TIMEOUT_SECONDS + "); " +
            "$result = $r.Recognize($timeout); " +
            "if ($result -ne $null) { " +
            "  Write-Output $result.Text; " +
            "  foreach ($alt in $result.Alternates) { Write-Output $alt.Text } " +
            "} else { Write-Output '' }; " +
            "$r.Dispose()";

    private Future<?> activeFuture;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "MedAssist-SpeechRecognizer");
        t.setDaemon(true);
        return t;
    });

    /**
     * Starts listening on the default microphone.
     *
     * <p>The method is non-blocking. Results are delivered asynchronously via the
     * two callbacks:</p>
     * <ul>
     *   <li>{@code onResult} — called with the transcribed text (may be empty on timeout).</li>
     *   <li>{@code onError}  — called with a human-readable error message on failure.</li>
     * </ul>
     *
     * @param onResult callback receiving the transcribed string (never {@code null})
     * @param onError  callback receiving an error description string
     */
    public void startListening(Consumer<String> onResult, Consumer<String> onError) {
        if (activeFuture != null && !activeFuture.isDone()) {
            onError.accept("Already listening — please wait for the current recording to finish.");
            return;
        }

        activeFuture = executor.submit(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        "powershell.exe", "-NoProfile", "-NonInteractive", "-Command", PS_SCRIPT);
                pb.redirectErrorStream(true);
                Process process = pb.start();

                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader =
                        new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String trimmed = line.trim();
                        if (!trimmed.isBlank()) {
                            if (sb.length() > 0) sb.append("\n");
                            sb.append(trimmed);
                        }
                    }
                }

                boolean finished = process.waitFor(TIMEOUT_SECONDS + 5L, TimeUnit.SECONDS);
                if (!finished) {
                    process.destroyForcibly();
                    onError.accept("Speech recognition timed out after " + TIMEOUT_SECONDS + " seconds.");
                    return;
                }

                String result = sb.toString().trim();
                onResult.accept(result);

            } catch (Exception ex) {
                String msg = ex.getMessage();
                if (msg == null) msg = ex.getClass().getSimpleName();
                onError.accept("Speech recognition failed: " + msg
                        + " (Check that Windows Speech Recognition is enabled in Control Panel).");
            }
        });
    }

    /**
     * Attempts to cancel an in-progress recognition by forcibly terminating
     * the PowerShell process. Since we cannot signal the subprocess directly,
     * we cancel the Future and rely on the OS to clean up.
     */
    public void stopListening() {
        if (activeFuture != null) {
            activeFuture.cancel(true);
        }
    }

    /**
     * Returns {@code true} if a recognition session is currently in progress.
     *
     * @return whether the recognizer is actively listening
     */
    public boolean isListening() {
        return activeFuture != null && !activeFuture.isDone();
    }

    /**
     * Shuts down the internal thread pool. Call when the application closes.
     */
    public void shutdown() {
        executor.shutdownNow();
    }
}
