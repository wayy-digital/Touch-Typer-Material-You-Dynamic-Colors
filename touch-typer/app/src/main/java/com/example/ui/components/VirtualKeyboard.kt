package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VirtualKeyboard(
    nextCharToType: Char?,
    onKeyClick: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    val uppercase = nextCharToType?.isUpperCase() ?: false

    val row1 = listOf('q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p')
    val row2 = listOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', ';')
    val row3 = listOf('z', 'x', 'c', 'v', 'b', 'n', 'm', ',', '.', '/')

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            row1.forEach { char ->
                val displayChar = if (uppercase) char.uppercaseChar() else char
                KeyButton(
                    char = displayChar,
                    isNext = nextCharToType?.lowercaseChar() == char,
                    onClick = { onKeyClick(displayChar) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            row2.forEach { char ->
                val displayChar = if (uppercase) char.uppercaseChar() else char
                KeyButton(
                    char = displayChar,
                    isNext = nextCharToType?.lowercaseChar() == char,
                    onClick = { onKeyClick(displayChar) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 3 (with standard QWERTY offset)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            row3.forEach { char ->
                val displayChar = if (uppercase) char.uppercaseChar() else char
                KeyButton(
                    char = displayChar,
                    isNext = nextCharToType?.lowercaseChar() == char,
                    onClick = { onKeyClick(displayChar) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Spacebar and special actions row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Spacebar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (nextCharToType == ' ') MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.secondaryContainer
                    )
                    .clickable { onKeyClick(' ') }
                    .semantics { contentDescription = "Spacebar" },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SPACE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (nextCharToType == ' ') MaterialTheme.colorScheme.onPrimary 
                            else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun KeyButton(
    char: Char,
    isNext: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isNext -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surface
    }

    val textColor = when {
        isNext -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char.toString(),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}
