package com.example.ui.drawers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.local.CustomerEntity
import com.example.data.local.KasbonEntryEntity
import com.example.data.local.ProductEntity
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.AmberStatus
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.EmeraldVibrant
import com.example.ui.theme.PearlWhite
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.util.CurrencyAndDateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormSheet(
    existingProduct: ProductEntity?,
    onSave: (ProductEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember(existingProduct) { mutableStateOf(existingProduct?.name.orEmpty()) }
    var barcode by remember(existingProduct) {
        mutableStateOf(existingProduct?.barcode ?: "899${System.currentTimeMillis() % 10000000000L}")
    }
    var category by remember(existingProduct) { mutableStateOf(existingProduct?.category ?: "Sembako") }
    var costPriceText by remember(existingProduct) { mutableStateOf(existingProduct?.costPrice?.toString().orEmpty()) }
    var sellPriceText by remember(existingProduct) { mutableStateOf(existingProduct?.sellPrice?.toString().orEmpty()) }
    var stockText by remember(existingProduct) { mutableStateOf(existingProduct?.stock?.toString() ?: "10") }
    var minStockText by remember(existingProduct) { mutableStateOf(existingProduct?.minStock?.toString() ?: "5") }

    val canSave = name.isNotBlank() && (sellPriceText.toLongOrNull() ?: 0L) > 0L

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
                    text = if (existingProduct == null) "Tambah Produk Baru" else "Edit Produk",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Slate900
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Tutup", tint = Slate700)
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Produk") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("Kode Barcode") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1.2f)
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategori") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = costPriceText,
                    onValueChange = { costPriceText = it.filter { c -> c.isDigit() } },
                    label = { Text("Harga Modal (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = sellPriceText,
                    onValueChange = { sellPriceText = it.filter { c -> c.isDigit() } },
                    label = { Text("Harga Jual (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = stockText,
                    onValueChange = { stockText = it.filter { c -> c.isDigit() } },
                    label = { Text("Stok Saat Ini") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = minStockText,
                    onValueChange = { minStockText = it.filter { c -> c.isDigit() } },
                    label = { Text("Batas Stok Min") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = {
                    val product = ProductEntity(
                        id = existingProduct?.id ?: 0L,
                        name = name.trim(),
                        barcode = barcode.trim(),
                        category = category.trim().ifBlank { "Umum" },
                        costPrice = costPriceText.toLongOrNull() ?: 0L,
                        sellPrice = sellPriceText.toLongOrNull() ?: 0L,
                        stock = stockText.toIntOrNull() ?: 0,
                        minStock = minStockText.toIntOrNull() ?: 5
                    )
                    onSave(product)
                },
                enabled = canSave,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_product_button")
            ) {
                Text(
                    text = "Simpan Produk",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun AddCustomerDialog(
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            containerColor = Color.White.copy(alpha = 0.96f),
            contentPadding = PaddingValues(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Tambah Pelanggan Kasbon",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Slate900
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Pelanggan") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("No. WhatsApp (0812...)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal", color = Slate700)
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(name.trim(), whatsapp.trim())
                            }
                        },
                        enabled = name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Simpan", color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasbonDetailSheet(
    customer: CustomerEntity,
    entries: List<KasbonEntryEntity>,
    onRecordInstallment: (amount: Long, note: String) -> Unit,
    onDownloadPdf: () -> Unit,
    onTagihWhatsApp: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var paymentText by remember { mutableStateOf("") }
    var paymentNote by remember { mutableStateOf("Cicilan Tunai") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PearlWhite.copy(alpha = 0.96f),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Slate900
                    )
                    Text(
                        text = customer.whatsapp.ifBlank { "No. WhatsApp belum diisi" },
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Tutup", tint = Slate700)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Debt summary & progress
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Sisa Tagihan", style = MaterialTheme.typography.labelSmall, color = Slate500)
                            Text(
                                text = CurrencyAndDateUtils.formatRupiah(customer.remainingDebt),
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (customer.remainingDebt > 0) AmberStatus else EmeraldDeep
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Sudah Dibayar", style = MaterialTheme.typography.labelSmall, color = Slate500)
                            Text(
                                text = CurrencyAndDateUtils.formatRupiah(customer.paidDebt),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldDeep
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(customer.repaymentProgress.coerceIn(0.03f, 1f))
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(Brush.horizontalGradient(listOf(EmeraldPrimary, EmeraldVibrant)))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // PDF & Tagih WA buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDownloadPdf,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("kasbon_download_pdf")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PictureAsPdf,
                        contentDescription = null,
                        tint = EmeraldDeep,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download PDF", color = EmeraldDeep, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onTagihWhatsApp,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("kasbon_tagih_wa")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Send,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tagih via WA", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // Record Installment Form
            if (customer.remainingDebt > 0L) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Catat Cicilan / Pelunasan",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Slate900
                    )
                    Text(
                        text = "Lunasi Semua",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldPrimary,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(EmeraldSoft)
                            .clickable {
                                paymentText = customer.remainingDebt.toString()
                                paymentNote = "Pelunasan Kasbon"
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = paymentText,
                        onValueChange = { paymentText = it.filter { c -> c.isDigit() } },
                        label = { Text("Nominal (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            val amt = paymentText.toLongOrNull() ?: 0L
                            if (amt > 0L) {
                                onRecordInstallment(amt, paymentNote)
                                paymentText = ""
                            }
                        },
                        enabled = (paymentText.toLongOrNull() ?: 0L) > 0L,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("kasbon_pay_button")
                    ) {
                        Text("Bayar", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Riwayat Pengambilan & Cicilan",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Slate900
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 190.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    val isPayment = entry.type == "CICILAN"
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.note,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = Slate900
                                )
                                Text(
                                    text = CurrencyAndDateUtils.formatDateTime(entry.timestamp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate500
                                )
                            }
                            Text(
                                text = (if (isPayment) "- " else "+ ") + CurrencyAndDateUtils.formatRupiah(entry.amount),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (isPayment) EmeraldDeep else AmberStatus
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
