package com.medassist.model;

/**
 * Enum representing supported languages in the MedAssist system.
 *
 * <p>Each language stores an associated locale string and a greeting message
 * used by the VoiceEngine when interacting with patients in their preferred language.</p>
 *
 * @author MedAssist Team
 * @version 1.0
 */
public enum Language {

    /**
     * English language with a standard greeting.
     */
    ENGLISH("en-US", "Hello! I am MedAssist, your medication companion."),

    /**
     * Telugu language with a greeting in Telugu script.
     */
    TELUGU("te-IN", "నమస్కారం! నేను MedAssist, మీ మందుల సహాయకుడను."),

    /**
     * Hindi language with a greeting in Hindi script.
     */
    HINDI("hi-IN", "नमस्ते! मैं MedAssist हूँ, आपका दवा सहायक।");

    /** The BCP-47 locale string for this language. */
    private final String locale;

    /** The greeting message in this language. */
    private final String greeting;

    /**
     * Constructs a Language enum constant.
     *
     * @param locale   the BCP-47 locale string (e.g., "en-US")
     * @param greeting the greeting message in the target language
     */
    Language(String locale, String greeting) {
        this.locale = locale;
        this.greeting = greeting;
    }

    /**
     * Returns the BCP-47 locale string for this language.
     *
     * @return locale string
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Returns the greeting message in this language.
     *
     * @return greeting message
     */
    public String getGreeting() {
        return greeting;
    }
}
