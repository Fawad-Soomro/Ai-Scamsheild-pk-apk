package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Recommendation(
    val action: String,
    val priority: String = "Important", // "Urgent", "Important", "General"
    val description: String = ""
)
