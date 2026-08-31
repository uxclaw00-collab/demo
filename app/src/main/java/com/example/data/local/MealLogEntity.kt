package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.MealLog

@Entity(tableName = "meal_logs")
data class MealLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipeId: String,
    val recipeTitle: String,
    val servings: Float = 1.0f,
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val mealType: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val dateString: String
) {
    fun toDomain(): MealLog = MealLog(
        id = id,
        recipeId = recipeId,
        recipeTitle = recipeTitle,
        servings = servings,
        calories = calories,
        proteinGrams = proteinGrams,
        carbsGrams = carbsGrams,
        fatGrams = fatGrams,
        mealType = mealType,
        timestampMillis = timestampMillis,
        dateString = dateString
    )

    companion object {
        fun fromDomain(m: MealLog): MealLogEntity = MealLogEntity(
            id = m.id,
            recipeId = m.recipeId,
            recipeTitle = m.recipeTitle,
            servings = m.servings,
            calories = m.calories,
            proteinGrams = m.proteinGrams,
            carbsGrams = m.carbsGrams,
            fatGrams = m.fatGrams,
            mealType = m.mealType,
            timestampMillis = m.timestampMillis,
            dateString = m.dateString
        )
    }
}
