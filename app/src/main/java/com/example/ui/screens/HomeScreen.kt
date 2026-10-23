package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ScanRecord
import com.example.ui.components.CyberCard
import com.example.ui.components.DisclaimerBanner
import com.example.ui.components.RiskBadge
import com.example.ui.components.getRiskColor
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmeraldDark
import com.example.ui.theme.CyberEmeraldPrimary
import com.example.ui.theme.RiskCriticalRed
import com.example.ui.theme.RiskHighOrange
import com.example.ui.viewmodel.ScamShieldViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: ScamShieldViewModel,
    onNavigateToScanner: (tabIndex: Int) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToDemo: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToResult: (recordId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val allScans by viewModel.allScans.collectAsStateWithLifecycle()
    val metrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            HeroHeader(modelName = selectedModel)
        }

        item {
            Text(
                text = "Instant Scam Assessment",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            QuickScanActions(
                onScanMessage = { onNavigateToScanner(0) },
                onScanScreenshot = { onNavigateToScanner(1) },
                onCheckUrl = { onNavigateToScanner(2) }
            )
        }

        item {
            PakistanThreatAlertCard()
        }

        item {
            LiveStatsCard(metrics = metrics, onHistoryClick = onNavigateToHistory)
        }

        item {
            DemoModeHighlightCard(onDemoClick = onNavigateToDemo)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Risk Scans",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (allScans.isNotEmpty()) {
                    Text(
                        text = "View All (${allScans.size})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = CyberEmeraldPrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .clickable(onClick = onNavigateToHistory)
                            .testTag("view_all_scans_btn")
                    )
                }
            }
        }

        if (allScans.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No scans completed yet",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Paste a suspicious message, link, or screenshot above to assess risk.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(allScans.take(3)) { scan ->
                RecentScanItem(scan = scan, onClick = { onNavigateToResult(scan.id) })
            }
        }

        item {
            PakistanHelplinesQuickStrip()
        }

        item {
            DisclaimerBanner()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HeroHeader(modelName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_header_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            CyberEmeraldDark.copy(alpha = 0.25f)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = CyberEmeraldPrimary.copy(alpha = 0.2f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "App Shield Logo",
                                tint = CyberEmeraldPrimary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "AI ScamShield PK",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Detect. Understand. Stay Safe.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CyberCyan,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(CyberEmeraldPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Gemini AI ($modelName)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = CyberCyan.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Developed by Team Trident AI",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickScanActions(
    onScanMessage: () -> Unit,
    onScanScreenshot: () -> Unit,
    onCheckUrl: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ActionTile(
            title = "Message",
            subtitle = "SMS / WhatsApp",
            icon = Icons.AutoMirrored.Filled.Chat,
            accentColor = CyberEmeraldPrimary,
            modifier = Modifier.weight(1f),
            onClick = onScanMessage
        )
        ActionTile(
            title = "Screenshot",
            subtitle = "Image OCR",
            icon = Icons.Default.Image,
            accentColor = CyberCyan,
            modifier = Modifier.weight(1f),
            onClick = onScanScreenshot
        )
        ActionTile(
            title = "Check URL",
            subtitle = "Link Safety",
            icon = Icons.Default.Link,
            accentColor = RiskHighOrange,
            modifier = Modifier.weight(1f),
            onClick = onCheckUrl
        )
    }
}

@Composable
private fun ActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("action_${title.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.15f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PakistanThreatAlertCard() {
    CyberCard(
        borderColor = RiskHighOrange.copy(alpha = 0.4f),
        modifier = Modifier.testTag("pak_threat_alert_card")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = RiskHighOrange.copy(alpha = 0.15f)
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = "Alert",
                    tint = RiskHighOrange,
                    modifier = Modifier.padding(6.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Active Pakistani Cyber Threat Alert",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "High volume of spoofed Easypaisa/JazzCash biometric block threats and fake BISP 25,000 lottery WhatsApp links targeting local users.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LiveStatsCard(
    metrics: com.example.data.repository.DashboardMetrics,
    onHistoryClick: () -> Unit
) {
    CyberCard(
        onClick = onHistoryClick,
        modifier = Modifier.testTag("live_stats_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Protection Overview",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(label = "Total Scans", value = "${metrics.totalScans}")
                StatItem(
                    label = "High / Critical",
                    value = "${metrics.highRiskCount}",
                    color = if (metrics.highRiskCount > 0) RiskCriticalRed else MaterialTheme.colorScheme.onSurface
                )
                StatItem(label = "Avg Threat", value = "${metrics.averageRiskScore}%")
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DemoModeHighlightCard(onDemoClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDemoClick)
            .testTag("demo_mode_shortcut_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = CyberCyan.copy(alpha = 0.15f)
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "Demo Mode",
                    tint = CyberCyan,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Scam Awareness Trainer & Practice",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Interactive simulated scenarios for detecting fraud in Pakistan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = CyberCyan,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun RecentScanItem(
    scan: ScanRecord,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()) }
    val dateString = remember(scan.timestampMs) { dateFormat.format(Date(scan.timestampMs)) }
    val riskColor = getRiskColor(scan.riskLevel)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("recent_scan_${scan.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(riskColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${scan.riskScore}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = riskColor
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = scan.category,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = scan.inputType,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = scan.contentPreview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$dateString • ${scan.language}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PakistanHelplinesQuickStrip() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = CyberEmeraldPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Official Pakistani Cybercrime Helplines",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "• FIA Cyber Crime: 1991  • PTA Spam: 0800-55055  • SBP Financial Fraud: 111-727-273",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
