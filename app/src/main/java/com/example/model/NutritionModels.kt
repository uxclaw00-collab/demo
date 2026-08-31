package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MealLog(
    val id: Long = 0,
    val recipeId: String,
    val recipeTitle: String,
    val servings: Float = 1.0f,
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val mealType: String = "Dinner", // Breakfast, Lunch, Dinner, Snack
    val timestampMillis: Long = System.currentTimeMillis(),
    val dateString: String = getTodayDateString()
) {
    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            return sdf.format(Date(timestampMillis))
        }

    companion object {
        fun getTodayDateString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }

        fun determineMealType(timestampMillis: Long = System.currentTimeMillis()): String {
            val sdf = SimpleDateFormat("H", Locale.getDefault())
            val hour = sdf.format(Date(timestampMillis)).toIntOrNull() ?: 12
            return when (hour) {
                in 5..10 -> "Breakfast"
                in 11..15 -> "Lunch"
                in 16..21 -> "Dinner"
                else -> "Late Snack"
            }
        }
    }
}

data class DailyNutritionSummary(
    val dateString: String,
    val totalCalories: Int,
    val totalProteinGrams: Int,
    val totalCarbsGrams: Int,
    val totalFatGrams: Int,
    val mealsCount: Int,
    val targetCalories: Int = 2000,
    val targetProteinGrams: Int = 120,
    val targetCarbsGrams: Int = 200,
    val targetFatGrams: Int = 65
) {
    val caloriesProgress: Float
        get() = if (targetCalories > 0) (totalCalories.toFloat() / targetCalories).coerceIn(0f, 1f) else 0f

    val proteinProgress: Float
        get() = if (targetProteinGrams > 0) (totalProteinGrams.toFloat() / targetProteinGrams).coerceIn(0f, 1f) else 0f

    val carbsProgress: Float
        get() = if (targetCarbsGrams > 0) (totalCarbsGrams.toFloat() / targetCarbsGrams).coerceIn(0f, 1f) else 0f

    val fatProgress: Float
        get() = if (targetFatGrams > 0) (totalFatGrams.toFloat() / targetFatGrams).coerceIn(0f, 1f) else 0f

    val remainingCalories: Int
        get() = (targetCalories - totalCalories).coerceAtLeast(0)

    val isCalorieOverGoal: Boolean
        get() = totalCalories > targetCalories
}

data class NutritionGoals(
    val targetCalories: Int = 2000,
    val targetProteinGrams: Int = 120,
    val targetCarbsGrams: Int = 200,
    val targetFatGrams: Int = 65
)
