package com.metroapp.lima.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metroapp.lima.R
import com.metroapp.lima.ui.theme.MetroGreenContainer
import com.metroapp.lima.ui.theme.MetroGreenDark
import com.metroapp.lima.ui.theme.MetroGreenLight
import com.metroapp.lima.ui.theme.MetroGreenPrimary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val scale = remember { Animatable(0.75f) }
    val alpha = remember { Animatable(0f) }
    val trainOffset = remember { Animatable(-100f) }

    LaunchedEffect(Unit) {
        // Animate entrance of logo
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700)
        )
    }

    LaunchedEffect(Unit) {
        // Animate subtle train track flow
        trainOffset.animateTo(
            targetValue = 100f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    LaunchedEffect(Unit) {
        // Stay on splash for 2.2 seconds total then seamlessly transition
        delay(2200)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF9FBF9),
                        Color(0xFFF0F7F2),
                        Color(0xFFE2F3E7)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(32.dp)
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            // Logo Card with soft shadow
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .size(170.dp)
                    .shadow(12.dp, RoundedCornerShape(28.dp), spotColor = MetroGreenPrimary.copy(alpha = 0.3f))
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_metro_logo),
                        contentDescription = "Metro - Línea 1",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Main Branding Title
            Text(
                text = "Metro - Línea 1",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MetroGreenDark,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle Tagline
            Text(
                text = "Control de Saldo, Pasajes y Viajes",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF5A685D)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Professional Transit-themed track progress indicator
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(4.dp)
                    .background(Color(0xFFD6E8DC), RoundedCornerShape(2.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = (trainOffset.value * 0.45f).dp)
                        .width(42.dp)
                        .height(4.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(MetroGreenLight, MetroGreenPrimary, MetroGreenDark)
                            ),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Iniciando sistema...",
                fontSize = 11.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Normal
            )
        }

        // Bottom Footer
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MetroGreenPrimary, CircleShape)
                )
                Text(
                    text = "Sistema Oficial de Control Offline",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MetroGreenPrimary
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Lima, Perú • Red del Metro",
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}
