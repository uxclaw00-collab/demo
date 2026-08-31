package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.PlannedMeal
import org.json.JSONArray

@Entity(tableName = "planned_meals")
data class PlannedMealEntity(
    @PrimaryKey(autoGenerate = true)
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
    val matchedIngredientsJson: String = "[]",
    val missingIngredientsJson: String = "[]",
    val isCooked: Boolean = false,
    val timestampMillis: Long = System.currentTimeMillis()
) {
    fun toDomain(): PlannedMeal {
        val matched = mutableListOf<String>()
        try {
            val arr = JSONArray(matchedIngredientsJson)
            for (i in 0 until arr.length()) matched.add(arr.getString(i))
        } catch (_: Exception) {}

        val missing = mutableListOf<String>()
        try {
            val arr = JSONArray(missingIngredientsJson)
            for (i in 0 until arr.length()) missing.add(arr.getString(i))
        } catch (_: Exception) {}

        return PlannedMeal(
            id = id,
            recipeId = recipeId,
            recipeTitle = recipeTitle,
            dayOfWeek = dayOfWeek,
            dateString = dateString,
            mealSlot = mealSlot,
            servings = servings,
            calories = calories,
            proteinGrams = proteinGrams,
            carbsGrams = carbsGrams,
            fatGrams = fatGrams,
            prepTimeMinutes = prepTimeMinutes,
            cookTimeMinutes = cookTimeMinutes,
            cuisine = cuisine,
            matchedIngredients = matched,
            missingIngredients = missing,
            isCooked = isCooked,
            timestampMillis = timestampMillis
        )
    }

    companion object {
        fun fromDomain(m: PlannedMeal): PlannedMealEntity {
            val matchedArr = JSONArray()
            m.matchedIngredients.forEach { matchedArr.put(it) }

            val missingArr = JSONArray()
            m.missingIngredients.forEach { missingArr.put(it) }

            return PlannedMealEntity(
                id = m.id,
                recipeId = m.recipeId,
                recipeTitle = m.recipeTitle,
                dayOfWeek = m.dayOfWeek,
                dateString = m.dateString,
                mealSlot = m.mealSlot,
                servings = m.servings,
                calories = m.calories,
                proteinGrams = m.proteinGrams,
                carbsGrams = m.carbsGrams,
                fatGrams = m.fatGrams,
                prepTimeMinutes = m.prepTimeMinutes,
                cookTimeMinutes = m.cookTimeMinutes,
                cuisine = m.cuisine,
                matchedIngredientsJson = matchedArr.toString(),
                missingIngredientsJson = missingArr.toString(),
                isCooked = m.isCooked,
                timestampMillis = m.timestampMillis
            )
        }
    }
}
