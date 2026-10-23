package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UrlFeatures(
    val originalUrl: String,
    val scheme: String = "https",
    val domain: String = "",
    val domainLength: Int = 0,
    val urlLength: Int = 0,
    val pathLength: Int = 0,
    val subdomainCount: Int = 0,
    val queryParamCount: Int = 0,
    val usesHttps: Boolean = true,
    val containsIpAddress: Boolean = false,
    val containsPunycode: Boolean = false,
    val containsShortener: Boolean = false,
    val containsPakistaniBrandKeywords: Boolean = false,
    val suspiciousTld: Boolean = false,
    val flags: List<String> = emptyList(),
    val heuristicRiskScore: Int = 0
)
