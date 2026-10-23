package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CyberCard
import com.example.ui.components.DisclaimerBanner
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmeraldPrimary
import com.example.ui.theme.RiskCriticalRed
import com.example.ui.viewmodel.ScamShieldViewModel

@Composable
fun SettingsScreen(
    viewModel: ScamShieldViewModel,
    onNavigateToVivaQnA: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val soundEffectsEnabled by viewModel.soundEffectsEnabled.collectAsStateWithLifecycle()
    val allScans by viewModel.allScans.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showClearDialog by remember { mutableStateOf(false) }

    val modelOptions = listOf(
        "gemini-3.5-flash" to "Gemini 3.5 Flash (Recommended - Balanced speed & reasoning)",
        "gemini-3.1-pro-preview" to "Gemini 3.1 Pro (Deep reasoning for complex edge-cases)",
        "gemini-3.1-flash-lite-preview" to "Gemini 3.1 Flash Lite (Ultra low latency)"
    )

    val helplines = listOf(
        Triple("FIA Cyber Crime Wing (NR3C)", "1991", "Report digital fraud, blackmails, unauthorized bank transfers"),
        Triple("PTA Consumer Protection", "0800-55055", "Report spam SMS, unsolicited robocalls, illegal SIMs"),
        Triple("State Bank of Pakistan (SBP)", "111-727-273", "Banking complaints & unauthorized mobile wallet transactions")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = CyberEmeraldPrimary.copy(alpha = 0.15f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = CyberEmeraldPrimary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Settings & About",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "AI reasoning model, audio alerts, and security FAQs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Developer Attribution Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("about_developer_section_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = CyberEmeraldPrimary.copy(alpha = 0.2f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Developer Attribution",
                            tint = CyberEmeraldPrimary,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Developed by Team Trident AI",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Cybersecurity & Digital Scam Prevention Initiative",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberCyan
                        )
                    }
                }
            }
        }

        // Sound & Audio Alerts
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("audio_settings_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = CyberEmeraldPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Cyber Acoustic Effects",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Audio cues when scanning and completing threat assessments",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = soundEffectsEnabled,
                        onCheckedChange = { viewModel.setSoundEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CyberEmeraldPrimary
                        ),
                        modifier = Modifier.testTag("sound_effects_switch")
                    )
                }
            }
        }

        // Appearance
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("appearance_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DarkMode, contentDescription = null, tint = CyberCyan)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Cyber Dark Theme",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Optimized high-contrast cybersecurity palette",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.isDarkMode.value = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CyberEmeraldPrimary
                        ),
                        modifier = Modifier.testTag("dark_mode_switch")
                    )
                }
            }
        }

        // Gemini AI Model Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("ai_model_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = CyberEmeraldPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Gemini Reasoning Engine",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    modelOptions.forEach { (modelKey, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectedModel.value = modelKey }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedModel == modelKey,
                                onClick = { viewModel.selectedModel.value = modelKey },
                                colors = RadioButtonDefaults.colors(selectedColor = CyberEmeraldPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Security FAQs & Technical Knowledge Base
        item {
            CyberCard(
                onClick = onNavigateToVivaQnA,
                modifier = Modifier.testTag("viva_qna_shortcut")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.QuestionAnswer,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Security & Technical FAQs",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Detailed answers on Gemini AI reasoning, Roman Urdu detection, and on-device privacy",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Official Helplines Directory
        item {
            Text(
                text = "Pakistan Cybercrime & Consumer Helplines",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(helplines) { (name, phone, desc) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$phone")
                        }
                        context.startActivity(intent)
                    }
                    .testTag("helpline_${phone.replace("-", "")}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = RiskCriticalRed.copy(alpha = 0.15f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call",
                            tint = RiskCriticalRed,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CyberEmeraldPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = phone,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CyberEmeraldPrimary
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Local Storage Management
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("storage_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Local Storage (Room DB)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${allScans.size} scan records stored securely on-device",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (allScans.isNotEmpty()) {
                        TextButton(
                            onClick = { showClearDialog = true },
                            modifier = Modifier.testTag("clear_db_btn")
                        ) {
                            Text("Clear Data", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // App Information
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "About AI ScamShield PK",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Version 1.1.0 • Developed by Team Trident AI • Engineered for Pakistan Digital Defense",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            DisclaimerBanner()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Records?") },
            text = { Text("This will permanently delete all scan records from the local SQLite database.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearDialog = false
                    }
                ) {
                    Text("Delete All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
