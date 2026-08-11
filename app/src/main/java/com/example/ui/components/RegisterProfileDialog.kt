package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.GrabGreenDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterProfileDialog(
    onDismiss: () -> Unit,
    onCompleteRegistration: (name: String, email: String, phone: String, address: String?, lat: Double, lng: Double) -> Unit
) {
    val context = LocalContext.current
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("+63 917 123 4567") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(24.dp))
                .testTag("register_profile_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEA4335).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("G", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFFEA4335))
                        }
                        Column {
                            Text(
                                text = "Set Up User Profile",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Unlock OneGo Points & Member Perks 🌟",
                                fontSize = 11.sp,
                                color = GrabGreenDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Benefits summary banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = GrabGreen.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, GrabGreen.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("🎁 Member Perks Unlocked upon Registration:", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = GrabGreenDark)
                        Text("• Earn OneGo Points on every Ride & Food order\n• Get 20% OFF Member Promos & Cashback\n• Save & Sync primary Home / Office addresses", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                // Quick Fill Button
                OutlinedButton(
                    onClick = {
                        nameInput = "Juan Dela Cruz"
                        emailInput = "juan.delacruz@gmail.com"
                        phoneInput = "+63 917 888 9999"
                        Toast.makeText(context, "Google account profile autofilled!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFEA4335).copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEA4335))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color(0xFFEA4335), modifier = Modifier.size(18.dp))
                        Text("Quick Fill with Google Account 📧", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                var hasGoogleConsent by remember { mutableStateOf(true) }

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it; errorMessage = null },
                    label = { Text("Full Name") },
                    placeholder = { Text("e.g. Juan Dela Cruz") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GrabGreen) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it; errorMessage = null },
                    label = { Text("Gmail Address") },
                    placeholder = { Text("e.g. your.name@gmail.com") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFFEA4335)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it; errorMessage = null },
                    label = { Text("Mobile Number") },
                    placeholder = { Text("+63 9XX XXX XXXX") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = GrabGreen) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

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
                        text = "I grant permission to link my Google Account profile (Email & Name) to OneGo for account sync & rewards points.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        lineHeight = 15.sp
                    )
                }

                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val cleanName = nameInput.trim()
                        val cleanEmail = emailInput.trim().lowercase()
                        if (!hasGoogleConsent) {
                            errorMessage = "Please check the permission box to link your Google Account."
                        } else if (cleanName.length < 2) {
                            errorMessage = "Please enter your full name."
                        } else if (!cleanEmail.contains("@gmail.com")) {
                            errorMessage = "Please enter a valid Gmail address (@gmail.com)."
                        } else {
                            onCompleteRegistration(cleanName, cleanEmail, phoneInput.trim(), null, 10.7123, 122.5518)
                            Toast.makeText(context, "Google Account linked! Registration complete 🎉", Toast.LENGTH_LONG).show()
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GrabGreen, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Register Profile & Unlock Perks 🚀", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
