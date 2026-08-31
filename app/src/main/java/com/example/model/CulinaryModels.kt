package com.example.model

enum class DietaryFilter(
    val id: String,
    val label: String,
    val description: String,
    val iconEmoji: String
) {
    VEGETARIAN("vegetarian", "Vegetarian", "No meat or seafood", "🥗"),
    KETO("keto", "Keto", "High-fat, low-carb (<15g net carbs)", "🥑"),
    VEGAN("vegan", "Vegan", "100% plant-based, no dairy/eggs", "🌱"),
    GLUTEN_FREE("gluten_free", "Gluten-Free", "No wheat, barley, or rye", "🌾"),
    LOW_CARB("low_carb", "Low Carb", "Reduced carbohydrates", "🥦"),
    HIGH_PROTEIN("high_protein", "High Protein", ">30g protein per serving", "🍗"),
    DAIRY_FREE("dairy_free", "Dairy-Free", "No milk, cheese, or butter", "🥛"),
    QUICK_EASY("quick_easy", "Under 30 Min", "Fast weekday prep and cook", "⚡")
}

enum class Difficulty(val label: String, val stars: Int) {
    EASY("Easy", 1),
    MEDIUM("Medium", 2),
    HARD("Master Chef", 3)
}

data class DetectedIngredient(
    val name: String,
    val category: String = "Produce",
    val freshness: String = "Fresh",
    val isSelected: Boolean = true
)

data class CookingStep(
    val stepNumber: Int,
    val title: String,
    val instruction: String,
    val timerSeconds: Int? = null,
    val ingredientsUsed: List<String> = emptyList(),
    val chefTip: String? = null
)

data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val cuisine: String,
    val difficulty: Difficulty,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val calories: Int,
    val servings: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val dietaryTags: List<String>,
    val matchedIngredients: List<String>,
    val missingIngredients: List<String>,
    val steps: List<CookingStep>,
    val isSaved: Boolean = false
) {
    val totalTimeMinutes: Int get() = prepTimeMinutes + cookTimeMinutes
    val matchPercentage: Int
        get() {
            val total = matchedIngredients.size + missingIngredients.size
            if (total == 0) return 100
            return ((matchedIngredients.size.toDouble() / total) * 100).toInt()
        }
}

data class ShoppingItem(
    val id: Long = 0,
    val name: String,
    val amount: String = "1 item",
    val category: String = "Pantry",
    val recipeSource: String? = null,
    val isBought: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
