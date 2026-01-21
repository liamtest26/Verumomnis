package org.verumomnis.forensic.integrity

import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.UUID

/**
 * APKIntegrityChecker - Forensic Chain of Trust
 * 
 * Root anchor: APK hash 56937d92ecf2f23bb9f11dbd619c3ce13f324ead1765311fccd18b6dbf209466
 * Boot verification: App checks own integrity on launch
 * Tamper detection: Refuses to process if APK modified
 */
class APKIntegrityChecker {

    companion object {
        private const val TAG = "APKIntegrityChecker"
        const val AUTHENTIC_APK_HASH = "56937d92ecf2f23bb9f11dbd619c3ce13f324ead1765311fccd18b6dbf209466"
        const val ALGORITHM = "SHA-256"
    }

    /**
     * Check APK integrity on app startup
     */
    fun verifyBootIntegrity(apkFilePath: String): IntegrityReport {
        val startTime = System.currentTimeMillis()
        
        return try {
            val calculatedHash = calculateAPKHash(apkFilePath)
            val isAuthentic = calculatedHash.equals(AUTHENTIC_APK_HASH, ignoreCase = true)
            val verificationTime = System.currentTimeMillis() - startTime
            
            IntegrityReport(
                reportId = UUID.randomUUID().toString(),
                timestamp = LocalDateTime.now(),
                verificationType = "BOOT_INTEGRITY",
                calculatedHash = calculatedHash,
                expectedHash = AUTHENTIC_APK_HASH,
                isAuthentic = isAuthentic,
                verificationTimeMs = verificationTime,
                status = if (isAuthentic) "AUTHENTIC" else "TAMPERED",
                message = if (isAuthentic) {
                    "APK integrity verified. Application is authentic."
                } else {
                    "WARNING: APK hash mismatch! Application may be tampered."
                }
            )
        } catch (e: Exception) {
            val verificationTime = System.currentTimeMillis() - startTime
            IntegrityReport(
                reportId = UUID.randomUUID().toString(),
                timestamp = LocalDateTime.now(),
                verificationType = "BOOT_INTEGRITY",
                calculatedHash = "",
                expectedHash = AUTHENTIC_APK_HASH,
                isAuthentic = false,
                verificationTimeMs = verificationTime,
                status = "VERIFICATION_FAILED",
                message = "APK verification failed: ${e.message}"
            )
        }
    }

    /**
     * Calculate APK file hash (SHA-256)
     */
    fun calculateAPKHash(apkFilePath: String): String {
        val file = java.io.File(apkFilePath)
        require(file.exists()) { "APK file not found: $apkFilePath" }
        
        val digest = MessageDigest.getInstance(ALGORITHM)
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        
        return bytesToHex(digest.digest())
    }

    /**
     * Generate chain of trust verification
     */
    fun generateChainOfTrust(
        apkHash: String,
        caseId: String,
        deviceId: String,
        deviceInfo: String,
        custodyLog: ChainOfCustodyReport
    ): ChainOfTrust {
        val verificationStatus = if (apkHash.equals(AUTHENTIC_APK_HASH, ignoreCase = true)) {
            "VERIFIED"
        } else {
            "UNVERIFIED"
        }
        
        return ChainOfTrust(
            id = UUID.randomUUID().toString(),
            timestamp = LocalDateTime.now(),
            apkHash = apkHash,
            apkVerificationStatus = verificationStatus,
            caseId = caseId,
            deviceId = deviceId,
            deviceInfo = deviceInfo,
            custodyLog = custodyLog,
            trustAnchor = TrustAnchor(
                algorithm = ALGORITHM,
                anchor = AUTHENTIC_APK_HASH,
                source = "Verum Omnis Official Release",
                verificationMethod = "Direct Hash Comparison"
            ),
            independentVerificationGuide = generateIndependentVerificationGuide()
        )
    }

    /**
     * Generate independent verification guide
     */
    private fun generateIndependentVerificationGuide(): String {
        return """
            ═══════════════════════════════════════════════════════════════════
            INDEPENDENT APK VERIFICATION GUIDE
            ═══════════════════════════════════════════════════════════════════
            
            STEP 1: EXTRACT APK FROM DEVICE
            Command: adb pull /data/app/org.verumomnis.forensic/base.apk ./extracted.apk
            
            STEP 2: CALCULATE SHA-256 HASH
            Command: sha256sum extracted.apk
            (On macOS: shasum -a 256 extracted.apk)
            (On Windows: certUtil -hashfile extracted.apk SHA256)
            
            STEP 3: COMPARE HASH
            Expected Hash: $AUTHENTIC_APK_HASH
            
            Result:
            ✓ AUTHENTIC - Hashes match, APK is genuine
            ✗ TAMPERED - Hashes don't match, APK has been modified
            
            STEP 4: ADDITIONAL VERIFICATION
            
            a) Verify APK Signature:
               Command: jarsigner -verify -verbose extracted.apk
               Look for: "jar verified."
            
            b) Check APK Contents:
               Command: unzip -l extracted.apk | grep "classes.dex"
               Verify DEX file is present
            
            c) Inspect Manifest:
               Command: aapt dump badging extracted.apk
               Verify package: org.verumomnis.forensic
               Verify versionName: 5.2.7
            
            d) Verify Code Integrity:
               Command: apktool d extracted.apk -o decoded/
               Review source code for unauthorized modifications
            
            STEP 5: FORENSIC DOCUMENTATION
            
            Record:
            - Date and time of verification
            - Device information
            - Hash calculation method
            - Verification result (AUTHENTIC/TAMPERED)
            - Any anomalies or concerns
            - Verifier name and signature
            
            LEGAL REFERENCE:
            - Port Shepstone Magistrate's Court Case H208/25 (October 2025)
            - SAPS Criminal Case CAS 126/4/2025
            - Professional Legal Review: South Bridge Legal (UAE)
            
            FURTHER ASSISTANCE:
            - Visit: forensic.verumomnis.org
            - Email: verify@verumomnis.org
            - Documentation: github.com/verumomnis/forensic-engine
            
            ═══════════════════════════════════════════════════════════════════
        """.trimIndent()
    }

    /**
     * Verify tamper detection
     */
    fun checkTamperDetection(apkHash: String): TamperDetectionResult {
        val isAuthentic = apkHash.equals(AUTHENTIC_APK_HASH, ignoreCase = true)
        
        return TamperDetectionResult(
            isTampered = !isAuthentic,
            detectedAt = LocalDateTime.now(),
            authenticHash = AUTHENTIC_APK_HASH,
            providedHash = apkHash,
            recommendation = if (!isAuthentic) {
                "APK appears to be modified. Application will not process sensitive evidence until verified."
            } else {
                "APK verification passed. Safe to process."
            }
        )
    }

    /**
     * Convert bytes to hex
     */
    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Generate verification UI display data
     */
    fun generateVerificationUIData(integrityReport: IntegrityReport): VerificationUIData {
        val statusColor = if (integrityReport.isAuthentic) "#4CAF50" else "#F44336" // Green or Red
        val statusIcon = if (integrityReport.isAuthentic) "✓" else "✗"
        val statusText = if (integrityReport.isAuthentic) "AUTHENTIC" else "TAMPERED"
        
        return VerificationUIData(
            statusText = statusText,
            statusColor = statusColor,
            statusIcon = statusIcon,
            calculatedHash = integrityReport.calculatedHash,
            expectedHash = integrityReport.expectedHash,
            hashMatch = integrityReport.calculatedHash.equals(
                integrityReport.expectedHash, 
                ignoreCase = true
            ),
            verificationTimeMs = integrityReport.verificationTimeMs,
            timestamp = integrityReport.timestamp,
            message = integrityReport.message,
            instructionsUrl = "forensic.verumomnis.org/verify",
            qrCodeData = generateQRVerificationData(integrityReport)
        )
    }

    /**
     * Generate QR code data for independent verification
     */
    private fun generateQRVerificationData(report: IntegrityReport): String {
        return """
            VERUM_OMNIS_APK_VERIFICATION
            Hash: ${report.calculatedHash.take(32)}
            Status: ${report.status}
            Time: ${report.timestamp}
            Verify: forensic.verumomnis.org/verify
        """.trimIndent()
    }
}

// Data Classes

data class IntegrityReport(
    val reportId: String,
    val timestamp: LocalDateTime,
    val verificationType: String, // BOOT_INTEGRITY, DOCUMENT_PROCESSING, etc.
    val calculatedHash: String,
    val expectedHash: String,
    val isAuthentic: Boolean,
    val verificationTimeMs: Long,
    val status: String, // AUTHENTIC, TAMPERED, VERIFICATION_FAILED
    val message: String
)

data class ChainOfTrust(
    val id: String,
    val timestamp: LocalDateTime,
    val apkHash: String,
    val apkVerificationStatus: String, // VERIFIED, UNVERIFIED
    val caseId: String,
    val deviceId: String,
    val deviceInfo: String,
    val custodyLog: ChainOfCustodyReport,
    val trustAnchor: TrustAnchor,
    val independentVerificationGuide: String
)

data class TrustAnchor(
    val algorithm: String,
    val anchor: String,
    val source: String,
    val verificationMethod: String
)

data class ChainOfCustodyReport(
    val entries: List<CustodyEntry>,
    val totalEntries: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val integrityStatus: String // ALL_VERIFIED, SOME_FAILED, FAILED
)

data class CustodyEntry(
    val id: String,
    val timestamp: LocalDateTime,
    val action: String, // UPLOADED, PROCESSED, SEALED, EXPORTED
    val hash: String,
    val userId: String,
    val deviceId: String,
    val integrityCheckPassed: Boolean
)

data class TamperDetectionResult(
    val isTampered: Boolean,
    val detectedAt: LocalDateTime,
    val authenticHash: String,
    val providedHash: String,
    val recommendation: String
)

data class VerificationUIData(
    val statusText: String,
    val statusColor: String,
    val statusIcon: String,
    val calculatedHash: String,
    val expectedHash: String,
    val hashMatch: Boolean,
    val verificationTimeMs: Long,
    val timestamp: LocalDateTime,
    val message: String,
    val instructionsUrl: String,
    val qrCodeData: String
)
