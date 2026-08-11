package com.example.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

private val GrabGreen = Color(0xFF00B14F)
private val GrabGreenDark = Color(0xFF00853B)
private val GrabNavy = Color(0xFF1B2238)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionCard(
    modifier: Modifier = Modifier,
    onLocationUpdated: ((Double, Double) -> Unit)? = null
) {
    val context = LocalContext.current
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    var currentCoordinates by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var isFetchingLocation by remember { mutableStateOf(false) }

    // Fetch device GPS location when fine permission is granted
    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            isFetchingLocation = true
            fetchCurrentGpsLocation(context) { lat, lng ->
                currentCoordinates = Pair(lat, lng)
                isFetchingLocation = false
                onLocationUpdated?.invoke(lat, lng)
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = if (locationPermissionsState.allPermissionsGranted) GrabGreen.copy(alpha = 0.5f) else Color(0xFFFFB74D),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("location_permission_card"),
        colors = CardDefaults.cardColors(
            containerColor = if (locationPermissionsState.allPermissionsGranted) Color(0xFFF2FBF5) else Color(0xFFFFF9F0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            if (locationPermissionsState.allPermissionsGranted) {
                // GRANTED STATE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(GrabGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = "GPS Active",
                                tint = GrabGreenDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "GPS Fine Location Active",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GrabNavy
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(GrabGreen)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "HIGH ACCURACY",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                            Text(
                                text = currentCoordinates?.let { "Lat: ${"%.4f".format(it.first)}, Lng: ${"%.4f".format(it.second)} • ±2m" }
                                    ?: if (isFetchingLocation) "Acquiring GPS lock..." else "Mandurriao / City Proper Area • ±5m",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            isFetchingLocation = true
                            fetchCurrentGpsLocation(context) { lat, lng ->
                                currentCoordinates = Pair(lat, lng)
                                isFetchingLocation = false
                                onLocationUpdated?.invoke(lat, lng)
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("refresh_gps_button")
                    ) {
                        if (isFetchingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = GrabGreen
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Location",
                                tint = GrabGreenDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Nearby services count summary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = GrabGreenDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Nearby Active Services:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "18 Drivers within 2km",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrabGreenDark
                    )
                }

            } else {
                // PERMISSION REQUEST / DENIED STATE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFB74D).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOff,
                            contentDescription = "Location Disabled",
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Fine Location Access",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B2238)
                        )
                        Text(
                            text = if (locationPermissionsState.shouldShowRationale)
                                "Fine location is required to calculate accurate pick-up points and exact ride prices."
                            else
                                "Grant fine GPS access to view precise nearby ride drivers and exact destinations.",
                            fontSize = 11.sp,
                            color = Color(0xFF424242),
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        locationPermissionsState.launchMultiplePermissionRequest()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("request_fine_location_permission_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GrabGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Allow Fine Location Permission",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun fetchCurrentGpsLocation(context: Context, onResult: (Double, Double) -> Unit) {
    try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onResult(location.latitude, location.longitude)
                } else {
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                        if (lastLoc != null) {
                            onResult(lastLoc.latitude, lastLoc.longitude)
                        } else {
                            // Default location (e.g. Mandurriao, Iloilo City)
                            onResult(10.7150, 122.5520)
                        }
                    }
                }
            }
            .addOnFailureListener {
                onResult(10.7150, 122.5520)
            }
    } catch (e: Exception) {
        onResult(10.7150, 122.5520)
    }
}
