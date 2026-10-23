package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.TextPreprocessor
import com.example.ui.components.AnalysisLoadingOverlay
import com.example.ui.components.DisclaimerBanner
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmeraldPrimary
import com.example.ui.theme.RiskCriticalRed
import com.example.ui.theme.RiskHighOrange
import com.example.ui.viewmodel.ScamShieldViewModel
import java.io.InputStream

@Composable
fun ScannerScreen(
    viewModel: ScamShieldViewModel,
    initialTab: Int = 0,
    onNavigateToResult: (recordId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(initialTab.coerceIn(0, 2)) }
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(scanState.errorMessage) {
        scanState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
    }

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
                    .testTag("scanner_tabs")
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Message", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_message")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Screenshot", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_screenshot")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("URL Link", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_url")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> MessageScannerTab(viewModel = viewModel, clipboardText = clipboardManager.getText()?.text ?: "")
                1 -> ScreenshotScannerTab(viewModel = viewModel)
                2 -> UrlScannerTab(viewModel = viewModel, clipboardText = clipboardManager.getText()?.text ?: "")
            }
        }

        if (scanState.isScanning) {
            AnalysisLoadingOverlay(stepText = scanState.analysisStep)
        }
    }
}

@Composable
private fun MessageScannerTab(
    viewModel: ScamShieldViewModel,
    clipboardText: String
) {
    val messageText by viewModel.messageInput.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val entities = remember(messageText) {
        if (messageText.isNotBlank()) TextPreprocessor.preprocess(messageText) else null
    }

    val sampleMessages = listOf(
        "Easypaisa OTP SMS" to "Muaziz Sarif! Apka Easypaisa account biometric tasdeeq na honay par 2 ghantay mein band ho jayega. Account bachanay k liye foran apna OTP 8842 par share karein ya is link par login karein: http://easypaisa-secure-verify.top/auth",
        "BISP 25,000 (Urdu)" to "مبارک ہو! بے نظیر انکم سپورٹ پروگرام کی طرف سے آپ کا 25,000 روپے کا امدادی کیش منظور ہو گیا ہے۔ ابھی رقم وصول کرنے کے لیے 0304-9876543 پر رابطہ کریں یا رجسٹریشن فیس 500 روپے نیچے دیے گئے نمبر پر ایزی پیسہ کریں۔",
        "Remote Task Job" to "Exclusive Remote Job Offer! Earn Rs. 6,000 to Rs. 15,000 daily from home by simply watching and liking YouTube videos. To activate your employee portal, deposit Rs. 1,200 VIP security deposit via JazzCash to 0300-1234567.",
        "Nanoloan Blackmail" to "Aapka 50,000 ka EasyLoan approve ho chuka hai bina kisi zimanat k صرف 10 minute mein. Hamari app foran download karein aur apni contacts aur gallery permission allow karein: http://fast-pkr-loan.xyz/app.apk",
        "Legitimate SMS" to "Dear Customer, PKR 10,000.00 has been debited from your Account **3421 at Meezan Bank ATM on 14-Aug-2026. Available Balance is PKR 45,210.50. If this was not done by you, please call 111-331-331 immediately."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Message Content",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Row {
                if (clipboardText.isNotBlank()) {
                    OutlinedButton(
                        onClick = { viewModel.messageInput.value = clipboardText },
                        modifier = Modifier.height(34.dp).testTag("paste_message_btn"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paste", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                if (messageText.isNotBlank()) {
                    OutlinedButton(
                        onClick = { viewModel.messageInput.value = "" },
                        modifier = Modifier.height(34.dp).testTag("clear_message_btn"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = messageText,
            onValueChange = { viewModel.messageInput.value = it },
            placeholder = {
                Text("Paste suspicious SMS, WhatsApp message, email snippet, or lottery notification here...")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .testTag("message_input_field"),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberEmeraldPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (entities != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Detected: ${entities.detectedLanguage}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    if (entities.hasUrgencySignals) {
                        Text(
                            text = " • Urgency Detected",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = RiskHighOrange,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            Text(
                text = "${messageText.length} chars",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Or Load Synthetic Pakistani Test Cases:",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sampleMessages) { (label, sample) ->
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.messageInput.value = sample }
                        .testTag("sample_chip_${label.lowercase().replace(" ", "_")}"),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberEmeraldPrimary.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.scanMessage() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("analyze_message_btn"),
            enabled = messageText.isNotBlank(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyberEmeraldPrimary,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Default.Security, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Analyze Message with Gemini AI",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        DisclaimerBanner()
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ScreenshotScannerTab(
    viewModel: ScamShieldViewModel
) {
    val selectedBitmap by viewModel.selectedImageBitmap.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeStream(stream, null, options)
                    var sampleSize = 1
                    val maxDim = maxOf(options.outWidth, options.outHeight)
                    if (maxDim > 1600) {
                        sampleSize = (maxDim / 1600).coerceAtLeast(1)
                    }

                    context.contentResolver.openInputStream(it)?.use { actualStream ->
                        val decodeOptions = BitmapFactory.Options().apply {
                            inSampleSize = sampleSize
                            inPreferredConfig = Bitmap.Config.ARGB_8888
                        }
                        val bitmap = BitmapFactory.decodeStream(actualStream, null, decodeOptions)
                        viewModel.selectedImageBitmap.value = bitmap
                    }
                }
            } catch (e: Exception) {
                // Handled in ViewModel
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Screenshot & Image OCR Scanner",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Upload a screenshot of a WhatsApp chat, SMS inbox, banking app notification, or social media banner to extract text and analyze risk.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedBitmap != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .testTag("selected_image_preview"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        bitmap = selectedBitmap!!.asImageBitmap(),
                        contentDescription = "Selected Screenshot",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )

                    IconButton(
                        onClick = { viewModel.selectedImageBitmap.value = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color.Black.copy(alpha = 0.65f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove Image", tint = Color.White)
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { galleryLauncher.launch("image/*") }
                    .border(
                        1.dp,
                        CyberEmeraldPrimary.copy(alpha = 0.4f),
                        RoundedCornerShape(16.dp)
                    )
                    .testTag("upload_image_box"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        modifier = Modifier.size(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = CyberEmeraldPrimary.copy(alpha = 0.15f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = CyberEmeraldPrimary,
                            modifier = Modifier
                                .padding(12.dp)
                                .size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Select Screenshot from Gallery",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Supports JPG, PNG, WEBP",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f).height(46.dp).testTag("choose_gallery_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Choose Image")
            }

            OutlinedButton(
                onClick = {
                    // Generate a synthetic test screenshot bitmap with fraudulent text
                    val syntheticBitmap = createSyntheticTestBitmap()
                    viewModel.selectedImageBitmap.value = syntheticBitmap
                },
                modifier = Modifier.weight(1f).height(46.dp).testTag("load_sample_img_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Load Sample")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.scanScreenshot(selectedBitmap) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("analyze_screenshot_btn"),
            enabled = selectedBitmap != null,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyberCyan,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Default.Security, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Analyze Screenshot with Multimodal AI",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        DisclaimerBanner()
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun UrlScannerTab(
    viewModel: ScamShieldViewModel,
    clipboardText: String
) {
    val urlText by viewModel.urlInput.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val sampleUrls = listOf(
        "Easypaisa Phish" to "http://easypaisa-secure-verify.top/auth?token=923001234567",
        "BISP Fake Grant" to "http://bisp-pkr25000-reward.xyz/claim",
        "PakPost Parcel Fee" to "https://pakpost-redelivery-fees.work/track?pkg=PK8842",
        "PTA Tax Spoof" to "http://pta-dirbs-tax-portal.xyz/pay",
        "Official Bank (Safe)" to "https://www.meezanbank.com/online-banking"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Static URL & Domain Risk Inspector",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Performs passive, non-invasive static feature extraction checking domain length, HTTPS, IP address, Punycode, typosquatting of Pakistani services, and suspicious TLDs.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Target Web Link",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Row {
                if (clipboardText.isNotBlank()) {
                    OutlinedButton(
                        onClick = { viewModel.urlInput.value = clipboardText },
                        modifier = Modifier.height(34.dp).testTag("paste_url_btn"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paste", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                if (urlText.isNotBlank()) {
                    OutlinedButton(
                        onClick = { viewModel.urlInput.value = "" },
                        modifier = Modifier.height(34.dp).testTag("clear_url_btn"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = urlText,
            onValueChange = { viewModel.urlInput.value = it },
            placeholder = { Text("https://example.com/login...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("url_input_field"),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Link, contentDescription = null, tint = CyberEmeraldPrimary)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberEmeraldPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Or Test Synthetic Target URLs:",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sampleUrls) { (label, url) ->
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.urlInput.value = url }
                        .testTag("url_sample_${label.lowercase().replace(" ", "_")}"),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RiskHighOrange.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.scanUrl() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("analyze_url_btn"),
            enabled = urlText.isNotBlank(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RiskHighOrange,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Default.Security, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Inspect URL with Gemini AI",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        DisclaimerBanner()
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Helper to generate a clean synthetic bitmap for demo testing
private fun createSyntheticTestBitmap(): Bitmap {
    val width = 800
    val height = 450
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Background
    val bgPaint = Paint().apply { color = android.graphics.Color.parseColor("#0F172A") }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    // Header card
    val cardPaint = Paint().apply { color = android.graphics.Color.parseColor("#1E293B") }
    canvas.drawRoundRect(40f, 40f, 760f, 410f, 24f, 24f, cardPaint)

    // Text lines
    val titlePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#EF4444")
        textSize = 32f
        isFakeBoldText = true
    }
    canvas.drawText("URGENT: Easypaisa Account Verification", 70f, 100f, titlePaint)

    val bodyPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#F1F5F9")
        textSize = 24f
    }
    canvas.drawText("Your account will be suspended within 2 hours.", 70f, 160f, bodyPaint)
    canvas.drawText("Share your 4-digit biometric OTP code immediately", 70f, 205f, bodyPaint)
    canvas.drawText("to avoid permanent termination and penalty fine.", 70f, 250f, bodyPaint)

    val linkPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#10B981")
        textSize = 22f
    }
    canvas.drawText("Portal: http://easypaisa-secure-verify.top/auth", 70f, 320f, linkPaint)

    val notePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#94A3B8")
        textSize = 18f
    }
    canvas.drawText("From: +92 301 9988776  •  14 Aug 2026", 70f, 375f, notePaint)

    return bitmap
}
