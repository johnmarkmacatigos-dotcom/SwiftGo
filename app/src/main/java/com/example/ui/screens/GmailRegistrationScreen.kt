package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.GrabGreenDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GmailRegistrationScreen(
    onRegistrationComplete: (name: String, email: String, phone: String, address: String?, lat: Double, lng: Double) -> Unit,
    onContinueAsGuest: () -> Unit = {}
) {
    val context = LocalContext.current
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("+63 917 123 4567") }
    var hasGoogleConsent by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // User Primary Location State
    var userAddress by remember { mutableStateOf("") }
    var userLat by remember { mutableStateOf(10.7123) }
    var userLng by remember { mutableStateOf(122.5518) }
    var showUserLocationPicker by remember { mutableStateOf(false) }

    if (showUserLocationPicker) {
        LocationPickerDialog(
            title = "Set Primary Delivery / Home Location",
            places = emptyList(),
            onDismiss = { showUserLocationPicker = false },
            onSelect = { selectedPlace ->
                userAddress = selectedPlace.address
                userLat = selectedPlace.latitude
                userLng = selectedPlace.longitude
                showUserLocationPicker = false
            }
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("gmail_registration_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Hero Brand Logo Header
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GrabGreen, GrabGreenDark)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = "OneGo App Logo",
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome to OneGo 🛵",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "The All-In-One Ride, Food & Delivery Aggregator",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Gmail Registration Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gmail_registration_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                border = BorderStroke(1.dp, GrabGreen.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEA4335).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("G", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFFEA4335))
                        }
                        Column {
                            Text(
                                text = "Register with Gmail Account",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Required before using OneGo services",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // One-Tap Gmail Quick Fill Button
                    OutlinedButton(
                        onClick = {
                            nameInput = "Juan Dela Cruz"
                            emailInput = "juan.delacruz@gmail.com"
                            phoneInput = "+63 917 888 9999"
                            Toast.makeText(context, "Google Gmail account loaded!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("google_onetap_fill_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFEA4335).copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEA4335)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Google",
                                tint = Color(0xFFEA4335),
                                modifier = Modifier.size(20.dp)
                            )
                            Text("Quick Fill with Google Account 📧", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "Or enter your Gmail details manually:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = {
                            nameInput = it
                            errorMessage = null
                        },
                        label = { Text("Full Name") },
                        placeholder = { Text("e.g. Juan Dela Cruz") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GrabGreen) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gmail_reg_name_input")
                    )

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = {
                            emailInput = it
                            errorMessage = null
                        },
                        label = { Text("Gmail Address") },
                        placeholder = { Text("e.g. your.name@gmail.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFFEA4335)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gmail_reg_email_input")
                    )

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = {
                            phoneInput = it
                            errorMessage = null
                        },
                        label = { Text("Mobile Number") },
                        placeholder = { Text("+63 9XX XXX XXXX") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = GrabGreen) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gmail_reg_phone_input")
                    )

                    // Add Primary Home / Delivery Location Card
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
                                    text = "Primary Home / Delivery Location (Optional)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (userAddress.isNotBlank()) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showUserLocationPicker = true },
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, GrabGreen.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Home, contentDescription = null, tint = GrabGreen, modifier = Modifier.size(20.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(userAddress, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            Text("GPS: ${"%.4f".format(userLat)}, ${"%.4f".format(userLng)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Location", tint = GrabGreen, modifier = Modifier.size(16.dp))
                                    }
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { showUserLocationPicker = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("add_user_location_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, GrabGreen)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.AddLocationAlt, contentDescription = null, tint = GrabGreen, modifier = Modifier.size(16.dp))
                                        Text("Add Home / Delivery Location 📍", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrabGreen)
                                    }
                                }
                            }
                        }
                    }

                    // Explicit Google Permission Consent Checkbox
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = hasGoogleConsent,
                            onCheckedChange = { hasGoogleConsent = it; errorMessage = null },
                            colors = CheckboxDefaults.colors(checkedColor = GrabGreen)
                        )
                        Text(
                            text = "I grant permission to link my Google Account profile (Email & Name) to OneGo for account synchronization & loyalty rewards.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            lineHeight = 15.sp
                        )
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            val cleanEmail = emailInput.trim().lowercase()
                            val cleanName = nameInput.trim()
                            if (!hasGoogleConsent) {
                                errorMessage = "Please check the box to grant permission to link your Google Account."
                            } else if (cleanName.length < 2) {
                                errorMessage = "Please enter your full name."
                            } else if (!cleanEmail.contains("@gmail.com")) {
                                errorMessage = "Please enter a valid Gmail address (@gmail.com)."
                            } else {
                                onRegistrationComplete(
                                    cleanName,
                                    cleanEmail,
                                    phoneInput.trim(),
                                    userAddress.ifBlank { null },
                                    userLat,
                                    userLng
                                )
                                Toast.makeText(context, "Google Account linked! Welcome to OneGo, $cleanName 🎉", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("complete_gmail_registration_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GrabGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Complete Registration & Enter OneGo", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Option 2: Continue as Guest Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("continue_as_guest_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonOutline,
                            contentDescription = "Guest",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Or Continue as Guest User",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Warning / Limitation Banner
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFF9800).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Perks Warning",
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "Guest Mode Limitations:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                                Text(
                                    text = "• You won't earn OneGo Rewards Points on bookings.\n• Exclusive 20% OFF member promos & vouchers are locked.\n• Saved places & order history won't sync across devices.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            onContinueAsGuest()
                            Toast.makeText(context, "Welcome! You are browsing as Guest.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("continue_as_guest_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Continue as Guest (Skip Account Setup)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Feature Highlights Footnote Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GrabGreen, modifier = Modifier.size(16.dp))
                        Text("Compare 8+ Transport & Delivery Platforms", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GrabGreen, modifier = Modifier.size(16.dp))
                        Text("Fine Location GPS Precision for Exact Pricing", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
