package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_records")
data class ScanRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val inputType: String, // "MESSAGE", "SCREENSHOT", "URL"
    val contentPreview: String,
    val rawInput: String,
    val extractedText: String? = null,
    val language: String = "English",
    val category: String = "Legitimate / Low Risk",
    val riskScore: Int = 0,
    val riskLevel: String = "Low",
    val confidence: Double = 0.85,
    val summary: String = "",
    val indicatorsJson: String = "[]",
    val recommendationsJson: String = "[]",
    val urlFeaturesJson: String? = null,
    val modelName: String = "gemini-3.5-flash",
    val timestampMs: Long = System.currentTimeMillis(),
    val executionTimeMs: Long = 0L
)
