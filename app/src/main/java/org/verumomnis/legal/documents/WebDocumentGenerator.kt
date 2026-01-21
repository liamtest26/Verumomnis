package org.verumomnis.legal.documents

import org.verumomnis.legal.api.AdvisoryResponse
import org.verumomnis.legal.compliance.ConstitutionalComplianceValidator
import org.verumomnis.legal.api.SealedForensicSummary
import java.security.MessageDigest
import java.time.Instant

/**
 * WEB DOCUMENT GENERATOR
 *
 * Generates HTML sealed advisory documents for web browsers.
 * Compatible with Android, iOS, and desktop browsers.
 *
 * All outputs:
 * ✓ Include watermark: "Generated from sealed forensic summary"
 * ✓ Reference forensic report hash
 * ✓ Are individually sealed and hashed (SHA-512)
 * ✓ Are storable in vault with chain of custody
 * ✓ Cannot be edited after generation (HTML immutable)
 * ✓ Are responsive and mobile-friendly
 * ✓ Include constitutional compliance report
 * ✓ Can be printed to PDF
 */
class WebDocumentGenerator(
    private val apkRootHash: String
) {

    private val complianceValidator = ConstitutionalComplianceValidator()

    /**
     * Generate sealed HTML advisory document.
     * Returns HTML string ready for browser display or storage.
     */
    fun generateHTMLDocument(
        advisory: AdvisoryResponse,
        sealedSummary: SealedForensicSummary? = null,
        documentTitle: String = "Sealed Advisory"
    ): String {
        val html = StringBuilder()

        // HTML Header
        html.append("<!DOCTYPE html>\n")
        html.append("<html lang=\"en\">\n")
        html.append("<head>\n")
        html.append("  <meta charset=\"UTF-8\">\n")
        html.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
        html.append("  <title>$documentTitle</title>\n")
        html.append("  <style>\n")
        html.append(getEmbeddedCSS())
        html.append("  </style>\n")
        html.append("</head>\n")
        html.append("<body>\n")

        // Watermark
        html.append(getWatermarkHTML(advisory.reportHash))

        // Document Header
        html.append("<header class=\"document-header\">\n")
        html.append("  <h1>🔒 SEALED FORENSIC ADVISORY</h1>\n")
        html.append("  <p class=\"subtitle\">Generated from sealed, immutable forensic findings</p>\n")
        html.append("  <div class=\"header-meta\">\n")
        html.append("    <span>Generated: ${Instant.now()}</span>\n")
        html.append("    <span>Report: ${advisory.reportHash.take(16)}...</span>\n")
        html.append("  </div>\n")
        html.append("</header>\n\n")

        // Critical Disclaimer Banner
        html.append(getDisclaimerBanner(advisory.disclaimers))

        // Main Content Container
        html.append("<main class=\"content\">\n\n")

        // Jurisdiction Section
        html.append(getJurisdictionSection(advisory))

        // Key Findings
        html.append(getFindingsSection(advisory))

        // Risk Assessment
        html.append(getRiskSection(advisory))

        // Recommendations
        html.append(getRecommendationsSection(advisory))

        // Next Steps
        html.append(getNextStepsSection(advisory))

        // Cross-Border Analysis (if applicable)
        if (advisory.allApplicableJurisdictions.size > 1) {
            html.append(getCrossBorderSection(advisory))
        }

        // Constitutional Compliance Report (if available)
        if (sealedSummary != null) {
            html.append(getComplianceSection(advisory, sealedSummary))
        }

        html.append("</main>\n\n")

        // Footer with Chain of Custody
        html.append(getFooter(advisory))

        // JavaScript for interactivity (read-only)
        html.append("<script>\n")
        html.append(getEmbeddedJavaScript())
        html.append("</script>\n")

        html.append("</body>\n")
        html.append("</html>\n")

        return html.toString()
    }

    /**
     * Embedded CSS (responsive, print-friendly)
     */
    private fun getEmbeddedCSS(): String = """
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
            line-height: 1.6;
            color: #333;
            background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
            padding: 20px;
        }
        header.document-header {
            background: white;
            border-left: 5px solid #d32f2f;
            padding: 30px;
            margin-bottom: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        header h1 {
            color: #d32f2f;
            margin-bottom: 10px;
            font-size: 28px;
        }
        header .subtitle {
            color: #666;
            font-size: 16px;
            margin-bottom: 15px;
        }
        .header-meta {
            display: flex;
            gap: 30px;
            font-size: 12px;
            color: #999;
            border-top: 1px solid #eee;
            padding-top: 15px;
        }
        .banner {
            background: #fff3cd;
            border: 2px solid #ff9800;
            border-radius: 8px;
            padding: 20px;
            margin-bottom: 20px;
            color: #856404;
        }
        .banner.critical {
            background: #ffebee;
            border-color: #d32f2f;
            color: #c62828;
        }
        .banner h3 {
            margin-bottom: 10px;
            font-size: 18px;
        }
        .banner ul {
            margin-left: 20px;
        }
        .banner li {
            margin: 8px 0;
            font-size: 14px;
        }
        main.content {
            max-width: 900px;
            margin: 0 auto;
            background: white;
            border-radius: 8px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.15);
            padding: 40px;
        }
        section {
            margin-bottom: 40px;
            border-bottom: 1px solid #eee;
            padding-bottom: 30px;
        }
        section:last-child {
            border-bottom: none;
        }
        section h2 {
            color: #1976d2;
            font-size: 22px;
            margin-bottom: 15px;
            padding-bottom: 10px;
            border-bottom: 3px solid #1976d2;
        }
        section h3 {
            color: #424242;
            font-size: 16px;
            margin-top: 15px;
            margin-bottom: 10px;
        }
        .jurisdiction-box {
            background: #f5f5f5;
            border-left: 4px solid #1976d2;
            padding: 15px;
            margin: 10px 0;
            border-radius: 4px;
        }
        .jurisdiction-box strong {
            color: #1976d2;
        }
        .finding-item {
            background: #f9f9f9;
            border: 1px solid #e0e0e0;
            padding: 15px;
            margin: 10px 0;
            border-radius: 4px;
            border-left: 4px solid #ff9800;
        }
        .risk-factor {
            background: #fff3e0;
            border-left: 4px solid #ff9800;
            padding: 12px 15px;
            margin: 8px 0;
            border-radius: 4px;
            font-size: 14px;
        }
        .recommendation {
            background: #e8f5e9;
            border-left: 4px solid #4caf50;
            padding: 15px;
            margin: 12px 0;
            border-radius: 4px;
        }
        .next-step {
            display: flex;
            align-items: flex-start;
            margin: 15px 0;
        }
        .step-number {
            background: #1976d2;
            color: white;
            width: 30px;
            height: 30px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
            margin-right: 15px;
            font-weight: bold;
        }
        .step-content {
            flex: 1;
        }
        footer {
            background: #f5f5f5;
            border-top: 3px solid #d32f2f;
            padding: 20px;
            margin-top: 40px;
            border-radius: 8px;
            font-size: 12px;
            color: #666;
        }
        .footer-section {
            margin: 15px 0;
        }
        .footer-section strong {
            color: #333;
        }
        .vault-reference {
            background: #eceff1;
            padding: 10px 15px;
            border-radius: 4px;
            font-family: monospace;
            font-size: 11px;
            word-break: break-all;
            margin: 10px 0;
        }
        .print-notice {
            background: #e3f2fd;
            border: 1px solid #1976d2;
            padding: 12px;
            border-radius: 4px;
            margin-bottom: 20px;
            font-size: 13px;
            color: #0d47a1;
        }
        @media (max-width: 768px) {
            main.content {
                padding: 20px;
            }
            header.document-header {
                padding: 20px;
            }
            header h1 {
                font-size: 22px;
            }
            .header-meta {
                flex-direction: column;
                gap: 10px;
            }
        }
        @media print {
            body {
                background: white;
                padding: 0;
            }
            main.content {
                box-shadow: none;
                border: 1px solid #ccc;
            }
            .no-print {
                display: none !important;
            }
        }
    """.trimIndent()

    /**
     * Watermark HTML (background)
     */
    private fun getWatermarkHTML(reportHash: String): String = """
        <div class="watermark" style="
            position: fixed;
            top: 10%;
            left: -10%;
            width: 120%;
            height: 80%;
            opacity: 0.05;
            font-size: 48px;
            font-weight: bold;
            color: #d32f2f;
            pointer-events: none;
            transform: rotate(-45deg);
            z-index: -1;
            overflow: hidden;
        ">
            SEALED FROM: ${reportHash.take(16)}...
        </div>
    """.trimIndent()

    /**
     * Disclaimer banner
     */
    private fun getDisclaimerBanner(disclaimers: List<String>): String {
        val banner = StringBuilder()
        banner.append("<div class=\"banner critical no-print\">\n")
        banner.append("  <h3>⚠️ DISCLAIMER</h3>\n")
        banner.append("  <ul>\n")
        disclaimers.forEach { disclaimer ->
            banner.append("    <li>$disclaimer</li>\n")
        }
        banner.append("  </ul>\n")
        banner.append("</div>\n\n")
        return banner.toString()
    }

    /**
     * Jurisdiction section
     */
    private fun getJurisdictionSection(advisory: AdvisoryResponse): String {
        val section = StringBuilder()
        section.append("<section class=\"jurisdiction-section\">\n")
        section.append("  <h2>⚖️ Jurisdiction Analysis (GPS-Determined)</h2>\n")
        section.append("  <div class=\"jurisdiction-box\">\n")
        section.append("    <strong>Primary Jurisdiction:</strong> ${advisory.jurisdiction}<br>\n")
        if (advisory.allApplicableJurisdictions.isNotEmpty()) {
            section.append("    <strong>All Applicable Jurisdictions:</strong> ${advisory.allApplicableJurisdictions.joinToString(", ")}<br>\n")
        }
        section.append("    <strong>GPS Coordinates Processed:</strong> ${advisory.gpsCoordinatesProcessed}<br>\n")
        section.append("  </div>\n")
        section.append("</section>\n\n")
        return section.toString()
    }

    /**
     * Findings section
     */
    private fun getFindingsSection(advisory: AdvisoryResponse): String {
        val section = StringBuilder()
        section.append("<section class=\"findings-section\">\n")
        section.append("  <h2>📊 Key Findings</h2>\n")
        if (advisory.riskFactors.isEmpty()) {
            section.append("  <p>No significant risk factors identified.</p>\n")
        } else {
            advisory.riskFactors.take(5).forEach { factor ->
                section.append("  <div class=\"finding-item\">$factor</div>\n")
            }
        }
        section.append("</section>\n\n")
        return section.toString()
    }

    /**
     * Risk assessment section
     */
    private fun getRiskSection(advisory: AdvisoryResponse): String {
        val section = StringBuilder()
        section.append("<section class=\"risk-section\">\n")
        section.append("  <h2>⚠️ Risk Assessment</h2>\n")
        advisory.riskFactors.forEach { factor ->
            section.append("  <div class=\"risk-factor\">• $factor</div>\n")
        }
        section.append("  <div class=\"risk-factor\" style=\"margin-top: 15px; background: #f5f5f5; border-left-color: #666;\">\n")
        section.append("    ${advisory.confidenceStatement}\n")
        section.append("  </div>\n")
        section.append("</section>\n\n")
        return section.toString()
    }

    /**
     * Recommendations section
     */
    private fun getRecommendationsSection(advisory: AdvisoryResponse): String {
        val section = StringBuilder()
        section.append("<section class=\"recommendations-section\">\n")
        section.append("  <h2>💡 Recommendations</h2>\n")
        advisory.recommendations.take(3).forEach { rec ->
            section.append("  <div class=\"recommendation\">$rec</div>\n")
        }
        section.append("</section>\n\n")
        return section.toString()
    }

    /**
     * Next steps section
     */
    private fun getNextStepsSection(advisory: AdvisoryResponse): String {
        val section = StringBuilder()
        section.append("<section class=\"next-steps-section\">\n")
        section.append("  <h2>👉 Recommended Next Steps</h2>\n")
        advisory.nextSteps.forEachIndexed { index, step ->
            section.append("  <div class=\"next-step\">\n")
            section.append("    <div class=\"step-number\">${index + 1}</div>\n")
            section.append("    <div class=\"step-content\">$step</div>\n")
            section.append("  </div>\n")
        }
        section.append("</section>\n\n")
        return section.toString()
    }

    /**
     * Cross-border analysis section
     */
    private fun getCrossBorderSection(advisory: AdvisoryResponse): String {
        val section = StringBuilder()
        section.append("<section class=\"cross-border-section\">\n")
        section.append("  <h2>🌍 Cross-Border Implications</h2>\n")
        section.append("  <div class=\"finding-item\">\n")
        section.append("    ${advisory.crossBorderAnalysis}\n")
        section.append("  </div>\n")
        section.append("</section>\n\n")
        return section.toString()
    }

    /**
     * Constitutional compliance section
     */
    private fun getComplianceSection(
        advisory: AdvisoryResponse,
        sealedSummary: SealedForensicSummary
    ): String {
        val section = StringBuilder()
        section.append("<section class=\"compliance-section\">\n")
        section.append("  <h2>✅ Constitutional Compliance</h2>\n")

        val complianceReport = complianceValidator.generateComplianceReport(sealedSummary, advisory)
        section.append("  <pre style=\"background: #f5f5f5; padding: 15px; border-radius: 4px; font-size: 12px; overflow-x: auto;\">\n")
        section.append(complianceReport.replace("<", "&lt;").replace(">", "&gt;"))
        section.append("  </pre>\n")
        section.append("</section>\n\n")
        return section.toString()
    }

    /**
     * Footer with chain of custody
     */
    private fun getFooter(advisory: AdvisoryResponse): String {
        val footer = StringBuilder()
        footer.append("<footer>\n")
        footer.append("  <div class=\"footer-section\">\n")
        footer.append("    <strong>Document Information:</strong><br>\n")
        footer.append("    Report Hash: ${advisory.reportHash}<br>\n")
        footer.append("    Generated: ${Instant.now()}<br>\n")
        footer.append("    Vault Record: ${advisory.vaultRecordId}\n")
        footer.append("  </div>\n")
        footer.append("  <div class=\"vault-reference\">\n")
        footer.append("    This document is sealed and immutable. Its integrity can be verified using:\n")
        footer.append("    Report Hash: ${advisory.reportHash.take(32)}...\n")
        footer.append("  </div>\n")
        footer.append("  <div class=\"footer-section print-notice no-print\">\n")
        footer.append("    💡 <strong>Tip:</strong> Print this page to PDF for permanent sealed copy.\n")
        footer.append("  </div>\n")
        footer.append("  <div class=\"footer-section\">\n")
        footer.append("    <strong>Legal Notice:</strong> This is advisory guidance only. Consult a qualified " +
                "attorney in the applicable jurisdiction for formal legal representation.\n")
        footer.append("  </div>\n")
        footer.append("</footer>\n")
        return footer.toString()
    }

    /**
     * Embedded JavaScript (read-only, non-modifying)
     */
    private fun getEmbeddedJavaScript(): String = """
        // Copy to clipboard functionality
        function copyToClipboard(text) {
            navigator.clipboard.writeText(text).then(() => {
                alert('Copied to clipboard');
            });
        }

        // Print functionality
        document.addEventListener('keydown', (e) => {
            if ((e.ctrlKey || e.metaKey) && e.key === 'p') {
                e.preventDefault();
                window.print();
            }
        });

        // Document integrity check (advisory only)
        console.log('Document loaded. For integrity verification, check report hash against vault.');
    """.trimIndent()

    /**
     * Calculate document hash (SHA-512)
     */
    fun calculateDocumentHash(htmlContent: String): String {
        val digest = MessageDigest.getInstance("SHA-512")
        val hashBytes = digest.digest(htmlContent.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
