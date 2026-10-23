package com.example.engine

import java.util.regex.Pattern

data class ExtractedEntities(
    val urls: List<String> = emptyList(),
    val phoneNumbers: List<String> = emptyList(),
    val amountsPkr: List<String> = emptyList(),
    val detectedLanguage: String = "English",
    val hasUrgencySignals: Boolean = false,
    val hasOtpKeywords: Boolean = false,
    val hasFinancialKeywords: Boolean = false
)

object TextPreprocessor {

    private val URL_PATTERN = Pattern.compile(
        "https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]",
        Pattern.CASE_INSENSITIVE
    )

    private val PAK_PHONE_PATTERN = Pattern.compile(
        "(?:\\+92|0092|0)?3[0-9]{2}[-\\s]?[0-9]{7}",
        Pattern.CASE_INSENSITIVE
    )

    private val AMOUNT_PATTERN = Pattern.compile(
        "(?:Rs\\.?|PKR|rupees?|rupay|roopay|lakh|crore|hazaar|hazaro)\\s*\\d+(?:,\\d+)*(?:\\.\\d+)?|\\d+(?:,\\d+)*(?:\\.\\d+)?\\s*(?:Rs\\.?|PKR|lakh|crore|hazaar|rupay)",
        Pattern.CASE_INSENSITIVE
    )

    private val URDU_ARABIC_REGEX = Regex("[\\u0600-\\u06FF\\u0750-\\u077F\\uFB50-\\uFDFF\\uFE70-\\uFEFF]")

    private val ROMAN_URDU_KEYWORDS = listOf(
        "aap", "apka", "apki", "mubarak", "karein", "karen", "bhejein", "foran",
        "warna", "band", "jeetay", "inaam", "raasta", "paise", "rupay", "hoga",
        "hogi", "khatam", "bisp", "ehsaas", "khata", "jazzcash", "easypaisa", "taraf",
        "rabta", "tasdeeq", "par", "chuka", "honay"
    )

    private val URGENCY_KEYWORDS = listOf(
        "urgent", "immediately", "foran", "24 hours", "24 ghante", "warna",
        "band ho jayega", "band", "suspended", "blocked", "block", "expire",
        "action required", "last chance", "jald", "jaldi"
    )

    private val OTP_KEYWORDS = listOf(
        "otp", "pin", "code", "password", "verification", "tasdeeq", "cvv", "passcode"
    )

    private val FINANCIAL_KEYWORDS = listOf(
        "won", "prize", "lottery", "cash", "rs", "pkr", "lakh", "crore", "reward",
        "bonus", "loan", "qarz", "transfer", "deposit", "fee", "free balance", "inaam",
        "8171", "bisp", "ehsaas", "account", "bank"
    )

    fun preprocess(text: String): ExtractedEntities {
        val trimmed = text.trim()
        val lower = trimmed.lowercase()

        // Extract URLs
        val urls = mutableListOf<String>()
        val urlMatcher = URL_PATTERN.matcher(trimmed)
        while (urlMatcher.find()) {
            urls.add(urlMatcher.group())
        }

        // Extract Phone Numbers
        val phoneNumbers = mutableListOf<String>()
        val phoneMatcher = PAK_PHONE_PATTERN.matcher(trimmed)
        while (phoneMatcher.find()) {
            phoneNumbers.add(phoneMatcher.group())
        }

        // Extract PKR Amounts
        val amounts = mutableListOf<String>()
        val amountMatcher = AMOUNT_PATTERN.matcher(trimmed)
        while (amountMatcher.find()) {
            amounts.add(amountMatcher.group())
        }

        // Detect Language
        val detectedLanguage = when {
            URDU_ARABIC_REGEX.containsMatchIn(trimmed) -> "Urdu (اردو)"
            ROMAN_URDU_KEYWORDS.any { lower.contains(it) } -> "Roman Urdu"
            else -> "English"
        }

        val hasUrgency = URGENCY_KEYWORDS.any { lower.contains(it) }
        val hasOtp = OTP_KEYWORDS.any { lower.contains(it) }
        val hasFinancial = FINANCIAL_KEYWORDS.any { lower.contains(it) }

        return ExtractedEntities(
            urls = urls,
            phoneNumbers = phoneNumbers,
            amountsPkr = amounts,
            detectedLanguage = detectedLanguage,
            hasUrgencySignals = hasUrgency,
            hasOtpKeywords = hasOtp,
            hasFinancialKeywords = hasFinancial
        )
    }
}
