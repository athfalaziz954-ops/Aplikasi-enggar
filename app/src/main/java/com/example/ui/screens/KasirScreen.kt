package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingCartCheckout
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.ui.theme.EmeraldVibrant
import com.example.ui.theme.GlassBorderBright
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.util.CurrencyAndDateUtils

@Composable
fun KasirScreen(
    products: List<ProductEntity>,
    cartQuantities: Map<Long, Int>,
    cartItemCount: Int,
    cartTotal: Long,
    flashProductId: Long?,
    onAddToCart: (ProductEntity) -> Unit,
    onOpenScanner: () -> Unit,
    onOpenCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }

    val categories by remember(products) {
        derivedStateOf {
            listOf("Semua") + products.map { it.category }.distinct().sorted()
        }
    }

    val filteredProducts by remember(products, searchQuery, selectedCategory) {
        derivedStateOf {
            products.filter { product ->
                val matchesCat = selectedCategory == "Semua" || product.category.equals(selectedCategory, ignoreCase = true)
                val q = searchQuery.trim()
                val matchesSearch = q.isEmpty() ||
                    product.name.contains(q, ignoreCase = true) ||
                    product.barcode.contains(q, ignoreCase = true)
                matchesCat && matchesSearch
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 156.dp),
            contentPadding = PaddingValues(
                start = 14.dp,
                end = 14.dp,
                top = 6.dp,
                bottom = if (cartItemCount > 0) 210.dp else 148.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Search & Scanner Bar
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LiquidGlassCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(22.dp),
                        containerColor = GlassSurfaceElevated,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = Slate500,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Cari produk atau barcode...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Slate400
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        color = Slate900,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    cursorBrush = SolidColor(EmeraldPrimary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("kasir_search_input")
                                )
                            }
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = stringResource(R.string.cd_clear_search),
                                        tint = Slate500,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    LiquidGlassCard(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("open_scanner_button"),
                        shape = RoundedCornerShape(22.dp),
                        containerColor = EmeraldPrimary,
                        onClick = onOpenScanner,
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.QrCodeScanner,
                            contentDescription = stringResource(R.string.cd_scan_barcode),
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Glass Category Pills
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val selected = category == selectedCategory
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (selected) EmeraldPrimary else Color.White.copy(alpha = 0.7f)
                                )
                                .border(
                                    1.dp,
                                    if (selected) EmeraldPrimary else GlassBorderBright,
                                    CircleShape
                                )
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 14.dp, vertical = 9.dp)
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) Color.White else Slate700
                            )
                        }
                    }
                }
            }

            if (filteredProducts.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        contentPadding = PaddingValues(28.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ShoppingBag,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Produk tidak ditemukan",
                                style = MaterialTheme.typography.titleMedium,
                                color = Slate900
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Coba kata kunci lain atau tambahkan produk baru di tab Stok.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500
                            )
                        }
                    }
                }
            } else {
                items(filteredProducts, key = { it.id }) { product ->
                    val qtyInCart = cartQuantities[product.id] ?: 0
                    val isFlash = flashProductId == product.id
                    ProductPosCard(
                        product = product,
                        qtyInCart = qtyInCart,
                        isFlashing = isFlash,
                        onTap = { onAddToCart(product) }
                    )
                }
            }
        }

        // Floating Cart Summary Bar above bottom navigation
        AnimatedVisibility(
            visible = cartItemCount > 0,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 100.dp)
        ) {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_cart_button"),
                shape = RoundedCornerShape(26.dp),
                containerColor = EmeraldDeep.copy(alpha = 0.94f),
                onClick = onOpenCart,
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ShoppingCartCheckout,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "$cartItemCount Barang di Keranjang",
                                style = MaterialTheme.typography.labelMedium,
                                color = EmeraldSoft
                            )
                            Text(
                                text = CurrencyAndDateUtils.formatRupiah(cartTotal),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Bayar",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldDeep
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductPosCard(
    product: ProductEntity,
    qtyInCart: Int,
    isFlashing: Boolean,
    onTap: () -> Unit
) {
    val outOfStock = product.stock <= 0
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pos_product_card_${product.id}"),
        shape = RoundedCornerShape(24.dp),
        highlightFlash = isFlashing,
        onClick = onTap,
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(EmeraldSoft.copy(alpha = 0.85f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = product.category,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = EmeraldDeep,
                        maxLines = 1
                    )
                }

                if (qtyInCart > 0) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(EmeraldPrimary, EmeraldVibrant))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$qtyInCart",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Slate900,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                minLines = 2
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = CurrencyAndDateUtils.formatRupiah(product.sellPrice),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Slate900
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val stockBg = when {
                    outOfStock -> CoralSoft
                    product.isLowStock -> AmberSoft
                    else -> Color.White.copy(alpha = 0.8f)
                }
                val stockFg = when {
                    outOfStock -> CoralDanger
                    product.isLowStock -> AmberStatus
                    else -> Slate700
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(stockBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (outOfStock) "Habis" else "Stok: ${product.stock}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = stockFg
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (outOfStock) Slate400.copy(alpha = 0.3f) else EmeraldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Tambah ${product.name}",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
