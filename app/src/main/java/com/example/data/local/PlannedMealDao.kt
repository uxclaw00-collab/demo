package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannedMealDao {
    @Query("SELECT * FROM planned_meals ORDER BY dateString ASC, id ASC")
    fun getAllPlannedMeals(): Flow<List<PlannedMealEntity>>

    @Query("SELECT * FROM planned_meals WHERE dateString BETWEEN :startDate AND :endDate ORDER BY dateString ASC, id ASC")
    fun getPlannedMealsForWeek(startDate: String, endDate: String): Flow<List<PlannedMealEntity>>

    @Query("SELECT * FROM planned_meals WHERE dateString = :dateString ORDER BY id ASC")
    fun getPlannedMealsForDate(dateString: String): Flow<List<PlannedMealEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannedMeal(plannedMeal: PlannedMealEntity): Long

    @Update
    suspend fun updatePlannedMeal(plannedMeal: PlannedMealEntity)

    @Delete
    suspend fun deletePlannedMeal(plannedMeal: PlannedMealEntity)

    @Query("DELETE FROM planned_meals WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM planned_meals WHERE dateString BETWEEN :startDate AND :endDate")
    suspend fun clearWeek(startDate: String, endDate: String)

    @Query("DELETE FROM planned_meals WHERE dateString = :dateString")
    suspend fun clearDate(dateString: String)
}
