package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.CustomerEntity
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.AmberStatus
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.EmeraldVibrant
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.util.CurrencyAndDateUtils

@Composable
fun KasbonScreen(
    customers: List<CustomerEntity>,
    onSelectCustomer: (CustomerEntity) -> Unit,
    onAddCustomerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalRemaining by remember(customers) {
        derivedStateOf { customers.sumOf { it.remainingDebt } }
    }
    val totalPaid by remember(customers) {
        derivedStateOf { customers.sumOf { it.paidDebt } }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 156.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary Glass Header
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                containerColor = GlassSurfaceElevated,
                contentPadding = PaddingValues(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Piutang Aktif",
                                style = MaterialTheme.typography.labelMedium,
                                color = Slate500
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = CurrencyAndDateUtils.formatRupiah(totalRemaining),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = Slate900
                            )
                        }

                        LiquidGlassCard(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("add_customer_button"),
                            shape = CircleShape,
                            containerColor = EmeraldPrimary,
                            onClick = onAddCustomerClick,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = stringResource(R.string.cd_add_customer),
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Pelanggan",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(EmeraldSoft.copy(alpha = 0.7f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Sudah Tertagih / Dibayar: ${CurrencyAndDateUtils.formatRupiah(totalPaid)}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = EmeraldDeep
                        )
                    }
                }
            }
        }

        if (customers.isEmpty()) {
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(28.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MenuBook,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(46.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Buku Kasbon Masih Kosong",
                            style = MaterialTheme.typography.titleMedium,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Catat transaksi kasbon langsung dari Kasir atau tambahkan pelanggan baru di atas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                }
            }
        } else {
            items(customers, key = { it.id }) { customer ->
                KasbonCustomerCard(
                    customer = customer,
                    onClick = { onSelectCustomer(customer) }
                )
            }
        }
    }
}

@Composable
private fun KasbonCustomerCard(
    customer: CustomerEntity,
    onClick: () -> Unit
) {
    val isSettled = customer.remainingDebt <= 0L
    val progress = customer.repaymentProgress
    val percent = (progress * 100).toInt()

    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("kasbon_customer_card_${customer.id}"),
        shape = RoundedCornerShape(24.dp),
        onClick = onClick,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isSettled) EmeraldSoft else AmberSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = if (isSettled) EmeraldDeep else AmberStatus,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate900
                        )
                        Text(
                            text = customer.whatsapp.ifBlank { "Tanpa nomor WA" },
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSettled) EmeraldSoft else AmberSoft)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isSettled) "LUNAS" else "BELUM LUNAS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSettled) EmeraldDeep else AmberStatus
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyAndDateUtils.formatRupiah(customer.remainingDebt),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Slate900
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Liquid Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Progres Pelunasan",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate700
                )
                Text(
                    text = "$percent% (${CurrencyAndDateUtils.formatRupiah(customer.paidDebt)} / ${CurrencyAndDateUtils.formatRupiah(customer.totalDebt)})",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Slate900
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.75f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progress.coerceIn(0.03f, 1f))
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                if (isSettled) listOf(EmeraldPrimary, EmeraldVibrant)
                                else listOf(AmberStatus, EmeraldPrimary)
                            )
                        )
                )
            }
        }
    }
}
