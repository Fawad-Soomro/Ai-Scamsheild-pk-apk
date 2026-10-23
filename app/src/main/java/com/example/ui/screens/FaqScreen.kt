package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
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
import com.example.ui.components.DisclaimerBanner
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmeraldPrimary

data class SecurityFaqItem(
    val id: String,
    val category: String,
    val question: String,
    val answer: String,
    val keyTakeaway: String
)

val securityFaqList = listOf(
    SecurityFaqItem(
        id = "faq1",
        category = "AI Architecture",
        question = "How does ScamShield analyze unstructured text and images?",
        answer = "The application integrates the Gemini AI reasoning engine with deterministic JSON schema validation. Unstructured text messages are preprocessed locally to identify language scripts and urgency cues, while screenshots are passed as optimized byte arrays directly into Gemini's multimodal vision pipeline for optical character recognition (OCR) and contextual fraud extraction.",
        keyTakeaway = "Combines on-device preprocessing with multimodal Gemini reasoning and deterministic JSON output."
    ),
    SecurityFaqItem(
        id = "faq2",
        category = "Pakistani Localization",
        question = "How does ScamShield detect Roman Urdu and localized Pakistani threats?",
        answer = "Pakistani digital scammers frequently communicate in Roman Urdu (e.g. 'Apka account biometric tasdeeq na honay par block ho jayega') or Nastaliq script. ScamShield injects localized Pakistani fraud lexicons, common slang, Pakistani telecom prefixes, and official government schemes (BISP, Ehsaas, 8171, FIA, PTA) into the AI system instructions to accurately classify threats across all local dialects.",
        keyTakeaway = "Domain-specific prompt engineering embeds Pakistani financial and cultural threat patterns."
    ),
    SecurityFaqItem(
        id = "faq3",
        category = "URL Security",
        question = "Why does the URL scanner use static feature extraction rather than a live web crawler?",
        answer = "Crawling malicious links in real-time exposes the user's device/IP to drive-by malware, exploits, and server-side tracking. ScamShield uses passive static heuristic feature extraction (calculating Shannon entropy, domain length, Punycode detection, IP presence, subdomain nesting, and typosquatting of Easypaisa, JazzCash, Meezan, PakPost) before passing the structural breakdown to the AI model.",
        keyTakeaway = "Non-invasive analysis prevents malware execution and guarantees user device safety."
    ),
    SecurityFaqItem(
        id = "faq4",
        category = "Explainable AI",
        question = "How does ScamShield ensure explainability in scam risk assessments?",
        answer = "Rather than providing an opaque 'Safe/Unsafe' binary classification, ScamShield outputs a 0-100 continuous risk score, severity band (Critical/High/Moderate/Low), structured quotes highlighting specific fraud markers (urgency, OTP requests, advance fee demands), confidence score, and concrete remedial actions with official Pakistani helplines.",
        keyTakeaway = "Direct evidence citations and actionable mitigation replace black-box outputs."
    ),
    SecurityFaqItem(
        id = "faq5",
        category = "Data Privacy",
        question = "Is sensitive user communication stored or shared on external servers?",
        answer = "No. The application strictly adheres to on-device privacy principles. Scan records, analysis scores, and historical metrics are stored locally inside the device's encrypted SQLite database using Android Room. Zero private user data is sent to external databases or third-party servers.",
        keyTakeaway = "Offline-first Room database keeps scan telemetry strictly on-device."
    ),
    SecurityFaqItem(
        id = "faq6",
        category = "Offline Safety",
        question = "How does the app protect users when offline or without internet?",
        answer = "The local engine features rule-based regex and heuristic fallback extractors (TextPreprocessor and UrlRiskAnalyzer) that identify high-frequency keywords ('OTP', 'biometric block', '8171', 'BISP', 'lottery', unencrypted IP links) to render a baseline risk warning even if remote network requests are unavailable.",
        keyTakeaway = "Heuristic fallback ensures user safety even without network access."
    ),
    SecurityFaqItem(
        id = "faq7",
        category = "Responsible AI",
        question = "What are the legal and responsible AI guidelines of the system?",
        answer = "The app includes explicit Responsible AI disclaimers clarifying that results are automated probabilistic assessments for educational and digital safety guidance. It provides one-tap dialing to the FIA Cyber Crime Wing (NR3C) helpline 1991 and PTA to ensure official institutional reporting.",
        keyTakeaway = "Responsible AI notices and statutory helpline handoffs ensure institutional compliance."
    )
)

@Composable
fun FaqScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val categories = listOf("ALL", "AI Architecture", "Pakistani Localization", "URL Security", "Explainable AI", "Data Privacy", "Offline Safety", "Responsible AI")

    val filteredList = remember(searchQuery, selectedCategory) {
        securityFaqList.filter { item ->
            val matchesCategory = selectedCategory == "ALL" || item.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    item.question.contains(searchQuery, ignoreCase = true) ||
                    item.answer.contains(searchQuery, ignoreCase = true) ||
                    item.keyTakeaway.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top App Bar Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.testTag("faq_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Security & Technical FAQs",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Official Knowledge Base & Threat Protection Architecture",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search FAQ topics, Pakistani threats, AI...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = CyberEmeraldPrimary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberEmeraldPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("faq_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = { Text(category, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberEmeraldPrimary.copy(alpha = 0.2f),
                        selectedLabelColor = CyberEmeraldPrimary,
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("faq_chip_$category")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // FAQ List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Developer & About Highlight Card
            item {
                AboutDeveloperCard()
            }

            if (filteredList.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No matching FAQ entries found",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Try searching with keywords like 'Urdu', 'Room', 'Gemini', or 'OCR'.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredList) { item ->
                    SecurityFaqAccordionCard(item = item)
                }
            }

            item {
                DisclaimerBanner()
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun AboutDeveloperCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("about_developer_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = CyberEmeraldPrimary.copy(alpha = 0.2f)
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Verified Developer",
                        tint = CyberEmeraldPrimary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Developed by Team Trident AI",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Cybersecurity & Digital Scam Prevention Initiative",
                        style = MaterialTheme.typography.bodySmall.copy(color = CyberCyan)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "AI ScamShield PK was engineered by Team Trident AI to provide modern scam detection for Pakistan's digital ecosystem—protecting mobile banking users against OTP fraud, Urdu/Roman Urdu social engineering, lottery traps, and malicious phishing URLs.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SecurityFaqAccordionCard(item: SecurityFaqItem) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .testTag("faq_card_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CyberEmeraldPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyberEmeraldPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.question,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = CyberEmeraldPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = item.answer,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "Key Architecture Takeaway: ",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CyberCyan
                                )
                            )
                            Text(
                                text = item.keyTakeaway,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
