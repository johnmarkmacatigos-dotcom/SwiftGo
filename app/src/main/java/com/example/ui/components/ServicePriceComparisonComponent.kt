package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class PriceSortMode {
    PRICE, // Sort by lowest fare
    ETA    // Sort by fastest arrival
}

/**
 * Interactive Sort Toggle Component for comparing transportation / delivery provider rates
 */
@Composable
fun ServicePriceSortToggle(
    currentSortMode: PriceSortMode,
    onSortModeChange: (PriceSortMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sort_toggle_group"),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Price Toggle Pill
            val isPriceSelected = currentSortMode == PriceSortMode.PRICE
            val priceBgColor by animateColorAsState(
                if (isPriceSelected) GrabGreen else Color.Transparent,
                label = "priceBg"
            )
            val priceTextColor by animateColorAsState(
                if (isPriceSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "priceText"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(priceBgColor)
                    .clickable { onSortModeChange(PriceSortMode.PRICE) }
                    .testTag("sort_toggle_price"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sell,
                        contentDescription = "Sort by Price",
                        tint = priceTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sort by Price 🏷️",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = priceTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // ETA Toggle Pill
            val isEtaSelected = currentSortMode == PriceSortMode.ETA
            val etaBgColor by animateColorAsState(
                if (isEtaSelected) GrabGreen else Color.Transparent,
                label = "etaBg"
            )
            val etaTextColor by animateColorAsState(
                if (isEtaSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "etaText"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(etaBgColor)
                    .clickable { onSortModeChange(PriceSortMode.ETA) }
                    .testTag("sort_toggle_eta"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Sort by ETA",
                        tint = etaTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sort by ETA ⚡",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = etaTextColor
                    )
                }
            }
        }
    }
}

/**
 * Summary metrics card highlighting best price and fastest ETA across providers
 */
@Composable
fun ServiceComparisonSummaryHeader(
    cheapestProviderName: String,
    cheapestPrice: Double,
    fastestProviderName: String,
    fastestEtaMins: Int,
    maxPriceDifference: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GrabGreenLight.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, GrabGreen.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CompareArrows,
                        contentDescription = "Compare Services",
                        tint = GrabGreenDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PROVIDER COMPARISON SUMMARY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = GrabGreenDark
                    )
                }

                if (maxPriceDifference > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GrabGreen
                    ) {
                        Text(
                            text = "Save up to ₱${"%.0f".format(maxPriceDifference)}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cheapest Badge Box
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(GrabGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏷️", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("CHEAPEST", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = GrabGreenDark)
                            Text(
                                text = cheapestProviderName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "₱${"%.2f".format(cheapestPrice)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = GrabGreenDark
                            )
                        }
                    }
                }

                // Fastest Badge Box
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚡", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("FASTEST PICKUP", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = fastestProviderName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "$fastestEtaMins mins arrival",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Visual relative price meter bar for provider cards
 */
@Composable
fun RelativePriceSpectrumBar(
    currentPrice: Double,
    minPrice: Double,
    maxPrice: Double,
    modifier: Modifier = Modifier
) {
    val fraction = remember(currentPrice, minPrice, maxPrice) {
        if (maxPrice <= minPrice) 0.5f
        else ((currentPrice - minPrice) / (maxPrice - minPrice)).coerceIn(0.0, 1.0).toFloat()
    }

    val barColor = when {
        fraction < 0.33f -> GrabGreen
        fraction < 0.66f -> Color(0xFFFFA726) // Orange
        else -> MaximRed
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = when {
                    fraction < 0.1f -> "Lowest Fare Available"
                    fraction < 0.4f -> "Competitive Rate"
                    else -> "Premium / Rush Fare"
                },
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
            Text(
                text = "${(fraction * 100).toInt()}% price index",
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction.coerceAtLeast(0.08f))
                    .clip(CircleShape)
                    .background(barColor)
            )
        }
    }
}

/**
 * Clean Comparison Sheet Container layout without map view.
 */
@Composable
fun DraggableComparisonSheetContainer(
    modifier: Modifier = Modifier,
    headerPanel: @Composable () -> Unit,
    mapVisionContent: (@Composable (isWideVision: Boolean) -> Unit)? = null,
    ratesSheetContent: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("draggable_comparison_container")
    ) {
        // Top Header Panel
        headerPanel()

        // Rates Comparison Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            ratesSheetContent()
        }
    }
}
