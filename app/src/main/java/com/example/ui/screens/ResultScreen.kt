package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Recommendation
import com.example.data.model.RiskIndicator
import com.example.data.model.ScanRecord
import com.example.ui.components.CyberCard
import com.example.ui.components.DisclaimerBanner
import com.example.ui.components.IndicatorCard
import com.example.ui.components.RecommendationCard
import com.example.ui.components.RiskScoreDial
import com.example.ui.components.getRiskColor
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmeraldPrimary
import com.example.ui.theme.RiskCriticalRed
import com.example.ui.theme.RiskHighOrange
import com.example.ui.theme.RiskModerateAmber
import com.example.ui.viewmodel.AssistantMessage
import com.example.ui.viewmodel.ScamShieldViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ResultScreen(
    recordId: Long,
    viewModel: ScamShieldViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToNewScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isTtsSpeaking by viewModel.isTtsSpeaking.collectAsStateWithLifecycle()

    var showExportDialog by remember { mutableStateOf(false) }
    var isExportingPdf by remember { mutableStateOf(false) }
    var includeRawContentInReport by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopSpeech()
        }
    }

    LaunchedEffect(recordId) {
        viewModel.selectRecordForDetail(recordId)
    }

    val record by viewModel.selectedDetailRecord.collectAsStateWithLifecycle()

    if (record == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading scan assessment...")
        }
        return
    }

    val currentRecord = record!!
    val indicators = remember(currentRecord) { viewModel.getIndicatorsForRecord(currentRecord) }
    val recommendations = remember(currentRecord) { viewModel.getRecommendationsForRecord(currentRecord) }
    val urlFeatures = remember(currentRecord) { viewModel.getUrlFeaturesForRecord(currentRecord) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }
    val dateString = remember(currentRecord.timestampMs) { dateFormat.format(Date(currentRecord.timestampMs)) }

    var isTechnicalExpanded by remember { mutableStateOf(false) }

    // Multilingual Explanation States
    val currentExplanationLang by viewModel.currentExplanationLanguage.collectAsStateWithLifecycle()
    val translatedExplanations by viewModel.translatedExplanations.collectAsStateWithLifecycle()
    val isTranslating by viewModel.isTranslating.collectAsStateWithLifecycle()

    // Assistant States
    val assistantMessages by viewModel.assistantChatMessages.collectAsStateWithLifecycle()
    val isAssistantLoading by viewModel.isAssistantLoading.collectAsStateWithLifecycle()
    var assistantInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top App Bar Navigation & Export Action
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            viewModel.stopSpeech()
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "Scan Assessment",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row {
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.testTag("export_report_btn")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Export Report", tint = CyberEmeraldPrimary)
                    }

                    IconButton(
                        onClick = {
                            shareTextReport(context, currentRecord, indicators, recommendations)
                        },
                        modifier = Modifier.testTag("share_report_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share Report", tint = CyberCyan)
                    }
                }
            }
        }

        // Score Dial Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("result_score_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RiskScoreDial(
                        score = currentRecord.riskScore,
                        level = currentRecord.riskLevel
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = currentRecord.category,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "Type: ${currentRecord.inputType}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "Lang: ${currentRecord.language}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "Conf: ${(currentRecord.confidence * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$dateString • Developed by Team Trident AI",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Voice Briefing Player (English Default)
        item {
            TtsVoiceBriefingCard(
                isSpeaking = isTtsSpeaking,
                onPlay = {
                    val speechText = buildString {
                        append("ScamShield Safety Assessment. ")
                        append("Risk level is ${currentRecord.riskLevel}, with a risk score of ${currentRecord.riskScore} out of 100 for ${currentRecord.category}. ")
                        append("Assessment Summary: ${currentRecord.summary}. ")
                        if (recommendations.isNotEmpty()) {
                            append("Recommended action: ${recommendations.first().action}. ${recommendations.first().description}. ")
                        }
                        if (currentRecord.riskLevel.equals("CRITICAL", true) || currentRecord.riskLevel.equals("HIGH", true)) {
                            append("Warning: If you suspect unauthorized access or shared OTPs, call the FIA Cyber Crime helpline at 1991 immediately.")
                        }
                    }
                    viewModel.speakText(speechText)
                },
                onStop = {
                    viewModel.stopSpeech()
                }
            )
        }

        // FEATURE 2: Multilingual Explanations (English, Urdu Script, Roman Urdu)
        item {
            MultilingualExplanationCard(
                currentRecord = currentRecord,
                currentLanguage = currentExplanationLang,
                translatedExplanations = translatedExplanations,
                isTranslating = isTranslating,
                onSelectLanguage = { lang ->
                    viewModel.requestTranslation(currentRecord, lang)
                }
            )
        }

        // FEATURE 5: Screenshot OCR & Suspicious Text Highlighting
        if (!currentRecord.extractedText.isNullOrBlank() || currentRecord.inputType == "SCREENSHOT") {
            item {
                ScreenshotTextHighlightCard(
                    extractedText = currentRecord.extractedText ?: currentRecord.rawInput,
                    indicators = indicators
                )
            }
        }

        // URL Static Features Breakdown
        if (urlFeatures != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("url_features_breakdown_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Static URL Feature Extraction Matrix:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = RiskHighOrange
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "• Domain: ${urlFeatures.domain} (${urlFeatures.domainLength} chars)", style = MaterialTheme.typography.bodySmall)
                        Text(text = "• TLS/HTTPS: ${if (urlFeatures.usesHttps) "Yes (Secure)" else "No (Insecure HTTP)"}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "• Raw IP Address: ${urlFeatures.containsIpAddress}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "• Punycode Spoof: ${urlFeatures.containsPunycode}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "• Subdomain Count: ${urlFeatures.subdomainCount}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "• Pakistani Brand Detected: ${urlFeatures.containsPakistaniBrandKeywords}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "• High-Risk TLD: ${urlFeatures.suspiciousTld}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "• Heuristic Local Score: ${urlFeatures.heuristicRiskScore} / 100", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // FEATURE 1: Contextual AI Scam Assistant
        item {
            AiScamAssistantSection(
                record = currentRecord,
                messages = assistantMessages,
                isLoading = isAssistantLoading,
                inputValue = assistantInput,
                onInputChange = { assistantInput = it },
                onSendQuestion = { q ->
                    viewModel.askAssistant(currentRecord, q)
                    assistantInput = ""
                }
            )
        }

        // Explainable Indicators Section
        item {
            Text(
                text = "Detected Threat Indicators (${indicators.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (indicators.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "No prominent scam indicators detected in this content.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(indicators) { indicator ->
                IndicatorCard(indicator = indicator)
            }
        }

        // Actionable Safety Recommendations
        item {
            Text(
                text = "Recommended Safety Actions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(recommendations) { recommendation ->
            RecommendationCard(recommendation = recommendation)
        }

        // Expandable Technical Telemetry Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isTechnicalExpanded = !isTechnicalExpanded }
                    .testTag("technical_details_toggle"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Technical Security & Model Telemetry",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = if (isTechnicalExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = isTechnicalExpanded) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Text(text = "• AI Model: ${currentRecord.modelName}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "• Inference Latency: ${currentRecord.executionTimeMs} ms", style = MaterialTheme.typography.bodySmall)
                            Text(text = "• Input Mode: ${currentRecord.inputType}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "• Storage: Local SQLite / Room DB", style = MaterialTheme.typography.bodySmall)
                            Text(text = "• Attribution: Developed by Team Trident AI", style = MaterialTheme.typography.bodySmall)

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Structured Findings Summary:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(currentRecord.rawInput))
                                        Toast.makeText(context, "Input copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = buildString {
                                        appendLine("{")
                                        appendLine("  \"risk_score\": ${currentRecord.riskScore},")
                                        appendLine("  \"risk_level\": \"${currentRecord.riskLevel}\",")
                                        appendLine("  \"category\": \"${currentRecord.category}\",")
                                        appendLine("  \"language\": \"${currentRecord.language}\",")
                                        appendLine("  \"confidence\": ${currentRecord.confidence}")
                                        appendLine("}")
                                    },
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    modifier = Modifier.padding(10.dp),
                                    color = CyberEmeraldPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.stopSpeech()
                        onNavigateToNewScan()
                    },
                    modifier = Modifier.weight(1f).height(48.dp).testTag("new_scan_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Scan")
                }

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = android.net.Uri.parse("tel:1991")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f).height(48.dp).testTag("report_fia_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RiskCriticalRed,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call FIA (1991)")
                }
            }
        }

        item {
            DisclaimerBanner()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // FEATURE 6: Export Report Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = CyberEmeraldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Threat Report", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Generate a verified, shareable digital threat assessment report with forensic findings, indicators, and recommended remedial steps.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Privacy by default option
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = includeRawContentInReport,
                                onCheckedChange = { includeRawContentInReport = it },
                                colors = CheckboxDefaults.colors(checkedColor = CyberEmeraldPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Include original submitted text",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "Keep unchecked to mask potentially sensitive details",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (isExportingPdf) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = CyberEmeraldPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Generating PDF document...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isExportingPdf = true
                        viewModel.exportPdfReport(
                            context = context,
                            record = currentRecord,
                            includeRawContent = includeRawContentInReport
                        ) { result ->
                            isExportingPdf = false
                            showExportDialog = false
                            if (result.isSuccess) {
                                val pdfFile = result.getOrThrow()
                                sharePdfFile(context, pdfFile)
                            } else {
                                Toast.makeText(context, "PDF export failed, sharing text report instead", Toast.LENGTH_SHORT).show()
                                shareTextReport(context, currentRecord, indicators, recommendations)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberEmeraldPrimary, contentColor = Color.White),
                    enabled = !isExportingPdf,
                    modifier = Modifier.testTag("confirm_export_pdf_btn")
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export PDF")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExportDialog = false },
                    enabled = !isExportingPdf
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// Helper UI Components
// -------------------------------------------------------------

@Composable
private fun MultilingualExplanationCard(
    currentRecord: ScanRecord,
    currentLanguage: String,
    translatedExplanations: Map<String, String>,
    isTranslating: Boolean,
    onSelectLanguage: (String) -> Unit
) {
    val languages = listOf("English", "Urdu (اردو)", "Roman Urdu")
    val translationKey = "${currentRecord.id}_$currentLanguage"
    val translatedContent = translatedExplanations[translationKey]

    CyberCard(modifier = Modifier.testTag("multilingual_explanation_card")) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Translate, contentDescription = null, tint = CyberEmeraldPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Multilingual Assessment",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isTranslating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CyberEmeraldPrimary, strokeWidth = 2.dp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Language Selector Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(languages) { lang ->
                    val isSelected = currentLanguage == lang
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectLanguage(lang) },
                        label = { Text(lang, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberEmeraldPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = CyberEmeraldPrimary
                        ),
                        modifier = Modifier.testTag("lang_chip_${lang.take(4).lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isTranslating) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Preparing $currentLanguage explanation with Gemini AI...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else if (currentLanguage == "English") {
                Text(
                    text = currentRecord.summary.ifBlank { "Analysis completed: ${currentRecord.category}" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else if (!translatedContent.isNullOrBlank()) {
                val isUrduScript = currentLanguage.contains("اردو")
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = translatedContent,
                        style = if (isUrduScript) {
                            MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 22.sp,
                                textDirection = TextDirection.Rtl
                            )
                        } else {
                            MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp)
                        },
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenshotTextHighlightCard(
    extractedText: String,
    indicators: List<RiskIndicator>
) {
    var selectedIndicatorEvidence by remember { mutableStateOf<RiskIndicator?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("screenshot_highlight_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Screenshot Evidence Inspector",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "Tap any detected evidence marker to see why that phrase was flagged:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            // Extracted raw OCR text box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Extracted Text (OCR):",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = CyberCyan)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = extractedText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Evidence Chips
            Text(
                text = "Flagged Snippets:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(indicators) { ind ->
                    val isCritical = ind.severity.equals("CRITICAL", true) || ind.severity.equals("HIGH", true)
                    val chipColor = if (isCritical) RiskCriticalRed else RiskModerateAmber
                    val isSelected = selectedIndicatorEvidence?.name == ind.name

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) chipColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) chipColor else Color.Transparent),
                        modifier = Modifier
                            .clickable {
                                selectedIndicatorEvidence = if (isSelected) null else ind
                            }
                            .testTag("evidence_chip_${ind.name.take(6).lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(chipColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = ind.evidence.ifBlank { ind.name },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            selectedIndicatorEvidence?.let { ind ->
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "⚠ ${ind.name} (${ind.severity})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (ind.severity.equals("CRITICAL", true)) RiskCriticalRed else RiskModerateAmber
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = ind.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AiScamAssistantSection(
    record: ScanRecord,
    messages: List<AssistantMessage>,
    isLoading: Boolean,
    inputValue: String,
    onInputChange: (String) -> Unit,
    onSendQuestion: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val suggestedQuestions = listOf(
        "Why was this flagged?",
        "What should I do now?",
        "How could this scam work?",
        "Explain in simple Urdu",
        "What info should I never share?"
    )

    Card(
        modifier = Modifier.fillMaxWidth().testTag("ai_scam_assistant_section"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = CyberEmeraldPrimary.copy(alpha = 0.2f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assistant,
                            contentDescription = null,
                            tint = CyberEmeraldPrimary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI Scam Assistant",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Ask questions about this specific scan & safety steps",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyan
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = CyberEmeraldPrimary
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Suggested Queries
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(suggestedQuestions) { prompt ->
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clickable { onSendQuestion(prompt) }
                                    .testTag("assistant_suggestion_${prompt.take(5).lowercase()}")
                            ) {
                                Text(
                                    text = prompt,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Chat Messages Container
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(messages, key = { it.id }) { msg ->
                                AssistantChatBubble(message = msg)
                            }

                            if (isLoading) {
                                item {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = CyberEmeraldPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Analyzing context with Gemini AI...",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Input Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputValue,
                            onValueChange = onInputChange,
                            placeholder = { Text("Ask follow-up question...", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("assistant_text_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberEmeraldPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = {
                                if (inputValue.isNotBlank()) {
                                    onSendQuestion(inputValue)
                                }
                            },
                            enabled = inputValue.isNotBlank() && !isLoading,
                            modifier = Modifier.testTag("assistant_send_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (inputValue.isNotBlank()) CyberEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssistantChatBubble(message: AssistantMessage) {
    val isUser = message.isFromUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isUser) 12.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 12.dp
            ),
            color = if (isUser) CyberEmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 17.sp),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}

@Composable
private fun TtsVoiceBriefingCard(
    isSpeaking: Boolean,
    onPlay: () -> Unit,
    onStop: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ttsPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ttsPulseAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tts_voice_briefing_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSpeaking) CyberEmeraldPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSpeaking) CyberEmeraldPrimary.copy(alpha = pulseAlpha * 0.3f) else CyberCyan.copy(alpha = 0.15f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Voice Briefing",
                        tint = if (isSpeaking) CyberEmeraldPrimary else CyberCyan,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isSpeaking) "Speaking Voice Briefing..." else "Audio Threat Summary",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isSpeaking) "Tap 'Stop' to end speech" else "Listen to English voice assessment",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSpeaking) CyberEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isSpeaking) {
                Button(
                    onClick = onStop,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RiskCriticalRed,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("tts_stop_btn")
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stop", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            } else {
                Button(
                    onClick = onPlay,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberEmeraldPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("tts_play_btn")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Listen", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Share & Export Intent Helpers
// -------------------------------------------------------------

private fun shareTextReport(
    context: Context,
    record: ScanRecord,
    indicators: List<RiskIndicator>,
    recommendations: List<Recommendation>
) {
    val shareText = buildString {
        appendLine("🛡️ AI ScamShield PK — Threat Assessment Report")
        appendLine("Assessment: ${record.riskLevel.uppercase()} (${record.riskScore}/100)")
        appendLine("Category: ${record.category}")
        appendLine("Modality: ${record.inputType} • Date: ${SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Date(record.timestampMs))}")
        appendLine("Summary: ${record.summary}")
        if (indicators.isNotEmpty()) {
            appendLine("\nKey Indicators:")
            indicators.take(3).forEach { appendLine("• [${it.severity}] ${it.name}: ${it.explanation}") }
        }
        if (recommendations.isNotEmpty()) {
            appendLine("\nActionable Advice:")
            recommendations.take(2).forEach { appendLine("✔ ${it.action}") }
        }
        appendLine("\nOfficial Pakistan Cyber Helplines:")
        appendLine("• FIA Cyber Crime: Call 1991")
        appendLine("• PTA Anti-Fraud: 0800-55055")
        appendLine("\nDeveloped by Team Trident AI")
    }

    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(intent, "Share ScamShield Report"))
}

private fun sharePdfFile(context: Context, pdfFile: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "application/pdf"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Threat Assessment PDF"))
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open share sheet: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
