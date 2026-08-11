package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.LocationPermissionCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.OneGoViewModel

@Composable
fun MainAppContainer(
    viewModel: OneGoViewModel = viewModel()
) {
    var showSplashScreen by remember { mutableStateOf(true) }

    if (showSplashScreen) {
        SplashScreen(
            onSplashFinished = {
                showSplashScreen = false
            }
        )
        return
    }

    val isProfileConfigured by viewModel.isProfileConfiguredState.collectAsStateWithLifecycle()

    if (!isProfileConfigured) {
        GmailRegistrationScreen(
            onRegistrationComplete = { name, email, phone, address, lat, lng ->
                viewModel.completeGmailRegistration(name, email, phone, address, lat, lng)
            },
            onContinueAsGuest = {
                viewModel.continueAsGuest()
            }
        )
        return
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Check if the current screen is a primary bottom-nav destination
    val showBottomBar = currentRoute in listOf("home", "history", "wallet", "messages", "account")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("bottom_nav_bar"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentRoute == "home",
                        onClick = {
                            if (currentRoute != "home") {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == "home") Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GrabGreenDark,
                            selectedTextColor = GrabGreenDark,
                            indicatorColor = GrabGreenLight
                        ),
                        modifier = Modifier.testTag("nav_home")
                    )

                    NavigationBarItem(
                        selected = currentRoute == "history",
                        onClick = {
                            if (currentRoute != "history") {
                                navController.navigate("history")
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == "history") Icons.Filled.History else Icons.Outlined.History,
                                contentDescription = "Activity"
                            )
                        },
                        label = { Text("Activity", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GrabGreenDark,
                            selectedTextColor = GrabGreenDark,
                            indicatorColor = GrabGreenLight
                        ),
                        modifier = Modifier.testTag("nav_history")
                    )

                    NavigationBarItem(
                        selected = currentRoute == "wallet",
                        onClick = {
                            if (currentRoute != "wallet") {
                                navController.navigate("wallet")
                            }
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = Color(0xFFFF9800),
                                        contentColor = Color.White
                                    ) {
                                        Text("SOON", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentRoute == "wallet") Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                                    contentDescription = "Wallet (Coming Soon)"
                                )
                            }
                        },
                        label = { Text("Wallet (Soon)", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GrabGreenDark,
                            selectedTextColor = GrabGreenDark,
                            indicatorColor = GrabGreenLight
                        ),
                        modifier = Modifier.testTag("nav_wallet")
                    )

                    NavigationBarItem(
                        selected = currentRoute == "messages",
                        onClick = {
                            if (currentRoute != "messages") {
                                navController.navigate("messages")
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == "messages") Icons.Filled.ChatBubble else Icons.Outlined.ChatBubble,
                                contentDescription = "Messages"
                            )
                        },
                        label = { Text("Messages", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GrabGreenDark,
                            selectedTextColor = GrabGreenDark,
                            indicatorColor = GrabGreenLight
                        ),
                        modifier = Modifier.testTag("nav_messages")
                    )

                    NavigationBarItem(
                        selected = currentRoute == "account",
                        onClick = {
                            if (currentRoute != "account") {
                                navController.navigate("account")
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == "account") Icons.Filled.Person else Icons.Outlined.Person,
                                contentDescription = "Account"
                            )
                        },
                        label = { Text("Account", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GrabGreenDark,
                            selectedTextColor = GrabGreenDark,
                            indicatorColor = GrabGreenLight
                        ),
                        modifier = Modifier.testTag("nav_account")
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Location Permission Banner (hidden on Home and primary bottom bar screens)
            if (!showBottomBar) {
                LocationPermissionCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    onLocationUpdated = { lat, lng ->
                        viewModel.updateOriginFromDeviceLocation(lat, lng)
                    }
                )
            }

            val bottomNavRoutes = setOf("home", "history", "wallet", "messages", "account")

            NavHost(
                navController = navController,
                startDestination = "home",
                enterTransition = {
                    if (targetState.destination.route in bottomNavRoutes && initialState.destination.route in bottomNavRoutes) {
                        fadeIn(animationSpec = tween(280)) + scaleIn(initialScale = 0.97f, animationSpec = tween(280))
                    } else {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(350))
                    }
                },
                exitTransition = {
                    if (targetState.destination.route in bottomNavRoutes && initialState.destination.route in bottomNavRoutes) {
                        fadeOut(animationSpec = tween(280))
                    } else {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(350))
                    }
                },
                popEnterTransition = {
                    if (targetState.destination.route in bottomNavRoutes && initialState.destination.route in bottomNavRoutes) {
                        fadeIn(animationSpec = tween(280))
                    } else {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(350))
                    }
                },
                popExitTransition = {
                    if (targetState.destination.route in bottomNavRoutes && initialState.destination.route in bottomNavRoutes) {
                        fadeOut(animationSpec = tween(280))
                    } else {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(350))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToRides = {
                        viewModel.currentMode.value = "RIDES"
                        navController.navigate("compare_rides")
                    },
                    onNavigateToFood = {
                        viewModel.currentMode.value = "FOOD"
                        navController.navigate("compare_food")
                    },
                    onNavigateToDelivery = {
                        viewModel.currentMode.value = "DELIVERY"
                        navController.navigate("compare_delivery")
                    },
                    onNavigateToPabili = {
                        viewModel.currentMode.value = "PABILI"
                        navController.navigate("compare_pabili")
                    },
                    onNavigateToSavedPlaces = {
                        navController.navigate("saved_places")
                    },
                    onNavigateToHistory = {
                        navController.navigate("history")
                    }
                )
            }

            composable("compare_rides") {
                CompareRidesScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSavedPlaces = { navController.navigate("saved_places") }
                )
            }

            composable("compare_food") {
                CompareFoodScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("compare_delivery") {
                CompareDeliveryScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("compare_pabili") {
                PabiliScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("saved_places") {
                SavedPlacesScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.navigate("home") }
                )
            }

            composable("history") {
                HistoryScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.navigate("home") },
                    onNavigateToCompareRides = { navController.navigate("compare_rides") },
                    onNavigateToCompareFood = { navController.navigate("compare_food") },
                    onNavigateToCompareDelivery = { navController.navigate("compare_delivery") }
                )
            }

            composable("wallet") {
                WalletScreen(viewModel = viewModel)
            }

            composable("messages") {
                MessagesScreen()
            }

            composable("account") {
                AccountScreen(
                    viewModel = viewModel,
                    onNavigateToSavedPlaces = {
                        navController.navigate("saved_places")
                    }
                )
            }
        }
    }
}
}
