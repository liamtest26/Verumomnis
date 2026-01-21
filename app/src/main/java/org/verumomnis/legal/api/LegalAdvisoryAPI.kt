package org.verumomnis.legal.api

import java.time.Instant
import java.util.*
import org.verumomnis.legal.jurisdictions.GPSJurisdictionRouter
import org.verumomnis.legal.compliance.ConstitutionalComplianceValidator

/**
 * LEGAL API CORE COORDINATOR
 *
 * Responsibilities:
 * ✓ Accept SEALED summaries only
 * ✓ Map abstract findings to legal pathways
 * ✓ Generate advisory recommendations
 * ✓ Seal all outputs
 * ✓ Maintain chain of custody for advisory sessions
 *
 * Prohibitions:
 * ❌ Cannot access raw evidence
 * ❌ Cannot modify forensic findings
 * ❌ Cannot run online inference
 * ❌ Cannot edit scores
 * ❌ Cannot re-analyze documents
 */
class LegalAdvisoryAPI private constructor(
    private val evidenceVault: EvidenceVault,
    private val gpsJurisdictionRouter: GPSJurisdictionRouter,
    private val sessionManager: LegalSessionManager,
    private val documentSealer: SealedDocumentGenerator,
    private val complianceValidator: ConstitutionalComplianceValidator
) {
    companion object {
        /**
         * Factory: Instantiate Legal API with validated dependencies.
         * This prevents malformed initialization.
         */
        fun initialize(
            vaultPath: String,
            sessionStoragePath: String,
            apkRootHash: String
        ): LegalAdvisoryAPI {
            val vault = EvidenceVault(vaultPath)
            val gpsRouter = GPSJurisdictionRouter()
            val sessions = LegalSessionManager(sessionStoragePath, vault)
            val sealer = SealedDocumentGenerator(apkRootHash, vault)
            val compliance = ConstitutionalComplianceValidator()

            return LegalAdvisoryAPI(vault, gpsRouter, sessions, sealer, compliance)
        }
    }

    /**
     * PRIMARY ENTRY POINT
     *
     * Accept sealed forensic summary → Generate advisory.
     * Automatically determines jurisdiction from GPS coordinates.
     */
    fun getAdvisory(
        summary: SealedForensicSummary,
        jurisdictionOverride: String? = null
    ): Result<AdvisoryResponse> = try {
        // ═══════════════════════════════════════════════════════════════════════════
        // STEP 1: Enforce input contract (refuse contamination)
        // ═══════════════════════════════════════════════════════════════════════════
        summary.enforceInputContract()

        // ═══════════════════════════════════════════════════════════════════════════
        // STEP 1.5: CONSTITUTIONAL COMPLIANCE CHECK
        // ═══════════════════════════════════════════════════════════════════════════
        val complianceCheck = complianceValidator.validateSummaryCompliance(summary)
        if (complianceCheck.isFailure) {
            return Result.failure(
                IllegalArgumentException(
                    "Constitutional compliance failed: ${complianceCheck.exceptionOrNull()?.message}"
                )
            )
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // STEP 2: Verify sealed summary is in vault (chain of custody)
        // ═══════════════════════════════════════════════════════════════════════════
        val vaultRecord = evidenceVault.lookupByHash(summary.reportHash)
            ?: return Result.failure(IllegalArgumentException("Summary hash not found in vault"))

        // ═══════════════════════════════════════════════════════════════════════════
        // STEP 3: Determine jurisdiction from GPS coordinates (or use override)
        // ═══════════════════════════════════════════════════════════════════════════
        val jurisdiction = if (jurisdictionOverride != null) {
            jurisdictionOverride
        } else if (summary.gpsCoordinates.isNotEmpty()) {
            gpsJurisdictionRouter.getPrimaryJurisdiction(summary.gpsCoordinates)
        } else {
            summary.jurisdictionHint ?: "UNKNOWN"
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // STEP 4: Route to appropriate jurisdiction module (GPS-aware)
        // ═══════════════════════════════════════════════════════════════════════════
        val jurisdictionGuidanceText = gpsJurisdictionRouter.getJurisdictionGuidance(
            jurisdiction = jurisdiction,
            findings = summary.findings,
            integrityScore = summary.integrityIndexScore
        )

        // ═══════════════════════════════════════════════════════════════════════════
        // STEP 5: Generate cross-border analysis if multiple jurisdictions
        // ═══════════════════════════════════════════════════════════════════════════
        val allJurisdictions = gpsJurisdictionRouter.determineJurisdictionsFromGPS(summary.gpsCoordinates)
        val crossBorderAnalysis = gpsJurisdictionRouter.generateCrossBorderAnalysis(summary.gpsCoordinates)

        // ═══════════════════════════════════════════════════════════════════════════
        // STEP 6: Compose advisory (advisory language, not accusation)
        // ═══════════════════════════════════════════════════════════════════════════
        val advisory = composeAdvisory(summary, jurisdictionGuidanceText)

        // ═══════════════════════════════════════════════════════════════════════════
        // STEP 7: Create response object (advisory only)
        // ═══════════════════════════════════════════════════════════════════════════
        val response = AdvisoryResponse(
            reportHash = summary.reportHash,
            generatedAt = Instant.now(),
            jurisdiction = jurisdiction,
            allApplicableJurisdictions = allJurisdictions,
            gpsCoordinatesProcessed = summary.gpsCoordinates.size,
            recommendations = advisory.recommendations,
            nextSteps = advisory.nextSteps,
            riskFactors = advisory.riskFactors,
            confidenceStatement = advisory.confidenceStatement,
            crossBorderAnalysis = crossBorderAnalysis,
            disclaimers = getDisclaimers(),
            vaultRecordId = vaultRecord.recordId
        )

        // ═══════════════════════════════════════════════════════════════════════════
        // STEP 8: VERIFY ADVISORY COMPLIANCE
        // ═══════════════════════════════════════════════════════════════════════════
        val advisoryCompliance = complianceValidator.validateAdvisoryCompliance(response)
        if (advisoryCompliance.isFailure) {
            return Result.failure(
                IllegalArgumentException("Advisory compliance failed")
            )
        }

        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Generate sealed advisory output (PDF letter/email).
     * Output includes watermark, hash, and vault reference.
     */
    fun generateSealedLetter(
        advisory: AdvisoryResponse,
        letterType: String, // "email", "letter", "brief"
        recipient: String
    ): Result<SealedAdvisoryDocument> = try {
        // Create sealed PDF/document
        val document = documentSealer.generateDocument(
            advisory = advisory,
            letterType = letterType,
            recipient = recipient
        )

        // Store in vault
        evidenceVault.store(document)

        Result.success(document)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Start advisory session (user asking follow-up questions).
     * All conversation is sealed and stored in vault.
     */
    fun startAdvisorySession(
        reportHash: String,
        sessionMetadata: SessionMetadata
    ): Result<String> = try {
        val sessionId = sessionManager.createSession(reportHash, sessionMetadata)
        Result.success(sessionId)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Add user question to session (sealed).
     * Response is also sealed before transmission.
     */
    fun askQuestion(
        sessionId: String,
        question: String
    ): Result<SessionResponse> = try {
        val session = sessionManager.getSession(sessionId)
            ?: return Result.failure(IllegalArgumentException("Session not found"))

        // Validate question doesn't try to introduce evidence
        validateQuestionIsAdvisoryOnly(question)

        // Get response from jurisdiction module
        val response = jurisdictionRouter.answerQuestion(
            question = question,
            jurisdiction = session.metadata.jurisdiction,
            reportHash = session.reportHash
        )

        // Seal response and store in vault
        val sealed = sessionManager.addExchange(
            sessionId = sessionId,
            question = question,
            response = response
        )

        Result.success(sealed)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Close advisory session.
     * Seals transcript and stores in vault.
     */
    fun closeSession(sessionId: String): Result<String> = try {
        val transcript = sessionManager.closeSession(sessionId)
        // Transcript is now immutable in vault
        Result.success(transcript.transcriptHash)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Compose advisory text from sealed findings.
     * Uses probabilistic language ("may", "could"), not assertions.
     */
    private fun composeAdvisory(
        summary: SealedForensicSummary,
        jurisdictionGuidanceText: String
    ): AdvisoryComposition {
        val recommendations = mutableListOf<String>()
        val riskFactors = mutableListOf<String>()

        // For each finding, generate advisory text
        summary.findings.forEach { finding ->
            val weight = when (finding.severity) {
                5 -> "critical"
                4 -> "high"
                3 -> "moderate"
                else -> "lower"
            }

            recommendations.add(
                "This case presents a ${weight}-level finding in the category of " +
                "${finding.category}. Based on sealed forensic findings, common next steps " +
                "in the applicable jurisdiction may include consultation with specialized counsel."
            )

            if (finding.confidence >= 75) {
                riskFactors.add(
                    "High confidence (${finding.confidence}%) that ${finding.category} " +
                    "factors may be present (${finding.evidenceCount} supporting pieces)."
                )
            }
        }

        // Add jurisdiction guidance to recommendations
        recommendations.add("\n$jurisdictionGuidanceText")

        return AdvisoryComposition(
            recommendations = recommendations,
            nextSteps = listOf(
                "Review sealed forensic findings in detail",
                "Consult with qualified attorney in applicable jurisdiction",
                "Preserve all evidence and maintain chain of custody",
                "Follow jurisdiction-specific legal procedures"
            ),
            riskFactors = riskFactors,
            confidenceStatement = "Based on sealed findings with integrity score " +
                "${summary.integrityIndexScore}/100 and ${summary.tripleVerificationStatus} verification."
        )
    }

    /**
     * Validate user question doesn't try to smuggle evidence.
     */
    private fun validateQuestionIsAdvisoryOnly(question: String) {
        // Simple heuristics (can be enhanced)
        require(!question.contains("raw", ignoreCase = true)) {
            "Questions must not reference raw evidence"
        }
        require(!question.contains("upload", ignoreCase = true)) {
            "New evidence cannot be added to advisory sessions"
        }
        require(!question.contains("document", ignoreCase = true)) {
            "Document uploads are not permitted in advisory sessions"
        }
    }

    /**
     * Standard disclaimers appended to all advisory outputs.
     */
    private fun getDisclaimers(): List<String> = listOf(
        "DISCLAIMER: This is advisory guidance only, not legal representation or counsel.",
        "This guidance is based on sealed forensic findings and abstracted legal categories.",
        "No new evidence has been added or reviewed in generating this advisory.",
        "The forensic findings upon which this advisory is based are sealed and immutable.",
        "This document itself is sealed, hashed, and stored in the evidence vault for chain of custody.",
        "For formal legal representation, consult a qualified attorney licensed in your jurisdiction."
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// RESPONSE TYPES
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Advisory response: Recommendations, next steps, risk factors.
 * Includes GPS-determined jurisdiction information.
 */
data class AdvisoryResponse(
    val reportHash: String,
    val generatedAt: Instant,
    val jurisdiction: String,
    val allApplicableJurisdictions: List<String> = emptyList(),
    val gpsCoordinatesProcessed: Int = 0,
    val recommendations: List<String>,
    val nextSteps: List<String>,
    val riskFactors: List<String>,
    val confidenceStatement: String,
    val crossBorderAnalysis: String = "",
    val disclaimers: List<String>,
    val vaultRecordId: String // Linkage to forensic report
)

/**
 * Internal composition type (used during advisory generation).
 */
internal data class AdvisoryComposition(
    val recommendations: List<String>,
    val nextSteps: List<String>,
    val riskFactors: List<String>,
    val confidenceStatement: String
)

/**
 * Session response: Question + sealed answer.
 */
data class SessionResponse(
    val sessionId: String,
    val questionHash: String,
    val responseHash: String,
    val timestamp: Instant,
    val transcriptHash: String // Running hash of all exchanges
)

/**
 * Session metadata for tracking.
 */
data class SessionMetadata(
    val jurisdiction: String,
    val caseType: String = "unknown",
    val userRole: String = "citizen" // "citizen", "investigator", "attorney"
)

// ═══════════════════════════════════════════════════════════════════════════════
// PLACEHOLDER STUBS (Implemented separately)
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Evidence Vault: Stores sealed outputs from forensic engine and Legal API.
 * Maintains chain of custody.
 */
class EvidenceVault(val vaultPath: String) {
    fun lookupByHash(hash: String): VaultRecord? = null
    fun store(document: SealedAdvisoryDocument) {}
    fun store(document: Any) {}
}

data class VaultRecord(
    val recordId: String,
    val hash: String,
    val timestamp: Instant,
    val type: String // "forensic", "advisory", "session_transcript"
)

data class SealedAdvisoryDocument(
    val documentHash: String,
    val content: ByteArray,
    val watermark: String,
    val reportHashReference: String,
    val generatedAt: Instant
) {
    override fun equals(other: Any?): Boolean = other is SealedAdvisoryDocument &&
        documentHash == other.documentHash
    override fun hashCode(): Int = documentHash.hashCode()
}

/**
 * Jurisdiction Router: Maps abstract findings to jurisdiction-specific guidance.
 */
class JurisdictionRouter {
    fun getGuidance(
        jurisdiction: String,
        findings: List<FindingSummary>,
        integrityScore: Int
    ): JurisdictionGuidance = JurisdictionGuidance()

    fun answerQuestion(
        question: String,
        jurisdiction: String,
        reportHash: String
    ): String = ""
}

data class JurisdictionGuidance(
    val suggestedNextSteps: List<String> = emptyList(),
    val categoryGuidance: Map<String, String> = emptyMap()
) {
    fun nextStepForCategory(category: String): String = categoryGuidance[category] ?: "seek qualified legal counsel"
    fun suggestedNextSteps(): List<String> = suggestedNextSteps
}

/**
 * Legal Session Manager: Manages sealed advisory sessions.
 */
class LegalSessionManager(val storagePath: String, val vault: EvidenceVault) {
    fun createSession(reportHash: String, metadata: SessionMetadata): String = UUID.randomUUID().toString()
    fun getSession(sessionId: String): LegalSession? = null
    fun addExchange(sessionId: String, question: String, response: String): SessionResponse =
        SessionResponse("", "", "", Instant.now(), "")
    fun closeSession(sessionId: String): SessionTranscript =
        SessionTranscript("", "", Instant.now(), "", 0)
}

data class LegalSession(
    val sessionId: String,
    val reportHash: String,
    val metadata: SessionMetadata,
    val exchanges: List<SessionExchange> = emptyList(),
    val createdAt: Instant = Instant.now()
)

data class SessionExchange(
    val question: String,
    val response: String,
    val timestamp: Instant
)

data class SessionTranscript(
    val sessionId: String,
    val transcriptHash: String,
    val closedAt: Instant,
    val reportHash: String,
    val exchangeCount: Int
)

/**
 * Sealed Document Generator: Creates PDFs with watermarks and hashes.
 */
class SealedDocumentGenerator(val apkRootHash: String, val vault: EvidenceVault) {
    fun generateDocument(
        advisory: AdvisoryResponse,
        letterType: String,
        recipient: String
    ): SealedAdvisoryDocument = SealedAdvisoryDocument(
        documentHash = "",
        content = ByteArray(0),
        watermark = "Generated from sealed forensic summary",
        reportHashReference = advisory.reportHash,
        generatedAt = Instant.now()
    )
}
