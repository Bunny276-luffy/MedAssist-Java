package com.medassist.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class providing static regex-based parsing of raw OCR prescription text.
 *
 * <p>The {@link #parsePrescription(String)} method applies a layered set of regex
 * strategies to extract drug name, dosage, and frequency from free-text prescription
 * strings produced by the OCR engine — including real-world Indian drug packaging
 * such as blister packs and strip labels. Results are returned as a
 * {@code Map<String, String>} keyed by field name.</p>
 *
 * <p>This class is stateless and thread-safe. It cannot be instantiated.</p>
 *
 * @author MedAssist Team
 * @version 2.0
 * @see com.medassist.service.OCRService
 */
public final class RegexParser {

    // -------------------------------------------------------------------------
    // Strategy 1 — Labelled-field patterns (structured prescription text)
    // -------------------------------------------------------------------------

    /**
     * Matches drug name prefixes like "Drug:", "Medicine:", "Rx:", "Medication:".
     * Captures the drug name that follows on the same line.
     */
    private static final Pattern DRUG_LABEL_PATTERN = Pattern.compile(
            "(?i)(?:drug|medicine|rx|medic(?:ation)?)[:\\s]+([A-Za-z0-9\\s\\-]+?)(?=\\n|,|\\d{3}|$)",
            Pattern.MULTILINE);

    /**
     * Matches dosage with an explicit label: "Dosage: 500mg", "Dose: 1 tablet", "Strength: 10mg".
     */
    private static final Pattern DOSAGE_LABEL_PATTERN = Pattern.compile(
            "(?i)(?:dosage|dose|strength|each\\s+(?:tablet|cap(?:sule)?)\\s+contains?)[:\\s]+([\\d]+\\s*(?:mg|ml|mcg|g|iu|unit|tablet|tab|cap|capsule)s?)",
            Pattern.MULTILINE);

    /**
     * Matches frequency with an explicit label: "Frequency:", "Direction:", "Take:", "Sig:".
     */
    private static final Pattern FREQUENCY_LABEL_PATTERN = Pattern.compile(
            "(?i)(?:frequency|freq|direction|take|sig|dosing|schedule)[:\\s]+([^\\n,;]{4,60})",
            Pattern.MULTILINE);

    /**
     * Matches duration with an explicit label: "Duration: 30 days", "For: 2 weeks".
     */
    private static final Pattern DURATION_LABEL_PATTERN = Pattern.compile(
            "(?i)(?:duration|for|days?\\s*supply)[:\\s]+(\\d+\\s*(?:days?|weeks?|months?))",
            Pattern.MULTILINE);

    // -------------------------------------------------------------------------
    // Strategy 2 — Bare dosage + drug name (real blister/strip packaging)
    // -------------------------------------------------------------------------

    /**
     * Matches Indian packaging suffixes "Tablets IP", "Capsules IP", "Syrup IP" etc.
     * The word before the suffix is typically the drug name.
     * e.g. "Azithromycin Tablets IP" → drug = "Azithromycin".
     */
    private static final Pattern INDIAN_PACKAGING_PATTERN = Pattern.compile(
            "\\b([A-Z][a-zA-Z]{2,}(?:\\s[A-Z][a-zA-Z]{2,})?)\\s+"
            + "(?:Tablets?|Capsules?|Syrup|Injection|Cream|Gel|Drops?)\\s+IP\\b",
            Pattern.CASE_INSENSITIVE);

    /**
     * Matches a drug name immediately followed by a dosage number + unit.
     * Handles: "Azithromycin 500mg", "Metformin 500 mg", "Amlodipine 5mg".
     */
    private static final Pattern DRUG_WITH_DOSAGE_PATTERN = Pattern.compile(
            "\\b([A-Z][a-z]{2,}(?:\\s[A-Z][a-z]{2,})?)\\s+(\\d+(?:\\.\\d+)?\\s*(?:mg|ml|mcg|g|iu))",
            Pattern.CASE_INSENSITIVE);

    /**
     * Matches a bare dosage number+unit appearing anywhere in the text without a label —
     * common on Indian blister packs: "500 mg", "10mg", "250mcg".
     */
    private static final Pattern BARE_DOSAGE_PATTERN = Pattern.compile(
            "\\b(\\d{1,4}(?:\\.\\d+)?\\s*(?:mg|ml|mcg|g|iu))\\b",
            Pattern.CASE_INSENSITIVE);

    // -------------------------------------------------------------------------
    // Strategy 3 — Frequency heuristics (no label)
    // -------------------------------------------------------------------------

    /**
     * Matches natural-language frequency phrases without a label prefix.
     * e.g. "once daily", "twice a day", "three times daily", "every 8 hours".
     */
    private static final Pattern NATURAL_FREQUENCY_PATTERN = Pattern.compile(
            "(?i)\\b(once\\s+daily|twice\\s+(?:a\\s+)?daily|three\\s+times\\s+(?:a\\s+)?daily"
            + "|every\\s+\\d+\\s+hours?|at\\s+bedtime|with\\s+meals?"
            + "|as\\s+needed|once\\s+a\\s+(?:day|week)|four\\s+times\\s+daily)\\b");

    /**
     * Matches Indian M-A-N dose schedule notation like "1-0-1", "1-1-1", "0-0-1"
     * (morning-afternoon-night tablet counts).
     */
    private static final Pattern DOSE_SCHEDULE_PATTERN = Pattern.compile(
            "\\b([0-2]-[0-2]-[0-2])\\b");

    /**
     * Matches "FOR 3 TABS" quantity instructions common on Indian OTC packs.
     */
    private static final Pattern QUANTITY_PATTERN = Pattern.compile(
            "(?i)FOR\\s+(\\d+)\\s+TAB(?:S|LET)?S?");

    // -------------------------------------------------------------------------
    // Strategy 4 — Last-resort: first meaningful capitalised word
    // -------------------------------------------------------------------------

    /**
     * Last-resort fallback: captures the first word of 5+ characters starting with
     * an uppercase letter — almost certainly a drug name on any packaging.
     */
    private static final Pattern LAST_RESORT_DRUG_PATTERN = Pattern.compile(
            "\\b([A-Z][a-zA-Z]{4,})\\b");

    /** Words to skip in last-resort drug name extraction. */
    private static final java.util.Set<String> SKIP_WORDS = new java.util.HashSet<>(
            java.util.Arrays.asList(
                    "patient", "doctor", "batch", "expiry", "store", "india",
                    "limited", "pharma", "pharmaceutical", "manufacturing",
                    "laboratories", "healthcare", "tablets", "capsules",
                    "injection", "includes", "warning", "directions",
                    "ingredients", "informations", "storage", "license"
            ));

    /** Private constructor prevents instantiation of this utility class. */
    private RegexParser() {
        throw new UnsupportedOperationException("RegexParser is a utility class.");
    }

    /**
     * Parses a raw OCR text string and extracts prescription fields using a
     * layered set of regex strategies, from most-specific to most-general.
     *
     * <p>The returned map contains zero or more of the following keys:
     * <ul>
     *   <li>{@code "drugName"} — extracted drug name</li>
     *   <li>{@code "dosage"} — extracted dosage (e.g., "500mg")</li>
     *   <li>{@code "frequency"} — extracted frequency (e.g., "twice daily")</li>
     *   <li>{@code "duration"} — extracted duration (e.g., "30 days")</li>
     * </ul>
     * At minimum, a {@code "drugName"} will always be returned via the last-resort
     * heuristic if any meaningful alphabetic text is present in the OCR output.</p>
     *
     * @param ocrText the raw text produced by the OCR engine; must not be {@code null}
     * @return a {@link Map} of extracted field names to their string values
     */
    public static Map<String, String> parsePrescription(String ocrText) {
        Map<String, String> result = new HashMap<>();

        if (ocrText == null || ocrText.isBlank()) {
            return result;
        }

        // ── Strategy 1: Labelled fields (structured text) ─────────────────────
        extractFirst(DRUG_LABEL_PATTERN,      ocrText, "drugName",  result);
        extractFirst(DOSAGE_LABEL_PATTERN,    ocrText, "dosage",    result);
        extractFirst(FREQUENCY_LABEL_PATTERN, ocrText, "frequency", result);
        extractFirst(DURATION_LABEL_PATTERN,  ocrText, "duration",  result);

        // ── Strategy 2a: "DrugName Tablets IP" packaging ──────────────────────
        if (!result.containsKey("drugName")) {
            Matcher m = INDIAN_PACKAGING_PATTERN.matcher(ocrText);
            if (m.find()) {
                result.put("drugName", capitalize(m.group(1).trim()));
            }
        }

        // ── Strategy 2b: "DrugName 500mg" inline dosage ───────────────────────
        if (!result.containsKey("drugName")) {
            Matcher m = DRUG_WITH_DOSAGE_PATTERN.matcher(ocrText);
            if (m.find()) {
                result.put("drugName", capitalize(m.group(1).trim()));
                if (!result.containsKey("dosage")) {
                    result.put("dosage", m.group(2).trim().toLowerCase());
                }
            }
        }

        // ── Strategy 2c: Bare dosage number anywhere in text ──────────────────
        if (!result.containsKey("dosage")) {
            Matcher m = BARE_DOSAGE_PATTERN.matcher(ocrText);
            if (m.find()) {
                result.put("dosage", m.group(1).trim().toLowerCase());
            }
        }

        // ── Strategy 3a: Natural-language frequency phrase ────────────────────
        if (!result.containsKey("frequency")) {
            Matcher m = NATURAL_FREQUENCY_PATTERN.matcher(ocrText);
            if (m.find()) {
                result.put("frequency", m.group(1).trim().toLowerCase());
            }
        }

        // ── Strategy 3b: M-A-N dose schedule (e.g. 1-0-1) ────────────────────
        if (!result.containsKey("frequency")) {
            Matcher m = DOSE_SCHEDULE_PATTERN.matcher(ocrText);
            if (m.find()) {
                result.put("frequency", decodeSchedule(m.group(1)));
            }
        }

        // ── Strategy 3c: "FOR 3 TABS" quantity instruction ────────────────────
        if (!result.containsKey("frequency")) {
            Matcher m = QUANTITY_PATTERN.matcher(ocrText);
            if (m.find()) {
                result.put("frequency", m.group(1) + " tablets per dose");
            }
        }

        // ── Strategy 4: Last-resort drug name from first meaningful word ───────
        if (!result.containsKey("drugName")) {
            Matcher m = LAST_RESORT_DRUG_PATTERN.matcher(ocrText);
            while (m.find()) {
                String word = m.group(1);
                if (!SKIP_WORDS.contains(word.toLowerCase())) {
                    result.put("drugName", capitalize(word));
                    break;
                }
            }
        }

        System.out.println("[RegexParser] Extracted fields: " + result);
        return result;
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    /**
     * Applies a regex pattern to the input text and, if a match is found,
     * stores the first capturing group value under the given key in the result map.
     * Does nothing if the key is already present (earlier strategies take priority).
     *
     * @param pattern the compiled regex pattern
     * @param text    the text to search
     * @param key     the map key to store the result under
     * @param result  the result map to update
     */
    private static void extractFirst(Pattern pattern, String text,
                                     String key, Map<String, String> result) {
        if (result.containsKey(key)) return;
        Matcher m = pattern.matcher(text);
        if (m.find()) {
            String value = m.group(1).trim();
            if (!value.isBlank()) {
                result.put(key, value);
            }
        }
    }

    /**
     * Capitalizes the first letter of each word in {@code s}.
     *
     * @param s the input string
     * @return title-cased string
     */
    private static String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        String[] words = s.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)))
                  .append(w.substring(1).toLowerCase())
                  .append(' ');
            }
        }
        return sb.toString().trim();
    }

    /**
     * Decodes an Indian "M-A-N" tablet schedule string into a human-readable frequency.
     * For example, "1-0-1" becomes "twice daily (morning and night)".
     *
     * @param schedule the M-A-N formatted string (e.g. "1-0-1")
     * @return a human-readable frequency description
     */
    private static String decodeSchedule(String schedule) {
        String[] parts = schedule.split("-");
        if (parts.length != 3) return schedule;
        int m = Integer.parseInt(parts[0]);
        int a = Integer.parseInt(parts[1]);
        int n = Integer.parseInt(parts[2]);
        int total = m + a + n;
        if (total == 1) return "once daily";
        if (total == 2 && a == 0) return "twice daily (morning and night)";
        if (total == 2) return "twice daily";
        if (total == 3) return "three times daily";
        return schedule + " (morning-afternoon-night)";
    }
}
