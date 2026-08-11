package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import com.example.data.database.SavedPlaceEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.OneGoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPlacesScreen(
    viewModel: OneGoViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val savedPlaces by viewModel.savedPlacesState.collectAsStateWithLifecycle()

    var showAddForm by remember { mutableStateOf(false) }

    var inputName by remember { mutableStateOf("") }
    var inputAddress by remember { mutableStateOf("") }
    var inputLat by remember { mutableStateOf("10.7123") }
    var inputLng by remember { mutableStateOf("122.5518") }
    var selectedIcon by remember { mutableStateOf("home") }

    val iconCategories = listOf(
        IconCategory("home", Icons.Default.Home, "Home"),
        IconCategory("work", Icons.Default.Work, "Work"),
        IconCategory("school", Icons.Default.School, "School"),
        IconCategory("shopping", Icons.Default.ShoppingBag, "Shop"),
        IconCategory("restaurant", Icons.Default.Restaurant, "Eat"),
        IconCategory("star", Icons.Default.Star, "Fav")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Saved Places",
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
                    IconButton(onClick = { showAddForm = !showAddForm }) {
                        Icon(
                            imageVector = if (showAddForm) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Add Custom Location",
                            tint = GrabGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = Modifier.testTag("saved_places_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Expandable Add Saved Location Form
            AnimatedVisibility(
                visible = showAddForm,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Add Custom Saved Location",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = GrabGreenDark
                        )

                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            label = { Text("Location Tag (e.g. My College)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrabGreen,
                                focusedLabelColor = GrabGreenDark
                            )
                        )

                        OutlinedTextField(
                            value = inputAddress,
                            onValueChange = { inputAddress = it },
                            label = { Text("Street Address (e.g. Jaro, Iloilo City)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrabGreen,
                                focusedLabelColor = GrabGreenDark
                            )
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = inputLat,
                                onValueChange = { inputLat = it },
                                label = { Text("Latitude") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GrabGreen
                                )
                            )

                            OutlinedTextField(
                                value = inputLng,
                                onValueChange = { inputLng = it },
                                label = { Text("Longitude") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GrabGreen
                                )
                            )
                        }

                        // Icon Category Selector Grid
                        Text(
                            text = "Select Icon Theme",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            iconCategories.forEach { category ->
                                val isSelected = category.id == selectedIcon
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { selectedIcon = category.id }
                                        .padding(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) GrabGreen else Color.LightGray.copy(alpha = 0.2f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = category.icon,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = category.label, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (inputName.isBlank() || inputAddress.isBlank()) {
                                    Toast.makeText(context, "Please fill in all location fields!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val cleanLat = inputLat.trim().replace(',', '.')
                                val cleanLng = inputLng.trim().replace(',', '.')
                                val latDouble = cleanLat.toDoubleOrNull() ?: 10.7123
                                val lngDouble = cleanLng.toDoubleOrNull() ?: 122.5518

                                viewModel.addSavedPlace(
                                    name = inputName,
                                    address = inputAddress,
                                    lat = latDouble,
                                    lng = lngDouble,
                                    iconName = selectedIcon
                                )

                                // Clear fields
                                inputName = ""
                                inputAddress = ""
                                inputLat = "10.7"
                                inputLng = "122.5"
                                showAddForm = false
                                Toast.makeText(context, "Location Saved successfully!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Save Location Details", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Saved Locations Title Label
            Text(
                text = "My Saved Addresses",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp)
            )

            // Saved locations list
            if (savedPlaces.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No saved addresses yet.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(savedPlaces) { place ->
                        SavedPlaceRowItem(
                            place = place,
                            onDelete = {
                                viewModel.deleteSavedPlace(place.id)
                                Toast.makeText(context, "Address deleted.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

data class IconCategory(
    val id: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)

@Composable
fun SavedPlaceRowItem(place: SavedPlaceEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_place_item_${place.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Customized category icon
                val icon = when (place.iconName) {
                    "home" -> Icons.Default.Home
                    "work" -> Icons.Default.Work
                    "school" -> Icons.Default.School
                    "shopping" -> Icons.Default.ShoppingBag
                    "restaurant" -> Icons.Default.Restaurant
                    else -> Icons.Default.Star
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GrabGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = place.name,
                        tint = GrabGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = place.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = place.address,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Coords: ${"%.4f".format(place.latitude)}, ${"%.4f".format(place.longitude)}",
                        fontSize = 9.sp,
                        color = GrabGreenDark
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaximRed.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
