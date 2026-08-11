package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.database.SavedPlaceEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.FoodOption
import com.example.ui.viewmodel.OneGoViewModel
import com.example.ui.viewmodel.Restaurant
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareFoodScreen(
    viewModel: OneGoViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val origin by viewModel.selectedOrigin.collectAsStateWithLifecycle()
    val savedPlaces by viewModel.savedPlacesState.collectAsStateWithLifecycle()
    var showOriginPicker by remember { mutableStateOf(false) }
    var showPinMapForPickup by remember { mutableStateOf(false) }
    val distanceKm = remember(origin) { viewModel.getCalculatedDistanceKm() }

    val selectedRestIdx by viewModel.selectedRestaurantIndex.collectAsStateWithLifecycle()
    val isRaining by viewModel.isRainingState.collectAsStateWithLifecycle()
    val isRushHour by viewModel.isRushHourState.collectAsStateWithLifecycle()

    val currentRestaurant = viewModel.restaurants[selectedRestIdx]
    var selectedRegionFilter by remember { mutableStateOf(0) } // 0 = Local PH, 1 = Asia-Wide, 2 = Combined All
    var priceSortMode by remember { mutableStateOf(PriceSortMode.PRICE) }
    var activeOptionForBooking by remember { mutableStateOf<com.example.ui.viewmodel.FoodOption?>(null) }

    val comparedFoodOptions = remember(selectedRestIdx, origin, isRaining, isRushHour) {
        viewModel.getComparedFood()
    }
    val filteredFoodOptions = remember(comparedFoodOptions, selectedRegionFilter) {
        when (selectedRegionFilter) {
            0 -> comparedFoodOptions.filter { it.platformId in listOf("grabfood", "foodpanda", "shopeefood") }
            1 -> comparedFoodOptions.filter { it.platformId in listOf("lineman_food", "baemin", "deliveroo", "meituan", "wolt", "gojekfood") }
            else -> comparedFoodOptions
        }
    }
    val sortedFilteredFoodOptions = remember(filteredFoodOptions, priceSortMode) {
        when (priceSortMode) {
            PriceSortMode.PRICE -> filteredFoodOptions.sortedBy { it.estimatedTotal }
            PriceSortMode.ETA -> filteredFoodOptions.sortedBy { it.etaMins }
        }
    }
    val cheapestFood = remember(sortedFilteredFoodOptions) { sortedFilteredFoodOptions.minByOrNull { it.estimatedTotal } }
    val fastestFood = remember(sortedFilteredFoodOptions) { sortedFilteredFoodOptions.minByOrNull { it.etaMins } }
    val maxFoodPriceDiff = remember(sortedFilteredFoodOptions) {
        val minP = sortedFilteredFoodOptions.minOfOrNull { it.estimatedTotal } ?: 0.0
        val maxP = sortedFilteredFoodOptions.maxOfOrNull { it.estimatedTotal } ?: 0.0
        (maxP - minP).coerceAtLeast(0.0)
    }

    // Toggle flow for explorer vs comparison detail view
    var isShowingComparison by remember { mutableStateOf(false) }
    val initialCategory by viewModel.selectedFoodCategoryState.collectAsStateWithLifecycle()
    var selectedCategory by remember(initialCategory) { mutableStateOf(initialCategory) }
    var showAddStoreDialog by remember { mutableStateOf(false) }

    // Collect all restaurants (default + custom merchant stores)
    val allRestaurants by viewModel.allRestaurantsState.collectAsStateWithLifecycle()

    // Expanded categories definition matching user's food preferences
    val categories = listOf(
        CategoryData("All", Icons.Default.RestaurantMenu),
        CategoryData("Burgers & Fast Food", Icons.Default.LunchDining),
        CategoryData("Chicken & BBQ", Icons.Default.KebabDining),
        CategoryData("Coffee & Tea", Icons.Default.LocalCafe),
        CategoryData("Rice Meals", Icons.Default.RiceBowl),
        CategoryData("Pizza & Pasta", Icons.Default.LocalPizza),
        CategoryData("Noodles & Chinese", Icons.Default.SoupKitchen),
        CategoryData("Desserts & Bakery", Icons.Default.Cake)
    )

    // Filtered restaurants based on category selection
    val filteredRestaurants = remember(selectedCategory, allRestaurants) {
        if (selectedCategory == "All") {
            allRestaurants
        } else {
            allRestaurants.filter { rest ->
                // Check explicit multi-category list if present
                if (rest.categories.contains(selectedCategory)) {
                    return@filter true
                }
                // Fallback category filter matching
                when (selectedCategory) {
                    "Burgers & Fast Food" -> rest.cuisines.contains("Burger", ignoreCase = true) || rest.cuisines.contains("Fast Food", ignoreCase = true)
                    "Chicken & BBQ" -> rest.cuisines.contains("Chicken", ignoreCase = true) || rest.cuisines.contains("BBQ", ignoreCase = true)
                    "Coffee & Tea" -> rest.cuisines.contains("Coffee", ignoreCase = true) || rest.cuisines.contains("Tea", ignoreCase = true) || rest.cuisines.contains("Boba", ignoreCase = true)
                    "Rice Meals" -> rest.cuisines.contains("Rice", ignoreCase = true) || rest.cuisines.contains("Filipino", ignoreCase = true)
                    "Pizza & Pasta" -> rest.cuisines.contains("Pizza", ignoreCase = true) || rest.cuisines.contains("Italian", ignoreCase = true)
                    "Noodles & Chinese" -> rest.cuisines.contains("Chinese", ignoreCase = true) || rest.cuisines.contains("Dimsum", ignoreCase = true) || rest.id == "chowking"
                    "Desserts & Bakery" -> rest.cuisines.contains("Bakery", ignoreCase = true) || rest.cuisines.contains("Cake", ignoreCase = true) || rest.cuisines.contains("Donut", ignoreCase = true)
                    else -> true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isShowingComparison) "Compare Food Delivery" else "Food Delivery Explorer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isShowingComparison) {
                                isShowingComparison = false
                            } else {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { showAddStoreDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GrabGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .padding(end = 8.dp)
                            .testTag("open_add_store_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("Add Store 🏪", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = Modifier.testTag("compare_food_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Delivery Destination Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Destination",
                        tint = GrabGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showOriginPicker = true }
                    ) {
                        Text(
                            text = "DELIVER TO",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GrabGreen
                        )
                        Text(
                            text = origin?.name ?: "Search delivery location…",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
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
                                    contentDescription = "Clear Delivery Address",
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
                                contentDescription = "Search Location Map",
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
                                contentDescription = "Pin Delivery Location",
                                tint = MaximRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Animating between Dashboard explorer view and Detail comparison view
            AnimatedContent(
                targetState = isShowingComparison,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "food_screen_flow"
            ) { showingDetail ->
                if (!showingDetail) {
                    // FOOD EXPLORER DASHBOARD (M3 Grid with Categories list!)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("food_dashboard_list"),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Merchant Store Owner Registration Banner
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 2.dp)
                                    .clickable { showAddStoreDialog = true }
                                    .testTag("merchant_add_store_banner"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = GrabGreen.copy(alpha = 0.12f)),
                                border = BorderStroke(1.dp, GrabGreen.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(GrabGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Storefront,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Are you a Food Merchant or Store Owner? 🏪",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Add your store, upload image, & select food categories!",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 14.sp
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = GrabGreenDark
                                    )
                                }
                            }
                        }

                        // Category Scrollable Row Header
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Categories",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                                )
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(categories) { cat ->
                                        CategoryItem(
                                            name = cat.name,
                                            icon = cat.icon,
                                            isSelected = selectedCategory == cat.name,
                                            onClick = {
                                                selectedCategory = cat.name
                                                viewModel.selectedFoodCategoryState.value = cat.name
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Restaurant Grid Section Title
                        item {
                            Text(
                                text = "Select Restaurant to Compare Prices",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        if (filteredRestaurants.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.SearchOff,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "No restaurants found for \"$selectedCategory\"",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        } else {
                            // Render restaurants as a beautiful 2-column grid inside LazyColumn
                            val chunks = filteredRestaurants.chunked(2)
                            items(chunks) { rowItems ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowItems.forEach { rest ->
                                        Box(modifier = Modifier.weight(1f)) {
                                            RestaurantGridCard(
                                                rest = rest,
                                                distanceKm = distanceKm,
                                                onClick = {
                                                    val realIndex = viewModel.restaurants.indexOf(rest)
                                                    if (realIndex != -1) {
                                                        viewModel.selectedRestaurantIndex.value = realIndex
                                                        isShowingComparison = true
                                                    }
                                                }
                                            )
                                        }
                                    }
                                    if (rowItems.size < 2) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // DELIVERY FARE COMPARISON VIEW (Original functional comparator screen)
                    DraggableComparisonSheetContainer(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("food_comparison_detail"),
                        headerPanel = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Quick Breadcrumb / Selector Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Other Restaurants:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = "Show All Explorer",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GrabGreen,
                                        modifier = Modifier.clickable { isShowingComparison = false }
                                    )
                                }

                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 6.dp)
                                ) {
                                    itemsIndexed(viewModel.restaurants) { index, rest ->
                                        val isSelected = index == selectedRestIdx
                                        AssistChip(
                                            onClick = { viewModel.selectedRestaurantIndex.value = index },
                                            label = { Text(rest.name, fontSize = 11.sp) },
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = if (isSelected) GrabGreenLight else Color.Transparent,
                                                labelColor = if (isSelected) GrabGreenDark else MaterialTheme.colorScheme.onBackground
                                            ),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isSelected) GrabGreen else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
                                            )
                                        )
                                    }
                                }

                                // Selected Food Item details Card
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(FoodpandaPink.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.RestaurantMenu,
                                                contentDescription = null,
                                                tint = FoodpandaPink,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "ORDER COMPARISON FOR",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = FoodpandaPink
                                            )
                                            Text(
                                                text = currentRestaurant.sampleItemName,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Base Price: ₱${"%.2f".format(currentRestaurant.itemPrice)} (Range: ${currentRestaurant.itemPriceRange}) • ${currentRestaurant.name}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                            )
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



                                // Aggregated Options Headers
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (priceSortMode == PriceSortMode.PRICE) "Food Delivery Rates (Cheapest First)" else "Food Delivery Rates (Fastest First)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "${sortedFilteredFoodOptions.size} Platforms",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Parallel delivery options sorted list
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(sortedFilteredFoodOptions) { option ->
                                        FoodOptionCard(
                                            option = option,
                                            onOpenApp = {
                                                viewModel.launchPlatform(
                                                    context = context,
                                                    platformId = option.platformId,
                                                    platformName = option.platformName,
                                                    price = option.estimatedTotal,
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
                }
            }
        }

        activeOptionForBooking?.let { option ->
            InstantBookingDialog(
                visible = true,
                platformId = option.platformId,
                platformName = option.platformName,
                serviceName = "${currentRestaurant.name} - ${currentRestaurant.sampleItemName}",
                estimatedFare = option.estimatedTotal,
                category = "FOOD",
                viewModel = viewModel,
                onDismiss = { activeOptionForBooking = null }
            )
        }

        if (showPinMapForPickup) {
            PinLocationMapDialog(
                dialogTitle = "Pin Delivery Location",
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

        if (showOriginPicker) {
            LocationPickerDialog(
                title = "Select Delivering Location",
                places = savedPlaces,
                onDismiss = { showOriginPicker = false },
                onSelect = {
                    viewModel.selectOrigin(it)
                    showOriginPicker = false
                },
                viewModel = viewModel
            )
        }

        if (showAddStoreDialog) {
            AddStoreDialog(
                onDismiss = { showAddStoreDialog = false },
                savedPlaces = savedPlaces,
                onAddStore = { name, cuisines, sampleItem, price, categories, imageUri, address, lat, lng ->
                    viewModel.addCustomStore(name, cuisines, sampleItem, price, categories, imageUri, address, lat, lng)
                }
            )
        }
    }
}

data class CategoryData(
    val name: String,
    val icon: ImageVector
)

@Composable
fun CategoryItem(
    name: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag("category_filter_$name")
            .padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isSelected) GrabGreen.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
                .border(
                    width = 1.dp,
                    color = if (isSelected) GrabGreen else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = if (isSelected) GrabGreenDark else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isSelected) GrabGreenDark else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun RestaurantGridCard(
    rest: Restaurant,
    distanceKm: Double,
    onClick: () -> Unit
) {
    // Dynamic image mapping with generated premium asset drawables
    val imageResId = when (rest.id) {
        "jollibee" -> R.drawable.img_resto_jollibee_mcd_1783037783020
        "mcdonalds" -> R.drawable.img_resto_jollibee_mcd_1783037783020
        "mang_inasal" -> R.drawable.img_resto_mang_inasal_1783037802204
        "chowking" -> R.drawable.img_resto_chowking_1783037814473
        "greenwich" -> R.drawable.img_resto_greenwich_1783037826680
        else -> R.drawable.img_promo_banner
    }

    // Dynamic ETA calculation relative to geographic distance
    val baseEta = when (rest.id) {
        "jollibee" -> 15
        "mang_inasal" -> 20
        "mcdonalds" -> 18
        "chowking" -> 22
        "greenwich" -> 25
        else -> 20
    }
    val dynamicEta = max(12, (baseEta + distanceKm * 2.2).toInt())
    val deliveryTimeStr = "$dynamicEta-${dynamicEta + 8} mins"

    // Custom promotions
    val promoText = when (rest.id) {
        "jollibee" -> "₱30 Off • GrabUnlimited"
        "mang_inasal" -> "Free delivery (₱299+)"
        "mcdonalds" -> "Buy 1 Get 1 Deal"
        "chowking" -> "₱40 discount promo"
        "greenwich" -> "Free delivery over ₱350"
        else -> "Free delivery on select platforms"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("restaurant_card_${rest.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                if (rest.imageUri != null) {
                    if (rest.imageUri.startsWith("preset_")) {
                        val presetId = rest.imageUri.removePrefix("preset_")
                        val presetDrawable = when (presetId) {
                            "p1" -> R.drawable.img_resto_jollibee_mcd_1783037783020
                            "p2" -> R.drawable.img_resto_mang_inasal_1783037802204
                            "p3" -> R.drawable.img_resto_chowking_1783037814473
                            "p4" -> R.drawable.img_resto_greenwich_1783037826680
                            else -> R.drawable.img_promo_banner
                        }
                        Image(
                            painter = painterResource(id = presetDrawable),
                            contentDescription = rest.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        AsyncImage(
                            model = rest.imageUri,
                            contentDescription = rest.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = rest.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                if (rest.isUserStore) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(GrabGreen)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "MY STORE",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                // Rating Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.72f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${rest.rating}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Delivery Time Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(GrabGreenDark)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Delivery Time",
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = deliveryTimeStr,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = rest.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = rest.cuisines.split(",").take(2).joinToString(", "),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))
                Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = "Promo",
                        tint = FoodpandaPink,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = promoText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FoodpandaPink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun FoodOptionCard(
    option: com.example.ui.viewmodel.FoodOption,
    onOpenApp: () -> Unit,
    onInstantBook: () -> Unit
) {
    val platformAccentColor = when (option.platformId) {
        "grabfood" -> GrabGreen
        "foodpanda" -> FoodpandaPink
        "shopeefood" -> ShopeeOrange
        "gojekfood" -> GojekGreen
        "lineman_food" -> LineManGreen
        "baemin" -> BaeminMint
        "deliveroo" -> DeliverooTeal
        "meituan" -> MeituanYellow
        "wolt" -> WoltBlue
        else -> GrabGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("food_option_${option.platformId}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Platform Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(platformAccentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeliveryDining,
                            contentDescription = option.platformName,
                            tint = platformAccentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = option.platformName,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
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
                    text = "Delivery ETA: ${option.deliveryEtaRange}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
                )
            }

            val isAvailable = option.platformId in listOf("grabfood", "foodpanda", "shopeefood")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp, start = 2.dp)
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

            Spacer(modifier = Modifier.height(10.dp))

            // Breakdowns Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Item Subtotal: ₱${"%.2f".format(option.basePrice)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Delivery Fee: ₱${"%.2f".format(option.deliveryFee)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Service Fee: ₱${"%.2f".format(option.serviceFee)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                // Total Pricing Box
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total: ₱${"%.2f".format(option.estimatedTotal)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = GrabGreenDark
                    )

                    option.discountLabel?.let { promo ->
                        Text(
                            text = promo,
                            fontSize = 9.sp,
                            color = FoodpandaPink,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 6.dp)
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
                                text = "Instant Order (Soon)",
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

