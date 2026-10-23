package com.example.data.repository

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.ScanDao
import com.example.data.model.Recommendation
import com.example.data.model.RiskIndicator
import com.example.data.model.ScamAnalysisResult
import com.example.data.model.ScanRecord
import com.example.data.model.UrlFeatures
import com.example.data.remote.Content
import com.example.data.remote.GenerateContentRequest
import com.example.data.remote.GenerationConfig
import com.example.data.remote.InlineData
import com.example.data.remote.Part
import com.example.data.remote.RetrofitClient
import com.example.engine.TextPreprocessor
import com.example.engine.UrlRiskAnalyzer
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

data class DashboardMetrics(
    val totalScans: Int = 0,
    val highRiskCount: Int = 0,
    val moderateRiskCount: Int = 0,
    val lowRiskCount: Int = 0,
    val averageRiskScore: Int = 0,
    val mostCommonCategory: String = "None",
    val topRiskLevel: String = "Low",
    val scansThisWeek: Int = 0,
    val topIndicators: List<Pair<String, Int>> = emptyList(),
    val personalizedSafetyTips: List<String> = emptyList()
)

class ScamShieldRepository(
    private val scanDao: ScanDao
) {

    private val moshi: Moshi = RetrofitClient.moshiInstance

    private val indicatorListType = Types.newParameterizedType(List::class.java, RiskIndicator::class.java)
    private val indicatorAdapter = moshi.adapter<List<RiskIndicator>>(indicatorListType)

    private val recommendationListType = Types.newParameterizedType(List::class.java, Recommendation::class.java)
    private val recommendationAdapter = moshi.adapter<List<Recommendation>>(recommendationListType)

    private val urlFeaturesAdapter = moshi.adapter(UrlFeatures::class.java)

    private val systemInstructionText = """
        You are AI ScamShield PK, a specialized digital safety and cybersecurity risk assessment engine tailored for Pakistan and global users.
        You understand English, Urdu (اردو script), Roman Urdu (e.g. 'Aapka account block ho jayega, foran OTP share karein'), and mixed language messages.
        
        Analyze the submitted content (message, image, or URL static features) with high precision and explainability.
        DO NOT invent evidence. Only cite evidence directly present in the input.
        Communicate uncertainty: do not claim absolute certainty.
        
        You MUST return ONLY a strictly valid JSON object with the following schema:
        {
          "risk_score": <integer between 0 and 100>,
          "risk_level": "<Low | Moderate | High | Critical>",
          "category": "<e.g. Banking Scam | OTP / Account Takeover | Prize / Lottery Scam | Job Scam | Loan Scam | Impersonation Scam | Phishing URL | Delivery Scam | Legitimate / Low Risk>",
          "confidence": <float between 0.0 and 1.0>,
          "language": "<English | Urdu (اردو) | Roman Urdu | Mixed>",
          "summary": "<Concise 1-2 sentence overview of findings>",
          "indicators": [
            {
              "name": "<Indicator title e.g. OTP Request, Urgency Pressure, Upfront Fee Demand, Unofficial Domain, Prize Lure>",
              "severity": "<Critical | High | Moderate | Low>",
              "evidence": "<Exact short snippet or feature from the content>",
              "explanation": "<Clear reason why this is suspicious>"
            }
          ],
          "recommendations": [
            {
              "action": "<Direct actionable safety step e.g. Do not share your OTP or PIN>",
              "priority": "<Urgent | Important | General>",
              "description": "<Practical helpful detail>"
            }
          ],
          "extracted_text": "<If image analysis was performed, provide the full extracted text from the screenshot; otherwise null>"
        }
    """.trimIndent()

    suspend fun analyzeMessage(message: String, model: String = "gemini-3.5-flash"): Result<ScamAnalysisResult> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val entities = TextPreprocessor.preprocess(message)

        val prompt = """
            Analyze the following message received by a user in Pakistan for potential scam or fraudulent risk:
            
            MESSAGE CONTENT:
            \"\"\"
            $message
            \"\"\"
            
            Extracted Local Preprocessing Context:
            - Detected Language Script: ${entities.detectedLanguage}
            - Detected URLs: ${entities.urls.joinToString(", ").ifEmpty { "None" }}
            - Detected Phone Numbers: ${entities.phoneNumbers.joinToString(", ").ifEmpty { "None" }}
            - Detected Amounts: ${entities.amountsPkr.joinToString(", ").ifEmpty { "None" }}
            - Has Urgency Signals: ${entities.hasUrgencySignals}
            - Has OTP Keywords: ${entities.hasOtpKeywords}
            - Has Financial Keywords: ${entities.hasFinancialKeywords}
            
            Return ONLY the valid JSON object according to the system instruction.
        """.trimIndent()

        callGeminiApi(
            prompt = prompt,
            model = model,
            bitmap = null,
            extractedTextFallback = null,
            urlFeatures = null,
            startTime = startTime,
            fallbackGenerator = {
                createLocalMessageFallback(message, entities, model, startTime)
            }
        )
    }

    suspend fun analyzeScreenshot(bitmap: Bitmap, model: String = "gemini-3.5-flash"): Result<ScamAnalysisResult> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val base64Image = bitmapToBase64(bitmap)

        val prompt = """
            Perform multimodal OCR and scam risk assessment on this screenshot of a message/app screen/social media post received in Pakistan.
            
            Tasks:
            1. Extract all readable text from the screenshot into the 'extracted_text' field.
            2. Analyze the text and visual indicators for digital fraud, phishing, OTP harvesting, fake lottery (BISP/Ehsaas/Jeeto Pakistan), fake jobs, fake loan apps, or impersonation.
            3. Return ONLY the valid JSON object according to the system instruction.
        """.trimIndent()

        callGeminiApi(
            prompt = prompt,
            model = model,
            bitmap = base64Image,
            extractedTextFallback = "Text extracted via Gemini Multimodal Vision",
            urlFeatures = null,
            startTime = startTime,
            fallbackGenerator = {
                createLocalImageFallback(model, startTime)
            }
        )
    }

    suspend fun analyzeUrl(url: String, model: String = "gemini-3.5-flash"): Result<ScamAnalysisResult> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val urlFeatures = UrlRiskAnalyzer.analyzeUrl(url)

        val prompt = """
            Analyze the following URL submitted by a Pakistani user for phishing, credential harvesting, brand impersonation, and fraudulent infrastructure:
            
            URL: $url
            
            STATIC URL FEATURE EXTRACTION MATRIX:
            - Target URL: ${urlFeatures.originalUrl}
            - Domain: ${urlFeatures.domain}
            - Protocol: ${urlFeatures.scheme} (HTTPS: ${urlFeatures.usesHttps})
            - Domain Length: ${urlFeatures.domainLength} chars | Total URL Length: ${urlFeatures.urlLength} chars
            - Subdomain Count: ${urlFeatures.subdomainCount}
            - Query Parameters Count: ${urlFeatures.queryParamCount}
            - Raw IP Address in Domain: ${urlFeatures.containsIpAddress}
            - Punycode / IDN Homograph: ${urlFeatures.containsPunycode}
            - URL Shortener: ${urlFeatures.containsShortener}
            - Pakistani Brand Detected: ${urlFeatures.containsPakistaniBrandKeywords}
            - Suspicious High-Risk TLD: ${urlFeatures.suspiciousTld}
            - Local Heuristic Flags: ${urlFeatures.flags.joinToString("; ").ifEmpty { "None" }}
            - Local Heuristic Score: ${urlFeatures.heuristicRiskScore} / 100
            
            Evaluate both the technical static features and semantic intention behind this link.
            Return ONLY the valid JSON object according to the system instruction.
        """.trimIndent()

        callGeminiApi(
            prompt = prompt,
            model = model,
            bitmap = null,
            extractedTextFallback = null,
            urlFeatures = urlFeatures,
            startTime = startTime,
            fallbackGenerator = {
                createLocalUrlFallback(urlFeatures, model, startTime)
            }
        )
    }

    private suspend fun callGeminiApi(
        prompt: String,
        model: String,
        bitmap: String?,
        extractedTextFallback: String?,
        urlFeatures: UrlFeatures?,
        startTime: Long,
        fallbackGenerator: () -> ScamAnalysisResult
    ): Result<ScamAnalysisResult> {
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w("ScamShieldRepo", "Gemini API key is placeholder or empty. Using intelligent local rule engine fallback.")
            return Result.success(fallbackGenerator())
        }

        return try {
            val parts = mutableListOf<Part>()
            parts.add(Part(text = prompt))
            if (bitmap != null) {
                parts.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = bitmap)))
            }

            val request = GenerateContentRequest(
                contents = listOf(Content(parts = parts)),
                generationConfig = GenerationConfig(
                    temperature = 0.15f,
                    responseMimeType = "application/json"
                ),
                systemInstruction = Content(parts = listOf(Part(text = systemInstructionText)))
            )

            val response = RetrofitClient.geminiService.generateContent(
                model = model,
                apiKey = apiKey,
                request = request
            )

            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw IllegalStateException("Gemini returned empty candidate response")

            val executionTime = System.currentTimeMillis() - startTime
            val parsedResult = parseGeminiJsonResponse(
                jsonText = responseText,
                modelName = model,
                executionTimeMs = executionTime,
                urlFeatures = urlFeatures,
                extractedTextFallback = extractedTextFallback
            )

            Result.success(parsedResult)
        } catch (e: Exception) {
            Log.e("ScamShieldRepo", "Error calling Gemini API: ${e.message}", e)
            // If network or API fails, gracefully provide explainable local fallback
            val localResult = fallbackGenerator()
            Result.success(localResult)
        }
    }

    private fun parseGeminiJsonResponse(
        jsonText: String,
        modelName: String,
        executionTimeMs: Long,
        urlFeatures: UrlFeatures?,
        extractedTextFallback: String?
    ): ScamAnalysisResult {
        // Strip markdown code fences if present
        var cleaned = jsonText.trim()
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.removePrefix("```json").trim()
        }
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.removePrefix("```").trim()
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.removeSuffix("```").trim()
        }

        val jsonObject = JSONObject(cleaned)

        val riskScore = jsonObject.optInt("risk_score", urlFeatures?.heuristicRiskScore ?: 50).coerceIn(0, 100)
        val riskLevel = jsonObject.optString("risk_level", calculateRiskLevel(riskScore))
        val category = jsonObject.optString("category", "General Digital Fraud")
        val confidence = jsonObject.optDouble("confidence", 0.88).coerceIn(0.1, 1.0)
        val language = jsonObject.optString("language", "English")
        val summary = jsonObject.optString("summary", "Analysis completed.")
        val extractedText = jsonObject.optString("extracted_text", extractedTextFallback ?: "")

        val indicators = mutableListOf<RiskIndicator>()
        val indArray = jsonObject.optJSONArray("indicators")
        if (indArray != null) {
            for (i in 0 until indArray.length()) {
                val item = indArray.optJSONObject(i) ?: continue
                indicators.add(
                    RiskIndicator(
                        name = item.optString("name", "Suspicious Indicator"),
                        severity = item.optString("severity", "Moderate"),
                        evidence = item.optString("evidence", ""),
                        explanation = item.optString("explanation", "")
                    )
                )
            }
        }

        val recommendations = mutableListOf<Recommendation>()
        val recArray = jsonObject.optJSONArray("recommendations")
        if (recArray != null) {
            for (i in 0 until recArray.length()) {
                val item = recArray.optJSONObject(i)
                if (item != null) {
                    recommendations.add(
                        Recommendation(
                            action = item.optString("action", "Stay alert"),
                            priority = item.optString("priority", "Important"),
                            description = item.optString("description", "")
                        )
                    )
                } else {
                    val str = recArray.optString(i)
                    if (str.isNotEmpty()) {
                        recommendations.add(Recommendation(action = str))
                    }
                }
            }
        }

        return ScamAnalysisResult(
            riskScore = riskScore,
            riskLevel = riskLevel,
            category = category,
            confidence = confidence,
            language = language,
            summary = summary,
            indicators = indicators,
            recommendations = recommendations,
            extractedText = if (extractedText.isNotEmpty()) extractedText else null,
            urlFeatures = urlFeatures,
            modelName = modelName,
            rawJson = cleaned,
            executionTimeMs = executionTimeMs
        )
    }

    private fun calculateRiskLevel(score: Int): String {
        return when {
            score >= 75 -> "Critical"
            score >= 50 -> "High"
            score >= 25 -> "Moderate"
            else -> "Low"
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // Resize bitmap if very large to prevent memory overhead while keeping text sharp
        val maxDim = 1200
        val scaledBitmap = if (bitmap.width > maxDim || bitmap.height > maxDim) {
            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val newWidth = if (ratio > 1) maxDim else (maxDim * ratio).toInt()
            val newHeight = if (ratio > 1) (maxDim / ratio).toInt() else maxDim
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } else {
            bitmap
        }
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    // Local Fallback Engines (When Offline or Key not entered)
    private fun createLocalMessageFallback(
        message: String,
        entities: com.example.engine.ExtractedEntities,
        model: String,
        startTime: Long
    ): ScamAnalysisResult {
        val indicators = mutableListOf<RiskIndicator>()
        val recommendations = mutableListOf<Recommendation>()
        var baseScore = 10

        if (entities.hasOtpKeywords) {
            baseScore += 35
            indicators.add(
                RiskIndicator(
                    name = "Verification Code / OTP Request",
                    severity = "Critical",
                    evidence = "OTP / PIN / Verification terms detected",
                    explanation = "Legitimate banks (Easypaisa, JazzCash, HBL) NEVER ask customers to share their OTP or PIN."
                )
            )
            recommendations.add(
                Recommendation(
                    action = "Never share your 4-digit PIN or OTP with anyone",
                    priority = "Urgent",
                    description = "Sharing OTP grants attackers full access to transfer your funds or hijack your account."
                )
            )
        }

        if (entities.hasUrgencySignals) {
            baseScore += 25
            indicators.add(
                RiskIndicator(
                    name = "Psychological Urgency & Fear Pressure",
                    severity = "High",
                    evidence = "Threat of account suspension / immediate time limit",
                    explanation = "Scammers manufacture artificial urgency (e.g. '24 ghante mein band hoga') to prevent victims from verifying."
                )
            )
        }

        if (entities.hasFinancialKeywords) {
            baseScore += 20
            indicators.add(
                RiskIndicator(
                    name = "Unsolicited Financial / Prize Lure",
                    severity = "Moderate",
                    evidence = "Cash reward / Lottery / Loan approval terms",
                    explanation = "Unsolicited monetary promises (e.g. BISP 25,000, prize draws) frequently precede advance-fee requests."
                )
            )
        }

        if (entities.urls.isNotEmpty()) {
            baseScore += 15
            indicators.add(
                RiskIndicator(
                    name = "External Link in Message",
                    severity = "Moderate",
                    evidence = entities.urls.firstOrNull() ?: "Link present",
                    explanation = "Links in SMS or WhatsApp often redirect to credential harvesting phishing web pages."
                )
            )
            recommendations.add(
                Recommendation(
                    action = "Do not open links from unverified senders",
                    priority = "Important",
                    description = "Always navigate to official banking apps or websites directly."
                )
            )
        }

        val score = baseScore.coerceIn(5, 95)
        val level = calculateRiskLevel(score)
        val category = when {
            entities.hasOtpKeywords -> "Banking / OTP Account Takeover"
            entities.hasFinancialKeywords -> "Prize / Advance Fee Scam"
            entities.urls.isNotEmpty() -> "Phishing Message"
            else -> "Suspicious Communication"
        }

        recommendations.add(
            Recommendation(
                action = "Report suspicious numbers to FIA Cyber Crime (1991) or PTA (0800-55055)",
                priority = "General",
                description = "Helps regulatory authorities block fraudulent SIM cards and accounts."
            )
        )

        return ScamAnalysisResult(
            riskScore = score,
            riskLevel = level,
            category = category,
            confidence = 0.80,
            language = entities.detectedLanguage,
            summary = "Local heuristic assessment detected ${indicators.size} suspicious risk factors in this ${entities.detectedLanguage} message.",
            indicators = indicators,
            recommendations = recommendations,
            extractedText = null,
            urlFeatures = null,
            modelName = "$model (Local Fallback Mode)",
            rawJson = "{}",
            executionTimeMs = System.currentTimeMillis() - startTime
        )
    }

    private fun createLocalImageFallback(model: String, startTime: Long): ScamAnalysisResult {
        return ScamAnalysisResult(
            riskScore = 55,
            riskLevel = "Moderate",
            category = "Screenshot / Image Scan",
            confidence = 0.75,
            language = "Multilingual",
            summary = "Image received. For full AI multimodal OCR and deep reasoning, ensure your internet connection and Gemini API key are active.",
            indicators = listOf(
                RiskIndicator(
                    name = "Image Multimodal Analysis Required",
                    severity = "Moderate",
                    evidence = "Screenshot submitted",
                    explanation = "Examine the screenshot carefully for spoofed sender IDs, fake logos, or requests for PIN/OTP."
                )
            ),
            recommendations = listOf(
                Recommendation(
                    action = "Verify the official sender ID and contact details",
                    priority = "Important",
                    description = "Official Pakistani banks use registered shortcodes (e.g. 8842, 3737, 4250), not random 11-digit mobile numbers."
                )
            ),
            extractedText = "Screenshot processed in local standby mode.",
            urlFeatures = null,
            modelName = "$model (Local Fallback)",
            rawJson = "{}",
            executionTimeMs = System.currentTimeMillis() - startTime
        )
    }

    private fun createLocalUrlFallback(
        features: UrlFeatures,
        model: String,
        startTime: Long
    ): ScamAnalysisResult {
        val indicators = mutableListOf<RiskIndicator>()
        val recommendations = mutableListOf<Recommendation>()

        for (flag in features.flags) {
            indicators.add(
                RiskIndicator(
                    name = "Technical URL Anomaly",
                    severity = if (features.heuristicRiskScore >= 60) "High" else "Moderate",
                    evidence = features.domain,
                    explanation = flag
                )
            )
        }

        if (features.containsPakistaniBrandKeywords) {
            recommendations.add(
                Recommendation(
                    action = "Do NOT enter credentials or wallet PIN on this website",
                    priority = "Urgent",
                    description = "This site appears to impersonate a Pakistani brand on an unofficial non-governmental/non-banking domain."
                )
            )
        }

        recommendations.add(
            Recommendation(
                action = "Check for official domain endings (.com.pk, .gov.pk, .edu.pk)",
                priority = "Important",
                description = "Authentic Pakistani financial and public services use verified registered country domains."
            )
        )

        val score = features.heuristicRiskScore.coerceIn(5, 95)
        val level = calculateRiskLevel(score)
        val category = if (score > 40) "Phishing URL / Domain Spoofing" else "Low Risk / Standard Link"

        return ScamAnalysisResult(
            riskScore = score,
            riskLevel = level,
            category = category,
            confidence = 0.85,
            language = "English",
            summary = "Static URL analysis identified ${features.flags.size} risk markers with a technical heuristic score of $score/100.",
            indicators = indicators,
            recommendations = recommendations,
            extractedText = null,
            urlFeatures = features,
            modelName = "$model (Static Feature Engine)",
            rawJson = "{}",
            executionTimeMs = System.currentTimeMillis() - startTime
        )
    }

    // Room Database Operations
    suspend fun saveScanRecord(
        result: ScamAnalysisResult,
        inputType: String,
        rawInput: String
    ): Long = withContext(Dispatchers.IO) {
        val snippet = when (inputType) {
            "URL" -> rawInput.take(120)
            "SCREENSHOT" -> result.extractedText?.take(120) ?: "Screenshot Image Scan"
            else -> rawInput.take(120).replace("\n", " ")
        }

        val indicatorsJson = indicatorAdapter.toJson(result.indicators)
        val recommendationsJson = recommendationAdapter.toJson(result.recommendations)
        val urlFeaturesJson = result.urlFeatures?.let { urlFeaturesAdapter.toJson(it) }

        val record = ScanRecord(
            inputType = inputType,
            contentPreview = snippet,
            rawInput = rawInput,
            extractedText = result.extractedText,
            language = result.language,
            category = result.category,
            riskScore = result.riskScore,
            riskLevel = result.riskLevel,
            confidence = result.confidence,
            summary = result.summary,
            indicatorsJson = indicatorsJson,
            recommendationsJson = recommendationsJson,
            urlFeaturesJson = urlFeaturesJson,
            modelName = result.modelName,
            timestampMs = System.currentTimeMillis(),
            executionTimeMs = result.executionTimeMs
        )

        scanDao.insertScan(record)
    }

    fun getAllScans(): Flow<List<ScanRecord>> = scanDao.getAllScans()

    fun getScanById(id: Long): Flow<ScanRecord?> = scanDao.observeScanById(id)

    suspend fun getScanByIdSync(id: Long): ScanRecord? = scanDao.getScanById(id)

    fun searchScans(query: String): Flow<List<ScanRecord>> = scanDao.searchScans(query)

    fun filterScansByRisk(level: String): Flow<List<ScanRecord>> = scanDao.getScansByRiskLevel(level)

    fun filterScansByInputType(type: String): Flow<List<ScanRecord>> = scanDao.getScansByInputType(type)

    suspend fun deleteScan(id: Long) = withContext(Dispatchers.IO) {
        scanDao.deleteScanById(id)
    }

    suspend fun deleteAllScans() = withContext(Dispatchers.IO) {
        scanDao.deleteAllScans()
    }

    fun getDashboardMetrics(): Flow<DashboardMetrics> {
        return scanDao.getAllScans().combine(scanDao.getTotalCount()) { scans, total ->
            if (scans.isEmpty()) {
                DashboardMetrics()
            } else {
                val high = scans.count { it.riskLevel == "High" || it.riskLevel == "Critical" }
                val moderate = scans.count { it.riskLevel == "Moderate" }
                val low = scans.count { it.riskLevel == "Low" }
                val avgScore = (scans.map { it.riskScore }.average()).toInt()
                val topCat = scans.groupBy { it.category }
                    .maxByOrNull { it.value.size }?.key ?: "None"
                val topRisk = when {
                    high > 0 -> "Critical"
                    moderate > 0 -> "Moderate"
                    else -> "Low"
                }

                val oneWeekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
                val scansThisWeek = scans.count { it.timestampMs >= oneWeekAgo }

                // Count detected indicators
                val indicatorCounts = mutableMapOf<String, Int>()
                scans.forEach { scan ->
                    val indList = deserializeIndicators(scan.indicatorsJson)
                    indList.forEach { ind ->
                        val key = ind.name
                        indicatorCounts[key] = (indicatorCounts[key] ?: 0) + 1
                    }
                }
                val topIndicators = indicatorCounts.toList().sortedByDescending { it.second }.take(4)

                // Generate personalized safety suggestions based on local history
                val tips = mutableListOf<String>()
                val hasOtpScans = scans.any { it.category.contains("OTP", true) || it.summary.contains("OTP", true) }
                val hasUrlScans = scans.any { it.inputType == "URL" || it.category.contains("Phishing", true) }
                val hasPrizeScans = scans.any { it.category.contains("Lottery", true) || it.category.contains("Prize", true) || it.category.contains("BISP", true) }

                if (hasOtpScans) {
                    tips.add("OTP Vigilance: Legitimate banks and wallet services (EasyPaisa, JazzCash) NEVER ask customers for their SMS verification code.")
                }
                if (hasUrlScans) {
                    tips.add("URL Precaution: Always verify the exact website domain name before entering wallet credentials. Look for authentic .pk / .com.pk domains.")
                }
                if (hasPrizeScans) {
                    tips.add("No Advance Fees: Legitimate government aid (e.g. BISP 8171) or prize draws never require sending upfront deposit fees.")
                }
                if (tips.isEmpty()) {
                    tips.add("Proactive Protection: Keep checking suspicious SMS and WhatsApp messages before clicking any embedded links.")
                }

                DashboardMetrics(
                    totalScans = total,
                    highRiskCount = high,
                    moderateRiskCount = moderate,
                    lowRiskCount = low,
                    averageRiskScore = avgScore,
                    mostCommonCategory = topCat,
                    topRiskLevel = topRisk,
                    scansThisWeek = scansThisWeek,
                    topIndicators = topIndicators,
                    personalizedSafetyTips = tips
                )
            }
        }
    }

    suspend fun askAssistant(
        record: ScanRecord,
        userQuestion: String,
        model: String = "gemini-3.5-flash"
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val indicators = deserializeIndicators(record.indicatorsJson)
        val recommendations = deserializeRecommendations(record.recommendationsJson)

        val assistantSystemInstruction = """
            You are the AI Scam Assistant in AI ScamShield PK.
            Your role is to help a user in Pakistan understand their scan result and provide practical cybersecurity advice.
            
            Current Scan Context:
            - Content Type: ${record.inputType}
            - Severity & Score: ${record.riskLevel.uppercase()} (${record.riskScore}/100)
            - Category: ${record.category}
            - Summary: ${record.summary}
            - Indicators: ${indicators.joinToString("; ") { "${it.name}: ${it.evidence} (${it.explanation})" }}
            - Safety Steps: ${recommendations.joinToString("; ") { it.action }}
            
            Guidelines:
            1. Base your answer strictly on the facts of this scan and established cybersecurity best practices.
            2. Never ask the user to share passwords, OTPs, PINs, or banking details.
            3. Answer in the same language as the user's question (supports English, Urdu script, or Roman Urdu).
            4. Keep answers concise (2-4 short paragraphs), practical, and beginner-friendly.
            5. Explain technical terms simply.
            6. Distinguish clearly between detected evidence, potential scam mechanisms, and practical precautions.
        """.trimIndent()

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.success(generateLocalAssistantFallback(record, userQuestion, indicators, recommendations))
        }

        try {
            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = "User Question: $userQuestion")))),
                generationConfig = GenerationConfig(
                    temperature = 0.3f,
                    responseMimeType = "text/plain"
                ),
                systemInstruction = Content(parts = listOf(Part(text = assistantSystemInstruction)))
            )

            val response = RetrofitClient.geminiService.generateContent(
                model = model,
                apiKey = apiKey,
                request = request
            )

            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: generateLocalAssistantFallback(record, userQuestion, indicators, recommendations)

            Result.success(text)
        } catch (e: Exception) {
            Log.e("ScamShieldRepo", "Assistant API error: ${e.message}")
            Result.success(generateLocalAssistantFallback(record, userQuestion, indicators, recommendations))
        }
    }

    private fun generateLocalAssistantFallback(
        record: ScanRecord,
        question: String,
        indicators: List<RiskIndicator>,
        recommendations: List<Recommendation>
    ): String {
        val q = question.lowercase()
        return when {
            q.contains("why") || q.contains("flag") || q.contains("reason") || q.contains("kyun") || q.contains("کیوں") -> {
                buildString {
                    appendLine("This content was flagged with a **${record.riskLevel.uppercase()}** risk level (${record.riskScore}/100) under the **${record.category}** category.")
                    if (indicators.isNotEmpty()) {
                        appendLine("\nKey reasons detected:")
                        indicators.forEach { ind ->
                            appendLine("• **${ind.name}**: ${ind.explanation} (Matched: \"${ind.evidence}\")")
                        }
                    } else {
                        appendLine("\n${record.summary}")
                    }
                    appendLine("\nLegitimate Pakistani organizations (e.g., SBP, PTA, EasyPaisa, JazzCash) do not ask users to verify accounts through urgent SMS or non-standard web domains.")
                }
            }
            q.contains("do now") || q.contains("next") || q.contains("action") || q.contains("kya karoon") || q.contains("کیا کروں") -> {
                buildString {
                    appendLine("Recommended immediate safety steps:")
                    if (recommendations.isNotEmpty()) {
                        recommendations.forEachIndexed { i, rec ->
                            appendLine("${i + 1}. **${rec.action}**${if (rec.description.isNotBlank()) " — ${rec.description}" else ""}")
                        }
                    } else {
                        appendLine("1. Do not click any links or download attached APK files.")
                        appendLine("2. Never share SMS OTPs or 4-digit mobile wallet PINs.")
                    }
                    appendLine("\nIf you shared financial credentials or funds were stolen, immediately contact:")
                    appendLine("• **FIA Cyber Crime Helpline**: 1991 (Free 24/7 call)")
                    appendLine("• **PTA Unsolicited SMS Reporting**: 0800-55055")
                    appendLine("• Your bank or wallet customer care to freeze compromised accounts.")
                }
            }
            (q.contains("how") && (q.contains("work") || q.contains("scam"))) || q.contains("tactic") || q.contains("tariqa") -> {
                buildString {
                    appendLine("How scams in the **${record.category}** category typically operate:")
                    appendLine("1. **The Bait / Trigger**: Scammers contact you via SMS, WhatsApp, or spoofed web links creating artificial panic (e.g. \"Account blocked within 24h\") or excitement (e.g. \"Prize of PKR 25,000 approved\").")
                    appendLine("2. **The Deception**: They impersonate trusted brands (EasyPaisa, JazzCash, BISP, NADRA, PakPost) using similar names or unofficial numbers.")
                    appendLine("3. **The Trap**: They ask for an OTP, advance registration fee, or direct you to a fake phishing website to harvest your PIN.")
                    appendLine("4. **The Loss**: Once credentials or money are transferred, the fraudster disappears and blocks communication.")
                }
            }
            q.contains("urdu") || q.contains("اردو") -> {
                buildString {
                    appendLine("**خلاصہ برائے سکیورٹی (Urdu Summary):**")
                    appendLine("اس مواد کو **${record.category}** کے تحت **${record.riskLevel}** قرار دیا گیا ہے۔")
                    appendLine("• کبھی بھی اپنا OTP، بینک لاگ ان یا پن کوڈ کسی نامعلوم شخص کے ساتھ شیئر نہ کریں۔")
                    appendLine("• مشکوک پیغامات میں دیے گئے لنکس پر کلک کرنے سے گریز کریں۔")
                    appendLine("• کسی بھی دھوکہ دہی کی صورت میں فوری طور پر FIA سائبر کرائم ہیلپ لائن 1991 پر رابطہ کریں۔")
                }
            }
            q.contains("avoid") || q.contains("share") || q.contains("information") || q.contains("secret") -> {
                buildString {
                    appendLine("Crucial Information You Must **NEVER** Share:")
                    appendLine("1. **SMS OTP (One-Time Password)**: Grants complete access to authorize transactions.")
                    appendLine("2. **Mobile Banking / Wallet PIN**: Your 4-digit or 5-digit secret MPIN.")
                    appendLine("3. **ATM Card CVV / Expiry**: The 3-digit number on the back of your debit card.")
                    appendLine("4. **CNIC Copy / Mother's Maiden Name**: Used to reset authentication passwords.")
                    appendLine("\nOfficial Pakistani banks and government programs will **never** phone you asking for these codes.")
                }
            }
            else -> {
                buildString {
                    appendLine("Based on this scan assessment (${record.riskLevel.uppercase()} Risk, ${record.riskScore}/100):")
                    appendLine("Category: **${record.category}**")
                    appendLine("Summary: ${record.summary}")
                    appendLine("\nKey takeaway: Treat unsolicited requests for personal or financial information with caution. When in doubt, call the official helpline of the organization directly rather than using contact details provided in the message.")
                }
            }
        }
    }

    suspend fun translateExplanation(
        record: ScanRecord,
        targetLanguage: String,
        model: String = "gemini-3.5-flash"
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val indicators = deserializeIndicators(record.indicatorsJson)
        val recommendations = deserializeRecommendations(record.recommendationsJson)

        if (targetLanguage.equals("English", ignoreCase = true)) {
            val englishText = buildString {
                appendLine("Risk Assessment: ${record.riskLevel.uppercase()} (${record.riskScore}/100)")
                appendLine("Category: ${record.category}")
                appendLine("\nSummary: ${record.summary}")
                if (indicators.isNotEmpty()) {
                    appendLine("\nKey Detected Indicators:")
                    indicators.forEach { ind ->
                        appendLine("• ${ind.name}: ${ind.explanation} [Evidence: \"${ind.evidence}\"]")
                    }
                }
                if (recommendations.isNotEmpty()) {
                    appendLine("\nRecommended Actions:")
                    recommendations.forEach { rec ->
                        appendLine("✔ ${rec.action}${if (rec.description.isNotBlank()) " — ${rec.description}" else ""}")
                    }
                }
            }
            return@withContext Result.success(englishText)
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.success(generateLocalTranslationFallback(record, targetLanguage, indicators, recommendations))
        }

        try {
            val translationSystemInstruction = """
                You are a specialized cybersecurity translator for AI ScamShield PK in Pakistan.
                Translate and simplify this threat assessment into $targetLanguage.
                
                Rules:
                1. If target is 'Urdu (اردو)', output natural, fluent Urdu script (Nastaliq readable).
                2. If target is 'Roman Urdu', output natural, conversational Roman Urdu widely used in Pakistani SMS/WhatsApp (e.g. 'Aapka account biometric verify na honay ki wajah se block honay ka dar hai. Kisi ko OTP mat dein').
                3. Accurately preserve all numbers, URLs, dates, and risk scores (${record.riskScore}/100).
                4. Do NOT alter the severity (${record.riskLevel}) or invent findings.
                5. Keep tone clear, reassuring, and safety-focused.
            """.trimIndent()

            val contentToTranslate = """
                Risk Level: ${record.riskLevel} (${record.riskScore}/100)
                Category: ${record.category}
                Summary: ${record.summary}
                Indicators:
                ${indicators.joinToString("\n") { "- ${it.name}: ${it.explanation} (Snippet: ${it.evidence})" }}
                Recommended Safety Actions:
                ${recommendations.joinToString("\n") { "- ${it.action}: ${it.description}" }}
            """.trimIndent()

            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = "Please translate the following threat assessment into $targetLanguage:\n\n$contentToTranslate")))),
                generationConfig = GenerationConfig(
                    temperature = 0.2f,
                    responseMimeType = "text/plain"
                ),
                systemInstruction = Content(parts = listOf(Part(text = translationSystemInstruction)))
            )

            val response = RetrofitClient.geminiService.generateContent(
                model = model,
                apiKey = apiKey,
                request = request
            )

            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: generateLocalTranslationFallback(record, targetLanguage, indicators, recommendations)

            Result.success(text)
        } catch (e: Exception) {
            Log.e("ScamShieldRepo", "Translation API error: ${e.message}")
            Result.success(generateLocalTranslationFallback(record, targetLanguage, indicators, recommendations))
        }
    }

    private fun generateLocalTranslationFallback(
        record: ScanRecord,
        targetLanguage: String,
        indicators: List<RiskIndicator>,
        recommendations: List<Recommendation>
    ): String {
        return if (targetLanguage.contains("Urdu", ignoreCase = true) && !targetLanguage.contains("Roman", ignoreCase = true)) {
            // Urdu Script
            val urduRisk = when (record.riskLevel.uppercase()) {
                "CRITICAL" -> "انتہائی سنگین (Critical)"
                "HIGH" -> "زیادہ خطرہ (High)"
                "MODERATE" -> "درمیانہ خطرہ (Moderate)"
                else -> "محفوظ / کم خطرہ (Low)"
            }
            buildString {
                appendLine("🛡️ خطرے کی تفصیلی جانچ (سکیورٹی رپورٹ)")
                appendLine("• خطرے کا لیول: $urduRisk")
                appendLine("• سکور: ${record.riskScore} از 100")
                appendLine("• فراڈ کا زمرہ: ${record.category}")
                appendLine("\nخلاصہ:")
                appendLine(record.summary)
                if (indicators.isNotEmpty()) {
                    appendLine("\nشناخت شدہ مشکوک علامات:")
                    indicators.forEach { ind ->
                        appendLine("• ${ind.name}: \"${ind.evidence}\" — ${ind.explanation}")
                    }
                }
                if (recommendations.isNotEmpty()) {
                    appendLine("\nحفاظتی اقدامات:")
                    recommendations.forEach { rec ->
                        appendLine("✔ ${rec.action} (${rec.description})")
                    }
                }
                appendLine("\nایمرجنسی مدد:")
                appendLine("ایف آئی اے (FIA) سائبر کرائم ہیلپ لائن: 1991")
                appendLine("پی ٹی اے (PTA) ہیلپ لائن: 0800-55055")
            }
        } else {
            // Roman Urdu
            buildString {
                appendLine("🛡️ Scan Ki Tafseelat (Roman Urdu)")
                appendLine("• Khatray Ka Level: ${record.riskLevel.uppercase()} (${record.riskScore}/100)")
                appendLine("• Fraud Category: ${record.category}")
                appendLine("\nKhulasa:")
                appendLine(record.summary)
                if (indicators.isNotEmpty()) {
                    appendLine("\nMashkook Nishaniyan (Indicators):")
                    indicators.forEach { ind ->
                        appendLine("• ${ind.name}: \"${ind.evidence}\" — ${ind.explanation}")
                    }
                }
                if (recommendations.isNotEmpty()) {
                    appendLine("\nAapko Kya Karna Chahiye:")
                    recommendations.forEach { rec ->
                        appendLine("✔ ${rec.action} (${rec.description})")
                    }
                }
                appendLine("\nMadad Ke Liye:")
                appendLine("Agar aapka nuqsaan hua hai toh foran FIA Cyber Crime helpline 1991 par call karein.")
            }
        }
    }

    fun deserializeIndicators(json: String): List<RiskIndicator> {
        return try {
            indicatorAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun deserializeRecommendations(json: String): List<Recommendation> {
        return try {
            recommendationAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun deserializeUrlFeatures(json: String?): UrlFeatures? {
        if (json.isNullOrEmpty()) return null
        return try {
            urlFeaturesAdapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }
}
