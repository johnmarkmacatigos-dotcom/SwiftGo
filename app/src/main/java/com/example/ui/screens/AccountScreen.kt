package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.SavedPlaceEntity
import com.example.ui.components.RegisterProfileDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.OneGoViewModel
import com.example.ui.viewmodel.PaymentMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    viewModel: OneGoViewModel,
    onNavigateToSavedPlaces: () -> Unit
) {
    val context = LocalContext.current

    // Firebase Auth State
    val firebaseUser by viewModel.firebaseUser.collectAsStateWithLifecycle()

    // Profile details
    val isGuestMode by viewModel.isGuestModeState.collectAsStateWithLifecycle()
    val userName by viewModel.userNameState.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmailState.collectAsStateWithLifecycle()
    val userPhone by viewModel.userPhoneState.collectAsStateWithLifecycle()
    val secondaryContact by viewModel.secondaryContactState.collectAsStateWithLifecycle()
    val preferredContactMethod by viewModel.preferredContactMethodState.collectAsStateWithLifecycle()

    // Payment methods & Saved delivery places
    val paymentMethods by viewModel.paymentMethodsState.collectAsStateWithLifecycle()
    val savedPlaces by viewModel.savedPlacesState.collectAsStateWithLifecycle()
    val defaultDeliveryId by viewModel.defaultDeliveryAddressIdState.collectAsStateWithLifecycle()

    // Dialog state controllers
    var showRegisterDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showEditContactDialog by remember { mutableStateOf(false) }
    var showAddPaymentDialog by remember { mutableStateOf(false) }
    var showAddAddressDialog by remember { mutableStateOf(false) }

    if (showRegisterDialog) {
        RegisterProfileDialog(
            onDismiss = { showRegisterDialog = false },
            onCompleteRegistration = { name, email, phone, address, lat, lng ->
                viewModel.completeGmailRegistration(name, email, phone, address, lat, lng)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Profile & Account Hub",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Managed with Firebase Auth",
                            fontSize = 11.sp,
                            color = GrabGreenDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    if (firebaseUser != null) {
                        IconButton(
                            onClick = {
                                viewModel.firebaseSignOut()
                                Toast.makeText(context, "Signed out from Firebase Auth", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("auth_signout_top_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Sign Out",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = Modifier.testTag("account_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // GUEST MODE PROFILE UPGRADE CARD
            if (isGuestMode) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .testTag("guest_account_upgrade_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF9800).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PersonOutline,
                                        contentDescription = null,
                                        tint = Color(0xFFE65100),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Guest Profile Active 👤",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFE65100)
                                    )
                                    Text(
                                        text = "Upgrade to Google Account to enjoy full OneGo perks",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.4f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("OneGo Perks Comparison:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                                        Text("OneGo Points: 0 pts (Locked for Guest)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                                        Text("20% OFF Member Promos: Locked", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = GrabGreen, modifier = Modifier.size(14.dp))
                                        Text("Fare Comparison: Available for all platforms", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = GrabGreenDark)
                                    }
                                }
                            }

                            Button(
                                onClick = { showRegisterDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("upgrade_guest_to_google_button"),
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
                                    Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text("Set Up User Profile (Register with Google)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            // FIREBASE AUTH PROFILE HEADER CARD
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("firebase_auth_header_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Auth Status Indicator Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (firebaseUser != null && !firebaseUser!!.isAnonymous) GrabGreen
                                            else if (firebaseUser != null) Color(0xFFFF9800)
                                            else Color.Gray
                                        )
                                )
                                Text(
                                    text = when {
                                        firebaseUser != null && !firebaseUser!!.isAnonymous -> "Firebase Auth Active"
                                        firebaseUser != null && firebaseUser!!.isAnonymous -> "Guest Session (Firebase)"
                                        else -> "Firebase Session Offline"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (firebaseUser != null) GrabGreenDark else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = { showEditContactDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GrabGreen,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(30.dp)
                                    .testTag("open_edit_profile_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Text("Edit Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // User Main Information Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(GrabGreen, GrabGreenDark)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "User Avatar",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = firebaseUser?.displayName?.ifBlank { userName } ?: userName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.testTag("profile_display_name")
                                )
                                Text(
                                    text = firebaseUser?.email ?: userEmail,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.testTag("profile_email")
                                )
                                Text(
                                    text = userPhone,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.testTag("profile_phone")
                                )

                                if (firebaseUser != null) {
                                    Text(
                                        text = "UID: ${firebaseUser!!.uid.take(12)}...",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showEditContactDialog = true },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                    .testTag("edit_contact_info_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Contact Info",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Email Verification & Quick Auth Actions
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )

                        OutlinedButton(
                            onClick = {
                                viewModel.syncCurrentProfileToFirestore()
                                Toast.makeText(context, "Profile & data synced with Firestore persistence! ☁️", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("firestore_sync_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, GrabGreen)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = GrabGreen, modifier = Modifier.size(16.dp))
                                Text("Sync User Profile to Firestore Database ☁️", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrabGreen)
                            }
                        }

                        if (firebaseUser != null) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (firebaseUser!!.isEmailVerified) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (firebaseUser!!.isEmailVerified) GrabGreen else Color(0xFFFF9800),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (firebaseUser!!.isEmailVerified) "Email Verified" else "Email Unverified",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (firebaseUser!!.isEmailVerified) GrabGreenDark else Color(0xFFD84315)
                                    )
                                }

                                if (!firebaseUser!!.isEmailVerified && firebaseUser?.email != null) {
                                    TextButton(
                                        onClick = {
                                            viewModel.firebaseSendEmailVerification { success, msg ->
                                                if (success) {
                                                    Toast.makeText(context, "Verification email sent to ${firebaseUser!!.email}!", Toast.LENGTH_LONG).show()
                                                } else {
                                                    Toast.makeText(context, "Verification notice: ${msg ?: "Already sent or failed"}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Send Verification", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                TextButton(
                                    onClick = { showAuthDialog = true },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Switch Account", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 1: CONTACT INFO MANAGEMENT
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContactPhone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Contact Information",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        TextButton(
                            onClick = { showEditContactDialog = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Manage", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ContactDetailRow(
                                icon = Icons.Outlined.Person,
                                label = "Full Name",
                                value = userName
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            ContactDetailRow(
                                icon = Icons.Outlined.Email,
                                label = "Primary Email",
                                value = userEmail
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            ContactDetailRow(
                                icon = Icons.Outlined.Phone,
                                label = "Primary Mobile Phone",
                                value = userPhone
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            ContactDetailRow(
                                icon = Icons.Outlined.ContactPhone,
                                label = "Secondary Contact Person / Emergency Phone",
                                value = secondaryContact
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            ContactDetailRow(
                                icon = Icons.Outlined.Notifications,
                                label = "Preferred Communication Channel",
                                value = preferredContactMethod
                            )
                        }
                    }
                }
            }

            // SECTION 2: SAVED PAYMENT METHODS MANAGEMENT
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Saved Payment Methods",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Button(
                            onClick = { showAddPaymentDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GrabGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("add_payment_method_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Method", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (paymentMethods.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No saved payment methods configured.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                paymentMethods.forEach { method ->
                                    PaymentMethodCardItem(
                                        method = method,
                                        onSetDefault = {
                                            viewModel.setDefaultPaymentMethod(method.id)
                                            Toast.makeText(context, "${method.label} set as default payment method.", Toast.LENGTH_SHORT).show()
                                        },
                                        onDelete = {
                                            viewModel.deletePaymentMethod(method.id)
                                            Toast.makeText(context, "${method.label} removed.", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 3: SAVED DELIVERY ADDRESSES MANAGEMENT
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Delivery Addresses & Places",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Button(
                            onClick = { showAddAddressDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GrabGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("add_delivery_address_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Address", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (savedPlaces.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No saved delivery addresses found.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                savedPlaces.forEach { place ->
                                    val isDefault = defaultDeliveryId == place.id || (defaultDeliveryId == null && place.name == "Home")
                                    DeliveryAddressItemCard(
                                        place = place,
                                        isDefault = isDefault,
                                        userName = userName,
                                        userPhone = userPhone,
                                        onSetDefault = {
                                            viewModel.setDefaultDeliveryAddress(place.id)
                                            Toast.makeText(context, "${place.name} set as primary delivery address!", Toast.LENGTH_SHORT).show()
                                        },
                                        onDelete = {
                                            viewModel.deleteSavedPlace(place.id)
                                            Toast.makeText(context, "${place.name} deleted.", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 4: PROMOS & VOUCHER CENTER
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = Color(0xFFE91E63),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Promos & Voucher Center",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE91E63).copy(alpha = 0.1f))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalOffer,
                                        contentDescription = null,
                                        tint = Color(0xFFE91E63),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("ONEGOFIRST50", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color(0xFFE91E63))
                                        Text("₱50 OFF on your first Grab / Food order", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Promo code ONEGOFIRST50 applied!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Apply", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(GrabGreen.copy(alpha = 0.1f))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        tint = GrabGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("FREEDELIVERY", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = GrabGreen)
                                        Text("Free Delivery on Orders ₱300+", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Free delivery voucher claimed!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Claim", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOG 1: EDIT CONTACT INFO (Name, Email, Primary Phone, Secondary Contact, Preferred Method)
    if (showEditContactDialog) {
        var editName by remember { mutableStateOf(userName) }
        var editEmail by remember { mutableStateOf(userEmail) }
        var editPhone by remember { mutableStateOf(userPhone) }
        var editSecondary by remember { mutableStateOf(secondaryContact) }
        var editPreferredMethod by remember { mutableStateOf(preferredContactMethod) }

        AlertDialog(
            onDismissRequest = { showEditContactDialog = false },
            title = {
                Text(
                    text = "Manage Contact Information",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("contact_edit_name_input")
                    )

                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Primary Email Address") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("contact_edit_email_input")
                    )

                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Primary Mobile Phone") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("contact_edit_phone_input")
                    )

                    OutlinedTextField(
                        value = editSecondary,
                        onValueChange = { editSecondary = it },
                        label = { Text("Secondary / Emergency Contact") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("contact_edit_secondary_input")
                    )

                    Text(
                        text = "Preferred Notification Channel:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    val channels = listOf("SMS & Push Notifications", "Email Receipts", "WhatsApp / Viber Alerts")
                    channels.forEach { channel ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { editPreferredMethod = channel }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = (editPreferredMethod == channel),
                                onClick = { editPreferredMethod = channel }
                            )
                            Text(
                                text = channel,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isBlank() || editEmail.isBlank() || editPhone.isBlank()) {
                            Toast.makeText(context, "Full Name, Email, and Phone cannot be blank!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.updateExtendedContactInfo(
                                name = editName,
                                email = editEmail,
                                phone = editPhone,
                                secondaryContact = editSecondary,
                                preferredMethod = editPreferredMethod
                            )
                            showEditContactDialog = false
                            Toast.makeText(context, "Contact information updated & synced!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GrabGreen)
                ) {
                    Text("Save Contact Info")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditContactDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // DIALOG 2: ADD PAYMENT METHOD
    if (showAddPaymentDialog) {
        var selectedType by remember { mutableStateOf("CARD") } // CARD, GCASH, MAYA, GRABPAY
        var label by remember { mutableStateOf("") }
        var numberDetail by remember { mutableStateOf("") }
        var cardholderName by remember { mutableStateOf("") }
        var expiryDate by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddPaymentDialog = false },
            title = {
                Text(
                    text = "Add Saved Payment Method",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Select Payment Type:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("CARD", "GCASH", "MAYA", "GRABPAY").forEach { type ->
                            val isSel = selectedType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSel) GrabGreen.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable {
                                        selectedType = type
                                        if (label.isBlank() || label in listOf("Visa Card", "GCash Wallet", "Maya Wallet", "GrabPay Wallet")) {
                                            label = when (type) {
                                                "CARD" -> "Visa Credit Card"
                                                "GCASH" -> "GCash e-Wallet"
                                                "MAYA" -> "Maya Wallet"
                                                else -> "GrabPay Wallet"
                                            }
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) GrabGreenDark else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Method Nickname") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_payment_label_input")
                    )

                    if (selectedType == "CARD") {
                        OutlinedTextField(
                            value = cardholderName,
                            onValueChange = { cardholderName = it },
                            label = { Text("Cardholder Name") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_payment_cardholder_input")
                        )

                        OutlinedTextField(
                            value = numberDetail,
                            onValueChange = { if (it.length <= 16) numberDetail = it },
                            label = { Text("16-Digit Card Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_payment_card_number_input")
                        )

                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = { expiryDate = it },
                            label = { Text("Expiry Date (MM/YY)") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_payment_expiry_input")
                        )
                    } else {
                        OutlinedTextField(
                            value = numberDetail,
                            onValueChange = { numberDetail = it },
                            label = { Text("Mobile Phone Number (0917 *** ****)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_payment_phone_number_input")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (label.isBlank() || numberDetail.isBlank()) {
                            Toast.makeText(context, "Please complete required fields!", Toast.LENGTH_SHORT).show()
                        } else {
                            val formattedDetail = if (selectedType == "CARD") {
                                if (numberDetail.length >= 4) "**** **** **** ${numberDetail.takeLast(4)}" else numberDetail
                            } else {
                                if (numberDetail.length >= 4) "${numberDetail.take(4)} **** ${numberDetail.takeLast(4)}" else numberDetail
                            }

                            viewModel.addPaymentMethod(
                                type = selectedType,
                                label = label,
                                detail = formattedDetail,
                                cardholderName = cardholderName,
                                expiryDate = expiryDate,
                                billingPhone = numberDetail
                            )
                            showAddPaymentDialog = false
                            Toast.makeText(context, "$label added successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GrabGreen)
                ) {
                    Text("Save Method")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPaymentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // DIALOG 3: ADD SAVED DELIVERY ADDRESS
    if (showAddAddressDialog) {
        var placeName by remember { mutableStateOf("") }
        var fullAddress by remember { mutableStateOf("") }
        var landmark by remember { mutableStateOf("") }
        var recipientName by remember { mutableStateOf(userName) }
        var recipientPhone by remember { mutableStateOf(userPhone) }
        var iconName by remember { mutableStateOf("home") }

        AlertDialog(
            onDismissRequest = { showAddAddressDialog = false },
            title = {
                Text(
                    text = "Add Saved Delivery Address",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = placeName,
                        onValueChange = { placeName = it },
                        label = { Text("Location Name (e.g. Home / Gym / Condo)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_address_name_input")
                    )

                    OutlinedTextField(
                        value = fullAddress,
                        onValueChange = { fullAddress = it },
                        label = { Text("Street Address, Barangay, City") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_address_full_street_input")
                    )

                    OutlinedTextField(
                        value = landmark,
                        onValueChange = { landmark = it },
                        label = { Text("Landmark / Delivery Note (e.g., Gate 2)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_address_landmark_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = recipientName,
                            onValueChange = { recipientName = it },
                            label = { Text("Recipient Name") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_address_recipient_name_input")
                        )
                        OutlinedTextField(
                            value = recipientPhone,
                            onValueChange = { recipientPhone = it },
                            label = { Text("Recipient Phone") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_address_recipient_phone_input")
                        )
                    }

                    Text(
                        text = "Category Icon:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    val categories = listOf("home", "work", "shopping", "school", "star", "restaurant")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        categories.forEach { cat ->
                            val isSel = iconName == cat
                            IconButton(
                                onClick = { iconName = cat },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSel) GrabGreen.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                            ) {
                                Icon(
                                    imageVector = when (cat) {
                                        "home" -> Icons.Default.Home
                                        "work" -> Icons.Default.Work
                                        "shopping" -> Icons.Default.ShoppingCart
                                        "school" -> Icons.Default.School
                                        "restaurant" -> Icons.Default.Restaurant
                                        else -> Icons.Default.Star
                                    },
                                    contentDescription = cat,
                                    tint = if (isSel) GrabGreenDark else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (placeName.isBlank() || fullAddress.isBlank()) {
                            Toast.makeText(context, "Location name & address are required!", Toast.LENGTH_SHORT).show()
                        } else {
                            val simLat = 10.7 + (Math.random() * 0.05)
                            val simLng = 122.5 + (Math.random() * 0.05)

                            val detailedAddressStr = if (landmark.isNotBlank()) "$fullAddress (Note: $landmark)" else fullAddress
                            viewModel.addSavedPlace(placeName, detailedAddressStr, simLat, simLng, iconName)
                            showAddAddressDialog = false
                            Toast.makeText(context, "$placeName added to delivery addresses!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GrabGreen)
                ) {
                    Text("Save Location")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAddressDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // DIALOG 4: FIREBASE AUTH MANAGEMENT (Sign In, Register, Reset Password, Guest Login)
    if (showAuthDialog) {
        FirebaseAuthDialog(
            currentEmail = userEmail,
            onDismiss = { showAuthDialog = false },
            onSignIn = { email, pass, callback ->
                viewModel.firebaseSignIn(email, pass, callback)
            },
            onRegister = { name, email, pass, phone, callback ->
                viewModel.firebaseRegister(name, email, pass, phone, callback)
            },
            onAnonymousSignIn = { callback ->
                viewModel.firebaseAnonymousSignIn(callback)
            },
            onPasswordReset = { email, callback ->
                viewModel.firebasePasswordReset(email, callback)
            }
        )
    }
}

@Composable
fun ContactDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value.ifBlank { "Not configured" },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun PaymentMethodCardItem(
    method: PaymentMethod,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("payment_method_item"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (method.type) {
                            "CARD" -> Color(0xFF1E88E5).copy(alpha = 0.15f)
                            "GCASH" -> GrabGreen.copy(alpha = 0.15f)
                            "MAYA" -> Color(0xFF6200EE).copy(alpha = 0.15f)
                            "GRABPAY" -> GrabGreenDark.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (method.type) {
                        "CARD" -> Icons.Default.CreditCard
                        "GCASH" -> Icons.Default.Payments
                        "MAYA" -> Icons.Default.AccountBalanceWallet
                        "GRABPAY" -> Icons.Default.LocalTaxi
                        else -> Icons.Default.AttachMoney
                    },
                    contentDescription = null,
                    tint = when (method.type) {
                        "CARD" -> Color(0xFF1E88E5)
                        "GCASH" -> GrabGreen
                        "MAYA" -> Color(0xFF6200EE)
                        "GRABPAY" -> GrabGreenDark
                        else -> MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = method.label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (method.isDefault) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GrabGreen)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "DEFAULT",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Text(
                    text = method.detail,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (method.cardholderName.isNotBlank()) {
                    Text(
                        text = "Name: ${method.cardholderName} ${if (method.expiryDate.isNotBlank()) "• Exp: ${method.expiryDate}" else ""}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!method.isDefault) {
                TextButton(
                    onClick = onSetDefault,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Make Default", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (method.type != "CASH") {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DeliveryAddressItemCard(
    place: SavedPlaceEntity,
    isDefault: Boolean,
    userName: String,
    userPhone: String,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("delivery_address_item"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDefault) GrabGreen.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (place.iconName) {
                        "home" -> Icons.Default.Home
                        "work" -> Icons.Default.Work
                        "shopping" -> Icons.Default.ShoppingCart
                        "school" -> Icons.Default.School
                        "restaurant" -> Icons.Default.Restaurant
                        else -> Icons.Default.Star
                    },
                    contentDescription = place.name,
                    tint = if (isDefault) GrabGreenDark else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = place.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (isDefault) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GrabGreen)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "PRIMARY DELIVERY",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Text(
                    text = place.address,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Recipient: $userName ($userPhone)",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isDefault) {
                TextButton(
                    onClick = onSetDefault,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Set Primary", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun FirebaseAuthDialog(
    currentEmail: String,
    onDismiss: () -> Unit,
    onSignIn: (String, String, (Boolean, String?) -> Unit) -> Unit,
    onRegister: (String, String, String, String, (Boolean, String?) -> Unit) -> Unit,
    onAnonymousSignIn: ((Boolean, String?) -> Unit) -> Unit,
    onPasswordReset: (String, (Boolean, String?) -> Unit) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Sign In, 1: Register, 2: Reset Password
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Input fields
    var emailInput by remember { mutableStateOf(currentEmail) }
    var passwordInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("+63 917 123 4567") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Firebase Auth Portal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Secure User Account Management",
                    fontSize = 11.sp,
                    color = GrabGreenDark,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Tab Selection (Sign In and Reset Pass only - registration is done via mandatory Gmail flow)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = GrabGreenDark
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0; errorMessage = null },
                        text = { Text("Sign In", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1; errorMessage = null },
                        text = { Text("Reset Pass", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }

                when (selectedTab) {
                    0 -> {
                        // SIGN IN FORM
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Firebase Email") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("firebase_signin_email_input")
                        )

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("firebase_signin_password_input")
                        )

                        Button(
                            onClick = {
                                if (emailInput.isBlank() || passwordInput.isBlank()) {
                                    errorMessage = "Email and Password are required."
                                } else {
                                    isLoading = true
                                    onSignIn(emailInput, passwordInput) { success, err ->
                                        isLoading = false
                                        if (success) {
                                            Toast.makeText(context, "Successfully signed in to Firebase!", Toast.LENGTH_SHORT).show()
                                            onDismiss()
                                        } else {
                                            errorMessage = err ?: "Sign in failed"
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("firebase_submit_signin_button")
                        ) {
                            Text("Sign In with Email")
                        }

                        OutlinedButton(
                            onClick = {
                                isLoading = true
                                onAnonymousSignIn { success, err ->
                                    isLoading = false
                                    if (success) {
                                        Toast.makeText(context, "Signed in as Guest User!", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    } else {
                                        errorMessage = err ?: "Guest sign in failed"
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("firebase_guest_signin_button")
                        ) {
                            Text("Continue as Guest (Anonymous)", fontSize = 12.sp)
                        }
                    }

                    1 -> {
                        // RESET PASSWORD FORM
                        Text(
                            text = "Enter your registered email address to receive a Firebase password reset link:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Registered Email") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("firebase_reset_email_input")
                        )

                        Button(
                            onClick = {
                                if (emailInput.isBlank()) {
                                    errorMessage = "Please enter your email."
                                } else {
                                    isLoading = true
                                    onPasswordReset(emailInput) { success, err ->
                                        isLoading = false
                                        if (success) {
                                            Toast.makeText(context, "Password reset email sent to $emailInput", Toast.LENGTH_LONG).show()
                                            onDismiss()
                                        } else {
                                            errorMessage = err ?: "Password reset failed"
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("firebase_submit_reset_button")
                        ) {
                            Text("Send Reset Link")
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
