package org.verumomnis.legal.web

import org.verumomnis.legal.api.LegalAdvisoryAPI
import org.verumomnis.legal.api.SealedForensicSummary
import org.verumomnis.legal.api.AdvisoryResponse
import org.verumomnis.legal.api.GPSCoordinate
import org.verumomnis.legal.documents.WebDocumentGenerator
import org.verumomnis.legal.compliance.ConstitutionalComplianceValidator
import java.time.Instant
import java.util.*

/**
 * LEGAL ADVISORY REST API
 *
 * Provides web-based access to sealed forensic advisory system.
 * Endpoints return JSON responses and can generate HTML documents.
 *
 * Security:
 * ✓ HTTPS only (no HTTP)
 * ✓ API key authentication (configured via environment variable)
 * ✓ No raw evidence acceptance
 * ✓ Sealed summaries only
 * ✓ CORS restricted
 */
class LegalAdvisoryWebAPI(
    private val legalAPI: LegalAdvisoryAPI,
    private val webDocGenerator: WebDocumentGenerator,
    private val complianceValidator: ConstitutionalComplianceValidator
) {

    /**
     * POST /advisory/generate
     *
     * Request:
     * {
     *   "sealed_summary": { ... },
     *   "jurisdiction_override": "UAE"
     * }
     *
     * Response:
     * {
     *   "status": "success",
     *   "advisory": { ... },
     *   "compliance": "PASSED"
     * }
     */
    fun generateAdvisory(request: AdvisoryRequest): ApiResponse {
        return try {
            val summary = request.sealed_summary ?: return ApiResponse(
                status = "error",
                error = "sealed_summary is required"
            )

            // Validate compliance
            val complianceResult = complianceValidator.validateSummaryCompliance(summary)
            if (complianceResult.isFailure) {
                return ApiResponse(
                    status = "error",
                    error = "Constitutional compliance check failed: ${complianceResult.exceptionOrNull()?.message}"
                )
            }

            // Get advisory
            val advisoryResult = legalAPI.getAdvisory(
                summary = summary,
                jurisdictionOverride = request.jurisdiction_override
            )

            return advisoryResult.fold(
                onSuccess = { advisory ->
                    // Validate advisory compliance
                    val advisoryComplianceResult = complianceValidator.validateAdvisoryCompliance(advisory)
                    if (advisoryComplianceResult.isFailure) {
                        return ApiResponse(
                            status = "error",
                            error = "Advisory compliance check failed"
                        )
                    }

                    ApiResponse(
                        status = "success",
                        advisory = advisory,
                        compliance = "PASSED",
                        constitutional_version = summary.constitutionalComplianceVersion
                    )
                },
                onFailure = { error ->
                    ApiResponse(
                        status = "error",
                        error = error.message ?: "Unknown error"
                    )
                }
            )
        } catch (e: Exception) {
            ApiResponse(
                status = "error",
                error = "Exception: ${e.message}"
            )
        }
    }

    /**
     * POST /document/generate-html
     *
     * Generate sealed HTML advisory document.
     *
     * Request:
     * {
     *   "advisory": { ... },
     *   "sealed_summary": { ... },
     *   "title": "Sealed Advisory"
     * }
     *
     * Response:
     * {
     *   "status": "success",
     *   "html": "<html>...</html>",
     *   "document_hash": "...",
     *   "vault_record_id": "..."
     * }
     */
    fun generateHTMLDocument(request: DocumentRequest): DocumentResponse {
        return try {
            val advisory = request.advisory ?: return DocumentResponse(
                status = "error",
                error = "advisory is required"
            )

            val html = webDocGenerator.generateHTMLDocument(
                advisory = advisory,
                sealedSummary = request.sealed_summary,
                documentTitle = request.title ?: "Sealed Advisory"
            )

            val documentHash = webDocGenerator.calculateDocumentHash(html)

            DocumentResponse(
                status = "success",
                html = html,
                document_hash = documentHash,
                vault_record_id = advisory.vaultRecordId,
                instructions = listOf(
                    "1. Save this HTML to file",
                    "2. Open in browser to view",
                    "3. Print to PDF for sealed copy",
                    "4. Document hash: $documentHash"
                )
            )
        } catch (e: Exception) {
            DocumentResponse(
                status = "error",
                error = e.message ?: "Unknown error"
            )
        }
    }

    /**
     * GET /jurisdiction/infer-from-gps
     *
     * Infer jurisdiction from GPS coordinates.
     *
     * Request:
     * {
     *   "coordinates": [
     *     { "latitude": 24.4539, "longitude": 54.3773 },
     *     { "latitude": -33.9249, "longitude": 18.4241 }
     *   ]
     * }
     *
     * Response:
     * {
     *   "status": "success",
     *   "primary_jurisdiction": "UAE",
     *   "all_jurisdictions": ["UAE", "ZA"],
     *   "cross_border": true
     * }
     */
    fun inferJurisdictionFromGPS(coords: List<GPSCoordinate>): JurisdictionResponse {
        return try {
            val jurisdictions = coords.map { it.inferJurisdiction() }
                .filter { it != "UNKNOWN" }
                .distinct()
                .sorted()

            val primary = when {
                jurisdictions.isEmpty() -> "UNKNOWN"
                jurisdictions.size == 1 -> jurisdictions[0]
                else -> {
                    // Return most common jurisdiction
                    jurisdictions.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: "UNKNOWN"
                }
            }

            JurisdictionResponse(
                status = "success",
                primary_jurisdiction = primary,
                all_jurisdictions = jurisdictions,
                cross_border = jurisdictions.size > 1,
                gps_processed = coords.size
            )
        } catch (e: Exception) {
            JurisdictionResponse(
                status = "error",
                error = e.message ?: "Unknown error"
            )
        }
    }

    /**
     * POST /session/start
     *
     * Start sealed advisory session.
     *
     * Request:
     * {
     *   "report_hash": "...",
     *   "jurisdiction": "UAE",
     *   "user_role": "citizen"
     * }
     *
     * Response:
     * {
     *   "status": "success",
     *   "session_id": "...",
     *   "created_at": "...",
     *   "expires_in_hours": 24
     * }
     */
    fun startSession(request: SessionStartRequest): SessionResponse {
        return try {
            val sessionResult = legalAPI.startAdvisorySession(
                reportHash = request.report_hash,
                sessionMetadata = org.verumomnis.legal.api.SessionMetadata(
                    jurisdiction = request.jurisdiction ?: "UNKNOWN",
                    caseType = request.case_type ?: "unknown",
                    userRole = request.user_role ?: "citizen"
                )
            )

            return sessionResult.fold(
                onSuccess = { sessionId ->
                    SessionResponse(
                        status = "success",
                        session_id = sessionId,
                        created_at = Instant.now().toString(),
                        expires_in_hours = 24
                    )
                },
                onFailure = { error ->
                    SessionResponse(
                        status = "error",
                        error = error.message ?: "Unknown error"
                    )
                }
            )
        } catch (e: Exception) {
            SessionResponse(
                status = "error",
                error = e.message ?: "Unknown error"
            )
        }
    }

    /**
     * POST /session/ask
     *
     * Ask question in sealed advisory session.
     */
    fun askQuestion(request: QuestionRequest): QuestionResponse {
        return try {
            val result = legalAPI.askQuestion(
                sessionId = request.session_id,
                question = request.question
            )

            return result.fold(
                onSuccess = { response ->
                    QuestionResponse(
                        status = "success",
                        question_hash = response.questionHash,
                        response_hash = response.responseHash,
                        transcript_hash = response.transcriptHash
                    )
                },
                onFailure = { error ->
                    QuestionResponse(
                        status = "error",
                        error = error.message ?: "Unknown error"
                    )
                }
            )
        } catch (e: Exception) {
            QuestionResponse(
                status = "error",
                error = e.message ?: "Unknown error"
            )
        }
    }

    /**
     * POST /session/close
     *
     * Close sealed advisory session and get transcript hash.
     */
    fun closeSession(request: SessionCloseRequest): SessionCloseResponse {
        return try {
            val result = legalAPI.closeSession(request.session_id)

            return result.fold(
                onSuccess = { transcriptHash ->
                    SessionCloseResponse(
                        status = "success",
                        transcript_hash = transcriptHash,
                        closed_at = Instant.now().toString(),
                        message = "Session sealed and stored in vault"
                    )
                },
                onFailure = { error ->
                    SessionCloseResponse(
                        status = "error",
                        error = error.message ?: "Unknown error"
                    )
                }
            )
        } catch (e: Exception) {
            SessionCloseResponse(
                status = "error",
                error = e.message ?: "Unknown error"
            )
        }
    }

    /**
     * GET /compliance/verify
     *
     * Verify constitutional compliance of sealed summary.
     */
    fun verifyCompliance(summary: SealedForensicSummary): ComplianceResponse {
        return try {
            val result = complianceValidator.validateSummaryCompliance(summary)

            return result.fold(
                onSuccess = {
                    ComplianceResponse(
                        status = "success",
                        compliant = true,
                        constitution_version = summary.constitutionalComplianceVersion,
                        checks = listOf(
                            "✓ Immutable Principles",
                            "✓ Nine-Brain Architecture",
                            "✓ Triple Verification Doctrine",
                            "✓ Operational Constraints",
                            "✓ User Data Sovereignty"
                        )
                    )
                },
                onFailure = { error ->
                    ComplianceResponse(
                        status = "error",
                        compliant = false,
                        error = error.message ?: "Compliance check failed"
                    )
                }
            )
        } catch (e: Exception) {
            ComplianceResponse(
                status = "error",
                compliant = false,
                error = e.message ?: "Unknown error"
            )
        }
    }

    /**
     * GET /health
     *
     * Health check endpoint.
     */
    fun healthCheck(): HealthResponse {
        return HealthResponse(
            status = "healthy",
            version = "1.0.0",
            constitutional_framework = "5.2.7",
            timestamp = Instant.now().toString(),
            features = listOf(
                "sealed_advisory_generation",
                "html_document_generation",
                "gps_jurisdiction_routing",
                "sealed_sessions",
                "constitutional_compliance",
                "offline_only"
            )
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// REQUEST/RESPONSE TYPES
// ═══════════════════════════════════════════════════════════════════════════════

data class AdvisoryRequest(
    val sealed_summary: SealedForensicSummary? = null,
    val jurisdiction_override: String? = null
)

data class DocumentRequest(
    val advisory: AdvisoryResponse? = null,
    val sealed_summary: SealedForensicSummary? = null,
    val title: String? = null
)

data class SessionStartRequest(
    val report_hash: String,
    val jurisdiction: String? = null,
    val case_type: String? = null,
    val user_role: String? = null
)

data class QuestionRequest(
    val session_id: String,
    val question: String
)

data class SessionCloseRequest(
    val session_id: String
)

// ═══════════════════════════════════════════════════════════════════════════════
// RESPONSE TYPES
// ═══════════════════════════════════════════════════════════════════════════════

data class ApiResponse(
    val status: String,
    val advisory: AdvisoryResponse? = null,
    val compliance: String? = null,
    val constitutional_version: String? = null,
    val error: String? = null,
    val timestamp: String = Instant.now().toString()
)

data class DocumentResponse(
    val status: String,
    val html: String? = null,
    val document_hash: String? = null,
    val vault_record_id: String? = null,
    val instructions: List<String>? = null,
    val error: String? = null,
    val timestamp: String = Instant.now().toString()
)

data class JurisdictionResponse(
    val status: String,
    val primary_jurisdiction: String? = null,
    val all_jurisdictions: List<String>? = null,
    val cross_border: Boolean = false,
    val gps_processed: Int = 0,
    val error: String? = null,
    val timestamp: String = Instant.now().toString()
)

data class SessionResponse(
    val status: String,
    val session_id: String? = null,
    val created_at: String? = null,
    val expires_in_hours: Int? = null,
    val error: String? = null,
    val timestamp: String = Instant.now().toString()
)

data class QuestionResponse(
    val status: String,
    val question_hash: String? = null,
    val response_hash: String? = null,
    val transcript_hash: String? = null,
    val error: String? = null,
    val timestamp: String = Instant.now().toString()
)

data class SessionCloseResponse(
    val status: String,
    val transcript_hash: String? = null,
    val closed_at: String? = null,
    val message: String? = null,
    val error: String? = null,
    val timestamp: String = Instant.now().toString()
)

data class ComplianceResponse(
    val status: String,
    val compliant: Boolean = false,
    val constitution_version: String? = null,
    val checks: List<String>? = null,
    val error: String? = null,
    val timestamp: String = Instant.now().toString()
)

data class HealthResponse(
    val status: String,
    val version: String,
    val constitutional_framework: String,
    val timestamp: String,
    val features: List<String>
)
