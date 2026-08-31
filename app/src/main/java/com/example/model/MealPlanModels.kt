package com.example.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class MealSlot(val displayName: String, val iconEmoji: String) {
    BREAKFAST("Breakfast", "🍳"),
    LUNCH("Lunch", "🥗"),
    DINNER("Dinner", "🍲"),
    SNACK("Snack", "🍎")
}

data class DayInfo(
    val dayName: String,         // "Monday", "Tuesday", etc.
    val dayShort: String,        // "Mon", "Tue", etc.
    val dayNumber: String,       // "31", "1", etc.
    val dateString: String,      // "2026-08-31"
    val formattedDisplay: String, // "Mon, Aug 31"
    val isToday: Boolean
)

data class PlannedMeal(
    val id: Long = 0,
    val recipeId: String,
    val recipeTitle: String,
    val dayOfWeek: String,       // "Monday", "Tuesday", etc.
    val dateString: String,      // "yyyy-MM-dd"
    val mealSlot: String,        // "Breakfast", "Lunch", "Dinner", "Snack"
    val servings: Int = 1,
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val prepTimeMinutes: Int = 15,
    val cookTimeMinutes: Int = 20,
    val cuisine: String = "Global",
    val matchedIngredients: List<String> = emptyList(),
    val missingIngredients: List<String> = emptyList(),
    val isCooked: Boolean = false,
    val timestampMillis: Long = System.currentTimeMillis()
) {
    val totalTimeMinutes: Int get() = prepTimeMinutes + cookTimeMinutes

    companion object {
        fun fromRecipe(
            recipe: Recipe,
            dayOfWeek: String,
            dateString: String,
            mealSlot: String = "Dinner",
            servings: Int = 1
        ): PlannedMeal {
            return PlannedMeal(
                recipeId = recipe.id,
                recipeTitle = recipe.title,
                dayOfWeek = dayOfWeek,
                dateString = dateString,
                mealSlot = mealSlot,
                servings = servings,
                calories = recipe.calories * servings,
                proteinGrams = recipe.proteinGrams * servings,
                carbsGrams = recipe.carbsGrams * servings,
                fatGrams = recipe.fatGrams * servings,
                prepTimeMinutes = recipe.prepTimeMinutes,
                cookTimeMinutes = recipe.cookTimeMinutes,
                cuisine = recipe.cuisine,
                matchedIngredients = recipe.matchedIngredients,
                missingIngredients = recipe.missingIngredients,
                isCooked = false
            )
        }

        fun getMondayOfWeek(dateString: String = getTodayDateString()): String {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = sdf.parse(dateString) ?: Date()
                val cal = Calendar.getInstance().apply {
                    time = date
                    firstDayOfWeek = Calendar.MONDAY
                    set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                }
                sdf.format(cal.time)
            } catch (e: Exception) {
                getTodayDateString()
            }
        }

        fun getWeekDays(startMondayDateString: String): List<DayInfo> {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = getTodayDateString()
            val baseDate = try {
                sdf.parse(startMondayDateString) ?: Date()
            } catch (e: Exception) {
                Date()
            }

            val cal = Calendar.getInstance().apply {
                time = baseDate
                firstDayOfWeek = Calendar.MONDAY
                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            }

            val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
            val dayShorts = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val outSdf = SimpleDateFormat("MMM d", Locale.getDefault())
            val dayNumSdf = SimpleDateFormat("d", Locale.getDefault())

            val list = mutableListOf<DayInfo>()
            for (i in 0 until 7) {
                val dStr = sdf.format(cal.time)
                val isToday = dStr == todayStr
                list.add(
                    DayInfo(
                        dayName = dayNames[i],
                        dayShort = dayShorts[i],
                        dayNumber = dayNumSdf.format(cal.time),
                        dateString = dStr,
                        formattedDisplay = "${dayShorts[i]}, ${outSdf.format(cal.time)}",
                        isToday = isToday
                    )
                )
                cal.add(Calendar.DAY_OF_MONTH, 1)
            }
            return list
        }

        fun getTodayDateString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }

        fun shiftWeek(currentMonday: String, weekDelta: Int): String {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = sdf.parse(currentMonday) ?: Date()
                val cal = Calendar.getInstance().apply {
                    time = date
                    add(Calendar.WEEK_OF_YEAR, weekDelta)
                }
                sdf.format(cal.time)
            } catch (e: Exception) {
                getTodayDateString()
            }
        }

        fun formatWeekRange(startMonday: String): String {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val startDate = sdf.parse(startMonday) ?: Date()
                val cal = Calendar.getInstance().apply {
                    time = startDate
                    add(Calendar.DAY_OF_MONTH, 6)
                }
                val endDate = cal.time
                val outMonthSdf = SimpleDateFormat("MMM d", Locale.getDefault())
                val outYearSdf = SimpleDateFormat("yyyy", Locale.getDefault())
                "${outMonthSdf.format(startDate)} – ${outMonthSdf.format(endDate)}, ${outYearSdf.format(endDate)}"
            } catch (e: Exception) {
                startMonday
            }
        }
    }
}

data class WeeklyNutritionSummary(
    val totalCalories: Int,
    val avgDailyCalories: Int,
    val totalProteinGrams: Int,
    val totalCarbsGrams: Int,
    val totalFatGrams: Int,
    val totalMealsCount: Int,
    val cookedMealsCount: Int
)
