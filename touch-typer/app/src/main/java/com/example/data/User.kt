package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val aboutMe: String = "Keep typing, keep moving!",
    val avatarName: String = "avatar_ninja", // e.g. avatar_ninja, avatar_astronaut, avatar_cat, avatar_wizard
    val xp: Int = 0,
    val level: Int = 1,
    val averageWpm: Double = 0.0,
    val maxWpm: Double = 0.0,
    val accuracy: Double = 0.0,
    val lessonsCompleted: Int = 0,
    val githubLink: String = "",
    val linkedinLink: String = "",
    val twitterLink: String = "",
    val isMe: Boolean = false
)

data class AchievementItem(
    val name: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean
)

fun getAchievementsForUser(user: User): List<AchievementItem> {
    return listOf(
        AchievementItem("Home Row Graduate", "Reach Level 5 as a touch-typist", "🔰", user.level >= 5),
        AchievementItem("Grandmaster Ascent", "Reach Level 10 of ultimate muscle memories", "🔮", user.level >= 10),
        AchievementItem("Swift Keys", "Reach a max speed of 50+ WPM", "⚡", user.maxWpm >= 50.0),
        AchievementItem("Meteor Velocity", "Reach an elite speed of 90+ WPM", "☄️", user.maxWpm >= 90.0),
        AchievementItem("Precision Sniper", "Maintain a historic accuracy of 96%+", "🎯", user.accuracy >= 96.0 && user.lessonsCompleted >= 3),
        AchievementItem("Marathon Climber", "Log 20+ completed typing matches", "🏃", user.lessonsCompleted >= 20),
        AchievementItem("Dedicated Starter", "Complete at least 5 rounds", "⚙️", user.lessonsCompleted >= 5)
    )
}

