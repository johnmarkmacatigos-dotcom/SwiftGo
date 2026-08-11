package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.GrabGreenDark
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

data class OrbitingService(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Animatable states for logo scale, alpha, and text reveal
    val logoScale = remember { Animatable(0.2f) }
    val logoAlpha = remember { Animatable(0f) }
    val orbitAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffsetY = remember { Animatable(40f) }

    // Pulsing glow animation around logo
    val infiniteTransition = rememberInfiniteTransition(label = "splashGlow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Continuous 360-degree rotation angle for orbiting service icons
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitAngle"
    )

    // Aggregated service icons representing OneGo Super App features
    val services = remember {
        listOf(
            OrbitingService("Rides", Icons.Default.DirectionsCar, Color(0xFF00B14F)),      // Grab/JoyRide Green
            OrbitingService("Food", Icons.Default.Restaurant, Color(0xFFD70F64)),         // Foodpanda Pink
            OrbitingService("Express", Icons.Default.Inventory2, Color(0xFFF26522)),      // Lalamove Orange
            OrbitingService("Pabili", Icons.Default.ShoppingBag, Color(0xFF8E24AA)),       // Shopping Purple
            OrbitingService("Coffee", Icons.Default.LocalCafe, Color(0xFF795548)),        // Warm Coffee Brown
            OrbitingService("Pay", Icons.Default.AccountBalanceWallet, Color(0xFFFFB300))  // Gold Wallet
        )
    }

    LaunchedEffect(Unit) {
        // Step 1: Animate logo appearance with elastic bounce
        logoAlpha.animateTo(1f, animationSpec = tween(500))
        logoScale.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        // Step 2: Fade in orbiting service icons & text
        orbitAlpha.animateTo(1f, animationSpec = tween(600))
        textAlpha.animateTo(1f, animationSpec = tween(600))
        textOffsetY.animateTo(0f, animationSpec = tween(600, easing = FastOutSlowInEasing))

        // Step 3: Hold splash so user can observe orbiting service aggregator animation
        delay(2600)

        // Step 4: Exit animation
        logoAlpha.animateTo(0f, animationSpec = tween(400))
        orbitAlpha.animateTo(0f, animationSpec = tween(400))
        textAlpha.animateTo(0f, animationSpec = tween(400))

        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF05110B))
            .testTag("animated_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Scenery background image (futuristic emerald city, highway & skyscrapers)
        Image(
            painter = painterResource(id = R.drawable.img_splash_bg_1786077162468),
            contentDescription = "Start-up Scenery Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.85f)
        )

        // Balanced dark gradient overlay so futuristic city backdrop is clearly visible while text has rich contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.45f),
                            Color(0xFF05110B).copy(alpha = 0.58f),
                            Color.Black.copy(alpha = 0.75f)
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Container for Logo + Orbiting Service Icons
            val orbitRadiusDp: Dp = 110.dp

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp)
            ) {
                // Orbital Track Ring Line
                Box(
                    modifier = Modifier
                        .size(orbitRadiusDp * 2)
                        .alpha(0.35f * orbitAlpha.value)
                        .clip(CircleShape)
                        .border(
                            width = 1.5.dp,
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    GrabGreen,
                                    Color(0xFFFF9800),
                                    Color(0xFFD70F64),
                                    Color(0xFF8E24AA),
                                    GrabGreen
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // Animated Glowing Pulsing Backdrop behind Central Logo
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(glowScale)
                        .alpha(glowAlpha * logoAlpha.value)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    GrabGreen.copy(alpha = 0.7f),
                                    Color(0xFFFF9800).copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Central OneGo Aggregator Logo Image
                Image(
                    painter = painterResource(id = R.drawable.img_onego_logo_1785916273591),
                    contentDescription = "OneGo Aggregator Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(115.dp)
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                        .clip(CircleShape)
                        .border(
                            width = 2.5.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(GrabGreen, Color(0xFFFFB74D))
                            ),
                            shape = CircleShape
                        )
                )

                // Render Orbiting Aggregated Service Icons around Logo
                val numServices = services.size
                services.forEachIndexed { index, service ->
                    val angleDeg = (orbitAngle + (index * (360f / numServices))) % 360f
                    val angleRad = Math.toRadians(angleDeg.toDouble())

                    val offsetX = (orbitRadiusDp.value * cos(angleRad)).dp
                    val offsetY = (orbitRadiusDp.value * sin(angleRad)).dp

                    Box(
                        modifier = Modifier
                            .offset(x = offsetX, y = offsetY)
                            .alpha(orbitAlpha.value)
                            .clip(CircleShape)
                            .background(service.color)
                            .border(1.5.dp, Color.White.copy(alpha = 0.9f), CircleShape)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = service.icon,
                            contentDescription = service.name,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Branding Title & Subtitle with High-Contrast Dark Backdrop Pill Cards
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = textOffsetY.value.dp)
                    .alpha(textAlpha.value)
            ) {
                // High-Contrast Title Container
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "One",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Go",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GrabGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // High-Contrast Tagline Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .border(1.5.dp, GrabGreen, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "SUPER APP AGGREGATOR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Services List Subtitle Card
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.50f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Rides • Food • Express • Pabili • Coffee",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Pulsing Bottom Loading Dots Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(GrabGreen)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(GrabGreen.copy(alpha = 0.6f))
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(GrabGreen.copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
}

