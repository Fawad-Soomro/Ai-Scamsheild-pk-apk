package com.example.ui.navigation

sealed class Screen(val route: String, val title: String) {
    data object Home : Screen("home", "Home")
    data object Scanner : Screen("scanner", "Scanner")
    data object Result : Screen("result/{recordId}", "Scan Result") {
        fun createRoute(recordId: Long) = "result/$recordId"
    }
    data object History : Screen("history", "History")
    data object Insights : Screen("insights", "Threat Insights")
    data object DemoMode : Screen("demo_mode", "Scam Trainer")
    data object Settings : Screen("settings", "Settings & About")
    data object SafetyGuide : Screen("safety_guide", "Safety Guide")
    data object Faq : Screen("faq", "Security FAQs & Help")
    data object VivaQnA : Screen("faq", "Security FAQs")
}
