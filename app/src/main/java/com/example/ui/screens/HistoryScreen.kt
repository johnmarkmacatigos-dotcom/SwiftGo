package com.example.ui.screens

import android.text.format.DateUtils
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.HistoryEntity
import com.example.data.database.SavedPlaceEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.OneGoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: OneGoViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCompareRides: () -> Unit,
    onNavigateToCompareFood: () -> Unit,
    onNavigateToCompareDelivery: () -> Unit
) {
    val context = LocalContext.current
    val historyLog by viewModel.historyState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Aggregator Activity Log",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (historyLog.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.clearHistory()
                            Toast.makeText(context, "Search history cleared.", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear All",
                                tint = MaximRed
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = Modifier.testTag("history_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Reward Banner Card showing referral stats
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = GrabGreenLight)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GrabGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = "Rewards",
                            tint = GrabGreenDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Affiliate Referral Rewards",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = GrabGreenDark
                        )
                        Text(
                            text = "You receive +15 OneGo Points for every ride or food comparison booking made through the aggregator.",
                            fontSize = 10.sp,
                            color = GrabGreenDark.copy(alpha = 0.85f),
                            lineHeight = 13.sp
                        )
                    }
                }
            }

            Text(
                text = "Recent Comparisons",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            )

            if (historyLog.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.HistoryToggleOff,
                            contentDescription = "No searches",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No recent searches found.",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Fares you compare will appear here for one-click re-search.",
                            color = Color.Gray.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(historyLog) { entry ->
                        HistoryRowItem(
                            entry = entry,
                            onClick = {
                                // Load search into viewModel states
                                viewModel.selectOrigin(
                                    SavedPlaceEntity(
                                        name = entry.originName,
                                        address = "Stored Coordinate",
                                        latitude = entry.originLat,
                                        longitude = entry.originLng,
                                        iconName = "home"
                                    )
                                )
                                viewModel.selectDestination(
                                    SavedPlaceEntity(
                                        name = entry.destinationName,
                                        address = "Stored Coordinate",
                                        latitude = entry.destinationLat,
                                        longitude = entry.destinationLng,
                                        iconName = "star"
                                    )
                                )
                                viewModel.currentMode.value = entry.category

                                when (entry.category) {
                                    "RIDES" -> onNavigateToCompareRides()
                                    "FOOD" -> onNavigateToCompareFood()
                                    "DELIVERY" -> onNavigateToCompareDelivery()
                                    else -> onNavigateToCompareRides()
                                }

                                Toast.makeText(context, "Restoring search: ${entry.originName} ➔ ${entry.destinationName}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryRowItem(entry: HistoryEntity, onClick: () -> Unit) {
    val dateText = DateUtils.getRelativeTimeSpanString(
        entry.timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()

    val categoryColor = when (entry.category) {
        "RIDES" -> GrabGreen
        "FOOD" -> FoodpandaPink
        "DELIVERY" -> LalamoveOrange
        else -> GrabGreen
    }

    val categoryIcon = when (entry.category) {
        "RIDES" -> Icons.Default.DirectionsCar
        "FOOD" -> Icons.Default.Restaurant
        "DELIVERY" -> Icons.Default.LocalPostOffice
        else -> Icons.Default.DirectionsCar
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("history_item_${entry.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon with colored background
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = entry.category,
                    tint = categoryColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Booking Ref: #GO-${entry.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                    Text(
                        text = dateText,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${entry.originName} ➔ ${entry.destinationName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Via: ${entry.platformId.uppercase()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                    Text(
                        text = "•",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                    Text(
                        text = "Fare: ₱${"%.2f".format(entry.fare)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrabGreenDark
                    )
                    Text(
                        text = "•",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                    Text(
                        text = "+${entry.pointsEarned} pts",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFB300)
                    )
                }
            }
        }
    }
}
