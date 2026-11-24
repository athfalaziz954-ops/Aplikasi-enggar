package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.PointOfSale
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
import com.example.ui.theme.GlassSurfaceLight
import com.example.ui.theme.IndigoAi
import com.example.ui.theme.MintGlow
import com.example.ui.theme.PearlWhite
import com.example.ui.theme.SkyGlow
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.TealMint
import com.example.ui.viewmodel.ToastEvent
import kotlin.math.cos
import kotlin.math.sin

enum class WarungTab(val label: String, val icon: ImageVector, val testTag: String) {
    KASIR("Kasir", Icons.Rounded.PointOfSale, "tab_kasir"),
    STOK("Stok", Icons.Rounded.Inventory2, "tab_stok"),
    KASBON("Kasbon", Icons.Rounded.MenuBook, "tab_kasbon"),
    LAPORAN("Laporan", Icons.Rounded.Analytics, "tab_laporan")
}

@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_blobs")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blob_phase"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFECFDF5),
                        PearlWhite,
                        Color(0xFFF0F9FF)
                    )
                )
            )

            // Ambient Blob 1: Emerald Mint Top-Left
            val b1Center = Offset(
                x = size.width * (0.22f + 0.08f * cos(phase)),
                y = size.height * (0.14f + 0.06f * sin(phase))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(MintGlow.copy(alpha = 0.72f), Color.Transparent),
                    center = b1Center,
                    radius = size.width * 0.65f
                ),
                radius = size.width * 0.65f,
                center = b1Center
            )

            // Ambient Blob 2: Pale Sky Blue Mid-Right
            val b2Center = Offset(
                x = size.width * (0.84f + 0.09f * sin(phase * 0.8f)),
                y = size.height * (0.42f + 0.07f * cos(phase * 0.8f))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(SkyGlow.copy(alpha = 0.7f), Color.Transparent),
                    center = b2Center,
                    radius = size.width * 0.62f
                ),
                radius = size.width * 0.62f,
                center = b2Center
            )

            // Ambient Blob 3: Soft Teal Mint Bottom-Left
            val b3Center = Offset(
                x = size.width * (0.28f + 0.07f * sin(phase + 1.5f)),
                y = size.height * (0.82f + 0.06f * cos(phase + 1.5f))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(TealMint.copy(alpha = 0.75f), Color.Transparent),
                    center = b3Center,
                    radius = size.width * 0.70f
                ),
                radius = size.width * 0.70f,
                center = b3Center
            )
        }
        content()
    }
}

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    containerColor: Color = GlassSurfaceLight,
    highlightFlash: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tactile_scale"
    )
    val bgColor by animateColorAsState(
        targetValue = if (highlightFlash) EmeraldSoft.copy(alpha = 0.92f) else containerColor,
        animationSpec = tween(250),
        label = "flash_bg"
    )

    val borderBrush = Brush.verticalGradient(
        colors = listOf(
            if (highlightFlash) EmeraldVibrant else GlassBorderBright,
            Color.White.copy(alpha = 0.45f)
        )
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 10.dp,
                shape = shape,
                ambientColor = EmeraldPrimary.copy(alpha = 0.09f),
                spotColor = Slate900.copy(alpha = 0.08f)
            )
            .clip(shape)
            .background(bgColor)
            .border(width = 1.2.dp, brush = borderBrush, shape = shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = androidx.compose.material3.ripple(color = EmeraldPrimary),
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun FloatingHeaderCapsule(
    storeName: String,
    isOnline: Boolean,
    pendingCount: Int,
    isSyncing: Boolean,
    onToggleOffline: () -> Unit,
    onSyncClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
    val syncRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sync_angle"
    )

    val statusBg = if (isOnline) EmeraldSoft.copy(alpha = 0.9f) else AmberSoft.copy(alpha = 0.95f)
    val statusFg = if (isOnline) EmeraldDeep else AmberStatus

    LiquidGlassCard(
        modifier = modifier
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        containerColor = GlassSurfaceElevated,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = storeName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Slate900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(statusBg)
                        .clickable(onClick = onToggleOffline)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("network_status_toggle"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isOnline) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                        contentDescription = null,
                        tint = statusFg,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = when {
                            !isOnline -> stringResource(R.string.status_offline_local)
                            pendingCount > 0 -> stringResource(R.string.status_online_pending, pendingCount)
                            else -> stringResource(R.string.status_online_synced)
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = statusFg,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Animated Sync Button
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (pendingCount > 0) EmeraldPrimary else Color.White.copy(alpha = 0.78f)
                        )
                        .border(
                            1.dp,
                            if (pendingCount > 0) EmeraldPrimary else GlassBorderBright,
                            CircleShape
                        )
                        .clickable(enabled = !isSyncing, onClick = onSyncClick)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .testTag("sync_header_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Sync,
                            contentDescription = stringResource(R.string.cd_sync_data),
                            tint = if (pendingCount > 0) Color.White else EmeraldDeep,
                            modifier = Modifier
                                .size(16.dp)
                                .rotate(if (isSyncing) syncRotation else 0f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (pendingCount > 0) "$pendingCount" else "Sync",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (pendingCount > 0) Color.White else EmeraldDeep
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = stringResource(R.string.cd_settings),
                        tint = Slate700
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingLiquidBottomNav(
    selectedTab: WarungTab,
    onSelectTab: (WarungTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = WarungTab.entries
    val selectedIndex = tabs.indexOf(selectedTab).coerceAtLeast(0)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            shape = RoundedCornerShape(34.dp),
            containerColor = GlassSurfaceElevated,
            contentPadding = PaddingValues(6.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val tabWidth: Dp = maxWidth / tabs.size
                val indicatorOffset by animateDpAsState(
                    targetValue = tabWidth * selectedIndex,
                    animationSpec = spring(
                        dampingRatio = 0.72f,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "liquid_pill_offset"
                )

                // Sliding Emerald Liquid Pill
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .width(tabWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(EmeraldPrimary, EmeraldVibrant)
                            )
                        )
                )

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEach { tab ->
                        val isSelected = tab == selectedTab
                        val contentColor by animateColorAsState(
                            targetValue = if (isSelected) Color.White else Slate700,
                            animationSpec = tween(220),
                            label = "tab_tint"
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(28.dp))
                                .clickable { onSelectTab(tab) }
                                .testTag(tab.testTag),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = contentColor,
                                modifier = Modifier.size(21.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                color = contentColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingAiButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag("ai_assistant_fab"),
        shape = CircleShape,
        containerColor = Color.White.copy(alpha = 0.88f),
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(EmeraldPrimary, IndigoAi)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = stringResource(R.string.ai_assistant_fab),
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.ai_assistant_fab),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = Slate900
            )
        }
    }
}

@Composable
fun GlassToastBanner(
    toast: ToastEvent?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = toast != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        if (toast != null) {
            LiquidGlassCard(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                containerColor = if (toast.isError) CoralSoft.copy(alpha = 0.94f) else Color.White.copy(alpha = 0.94f),
                onClick = onDismiss,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (toast.isError) CoralDanger else EmeraldPrimary)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = toast.message,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (toast.isError) CoralDanger else Slate900,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
