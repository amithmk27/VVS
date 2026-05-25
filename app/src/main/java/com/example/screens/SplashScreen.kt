package com.example.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleSecondary
import com.example.ui.theme.DeepGold
import com.example.ui.theme.TextMedium
import com.example.ui.theme.SoftGreen
import com.example.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    currentUser: UserProfile?,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(2200) // Beautiful cinematic delay for the animated elements
        if (currentUser != null) {
            onNavigateToHome()
        } else {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFDF9), // warm off-white touch of gold
                        Color(0xFFF8FAF5)  // premium background sage-cream
                    )
                )
            )
            .testTag("splash_screen_root"),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(1200)) + expandVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                // 1. Auspicious styled App Logo (interlocking geometric mandalas)
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .background(PurplePrimary.copy(alpha = 0.08f), CircleShape)
                        .border(BorderStroke(2.dp, GoldAccent), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .border(BorderStroke(1.5.dp, GoldAccent), RoundedCornerShape(10.dp))
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .rotate(45f)
                            .border(BorderStroke(1.5.dp, GoldAccent), RoundedCornerShape(10.dp))
                    )
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(GoldAccent, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 2. Three Acharya portraits horizontally with gold circular borders, soft green accents & soft shadows (Responsive Layout)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .widthIn(max = 350.dp)
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    listOf(
                        R.drawable.shri_adi_shankaracharya to "Adi Shankaracharya",
                        R.drawable.shri_ramanandacharya to "Ramanandacharya",
                        R.drawable.shri_madhvacharya to "Madhvacharya"
                    ).forEach { (resId, name) ->
                        Card(
                            elevation = CardDefaults.cardElevation(8.dp),
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(2.5.dp, GoldAccent),
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(SoftGreen)
                                    .padding(4.dp), // Elegant inner framing mount effect
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = resId),
                                    contentDescription = "Portrait of $name",
                                    contentScale = ContentScale.Fit, // Fits completely without any cropping
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // 3. App title
                Text(
                    text = "VVS BRAHMIN MATRIMONY",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PurplePrimary,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Kannada Text
                Text(
                    text = "ವಧು ವರ ಸಂಗಮ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurplePrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 4. Tagline
                Text(
                    text = "“A Platform by Brahmins, for Brahmins”",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepGold,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 5. Traditional values subtitle
                Text(
                    text = "Connecting Brahmin families with trust and tradition",
                    fontSize = 13.sp,
                    color = TextMedium,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                CircularProgressIndicator(
                    color = PurplePrimary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
