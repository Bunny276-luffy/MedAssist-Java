package com.medassist.ui;

import com.medassist.model.Medication;
import com.medassist.model.MedicationStatus;
import com.medassist.model.Patient;
import com.medassist.util.DateTimeUtil;
import com.medassist.util.FileStorageUtil;
import com.medassist.model.DoseLog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 * Premium medication panel — v2.1
 * <p>Added: styled Add dialog with frequency dropdown and validation, Delete button,
 * and live adherence percentage label below stat cards.</p>
 *
 * @author MedAssist Team
 * @version 2.1
 */
public class MedicationPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // ── Palette ─────────────────────────────────────────────────────────────────
    private static final Color BG             = new Color(13,  17,  23);
    private static final Color CARD_BG        = new Color(22,  27,  34);
    private static final Color CARD_BORDER    = new Color(48,  54,  61);
    private static final Color ROW_ODD        = new Color(22,  27,  34);
    private static final Color ROW_EVEN       = new Color(26,  32,  40);
    private static final Color ROW_SELECTED   = new Color(31,  48,  78);
    private static final Color ROW_HOVER      = new Color(30,  38,  52);
    private static final Color HDR_BG         = new Color(33,  38,  45);
    private static final Color TEXT_PRIMARY   = new Color(230, 237, 243);
    private static final Color TEXT_SECONDARY = new Color(139, 148, 158);
    private static final Color ACCENT_BLUE    = new Color(88,  166, 255);
    private static final Color GREEN          = new Color(63,  185, 80);
    private static final Color RED            = new Color(248, 81,  73);
    private static final Color AMBER          = new Color(210, 153, 34);
    private static final Color CRITICAL_RED   = new Color(255, 60,  60);
    private static final Color GRAY_BADGE     = new Color(75,  85,  99);
    private static final Color DELETE_RED     = new Color(185, 28,  28);
    private static final Color DIALOG_BG      = new Color(22,  27,  34);
    private static final Color FIELD_BG       = new Color(33,  38,  45);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD,  22);
    private static final Font FONT_BODY  = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BOLD  = new Font("Segoe UI", Font.BOLD,  14);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    private static final String[] FREQUENCIES = {
        "once daily", "twice daily", "three times daily", "every 8 hours",
        "every 12 hours", "as needed", "at bedtime", "with meals"
    };

    private final Patient patient;
    private DefaultTableModel tableModel;
    private JTable table;
    private int hoveredRow = -1;

    private JLabel statTotal, statTaken, statPending, statCritical;
    private JLabel adherenceLabel; // NEW: adherence % label

    public MedicationPanel(Patient patient) {
        this.patient = patient;
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);
        setBorder(new EmptyBorder(20, 20, 15, 20));
        add(buildHeader(),      BorderLayout.NORTH);
        add(buildTablePanel(),  BorderLayout.CENTER);
        add(buildButtonBar(),   BorderLayout.SOUTH);
        refreshTable();
    }

    // ── Header + Stats ──────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel outer = new JPanel(new BorderLayout(0, 10));
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel title = new JLabel("💊  Today's Medication Schedule");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_PRIMARY);

        // Stats cards row
        JPanel statsRow = new JPanel(new BorderLayout(0, 6));
        statsRow.setOpaque(false);
        statsRow.add(buildStatsBar(), BorderLayout.CENTER);

        // Adherence label below cards
        adherenceLabel = new JLabel("Adherence today: — %");
        adherenceLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        adherenceLabel.setForeground(ACCENT_BLUE);
        adherenceLabel.setBorder(new EmptyBorder(4, 2, 0, 0));
        statsRow.add(adherenceLabel, BorderLayout.SOUTH);

        outer.add(title,    BorderLayout.NORTH);
        outer.add(statsRow, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildStatsBar() {
        statTotal    = makeStatValue("0", ACCENT_BLUE);
        statTaken    = makeStatValue("0", GREEN);
        statPending  = makeStatValue("0", AMBER);
        statCritical = makeStatValue("0", RED);

        JPanel bar = new JPanel(new GridLayout(1, 4, 10, 0));
        bar.setOpaque(false);
        bar.add(statCard("Total",    statTotal,    ACCENT_BLUE));
        bar.add(statCard("Taken",    statTaken,    GREEN));
        bar.add(statCard("Pending",  statPending,  AMBER));
        bar.add(statCard("Critical", statCritical, RED));
        return bar;
    }

    private JLabel makeStatValue(String v, Color c) {
        JLabel l = new JLabel(v);
        l.setFont(new Font("Segoe UI", Font.BOLD, 28));
        l.setForeground(c);
        return l;
    }

    private JPanel statCard(String name, JLabel valueLbl, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 2)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(accent);
                g2.fillRect(0, 0, 4, getHeight());
                g2.setColor(CARD_BORDER);
                g2.draw(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,12,12));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 18, 10, 14));
        card.setPreferredSize(new Dimension(0, 76));

        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(FONT_SMALL);
        nameLbl.setForeground(TEXT_SECONDARY);

        card.add(valueLbl, BorderLayout.CENTER);
        card.add(nameLbl,  BorderLayout.SOUTH);
        return card;
    }

    // ── Table ───────────────────────────────────────────────────────────────────

    private JPanel buildTablePanel() {
        String[] cols = {"  Drug Name", "Dosage", "Scheduled Time", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setBackground(CARD_BG);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(new Color(35, 42, 52));
        table.setRowHeight(50);
        table.setFont(FONT_BODY);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(ROW_SELECTED);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setFocusable(false);

        JTableHeader hdr = table.getTableHeader();
        hdr.setBackground(HDR_BG);
        hdr.setForeground(TEXT_SECONDARY);
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 12));
        hdr.setPreferredSize(new Dimension(0, 36));
        hdr.setBorder(BorderFactory.createMatteBorder(0,0,1,0, CARD_BORDER));
        hdr.setReorderingAllowed(false);

        table.getColumnModel().getColumn(0).setPreferredWidth(260);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(160);

        table.getColumnModel().getColumn(0).setCellRenderer(new DrugNameRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new BaseRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new BaseRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusBadgeRenderer());

        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) { hoveredRow = row; table.repaint(); }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { hoveredRow = -1; table.repaint(); }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
        scroll.getViewport().setBackground(CARD_BG);
        scroll.setBackground(CARD_BG);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 0, 8, 0));
        wrapper.add(scroll);
        return wrapper;
    }

    // ── Button Bar ──────────────────────────────────────────────────────────────

    private JPanel buildButtonBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        bar.setOpaque(false);
        bar.add(pillBtn("✅  Mark as Taken",  GREEN,                   e -> onMarkTaken()));
        bar.add(pillBtn("➕  Add Medication", ACCENT_BLUE,             e -> onAddMedication()));
        bar.add(pillBtn("🗑  Remove",         DELETE_RED,              e -> onRemoveMedication()));
        bar.add(pillBtn("🔄  Refresh",        new Color(55, 65, 81),   e -> refreshTable()));
        return bar;
    }

    private JButton pillBtn(String text, Color bg, ActionListener al) {
        JButton btn = new JButton(text) {
            private boolean hov = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e){ hov=true;  repaint(); }
                public void mouseExited (MouseEvent e){ hov=false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? bg.brighter() : bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
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
        btn.setPreferredSize(new Dimension(175, 40));
        btn.addActionListener(al);
        return btn;
    }

    // ── Handlers ────────────────────────────────────────────────────────────────

    private void onMarkTaken() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a medication row first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Medication med = patient.getMedications().get(row);
        if (med.getStatus() == MedicationStatus.TAKEN) {
            JOptionPane.showMessageDialog(this, med.getDrugName() + " is already marked as taken.",
                    "Already Taken", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        med.setStatus(MedicationStatus.TAKEN);
        DoseLog log = new DoseLog(med.getDrugName(), med.getScheduledTime(), LocalTime.now(), MedicationStatus.TAKEN);
        try { FileStorageUtil.saveDoseLog(log); FileStorageUtil.savePatient(patient); }
        catch (IOException ex) { System.err.println("Save failed: " + ex.getMessage()); }
        refreshTable();
        JOptionPane.showMessageDialog(this, "✅  " + med.getDrugName() + " marked as taken!",
                "Dose Recorded", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows a fully styled dark-theme JDialog for adding a new medication.
     * Includes all required fields, a frequency dropdown, a critical checkbox,
     * and inline red validation error messages.
     */
    private void onAddMedication() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "➕  Add New Medication", true);
        dialog.setSize(480, 440);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        // ── Title ──
        JLabel title = new JLabel("Add New Medication");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 16, 0));
        root.add(title, BorderLayout.NORTH);

        // ── Form ──
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(DIALOG_BG);
        form.setOpaque(true);

        GridBagConstraints lc = new GridBagConstraints();
        lc.gridx = 0; lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(8, 0, 4, 14);

        GridBagConstraints fc = new GridBagConstraints();
        fc.gridx = 1; fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0; fc.insets = new Insets(8, 0, 4, 0);

        // Drug Name
        lc.gridy = 0; fc.gridy = 0;
        form.add(dlgLabel("Drug Name *"), lc);
        JTextField nameF = dlgField(); form.add(nameF, fc);

        // Dosage
        lc.gridy = 1; fc.gridy = 1;
        form.add(dlgLabel("Dosage *"), lc);
        JTextField doseF = dlgField(); doseF.setToolTipText("e.g. 500mg, 1 tablet");
        form.add(doseF, fc);

        // Frequency dropdown
        lc.gridy = 2; fc.gridy = 2;
        form.add(dlgLabel("Frequency *"), lc);
        JComboBox<String> freqBox = new JComboBox<>(FREQUENCIES);
        styleComboBox(freqBox);
        form.add(freqBox, fc);

        // Scheduled Time
        lc.gridy = 3; fc.gridy = 3;
        form.add(dlgLabel("Time (HH:mm) *"), lc);
        JTextField timeF = dlgField(); timeF.setText("08:00");
        timeF.setToolTipText("24-hour format, e.g. 09:30");
        form.add(timeF, fc);

        // Critical checkbox
        lc.gridy = 4; fc.gridy = 4;
        form.add(new JLabel(), lc);
        JCheckBox critBox = new JCheckBox("⚠  Mark as Critical");
        critBox.setForeground(AMBER);
        critBox.setFont(FONT_BODY);
        critBox.setBackground(DIALOG_BG);
        critBox.setFocusPainted(false);
        form.add(critBox, fc);

        // Validation error label (hidden by default)
        lc.gridy = 5; fc.gridy = 5;
        fc.gridwidth = 2; lc.gridwidth = 2;
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setFont(FONT_SMALL);
        errorLabel.setForeground(RED);
        errorLabel.setBorder(new EmptyBorder(4, 0, 0, 0));
        form.add(errorLabel, fc);

        root.add(form, BorderLayout.CENTER);

        // ── Buttons ──
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setBackground(DIALOG_BG);
        btnRow.setBorder(new EmptyBorder(16, 0, 0, 0));

        JButton cancelBtn = dialogBtn("Cancel", new Color(55, 65, 81));
        JButton addBtn    = dialogBtn("  Add Medication  ", ACCENT_BLUE);

        cancelBtn.addActionListener(e -> dialog.dispose());
        addBtn.addActionListener(e -> {
            // Validate
            String name  = nameF.getText().trim();
            String dose  = doseF.getText().trim();
            String time  = timeF.getText().trim();
            String freq  = (String) freqBox.getSelectedItem();

            if (name.isEmpty() || dose.isEmpty() || time.isEmpty()) {
                errorLabel.setText("⚠  All fields marked with * are required.");
                return;
            }

            LocalTime scheduledTime;
            try {
                scheduledTime = LocalTime.parse(time);
            } catch (DateTimeParseException ex) {
                errorLabel.setText("⚠  Invalid time format. Use HH:mm (e.g. 09:00).");
                timeF.setForeground(RED);
                return;
            }

            patient.addMedication(new Medication(name, dose, freq, scheduledTime, critBox.isSelected()));
            try { FileStorageUtil.savePatient(patient); } catch (IOException ignored) {}
            refreshTable();
            dialog.dispose();
            JOptionPane.showMessageDialog(MedicationPanel.this,
                    "✅  " + name + " added to your schedule at " + scheduledTime + ".",
                    "Medication Added", JOptionPane.INFORMATION_MESSAGE);
        });

        btnRow.add(cancelBtn);
        btnRow.add(addBtn);
        root.add(btnRow, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    /**
     * Removes the selected medication after a confirmation dialog.
     */
    private void onRemoveMedication() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a medication row to remove.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Medication med = patient.getMedications().get(row);
        int choice = JOptionPane.showConfirmDialog(this,
                "Remove \"" + med.getDrugName() + "\" from schedule?\n"
                + "This cannot be undone.",
                "Confirm Remove",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            patient.getMedications().remove(row);
            try { FileStorageUtil.savePatient(patient); } catch (IOException ignored) {}
            refreshTable();
        }
    }

    // ── Refresh ─────────────────────────────────────────────────────────────────

    public void refreshTable() {
        tableModel.setRowCount(0);
        int total = patient.getMedications().size();
        int taken = 0, pending = 0, critical = 0;

        for (Medication med : patient.getMedications()) {
            boolean crit = med.isCritical() && med.getStatus() != MedicationStatus.TAKEN;
            tableModel.addRow(new Object[]{
                    (crit ? "⚠  " : "    ") + med.getDrugName(),
                    med.getDosage(),
                    DateTimeUtil.formatTimeForDisplay(med.getScheduledTime()),
                    med.getStatus().name()
            });
            if (med.getStatus() == MedicationStatus.TAKEN)  taken++;
            if (med.getStatus() == MedicationStatus.PENDING) pending++;
            if (med.isCritical() && med.getStatus() != MedicationStatus.TAKEN) critical++;
        }

        if (statTotal != null) {
            statTotal.setText(String.valueOf(total));
            statTaken.setText(String.valueOf(taken));
            statPending.setText(String.valueOf(pending));
            statCritical.setText(String.valueOf(critical));
        }

        // Update adherence label
        if (adherenceLabel != null) {
            if (total == 0) {
                adherenceLabel.setText("Adherence today: — %");
                adherenceLabel.setForeground(TEXT_SECONDARY);
            } else {
                int pct = (int) Math.round((taken * 100.0) / total);
                adherenceLabel.setText("Adherence today:  " + pct + "%");
                // Color: green ≥70%, amber ≥40%, red <40%
                adherenceLabel.setForeground(pct >= 70 ? GREEN : (pct >= 40 ? AMBER : RED));
            }
        }
    }

    // ── Cell Renderers ──────────────────────────────────────────────────────────

    private Color rowBg(int row, boolean sel) {
        if (sel)               return ROW_SELECTED;
        if (row == hoveredRow) return ROW_HOVER;
        return row % 2 == 0 ? ROW_ODD : ROW_EVEN;
    }

    private class BaseRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            setFont(FONT_BODY); setForeground(TEXT_PRIMARY);
            setBorder(new EmptyBorder(0, 12, 0, 8));
            setBackground(rowBg(row, sel));
            return this;
        }
    }

    private class DrugNameRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            String s = v == null ? "" : v.toString();
            setFont(FONT_BOLD);
            setForeground(s.startsWith("⚠") ? CRITICAL_RED : TEXT_PRIMARY);
            setBorder(new EmptyBorder(0, 8, 0, 8));
            setBackground(rowBg(row, sel));
            return this;
        }
    }

    private class StatusBadgeRenderer extends JPanel implements TableCellRenderer {
        private String text = "";
        private Color  badgeColor = GRAY_BADGE;

        StatusBadgeRenderer() { setOpaque(true); }

        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                boolean selected, boolean foc, int row, int col) {
            text = v == null ? "" : v.toString();
            badgeColor = switch (text) {
                case "TAKEN"           -> GREEN;
                case "MISSED"          -> RED;
                case "CRITICAL_MISSED" -> CRITICAL_RED;
                case "SKIPPED"         -> AMBER;
                default                -> GRAY_BADGE;
            };
            setBackground(rowBg(row, selected));
            return this;
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(FONT_BOLD);
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(text);
            int bw = tw + 22, bh = 26;
            int bx = (getWidth() - bw) / 2;
            int by = (getHeight() - bh) / 2;
            g2.setColor(new Color(badgeColor.getRed(), badgeColor.getGreen(), badgeColor.getBlue(), 25));
            g2.fill(new RoundRectangle2D.Float(bx, by, bw, bh, bh, bh));
            g2.setColor(new Color(badgeColor.getRed(), badgeColor.getGreen(), badgeColor.getBlue(), 170));
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Float(bx+0.75f, by+0.75f, bw-1.5f, bh-1.5f, bh, bh));
            g2.setColor(badgeColor);
            int textY = by + ((bh - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(text, bx + 11, textY);
            g2.dispose();
        }
    }

    // ── Dialog Helpers ──────────────────────────────────────────────────────────

    private JLabel dlgLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_SECONDARY);
        l.setFont(FONT_BODY);
        return l;
    }

    private JTextField dlgField() {
        JTextField f = new JTextField(22);
        f.setBackground(FIELD_BG);
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(ACCENT_BLUE);
        f.setFont(FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                new EmptyBorder(6, 9, 6, 9)));
        return f;
    }

    private void styleComboBox(JComboBox<String> box) {
        box.setBackground(FIELD_BG);
        box.setForeground(TEXT_PRIMARY);
        box.setFont(FONT_BODY);
        box.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
        box.setFocusable(false);
        // Style the renderer to match dark theme
        box.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? ROW_SELECTED : FIELD_BG);
                setForeground(TEXT_PRIMARY);
                setFont(FONT_BODY);
                setBorder(new EmptyBorder(4, 8, 4, 8));
                return this;
            }
        });
    }

    private JButton dialogBtn(String text, Color bg) {
        JButton btn = new JButton(text) {
            private boolean hov = false;
            { addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){hov=true; repaint();}
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
        btn.setPreferredSize(new Dimension(160, 38));
        return btn;
    }

    // Kept for compatibility (used by VoicePanel indirectly through patient model)
    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_SECONDARY);
        l.setFont(FONT_BODY);
        return l;
    }
}
