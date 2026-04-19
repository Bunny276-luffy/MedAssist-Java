package com.medassist.voice;

import com.medassist.model.Language;
import com.medassist.util.AppConstants;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Concrete implementation of {@link SpeechCapable} with natural-language parsing.
 *
 * <p>Enhanced in v2.0 to support structured extraction of medication details
 * (drug name, dosage, time, frequency) from free-form voice/text commands such as:</p>
 * <pre>
 *   "add Aspirin 100mg at 9am"
 *   "schedule Metformin 500mg twice daily at 8:30"
 *   "mark Amlodipine as taken"
 *   "I missed my Aspirin dose"
 * </pre>
 *
 * @author MedAssist Team
 * @version 2.0
 */
public class VoiceEngine implements SpeechCapable {

    // ── Compiled extraction patterns ──────────────────────────────────────────

    /**
     * Matches time expressions like "at 9am", "at 9:30", "at 21:00", "at 9:30pm".
     * Group 1 = hour, Group 2 = optional minutes, Group 3 = optional am/pm.
     */
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "\\bat\\s+(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?",
            Pattern.CASE_INSENSITIVE);

    /**
     * Matches dosage expressions like "500mg", "100 mg", "2 tablets", "1 tab", "5ml".
     */
    private static final Pattern DOSAGE_PATTERN = Pattern.compile(
            "(\\d+\\.?\\d*)\\s*(mg|ml|mcg|tablet|tab|cap|capsule|iu|unit)s?",
            Pattern.CASE_INSENSITIVE);

    /**
     * Matches frequency keywords: once/twice/thrice daily, every X hours, etc.
     */
    private static final Pattern FREQUENCY_PATTERN = Pattern.compile(
            "(once|twice|thrice|\\d+\\s*x)\\s*(a\\s*)?(day|daily|morning|evening|night"
            + "|week|weekly|hour|hours?)(?:\\s*at\\s+\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)?)?",
            Pattern.CASE_INSENSITIVE);

    /**
     * Extracts a drug/medication name after keywords "add", "schedule", "take", "mark", etc.
     * Captures 1–4 capitalized or mixed-case words, stopping before dosage/time keywords.
     */
    private static final Pattern DRUG_NAME_PATTERN = Pattern.compile(
            "(?:add|schedule|take|mark|remind(?:er)?\\s+for|for)\\s+"
            + "([A-Za-z][A-Za-z0-9\\-]*(?:\\s+[A-Za-z][A-Za-z0-9\\-]*){0,3})"
            + "(?=\\s+\\d|\\s+at|\\s+once|\\s+twice|\\s+as|\\s+mg|$)",
            Pattern.CASE_INSENSITIVE);

    /**
     * Fallback: extract drug name after "missed" keyword.
     * e.g., "I missed my Aspirin dose"
     */
    private static final Pattern MISSED_DRUG_PATTERN = Pattern.compile(
            "missed\\s+(?:my\\s+)?([A-Za-z][A-Za-z0-9\\-]*(?:\\s+[A-Za-z][A-Za-z0-9\\-]*){0,2})"
            + "(?:\\s+dose|\\s+medication)?",
            Pattern.CASE_INSENSITIVE);

    /**
     * Fallback: extract drug name after "taken" keyword.
     * e.g., "I have taken Metformin"
     */
    private static final Pattern TAKEN_DRUG_PATTERN = Pattern.compile(
            "taken\\s+(?:my\\s+)?([A-Za-z][A-Za-z0-9\\-]*(?:\\s+[A-Za-z][A-Za-z0-9\\-]*){0,2})",
            Pattern.CASE_INSENSITIVE);

    // ── Fields ────────────────────────────────────────────────────────────────

    /** Last text pushed to the virtual "microphone" from the UI input field. */
    private String pendingInput = null;

    /** Last spoken text, stored for UI retrieval. */
    private String lastSpokenText = "";

    // ── Demo fallback commands ────────────────────────────────────────────────

    private static final String[] DEMO_COMMANDS = {
            "add Aspirin 100mg at 9am once daily",
            "mark Metformin as taken",
            "I missed my Amlodipine dose",
            "schedule Atorvastatin 20mg at 9pm once daily",
            "scan prescription"
    };
    private int demoIdx = 0;

    public VoiceEngine() {}

    // ── SpeechCapable ─────────────────────────────────────────────────────────

    /**
     * Speaks the given text in the specified language, printing to console.
     *
     * @param text     message to speak (or a {@link LanguagePack} key)
     * @param language target language
     */
    @Override
    public void speak(String text, Language language) {
        String output = resolveText(text, language);
        lastSpokenText = "[" + language.name() + "]  " + output;
        System.out.println("🔊 " + lastSpokenText);
    }

    /**
     * Returns the pending input set via {@link #setInput(String)}, or cycles through
     * built-in demo commands if no pending input is set.
     *
     * @return the raw voice/text input string
     */
    @Override
    public String listen() {
        if (pendingInput != null && !pendingInput.isBlank()) {
            String input = pendingInput;
            pendingInput = null;
            System.out.println("🎤 [VoiceEngine] Input: \"" + input + "\"");
            return input;
        }
        String cmd = DEMO_COMMANDS[demoIdx % DEMO_COMMANDS.length];
        demoIdx++;
        System.out.println("🎤 [VoiceEngine] Demo: \"" + cmd + "\"");
        return cmd;
    }

    /**
     * Parses raw input and returns a CMD_* constant from {@link AppConstants}.
     *
     * @param input raw voice/text input
     * @return command type constant
     */
    @Override
    public String parseCommand(String input) {
        if (input == null || input.isBlank()) return AppConstants.CMD_UNKNOWN;
        String lo = input.toLowerCase();

        // Order matters — check more-specific before less-specific
        if (lo.contains("add"))                          return AppConstants.CMD_ADD;
        if (lo.contains("schedule") || lo.contains("remind")) return AppConstants.CMD_SCHEDULE;
        if (lo.contains("taken") || lo.contains("take") || lo.contains("took"))
                                                          return AppConstants.CMD_TAKEN;
        if (lo.contains("missed") || lo.contains("miss")) return AppConstants.CMD_MISSED;
        if (lo.contains("scan") || lo.contains("prescription")) return AppConstants.CMD_SCAN;
        return AppConstants.CMD_UNKNOWN;
    }

    // ── Natural-language extraction ───────────────────────────────────────────

    /**
     * Extracts structured medication details from a natural-language command string.
     *
     * <p>Returns a map with zero or more of these keys:
     * <ul>
     *   <li>{@code "drugName"}  — e.g., {@code "Aspirin"}</li>
     *   <li>{@code "dosage"}    — e.g., {@code "100mg"}</li>
     *   <li>{@code "time"}      — 24-hour string e.g., {@code "09:00"}</li>
     *   <li>{@code "frequency"} — e.g., {@code "once daily"}</li>
     * </ul>
     * </p>
     *
     * <p>Examples:
     * <pre>
     *   "add Aspirin 100mg at 9am"           → {drugName:"Aspirin", dosage:"100mg", time:"09:00"}
     *   "mark Metformin as taken"             → {drugName:"Metformin"}
     *   "I missed my Amlodipine dose"         → {drugName:"Amlodipine"}
     *   "schedule Atorvastatin at 9pm twice daily" → {drugName:"Atorvastatin", time:"21:00", frequency:"twice daily"}
     * </pre>
     * </p>
     *
     * @param input the raw command string; must not be {@code null}
     * @return a map of extracted field names to their string values
     */
    public Map<String, String> extractMedicationDetails(String input) {
        Map<String, String> result = new HashMap<>();
        if (input == null || input.isBlank()) return result;

        // Drug name
        Matcher m = DRUG_NAME_PATTERN.matcher(input);
        if (m.find()) {
            result.put("drugName", cleanDrugName(m.group(1)));
        } else {
            // Try missed/taken fallbacks
            Matcher mm = MISSED_DRUG_PATTERN.matcher(input);
            if (mm.find()) result.put("drugName", cleanDrugName(mm.group(1)));
            else {
                Matcher tm = TAKEN_DRUG_PATTERN.matcher(input);
                if (tm.find()) result.put("drugName", cleanDrugName(tm.group(1)));
            }
        }

        // Dosage
        Matcher dm = DOSAGE_PATTERN.matcher(input);
        if (dm.find()) {
            result.put("dosage", dm.group(1).trim() + dm.group(2).toLowerCase());
        }

        // Time
        Matcher tm = TIME_PATTERN.matcher(input);
        if (tm.find()) {
            result.put("time", parseTime(tm.group(1), tm.group(2), tm.group(3)));
        }

        // Frequency
        Matcher fm = FREQUENCY_PATTERN.matcher(input);
        if (fm.find()) {
            result.put("frequency", fm.group(0).trim());
        }

        return result;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Sets text that will be returned as the next {@link #listen()} result.
     * Called from the VoicePanel when the user types a command.
     *
     * @param text the command text to inject
     */
    public void setInput(String text) {
        this.pendingInput = text;
    }

    /**
     * Returns the last spoken text.
     *
     * @return last spoken string
     */
    public String getLastSpokenText() {
        return lastSpokenText;
    }

    /**
     * Resolves a text string: if it matches a LanguagePack key, returns the
     * localised string; otherwise returns the raw text.
     *
     * @param text     input text or key
     * @param language target language
     * @return resolved string
     */
    private String resolveText(String text, Language language) {
        if (text.equalsIgnoreCase(LanguagePack.KEY_GREETING))         return LanguagePack.get(language, LanguagePack.KEY_GREETING);
        if (text.equalsIgnoreCase(LanguagePack.KEY_REMINDER))         return LanguagePack.get(language, LanguagePack.KEY_REMINDER);
        if (text.equalsIgnoreCase(LanguagePack.KEY_TAKEN_CONFIRM))    return LanguagePack.get(language, LanguagePack.KEY_TAKEN_CONFIRM);
        if (text.equalsIgnoreCase(LanguagePack.KEY_MISSED_ALERT))     return LanguagePack.get(language, LanguagePack.KEY_MISSED_ALERT);
        if (text.equalsIgnoreCase(LanguagePack.KEY_ESCALATION_SENT))  return LanguagePack.get(language, LanguagePack.KEY_ESCALATION_SENT);
        return text;
    }

    /**
     * Converts extracted hour/minute/ampm strings into a 24-hour "HH:mm" string.
     *
     * @param hourStr  hour string (1–12 or 0–23)
     * @param minStr   minute string or {@code null}
     * @param ampm     "am", "pm", or {@code null}
     * @return formatted 24-hour time string
     */
    private String parseTime(String hourStr, String minStr, String ampm) {
        try {
            int hour   = Integer.parseInt(hourStr);
            int minute = (minStr != null) ? Integer.parseInt(minStr) : 0;

            if (ampm != null) {
                if (ampm.equalsIgnoreCase("pm") && hour < 12) hour += 12;
                if (ampm.equalsIgnoreCase("am") && hour == 12) hour = 0;
            } else if (hour < 7 && ampm == null) {
                // Heuristic: hours 1–6 without am/pm are likely PM (e.g., "at 9" → 9am is fine, "at 2" → 2pm)
                // Leave ambiguous — user can adjust in the dialog
            }

            return String.format("%02d:%02d", hour, minute);
        } catch (NumberFormatException e) {
            return LocalTime.now().plusHours(1).format(DateTimeFormatter.ofPattern("HH:mm"));
        }
    }

    /**
     * Removes trailing noise words ("dose", "medication", "medicine", "tablet") from
     * an extracted drug name and trims whitespace.
     *
     * @param raw the raw extracted drug name
     * @return cleaned drug name string
     */
    private String cleanDrugName(String raw) {
        return raw.replaceAll("(?i)\\s+(dose|medication|medicine|tablet|tab|pill)s?$", "").trim();
    }
}
