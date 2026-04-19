package com.medassist.ui;

import com.medassist.model.Caregiver;
import com.medassist.model.Language;
import com.medassist.model.Medication;
import com.medassist.model.Patient;
import com.medassist.service.EscalationService;
import com.medassist.service.OCRService;
import com.medassist.service.ReminderService;
import com.medassist.util.AppConstants;
import com.medassist.util.DateTimeUtil;
import com.medassist.util.FileStorageUtil;
import com.medassist.voice.VoiceEngine;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.time.LocalTime;

/**
 * Premium main application window with gradient header, custom tab bar, and polished layout.
 *
 * @author MedAssist Team
 * @version 2.0
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    // Palette
    private static final Color BG           = new Color(13, 17, 23);
    private static final Color HEADER_TOP   = new Color(14, 30, 70);
    private static final Color HEADER_BOT   = new Color(9, 18, 45);
    private static final Color ACCENT_BLUE  = new Color(88, 166, 255);
    private static final Color CARD_BG      = new Color(22, 27, 34);
    private static final Color CARD_BORDER  = new Color(48, 54, 61);
    private static final Color TEXT_PRIMARY = new Color(230, 237, 243);
    private static final Color TEXT_MUTED   = new Color(139, 148, 158);
    private static final Color TAB_ACTIVE   = new Color(31, 48, 78);
    private static final Color TAB_HOVER    = new Color(26, 34, 52);
    private static final Color STATUS_BAR   = new Color(10, 13, 20);

    private Patient patient;
    private VoiceEngine voiceEngine;
    private ReminderService reminderService;
    private EscalationService escalationService;
    private OCRService ocrService;

    private MedicationPanel medicationPanel;
    private ScanPanel       scanPanel;
    private VoicePanel      voicePanel;
    private CaregiverPanel  caregiverPanel;

    // Custom tab bar
    private JPanel tabBar;
    private JPanel contentArea;
    private CardLayout cardLayout;
    private JButton[] tabButtons;
    private static final String[] TAB_NAMES  = {"💊  Medications", "🔬  Scan Rx", "🎙️  Voice", "👩‍⚕️  Caregiver"};
    private static final String[] TAB_KEYS   = {"MED", "SCAN", "VOICE", "CG"};
    private int activeTab = 0;

    public MainFrame() {
        super(AppConstants.APP_NAME);
        loadOrCreatePatient();
        initServices();
        buildUI();
        configureFrame();
    }

    // ── Patient & Services ──────────────────────────────────────────────────────

    private void loadOrCreatePatient() {
        try { patient = FileStorageUtil.loadPatient(); }
        catch (Exception e) { patient = null; }
        if (patient == null) {
            patient = createDemoPatient();
            try { FileStorageUtil.savePatient(patient); } catch (IOException ignored) {}
        }
    }

    private Patient createDemoPatient() {
        Caregiver cg = new Caregiver("Priya Sharma", "priya.sharma@example.com", "+91-9876543210", "Daughter");
        Patient p = new Patient("P-001", "Ramu Rao", Language.ENGLISH, cg);
        p.addMedication(new Medication("Metformin 500mg",    "1 tablet", "twice daily",             LocalTime.of(8,  0), false));
        p.addMedication(new Medication("Amlodipine 5mg",     "1 tablet", "once daily",              LocalTime.of(9,  0), true));
        p.addMedication(new Medication("Atorvastatin 20mg",  "1 tablet", "once daily at bedtime",   LocalTime.of(21, 0), false));
        p.addMedication(new Medication("Aspirin 81mg",       "1 tablet", "once daily",              LocalTime.of(10, 0), true));
        return p;
    }

    private void initServices() {
        voiceEngine       = new VoiceEngine();
        reminderService   = new ReminderService(patient, voiceEngine);
        escalationService = new EscalationService(patient);
        ocrService        = new OCRService();
        reminderService.addNotifiable(escalationService);
    }

    // ── UI Build ────────────────────────────────────────────────────────────────

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(BG);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        root.setOpaque(true);

        root.add(buildHeader(),    BorderLayout.NORTH);
        root.add(buildCenter(),    BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient background
                GradientPaint gp = new GradientPaint(0, 0, HEADER_TOP, 0, getHeight(), HEADER_BOT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Subtle bottom highlight line
                g2.setColor(new Color(88, 166, 255, 60));
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
                g2.dispose();
            }
        };
        hdr.setPreferredSize(new Dimension(0, 68));
        hdr.setBorder(new EmptyBorder(0, 22, 0, 22));

        // Left: icon + name
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel icon = new JLabel("💊");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JLabel appName = new JLabel("MedAssist");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 24));
        appName.setForeground(Color.WHITE);

        JLabel tagLine = new JLabel("AI Medication Adherence");
        tagLine.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tagLine.setForeground(new Color(ACCENT_BLUE.getRed(), ACCENT_BLUE.getGreen(), ACCENT_BLUE.getBlue(), 200));

        JPanel nameStack = new JPanel(new BorderLayout(0, 0));
        nameStack.setOpaque(false);
        nameStack.add(appName, BorderLayout.CENTER);
        nameStack.add(tagLine, BorderLayout.SOUTH);

        left.add(icon);
        left.add(nameStack);

        // Right: patient info chip + About button
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(buildAboutButton());
        right.add(buildPatientChip());

        hdr.add(left,  BorderLayout.WEST);
        hdr.add(right, BorderLayout.EAST);

        // Wrap with vertical centering
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(12, 0, 12, 0));
        wrapper.add(hdr);
        return wrapper;
    }

    private JPanel buildPatientChip() {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,255,255,12));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,20,20));
                g2.setColor(new Color(88,166,255,60));
                g2.draw(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,20,20));
                g2.dispose();
            }
        };
        chip.setOpaque(false);
        chip.setBorder(new EmptyBorder(6, 14, 6, 14));

        JLabel avatar = new JLabel("👤");
        avatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));

        JPanel info = new JPanel(new BorderLayout(0, 0));
        info.setOpaque(false);

        JLabel name = new JLabel(patient.getName());
        name.setFont(new Font("Segoe UI", Font.BOLD, 13));
        name.setForeground(Color.WHITE);

        JLabel date = new JLabel(DateTimeUtil.todayAsDisplayString());
        date.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        date.setForeground(TEXT_MUTED);

        info.add(name, BorderLayout.NORTH);
        info.add(date, BorderLayout.SOUTH);

        chip.add(avatar);
        chip.add(info);
        return chip;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(0, 0, 0, 0));

        center.add(buildTabBar(),     BorderLayout.NORTH);
        center.add(buildContentArea(), BorderLayout.CENTER);
        return center;
    }

    private JPanel buildTabBar() {
        tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(22, 27, 34));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(CARD_BORDER);
                g.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
            }
        };
        tabBar.setPreferredSize(new Dimension(0, 46));
        tabBar.setOpaque(false);

        tabButtons = new JButton[TAB_NAMES.length];
        for (int i = 0; i < TAB_NAMES.length; i++) {
            final int idx = i;
            tabButtons[i] = buildTabButton(TAB_NAMES[i], i == 0);
            tabButtons[i].addActionListener(e -> switchTab(idx));
            tabBar.add(tabButtons[i]);
        }
        return tabBar;
    }

    private JButton buildTabButton(String text, boolean active) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                boolean isActive = (getClientProperty("active") == Boolean.TRUE);
                Color bg = isActive ? TAB_ACTIVE : (hovered ? TAB_HOVER : new Color(22,27,34));
                g2.setColor(bg);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Active indicator bar at bottom
                if (isActive) {
                    g2.setColor(ACCENT_BLUE);
                    g2.fillRect(0, getHeight()-3, getWidth(), 3);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.putClientProperty("active", active);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(active ? Color.WHITE : TEXT_MUTED);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(155, 46));
        return btn;
    }

    private void switchTab(int idx) {
        activeTab = idx;
        for (int i = 0; i < tabButtons.length; i++) {
            boolean active = (i == idx);
            tabButtons[i].putClientProperty("active", active);
            tabButtons[i].setForeground(active ? Color.WHITE : TEXT_MUTED);
            tabButtons[i].repaint();
        }
        cardLayout.show(contentArea, TAB_KEYS[idx]);
        if (idx == 0) medicationPanel.refreshTable();
        if (idx == 3) caregiverPanel.loadEscalationLog();
    }

    private JPanel buildContentArea() {
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(BG);

        medicationPanel = new MedicationPanel(patient);
        scanPanel       = new ScanPanel(patient, ocrService);
        voicePanel      = new VoicePanel(patient, voiceEngine);
        caregiverPanel  = new CaregiverPanel(patient, escalationService);

        contentArea.add(medicationPanel, TAB_KEYS[0]);
        contentArea.add(scanPanel,       TAB_KEYS[1]);
        contentArea.add(voicePanel,      TAB_KEYS[2]);
        contentArea.add(caregiverPanel,  TAB_KEYS[3]);

        cardLayout.show(contentArea, TAB_KEYS[0]);
        return contentArea;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 5));
        bar.setBackground(STATUS_BAR);
        bar.setBorder(BorderFactory.createMatteBorder(1,0,0,0, CARD_BORDER));
        bar.setPreferredSize(new Dimension(0, 30));

        // Green dot + status
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dot.setForeground(new Color(63, 185, 80));

        JLabel status = new JLabel("Reminder service active  ·  " + AppConstants.APP_NAME
                + "  ·  v" + AppConstants.APP_VERSION);
        status.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        status.setForeground(TEXT_MUTED);

        bar.add(dot);
        bar.add(status);
        return bar;
    }

    private void configureFrame() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                int c = JOptionPane.showConfirmDialog(MainFrame.this,
                        "Stop MedAssist and exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
                if (c == JOptionPane.YES_OPTION) {
                    reminderService.cancelReminder();
                    escalationService.stopMonitoring();
                    dispose(); System.exit(0);
                }
            }
        });
        setSize(AppConstants.WINDOW_WIDTH, AppConstants.WINDOW_HEIGHT);
        setMinimumSize(new Dimension(860, 580));
        setLocationRelativeTo(null);
    }

    public void startBackgroundServices() {
        Thread t = new Thread(reminderService::scheduleAllMedications, "MedAssist-Reminder");
        t.setDaemon(true); t.start();
        escalationService.startMonitoring();
        System.out.println("[MainFrame] Background services started.");
    }

    public ReminderService   getReminderService()   { return reminderService; }
    public EscalationService getEscalationService() { return escalationService; }

    // ── About ────────────────────────────────────────────────────────────────────

    private JButton buildAboutButton() {
        JButton btn = new JButton("ⓘ") {
            private boolean hov = false;
            { addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){hov=true; repaint();}
                public void mouseExited (MouseEvent e){hov=false;repaint();}
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = new Color(255,255,255, hov ? 25 : 12);
                g2.setColor(base);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0,0,getWidth(),getHeight(),24,24));
                g2.setColor(new Color(ACCENT_BLUE.getRed(),ACCENT_BLUE.getGreen(),ACCENT_BLUE.getBlue(),120));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,24,24));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(ACCENT_BLUE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(36, 36));
        btn.setToolTipText("About MedAssist");
        btn.addActionListener(e -> showAboutDialog());
        return btn;
    }

    private void showAboutDialog() {
        JDialog dlg = new JDialog(this, "About MedAssist", true);
        dlg.setSize(420, 340);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(new Color(22, 27, 34));
        root.setBorder(new EmptyBorder(24, 28, 24, 28));

        // App icon + name
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleRow.setOpaque(false);
        JLabel iconLbl = new JLabel("💊");
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        JLabel nameLbl = new JLabel("MedAssist");
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        nameLbl.setForeground(Color.WHITE);
        titleRow.add(iconLbl);
        titleRow.add(nameLbl);
        root.add(titleRow, BorderLayout.NORTH);

        // Info grid
        JPanel info = new JPanel(new GridLayout(0, 2, 10, 10));
        info.setOpaque(false);
        info.setBorder(new EmptyBorder(18, 0, 18, 0));

        String[][] rows = {
            {"Version",     AppConstants.APP_VERSION},
            {"Description", "AI Medication Adherence System"},
            {"Developer",   "MedAssist Team"},
            {"College",     "Your College Name Here"},
            {"Subject",     "Advanced Java Programming"},
            {"Build",       "Java SE 17 · Maven · Swing"}
        };
        for (String[] r : rows) {
            JLabel k = new JLabel(r[0]);
            k.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            k.setForeground(new Color(139, 148, 158));
            JLabel v = new JLabel(r[1]);
            v.setFont(new Font("Segoe UI", Font.BOLD, 13));
            v.setForeground(new Color(230, 237, 243));
            info.add(k); info.add(v);
        }
        root.add(info, BorderLayout.CENTER);

        // Close button
        JButton closeBtn = new JButton("Close") {
            private boolean hov=false;
            {addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){hov=true;repaint();}
                public void mouseExited(MouseEvent e){hov=false;repaint();}
            });}
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov?ACCENT_BLUE.brighter():ACCENT_BLUE);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0,0,getWidth(),getHeight(),10,10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.setPreferredSize(new Dimension(100, 36));
        closeBtn.addActionListener(e -> dlg.dispose());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setOpaque(false);
        btnRow.add(closeBtn);
        root.add(btnRow, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }
}
