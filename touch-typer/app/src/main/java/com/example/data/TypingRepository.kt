package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.math.roundToInt

class TypingRepository(private val database: AppDatabase) {
    private val userDao = database.userDao()
    private val sessionDao = database.sessionDao()

    val myProfile: Flow<User?> = userDao.getMyProfileFlow()
    val leaderboard: Flow<List<User>> = userDao.getLeaderboardFlow()
    val allSessions: Flow<List<Session>> = sessionDao.getAllSessionsFlow()

    suspend fun getMyProfileDirect(): User? = userDao.getMyProfileDirect()
    suspend fun getUserById(userId: Int): User? = userDao.getUserByIdDirect(userId)

    suspend fun updateMyProfile(user: User) {
        userDao.updateUser(user)
    }

    suspend fun logSession(wpm: Double, accuracy: Double, mode: String) {
        // 1. Save typing session
        val session = Session(wpm = wpm, accuracy = accuracy, mode = mode)
        sessionDao.insertSession(session)

        // 2. Fetch current user
        val profile = userDao.getMyProfileDirect() ?: return

        // 3. Compute updated typing statistics from all sessions
        val sessionsCount = sessionDao.getSessionsCount()
        val avgWpm = sessionDao.getAverageWpm() ?: wpm
        val maxWpm = sessionDao.getMaxWpm() ?: wpm
        val avgAccuracy = sessionDao.getAverageAccuracy() ?: accuracy

        // 4. Calculate XP earned
        // E.g., WPM * (Accuracy/100) * 10 = XP. Min 5 XP, max 100 XP per lesson to keep it balanced.
        val baseXp = (wpm * (accuracy / 100.0) * 8.0).roundToInt()
        val xpEarned = baseXp.coerceIn(10, 150)
        
        val newXp = profile.xp + xpEarned
        // XP formula for levels: 200 XP per level
        val newLevel = (newXp / 200) + 1

        val updatedProfile = profile.copy(
            xp = newXp,
            level = newLevel,
            averageWpm = (avgWpm * 10.0).roundToInt() / 10.0,
            maxWpm = (maxWpm * 10.0).roundToInt() / 10.0,
            accuracy = (avgAccuracy * 10.0).roundToInt() / 10.0,
            lessonsCompleted = sessionsCount
        )

        userDao.updateUser(updatedProfile)
    }

    // Seed check
    suspend fun checkAndSeedIfEmpty() {
        if (userDao.getUserCount() == 0) {
            // Under normal circumstances Room callback triggers on database creation,
            // but we can have this extra safeguard.
            val guest = User(
                username = "Guest Typist",
                aboutMe = "Leveling up my finger muscle memory!",
                avatarName = "avatar_ninja",
                xp = 0,
                level = 1,
                averageWpm = 0.0,
                maxWpm = 0.0,
                accuracy = 0.0,
                lessonsCompleted = 0,
                githubLink = "https://github.com",
                linkedinLink = "",
                twitterLink = "",
                isMe = true
            )
            userDao.insertUser(guest)
        }
    }
}
