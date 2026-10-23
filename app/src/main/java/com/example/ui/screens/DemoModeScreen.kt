package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.DemoDataProvider
import com.example.engine.DemoScenario
import com.example.ui.components.AnalysisLoadingOverlay
import com.example.ui.components.DisclaimerBanner
import com.example.ui.components.RiskBadge
import com.example.ui.components.getRiskColor
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmeraldPrimary
import com.example.ui.theme.RiskCriticalRed
import com.example.ui.theme.RiskHighOrange
import com.example.ui.theme.RiskLowGreen
import com.example.ui.theme.RiskModerateAmber
import com.example.ui.viewmodel.ScamShieldViewModel

@Composable
fun DemoModeScreen(
    viewModel: ScamShieldViewModel,
    onNavigateToResult: (recordId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()

    LaunchedEffect(scanState.activeRecordId) {
        scanState.activeRecordId?.let { id ->
            onNavigateToResult(id)
            viewModel.clearActiveResult()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = CyberEmeraldPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("demo_mode_tabs")
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Scam Trainer", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_scenarios")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Security Architecture", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_guide")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> InteractiveTrainerTab(
                    scenarios = DemoDataProvider.scenarios,
                    onRunScenarioInScanner = { scenario ->
                        viewModel.runDemoScenario(scenario)
                    }
                )
                1 -> SecurityArchitectureTab()
            }
        }

        if (scanState.isScanning) {
            AnalysisLoadingOverlay(stepText = scanState.analysisStep)
        }
    }
}

@Composable
private fun InteractiveTrainerTab(
    scenarios: List<DemoScenario>,
    onRunScenarioInScanner: (DemoScenario) -> Unit
) {
    var currentScenarioIndex by remember { mutableIntStateOf(0) }
    var selectedUserDecision by remember { mutableStateOf<String?>(null) } // "Safe", "Suspicious", "Dangerous"
    var scoreCorrect by remember { mutableIntStateOf(0) }
    var totalAttempted by remember { mutableIntStateOf(0) }
    val answeredScenarios = remember { mutableMapOf<String, String>() }

    val currentScenario = scenarios[currentScenarioIndex]

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Trainer Header & Score Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("trainer_header_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Interactive Scam Awareness Trainer",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Can you spot Pakistani digital fraud before the AI evaluates it?",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberCyan
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CyberEmeraldPrimary.copy(alpha = 0.15f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$scoreCorrect / $totalAttempted",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = CyberEmeraldPrimary
                                )
                            )
                            Text(
                                text = "Score",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Active Scenario Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("scenario_trainer_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Scenario ${currentScenarioIndex + 1} of ${scenarios.size}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan
                            )
                        )

                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = currentScenario.language,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentScenario.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Simulated message envelope
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CyberCyan))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Simulated Incoming ${currentScenario.inputType}:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CyberCyan
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentScenario.sampleContent,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Interactive Decision Buttons (If not yet decided for this card)
                    if (selectedUserDecision == null) {
                        Text(
                            text = "What is your assessment of this communication?",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DecisionButton(
                                label = "Safe / OK",
                                color = RiskLowGreen,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedUserDecision = "Safe"
                                    totalAttempted++
                                    if (currentScenario.expectedRisk.equals("Low", true)) scoreCorrect++
                                    answeredScenarios[currentScenario.id] = "Safe"
                                }
                            )
                            DecisionButton(
                                label = "Suspicious",
                                color = RiskModerateAmber,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedUserDecision = "Suspicious"
                                    totalAttempted++
                                    if (currentScenario.expectedRisk.equals("Moderate", true)) scoreCorrect++
                                    answeredScenarios[currentScenario.id] = "Suspicious"
                                }
                            )
                            DecisionButton(
                                label = "Dangerous",
                                color = RiskCriticalRed,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedUserDecision = "Dangerous"
                                    totalAttempted++
                                    if (currentScenario.expectedRisk.equals("Critical", true) || currentScenario.expectedRisk.equals("High", true)) scoreCorrect++
                                    answeredScenarios[currentScenario.id] = "Dangerous"
                                }
                            )
                        }
                    } else {
                        // Feedback & Explanation Card
                        val isActualSafe = currentScenario.expectedRisk.equals("Low", true)
                        val isUserCorrect = when (selectedUserDecision) {
                            "Safe" -> isActualSafe
                            "Suspicious" -> currentScenario.expectedRisk.equals("Moderate", true)
                            "Dangerous" -> currentScenario.expectedRisk.equals("High", true) || currentScenario.expectedRisk.equals("Critical", true)
                            else -> false
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isUserCorrect) CyberEmeraldPrimary.copy(alpha = 0.15f) else RiskCriticalRed.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isUserCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                                        contentDescription = null,
                                        tint = if (isUserCorrect) CyberEmeraldPrimary else RiskCriticalRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isUserCorrect) "Correct Assessment!" else "Warning: This is actually a ${currentScenario.expectedRisk.uppercase()} threat!",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUserCorrect) CyberEmeraldPrimary else RiskCriticalRed
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = currentScenario.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            selectedUserDecision = null
                                            currentScenarioIndex = (currentScenarioIndex + 1) % scenarios.size
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("next_scenario_btn")
                                    ) {
                                        Text("Next Scenario")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }

                                    Button(
                                        onClick = { onRunScenarioInScanner(currentScenario) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberEmeraldPrimary),
                                        modifier = Modifier.testTag("run_scenario_ai_btn")
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Run AI Deep Scan", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Scenario Selector Carousel / Chips
        item {
            Text(
                text = "Select Practice Scenario:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                scenarios.forEachIndexed { index, scenario ->
                    val isSelected = index == currentScenarioIndex
                    val answered = answeredScenarios[scenario.id]

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) CyberEmeraldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) CyberEmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                currentScenarioIndex = index
                                selectedUserDecision = answered
                            }
                            .testTag("scenario_item_$index")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = scenario.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${scenario.category} • ${scenario.language}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (answered != null) {
                                Icon(Icons.Default.Check, contentDescription = "Answered", tint = CyberEmeraldPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        item {
            DisclaimerBanner()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DecisionButton(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.85f), contentColor = Color.White),
        modifier = modifier.height(42.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun SecurityArchitectureTab() {
    val architectureComponents = listOf(
        "Multilingual Urdu & Roman Urdu Reasoning" to "Integrates localized domain lexicons, common slang, Pakistani telecom prefixes, and official government schemes (BISP, Ehsaas, 8171, FIA, PTA) to recognize threats across all local dialects.",
        "Multimodal Vision & OCR" to "Transfers mobile inbox screenshots, banking notices, and banner graphics into Gemini's multimodal vision model for direct optical character recognition and contextual fraud extraction.",
        "Explainable Evidence Citations" to "Extracts exact direct quotes and specifies risk indicators (e.g. OTP theft, artificial urgency, advance fee demand) rather than an opaque score.",
        "Passive Static URL Extraction" to "Analyzes TLS encryption, IP addresses, Punycode, subdomain nesting, and typosquatting of Pakistani financial entities without web crawling.",
        "Strict On-Device Privacy" to "Scan records and telemetry are stored locally in encrypted Room SQLite on-device. Zero personal data is transmitted to third-party databases."
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ScamShield Security Engine Architecture",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Engineered by Team Trident AI for Pakistan's digital fraud defense:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(architectureComponents) { (title, description) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = CyberEmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            DisclaimerBanner()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
