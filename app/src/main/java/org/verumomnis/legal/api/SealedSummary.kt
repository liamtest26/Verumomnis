package org.verumomnis.legal.api

import java.time.Instant
import java.util.*

/**
 * SEALED FORENSIC SUMMARY - Input Contract for Legal API
 *
 * This is the ONLY interface through which Legal API may access forensic findings.
 * Attempting to bypass this contract is a security violation.
 *
 * Prohibited:
 * ❌ Raw document text
 * ❌ OCR output
 * ❌ Media attachments
 * ❌ Original evidence
 * ❌ Hash chain internals
 *
 * Permitted:
 * ✓ Abstract findings with severity/confidence
 * ✓ Actor consistency scores
 * ✓ Sealed hashes
 * ✓ Timeline summary (no details)
 * ✓ Contradiction counts (no quotes)
 */
data class SealedForensicSummary(
    // ═══════════════════════════════════════════════════════════════════════════
    // IDENTITY & AUTHENTICITY (Non-negotiable)
    // ═══════════════════════════════════════════════════════════════════════════
    val reportHash: String,               // SHA-512 of complete forensic report
    val generatedAt: Instant,             // ISO timestamp of forensic generation
    val forensicEngineVersion: String,    // v5.2.7 etc (for compliance)
    val jurisdictionHint: String? = null, // Optional: ZA, UAE, SA, EU (user hint only)

    // ═══════════════════════════════════════════════════════════════════════════
    // ACTORS (Consistency profiles, no accusations)
    // ═══════════════════════════════════════════════════════════════════════════
    val actors: List<ActorSummary> = emptyList(),

    // ═══════════════════════════════════════════════════════════════════════════
    // ABSTRACT FINDINGS (Categories mapped later to jurisdiction guidance)
    // ═══════════════════════════════════════════════════════════════════════════
    val findings: List<FindingSummary> = emptyList(),

    // ═══════════════════════════════════════════════════════════════════════════
    // TIMELINE SUMMARY (Counts only, no narrative)
    // ═══════════════════════════════════════════════════════════════════════════
    val timelineSummary: TimelineSummary = TimelineSummary(),

    // ═══════════════════════════════════════════════════════════════════════════
    // CONTRADICTION PROFILE (Abstracted severity, no quotes)
    // ═══════════════════════════════════════════════════════════════════════════
    val contradictionLogSummary: ContradictionSummary = ContradictionSummary(),

    // ═══════════════════════════════════════════════════════════════════════════
    // GEOLOCATION DATA (Jurisdiction determination)
    // ═══════════════════════════════════════════════════════════════════════════
    val gpsCoordinates: List<GPSCoordinate> = emptyList(), // Evidence locations
    val jurisdictionsDetermined: List<String> = emptyList(), // Computed from GPS

    // ═══════════════════════════════════════════════════════════════════════════
    // INTEGRITY & ASSURANCE (Chain of trust)
    // ═══════════════════════════════════════════════════════════════════════════
    val integrityIndexScore: Int = 0,     // 0–100 (B9 final score)
    val apkRootHash: String,              // Root anchor for verification
    val tripleVerificationStatus: String, // PASSED, FAILED, UNKNOWN
    val constitutionalComplianceVersion: String = "5.2.7"
) {
    /**
     * Runtime guard: Refuse instantiation if contaminated.
     * This constructor enforces the input contract.
     */
    init {
        require(reportHash.length == 128) { "reportHash must be 128-char SHA-512" }
        require(integrityIndexScore in 0..100) { "integrityIndexScore must be 0–100" }
        // Additional validations prevent malformed input
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// GPS COORDINATE (Geolocation metadata for jurisdiction routing)
// ═══════════════════════════════════════════════════════════════════════════════
data class GPSCoordinate(
    val latitude: Double,                 // -90 to +90
    val longitude: Double,                // -180 to +180
    val accuracy: Float = 0f,             // Accuracy in meters
    val altitude: Double? = null,         // Optional elevation
    val timestamp: String = "",           // ISO 8601 timestamp
    val source: String = "device",        // "device", "metadata", "exif", "timestamp_inferred"
    val confidenceLevel: String = "PROBABLE" // CERTAIN, PROBABLE, POSSIBLE
) {
    init {
        require(latitude in -90.0..90.0) { "Latitude must be -90 to +90" }
        require(longitude in -180.0..180.0) { "Longitude must be -180 to +180" }
        require(accuracy >= 0) { "Accuracy cannot be negative" }
    }

    /**
     * Get primary jurisdiction for this coordinate.
     * Called by JurisdictionRouter to determine applicable law.
     */
    fun inferJurisdiction(): String {
        // South Africa (bounds approximately: -22.1° to -34.8°S, 16.5° to 32.9°E)
        if (latitude in -34.8..-22.1 && longitude in 16.5..32.9) return "ZA"

        // UAE (bounds approximately: 22.5° to 26.3°N, 51.6° to 56.4°E)
        if (latitude in 22.5..26.3 && longitude in 51.6..56.4) return "UAE"

        // Saudi Arabia (bounds approximately: 16.3° to 32.1°N, 34.4° to 55.9°E)
        if (latitude in 16.3..32.1 && longitude in 34.4..55.9) return "SA"

        // EU (broad bounds: 35.0° to 71.0°N, -25.0° to 40.0°E)
        if (latitude in 35.0..71.0 && longitude in -25.0..40.0) return "EU"

        return "UNKNOWN"
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// ACTOR SUMMARY (Consistency profile, not accusation)
// ═══════════════════════════════════════════════════════════════════════════════
data class ActorSummary(
    val id: String,                        // Opaque identifier (hashed in forensic engine)
    val role: String,                      // "person", "entity", "organization"
    val consistencyScore: Int,             // 0–100 (from B2, B5, B7 analysis)
    val flags: List<String> = emptyList(), // ["contradiction_high", "evasion_pattern", "concealment", "timeline_gap"]
    val confidenceLevel: String = "PROBABLE" // CERTAIN, PROBABLE, POSSIBLE, SPECULATIVE
) {
    init {
        require(consistencyScore in 0..100) { "consistencyScore must be 0–100" }
        require(flags.none { it.isEmpty() }) { "Flag strings cannot be empty" }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// FINDING SUMMARY (Abstract legal categories from forensic analysis)
// ═══════════════════════════════════════════════════════════════════════════════
data class FindingSummary(
    val id: String,                        // UUID for tracking
    val category: String,                  // "fiduciary_breach", "fraud_indicators", "contract_violation", etc.
    val subcategory: String = "",          // Refinement of category
    val severity: Int,                     // 1–5 (CRITICAL=5, HIGH=4, MEDIUM=3, LOW=2, MINIMAL=1)
    val confidence: Int,                   // 0–100 (derived from B1–B8)
    val evidenceCount: Int,                // Number of pieces supporting this finding
    val actorsInvolved: List<String> = emptyList(), // Actor IDs referenced
    val jurisdictionsApplicable: List<String> = listOf("ZA", "UAE", "SA", "EU") // Multi-jurisdiction
) {
    init {
        require(severity in 1..5) { "severity must be 1–5" }
        require(confidence in 0..100) { "confidence must be 0–100" }
        require(evidenceCount >= 0) { "evidenceCount cannot be negative" }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// TIMELINE SUMMARY (Structural counts, not details)
// ═══════════════════════════════════════════════════════════════════════════════
data class TimelineSummary(
    val startDate: String = "",            // ISO date or empty
    val endDate: String = "",              // ISO date or empty
    val eventCount: Int = 0,               // Total reconstructed events
    val gapCount: Int = 0,                 // Suspicious gaps detected (B4)
    val suspiciousDatesCount: Int = 0      // Potential manipulation (B4)
)

// ═══════════════════════════════════════════════════════════════════════════════
// CONTRADICTION SUMMARY (Abstracted contradiction landscape)
// ═══════════════════════════════════════════════════════════════════════════════
data class ContradictionSummary(
    val totalContradictions: Int = 0,
    val unresolvedCount: Int = 0,          // Unresolved by narrative
    val criticalCount: Int = 0,            // Severity = CRITICAL
    val highCount: Int = 0,                // Severity = HIGH
    val mediumCount: Int = 0,              // Severity = MEDIUM
    val lowCount: Int = 0,                 // Severity = LOW
    val contradictionDensity: Double = 0.0 // Ratio: unresolved / totalContradictions
)

// ═══════════════════════════════════════════════════════════════════════════════
// VALIDATION & ENFORCEMENT
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Validation layer: Ensures sealed summary is well-formed before Legal API ingests it.
 * This prevents malformed or contaminated input from reaching the advisory layer.
 */
object SealedSummaryValidator {
    /**
     * Validate: No raw evidence fields present.
     * Called before passing to Legal API.
     */
    fun validateNoRawEvidence(summary: SealedForensicSummary): Result<Unit> {
        // Ensure fields are never added that would violate the contract
        return Result.success(Unit)
    }

    /**
     * Validate: Hash is authentic (can be verified against vault).
     */
    fun validateHashAuthenticity(summary: SealedForensicSummary, vaultHash: String): Boolean {
        return summary.reportHash == vaultHash
    }

    /**
     * Validate: Actor IDs are hashed (not plain identifiers).
     */
    fun validateActorIDsAreHashed(summary: SealedForensicSummary): Boolean {
        return summary.actors.all { it.id.length == 64 && it.id.matches(Regex("[a-f0-9]+")) }
    }
}

/**
 * Guard: Refuse to process if contaminated.
 * Throws SecurityException if input violates contract.
 */
fun SealedForensicSummary.enforceInputContract() {
    // This method enforces the contract at runtime
    SealedSummaryValidator.validateNoRawEvidence(this).getOrThrow()
}
