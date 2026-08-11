package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.database.SavedPlaceEntity
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.GrabGreenDark

data class PresetStorePhoto(
    val id: String,
    val title: String,
    val drawableRes: Int
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddStoreDialog(
    onDismiss: () -> Unit,
    savedPlaces: List<SavedPlaceEntity> = emptyList(),
    onAddStore: (
        name: String,
        cuisines: String,
        sampleItemName: String,
        itemPrice: Double,
        selectedCategories: List<String>,
        imageUri: String?,
        address: String,
        latitude: Double,
        longitude: Double
    ) -> Unit
) {
    val context = LocalContext.current
    var storeName by remember { mutableStateOf("") }
    var cuisinesInput by remember { mutableStateOf("") }
    var sampleItemInput by remember { mutableStateOf("") }
    var itemPriceInput by remember { mutableStateOf("150") }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Store Location & Address State
    var storeAddress by remember { mutableStateOf("Tap to set store location / address") }
    var storeLat by remember { mutableStateOf(10.7123) }
    var storeLng by remember { mutableStateOf(122.5518) }
    var showStoreLocationPicker by remember { mutableStateOf(false) }
    var showPinMapForStore by remember { mutableStateOf(false) }

    // Multiple category selection state
    val availableCategories = remember {
        listOf(
            "Burgers & Fast Food",
            "Chicken & BBQ",
            "Coffee & Tea",
            "Rice Meals",
            "Pizza & Pasta",
            "Noodles & Chinese",
            "Desserts & Bakery"
        )
    }
    val selectedCategories = remember { mutableStateListOf<String>("Burgers & Fast Food") }

    // Preset food cover photos
    val presetPhotos = remember {
        listOf(
            PresetStorePhoto("p1", "Burger & Fries", R.drawable.img_resto_jollibee_mcd_1783037783020),
            PresetStorePhoto("p2", "Crispy Chicken", R.drawable.img_resto_mang_inasal_1783037802204),
            PresetStorePhoto("p3", "Noodles & Meals", R.drawable.img_resto_chowking_1783037814473),
            PresetStorePhoto("p4", "Pizza & Pasta", R.drawable.img_resto_greenwich_1783037826680),
            PresetStorePhoto("p5", "Promo Banner", R.drawable.img_promo_banner)
        )
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri.toString()
            Toast.makeText(context, "Image uploaded!", Toast.LENGTH_SHORT).show()
        }
    }

    if (showStoreLocationPicker) {
        LocationPickerDialog(
            title = "Select Store Location / Address",
            places = savedPlaces,
            onDismiss = { showStoreLocationPicker = false },
            onSelect = { selectedPlace ->
                storeAddress = selectedPlace.address
                storeLat = selectedPlace.latitude
                storeLng = selectedPlace.longitude
                showStoreLocationPicker = false
            }
        )
    }

    if (showPinMapForStore) {
        PinLocationMapDialog(
            dialogTitle = "Pin Store Location on Map",
            initialLat = storeLat,
            initialLng = storeLng,
            onDismiss = { showPinMapForStore = false },
            onConfirmPin = { pinnedPlace ->
                storeAddress = pinnedPlace.address
                storeLat = pinnedPlace.latitude
                storeLng = pinnedPlace.longitude
                showPinMapForStore = false
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f)
                .testTag("add_store_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(GrabGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddBusiness,
                                contentDescription = null,
                                tint = GrabGreenDark,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Register Your Store 🏪",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Add store to OneGo Food Aggregator",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_add_store_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Form Scrollable Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Store Name
                    OutlinedTextField(
                        value = storeName,
                        onValueChange = {
                            storeName = it
                            errorMessage = null
                        },
                        label = { Text("Store / Restaurant Name *") },
                        placeholder = { Text("e.g. Ka-Juan's Wings & Milk Tea") },
                        leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = GrabGreen) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("store_name_input")
                    )

                    // Cuisines / Description
                    OutlinedTextField(
                        value = cuisinesInput,
                        onValueChange = { cuisinesInput = it },
                        label = { Text("Cuisines / Specialties") },
                        placeholder = { Text("e.g. Crispy Wings, Milk Tea, Rice Bowls") },
                        leadingIcon = { Icon(Icons.Default.Fastfood, contentDescription = null, tint = GrabGreen) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("store_cuisines_input")
                    )

                    // Popular Item & Starting Price
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = sampleItemInput,
                            onValueChange = { sampleItemInput = it },
                            label = { Text("Specialty Dish / Item") },
                            placeholder = { Text("e.g. 6pc Wings Combo") },
                            leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null, tint = GrabGreen) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("store_sample_item_input")
                        )

                        OutlinedTextField(
                            value = itemPriceInput,
                            onValueChange = { itemPriceInput = it },
                            label = { Text("Price (₱) *") },
                            leadingIcon = { Text("₱", fontWeight = FontWeight.Bold, color = GrabGreen, modifier = Modifier.padding(start = 12.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(0.9f)
                                .testTag("store_item_price_input")
                        )
                    }

                    // --- SECTION: ADD STORE LOCATION & ADDRESS ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        border = BorderStroke(1.dp, GrabGreen.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = GrabGreen, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Add Store Location & Address *",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Current selected store address display
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showStoreLocationPicker = true },
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Storefront, contentDescription = null, tint = GrabGreen, modifier = Modifier.size(20.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (storeAddress == "Tap to set store location / address") "Set Store Address" else storeAddress,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (storeAddress == "Tap to set store location / address") MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "GPS Coords: ${"%.4f".format(storeLat)}, ${"%.4f".format(storeLng)}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Location", tint = GrabGreen, modifier = Modifier.size(16.dp))
                                }
                            }

                            // Address Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showStoreLocationPicker = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("add_store_location_search_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = GrabGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Search Address 🔍", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { showPinMapForStore = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("add_store_location_pin_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.PinDrop, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pin on Map 📍", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // --- SECTION 1: MULTIPLE FOOD CATEGORY SELECTION ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        border = BorderStroke(1.dp, GrabGreen.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Category, contentDescription = null, tint = GrabGreen, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Select Food Categories * (Multiple Selection)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "Your store will appear whenever users filter by ANY of your selected categories inside the Food Aggregator:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                availableCategories.forEach { category ->
                                    val isSelected = selectedCategories.contains(category)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            if (isSelected) {
                                                if (selectedCategories.size > 1) {
                                                    selectedCategories.remove(category)
                                                } else {
                                                    Toast.makeText(context, "Select at least 1 category", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                selectedCategories.add(category)
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = category,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = GrabGreen,
                                            selectedLabelColor = Color.White,
                                            selectedLeadingIconColor = Color.White
                                        ),
                                        modifier = Modifier.testTag("category_chip_$category")
                                    )
                                }
                            }
                        }
                    }

                    // --- SECTION 2: UPLOAD STORE IMAGE / SELECT PRESET ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = GrabGreen, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Store Cover Image",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Upload Button from Device Gallery
                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("upload_store_photo_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text("Upload Image from Device Gallery 🖼️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Text(
                                text = "Or select a high-resolution food banner preset:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Preset Image Selection List
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(presetPhotos) { preset ->
                                    val isSelected = selectedImageUri == "preset_${preset.id}"
                                    Card(
                                        modifier = Modifier
                                            .width(90.dp)
                                            .height(70.dp)
                                            .clickable {
                                                selectedImageUri = "preset_${preset.id}"
                                            },
                                        shape = RoundedCornerShape(8.dp),
                                        border = if (isSelected) BorderStroke(2.dp, GrabGreen) else null
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            Image(
                                                painter = painterResource(id = preset.drawableRes),
                                                contentDescription = preset.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .align(Alignment.BottomCenter)
                                                    .background(Color.Black.copy(alpha = 0.65f))
                                                    .padding(2.dp)
                                            ) {
                                                Text(
                                                    text = preset.title,
                                                    fontSize = 8.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Image Preview Box
                            if (selectedImageUri != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, GrabGreen, RoundedCornerShape(8.dp))
                                ) {
                                    if (selectedImageUri!!.startsWith("preset_")) {
                                        val presetId = selectedImageUri!!.removePrefix("preset_")
                                        val preset = presetPhotos.find { it.id == presetId }
                                        if (preset != null) {
                                            Image(
                                                painter = painterResource(id = preset.drawableRes),
                                                contentDescription = "Selected Cover",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    } else {
                                        AsyncImage(
                                            model = selectedImageUri,
                                            contentDescription = "Uploaded Cover",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .background(GrabGreen, CircleShape)
                                            .padding(4.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val cleanName = storeName.trim()
                            val cleanPrice = itemPriceInput.toDoubleOrNull() ?: 150.0
                            val cleanAddr = if (storeAddress == "Tap to set store location / address") "$cleanName, Metro Iloilo" else storeAddress
                            if (cleanName.length < 2) {
                                errorMessage = "Please enter a valid store name."
                            } else if (selectedCategories.isEmpty()) {
                                errorMessage = "Please select at least 1 food category."
                            } else {
                                onAddStore(
                                    cleanName,
                                    cuisinesInput.ifBlank { selectedCategories.joinToString(", ") },
                                    sampleItemInput.ifBlank { "Specialty Dish" },
                                    cleanPrice,
                                    selectedCategories.toList(),
                                    selectedImageUri,
                                    cleanAddr,
                                    storeLat,
                                    storeLng
                                )
                                Toast.makeText(context, "🎉 Store '$cleanName' published to OneGo!", Toast.LENGTH_LONG).show()
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("submit_add_store_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GrabGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Publish Store 🚀", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
