package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RiskIndicator(
    val name: String,
    val severity: String, // "Critical", "High", "Moderate", "Low"
    val evidence: String,
    val explanation: String
)
