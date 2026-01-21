package org.verumomnis.legal.jurisdictions

import org.verumomnis.legal.api.GPSCoordinate
import org.verumomnis.legal.api.FindingSummary
import java.util.*

/**
 * GPS-BASED JURISDICTION ROUTER
 *
 * Maps evidence coordinates to applicable jurisdictions.
 * All uploaded evidence includes GPS coordinates for location-aware routing.
 *
 * Workflow:
 * 1. Evidence is geotagged (from device, metadata, EXIF, timestamps)
 * 2. Coordinates added to SealedForensicSummary.gpsCoordinates
 * 3. Router infers primary jurisdiction from GPS
 * 4. Applies jurisdiction-specific guidance rules
 * 5. Returns multi-jurisdiction recommendations if evidence spans borders
 */
class GPSJurisdictionRouter {

    /**
     * Determine applicable jurisdictions from GPS coordinates.
     * Returns list of jurisdictions where evidence occurred.
     */
    fun determineJurisdictionsFromGPS(coordinates: List<GPSCoordinate>): List<String> {
        val jurisdictions = mutableSetOf<String>()

        coordinates.forEach { coord ->
            val inferred = coord.inferJurisdiction()
            if (inferred != "UNKNOWN") {
                jurisdictions.add(inferred)
            }
        }

        return jurisdictions.toList().sorted()
    }

    /**
     * Get primary jurisdiction (most evidence locations).
     */
    fun getPrimaryJurisdiction(coordinates: List<GPSCoordinate>): String {
        val jurisdictions = determineJurisdictionsFromGPS(coordinates)

        return when {
            jurisdictions.isEmpty() -> "UNKNOWN"
            jurisdictions.size == 1 -> jurisdictions[0]
            else -> {
                // Count evidence by jurisdiction
                val jurisdictionCounts = mutableMapOf<String, Int>()
                coordinates.forEach { coord ->
                    val j = coord.inferJurisdiction()
                    if (j != "UNKNOWN") {
                        jurisdictionCounts[j] = (jurisdictionCounts[j] ?: 0) + 1
                    }
                }
                // Return jurisdiction with most evidence
                jurisdictionCounts.maxByOrNull { it.value }?.key ?: "UNKNOWN"
            }
        }
    }

    /**
     * Get jurisdiction-specific legal guidance for findings.
     */
    fun getJurisdictionGuidance(
        jurisdiction: String,
        findings: List<FindingSummary>,
        integrityScore: Int
    ): String = when (jurisdiction) {
        "ZA" -> getZAGuidance(findings, integrityScore)
        "UAE" -> getUAEGuidance(findings, integrityScore)
        "SA" -> getSAGuidance(findings, integrityScore)
        "EU" -> getEUGuidance(findings, integrityScore)
        else -> getUniversalGuidance(findings, integrityScore)
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // SOUTH AFRICA (ZA) - Companies Act 2008, POPIA 2020
    // ═══════════════════════════════════════════════════════════════════════════════

    private fun getZAGuidance(findings: List<FindingSummary>, integrityScore: Int): String {
        val guidance = StringBuilder()
        guidance.append("SOUTH AFRICA JURISDICTION GUIDANCE (GPS-Determined)\n")
        guidance.append("═══════════════════════════════════════════════════════════════════════════════\n\n")

        findings.forEach { finding ->
            when (finding.category.lowercase()) {
                "fiduciary_breach" -> {
                    guidance.append("FIDUCIARY DUTY BREACH (Companies Act s76):\n")
                    guidance.append("• Directors and officers owe fiduciary duties to the company and shareholders\n")
                    guidance.append("• Common remedies: Interdict (restraining order), damages, account of profits\n")
                    guidance.append("• Evidence suggests ")
                    guidance.append(if (integrityScore < 40) "serious breach indicators" else "moderate concerns")
                    guidance.append("\n• Next step: Consider application to Companies Tribunal or High Court\n\n")
                }
                "fraud_indicators" -> {
                    guidance.append("FRAUD ALLEGATIONS (Criminal Law):\n")
                    guidance.append("• Threshold: Unlawful, intentional misrepresentation causing loss\n")
                    guidance.append("• Report to: SAPS or financial regulator (FSCA)\n")
                    guidance.append("• Criminal procedure may precede civil claims\n")
                    guidance.append("• Preservation: Document chain of custody (already sealed)\n\n")
                }
                "contract_violation" -> {
                    guidance.append("BREACH OF CONTRACT (Law of Contract):\n")
                    guidance.append("• Civil remedy available in High Court or Magistrate's Court\n")
                    guidance.append("• Remedies: Specific performance, damages, cancellation\n")
                    guidance.append("• Consider alternative dispute resolution (arbitration, mediation)\n\n")
                }
            }
        }

        guidance.append("POPIA COMPLIANCE (Personal Information):\n")
        guidance.append("• All evidence handling must comply with POPIA 2020\n")
        guidance.append("• Forensic summary is sealed and not exported without consent\n")
        guidance.append("• Chain of custody maintained throughout legal process\n")

        return guidance.toString()
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // UNITED ARAB EMIRATES (UAE) - Cybercrime Law 2012, VAT Law
    // ═══════════════════════════════════════════════════════════════════════════════

    private fun getUAEGuidance(findings: List<FindingSummary>, integrityScore: Int): String {
        val guidance = StringBuilder()
        guidance.append("UNITED ARAB EMIRATES JURISDICTION GUIDANCE (GPS-Determined)\n")
        guidance.append("═══════════════════════════════════════════════════════════════════════════════\n\n")

        findings.forEach { finding ->
            when (finding.category.lowercase()) {
                "fraud_indicators" -> {
                    guidance.append("CYBERCRIME & FRAUD (UAE Cybercrime Law 2012):\n")
                    guidance.append("• Criminal penalties: Imprisonment and/or fines\n")
                    guidance.append("• Report to: Dubai Police (General Command), local prosecution\n")
                    guidance.append("• Jurisdiction: Federal Court system\n")
                    guidance.append("• Sharia law may apply to certain civil remedies\n\n")
                }
                "financial_anomaly" -> {
                    guidance.append("FINANCIAL IRREGULARITIES (VAT Law, Commercial Law):\n")
                    guidance.append("• Regulatory authority: FTA (Federal Tax Authority)\n")
                    guidance.append("• Possible violations: VAT evasion, commercial fraud\n")
                    guidance.append("• Administrative and criminal penalties available\n\n")
                }
                "shareholder_oppression" -> {
                    guidance.append("SHAREHOLDER DISPUTES (UAE Commercial Law):\n")
                    guidance.append("• DIFC (Dubai International Financial Centre) courts available\n")
                    guidance.append("• English law often applies in DIFC cases\n")
                    guidance.append("• Alternative: General Federal Courts\n\n")
                }
            }
        }

        guidance.append("LANGUAGE & TRANSLATION:\n")
        guidance.append("• Official documents may require Arabic translation\n")
        guidance.append("• Certified translator required for court submission\n")

        return guidance.toString()
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // SAUDI ARABIA (SA) - ECT Act 2002, Shariah Governance
    // ═══════════════════════════════════════════════════════════════════════════════

    private fun getSAGuidance(findings: List<FindingSummary>, integrityScore: Int): String {
        val guidance = StringBuilder()
        guidance.append("SAUDI ARABIA JURISDICTION GUIDANCE (GPS-Determined)\n")
        guidance.append("═══════════════════════════════════════════════════════════════════════════════\n\n")

        guidance.append("GENERAL FRAMEWORK:\n")
        guidance.append("• Primary law: Saudi Commercial Code & ECT Act 2002\n")
        guidance.append("• Shariah law applies to interpretation of contracts\n")
        guidance.append("• Regulatory body: SAMA (Saudi Arabian Monetary Authority)\n\n")

        findings.forEach { finding ->
            when (finding.category.lowercase()) {
                "fraud_indicators" -> {
                    guidance.append("FRAUD & DECEPTION (Shariah + Commercial Law):\n")
                    guidance.append("• Criminal court jurisdiction (not Shariah courts)\n")
                    guidance.append("• Report to: General Court or SAMA (if financial)\n")
                    guidance.append("• Remedies: Restitution, imprisonment\n\n")
                }
                "contract_violation" -> {
                    guidance.append("CONTRACT DISPUTES (Shariah-Compliant):\n")
                    guidance.append("• Contract must not violate Shariah principles\n")
                    guidance.append("• Interest (riba) not enforceable\n")
                    guidance.append("• Courts will interpret per Islamic law principles\n\n")
                }
            }
        }

        guidance.append("ELECTRONIC EVIDENCE (ECT Act 2002):\n")
        guidance.append("• Digital evidence is admissible in Saudi courts\n")
        guidance.append("• Signature and seal requirements in Shariah format\n")
        guidance.append("• Chain of custody critical for acceptance\n")

        return guidance.toString()
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // EUROPEAN UNION (EU) - GDPR 2016/679, Data Protection
    // ═══════════════════════════════════════════════════════════════════════════════

    private fun getEUGuidance(findings: List<FindingSummary>, integrityScore: Int): String {
        val guidance = StringBuilder()
        guidance.append("EUROPEAN UNION JURISDICTION GUIDANCE (GPS-Determined)\n")
        guidance.append("═══════════════════════════════════════════════════════════════════════════════\n\n")

        guidance.append("GDPR COMPLIANCE (2016/679):\n")
        guidance.append("• Personal data handling strictly regulated\n")
        guidance.append("• Data minimization principle: Only necessary data processed\n")
        guidance.append("• Right to erasure ('right to be forgotten') may apply\n")
        guidance.append("• Sealed summary does not export personal data\n\n")

        findings.forEach { finding ->
            when (finding.category.lowercase()) {
                "fraud_indicators" -> {
                    guidance.append("FRAUD & CYBERCRIME (EU Cybercrime Directive):\n")
                    guidance.append("• Criminal law: Varies by member state\n")
                    guidance.append("• Report to: Local police or prosecuting authority\n")
                    guidance.append("• EU-wide arrest warrant possible (serious cases)\n\n")
                }
                "data_breach" -> {
                    guidance.append("DATA PROTECTION VIOLATIONS (GDPR):\n")
                    guidance.append("• Regulatory authority: National Data Protection Authority\n")
                    guidance.append("• Fines: Up to €20 million or 4% of global revenue\n")
                    guidance.append("• Victim: Right to compensation\n\n")
                }
            }
        }

        guidance.append("ADMISSIBILITY OF DIGITAL EVIDENCE:\n")
        guidance.append("• eIDAS Regulation governs electronic identification\n")
        guidance.append("• Digital signatures and seals recognized\n")
        guidance.append("• Chain of custody essential for court acceptance\n")

        return guidance.toString()
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // UNIVERSAL GUIDANCE (No specific jurisdiction detected)
    // ═══════════════════════════════════════════════════════════════════════════════

    private fun getUniversalGuidance(findings: List<FindingSummary>, integrityScore: Int): String {
        val guidance = StringBuilder()
        guidance.append("UNIVERSAL JURISDICTION GUIDANCE\n")
        guidance.append("═══════════════════════════════════════════════════════════════════════════════\n\n")

        guidance.append("GPS COORDINATES NOT MAPPED TO SPECIFIC JURISDICTION\n")
        guidance.append("• Consider location where evidence was created/collected\n")
        guidance.append("• Consider location of parties (domicile, residence, principal place of business)\n")
        guidance.append("• Consider location where harm occurred\n\n")

        guidance.append("COMMON REMEDIES ACROSS JURISDICTIONS:\n")
        guidance.append("• Civil remedies: Damages, restitution, specific performance\n")
        guidance.append("• Criminal remedies: Prosecution, imprisonment, fines\n")
        guidance.append("• Administrative remedies: Regulatory sanctions, license revocation\n\n")

        guidance.append("NEXT STEPS:\n")
        guidance.append("1. Confirm primary jurisdiction based on party locations\n")
        guidance.append("2. Consult qualified attorney in that jurisdiction\n")
        guidance.append("3. Provide sealed forensic summary as supporting evidence\n")
        guidance.append("4. Chain of custody maintained throughout legal process\n")

        return guidance.toString()
    }

    /**
     * Generate summary of cross-border implications.
     */
    fun generateCrossBorderAnalysis(coordinates: List<GPSCoordinate>): String {
        val jurisdictions = determineJurisdictionsFromGPS(coordinates)

        return when {
            jurisdictions.isEmpty() -> "No jurisdictions determined from GPS coordinates."
            jurisdictions.size == 1 -> "Evidence is localized to ${jurisdictions[0]} jurisdiction."
            else -> {
                val jurisdictionText = jurisdictions.joinToString(", ")
                "CROSS-BORDER CASE: Evidence spans $jurisdictionText\n" +
                "• Complexity: Multiple jurisdictions may apply\n" +
                "• Conflict of laws: Determine which law governs\n" +
                "• Forum: Consider which court has jurisdiction\n" +
                "• Coordination: May require multiple legal actions"
            }
        }
    }
}
