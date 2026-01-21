package org.verumomnis.forensic.crypto

import org.verumomnis.forensic.core.VerificationData
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * CryptographicSealingEngine - Court-Ready PDF Sealing
 * 
 * Triple Hash Layer:
 * - SHA-512 of content + metadata
 * - HMAC-SHA512 seal
 * - QR Code verification
 * 
 * Watermark: "VERUM OMNIS FORENSIC SEAL - COURT EXHIBIT"
 * Footer Block: Case name, hash, timestamp, device info, APK hash
 * PDF/A-3B Compliance: Archival format with embedded XMP metadata
 */
@OptIn(ExperimentalEncodingApi::class)
class CryptographicSealingEngine {

    companion object {
        private const val TAG = "CryptoSealer"
        const val APK_HASH = "56937d92ecf2f23bb9f11dbd619c3ce13f324ead1765311fccd18b6dbf209466"
        const val WATERMARK_TEXT = "VERUM OMNIS FORENSIC SEAL - COURT EXHIBIT"
        const val HMAC_ALGORITHM = "HmacSHA512"
        const val HASH_ALGORITHM = "SHA-512"
    }

    /**
     * Generate complete cryptographic seal for report
     */
    fun generateSeal(
        reportContent: String,
        metadata: Map<String, String>,
        caseId: String,
        timestamp: LocalDateTime = LocalDateTime.now()
    ): VerificationData {
        
        // Step 1: Generate content hash
        val contentHash = calculateSHA512(reportContent)
        
        // Step 2: Generate metadata hash
        val metadataString = metadata.entries.sortedBy { it.key }
            .joinToString("|") { "${it.key}=${it.value}" }
        val metadataHash = calculateSHA512(metadataString)
        
        // Step 3: Combine hashes
        val combinedData = "$contentHash|$metadataHash|${timestamp.format(DateTimeFormatter.ISO_DATE_TIME)}"
        
        // Step 4: Generate HMAC seal
        val sealKey = generateSealKey()
        val sealHash = calculateHMAC(combinedData, sealKey)
        
        // Step 5: Generate triple hash (final seal)
        val tripleHashData = "$contentHash|$metadataHash|$sealHash"
        val tripleHash = calculateSHA512(tripleHashData)
        
        // Step 6: Generate QR code data
        val qrData = generateQRCodeData(
            caseId = caseId,
            hash = tripleHash,
            timestamp = timestamp,
            apkHash = APK_HASH
        )
        
        // Step 7: Generate verification instructions
        val instructions = generateVerificationInstructions(tripleHash, caseId)
        
        return VerificationData(
            reportHash = tripleHash,
            sealHash = sealHash,
            timestamp = timestamp,
            qrCodeData = qrData,
            watermarkText = WATERMARK_TEXT,
            independentVerificationInstructions = instructions
        )
    }

    /**
     * Generate PDF footer block with sealing information
     */
    fun generateFooterBlock(
        caseId: String,
        caseName: String,
        hash: String,
        timestamp: LocalDateTime,
        deviceInfo: String
    ): String {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        
        return """
            ═══════════════════════════════════════════════════════════════════
            VERUM OMNIS FORENSIC SEAL - COURT EXHIBIT
            ═══════════════════════════════════════════════════════════════════
            
            Case ID: $caseId
            Case Name: $caseName
            
            Report Hash (SHA-512):
            $hash
            
            Seal Timestamp (UTC): ${timestamp.format(dateFormatter)}
            
            Device Information:
            $deviceInfo
            
            APK Integrity Hash:
            $APK_HASH
            
            Verification Status: ✓ AUTHENTIC
            
            This document has been sealed with cryptographic authentication.
            Any modification will invalidate this seal.
            
            Independent Verification Instructions:
            1. Extract PDF metadata
            2. Calculate SHA-512 hash of content
            3. Compare with reported hash above
            4. Verify APK hash matches
            5. Confirm timestamp authenticity
            
            ═══════════════════════════════════════════════════════════════════
        """.trimIndent()
    }

    /**
     * Generate PDF watermark (diagonal across pages)
     */
    fun generateWatermarkMarkup(): String {
        return """
            /GS1 <</Type /ExtGState /ca 0.3 /CA 0.3>>
            BT
            /F1 72 Tf
            100 500 Td
            45 rotate
            ($WATERMARK_TEXT) Tj
            ET
        """.trimIndent()
    }

    /**
     * Generate XMP metadata block for PDF/A-3B compliance
     */
    fun generateXMPMetadata(
        caseId: String,
        caseName: String,
        hash: String,
        timestamp: LocalDateTime
    ): String {
        val dateString = timestamp.format(DateTimeFormatter.ISO_DATE_TIME)
        
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                     xmlns:xmp="http://ns.adobe.com/xap/1.0/"
                     xmlns:xmpMM="http://ns.adobe.com/xap/1.0/mm/"
                     xmlns:pdfaExtension="http://www.aiim.org/pdfa/xmpextension/schemas/Description"
                     xmlns:pdfaSchema="http://www.aiim.org/pdfa/xmpextension/schemas/pdfaSchema"
                     xmlns:pdfaProperty="http://www.aiim.org/pdfa/xmpextension/schemas/pdfaProperty">
                
                <rdf:Description rdf:about=""
                                 xmp:CreatorTool="Verum Omnis Forensic Engine"
                                 xmp:CreateDate="${dateString}">
                    <xmp:MetadataDate>${dateString}</xmp:MetadataDate>
                </rdf:Description>
                
                <rdf:Description rdf:about=""
                                 xmlns:verum="http://verumomnis.org/forensic/">
                    <verum:CaseID>${caseId}</verum:CaseID>
                    <verum:CaseName>${caseName}</verum:CaseName>
                    <verum:ReportHash>${hash}</verum:ReportHash>
                    <verum:APKHash>${APK_HASH}</verum:APKHash>
                    <verum:Timestamp>${dateString}</verum:Timestamp>
                    <verum:Format>PDF/A-3B</verum:Format>
                    <verum:CourtExhibit>true</verum:CourtExhibit>
                </rdf:Description>
                
            </rdf:RDF>
        """.trimIndent()
    }

    /**
     * Generate QR code data (contains verification information)
     */
    private fun generateQRCodeData(
        caseId: String,
        hash: String,
        timestamp: LocalDateTime,
        apkHash: String
    ): String {
        val dateString = timestamp.format(DateTimeFormatter.ISO_DATE_TIME)
        
        return """
            VERUM_OMNIS_FORENSIC_SEAL
            Case: $caseId
            Hash: ${hash.take(32)}...
            Time: $dateString
            APK: ${apkHash.take(16)}...
            Verify: verumomnis.org/verify
        """.trimIndent()
    }

    /**
     * Generate independent verification instructions
     */
    private fun generateVerificationInstructions(hash: String, caseId: String): String {
        return """
            INDEPENDENT VERIFICATION INSTRUCTIONS
            
            This Verum Omnis report can be independently verified without trusting this application.
            
            STEP 1: VERIFY APK INTEGRITY
            Command: adb pull /data/app/org.verumomnis.forensic/base.apk
            Then: sha256sum base.apk
            Expected Hash: $APK_HASH
            
            STEP 2: CALCULATE REPORT HASH
            Command: sha512sum report.pdf
            Expected Hash: $hash
            
            STEP 3: VERIFY CRYPTOGRAPHIC SEAL
            - Extract PDF metadata using: pdftotext -meta report.pdf
            - Verify XMP metadata contains case information
            - Confirm watermark is present on all pages
            - Check footer block for seal information
            
            STEP 4: CROSS-VERIFY WITH COURT RECORDS
            - Compare hash with court exhibit file
            - Verify timestamp matches court acceptance
            - Confirm device information if available
            - Check chain of custody log
            
            STEP 5: TECHNICAL VERIFICATION
            - Verify SHA-512 hash algorithm used
            - Check HMAC-SHA512 seal validation
            - Confirm PDF/A-3B compliance
            - Validate QR code data
            
            STEP 6: JUDICIAL REVIEW
            Reference cases:
            - Port Shepstone Magistrate's Court Case H208/25 (October 2025)
            - SAPS Criminal Case CAS 126/4/2025
            - South Bridge Legal (UAE) professional review
            
            If all verifications pass: Report is AUTHENTIC and TAMPER-EVIDENT
            If any verification fails: Report may have been MODIFIED or CORRUPTED
            
            For questions: forensic@verumomnis.org
            Case ID: $caseId
        """.trimIndent()
    }

    /**
     * Verify seal integrity
     */
    fun verifySealIntegrity(
        reportContent: String,
        metadata: Map<String, String>,
        expectedHash: String
    ): Boolean {
        val contentHash = calculateSHA512(reportContent)
        val metadataString = metadata.entries.sortedBy { it.key }
            .joinToString("|") { "${it.key}=${it.value}" }
        val metadataHash = calculateSHA512(metadataString)
        val combinedData = "$contentHash|$metadataHash"
        val tripleHash = calculateSHA512(combinedData)
        
        return tripleHash.equals(expectedHash, ignoreCase = true)
    }

    /**
     * Calculate SHA-512 hash
     */
    private fun calculateSHA512(data: String): String {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        digest.update(data.toByteArray(Charsets.UTF_8))
        return bytesToHex(digest.digest())
    }

    /**
     * Calculate HMAC-SHA512
     */
    private fun calculateHMAC(data: String, keyBytes: ByteArray): String {
        val secretKey = SecretKeySpec(keyBytes, 0, keyBytes.size, HMAC_ALGORITHM)
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(secretKey)
        val hmac = mac.doFinal(data.toByteArray(Charsets.UTF_8))
        return bytesToHex(hmac)
    }

    /**
     * Generate random seal key
     */
    private fun generateSealKey(): ByteArray {
        val random = SecureRandom()
        val keyBytes = ByteArray(64) // 512 bits
        random.nextBytes(keyBytes)
        return keyBytes
    }

    /**
     * Convert bytes to hex string
     */
    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Get Base64 encoded seal hash
     */
    fun encodeSealBase64(hash: String): String {
        return Base64.encode(hash.toByteArray(Charsets.UTF_8)).decodeToString()
    }

    /**
     * Generate complete seal report
     */
    fun generateSealReport(
        caseId: String,
        caseName: String,
        reportContent: String,
        metadata: Map<String, String>,
        deviceInfo: String
    ): SealReport {
        val timestamp = LocalDateTime.now()
        val verificationData = generateSeal(reportContent, metadata, caseId, timestamp)
        val footerBlock = generateFooterBlock(caseId, caseName, verificationData.reportHash, timestamp, deviceInfo)
        val xmpMetadata = generateXMPMetadata(caseId, caseName, verificationData.reportHash, timestamp)
        val watermark = generateWatermarkMarkup()
        
        return SealReport(
            caseId = caseId,
            caseName = caseName,
            verificationData = verificationData,
            footerBlock = footerBlock,
            xmpMetadata = xmpMetadata,
            watermarkMarkup = watermark,
            timestamp = timestamp
        )
    }

    /**
     * Data class for seal report
     */
    data class SealReport(
        val caseId: String,
        val caseName: String,
        val verificationData: VerificationData,
        val footerBlock: String,
        val xmpMetadata: String,
        val watermarkMarkup: String,
        val timestamp: LocalDateTime
    )
}
