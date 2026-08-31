package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MealLogDao {
    @Query("SELECT * FROM meal_logs ORDER BY timestampMillis DESC")
    fun getAllMealLogs(): Flow<List<MealLogEntity>>

    @Query("SELECT * FROM meal_logs WHERE dateString = :dateString ORDER BY timestampMillis DESC")
    fun getMealLogsByDate(dateString: String): Flow<List<MealLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLog(mealLog: MealLogEntity): Long

    @Update
    suspend fun updateMealLog(mealLog: MealLogEntity)

    @Delete
    suspend fun deleteMealLog(mealLog: MealLogEntity)

    @Query("DELETE FROM meal_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM meal_logs")
    suspend fun clearAll()
}
