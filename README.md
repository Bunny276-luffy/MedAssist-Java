<div align="center">

# 💊 MedAssist — AI Medication Adherence System

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9.6-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Swing](https://img.shields.io/badge/UI-Java%20Swing-4A90D9?style=for-the-badge&logo=java&logoColor=white)
![Platform](https://img.shields.io/badge/Platform-Windows-0078D6?style=for-the-badge&logo=windows&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

**A premium desktop application that helps elderly and visually impaired patients manage medication schedules using voice commands, OCR prescription scanning, and intelligent reminders — built with Java SE 17 and Swing.**

[Features](#-features) · [Tech Stack](#-tech-stack) · [Getting Started](#-getting-started) · [Project Structure](#-project-structure) · [OOP Concepts](#-oop-concepts-implemented) · [Configuration](#-configuration) · [Screenshots](#-screenshots)

</div>

---

## 🩺 Problem Statement

Medication non-adherence is one of the leading causes of preventable hospitalisation, especially among elderly patients managing multiple chronic conditions. Caregivers often cannot monitor real-time compliance, and patients struggle to remember complex schedules.

**MedAssist** solves this by providing:
- Automated reminders at the exact scheduled time
- Voice-driven commands so even users with low literacy can interact
- Automatic caregiver escalation when critical doses are missed
- OCR-based prescription scanning to eliminate manual data entry

---

## ✨ Features

| Feature | Description |
|---|---|
| 💊 **Medication Dashboard** | Track all medications with status badges (Taken / Pending / Missed / Critical) |
| 📊 **Adherence Stats** | Live stat cards showing Total / Taken / Pending / Critical counts + daily adherence % |
| 🎙️ **Voice Commands** | Natural language input: *"add Aspirin 100mg at 9am"*, *"mark Metformin as taken"* |
| 🔴 **Microphone Recording** | Real Windows Speech Recognition via `System.Speech` — no external API needed |
| 📋 **Review & Confirm** | Post-recognition dialog shows what was heard + alternates so you can correct before processing |
| 🔬 **OCR Scan** | Upload a prescription image → auto-extracts Drug Name, Dosage, Frequency via Tess4J |
| 🖼️ **Image Preview** | Scaled thumbnail of selected prescription shown before OCR runs |
| 🔔 **Smart Reminders** | Activity-aware scheduler — delays reminders if patient is detected as inactive |
| 📧 **Caregiver Escalation** | Sends HTML email alerts to caregiver when critical medications are missed |
| 🌐 **Multilingual** | Responses in English, Telugu (తెలుగు), and Hindi (हिंदी) |
| ➕ **Add Medication** | Fully styled dark-theme dialog with frequency dropdown, validation, and critical flag |
| 🗑️ **Remove Medication** | Confirmation-guarded delete from schedule |
| ℹ️ **About Dialog** | App info panel accessible from the header at all times |
| 💾 **Persistence** | Patient data and dose logs saved to disk automatically |

---

## 🛠 Tech Stack

| Layer | Technology | Purpose |
|---|---|---|
| **Language** | Java SE 17 (LTS) | Core application logic |
| **UI Framework** | Java Swing (custom-painted) | Dark premium UI — no external widget libraries |
| **Build Tool** | Apache Maven 3.9 | Dependency management, compile, run |
| **OCR Engine** | Tess4J 5.8 (Tesseract wrapper) | Prescription image text extraction |
| **Email** | JavaMail 1.6.2 | HTML caregiver alert emails via SMTP |
| **Speech** | Windows `System.Speech` (.NET) | Microphone speech-to-text via PowerShell bridge |
| **Persistence** | Java Serialization + flat-file I/O | Patient data and dose logs |
| **NLP** | Java Regex (`java.util.regex`) | Natural language command parsing |
| **Concurrency** | `SwingWorker` + `ScheduledExecutorService` | Non-blocking UI during OCR, voice, and reminders |

---

## 📐 OOP Concepts Implemented

| OOP Concept | Where Implemented | Details |
|---|---|---|
| **Encapsulation** | All model classes | `Medication`, `Patient`, `Caregiver` — all fields `private`, accessed via getters/setters |
| **Inheritance** | `ReminderService extends BaseReminder` | `BaseReminder` is an abstract class with `scheduleReminder()` template |
| **Polymorphism** | `List<Notifiable>` in `ReminderService` | Both `ReminderService` and `EscalationService` implement `Notifiable`; dispatched polymorphically |
| **Abstraction** | `Notifiable`, `SpeechCapable` interfaces | Define contracts for notification and speech without exposing implementation |
| **Custom Exceptions** | `exception/` package | `MedicationNotFoundException`, `OCRFailureException`, `EscalationException` |
| **Generics** | Throughout services and utils | `ArrayList<Medication>`, `Map<String, String>` from `RegexParser`, `SwingWorker<T, V>` |
| **Multithreading** | `ReminderService`, `VoicePanel`, `ScanPanel` | `ScheduledExecutorService` for reminders, `SwingWorker` for OCR/voice, `ExecutorService` for speech |
| **File I/O** | `FileStorageUtil` | Java serialization for patient data; `FileWriter` append for dose and escalation logs |
| **Javadoc** | All 30 source files | Every class and public method fully documented |

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Version | Download |
|---|---|---|
| JDK (Temurin) | 17 LTS | [adoptium.net](https://adoptium.net) |
| Apache Maven | 3.9+ | [maven.apache.org](https://maven.apache.org) |
| Windows OS | 10 / 11 | Required for `System.Speech` voice recognition |

> **Note:** Tesseract OCR is optional. Without it, the Scan panel runs in simulation mode automatically.

### 1 — Clone the Repository

```bash
git clone https://github.com/Bunny276-luffy/MedAssist-Java.git
cd MedAssist-Java
```

### 2 — Set JAVA_HOME (if needed)

```powershell
# Run this in PowerShell if 'mvn' can't find Java
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
```

### 3 — Compile

```powershell
mvn compile
```

### 4 — Run

```powershell
mvn exec:java
```

> ⚠️ Run `compile` and `exec:java` as **separate commands** — PowerShell does not support `&&`.

---

## 📁 Project Structure

```
medassist/
├── pom.xml                               # Maven project descriptor
├── mvnw.cmd                              # Maven Wrapper (no install required)
├── README.md
├── MEDASSIST_PROJECT_LOG.txt             # Full development log
│
└── src/main/java/com/medassist/
    │
    ├── MedAssistApp.java                 # Entry point — bootstraps UI & services
    │
    ├── model/                            # Data model layer (Encapsulation)
    │   ├── Medication.java
    │   ├── MedicationStatus.java         # Enum: PENDING / TAKEN / MISSED / SKIPPED
    │   ├── Patient.java
    │   ├── Caregiver.java
    │   ├── Prescription.java             # OCR result container
    │   ├── DoseLog.java                  # Audit record per dose event
    │   └── Language.java                 # Enum: ENGLISH / TELUGU / HINDI
    │
    ├── service/                          # Business logic layer (Inheritance, Polymorphism)
    │   ├── Notifiable.java               # Interface — sendNotification(), escalate()
    │   ├── BaseReminder.java             # Abstract class — scheduleReminder() template
    │   ├── ReminderService.java          # Extends BaseReminder, implements Notifiable
    │   ├── EscalationService.java        # Implements Notifiable — email caregiver
    │   ├── OCRService.java               # Tess4J OCR with simulation fallback
    │   └── ActivityMonitor.java          # Simulates patient activity detection
    │
    ├── voice/                            # Voice & NLP layer
    │   ├── SpeechCapable.java            # Interface — speak(), listen(), parseCommand()
    │   ├── LanguagePack.java             # Multilingual string repository
    │   ├── VoiceEngine.java              # NLP regex engine (implements SpeechCapable)
    │   └── WindowsSpeechRecognizer.java  # Windows Speech API bridge via PowerShell
    │
    ├── ui/                               # Presentation layer (Swing)
    │   ├── MainFrame.java                # Root window — gradient header, tab bar, About dialog
    │   ├── MedicationPanel.java          # Dashboard — stat cards, table, Add/Remove/Mark dialogs
    │   ├── ScanPanel.java                # OCR scan workflow with image preview
    │   ├── VoicePanel.java               # Voice command input, recording, review dialog
    │   └── CaregiverPanel.java           # Caregiver info and escalation log
    │
    ├── exception/                        # Custom checked exceptions
    │   ├── MedicationNotFoundException.java
    │   ├── OCRFailureException.java
    │   └── EscalationException.java
    │
    └── util/                             # Utilities and constants
        ├── AppConstants.java             # App-wide constants, CMD_* tokens, SMTP config
        ├── DateTimeUtil.java             # Time formatting helpers
        ├── RegexParser.java              # OCR text → structured medication fields
        └── FileStorageUtil.java          # Serialization, DoseLog, EscalationLog I/O
```

**Generated at runtime:**
```
data/
├── patient.dat             # Serialized Patient object
├── dose_log.txt            # Append-only dose audit trail
└── escalation_log.txt      # Append-only caregiver alert log
```

---

## 🎙️ Voice Command Reference

```
add <drug> <dosage> at <time>          → Add new medication to schedule
add <drug> <dosage> at <time> <freq>   → Add with specific frequency

mark <drug> as taken                   → Mark dose as taken
I took my <drug>                       → Mark dose as taken (alternate phrasing)
taken                                  → Marks first PENDING medication

I missed my <drug> dose                → Log a missed dose
missed <drug>                          → Log missed dose (short form)

schedule <drug> at <time>              → Reschedule medication to new time

scan prescription                      → Navigate to Scan Rx tab
```

**Voice Examples:**
```
"add Paracetamol 500mg at 8am once daily"
"mark Aspirin as taken"
"I missed my Amlodipine dose"
"schedule Metformin at 7:30am"
```

---

## ⚙️ Configuration

### SMTP Email (Caregiver Alerts)

Edit `AppConstants.java`:

```java
public static final String SENDER_EMAIL    = "your-email@gmail.com";
public static final String SENDER_PASSWORD = "your-app-password";  // NOT your real password
```

> 1. Enable 2-Factor Authentication on your Gmail account
> 2. Go to **Google Account → Security → App Passwords**
> 3. Create an app password and paste it above

### OCR / Tesseract (Real Prescription Scanning)

1. Download the installer from [UB-Mannheim/tesseract](https://github.com/UB-Mannheim/tesseract/wiki)
2. Install to `C:\Program Files\Tesseract-OCR\`
3. Add to your system `PATH`
4. Place `eng.traineddata` in a `tessdata/` folder inside the project root

> Without Tesseract, OCRService automatically falls back to **simulation mode** — no crash, no config required.

### Windows Speech Recognition (Voice Accuracy)

1. Open **Control Panel → Ease of Access → Speech Recognition**
2. Click **Train your computer to better understand you**
3. Complete the 5-minute session — significantly improves accuracy, especially for medical terms and non-native accents

### Permanent JAVA_HOME (Avoid setting every session)

1. Open **System Properties → Advanced → Environment Variables**
2. Add `JAVA_HOME` = `C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot`
3. Add `%JAVA_HOME%\bin` to your `Path`

---

## 📸 Screenshots

> _Screenshots will be added here after first deployment._

| Screen | Preview |
|---|---|
| Medication Dashboard | _(coming soon)_ |
| Add Medication Dialog | _(coming soon)_ |
| Voice Panel — Recording | _(coming soon)_ |
| Voice Review Dialog | _(coming soon)_ |
| Scan Rx — Image Preview | _(coming soon)_ |
| Caregiver Panel | _(coming soon)_ |
| About Dialog | _(coming soon)_ |

---

## 🗺️ Roadmap

- [ ] Persistent `JAVA_HOME` setup script (`setup.bat`)
- [ ] JAR distribution with embedded launcher (`mvn package`)
- [ ] Real Tesseract OCR integration (auto-detect installation path)
- [ ] Drug interaction warnings using an open medication API
- [ ] Exportable adherence reports (PDF via Apache PDFBox)
- [ ] Android companion app for caregiver notifications

---

## 👨‍💻 Authors

| Name | Role |
|---|---|
| MedAssist Team | Design, Architecture, Implementation |

> This project was developed as part of an **Advanced Java Programming** course assignment demonstrating real-world application of OOP principles, multithreading, file I/O, and GUI design using Java SE 17.

---

## 📄 License

```
MIT License

Copyright (c) 2026 MedAssist Team

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

<div align="center">

Made with ❤️ using Java SE 17 · Swing · Maven

⭐ Star this repo if it helped you!

</div>
