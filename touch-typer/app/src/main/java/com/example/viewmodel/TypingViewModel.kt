package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Session
import com.example.data.TypingRepository
import com.example.data.User
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

enum class AppTab {
    PRACTICE, GAMES, LEADERBOARD, PROFILE, SETTINGS
}

enum class PracticeSubMode {
    LEVELS, ENDLESS, GAME_FALLING, GAME_SPRINT
}

// Falling Letters Game Element
data class FallingLetter(
    val id: Int,
    val char: Char,
    val posX: Float, // 0.0 to 1.0 representing horizontal progress in the game canvas
    val posY: Float, // 0.0 to 1.0 representing vertical drop progress
    val speed: Float
)

enum class LeaderboardSortBy {
    XP, SPEED, ACCURACY
}

class TypingViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val repository: TypingRepository
    private val prefs = context.getSharedPreferences("touch_typer_prefs", Context.MODE_PRIVATE)

    // User preference settings initialized dynamically at declaration
    var clickSoundEnabled by mutableStateOf(prefs.getBoolean("click_sound", true))
    var errorHapticsEnabled by mutableStateOf(prefs.getBoolean("error_haptics", true))
    var dynamicColorEnabled by mutableStateOf(prefs.getBoolean("dynamic_color", true))
    var darkThemeMode by mutableStateOf<Boolean?>(
        when (prefs.getString("dark_theme_mode", "SYSTEM")) {
            "DARK" -> true
            "LIGHT" -> false
            else -> null
        }
    )

    init {
        val database = AppDatabase.getDatabase(context)
        repository = TypingRepository(database)
        viewModelScope.launch {
            repository.checkAndSeedIfEmpty()
        }
    }

    // 1. Navigation and Tab states
    var currentTab by mutableStateOf(AppTab.PRACTICE)
    var practiceSubMode by mutableStateOf(PracticeSubMode.LEVELS)
    var selectedLevelIndex by mutableStateOf(0)
    var selectedLeaderboardUser by mutableStateOf<User?>(null)

    // Leaderboard Sort States
    private val _leaderboardSortBy = MutableStateFlow(LeaderboardSortBy.XP)
    val leaderboardSortByState = _leaderboardSortBy.asStateFlow()

    var leaderboardSortBy: LeaderboardSortBy
        get() = _leaderboardSortBy.value
        set(value) {
            _leaderboardSortBy.value = value
        }

    // Observables from Database
    val myProfile: StateFlow<User?> = repository.myProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val leaderboard: StateFlow<List<User>> = combine(
        repository.leaderboard,
        _leaderboardSortBy
    ) { list, sortBy ->
        when (sortBy) {
            LeaderboardSortBy.XP -> list.sortedWith(compareByDescending<User> { it.xp }.thenByDescending { it.averageWpm })
            LeaderboardSortBy.SPEED -> list.sortedWith(compareByDescending<User> { it.averageWpm }.thenByDescending { it.accuracy })
            LeaderboardSortBy.ACCURACY -> list.sortedWith(compareByDescending<User> { it.accuracy }.thenByDescending { it.averageWpm })
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allSessions: StateFlow<List<Session>> = repository.allSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 2. Typing Engine State (Levels & Endless)
    var targetText by mutableStateOf("")
    var typedText by mutableStateOf("")
    var typingStartTime by mutableStateOf(0L)
    var typingWpm by mutableStateOf(0.0)
    var typingAccuracy by mutableStateOf(100.0)
    var typingCpm by mutableStateOf(0)
    var typingErrors by mutableStateOf(0)
    var isTypingFinished by mutableStateOf(false)
    var isTypingActive by mutableStateOf(false)
    
    // Configurable endless length
    var endlessLength by mutableStateOf("Medium") // "Short", "Medium", "Long"

    fun setClickSound(enabled: Boolean) {
        clickSoundEnabled = enabled
        prefs.edit().putBoolean("click_sound", enabled).apply()
    }

    fun setErrorHaptics(enabled: Boolean) {
        errorHapticsEnabled = enabled
        prefs.edit().putBoolean("error_haptics", enabled).apply()
    }

    fun setDarkThemePreference(mode: Boolean?) {
        darkThemeMode = mode
        val modeStr = when (mode) {
            true -> "DARK"
            false -> "LIGHT"
            else -> "SYSTEM"
        }
        prefs.edit().putString("dark_theme_mode", modeStr).apply()
    }

    fun setDynamicColor(enabled: Boolean) {
        dynamicColorEnabled = enabled
        prefs.edit().putBoolean("dynamic_color", enabled).apply()
    }

    // Standard lists of materials for Level Exercises
    val levelsList = listOf(
        LevelItem(1, "Home Row Basic (Index & Middle)", "f j d k f j f j f j d k d k d k f d j k f j f j d k"),
        LevelItem(2, "Full Home Row Lineup", "f j s l a ; f s a d f l k ; l a s k j f d l a ; a l s k"),
        LevelItem(3, "Top Row Introduction", "r u e i o w q p f r j u d e k i f r j u s o l w a a r r e"),
        LevelItem(4, "Bottom Row Additions", "v m c , x . z / f v j m d c k , s x l . a z f v m c x z"),
        LevelItem(5, "Fluid home row & top row words", "red side rude desk rose poke wise free file row user seed pass feel real roof lose glass gold door"),
        LevelItem(6, "Full alphabet lower-case sentence", "the quick brown fox jumps over the lazy dog as fingers dance across the home row keyboard"),
        LevelItem(7, "Numeric Sequence speed run", "102 938 475 561 029 384 475 1928374650 49302 1029 3847 4710 4910"),
        LevelItem(8, "Punctuation & Capitalization Mastery", "Practicing touch-typing! Does WPM really matter? Yes, accuracy is #1. Hold Shift with your pinky."),
        LevelItem(9, "Developer Syntax Practice", "fun makeApp(user: String) { if (wpm >= 90) { return true } else { count++; } }"),
        LevelItem(10, "Ultimate Typing Champion Ascent", "Welcome to the grand master challenge. By practicing muscle memory daily, your fingers move automatically without visual search. True freedom is found in touch typing absolute mastery.")
    )

    // Library of words for custom typing lists
    private val vocabularyList = listOf(
        "speed", "touch", "typing", "memory", "muscle", "keyboard", "practice", "accuracy", "performance", "mastery",
        "trainer", "concept", "android", "material", "google", "competitor", "profile", "leaderboard", "achievement",
        "infinite", "challenge", "reflex", "sprint", "layout", "qwerty", "system", "theme", "database", "feedback",
        "visual", "level", "stat", "progress", "score", "game", "index", "middle", "finger", "gesture", "success",
        "expert", "developer", "champion", "experience", "learning", "engine", "interactive", "custom", "tactile"
    )

    // Mini-Game 1: Falling Letters States
    var fallingScore by mutableStateOf(0)
    var fallingLives by mutableStateOf(3)
    var fallingActiveLetters by mutableStateOf<List<FallingLetter>>(emptyList())
    var isFallingGameOver by mutableStateOf(false)
    private var fallingGameJob: Job? = null
    private var fallingIncrementId = 0

    // Mini-Game 2: Word Sprint States
    var sprintScore by mutableStateOf(0)
    var sprintTargetWord by mutableStateOf("")
    var sprintTypedValue by mutableStateOf("")
    var sprintTimerProgress by mutableStateOf(1.0f) // from 1.0 down to 0.0
    var isSprintGameOver by mutableStateOf(false)
    private var sprintGameJob: Job? = null

    // Initialize/Reset Touch Typing Levels or Endless Exercise
    fun initializeTypingExercise(customTextOverride: String? = null) {
        if (customTextOverride != null) {
            targetText = customTextOverride
        } else {
            if (practiceSubMode == PracticeSubMode.LEVELS) {
                targetText = levelsList[selectedLevelIndex].text
            } else {
                targetText = generateEndlessWords()
            }
        }
        typedText = ""
        typingStartTime = 0L
        typingWpm = 0.0
        typingAccuracy = 100.0
        typingCpm = 0
        typingErrors = 0
        isTypingFinished = false
        isTypingActive = false
    }

    private fun generateEndlessWords(): String {
        val wordCount = when (endlessLength) {
            "Short" -> 10
            "Medium" -> 20
            "Long" -> 40
            else -> 20
        }
        val sb = StringBuilder()
        for (i in 0 until wordCount) {
            sb.append(vocabularyList[Random.nextInt(vocabularyList.size)])
            if (i < wordCount - 1) sb.append(" ")
        }
        return sb.toString()
    }

    // Handles keystrokes from physical keyboard, virtual keyboard, or screen buttons
    fun handleInputChar(char: Char) {
        if (isTypingFinished) return

        if (!isTypingActive) {
            isTypingActive = true
            typingStartTime = System.currentTimeMillis()
        }

        val expectedNextIndex = typedText.length
        if (expectedNextIndex >= targetText.length) return

        val expectedChar = targetText[expectedNextIndex]

        if (char == expectedChar) {
            // Correct input
            typedText += char
            playSystemSound(SoundEffectConstants.CLICK)
        } else {
            // Incorrect input
            typingErrors++
            triggerErrorHaptics()
            // Optional: We can still record typing with highlighted color, or restrict typing until they press backspace.
            // Let's implement immediate error visual lock: they must write the correct character to advance!
            // This is standard blind typing protocol which strictly forces correction during learning.
        }

        // Calculate statistics live
        calculateLiveStats()

        // Check completion check
        if (typedText == targetText) {
            finishTypingExercise()
        }
    }

    fun handleBackspace() {
        if (typedText.isNotEmpty() && !isTypingFinished) {
            typedText = typedText.substring(0, typedText.length - 1)
            calculateLiveStats()
        }
    }

    private fun calculateLiveStats() {
        val elapsed = System.currentTimeMillis() - typingStartTime
        if (elapsed <= 0) return

        val elapsedMinutes = elapsed / 60000.0
        val typedCount = typedText.length
        
        // standard WPM formula: (typedCount / 5) / minutes
        typingWpm = if (elapsedMinutes > 0) {
            val rawWpm = (typedCount / 5.0) / elapsedMinutes
            (rawWpm * 10.0).roundToInt() / 10.0
        } else 0.0

        typingCpm = if (elapsedMinutes > 0) {
            (typedCount / elapsedMinutes).roundToInt()
        } else 0

        val totalInputs = typedCount + typingErrors
        typingAccuracy = if (totalInputs > 0) {
            val score = (typedCount.toDouble() / totalInputs.toDouble()) * 100.0
            (score * 10.0).roundToInt() / 10.0
        } else 100.0
    }

    private fun finishTypingExercise() {
        isTypingFinished = true
        isTypingActive = false

        // Logging statistics into database
        viewModelScope.launch {
            val modeName = if (practiceSubMode == PracticeSubMode.LEVELS) {
                "LEVEL_${selectedLevelIndex + 1}"
            } else {
                "PRACTICE"
            }
            repository.logSession(typingWpm, typingAccuracy, modeName)
        }
    }

    // Sound manager helpers
    private fun playSystemSound(effect: Int) {
        if (clickSoundEnabled) {
            // Android sound generator via standard click
            // Can trigger a silent window view click
        }
    }

    private fun triggerErrorHaptics() {
        if (errorHapticsEnabled) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    val vibrator = vibratorManager?.defaultVibrator
                    vibrator?.vibrate(android.os.VibrationEffect.createOneShot(45, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(45)
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    // Mini Games Engines

    // Mini-Game 1: Falling Letters Engine
    fun startFallingLettersGame() {
        stopFallingLettersGame()
        fallingScore = 0
        fallingLives = 3
        fallingActiveLetters = emptyList()
        isFallingGameOver = false
        fallingIncrementId = 0

        fallingGameJob = viewModelScope.launch {
            var spawnTimer = 0
            while (fallingLives > 0) {
                delay(50) // Tick speed
                spawnTimer += 50

                // 1. Core Spawn letters logic
                if (spawnTimer >= 1500) { // Spawn every 1.5 seconds
                    val randomChar = ('a'..'z').random()
                    // Random X coordinate (keep buffer from borders)
                    val randomX = Random.nextFloat() * 0.8f + 0.1f
                    // Speed scalar based on screen settings
                    val randomSpeed = Random.nextFloat() * 0.015f + 0.007f + (fallingScore * 0.0005f)
                    
                    val newLetter = FallingLetter(
                        id = fallingIncrementId++,
                        char = randomChar,
                        posX = randomX,
                        posY = 0.0f,
                        speed = randomSpeed
                    )
                    fallingActiveLetters = fallingActiveLetters + newLetter
                    spawnTimer = 0
                }

                // 2. Core Movement and Collision check
                val updatedList = mutableListOf<FallingLetter>()
                for (letter in fallingActiveLetters) {
                    val newY = letter.posY + letter.speed
                    if (newY >= 1.0f) {
                        // Letter hit the ground, lost life
                        fallingLives--
                        triggerErrorHaptics()
                        if (fallingLives <= 0) {
                            endFallingLettersGame()
                            break
                        }
                    } else {
                        updatedList.add(letter.copy(posY = newY))
                    }
                }
                if (fallingLives > 0) {
                    fallingActiveLetters = updatedList
                }
            }
        }
    }

    fun handleFallingLetterInput(char: Char) {
        if (isFallingGameOver) return
        
        // Find if the typed char matches any letters on the canvas
        // Prioritize matching letters that are lowest down (highest posY)
        val matchingLetter = fallingActiveLetters
            .filter { it.char.lowercaseChar() == char.lowercaseChar() }
            .maxByOrNull { it.posY }

        if (matchingLetter != null) {
            // Scored hit! Correct blind key hit
            fallingActiveLetters = fallingActiveLetters.filter { it.id != matchingLetter.id }
            fallingScore += 10
            playSystemSound(SoundEffectConstants.CLICK)

            // Log micro progression XP for keeping gameplay active
            if (fallingScore % 100 == 0) {
                viewModelScope.launch {
                    repository.logSession(wpm = 75.0, accuracy = 100.0, mode = "GAME_FALLING")
                }
            }
        } else {
            // Visual error feedback on wrong key
            triggerErrorHaptics()
        }
    }

    private fun endFallingLettersGame() {
        isFallingGameOver = true
        stopFallingLettersGame()
        // Finalize state save
        viewModelScope.launch {
            // Log final stats
            repository.logSession(wpm = (fallingScore / 2.0).coerceIn(20.0, 110.0), accuracy = 95.0, mode = "GAME_FALLING")
        }
    }

    fun stopFallingLettersGame() {
        fallingGameJob?.cancel()
        fallingGameJob = null
    }

    // Mini-Game 2: Word Sprint Engine
    fun startWordSprintGame() {
        stopWordSprintGame()
        sprintScore = 0
        isSprintGameOver = false
        sprintTypedValue = ""
        spawnSprintWord()
    }

    private fun spawnSprintWord() {
        sprintTargetWord = vocabularyList[Random.nextInt(vocabularyList.size)]
        sprintTypedValue = ""
        sprintTimerProgress = 1.0f

        sprintGameJob?.cancel()
        sprintGameJob = viewModelScope.launch {
            val durationMs = (4000 - (sprintScore * 50)).coerceAtLeast(1500) // Word time shortens with higher score
            val intervalMs = 30L
            var elapsedMs = 0L

            while (elapsedMs < durationMs) {
                delay(intervalMs)
                elapsedMs += intervalMs
                sprintTimerProgress = 1.0f - (elapsedMs.toFloat() / durationMs.toFloat())
            }

            // Time ran out! Game over
            isSprintGameOver = true
            triggerErrorHaptics()
            repository.logSession(wpm = (sprintScore * 10.0).coerceIn(20.0, 120.0), accuracy = 90.0, mode = "GAME_SPRINT")
        }
    }

    fun handleSprintLetterInput(char: Char) {
        if (isSprintGameOver) return

        val expectedChar = sprintTargetWord.getOrNull(sprintTypedValue.length)
        if (char == expectedChar) {
            sprintTypedValue += char
            playSystemSound(SoundEffectConstants.CLICK)

            if (sprintTypedValue == sprintTargetWord) {
                sprintScore++
                // Word cleared successfully! Move to next word immediately
                spawnSprintWord()
            }
        } else {
            triggerErrorHaptics()
        }
    }

    fun handleSprintBackspace() {
        if (sprintTypedValue.isNotEmpty() && !isSprintGameOver) {
            sprintTypedValue = sprintTypedValue.substring(0, sprintTypedValue.length - 1)
        }
    }

    fun stopWordSprintGame() {
        sprintGameJob?.cancel()
        sprintGameJob = null
    }

    // Current Profile Control
    fun updateNicknameAndBio(newNickname: String, newBio: String, github: String, linkedin: String, twitter: String) {
        viewModelScope.launch {
            val current = repository.getMyProfileDirect() ?: return@launch
            val updated = current.copy(
                username = if (newNickname.trim().isEmpty()) "Guest Typist" else newNickname.trim(),
                aboutMe = newBio.trim(),
                githubLink = github.trim(),
                linkedinLink = linkedin.trim(),
                twitterLink = twitter.trim()
            )
            repository.updateMyProfile(updated)
        }
    }

    fun updateAvatar(avatarName: String) {
        viewModelScope.launch {
            val current = repository.getMyProfileDirect() ?: return@launch
            val updated = current.copy(avatarName = avatarName)
            repository.updateMyProfile(updated)
        }
    }

    // Reset statistics
    fun resetStatistics() {
        viewModelScope.launch {
            val current = repository.getMyProfileDirect() ?: return@launch
            val database = AppDatabase.getDatabase(context)
            
            // Re-seed DB
            database.clearAllTables()
            
            // Setup base profile again
            val blankUser = User(
                username = "Guest Typist",
                aboutMe = "Leveling up my finger muscle memory!",
                avatarName = "avatar_ninja",
                xp = 0,
                level = 1,
                averageWpm = 0.0,
                maxWpm = 0.0,
                accuracy = 0.0,
                lessonsCompleted = 0,
                githubLink = "",
                linkedinLink = "",
                twitterLink = "",
                isMe = true
            )
            database.userDao().insertUser(blankUser)

            // Seed initial bots again so and profile resets clean
            val botCompetitors = listOf(
                User(
                    username = "QwertyMaster",
                    aboutMe = "I live in the home row. 140 WPM is my chill pace.",
                    avatarName = "avatar_wizard",
                    xp = 4850,
                    level = 15,
                    averageWpm = 112.5,
                    maxWpm = 138.0,
                    accuracy = 98.7,
                    lessonsCompleted = 84,
                    githubLink = "https://github.com/qwertymaster",
                    linkedinLink = "https://linkedin.com/in/qwertymaster",
                    twitterLink = "https://twitter.com/qwertymaster",
                    isMe = false
                ),
                User(
                    username = "FingerDance",
                    aboutMe = "Using Dvorak layout! Absolute fluid motion.",
                    avatarName = "avatar_cat",
                    xp = 3900,
                    level = 12,
                    averageWpm = 94.2,
                    maxWpm = 115.0,
                    accuracy = 97.4,
                    lessonsCompleted = 72,
                    githubLink = "https://github.com/fingerdancer",
                    linkedinLink = "",
                    twitterLink = "https://twitter.com/fingerdancer",
                    isMe = false
                ),
                User(
                    username = "SpeedyKeys",
                    aboutMe = "Accuracy is temporary, speed is eternal- practicing touch typing has been awesome!",
                    avatarName = "avatar_astronaut",
                    xp = 2800,
                    level = 9,
                    averageWpm = 82.0,
                    maxWpm = 104.0,
                    accuracy = 94.8,
                    lessonsCompleted = 55,
                    isMe = false
                ),
                User(
                    username = "KeyboardPro",
                    aboutMe = "Mechanical keyboard enthusiast. Blue switches are lovely.",
                    avatarName = "avatar_fox",
                    xp = 1750,
                    level = 6,
                    averageWpm = 67.5,
                    maxWpm = 85.0,
                    accuracy = 96.2,
                    lessonsCompleted = 32,
                    githubLink = "https://github.com/kbpro",
                    isMe = false
                ),
                User(
                    username = "TypingTagger",
                    aboutMe = "Slowly but surely. Practice makes perfect!",
                    avatarName = "avatar_koala",
                    xp = 820,
                    level = 3,
                    averageWpm = 45.3,
                    maxWpm = 58.0,
                    accuracy = 93.1,
                    lessonsCompleted = 18,
                    isMe = false
                )
            )

            for (bot in botCompetitors) {
                database.userDao().insertUser(bot)
            }
        }
    }

    override fun onCleared() {
        stopFallingLettersGame()
        stopWordSprintGame()
        super.onCleared()
    }
}

// Data holder for Level info
data class LevelItem(
    val levelNumber: Int,
    val title: String,
    val text: String
)
