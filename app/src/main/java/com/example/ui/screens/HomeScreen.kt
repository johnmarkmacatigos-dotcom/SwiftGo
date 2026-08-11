package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.R
import com.example.data.database.SavedPlaceEntity
import com.example.ui.components.GoogleMapsGroundingCard
import com.example.ui.components.GoogleSearchGroundingCard
import com.example.ui.components.LocationPermissionCard
import com.example.ui.components.RegisterProfileDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.OneGoViewModel
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: OneGoViewModel,
    onNavigateToRides: () -> Unit,
    onNavigateToFood: () -> Unit,
    onNavigateToDelivery: () -> Unit,
    onNavigateToPabili: () -> Unit,
    onNavigateToSavedPlaces: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val loyaltyPoints by viewModel.loyaltyPointsState.collectAsStateWithLifecycle()
    val savedPlaces by viewModel.savedPlacesState.collectAsStateWithLifecycle()
    val isRaining by viewModel.isRainingState.collectAsStateWithLifecycle()
    val isRushHour by viewModel.isRushHourState.collectAsStateWithLifecycle()
    val isGuestMode by viewModel.isGuestModeState.collectAsStateWithLifecycle()
    val userName by viewModel.userNameState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showRegisterDialog by remember { mutableStateOf(false) }

    if (showRegisterDialog) {
        RegisterProfileDialog(
            onDismiss = { showRegisterDialog = false },
            onCompleteRegistration = { name, email, phone, address, lat, lng ->
                viewModel.completeGmailRegistration(name, email, phone, address, lat, lng)
            }
        )
    }

    // Loyalty level computation
    val loyaltyTier = when {
        isGuestMode -> "Guest User"
        loyaltyPoints > 500 -> "Platinum Member"
        loyaltyPoints > 350 -> "Gold Member"
        else -> "Silver Member"
    }
    val tierColor = when {
        loyaltyPoints > 500 -> Color(0xFFE5E4E2) // Platinum
        loyaltyPoints > 350 -> Color(0xFFFFD700) // Gold
        else -> Color(0xFFC0C0C0) // Silver
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Grab-style Top Green Bar / Header Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(GrabGreen, GrabGreenDark)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Column {
                    // Profile greeting & Weather simulation control
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Hello, Ka-OneGo!",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Iloilo City, PH",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Status indicators & Weather modifiers
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.toggleRain() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isRaining) Color.White.copy(alpha = 0.4f)
                                        else Color.White.copy(alpha = 0.15f)
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isRaining) Icons.Default.Cloud else Icons.Default.WbSunny,
                                    contentDescription = "Simulate Rain",
                                    tint = if (isRaining) Color(0xFF81D4FA) else Color(0xFFFFEB3B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.toggleRushHour() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isRushHour) Color.White.copy(alpha = 0.4f)
                                        else Color.White.copy(alpha = 0.15f)
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isRushHour) Icons.Default.AccessTimeFilled else Icons.Default.AccessTime,
                                    contentDescription = "Simulate Rush Hour",
                                    tint = if (isRushHour) Color(0xFFFF7043) else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // GrabPay / OneGo Balance Card Clone
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("balance_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = "Wallet",
                                        tint = GrabGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "OneGoPay Wallet",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFFF9800))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "SOON",
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Points",
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$loyaltyPoints pts",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "₱1,540.25",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(tierColor)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "$loyaltyTier status",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.clickable {
                                            Toast.makeText(context, "Wallet Top-up is simulated! +₱500", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.AddCircle,
                                            contentDescription = "Top Up",
                                            tint = GrabGreen,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text("Top Up", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.clickable {
                                            onNavigateToHistory()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.History,
                                            contentDescription = "Activity",
                                            tint = GrabGreen,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text("History", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Guest Mode Alert Banner
        if (isGuestMode) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("guest_mode_home_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    border = BorderStroke(1.dp, Color(0xFFFFB74D))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF9800).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonOutline,
                                    contentDescription = "Guest",
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Browsing as Guest User 👤",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFE65100)
                                )
                                Text(
                                    text = "Register with Google to earn OneGo Points & unlock member promos!",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { showRegisterDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GrabGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("home_guest_register_btn")
                        ) {
                            Text("Register", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Active Weather Surge alert banner if active
        if (isRaining || isRushHour) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isRaining) Color(0xFFE1F5FE) else Color(0xFFFBE9E7)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isRaining) Icons.Default.Thunderstorm else Icons.Default.TrendingUp,
                            contentDescription = "Condition",
                            tint = if (isRaining) Color(0xFF0288D1) else Color(0xFFD84315),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isRaining) "Simulated Rain Active 🌧️" else "Simulated Rush Hour Active 🚗💨",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isRaining) Color(0xFF01579B) else Color(0xFFBF360C)
                            )
                            Text(
                                text = "Surge multipliers are applied to comparison pricing to reflect real-world conditions.",
                                fontSize = 11.sp,
                                color = if (isRaining) Color(0xFF0277BD) else Color(0xFFD84315)
                            )
                        }
                    }
                }
            }
        } else {
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }

        // Services Grid Header
        item {
            Text(
                text = "Services Aggregator",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            )
        }

        // Service Grid (Clean & Simple)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                ServiceGridItem(
                    name = "Rides",
                    icon = Icons.Default.DirectionsCar,
                    color = GrabGreen,
                    tag = "service_rides",
                    onClick = onNavigateToRides
                )
                ServiceGridItem(
                    name = "Food",
                    icon = Icons.Default.Restaurant,
                    color = FoodpandaPink,
                    tag = "service_food",
                    onClick = {
                        viewModel.selectedFoodCategoryState.value = "All"
                        onNavigateToFood()
                    }
                )
                ServiceGridItem(
                    name = "Express",
                    icon = Icons.Default.LocalPostOffice,
                    color = LalamoveOrange,
                    tag = "service_delivery",
                    onClick = onNavigateToDelivery
                )
                ServiceGridItem(
                    name = "Pabili",
                    icon = Icons.Default.ShoppingBag,
                    color = GrabGreen,
                    tag = "service_pabili",
                    onClick = onNavigateToPabili
                )
                ServiceGridItem(
                    name = "Coffee & Tea",
                    icon = Icons.Default.LocalCafe,
                    color = Color(0xFF795548),
                    tag = "service_coffee",
                    onClick = {
                        // Strictly redirect to Coffee & Tea category selection
                        viewModel.selectedFoodCategoryState.value = "Coffee & Tea"
                        val starbucksIdx = viewModel.restaurants.indexOfFirst { it.id == "starbucks" }
                        if (starbucksIdx != -1) {
                            viewModel.selectedRestaurantIndex.value = starbucksIdx
                        }
                        onNavigateToFood()
                    }
                )
            }
        }

        // Live Google Search Grounding with gemini-3.5-flash
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                GoogleSearchGroundingCard(
                    viewModel = viewModel,
                    initialQuery = "Best coffee & food deals in Iloilo"
                )
            }
        }

        // Live Google Maps Data Grounding with gemini-3.5-flash + googleMaps tool
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                GoogleMapsGroundingCard(
                    viewModel = viewModel,
                    initialLocationQuery = "SM City Iloilo Mandurriao",
                    onLocationSelected = { place ->
                        viewModel.updateOrigin(place)
                    }
                )
            }
        }

        // Promo / Advert banner using generated img_promo_banner
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "Featured Promotions",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Display the generated image beautifully
                        Image(
                            painter = painterResource(id = R.drawable.img_promo_banner),
                            contentDescription = "Promo banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Subtle dark overlay for readability
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                    )
                                )
                        )

                        // Text content overlay
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Compare Fares & Save Big!",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "OneGo analyzes prices across 10+ platforms including Grab, Maxim, XiCar, Angkas & more in real-time.",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Philippine Hotspots / Fast Selection Section
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Iloilo City Hotspots",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Instant Compare",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrabGreen
                    )
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val hotspots = listOf(
                        Hotspot("SM City", "SM City Iloilo Mall", "Mandurriao, Iloilo City", 10.7123, 122.5518),
                        Hotspot("Megaworld", "Festive Walk Mall", "Iloilo Business Park", 10.7176, 122.5422),
                        Hotspot("Esplanade", "Iloilo River Esplanade", "Molo, Iloilo City", 10.7011, 122.5512),
                        Hotspot("Jaro Plaza", "Jaro Cathedral & Plaza", "Jaro, Iloilo City", 10.7258, 122.5583),
                        Hotspot("CPU", "Central Philippine University", "Jaro, Iloilo City", 10.7247, 122.5606)
                    )

                    items(hotspots) { spot ->
                        HotspotCard(
                            spot = spot,
                            onClick = {
                                // Instantly set the destination to this hotspot
                                val place = SavedPlaceEntity(
                                    name = spot.name,
                                    address = spot.address,
                                    latitude = spot.lat,
                                    longitude = spot.lng,
                                    iconName = "star"
                                )
                                viewModel.selectDestination(place)
                                onNavigateToRides()
                            }
                        )
                    }
                }
            }
        }
    }
}

data class Hotspot(
    val name: String,
    val title: String,
    val address: String,
    val lat: Double,
    val lng: Double
)

@Composable
fun HotspotCard(spot: Hotspot, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GrabGreenLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationCity,
                    contentDescription = null,
                    tint = GrabGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = spot.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = spot.address,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ServiceGridItem(
    name: String,
    icon: ImageVector,
    color: Color,
    tag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(66.dp)
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
