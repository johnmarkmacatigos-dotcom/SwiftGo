package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.ui.theme.*

data class ChatMessage(
    val id: String,
    val senderName: String,
    val serviceType: String, // RIDES, FOOD, DELIVERY
    val lastMessage: String,
    val timestamp: String,
    val isUnread: Boolean,
    val avatarColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen() {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val primaryColor = MaterialTheme.colorScheme.primary

    val chats = remember(primaryColor) {
        listOf(
            ChatMessage(
                id = "1",
                senderName = "Kuya Ronald (GrabFood)",
                serviceType = "FOOD",
                lastMessage = "I am at the lobby now, Ka-OneGo! Will leave the food with reception.",
                timestamp = "10 mins ago",
                isUnread = true,
                avatarColor = FoodpandaPink
            ),
            ChatMessage(
                id = "2",
                senderName = "Maxim Driver (Car)",
                serviceType = "RIDES",
                lastMessage = "Going near your pickup pin. Please wear your face mask if required.",
                timestamp = "35 mins ago",
                isUnread = false,
                avatarColor = GrabGreen
            ),
            ChatMessage(
                id = "3",
                senderName = "Lalamove Delivery Rider",
                serviceType = "DELIVERY",
                lastMessage = "Parcel successfully picked up! On my way to the dropoff.",
                timestamp = "2 hours ago",
                isUnread = false,
                avatarColor = LalamoveOrange
            ),
            ChatMessage(
                id = "4",
                senderName = "OneGo Support Team",
                serviceType = "SUPPORT",
                lastMessage = "Welcome to OneGo! Let us know if you need help comparing rates.",
                timestamp = "Yesterday",
                isUnread = false,
                avatarColor = primaryColor
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Messages",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "All messages marked as read.", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Mark all read",
                            tint = GrabGreenDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = Modifier.testTag("messages_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Segmented tab headers
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = GrabGreenDark
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("All", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Active Rides/Orders", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Support", fontWeight = FontWeight.Bold) }
                )
            }

            val filteredChats = remember(selectedTab) {
                when (selectedTab) {
                    0 -> chats
                    1 -> chats.filter { it.serviceType != "SUPPORT" }
                    2 -> chats.filter { it.serviceType == "SUPPORT" }
                    else -> chats
                }
            }

            if (filteredChats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "No chats",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No messages yet",
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredChats) { chat ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    Toast.makeText(context, "Opening chat with ${chat.senderName}", Toast.LENGTH_SHORT).show()
                                }
                                .testTag("chat_item_${chat.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (chat.isUnread) chat.avatarColor.copy(alpha = 0.05f) 
                                                 else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar Circle
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(chat.avatarColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val icon = when (chat.serviceType) {
                                        "FOOD" -> Icons.Default.Restaurant
                                        "RIDES" -> Icons.Default.DirectionsCar
                                        "DELIVERY" -> Icons.Default.LocalPostOffice
                                        else -> Icons.Default.SupportAgent
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = chat.senderName,
                                            fontWeight = if (chat.isUnread) FontWeight.ExtraBold else FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = chat.timestamp,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = chat.lastMessage,
                                        fontSize = 12.sp,
                                        color = if (chat.isUnread) MaterialTheme.colorScheme.onBackground 
                                                else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (chat.isUnread) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(GrabGreen)
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
