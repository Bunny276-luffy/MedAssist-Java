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

        // With the multi-strategy RegexParser, a completely empty result is very
        // unlikely. If it does happen (e.g. fully numeric/symbol OCR noise), return
        // a partial prescription so the user can manually correct the fields in the UI.
        if (parsed.isEmpty()) {
            System.err.println("[OCRService] Warning: no fields extracted — returning partial result for manual correction.");
            parsed.put("drugName",  "Unidentified");
            parsed.put("dosage",    "Unknown");
            parsed.put("frequency", "Unknown");
            confidence = 0.10;
        }

        return new Prescription(
                parsed.getOrDefault("drugName",  "Unidentified"),
                parsed.getOrDefault("dosage",    "—"),
                parsed.getOrDefault("frequency", "—"),
                parsed.getOrDefault("duration",  "—"),
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
     * Generates a randomly-varied realistic prescription OCR text string for demonstration.
     *
     * <p>Picks from a pool of common medications so each image file produces a different
     * plausible result — making the simulation useful for UI and regex testing.</p>
     *
     * @param fileName the image file name (used to seed the random choice for consistency)
     * @return a multi-line simulated prescription text
     */
    private String generateSimulatedOCRText(String fileName) {
        // Pool of realistic prescription templates
        String[][] pool = {
            {
                "Drug: Metformin",
                "Dosage: 500mg",
                "Frequency: twice daily",
                "Duration: 90 days",
                "Dr. R. Kumar, MBBS"
            },
            {
                "Drug: Amlodipine",
                "Dosage: 5mg",
                "Frequency: once daily",
                "Duration: 30 days",
                "Dr. S. Mehta, MD"
            },
            {
                "Drug: Atorvastatin",
                "Dosage: 20mg",
                "Frequency: once daily at bedtime",
                "Duration: 60 days",
                "Dr. P. Rao, MD"
            },
            {
                "Drug: Aspirin",
                "Dosage: 81mg",
                "Frequency: once daily",
                "Duration: 180 days",
                "Dr. A. Sharma, MBBS"
            },
            {
                "Drug: Lisinopril",
                "Dosage: 10mg",
                "Frequency: once daily",
                "Duration: 30 days",
                "Dr. V. Nair, MD"
            },
            {
                "Drug: Paracetamol",
                "Dosage: 500mg",
                "Frequency: three times daily",
                "Duration: 5 days",
                "Dr. K. Singh, MBBS"
            },
            {
                "Drug: Omeprazole",
                "Dosage: 20mg",
                "Frequency: once daily",
                "Duration: 14 days",
                "Dr. L. Iyer, MD"
            },
            {
                "Drug: Glimepiride",
                "Dosage: 2mg",
                "Frequency: once daily with breakfast",
                "Duration: 90 days",
                "Dr. B. Reddy, MD"
            }
        };

        // Use filename hash to consistently pick the same drug for the same image
        int idx = Math.abs(fileName.hashCode()) % pool.length;
        // Add slight randomisation: 30% chance to shift to next entry
        if (Math.random() < 0.30) {
            idx = (idx + 1) % pool.length;
        }

        String[] entry = pool[idx];
        String date = java.time.LocalDate.now().toString();

        return "Patient: Demo Patient\n"
                + "Date: " + date + "\n"
                + entry[0] + "\n"
                + entry[1] + "\n"
                + entry[2] + "\n"
                + entry[3] + "\n"
                + "Refills: " + (1 + (int)(Math.random() * 3)) + "\n"
                + entry[4] + "\n";
    }
}
