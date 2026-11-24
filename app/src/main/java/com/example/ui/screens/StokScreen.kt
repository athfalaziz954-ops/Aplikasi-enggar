package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.ProductEntity
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.AmberStatus
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.CoralSoft
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.GlassBorderBright
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.util.CurrencyAndDateUtils

@Composable
fun StokScreen(
    products: List<ProductEntity>,
    onAdjustStock: (Long, Int) -> Unit,
    onAddProduct: () -> Unit,
    onEditProduct: (ProductEntity) -> Unit,
    onDeleteProduct: (ProductEntity) -> Unit,
    onExportStockCsv: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterLowStockOnly by remember { mutableStateOf(false) }

    val lowStockCount by remember(products) {
        derivedStateOf { products.count { it.isLowStock } }
    }

    val displayedProducts by remember(products, searchQuery, filterLowStockOnly) {
        derivedStateOf {
            products.filter { p ->
                val matchesLow = !filterLowStockOnly || p.isLowStock
                val q = searchQuery.trim()
                val matchesQ = q.isEmpty() ||
                    p.name.contains(q, ignoreCase = true) ||
                    p.barcode.contains(q, ignoreCase = true) ||
                    p.category.contains(q, ignoreCase = true)
                matchesLow && matchesQ
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 156.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Low Stock Alert Glass Banner
        if (lowStockCount > 0) {
            item {
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("low_stock_banner"),
                    shape = RoundedCornerShape(22.dp),
                    containerColor = if (filterLowStockOnly) AmberStatus.copy(alpha = 0.22f) else AmberSoft.copy(alpha = 0.92f),
                    onClick = { filterLowStockOnly = !filterLowStockOnly },
                    contentPadding = PaddingValues(14.dp)
                ) {
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AmberStatus.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.WarningAmber,
                                    contentDescription = null,
                                    tint = AmberStatus,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "$lowStockCount Produk Stok Menipis!",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Slate900
                                )
                                Text(
                                    text = if (filterLowStockOnly) "Ketuk lagi untuk tampilkan semua stok"
                                    else "Ketuk untuk menyaring barang yang perlu kulakan",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate700
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (filterLowStockOnly) AmberStatus else Color.White)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (filterLowStockOnly) "Aktif" else "Filter",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (filterLowStockOnly) Color.White else AmberStatus
                            )
                        }
                    }
                }
            }
        }

        // Search + Add + Export CSV Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LiquidGlassCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    containerColor = GlassSurfaceElevated,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = Slate500,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Cari stok barang...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Slate400
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Slate900),
                                cursorBrush = SolidColor(EmeraldPrimary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                LiquidGlassCard(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("export_stock_csv_button"),
                    shape = RoundedCornerShape(20.dp),
                    containerColor = Color.White.copy(alpha = 0.85f),
                    onClick = onExportStockCsv,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = "Ekspor Stok CSV",
                        tint = EmeraldDeep,
                        modifier = Modifier.size(20.dp)
                    )
                }

                LiquidGlassCard(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("add_product_button"),
                    shape = RoundedCornerShape(20.dp),
                    containerColor = EmeraldPrimary,
                    onClick = onAddProduct,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.cd_add_product),
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tambah",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }

        if (displayedProducts.isEmpty()) {
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
                            imageVector = Icons.Rounded.Inventory2,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Belum ada produk sesuai filter",
                            style = MaterialTheme.typography.titleMedium,
                            color = Slate900
                        )
                    }
                }
            }
        } else {
            items(displayedProducts, key = { it.id }) { product ->
                StockProductCard(
                    product = product,
                    onPlusOne = { onAdjustStock(product.id, 1) },
                    onPlusFive = { onAdjustStock(product.id, 5) },
                    onEdit = { onEditProduct(product) },
                    onDelete = { onDeleteProduct(product) }
                )
            }
        }
    }
}

@Composable
private fun StockProductCard(
    product: ProductEntity,
    onPlusOne: () -> Unit,
    onPlusFive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(EmeraldSoft)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = product.category,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = EmeraldDeep
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = product.barcode,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = Slate500
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Modal ${CurrencyAndDateUtils.formatRupiah(product.costPrice)}  •  Jual ${CurrencyAndDateUtils.formatRupiah(product.sellPrice)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = Slate700
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit ${product.name}",
                            tint = Slate700,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Hapus ${product.name}",
                            tint = CoralDanger,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isCritical = product.stock <= 0
                val isLow = product.isLowStock
                val badgeBg = when {
                    isCritical -> CoralSoft
                    isLow -> AmberSoft
                    else -> EmeraldSoft.copy(alpha = 0.7f)
                }
                val badgeFg = when {
                    isCritical -> CoralDanger
                    isLow -> AmberStatus
                    else -> EmeraldDeep
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Stok: ${product.stock}  (Min ${product.minStock})",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = badgeFg
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.85f))
                            .border(1.dp, GlassBorderBright, CircleShape)
                            .clickable(onClick = onPlusOne)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("stock_plus_1_${product.id}")
                    ) {
                        Text(
                            text = "+1",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldDeep
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(EmeraldPrimary)
                            .clickable(onClick = onPlusFive)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("stock_plus_5_${product.id}")
                    ) {
                        Text(
                            text = "+5",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
