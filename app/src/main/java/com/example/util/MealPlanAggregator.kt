package com.example.util

import com.example.model.PantryItem
import com.example.model.PlannedMeal
import java.util.Locale

data class AggregatedIngredientItem(
    val name: String,
    val category: String,
    val recipeSources: List<String>,
    val occurrenceCount: Int,
    val isInPantry: Boolean,
    val pantryQuantity: String? = null,
    val isSelectedForExport: Boolean = true
) {
    val isNeeded: Boolean get() = !isInPantry
}

data class AggregatedCategoryGroup(
    val category: String,
    val iconEmoji: String,
    val items: List<AggregatedIngredientItem>
)

data class AggregatedMealPlanSummary(
    val selectedMealsCount: Int,
    val selectedMeals: List<PlannedMeal>,
    val totalUniqueIngredientsCount: Int,
    val neededToBuyCount: Int,
    val inPantryCount: Int,
    val categories: List<AggregatedCategoryGroup>,
    val allItems: List<AggregatedIngredientItem>
) {
    fun toShareableText(): String {
        val sb = StringBuilder()
        sb.append("📋 Aggregated Grocery List from $selectedMealsCount Planned Meals\n")
        sb.append("────────────────────────────────────\n")
        sb.append("Selected Meals:\n")
        selectedMeals.forEach { meal ->
            sb.append("• ${meal.dayOfWeek} ${meal.mealSlot}: ${meal.recipeTitle}\n")
        }
        sb.append("\n🛒 NEEDED INGREDIENTS ($neededToBuyCount items):\n")
        val needed = allItems.filter { it.isNeeded }
        if (needed.isEmpty()) {
            sb.append("All ingredients are already in your pantry!\n")
        } else {
            needed.forEach { item ->
                sb.append("• [ ] ${item.name} (${item.category}) — for: ${item.recipeSources.joinToString(", ")}\n")
            }
        }
        sb.append("\n✅ ALREADY IN PANTRY ($inPantryCount items):\n")
        allItems.filter { it.isInPantry }.forEach { item ->
            val qty = item.pantryQuantity?.let { " ($it)" } ?: ""
            sb.append("• [✓] ${item.name}$qty — in stock\n")
        }
        return sb.toString()
    }
}

object MealPlanAggregator {

    fun aggregateIngredients(
        meals: List<PlannedMeal>,
        pantryItems: List<PantryItem>
    ): AggregatedMealPlanSummary {
        if (meals.isEmpty()) {
            return AggregatedMealPlanSummary(
                selectedMealsCount = 0,
                selectedMeals = emptyList(),
                totalUniqueIngredientsCount = 0,
                neededToBuyCount = 0,
                inPantryCount = 0,
                categories = emptyList(),
                allItems = emptyList()
            )
        }

        // Map ingredient key (lowercase normalized) -> Pair of canonical name, list of recipe titles, and category
        val ingredientMap = mutableMapOf<String, MutableMap<String, Any>>()

        meals.forEach { meal ->
            val allIngredients = (meal.matchedIngredients + meal.missingIngredients).distinct()
            allIngredients.forEach { rawName ->
                val trimmed = rawName.trim()
                if (trimmed.isNotBlank()) {
                    val key = normalizeIngredientName(trimmed)
                    val currentEntry = ingredientMap.getOrPut(key) {
                        mutableMapOf(
                            "name" to trimmed,
                            "recipes" to mutableListOf<String>(),
                            "category" to categorizeIngredient(trimmed)
                        )
                    }
                    @Suppress("UNCHECKED_CAST")
                    val recipeList = currentEntry["recipes"] as MutableList<String>
                    if (!recipeList.contains(meal.recipeTitle)) {
                        recipeList.add(meal.recipeTitle)
                    }
                }
            }
        }

        val allAggregatedList = ingredientMap.map { (_, data) ->
            val name = data["name"] as String
            val category = data["category"] as String
            @Suppress("UNCHECKED_CAST")
            val recipes = data["recipes"] as List<String>

            // Cross-reference with pantry items
            val matchedPantryItem = pantryItems.firstOrNull { pantry ->
                val pKey = normalizeIngredientName(pantry.name)
                val iKey = normalizeIngredientName(name)
                pKey.contains(iKey) || iKey.contains(pKey) ||
                        pantry.name.contains(name, ignoreCase = true) ||
                        name.contains(pantry.name, ignoreCase = true)
            }

            val isInPantry = matchedPantryItem != null
            val pantryQty = matchedPantryItem?.quantity

            AggregatedIngredientItem(
                name = name,
                category = category,
                recipeSources = recipes,
                occurrenceCount = recipes.size,
                isInPantry = isInPantry,
                pantryQuantity = pantryQty,
                isSelectedForExport = !isInPantry
            )
        }.sortedWith(compareBy({ it.isInPantry }, { it.category }, { it.name }))

        val neededCount = allAggregatedList.count { it.isNeeded }
        val inPantryCount = allAggregatedList.count { it.isInPantry }

        // Group by category
        val categoryOrder = listOf(
            "Produce" to "🥦",
            "Dairy & Eggs" to "🧀",
            "Meat & Seafood" to "🥩",
            "Pantry & Grains" to "🌾",
            "Spices & Condiments" to "🧂",
            "Bakery & Bread" to "🍞",
            "Other" to "🛒"
        )

        val grouped = allAggregatedList.groupBy { it.category }
        val categories = categoryOrder.mapNotNull { (catName, icon) ->
            val items = grouped[catName]
            if (!items.isNullOrEmpty()) {
                AggregatedCategoryGroup(category = catName, iconEmoji = icon, items = items)
            } else null
        } + grouped.filterNot { group -> categoryOrder.any { it.first == group.key } }.map { (catName, items) ->
            AggregatedCategoryGroup(category = catName, iconEmoji = "📦", items = items)
        }

        return AggregatedMealPlanSummary(
            selectedMealsCount = meals.size,
            selectedMeals = meals,
            totalUniqueIngredientsCount = allAggregatedList.size,
            neededToBuyCount = neededCount,
            inPantryCount = inPantryCount,
            categories = categories,
            allItems = allAggregatedList
        )
    }

    fun normalizeIngredientName(name: String): String {
        return name.lowercase(Locale.ROOT)
            .replace(Regex("^(fresh|raw|organic|diced|chopped|sliced|minced|dried|crushed|ground|cooked|boneless|skinless)\\s+"), "")
            .replace(Regex("\\s+(powder|flakes|leaves|seeds|sauce|paste|crushed|diced|sliced|chopped)$"), "")
            .trim()
    }

    fun categorizeIngredient(name: String): String {
        val lower = name.lowercase(Locale.ROOT)
        return when {
            lower.contains("egg") || lower.contains("milk") || lower.contains("cheese") ||
                    lower.contains("yogurt") || lower.contains("butter") || lower.contains("cream") ||
                    lower.contains("cheddar") || lower.contains("mozzarella") || lower.contains("parmesan") ||
                    lower.contains("feta") || lower.contains("ricotta") || lower.contains("sour cream") -> "Dairy & Eggs"

            lower.contains("chicken") || lower.contains("beef") || lower.contains("steak") ||
                    lower.contains("pork") || lower.contains("salmon") || lower.contains("tuna") ||
                    lower.contains("shrimp") || lower.contains("turkey") || lower.contains("bacon") ||
                    lower.contains("sausage") || lower.contains("fish") || lower.contains("lamb") ||
                    lower.contains("tilapia") || lower.contains("cod") || lower.contains("ham") -> "Meat & Seafood"

            lower.contains("bread") || lower.contains("tortilla") || lower.contains("pita") ||
                    lower.contains("bun") || lower.contains("bagel") || lower.contains("crust") -> "Bakery & Bread"

            lower.contains("oil") || lower.contains("rice") || lower.contains("pasta") ||
                    lower.contains("flour") || lower.contains("sugar") || lower.contains("spaghetti") ||
                    lower.contains("noodle") || lower.contains("bean") || lower.contains("chickpea") ||
                    lower.contains("lentil") || lower.contains("can") || lower.contains("broth") ||
                    lower.contains("stock") || lower.contains("quinoa") || lower.contains("oat") -> "Pantry & Grains"

            lower.contains("salt") || lower.contains("pepper") || lower.contains("spice") ||
                    lower.contains("oregano") || lower.contains("paprika") || lower.contains("cumin") ||
                    lower.contains("soy sauce") || lower.contains("vinegar") || lower.contains("mayo") ||
                    lower.contains("mustard") || lower.contains("ketchup") || lower.contains("salsa") ||
                    lower.contains("honey") || lower.contains("syrup") || lower.contains("cinnamon") ||
                    lower.contains("curry") || lower.contains("turmeric") || lower.contains("garlic powder") -> "Spices & Condiments"

            else -> "Produce"
        }
    }
}
