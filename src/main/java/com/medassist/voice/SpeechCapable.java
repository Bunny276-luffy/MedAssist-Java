package com.medassist.voice;

import com.medassist.model.Language;

/**
 * Contract for voice-interaction-capable components in the MedAssist system.
 *
 * <p>Components that implement {@code SpeechCapable} can synthesize speech output
 * for the patient and parse natural-language voice commands. The interface is designed
 * to be language-aware, enabling multilingual support for English, Telugu, and Hindi.</p>
 *
 * <p>The {@link #listen()} method simulates microphone input for demonstration purposes;
 * a production implementation would integrate with a platform speech recognition SDK.</p>
 *
 * @author MedAssist Team
 * @version 1.0
 * @see VoiceEngine
 */
public interface SpeechCapable {

    /**
     * Synthesizes and outputs the given text in the specified language.
     *
     * <p>In the current simulation, this method prints the text to the console and
     * to the {@code VoicePanel} text area. A production build would use a TTS engine.</p>
     *
     * @param text     the message to speak; must not be {@code null}
     * @param language the target language for speech synthesis; must not be {@code null}
     */
    void speak(String text, Language language);

    /**
     * Activates the microphone and listens for patient voice input.
     *
     * <p>Returns a simulated or real voice input string. In the current demo mode,
     * this returns a predetermined test command. A production build would use
     * a speech-to-text API.</p>
     *
     * @return the recognized speech as a raw text string; never {@code null}
     */
    String listen();

    /**
     * Parses a raw voice command string and returns a structured command type keyword.
     *
     * <p>Recognized keywords and their return values:</p>
     * <ul>
     *   <li>{@code "taken"} → {@code "CMD_TAKEN"}</li>
     *   <li>{@code "missed"} → {@code "CMD_MISSED"}</li>
     *   <li>{@code "add"} → {@code "CMD_ADD"}</li>
     *   <li>{@code "schedule"} → {@code "CMD_SCHEDULE"}</li>
     *   <li>{@code "scan"} → {@code "CMD_SCAN"}</li>
     *   <li>(no match) → {@code "CMD_UNKNOWN"}</li>
     * </ul>
     *
     * @param input the raw voice input string; must not be {@code null}
     * @return a command type string constant
     */
    String parseCommand(String input);
}
