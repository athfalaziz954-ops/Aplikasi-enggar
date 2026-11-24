package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.TransactionEntity
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.AmberStatus
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.EmeraldVibrant
import com.example.ui.theme.GlassBorderBright
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.util.CurrencyAndDateUtils

private enum class SyncFilter(val label: String) {
    ALL("Semua"),
    SYNCED("Tersinkronisasi"),
    PENDING("Tertunda Offline")
}

private data class TopProductStat(
    val name: String,
    val qty: Int,
    val revenue: Long
)

@Composable
fun LaporanScreen(
    transactions: List<TransactionEntity>,
    isSyncing: Boolean,
    onSyncToSheets: () -> Unit,
    onDownloadInvoicePdf: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var syncFilter by remember { mutableStateOf(SyncFilter.ALL) }
    var animateTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animateTrigger = true }

    val startOfToday = remember { CurrencyAndDateUtils.startOfTodayMillis() }

    val todayStats by remember(transactions) {
        derivedStateOf {
            val today = transactions.filter { it.timestamp >= startOfToday }
            val omzet = today.sumOf { it.totalAmount }
            val laba = today.sumOf { it.estimatedProfit }
            val tunaiCount = today.count { it.paymentMethod == "TUNAI" }
            val kasbonBaru = today.filter { it.paymentMethod == "KASBON" }.sumOf { it.changeOrRemaining }
            listOf(omzet, laba, tunaiCount.toLong(), kasbonBaru)
        }
    }

    val topProducts by remember(transactions) {
        derivedStateOf {
            transactions
                .flatMap { it.items }
                .groupBy { it.name }
                .map { (name, items) ->
                    TopProductStat(
                        name = name,
                        qty = items.sumOf { it.quantity },
                        revenue = items.sumOf { it.subtotal }
                    )
                }
                .sortedByDescending { it.qty }
                .take(5)
        }
    }

    var selectedBarIndex by remember { mutableIntStateOf(0) }

    val filteredTransactions by remember(transactions, syncFilter) {
        derivedStateOf {
            when (syncFilter) {
                SyncFilter.ALL -> transactions
                SyncFilter.SYNCED -> transactions.filter { it.isSynced }
                SyncFilter.PENDING -> transactions.filter { !it.isSynced }
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 156.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 4 Animated KPI Glass Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnimatedKpiCard(
                        title = "Omzet Hari Ini",
                        targetValue = if (animateTrigger) todayStats[0].toInt() else 0,
                        isCurrency = true,
                        accentColor = EmeraldPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    AnimatedKpiCard(
                        title = "Estimasi Laba",
                        targetValue = if (animateTrigger) todayStats[1].toInt() else 0,
                        isCurrency = true,
                        accentColor = SkyAccent,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnimatedKpiCard(
                        title = "Transaksi Tunai",
                        targetValue = if (animateTrigger) todayStats[2].toInt() else 0,
                        isCurrency = false,
                        suffix = " trx",
                        accentColor = EmeraldDeep,
                        modifier = Modifier.weight(1f)
                    )
                    AnimatedKpiCard(
                        title = "Kasbon Baru",
                        targetValue = if (animateTrigger) todayStats[3].toInt() else 0,
                        isCurrency = true,
                        accentColor = AmberStatus,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Interactive Top 5 Best-Selling Products Bar Chart
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "5 Produk Terlaris",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Slate900
                    )
                    Text(
                        text = "Ketuk batang grafik untuk lihat rincian unit & omzet",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (topProducts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Belum ada data penjualan. Lakukan transaksi di Kasir!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500
                            )
                        }
                    } else {
                        val safeIndex = selectedBarIndex.coerceIn(0, topProducts.lastIndex)
                        val selectedStat = topProducts[safeIndex]
                        val maxQty = topProducts.maxOf { it.qty }.coerceAtLeast(1)

                        // Interactive Canvas Bar Chart
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .pointerInput(topProducts) {
                                    detectTapGestures { offset ->
                                        val slotWidth = size.width / topProducts.size.toFloat()
                                        val tapped = (offset.x / slotWidth).toInt().coerceIn(0, topProducts.lastIndex)
                                        selectedBarIndex = tapped
                                    }
                                }
                        ) {
                            val count = topProducts.size
                            val slotWidth = size.width / count.toFloat()
                            val barWidth = (slotWidth * 0.54f).coerceAtMost(48.dp.toPx())
                            val maxBarHeight = size.height - 16.dp.toPx()

                            topProducts.forEachIndexed { index, stat ->
                                val ratio = stat.qty.toFloat() / maxQty.toFloat()
                                val barHeight = (maxBarHeight * ratio).coerceAtLeast(14.dp.toPx())
                                val left = index * slotWidth + (slotWidth - barWidth) / 2f
                                val top = size.height - barHeight
                                val isSelected = index == safeIndex

                                val brush = if (isSelected) {
                                    Brush.verticalGradient(listOf(EmeraldPrimary, EmeraldVibrant))
                                } else {
                                    Brush.verticalGradient(
                                        listOf(
                                            EmeraldPrimary.copy(alpha = 0.35f),
                                            EmeraldSoft.copy(alpha = 0.7f)
                                        )
                                    )
                                }

                                drawRoundRect(
                                    brush = brush,
                                    topLeft = Offset(left, top),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Product short labels beneath bars
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            topProducts.forEachIndexed { index, stat ->
                                val isSelected = index == safeIndex
                                Text(
                                    text = stat.name.split(" ").firstOrNull().orEmpty(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) EmeraldDeep else Slate500,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedBarIndex = index }
                                        .padding(horizontal = 2.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Tapped Bar Detail Pill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(EmeraldSoft.copy(alpha = 0.7f))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${selectedStat.name} (${selectedStat.qty} terjual)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Slate900
                                )
                                Text(
                                    text = CurrencyAndDateUtils.formatRupiah(selectedStat.revenue),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldDeep
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sync to Google Sheets Action Button
        item {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("laporan_sync_sheets_button"),
                shape = RoundedCornerShape(22.dp),
                containerColor = EmeraldPrimary,
                onClick = onSyncToSheets,
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Sync,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSyncing) "Sedang Menyinkronkan..." else "Sinkronkan ke Google Sheets",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }

        // Transaction History Filter Tabs
        item {
            Column {
                Text(
                    text = "Riwayat Transaksi",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SyncFilter.entries.forEach { filter ->
                        val active = filter == syncFilter
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (active) EmeraldPrimary else Color.White.copy(alpha = 0.72f))
                                .border(1.dp, if (active) EmeraldPrimary else GlassBorderBright, CircleShape)
                                .clickable { syncFilter = filter }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (active) Color.White else Slate700
                            )
                        }
                    }
                }
            }
        }

        if (filteredTransactions.isEmpty()) {
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Analytics,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Belum ada riwayat transaksi",
                            style = MaterialTheme.typography.titleSmall,
                            color = Slate900
                        )
                    }
                }
            }
        } else {
            items(filteredTransactions, key = { it.id }) { tx ->
                TransactionHistoryCard(
                    transaction = tx,
                    onDownloadPdf = { onDownloadInvoicePdf(tx) }
                )
            }
        }
    }
}

@Composable
private fun AnimatedKpiCard(
    title: String,
    targetValue: Int,
    isCurrency: Boolean,
    accentColor: Color,
    suffix: String = "",
    modifier: Modifier = Modifier
) {
    val animatedVal by animateIntAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "kpi_count_up"
    )

    LiquidGlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Slate500
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isCurrency) CurrencyAndDateUtils.formatRupiah(animatedVal.toLong()) else "$animatedVal$suffix",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Slate900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TransactionHistoryCard(
    transaction: TransactionEntity,
    onDownloadPdf: () -> Unit
) {
    val isTunai = transaction.paymentMethod == "TUNAI"
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.invoiceNumber,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Slate900
                    )
                    Text(
                        text = "${CurrencyAndDateUtils.formatDateTime(transaction.timestamp)} • ${transaction.customerName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (transaction.isSynced) Icons.Rounded.CloudDone else Icons.Rounded.CloudQueue,
                        contentDescription = if (transaction.isSynced) "Tersinkronisasi" else "Tertunda Offline",
                        tint = if (transaction.isSynced) EmeraldPrimary else AmberStatus,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isTunai) EmeraldSoft else AmberSoft)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isTunai) "LUNAS" else "KASBON",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isTunai) EmeraldDeep else AmberStatus
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = transaction.items.joinToString(", ") { "${it.name} x${it.quantity}" },
                style = MaterialTheme.typography.bodySmall,
                color = Slate700,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = CurrencyAndDateUtils.formatRupiah(transaction.totalAmount),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Slate900
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.88f))
                        .border(1.dp, GlassBorderBright, CircleShape)
                        .clickable(onClick = onDownloadPdf)
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                        .testTag("laporan_pdf_button_${transaction.id}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.PictureAsPdf,
                            contentDescription = "Download Invoice PDF",
                            tint = EmeraldDeep,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Invoice PDF",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldDeep
                        )
                    }
                }
            }
        }
    }
}
