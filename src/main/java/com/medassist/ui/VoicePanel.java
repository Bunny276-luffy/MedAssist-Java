package com.medassist.ui;

import com.medassist.model.Language;
import com.medassist.model.Medication;
import com.medassist.model.MedicationStatus;
import com.medassist.model.Patient;
import com.medassist.util.AppConstants;
import com.medassist.util.FileStorageUtil;
import com.medassist.voice.LanguagePack;
import com.medassist.voice.VoiceEngine;
import com.medassist.voice.WindowsSpeechRecognizer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;

/**
 * Premium voice panel with a live text-input command field, natural-language
 * parsing, and real action execution (mark taken, add medication, schedule update).
 *
 * <p>Supports commands like:</p>
 * <ul>
 *   <li>{@code "add Aspirin 100mg at 9am once daily"}</li>
 *   <li>{@code "mark Metformin as taken"}</li>
 *   <li>{@code "I missed my Amlodipine dose"}</li>
 *   <li>{@code "schedule Atorvastatin 20mg at 9pm"}</li>
 * </ul>
 *
 * @author MedAssist Team
 * @version 2.0
 */
public class VoicePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // ── Palette ────────────────────────────────────────────────────────────────
    private static final Color BG          = new Color(13,  17,  23);
    private static final Color CARD_BG     = new Color(22,  27,  34);
    private static final Color CARD_BORDER = new Color(48,  54,  61);
    private static final Color INPUT_BG    = new Color(15,  20,  32);
    private static final Color TEXT_PRI    = new Color(230, 237, 243);
    private static final Color TEXT_MUT    = new Color(139, 148, 158);
    private static final Color ACCENT      = new Color(88,  166, 255);
    private static final Color PURPLE      = new Color(139, 92,  246);
    private static final Color GREEN       = new Color(63,  185, 80);
    private static final Color TEAL        = new Color(20,  184, 166);
    private static final Color RED         = new Color(248, 81,  73);
    private static final Color AMBER       = new Color(210, 153, 34);

    private static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,  22);
    private static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD,  14);
    private static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_MONO    = new Font("Consolas", Font.PLAIN, 13);
    private static final Font FONT_INPUT   = new Font("Segoe UI", Font.PLAIN, 15);

    private final Patient patient;
    private final VoiceEngine voiceEngine;

    private Language selectedLanguage;
    private JButton[] langButtons;
    private static final Language[] LANGS       = {Language.ENGLISH, Language.TELUGU, Language.HINDI};
    private static final String[]   LANG_LABELS = {"🇺🇸 English", "🇮🇳 Telugu", "🇮🇳 Hindi"};

    // Input + output widgets
    private JTextField commandInput;
    private JButton    processBtn;
    private JTextArea  outputArea;
    private JLabel     statusLabel;
    private boolean    processing  = false;
    private boolean    recording   = false;
    private JButton    recordBtn;
    private Timer      recordTimer;
    private int        recordSeconds = 0;
    private final WindowsSpeechRecognizer speechRecognizer = new WindowsSpeechRecognizer();

    public VoicePanel(Patient patient, VoiceEngine voiceEngine) {
        this.patient = patient;
        this.voiceEngine = voiceEngine;
        this.selectedLanguage = patient.getLanguage();
        setLayout(new BorderLayout(0, 12));
        setBackground(BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        add(buildHeader(),       BorderLayout.NORTH);
        add(buildCenter(),       BorderLayout.CENTER);
        add(buildStatusBar(),    BorderLayout.SOUTH);
    }

    // ── Layout ─────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel title = new JLabel("🎙️  Voice Assistant");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_PRI);

        JLabel sub = new JLabel("Type or speak commands to manage your medications in natural language");
        sub.setFont(FONT_SMALL);
        sub.setForeground(TEXT_MUT);

        p.add(title, BorderLayout.NORTH);
        p.add(sub,   BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(buildLangBar(),      BorderLayout.NORTH);
        top.add(buildCommandInput(), BorderLayout.CENTER);
        top.add(buildExampleChips(), BorderLayout.SOUTH);

        center.add(top,             BorderLayout.NORTH);
        center.add(buildOutputArea(), BorderLayout.CENTER);
        return center;
    }

    private JPanel buildLangBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);

        JLabel lbl = new JLabel("Language:");
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_MUT);
        bar.add(lbl);

        langButtons = new JButton[LANGS.length];
        for (int i = 0; i < LANGS.length; i++) {
            final int idx = i;
            langButtons[i] = langChip(LANG_LABELS[i], LANGS[i] == selectedLanguage);
            langButtons[i].addActionListener(e -> selectLanguage(idx));
            bar.add(langButtons[i]);
        }

        // Greet + Clear on the right
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(pillBtn("👋 Greet", TEAL,            e -> onGreet()));
        right.add(pillBtn("🗑️ Clear", new Color(55,65,81), e -> outputArea.setText("")));

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(bar, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JPanel buildCommandInput() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Border color changes to red while recording
                Color border = recording
                        ? new Color(RED.getRed(), RED.getGreen(), RED.getBlue(), 180)
                        : new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 80);
                g2.setColor(INPUT_BG);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),12,12));
                g2.setColor(border);
                g2.setStroke(new BasicStroke(recording ? 2f : 1.5f));
                g2.draw(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,12,12));
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(4, 4, 4, 4));
        wrapper.setPreferredSize(new Dimension(0, 56));

        // Text field
        commandInput = new JTextField();
        commandInput.setName("cmdInput");
        commandInput.setBackground(new Color(0,0,0,0));
        commandInput.setOpaque(false);
        commandInput.setForeground(TEXT_PRI);
        commandInput.setCaretColor(ACCENT);
        commandInput.setFont(FONT_INPUT);
        commandInput.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 8));

        // Placeholder
        commandInput.setText("Type a command or press 🎙️ to speak");
        commandInput.setForeground(TEXT_MUT);
        commandInput.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (commandInput.getForeground().equals(TEXT_MUT)) {
                    commandInput.setText("");
                    commandInput.setForeground(TEXT_PRI);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (commandInput.getText().isBlank()) {
                    commandInput.setForeground(TEXT_MUT);
                    commandInput.setText("Type a command or press 🎙️ to speak");
                }
            }
        });
        commandInput.addActionListener(e -> processCommand());

        // Right button panel: [🎙️ Record] [Process ▶]
        JPanel rightBtns = new JPanel(new BorderLayout(4, 0));
        rightBtns.setOpaque(false);
        rightBtns.setBorder(new EmptyBorder(0, 0, 0, 4));
        recordBtn  = buildRecordButton();
        processBtn = buildProcessButton();
        rightBtns.add(recordBtn,  BorderLayout.WEST);
        rightBtns.add(processBtn, BorderLayout.EAST);

        wrapper.add(commandInput, BorderLayout.CENTER);
        wrapper.add(rightBtns,    BorderLayout.EAST);
        return wrapper;
    }

    private JButton buildRecordButton() {
        JButton btn = new JButton("🎙️") {
            private boolean hov = false;
            { addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){hov=true; repaint();}
                public void mouseExited (MouseEvent e){hov=false;repaint();}
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = recording ? RED : new Color(55, 65, 81);
                Color draw = hov ? base.brighter() : base;
                g2.setColor(draw);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),10,10));
                // Pulsing glow when recording
                if (recording) {
                    g2.setColor(new Color(RED.getRed(),RED.getGreen(),RED.getBlue(), 60));
                    g2.fill(new RoundRectangle2D.Float(-3,-3,getWidth()+6,getHeight()+6,14,14));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setName("btnRecord");
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(52, 48));
        btn.setToolTipText("Click to record voice command (Windows microphone)");
        btn.addActionListener(e -> toggleRecording());
        return btn;
    }

    private JButton buildProcessButton() {
        JButton btn = new JButton("  Process  ▶") {
            private boolean hov = false;
            { addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){hov=true; repaint();}
                public void mouseExited (MouseEvent e){hov=false;repaint();}
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = processing ? new Color(220,38,38) : (hov ? PURPLE.brighter() : PURPLE);
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),10,10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setName("btnProcess");
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 48));
        btn.setBorder(new EmptyBorder(0,4,0,8));
        btn.addActionListener(e -> processCommand());
        return btn;
    }

    private JPanel buildExampleChips() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        p.setOpaque(false);

        JLabel hint = new JLabel("Try: ");
        hint.setFont(FONT_SMALL);
        hint.setForeground(TEXT_MUT);
        p.add(hint);

        String[] examples = {
            "add Aspirin 100mg at 9am",
            "mark Metformin as taken",
            "I missed my Amlodipine dose",
            "schedule Atorvastatin at 9pm",
            "scan prescription"
        };
        for (String ex : examples) {
            p.add(exampleChip(ex));
        }
        return p;
    }

    private JButton exampleChip(String text) {
        JButton btn = new JButton(text) {
            private boolean hov = false;
            { addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){hov=true;repaint();}
                public void mouseExited(MouseEvent e){hov=false;repaint();}
            });}
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? new Color(30,40,68) : new Color(22,30,50));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),20,20));
                g2.setColor(new Color(ACCENT.getRed(),ACCENT.getGreen(),ACCENT.getBlue(), hov?100:50));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,20,20));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_SMALL);
        btn.setForeground(new Color(120, 180, 255));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(4, 12, 4, 12));
        btn.addActionListener(e -> {
            commandInput.setForeground(TEXT_PRI);
            commandInput.setText(text);
            commandInput.requestFocus();
        });
        return btn;
    }

    private JScrollPane buildOutputArea() {
        outputArea = new JTextArea();
        outputArea.setName("txtVoiceOutput");
        outputArea.setEditable(false);
        outputArea.setBackground(INPUT_BG);
        outputArea.setForeground(new Color(180, 230, 180));
        outputArea.setFont(FONT_MONO);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setMargin(new Insets(14, 16, 14, 16));
        outputArea.setText("══ MedAssist Voice Log ══════════════════════════════\n\n"
                + "Command syntax guide:\n"
                + "  add <drug> <dosage> at <time>        – Add a new medication\n"
                + "  schedule <drug> at <time>            – Update scheduled time\n"
                + "  mark <drug> as taken                 – Mark dose as taken\n"
                + "  I missed my <drug> dose              – Log a missed dose\n"
                + "  scan prescription                    – Go to Scan Rx tab\n\n"
                + "Click an example chip above or type your own command ↑");

        JScrollPane scroll = new JScrollPane(outputArea);
        scroll.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
        scroll.getViewport().setBackground(INPUT_BG);
        scroll.setBackground(INPUT_BG);
        return scroll;
    }

    private JPanel buildStatusBar() {
        statusLabel = new JLabel("  Ready — type a command above and press Process or Enter");
        statusLabel.setFont(FONT_SMALL);
        statusLabel.setForeground(TEXT_MUT);
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(statusLabel, BorderLayout.WEST);
        return p;
    }

    // ── Command Processing ──────────────────────────────────────────────────────

    /**
     * Toggles microphone recording on/off using Windows Speech Recognition.
     * On recognition result, fills the command input field with the transcription.
     */
    private void toggleRecording() {
        if (recording) {
            // Stop early
            recording = false;
            speechRecognizer.stopListening();
            if (recordTimer != null) recordTimer.stop();
            recordBtn.setText("🎙️");
            recordBtn.setToolTipText("Click to record voice command");
            recordBtn.repaint();
            if (commandInputWrapper() != null) commandInputWrapper().repaint();
            setStatus("Recording cancelled.", TEXT_MUT);
            return;
        }

        recording = true;
        recordSeconds = 0;
        recordBtn.setText("⏹");
        recordBtn.setToolTipText("Click to stop recording");
        recordBtn.repaint();
        commandInput.setForeground(RED);
        commandInput.setText("🔴  Listening… speak now");
        commandInput.setEnabled(false);
        processBtn.setEnabled(false);
        setStatus("🔴  Recording — speak your command clearly (up to 10 seconds)", RED);

        // Update timer label every second
        recordTimer = new Timer(1000, e -> {
            recordSeconds++;
            if (recording) {
                commandInput.setText("🔴  Listening… " + recordSeconds + "s");
                setStatus("🔴  Recording — " + recordSeconds + "s elapsed  (press ⏹ to stop early)", RED);
            }
        });
        recordTimer.start();

        speechRecognizer.startListening(
            // onResult — show Review dialog before processing
            transcribed -> SwingUtilities.invokeLater(() -> {
                recording = false;
                if (recordTimer != null) recordTimer.stop();
                recordBtn.setText("🎙️");
                recordBtn.setToolTipText("Click to record voice command");
                recordBtn.repaint();
                commandInput.setEnabled(true);
                processBtn.setEnabled(true);
                if (commandInputWrapper() != null) commandInputWrapper().repaint();

                if (transcribed.isBlank()) {
                    commandInput.setForeground(TEXT_MUT);
                    commandInput.setText("Type a command or press 🎙️ to speak");
                    setStatus("⚠  No speech detected — please try again or type manually.", AMBER);
                    return;
                }

                // Show Review & Confirm dialog
                showReviewDialog(transcribed);
            }),
            // onError
            error -> SwingUtilities.invokeLater(() -> {
                recording = false;
                if (recordTimer != null) recordTimer.stop();
                recordBtn.setText("🎙️"); recordBtn.repaint();
                commandInput.setEnabled(true);
                processBtn.setEnabled(true);
                commandInput.setForeground(TEXT_MUT);
                commandInput.setText("Type a command or press 🎙️ to speak");
                setStatus("❌  " + error, RED);
                if (commandInputWrapper() != null) commandInputWrapper().repaint();
            })
        );
    }

    /**
     * Shows a modal dialog displaying what was recognised.
     * The first line is the best guess; subsequent lines are alternates.
     * The user can edit the text before pressing Process or dismiss with Cancel.
     *
     * @param rawTranscription newline-separated recognition results (best guess first)
     */
    private void showReviewDialog(String rawTranscription) {
        String[] lines   = rawTranscription.split("\n", -1);
        String   bestGuess = lines[0].trim();

        // Build dialog panel
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(new Color(22, 27, 34));
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));
        panel.setPreferredSize(new Dimension(520, lines.length > 1 ? 220 : 160));

        // Header
        JLabel heard = new JLabel("🎙️  I heard:");
        heard.setFont(FONT_BOLD);
        heard.setForeground(TEXT_MUT);
        panel.add(heard, BorderLayout.NORTH);

        // Editable text field with the best guess
        JTextField editField = new JTextField(bestGuess);
        editField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        editField.setBackground(new Color(15, 20, 32));
        editField.setForeground(new Color(230, 237, 243));
        editField.setCaretColor(ACCENT);
        editField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                new EmptyBorder(6, 10, 6, 10)));
        editField.selectAll();

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(editField, BorderLayout.NORTH);

        // Show alternate results if available
        if (lines.length > 1) {
            JPanel alts = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            alts.setOpaque(false);
            JLabel altLbl = new JLabel("Other possibilities:");
            altLbl.setFont(FONT_SMALL);
            altLbl.setForeground(TEXT_MUT);
            alts.add(altLbl);
            for (int i = 1; i < Math.min(lines.length, 4); i++) {
                String alt = lines[i].trim();
                if (!alt.isBlank()) {
                    JButton chip = altChipBtn(alt, editField);
                    alts.add(chip);
                }
            }
            center.add(alts, BorderLayout.CENTER);
        }

        JLabel hint = new JLabel("✏️  Correct any mistakes, then press Process.");
        hint.setFont(FONT_SMALL);
        hint.setForeground(new Color(139, 148, 158));
        center.add(hint, BorderLayout.SOUTH);

        panel.add(center, BorderLayout.CENTER);

        // Show dialog
        int result = JOptionPane.showConfirmDialog(
                this, panel,
                "Review Voice Input",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String corrected = editField.getText().trim();
            if (!corrected.isBlank()) {
                commandInput.setForeground(TEXT_PRI);
                commandInput.setText(corrected);
                setStatus("✅  Transcribed: \"" + corrected + "\"  — processing…", GREEN);
                appendLog("[VOICE]", "TRANSCRIBED",
                        "Recognised & confirmed: \"" + corrected + "\"", "{}");
                // Auto-process after confirm
                processCommand();
            }
        } else {
            // User cancelled — put text in field for manual editing
            commandInput.setForeground(TEXT_PRI);
            commandInput.setText(bestGuess);
            commandInput.selectAll();
            commandInput.requestFocus();
            setStatus("✏️  Edit the transcription and press Process or Enter.", AMBER);
        }
    }

    /** Builds a small clickable alternative chip button that fills the edit field when clicked. */
    private JButton altChipBtn(String text, JTextField target) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_SMALL);
        btn.setForeground(new Color(120, 180, 255));
        btn.setBackground(new Color(22, 30, 50));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(48, 54, 61)),
                new EmptyBorder(3, 10, 3, 10)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            target.setText(text);
            target.selectAll();
            target.requestFocus();
        });
        return btn;
    }

    /** Returns the parent wrapper panel of commandInput for repaint (border color change). */
    private JPanel commandInputWrapper() {
        if (commandInput.getParent() instanceof JPanel p) return p;
        return null;
    }

    /**
     * Reads the command input field, injects it into the VoiceEngine, and handles
     * the resulting command by executing the appropriate medication action on the patient.
     */
    private void processCommand() {
        if (processing) return;

        String rawInput = commandInput.getText().trim();
        if (rawInput.isBlank() || rawInput.startsWith("Type a command") || rawInput.startsWith("🔴")) {
            setStatus("⚠  Please type or record a command first.", AMBER);
            return;
        }

        processing = true;
        processBtn.setText("  ⏳  ...");
        processBtn.repaint();
        commandInput.setEnabled(false);

        voiceEngine.setInput(rawInput);

        SwingWorker<String[], Void> worker = new SwingWorker<>() {
            @Override
            protected String[] doInBackground() {
                String heard = voiceEngine.listen();
                String cmd   = voiceEngine.parseCommand(heard);
                Map<String, String> details = voiceEngine.extractMedicationDetails(heard);
                String result = executeCommand(cmd, details, heard);
                voiceEngine.speak(result, selectedLanguage);
                return new String[]{heard, cmd, result, details.toString()};
            }

            @Override
            protected void done() {
                processing = false;
                processBtn.setText("  Process  ▶");
                processBtn.repaint();
                commandInput.setEnabled(true);
                commandInput.setForeground(TEXT_MUT);
                commandInput.setText("Type a command or press 🎙️ to speak");
                try {
                    String[] r = get();
                    appendLog(r[0], r[1], r[2], r[3]);
                } catch (Exception ex) {
                    setStatus("❌  Error: " + ex.getMessage(), RED);
                }
            }
        };
        worker.execute();
    }

    /**
     * Dispatches the parsed command to the correct action handler.
     *
     * @param cmd     the command type constant (CMD_ADD, CMD_TAKEN, etc.)
     * @param details extracted medication details map
     * @param raw     the original raw input
     * @return a human-readable result message
     */
    private String executeCommand(String cmd, Map<String, String> details, String raw) {
        return switch (cmd) {
            case AppConstants.CMD_ADD      -> handleAdd(details);
            case AppConstants.CMD_SCHEDULE -> handleSchedule(details);
            case AppConstants.CMD_TAKEN    -> handleTaken(details);
            case AppConstants.CMD_MISSED   -> handleMissed(details);
            case AppConstants.CMD_SCAN     -> "Please navigate to the '🔬 Scan Rx' tab to scan your prescription.";
            default                        -> "I couldn't understand that command. Try one of the examples above.";
        };
    }

    /**
     * Handles CMD_ADD: extracts details and schedules a new medication.
     */
    private String handleAdd(Map<String, String> details) {
        String drug  = details.getOrDefault("drugName",  "");
        String dose  = details.getOrDefault("dosage",    "1 tablet");
        String freq  = details.getOrDefault("frequency", "once daily");
        String timeS = details.getOrDefault("time",      "");

        if (drug.isBlank()) {
            return "❌  Could not detect a drug name. Try: \"add Aspirin 100mg at 9am\"";
        }

        LocalTime scheduledTime;
        try {
            scheduledTime = timeS.isBlank()
                    ? LocalTime.now().plusHours(1).withSecond(0).withNano(0)
                    : LocalTime.parse(timeS);
        } catch (DateTimeParseException e) {
            scheduledTime = LocalTime.now().plusHours(1).withSecond(0).withNano(0);
        }

        Medication med = new Medication(capitalize(drug), dose, freq, scheduledTime, false);
        patient.addMedication(med);
        try { FileStorageUtil.savePatient(patient); } catch (IOException ignored) {}

        return "✅  Added " + capitalize(drug) + " (" + dose + ") scheduled at "
                + scheduledTime + " — " + freq + ".";
    }

    /**
     * Handles CMD_SCHEDULE: finds a matching medication and updates its time.
     */
    private String handleSchedule(Map<String, String> details) {
        String drug  = details.getOrDefault("drugName", "");
        String timeS = details.getOrDefault("time", "");

        if (drug.isBlank()) {
            return "❌  Could not detect a drug name. Try: \"schedule Aspirin at 10am\"";
        }
        if (timeS.isBlank()) {
            return "❌  Could not detect a time. Try: \"schedule Aspirin at 10am\"";
        }

        Optional<Medication> match = findMedication(drug);
        if (match.isEmpty()) {
            return "❌  Medication \"" + drug + "\" not found in your list. "
                    + "Use \"add\" to add it first.";
        }

        try {
            LocalTime newTime = LocalTime.parse(timeS);
            match.get().setScheduledTime(newTime);
            FileStorageUtil.savePatient(patient);
            return "✅  Rescheduled " + match.get().getDrugName() + " to " + newTime + ".";
        } catch (Exception e) {
            return "❌  Invalid time format. Use: \"at 9am\" or \"at 21:00\"";
        }
    }

    /**
     * Handles CMD_TAKEN: marks a specific medication as taken, or the first PENDING one.
     */
    private String handleTaken(Map<String, String> details) {
        String drug = details.getOrDefault("drugName", "");

        Medication target;
        if (!drug.isBlank()) {
            Optional<Medication> match = findMedication(drug);
            if (match.isEmpty()) {
                return "❌  Medication \"" + capitalize(drug) + "\" not found in your list.";
            }
            target = match.get();
        } else {
            // Mark the first PENDING medication
            target = patient.getMedications().stream()
                    .filter(m -> m.getStatus() == MedicationStatus.PENDING)
                    .findFirst().orElse(null);
            if (target == null) {
                return "ℹ  No pending medications found to mark as taken.";
            }
        }

        if (target.getStatus() == MedicationStatus.TAKEN) {
            return "ℹ  " + target.getDrugName() + " is already marked as taken.";
        }

        target.setStatus(MedicationStatus.TAKEN);
        try { FileStorageUtil.savePatient(patient); } catch (IOException ignored) {}

        return LanguagePack.get(selectedLanguage, LanguagePack.KEY_TAKEN_CONFIRM)
                + "\n✅  Marked: " + target.getDrugName();
    }

    /**
     * Handles CMD_MISSED: marks a specific medication as MISSED.
     */
    private String handleMissed(Map<String, String> details) {
        String drug = details.getOrDefault("drugName", "");

        if (!drug.isBlank()) {
            Optional<Medication> match = findMedication(drug);
            if (match.isPresent()) {
                match.get().setStatus(MedicationStatus.MISSED);
                try { FileStorageUtil.savePatient(patient); } catch (IOException ignored) {}
                return LanguagePack.get(selectedLanguage, LanguagePack.KEY_MISSED_ALERT)
                        + "\n⚠  Logged missed dose: " + match.get().getDrugName();
            }
        }
        return LanguagePack.get(selectedLanguage, LanguagePack.KEY_MISSED_ALERT)
                + "\n⚠  Please go to the Medications tab to update the status manually.";
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    /**
     * Finds a medication in the patient's list by fuzzy name match (case-insensitive,
     * allows partial match if no exact match is found).
     *
     * @param drug partial or full drug name
     * @return matching Optional Medication
     */
    private Optional<Medication> findMedication(String drug) {
        String lower = drug.toLowerCase().trim();
        // Exact match
        Optional<Medication> exact = patient.getMedications().stream()
                .filter(m -> m.getDrugName().toLowerCase().contains(lower))
                .findFirst();
        return exact;
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private void appendLog(String heard, String cmd, String result, String details) {
        outputArea.append("\n───────────────────────────────────────────────────\n");
        outputArea.append("📥  Input:   \"" + heard + "\"\n");
        outputArea.append("🤖  Command: " + cmd + "\n");
        outputArea.append("📋  Parsed:  " + details + "\n");
        outputArea.append("🔊  Result:  " + result + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
        setStatus("✅  " + cmd + " executed", GREEN);
    }

    private void setStatus(String msg, Color color) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("  " + msg);
            statusLabel.setForeground(color);
        });
    }

    private void onGreet() {
        String g = LanguagePack.get(selectedLanguage, LanguagePack.KEY_GREETING);
        voiceEngine.speak(g, selectedLanguage);
        outputArea.append("\n───────────────────────────────────────────────────\n");
        outputArea.append("👋  Greeting [" + selectedLanguage.name() + "]:\n" + g + "\n");
        setStatus("Greeting spoken in " + selectedLanguage.name(), TEAL);
    }

    private void selectLanguage(int idx) {
        selectedLanguage = LANGS[idx];
        for (int i = 0; i < langButtons.length; i++) {
            boolean active = (i == idx);
            langButtons[i].putClientProperty("active", active);
            langButtons[i].setForeground(active ? ACCENT : TEXT_MUT);
            langButtons[i].repaint();
        }
    }

    // ── Widget builders ─────────────────────────────────────────────────────────

    private JButton langChip(String text, boolean active) {
        JButton btn = new JButton(text) {
            private boolean hov = false;
            { addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){hov=true;repaint();}
                public void mouseExited(MouseEvent e){hov=false;repaint();}
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                boolean a=(getClientProperty("active")==Boolean.TRUE);
                if(a){
                    g2.setColor(new Color(ACCENT.getRed(),ACCENT.getGreen(),ACCENT.getBlue(),28));
                    g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),20,20));
                    g2.setColor(new Color(ACCENT.getRed(),ACCENT.getGreen(),ACCENT.getBlue(),180));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,20,20));
                } else {
                    g2.setColor(hov?new Color(30,38,52):CARD_BG);
                    g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),20,20));
                    g2.setColor(CARD_BORDER);
                    g2.draw(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,20,20));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.putClientProperty("active", active);
        btn.setFont(FONT_SMALL);
        btn.setForeground(active ? ACCENT : TEXT_MUT);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(105, 32));
        return btn;
    }

    private JButton pillBtn(String text, Color bg, ActionListener al) {
        JButton btn = new JButton(text) {
            private boolean hov = false;
            { addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){hov=true;repaint();}
                public void mouseExited(MouseEvent e){hov=false;repaint();}
            });}
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov?bg.brighter():bg);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),10,10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(118, 36));
        btn.addActionListener(al);
        return btn;
    }
}
