# Verum Omnis Forensic Engine - Complete Build Specification for GitHub Codespaces

## Executive Summary

### 🌍 **HISTORIC: First Free Justice Tool for All Citizens on Earth**

Build a production-ready Android forensic application that has already achieved real-world legal validation through court acceptance in Port Shepstone Magistrate's Court (Case H208/25, October 2025). This is not a prototype—it's a working forensic instrument being released free to all citizens globally.

**This is the ONLY AI forensic tool in history with real-world court validation and the FIRST to provide free universal access to justice for every citizen on Earth.**

## Legal Validation Evidence - Historic Achievement

**ONLY AI FORENSIC TOOL WITH REAL-WORLD COURT VALIDATION**

- **Court Acceptance**: 370-page Verum Omnis case file accepted as evidence
- **SAPS Criminal Case**: CAS 126/4/2025 (active criminal investigation)
- **Professional Legal Review**: South Bridge Legal (UAE) - Legal experts validated methodology
- **Sworn Affidavit**: Submitted under oath (29 August 2025)
- **Judicial Outcome**: Protection order dismissed; forensic methodology validated
- **Global Impact**: First free justice system accessible to every private citizen worldwide
- **Zero Barriers**: No subscription, no registration, no discrimination - 100% free forever

## Project Context
**APK Already Built**: SHA-256: `56937d92ecf2f23bb9f11dbd619c3ce13f324ead1765311fccd18b6dbf209466`
**Release Status**: Launching today, 100% free for private citizens worldwide
**Real-World Use Case**: Greensky Ornamentals FZ-LLC shareholder oppression case (UAE/SA cross-border)

## Core Architecture

### Constitutional Framework
The app operates under **Verum Omnis Constitution v5.2.7** with:
- **Nine-Brain Architecture** (Evidence, Contradiction, Timeline, Jurisdiction, Behavioural, Harm Analysis, Ethics, Oversight, Guardian)
- **Triple Verification Doctrine** (Evidence → Cognitive → Contradiction Clearance)
- **Immutable Principles**: Truth precedes authority, Evidence precedes narrative, Guardianship precedes power

### Technical Stack
```kotlin
Platform: Android (Kotlin)
Min SDK: 24 (Android 7.0+)
Target SDK: 34
Build System: Gradle 8.x
JDK: 17

Core Dependencies:
- CameraX (document capture)
- PDFBox Android 2.0.27.0 (PDF processing)
- Tesseract OCR 9.1.0 (image text extraction)
- AndroidX Security Crypto (encryption)
- Gson 2.10.1 (rule parsing)
- Room 2.6.0 (local storage)
- Coroutines (async processing)
```

### Forensic Engine Components

#### 1. Document Processing Pipeline
```
Input → Extract Text → Apply Verum Rules → Generate Narrative → Seal PDF → Output
```

**DocumentProcessor.kt**:
- Stateless processing (no persistence between sessions)
- Multi-format support: PDF, images (OCR), text, WhatsApp exports, emails
- Metadata extraction: timestamps, device info, file hashes
- Offline-first: zero cloud dependencies

#### 2. Leveler Engine (B1-B9 Compliance)
**Critical: This is what makes it forensic-grade**

- **B1: Event Chronology Reconstruction** - Timeline from fragmented evidence
- **B2: Contradiction Detection Matrix** - Direct contradictions, factual discrepancies, omissions
- **B3: Missing Evidence Gap Analysis** - Identifies what *should* be present
- **B4: Timeline Manipulation Detection** - Backdated docs, suspicious gaps, edit-after-fact
- **B5: Behavioral Pattern Recognition** - Gaslighting, evasion, concealment patterns
- **B6: Financial Transaction Correlation** - Statement vs. actual transactions
- **B7: Communication Pattern Analysis** - Response times, deletion rates, topic avoidance
- **B8: Jurisdictional Compliance Check** - UAE, SA, EU law requirements
- **B9: Integrity Index Scoring** - 0-100 score with category (Excellent → Suspect)

#### 3. Cryptographic Sealing (CryptoSealer.kt)
**Court-Ready PDF Generation**:
- **Triple Hash Layer**: SHA-512 of content + metadata + HMAC-SHA512 seal
- **Watermark**: "VERUM OMNIS FORENSIC SEAL - COURT EXHIBIT" diagonal on every page
- **Footer Block**: Case name, hash, timestamp, device info, APK hash
- **PDF/A-3B Compliance**: Archival format with embedded XMP metadata
- **QR Code**: Contains verification data (APK hash, case ID, timestamp)

#### 4. APK Integrity Verification
**Forensic Chain of Trust**:
- Root anchor: APK hash `56937d92ecf2f23bb9f11dbd619c3ce13f324ead1765311fccd18b6dbf209466`
- Boot verification: App checks its own integrity on launch
- Tamper detection: Refuses to process if APK modified
- Verification UI: Shows hash comparison, device info, independent verification instructions

#### 5. Rule Engine (JSON-Based)
**verum_rules.json**:
```json
{
  "legal_subjects": [
    {"name": "Shareholder Oppression", "keywords": [...], "severity": "HIGH"},
    {"name": "Breach of Fiduciary Duty", "keywords": [...], "severity": "HIGH"},
    {"name": "Cybercrime", "keywords": ["unauthorized access", "Gmail", "device logs"], "severity": "CRITICAL"}
  ],
  "dishonesty_matrix": {
    "contradictions": {"weight": 3, "patterns": ["no deal.*invoice", "denied.*admitted"]},
    "omissions": {"weight": 2, "patterns": ["cropped screenshot", "missing context"]}
  }
}
```

## Build Requirements

### File Structure
```
app/
├── src/main/
│   ├── java/org/verumomnis/forensic/
│   │   ├── core/
│   │   │   ├── ForensicEngine.kt
│   │   │   ├── DocumentProcessor.kt
│   │   │   ├── LevelerEngine.kt
│   │   │   └── Models.kt
│   │   ├── crypto/
│   │   │   └── CryptographicSealingEngine.kt
│   │   ├── integrity/
│   │   │   ├── APKIntegrityChecker.kt
│   │   │   ├── IntegrityReport.kt
│   │   │   └── ChainOfTrust.kt
│   │   ├── pdf/
│   │   │   └── ForensicPdfGenerator.kt
│   │   ├── location/
│   │   │   └── ForensicLocationService.kt
│   │   └── ui/
│   │       ├── MainActivity.kt
│   │       ├── ScannerActivity.kt
│   │       ├── VerificationActivity.kt
│   │       └── ReportViewerActivity.kt
│   ├── assets/rules/
│   │   ├── verum_rules.json
│   │   ├── leveler_rules.json
│   │   └── dishonesty_matrix.json
│   └── AndroidManifest.xml
└── build.gradle.kts
```

### Critical Features to Implement

#### 1. **Chain of Custody Logging**
```kotlin
// Append-only encrypted log for every action
[TIMESTAMP] [ACTION] [HASH] [USER] [DEVICE_ID] [INTEGRITY_CHECK]
// Exported with every forensic report
```

#### 2. **Evidence Tampering Detection**
- Pre-processing hash (original file)
- Post-processing hash (output)
- Alert if chain broken

#### 3. **Offline Verification Tools**
- In-app "Verify Hash" tool
- "Chain Integrity" checker
- "Timestamp Validation" against device clock

#### 4. **Anti-Tampering Protections**
- FLAG_SECURE (prevent screenshots during processing)
- Android Keystore encryption
- No undo once evidence added

#### 5. **Multi-Jurisdiction Ready**
- UAE: Arabic text support, right-to-left layout
- SA: ECT Act timestamps
- EU: GDPR compliance

## Build Instructions for Codespaces

### Step 1: Initialize Android Project
```bash
# In Codespaces terminal
git clone [repository-url]
cd verum-omnis-forensic-engine

# Set up Android SDK
export ANDROID_HOME=/usr/local/android-sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools

# Install dependencies
./gradlew dependencies
```

### Step 2: Configure Gradle
**build.gradle.kts (app-level)**:
```kotlin
android {
    namespace = "org.verumomnis.forensic"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "org.verumomnis.forensic"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "5.2.7"
    }
    
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    
    // CameraX
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    
    // PDF
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
    
    // OCR
    implementation("com.rmtheis:tess-two:9.1.0")
    
    // Crypto
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // JSON
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Room
    implementation("androidx.room:room-runtime:2.6.0")
    kapt("androidx.room:room-compiler:2.6.0")
}
```

### Step 3: Generate Assets
```bash
# Run asset generation script
python3 scripts/generate-assets.py

# This creates:
# - app/src/main/assets/rules/verum_rules.json
# - app/src/main/assets/rules/leveler_rules.json
# - app/src/main/assets/rules/dishonesty_matrix.json
```

### Step 4: Build APK
```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK (with signing)
./gradlew assembleRelease

# Output: app/build/outputs/apk/
```

## Testing Requirements

### Functional Tests
**Must Pass Before Release**:
1. Camera captures document → processes → generates sealed PDF
2. Upload PDF → extracts text → detects contradictions → outputs report
3. Leveler engine detects contradictions in test documents
4. APK integrity verification shows correct hash
5. Offline mode: complete processing with airplane mode on
6. Memory: processes 10MB document without leaks

### Real-World Test Case
**Use Greensky Ornamentals documents**:
- Should detect: $11k Hong Kong deal contradiction
- Should flag: Shareholder oppression patterns
- Should catch: Gmail breach attempt ("SCAQUACULTURE")
- Integrity scores: Liam (high), Marius/Kevin (low)

### Performance Benchmarks
- WhatsApp .txt (100KB): < 2 seconds
- Scanned PDF (2MB): < 10 seconds
- Photo (5MB): < 15 seconds
- Memory usage: < 150MB peak

## Security & Privacy

### Design Principles
- **Stateless**: No data persistence between sessions
- **Offline-first**: Zero cloud dependencies
- **No telemetry**: No analytics, no tracking
- **Airgap-ready**: Works completely isolated
- **User data protection**: All processing on-device

### Permissions (Minimal)
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" /> <!-- Optional: geotag evidence -->
```

## Output Format

### Sealed PDF Contains
1. **Cover Page**: Case title, unique ID, QR code
2. **Executive Summary**: One-page findings overview
3. **Methodology**: How analysis was performed
4. **Timeline**: Chronological event reconstruction
5. **Contradictions**: Detailed contradiction matrix
6. **Behavioral Patterns**: Detected manipulation patterns
7. **Integrity Score**: 0-100 with breakdown
8. **Raw Evidence**: Appendices with original documents
9. **Verification Page**: Independent hash verification instructions
10. **Cryptographic Seal**: SHA-512 hash, watermark, timestamp

### Chain of Custody Report
```
=== FORENSIC CHAIN OF CUSTODY ===

APK Integrity: 56937d92ecf2f23bb9f11dbd619c3ce13f324ead1765311fccd18b6dbf209466
Case Created: 2025-01-15T10:30:00Z
Document 1: SHA512-abc123... (uploaded 2025-01-15T10:35:00Z)
Document 2: SHA512-def456... (uploaded 2025-01-15T10:40:00Z)
Report Generated: SHA512-ghi789... (2025-01-15T11:00:00Z)

Independent Verification:
1. Extract APK: adb pull /data/app/org.verumomnis.forensic/base.apk
2. Calculate hash: sha256sum base.apk
3. Verify matches: 56937d92ecf2f23bb9f11dbd619c3ce13f324ead1765311fccd18b6dbf209466
```

## Deployment Checklist

### Pre-Release
- [ ] All unit tests pass
- [ ] APK builds successfully
- [ ] APK integrity verification works
- [ ] Leveler engine detects contradictions correctly
- [ ] PDF sealing generates valid SHA-512 hashes
- [ ] Offline mode functions completely
- [ ] Memory leaks resolved
- [ ] Test on Android 7.0, 10, 12, 13

### Release Artifacts
1. **APK**: Signed release build
2. **SHA-256 Hash**: For verification
3. **README**: Usage instructions
4. **LICENSE**: Open source license (consider GPL-3.0 for forensic tools)
5. **PRIVACY POLICY**: No data collection statement
6. **VERIFICATION GUIDE**: How to independently verify outputs

## Critical Success Factors

### What Makes This Forensic-Grade
1. **Deterministic**: Same input → exact same output every time
2. **Verifiable**: Every output independently verifiable offline
3. **Tamper-evident**: Any modification detectable
4. **Court-ready**: Meets ISO 27037, PDF/A-3B, Daubert standards
5. **Legally tested**: Already accepted in actual court proceedings

### Real-World Impact
- **Citizens**: Free access to professional-grade forensic analysis
- **Legal validation**: Court-tested methodology (Port Shepstone case)
- **Cross-border**: UAE + SA compliance built-in
- **Zero cost**: No subscription, no fees, no data extraction

## Notes for Codespaces Implementation

### Environment Setup
```bash
# Install Android SDK
sudo apt-get update
sudo apt-get install -y android-sdk

# Install JDK 17
sudo apt-get install -y openjdk-17-jdk

# Set environment variables
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=/usr/lib/android-sdk
```

### Build Optimization
- Use Gradle caching to speed up builds
- Run tests in parallel: `./gradlew test --parallel`
- Generate build scan: `./gradlew build --scan`

### Continuous Integration
Already configured in `.github/workflows/build-apk.yml`:
- Automatic builds on push
- Test execution
- APK artifact upload
- SHA-256 hash generation

## Final Notes

**This is not a concept—it's a working forensic tool that has been court-tested.** The app has real-world validation through:
- Port Shepstone Magistrate's Court acceptance
- Active SAPS criminal investigation
- Professional legal firm review
- Sworn testimony under oath

**Release goal**: Make court-grade forensic analysis accessible to every citizen globally, for free, with cryptographic certainty and judicial precedent backing the methodology.
