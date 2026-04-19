package com.medassist.voice;

import com.medassist.model.Language;
import com.medassist.util.AppConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Central repository of multilingual UI strings for the MedAssist system.
 *
 * <p>{@code LanguagePack} provides static {@link Map} instances for each supported
 * language ({@link Language#ENGLISH}, {@link Language#TELUGU}, {@link Language#HINDI}).
 * Each map is keyed by a string constant representing a UI message type, and values
 * are the localised message strings.</p>
 *
 * <p>Supported keys:
 * <ul>
 *   <li>{@code "greeting"} — welcome message spoken at startup</li>
 *   <li>{@code "reminder"} — reminder message template</li>
 *   <li>{@code "taken_confirm"} — confirmation when a dose is marked taken</li>
 *   <li>{@code "missed_alert"} — alert when a dose is missed</li>
 *   <li>{@code "escalation_sent"} — notification that caregiver was alerted</li>
 * </ul>
 * </p>
 *
 * <p>This class is stateless and cannot be instantiated.</p>
 *
 * @author MedAssist Team
 * @version 1.0
 */
public final class LanguagePack {

    // -------------------------------------------------------------------------
    // Map Keys (language-neutral)
    // -------------------------------------------------------------------------

    /** Key for the greeting message. */
    public static final String KEY_GREETING = "greeting";

    /** Key for the medication reminder message. */
    public static final String KEY_REMINDER = "reminder";

    /** Key for the dose-taken confirmation message. */
    public static final String KEY_TAKEN_CONFIRM = "taken_confirm";

    /** Key for the missed dose alert message. */
    public static final String KEY_MISSED_ALERT = "missed_alert";

    /** Key for the escalation-sent notification message. */
    public static final String KEY_ESCALATION_SENT = "escalation_sent";

    // -------------------------------------------------------------------------
    // Language Maps
    // -------------------------------------------------------------------------

    /** Localised strings for English. */
    public static final Map<String, String> ENGLISH;

    /** Localised strings for Telugu. */
    public static final Map<String, String> TELUGU;

    /** Localised strings for Hindi. */
    public static final Map<String, String> HINDI;

    static {
        ENGLISH = new HashMap<>();
        ENGLISH.put(KEY_GREETING,
                "Hello! I am MedAssist, your medication companion. How can I help you today?");
        ENGLISH.put(KEY_REMINDER,
                "Reminder: It is time to take your medication. Please take it now.");
        ENGLISH.put(KEY_TAKEN_CONFIRM,
                "Great! Your medication has been marked as taken. Stay healthy!");
        ENGLISH.put(KEY_MISSED_ALERT,
                "Alert: You have missed your scheduled medication. Please take it as soon as possible.");
        ENGLISH.put(KEY_ESCALATION_SENT,
                "Your caregiver has been notified about your missed critical medication.");

        TELUGU = new HashMap<>();
        TELUGU.put(KEY_GREETING,
                "నమస్కారం! నేను MedAssist, మీ మందుల సహాయకుడను. ఈ రోజు నేను మీకు ఎలా సహాయం చేయగలను?");
        TELUGU.put(KEY_REMINDER,
                "రిమైండర్: మీ మందు తీసుకోవాల్సిన సమయం ఆయింది. దయచేసి ఇప్పుడే తీసుకోండి.");
        TELUGU.put(KEY_TAKEN_CONFIRM,
                "చాలా మంచిది! మీ మందు తీసుకున్నట్లు నమోదు చేయబడింది. ఆరోగ్యంగా ఉండండి!");
        TELUGU.put(KEY_MISSED_ALERT,
                "హెచ్చరిక: మీరు మీ నిర్ణీత మందు మిస్ చేసారు. దయచేసి వీలైనంత త్వరగా తీసుకోండి.");
        TELUGU.put(KEY_ESCALATION_SENT,
                "మీరు క్లిష్టమైన మందు మిస్ చేసినందుకు మీ సంరక్షకుడిని అప్రమత్తం చేయబడ్డారు.");

        HINDI = new HashMap<>();
        HINDI.put(KEY_GREETING,
                "नमस्ते! मैं MedAssist हूँ, आपका दवा सहायक। आज मैं आपकी कैसे मदद कर सकता हूँ?");
        HINDI.put(KEY_REMINDER,
                "याद दिलाना: आपकी दवा लेने का समय हो गया है। कृपया अभी दवा लें।");
        HINDI.put(KEY_TAKEN_CONFIRM,
                "बढ़िया! आपकी दवा ली गई के रूप में चिह्नित कर दी गई है। स्वस्थ रहें!");
        HINDI.put(KEY_MISSED_ALERT,
                "चेतावनी: आपने अपनी निर्धारित दवा छोड़ दी है। कृपया जल्द से जल्द लें।");
        HINDI.put(KEY_ESCALATION_SENT,
                "आपकी महत्वपूर्ण दवा छूटने के बारे में आपके देखभालकर्ता को सूचित किया गया है।");
    }

    /** Private constructor prevents instantiation. */
    private LanguagePack() {
        throw new UnsupportedOperationException("LanguagePack is a utility class.");
    }

    /**
     * Returns the localised string for the given key and language.
     *
     * <p>If the key is not found in the language map, the English fallback is returned.
     * If even English doesn't have it, {@code "[Missing key: " + key + "]"} is returned.</p>
     *
     * @param language the target language; must not be {@code null}
     * @param key      the message key (see constants in this class)
     * @return the localised string
     */
    public static String get(Language language, String key) {
        Map<String, String> map = switch (language) {
            case TELUGU -> TELUGU;
            case HINDI -> HINDI;
            default -> ENGLISH;
        };
        return map.getOrDefault(key, ENGLISH.getOrDefault(key, "[Missing key: " + key + "]"));
    }
}
