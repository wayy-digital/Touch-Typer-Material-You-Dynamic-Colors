package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AvatarView(
    avatarName: String,
    size: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    val (emoji, gradient) = rememberAvatarProps(avatarName)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = (size.value * 0.55).sp
        )
    }
}

@Composable
fun rememberAvatarProps(avatarName: String): Pair<String, Brush> {
    val emoji = when (avatarName) {
        "avatar_ninja" -> "🥷"
        "avatar_astronaut" -> "👨‍🚀"
        "avatar_cat" -> "🐱"
        "avatar_wizard" -> "🧙"
        "avatar_fox" -> "🦊"
        "avatar_koala" -> "🐨"
        else -> "👤"
    }

    val gradient = when (avatarName) {
        "avatar_ninja" -> Brush.radialGradient(listOf(Color(0xFF434343), Color(0xFF000000)))
        "avatar_astronaut" -> Brush.radialGradient(listOf(Color(0xFF2196F3), Color(0xFF1565C0)))
        "avatar_cat" -> Brush.radialGradient(listOf(Color(0xFFFF9800), Color(0xFFE65100)))
        "avatar_wizard" -> Brush.radialGradient(listOf(Color(0xFF9C27B0), Color(0xFF4A148C)))
        "avatar_fox" -> Brush.radialGradient(listOf(Color(0xFFFF5722), Color(0xFFBF360C)))
        "avatar_koala" -> Brush.radialGradient(listOf(Color(0xFF9E9E9E), Color(0xFF424242)))
        else -> Brush.radialGradient(listOf(Color(0xFFB0BEC5), Color(0xFF78909C)))
    }

    return Pair(emoji, gradient)
}
