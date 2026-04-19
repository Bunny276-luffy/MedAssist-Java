package com.medassist.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class providing static regex-based parsing of raw OCR prescription text.
 *
 * <p>The {@link #parsePrescription(String)} method applies a set of named regex patterns
 * to extract drug name, dosage, and frequency from free-text prescription strings
 * produced by the OCR engine. Results are returned as a {@code Map<String, String>}
 * keyed by field name.</p>
 *
 * <p>This class is stateless and thread-safe. It cannot be instantiated.</p>
 *
 * @author MedAssist Team
 * @version 1.0
 * @see com.medassist.service.OCRService
 */
public final class RegexParser {

    // -------------------------------------------------------------------------
    // Compiled Regex Patterns
    // -------------------------------------------------------------------------

    /**
     * Matches drug name prefixes like "Drug:", "Medicine:", "Rx:", "Medic:" (case-insensitive).
     * Captures the drug name that follows on the same line.
     */
    private static final Pattern DRUG_NAME_PATTERN = Pattern.compile(
            "(?i)(?:drug|medicine|rx|medic(?:ation)?)[:\\s]+([A-Za-z0-9\\s\\-]+?)(?=\\n|,|$)",
            Pattern.MULTILINE);

    /**
     * Matches dosage patterns like "Dosage: 500mg", "Dose: 1 tablet", "Strength: 10mg".
     * Captures the dosage value.
     */
    private static final Pattern DOSAGE_PATTERN = Pattern.compile(
            "(?i)(?:dosage|dose|strength)[:\\s]+([\\d]+\\s*(?:mg|ml|mcg|tablet|tab|cap|capsule|iu|unit)s?)",
            Pattern.MULTILINE);

    /**
     * Matches frequency instructions like "Frequency: twice daily", "Take: thrice a day",
     * "Direction: once every morning".
     */
    private static final Pattern FREQUENCY_PATTERN = Pattern.compile(
            "(?i)(?:frequency|freq|direction|take|sig)[:\\s]+([^\\n,;]+)",
            Pattern.MULTILINE);

    /**
     * Matches duration instructions like "Duration: 30 days", "For: 2 weeks".
     */
    private static final Pattern DURATION_PATTERN = Pattern.compile(
            "(?i)(?:duration|for|days?supply)[:\\s]+(\\d+\\s*(?:days?|weeks?|months?))",
            Pattern.MULTILINE);

    /**
     * Fallback pattern: matches a standalone drug name (capitalized word followed
     * optionally by a dosage number and unit) when no keyword prefix is found.
     */
    private static final Pattern FALLBACK_DRUG_PATTERN = Pattern.compile(
            "\\b([A-Z][a-zA-Z]+(?:\\s[A-Z][a-zA-Z]+)?)\\s+(\\d+\\s*(?:mg|ml|mcg|tablet|cap))");

    /** Private constructor prevents instantiation of this utility class. */
    private RegexParser() {
        throw new UnsupportedOperationException("RegexParser is a utility class.");
    }

    /**
     * Parses a raw OCR text string and extracts prescription fields using regex patterns.
     *
     * <p>The returned map contains zero or more of the following keys:
     * <ul>
     *   <li>{@code "drugName"} — extracted drug name</li>
     *   <li>{@code "dosage"} — extracted dosage (e.g., "500mg")</li>
     *   <li>{@code "frequency"} — extracted frequency (e.g., "twice daily")</li>
     *   <li>{@code "duration"} — extracted duration (e.g., "30 days")</li>
     * </ul>
     * If a field cannot be found, its key will not be present in the returned map.</p>
     *
     * @param ocrText the raw text produced by the OCR engine; must not be {@code null}
     * @return a {@link Map} of extracted field names to their string values
     */
    public static Map<String, String> parsePrescription(String ocrText) {
        Map<String, String> result = new HashMap<>();

        if (ocrText == null || ocrText.isBlank()) {
            return result;
        }

        // Try primary patterns first
        extractFirst(DRUG_NAME_PATTERN, ocrText, "drugName", result);
        extractFirst(DOSAGE_PATTERN, ocrText, "dosage", result);
        extractFirst(FREQUENCY_PATTERN, ocrText, "frequency", result);
        extractFirst(DURATION_PATTERN, ocrText, "duration", result);

        // Fallback: if drug name still not found, try capitalized word + unit
        if (!result.containsKey("drugName")) {
            Matcher m = FALLBACK_DRUG_PATTERN.matcher(ocrText);
            if (m.find()) {
                result.put("drugName", m.group(1).trim());
                if (!result.containsKey("dosage")) {
                    result.put("dosage", m.group(2).trim());
                }
            }
        }

        return result;
    }

    /**
     * Applies a regex pattern to the input text and if a match is found,
     * stores the first capturing group value under the given key in the result map.
     *
     * @param pattern the compiled regex pattern
     * @param text    the text to search
     * @param key     the map key to store the result under
     * @param result  the result map to update
     */
    private static void extractFirst(Pattern pattern, String text,
                                     String key, Map<String, String> result) {
        Matcher m = pattern.matcher(text);
        if (m.find()) {
            String value = m.group(1).trim();
            if (!value.isBlank()) {
                result.put(key, value);
            }
        }
    }
}
