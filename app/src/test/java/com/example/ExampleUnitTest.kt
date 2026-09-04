package com.example

import com.example.model.PantryItem
import com.example.model.Recipe
import com.example.model.Difficulty
import com.example.util.SubstitutionProvider
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testButterSubstitution() {
        val sub = SubstitutionProvider.findSubstitutionsForIngredient("butter")
        assertNotNull(sub)
        val substitutes = sub!!.defaultSubstitutes.map { it.substituteName }
        assertTrue(substitutes.any { it.contains("Olive Oil", ignoreCase = true) })
        assertTrue(substitutes.any { it.contains("Greek Yogurt", ignoreCase = true) || it.contains("Coconut Oil", ignoreCase = true) })
    }

    @Test
    fun testSourCreamSubstitution() {
        val sub = SubstitutionProvider.findSubstitutionsForIngredient("sour cream")
        assertNotNull(sub)
        val substitutes = sub!!.defaultSubstitutes.map { it.substituteName }
        assertTrue(substitutes.any { it.contains("Greek Yogurt", ignoreCase = true) })
    }

    @Test
    fun testRecipeSubstitutionsDetection() {
        val dummyRecipe = Recipe(
            id = "test-1",
            title = "Pancakes",
            description = "Fluffy pancakes",
            cuisine = "American",
            difficulty = Difficulty.EASY,
            prepTimeMinutes = 10,
            cookTimeMinutes = 15,
            calories = 300,
            proteinGrams = 8,
            carbsGrams = 40,
            fatGrams = 10,
            servings = 2,
            matchedIngredients = listOf("Eggs", "Milk"),
            missingIngredients = listOf("Butter", "Maple Syrup"),
            dietaryTags = listOf("Vegetarian"),
            steps = emptyList()
        )

        val result = SubstitutionProvider.findSubstitutionsForRecipe(dummyRecipe)
        assertTrue(result.isNotEmpty())
        assertTrue(result.any { it.first.equals("Butter", ignoreCase = true) })
        assertTrue(result.any { it.first.equals("Eggs", ignoreCase = true) || it.first.equals("Milk", ignoreCase = true) })
    }

    @Test
    fun testPantryExpirationCalculations() {
        val now = System.currentTimeMillis()
        val expiredItem = PantryItem(
            id = 1,
            name = "Milk",
            quantity = "1L",
            category = "Dairy",
            expirationDateMillis = now - (2 * 24 * 60 * 60 * 1000L) // 2 days ago
        )
        assertTrue(expiredItem.isExpired)

        val expiringSoonItem = PantryItem(
            id = 2,
            name = "Heavy Cream",
            quantity = "250ml",
            category = "Dairy",
            expirationDateMillis = now + (2 * 24 * 60 * 60 * 1000L) // 2 days from now
        )
        assertFalse(expiringSoonItem.isExpired)
        assertTrue(expiringSoonItem.isExpiringSoon)

        val freshItem = PantryItem(
            id = 3,
            name = "Olive Oil",
            quantity = "1L",
            category = "Oils",
            expirationDateMillis = now + (60 * 24 * 60 * 60 * 1000L) // 60 days from now
        )
        assertFalse(freshItem.isExpired)
        assertFalse(freshItem.isExpiringSoon)
    }

    @Test
    fun testGroceryPriceEstimation() {
        val avocadoCost = com.example.util.GroceryPriceEstimator.estimateItemCost("Avocado", "2 pcs", "Fresh Produce")
        assertTrue(avocadoCost > 0.0)

        val oliveOilCost = com.example.util.GroceryPriceEstimator.estimateItemCost("Olive Oil", "1 bottle", "Pantry & Oils")
        assertTrue(oliveOilCost >= 5.0)

        val salmonCost = com.example.util.GroceryPriceEstimator.estimateItemCost("Salmon Fillet", "2 lbs", "Meat & Seafood")
        assertTrue(salmonCost >= 10.0)
    }

    @Test
    fun testGroceryBudgetSummaryCalculations() {
        val items = listOf(
            com.example.model.ShoppingItem(id = 1, name = "Avocado", amount = "2", category = "Fresh Produce", isBought = false),
            com.example.model.ShoppingItem(id = 2, name = "Eggs", amount = "1 dozen", category = "Dairy & Eggs", isBought = true),
            com.example.model.ShoppingItem(id = 3, name = "Chicken Breast", amount = "2 lbs", category = "Meat & Seafood", isBought = false)
        )

        val budget = 50.0
        val summary = com.example.util.GroceryPriceEstimator.calculateBudgetSummary(items, budget)

        assertEquals(50.0, summary.weeklyBudget, 0.01)
        assertTrue(summary.totalEstimatedCost > 0.0)
        assertTrue(summary.purchasedCost > 0.0)
        assertTrue(summary.pendingCost > 0.0)
        assertEquals(com.example.model.BudgetStatus.WITHIN_BUDGET, summary.status)
        assertTrue(summary.categorySpends.isNotEmpty())
        assertTrue(summary.tips.isNotEmpty())
    }

    @Test
    fun testCommaSeparatedIngredientsParsing() {
        val rawInput = "Eggs, Spinach, Cheddar Cheese, Milk, Tomatoes"
        val items = rawInput.split(",", "\n", ";")
            .map { it.trim().trim('•', '-', '*', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '.') }
            .filter { it.isNotBlank() }
            .distinct()

        assertEquals(5, items.size)
        assertEquals("Eggs", items[0])
        assertEquals("Spinach", items[1])
        assertEquals("Cheddar Cheese", items[2])
        assertEquals("Milk", items[3])
        assertEquals("Tomatoes", items[4])
    }

    @Test
    fun testCommaSeparatedWithMixedPunctuationAndWhitespace() {
        val rawInput = "  • Eggs , - Greek Yogurt;\n 2. Spinach,  Cheddar  "
        val items = rawInput.split(",", "\n", ";")
            .map { it.trim().trim('•', '-', '*', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '.').trim() }
            .filter { it.isNotBlank() }
            .distinct()

        assertEquals(4, items.size)
        assertTrue(items.contains("Eggs"))
        assertTrue(items.contains("Greek Yogurt"))
        assertTrue(items.contains("Spinach"))
        assertTrue(items.contains("Cheddar"))
    }

    @Test
    fun testNutritionParserIngredientDetails() {
        val eggDetail = com.example.util.NutritionParser.parseIngredient("Eggs")
        assertTrue(eggDetail.estimatedCalories > 100)
        assertTrue(eggDetail.proteinGrams > 10.0)
        assertTrue(eggDetail.allergensContained.contains(com.example.model.AllergenType.EGGS))

        val cheeseDetail = com.example.util.NutritionParser.parseIngredient("Sharp Cheddar Cheese")
        assertTrue(cheeseDetail.estimatedCalories > 80)
        assertTrue(cheeseDetail.allergensContained.contains(com.example.model.AllergenType.DAIRY))

        val flourDetail = com.example.util.NutritionParser.parseIngredient("All-Purpose Flour")
        assertTrue(flourDetail.carbsGrams > 15.0)
        assertTrue(flourDetail.allergensContained.contains(com.example.model.AllergenType.GLUTEN))
    }

    @Test
    fun testNutritionParserRecipeAllergensAndMacros() {
        val testRecipe = Recipe(
            id = "test-omelette",
            title = "Spinach & Cheddar Omelette",
            description = "Quick breakfast",
            cuisine = "French",
            difficulty = Difficulty.EASY,
            prepTimeMinutes = 5,
            cookTimeMinutes = 8,
            calories = 320,
            proteinGrams = 22,
            carbsGrams = 4,
            fatGrams = 24,
            servings = 1,
            matchedIngredients = listOf("Eggs", "Spinach", "Cheddar Cheese"),
            missingIngredients = listOf("Butter"),
            dietaryTags = listOf("Keto", "High-Protein"),
            steps = emptyList()
        )

        val parsed = com.example.util.NutritionParser.parseRecipe(testRecipe)

        assertEquals("test-omelette", parsed.recipeId)
        assertEquals(320, parsed.caloriesPerServing)
        assertEquals(22.0, parsed.proteinGramsPerServing, 0.01)
        assertEquals(4.0, parsed.carbsGramsPerServing, 0.01)
        assertEquals(24.0, parsed.fatGramsPerServing, 0.01)

        // Allergen validation
        val detectedTypes = parsed.detectedAllergens.map { it.type }
        assertTrue(detectedTypes.contains(com.example.model.AllergenType.EGGS))
        assertTrue(detectedTypes.contains(com.example.model.AllergenType.DAIRY))

        // Allergen safe checks
        assertTrue(parsed.allergenFreeTags.contains("Gluten-Free"))
        assertTrue(parsed.allergenFreeTags.contains("Nut-Free"))
        assertTrue(parsed.allergenFreeTags.contains("Seafood-Free"))

        // Micronutrients and Badges
        assertTrue(parsed.micronutrientHighlights.isNotEmpty())
        assertTrue(parsed.proteinRatioPercent > 0)
        assertTrue(parsed.fatRatioPercent > 0)
    }

    @Test
    fun testGlutenFreeNutFreeRecipeAnalysis() {
        val veganSalad = Recipe(
            id = "test-salad",
            title = "Avocado & Tomato Chickpea Salad",
            description = "Fresh crisp salad",
            cuisine = "Mediterranean",
            difficulty = Difficulty.EASY,
            prepTimeMinutes = 10,
            cookTimeMinutes = 0,
            calories = 280,
            proteinGrams = 9,
            carbsGrams = 28,
            fatGrams = 15,
            servings = 2,
            matchedIngredients = listOf("Avocado", "Tomatoes", "Chickpeas", "Olive Oil"),
            missingIngredients = listOf("Lemon"),
            dietaryTags = listOf("Vegan", "Gluten-Free"),
            steps = emptyList()
        )

        val parsed = com.example.util.NutritionParser.parseRecipe(veganSalad)

        // Gluten and Dairy should not be detected
        val detectedTypes = parsed.detectedAllergens.map { it.type }
        assertFalse(detectedTypes.contains(com.example.model.AllergenType.GLUTEN))
        assertFalse(detectedTypes.contains(com.example.model.AllergenType.DAIRY))
        assertFalse(detectedTypes.contains(com.example.model.AllergenType.EGGS))

        // Nightshades detected from tomatoes
        assertTrue(detectedTypes.contains(com.example.model.AllergenType.NIGHTSHADES))

        // Allergen safe tags
        assertTrue(parsed.allergenFreeTags.contains("Gluten-Free"))
        assertTrue(parsed.allergenFreeTags.contains("Dairy-Free"))
        assertTrue(parsed.allergenFreeTags.contains("Nut-Free"))
        assertTrue(parsed.allergenFreeTags.contains("Egg-Free"))
        assertTrue(parsed.allergenFreeTags.contains("Soy-Free"))
    }
}
