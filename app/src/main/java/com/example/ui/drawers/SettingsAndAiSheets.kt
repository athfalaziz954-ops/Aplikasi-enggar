package com.example.ui.drawers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.local.CustomerEntity
import com.example.data.local.StoreSettings
import com.example.data.repository.CsvExportType
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.CoralSoft
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.GlassBorderBright
import com.example.ui.theme.IndigoAi
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PearlWhite
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.AiUiState
import com.example.util.AppsScriptTemplate
import com.example.util.CurrencyAndDateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    settings: StoreSettings,
    onSaveSettings: (
        storeName: String,
        storeAddress: String,
        storeWhatsapp: String,
        invoiceFooter: String,
        webhookUrl: String
    ) -> Unit,
    onExportCsv: (CsvExportType) -> Unit,
    onImportProductsCsv: () -> Unit,
    onCopiedToast: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var storeName by remember(settings) { mutableStateOf(settings.storeName) }
    var storeAddress by remember(settings) { mutableStateOf(settings.storeAddress) }
    var storeWhatsapp by remember(settings) { mutableStateOf(settings.storeWhatsapp) }
    var invoiceFooter by remember(settings) { mutableStateOf(settings.invoiceFooter) }
    var webhookUrl by remember(settings) { mutableStateOf(settings.webhookUrl) }
    var showAppsScriptDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PearlWhite.copy(alpha = 0.96f),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pengaturan & Google Sheets",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Slate900
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Tutup", tint = Slate700)
                }
            }

            Text(
                text = "Profil Warung (Muncul di Invoice PDF)",
                style = MaterialTheme.typography.labelLarge,
                color = EmeraldDeep
            )

            OutlinedTextField(
                value = storeName,
                onValueChange = { storeName = it },
                label = { Text("Nama Warung") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = storeAddress,
                onValueChange = { storeAddress = it },
                label = { Text("Alamat Warung") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = storeWhatsapp,
                onValueChange = { storeWhatsapp = it },
                label = { Text("No. WhatsApp Toko") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = invoiceFooter,
                onValueChange = { invoiceFooter = it },
                label = { Text("Catatan Kaki Invoice") },
                maxLines = 2,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Integrasi Google Sheets (Real-Time Webhook)",
                style = MaterialTheme.typography.labelLarge,
                color = EmeraldDeep
            )

            OutlinedTextField(
                value = webhookUrl,
                onValueChange = { webhookUrl = it },
                label = { Text("URL Webhook Google Apps Script") },
                placeholder = { Text("https://script.google.com/macros/s/.../exec") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = { showAppsScriptDialog = true },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("view_apps_script_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Code,
                    contentDescription = null,
                    tint = EmeraldDeep,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Lihat Kode Apps Script (doPost)", color = EmeraldDeep, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    onSaveSettings(storeName, storeAddress, storeWhatsapp, invoiceFooter, webhookUrl)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Simpan Pengaturan", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Cadangan Offline (CSV / Excel)",
                style = MaterialTheme.typography.labelLarge,
                color = EmeraldDeep
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onExportCsv(CsvExportType.TRANSAKSI) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Transaksi", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = { onExportCsv(CsvExportType.STOK) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stok", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = { onExportCsv(CsvExportType.KASBON) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kasbon", style = MaterialTheme.typography.labelSmall)
                }
            }

            OutlinedButton(
                onClick = onImportProductsCsv,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Rounded.UploadFile, contentDescription = null, tint = Slate900, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Impor Data Produk (CSV)", color = Slate900, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }

    if (showAppsScriptDialog) {
        val clipboardManager = LocalClipboardManager.current
        Dialog(onDismissRequest = { showAppsScriptDialog = false }) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White.copy(alpha = 0.98f),
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Kode Google Apps Script",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Slate900
                    )
                    Text(
                        text = "Tempel di Extensions > Apps Script pada Google Sheet Anda, lalu Deploy sebagai Web App (Anyone).",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Slate900)
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        Text(
                            text = AppsScriptTemplate.CODE,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                color = EmeraldSoft
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAppsScriptDialog = false },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Tutup", color = Slate700)
                        }
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(AppsScriptTemplate.CODE))
                                onCopiedToast("Kode Apps Script berhasil disalin!")
                                showAppsScriptDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Salin Kode", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantSheet(
    aiState: AiUiState,
    customers: List<CustomerEntity>,
    onAskKulakan: () -> Unit,
    onAskBillingMessage: (CustomerEntity?) -> Unit,
    onAskDailyReport: () -> Unit,
    onAskCustom: (String) -> Unit,
    onCopiedToast: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager = LocalClipboardManager.current
    var customQuestion by remember { mutableStateOf("") }
    val debtors = remember(customers) { customers.filter { it.remainingDebt > 0L } }
    var selectedDebtor by remember(debtors) { mutableStateOf(debtors.firstOrNull()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PearlWhite.copy(alpha = 0.96f),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(EmeraldPrimary, IndigoAi))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Asisten UMKM AI",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Slate900
                        )
                        Text(
                            text = "Kulakan cerdas, pesan WA sopan & analisis toko",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Tutup", tint = Slate700)
                }
            }

            // 3 Quick Instant Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AiActionChipCard(
                    title = "Susun Daftar Kulakan",
                    subtitle = "Otomatis cek stok menipis & buat daftar belanja distributor",
                    onClick = onAskKulakan,
                    modifier = Modifier.testTag("ai_chip_kulakan")
                )
                AiActionChipCard(
                    title = "Buat Pesan Tagihan Ramah",
                    subtitle = if (selectedDebtor != null) {
                        "Untuk ${selectedDebtor?.name} (${CurrencyAndDateUtils.formatRupiah(selectedDebtor?.remainingDebt ?: 0L)})"
                    } else {
                        "Susun template pengingat kasbon WhatsApp yang sopan"
                    },
                    onClick = { onAskBillingMessage(selectedDebtor) },
                    modifier = Modifier.testTag("ai_chip_tagihan")
                )
                if (debtors.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        debtors.forEach { d ->
                            val active = d.id == selectedDebtor?.id
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (active) EmeraldPrimary else Color.White)
                                    .border(1.dp, GlassBorderBright, CircleShape)
                                    .clickable { selectedDebtor = d }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = d.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active) Color.White else Slate700
                                )
                            }
                        }
                    }
                }
                AiActionChipCard(
                    title = "Ringkasan Performa Warung",
                    subtitle = "Analisis omzet, estimasi laba & produk paling laris hari ini",
                    onClick = onAskDailyReport,
                    modifier = Modifier.testTag("ai_chip_performa")
                )
            }

            // AI Output Area
            when (aiState) {
                is AiUiState.Idle -> Unit
                is AiUiState.Loading -> {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = EmeraldPrimary,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Menyiapkan ${aiState.actionLabel}...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate700
                            )
                        }
                    }
                }
                is AiUiState.Streaming -> {
                    AiResultCard(
                        title = aiState.actionLabel,
                        text = aiState.text,
                        isStreaming = true,
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(aiState.text))
                            onCopiedToast("Jawaban AI disalin!")
                        }
                    )
                }
                is AiUiState.Success -> {
                    AiResultCard(
                        title = aiState.actionLabel,
                        text = aiState.text,
                        isStreaming = false,
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(aiState.text))
                            onCopiedToast("Jawaban AI disalin!")
                        }
                    )
                }
                is AiUiState.Error -> {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        containerColor = CoralSoft.copy(alpha = 0.92f),
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Text(
                            text = aiState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CoralDanger
                        )
                    }
                }
            }

            // Custom Question Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customQuestion,
                    onValueChange = { customQuestion = it },
                    label = { Text("Tanya apa saja seputar warung...") },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (customQuestion.isNotBlank()) {
                            onAskCustom(customQuestion.trim())
                            customQuestion = ""
                        }
                    },
                    enabled = customQuestion.isNotBlank() && aiState !is AiUiState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.height(54.dp)
                ) {
                    Icon(Icons.Rounded.Send, contentDescription = "Kirim", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun AiActionChipCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = EmeraldDeep
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Slate700
            )
        }
    }
}

@Composable
private fun AiResultCard(
    title: String,
    text: String,
    isStreaming: Boolean,
    onCopy: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        containerColor = Color.White.copy(alpha = 0.92f),
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isStreaming) "$title (menulis...)" else title,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = IndigoAi
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(EmeraldSoft)
                        .clickable(onClick = onCopy)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "Salin",
                            tint = EmeraldDeep,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Salin",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldDeep
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = Slate900
            )
        }
    }
}
