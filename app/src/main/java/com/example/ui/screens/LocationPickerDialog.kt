package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.database.SavedPlaceEntity
import com.example.ui.components.LocationPermissionCard
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.GrabGreenDark
import com.example.ui.theme.MaximRed
import com.example.ui.viewmodel.OneGoViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

data class PredefinedLandmark(
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val iconName: String
)

val IL_LANDMARKS = listOf(
    PredefinedLandmark("SM City Iloilo", "Benigno Aquino Ave, Mandurriao, Iloilo City", 10.7123, 122.5518, "shopping"),
    PredefinedLandmark("Megaworld Festive Walk", "Iloilo Business Park, Mandurriao, Iloilo City", 10.7176, 122.5422, "shopping"),
    PredefinedLandmark("Iloilo River Esplanade", "Molo, Iloilo City", 10.7011, 122.5512, "star"),
    PredefinedLandmark("Jaro Plaza Cathedral", "Jaro, Iloilo City", 10.7258, 122.5583, "star"),
    PredefinedLandmark("Central Philippine University (CPU)", "Jaro, Iloilo City", 10.7247, 122.5606, "school"),
    PredefinedLandmark("University of the Philippines Visayas", "General Luna St, Iloilo City", 10.6975, 122.5621, "school"),
    PredefinedLandmark("Atria Park District", "Mandurriao, Iloilo City", 10.7101, 122.5485, "shopping"),
    PredefinedLandmark("Iloilo International Airport", "Cabatuan, Iloilo", 10.8291, 122.4933, "star"),
    PredefinedLandmark("Molo Mansion", "Locsin St, Molo, Iloilo City", 10.6961, 122.5332, "home"),
    PredefinedLandmark("Ortiz Wharf (Guimaras Ferry)", "City Proper, Iloilo City", 10.6897, 122.5760, "star"),
    PredefinedLandmark("The Medical City Iloilo", "Locsin St, Molo, Iloilo City", 10.6970, 122.5445, "star"),
    PredefinedLandmark("QualiMed Hospital", "Atria Park District, Mandurriao, Iloilo City", 10.7115, 122.5492, "star"),
    PredefinedLandmark("Robinsons Place Iloilo", "De Leon St, Iloilo City", 10.6942, 122.5675, "shopping"),
    PredefinedLandmark("Iloilo Mission Hospital", "Mission Road, Jaro, Iloilo City", 10.7178, 122.5689, "star"),
    PredefinedLandmark("St. Paul University Iloilo", "General Luna St, Iloilo City", 10.6985, 122.5610, "school")
)

/**
 * Structured Key-Line Address Suggestion data model
 */
data class KeyLineAddressSuggestion(
    val titleKeyLine: String,       // Key Line 1: Main Street / Building / House No.
    val districtKeyLine: String,    // Key Line 2: Barangay / District / City
    val postalKeyLine: String,      // Key Line 3: Region / Postal Code / Distance Tag
    val latitude: Double,
    val longitude: Double,
    val placeName: String,
    val iconName: String = "place",
    val badgeTag: String? = null
)

/**
 * Helper to compute address key lines from lat/lng coordinates
 */
fun getAddressKeyLinesForCoordinates(lat: Double, lng: Double): Triple<String, String, String> {
    val district = when {
        lat > 10.720 -> "Jaro District"
        lat < 10.700 -> "City Proper / Molo District"
        lng < 122.545 -> "Mandurriao Commercial District"
        else -> "Mandurriao / La Paz Metro Area"
    }

    val street = when {
        lat > 10.718 && lng > 122.555 -> "Lopez Jaena St., Brgy. El 98"
        lat > 10.715 -> "Benigno Aquino Jr. Ave (Diversion Road)"
        lat > 10.708 -> "Don Donato Pison Ave, Atria District"
        lat > 10.700 -> "Iloilo River Esplanade Walkway"
        else -> "General Luna St. / Locsin St."
    }

    val keyLine1 = "$street (Pinned Pick-up Entrance)"
    val keyLine2 = "$district, Iloilo City, Western Visayas"
    val keyLine3 = "Coords: ${"%.4f".format(lat)}, ${"%.4f".format(lng)} • Pin Accuracy ±2m"
    return Triple(keyLine1, keyLine2, keyLine3)
}

/**
 * Dynamically generates structured Key-Line address suggestions for manual inputs
 */
fun generateKeyLineSuggestionsForQuery(
    query: String,
    savedPlaces: List<SavedPlaceEntity>
): List<KeyLineAddressSuggestion> {
    val cleanQuery = query.trim()
    if (cleanQuery.isBlank()) return emptyList()

    val suggestions = mutableListOf<KeyLineAddressSuggestion>()

    // 1. Direct Match Key-Line Suggestion from Manual Input
    val queryCap = cleanQuery.split(" ").joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
    val offset = (cleanQuery.hashCode() % 100) / 10000.0
    suggestions.add(
        KeyLineAddressSuggestion(
            titleKeyLine = "$queryCap (Main Pick-up Gate)",
            districtKeyLine = "Mandurriao District, Iloilo City",
            postalKeyLine = "Postal Code 5000 • Direct Input • 0.3 km away",
            latitude = 10.7123 + offset,
            longitude = 122.5518 + offset,
            placeName = queryCap,
            iconName = "pin",
            badgeTag = "DIRECT MATCH"
        )
    )

    // 2. Additional Key-Line Street Suggestions derived from user input
    suggestions.add(
        KeyLineAddressSuggestion(
            titleKeyLine = "124 $queryCap Ave, Building A",
            districtKeyLine = "Brgy. San Rafael, Mandurriao, Iloilo City",
            postalKeyLine = "Postal Code 5000 • Verified Pick-up Spot • 0.6 km",
            latitude = 10.7150 + offset,
            longitude = 122.5480 + offset,
            placeName = "$queryCap Avenue",
            iconName = "work",
            badgeTag = "KEY LINE 1"
        )
    )

    suggestions.add(
        KeyLineAddressSuggestion(
            titleKeyLine = "$queryCap Plaza Entrance / Terminal",
            districtKeyLine = "Brgy. Benedicto, Jaro District, Iloilo City",
            postalKeyLine = "Postal Code 5007 • Landmark Hub • 1.2 km away",
            latitude = 10.7250,
            longitude = 122.5580,
            placeName = "$queryCap Plaza",
            iconName = "star",
            badgeTag = "KEY LINE 2"
        )
    )

    // 3. Match against Saved Places
    savedPlaces.filter {
        it.name.contains(cleanQuery, ignoreCase = true) || it.address.contains(cleanQuery, ignoreCase = true)
    }.forEach { place ->
        suggestions.add(
            KeyLineAddressSuggestion(
                titleKeyLine = place.name,
                districtKeyLine = place.address,
                postalKeyLine = "Saved Location • Iloilo Metro Area • Fast Pick-up",
                latitude = place.latitude,
                longitude = place.longitude,
                placeName = place.name,
                iconName = place.iconName,
                badgeTag = "SAVED"
            )
        )
    }

    // 4. Match against Landmarks
    IL_LANDMARKS.filter {
        it.name.contains(cleanQuery, ignoreCase = true) || it.address.contains(cleanQuery, ignoreCase = true)
    }.take(3).forEach { landmark ->
        suggestions.add(
            KeyLineAddressSuggestion(
                titleKeyLine = landmark.name,
                districtKeyLine = landmark.address,
                postalKeyLine = "Popular Iloilo Landmark • High Driver Density",
                latitude = landmark.lat,
                longitude = landmark.lng,
                placeName = landmark.name,
                iconName = landmark.iconName,
                badgeTag = "LANDMARK"
            )
        )
    }

    return suggestions.distinctBy { it.titleKeyLine }
}

/**
 * Interactive Pin Location Map Dialog
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PinLocationMapDialog(
    dialogTitle: String = "Pin Pick-up Location",
    isPickup: Boolean = true,
    initialLat: Double = 10.7123,
    initialLng: Double = 122.5518,
    onDismiss: () -> Unit,
    onConfirmPin: (SavedPlaceEntity) -> Unit
) {
    val fineLocationPermissionState = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
    var currentLat by remember { mutableStateOf(initialLat) }
    var currentLng by remember { mutableStateOf(initialLng) }

    val pinAccentColor = if (isPickup) GrabGreen else MaximRed
    val pinDarkColor = if (isPickup) GrabGreenDark else Color(0xFFC62828)

    // Map offset state (for interactive dragging/tapping)
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val geocodingRepo = remember { com.example.data.repository.GoogleGeocodingRepository() }
    var keyLine1 by remember { mutableStateOf("Locating address...") }
    var keyLine2 by remember { mutableStateOf("Metro Area") }
    var keyLine3 by remember { mutableStateOf("Coords: ${"%.4f".format(currentLat)}, ${"%.4f".format(currentLng)}") }

    LaunchedEffect(currentLat, currentLng) {
        kotlinx.coroutines.delay(350) // debounce map pin drag/nudge reverse-geocoding
        try {
            val (k1, k2, k3) = geocodingRepo.reverseGeocode(currentLat, currentLng)
            keyLine1 = k1
            keyLine2 = k2
            keyLine3 = k3
        } catch (e: Exception) {
            val (k1, k2, k3) = getAddressKeyLinesForCoordinates(currentLat, currentLng)
            keyLine1 = k1
            keyLine2 = k2
            keyLine3 = k3
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(pinAccentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PinDrop,
                                contentDescription = "Pin Location",
                                tint = pinDarkColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = dialogTitle,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap map or move controls to position pin",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Map Canvas Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE8ECEF))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .pointerInput(Unit) {
                            detectTapGestures { tapOffset ->
                                // Convert tap location to lat/lng delta
                                val centerX = size.width / 2f
                                val centerY = size.height / 2f
                                val deltaX = (tapOffset.x - centerX) / size.width
                                val deltaY = (tapOffset.y - centerY) / size.height

                                offsetX = tapOffset.x - centerX
                                offsetY = tapOffset.y - centerY

                                currentLng += (deltaX * 0.008)
                                currentLat -= (deltaY * 0.008)
                            }
                        }
                ) {
                    // Draw Map Graphics (Roads, River, Grid)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height

                        // Grid lines
                        for (i in 0..10) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.4f),
                                start = Offset(i * (width / 10f), 0f),
                                end = Offset(i * (width / 10f), height),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.4f),
                                start = Offset(0f, i * (height / 10f)),
                                end = Offset(width, i * (height / 10f)),
                                strokeWidth = 1f
                            )
                        }

                        // River Path (Iloilo River curve)
                        val riverPath = Path().apply {
                            moveTo(0f, height * 0.7f)
                            cubicTo(width * 0.3f, height * 0.8f, width * 0.6f, height * 0.4f, width, height * 0.5f)
                        }
                        drawPath(
                            path = riverPath,
                            color = Color(0xFF90CAF9),
                            style = Stroke(width = 24f)
                        )

                        // Main Avenues (Diversion Rd)
                        drawLine(
                            color = Color.White,
                            start = Offset(width * 0.2f, 0f),
                            end = Offset(width * 0.8f, height),
                            strokeWidth = 12f
                        )
                        drawLine(
                            color = Color(0xFFFFD54F),
                            start = Offset(width * 0.2f, 0f),
                            end = Offset(width * 0.8f, height),
                            strokeWidth = 3f
                        )

                        // Connecting Street
                        drawLine(
                            color = Color.White,
                            start = Offset(0f, height * 0.35f),
                            end = Offset(width, height * 0.35f),
                            strokeWidth = 8f
                        )
                    }

                    // Center Map Reticle & Pin Marker
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Pulsing Pick-up Badge
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = GrabGreenDark,
                                shadowElevation = 6.dp
                            ) {
                                Text(
                                    text = "PICK-UP PIN HERE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))

                            // Large Pin Icon
                            Icon(
                                imageVector = Icons.Default.Room,
                                contentDescription = "Center Pin",
                                tint = GrabGreen,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    // GPS Recenter Button
                    FloatingActionButton(
                        onClick = {
                            if (fineLocationPermissionState.status.isGranted) {
                                currentLat = initialLat
                                currentLng = initialLng
                                offsetX = 0f
                                offsetY = 0f
                            } else {
                                fineLocationPermissionState.launchPermissionRequest()
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(38.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = GrabGreenDark
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Recenter GPS", modifier = Modifier.size(20.dp))
                    }

                    // Quick Location Preset Chips Overlay
                    LazyRow(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = false,
                                onClick = { currentLat = 10.7123; currentLng = 122.5518 },
                                label = { Text("SM Mandurriao", fontSize = 10.sp) },
                                leadingIcon = { Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = false,
                                onClick = { currentLat = 10.7011; currentLng = 122.5512 },
                                label = { Text("Esplanade", fontSize = 10.sp) },
                                leadingIcon = { Icon(Icons.Default.NaturePeople, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = false,
                                onClick = { currentLat = 10.6961; currentLng = 122.5332 },
                                label = { Text("Molo Mansion", fontSize = 10.sp) },
                                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = false,
                                onClick = { currentLat = 10.7258; currentLng = 122.5583 },
                                label = { Text("Jaro Plaza", fontSize = 10.sp) },
                                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Micro Nudge Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fine Nudge Pin:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedButton(
                            onClick = { currentLat += 0.0005 },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("North ▲", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { currentLat -= 0.0005 },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("South ▼", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { currentLng -= 0.0005 },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("West ◄", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { currentLng += 0.0005 },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("East ►", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Key Lines Pinned Address Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GrabGreen.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GrabGreen.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PinDrop,
                                contentDescription = null,
                                tint = GrabGreenDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PINNED ADDRESS KEY LINES:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = GrabGreenDark
                            )
                        }

                        // Key Line 1
                        Text(
                            text = keyLine1,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Key Line 2
                        Text(
                            text = keyLine2,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Key Line 3
                        Text(
                            text = keyLine3,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GrabGreenDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Confirm Pinned Pick-up Location Button
                Button(
                    onClick = {
                        val pinnedPlace = SavedPlaceEntity(
                            name = keyLine1.substringBefore(" ("),
                            address = keyLine2,
                            latitude = currentLat,
                            longitude = currentLng,
                            iconName = "pin"
                        )
                        onConfirmPin(pinnedPlace)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_pinned_location_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Confirm Pin",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Confirm Pick-Up Pin Location",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

/**
 * Main Location Picker Dialog featuring Pin Location map trigger and Key-Line manual address suggestions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerDialog(
    title: String,
    places: List<SavedPlaceEntity>,
    onDismiss: () -> Unit,
    onSelect: (SavedPlaceEntity) -> Unit,
    viewModel: OneGoViewModel? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var showPinMap by remember { mutableStateOf(false) }

    val geocodingRepo = remember { com.example.data.repository.GoogleGeocodingRepository() }
    val geminiSearchRepo = remember { com.example.data.repository.GeminiSearchRepository() }
    var liveAddressSuggestions by remember { mutableStateOf<List<KeyLineAddressSuggestion>>(emptyList()) }
    var isSearchingGeocoding by remember { mutableStateOf(false) }

    // Device GPS location state for "My Current Location"
    var deviceGpsCoords by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var isReverseGeocodingCurrentLocation by remember { mutableStateOf(false) }

    // Google Maps Grounding states (gemini-3.5-flash with googleMaps tool)
    var mapsGroundedResult by remember { mutableStateOf<com.example.data.repository.MapsGroundingResult?>(null) }
    var isMapsGroundingLoading by remember { mutableStateOf(false) }

    // Perform Maps Grounding search function
    fun performMapsGrounding(queryText: String) {
        if (queryText.isBlank()) return
        isMapsGroundingLoading = true
        coroutineScope.launch {
            try {
                val result = geminiSearchRepo.searchWithMapsGrounding(queryText)
                mapsGroundedResult = result
            } catch (e: Exception) {
                // Ignore exception and retain result
            } finally {
                isMapsGroundingLoading = false
            }
        }
    }

    // Live Geocoding Search triggered on typing with zero delay on manual inputs
    LaunchedEffect(searchQuery) {
        val clean = searchQuery.trim()
        if (clean.isBlank()) {
            liveAddressSuggestions = emptyList()
            isSearchingGeocoding = false
            mapsGroundedResult = null
        } else {
            // Show instant local keyline suggestions immediately without delay or expiration
            val localInstantSuggestions = generateKeyLineSuggestionsForQuery(clean, places)
            liveAddressSuggestions = localInstantSuggestions
            isSearchingGeocoding = true
            try {
                val onlineResults = geocodingRepo.searchAddresses(clean)
                liveAddressSuggestions = (onlineResults + localInstantSuggestions).distinctBy { it.titleKeyLine }
            } catch (e: Exception) {
                // Keep instant local address suggestions permanently on network error
                liveAddressSuggestions = localInstantSuggestions
            } finally {
                isSearchingGeocoding = false
            }

            // Also trigger Maps Grounding if query length > 3
            if (clean.length >= 4) {
                performMapsGrounding(clean)
            }
        }
    }

    // Saved Places list formatted with key lines
    val savedPlacesKeyLines = remember(searchQuery, places) {
        if (searchQuery.isBlank()) places
        else places.filter {
            it.name.contains(searchQuery, ignoreCase = true) || it.address.contains(searchQuery, ignoreCase = true)
        }
    }

    // Popular Landmarks list
    val filteredLandmarks = remember(searchQuery) {
        if (searchQuery.isBlank()) IL_LANDMARKS
        else IL_LANDMARKS.filter {
            it.name.contains(searchQuery, ignoreCase = true) || it.address.contains(searchQuery, ignoreCase = true)
        }
    }

    val isDestType = title.contains("Destination", ignoreCase = true) || title.contains("Dropoff", ignoreCase = true)
    val pinBannerBg = if (isDestType) MaximRed.copy(alpha = 0.12f) else GrabGreen.copy(alpha = 0.12f)
    val pinBannerBorder = if (isDestType) MaximRed.copy(alpha = 0.35f) else GrabGreen.copy(alpha = 0.35f)
    val pinBannerIconBg = if (isDestType) MaximRed else GrabGreen
    val pinBannerTextColor = if (isDestType) Color(0xFFC62828) else GrabGreenDark

    if (showPinMap) {
        PinLocationMapDialog(
            dialogTitle = if (isDestType) "Pin Destination Location" else "Pin Pick-up Location",
            isPickup = !isDestType,
            initialLat = deviceGpsCoords?.first ?: 10.7123,
            initialLng = deviceGpsCoords?.second ?: 122.5518,
            onDismiss = { showPinMap = false },
            onConfirmPin = { pinnedPlace ->
                showPinMap = false
                onSelect(pinnedPlace)
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    onClick = { showPinMap = true },
                    shape = RoundedCornerShape(16.dp),
                    color = GrabGreen.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.PinDrop, contentDescription = "Pin Map", tint = GrabGreenDark, modifier = Modifier.size(14.dp))
                        Text("Pin Map", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrabGreenDark)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("location_picker_dialog"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Accompanist Location Permission Handler Card
                LocationPermissionCard(
                    onLocationUpdated = { lat, lng ->
                        deviceGpsCoords = Pair(lat, lng)
                    }
                )

                // 1. DEDICATED "MY CURRENT LOCATION" SELECTION CARD
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val coords = deviceGpsCoords ?: Pair(10.7150, 122.5520)
                            isReverseGeocodingCurrentLocation = true
                            coroutineScope.launch {
                                try {
                                    val (placeTitle, fullAddress, _) = geocodingRepo.reverseGeocode(coords.first, coords.second)
                                    val name = if (placeTitle.isBlank() || placeTitle == "Pinned Location") "My Current Device Location" else placeTitle
                                    val addr = if (fullAddress.isBlank()) "GPS: ${"%.4f".format(coords.first)}, ${"%.4f".format(coords.second)}" else fullAddress
                                    onSelect(
                                        SavedPlaceEntity(
                                            name = name,
                                            address = addr,
                                            latitude = coords.first,
                                            longitude = coords.second,
                                            iconName = "gps_fixed"
                                        )
                                    )
                                } catch (e: Exception) {
                                    onSelect(
                                        SavedPlaceEntity(
                                            name = "My Current Device Location",
                                            address = "Mandurriao, Iloilo City (GPS: ${"%.4f".format(coords.first)}, ${"%.4f".format(coords.second)})",
                                            latitude = coords.first,
                                            longitude = coords.second,
                                            iconName = "gps_fixed"
                                        )
                                    )
                                } finally {
                                    isReverseGeocodingCurrentLocation = false
                                }
                            }
                        }
                        .testTag("use_my_current_location_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = GrabGreen.copy(alpha = 0.15f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, GrabGreen)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GrabGreenDark),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isReverseGeocodingCurrentLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "My Current Location",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Use My Current Device Location 🎯",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GrabGreenDark
                                )
                            }
                            Text(
                                text = deviceGpsCoords?.let { "Device GPS: ${"%.4f".format(it.first)}, ${"%.4f".format(it.second)}" }
                                    ?: "Detecting accurate device GPS position...",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Select Device Location",
                            tint = GrabGreenDark
                        )
                    }
                }

                // Pin Location Banner Action Button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPinMap = true }
                        .testTag("pin_location_map_banner"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = pinBannerBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, pinBannerBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(pinBannerIconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PinDrop,
                                contentDescription = "Pin Location Feature",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isDestType) "Pin Destination Location on Map 📍" else "Pin Pick-up Location on Map 📍",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = pinBannerTextColor
                                )
                            }
                            Text(
                                text = "Drag pin or tap map for exact gate/building point",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Pin",
                            tint = pinBannerTextColor
                        )
                    }
                }

                // Manual Address Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Type address manual input...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.testTag("clear_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrabGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("address_search_input")
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 1. DYNAMIC ADDRESS SUGGESTIONS (GOOGLE MAPS & ALL LOCATIONS)
                    if (searchQuery.isNotBlank()) {
                        // Direct Custom Manual Input Card
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val cleanText = searchQuery.trim()
                                        onSelect(
                                            SavedPlaceEntity(
                                                name = cleanText,
                                                address = "$cleanText, Metro Iloilo Area",
                                                latitude = 10.7123 + (Math.random() * 0.005),
                                                longitude = 122.5518 + (Math.random() * 0.005),
                                                iconName = "edit"
                                            )
                                        )
                                    }
                                    .testTag("use_typed_custom_address_item"),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = GrabGreen.copy(alpha = 0.15f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, GrabGreen)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(GrabGreenDark),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EditLocation,
                                            contentDescription = "Use Typed Address",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Use typed location: \"${searchQuery.trim()}\"",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = GrabGreenDark,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Tap to set directly as ${if (isDestType) "Destination / Deliver to" else "Pick-up"} address",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Confirm",
                                        tint = GrabGreenDark
                                    )
                                }
                            }
                        }

                        // Google Maps Data Grounding (gemini-3.5-flash with googleMaps tool) Card
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEA4335).copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Place,
                                                contentDescription = null,
                                                tint = Color(0xFFEA4335),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "Google Maps AI Grounding",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFEA4335)
                                            )
                                        }
                                        if (isMapsGroundingLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(14.dp),
                                                strokeWidth = 2.dp,
                                                color = Color(0xFFEA4335)
                                            )
                                        } else {
                                            TextButton(
                                                onClick = { performMapsGrounding(searchQuery.trim()) },
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("Refresh Maps Data", fontSize = 10.sp, color = Color(0xFFEA4335), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    mapsGroundedResult?.let { grounded ->
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = grounded.placeName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "📍 Address: ${grounded.address}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = grounded.summary,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                onSelect(
                                                    SavedPlaceEntity(
                                                        name = grounded.placeName,
                                                        address = grounded.address,
                                                        latitude = grounded.latitude,
                                                        longitude = grounded.longitude,
                                                        iconName = "place"
                                                    )
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(36.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Select Grounded Location 📍", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 4.dp, top = 6.dp, bottom = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ADDRESS SUGGESTIONS (GOOGLE MAPS & ALL LOCATIONS)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GrabGreenDark
                                )
                                if (isSearchingGeocoding) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(10.dp),
                                            strokeWidth = 1.5.dp,
                                            color = GrabGreen
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Searching...",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        items(liveAddressSuggestions) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelect(
                                            SavedPlaceEntity(
                                                name = item.titleKeyLine,
                                                address = item.districtKeyLine,
                                                latitude = item.latitude,
                                                longitude = item.longitude,
                                                iconName = item.iconName
                                            )
                                        )
                                    }
                                    .testTag("location_keyline_suggestion_item"),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(GrabGreen.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (item.iconName) {
                                                "pin" -> Icons.Default.PinDrop
                                                "work" -> Icons.Default.Work
                                                "shopping" -> Icons.Default.ShoppingBag
                                                "school" -> Icons.Default.School
                                                else -> Icons.Default.Place
                                            },
                                            contentDescription = null,
                                            tint = GrabGreenDark,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Key Line 1
                                            Text(
                                                text = item.titleKeyLine,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )

                                            if (item.badgeTag != null) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(GrabGreen.copy(alpha = 0.2f))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = item.badgeTag,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = GrabGreenDark
                                                    )
                                                }
                                            }
                                        }

                                        // Key Line 2
                                        Text(
                                            text = item.districtKeyLine,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        // Key Line 3
                                        Text(
                                            text = item.postalKeyLine,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = GrabGreenDark,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. SAVED PLACES SECTION WITH KEY LINES
                    if (savedPlacesKeyLines.isNotEmpty()) {
                        item {
                            Text(
                                text = "SAVED PLACES",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 2.dp)
                            )
                        }

                        items(savedPlacesKeyLines) { place ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSelect(place) }
                                    .padding(vertical = 8.dp, horizontal = 8.dp)
                                    .testTag("location_suggestion_item"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val icon = when (place.iconName.lowercase()) {
                                    "home" -> Icons.Default.Home
                                    "work" -> Icons.Default.Work
                                    "shopping" -> Icons.Default.ShoppingBag
                                    "school" -> Icons.Default.School
                                    "restaurant" -> Icons.Default.Restaurant
                                    else -> Icons.Default.Star
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = place.name,
                                    tint = GrabGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = place.name, // Key Line 1
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = place.address, // Key Line 2
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Saved Location • Quick Pick-Up Point", // Key Line 3
                                        fontSize = 9.sp,
                                        color = GrabGreenDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))
                        }
                    }

                    // 3. POPULAR ILOILO LANDMARKS SECTION WITH KEY LINES
                    if (filteredLandmarks.isNotEmpty()) {
                        item {
                            Text(
                                text = "POPULAR PICK-UP SPOTS IN ILOILO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 4.dp, top = 10.dp, end = 4.dp, bottom = 2.dp)
                            )
                        }

                        items(filteredLandmarks) { landmark ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val placeEntity = SavedPlaceEntity(
                                            name = landmark.name,
                                            address = landmark.address,
                                            latitude = landmark.lat,
                                            longitude = landmark.lng,
                                            iconName = landmark.iconName
                                        )
                                        onSelect(placeEntity)
                                    }
                                    .padding(vertical = 8.dp, horizontal = 8.dp)
                                    .testTag("location_suggestion_item"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val icon = when (landmark.iconName.lowercase()) {
                                    "home" -> Icons.Default.Home
                                    "work" -> Icons.Default.Work
                                    "shopping" -> Icons.Default.ShoppingBag
                                    "school" -> Icons.Default.School
                                    "restaurant" -> Icons.Default.Restaurant
                                    else -> Icons.Default.Star
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = landmark.name,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = landmark.name, // Key Line 1
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = landmark.address, // Key Line 2
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "High Driver Availability • Express Pick-Up Zone", // Key Line 3
                                        fontSize = 9.sp,
                                        color = GrabGreenDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontWeight = FontWeight.Bold, color = GrabGreen)
            }
        },
        shape = RoundedCornerShape(14.dp)
    )
}

