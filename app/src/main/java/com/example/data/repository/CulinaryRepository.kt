package com.example.data.repository

import android.graphics.Bitmap
import com.example.data.api.GeminiService
import com.example.data.local.AppDatabase
import com.example.data.local.MealLogEntity
import com.example.data.local.PantryItemEntity
import com.example.data.local.PlannedMealEntity
import com.example.data.local.SavedRecipeEntity
import com.example.data.local.ShoppingItemEntity
import com.example.model.DayInfo
import com.example.model.DetectedIngredient
import com.example.model.DietaryFilter
import com.example.model.MealLog
import com.example.model.PantryItem
import com.example.model.PlannedMeal
import com.example.model.Recipe
import com.example.model.ShoppingItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class CulinaryRepository(
    private val database: AppDatabase,
    private val geminiService: GeminiService = GeminiService()
) {
    private val shoppingDao = database.shoppingDao()
    private val savedRecipeDao = database.savedRecipeDao()
    private val pantryDao = database.pantryDao()
    private val mealLogDao = database.mealLogDao()
    private val plannedMealDao = database.plannedMealDao()

    suspend fun analyzeFridge(
        bitmap: Bitmap?,
        sampleType: String?,
        filters: Set<DietaryFilter>
    ): Result<Pair<List<DetectedIngredient>, List<Recipe>>> {
        return geminiService.analyzeFridgeAndGenerateRecipes(bitmap, sampleType, filters)
    }

    // Pantry Inventory Operations
    fun getPantryItems(): Flow<List<PantryItem>> {
        return pantryDao.getAllPantryItems().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun addPantryItem(
        name: String,
        quantity: String = "1",
        category: String = "Pantry Staples",
        expirationDateMillis: Long? = null
    ) {
        pantryDao.insertItem(
            PantryItemEntity(
                name = name.trim(),
                quantity = quantity.trim(),
                category = category,
                expirationDateMillis = expirationDateMillis
            )
        )
    }

    suspend fun updatePantryItem(item: PantryItem) {
        pantryDao.updateItem(PantryItemEntity.fromDomain(item))
    }

    suspend fun deletePantryItem(item: PantryItem) {
        pantryDao.deleteItem(PantryItemEntity.fromDomain(item))
    }

    suspend fun deletePantryItemById(id: Long) {
        pantryDao.deleteById(id)
    }

    suspend fun seedInitialPantryIfEmpty() {
        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L
        val current = pantryDao.getAllPantryItems().first()
        if (current.isEmpty()) {
            val defaults = listOf(
                PantryItemEntity(
                    name = "Extra Virgin Olive Oil",
                    quantity = "500ml bottle",
                    category = "Oils & Vinegars",
                    expirationDateMillis = now + (90 * oneDay)
                ),
                PantryItemEntity(
                    name = "Organic Greek Yogurt",
                    quantity = "1 tub (750g)",
                    category = "Dairy & Refrigerated",
                    expirationDateMillis = now + (2 * oneDay) // Expiring soon!
                ),
                PantryItemEntity(
                    name = "Garlic Powder",
                    quantity = "1 jar (120g)",
                    category = "Spices & Seasonings",
                    expirationDateMillis = now + (180 * oneDay)
                ),
                PantryItemEntity(
                    name = "Soy Sauce / Tamari",
                    quantity = "1 bottle (300ml)",
                    category = "Condiments & Sauces",
                    expirationDateMillis = now + (120 * oneDay)
                ),
                PantryItemEntity(
                    name = "Jasmine Rice",
                    quantity = "2 kg bag",
                    category = "Baking & Grains",
                    expirationDateMillis = now + (300 * oneDay)
                ),
                PantryItemEntity(
                    name = "Fresh Heavy Cream",
                    quantity = "250ml carton",
                    category = "Dairy & Refrigerated",
                    expirationDateMillis = now + (1 * oneDay) // Expiring very soon!
                )
            )
            pantryDao.insertAll(defaults)
        }
    }

    // Shopping List Operations
    fun getShoppingItems(): Flow<List<ShoppingItem>> {
        return shoppingDao.getAllItems().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun addShoppingItem(name: String, amount: String = "1 item", category: String = "Pantry", recipeSource: String? = null) {
        shoppingDao.insertItem(
            ShoppingItemEntity(
                name = name.trim(),
                amount = amount.trim(),
                category = category,
                recipeSource = recipeSource,
                isBought = false
            )
        )
    }

    suspend fun addMissingIngredientsFromRecipe(recipe: Recipe) {
        val entities = recipe.missingIngredients.map { ingredientName ->
            ShoppingItemEntity(
                name = ingredientName,
                amount = "1 package/item",
                category = categorizeIngredient(ingredientName),
                recipeSource = recipe.title,
                isBought = false
            )
        }
        shoppingDao.insertAll(entities)
    }

    suspend fun toggleShoppingItemBought(item: ShoppingItem) {
        shoppingDao.updateItem(
            ShoppingItemEntity.fromDomain(item.copy(isBought = !item.isBought))
        )
    }

    suspend fun deleteShoppingItem(item: ShoppingItem) {
        shoppingDao.deleteItem(ShoppingItemEntity.fromDomain(item))
    }

    suspend fun clearBoughtShoppingItems() {
        shoppingDao.deleteBoughtItems()
    }

    suspend fun clearAllShoppingItems() {
        shoppingDao.clearAll()
    }

    // Saved Recipes Operations
    fun getSavedRecipes(): Flow<List<Recipe>> {
        return savedRecipeDao.getAllSavedRecipes().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun toggleSaveRecipe(recipe: Recipe) {
        val existing = savedRecipeDao.getRecipeById(recipe.id)
        if (existing != null) {
            savedRecipeDao.deleteById(recipe.id)
        } else {
            savedRecipeDao.insertRecipe(SavedRecipeEntity.fromDomain(recipe.copy(isSaved = true)))
        }
    }

    suspend fun isRecipeSaved(id: String): Boolean {
        return savedRecipeDao.getRecipeById(id) != null
    }

    // Nutrition & Meal Log Operations
    fun getAllMealLogs(): Flow<List<MealLog>> {
        return mealLogDao.getAllMealLogs().map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getMealLogsByDate(dateString: String): Flow<List<MealLog>> {
        return mealLogDao.getMealLogsByDate(dateString).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun logCookedRecipeMeal(
        recipe: Recipe,
        servings: Float = 1.0f,
        mealType: String = MealLog.determineMealType(),
        dateString: String = MealLog.getTodayDateString()
    ): MealLog {
        val totalCalories = (recipe.calories * servings).toInt()
        val totalProtein = (recipe.proteinGrams * servings).toInt()
        val totalCarbs = (recipe.carbsGrams * servings).toInt()
        val totalFat = (recipe.fatGrams * servings).toInt()

        val entity = MealLogEntity(
            recipeId = recipe.id,
            recipeTitle = recipe.title,
            servings = servings,
            calories = totalCalories,
            proteinGrams = totalProtein,
            carbsGrams = totalCarbs,
            fatGrams = totalFat,
            mealType = mealType,
            timestampMillis = System.currentTimeMillis(),
            dateString = dateString
        )
        val id = mealLogDao.insertMealLog(entity)
        return entity.copy(id = id).toDomain()
    }

    suspend fun logCustomMeal(
        title: String,
        calories: Int,
        proteinGrams: Int,
        carbsGrams: Int,
        fatGrams: Int,
        servings: Float = 1.0f,
        mealType: String = MealLog.determineMealType(),
        dateString: String = MealLog.getTodayDateString()
    ): MealLog {
        val entity = MealLogEntity(
            recipeId = "custom_${System.currentTimeMillis()}",
            recipeTitle = title.trim(),
            servings = servings,
            calories = (calories * servings).toInt(),
            proteinGrams = (proteinGrams * servings).toInt(),
            carbsGrams = (carbsGrams * servings).toInt(),
            fatGrams = (fatGrams * servings).toInt(),
            mealType = mealType,
            timestampMillis = System.currentTimeMillis(),
            dateString = dateString
        )
        val id = mealLogDao.insertMealLog(entity)
        return entity.copy(id = id).toDomain()
    }

    suspend fun deleteMealLog(mealLog: MealLog) {
        mealLogDao.deleteMealLog(MealLogEntity.fromDomain(mealLog))
    }

    suspend fun deleteMealLogById(id: Long) {
        mealLogDao.deleteById(id)
    }

    // --- Weekly Meal Planning Operations ---

    fun getAllPlannedMeals(): Flow<List<PlannedMeal>> {
        return plannedMealDao.getAllPlannedMeals().map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getPlannedMealsForWeek(startMonday: String, endSunday: String): Flow<List<PlannedMeal>> {
        return plannedMealDao.getPlannedMealsForWeek(startMonday, endSunday).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getPlannedMealsForDate(dateString: String): Flow<List<PlannedMeal>> {
        return plannedMealDao.getPlannedMealsForDate(dateString).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun planRecipeMeal(
        recipe: Recipe,
        dayOfWeek: String,
        dateString: String,
        mealSlot: String = "Dinner",
        servings: Int = 1
    ): PlannedMeal {
        val plannedMeal = PlannedMeal.fromRecipe(
            recipe = recipe,
            dayOfWeek = dayOfWeek,
            dateString = dateString,
            mealSlot = mealSlot,
            servings = servings
        )
        val entity = PlannedMealEntity.fromDomain(plannedMeal)
        val id = plannedMealDao.insertPlannedMeal(entity)
        return plannedMeal.copy(id = id)
    }

    suspend fun movePlannedMeal(
        meal: PlannedMeal,
        newDayOfWeek: String,
        newDateString: String,
        newMealSlot: String
    ) {
        val updated = meal.copy(
            dayOfWeek = newDayOfWeek,
            dateString = newDateString,
            mealSlot = newMealSlot
        )
        plannedMealDao.updatePlannedMeal(PlannedMealEntity.fromDomain(updated))
    }

    suspend fun updatePlannedMealServings(meal: PlannedMeal, newServings: Int) {
        val ratio = newServings.toFloat() / meal.servings.coerceAtLeast(1)
        val updated = meal.copy(
            servings = newServings,
            calories = (meal.calories * ratio).toInt(),
            proteinGrams = (meal.proteinGrams * ratio).toInt(),
            carbsGrams = (meal.carbsGrams * ratio).toInt(),
            fatGrams = (meal.fatGrams * ratio).toInt()
        )
        plannedMealDao.updatePlannedMeal(PlannedMealEntity.fromDomain(updated))
    }

    suspend fun toggleMealCooked(meal: PlannedMeal): Boolean {
        val nextCooked = !meal.isCooked
        val updated = meal.copy(isCooked = nextCooked)
        plannedMealDao.updatePlannedMeal(PlannedMealEntity.fromDomain(updated))

        // If marked cooked, automatically log it to Daily Nutrition Tracker
        if (nextCooked) {
            val entity = MealLogEntity(
                recipeId = meal.recipeId,
                recipeTitle = meal.recipeTitle,
                servings = meal.servings.toFloat(),
                calories = meal.calories,
                proteinGrams = meal.proteinGrams,
                carbsGrams = meal.carbsGrams,
                fatGrams = meal.fatGrams,
                mealType = meal.mealSlot,
                timestampMillis = System.currentTimeMillis(),
                dateString = meal.dateString
            )
            mealLogDao.insertMealLog(entity)
        }
        return nextCooked
    }

    suspend fun deletePlannedMeal(meal: PlannedMeal) {
        plannedMealDao.deletePlannedMeal(PlannedMealEntity.fromDomain(meal))
    }

    suspend fun deletePlannedMealById(id: Long) {
        plannedMealDao.deleteById(id)
    }

    suspend fun clearWeekPlan(startMonday: String, endSunday: String) {
        plannedMealDao.clearWeek(startMonday, endSunday)
    }

    suspend fun generateShoppingListForPlannedMeals(meals: List<PlannedMeal>): Int {
        val currentShopping = shoppingDao.getAllItems().first().map { it.name.lowercase() }
        var addedCount = 0

        for (meal in meals) {
            for (missing in meal.missingIngredients) {
                val trimmed = missing.trim()
                if (trimmed.isNotBlank() && !currentShopping.contains(trimmed.lowercase())) {
                    shoppingDao.insertItem(
                        ShoppingItemEntity(
                            name = trimmed,
                            amount = "For ${meal.recipeTitle} (${meal.dayOfWeek})",
                            category = categorizeIngredient(trimmed),
                            recipeSource = meal.recipeTitle,
                            isBought = false
                        )
                    )
                    addedCount++
                }
            }
        }
        return addedCount
    }

    suspend fun autoPlanWeek(
        recipes: List<Recipe>,
        weekDays: List<DayInfo>
    ): Int {
        if (recipes.isEmpty() || weekDays.isEmpty()) return 0
        var plannedCount = 0
        val slots = listOf("Breakfast", "Lunch", "Dinner")

        for ((dayIndex, day) in weekDays.withIndex()) {
            for ((slotIndex, slot) in slots.withIndex()) {
                val recipeIndex = (dayIndex * 3 + slotIndex) % recipes.size
                val recipe = recipes[recipeIndex]
                val planned = PlannedMeal.fromRecipe(
                    recipe = recipe,
                    dayOfWeek = day.dayName,
                    dateString = day.dateString,
                    mealSlot = slot,
                    servings = 1
                )
                plannedMealDao.insertPlannedMeal(PlannedMealEntity.fromDomain(planned))
                plannedCount++
            }
        }
        return plannedCount
    }

    private fun categorizeIngredient(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("pepper") || lower.contains("paprika") || lower.contains("herb") || lower.contains("salt") || lower.contains("thyme") || lower.contains("oregano") || lower.contains("spice") -> "Spices & Seasonings"
            lower.contains("oil") || lower.contains("vinegar") || lower.contains("broth") || lower.contains("sauce") || lower.contains("tamari") || lower.contains("cornstarch") -> "Pantry & Oils"
            lower.contains("milk") || lower.contains("cheese") || lower.contains("cream") || lower.contains("butter") || lower.contains("yogurt") -> "Dairy & Eggs"
            lower.contains("chicken") || lower.contains("beef") || lower.contains("pork") || lower.contains("bacon") || lower.contains("salmon") || lower.contains("turkey") -> "Meat & Seafood"
            lower.contains("spinach") || lower.contains("tomato") || lower.contains("lemon") || lower.contains("garlic") || lower.contains("onion") || lower.contains("zucchini") || lower.contains("broccoli") || lower.contains("kale") -> "Fresh Produce"
            else -> "Pantry"
        }
    }
}
