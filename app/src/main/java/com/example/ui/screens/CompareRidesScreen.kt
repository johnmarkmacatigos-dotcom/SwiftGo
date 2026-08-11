package com.example.ui.screens

import android.widget.Toast
import kotlin.math.max
import kotlin.math.min
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.SavedPlaceEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.OneGoViewModel
import com.example.ui.viewmodel.RideOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareRidesScreen(
    viewModel: OneGoViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSavedPlaces: () -> Unit
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
    var selectedRegionFilter by remember { mutableStateOf(0) } // 0 = Local PH, 1 = Asia-Wide, 2 = Combined All
    var priceSortMode by remember { mutableStateOf(PriceSortMode.PRICE) } // Sort by Price or ETA
    var activeOptionForBooking by remember { mutableStateOf<RideOption?>(null) }
    var selectedRideTier by remember { mutableStateOf("All") }

    val comparedOptions = remember(origin, destination, isRaining, isRushHour) {
        viewModel.getComparedRides()
    }
    val filteredOptions = remember(comparedOptions, selectedRegionFilter, selectedRideTier) {
        val regionList = when (selectedRegionFilter) {
            0 -> comparedOptions.filter { it.platformId in listOf("grab", "maxim_moto", "maxim_car", "xicar", "indrive", "angkas", "joyride", "moveit") }
            1 -> comparedOptions.filter { it.platformId in listOf("didi", "ola", "tada", "kakaot", "lineman_taxi", "gojek") }
            else -> comparedOptions
        }
        val categoryList = when (selectedRideTier) {
            "Economy" -> regionList.filter {
                it.serviceName.contains("Economy", ignoreCase = true) ||
                it.serviceName.contains("Car", ignoreCase = true) ||
                it.serviceName.contains("4-Seater", ignoreCase = true) ||
                it.serviceName.contains("Express", ignoreCase = true)
            }
            "Comfort" -> regionList.filter {
                it.serviceName.contains("Comfort", ignoreCase = true) ||
                it.serviceName.contains("Sedan", ignoreCase = true) ||
                it.serviceName.contains("Premium", ignoreCase = true) ||
                it.serviceName.contains("Prime", ignoreCase = true)
            }
            "XL" -> regionList.filter {
                it.serviceName.contains("XL", ignoreCase = true) ||
                it.serviceName.contains("6-Seater", ignoreCase = true) ||
                it.serviceName.contains("Van", ignoreCase = true) ||
                it.serviceName.contains("SUV", ignoreCase = true)
            }
            "Motorbike" -> regionList.filter {
                it.serviceName.contains("Moto", ignoreCase = true) ||
                it.serviceName.contains("Motor", ignoreCase = true) ||
                it.serviceName.contains("Tricycle", ignoreCase = true) ||
                it.serviceName.contains("Angkas", ignoreCase = true) ||
                it.serviceName.contains("Joyride", ignoreCase = true)
            }
            else -> regionList
        }
        if (categoryList.isNotEmpty()) categoryList else regionList
    }
    val sortedFilteredOptions = remember(filteredOptions, priceSortMode) {
        when (priceSortMode) {
            PriceSortMode.PRICE -> filteredOptions.sortedBy { it.estimatedFare }
            PriceSortMode.ETA -> filteredOptions.sortedBy { it.etaMins }
        }
    }
    val minPrice = remember(sortedFilteredOptions) { sortedFilteredOptions.minOfOrNull { it.estimatedFare } ?: 0.0 }
    val maxPrice = remember(sortedFilteredOptions) { sortedFilteredOptions.maxOfOrNull { it.estimatedFare } ?: 0.0 }
    val cheapestOption = remember(sortedFilteredOptions) { sortedFilteredOptions.minByOrNull { it.estimatedFare } }
    val fastestOption = remember(sortedFilteredOptions) { sortedFilteredOptions.minByOrNull { it.etaMins } }
    val maxPriceDiff = remember(minPrice, maxPrice) { (maxPrice - minPrice).coerceAtLeast(0.0) }
    val distanceKm = remember(origin, destination) {
        viewModel.getCalculatedDistanceKm()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Compare Rides",
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
        modifier = Modifier.testTag("compare_rides_screen")
    ) { innerPadding ->
        DraggableComparisonSheetContainer(
            modifier = Modifier.padding(innerPadding),
            headerPanel = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Origin & Destination Search Panel
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                        // Origin Input Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.RadioButtonChecked,
                                contentDescription = "Pickup",
                                tint = GrabGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showOriginPicker = true }
                            ) {
                                Text(
                                    text = "PICKUP",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GrabGreen
                                )
                                Text(
                                    text = origin?.name ?: "Search pickup location…",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (origin != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Right Action Icons: [X] [Map] [Red Pin]
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
                            modifier = Modifier.padding(vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)
                        )

                        // Destination Input Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Dropoff",
                                tint = MaximRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showDestPicker = true }
                            ) {
                                Text(
                                    text = "DESTINATION",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaximRed
                                )
                                Text(
                                    text = destination?.name ?: "Where to?",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (destination != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Right Action Icons: [X] [Map] [Red Pin]
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

                        // Quick Actions: Swap & Add Saved
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { viewModel.swapLocations() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GrabGreenLight,
                                    contentColor = GrabGreenDark
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = "Swap",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Swap", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Text(
                                text = "Distance: ${"%.1f".format(distanceKm)} km",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            )

                            TextButton(
                                onClick = onNavigateToSavedPlaces,
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddLocation,
                                    contentDescription = "Manage",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Saved Places", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        }
                    }
                }
            },
            ratesSheetContent = {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Vehicle / Ride Tier Category Selection Chips (Matching Image 2)
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val tierOptions = listOf(
                            Triple("All", "All types", Icons.Default.Search),
                            Triple("Economy", "Economy", Icons.Default.DirectionsCar),
                            Triple("Comfort", "Comfort", Icons.Default.DirectionsCar),
                            Triple("XL", "Van / XL", Icons.Default.AirportShuttle),
                            Triple("Motorbike", "Moto", Icons.Default.TwoWheeler)
                        )

                        items(tierOptions.size) { index ->
                            val (key, label, icon) = tierOptions[index]
                            val isSelected = selectedRideTier == key

                            Surface(
                                onClick = { selectedRideTier = key },
                                shape = RoundedCornerShape(50),
                                color = if (isSelected) GrabGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) GrabGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isSelected) GrabGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isSelected) GrabGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

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



                    // Real-Time Comparison Feed Label
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (priceSortMode == PriceSortMode.PRICE) "Ride Rates (Lowest Fare First)" else "Ride Rates (Fastest Pickup First)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${sortedFilteredOptions.size} Providers",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Fare Options List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(sortedFilteredOptions) { option ->
                            RideOptionCard(
                                option = option,
                                minPrice = minPrice,
                                maxPrice = maxPrice,
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
        )

        // Search Picker Dialog Sheets
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

        activeOptionForBooking?.let { option ->
            InstantBookingDialog(
                visible = true,
                platformId = option.platformId,
                platformName = option.platformName,
                serviceName = option.serviceName,
                estimatedFare = option.estimatedFare,
                category = "RIDES",
                viewModel = viewModel,
                onDismiss = { activeOptionForBooking = null }
            )
        }
    }
}

@Composable
fun SimulatedRouteMap(
    origin: SavedPlaceEntity?,
    destination: SavedPlaceEntity?,
    isRushHour: Boolean,
    isRaining: Boolean,
    distanceKm: Double,
    onInstantBookSelected: (RideOption) -> Unit
) {
    // Map feature removed per user request
}

@Composable
fun RideTypeRow(
    title: String,
    desc: String,
    price: Double,
    etaMins: Int,
    iconColor: Color,
    badgeText: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    priceDifferenceText: String? = null,
    priceDifferenceColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onInstantBook: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) GrabGreen.copy(alpha = 0.08f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) GrabGreen else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Radio button indicator
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) GrabGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (badgeText != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(iconColor.copy(alpha = 0.15f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = iconColor
                            )
                        }
                    }
                }

                Text(
                    text = desc,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "ETA: $etaMins mins",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrabGreenDark
                )
            }
        }

        // Price, difference and Instant Book action
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "₱${"%.1f".format(price)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (priceDifferenceText != null) {
                Text(
                    text = priceDifferenceText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = priceDifferenceColor
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GrabGreen)
                    .clickable { onInstantBook() }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Book",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun RideOptionCard(
    option: RideOption,
    minPrice: Double = 0.0,
    maxPrice: Double = 0.0,
    onOpenApp: () -> Unit,
    onInstantBook: () -> Unit
) {
    val platformAccentColor = when (option.platformId) {
        "grab" -> GrabGreen
        "maxim_moto", "maxim_car" -> MaximRed
        "xicar" -> XiCarBlue
        "gojek" -> GojekGreen
        "indrive" -> InDriveGreen
        "angkas" -> AngkasBlue
        "joyride" -> JoyrideYellow
        "moveit" -> MoveItRed
        "didi" -> DidiOrange
        "ola" -> OlaYellow
        "tada" -> TadaTeal
        "kakaot" -> KakaoYellow
        "lineman_taxi" -> LineManGreen
        else -> GrabGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInstantBook() }
            .testTag("ride_option_${option.platformId}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Platform Indicator Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(platformAccentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (option.platformId) {
                        "grab" -> Icons.Default.DirectionsCar
                        "maxim_moto" -> Icons.Default.TwoWheeler
                        "maxim_car" -> Icons.Default.DirectionsCar
                        "xicar" -> Icons.Default.Star
                        "gojek" -> Icons.Default.DirectionsCar
                        "indrive" -> Icons.Default.Gavel
                        "angkas" -> Icons.Default.TwoWheeler
                        "joyride" -> Icons.Default.TwoWheeler
                        "moveit" -> Icons.Default.TwoWheeler
                        "didi" -> Icons.Default.DirectionsCar
                        "ola" -> Icons.Default.DirectionsCar
                        "tada" -> Icons.Default.DirectionsCar
                        "kakaot" -> Icons.Default.DirectionsCar
                        "lineman_taxi" -> Icons.Default.DirectionsCar
                        else -> Icons.Default.DirectionsCar
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = option.platformName,
                        tint = platformAccentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Service Details
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
                                    .background(
                                        if (badge.contains("Surge")) MaximRed.copy(alpha = 0.15f)
                                        else GrabGreen.copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badge,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (badge.contains("Surge")) MaximRed else GrabGreenDark
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

                    Column(modifier = Modifier.padding(top = 3.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Pick-up ETA",
                                tint = GrabGreenDark,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Pick-up: ${option.pickupEtaRange}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 1.dp)) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "Destination ETA",
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Trip: ${option.destinationEtaRange}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                            )
                        }
                    }

                    val isAvailable = option.platformId in listOf("grab", "maxim_moto", "maxim_car", "xicar", "indrive", "angkas", "joyride", "moveit")
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

                // Fare Column
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
                                color = if (platformAccentColor == JoyrideYellow) Color.Black else platformAccentColor
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

            // Promotional or secondary instructions if present
            option.promoLabel?.let { promo ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.LightGray.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = "Promo",
                        tint = GrabGreenDark,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = promo,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            if (maxPrice > minPrice) {
                Spacer(modifier = Modifier.height(8.dp))
                RelativePriceSpectrumBar(
                    currentPrice = option.estimatedFare,
                    minPrice = minPrice,
                    maxPrice = maxPrice
                )
            }
        }
    }
}
