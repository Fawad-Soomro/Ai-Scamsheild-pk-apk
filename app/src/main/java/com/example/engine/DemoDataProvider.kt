package com.example.engine

data class DemoScenario(
    val id: String,
    val title: String,
    val category: String,
    val language: String,
    val inputType: String, // "MESSAGE", "URL", "SCREENSHOT"
    val sampleContent: String,
    val description: String,
    val expectedRisk: String // "Critical", "High", "Moderate", "Low"
)

object DemoDataProvider {

    val scenarios = listOf(
        DemoScenario(
            id = "demo_1_roman_urdu_banking",
            title = "Easypaisa Account Block Threat (Roman Urdu)",
            category = "Banking / OTP Account Takeover",
            language = "Roman Urdu",
            inputType = "MESSAGE",
            sampleContent = "Muaziz Sarif! Apka Easypaisa account biometric tasdeeq na honay par 2 ghantay mein band ho jayega. Account bachanay k liye foran apna OTP 8842 par share karein ya is link par login karein: http://easypaisa-secure-verify.top/auth",
            description = "Classic Pakistani banking urgency lure threatening instant account termination to harvest 4-digit biometric OTP and credentials.",
            expectedRisk = "Critical"
        ),
        DemoScenario(
            id = "demo_2_urdu_bisp_lottery",
            title = "BISP / Ehsaas 25,000 Cash Grant Scam (Urdu)",
            category = "Prize / Lottery Grant Scam",
            language = "Urdu (اردو)",
            inputType = "MESSAGE",
            sampleContent = "مبارک ہو! بے نظیر انکم سپورٹ پروگرام کی طرف سے آپ کا 25,000 روپے کا امدادی کیش منظور ہو گیا ہے۔ ابھی رقم وصول کرنے کے لیے 0304-9876543 پر رابطہ کریں یا رجسٹریشن فیس 500 روپے نیچے دیے گئے نمبر پر ایزی پیسہ کریں۔",
            description = "Exploits vulnerable citizens using government welfare program branding (BISP) requiring an upfront fake registration transfer fee.",
            expectedRisk = "High"
        ),
        DemoScenario(
            id = "demo_3_english_job_scam",
            title = "Daily YouTube Video Liking Job (English)",
            category = "Fake Task / Employment Scam",
            language = "English",
            inputType = "MESSAGE",
            sampleContent = "Exclusive Remote Job Offer! Earn Rs. 6,000 to Rs. 15,000 daily from home by simply watching and liking YouTube videos and rating Google Maps places. No experience required. To activate your employee portal, deposit Rs. 1,200 VIP security deposit via JazzCash to 0300-1234567. WhatsApp HR: +923129876543.",
            description = "High daily payout promised for trivial micro-tasks with an advance refundable security deposit requested via mobile wallet.",
            expectedRisk = "High"
        ),
        DemoScenario(
            id = "demo_4_roman_urdu_nanoloan",
            title = "Instant Easy Loan Harassment (Roman Urdu)",
            category = "Predatory Loan Extortion",
            language = "Roman Urdu",
            inputType = "MESSAGE",
            sampleContent = "Aapka 50,000 ka EasyLoan approve ho chuka hai bina kisi zimanat k sirf 10 minute mein. Hamari app foran download karein aur apni contacts aur gallery permission allow karein: http://fast-pkr-loan.xyz/app.apk",
            description = "Predatory nanoloan app requiring intrusive contacts & gallery permissions for extortion and unauthorized data harvesting.",
            expectedRisk = "High"
        ),
        DemoScenario(
            id = "demo_5_delivery_tcs_scam",
            title = "Pakistan Post / TCS Parcel Fee Scam (English)",
            category = "Delivery / Package Scam",
            language = "English",
            inputType = "MESSAGE",
            sampleContent = "Pakistan Post Alert: Your international parcel PK-994827 could not be dispatched due to an incomplete street address. Please update your delivery location and pay Rs. 185 redelivery custom fee within 12 hours: https://pakpost-redelivery-fees.work/track",
            description = "Low-amount fee lure designed to harvest debit/credit card CVV details on a typosquatted government courier landing page.",
            expectedRisk = "High"
        ),
        DemoScenario(
            id = "demo_6_pta_device_block",
            title = "PTA DIRBS Mobile Block Warning (Roman Urdu)",
            category = "Government Impersonation",
            language = "Roman Urdu",
            inputType = "MESSAGE",
            sampleContent = "PTA Final Notice: Apka mobile IMEI gher-tasdeeq shuda paya gaya hai. Aglay 24 ghantay mein SIM block kardi jaye gi. IMEI register karnay aur fine se bachnay k liye foran link open karein: http://pta-dirbs-tax-portal.xyz/pay",
            description = "Impersonates Pakistan Telecommunication Authority threatening device blocking unless tax is paid through an unofficial portal.",
            expectedRisk = "High"
        ),
        DemoScenario(
            id = "demo_7_typosquatted_url",
            title = "Spoofed Banking Portal URL (URL Scan)",
            category = "Phishing URL / Domain Spoofing",
            language = "English",
            inputType = "URL",
            sampleContent = "http://jazzcash-bonus-reward-2026.xyz/login?user_id=923001234567&claim=pkr10000",
            description = "Features insecure HTTP, high-risk .xyz TLD, unauthorized brand name typosquatting, and token harvesting parameters.",
            expectedRisk = "Critical"
        ),
        DemoScenario(
            id = "demo_8_legitimate_bank_alert",
            title = "Legitimate Bank ATM Withdrawal Alert (English)",
            category = "Legitimate Transaction Alert",
            language = "English",
            inputType = "MESSAGE",
            sampleContent = "Dear Customer, PKR 10,000.00 has been debited from your Account **3421 at Meezan Bank ATM F-10 Islamabad on 14-Aug-2026 11:24 AM. Available Balance is PKR 45,210.50. If this was not done by you, please call 111-331-331 immediately.",
            description = "Harmless authentic transactional notification from a registered bank with masked account digits and standard official helpline number.",
            expectedRisk = "Low"
        )
    )
}
