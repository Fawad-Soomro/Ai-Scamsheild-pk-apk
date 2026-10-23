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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ScanRecord
import com.example.ui.components.getRiskColor
import com.example.ui.theme.CyberEmeraldPrimary
import com.example.ui.theme.RiskCriticalRed
import com.example.ui.viewmodel.ScamShieldViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: ScamShieldViewModel,
    onNavigateToResult: (recordId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredScans by viewModel.filteredScans.collectAsStateWithLifecycle()
    val allScans by viewModel.allScans.collectAsStateWithLifecycle()
    val searchQuery by viewModel.historySearchQuery.collectAsStateWithLifecycle()
    val filterRisk by viewModel.historyFilterRisk.collectAsStateWithLifecycle()
    val filterType by viewModel.historyFilterType.collectAsStateWithLifecycle()

    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val riskOptions = listOf("ALL", "Critical", "High", "Moderate", "Low")
    val typeOptions = listOf("ALL", "MESSAGE", "SCREENSHOT", "URL")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Scan History",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${allScans.size} total scans stored locally",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (allScans.isNotEmpty()) {
                IconButton(
                    onClick = { showClearConfirmDialog = true },
                    modifier = Modifier.testTag("clear_history_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear History",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.historySearchQuery.value = it },
            placeholder = { Text("Search by category, summary, keywords...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("history_search_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = CyberEmeraldPrimary)
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { viewModel.historySearchQuery.value = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberEmeraldPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter chips (Risk)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            items(riskOptions) { risk ->
                FilterChip(
                    selected = filterRisk == risk,
                    onClick = { viewModel.historyFilterRisk.value = risk },
                    label = { Text(if (risk == "ALL") "All Risks" else "$risk Risk", fontSize = 11.sp) },
                    modifier = Modifier.testTag("filter_risk_$risk"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberEmeraldPrimary.copy(alpha = 0.2f),
                        selectedLabelColor = CyberEmeraldPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredScans.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (searchQuery.isNotBlank() || filterRisk != "ALL") "No scans match filters" else "No scan history recorded",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredScans, key = { it.id }) { scan ->
                    HistoryItemCard(
                        scan = scan,
                        onClick = { onNavigateToResult(scan.id) },
                        onDelete = { viewModel.deleteScan(scan.id) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear All Scan History?") },
            text = { Text("This will permanently remove all local scan records from your on-device SQLite Room database.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearConfirmDialog = false
                    },
                    modifier = Modifier.testTag("confirm_clear_history_btn")
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun HistoryItemCard(
    scan: ScanRecord,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }
    val dateString = remember(scan.timestampMs) { dateFormat.format(Date(scan.timestampMs)) }
    val riskColor = getRiskColor(scan.riskLevel)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("history_item_${scan.id}"),
        shape = RoundedCornerShape(14.dp),
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
                    .size(44.dp)
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
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = scan.inputType,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = riskColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = scan.riskLevel.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = riskColor
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = scan.summary.ifBlank { scan.contentPreview },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$dateString • ${scan.language}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp).testTag("delete_item_${scan.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete item",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
