package com.medassist.ui;

import com.medassist.exception.OCRFailureException;
import com.medassist.model.Medication;
import com.medassist.model.Patient;
import com.medassist.model.Prescription;
import com.medassist.service.OCRService;
import com.medassist.util.FileStorageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import javax.imageio.ImageIO;

/**
 * Swing panel that implements the prescription scanning workflow.
 *
 * <p>The {@code ScanPanel} provides a file chooser to select a prescription image,
 * runs {@link OCRService#extractPrescription} in a background {@link SwingWorker}
 * to keep the UI responsive, displays the extracted fields in editable text fields,
 * and allows the user to confirm and add the prescription to the patient's schedule.</p>
 *
 * @author MedAssist Team
 * @version 1.0
 * @see OCRService
 */
public class ScanPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Patient patient;
    private final OCRService ocrService;

    // ---- Result Fields ----
    private JTextField drugNameField;
    private JTextField dosageField;
    private JTextField frequencyField;
    private JTextField durationField;
    private JTextField confidenceField;
    private JLabel statusLabel;
    private JButton chooseImageBtn;
    private JButton confirmAddBtn;
    private JProgressBar progressBar;
    private JLabel imagePreviewLabel;  // NEW: image thumbnail
    private File   lastImageFile;      // NEW: keep reference for preview

    /** Holds the most recently extracted prescription. */
    private Prescription currentPrescription;

    /**
     * Constructs a new {@code ScanPanel} for the given patient.
     *
     * @param patient    the patient to whom a new prescription may be added; must not be {@code null}
     * @param ocrService the OCR service to use for image extraction; must not be {@code null}
     */
    public ScanPanel(Patient patient, OCRService ocrService) {
        this.patient = patient;
        this.ocrService = ocrService;
        initComponents();
    }

    /**
     * Initialises and lays out all Swing components.
     */
    private void initComponents() {
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(18, 18, 30));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // ---- Header ----
        JLabel header = new JLabel("🔬  Scan Prescription (OCR)");
        header.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.setForeground(new Color(130, 200, 255));
        header.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(header, BorderLayout.NORTH);

        // ---- Center: control + results ----
        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.setBackground(new Color(18, 18, 30));

        centerPanel.add(createControlPanel(), BorderLayout.NORTH);

        // NEW: image preview + results stacked vertically
        JPanel previewAndResults = new JPanel(new BorderLayout(0, 10));
        previewAndResults.setBackground(new Color(18, 18, 30));
        previewAndResults.add(createPreviewPanel(),  BorderLayout.NORTH);
        previewAndResults.add(createResultsPanel(),  BorderLayout.CENTER);
        centerPanel.add(previewAndResults, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // ---- South: status bar + confirm button ----
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    /**
     * Creates the top control panel with the file chooser button and progress bar.
     *
     * @return styled control {@link JPanel}
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBackground(new Color(25, 25, 40));
        panel.setBorder(BorderFactory.createLineBorder(new Color(60, 80, 120)));

        JLabel instrLabel = new JLabel("Select a prescription image (JPEG/PNG/TIFF):");
        instrLabel.setForeground(new Color(180, 180, 200));
        instrLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        chooseImageBtn = createStyledButton("📁  Choose Image", new Color(52, 152, 219));
        chooseImageBtn.setName("btnChooseImage");
        chooseImageBtn.addActionListener(e -> onChooseImage());

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(200, 22));
        progressBar.setString("Processing...");
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(52, 152, 219));

        panel.add(instrLabel);
        panel.add(chooseImageBtn);
        panel.add(progressBar);
        return panel;
    }

    /**
     * Creates a preview panel that shows a scaled thumbnail of the chosen image.
     * Hidden until an image is selected.
     *
     * @return the preview {@link JPanel}
     */
    private JPanel createPreviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(18, 18, 30));
        panel.setBorder(BorderFactory.createLineBorder(new Color(60, 80, 120)));

        imagePreviewLabel = new JLabel("No image selected", JLabel.CENTER);
        imagePreviewLabel.setForeground(new Color(100, 100, 130));
        imagePreviewLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        imagePreviewLabel.setPreferredSize(new Dimension(0, 30));
        imagePreviewLabel.setBorder(new EmptyBorder(4, 10, 4, 10));
        panel.add(imagePreviewLabel, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Creates the results form panel displaying extracted prescription fields.
     *
     * @return styled results {@link JPanel}
     */
    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(25, 25, 40));
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60, 80, 120)),
                "Extracted Prescription Fields");
        border.setTitleColor(new Color(130, 200, 255));
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.setBorder(border);

        drugNameField  = createResultField("Drug Name",   panel, 0);
        dosageField    = createResultField("Dosage",      panel, 1);
        frequencyField = createResultField("Frequency",   panel, 2);
        durationField  = createResultField("Duration",    panel, 3);
        confidenceField = createResultField("Confidence", panel, 4);
        confidenceField.setEditable(false);

        return panel;
    }

    /**
     * Helper method to add a labelled text field row to the results panel.
     *
     * @param label     the field label text
     * @param container the container to add to
     * @param row       the grid row index
     * @return the created {@link JTextField}
     */
    private JTextField createResultField(String label, JPanel container, int row) {
        GridBagConstraints lblC = new GridBagConstraints();
        lblC.gridx = 0; lblC.gridy = row;
        lblC.anchor = GridBagConstraints.WEST;
        lblC.insets = new Insets(6, 10, 6, 10);

        JLabel lbl = new JLabel(label + ":");
        lbl.setForeground(new Color(160, 180, 220));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        container.add(lbl, lblC);

        GridBagConstraints fldC = new GridBagConstraints();
        fldC.gridx = 1; fldC.gridy = row;
        fldC.fill = GridBagConstraints.HORIZONTAL;
        fldC.weightx = 1.0;
        fldC.insets = new Insets(6, 0, 6, 10);

        JTextField field = new JTextField(25);
        field.setBackground(new Color(35, 35, 55));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createLineBorder(new Color(80, 100, 150)));
        container.add(field, fldC);
        return field;
    }

    /**
     * Creates the bottom panel containing the status label and confirm button.
     *
     * @return styled bottom {@link JPanel}
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(18, 18, 30));

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(new Color(100, 220, 140));
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));

        confirmAddBtn = createStyledButton("✅  Confirm & Add to Schedule", new Color(39, 174, 96));
        confirmAddBtn.setName("btnConfirmAdd");
        confirmAddBtn.setEnabled(false);
        confirmAddBtn.addActionListener(e -> onConfirmAdd());

        panel.add(statusLabel, BorderLayout.CENTER);
        panel.add(confirmAddBtn, BorderLayout.EAST);
        return panel;
    }

    /**
     * Handles the 'Choose Image' button click.
     * Opens a {@link JFileChooser} and runs OCR in a background {@link SwingWorker}.
     */
    private void onChooseImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Prescription Image");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image Files (JPG, PNG, TIFF)", "jpg", "jpeg", "png", "tiff", "tif"));

        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File imageFile = fileChooser.getSelectedFile();
        lastImageFile = imageFile;
        statusLabel.setText("Processing: " + imageFile.getName() + " ...");
        confirmAddBtn.setEnabled(false);
        chooseImageBtn.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        clearFields();

        // Show image preview immediately
        showImagePreview(imageFile);

        // Run OCR in background thread to keep UI responsive
        SwingWorker<Prescription, Void> worker = new SwingWorker<>() {
            @Override
            protected Prescription doInBackground() throws Exception {
                return ocrService.extractPrescription(imageFile);
            }

            @Override
            protected void done() {
                progressBar.setIndeterminate(false);
                progressBar.setVisible(false);
                chooseImageBtn.setEnabled(true);
                try {
                    currentPrescription = get();
                    populateFields(currentPrescription);
                    confirmAddBtn.setEnabled(true);
                    statusLabel.setForeground(new Color(100, 220, 140));
                    statusLabel.setText("✅ OCR complete. Review fields and confirm.");
                } catch (Exception ex) {
                    String msg = ex.getCause() instanceof OCRFailureException
                            ? ex.getCause().getMessage()
                            : ex.getMessage();
                    statusLabel.setForeground(new Color(220, 80, 80));
                    statusLabel.setText("❌ OCR failed: " + msg);
                }
            }
        };
        worker.execute();
    }

    /**
     * Handles the 'Confirm & Add' button click.
     * Reads the (possibly edited) fields and adds a new {@link Medication} to the patient.
     */
    private void onConfirmAdd() {
        try {
            String name  = drugNameField.getText().trim();
            String dose  = dosageField.getText().trim();
            String freq  = frequencyField.getText().trim();

            if (name.isEmpty() || dose.isEmpty() || freq.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Drug Name, Dosage, and Frequency are required.",
                        "Incomplete Fields", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Default to current time + 1 hour if the user hasn't specified a time
            LocalTime scheduleTime = LocalTime.now().plusHours(1).withSecond(0).withNano(0);

            Medication med = new Medication(name, dose, freq, scheduleTime, false);
            patient.addMedication(med);
            FileStorageUtil.savePatient(patient);

            statusLabel.setForeground(new Color(100, 220, 140));
            statusLabel.setText("✅ " + name + " added to schedule at "
                    + scheduleTime + ".");
            confirmAddBtn.setEnabled(false);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to save patient: " + ex.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Populates the result text fields from the given prescription.
     *
     * @param p the prescription to display
     */
    private void populateFields(Prescription p) {
        drugNameField.setText(p.getDrugName());
        dosageField.setText(p.getDosage());
        frequencyField.setText(p.getFrequency());
        durationField.setText(p.getDuration());
        confidenceField.setText(String.format("%.0f%%", p.getConfidence() * 100));
    }

    /**
     * Clears all result text fields.
     */
    private void clearFields() {
        drugNameField.setText("");
        dosageField.setText("");
        frequencyField.setText("");
        durationField.setText("");
        confidenceField.setText("");
    }

    /**
     * Reads the image file and displays a scaled thumbnail (max 200 px tall)
     * in the {@link #imagePreviewLabel}.
     *
     * @param file the image file to preview
     */
    private void showImagePreview(File file) {
        try {
            BufferedImage original = ImageIO.read(file);
            if (original == null) {
                imagePreviewLabel.setIcon(null);
                imagePreviewLabel.setText("⚠  Could not load image preview.");
                imagePreviewLabel.setPreferredSize(new Dimension(0, 30));
                return;
            }
            int maxH = 200;
            int origW = original.getWidth();
            int origH = original.getHeight();
            int scaledH = Math.min(origH, maxH);
            int scaledW = (int) ((double) origW / origH * scaledH);

            Image scaled = original.getScaledInstance(scaledW, scaledH, Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon(scaled);

            imagePreviewLabel.setIcon(icon);
            imagePreviewLabel.setText(null);
            imagePreviewLabel.setHorizontalAlignment(JLabel.CENTER);
            imagePreviewLabel.setPreferredSize(new Dimension(0, scaledH + 8));
            imagePreviewLabel.getParent().revalidate();
            imagePreviewLabel.getParent().repaint();
        } catch (Exception ex) {
            imagePreviewLabel.setIcon(null);
            imagePreviewLabel.setText("Preview unavailable: " + ex.getMessage());
        }
    }

    /**
     * Creates a standardized styled button.
     *
     * @param text  button label
     * @param color button background color
     * @return styled {@link JButton}
     */
    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(220, 36));
        return btn;
    }
}
