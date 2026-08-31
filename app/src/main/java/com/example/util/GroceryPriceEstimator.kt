package com.example.util

import com.example.model.BudgetSavingsTip
import com.example.model.BudgetStatus
import com.example.model.CategorySpend
import com.example.model.GroceryBudgetSummary
import com.example.model.ShoppingItem
import java.util.Locale
import kotlin.math.roundToInt

object GroceryPriceEstimator {

    data class ItemPriceInfo(
        val basePrice: Double,
        val standardUnit: String,
        val defaultCategory: String,
        val priceTier: String = "$$" // $, $$, $$$
    )

    // Comprehensive simulated supermarket price database (USD)
    private val SIMULATED_PRICE_CATALOG = mapOf(
        // Produce
        "avocado" to ItemPriceInfo(1.49, "1 pc", "Fresh Produce", "$$"),
        "apple" to ItemPriceInfo(1.19, "1 pc", "Fresh Produce", "$"),
        "banana" to ItemPriceInfo(0.39, "1 pc", "Fresh Produce", "$"),
        "berries" to ItemPriceInfo(3.99, "6 oz pack", "Fresh Produce", "$$$"),
        "strawberry" to ItemPriceInfo(3.49, "1 lb pack", "Fresh Produce", "$$"),
        "blueberry" to ItemPriceInfo(3.99, "1 pint", "Fresh Produce", "$$$"),
        "raspberry" to ItemPriceInfo(4.29, "6 oz pack", "Fresh Produce", "$$$"),
        "spinach" to ItemPriceInfo(2.49, "1 bag (5 oz)", "Fresh Produce", "$"),
        "kale" to ItemPriceInfo(2.29, "1 bunch", "Fresh Produce", "$"),
        "lettuce" to ItemPriceInfo(1.99, "1 head", "Fresh Produce", "$"),
        "arugula" to ItemPriceInfo(2.99, "1 container", "Fresh Produce", "$$"),
        "onion" to ItemPriceInfo(1.09, "1 lb", "Fresh Produce", "$"),
        "red onion" to ItemPriceInfo(1.29, "1 lb", "Fresh Produce", "$"),
        "garlic" to ItemPriceInfo(0.79, "1 head", "Fresh Produce", "$"),
        "ginger" to ItemPriceInfo(1.29, "1 piece", "Fresh Produce", "$"),
        "lemon" to ItemPriceInfo(0.69, "1 pc", "Fresh Produce", "$"),
        "lime" to ItemPriceInfo(0.59, "1 pc", "Fresh Produce", "$"),
        "tomato" to ItemPriceInfo(1.89, "1 lb", "Fresh Produce", "$"),
        "cherry tomato" to ItemPriceInfo(2.99, "1 pint", "Fresh Produce", "$$"),
        "bell pepper" to ItemPriceInfo(1.49, "1 pc", "Fresh Produce", "$$"),
        "red pepper" to ItemPriceInfo(1.69, "1 pc", "Fresh Produce", "$$"),
        "jalapeno" to ItemPriceInfo(0.49, "1 pc", "Fresh Produce", "$"),
        "cucumber" to ItemPriceInfo(0.99, "1 pc", "Fresh Produce", "$"),
        "zucchini" to ItemPriceInfo(1.39, "1 pc", "Fresh Produce", "$"),
        "broccoli" to ItemPriceInfo(2.19, "1 crown", "Fresh Produce", "$"),
        "cauliflower" to ItemPriceInfo(2.99, "1 head", "Fresh Produce", "$$"),
        "carrot" to ItemPriceInfo(1.49, "1 lb bag", "Fresh Produce", "$"),
        "potato" to ItemPriceInfo(3.49, "5 lb bag", "Fresh Produce", "$"),
        "sweet potato" to ItemPriceInfo(1.39, "1 lb", "Fresh Produce", "$"),
        "mushroom" to ItemPriceInfo(2.69, "8 oz pack", "Fresh Produce", "$$"),
        "asparagus" to ItemPriceInfo(3.99, "1 bunch", "Fresh Produce", "$$$"),
        "celery" to ItemPriceInfo(1.79, "1 bunch", "Fresh Produce", "$"),
        "cilantro" to ItemPriceInfo(0.99, "1 bunch", "Fresh Produce", "$"),
        "parsley" to ItemPriceInfo(1.19, "1 bunch", "Fresh Produce", "$"),
        "basil" to ItemPriceInfo(2.49, "1 pack", "Fresh Produce", "$$"),
        "rosemary" to ItemPriceInfo(2.29, "1 pack", "Fresh Produce", "$$"),
        "thyme" to ItemPriceInfo(2.29, "1 pack", "Fresh Produce", "$$"),
        "green onion" to ItemPriceInfo(0.99, "1 bunch", "Fresh Produce", "$"),
        "scallion" to ItemPriceInfo(0.99, "1 bunch", "Fresh Produce", "$"),
        "corn" to ItemPriceInfo(0.79, "1 ear", "Fresh Produce", "$"),
        "cabbage" to ItemPriceInfo(1.89, "1 head", "Fresh Produce", "$"),
        "eggplant" to ItemPriceInfo(2.19, "1 pc", "Fresh Produce", "$$"),

        // Meat & Seafood
        "chicken breast" to ItemPriceInfo(6.49, "1 lb", "Meat & Seafood", "$$"),
        "chicken" to ItemPriceInfo(5.99, "1 lb", "Meat & Seafood", "$$"),
        "chicken thigh" to ItemPriceInfo(4.79, "1 lb", "Meat & Seafood", "$"),
        "chicken wing" to ItemPriceInfo(5.49, "1 lb", "Meat & Seafood", "$$"),
        "ground beef" to ItemPriceInfo(6.99, "1 lb", "Meat & Seafood", "$$"),
        "beef" to ItemPriceInfo(8.99, "1 lb", "Meat & Seafood", "$$$"),
        "steak" to ItemPriceInfo(11.99, "1 lb", "Meat & Seafood", "$$$"),
        "ribeye" to ItemPriceInfo(14.99, "1 lb", "Meat & Seafood", "$$$"),
        "sirloin" to ItemPriceInfo(10.49, "1 lb", "Meat & Seafood", "$$$"),
        "pork chop" to ItemPriceInfo(5.29, "1 lb", "Meat & Seafood", "$$"),
        "pork" to ItemPriceInfo(4.99, "1 lb", "Meat & Seafood", "$$"),
        "bacon" to ItemPriceInfo(5.99, "12 oz pack", "Meat & Seafood", "$$"),
        "sausage" to ItemPriceInfo(4.69, "1 lb pack", "Meat & Seafood", "$$"),
        "turkey" to ItemPriceInfo(5.49, "1 lb", "Meat & Seafood", "$$"),
        "ground turkey" to ItemPriceInfo(4.99, "1 lb", "Meat & Seafood", "$$"),
        "salmon" to ItemPriceInfo(10.99, "1 lb", "Meat & Seafood", "$$$"),
        "salmon fillet" to ItemPriceInfo(11.49, "1 lb", "Meat & Seafood", "$$$"),
        "tuna" to ItemPriceInfo(9.99, "1 lb", "Meat & Seafood", "$$$"),
        "cod" to ItemPriceInfo(8.49, "1 lb", "Meat & Seafood", "$$$"),
        "tilapia" to ItemPriceInfo(5.99, "1 lb", "Meat & Seafood", "$$"),
        "shrimp" to ItemPriceInfo(8.99, "1 lb bag", "Meat & Seafood", "$$$"),
        "tofu" to ItemPriceInfo(2.49, "14 oz block", "Meat & Seafood", "$"),
        "tempeh" to ItemPriceInfo(3.29, "8 oz pack", "Meat & Seafood", "$$"),

        // Dairy & Eggs
        "egg" to ItemPriceInfo(3.69, "1 dozen", "Dairy & Eggs", "$"),
        "eggs" to ItemPriceInfo(3.69, "1 dozen", "Dairy & Eggs", "$"),
        "milk" to ItemPriceInfo(3.49, "1 gallon", "Dairy & Eggs", "$"),
        "almond milk" to ItemPriceInfo(3.29, "64 oz carton", "Dairy & Eggs", "$$"),
        "oat milk" to ItemPriceInfo(3.99, "64 oz carton", "Dairy & Eggs", "$$"),
        "soy milk" to ItemPriceInfo(3.19, "64 oz carton", "Dairy & Eggs", "$"),
        "butter" to ItemPriceInfo(3.99, "1 lb (4 sticks)", "Dairy & Eggs", "$$"),
        "cheddar" to ItemPriceInfo(4.29, "8 oz block", "Dairy & Eggs", "$$"),
        "cheese" to ItemPriceInfo(3.99, "8 oz pack", "Dairy & Eggs", "$$"),
        "mozzarella" to ItemPriceInfo(3.89, "8 oz ball", "Dairy & Eggs", "$$"),
        "parmesan" to ItemPriceInfo(4.99, "5 oz wedge", "Dairy & Eggs", "$$$"),
        "feta" to ItemPriceInfo(4.49, "6 oz pack", "Dairy & Eggs", "$$"),
        "greek yogurt" to ItemPriceInfo(4.99, "32 oz tub", "Dairy & Eggs", "$$"),
        "yogurt" to ItemPriceInfo(3.49, "32 oz tub", "Dairy & Eggs", "$"),
        "sour cream" to ItemPriceInfo(2.19, "16 oz tub", "Dairy & Eggs", "$"),
        "heavy cream" to ItemPriceInfo(3.29, "1 pint", "Dairy & Eggs", "$$"),
        "cream cheese" to ItemPriceInfo(2.79, "8 oz block", "Dairy & Eggs", "$"),
        "cottage cheese" to ItemPriceInfo(2.99, "16 oz tub", "Dairy & Eggs", "$"),

        // Pantry, Grains & Oils
        "olive oil" to ItemPriceInfo(8.99, "500ml bottle", "Pantry & Oils", "$$$"),
        "extra virgin olive oil" to ItemPriceInfo(9.99, "500ml bottle", "Pantry & Oils", "$$$"),
        "vegetable oil" to ItemPriceInfo(3.99, "32 oz bottle", "Pantry & Oils", "$"),
        "sesame oil" to ItemPriceInfo(4.49, "5 oz bottle", "Pantry & Oils", "$$"),
        "coconut oil" to ItemPriceInfo(6.49, "14 oz jar", "Pantry & Oils", "$$"),
        "rice" to ItemPriceInfo(3.49, "2 lb bag", "Pantry & Oils", "$"),
        "jasmine rice" to ItemPriceInfo(4.29, "2 lb bag", "Pantry & Oils", "$$"),
        "basmati rice" to ItemPriceInfo(4.49, "2 lb bag", "Pantry & Oils", "$$"),
        "brown rice" to ItemPriceInfo(3.29, "2 lb bag", "Pantry & Oils", "$"),
        "quinoa" to ItemPriceInfo(4.99, "1 lb bag", "Pantry & Oils", "$$$"),
        "pasta" to ItemPriceInfo(1.69, "1 lb box", "Pantry & Oils", "$"),
        "spaghetti" to ItemPriceInfo(1.69, "1 lb box", "Pantry & Oils", "$"),
        "penne" to ItemPriceInfo(1.69, "1 lb box", "Pantry & Oils", "$"),
        "noodles" to ItemPriceInfo(1.99, "1 pack", "Pantry & Oils", "$"),
        "flour" to ItemPriceInfo(2.99, "5 lb bag", "Pantry & Oils", "$"),
        "all-purpose flour" to ItemPriceInfo(2.99, "5 lb bag", "Pantry & Oils", "$"),
        "sugar" to ItemPriceInfo(2.79, "4 lb bag", "Pantry & Oils", "$"),
        "brown sugar" to ItemPriceInfo(2.49, "2 lb bag", "Pantry & Oils", "$"),
        "honey" to ItemPriceInfo(5.49, "12 oz bottle", "Pantry & Oils", "$$$"),
        "maple syrup" to ItemPriceInfo(6.99, "12 oz bottle", "Pantry & Oils", "$$$"),
        "oats" to ItemPriceInfo(3.49, "18 oz canister", "Pantry & Oils", "$"),
        "bread" to ItemPriceInfo(2.99, "1 loaf", "Pantry & Oils", "$"),
        "sourdough" to ItemPriceInfo(4.49, "1 artisanal loaf", "Pantry & Oils", "$$"),
        "tortilla" to ItemPriceInfo(2.49, "10 ct pack", "Pantry & Oils", "$"),
        "black beans" to ItemPriceInfo(1.29, "15 oz can", "Pantry & Oils", "$"),
        "chickpeas" to ItemPriceInfo(1.39, "15 oz can", "Pantry & Oils", "$"),
        "beans" to ItemPriceInfo(1.29, "15 oz can", "Pantry & Oils", "$"),
        "canned tomato" to ItemPriceInfo(1.79, "28 oz can", "Pantry & Oils", "$"),
        "tomato paste" to ItemPriceInfo(1.09, "6 oz can", "Pantry & Oils", "$"),
        "tomato sauce" to ItemPriceInfo(1.49, "15 oz can", "Pantry & Oils", "$"),
        "broth" to ItemPriceInfo(2.49, "32 oz carton", "Pantry & Oils", "$"),
        "chicken broth" to ItemPriceInfo(2.49, "32 oz carton", "Pantry & Oils", "$"),
        "vegetable broth" to ItemPriceInfo(2.49, "32 oz carton", "Pantry & Oils", "$"),
        "peanut butter" to ItemPriceInfo(3.29, "16 oz jar", "Pantry & Oils", "$"),
        "almond butter" to ItemPriceInfo(6.49, "16 oz jar", "Pantry & Oils", "$$$"),
        "soy sauce" to ItemPriceInfo(2.79, "10 oz bottle", "Pantry & Oils", "$"),
        "tamari" to ItemPriceInfo(3.99, "10 oz bottle", "Pantry & Oils", "$$"),
        "fish sauce" to ItemPriceInfo(3.49, "7 oz bottle", "Pantry & Oils", "$$"),
        "hot sauce" to ItemPriceInfo(2.49, "6 oz bottle", "Pantry & Oils", "$"),
        "sriracha" to ItemPriceInfo(3.99, "17 oz bottle", "Pantry & Oils", "$$"),
        "mayonnaise" to ItemPriceInfo(3.79, "30 oz jar", "Pantry & Oils", "$$"),
        "mustard" to ItemPriceInfo(1.99, "12 oz bottle", "Pantry & Oils", "$"),
        "dijon mustard" to ItemPriceInfo(2.99, "8 oz jar", "Pantry & Oils", "$$"),
        "vinegar" to ItemPriceInfo(2.29, "16 oz bottle", "Pantry & Oils", "$"),
        "balsamic vinegar" to ItemPriceInfo(4.79, "16 oz bottle", "Pantry & Oils", "$$$"),
        "apple cider vinegar" to ItemPriceInfo(3.49, "16 oz bottle", "Pantry & Oils", "$$"),

        // Spices & Seasonings
        "salt" to ItemPriceInfo(1.19, "26 oz canister", "Spices & Seasonings", "$"),
        "sea salt" to ItemPriceInfo(2.49, "17 oz tub", "Spices & Seasonings", "$"),
        "kosher salt" to ItemPriceInfo(3.29, "3 lb box", "Spices & Seasonings", "$"),
        "black pepper" to ItemPriceInfo(3.49, "4 oz shaker", "Spices & Seasonings", "$$"),
        "peppercorns" to ItemPriceInfo(4.29, "6 oz grinder", "Spices & Seasonings", "$$"),
        "cumin" to ItemPriceInfo(2.99, "2 oz jar", "Spices & Seasonings", "$"),
        "paprika" to ItemPriceInfo(2.99, "2 oz jar", "Spices & Seasonings", "$"),
        "smoked paprika" to ItemPriceInfo(3.49, "2 oz jar", "Spices & Seasonings", "$$"),
        "oregano" to ItemPriceInfo(2.49, "1.5 oz jar", "Spices & Seasonings", "$"),
        "chili powder" to ItemPriceInfo(2.79, "2.5 oz jar", "Spices & Seasonings", "$"),
        "garlic powder" to ItemPriceInfo(2.49, "3 oz jar", "Spices & Seasonings", "$"),
        "onion powder" to ItemPriceInfo(2.49, "3 oz jar", "Spices & Seasonings", "$"),
        "cinnamon" to ItemPriceInfo(2.89, "2 oz jar", "Spices & Seasonings", "$"),
        "curry powder" to ItemPriceInfo(3.49, "2 oz jar", "Spices & Seasonings", "$$"),
        "turmeric" to ItemPriceInfo(3.19, "2 oz jar", "Spices & Seasonings", "$"),
        "vanilla extract" to ItemPriceInfo(6.99, "2 oz bottle", "Spices & Seasonings", "$$$"),
        "bay leaf" to ItemPriceInfo(2.49, "0.5 oz jar", "Spices & Seasonings", "$")
    )

    // Category default benchmark prices when item name is not in catalog
    private val CATEGORY_DEFAULTS = mapOf(
        "Fresh Produce" to 2.29,
        "Produce" to 2.29,
        "Meat & Seafood" to 7.49,
        "Dairy & Eggs" to 3.79,
        "Pantry & Oils" to 3.29,
        "Pantry" to 3.29,
        "Spices & Seasonings" to 2.79,
        "Condiments & Sauces" to 3.19,
        "Baking & Grains" to 3.49,
        "Snacks & Beverages" to 3.99,
        "Frozen Foods" to 4.49
    )

    /**
     * Estimates the cost of an item based on its name, amount, and category.
     */
    fun estimateItemCost(name: String, amount: String = "1", category: String = "Pantry"): Double {
        val lowerName = name.lowercase(Locale.ROOT).trim()
        val multiplier = parseQuantityMultiplier(amount)

        // 1. Direct or partial match in catalog
        val matchedCatalogEntry = SIMULATED_PRICE_CATALOG.entries
            .filter { (key, _) -> lowerName.contains(key) || key.contains(lowerName) }
            .maxByOrNull { (key, _) -> key.length }

        val baseCost = if (matchedCatalogEntry != null) {
            matchedCatalogEntry.value.basePrice
        } else {
            // 2. Category fallback with slight random deterministic adjustment based on name hash
            val defaultPrice = CATEGORY_DEFAULTS[category] ?: 2.99
            val variation = (name.hashCode().rem(10)) * 0.15
            (defaultPrice + variation).coerceAtLeast(0.99)
        }

        val total = baseCost * multiplier
        return (total * 100.0).roundToInt() / 100.0
    }

    /**
     * Returns a formatted price string e.g. "$4.29"
     */
    fun formatCurrency(amount: Double): String {
        return "$%.2f".format(Locale.US, amount)
    }

    /**
     * Retrieves price tier for item
     */
    fun getPriceTier(name: String, category: String): String {
        val lowerName = name.lowercase(Locale.ROOT).trim()
        val entry = SIMULATED_PRICE_CATALOG.entries
            .filter { (key, _) -> lowerName.contains(key) || key.contains(lowerName) }
            .maxByOrNull { (key, _) -> key.length }

        return entry?.value?.priceTier ?: when (category) {
            "Meat & Seafood" -> "$$$"
            "Dairy & Eggs", "Pantry & Oils" -> "$$"
            else -> "$"
        }
    }

    /**
     * Computes the complete budget summary across all shopping items against the weekly budget.
     */
    fun calculateBudgetSummary(items: List<ShoppingItem>, weeklyBudget: Double): GroceryBudgetSummary {
        var totalCost = 0.0
        var purchasedCost = 0.0
        var pendingCost = 0.0

        val categoryCostMap = mutableMapOf<String, Double>()
        val categoryCountMap = mutableMapOf<String, Int>()

        for (item in items) {
            val cost = estimateItemCost(item.name, item.amount, item.category)
            totalCost += cost
            if (item.isBought) {
                purchasedCost += cost
            } else {
                pendingCost += cost
            }

            val cat = normalizeCategory(item.category)
            categoryCostMap[cat] = (categoryCostMap[cat] ?: 0.0) + cost
            categoryCountMap[cat] = (categoryCountMap[cat] ?: 0) + 1
        }

        val remainingBudget = weeklyBudget - totalCost
        val percentUsed = if (weeklyBudget > 0) (totalCost / weeklyBudget).toFloat() else 0f

        val status = when {
            percentUsed > 1.0f -> BudgetStatus.OVER_BUDGET
            percentUsed >= 0.80f -> BudgetStatus.APPROACHING_BUDGET
            else -> BudgetStatus.WITHIN_BUDGET
        }

        val categorySpends = categoryCostMap.map { (cat, cost) ->
            CategorySpend(
                category = cat,
                totalCost = (cost * 100.0).roundToInt() / 100.0,
                itemCount = categoryCountMap[cat] ?: 0,
                percentageOfTotal = if (totalCost > 0) (cost / totalCost).toFloat() else 0f
            )
        }.sortedByDescending { it.totalCost }

        val avgCost = if (items.isNotEmpty()) totalCost / items.size else 0.0

        val tips = generateSavingsTips(items, totalCost, categorySpends, percentUsed)

        return GroceryBudgetSummary(
            weeklyBudget = weeklyBudget,
            totalEstimatedCost = (totalCost * 100.0).roundToInt() / 100.0,
            purchasedCost = (purchasedCost * 100.0).roundToInt() / 100.0,
            pendingCost = (pendingCost * 100.0).roundToInt() / 100.0,
            remainingBudget = (remainingBudget * 100.0).roundToInt() / 100.0,
            percentUsed = percentUsed,
            status = status,
            categorySpends = categorySpends,
            averageItemCost = (avgCost * 100.0).roundToInt() / 100.0,
            tips = tips
        )
    }

    private fun normalizeCategory(category: String): String {
        return when {
            category.contains("Produce", ignoreCase = true) -> "Fresh Produce"
            category.contains("Meat", ignoreCase = true) || category.contains("Seafood", ignoreCase = true) -> "Meat & Seafood"
            category.contains("Dairy", ignoreCase = true) || category.contains("Egg", ignoreCase = true) -> "Dairy & Eggs"
            category.contains("Spice", ignoreCase = true) || category.contains("Seasoning", ignoreCase = true) -> "Spices & Seasonings"
            else -> "Pantry & Oils"
        }
    }

    private fun parseQuantityMultiplier(amount: String): Double {
        val clean = amount.lowercase(Locale.ROOT).trim()

        // Match leading numbers e.g. "2 lbs", "3 cans", "1.5 kg"
        val numberRegex = """^([\d]+(?:\.[\d]+)?|\d+/\d+)""".toRegex()
        val match = numberRegex.find(clean)

        if (match != null) {
            val numStr = match.value
            val num = if (numStr.contains("/")) {
                val parts = numStr.split("/")
                val numPart = parts.getOrNull(0)?.toDoubleOrNull() ?: 1.0
                val denPart = parts.getOrNull(1)?.toDoubleOrNull() ?: 2.0
                numPart / denPart
            } else {
                numStr.toDoubleOrNull() ?: 1.0
            }

            // Adjust by unit weight/measure
            return when {
                clean.contains("kg") -> (num * 2.2).coerceIn(0.5, 6.0)
                clean.contains("g") && !clean.contains("kg") -> {
                    val grams = num
                    (grams / 400.0).coerceIn(0.3, 4.0)
                }
                clean.contains("oz") -> (num / 16.0).coerceIn(0.3, 4.0)
                clean.contains("lb") -> num.coerceIn(0.5, 5.0)
                clean.contains("bunch") -> num.coerceIn(0.8, 3.0)
                clean.contains("can") || clean.contains("bottle") || clean.contains("pack") -> num.coerceIn(0.5, 5.0)
                else -> num.coerceIn(0.5, 5.0)
            }
        }

        // Qualitative amounts
        return when {
            clean.contains("dozen") -> 1.0
            clean.contains("half") || clean.contains("1/2") -> 0.5
            clean.contains("quarter") || clean.contains("1/4") -> 0.3
            clean.contains("pinch") || clean.contains("dash") || clean.contains("tbsp") || clean.contains("tsp") -> 0.3
            clean.contains("large") || clean.contains("family") -> 1.5
            clean.contains("small") -> 0.7
            else -> 1.0
        }
    }

    private fun generateSavingsTips(
        items: List<ShoppingItem>,
        totalCost: Double,
        categorySpends: List<CategorySpend>,
        percentUsed: Float
    ): List<BudgetSavingsTip> {
        val tips = mutableListOf<BudgetSavingsTip>()

        val meatSpend = categorySpends.find { it.category == "Meat & Seafood" }
        if (meatSpend != null && meatSpend.percentageOfTotal >= 0.35f) {
            tips.add(
                BudgetSavingsTip(
                    title = "Plant-Protein Swaps",
                    description = "Meat & Seafood represents ${(meatSpend.percentageOfTotal * 100).toInt()}% of your basket. Replacing 1 meat meal with legumes, tofu, or eggs can save $4.50 - $8.00.",
                    potentialSavings = "~$6.50",
                    iconEmoji = "🌱"
                )
            )
        }

        val produceSpend = categorySpends.find { it.category == "Fresh Produce" }
        if (produceSpend != null && produceSpend.itemCount >= 3) {
            tips.add(
                BudgetSavingsTip(
                    title = "Seasonal Produce & Frozen Packs",
                    description = "Buying seasonal produce or frozen berry/veggie packs for cooked meals retains nutrients and reduces waste by up to 25%.",
                    potentialSavings = "~$3.20",
                    iconEmoji = "🥦"
                )
            )
        }

        val pantrySpend = categorySpends.find { it.category == "Pantry & Oils" }
        if (pantrySpend != null && pantrySpend.itemCount >= 4) {
            tips.add(
                BudgetSavingsTip(
                    title = "Store-Brand Pantry Staples",
                    description = "Grains, canned tomatoes, and cooking oils from store private labels are identical in quality but cost 20-30% less.",
                    potentialSavings = "~$4.00",
                    iconEmoji = "🏷️"
                )
            )
        }

        if (percentUsed > 1.0f) {
            tips.add(
                BudgetSavingsTip(
                    title = "Budget Target Alert",
                    description = "You're currently over your weekly budget target. Check your pantry inventory before checkout to avoid duplicate seasonings and oils.",
                    potentialSavings = "~$5.00+",
                    iconEmoji = "💡"
                )
            )
        } else if (tips.isEmpty()) {
            tips.add(
                BudgetSavingsTip(
                    title = "Well-Balanced Basket",
                    description = "Your grocery stash is well-proportioned across food groups and aligns comfortably with your weekly budget target.",
                    potentialSavings = "On Target",
                    iconEmoji = "✨"
                )
            )
        }

        return tips
    }
}
