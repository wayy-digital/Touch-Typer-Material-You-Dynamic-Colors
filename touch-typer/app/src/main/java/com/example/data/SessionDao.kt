package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM typing_sessions ORDER BY timestamp DESC")
    fun getAllSessionsFlow(): Flow<List<Session>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: Session): Long

    @Query("SELECT AVG(wpm) FROM typing_sessions")
    suspend fun getAverageWpm(): Double?

    @Query("SELECT MAX(wpm) FROM typing_sessions")
    suspend fun getMaxWpm(): Double?

    @Query("SELECT AVG(accuracy) FROM typing_sessions")
    suspend fun getAverageAccuracy(): Double?

    @Query("SELECT COUNT(*) FROM typing_sessions")
    suspend fun getSessionsCount(): Int
}
