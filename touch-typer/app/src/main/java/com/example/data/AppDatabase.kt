package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [User::class, Session::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "touch_typer_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(context.applicationContext))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Seed base competitors and user on background thread
            CoroutineScope(Dispatchers.IO).launch {
                val database = getDatabase(context)
                val userDao = database.userDao()
                
                // 1. Initial user profile
                userDao.insertUser(
                    User(
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
                )

                // 2. Initial simulated bot/competitor typists for the leaderboard
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
                        aboutMe = "Accuracy is temporary, speed is eternal! Just kidding.",
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
                    userDao.insertUser(bot)
                }
            }
        }
    }
}
