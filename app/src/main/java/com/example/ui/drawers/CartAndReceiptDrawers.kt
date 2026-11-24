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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.local.CartItemPayload
import com.example.data.local.CustomerEntity
import com.example.data.local.StoreSettings
import com.example.data.local.TransactionEntity
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.AmberStatus
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.GlassBorderBright
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PearlWhite
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.util.CurrencyAndDateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartCheckoutSheet(
    cartItems: List<CartItemPayload>,
    cartTotal: Long,
    customers: List<CustomerEntity>,
    onUpdateQty: (Long, Int) -> Unit,
    onClearCart: () -> Unit,
    onCheckout: (
        paymentMethod: String,
        paidAmount: Long,
        customerId: Long?,
        newCustomerName: String?,
        newCustomerWhatsapp: String?
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var paymentMethod by remember { mutableStateOf("TUNAI") }
    var cashPaidText by remember(cartTotal) { mutableStateOf(cartTotal.toString()) }
    var selectedCustomerId by remember { mutableStateOf<Long?>(customers.firstOrNull()?.id) }
    var createNewCustomer by remember { mutableStateOf(customers.isEmpty()) }
    var newCustomerName by remember { mutableStateOf("") }
    var newCustomerWhatsapp by remember { mutableStateOf("") }
    var dpAmountText by remember { mutableStateOf("0") }

    val cashPaid = cashPaidText.toLongOrNull() ?: 0L
    val changeAmount = (cashPaid - cartTotal).coerceAtLeast(0L)
    val dpAmount = (dpAmountText.toLongOrNull() ?: 0L).coerceIn(0L, cartTotal)

    val canSubmit = when {
        cartItems.isEmpty() -> false
        paymentMethod == "TUNAI" -> cashPaid >= cartTotal
        else -> (selectedCustomerId != null && !createNewCustomer) || newCustomerName.isNotBlank()
    }

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
                Text(
                    text = "Keranjang Belanja",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Slate900
                )
                Row {
                    if (cartItems.isNotEmpty()) {
                        IconButton(onClick = onClearCart) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteSweep,
                                contentDescription = "Kosongkan Keranjang",
                                tint = CoralDanger
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Tutup",
                            tint = Slate700
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cartItems, key = { it.productId }) { item ->
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Slate900,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${CurrencyAndDateUtils.formatRupiah(item.sellPrice)} x ${item.quantity} = ${CurrencyAndDateUtils.formatRupiah(item.subtotal)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate700
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(1.dp, GlassBorderBright, CircleShape)
                                        .clickable { onUpdateQty(item.productId, item.quantity - 1) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Remove,
                                        contentDescription = "Kurangi",
                                        tint = Slate900,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "${item.quantity}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Slate900,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimary)
                                        .clickable { onUpdateQty(item.productId, item.quantity + 1) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = "Tambah",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Payment Method Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("TUNAI" to "Tunai", "KASBON" to "Kasbon (Utang)").forEach { (key, label) ->
                    val active = paymentMethod == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (active) EmeraldPrimary else Color.White)
                            .border(1.dp, if (active) EmeraldPrimary else GlassBorderBright, RoundedCornerShape(16.dp))
                            .clickable { paymentMethod = key }
                            .padding(vertical = 11.dp)
                            .testTag("payment_method_$key"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (active) Color.White else Slate700
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (paymentMethod == "TUNAI") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickOptions = listOf(
                        "Uang Pas" to cartTotal,
                        "Rp 20.000" to 20000L,
                        "Rp 50.000" to 50000L,
                        "Rp 100.000" to 100000L
                    )
                    quickOptions.forEach { (label, amount) ->
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(EmeraldSoft)
                                .clickable { cashPaidText = amount.toString() }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldDeep
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = cashPaidText,
                    onValueChange = { cashPaidText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Nominal Dibayar (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (cashPaid < cartTotal) "Kurang Bayar" else "Kembalian",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (cashPaid < cartTotal) CoralDanger else Slate700
                    )
                    Text(
                        text = if (cashPaid < cartTotal) {
                            CurrencyAndDateUtils.formatRupiah(cartTotal - cashPaid)
                        } else {
                            CurrencyAndDateUtils.formatRupiah(changeAmount)
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (cashPaid < cartTotal) CoralDanger else EmeraldDeep
                    )
                }
            } else {
                // KASBON Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pilih Pelanggan",
                        style = MaterialTheme.typography.labelLarge,
                        color = Slate900
                    )
                    Text(
                        text = if (createNewCustomer && customers.isNotEmpty()) "Pilih dari Daftar" else "+ Pelanggan Baru",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldPrimary,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                if (customers.isNotEmpty()) {
                                    createNewCustomer = !createNewCustomer
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (!createNewCustomer && customers.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        customers.forEach { customer ->
                            val selected = customer.id == selectedCustomerId
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (selected) EmeraldPrimary else Color.White)
                                    .border(1.dp, if (selected) EmeraldPrimary else GlassBorderBright, CircleShape)
                                    .clickable { selectedCustomerId = customer.id }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = customer.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) Color.White else Slate900
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = newCustomerName,
                        onValueChange = { newCustomerName = it },
                        label = { Text("Nama Pelanggan") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newCustomerWhatsapp,
                        onValueChange = { newCustomerWhatsapp = it },
                        label = { Text("No. WhatsApp (contoh: 0812...)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dpAmountText,
                    onValueChange = { dpAmountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Uang Muka / DP (Opsional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    val paid = if (paymentMethod == "TUNAI") cashPaid else dpAmount
                    onCheckout(
                        paymentMethod,
                        paid,
                        if (createNewCustomer) null else selectedCustomerId,
                        if (createNewCustomer) newCustomerName else null,
                        if (createNewCustomer) newCustomerWhatsapp else null
                    )
                },
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("checkout_submit_button")
            ) {
                Text(
                    text = "Bayar Sekarang • ${CurrencyAndDateUtils.formatRupiah(cartTotal)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ReceiptModalDialog(
    transaction: TransactionEntity,
    store: StoreSettings,
    onDownloadPdf: () -> Unit,
    onSendWhatsApp: () -> Unit,
    onDismiss: () -> Unit
) {
    val isTunai = transaction.paymentMethod == "TUNAI"
    Dialog(onDismissRequest = onDismiss) {
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White.copy(alpha = 0.96f),
            contentPadding = PaddingValues(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(EmeraldSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Struk Digital",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Slate900
                )
                Text(
                    text = transaction.invoiceNumber,
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = Slate500
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isTunai) EmeraldSoft else AmberSoft)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isTunai) "LUNAS (TUNAI)" else "KASBON (BELUM LUNAS)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isTunai) EmeraldDeep else AmberStatus
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Slate500.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    transaction.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.name} x${item.quantity}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate700,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = CurrencyAndDateUtils.formatRupiah(item.subtotal),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Slate900
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Slate500.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Tagihan", style = MaterialTheme.typography.titleSmall, color = Slate900)
                    Text(
                        CurrencyAndDateUtils.formatRupiah(transaction.totalAmount),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Slate900
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (isTunai) "Dibayar" else "Uang Muka", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    Text(CurrencyAndDateUtils.formatRupiah(transaction.paidAmount), style = MaterialTheme.typography.bodySmall, color = Slate700)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (isTunai) "Kembalian" else "Sisa Kasbon", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    Text(
                        CurrencyAndDateUtils.formatRupiah(transaction.changeOrRemaining),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isTunai) EmeraldDeep else AmberStatus
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                            .testTag("receipt_download_pdf")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PictureAsPdf,
                            contentDescription = null,
                            tint = EmeraldDeep,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PDF",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldDeep,
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = onSendWhatsApp,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("receipt_send_wa")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Send,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "WhatsApp",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                androidx.compose.material3.TextButton(onClick = onDismiss) {
                    Text("Tutup Struk", color = Slate500)
                }
            }
        }
    }
}
