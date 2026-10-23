
# AI ScamShield PK

**An AI-assisted digital safety app for identifying potential scams and phishing threats.**

<p align="center">
  <strong>Developed by Team Trident AI</strong>
</p>

AI ScamShield PK is an Android application designed to help people in Pakistan assess suspicious messages, URLs, and screenshots. It combines Gemini-powered analysis with local URL-risk checks and offline fallback logic to present a risk level, supporting indicators, explanations, and recommended next steps.

> **Important:** ScamShield PK provides risk assessments, not guarantees. A low-risk result does not prove that content is safe, and a warning does not independently prove criminal intent. Always verify important requests through an organisation's official channels.

---

## About Team Trident AI

We are **Team Trident AI**, a three-member team working on practical AI-powered software to address real-world digital safety challenges. For this project, we combined Android development, AI integration, cybersecurity-focused analysis, and product testing to build an accessible scam-awareness tool.

---

## Key features

### 1. Message and text analysis
- Analyse submitted messages for potential scam and phishing indicators.
- Designed to handle English, Urdu, Roman Urdu, and mixed-language content through the existing analysis workflow.
- Present a risk score, severity level, possible category, evidence, explanation, and recommended action when available.

### 2. URL risk assessment
- Analyse URLs for structural warning signs, including insecure HTTP, raw IP-address hosts, punycode, URL shorteners, suspicious top-level domains, excessive subdomain depth, and possible impersonation of Pakistani services.
- Combine local heuristic indicators with Gemini analysis when the API is configured and available.
- Use a local fallback when Gemini cannot be called.

**Note:** The URL checks in this project include heuristic rules. Do not interpret them as a guarantee of safety or as proof that a URL belongs to a criminal.

### 3. Screenshot analysis and evidence
- Submit a screenshot for multimodal analysis and text extraction through the existing Gemini-based workflow.
- Review extracted text and interactive evidence indicators that explain why particular phrases may be suspicious.
- Results depend on image readability, available AI services, and the evidence returned by the analysis.

### 4. Contextual AI Scam Assistant
- Ask follow-up questions about a scan result.
- Get simpler explanations of warning signs and suggested next steps.
- Use quick prompts to explore why content was flagged or how to verify a request.
- Use local fallback guidance when the AI service is unavailable, where supported.

### 5. Multilingual explanations
- Choose **English**, **Urdu script**, or **Roman Urdu** for the explanation.
- Make scan findings easier to understand without changing the underlying risk assessment.

### 6. Personal Scam Insights
- View scan totals and risk-level summaries based on locally stored scan history.
- Review recent activity, detected indicators, categories, and input types where data is available.
- See personal patterns from the scans recorded on the device; these are not nationwide scam statistics.

### 7. Scam Awareness Trainer
- Practise identifying suspicious and legitimate-looking messages through sample scenarios.
- Review warning signs and explanations after making a choice.
- Scenarios are simulations for awareness and education, not live threat detections.

### 8. Scan history
- Store and revisit scan records using the app's local Room database.
- Review previous assessments and their available summaries.

### 9. Exportable scan reports
- Generate a PDF report for a scan and share a text summary where supported.
- Choose whether the original submitted content should be included in the report.
- Review the report before sharing it, especially if it contains personal information, phone numbers, or URLs.

### 10. Voice summary and sound feedback
- Listen to a concise English summary of a result using Android text-to-speech.
- Receive short scan-status sound effects, with sound behavior controlled by the app's settings where available.

### 11. Security help and settings
- Access security FAQs and safety guidance.
- Configure available app preferences and theme options.

---

## Technology stack

| Area | Technology |
|---|---|
| Platform | Android |
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Navigation | Navigation Compose |
| AI analysis | Gemini API integration |
| Networking | Retrofit and OkHttp |
| Local persistence | Room database |
| Asynchronous work | Kotlin Coroutines |
| PDF export | Android `PdfDocument` and `FileProvider` |
| Voice output | Android TextToSpeech |
| Sound feedback | Android ToneGenerator |
| Build system | Gradle with Kotlin DSL |

The app's Android configuration in this repository uses **minimum SDK 24**. The configured application ID is `com.aistudio.scamshield.pkzq`; the Gradle namespace is `com.example`.

---

## Project structure

```text
app/src/main/java/com/example/
├── data/
│   ├── local/          # Room database and scan DAO
│   ├── model/          # Scan results, records, indicators, URL features
│   ├── remote/         # Gemini request/response and API service
│   └── repository/     # Analysis, API calls, parsing, and fallbacks
├── engine/
│   ├── UrlRiskAnalyzer.kt
│   ├── TextPreprocessor.kt
│   ├── DemoDataProvider.kt
│   ├── PdfReportGenerator.kt
│   ├── TtsManager.kt
│   └── SoundEffectManager.kt
├── ui/
│   ├── components/     # Shared UI components
│   ├── navigation/     # App navigation routes
│   ├── screens/        # Home, scanner, results, history, insights, etc.
│   ├── theme/          # Theme and colours
│   └── viewmodel/      # UI state and app actions
└── AndroidManifest.xml
```

*This is a high-level guide to the source layout; individual files may evolve as the project changes.*

---

## Getting started

### Requirements

- Android Studio compatible with the project's Gradle and Android Gradle Plugin versions.
- A compatible JDK as required by the Gradle configuration.
- Android SDK components required by the project.
- A Gemini API key for remote AI features.
- An Android emulator or physical Android device for running the app.

### 1. Clone or download the repository

```bash
git clone <YOUR_REPOSITORY_URL>
cd <YOUR_PROJECT_DIRECTORY>
```

Alternatively, download the ZIP and extract the project.

### 2. Open the project

1. Open Android Studio.
2. Choose **Open** and select the project root containing `settings.gradle.kts`.
3. Allow Gradle sync to finish and install any required Android SDK components.

### 3. Configure the Gemini API key

The project includes `.env.example` and uses the Secrets Gradle Plugin configuration. Create a local `.env` file in the project root, following the example:

```dotenv
GEMINI_API_KEY=YOUR_GEMINI_API_KEY
```

Use a valid key from Google's official AI developer platform and follow its current terms and usage limits. Keep `.env` private: **do not commit your real API key to Git or paste it into public issues, screenshots, or documentation.** The repository's `.gitignore` excludes `.env`.

If the key is absent or invalid, the app may use local fallback analysis for supported workflows, but Gemini-dependent explanations and screenshot analysis may be limited or unavailable.

### 4. Build and run

Use Android Studio's **Run** action to build and launch the app on an emulator or connected device.

You can also run Gradle tasks from a terminal in the project root if the Gradle wrapper is present in your copy:

```bash
./gradlew assembleDebug
./gradlew test
```

On Windows Command Prompt, use `gradlew.bat` instead of `./gradlew`. If the wrapper files are not included in the downloaded project, open it in Android Studio or install/configure the appropriate Gradle version before running these commands.

**Signing note:** Review the signing configuration in `app/build.gradle.kts` before preparing a release build. Never publish a release using development credentials or commit signing keys and passwords to the repository. Configure release signing securely using your own credentials and local environment variables.

---

## Privacy and security considerations

- Scan history is stored locally using the app's Room database.
- Gemini-powered analysis requires submitting the relevant content to the configured AI service. Do not submit messages, screenshots, or URLs containing sensitive information unless you are comfortable with that processing.
- Review any report before sharing it, and omit the original content when it is not necessary.
- Never share OTPs, passwords, PINs, recovery codes, or banking credentials with anyone—including in a scan prompt.
- Treat every risk score as an estimate based on available indicators, not a guarantee.
- Keep API keys and release-signing credentials out of source control.

Before a public release, review API-key exposure, privacy disclosures, data handling, current Google Play requirements, accessibility, and real-device behavior. A successful local build alone does not establish production readiness or Google Play compliance.

---

## Limitations

- Detection can produce false positives and false negatives.
- AI-generated explanations may be incomplete or incorrect; compare them with the actual evidence.
- Local fallback analysis is not equivalent to full Gemini-powered multimodal analysis.
- Screenshot results depend on image quality and available processing services.
- The awareness trainer uses sample scenarios and should not be treated as a live threat feed.
- The app does not replace official advice from a bank, payment provider, telecom operator, or cybercrime authority.

---

## Roadmap

Potential future improvements include:

- Broader evaluation using labelled legitimate and malicious examples.
- Documented false-positive and false-negative testing.
- Stronger test coverage for URL analysis, translation, offline behavior, and report generation.
- More robust language-quality evaluation for Urdu and Roman Urdu.
- Further privacy and security review before public distribution.

---

## Contributing

Contributions, bug reports, and practical suggestions are welcome. Before submitting a change:

1. Explain the issue or proposed improvement.
2. Keep changes focused and avoid committing secrets or private scan data.
3. Run the relevant build and tests.
4. Document any new configuration requirements.

If this is a team or academic repository, follow your team's contribution and attribution requirements.

---

## Disclaimer

AI ScamShield PK is an informational digital-safety tool. It cannot guarantee that a message, URL, image, or transaction is safe or fraudulent. Verify sensitive requests through independently obtained official contact details, and never rely solely on an app's risk score when making financial or security decisions.

---

<p align="center">
  <strong>AI ScamShield PK</strong><br />
  Built with care by <strong>Team Trident AI</strong>
</p>
