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
            totalTimeMinutes = 25,
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
        assertTrue(result.containsKey("Butter"))
        assertTrue(result.containsKey("Eggs") || result.containsKey("Milk"))
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
}
