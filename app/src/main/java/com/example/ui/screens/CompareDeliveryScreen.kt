package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.data.database.SavedPlaceEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeliveryOption
import com.example.ui.viewmodel.OneGoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareDeliveryScreen(
    viewModel: OneGoViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val origin by viewModel.selectedOrigin.collectAsStateWithLifecycle()
    val destination by viewModel.selectedDestination.collectAsStateWithLifecycle()
    val savedPlaces by viewModel.savedPlacesState.collectAsStateWithLifecycle()
    val isRaining by viewModel.isRainingState.collectAsStateWithLifecycle()
    val isRushHour by viewModel.isRushHourState.collectAsStateWithLifecycle()

    var showOriginPicker by remember { mutableStateOf(false) }
    var showDestPicker by remember { mutableStateOf(false) }
    var showPinMapForPickup by remember { mutableStateOf(false) }
    var showPinMapForDest by remember { mutableStateOf(false) }

    val selectedTypeIdx by viewModel.deliveryPackageTypeIndex.collectAsStateWithLifecycle()
    var selectedRegionFilter by remember { mutableStateOf(0) } // 0 = Local PH, 1 = Asia-Wide, 2 = Combined All
    var priceSortMode by remember { mutableStateOf(PriceSortMode.PRICE) }
    var activeOptionForBooking by remember { mutableStateOf<com.example.ui.viewmodel.DeliveryOption?>(null) }

    val comparedDeliveryOptions = remember(selectedTypeIdx, origin, destination, isRaining, isRushHour) {
        viewModel.getComparedDelivery()
    }
    val filteredDeliveryOptions = remember(comparedDeliveryOptions, selectedRegionFilter) {
        when (selectedRegionFilter) {
            0 -> comparedDeliveryOptions.filter { it.platformId in listOf("grabexpress", "lalamove_moto", "lalamove_car", "maxim_delivery", "ninjavan", "jtexpress") }
            1 -> comparedDeliveryOptions.filter { it.platformId in listOf("gojek_logistics", "deliveree", "yamato") }
            else -> comparedDeliveryOptions
        }
    }
    val sortedFilteredDeliveryOptions = remember(filteredDeliveryOptions, priceSortMode) {
        when (priceSortMode) {
            PriceSortMode.PRICE -> filteredDeliveryOptions.sortedBy { it.estimatedFare }
            PriceSortMode.ETA -> filteredDeliveryOptions.sortedBy { it.etaMins }
        }
    }
    val cheapestDelivery = remember(sortedFilteredDeliveryOptions) { sortedFilteredDeliveryOptions.minByOrNull { it.estimatedFare } }
    val fastestDelivery = remember(sortedFilteredDeliveryOptions) { sortedFilteredDeliveryOptions.minByOrNull { it.etaMins } }
    val maxDeliveryPriceDiff = remember(sortedFilteredDeliveryOptions) {
        val minP = sortedFilteredDeliveryOptions.minOfOrNull { it.estimatedFare } ?: 0.0
        val maxP = sortedFilteredDeliveryOptions.maxOfOrNull { it.estimatedFare } ?: 0.0
        (maxP - minP).coerceAtLeast(0.0)
    }
    val distanceKm = remember(origin, destination) {
        viewModel.getCalculatedDistanceKm()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Compare Logistics",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = Modifier.testTag("compare_delivery_screen")
    ) { innerPadding ->
        DraggableComparisonSheetContainer(
            modifier = Modifier.padding(innerPadding),
            headerPanel = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Origin -> Destination Route Details
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TripOrigin,
                                    contentDescription = "Pickup Location",
                                    tint = GrabGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { showOriginPicker = true }
                                ) {
                                    Text("PICKUP LOCATION", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = GrabGreen)
                                    Text(
                                        text = origin?.name ?: "Search pickup location…",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (origin != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    if (origin != null) {
                                        IconButton(
                                            onClick = { viewModel.selectOrigin(SavedPlaceEntity(name="", address="", latitude=0.0, longitude=0.0, iconName="")) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear Pickup",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { showOriginPicker = true },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Map,
                                            contentDescription = "Search Pickup Map",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { showPinMapForPickup = true },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = "Pin Pickup Location",
                                            tint = MaximRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Dropoff Location",
                                    tint = MaximRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { showDestPicker = true }
                                ) {
                                    Text("DESTINATION (DROPOFF)", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = MaximRed)
                                    Text(
                                        text = destination?.name ?: "Where to?",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (destination != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    if (destination != null) {
                                        IconButton(
                                            onClick = { viewModel.selectDestination(SavedPlaceEntity(name="", address="", latitude=0.0, longitude=0.0, iconName="")) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear Destination",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { showDestPicker = true },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Map,
                                            contentDescription = "Search Destination Map",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { showPinMapForDest = true },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = "Pin Destination Location",
                                            tint = MaximRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Package Type selector
                    Text(
                        text = "What are you shipping?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 110.dp)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(viewModel.packageTypes) { index, typeName ->
                            val isSelected = index == selectedTypeIdx
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.deliveryPackageTypeIndex.value = index },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) GrabGreenLight else MaterialTheme.colorScheme.surface
                                ),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, GrabGreen) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { viewModel.deliveryPackageTypeIndex.value = index },
                                            colors = RadioButtonDefaults.colors(selectedColor = GrabGreen)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = typeName,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                    Icon(
                                        imageVector = if (index == 3) Icons.Default.LocalShipping else Icons.Default.CardGiftcard,
                                        contentDescription = null,
                                        tint = if (isSelected) GrabGreen else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            ratesSheetContent = {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Region Filter Selection TabRow
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            FilterChip(
                                selected = selectedRegionFilter == 0,
                                onClick = { selectedRegionFilter = 0 },
                                label = { Text("🇵🇭 Local (PH)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GrabGreenLight,
                                    selectedLabelColor = GrabGreenDark
                                ),
                                modifier = Modifier.testTag("filter_region_ph")
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedRegionFilter == 1,
                                onClick = { selectedRegionFilter = 1 },
                                label = { Text("🌏 Asia-Wide", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GrabGreenLight,
                                    selectedLabelColor = GrabGreenDark
                                ),
                                modifier = Modifier.testTag("filter_region_asia")
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedRegionFilter == 2,
                                onClick = { selectedRegionFilter = 2 },
                                label = { Text("🔗 Combined All", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GrabGreenLight,
                                    selectedLabelColor = GrabGreenDark
                                ),
                                modifier = Modifier.testTag("filter_region_all")
                            )
                        }
                    }



                    // Logistics options title
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (priceSortMode == PriceSortMode.PRICE) "Delivery Rates (Cheapest First)" else "Delivery Rates (Fastest First)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${sortedFilteredDeliveryOptions.size} Platforms",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Results Log
                    if (sortedFilteredDeliveryOptions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.ReportProblem,
                                    contentDescription = "No vehicles available",
                                    tint = MaximRed,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No logistics vehicle matches filter/cargo!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Try switching your region filter or selecting standard package weights.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(sortedFilteredDeliveryOptions) { option ->
                                DeliveryOptionCard(
                                    option = option,
                                    onOpenApp = {
                                        viewModel.launchPlatform(
                                            context = context,
                                            platformId = option.platformId,
                                            platformName = option.platformName,
                                            price = option.estimatedFare,
                                            customDeepLinkUrl = option.deepLinkUrl
                                        )
                                    },
                                    onInstantBook = {
                                        activeOptionForBooking = option
                                    }
                                )
                            }
                        }
                    }
                }
            }
        )

        activeOptionForBooking?.let { option ->
            InstantBookingDialog(
                visible = true,
                platformId = option.platformId,
                platformName = option.platformName,
                serviceName = option.serviceName,
                estimatedFare = option.estimatedFare,
                category = "DELIVERY",
                viewModel = viewModel,
                onDismiss = { activeOptionForBooking = null }
            )
        }

        if (showPinMapForPickup) {
            PinLocationMapDialog(
                dialogTitle = "Pin Pick-up Location",
                isPickup = true,
                initialLat = origin?.latitude ?: 10.7123,
                initialLng = origin?.longitude ?: 122.5518,
                onDismiss = { showPinMapForPickup = false },
                onConfirmPin = { pinnedPlace ->
                    viewModel.selectOrigin(pinnedPlace)
                    showPinMapForPickup = false
                }
            )
        }

        if (showPinMapForDest) {
            PinLocationMapDialog(
                dialogTitle = "Pin Destination Location",
                isPickup = false,
                initialLat = destination?.latitude ?: 10.7123,
                initialLng = destination?.longitude ?: 122.5518,
                onDismiss = { showPinMapForDest = false },
                onConfirmPin = { pinnedPlace ->
                    viewModel.selectDestination(pinnedPlace)
                    showPinMapForDest = false
                }
            )
        }

        if (showOriginPicker) {
            LocationPickerDialog(
                title = "Select Pickup Location",
                places = savedPlaces,
                onDismiss = { showOriginPicker = false },
                onSelect = {
                    viewModel.selectOrigin(it)
                    showOriginPicker = false
                },
                viewModel = viewModel
            )
        }

        if (showDestPicker) {
            LocationPickerDialog(
                title = "Select Destination",
                places = savedPlaces,
                onDismiss = { showDestPicker = false },
                onSelect = {
                    viewModel.selectDestination(it)
                    showDestPicker = false
                },
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun DeliveryOptionCard(
    option: com.example.ui.viewmodel.DeliveryOption,
    onOpenApp: () -> Unit,
    onInstantBook: () -> Unit
) {
    val platformAccentColor = when {
        option.platformId.startsWith("grab") -> GrabGreen
        option.platformId.startsWith("lalamove") -> LalamoveOrange
        option.platformId.startsWith("maxim") -> MaximRed
        option.platformId.startsWith("gojek") -> GojekGreen
        option.platformId.startsWith("ninjavan") -> NinjaVanRed
        option.platformId.startsWith("jtexpress") -> JtExpressRed
        option.platformId.startsWith("deliveree") -> DelivereeBlue
        option.platformId.startsWith("yamato") -> YamatoGreen
        else -> GrabGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("delivery_option_${option.platformId}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Platform logistics icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(platformAccentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (option.serviceName.contains("Sedan")) Icons.Default.LocalShipping else Icons.Default.ElectricBike,
                        contentDescription = option.platformName,
                        tint = platformAccentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Logistics details
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = option.platformName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = platformAccentColor
                        )

                        option.badgeLabel?.let { badge ->
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(GrabGreenLight)
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badge,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GrabGreenDark
                                )
                            }
                        }
                    }

                    Text(
                        text = option.serviceName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )

                    Text(
                        text = "Capacity: up to ${option.maxWeightKg.toInt()} kg",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 1.dp)
                    )

                    Column(modifier = Modifier.padding(top = 2.dp)) {
                        Text(
                            text = "Pick-up: ${option.pickupEtaRange} • Delivery: ${option.deliveryEtaRange}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
                        )
                    }

                    val isAvailable = option.platformId in listOf("grabexpress", "lalamove_moto", "lalamove_car", "maxim_delivery", "ninjavan", "jtexpress")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isAvailable) GrabGreenDark else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAvailable) "Available in your area" else "Not available in your area",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAvailable) GrabGreenDark else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }

                // Fare details
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₱${"%.2f".format(option.estimatedFare)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = GrabGreenDark
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onOpenApp,
                            border = androidx.compose.foundation.BorderStroke(1.dp, platformAccentColor),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = "Redirect",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = platformAccentColor
                            )
                        }

                        Button(
                            onClick = onInstantBook,
                            colors = ButtonDefaults.buttonColors(containerColor = GrabGreenDark),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Instant Book (Soon)",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
