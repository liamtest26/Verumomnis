# Legal Advisory API - Complete Technical Documentation

## Overview

The **Legal Advisory API** is a downstream consumer of sealed forensic outputs that provides jurisdiction-aware legal guidance without contaminating the core forensic engine or accessing raw evidence.

**Architectural Principle**: The API is physically and logically separated from the forensic engine and operates exclusively on sealed, abstracted forensic summaries.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    FORENSIC ENGINE (Offline)                    │
│                   - Deterministic Analysis                      │
│                   - B1-B9 Components                            │
│                   - Immutable Findings                          │
└─────────────────────────────────────────────────────────────────┘
                              ↓
        ┌──────────────────────────────────────────┐
        │  SealedForensicSummary (Abstracted Only) │
        │  ✓ Abstract findings                     │
        │  ✓ Actor consistency scores              │
        │  ✗ No raw evidence                       │
        │  ✗ No documents                          │
        │  GPS Coordinates (for jurisdiction)      │
        └──────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│              LEGAL ADVISORY API (Downstream Only)               │
│  ✓ Reads sealed summaries only                                  │
│  ✓ Maps findings to jurisdiction guidance                       │
│  ✓ Generates sealed advisory documents                          │
│  ✓ Manages sealed advisory sessions                             │
│  ✗ Cannot modify forensic findings                              │
│  ✗ Cannot access raw evidence                                   │
│  ✗ Cannot run online inference                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Core Components

### 1. SealedForensicSummary (Input Contract)

**File**: `legal/api/SealedSummary.kt`

The ONLY interface through which Legal API may access forensic data.

```kotlin
data class SealedForensicSummary(
    val reportHash: String,                          // SHA-512 of complete forensic report
    val generatedAt: Instant,                        // Timestamp of generation
    val forensicEngineVersion: String,               // v5.2.7
    val jurisdictionHint: String?,                   // User-provided hint (overridable)

    // GPS Coordinates for jurisdiction determination
    val gpsCoordinates: List<GPSCoordinate>,        // Evidence locations (mandatory for jurisdiction routing)
    val jurisdictionsDetermined: List<String>,       // Computed: ZA, UAE, SA, EU, etc.

    // Actors (consistency profiles, no accusations)
    val actors: List<ActorSummary>,

    // Findings (abstract categories, no raw data)
    val findings: List<FindingSummary>,

    // Counts only (no details/quotes)
    val timelineSummary: TimelineSummary,
    val contradictionLogSummary: ContradictionSummary,

    // Chain of trust
    val integrityIndexScore: Int,                    // 0–100
    val apkRootHash: String,                         // Root anchor for verification
    val tripleVerificationStatus: String,            // PASSED/FAILED/UNKNOWN
    val constitutionalComplianceVersion: String      // v5.2.7
)
```

**Prohibited**: Raw text, OCR output, media, document attachments, hash chain internals  
**Validated**: All fields checked via `SealedSummaryValidator` before Legal API ingestion

---

### 2. GPS Coordinates for Jurisdiction Routing

**File**: `legal/api/SealedSummary.kt` → `GPSCoordinate`

```kotlin
data class GPSCoordinate(
    val latitude: Double,                   // -90 to +90
    val longitude: Double,                  // -180 to +180
    val accuracy: Float,                    // Meters
    val altitude: Double?,                  // Optional elevation
    val timestamp: String,                  // ISO 8601
    val source: String,                     // "device", "metadata", "exif", "timestamp_inferred"
    val confidenceLevel: String             // CERTAIN, PROBABLE, POSSIBLE
)
```

**Supported Jurisdictions** (GPS Bounds):

| Jurisdiction | Latitude Range | Longitude Range | Primary Law |
|---|---|---|---|
| **ZA** (South Africa) | -34.8° to -22.1°S | 16.5° to 32.9°E | Companies Act 2008, POPIA 2020 |
| **UAE** | 22.5° to 26.3°N | 51.6° to 56.4°E | Cybercrime Law 2012, Commercial Law |
| **SA** (Saudi Arabia) | 16.3° to 32.1°N | 34.4° to 55.9°E | ECT Act 2002, Shariah Law |
| **EU** | 35.0° to 71.0°N | -25.0° to 40.0°E | GDPR 2016/679, National Laws |

---

### 3. LegalAdvisoryAPI (Main Coordinator)

**File**: `legal/api/LegalAdvisoryAPI.kt`

```kotlin
class LegalAdvisoryAPI {
    // Primary entry point
    fun getAdvisory(
        summary: SealedForensicSummary,
        jurisdictionOverride: String? = null
    ): Result<AdvisoryResponse>

    // Generate sealed advisory document
    fun generateSealedLetter(
        advisory: AdvisoryResponse,
        letterType: String,  // "email", "letter", "brief"
        recipient: String
    ): Result<SealedAdvisoryDocument>

    // Advisory sessions (sealed conversations)
    fun startAdvisorySession(reportHash: String): Result<String>
    fun askQuestion(sessionId: String, question: String): Result<SessionResponse>
    fun closeSession(sessionId: String): Result<String>
}
```

**Workflow**:
1. Accept sealed forensic summary
2. Enforce input contract (reject contamination)
3. Verify summary in vault (chain of custody)
4. Determine jurisdiction from GPS coordinates
5. Route to appropriate jurisdiction module
6. Generate advisory response
7. Seal and store all outputs

---

### 4. GPS-Based Jurisdiction Router

**File**: `legal/jurisdictions/GPSJurisdictionRouter.kt`

Maps GPS coordinates to applicable jurisdictions and legal guidance.

```kotlin
class GPSJurisdictionRouter {
    fun determineJurisdictionsFromGPS(coordinates: List<GPSCoordinate>): List<String>
    fun getPrimaryJurisdiction(coordinates: List<GPSCoordinate>): String
    fun getJurisdictionGuidance(
        jurisdiction: String,
        findings: List<FindingSummary>,
        integrityScore: Int
    ): String
    fun generateCrossBorderAnalysis(coordinates: List<GPSCoordinate>): String
}
```

**Features**:
- Automatic jurisdiction inference from GPS
- Multi-jurisdiction support (cross-border cases)
- Jurisdiction-specific legal guidance
- Cross-border risk analysis

---

### 5. Jurisdiction-Specific Guidance

#### South Africa (ZA)
- **Primary Law**: Companies Act 2008, POPIA 2020
- **Findings Mapped**:
  - `fiduciary_breach` → Director duties (s76), High Court remedies
  - `fraud_indicators` → Criminal prosecution, SAPS reporting
  - `contract_violation` → Civil recovery, alternative dispute resolution
  - `shareholder_oppression` → Tribunal/High Court remedies
- **Evidence Standard**: Chain of custody mandatory, sealed outputs admissible

#### United Arab Emirates (UAE)
- **Primary Law**: Cybercrime Law 2012, Commercial Law, DIFC
- **Findings Mapped**:
  - `fraud_indicators` → Criminal prosecution, Federal Courts
  - `financial_anomaly` → FTA (Federal Tax Authority) reporting
  - `shareholder_oppression` → DIFC or Federal Courts (English law option)
- **Requirements**: Arabic translation for official submission

#### Saudi Arabia (SA)
- **Primary Law**: ECT Act 2002, Shariah Law
- **Findings Mapped**:
  - `fraud_indicators` → Criminal courts (not Shariah)
  - `contract_violation` → Shariah-compliant interpretation
  - `financial_anomaly` → SAMA (Saudi Arabian Monetary Authority)
- **Special Rules**: Interest (riba) not enforceable, Shariah principles apply

#### European Union (EU)
- **Primary Law**: GDPR 2016/679, eIDAS, National laws
- **Findings Mapped**:
  - `fraud_indicators` → Criminal prosecution, EU coordination
  - `data_breach` → GDPR fines (€20M or 4% revenue), victim compensation
  - `privacy_violation` → Data Protection Authority, regulatory remedies
- **Evidence Standards**: eIDAS-compliant digital signatures, chain of custody

---

### 6. Sealed Document Generation

**File**: `legal/documents/SealedDocumentGenerator.kt`

Generates sealed advisory documents in multiple formats:

```kotlin
fun generateDocument(
    advisory: AdvisoryResponse,
    letterType: String,  // "email", "letter", "brief"
    recipient: String
): SealedAdvisoryDocument
```

**Document Types**:

| Type | Purpose | Format |
|---|---|---|
| **email** | Quick advisory via email | Structured email text |
| **letter** | Formal advisory letter | Professional letterhead |
| **brief** | Executive summary | Concise facts & actions |
| **generic** | Plain advisory | Unformatted text |

**All Outputs**:
- ✓ Watermarked: "Generated from sealed forensic summary"
- ✓ Include report hash reference
- ✓ Individually sealed (SHA-512)
- ✓ Stored in evidence vault
- ✓ Immutable (cannot be edited)
- ✓ Cross-linked to originating forensic report

---

### 7. Advisory Response

```kotlin
data class AdvisoryResponse(
    val reportHash: String,
    val generatedAt: Instant,
    val jurisdiction: String,                  // Primary jurisdiction (GPS-determined)
    val allApplicableJurisdictions: List<String>,  // All jurisdictions where evidence occurred
    val gpsCoordinatesProcessed: Int,         // Count of processed GPS points
    val recommendations: List<String>,        // Jurisdiction-specific guidance
    val nextSteps: List<String>,              // Recommended actions
    val riskFactors: List<String>,            // Key findings
    val confidenceStatement: String,          // Integrity score explanation
    val crossBorderAnalysis: String,          // Multi-jurisdiction implications
    val disclaimers: List<String>,            // Standard legal disclaimers
    val vaultRecordId: String                 // Link to forensic report
)
```

---

### 8. Session Management (Sealed Conversations)

**File**: `legal/sessions/` (Implemented via `LegalSessionManager`)

Advisory sessions enable follow-up questions with sealed conversation transcripts.

```kotlin
fun startAdvisorySession(reportHash: String): Result<String>
fun askQuestion(sessionId: String, question: String): Result<SessionResponse>
fun closeSession(sessionId: String): Result<String>
```

**Behavior**:
- User asks follow-up questions about advisory
- Each question/response sealed and stored in vault
- Conversation transcript immutable after session close
- Full audit trail maintained
- No new evidence can be added

**Validation**:
- Questions rejected if they attempt to introduce evidence
- Questions must be purely advisory/clarification
- Guard: Rejects "raw", "upload", "document" keywords

---

## Input/Output Contract Enforcement

### Input Validation (Runtime Guards)

```kotlin
fun SealedForensicSummary.enforceInputContract() {
    // Throws SecurityException if:
    // ✗ Contains raw text
    // ✗ Contains media attachments
    // ✗ Contains OCR output
    // ✗ reportHash not SHA-512
    // ✗ Integrity score out of range (0-100)
    // ✗ Actor IDs not hashed
}

object SealedSummaryValidator {
    fun validateNoRawEvidence(summary: SealedForensicSummary): Result<Unit>
    fun validateHashAuthenticity(summary: SealedForensicSummary, vaultHash: String): Boolean
    fun validateActorIDsAreHashed(summary: SealedForensicSummary): Boolean
}
```

### Output Guarantees

All outputs are:
- **Sealed**: SHA-512 hashed and immutable
- **Traced**: Cross-linked to source forensic report
- **Stored**: Maintained in evidence vault
- **Watermarked**: Clear identification as advisory
- **Disclaimered**: Standard legal disclaimers included

---

## Integration with ForensicEngine

### Emission of Sealed Summaries

**File**: `forensic/core/ForensicEngine.kt`

```kotlin
fun generateSealedForensicSummary(
    report: ForensicReport,
    gpsCoordinates: List<GPSCoordinate> = emptyList()
): SealedForensicSummary

fun getSealedSummaryForLegalAdvisory(
    report: ForensicReport,
    gpsCoordinates: List<GPSCoordinate> = emptyList()
): SealedForensicSummary
```

**Usage**:
```kotlin
// After forensic analysis completes
val forensicReport = forensicEngine.processForensicCase(...)
val gpsCoordinates = extractGPSFromEvidenceMetadata()

// Generate sealed summary for Legal API
val sealedSummary = forensicEngine.generateSealedForensicSummary(
    report = forensicReport,
    gpsCoordinates = gpsCoordinates
)

// Pass to Legal API
val legalAPI = LegalAdvisoryAPI.initialize(...)
val advisory = legalAPI.getAdvisory(sealedSummary).getOrNull()
```

---

## Explicit Prohibitions

The Legal API **MUST NEVER**:

| Prohibition | Reason |
|---|---|
| ❌ Accept raw evidence uploads | Forensic engine only |
| ❌ Edit forensic findings | Immutability requirement |
| ❌ Re-run B1-B9 analysis | Offline determinism |
| ❌ Execute online inference | Offline-only architecture |
| ❌ Re-calculate scores | Immutable output |
| ❌ Execute OCR or image analysis | Raw evidence processing |
| ❌ Auto-send emails/documents | User action required |
| ❌ Store conversation ephemeral | All sessions sealed & stored |
| ❌ Extract personal data | GDPR/POPIA compliant sealed processing |
| ❌ Delete any records | Chain of custody immutability |

---

## UI/UX Design Guidance

### Entry Points

**After forensic analysis completes**:
```
┌─────────────────────────────────────────┐
│         REPORT READY                    │
│  [View Sealed Report]  [Legal Advisory] │  ← Button appears here
│                        [Get Guidance]   │
└─────────────────────────────────────────┘
```

### Warning Banners

```
╔═════════════════════════════════════════════════════════════════╗
║  ⚠️  ADVISORY ONLY - NOT LEGAL REPRESENTATION                   ║
║  This guidance is based on sealed forensic findings abstracted  ║
║  at the advisory level. No raw evidence is embedded.            ║
║  Consult a qualified attorney for formal legal representation.  ║
╚═════════════════════════════════════════════════════════════════╝
```

### Button Labels

| Action | Label |
|---|---|
| View jurisdiction guidance | "Get Legal Guidance (Advisory)" |
| Generate email | "Draft Advisory Email" |
| Generate letter | "Draft Advisory Letter" |
| Start Q&A session | "Ask Advisory Questions" |
| View cross-border analysis | "Cross-Border Analysis" |

---

## Security Considerations

### Chain of Custody

All advisory outputs:
1. **Created** from sealed summary in vault
2. **Hashed** individually (SHA-512)
3. **Stored** in vault with metadata
4. **Cross-linked** to originating forensic report
5. **Immutable** (cannot be edited after creation)
6. **Verifiable** through vault audit trail

### Data Minimization

- No personal data exported from sealed summaries
- Actor IDs are hashed (not plain names)
- Findings abstracted (no quotes or details)
- Counts only (no narrative).
- GDPR/POPIA compliant sealed processing

### Independent Verification

Users can verify:
- Document hash matches vault record
- Report hash is authentic
- Chain of custody intact
- No tampering occurred

---

## Testing & Validation

### Unit Tests Required

```kotlin
// Sealed summary validation
fun testSealedSummaryRejectsRawEvidence()
fun testSealedSummaryHashValidation()
fun testActorIDsAreHashed()

// Jurisdiction routing
fun testGPSToJurisdictionMapping()
fun testCrossBorderAnalysis()
fun testMultiJurisdictionDetection()

// Document generation
fun testSealedDocumentGeneration()
fun testWatermarkInclusion()
fun testDocumentImmutability()
fun testVaultStorageAndRetrieval()

// Session management
fun testSessionCreation()
fun testQuestionValidation()
fun testSessionTranscriptSealing()
fun testEphemeralConversationRejection()
```

---

## Example Usage

### Scenario: Greensky Ornamentals Case (Cross-Border)

```kotlin
// 1. Forensic analysis completes
val forensicReport = forensicEngine.processForensicCase(
    caseId = "GreenSky-001",
    caseName = "Greensky Ornamentals FZ-LLC Shareholder Dispute",
    evidencePaths = listOf(...),  // 10 documents max
    jurisdiction = "UAE"
)

// 2. Extract GPS from evidence metadata
val gpsCoordinates = listOf(
    GPSCoordinate(24.4539, 54.3773, 15f, source = "metadata"),  // Dubai
    GPSCoordinate(-33.9249, 18.4241, 20f, source = "exif")      // Cape Town
)

// 3. Generate sealed summary
val sealedSummary = forensicEngine.generateSealedForensicSummary(
    report = forensicReport,
    gpsCoordinates = gpsCoordinates
)

// 4. Get legal advisory
val legalAPI = LegalAdvisoryAPI.initialize(...)
val advisory = legalAPI.getAdvisory(sealedSummary).getOrThrow()

// Result:
// - Primary Jurisdiction: UAE (24.4539, 54.3773 → Dubai)
// - Secondary Jurisdiction: ZA (South Africa office)
// - Cross-Border Analysis: "Multiple jurisdictions detected..."
// - Recommendations: Both UAE Cybercrime Law and ZA Companies Act apply
// - Next Steps: Consult attorney licensed in both jurisdictions

// 5. Generate sealed letter
val letter = legalAPI.generateSealedLetter(
    advisory = advisory,
    letterType = "letter",
    recipient = "investigator@case.local"
)

// Output: Sealed PDF/letter stored in vault, hash verified, immutable
```

---

## Deployment Checklist

- [ ] SealedForensicSummary validation implemented
- [ ] GPS coordinate bounds verified for all 4 jurisdictions
- [ ] GPSJurisdictionRouter tested against known coordinates
- [ ] Jurisdiction-specific guidance text reviewed by legal expert
- [ ] SealedDocumentGenerator tested for all letter types
- [ ] Watermark included in all outputs
- [ ] Evidence vault integration complete
- [ ] Session management sealed and immutable
- [ ] Question validation guards preventing evidence smuggling
- [ ] All disclaimers included in outputs
- [ ] UI entry points implemented (after report completion)
- [ ] Independent verification procedure documented
- [ ] Security audit completed

---

## Legal Notice

This Legal Advisory API provides **advisory guidance only** and is **NOT a substitute for legal representation**. All advisories include prominent disclaimers. Users must consult qualified attorneys licensed in the applicable jurisdiction for formal legal counsel.

The API is designed to empower citizens with informed guidance while maintaining strict separation from forensic analysis and ensuring no contamination of evidence.

---

## License & Distribution

**Verum Omnis Forensic Engine v5.2.7**
- Released freely to all citizens globally
- No subscription, fees, or cloud dependencies
- Open source (GPL-3.0)
- For justice, for truth, for all citizens
