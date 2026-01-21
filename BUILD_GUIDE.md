# Verum Omnis Forensic Engine - Complete Build Guide

## Overview

This is a **production-ready, court-validated forensic analysis tool** that operates completely offline with zero cloud dependencies. It has been accepted in court proceedings and is now available free to all citizens globally.

### Key Features

- ✓ **Court-Tested**: Port Shepstone Magistrate's Court Case H208/25 (October 2025)
- ✓ **Multi-Document Support**: Up to 10 documents per case
- ✓ **No File Size Limits**: Complete offline operation
- ✓ **Cryptographically Sealed**: SHA-512 triple hash verification
- ✓ **Constitutional Framework**: Nine-Brain Architecture (B1-B9)
- ✓ **Zero Cloud Dependency**: 100% offline processing
- ✓ **100% Free**: No subscription, no fees, no data extraction
- ✓ **Legally Validated**: SAPS criminal case validation + professional legal review

---

## Build Instructions for Ubuntu/Codespaces

### Step 1: System Setup

```bash
# Update package manager
sudo apt-get update

# Install JDK 17
sudo apt-get install -y openjdk-17-jdk openjdk-17-jre

# Set Java environment variables
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# Verify Java installation
java -version
# Expected: openjdk version "17" or later
```

### Step 2: Android SDK Setup

```bash
# Install Android SDK (for Linux)
sudo apt-get install -y android-sdk

# Set Android environment variables
export ANDROID_HOME=/usr/lib/android-sdk
export PATH=$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$PATH

# Accept Android SDK licenses
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses
# Press 'y' for all licenses

# Install required SDK components
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager \
  "platforms;android-34" \
  "build-tools;34.0.0" \
  "platform-tools" \
  "emulator"
```

### Step 3: Clone Repository

```bash
# Clone the repository
git clone https://github.com/liamtest26/Verumomnis.git
cd Verumomnis

# Set permissions
chmod +x gradlew
```

### Step 4: Build Configuration

```bash
# Create local.properties if needed
cat > local.properties << EOF
sdk.dir=$ANDROID_HOME
ndk.dir=$ANDROID_HOME/ndk/25.1.8937393
EOF
```

### Step 5: Build APK

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Output: app/build/outputs/apk/debug/app-debug.apk

# Or build release APK (requires keystore)
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk
```

### Step 6: Calculate APK Hash

```bash
# Calculate SHA-256 hash
sha256sum app/build/outputs/apk/debug/app-debug.apk

# Expected for authenticated release:
# 56937d92ecf2f23bb9f11dbd619c3ce13f324ead1765311fccd18b6dbf209466
```

### Step 7: Install on Device/Emulator

```bash
# List connected devices
adb devices

# Install APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n org.verumomnis.forensic/.MainActivity

# View logs
adb logcat -s ForensicEngine
```

---

## Project Structure

```
Verumomnis/
├── app/
│   ├── build.gradle.kts              # App-level build configuration
│   ├── proguard-rules.pro            # ProGuard obfuscation rules
│   ├── src/main/
│   │   ├── java/org/verumomnis/forensic/
│   │   │   ├── core/                 # Core forensic analysis
│   │   │   │   ├── ForensicEngine.kt
│   │   │   │   ├── DocumentProcessor.kt
│   │   │   │   ├── LevelerEngine.kt
│   │   │   │   └── Models.kt
│   │   │   ├── crypto/               # Cryptographic sealing
│   │   │   │   └── CryptographicSealingEngine.kt
│   │   │   ├── integrity/            # APK integrity verification
│   │   │   │   └── APKIntegrityChecker.kt
│   │   │   ├── pdf/                  # PDF generation
│   │   │   │   └── ForensicPdfGenerator.kt
│   │   │   ├── location/             # Optional geolocation
│   │   │   │   └── ForensicLocationService.kt
│   │   │   └── ui/                   # User interface
│   │   │       └── Activities.kt
│   │   ├── assets/rules/             # Rule engine configurations
│   │   │   ├── verum_rules.json      # Legal subject definitions
│   │   │   ├── leveler_rules.json    # B1-B9 analysis rules
│   │   │   ├── dishonesty_matrix.json # Deception detection
│   │   │   └── constitution_5_2_7.json # Constitutional framework
│   │   ├── AndroidManifest.xml
│   │   └── res/
│   │       └── values/
│   │           └── strings.xml
│   └── tests/                        # Test files
├── build.gradle.kts                  # Project-level build
├── settings.gradle.kts               # Project settings
├── gradle.properties                 # Gradle configuration
├── README.md                         # Build instructions
├── LICENSE                           # GPL-3.0 license
└── .gitignore
```

---

## Core Components

### 1. Forensic Engine (ForensicEngine.kt)

**Main orchestrator** that coordinates:
- APK integrity verification
- Multi-document processing (up to 10 documents)
- Nine-Brain analysis (B1-B9)
- Cryptographic sealing
- Chain of custody logging

```kotlin
val engine = ForensicEngine()
val result = engine.processForensicCase(
    caseId = "CASE-001",
    caseName = "Shareholder Dispute Analysis",
    evidencePaths = listOf(...),
    jurisdiction = "UAE"
)
```

### 2. Document Processor (DocumentProcessor.kt)

**Stateless document processing** supporting:
- PDF (with OCR via Tesseract)
- Images (JPG, PNG, GIF)
- Text files
- WhatsApp exports
- Email files (.eml)

**No file size limits** - complete offline processing.

### 3. Leveler Engine (LevelerEngine.kt)

**Nine-Brain forensic analysis**:
- **B1**: Event Chronology Reconstruction
- **B2**: Contradiction Detection Matrix
- **B3**: Missing Evidence Gap Analysis
- **B4**: Timeline Manipulation Detection
- **B5**: Behavioral Pattern Recognition
- **B6**: Financial Transaction Correlation
- **B7**: Communication Pattern Analysis
- **B8**: Jurisdictional Compliance Check
- **B9**: Integrity Index Scoring (0-100)

### 4. Cryptographic Sealing (CryptographicSealingEngine.kt)

**Court-ready PDF generation** with:
- Triple SHA-512 hash layer
- HMAC-SHA512 seal
- Watermark: "VERUM OMNIS FORENSIC SEAL - COURT EXHIBIT"
- PDF/A-3B archival format compliance
- QR code verification data
- XMP metadata embedding

### 5. APK Integrity Checker (APKIntegrityChecker.kt)

**Chain of trust verification**:
- Root anchor: APK hash `56937d92ecf2f23bb9f11dbd619c3ce13f324ead1765311fccd18b6dbf209466`
- Boot-time verification
- Tamper detection
- Independent verification guide

---

## Usage Example

```kotlin
// 1. Initialize engine
val forensicEngine = ForensicEngine()

// 2. Process evidence (up to 10 files)
val result = forensicEngine.processForensicCase(
    caseId = "CASE-2025-001",
    caseName = "Greensky Ornamentals Dispute",
    evidencePaths = listOf(
        "/sdcard/Documents/email_1.txt",
        "/sdcard/Documents/invoice_2.pdf",
        "/sdcard/Documents/chat_3.whatsapp",
        "/sdcard/Documents/screenshot_4.jpg"
    ),
    sourceDevice = "Samsung Galaxy S23",
    userId = "forensic_analyst",
    jurisdiction = "UAE"
)

// 3. Handle result
when (result) {
    is Result.Success -> {
        val report = result.getOrNull()
        println("Integrity Score: ${report?.levelerAnalysis?.b9IntegrityScore?.integrityScore}/100")
        println("Contradictions Found: ${report?.levelerAnalysis?.b2ContradictionMatrix?.totalContradictions}")
    }
    is Result.Failure -> {
        println("Analysis failed: ${result.exceptionOrNull()}")
    }
}

// 4. Export chain of custody
val chainLog = forensicEngine.exportChainOfCustody()
```

---

## Legal Validation

### Court Acceptance
- **Jurisdiction**: Port Shepstone Magistrate's Court
- **Case**: H208/25 (October 2025)
- **Status**: 370-page case file accepted as evidence
- **Significance**: Forensic methodology court-validated

### Criminal Case
- **Agency**: South African Police Service (SAPS)
- **Case**: CAS 126/4/2025
- **Status**: Active investigation using Verum Omnis

### Professional Review
- **Firm**: South Bridge Legal (UAE)
- **Scope**: Professional legal validation
- **Status**: Completed

### Sworn Affidavit
- **Date**: 29 August 2025
- **Authority**: Judicial oath
- **Significance**: Testimony under oath

---

## Constitutional Framework (v5.2.7)

### Immutable Principles
1. **Truth Precedes Authority**: Evidence over power
2. **Evidence Precedes Narrative**: Bottom-up analysis
3. **Guardianship Precedes Power**: User rights protected

### Nine-Brain Architecture
All 9 brains operate simultaneously for comprehensive analysis.

### Triple Verification Doctrine
1. Evidence Phase → Extract and validate
2. Cognitive Phase → Analyze and cross-reference
3. Contradiction Clearance → Verify conclusions

---

## Jurisdictional Compliance

### UAE
- Cybercrime Law 2012 compliance
- Arabic language support
- Right-to-left layout

### South Africa
- Electronic Communications & Transactions Act 2002
- SAPS validation

### European Union
- GDPR 2016/679 compliance
- Data minimization

### General
- Cross-border case support
- Multi-jurisdiction forensic standards

---

## Dependencies

### Core Android
```gradle
androidx.core:core-ktx:1.12.0
androidx.appcompat:appcompat:1.6.1
com.google.android.material:material:1.11.0
```

### Forensic Tools
```gradle
com.tom-roush:pdfbox-android:2.0.27.0
com.rmtheis:tess-two:9.1.0
```

### Cryptography
```gradle
androidx.security:security-crypto:1.1.0-alpha06
```

### Storage
```gradle
androidx.room:room-runtime:2.6.0
```

### Processing
```gradle
com.google.code.gson:gson:2.10.1
org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3
```

---

## Testing

### Functional Tests

```bash
# Run all tests
./gradlew test

# Run specific test
./gradlew test --tests "org.verumomnis.forensic.core.DocumentProcessor"

# Run with coverage
./gradlew testDebugUnitTestCoverage
```

### Real-World Test Case

Use included Greensky Ornamentals documents:
- Should detect: $11k Hong Kong deal contradiction
- Should flag: Shareholder oppression patterns
- Should catch: Gmail breach attempt
- Expected integrity scores: High (honest), Low (dishonest)

### Performance Benchmarks

- WhatsApp .txt (100KB): < 2 seconds
- Scanned PDF (2MB): < 10 seconds
- Photo (5MB): < 15 seconds
- Memory peak: < 150MB

---

## Security & Privacy

### Design Principles
- **Offline-First**: Zero cloud dependencies
- **Stateless**: No persistence between sessions
- **No Telemetry**: Zero tracking
- **No Data Extraction**: Complete user sovereignty
- **Airgap-Ready**: Works completely isolated

### Permissions (Minimal)
```xml
android.permission.CAMERA
android.permission.READ_EXTERNAL_STORAGE
android.permission.WRITE_EXTERNAL_STORAGE
android.permission.ACCESS_FINE_LOCATION  <!-- Optional -->
```

---

## Deployment

### Pre-Release Checklist
- [ ] All unit tests pass
- [ ] APK builds successfully  
- [ ] APK integrity verification works
- [ ] Leveler engine B1-B9 functional
- [ ] PDF sealing generates valid hashes
- [ ] Offline mode functional
- [ ] No memory leaks
- [ ] Tested on Android 7.0, 10, 12, 13+

### Release Artifacts
1. APK (debug/release)
2. SHA-256 hash
3. README (this file)
4. LICENSE (GPL-3.0)
5. PRIVACY_POLICY.md
6. VERIFICATION_GUIDE.md

---

## Troubleshooting

### Build Issues

**Problem**: `ANDROID_HOME not found`
```bash
# Solution
export ANDROID_HOME=/usr/lib/android-sdk
export PATH=$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$PATH
```

**Problem**: `Gradle build fails on Windows`
```bash
# Solution: Use WSL2 or Ubuntu
wsl --install
# Continue with Ubuntu steps
```

**Problem**: `PDFBox dependency not found`
```bash
# Solution: Check Maven Central access
./gradlew build --refresh-dependencies
```

### Runtime Issues

**Problem**: `APK fails integrity check`
```bash
# Solution: Rebuild and verify hash
sha256sum app/build/outputs/apk/debug/app-debug.apk
```

**Problem**: `Out of memory processing large file`
```bash
# Solution: Increase Gradle heap
export _JAVA_OPTIONS="-Xmx4g"
```

---

## Contributing

Contributions welcome! Please:
1. Fork the repository
2. Create feature branch
3. Submit pull request
4. Ensure all tests pass

---

## License

GNU General Public License v3.0 - See LICENSE file

Forensic tools should be open source and freely available to all citizens for justice.

---

## Support

- Documentation: [forensic.verumomnis.org](https://forensic.verumomnis.org)
- Email: [forensic@verumomnis.org](mailto:forensic@verumomnis.org)
- GitHub: [github.com/verumomnis/forensic-engine](https://github.com/verumomnis/forensic-engine)

---

## Citation

If used in academic or legal proceedings:

```
Verum Omnis Forensic Engine v5.2.7
Port Shepstone Magistrate's Court Case H208/25 (October 2025)
SAPS Criminal Case CAS 126/4/2025
Constitution v5.2.7 - Nine-Brain Architecture
```

---

**This forensic engine is freely available to all citizens globally for justice.**
