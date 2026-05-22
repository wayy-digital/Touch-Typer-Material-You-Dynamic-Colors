package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.User
import com.example.ui.components.AvatarView
import com.example.viewmodel.TypingViewModel

@Composable
fun ProfileScreen(
    viewModel: TypingViewModel,
    modifier: Modifier = Modifier
) {
    val myProfile by viewModel.myProfile.collectAsState()
    val scrollState = rememberScrollState()
    var isEditing by remember { mutableStateOf(false) }
    var showAvatarPicker by remember { mutableStateOf(false) }

    // local edit fields
    var editNickname by remember { mutableStateOf("") }
    var editBio by remember { mutableStateOf("") }
    var editGithub by remember { mutableStateOf("") }
    var editLinkedin by remember { mutableStateOf("") }
    var editTwitter by remember { mutableStateOf("") }

    // Put current profile info in edit fields when entering edit mode
    LaunchedEffect(isEditing) {
        if (isEditing) {
            myProfile?.let {
                editNickname = it.username
                editBio = it.aboutMe
                editGithub = it.githubLink
                editLinkedin = it.linkedinLink
                editTwitter = it.twitterLink
            }
        }
    }

    myProfile?.let { user ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header with Level Progress Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().testTag("profile_level_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        AvatarView(avatarName = user.avatarName, size = 80.dp)
                        IconButton(
                            onClick = { showAvatarPicker = true },
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .testTag("change_avatar_badge_btn")
                        ) {
                            Icon(Icons.Filled.Edit, "Edit Avatar", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                        }
                    }

                    Text(
                        text = user.username,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = user.aboutMe,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // XP Progressive Level tracker
                    val xpLimit = 200
                    val currentXpLevelProgress = user.xp % xpLimit
                    val ratio = currentXpLevelProgress.toFloat() / xpLimit.toFloat()

                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "LEVEL ${user.level}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "$currentXpLevelProgress / $xpLimit XP to next Lvl",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }

                        LinearProgressIndicator(
                            progress = ratio,
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }

            // High Level Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatsGaugeBox(label = "Average Speed", value = "${user.averageWpm.toInt()}", suffix = "WPM", modifier = Modifier.weight(1f))
                StatsGaugeBox(label = "Max Speed", value = "${user.maxWpm.toInt()}", suffix = "WPM", modifier = Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatsGaugeBox(label = "Accuracy", value = "${user.accuracy.toInt()}", suffix = "%", modifier = Modifier.weight(1f))
                StatsGaugeBox(label = "Completed", value = "${user.lessonsCompleted}", suffix = "Sess", modifier = Modifier.weight(1f))
            }

            // Edit Profile Button / Inline Fields
            if (!isEditing) {
                Button(
                    onClick = { isEditing = true },
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Icon(Icons.Filled.Edit, "Edit Bio")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Registration Profile", fontWeight = FontWeight.Bold)
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Update Registration Profile", fontWeight = FontWeight.Black, fontSize = 14.sp)

                        OutlinedTextField(
                            value = editNickname,
                            onValueChange = { editNickname = it },
                            label = { Text("Display Name") },
                            leadingIcon = { Icon(Icons.Filled.Person, "Name") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_nickname_field")
                        )

                        OutlinedTextField(
                            value = editBio,
                            onValueChange = { editBio = it },
                            label = { Text("About Me / Biography") },
                            leadingIcon = { Icon(Icons.Filled.Info, "Bio") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_bio_field")
                        )

                        OutlinedTextField(
                            value = editGithub,
                            onValueChange = { editGithub = it },
                            label = { Text("GitHub Profile URL") },
                            leadingIcon = { Icon(Icons.Filled.Create, "Github") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editLinkedin,
                            onValueChange = { editLinkedin = it },
                            label = { Text("LinkedIn URL") },
                            leadingIcon = { Icon(Icons.Filled.Home, "LinkedIn") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editTwitter,
                            onValueChange = { editTwitter = it },
                            label = { Text("Twitter / X URL") },
                            leadingIcon = { Icon(Icons.Filled.Share, "Twitter") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = { isEditing = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    viewModel.updateNicknameAndBio(
                                        newNickname = editNickname,
                                        newBio = editBio,
                                        github = editGithub,
                                        linkedin = editLinkedin,
                                        twitter = editTwitter
                                    )
                                    isEditing = false
                                },
                                modifier = Modifier.weight(1f).testTag("save_profile_btn")
                            ) {
                                Text("Save Settings")
                            }
                        }
                    }
                }
            }

            // Achievements Badge Center
            AchievementsDisplayCard(user = user)
        }

        // Avatar Picker dialog
        if (showAvatarPicker) {
            AvatarPickerDialog(
                onDismiss = { showAvatarPicker = false },
                onSelect = { avatarSelectedName ->
                    viewModel.updateAvatar(avatarSelectedName)
                    showAvatarPicker = false
                }
            )
        }
    }
}

@Composable
fun StatsGaugeBox(
    label: String,
    value: String,
    suffix: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(3.dp))
                Text(suffix, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun AchievementsDisplayCard(user: User) {
    // Collect achievement states dynamically based on the User's level, WPM, accuracy, lessons
    val accomplishments = com.example.data.getAchievementsForUser(user)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Earned Achievements 🏅",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                accomplishments.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (item.isUnlocked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (item.isUnlocked) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(item.icon, fontSize = 18.sp)
                        }

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                item.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (item.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                item.description,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }

                        if (item.isUnlocked) {
                            Icon(Icons.Filled.Done, "Unlocked", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Filled.Lock, "Locked", tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AvatarPickerDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val options = listOf(
        "avatar_ninja", "avatar_astronaut", "avatar_cat", "avatar_wizard", "avatar_fox", "avatar_koala"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp),
        title = {
            Text("Select Your Character Avatar", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        },
        text = {
            Box(modifier = Modifier.height(200.dp)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(options) { avatar ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelect(avatar) }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AvatarView(avatarName = avatar, size = 64.dp)
                        }
                    }
                }
            }
        }
    )
}


