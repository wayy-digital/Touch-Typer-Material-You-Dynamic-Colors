package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.VirtualKeyboard
import com.example.viewmodel.LevelItem
import com.example.viewmodel.PracticeSubMode
import com.example.viewmodel.TypingViewModel
import kotlinx.coroutines.delay

@Composable
fun PracticeScreen(
    viewModel: TypingViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Keep hidden text field state
    var hiddenInputState by remember { mutableStateOf("") }

    // Re-initialize whenever level or sub-mode changes
    LaunchedEffect(viewModel.practiceSubMode, viewModel.selectedLevelIndex) {
        viewModel.initializeTypingExercise()
        hiddenInputState = " "
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    // Auto-focus the field to catch keystrokes
    LaunchedEffect(Unit) {
        delay(200)
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Select Tab (Levels vs Endless)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = { viewModel.practiceSubMode = PracticeSubMode.LEVELS },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.practiceSubMode == PracticeSubMode.LEVELS) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (viewModel.practiceSubMode == PracticeSubMode.LEVELS) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("submode_levels_tab")
            ) {
                Icon(Icons.Filled.List, contentDescription = "Levels Mode", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Levels", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { viewModel.practiceSubMode = PracticeSubMode.ENDLESS },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.practiceSubMode == PracticeSubMode.ENDLESS) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (viewModel.practiceSubMode == PracticeSubMode.ENDLESS) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("submode_endless_tab")
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = "Endless Mode", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Endless", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Sub-panels (Level Selector / Endless Options)
        AnimatedContent(
            targetState = viewModel.practiceSubMode,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "sub_mode_panel"
        ) { mode ->
            when (mode) {
                PracticeSubMode.LEVELS -> {
                    LevelSelectorCard(
                        levels = viewModel.levelsList,
                        selectedIndex = viewModel.selectedLevelIndex,
                        onSelect = { idx ->
                            viewModel.selectedLevelIndex = idx
                        }
                    )
                }
                PracticeSubMode.ENDLESS -> {
                    EndlessOptionCard(
                        selectedLength = viewModel.endlessLength,
                        onLengthSelect = { length ->
                            viewModel.endlessLength = length
                            viewModel.initializeTypingExercise()
                        }
                    )
                }
                else -> {}
            }
        }

        // Statistics Card (WPM, Accuracy, Progress)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(label = "SPEED", pValue = "${viewModel.typingWpm.toInt()}", suffix = "WPM")
                Divider(modifier = Modifier.height(30.dp).width(1.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                StatItem(label = "ACCURACY", pValue = "${viewModel.typingAccuracy.toInt()}", suffix = "%")
                Divider(modifier = Modifier.height(30.dp).width(1.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                StatItem(label = "KEYS", pValue = "${viewModel.typedText.length}", suffix = "/${viewModel.targetText.length}")
            }
        }

        // Typing Canvas Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("typing_canvas_card")
                .clickable {
                    // Force focus on tap
                    try { focusRequester.requestFocus() } catch (_: Exception) {}
                },
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, if (viewModel.isTypingActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Hidden Text Input capturing hardware & soft keyboard in a foolproof manner
                BasicTextField(
                    value = hiddenInputState,
                    onValueChange = { newVal ->
                        if (viewModel.isTypingFinished) return@BasicTextField
                        
                        // Compute keystroke
                        val oldLen = hiddenInputState.length
                        val newLen = newVal.length

                        if (newLen > oldLen) {
                            // Character typed
                            val typedChar = newVal.last()
                            viewModel.handleInputChar(typedChar)
                        } else if (newLen < oldLen) {
                            // Backspace deleted
                            viewModel.handleBackspace()
                        }

                        // Maintain focus state & reset to safe text width to avoid cursor creep
                        hiddenInputState = " "
                    },
                    modifier = Modifier
                        .size(1.dp)
                        .focusRequester(focusRequester),
                    textStyle = TextStyle(color = Color.Transparent)
                )

                // Rendered target text with colorful highlights
                val annotatedText = buildAnnotatedString {
                    val typed = viewModel.typedText
                    val target = viewModel.targetText

                    // Part 1: Correctly Typed (Green/Primary)
                    withStyle(style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )) {
                        append(typed)
                    }

                    // Part 2: Current character to type
                    if (typed.length < target.length) {
                        val currentPendingChar = target[typed.length]
                        val displayPending = if (currentPendingChar == ' ') "_" else currentPendingChar.toString()
                        withStyle(style = SpanStyle(
                            background = MaterialTheme.colorScheme.secondaryContainer,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Black
                        )) {
                            append(displayPending)
                        }

                        // Part 3: Future pending text
                        if (typed.length + 1 < target.length) {
                            withStyle(style = SpanStyle(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                fontWeight = FontWeight.Normal
                            )) {
                                append(target.substring(typed.length + 1))
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = annotatedText,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 28.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!viewModel.isTypingActive && !viewModel.isTypingFinished) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "💡 Tap here to start typing!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        // Exercise finished details overlay
        AnimatedVisibility(
            visible = viewModel.isTypingFinished,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, "Finished", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Text("Exercise Completed!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }

                    Text(
                        text = "Terrific output! You finished with a typing speed of ${viewModel.typingWpm.toInt()} WPM and an accuracy of ${viewModel.typingAccuracy.toInt()}%. You gained XP and leveled up!",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.initializeTypingExercise() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Try Again")
                        }

                        if (viewModel.practiceSubMode == PracticeSubMode.LEVELS && viewModel.selectedLevelIndex < viewModel.levelsList.size - 1) {
                            Button(
                                onClick = {
                                    viewModel.selectedLevelIndex++
                                    viewModel.initializeTypingExercise()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Next Level")
                            }
                        }
                    }
                }
            }
        }

        // On-Screen companion Keyboard
        VirtualKeyboard(
            nextCharToType = if (!viewModel.isTypingFinished && viewModel.typedText.length < viewModel.targetText.length) {
                viewModel.targetText[viewModel.typedText.length]
            } else null,
            onKeyClick = { char ->
                viewModel.handleInputChar(char)
                hiddenInputState = " "
            },
            modifier = Modifier.fillMaxWidth().testTag("practice_virtual_keyboard")
        )
    }
}

@Composable
fun StatItem(label: String, pValue: String, suffix: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(text = pValue, fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.width(2.dp))
            Text(text = suffix, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun LevelSelectorCard(
    levels: List<LevelItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Select Level Progression:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ScrollableTabRow(
                selectedTabIndex = selectedIndex,
                edgePadding = 0.dp,
                divider = {},
                indicator = {}
            ) {
                levels.forEachIndexed { idx, level: LevelItem ->
                    Tab(
                        selected = selectedIndex == idx,
                        onClick = { onSelect(idx) },
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selectedIndex == idx) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "Lvl ${level.levelNumber}",
                                color = if (selectedIndex == idx) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Current Level Description
            val activeLevel = levels[selectedIndex]
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "Goal: ${activeLevel.title}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun EndlessOptionCard(
    selectedLength: String,
    onLengthSelect: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Select Endless Practice Target Length:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Short", "Medium", "Long").forEach { length ->
                    val isSel = selectedLength == length
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSel) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { onLengthSelect(length) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$length Words",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
