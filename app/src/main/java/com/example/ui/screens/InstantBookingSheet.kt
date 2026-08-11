package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import com.example.ui.viewmodel.OneGoViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstantBookingDialog(
    visible: Boolean,
    platformId: String,
    platformName: String,
    serviceName: String,
    estimatedFare: Double,
    category: String, // "RIDES", "FOOD", "DELIVERY"
    viewModel: OneGoViewModel,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val context = LocalContext.current
    val walletBalance by viewModel.walletBalanceState.collectAsStateWithLifecycle()
    val selectedOrigin by viewModel.selectedOrigin.collectAsStateWithLifecycle()
    val selectedDestination by viewModel.selectedDestination.collectAsStateWithLifecycle()

    var bookingStep by remember { mutableStateOf(0) } // 0 = Confirm, 1 = Loading, 2 = Success
    var paymentMethod by remember { mutableStateOf(0) } // 0 = OneGoPay Wallet, 1 = Cash
    var currentStatusText by remember { mutableStateOf("Initializing connection...") }
    var progressVal by remember { mutableStateOf(0.0f) }

    val animatedProgress by animateFloatAsState(
        targetValue = progressVal,
        animationSpec = tween(durationMillis = 4500),
        label = "booking_progress"
    )

    // Run the booking animation sequence when step 1 is active
    LaunchedEffect(bookingStep) {
        if (bookingStep == 1) {
            progressVal = 0.0f
            
            // Phase 1: API handshake
            currentStatusText = when (category) {
                "FOOD" -> "Establishing secure API handshake with $platformName kitchen..."
                "DELIVERY" -> "Registering package cargo route with $platformName dispatch..."
                else -> "Establishing secure API handshake with $platformName servers..."
            }
            delay(1500)
            progressVal = 0.4f

            // Phase 2: Dispatcher search
            currentStatusText = when (category) {
                "FOOD" -> "Restaurant accepted order! Food is being freshly prepared..."
                "DELIVERY" -> "Searching for nearby delivery couriers..."
                else -> "Searching for available nearby partner drivers..."
            }
            delay(1500)
            progressVal = 0.75f

            // Phase 3: Assignment
            currentStatusText = when (category) {
                "FOOD" -> "Courier assigned! Danilo (Honda Click 125) picking up meal..."
                "DELIVERY" -> "Courier assigned! Danilo (Honda Click 125) heading to pickup..."
                else -> "Driver found! Danilo (Toyota Vios NDK-8291) is heading your way."
            }
            delay(1500)
            progressVal = 1.0f
            delay(300)

            // Finalize booking transaction
            val pointsEarned = if (paymentMethod == 0) 30 else 15
            val originStr = if (category == "FOOD") "Jollibee / Restaurant" else (selectedOrigin?.name ?: "Current Location")
            val destStr = selectedDestination?.name ?: "Destination"

            if (paymentMethod == 0) {
                viewModel.deductWallet(estimatedFare)
            }

            // Insert to history
            viewModel.addInstantBookingToHistory(
                platformId = platformId,
                platformName = platformName,
                price = estimatedFare,
                category = category,
                pointsEarned = pointsEarned,
                originName = originStr,
                destinationName = destStr
            )

            bookingStep = 2
        }
    }

    Dialog(
        onDismissRequest = { if (bookingStep != 1) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .testTag("instant_booking_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(GrabGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = GrabGreenDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "OneGo Instant Booking",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFF9800))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "COMING SOON",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    if (bookingStep != 1) {
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (bookingStep) {
                    0 -> { // STEP 0: CONFIRM BOOKING & SELECT PAYMENT
                        // Coming Soon Preview Notice Banner
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Upcoming,
                                    contentDescription = "Coming Soon",
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "⚡ Instant Booking — Coming Soon Preview",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFFE65100)
                                    )
                                    Text(
                                        text = "In-app direct API dispatch is coming soon! Below is an interactive preview of the upcoming 1-tap checkout flow.",
                                        fontSize = 10.sp,
                                        color = Color(0xFFBF360C),
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Selected service details
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = platformName.uppercase(),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    color = GrabGreenDark
                                )
                                Text(
                                    text = serviceName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(8.dp))

                                // Route / details
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "FROM",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = if (category == "FOOD") "Restaurant Kitchen" else (selectedOrigin?.name ?: "Current Location"),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "TO",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = selectedDestination?.name ?: "Your Location",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Payment selector title
                        Text(
                            text = "Select Payment Method",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // OneGoPay Wallet Payment Option
                        val hasSufficientBalance = walletBalance >= estimatedFare
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (paymentMethod == 0) GrabGreenLight.copy(alpha = 0.3f)
                                    else Color.Transparent
                                )
                                .border(
                                    width = if (paymentMethod == 0) 1.5.dp else 1.dp,
                                    color = if (paymentMethod == 0) GrabGreenDark else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { paymentMethod = 0 }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = paymentMethod == 0,
                                onClick = { paymentMethod = 0 },
                                colors = RadioButtonDefaults.colors(selectedColor = GrabGreenDark)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "OneGoPay Wallet",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFFF9800))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "COMING SOON",
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                                Text(
                                    text = "Balance: ₱${"%,.2f".format(walletBalance)}",
                                    fontSize = 11.sp,
                                    color = if (hasSufficientBalance) GrabGreenDark else Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Cash Payment Option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (paymentMethod == 1) GrabGreenLight.copy(alpha = 0.3f)
                                    else Color.Transparent
                                )
                                .border(
                                    width = if (paymentMethod == 1) 1.5.dp else 1.dp,
                                    color = if (paymentMethod == 1) GrabGreenDark else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { paymentMethod = 1 }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = paymentMethod == 1,
                                onClick = { paymentMethod = 1 },
                                colors = RadioButtonDefaults.colors(selectedColor = GrabGreenDark)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Cash to Driver / Rider",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Pay standard cash fare upon physical arrival (+15 Points)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Quick Top-up Prompt if wallet is selected but insufficient
                        if (paymentMethod == 0 && !hasSufficientBalance) {
                            val shortAmount = estimatedFare - walletBalance
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Warning",
                                        tint = Color(0xFF856404),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Insufficient Wallet Balance",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF856404)
                                        )
                                        Text(
                                            text = "You need ₱${"%.2f".format(shortAmount)} more to pay via wallet.",
                                            fontSize = 10.sp,
                                            color = Color(0xFF856404).copy(alpha = 0.85f)
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.topUpWallet(500.0)
                                            Toast.makeText(context, "Added ₱500.00 instantly! 🚀", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF856404),
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("+₱500", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Total Price Display
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Unified Fare",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₱${"%,.2f".format(estimatedFare)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GrabGreenDark
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Book/Confirm Action Button
                        Button(
                            onClick = { bookingStep = 1 },
                            enabled = paymentMethod == 1 || hasSufficientBalance,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GrabGreenDark,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("confirm_instant_booking_btn")
                        ) {
                            Text(
                                text = if (category == "FOOD") "Place Direct Order (In-App)" else "Confirm In-App Instant Booking",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    1 -> { // STEP 1: ACTIVE LOADING ANIMATION SIMULATOR
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(60.dp),
                                color = GrabGreenDark,
                                strokeWidth = 5.dp
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "OneGo Proxy Dispatch Active",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = GrabGreenDark
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = currentStatusText,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .height(40.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            LinearProgressIndicator(
                                progress = animatedProgress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = GrabGreen,
                                trackColor = GrabGreenLight
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${(animatedProgress * 100).toInt()}% tunnel established",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    2 -> { // STEP 2: BOOKING SUCCESS PANEL
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(GrabGreenLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Success",
                                    tint = GrabGreenDark,
                                    modifier = Modifier.size(42.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = if (category == "FOOD") "Order Successfully Dispatched! 🍔" else "Instant Booking Confirmed! 🚗",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = GrabGreenDark,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Your request was processed successfully without leaving OneGo.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Loyalty reward tag
                            val pointsEarned = if (paymentMethod == 0) 30 else 15
                            Card(
                                colors = CardDefaults.cardColors(containerColor = GrabGreenLight.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CardGiftcard,
                                        contentDescription = "Gift",
                                        tint = GrabGreenDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "OneGo Loyalty Reward: +$pointsEarned Points!",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = GrabGreenDark
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GrabGreenDark,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("dismiss_success_btn")
                            ) {
                                Text(
                                    text = "Done & Track Courier",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
