package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.RiskHighOrange
import com.example.ui.theme.RiskLowGreen
import com.example.ui.theme.RiskModerateAmber
import com.example.ui.viewmodel.ScamShieldViewModel

@Composable
fun InsightsScreen(
    viewModel: ScamShieldViewModel,
    modifier: Modifier = Modifier
) {
    val allScans by viewModel.allScans.collectAsStateWithLifecycle()
    val metrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()

    val total = metrics.totalScans.coerceAtLeast(1)
    val criticalCount = allScans.count { it.riskLevel == "Critical" }
    val highCount = allScans.count { it.riskLevel == "High" }
    val moderateCount = allScans.count { it.riskLevel == "Moderate" }
    val lowCount = allScans.count { it.riskLevel == "Low" }

    val messageScansCount = allScans.count { it.inputType == "MESSAGE" }
    val screenshotScansCount = allScans.count { it.inputType == "SCREENSHOT" }
    val urlScansCount = allScans.count { it.inputType == "URL" }

    val categoryBreakdown = allScans.groupBy { it.category }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = CyberEmeraldPrimary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Personal Threat Insights",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Calculated locally on-device from your scan records",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (allScans.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(54.dp),
                            shape = CircleShape,
                            color = CyberCyan.copy(alpha = 0.15f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No Personal Scans Recorded Yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Your insights dashboard updates automatically as you scan suspicious messages, URLs, or screenshots on this phone.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Summary 4-Metric Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCard(
                            title = "Total Scans",
                            value = "${metrics.totalScans}",
                            accentColor = CyberCyan,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "High / Critical",
                            value = "${metrics.highRiskCount}",
                            accentColor = RiskCriticalRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCard(
                            title = "Safe / Low Risk",
                            value = "${metrics.lowRiskCount}",
                            accentColor = RiskLowGreen,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Scans This Week",
                            value = "${metrics.scansThisWeek}",
                            accentColor = CyberEmeraldPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Personalized Safety Suggestions (FEATURE 3)
            if (metrics.personalizedSafetyTips.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("personalized_tips_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = RiskModerateAmber, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Personal Safety Observations",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            metrics.personalizedSafetyTips.forEach { tip ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("•", color = CyberEmeraldPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 6.dp))
                                    Text(
                                        text = tip,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Risk Level Distribution Card
            item {
                CyberCard(modifier = Modifier.testTag("risk_distribution_card")) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Risk Level Distribution",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        DistributionBar(label = "Critical Risk (75-100%)", count = criticalCount, total = total, color = RiskCriticalRed)
                        Spacer(modifier = Modifier.height(8.dp))
                        DistributionBar(label = "High Risk (50-74%)", count = highCount, total = total, color = RiskHighOrange)
                        Spacer(modifier = Modifier.height(8.dp))
                        DistributionBar(label = "Moderate Risk (25-49%)", count = moderateCount, total = total, color = RiskModerateAmber)
                        Spacer(modifier = Modifier.height(8.dp))
                        DistributionBar(label = "Low Risk (0-24%)", count = lowCount, total = total, color = RiskLowGreen)
                    }
                }
            }

            // Top Detected Indicators in User History
            if (metrics.topIndicators.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("top_indicators_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = RiskHighOrange, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Common Threat Signals in Your Content",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            metrics.topIndicators.forEach { (indicatorName, count) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = indicatorName,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(100.dp),
                                        color = CyberCyan.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "$count times",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = CyberCyan,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Input Modality Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("modality_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Input Modality Analysis",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ModalityItem(
                                icon = Icons.AutoMirrored.Filled.Chat,
                                label = "Messages",
                                count = messageScansCount,
                                color = CyberEmeraldPrimary
                            )
                            ModalityItem(
                                icon = Icons.Default.Image,
                                label = "Screenshots",
                                count = screenshotScansCount,
                                color = CyberCyan
                            )
                            ModalityItem(
                                icon = Icons.Default.Link,
                                label = "URLs",
                                count = urlScansCount,
                                color = RiskHighOrange
                            )
                        }
                    }
                }
            }

            // Prevalent Fraud Categories Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("top_categories_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Prevalent Fraud Categories",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (categoryBreakdown.isEmpty()) {
                            Text(
                                text = "Run scans or test practice scenarios to populate fraud category insights.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            categoryBreakdown.take(5).forEach { (category, count) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "$count scans",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = CyberEmeraldPrimary
                                    )
                                }
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
private fun MetricCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DistributionBar(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val fraction = if (total > 0) (count.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
    val percentage = (fraction * 100).toInt()

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$count ($percentage%)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(100.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun ModalityItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: Int,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.15f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier
                    .padding(10.dp)
                    .size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
