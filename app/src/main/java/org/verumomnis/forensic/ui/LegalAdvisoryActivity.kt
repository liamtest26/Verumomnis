package org.verumomnis.forensic.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.ScrollView
import android.view.ViewGroup
import android.widget.LinearLayout
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import org.verumomnis.legal.api.LegalAdvisoryAPI
import org.verumomnis.legal.api.SealedForensicSummary
import org.verumomnis.legal.api.GPSCoordinate
import java.time.Instant

/**
 * LEGAL ADVISORY ACTIVITY
 *
 * Presents sealed forensic summary to user for jurisdiction-aware legal guidance.
 * Accessible ONLY after forensic report is sealed and complete.
 *
 * Enforces:
 * ✓ No raw evidence viewing
 * ✓ Sealed summary input only
 * ✓ GPS-based jurisdiction determination
 * ✓ All outputs sealed and stored in vault
 */
class LegalAdvisoryActivity : AppCompatActivity() {

    private lateinit var legalAPI: LegalAdvisoryAPI
    private var sealedSummary: SealedForensicSummary? = null
    private var gpsCoordinates: List<GPSCoordinate> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Legal API
        val vaultPath = getFilesDir().absolutePath + "/vault"
        val sessionPath = getFilesDir().absolutePath + "/sessions"
        val apkRootHash = "56937d92ecf2f23bb9f11dbd619c3ce13f324ead1765311fccd18b6dbf209466"

        legalAPI = LegalAdvisoryAPI.initialize(vaultPath, sessionPath, apkRootHash)

        // Get sealed summary from intent
        sealedSummary = intent.getSerializableExtra("sealed_summary") as? SealedForensicSummary
            ?: run {
                showError("No sealed forensic summary provided")
                return
            }

        gpsCoordinates = intent.getParcelableArrayExtra("gps_coordinates")?.
            map { it as GPSCoordinate }?.toList() ?: emptyList()

        // Build UI
        buildUI()
    }

    /**
     * Build advisory UI
     */
    private fun buildUI() {
        // Create main container
        val mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Scrollable content area
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val contentContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 16, 16, 16)
        }

        // Title
        contentContainer.addView(TextView(this).apply {
            text = "⚠️ SEALED FORENSIC ADVISORY"
            textSize = 20f
            setTextColor(android.graphics.Color.RED)
            setPadding(0, 0, 0, 16)
        })

        // Disclaimer banner
        contentContainer.addView(TextView(this).apply {
            text = "DISCLAIMER: This is advisory guidance only, not legal representation.\n" +
                    "Consult a qualified attorney for formal counsel.\n" +
                    "All information below is derived from sealed, immutable forensic findings.\n" +
                    "No raw evidence is included in this advisory."
            textSize = 12f
            setTextColor(android.graphics.Color.DKGRAY)
            setPadding(12, 12, 12, 12)
            setBackgroundColor(android.graphics.Color.LTGRAY)
        })

        contentContainer.addView(TextView(this).apply { setPadding(0, 16, 0, 0) })

        // Jurisdiction section
        contentContainer.addView(TextView(this).apply {
            text = "JURISDICTION DETERMINATION (GPS-Based)"
            textSize = 16f
            setTextStyle(android.graphics.Typeface.BOLD)
        })

        val summary = sealedSummary ?: return
        contentContainer.addView(TextView(this).apply {
            val jurisdictions = summary.jurisdictionsDetermined
            val primary = summary.jurisdictionHint ?: "UNKNOWN"
            text = "Primary Jurisdiction: $primary\n" +
                    "All Applicable Jurisdictions: ${jurisdictions.joinToString(", ")}\n" +
                    "GPS Coordinates Processed: ${gpsCoordinates.size}\n" +
                    "Generated: ${Instant.now()}"
            textSize = 12f
            setPadding(0, 8, 0, 16)
        })

        // Findings section
        contentContainer.addView(TextView(this).apply {
            text = "KEY FINDINGS & RISK FACTORS"
            textSize = 16f
            setTextStyle(android.graphics.Typeface.BOLD)
        })

        summary.findings.take(5).forEach { finding ->
            contentContainer.addView(TextView(this).apply {
                text = "• ${finding.category}: Severity ${finding.severity}/5, " +
                        "Confidence ${finding.confidence}%"
                textSize = 12f
                setPadding(16, 4, 0, 4)
            })
        }

        contentContainer.addView(TextView(this).apply { setPadding(0, 12, 0, 0) })

        // Integrity score section
        contentContainer.addView(TextView(this).apply {
            text = "INTEGRITY ASSESSMENT"
            textSize = 16f
            setTextStyle(android.graphics.Typeface.BOLD)
        })

        contentContainer.addView(TextView(this).apply {
            val category = when (summary.integrityIndexScore) {
                in 85..100 -> "Excellent"
                in 70..84 -> "Good"
                in 55..69 -> "Low"
                else -> "Very Low"
            }
            text = "Integrity Score: ${summary.integrityIndexScore}/100 ($category)\n" +
                    "Verification Status: ${summary.tripleVerificationStatus}\n" +
                    "Report Hash: ${summary.reportHash.take(32)}..."
            textSize = 12f
            setPadding(0, 8, 0, 16)
        })

        // Contradiction summary
        if (summary.contradictionLogSummary.totalContradictions > 0) {
            contentContainer.addView(TextView(this).apply {
                text = "CONTRADICTION ANALYSIS"
                textSize = 16f
                setTextStyle(android.graphics.Typeface.BOLD)
            })

            contentContainer.addView(TextView(this).apply {
                val contradiction = summary.contradictionLogSummary
                text = "Total Contradictions: ${contradiction.totalContradictions}\n" +
                        "Unresolved: ${contradiction.unresolvedCount}\n" +
                        "Critical: ${contradiction.criticalCount}, High: ${contradiction.highCount}, " +
                        "Medium: ${contradiction.mediumCount}"
                textSize = 12f
                setPadding(0, 8, 0, 16)
            })
        }

        scrollView.addView(contentContainer)
        mainContainer.addView(scrollView)

        // Action buttons
        val buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 8, 16, 16)
        }

        // Get Advisory button
        buttonContainer.addView(Button(this).apply {
            text = "Get Legal Guidance (Advisory)"
            setOnClickListener {
                getAdvisory()
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        })

        // Draft Letter button
        buttonContainer.addView(Button(this).apply {
            text = "Draft Advisory Letter"
            setOnClickListener {
                draftLetter()
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 12, 0, 0)
        })

        // Draft Email button
        buttonContainer.addView(Button(this).apply {
            text = "Draft Advisory Email"
            setOnClickListener {
                draftEmail()
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 12, 0, 0)
        })

        // Ask Questions button
        buttonContainer.addView(Button(this).apply {
            text = "Ask Advisory Questions"
            setOnClickListener {
                startAdvisorySession()
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 12, 0, 0)
        })

        mainContainer.addView(buttonContainer)
        setContentView(mainContainer)
    }

    /**
     * Get legal advisory from sealed summary
     */
    private fun getAdvisory() {
        val summary = sealedSummary ?: return

        val result = legalAPI.getAdvisory(
            summary = summary,
            jurisdictionOverride = null  // Auto-detect from GPS
        )

        result.onSuccess { advisory ->
            // Show advisory in dialog or new activity
            val recommendations = StringBuilder()
            recommendations.append("JURISDICTION: ${advisory.jurisdiction}\n")
            if (advisory.allApplicableJurisdictions.isNotEmpty()) {
                recommendations.append("ALL APPLICABLE: ${advisory.allApplicableJurisdictions.joinToString(", ")}\n\n")
            }

            recommendations.append("RECOMMENDATIONS:\n")
            advisory.recommendations.take(2).forEach { rec ->
                recommendations.append("$rec\n\n")
            }

            recommendations.append("NEXT STEPS:\n")
            advisory.nextSteps.forEach { step ->
                recommendations.append("• $step\n")
            }

            showInfo("LEGAL ADVISORY", recommendations.toString())
        }

        result.onFailure { error ->
            showError("Failed to generate advisory: ${error.message}")
        }
    }

    /**
     * Draft sealed advisory letter
     */
    private fun draftLetter() {
        val summary = sealedSummary ?: return
        val advisory = legalAPI.getAdvisory(summary).getOrNull() ?: return

        val documentResult = legalAPI.generateSealedLetter(
            advisory = advisory,
            letterType = "letter",
            recipient = "recipient@case.local"
        )

        documentResult.onSuccess { document ->
            showInfo(
                "SEALED LETTER GENERATED",
                "Letter sealed and stored in evidence vault.\n" +
                "Hash: ${document.documentHash.take(32)}...\n" +
                "Report Reference: ${document.reportHashReference.take(16)}...\n\n" +
                "The letter is immutable and can be independently verified."
            )
        }

        documentResult.onFailure { error ->
            showError("Failed to generate letter: ${error.message}")
        }
    }

    /**
     * Draft sealed advisory email
     */
    private fun draftEmail() {
        val summary = sealedSummary ?: return
        val advisory = legalAPI.getAdvisory(summary).getOrNull() ?: return

        val documentResult = legalAPI.generateSealedLetter(
            advisory = advisory,
            letterType = "email",
            recipient = "investigator@case.local"
        )

        documentResult.onSuccess { document ->
            showInfo(
                "SEALED EMAIL GENERATED",
                "Email sealed and stored in evidence vault.\n" +
                "Hash: ${document.documentHash.take(32)}...\n\n" +
                "Email is ready for transmission and is immutable."
            )
        }

        documentResult.onFailure { error ->
            showError("Failed to generate email: ${error.message}")
        }
    }

    /**
     * Start sealed advisory session
     */
    private fun startAdvisorySession() {
        val summary = sealedSummary ?: return

        val sessionResult = legalAPI.startAdvisorySession(
            reportHash = summary.reportHash,
            sessionMetadata = org.verumomnis.legal.api.SessionMetadata(
                jurisdiction = summary.jurisdictionHint ?: "UNKNOWN",
                caseType = "unknown",
                userRole = "citizen"
            )
        )

        sessionResult.onSuccess { sessionId ->
            showInfo(
                "SESSION STARTED",
                "Sealed advisory session created.\n" +
                "Session ID: $sessionId\n\n" +
                "You can now ask follow-up questions.\n" +
                "All questions and responses will be sealed and stored in the vault."
            )
            // TODO: Open session Q&A activity
        }

        sessionResult.onFailure { error ->
            showError("Failed to start session: ${error.message}")
        }
    }

    /**
     * Show info dialog
     */
    private fun showInfo(title: String, message: String) {
        android.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    /**
     * Show error dialog
     */
    private fun showError(message: String) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
