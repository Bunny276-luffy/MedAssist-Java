package com.medassist;

import com.medassist.ui.MainFrame;
import com.medassist.util.AppConstants;

import javax.swing.*;

/**
 * Application entry point for the MedAssist AI-driven Medication Adherence System.
 *
 * <p>This class launches the Swing UI on the Event Dispatch Thread (EDT) using
 * {@link SwingUtilities#invokeLater}, as mandated by Swing's threading model.
 * After the frame is visible, background services (reminders and escalation monitoring)
 * are started in separate daemon threads.</p>
 *
 * <p><b>Run sequence:</b>
 * <ol>
 *   <li>Configure the platform Look-and-Feel.</li>
 *   <li>Create and display {@link MainFrame} on the EDT.</li>
 *   <li>Start the {@code ReminderService} background thread.</li>
 *   <li>Start the {@code EscalationService} monitoring loop.</li>
 * </ol>
 * </p>
 *
 * @author MedAssist Team
 * @version 1.0
 * @see MainFrame
 */
public class MedAssistApp {

    /**
     * Application main method.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║  MedAssist — Medication Adherence    ║");
        System.out.println("║  Version: " + AppConstants.APP_VERSION + "                       ║");
        System.out.println("╚══════════════════════════════════════╝");

        // Configure platform-native look and feel for best accessibility on Windows/macOS
        configureLookAndFeel();

        // Launch the main window on the Swing Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);

                // Start background services after the UI is visible
                frame.startBackgroundServices();

                System.out.println("[MedAssistApp] Application started successfully.");
            } catch (Exception e) {
                System.err.println("[MedAssistApp] Fatal startup error: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to start MedAssist:\n" + e.getMessage(),
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }

    /**
     * Attempts to configure the system Look-and-Feel.
     *
     * <p>Falls back gracefully to the cross-platform Metal L&F if the native L&F
     * is unavailable or throws an exception. The Nimbus theme is applied when
     * neither native nor Metal is explicitly requested, as it provides better
     * visual quality on all platforms.</p>
     */
    private static void configureLookAndFeel() {
        try {
            // Try to use Nimbus for a modern look
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());

                    // Customize Nimbus colors for dark theme
                    UIManager.put("nimbusBase", new java.awt.Color(18, 30, 66));
                    UIManager.put("nimbusBlueGrey", new java.awt.Color(30, 30, 60));
                    UIManager.put("control", new java.awt.Color(25, 25, 45));
                    UIManager.put("text", java.awt.Color.WHITE);
                    UIManager.put("nimbusFocus", new java.awt.Color(60, 120, 200));
                    System.out.println("[MedAssistApp] Nimbus L&F applied.");
                    return;
                }
            }
            // Fallback to system L&F
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.out.println("[MedAssistApp] System L&F applied.");
        } catch (Exception e) {
            System.err.println("[MedAssistApp] Could not set L&F: " + e.getMessage());
        }
    }
}
