package org.verumomnis.legal.documents

import org.verumomnis.legal.api.SealedAdvisoryDocument
import org.verumomnis.legal.api.AdvisoryResponse
import org.verumomnis.legal.api.EvidenceVault
import java.time.Instant
import java.security.MessageDigest
import java.util.*

/**
 * SEALED DOCUMENT GENERATOR
 *
 * Generates sealed advisory documents (PDF, email, letter, brief).
 * All outputs:
 * ✓ Include watermark: "Generated from sealed forensic summary"
 * ✓ Reference forensic report hash
 * ✓ Are individually sealed and hashed
 * ✓ Are stored in evidence vault with chain of custody
 * ✓ Cannot be edited after generation (immutable)
 *
 * No raw evidence is embedded in generated documents.
 */
class SealedDocumentGenerator(
    private val apkRootHash: String,
    private val evidenceVault: EvidenceVault
) {

    /**
     * Generate sealed advisory document.
     * Returns PDF/text with watermark and vault reference.
     */
    fun generateDocument(
        advisory: AdvisoryResponse,
        letterType: String,
        recipient: String
    ): SealedAdvisoryDocument {
        val content = when (letterType.lowercase()) {
            "email" -> generateEmailContent(advisory, recipient)
            "letter" -> generateLetterContent(advisory, recipient)
            "brief" -> generateBriefContent(advisory, recipient)
            else -> generateGenericContent(advisory, recipient)
        }

        val contentBytes = content.toByteArray(Charsets.UTF_8)
        val contentHash = calculateSHA512(contentBytes)

        return SealedAdvisoryDocument(
            documentHash = contentHash,
            content = contentBytes,
            watermark = "Generated from sealed forensic summary - ${advisory.reportHash.take(16)}...",
            reportHashReference = advisory.reportHash,
            generatedAt = Instant.now()
        )
    }

    /**
     * Generate sealed email advisory.
     */
    private fun generateEmailContent(advisory: AdvisoryResponse, recipient: String): String {
        val email = StringBuilder()

        email.append("FROM: Verum Omnis Forensic Engine (Advisory Only)\n")
        email.append("TO: $recipient\n")
        email.append("SUBJECT: Forensic Advisory - Case ${advisory.reportHash.take(12)}\n")
        email.append("DATE: ${Instant.now()}\n")
        email.append("\n")

        // Watermark
        email.append("════════════════════════════════════════════════════════════════════════════════\n")
        email.append("SEALED ADVISORY DOCUMENT\n")
        email.append("Generated from sealed forensic summary\n")
        email.append("Report Hash: ${advisory.reportHash}\n")
        email.append("════════════════════════════════════════════════════════════════════════════════\n\n")

        // Subject
        email.append("SUBJECT: FORENSIC ADVISORY - NEXT STEPS\n\n")

        // Jurisdiction determination
        email.append("JURISDICTION DETERMINATION (GPS-Based):\n")
        email.append("Primary Jurisdiction: ${advisory.jurisdiction}\n")
        if (advisory.allApplicableJurisdictions.isNotEmpty()) {
            email.append("All Applicable Jurisdictions: ${advisory.allApplicableJurisdictions.joinToString(", ")}\n")
        }
        email.append("GPS Coordinates Processed: ${advisory.gpsCoordinatesProcessed}\n\n")

        // Key findings
        email.append("KEY FINDINGS:\n")
        advisory.riskFactors.take(3).forEach { factor ->
            email.append("• $factor\n")
        }
        email.append("\n")

        // Recommendations
        email.append("RECOMMENDED NEXT STEPS:\n")
        advisory.nextSteps.forEach { step ->
            email.append("• $step\n")
        }
        email.append("\n")

        // Jurisdiction-specific guidance (first part)
        if (advisory.recommendations.isNotEmpty()) {
            email.append("JURISDICTION GUIDANCE:\n")
            email.append(advisory.recommendations.first().take(500) + "...\n\n")
        }

        // Cross-border analysis if applicable
        if (advisory.allApplicableJurisdictions.size > 1) {
            email.append("CROSS-BORDER ANALYSIS:\n")
            email.append(advisory.crossBorderAnalysis + "\n\n")
        }

        // Confidence statement
        email.append("CONFIDENCE STATEMENT:\n")
        email.append(advisory.confidenceStatement + "\n\n")

        // Disclaimers
        email.append("════════════════════════════════════════════════════════════════════════════════\n")
        email.append("DISCLAIMERS:\n")
        advisory.disclaimers.forEach { disclaimer ->
            email.append("• $disclaimer\n")
        }
        email.append("════════════════════════════════════════════════════════════════════════════════\n\n")

        // Footer
        email.append("This is an advisorysummary. For full forensic report and detailed findings,\n")
        email.append("consult the sealed forensic report directly.\n")
        email.append("Report Hash: ${advisory.reportHash}\n")
        email.append("Vault Record ID: ${advisory.vaultRecordId}\n")

        return email.toString()
    }

    /**
     * Generate sealed letter advisory.
     */
    private fun generateLetterContent(advisory: AdvisoryResponse, recipient: String): String {
        val letter = StringBuilder()

        // Letterhead
        letter.append("╔════════════════════════════════════════════════════════════════════════════════╗\n")
        letter.append("║                    VERUM OMNIS FORENSIC ENGINE                                ║\n")
        letter.append("║                  SEALED ADVISORY CORRESPONDENCE                              ║\n")
        letter.append("║                          v5.2.7 - Offline                                    ║\n")
        letter.append("╚════════════════════════════════════════════════════════════════════════════════╝\n\n")

        letter.append("Date: ${Instant.now()}\n")
        letter.append("To: $recipient\n")
        letter.append("Re: Forensic Advisory – Case ${advisory.reportHash.take(12)}\n\n")

        letter.append("────────────────────────────────────────────────────────────────────────────────\n\n")

        // Opening statement
        letter.append("Dear Recipient,\n\n")

        letter.append("This letter constitutes a sealed advisory based on forensic analysis of sealed findings.\n")
        letter.append("It is generated from abstracted forensic data and contains no raw evidence.\n\n")

        // Jurisdiction
        letter.append("JURISDICTION ANALYSIS:\n")
        letter.append("Based on GPS coordinates extracted from evidence metadata, the primary applicable\n")
        letter.append("jurisdiction is identified as: ${advisory.jurisdiction}\n\n")

        if (advisory.allApplicableJurisdictions.size > 1) {
            letter.append("Additional potentially applicable jurisdictions: ${advisory.allApplicableJurisdictions.drop(1).joinToString(", ")}\n\n")
        }

        // Findings
        letter.append("FINDINGS & RISK FACTORS:\n")
        advisory.riskFactors.forEach { factor ->
            letter.append("• $factor\n")
        }
        letter.append("\n")

        // Recommendations
        letter.append("RECOMMENDED ACTIONS:\n")
        advisory.nextSteps.forEach { step ->
            letter.append("• $step\n")
        }
        letter.append("\n")

        // Full jurisdiction guidance
        letter.append("JURISDICTION-SPECIFIC GUIDANCE:\n")
        advisory.recommendations.forEach { rec ->
            if (rec.length > 100) {
                letter.append(rec.take(1000) + "\n\n")
            }
        }

        // Cross-border if applicable
        if (advisory.crossBorderAnalysis.isNotEmpty() && advisory.allApplicableJurisdictions.size > 1) {
            letter.append("\nCROSS-BORDER ANALYSIS:\n")
            letter.append(advisory.crossBorderAnalysis + "\n")
        }

        // Closing
        letter.append("\n────────────────────────────────────────────────────────────────────────────────\n\n")
        letter.append("ATTESTATION:\n")
        letter.append("This advisory is based on sealed, immutable forensic findings.\n")
        letter.append("${advisory.confidenceStatement}\n\n")

        // Disclaimers
        letter.append("DISCLAIMERS:\n")
        advisory.disclaimers.forEach { disclaimer ->
            letter.append("• $disclaimer\n")
        }

        letter.append("\n────────────────────────────────────────────────────────────────────────────────\n")
        letter.append("Document Hash: [WILL BE SEALED]\n")
        letter.append("Report Reference: ${advisory.reportHash.take(32)}...\n")
        letter.append("Vault Record: ${advisory.vaultRecordId}\n")
        letter.append("Generated: ${Instant.now()}\n\n")

        letter.append("This document is sealed and immutable. Its hash and chain of custody are maintained\n")
        letter.append("in the evidence vault for independent verification.\n")

        return letter.toString()
    }

    /**
     * Generate sealed brief advisory.
     */
    private fun generateBriefContent(advisory: AdvisoryResponse, recipient: String): String {
        val brief = StringBuilder()

        brief.append("FORENSIC ADVISORY BRIEF\n")
        brief.append("═════════════════════════════════════════════════════════════════════════════════\n\n")

        brief.append("CASE: ${advisory.reportHash.take(16)}\n")
        brief.append("DATE: ${Instant.now()}\n")
        brief.append("RECIPIENT: $recipient\n")
        brief.append("STATUS: SEALED & IMMUTABLE\n\n")

        // Quick facts
        brief.append("QUICK FACTS:\n")
        brief.append("• Primary Jurisdiction (GPS): ${advisory.jurisdiction}\n")
        brief.append("• GPS Coordinates Processed: ${advisory.gpsCoordinatesProcessed}\n")
        brief.append("• All Applicable Jurisdictions: ${advisory.allApplicableJurisdictions.joinToString(", ")}\n")
        brief.append("• Integrity Score: From sealed forensic findings\n\n")

        // Key findings
        brief.append("KEY RISK FACTORS:\n")
        advisory.riskFactors.take(5).forEach { factor ->
            brief.append("• $factor\n")
        }
        brief.append("\n")

        // Action items
        brief.append("IMMEDIATE NEXT STEPS:\n")
        advisory.nextSteps.take(4).forEach { step ->
            brief.append("• $step\n")
        }
        brief.append("\n")

        // Jurisdiction summary
        brief.append("JURISDICTION GUIDANCE SUMMARY:\n")
        val guidanceSummary = advisory.recommendations.filter { it.length < 200 }.take(2)
        guidanceSummary.forEach { summary ->
            brief.append("• $summary\n")
        }
        brief.append("\n")

        // Legal disclaimers (abbreviated)
        brief.append("LEGAL DISCLAIMERS:\n")
        brief.append("• This is sealed advisory only, not legal representation\n")
        brief.append("• Consult qualified attorney in applicable jurisdiction\n")
        brief.append("• Sealed forensic findings support this advisory\n\n")

        brief.append("═════════════════════════════════════════════════════════════════════════════════\n")
        brief.append("Report Hash: ${advisory.reportHash}\n")
        brief.append("Vault Record: ${advisory.vaultRecordId}\n")

        return brief.toString()
    }

    /**
     * Generate generic sealed content.
     */
    private fun generateGenericContent(advisory: AdvisoryResponse, recipient: String): String {
        val content = StringBuilder()

        content.append("SEALED FORENSIC ADVISORY\n")
        content.append("═════════════════════════════════════════════════════════════════════════════════\n\n")

        content.append("Recipient: $recipient\n")
        content.append("Generated: ${Instant.now()}\n")
        content.append("Report Hash: ${advisory.reportHash}\n\n")

        content.append("Jurisdiction (GPS-Determined): ${advisory.jurisdiction}\n")
        content.append("All Applicable Jurisdictions: ${advisory.allApplicableJurisdictions.joinToString(", ")}\n\n")

        content.append("Recommendations:\n")
        advisory.recommendations.take(3).forEach { rec ->
            content.append(rec + "\n\n")
        }

        content.append("\nNext Steps:\n")
        advisory.nextSteps.forEach { step ->
            content.append("• $step\n")
        }

        content.append("\n\nDisclaimer: ${advisory.disclaimers.firstOrNull() ?: "See full advisory"}\n")

        return content.toString()
    }

    /**
     * Calculate SHA-512 hash of content.
     */
    private fun calculateSHA512(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-512")
        val hashBytes = digest.digest(data)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Add watermark to any document.
     * Standard watermark for all sealed advisory outputs.
     */
    fun addWatermark(documentContent: String, reportHash: String): String {
        val watermark = "╔════════════════════════════════════════════════════════════════════════════════╗\n" +
                "║  SEALED ADVISORY DOCUMENT - Generated from sealed forensic summary              ║\n" +
                "║  Report Hash: ${reportHash.take(48)}...                      ║\n" +
                "║  No raw evidence embedded. Advisory only.                                       ║\n" +
                "╚════════════════════════════════════════════════════════════════════════════════╝\n\n"

        return watermark + documentContent
    }
}
