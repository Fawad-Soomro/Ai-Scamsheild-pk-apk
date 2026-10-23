package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ScamAnalysisResult(
    val riskScore: Int = 0, // 0 - 100
    val riskLevel: String = "Low", // "Low", "Moderate", "High", "Critical"
    val category: String = "Legitimate / Low Risk",
    val confidence: Double = 0.85,
    val language: String = "English",
    val summary: String = "",
    val indicators: List<RiskIndicator> = emptyList(),
    val recommendations: List<Recommendation> = emptyList(),
    val extractedText: String? = null,
    val urlFeatures: UrlFeatures? = null,
    val modelName: String = "gemini-3.5-flash",
    val rawJson: String = "",
    val executionTimeMs: Long = 0L
)
