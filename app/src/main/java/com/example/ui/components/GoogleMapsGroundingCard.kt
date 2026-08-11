package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Place
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.SavedPlaceEntity
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.GrabGreenDark
import com.example.ui.viewmodel.OneGoViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GoogleMapsGroundingCard(
    viewModel: OneGoViewModel,
    modifier: Modifier = Modifier,
    initialLocationQuery: String = "SM City Iloilo Mandurriao",
    onLocationSelected: ((SavedPlaceEntity) -> Unit)? = null
) {
    val context = LocalContext.current
    var locationQuery by remember { mutableStateOf(initialLocationQuery) }
    val mapsResult by viewModel.mapsGroundingState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isMapsGroundingLoadingState.collectAsStateWithLifecycle()

    val quickPlacePrompts = listOf(
        "SM City Iloilo 🏬",
        "Festive Walk Mall 🛍️",
        "Atria Park District 🌳",
        "Iloilo Airport ✈️"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("google_maps_grounding_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, Color(0xFFEA4335).copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFEA4335), Color(0xFF4285F4))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Maps Grounding",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Google Maps Data Grounding",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "gemini-3.5-flash with googleMaps tool",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFEA4335)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEA4335).copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFEA4335),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "VERIFIED MAPS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFEA4335)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Location Chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                quickPlacePrompts.forEach { prompt ->
                    val cleanPrompt = prompt.replace(Regex("[^a-zA-Z0-9 ]"), "").trim()
                    FilterChip(
                        selected = locationQuery.contains(cleanPrompt, ignoreCase = true),
                        onClick = {
                            locationQuery = cleanPrompt
                            viewModel.performMapsGrounding(cleanPrompt)
                        },
                        label = { Text(prompt, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEA4335),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Address Search Box
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = locationQuery,
                    onValueChange = { locationQuery = it },
                    placeholder = { Text("Search address or place in Maps...", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFEA4335),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )

                Button(
                    onClick = {
                        if (locationQuery.isNotBlank()) {
                            viewModel.performMapsGrounding(locationQuery)
                        } else {
                            Toast.makeText(context, "Please enter a location query!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA4335)),
                    modifier = Modifier.testTag("maps_grounding_search_btn")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Map, contentDescription = "Search Maps", modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Maps Grounding Result Section
            AnimatedVisibility(
                visible = mapsResult != null || isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(12.dp)
                ) {
                    if (isLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFFEA4335), strokeWidth = 2.dp)
                            Text("Querying Google Maps Grounding data...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else if (mapsResult != null) {
                        val result = mapsResult!!

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEA4335), modifier = Modifier.size(16.dp))
                            Text(
                                text = result.placeName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "📍 Address: ${result.address}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = result.summary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (onLocationSelected != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    val entity = SavedPlaceEntity(
                                        name = result.placeName,
                                        address = result.address,
                                        latitude = result.latitude,
                                        longitude = result.longitude,
                                        iconName = "pin"
                                    )
                                    onLocationSelected(entity)
                                    Toast.makeText(context, "Selected ${result.placeName}!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("select_maps_grounded_place_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Select Grounded Location 📍", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        if (result.mapSources.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Google Maps Web Citations:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEA4335)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                result.mapSources.forEach { source ->
                                    SuggestionChip(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(source.url))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Opening Google Maps link...", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        label = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(source.title, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(10.dp))
                                            }
                                        },
                                        border = BorderStroke(1.dp, Color(0xFFEA4335).copy(alpha = 0.4f))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
