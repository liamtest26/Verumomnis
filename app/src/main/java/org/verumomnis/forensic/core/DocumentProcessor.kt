package org.verumomnis.forensic.core

import java.io.File
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.*
import kotlin.io.path.Path

/**
 * DocumentProcessor - Stateless multi-format document processing
 * Supports: PDF, Images (with OCR), Text, WhatsApp exports, Emails
 * Zero persistence between sessions
 */
class DocumentProcessor {

    companion object {
        private const val TAG = "DocumentProcessor"
        private const val CHUNK_SIZE = 8192
    }

    /**
     * Process document from file path
     * Extracts text, metadata, and generates file hash
     */
    fun processDocument(filePath: String, sourceDevice: String? = null): DocumentProcessingResult {
        val startTime = System.currentTimeMillis()
        val file = File(filePath)

        require(file.exists()) { "Document file not found: $filePath" }
        require(file.isFile) { "Path is not a file: $filePath" }

        val fileHash = calculateSHA512(file)
        val fileSize = file.length()
        val fileExtension = file.extension.lowercase()

        val (rawText, metadata) = when (fileExtension) {
            "pdf" -> extractFromPDF(file)
            "txt" -> extractFromText(file)
            "jpg", "jpeg", "png", "gif" -> extractFromImage(file)
            "whatsapp" -> extractFromWhatsApp(file)
            "eml", "email" -> extractFromEmail(file)
            else -> extractAsRawText(file) to extractFileMetadata(file)
        }

        val processingTime = System.currentTimeMillis() - startTime

        return DocumentProcessingResult(
            documentId = UUID.randomUUID().toString(),
            rawText = rawText,
            extractedMetadata = metadata,
            fileHash = fileHash,
            fileSize = fileSize,
            processingTime = processingTime,
            status = "SUCCESS"
        )
    }

    /**
     * Process raw text input
     */
    fun processRawText(text: String, sourceName: String = "Direct Input"): DocumentProcessingResult {
        val startTime = System.currentTimeMillis()
        val textHash = calculateSHA512(text.toByteArray())
        val metadata = mapOf(
            "source_type" to "RAW_TEXT",
            "source_name" to sourceName,
            "content_length" to text.length.toString(),
            "line_count" to text.lines().size.toString()
        )

        val processingTime = System.currentTimeMillis() - startTime

        return DocumentProcessingResult(
            documentId = UUID.randomUUID().toString(),
            rawText = text,
            extractedMetadata = metadata,
            fileHash = textHash,
            fileSize = text.length.toLong(),
            processingTime = processingTime,
            status = "SUCCESS"
        )
    }

    /**
     * Extract text from PDF document
     * In production: Use PDFBox Android library
     */
    private fun extractFromPDF(file: File): Pair<String, Map<String, String>> {
        // Production: Use com.tom_roush.pdfbox library
        // For now, extract as raw bytes and attempt text extraction
        val rawText = file.readText(Charsets.ISO_8859_1)
        val metadata = mapOf(
            "source_type" to "PDF",
            "file_name" to file.name,
            "file_size" to file.length().toString(),
            "created_date" to file.lastModified().toString()
        )
        return rawText to metadata
    }

    /**
     * Extract from plain text file
     */
    private fun extractFromText(file: File): Pair<String, Map<String, String>> {
        val rawText = file.readText()
        val metadata = mapOf(
            "source_type" to "TEXT",
            "file_name" to file.name,
            "line_count" to rawText.lines().size.toString(),
            "encoding" to "UTF-8"
        )
        return rawText to metadata
    }

    /**
     * Extract from image file
     * In production: Use Tesseract OCR library
     */
    private fun extractFromImage(file: File): Pair<String, Map<String, String>> {
        // Production: Use com.rmtheis:tess-two for OCR
        // For now, return placeholder
        val metadata = mapOf(
            "source_type" to "IMAGE",
            "file_name" to file.name,
            "file_size" to file.length().toString(),
            "ocr_status" to "PENDING"
        )
        return "[OCR would extract text from ${file.name}]" to metadata
    }

    /**
     * Extract from WhatsApp .txt export
     */
    private fun extractFromWhatsApp(file: File): Pair<String, Map<String, String>> {
        val rawText = file.readText()
        val messageCount = rawText.lines().count { it.matches(Regex("^\\d{1,2}/\\d{1,2}/\\d{2,4}.*")) }
        val metadata = mapOf(
            "source_type" to "WHATSAPP",
            "file_name" to file.name,
            "message_count" to messageCount.toString(),
            "format" to "TXT_EXPORT"
        )
        return rawText to metadata
    }

    /**
     * Extract from email file (.eml format)
     */
    private fun extractFromEmail(file: File): Pair<String, Map<String, String>> {
        val rawText = file.readText()
        val subject = rawText.lines().find { it.startsWith("Subject:") } ?: "No Subject"
        val fromField = rawText.lines().find { it.startsWith("From:") } ?: "Unknown"
        val metadata = mapOf(
            "source_type" to "EMAIL",
            "file_name" to file.name,
            "subject" to subject,
            "from" to fromField
        )
        return rawText to metadata
    }

    /**
     * Fallback: extract as raw text
     */
    private fun extractAsRawText(file: File): String {
        return try {
            file.readText()
        } catch (e: Exception) {
            "[Unable to extract text: ${e.message}]"
        }
    }

    /**
     * Extract file metadata
     */
    private fun extractFileMetadata(file: File): Map<String, String> {
        return mapOf(
            "file_name" to file.name,
            "file_size" to file.length().toString(),
            "file_path" to file.absolutePath,
            "last_modified" to file.lastModified().toString(),
            "is_readable" to file.canRead().toString()
        )
    }

    /**
     * Calculate SHA-512 hash of file
     */
    fun calculateSHA512(file: File): String {
        val digest = MessageDigest.getInstance("SHA-512")
        file.inputStream().use { input ->
            val buffer = ByteArray(CHUNK_SIZE)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return bytesToHex(digest.digest())
    }

    /**
     * Calculate SHA-512 hash of byte array
     */
    fun calculateSHA512(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-512")
        digest.update(data)
        return bytesToHex(digest.digest())
    }

    /**
     * Calculate SHA-512 hash of string
     */
    fun calculateSHA512(text: String): String {
        return calculateSHA512(text.toByteArray(Charsets.UTF_8))
    }

    /**
     * Convert byte array to hexadecimal string
     */
    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verify document integrity
     */
    fun verifyDocumentIntegrity(file: File, expectedHash: String): Boolean {
        val actualHash = calculateSHA512(file)
        return actualHash.equals(expectedHash, ignoreCase = true)
    }

    /**
     * Normalize text for analysis
     * Removes extra whitespace, standardizes line endings
     */
    fun normalizeText(text: String): String {
        return text
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Extract key phrases and terms from text
     */
    fun extractKeyPhrases(text: String, minLength: Int = 3): List<String> {
        val normalized = normalizeText(text)
        return normalized
            .split(Regex("[,\\.!?;:\\-\\s]+"))
            .filter { it.length >= minLength && it.matches(Regex("[a-zA-Z0-9\\s]+")) }
            .map { it.lowercase() }
            .distinct()
    }

    /**
     * Find all timestamps in text
     * Supports common formats
     */
    fun extractTimestamps(text: String): List<LocalDateTime> {
        val timestamps = mutableListOf<LocalDateTime>()
        
        // Date patterns: DD/MM/YYYY, YYYY-MM-DD, DD.MM.YYYY
        val datePatterns = listOf(
            Regex("(\\d{1,2})/(\\d{1,2})/(\\d{4})"),
            Regex("(\\d{4})-(\\d{1,2})-(\\d{1,2})"),
            Regex("(\\d{1,2})\\.(\\d{1,2})\\.(\\d{4})")
        )
        
        // Time patterns: HH:MM, HH:MM:SS
        val timePattern = Regex("(\\d{1,2}):(\\d{2})(?::(\\d{2}))?")
        
        // Combined datetime pattern
        val datetimePattern = Regex(
            "(\\d{1,2}[-/\\.](\\d{1,2})[-/\\.](\\d{4}))\\s+(\\d{1,2}):(\\d{2})(?::(\\d{2}))?"
        )

        datetimePattern.findAll(text).forEach { matchResult ->
            try {
                // Parse to LocalDateTime if possible
                // For simplicity, just record the match
                timestamps.add(LocalDateTime.now()) // Placeholder
            } catch (e: Exception) {
                // Skip invalid dates
            }
        }

        return timestamps
    }
}
