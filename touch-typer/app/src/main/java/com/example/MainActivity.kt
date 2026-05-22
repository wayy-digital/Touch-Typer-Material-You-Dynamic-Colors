package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.GamesScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.PracticeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppTab
import com.example.viewmodel.TypingViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: TypingViewModel = viewModel()
      
      // Determine the dark theme preference:
      // If darkThemeMode is null -> follow system dark theme.
      // If true -> force dark.
      // If false -> force light.
      val darkTheme = when (viewModel.darkThemeMode) {
        null -> isSystemInDarkTheme()
        else -> viewModel.darkThemeMode == true
      }

      MyApplicationTheme(darkTheme = darkTheme, dynamicColor = viewModel.dynamicColorEnabled) {
        Scaffold(
          modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
          containerColor = MaterialTheme.colorScheme.background,
          bottomBar = {
            NavigationBar(
              containerColor = MaterialTheme.colorScheme.surfaceVariant,
              tonalElevation = 0.dp,
              modifier = Modifier.testTag("app_navigation_bar")
            ) {
              // 1. Practice Tab
              NavigationBarItem(
                selected = viewModel.currentTab == AppTab.PRACTICE,
                onClick = { viewModel.currentTab = AppTab.PRACTICE },
                icon = { Icon(Icons.Filled.Create, contentDescription = "Practice & Levels") },
                label = { Text("Learn", fontSize = 11.sp) },
                modifier = Modifier.testTag("nav_practice_tab")
              )

              // 2. Games Tab
              NavigationBarItem(
                selected = viewModel.currentTab == AppTab.GAMES,
                onClick = { viewModel.currentTab = AppTab.GAMES },
                icon = { Icon(Icons.Filled.PlayArrow, contentDescription = "Mini Games") },
                label = { Text("Play", fontSize = 11.sp) },
                modifier = Modifier.testTag("nav_games_tab")
              )

              // 3. Leaderboard Tab
              NavigationBarItem(
                selected = viewModel.currentTab == AppTab.LEADERBOARD,
                onClick = { viewModel.currentTab = AppTab.LEADERBOARD },
                icon = { Icon(Icons.Filled.Star, contentDescription = "Global Leaderboard") },
                label = { Text("Rank", fontSize = 11.sp) },
                modifier = Modifier.testTag("nav_leaderboard_tab")
              )

              // 4. Profile Tab
              NavigationBarItem(
                selected = viewModel.currentTab == AppTab.PROFILE,
                onClick = { viewModel.currentTab = AppTab.PROFILE },
                icon = { Icon(Icons.Filled.Person, contentDescription = "My Profile") },
                label = { Text("Profile", fontSize = 11.sp) },
                modifier = Modifier.testTag("nav_profile_tab")
              )

              // 5. Settings Tab
              NavigationBarItem(
                selected = viewModel.currentTab == AppTab.SETTINGS,
                onClick = { viewModel.currentTab = AppTab.SETTINGS },
                icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings Options") },
                label = { Text("Setup", fontSize = 11.sp) },
                modifier = Modifier.testTag("nav_settings_tab")
              )
            }
          }
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          ) {
            when (viewModel.currentTab) {
              AppTab.PRACTICE -> PracticeScreen(viewModel)
              AppTab.GAMES -> GamesScreen(viewModel)
              AppTab.LEADERBOARD -> LeaderboardScreen(viewModel)
              AppTab.PROFILE -> ProfileScreen(viewModel)
              AppTab.SETTINGS -> SettingsScreen(viewModel)
            }
          }
        }
      }
    }
  }
}
