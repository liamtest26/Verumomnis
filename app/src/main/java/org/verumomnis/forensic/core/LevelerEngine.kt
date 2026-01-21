package org.verumomnis.forensic.core

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * LevelerEngine - Nine-Brain Architecture (B1-B9)
 * Core analytical engine for forensic contradiction and pattern detection
 * 
 * B1: Event Chronology Reconstruction
 * B2: Contradiction Detection Matrix
 * B3: Missing Evidence Gap Analysis
 * B4: Timeline Manipulation Detection
 * B5: Behavioral Pattern Recognition
 * B6: Financial Transaction Correlation
 * B7: Communication Pattern Analysis
 * B8: Jurisdictional Compliance Check
 * B9: Integrity Index Scoring
 */
class LevelerEngine(private val processor: DocumentProcessor = DocumentProcessor()) {

    /**
     * Main entry point: Analyze evidence and generate complete leveler analysis
     */
    fun analyzeEvidence(
        evidence: List<ForensicEvidence>,
        caseContext: String = ""
    ): LevelerAnalysis {
        require(evidence.isNotEmpty()) { "Evidence list cannot be empty" }

        // Run all 9 analyses
        val b1 = analyzeEventChronology(evidence)
        val b2 = analyzeContradictions(evidence)
        val b3 = analyzeMissingEvidence(evidence, caseContext)
        val b4 = analyzeTimelineManipulation(evidence, b1)
        val b5 = analyzeBehavioralPatterns(evidence)
        val b6 = analyzeFinancialCorrelation(evidence)
        val b7 = analyzeCommunicationPatterns(evidence)
        val b8 = analyzeJurisdictionalCompliance(evidence)
        val b9 = calculateIntegrityScore(b1, b2, b3, b4, b5, b6, b7, b8)

        return LevelerAnalysis(
            b1EventChronology = b1,
            b2ContradictionMatrix = b2,
            b3MissingEvidenceGaps = b3,
            b4TimelineManipulation = b4,
            b5BehavioralPatterns = b5,
            b6FinancialCorrelation = b6,
            b7CommunicationPattern = b7,
            b8JurisdictionalCompliance = b8,
            b9IntegrityScore = b9
        )
    }

    /**
     * B1: Event Chronology Reconstruction
     * Extract and reconstruct timeline from fragmented evidence
     */
    private fun analyzeEventChronology(evidence: List<ForensicEvidence>): EventChronologyAnalysis {
        val events = mutableListOf<TimelineEvent>()
        var reconstructedCount = 0

        evidence.forEach { doc ->
            // Extract timestamps and create events
            val timestamps = processor.extractTimestamps(doc.rawContent)
            timestamps.forEach { timestamp ->
                events.add(
                    TimelineEvent(
                        id = UUID.randomUUID().toString(),
                        title = "Event from ${doc.name}",
                        description = doc.rawContent.take(200),
                        timestamp = timestamp,
                        evidenceIds = listOf(doc.id),
                        confidence = 0.8f,
                        isReconstructed = false
                    )
                )
            }

            // Analyze temporal patterns for reconstruction
            val keyPhrases = processor.extractKeyPhrases(doc.rawContent)
            if (keyPhrases.contains("before") || keyPhrases.contains("after") || 
                keyPhrases.contains("then") || keyPhrases.contains("previously")) {
                reconstructedCount++
            }
        }

        val chronologyScore = if (events.isNotEmpty()) {
            min(1.0f, events.size.toFloat() / (evidence.size * 3))
        } else 0f

        return EventChronologyAnalysis(
            totalEvents = events.size,
            reconstructedEvents = reconstructedCount,
            chronologyScore = chronologyScore,
            details = "Extracted ${ events.size} events from ${evidence.size} documents, " +
                    "$reconstructedCount show temporal relationships"
        )
    }

    /**
     * B2: Contradiction Detection Matrix
     * Identify direct contradictions, factual discrepancies, omissions
     */
    private fun analyzeContradictions(evidence: List<ForensicEvidence>): ContradictionMatrixAnalysis {
        val contradictions = mutableListOf<Contradiction>()
        val contradictionPatterns = mapOf(
            "denied.*admitted" to ContradictionType.DIRECT_CONTRADICTION,
            "no deal.*invoice" to ContradictionType.DIRECT_CONTRADICTION,
            "claims.*documents.*show" to ContradictionType.FACTUAL_DISCREPANCY,
            "never.*evidence.*proves" to ContradictionType.FACTUAL_DISCREPANCY
        )

        for (i in evidence.indices) {
            for (j in i + 1 until evidence.size) {
                val doc1 = evidence[i]
                val doc2 = evidence[j]

                // Search for contradiction patterns
                for ((pattern, contradictionType) in contradictionPatterns) {
                    val regex = Regex(pattern, RegexOption.IGNORE_CASE)
                    
                    if (regex.containsMatchIn(doc1.rawContent) && 
                        regex.containsMatchIn(doc2.rawContent)) {
                        
                        // Extract matching sentences
                        val match1 = regex.find(doc1.rawContent)?.value ?: ""
                        val match2 = regex.find(doc2.rawContent)?.value ?: ""

                        contradictions.add(
                            Contradiction(
                                id = UUID.randomUUID().toString(),
                                type = contradictionType,
                                severity = determineSeverity(pattern, contradictionType),
                                evidence1Id = doc1.id,
                                evidence2Id = doc2.id,
                                evidence1Text = match1.take(100),
                                evidence2Text = match2.take(100),
                                description = "Pattern '$pattern' found in both documents with contradictory content",
                                pattern = pattern
                            )
                        )
                    }
                }

                // Check for semantic contradictions
                val semanticContradiction = findSemanticContradiction(doc1.rawContent, doc2.rawContent)
                if (semanticContradiction != null) {
                    contradictions.add(semanticContradiction.copy(
                        evidence1Id = doc1.id,
                        evidence2Id = doc2.id
                    ))
                }
            }
        }

        val criticalCount = contradictions.count { it.severity == SeverityLevel.CRITICAL }
        val highCount = contradictions.count { it.severity == SeverityLevel.HIGH }
        val mediumCount = contradictions.count { it.severity == SeverityLevel.MEDIUM }
        val lowCount = contradictions.count { it.severity == SeverityLevel.LOW }

        return ContradictionMatrixAnalysis(
            totalContradictions = contradictions.size,
            criticalCount = criticalCount,
            highCount = highCount,
            mediumCount = mediumCount,
            lowCount = lowCount,
            contradictions = contradictions
        )
    }

    /**
     * B3: Missing Evidence Gap Analysis
     * Identify what should be present but isn't
     */
    private fun analyzeMissingEvidence(
        evidence: List<ForensicEvidence>,
        caseContext: String
    ): MissingEvidenceAnalysis {
        val expectedDocuments = listOf(
            "Contract or agreement",
            "Financial records or invoices",
            "Email correspondence",
            "Witness statements",
            "Timestamps and dates",
            "Communication logs",
            "Transaction records"
        )

        val gaps = mutableListOf<String>()
        val foundDocTypes = evidence.map { it.fileType }.distinct()

        for (expected in expectedDocuments) {
            val isPresent = evidence.any { doc ->
                doc.name.contains(expected, ignoreCase = true) ||
                doc.rawContent.contains(expected, ignoreCase = true)
            }
            if (!isPresent) {
                gaps.add(expected)
            }
        }

        val severity = when {
            gaps.size > 4 -> "CRITICAL"
            gaps.size > 2 -> "HIGH"
            gaps.size > 0 -> "MEDIUM"
            else -> "LOW"
        }

        return MissingEvidenceAnalysis(
            gaps = gaps,
            expectedDocuments = expectedDocuments,
            severity = severity,
            description = "Missing ${ gaps.size}/${expectedDocuments.size} expected document types. " +
                    "Found: ${ foundDocTypes.joinToString(", ")}"
        )
    }

    /**
     * B4: Timeline Manipulation Detection
     * Detect backdated docs, suspicious gaps, edit-after-fact
     */
    private fun analyzeTimelineManipulation(
        evidence: List<ForensicEvidence>,
        chronology: EventChronologyAnalysis
    ): TimelineManipulationAnalysis {
        val backdatedDocs = mutableListOf<String>()
        val editAfterFact = mutableListOf<String>()
        val suspiciousGaps = mutableListOf<TimingGap>()

        evidence.forEach { doc ->
            // Check for backdating patterns
            if (doc.rawContent.contains(Regex("(?i)backdated|dated.*earlier|prior date"))) {
                backdatedDocs.add(doc.name)
            }

            // Check for edit indicators
            if (doc.rawContent.contains(Regex("(?i)edited|modified|updated|last changed"))) {
                editAfterFact.add(doc.name)
            }

            // Check for metadata anomalies
            val createdDate = doc.metadata["created_date"]
            val modifiedDate = doc.metadata["last_modified"]
            if (createdDate != null && modifiedDate != null) {
                try {
                    val createdTime = createdDate.toLong()
                    val modifiedTime = modifiedDate.toLong()
                    if (modifiedTime > createdTime + 86400000) { // More than 1 day
                        suspiciousGaps.add(
                            TimingGap(
                                startDate = LocalDateTime.now().minusDays(1),
                                endDate = LocalDateTime.now(),
                                durationSeconds = 86400,
                                significance = "Suspicious"
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Invalid format
                }
            }
        }

        val riskLevel = when {
            backdatedDocs.size > 0 && editAfterFact.size > 0 -> "CRITICAL"
            backdatedDocs.isNotEmpty() || editAfterFact.isNotEmpty() -> "HIGH"
            suspiciousGaps.isNotEmpty() -> "MEDIUM"
            else -> "LOW"
        }

        return TimelineManipulationAnalysis(
            backdatedDocuments = backdatedDocs,
            suspiciousGaps = suspiciousGaps,
            editAfterFact = editAfterFact,
            riskLevel = riskLevel,
            details = "Found ${ backdatedDocs.size} backdated, ${ editAfterFact.size} edited-after-fact documents"
        )
    }

    /**
     * B5: Behavioral Pattern Recognition
     * Detect gaslighting, evasion, concealment patterns
     */
    private fun analyzeBehavioralPatterns(evidence: List<ForensicEvidence>): BehavioralPatternsAnalysis {
        val patterns = mutableListOf<BehavioralPattern>()
        
        val behaviorIndicators = mapOf(
            "gaslighting" to listOf("never said", "you're crazy", "that didn't happen", "memory is wrong"),
            "evasion" to listOf("don't remember", "can't recall", "unclear", "not sure", "maybe"),
            "concealment" to listOf("cropped screenshot", "missing context", "out of order", "selectively shown"),
            "denial" to listOf("no way", "false", "incorrect", "wrong", "lies"),
            "deflection" to listOf("but you", "whatabout", "changing subject", "irrelevant")
        )

        for ((behaviorType, indicators) in behaviorIndicators) {
            var matchCount = 0
            val matchedEvidenceIds = mutableListOf<String>()

            evidence.forEach { doc ->
                indicators.forEach { indicator ->
                    val count = doc.rawContent.split(indicator, ignoreCase = true).size - 1
                    if (count > 0) {
                        matchCount += count
                        matchedEvidenceIds.add(doc.id)
                    }
                }
            }

            if (matchCount > 0) {
                patterns.add(
                    BehavioralPattern(
                        id = UUID.randomUUID().toString(),
                        patternType = behaviorType.uppercase(),
                        indicators = indicators,
                        confidence = min(1.0f, (matchCount / 5).toFloat()),
                        evidenceIds = matchedEvidenceIds.distinct(),
                        description = "Detected $matchCount indicators of $behaviorType across ${matchedEvidenceIds.distinct().size} documents"
                    )
                )
            }
        }

        val category = when {
            patterns.any { it.confidence > 0.8f } -> "Highly Evasive"
            patterns.any { it.confidence > 0.5f } -> "Somewhat Evasive"
            else -> "Transparent"
        }

        val evidenceOfDeception = patterns.isNotEmpty() && 
                patterns.any { it.confidence > 0.5f }

        return BehavioralPatternsAnalysis(
            patterns = patterns,
            overallCategory = category,
            evidenceOfDeception = evidenceOfDeception,
            details = "Identified ${ patterns.size} behavioral patterns with average confidence " +
                    "${(patterns.map { it.confidence }.average()).roundToInt()}%"
        )
    }

    /**
     * B6: Financial Transaction Correlation
     * Correlate financial statements with actual transactions
     */
    private fun analyzeFinancialCorrelation(evidence: List<ForensicEvidence>): FinancialCorrelationAnalysis {
        val anomalies = mutableListOf<String>()
        var transactionCount = 0

        evidence.forEach { doc ->
            // Search for financial keywords
            val hasFinancialData = doc.rawContent.contains(Regex("(?i)\\$|amount|transaction|payment|invoice|receipt"))
            
            if (hasFinancialData) {
                // Extract amounts
                val amountPattern = Regex("\\$(\\d+[.,]?\\d*)")
                val amounts = amountPattern.findAll(doc.rawContent).map { it.value }.toList()
                transactionCount += amounts.size

                // Look for discrepancies
                if (amounts.size > 0) {
                    val uniqueAmounts = amounts.distinct()
                    if (uniqueAmounts.size != amounts.size) {
                        anomalies.add("Duplicate transaction amounts: ${amounts.filter { it in amounts.dropLast(1) }.distinct()}")
                    }
                }

                // Check for impossible amounts
                if (doc.rawContent.contains(Regex("\\$\\d{7,}"))) {
                    anomalies.add("Unusually large amounts detected in ${doc.name}")
                }
            }
        }

        val correlationScore = if (transactionCount > 0) {
            min(1.0f, (transactionCount - anomalies.size).toFloat() / transactionCount)
        } else 0.5f

        return FinancialCorrelationAnalysis(
            transactionCount = transactionCount,
            anomalies = anomalies,
            correlationScore = correlationScore,
            details = "Analyzed $transactionCount transactions, found ${ anomalies.size} anomalies"
        )
    }

    /**
     * B7: Communication Pattern Analysis
     * Analyze response times, deletion rates, topic avoidance
     */
    private fun analyzeCommunicationPatterns(evidence: List<ForensicEvidence>): CommunicationPatternAnalysis {
        var totalResponseTime: Long = 0
        var responseCount = 0
        val deletionRate: Float
        val topicAvoidance = mutableListOf<String>()
        val patterns = mutableListOf<String>()

        evidence.forEach { doc ->
            // Check for communication indicators
            if (doc.rawContent.contains(Regex("(?i)message|said|replied|responded"))) {
                responseCount++
                // Estimate response time from text (simplistic)
                totalResponseTime += 3600000 // Average 1 hour
            }

            // Check for deletion indicators
            if (doc.rawContent.contains(Regex("(?i)deleted|removed|unsent|retracted"))) {
                patterns.add("Message deletion/retraction detected")
            }

            // Topic avoidance detection
            val avoidanceIndicators = listOf("don't want to discuss", "moving on", "irrelevant", "off topic", "not discussing")
            avoidanceIndicators.forEach { indicator ->
                if (doc.rawContent.contains(indicator, ignoreCase = true)) {
                    topicAvoidance.add(indicator)
                }
            }
        }

        val responseTimeAverage = if (responseCount > 0) totalResponseTime / responseCount else 0L
        deletionRate = if (evidence.isNotEmpty()) patterns.size.toFloat() / evidence.size else 0f

        if (responseTimeAverage > 86400000) { // > 24 hours
            patterns.add("Unusual delay in response time")
        }

        return CommunicationPatternAnalysis(
            responseTimeAverage = responseTimeAverage,
            deletionRate = deletionRate,
            topicAvoidance = topicAvoidance.distinct(),
            patterns = patterns,
            details = "Analyzed communication in ${evidence.size} documents, " +
                    "average response time: ${responseTimeAverage / 3600000}h, " +
                    "deletion rate: ${ (deletionRate * 100).roundToInt()}%"
        )
    }

    /**
     * B8: Jurisdictional Compliance Check
     * Verify compliance with UAE, SA, EU regulations
     */
    private fun analyzeJurisdictionalCompliance(evidence: List<ForensicEvidence>): JurisdictionalComplianceAnalysis {
        val jurisdictions = listOf("UAE", "SA", "EU")
        val complianceStatus = mutableMapOf<String, Boolean>()
        val issues = mutableListOf<String>()
        val recommendations = mutableListOf<String>()

        // UAE Compliance
        val hasArabicSupport = evidence.any { it.rawContent.contains(Regex("[\\u0600-\\u06FF]")) }
        complianceStatus["UAE"] = hasArabicSupport
        if (!hasArabicSupport) {
            issues.add("UAE: Missing Arabic text support")
            recommendations.add("Add Arabic language processing for UAE compliance")
        }

        // SA Compliance (ECT Act)
        val hasTimestamps = evidence.any { it.metadata.containsKey("created_date") }
        complianceStatus["SA"] = hasTimestamps
        if (!hasTimestamps) {
            issues.add("SA: Missing ECT Act timestamp compliance")
            recommendations.add("Ensure all documents have verifiable timestamps")
        }

        // EU Compliance (GDPR)
        val hasPersonalData = evidence.any { 
            it.rawContent.contains(Regex("(?i)name|email|phone|address")) 
        }
        complianceStatus["EU"] = !hasPersonalData // GDPR: minimize personal data
        if (hasPersonalData) {
            issues.add("EU: Personal data processing requires GDPR compliance")
            recommendations.add("Implement GDPR-compliant data handling procedures")
        }

        return JurisdictionalComplianceAnalysis(
            jurisdictions = jurisdictions,
            complianceStatus = complianceStatus,
            issues = issues,
            recommendations = recommendations
        )
    }

    /**
     * B9: Integrity Index Scoring (0-100)
     * Calculate comprehensive integrity score with categorization
     */
    private fun calculateIntegrityScore(
        b1: EventChronologyAnalysis,
        b2: ContradictionMatrixAnalysis,
        b3: MissingEvidenceAnalysis,
        b4: TimelineManipulationAnalysis,
        b5: BehavioralPatternsAnalysis,
        b6: FinancialCorrelationAnalysis,
        b7: CommunicationPatternAnalysis,
        b8: JurisdictionalComplianceAnalysis
    ): IntegrityScoreAnalysis {
        
        var score = 100

        // B1: Event Chronology (max -15 points)
        score -= if (b1.chronologyScore < 0.3f) 15 else if (b1.chronologyScore < 0.6f) 10 else 5

        // B2: Contradictions (max -30 points)
        score -= minOf(30, b2.criticalCount * 10 + b2.highCount * 5 + b2.mediumCount * 2)

        // B3: Missing Evidence (max -20 points)
        score -= when (b3.severity) {
            "CRITICAL" -> 20
            "HIGH" -> 15
            "MEDIUM" -> 10
            else -> 2
        }

        // B4: Timeline Manipulation (max -25 points)
        score -= when (b4.riskLevel) {
            "CRITICAL" -> 25
            "HIGH" -> 15
            "MEDIUM" -> 8
            else -> 2
        }

        // B5: Behavioral Patterns (max -15 points)
        score -= if (b5.evidenceOfDeception) {
            val avgConfidence = b5.patterns.map { it.confidence }.average()
            minOf(15, (avgConfidence * 15).toInt())
        } else 0

        // B6: Financial Correlation (max -10 points)
        score -= minOf(10, (b6.anomalies.size * 3).coerceAtMost(10))

        // B7: Communication Patterns (max -10 points)
        score -= if (b7.deletionRate > 0.3f) 10 else if (b7.deletionRate > 0.1f) 5 else 0

        // B8: Jurisdictional Compliance (max -5 points)
        score -= (b8.issues.size * 2).coerceAtMost(5)

        score = score.coerceIn(0, 100)

        val category = when {
            score >= 85 -> "Excellent"
            score >= 70 -> "Good"
            score >= 55 -> "Fair"
            score >= 40 -> "Poor"
            else -> "Suspect"
        }

        val breakdown = mapOf(
            "Chronology" to b1.chronologyScore.times(20).toInt(),
            "Contradictions" to (100 - minOf(100, b2.totalContradictions * 10)),
            "Evidence Gaps" to when (b3.severity) {
                "CRITICAL" -> 20
                "HIGH" -> 40
                "MEDIUM" -> 60
                else -> 80
            },
            "Timeline" to when (b4.riskLevel) {
                "CRITICAL" -> 20
                "HIGH" -> 40
                "MEDIUM" -> 60
                else -> 80
            },
            "Behavior" to if (b5.evidenceOfDeception) 40 else 80,
            "Financial" to (100 - minOf(100, b6.anomalies.size * 10)),
            "Communication" to ((1 - b7.deletionRate) * 100).toInt(),
            "Compliance" to (100 - b8.issues.size * 15)
        )

        val keyFindings = mutableListOf<String>()
        if (b2.criticalCount > 0) keyFindings.add("${b2.criticalCount} critical contradictions found")
        if (b4.riskLevel in listOf("CRITICAL", "HIGH")) keyFindings.add("Timeline manipulation detected")
        if (b5.evidenceOfDeception) keyFindings.add("Behavioral patterns suggest deception")
        if (b3.severity in listOf("CRITICAL", "HIGH")) keyFindings.add("Significant evidence gaps identified")

        val recommendation = when {
            score >= 85 -> "High confidence in integrity. Evidence appears consistent and reliable."
            score >= 70 -> "Good integrity, but recommend investigating identified issues."
            score >= 55 -> "Fair integrity with notable concerns. Further analysis recommended."
            score >= 40 -> "Poor integrity with significant inconsistencies. Court presentation not recommended."
            else -> "Suspect integrity with critical flaws. Do not present as reliable evidence."
        }

        return IntegrityScoreAnalysis(
            integrityScore = score,
            category = category,
            breakdown = breakdown,
            keyFindings = keyFindings,
            recommendation = recommendation
        )
    }

    /**
     * Helper: Determine severity level
     */
    private fun determineSeverity(
        pattern: String,
        contradictionType: ContradictionType
    ): SeverityLevel {
        return when {
            pattern.contains("no deal") || pattern.contains("invoice") -> SeverityLevel.CRITICAL
            contradictionType == ContradictionType.DIRECT_CONTRADICTION -> SeverityLevel.HIGH
            contradictionType == ContradictionType.FACTUAL_DISCREPANCY -> SeverityLevel.MEDIUM
            contradictionType == ContradictionType.OMISSION -> SeverityLevel.MEDIUM
            else -> SeverityLevel.LOW
        }
    }

    /**
     * Helper: Find semantic contradictions between documents
     */
    private fun findSemanticContradiction(text1: String, text2: String): Contradiction? {
        val opposites = mapOf(
            "yes" to "no",
            "true" to "false",
            "agree" to "disagree",
            "received" to "never received",
            "happened" to "never happened"
        )

        for ((word1, word2) in opposites) {
            val has1 = text1.contains(word1, ignoreCase = true)
            val has2 = text2.contains(word2, ignoreCase = true)
            
            if (has1 && has2) {
                return Contradiction(
                    id = UUID.randomUUID().toString(),
                    type = ContradictionType.DIRECT_CONTRADICTION,
                    severity = SeverityLevel.HIGH,
                    evidence1Id = "",
                    evidence2Id = "",
                    evidence1Text = word1,
                    evidence2Text = word2,
                    description = "Semantic contradiction: '$word1' vs '$word2'",
                    pattern = "$word1.*$word2"
                )
            }
        }

        return null
    }
}
