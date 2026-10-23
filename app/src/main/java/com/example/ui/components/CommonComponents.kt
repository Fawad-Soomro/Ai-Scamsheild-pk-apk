package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Recommendation
import com.example.data.model.RiskIndicator
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmeraldPrimary
import com.example.ui.theme.RiskCriticalRed
import com.example.ui.theme.RiskCriticalRedContainer
import com.example.ui.theme.RiskCriticalRedText
import com.example.ui.theme.RiskHighOrange
import com.example.ui.theme.RiskHighOrangeContainer
import com.example.ui.theme.RiskHighOrangeText
import com.example.ui.theme.RiskLowGreen
import com.example.ui.theme.RiskLowGreenContainer
import com.example.ui.theme.RiskLowGreenText
import com.example.ui.theme.RiskModerateAmber
import com.example.ui.theme.RiskModerateAmberContainer
import com.example.ui.theme.RiskModerateAmberText

@Composable
fun getRiskColor(level: String): Color {
    return when (level.lowercase()) {
        "critical" -> RiskCriticalRed
        "high" -> RiskHighOrange
        "moderate" -> RiskModerateAmber
        else -> RiskLowGreen
    }
}

@Composable
fun getRiskContainerColor(level: String): Color {
    return when (level.lowercase()) {
        "critical" -> RiskCriticalRedContainer
        "high" -> RiskHighOrangeContainer
        "moderate" -> RiskModerateAmberContainer
        else -> RiskLowGreenContainer
    }
}

@Composable
fun getRiskTextColor(level: String): Color {
    return when (level.lowercase()) {
        "critical" -> RiskCriticalRedText
        "high" -> RiskHighOrangeText
        "moderate" -> RiskModerateAmberText
        else -> RiskLowGreenText
    }
}

@Composable
fun RiskBadge(
    level: String,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true
) {
    val bgColor = getRiskContainerColor(level)
    val textColor = getRiskTextColor(level)
    val icon: ImageVector = when (level.lowercase()) {
        "critical" -> Icons.Default.Warning
        "high" -> Icons.Default.Warning
        "moderate" -> Icons.Default.Info
        else -> Icons.Default.CheckCircle
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .border(1.dp, getRiskColor(level).copy(alpha = 0.5f), RoundedCornerShape(100.dp)),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (showIcon) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = "${level.uppercase()} RISK",
                color = textColor,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            )
        }
    }
}

@Composable
fun RiskScoreDial(
    score: Int,
    level: String,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp
) {
    val strokeWidth = 14.dp
    val riskColor = getRiskColor(level)
    val animatedProgress by animateFloatAsState(
        targetValue = (score / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000),
        label = "scoreDial"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            // Background track
            drawArc(
                color = Color.DarkGray.copy(alpha = 0.35f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            // Foreground score arc
            drawArc(
                brush = Brush.sweepGradient(
                    0.0f to CyberEmeraldPrimary,
                    0.4f to RiskModerateAmber,
                    0.7f to RiskHighOrange,
                    1.0f to RiskCriticalRed
                ),
                startAngle = 135f,
                sweepAngle = 270f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = riskColor
                )
            )
            Text(
                text = "OUT OF 100",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            RiskBadge(level = level, showIcon = false)
        }
    }
}

@Composable
fun IndicatorCard(
    indicator: RiskIndicator,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }
    val riskColor = getRiskColor(indicator.severity)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("indicator_${indicator.name.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(riskColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = indicator.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                RiskBadge(level = indicator.severity, showIcon = false)
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    if (indicator.evidence.isNotBlank()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, riskColor.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "Quote: ",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = riskColor
                                    )
                                )
                                Text(
                                    text = "\"${indicator.evidence}\"",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = indicator.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun RecommendationCard(
    recommendation: Recommendation,
    modifier: Modifier = Modifier
) {
    val isUrgent = recommendation.priority.equals("urgent", ignoreCase = true)
    val accentColor = if (isUrgent) RiskCriticalRed else CyberEmeraldPrimary

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("rec_item"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.15f)
            ) {
                Icon(
                    imageVector = if (isUrgent) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = recommendation.action,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    if (isUrgent) {
                        Surface(
                            color = RiskCriticalRedContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "URGENT",
                                color = RiskCriticalRedText,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (recommendation.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = recommendation.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    borderColor: Color = CyberEmeraldPrimary.copy(alpha = 0.25f),
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        content()
    }
}

@Composable
fun DisclaimerBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = CyberCyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "ScamShield provides digital safety risk assessment and awareness assistance. It cannot guarantee that a message, image, or URL is safe or malicious.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AnalysisLoadingOverlay(
    stepText: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAnim"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(0.88f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(CyberEmeraldPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Shield Scanning",
                        tint = CyberEmeraldPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "AI Threat Assessment",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp,
                    color = CyberEmeraldPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stepText.ifBlank { "Analyzing content with Gemini AI..." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
