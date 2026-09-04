package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.CookingStep
import com.example.model.Difficulty
import com.example.model.Recipe
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "saved_recipes")
data class SavedRecipeEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val cuisine: String,
    val difficulty: String,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val calories: Int,
    val servings: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val dietaryTagsJson: String,
    val matchedIngredientsJson: String,
    val missingIngredientsJson: String,
    val stepsJson: String,
    val mealType: String = "Dinner",
    val savedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Recipe {
        val diff = try {
            Difficulty.valueOf(difficulty)
        } catch (_: Exception) {
            Difficulty.MEDIUM
        }

        val tags = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(dietaryTagsJson)
            for (i in 0 until jsonArray.length()) {
                tags.add(jsonArray.getString(i))
            }
        } catch (_: Exception) {}

        val matched = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(matchedIngredientsJson)
            for (i in 0 until jsonArray.length()) {
                matched.add(jsonArray.getString(i))
            }
        } catch (_: Exception) {}

        val missing = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(missingIngredientsJson)
            for (i in 0 until jsonArray.length()) {
                missing.add(jsonArray.getString(i))
            }
        } catch (_: Exception) {}

        val steps = mutableListOf<CookingStep>()
        try {
            val stepsArray = JSONArray(stepsJson)
            for (i in 0 until stepsArray.length()) {
                val obj = stepsArray.getJSONObject(i)
                val usedIngredients = mutableListOf<String>()
                if (obj.has("ingredientsUsed")) {
                    val ingArray = obj.getJSONArray("ingredientsUsed")
                    for (j in 0 until ingArray.length()) {
                        usedIngredients.add(ingArray.getString(j))
                    }
                }
                steps.add(
                    CookingStep(
                        stepNumber = obj.optInt("stepNumber", i + 1),
                        title = obj.optString("title", "Step ${i + 1}"),
                        instruction = obj.optString("instruction", ""),
                        timerSeconds = if (obj.has("timerSeconds") && !obj.isNull("timerSeconds")) obj.getInt("timerSeconds") else null,
                        ingredientsUsed = usedIngredients,
                        chefTip = if (obj.has("chefTip") && !obj.isNull("chefTip")) obj.getString("chefTip") else null
                    )
                )
            }
        } catch (_: Exception) {}

        return Recipe(
            id = id,
            title = title,
            description = description,
            cuisine = cuisine,
            difficulty = diff,
            prepTimeMinutes = prepTimeMinutes,
            cookTimeMinutes = cookTimeMinutes,
            calories = calories,
            servings = servings,
            proteinGrams = proteinGrams,
            carbsGrams = carbsGrams,
            fatGrams = fatGrams,
            dietaryTags = tags,
            matchedIngredients = matched,
            missingIngredients = missing,
            steps = steps,
            isSaved = true,
            mealType = mealType
        )
    }

    companion object {
        fun fromDomain(recipe: Recipe): SavedRecipeEntity {
            val tagsArray = JSONArray()
            recipe.dietaryTags.forEach { tagsArray.put(it) }

            val matchedArray = JSONArray()
            recipe.matchedIngredients.forEach { matchedArray.put(it) }

            val missingArray = JSONArray()
            recipe.missingIngredients.forEach { missingArray.put(it) }

            val stepsArray = JSONArray()
            recipe.steps.forEach { step ->
                val obj = JSONObject().apply {
                    put("stepNumber", step.stepNumber)
                    put("title", step.title)
                    put("instruction", step.instruction)
                    if (step.timerSeconds != null) put("timerSeconds", step.timerSeconds)
                    val ingArray = JSONArray()
                    step.ingredientsUsed.forEach { ingArray.put(it) }
                    put("ingredientsUsed", ingArray)
                    if (step.chefTip != null) put("chefTip", step.chefTip)
                }
                stepsArray.put(obj)
            }

            return SavedRecipeEntity(
                id = recipe.id,
                title = recipe.title,
                description = recipe.description,
                cuisine = recipe.cuisine,
                difficulty = recipe.difficulty.name,
                prepTimeMinutes = recipe.prepTimeMinutes,
                cookTimeMinutes = recipe.cookTimeMinutes,
                calories = recipe.calories,
                servings = recipe.servings,
                proteinGrams = recipe.proteinGrams,
                carbsGrams = recipe.carbsGrams,
                fatGrams = recipe.fatGrams,
                dietaryTagsJson = tagsArray.toString(),
                matchedIngredientsJson = matchedArray.toString(),
                missingIngredientsJson = missingArray.toString(),
                stepsJson = stepsArray.toString(),
                mealType = recipe.mealType
            )
        }
    }
}
