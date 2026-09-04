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

enum class AllergenType(
    val id: String,
    val displayName: String,
    val iconEmoji: String,
    val description: String
) {
    DAIRY("dairy", "Dairy / Lactose", "🥛", "Contains milk, cheese, butter, yogurt, cream, or dairy derivatives"),
    EGGS("eggs", "Eggs", "🥚", "Contains whole eggs, egg whites, yolks, or mayonnaise"),
    GLUTEN("gluten", "Gluten / Wheat", "🌾", "Contains wheat, all-purpose flour, pasta, bread, barley, or soy sauce"),
    PEANUTS("peanuts", "Peanuts", "🥜", "Contains peanuts, peanut butter, or peanut oil"),
    TREE_NUTS("tree_nuts", "Tree Nuts", "🌰", "Contains almonds, walnuts, cashews, pecans, hazelnuts, or pine nuts"),
    SOY("soy", "Soy", "🌱", "Contains soy sauce, tofu, edamame, tempeh, miso, or soybean oil"),
    FISH("fish", "Fish", "🐟", "Contains finfish like salmon, tuna, cod, tilapia, or anchovies/fish sauce"),
    SHELLFISH("shellfish", "Shellfish & Crustaceans", "🦐", "Contains shrimp, prawns, crab, lobster, clams, or mussels"),
    SESAME("sesame", "Sesame", "🥯", "Contains sesame seeds, tahini, or sesame oil"),
    SULFITES("sulfites", "Sulfites / Wine", "🍷", "Contains wine, wine vinegars, or preserved dried fruits"),
    NIGHTSHADES("nightshades", "Nightshades", "🍅", "Contains tomatoes, bell peppers, chili peppers, or eggplants")
}

data class DetectedAllergen(
    val type: AllergenType,
    val triggerIngredients: List<String>,
    val riskLevel: String = "Contains Allergen"
)

data class IngredientNutritionDetail(
    val ingredientName: String,
    val estimatedCalories: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val fiberGrams: Double = 0.0,
    val sugarGrams: Double = 0.0,
    val sodiumMg: Int = 0,
    val portionDescription: String = "1 standard serving",
    val allergensContained: List<AllergenType> = emptyList(),
    val isMainProtein: Boolean = false,
    val isHealthyFat: Boolean = false
)

data class MicronutrientHighlight(
    val name: String,
    val amount: String,
    val dailyValuePercent: Int,
    val benefit: String,
    val iconEmoji: String
)

data class NutritionHealthBadge(
    val label: String,
    val emoji: String,
    val description: String
)

data class ParsedRecipeNutrition(
    val recipeId: String,
    val recipeTitle: String,
    val servings: Int,
    val caloriesPerServing: Int,
    val totalCalories: Int,
    val proteinGramsPerServing: Double,
    val carbsGramsPerServing: Double,
    val fatGramsPerServing: Double,
    val fiberGramsPerServing: Double,
    val sugarGramsPerServing: Double,
    val sodiumMgPerServing: Int,
    val netCarbsGramsPerServing: Double,
    val proteinRatioPercent: Int,
    val carbsRatioPercent: Int,
    val fatRatioPercent: Int,
    val detectedAllergens: List<DetectedAllergen>,
    val allergenFreeTags: List<String>,
    val ingredientDetails: List<IngredientNutritionDetail>,
    val healthBadges: List<NutritionHealthBadge>,
    val micronutrientHighlights: List<MicronutrientHighlight>
)
