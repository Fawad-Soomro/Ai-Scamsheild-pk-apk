package com.example.engine

import android.net.Uri
import com.example.data.model.UrlFeatures
import java.net.URI
import java.util.Locale

object UrlRiskAnalyzer {

    private val SUSPICIOUS_TLDS = setOf(
        "top", "xyz", "work", "click", "link", "zip", "gq", "cf", "tk", "ml", "cc", "buzz", "rest", "fit", "surf", "casa"
    )

    private val URL_SHORTENERS = setOf(
        "bit.ly", "tinyurl.com", "t.co", "is.gd", "ow.ly", "cutt.ly", "rb.gy", "shorturl.at", "tiny.cc", "v.gd"
    )

    private val PAKISTANI_TARGET_BRANDS = listOf(
        "easypaisa", "easy-paisa", "esypaisa",
        "jazzcash", "jazz-cash", "jazcash", "mobilink",
        "bisp", "ehsaas", "benazir", "jeeto-pakistan",
        "hbl", "ubl", "mcb", "meezan", "alfalah", "askari", "alliedbank", "nbp", "faysal",
        "nayapay", "sadapay", "1link", "nadra", "pta"
    )

    private val SUSPICIOUS_KEYWORDS = listOf(
        "login", "verify", "secure", "account", "unblock", "reward", "claim", "prize",
        "lottery", "free-recharge", "bonus", "cash", "update-kyc", "otp", "confirm",
        "authenticate", "wallet", "banking"
    )

    fun analyzeUrl(inputUrl: String): UrlFeatures {
        var cleanUrl = inputUrl.trim()
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "https://$cleanUrl"
        }

        val flags = mutableListOf<String>()
        var heuristicScore = 0

        var domain = ""
        var scheme = "https"
        var path = ""
        var queryParamsCount = 0
        var usesHttps = true

        try {
            val uri = URI(cleanUrl)
            scheme = uri.scheme ?: "https"
            usesHttps = scheme.equals("https", ignoreCase = true)
            domain = uri.host?.lowercase(Locale.ROOT) ?: ""
            path = uri.path ?: ""
            val query = uri.query
            if (!query.isNullOrEmpty()) {
                queryParamsCount = query.split("&").size
            }
        } catch (e: Exception) {
            // Fallback parsing with Android Uri
            val androidUri = Uri.parse(cleanUrl)
            scheme = androidUri.scheme ?: "https"
            usesHttps = scheme.equals("https", ignoreCase = true)
            domain = androidUri.host?.lowercase(Locale.ROOT) ?: cleanUrl.substringBefore("/").substringAfter("://")
            path = androidUri.path ?: ""
            queryParamsCount = androidUri.queryParameterNames.size
        }

        val urlLength = cleanUrl.length
        val domainLength = domain.length
        val pathLength = path.length

        // Check 1: Insecure HTTP
        if (!usesHttps) {
            flags.add("Insecure HTTP protocol (No TLS/SSL encryption)")
            heuristicScore += 18
        }

        // Check 2: IP Address instead of Domain
        val ipRegex = Regex("^(\\d{1,3}\\.){3}\\d{1,3}$")
        val containsIp = ipRegex.matches(domain)
        if (containsIp) {
            flags.add("Raw IP address used instead of legitimate domain name")
            heuristicScore += 35
        }

        // Check 3: Punycode / Internationalized Domain Names (IDN Homograph)
        val containsPunycode = domain.contains("xn--")
        if (containsPunycode) {
            flags.add("Punycode (xn--) detected - potential homograph/spoofing attack")
            heuristicScore += 30
        }

        // Check 4: URL Shorteners
        val containsShortener = URL_SHORTENERS.any { domain.contains(it) }
        if (containsShortener) {
            flags.add("URL shortener used - hides destination target")
            heuristicScore += 20
        }

        // Check 5: Suspicious TLD
        val tld = domain.substringAfterLast(".", "")
        val hasSuspiciousTld = SUSPICIOUS_TLDS.contains(tld)
        if (hasSuspiciousTld) {
            flags.add("High-risk Top-Level Domain (.$tld) commonly abused for phishing")
            heuristicScore += 25
        }

        // Check 6: Subdomain count & nesting
        val subdomains = domain.split(".")
        val subdomainCount = if (subdomains.size > 2) subdomains.size - 2 else 0
        if (subdomainCount >= 3) {
            flags.add("Excessive subdomain depth ($subdomainCount subdomains) indicating domain masquerading")
            heuristicScore += 22
        }

        // Check 7: Pakistani Brand Spoofing / Typosquatting
        var containsPakBrand = false
        for (brand in PAKISTANI_TARGET_BRANDS) {
            if (cleanUrl.lowercase(Locale.ROOT).contains(brand)) {
                // If it contains the brand name, but domain is NOT the genuine official domain
                val isOfficialDomain = when (brand) {
                    "easypaisa", "easy-paisa" -> domain.endsWith("easypaisa.com.pk") || domain == "easypaisa.com.pk"
                    "jazzcash", "jazz-cash" -> domain.endsWith("jazzcash.com.pk") || domain == "jazzcash.com.pk"
                    "hbl" -> domain.endsWith("hbl.com") || domain == "hbl.com"
                    "meezan" -> domain.endsWith("meezanbank.com") || domain == "meezanbank.com"
                    "ubl" -> domain.endsWith("ubldigital.com") || domain == "ubldigital.com"
                    "nayapay" -> domain.endsWith("nayapay.com") || domain == "nayapay.com"
                    "sadapay" -> domain.endsWith("sadapay.pk") || domain == "sadapay.pk"
                    "bisp" -> domain.endsWith("bisp.gov.pk") || domain == "bisp.gov.pk"
                    "pta" -> domain.endsWith("pta.gov.pk") || domain == "pta.gov.pk"
                    "nadra" -> domain.endsWith("nadra.gov.pk") || domain == "nadra.gov.pk"
                    else -> false
                }

                if (!isOfficialDomain) {
                    containsPakBrand = true
                    flags.add("Pakistani financial/governmental service name ($brand) detected in unauthorized non-official domain")
                    heuristicScore += 38
                    break
                }
            }
        }

        // Check 8: Suspicious keywords in domain or path
        var hasKeywords = false
        for (kw in SUSPICIOUS_KEYWORDS) {
            if (cleanUrl.lowercase(Locale.ROOT).contains(kw)) {
                hasKeywords = true
                flags.add("High-risk credential/lure keyword '$kw' in URL structure")
                heuristicScore += 15
                break
            }
        }

        // Check 9: Excessive URL length
        if (urlLength > 90) {
            flags.add("Unusually long URL length ($urlLength chars) - typical of obfuscated phishing tokens")
            heuristicScore += 12
        }

        // Check 10: Query parameters
        if (queryParamsCount >= 4) {
            flags.add("Multiple dynamic tracking / redirect query parameters ($queryParamsCount parameters)")
            heuristicScore += 10
        }

        // Normalization
        val finalScore = heuristicScore.coerceIn(0, 100)

        return UrlFeatures(
            originalUrl = cleanUrl,
            scheme = scheme,
            domain = domain,
            domainLength = domainLength,
            urlLength = urlLength,
            pathLength = pathLength,
            subdomainCount = subdomainCount,
            queryParamCount = queryParamsCount,
            usesHttps = usesHttps,
            containsIpAddress = containsIp,
            containsPunycode = containsPunycode,
            containsShortener = containsShortener,
            containsPakistaniBrandKeywords = containsPakBrand,
            suspiciousTld = hasSuspiciousTld,
            flags = flags,
            heuristicRiskScore = finalScore
        )
    }
}
