package com.medassist.ui;

import com.medassist.model.Caregiver;
import com.medassist.model.Patient;
import com.medassist.service.EscalationService;
import com.medassist.util.FileStorageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Premium caregiver panel with info cards, escalation log, and test alert button.
 *
 * @author MedAssist Team
 * @version 2.0
 */
public class CaregiverPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Color BG           = new Color(13, 17, 23);
    private static final Color CARD_BG      = new Color(22, 27, 34);
    private static final Color CARD_BORDER  = new Color(48, 54, 61);
    private static final Color TEXT_PRIMARY = new Color(230, 237, 243);
    private static final Color TEXT_MUTED   = new Color(139, 148, 158);
    private static final Color ACCENT_BLUE  = new Color(88, 166, 255);
    private static final Color GREEN        = new Color(63, 185, 80);
    private static final Color RED         = new Color(248, 81, 73);
    private static final Color AMBER       = new Color(210, 153, 34);

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_MONO   = new Font("Consolas", Font.PLAIN, 13);

    private final Patient patient;
    private final EscalationService escalationService;

    private JTextArea logArea;
    private JLabel statusLabel;

    public CaregiverPanel(Patient patient, EscalationService escalationService) {
        this.patient = patient;
        this.escalationService = escalationService;
        initComponents();
        loadEscalationLog();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 14));
        setBackground(BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(buildHeader(),    BorderLayout.NORTH);
        add(buildCenter(),    BorderLayout.CENTER);
        add(buildBottom(),    BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 6, 0));

        JLabel title = new JLabel("👩‍⚕️  Caregiver & Escalation");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_PRIMARY);

        JLabel sub = new JLabel("Emergency contacts · Alert history · Test notifications");
        sub.setFont(FONT_SMALL);
        sub.setForeground(TEXT_MUTED);

        p.add(title, BorderLayout.NORTH);
        p.add(sub, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setOpaque(false);

        center.add(buildInfoCard(),  BorderLayout.NORTH);
        center.add(buildLogCard(),   BorderLayout.CENTER);
        return center;
    }

    private JPanel buildInfoCard() {
        Caregiver cg = patient.getCaregiver();

        JPanel card = new JPanel(new GridLayout(2, 2, 0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),14,14));
                g2.setColor(CARD_BORDER);
                g2.draw(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,14,14));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 20, 16, 20));
        card.setPreferredSize(new Dimension(0, 115));

        card.add(infoCell("👤  Name",         cg.getName()));
        card.add(infoCell("📧  Email",         cg.getEmail()));
        card.add(infoCell("📱  Phone",         cg.getPhone()));
        card.add(infoCell("🤝  Relationship",  cg.getRelationship()));

        return wrapWithLabel("Caregiver Details", card);
    }

    private JPanel infoCell(String label, String value) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(6, 0, 6, 20));

        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(FONT_BOLD);
        val.setForeground(TEXT_PRIMARY);

        p.add(lbl, BorderLayout.NORTH);
        p.add(val, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildLogCard() {
        logArea = new JTextArea();
        logArea.setName("txtEscalationLog");
        logArea.setEditable(false);
        logArea.setBackground(new Color(15, 20, 30));
        logArea.setForeground(new Color(230, 160, 80));
        logArea.setFont(FONT_MONO);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setMargin(new Insets(12, 14, 12, 14));

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
        scroll.setBackground(new Color(15, 20, 30));
        scroll.getViewport().setBackground(new Color(15, 20, 30));

        return wrapWithLabel("Escalation Alert Log", scroll);
    }

    private JPanel wrapWithLabel(String title, Component comp) {
        JPanel outer = new JPanel(new BorderLayout(0, 8));
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(12, 0, 0, 0));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_MUTED);
        lbl.setBorder(new EmptyBorder(0, 2, 0, 0));

        outer.add(lbl,  BorderLayout.NORTH);
        outer.add(comp, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildBottom() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(8, 0, 0, 0));

        statusLabel = new JLabel("  ");
        statusLabel.setFont(FONT_SMALL);
        statusLabel.setForeground(GREEN);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        btns.add(pillBtn("🔄  Refresh Log",  new Color(55,65,81), e -> loadEscalationLog()));
        btns.add(pillBtn("⚠️  Test Alert",   RED,                   e -> onTestAlert()));

        row.add(statusLabel, BorderLayout.CENTER);
        row.add(btns,        BorderLayout.EAST);
        return row;
    }

    private void onTestAlert() {
        statusLabel.setText("⏳  Triggering test alert...");
        statusLabel.setForeground(AMBER);
        SwingWorker<Void,Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                escalationService.triggerTestAlert(); return null;
            }
            @Override protected void done() {
                loadEscalationLog();
                statusLabel.setText("✅  Test alert triggered. Check log above.");
                statusLabel.setForeground(GREEN);
            }
        };
        w.execute();
    }

    public void loadEscalationLog() {
        StringBuilder sb = new StringBuilder();
        List<String> history = escalationService.getEscalationHistory();
        if (!history.isEmpty()) {
            sb.append("═══ Session Alerts ═══════════════════\n");
            history.forEach(e -> sb.append("  ⚠  ").append(e).append("\n"));
            sb.append("\n");
        }
        String file = FileStorageUtil.readEscalationLog();
        if (!file.isBlank()) {
            sb.append("═══ Persisted Log ════════════════════\n").append(file);
        }
        logArea.setText(sb.isEmpty()
                ? "(No escalation alerts recorded yet.)"
                : sb.toString());
    }

    private JButton pillBtn(String text, Color bg, ActionListener al) {
        JButton btn = new JButton(text) {
            private boolean hov=false;
            { addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){hov=true;repaint();}
                public void mouseExited(MouseEvent e){hov=false;repaint();}
            });}
            @Override protected void paintComponent(Graphics g){
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
        btn.setPreferredSize(new Dimension(160, 40));
        btn.addActionListener(al);
        return btn;
    }
}
