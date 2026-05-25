package com.example.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent

@Composable
fun BrahminAvatar(
    avatarUrl: String?,
    fullName: String,
    size: Dp = 80.dp,
    modifier: Modifier = Modifier
) {
    val isMale = avatarUrl?.startsWith("avatar_m") == true
    val nameInitials = fullName.split(" ")
        .mapNotNull { it.firstOrNull() }
        .take(2)
        .joinToString("")
        .uppercase()

    // Gorgeous gradient colors matching Brahmin Sage & Sandal branding
    val gradient = if (isMale) {
        listOf(Color(0xFF4C6654), Color(0xFF6A8B74), Color(0xFF8BA693)) // Sage Green gradient
    } else {
        listOf(Color(0xFFB48C5E), Color(0xFFD2A56D), Color(0xFFEAD0A8)) // Sandal & Gold gradient
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.radialGradient(gradient))
            .border(2.dp, GoldAccent, CircleShape)
            .testTag("brahmin_avatar_${avatarUrl ?: "placeholder"}"),
        contentAlignment = Alignment.Center
    ) {
        // Draw elegant spiritual symbol and styling on the background using standard Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = this.size.width
            val height = this.size.height

            // Render a subtle Vedic halo or temple ring on the border of the avatar
            drawCircle(
                color = GoldAccent.copy(alpha = 0.25f),
                radius = width * 0.42f,
                style = Stroke(width = 2.dp.toPx())
            )

            // For male profiles, let's draw a subtle traditional red & white Brahmin Tilak/Urdhva Pundra symbol
            if (isMale) {
                // Left white line
                drawLine(
                    color = Color.White.copy(alpha = 0.8f),
                    start = Offset(width * 0.45f, height * 0.15f),
                    end = Offset(width * 0.45f, height * 0.45f),
                    strokeWidth = 3.dp.toPx()
                )
                // Right white line
                drawLine(
                    color = Color.White.copy(alpha = 0.8f),
                    start = Offset(width * 0.55f, height * 0.15f),
                    end = Offset(width * 0.55f, height * 0.45f),
                    strokeWidth = 3.dp.toPx()
                )
                // Center yellow/saffron spot
                drawCircle(
                    color = Color(0xFFFF5722), // Saffron Red
                    radius = 2.dp.toPx(),
                    center = Offset(width * 0.5f, height * 0.35f)
                )
            } else {
                // For female profiles, let's draw a subtle sacred saffron and vermillion Bindi/Kumkum dot
                drawCircle(
                    color = Color(0xFFFFEB3B), // Saffron yellow outer halo
                    radius = 4.dp.toPx(),
                    center = Offset(width * 0.5f, height * 0.3f)
                )
                drawCircle(
                    color = Color(0xFFC2185B), // Deep red Kumkum center
                    radius = 2.5f.dp.toPx(),
                    center = Offset(width * 0.5f, height * 0.3f)
                )
            }
        }

        // Initials Text
        Text(
            text = nameInitials,
            color = Color.White,
            fontSize = (size.value * 0.35f).sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
        )
    }
}
