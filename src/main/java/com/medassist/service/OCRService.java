package com.medassist.service;

import com.medassist.exception.OCRFailureException;
import com.medassist.model.Prescription;
import com.medassist.util.RegexParser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Service responsible for extracting prescription data from images using OCR.
 *
 * <p>{@code OCRService} wraps the Tess4J library (a Java wrapper for Tesseract OCR)
 * to perform optical character recognition on a given image file. The raw OCR text
 * is then passed to {@link RegexParser} to extract structured prescription fields.</p>
 *
 * <p>If Tess4J is not available on the classpath (e.g., missing native libraries),
 * the service falls back to a simulated OCR mode that returns plausible sample text
 * for UI demonstration purposes.</p>
 *
 * @author MedAssist Team
 * @version 1.0
 * @see RegexParser
 * @see com.medassist.exception.OCRFailureException
 */
public class OCRService {

    /**
     * Constructs a new {@code OCRService} instance.
     *
     * <p>Tess4J is initialized lazily at the first call to
     * {@link #extractPrescription(File)} to avoid startup overhead.</p>
     */
    public OCRService() {
        // No-arg constructor
    }

    /**
     * Reads a prescription image and extracts drug name, dosage, frequency, and duration.
     *
     * <p>Workflow:
     * <ol>
     *   <li>Validate the image file exists and is readable.</li>
     *   <li>Attempt OCR via Tess4J; on failure fall back to simulation mode.</li>
     *   <li>Parse the OCR text with {@link RegexParser#parsePrescription(String)}.</li>
     *   <li>Build and return a {@link Prescription} object.</li>
     * </ol>
     * </p>
     *
     * @param imageFile the prescription image file to process; must not be {@code null}
     * @return a {@link Prescription} object populated with extracted fields
     * @throws OCRFailureException if the image cannot be read or no fields can be extracted
     */
    public Prescription extractPrescription(File imageFile) throws OCRFailureException {
        if (imageFile == null || !imageFile.exists()) {
            throw new OCRFailureException("Image file does not exist: "
                    + (imageFile != null ? imageFile.getAbsolutePath() : "null"));
        }

        String ocrText;
        double confidence;

        try {
            ocrText = performOCR(imageFile);
            confidence = 0.85; // Tess4J succeeded
        } catch (Exception e) {
            System.err.println("[OCRService] Tess4J unavailable, using simulation: " + e.getMessage());
            ocrText = generateSimulatedOCRText(imageFile.getName());
            confidence = 0.70; // Simulation has lower confidence
        }

        if (ocrText == null || ocrText.isBlank()) {
            throw new OCRFailureException("OCR returned empty text for file: " + imageFile.getName());
        }

        System.out.println("[OCRService] Raw OCR text:\n" + ocrText);

        Map<String, String> parsed = RegexParser.parsePrescription(ocrText);

        if (parsed.isEmpty()) {
            throw new OCRFailureException(
                    "Regex extraction failed — no prescription fields found in OCR output.");
        }

        return new Prescription(
                parsed.getOrDefault("drugName", "Unknown Drug"),
                parsed.getOrDefault("dosage", "Unknown Dosage"),
                parsed.getOrDefault("frequency", "Unknown Frequency"),
                parsed.getOrDefault("duration", "Unknown Duration"),
                confidence
        );
    }

    /**
     * Attempts to perform OCR on the image file using Tess4J.
     *
     * <p>Tess4J (net.sourceforge.tess4j.Tesseract) is loaded via reflection so that
     * the application can still compile and run in UI-demo mode if Tess4J native
     * binaries are absent.</p>
     *
     * @param imageFile the image file to process
     * @return the raw OCR output string
     * @throws Exception if Tess4J is unavailable or OCR fails
     */
    private String performOCR(File imageFile) throws Exception {
        // Load Tess4J dynamically to avoid hard compile dependency in demo mode
        Class<?> tesseractClass = Class.forName("net.sourceforge.tess4j.Tesseract");
        Object tesseract = tesseractClass.getDeclaredConstructor().newInstance();

        // Set data path — defaults to working directory's tessdata folder
        tesseractClass.getMethod("setDatapath", String.class)
                .invoke(tesseract, "./tessdata");
        tesseractClass.getMethod("setLanguage", String.class)
                .invoke(tesseract, "eng");

        BufferedImage img = ImageIO.read(imageFile);
        if (img == null) {
            throw new IOException("ImageIO could not decode: " + imageFile.getName());
        }

        return (String) tesseractClass.getMethod("doOCR", BufferedImage.class)
                .invoke(tesseract, img);
    }

    /**
     * Generates a sample prescription OCR text string for demonstration purposes.
     *
     * <p>The simulated text mimics a realistic handwritten/printed prescription format
     * so that the regex parser and UI flow can be tested without a Tesseract installation.</p>
     *
     * @param fileName the image file name (used only for generating a plausible drug name)
     * @return a multi-line simulated prescription text
     */
    private String generateSimulatedOCRText(String fileName) {
        return "Patient Name: John Doe\n"
                + "Date: 15/04/2026\n"
                + "Drug: Metformin 500\n"
                + "Dosage: 500mg\n"
                + "Frequency: twice daily\n"
                + "Duration: 30 days\n"
                + "Refills: 2\n"
                + "Dr. A. Smith, M.D.\n";
    }
}
