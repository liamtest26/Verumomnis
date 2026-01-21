package org.verumomnis.forensic.core

import java.io.Serializable
import java.time.LocalDateTime

/**
 * Core data models for the Verum Omnis Forensic Engine
 * Based on the Nine-Brain Architecture and Triple Verification Doctrine
 */

// Evidence Models
data class ForensicEvidence(
    val id: String,
    val name: String,
    val fileType: String, // PDF, IMAGE, TEXT, WHATSAPP, EMAIL
    val fileHash: String, // SHA-512 of original file
    val fileSize: Long,
    val sourceDevice: String?,
    val captureTimestamp: LocalDateTime,
    val metadata: Map<String, String> = mapOf(),
    val rawContent: String
) : Serializable

// Contradiction Detection Models
data class Contradiction(
    val id: String,
    val type: ContradictionType,
    val severity: SeverityLevel,
    val evidence1Id: String,
    val evidence2Id: String,
    val evidence1Text: String,
    val evidence2Text: String,
    val description: String,
    val pattern: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
) : Serializable

enum class ContradictionType {
    DIRECT_CONTRADICTION,
    FACTUAL_DISCREPANCY,
    OMISSION,
    TEMPORAL_INCONSISTENCY,
    BEHAVIORAL_PATTERN
}

enum class SeverityLevel {
    LOW, MEDIUM, HIGH, CRITICAL
}

// Timeline Models
data class TimelineEvent(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: LocalDateTime,
    val evidenceIds: List<String>,
    val confidence: Float, // 0.0-1.0
    val isReconstructed: Boolean = false,
    val reconstructionMethod: String? = null
) : Serializable

data class Timeline(
    val events: List<TimelineEvent>,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val gaps: List<TimingGap>
) : Serializable

data class TimingGap(
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val durationSeconds: Long,
    val significance: String // "Expected" or "Suspicious"
)

// Behavioral Pattern Models
data class BehavioralPattern(
    val id: String,
    val patternType: String, // Gaslighting, Evasion, Concealment, etc.
    val indicators: List<String>,
    val confidence: Float,
    val evidenceIds: List<String>,
    val description: String
) : Serializable

// Leveler Engine (B1-B9) Results
data class LevelerAnalysis(
    val b1EventChronology: EventChronologyAnalysis,
    val b2ContradictionMatrix: ContradictionMatrixAnalysis,
    val b3MissingEvidenceGaps: MissingEvidenceAnalysis,
    val b4TimelineManipulation: TimelineManipulationAnalysis,
    val b5BehavioralPatterns: BehavioralPatternsAnalysis,
    val b6FinancialCorrelation: FinancialCorrelationAnalysis,
    val b7CommunicationPattern: CommunicationPatternAnalysis,
    val b8JurisdictionalCompliance: JurisdictionalComplianceAnalysis,
    val b9IntegrityScore: IntegrityScoreAnalysis
) : Serializable

data class EventChronologyAnalysis(
    val totalEvents: Int,
    val reconstructedEvents: Int,
    val chronologyScore: Float,
    val details: String
) : Serializable

data class ContradictionMatrixAnalysis(
    val totalContradictions: Int,
    val criticalCount: Int,
    val highCount: Int,
    val mediumCount: Int,
    val lowCount: Int,
    val contradictions: List<Contradiction>
) : Serializable

data class MissingEvidenceAnalysis(
    val gaps: List<String>,
    val expectedDocuments: List<String>,
    val severity: String,
    val description: String
) : Serializable

data class TimelineManipulationAnalysis(
    val backdatedDocuments: List<String>,
    val suspiciousGaps: List<TimingGap>,
    val editAfterFact: List<String>,
    val riskLevel: String,
    val details: String
) : Serializable

data class BehavioralPatternsAnalysis(
    val patterns: List<BehavioralPattern>,
    val overallCategory: String, // Transparent, Somewhat Evasive, Highly Evasive
    val evidenceOfDeception: Boolean,
    val details: String
) : Serializable

data class FinancialCorrelationAnalysis(
    val transactionCount: Int,
    val anomalies: List<String>,
    val correlationScore: Float,
    val details: String
) : Serializable

data class CommunicationPatternAnalysis(
    val responseTimeAverage: Long, // milliseconds
    val deletionRate: Float,
    val topicAvoidance: List<String>,
    val patterns: List<String>,
    val details: String
) : Serializable

data class JurisdictionalComplianceAnalysis(
    val jurisdictions: List<String>, // UAE, SA, EU, etc.
    val complianceStatus: Map<String, Boolean>,
    val issues: List<String>,
    val recommendations: List<String>
) : Serializable

data class IntegrityScoreAnalysis(
    val integrityScore: Int, // 0-100
    val category: String, // Excellent, Good, Fair, Poor, Suspect
    val breakdown: Map<String, Int>,
    val keyFindings: List<String>,
    val recommendation: String
) : Serializable

// Forensic Report
data class ForensicReport(
    val caseId: String,
    val caseName: String,
    val createdDate: LocalDateTime,
    val createdBy: String,
    val jurisdiction: String = "UAE", // Default jurisdiction
    val evidence: List<ForensicEvidence>,
    val levelerAnalysis: LevelerAnalysis,
    val chainOfCustody: ChainOfCustodyLog,
    val verificationData: VerificationData,
    val outputPdfHash: String? = null,
    val constitutionalCompliance: ConstitutionalCompliance = ConstitutionalCompliance(),
    val narrationLog: List<NarrationEntry> = listOf()
) : Serializable

// Constitutional Compliance Tracking
data class ConstitutionalCompliance(
    val version: String = "5.2.7",
    val nineBrainArchitecture: NineBrainStatus = NineBrainStatus(),
    val tripleVerificationDoctrine: TripleVerificationStatus = TripleVerificationStatus(),
    val immutablePrinciples: ImmutablePrinciples = ImmutablePrinciples()
) : Serializable

data class NineBrainStatus(
    val evidence: Boolean = true,
    val contradiction: Boolean = true,
    val timeline: Boolean = true,
    val jurisdiction: Boolean = true,
    val behavioural: Boolean = true,
    val harmAnalysis: Boolean = true,
    val ethics: Boolean = true,
    val oversight: Boolean = true,
    val guardian: Boolean = true
) : Serializable

data class TripleVerificationStatus(
    val evidencePhase: Boolean = true,
    val cognitivePhase: Boolean = true,
    val contradictionClearance: Boolean = true
) : Serializable

data class ImmutablePrinciples(
    val truthPrecedesAuthority: Boolean = true,
    val evidencePrecedesNarrative: Boolean = true,
    val guardianshipPrecedesPower: Boolean = true
) : Serializable

// Detailed Narration Entry for forensic narrative building
data class NarrationEntry(
    val id: String,
    val timestamp: LocalDateTime,
    val section: String, // B1, B2, B3, etc.
    val narrative: String,
    val evidenceReferences: List<String>,
    val conclusionLevel: String, // CERTAIN, PROBABLE, POSSIBLE, SPECULATIVE
    val legalRelevance: String,
    val jurisdictionalApplicability: List<String>
) : Serializable

// Chain of Custody
data class ChainOfCustodyLog(
    val entries: List<CustodyEntry>,
    val apkHash: String,
    val apkVerificationStatus: Boolean,
    val deviceId: String,
    val deviceInfo: String
) : Serializable

data class CustodyEntry(
    val timestamp: LocalDateTime,
    val action: String,
    val hash: String,
    val userId: String,
    val deviceId: String,
    val integrityCheckPassed: Boolean
) : Serializable

// Verification Data
data class VerificationData(
    val reportHash: String, // SHA-512
    val sealHash: String, // HMAC-SHA512
    val timestamp: LocalDateTime,
    val qrCodeData: String,
    val watermarkText: String = "VERUM OMNIS FORENSIC SEAL - COURT EXHIBIT",
    val independentVerificationInstructions: String
) : Serializable

// Processing Status
sealed class ProcessingStatus {
    object Idle : ProcessingStatus()
    data class Processing(val stage: String, val progress: Int) : ProcessingStatus()
    data class Success(val report: ForensicReport) : ProcessingStatus()
    data class Error(val message: String, val throwable: Throwable? = null) : ProcessingStatus()
}

// Document Processing Result
data class DocumentProcessingResult(
    val documentId: String,
    val rawText: String,
    val extractedMetadata: Map<String, String>,
    val fileHash: String,
    val fileSize: Long,
    val processingTime: Long, // milliseconds
    val status: String
) : Serializable
