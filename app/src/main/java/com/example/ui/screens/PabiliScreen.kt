package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.SavedPlaceEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.OneGoViewModel
import com.example.ui.viewmodel.PabiliItem
import com.example.ui.viewmodel.PabiliOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PabiliScreen(
    viewModel: OneGoViewModel,
    onNavigateBack: () -> Unit
) {
    val origin by viewModel.selectedOrigin.collectAsStateWithLifecycle()
    val destination by viewModel.selectedDestination.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Store / Market Location state
    var storeName by remember { mutableStateOf("SM Supermarket Mandurriao") }

    // Dialog control states
    var showStoreLocationPicker by remember { mutableStateOf(false) }
    var showPinMapForStore by remember { mutableStateOf(false) }
    var showDeliveryLocationPicker by remember { mutableStateOf(false) }
    var showPinMapForDelivery by remember { mutableStateOf(false) }

    // Manual Direct Text Input dialog states
    var showManualStoreDialog by remember { mutableStateOf(false) }
    var showManualDeliveryDialog by remember { mutableStateOf(false) }
    var manualAddressInputText by remember { mutableStateOf("") }

    // Shopping List items
    var shoppingItems by remember {
        mutableStateOf(
            listOf(
                PabiliItem(name = "Paracetamol 500mg (1 Box)", estimatedPrice = 120.0, quantity = 1, notes = "Biogesic preferred"),
                PabiliItem(name = "Fresh Milk 1L", estimatedPrice = 95.0, quantity = 2, notes = "Full Cream"),
                PabiliItem(name = "Pandesal (1 Bag)", estimatedPrice = 50.0, quantity = 1, notes = "Warm if available")
            )
        )
    }

    var showAddItemDialog by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }
    var newItemPrice by remember { mutableStateOf("") }
    var newItemQty by remember { mutableStateOf(1) }
    var newItemNotes by remember { mutableStateOf("") }

    // Contact info
    var recipientName by remember { mutableStateOf("Juan Dela Cruz") }
    var recipientPhone by remember { mutableStateOf("0917-555-0199") }
    var driverNotes by remember { mutableStateOf("Please ask driver for official store receipt upon purchase.") }

    // Price sort mode: 0 = Price, 1 = ETA
    var priceSortMode by remember { mutableStateOf(0) }

    // Active option for instant booking
    var activeOptionForBooking by remember { mutableStateOf<PabiliOption?>(null) }

    // Compute items total
    val itemsTotalCost = remember(shoppingItems) { shoppingItems.sumOf { it.estimatedPrice * it.quantity } }

    // Compared Pabili Options
    val rawPabiliOptions = remember(viewModel, itemsTotalCost, origin, destination) {
        viewModel.getComparedPabili(itemsTotalCost)
    }

    val sortedPabiliOptions = remember(rawPabiliOptions, priceSortMode) {
        if (priceSortMode == 0) rawPabiliOptions.sortedBy { it.totalDeliveryAndServiceFee }
        else rawPabiliOptions.sortedBy { it.etaMins }
    }

    val cheapestOption = remember(sortedPabiliOptions) { sortedPabiliOptions.minByOrNull { it.totalDeliveryAndServiceFee } }
    val fastestOption = remember(sortedPabiliOptions) { sortedPabiliOptions.minByOrNull { it.etaMins } }

    val savedPlaces by viewModel.savedPlacesState.collectAsStateWithLifecycle()

    // Handlers for Store & Delivery Pin Maps
    if (showPinMapForStore) {
        PinLocationMapDialog(
            dialogTitle = "Pin Store / Market Location",
            isPickup = true,
            initialLat = origin?.latitude ?: 10.7123,
            initialLng = origin?.longitude ?: 122.5518,
            onDismiss = { showPinMapForStore = false },
            onConfirmPin = { pinnedPlace ->
                showPinMapForStore = false
                viewModel.selectOrigin(pinnedPlace)
                storeName = pinnedPlace.name
            }
        )
    }

    if (showPinMapForDelivery) {
        PinLocationMapDialog(
            dialogTitle = "Pin Deliver To Location",
            isPickup = false,
            initialLat = destination?.latitude ?: 10.7123,
            initialLng = destination?.longitude ?: 122.5518,
            onDismiss = { showPinMapForDelivery = false },
            onConfirmPin = { pinnedPlace ->
                showPinMapForDelivery = false
                viewModel.selectDestination(pinnedPlace)
            }
        )
    }

    // Location Picker Dialogs
    if (showStoreLocationPicker) {
        LocationPickerDialog(
            title = "Select Store / Shopping Location",
            places = savedPlaces,
            onDismiss = { showStoreLocationPicker = false },
            onSelect = {
                showStoreLocationPicker = false
                viewModel.selectOrigin(it)
                storeName = it.name
            }
        )
    }

    if (showDeliveryLocationPicker) {
        LocationPickerDialog(
            title = "Select Deliver To Location",
            places = savedPlaces,
            onDismiss = { showDeliveryLocationPicker = false },
            onSelect = {
                showDeliveryLocationPicker = false
                viewModel.selectDestination(it)
            }
        )
    }

    // Direct Manual Address Input Dialog for Store
    if (showManualStoreDialog) {
        LocationPickerDialog(
            title = "Manual Input Store / Market Address",
            places = savedPlaces,
            onDismiss = { showManualStoreDialog = false },
            onSelect = { selectedPlace ->
                storeName = selectedPlace.name
                viewModel.selectOrigin(selectedPlace)
                showManualStoreDialog = false
            }
        )
    }

    // Direct Manual Address Input Dialog for Delivery
    if (showManualDeliveryDialog) {
        LocationPickerDialog(
            title = "Manual Input Deliver To Address",
            places = savedPlaces,
            onDismiss = { showManualDeliveryDialog = false },
            onSelect = { selectedPlace ->
                viewModel.selectDestination(selectedPlace)
                showManualDeliveryDialog = false
            }
        )
    }

    // Add Item Dialog
    if (showAddItemDialog) {
        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = { Text("Add Item to Shopping List 🛍️", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        label = { Text("Item Name *") },
                        placeholder = { Text("e.g. Paracetamol 500mg, Bread, Milk") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newItemPrice,
                            onValueChange = { newItemPrice = it },
                            label = { Text("Est. Price (₱)") },
                            placeholder = { Text("150.00") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        // Quantity Stepper
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            IconButton(
                                onClick = { if (newItemQty > 1) newItemQty-- },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Text("-", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Text("$newItemQty", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                            IconButton(
                                onClick = { newItemQty++ },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Text("+", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newItemNotes,
                        onValueChange = { newItemNotes = it },
                        label = { Text("Notes / Brand Preference (Optional)") },
                        placeholder = { Text("e.g. Biogesic brand, low fat milk") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newItemName.trim()
                        if (name.isNotBlank()) {
                            val price = newItemPrice.toDoubleOrNull() ?: 50.0
                            shoppingItems = shoppingItems + PabiliItem(
                                name = name,
                                estimatedPrice = price,
                                quantity = newItemQty,
                                notes = newItemNotes.trim()
                            )
                            newItemName = ""
                            newItemPrice = ""
                            newItemQty = 1
                            newItemNotes = ""
                            showAddItemDialog = false
                        } else {
                            Toast.makeText(context, "Please enter item name", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GrabGreen)
                ) {
                    Text("Add to List", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Pabili Service", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GrabGreen)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("BUY FOR ME", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = Modifier.testTag("pabili_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Hero Pabili Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(GrabGreen, GrabGreenDark)
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = "Pabili Service",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pabili Assistant 🛍️",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Need items bought or picked up? Drivers purchase items for you up to ₱2,000 cash on delivery!",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // SECTION 1: WHERE TO BUY (STORE LOCATION & MANUAL INPUT)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = GrabGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("1. WHERE TO BUY (STORE / MARKET)", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = GrabGreen)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Store Name TextField
                        OutlinedTextField(
                            value = storeName,
                            onValueChange = { storeName = it },
                            label = { Text("Store / Merchant Name") },
                            placeholder = { Text("e.g. SM Supermarket, Mercury Drug, Jollibee") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Store Address Location Picker Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = GrabGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("STORE ADDRESS LOCATION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GrabGreen)
                                Text(
                                    text = origin?.name ?: storeName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = origin?.address ?: "Metro Iloilo Shopping Zone",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Manual input buttons
                            Row {
                                IconButton(
                                    onClick = {
                                        manualAddressInputText = origin?.name ?: storeName
                                        showManualStoreDialog = true
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Manual Input Store", tint = GrabGreen, modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { showStoreLocationPicker = true },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = "Search Store Map", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { showPinMapForStore = true },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.PinDrop, contentDescription = "Pin Store", tint = MaximRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 2: SHOPPING LIST (WHAT TO BUY)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FormatListNumbered, contentDescription = null, tint = GrabGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("2. SHOPPING LIST (WHAT TO BUY)", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = GrabGreen)
                            }

                            Button(
                                onClick = { showAddItemDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Add Item", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (shoppingItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No items added yet. Tap '+ Add Item' to create list.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                shoppingItems.forEachIndexed { idx, item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${idx + 1}.", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GrabGreenDark)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Row {
                                                Text("Qty: ${item.quantity} • Est. ₱${"%.2f".format(item.estimatedPrice * item.quantity)} (Range: ${item.priceRange})", fontSize = 11.sp, color = GrabGreenDark, fontWeight = FontWeight.SemiBold)
                                                if (item.notes.isNotBlank()) {
                                                    Text(" (${item.notes})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                shoppingItems = shoppingItems.filter { it.id != item.id }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaximRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        // Items Budget Summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("ESTIMATED ITEMS COST", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Driver Advances Payment", fontSize = 10.sp, color = GrabGreenDark)
                            }
                            Text("₱${"%.2f".format(itemsTotalCost)}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = GrabGreenDark)
                        }
                    }
                }
            }

            // SECTION 3: DELIVER TO ADDRESS (DELIVERY DESTINATION)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaximRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("3. DELIVER TO ADDRESS", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = MaximRed)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Deliver To Address Location Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = MaximRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("DELIVERY DESTINATION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaximRed)
                                Text(
                                    text = destination?.name ?: "Select Delivery Location...",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = destination?.address ?: "Tap edit/search to specify exact address",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        manualAddressInputText = destination?.name ?: ""
                                        showManualDeliveryDialog = true
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Manual Input Destination", tint = GrabGreen, modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { showDeliveryLocationPicker = true },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = "Search Destination Map", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { showPinMapForDelivery = true },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.PinDrop, contentDescription = "Pin Destination", tint = MaximRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Recipient Contact Name & Phone
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = recipientName,
                                onValueChange = { recipientName = it },
                                label = { Text("Recipient Name") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = recipientPhone,
                                onValueChange = { recipientPhone = it },
                                label = { Text("Contact Number") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // SECTION 4: PROVIDER AGGREGATOR & FEE COMPARISON
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("4. CHOOSE PABILI PARTNER", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)

                        // Sort toggle buttons
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(2.dp)
                        ) {
                            FilterChip(
                                selected = priceSortMode == 0,
                                onClick = { priceSortMode = 0 },
                                label = { Text("Cheapest", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.height(28.dp)
                            )
                            FilterChip(
                                selected = priceSortMode == 1,
                                onClick = { priceSortMode = 1 },
                                label = { Text("Fastest", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // LIST OF COMPARED PABILI OPTIONS
            items(sortedPabiliOptions) { option ->
                val platformColor = when (option.platformId) {
                    "grab_pabili" -> GrabGreen
                    "joyride_pabili" -> JoyrideYellow
                    "maxim_pabili" -> MaximRed
                    "lalamove_pabili" -> LalamoveOrange
                    "moveit_pabili" -> MoveItRed
                    else -> GrabGreen
                }

                val grandTotal = itemsTotalCost + option.totalDeliveryAndServiceFee
                val isCheapest = option == cheapestOption
                val isFastest = option == fastestOption

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                        .testTag("pabili_option_card_${option.platformId}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isCheapest || option.platformId == "grab_pabili") 1.5.dp else 1.dp,
                        color = if (isCheapest) GrabGreen else if (option.platformId == "grab_pabili") GrabGreen.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(platformColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = platformColor, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(option.platformName, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                                    Text(option.serviceName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            // Price breakdown
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Delivery: ₱${"%.2f".format(option.totalDeliveryAndServiceFee)}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = platformColor)
                                Text("Grand Total: ₱${"%.2f".format(grandTotal)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (option.badgeLabel != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(platformColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(option.badgeLabel, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = platformColor)
                                    }
                                }

                                if (isCheapest) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(GrabGreen)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("CHEAPEST 💰", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                                    }
                                }

                                if (isFastest) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF0288D1))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("FASTEST ⚡", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                                    }
                                }
                            }

                            Text("Pick-up: ${option.pickupEtaRange} • Delivery: ${option.deliveryEtaRange}", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        // Action Buttons: Open App vs Instant Book
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.launchPlatform(
                                        context = context,
                                        platformId = option.platformId,
                                        platformName = option.platformName,
                                        price = grandTotal,
                                        customDeepLinkUrl = option.deepLinkUrl
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Open App", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    activeOptionForBooking = option
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = platformColor)
                            ) {
                                Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Instant Book", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Instant Booking Sheet for Pabili
    activeOptionForBooking?.let { option ->
        val grandTotal = itemsTotalCost + option.totalDeliveryAndServiceFee
        InstantBookingDialog(
            visible = true,
            platformId = option.platformId,
            platformName = option.platformName,
            serviceName = "${storeName} ➔ ${destination?.name ?: "Delivery Target"}",
            estimatedFare = grandTotal,
            category = "PABILI",
            viewModel = viewModel,
            onDismiss = { activeOptionForBooking = null }
        )
    }
}
