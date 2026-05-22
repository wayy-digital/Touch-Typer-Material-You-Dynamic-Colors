package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.PracticeSubMode
import com.example.viewmodel.TypingViewModel
import kotlinx.coroutines.delay

@Composable
fun GamesScreen(
    viewModel: TypingViewModel,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    var hiddenInputState by remember { mutableStateOf("") }
    var selectedGameType by remember { mutableStateOf("FALLING") } // "FALLING" or "SPRINT"

    // Focus handler
    LaunchedEffect(selectedGameType) {
        delay(200)
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    // Capture user inputs for games
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        BasicTextField(
            value = hiddenInputState,
            onValueChange = { newVal ->
                if (newVal.isNotEmpty()) {
                    val typedChar = newVal.last()
                    if (selectedGameType == "FALLING") {
                        viewModel.handleFallingLetterInput(typedChar)
                    } else {
                        viewModel.handleSprintLetterInput(typedChar)
                    }
                }
                hiddenInputState = " "
            },
            modifier = Modifier
                .size(1.dp)
                .focusRequester(focusRequester),
            textStyle = TextStyle(color = Color.Transparent)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mode select
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = {
                        selectedGameType = "FALLING"
                        viewModel.startFallingLettersGame()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedGameType == "FALLING") MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (selectedGameType == "FALLING") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).testTag("select_falling_game_btn")
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Falling Letters")
                    Spacer(Modifier.width(8.dp))
                    Text("Falling Keys", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        selectedGameType = "SPRINT"
                        viewModel.startWordSprintGame()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedGameType == "SPRINT") MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (selectedGameType == "SPRINT") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).testTag("select_sprint_game_btn")
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Word Sprint")
                    Spacer(Modifier.width(8.dp))
                    Text("Word Sprint", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (selectedGameType == "FALLING") {
                // FALLING KEYS GAME ARENA
                FallingKeysGameArena(viewModel, focusRequester)
            } else {
                // WORD SPRINT GAME ARENA
                WordSprintGameArena(viewModel, focusRequester)
            }
        }
    }
}

@Composable
fun ColumnScope.FallingKeysGameArena(
    viewModel: TypingViewModel,
    focusRequester: FocusRequester
) {
    // Start game initially
    LaunchedEffect(Unit) {
        viewModel.startFallingLettersGame()
    }

    // Stop thread when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopFallingLettersGame()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .clickable { try { focusRequester.requestFocus() } catch (_: Exception) {} },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Stats line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SCORE: ${viewModel.fallingScore}",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 1..3) {
                        val active = i <= viewModel.fallingLives
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Life $i",
                            tint = if (active) Color(0xFFE91E63) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF0F172A)) // Night Space theme
            ) {
                val canvasWidth = maxWidth
                val canvasHeight = maxHeight

                if (viewModel.isFallingGameOver) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("GAME OVER 🕹️", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Text("Final Score: ${viewModel.fallingScore}", fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)
                            Button(
                                onClick = { viewModel.startFallingLettersGame() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Play Again")
                            }
                        }
                    }
                } else {
                    // Render falling letters
                    viewModel.fallingActiveLetters.forEach { letter ->
                        val xOffset = (letter.posX * canvasWidth.value).dp
                        val yOffset = (letter.posY * canvasHeight.value).dp

                        Box(
                            modifier = Modifier
                                .offset(x = xOffset, y = yOffset)
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = letter.char.uppercaseChar().toString(),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Tip bar on bottom
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Red.copy(alpha = 0.15f))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "CRITICAL DANGER ZONE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF8A80)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ColumnScope.WordSprintGameArena(
    viewModel: TypingViewModel,
    focusRequester: FocusRequester
) {
    LaunchedEffect(Unit) {
        viewModel.startWordSprintGame()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopWordSprintGame()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .clickable { try { focusRequester.requestFocus() } catch (_: Exception) {} },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WORD SPRINT ⚡",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "SCORE: ${viewModel.sprintScore}",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (viewModel.isSprintGameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("TIME'S UP! ⏰", fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error)
                        Text("Sprint Cleared: ${viewModel.sprintScore} words", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(
                            onClick = { viewModel.startWordSprintGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Sprint Again")
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Timer bar
                    LinearProgressIndicator(
                        progress = viewModel.sprintTimerProgress,
                        color = if (viewModel.sprintTimerProgress < 0.3f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    // Sprint target word
                    Text(
                        text = viewModel.sprintTargetWord,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Typed value
                    Text(
                        text = viewModel.sprintTypedValue,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            "Type rapidly to clear the word!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}
