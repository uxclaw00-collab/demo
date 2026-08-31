package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.CulinaryRepository
import com.example.model.CookingStep
import com.example.model.DayInfo
import com.example.model.DetectedIngredient
import com.example.model.DietaryFilter
import com.example.model.GroceryBudgetSummary
import com.example.model.MealLog
import com.example.model.NutritionGoals
import com.example.model.PantryItem
import com.example.model.PlannedMeal
import com.example.model.Recipe
import com.example.model.ShoppingItem
import com.example.util.GroceryPriceEstimator
import com.example.util.TtsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppScreen {
    FRIDGE_RECIPES,
    WEEKLY_PLANNER,
    PANTRY_INVENTORY,
    NUTRITION_LOG,
    SHOPPING_LIST,
    SAVED_RECIPES, // Dedicated Favorites
    COOKING_MODE
}

class CulinaryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CulinaryRepository
    val ttsManager: TtsManager = TtsManager(application)

    init {
        val db = AppDatabase.getInstance(application)
        repository = CulinaryRepository(db)
    }

    private val _currentScreen = MutableStateFlow(AppScreen.FRIDGE_RECIPES)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _currentPhotoBitmap = MutableStateFlow<Bitmap?>(null)
    val currentPhotoBitmap: StateFlow<Bitmap?> = _currentPhotoBitmap.asStateFlow()

    private val _selectedPreset = MutableStateFlow("fresh")
    val selectedPreset: StateFlow<String> = _selectedPreset.asStateFlow()

    private val _detectedIngredients = MutableStateFlow<List<DetectedIngredient>>(emptyList())
    val detectedIngredients: StateFlow<List<DetectedIngredient>> = _detectedIngredients.asStateFlow()

    private val _dietaryFilters = MutableStateFlow<Set<DietaryFilter>>(emptySet())
    val dietaryFilters: StateFlow<Set<DietaryFilter>> = _dietaryFilters.asStateFlow()

    private val _suggestedRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val suggestedRecipes: StateFlow<List<Recipe>> = _suggestedRecipes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadingStatus = MutableStateFlow("Analyzing your fridge...")
    val loadingStatus: StateFlow<String> = _loadingStatus.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _isFilterSheetOpen = MutableStateFlow(false)
    val isFilterSheetOpen: StateFlow<Boolean> = _isFilterSheetOpen.asStateFlow()

    // Active Cooking State
    private val _activeCookingRecipe = MutableStateFlow<Recipe?>(null)
    val activeCookingRecipe: StateFlow<Recipe?> = _activeCookingRecipe.asStateFlow()

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex: StateFlow<Int> = _currentStepIndex.asStateFlow()

    private val _stepTimerRemainingSeconds = MutableStateFlow<Int?>(null)
    val stepTimerRemainingSeconds: StateFlow<Int?> = _stepTimerRemainingSeconds.asStateFlow()

    private val _isStepTimerRunning = MutableStateFlow(false)
    val isStepTimerRunning: StateFlow<Boolean> = _isStepTimerRunning.asStateFlow()

    private val _completedSteps = MutableStateFlow<Set<Int>>(emptySet())
    val completedSteps: StateFlow<Set<Int>> = _completedSteps.asStateFlow()

    private val _isRecipeCompleted = MutableStateFlow(false)
    val isRecipeCompleted: StateFlow<Boolean> = _isRecipeCompleted.asStateFlow()

    private var countDownTimer: CountDownTimer? = null

    // Room Database Flows
    val shoppingItems: StateFlow<List<ShoppingItem>> = repository.getShoppingItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Grocery Budget Tracker State
    private val _weeklyGroceryBudget = MutableStateFlow(75.0)
    val weeklyGroceryBudget: StateFlow<Double> = _weeklyGroceryBudget.asStateFlow()

    val groceryBudgetSummary: StateFlow<GroceryBudgetSummary> = combine(
        shoppingItems,
        _weeklyGroceryBudget
    ) { items, budget ->
        GroceryPriceEstimator.calculateBudgetSummary(items, budget)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GroceryPriceEstimator.calculateBudgetSummary(emptyList(), 75.0)
    )

    val savedRecipes: StateFlow<List<Recipe>> = repository.getSavedRecipes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pantryItems: StateFlow<List<PantryItem>> = repository.getPantryItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Nutrition Logging State & Flows
    val allMealLogs: StateFlow<List<MealLog>> = repository.getAllMealLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedNutritionDate = MutableStateFlow(MealLog.getTodayDateString())
    val selectedNutritionDate: StateFlow<String> = _selectedNutritionDate.asStateFlow()

    private val _nutritionGoals = MutableStateFlow(NutritionGoals())
    val nutritionGoals: StateFlow<NutritionGoals> = _nutritionGoals.asStateFlow()

    private val _lastLoggedMeal = MutableStateFlow<MealLog?>(null)
    val lastLoggedMeal: StateFlow<MealLog?> = _lastLoggedMeal.asStateFlow()

    private val _hasAutoLoggedCurrentSession = MutableStateFlow(false)
    val hasAutoLoggedCurrentSession: StateFlow<Boolean> = _hasAutoLoggedCurrentSession.asStateFlow()

    // Weekly Meal Planning State & Flows
    val allPlannedMeals: StateFlow<List<PlannedMeal>> = repository.getAllPlannedMeals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedWeekMonday = MutableStateFlow(PlannedMeal.getMondayOfWeek())
    val selectedWeekMonday: StateFlow<String> = _selectedWeekMonday.asStateFlow()

    // Drag & Drop Runtime State
    private val _isDragging = MutableStateFlow(false)
    val isDragging: StateFlow<Boolean> = _isDragging.asStateFlow()

    private val _draggedRecipe = MutableStateFlow<Recipe?>(null)
    val draggedRecipe: StateFlow<Recipe?> = _draggedRecipe.asStateFlow()

    private val _draggedMeal = MutableStateFlow<PlannedMeal?>(null)
    val draggedMeal: StateFlow<PlannedMeal?> = _draggedMeal.asStateFlow()

    private val _dragHoverDay = MutableStateFlow<String?>(null)
    val dragHoverDay: StateFlow<String?> = _dragHoverDay.asStateFlow()

    private val _dragHoverSlot = MutableStateFlow<String?>(null)
    val dragHoverSlot: StateFlow<String?> = _dragHoverSlot.asStateFlow()

    init {
        // Seed initial pantry staples & load smart fridge preset
        viewModelScope.launch {
            repository.seedInitialPantryIfEmpty()
        }
        loadPreset("fresh")
    }

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value == AppScreen.COOKING_MODE && screen != AppScreen.COOKING_MODE) {
            ttsManager.stop()
        }
        _currentScreen.value = screen
    }

    fun setFilterSheetOpen(isOpen: Boolean) {
        _isFilterSheetOpen.value = isOpen
    }

    fun toggleDietaryFilter(filter: DietaryFilter) {
        val current = _dietaryFilters.value.toMutableSet()
        if (current.contains(filter)) {
            current.remove(filter)
        } else {
            current.add(filter)
        }
        _dietaryFilters.value = current
    }

    fun clearDietaryFilters() {
        _dietaryFilters.value = emptySet()
    }

    fun loadPreset(presetName: String) {
        _selectedPreset.value = presetName
        _currentPhotoBitmap.value = null
        scanFridge(bitmap = null, preset = presetName)
    }

    fun scanFridgeWithCustomPhoto(bitmap: Bitmap) {
        _currentPhotoBitmap.value = bitmap
        _selectedPreset.value = "custom"
        scanFridge(bitmap = bitmap, preset = null)
    }

    fun regenerateRecipesWithActiveFilters() {
        scanFridge(bitmap = _currentPhotoBitmap.value, preset = _selectedPreset.value)
    }

    private fun scanFridge(bitmap: Bitmap?, preset: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _loadingStatus.value = if (bitmap != null) {
                "Analyzing fridge with Gemini AI..."
            } else {
                "Detecting ingredients & crafting recipes..."
            }

            val result = repository.analyzeFridge(
                bitmap = bitmap,
                sampleType = preset,
                filters = _dietaryFilters.value
            )

            _isLoading.value = false
            if (result.isSuccess) {
                val (detected, recipes) = result.getOrThrow()
                _detectedIngredients.value = detected
                _suggestedRecipes.value = recipes
                _snackbarMessage.value = "Detected ${detected.size} items! Generated ${recipes.size} personalized recipes."
            } else {
                _snackbarMessage.value = "Could not connect to AI. Using smart pantry recommendations."
            }
        }
    }

    fun toggleIngredientSelected(name: String) {
        val current = _detectedIngredients.value.map {
            if (it.name.equals(name, ignoreCase = true)) it.copy(isSelected = !it.isSelected) else it
        }
        _detectedIngredients.value = current
    }

    fun addCustomIngredient(name: String, category: String = "Produce") {
        if (name.isBlank()) return
        val current = _detectedIngredients.value.toMutableList()
        current.add(0, DetectedIngredient(name.trim(), category, "Added", true))
        _detectedIngredients.value = current
        _snackbarMessage.value = "Added '$name' to fridge inventory"
    }

    fun removeIngredient(name: String) {
        _detectedIngredients.value = _detectedIngredients.value.filterNot { it.name.equals(name, ignoreCase = true) }
    }

    // --- Step-by-Step Cooking Mode Actions ---

    fun startCooking(recipe: Recipe) {
        _activeCookingRecipe.value = recipe
        _currentStepIndex.value = 0
        _completedSteps.value = emptySet()
        _isRecipeCompleted.value = false
        _hasAutoLoggedCurrentSession.value = false
        _lastLoggedMeal.value = null
        setupStepTimer(recipe.steps.getOrNull(0))
        _currentScreen.value = AppScreen.COOKING_MODE

        // Speak first step welcome
        val firstStep = recipe.steps.getOrNull(0)
        if (firstStep != null) {
            ttsManager.speak("Starting ${recipe.title}. Step 1: ${firstStep.title}. ${firstStep.instruction}")
        }
    }

    fun nextStep() {
        val recipe = _activeCookingRecipe.value ?: return
        if (_currentStepIndex.value < recipe.steps.size - 1) {
            val nextIdx = _currentStepIndex.value + 1
            _currentStepIndex.value = nextIdx
            val step = recipe.steps[nextIdx]
            setupStepTimer(step)
            ttsManager.speak("Step ${nextIdx + 1}: ${step.title}. ${step.instruction}")
        } else {
            // Completed all steps
            _isRecipeCompleted.value = true
            ttsManager.speak("Congratulations! You've finished cooking ${recipe.title}. Bon appétit!")
            
            // Automatically log nutrition for today
            if (!_hasAutoLoggedCurrentSession.value) {
                autoLogCompletedRecipe(recipe)
            }
        }
    }

    fun prevStep() {
        val recipe = _activeCookingRecipe.value ?: return
        if (_currentStepIndex.value > 0) {
            val prevIdx = _currentStepIndex.value - 1
            _currentStepIndex.value = prevIdx
            val step = recipe.steps[prevIdx]
            setupStepTimer(step)
            ttsManager.speak("Step ${prevIdx + 1}: ${step.title}. ${step.instruction}")
        }
    }

    fun goToStep(index: Int) {
        val recipe = _activeCookingRecipe.value ?: return
        if (index in recipe.steps.indices) {
            _currentStepIndex.value = index
            val step = recipe.steps[index]
            setupStepTimer(step)
            ttsManager.speak("Step ${index + 1}: ${step.title}. ${step.instruction}")
        }
    }

    fun speakCurrentStep() {
        val recipe = _activeCookingRecipe.value ?: return
        val step = recipe.steps.getOrNull(_currentStepIndex.value) ?: return
        ttsManager.speak("Step ${step.stepNumber}: ${step.title}. ${step.instruction} ${step.chefTip?.let { "Chef tip: $it" } ?: ""}")
    }

    fun toggleTts() {
        if (ttsManager.isSpeaking.value) {
            ttsManager.stop()
        } else {
            speakCurrentStep()
        }
    }

    fun setSpeechRate(rate: Float) {
        ttsManager.setSpeechRate(rate)
    }

    fun toggleStepCompleted(index: Int) {
        val current = _completedSteps.value.toMutableSet()
        if (current.contains(index)) {
            current.remove(index)
        } else {
            current.add(index)
        }
        _completedSteps.value = current
    }

    private fun setupStepTimer(step: CookingStep?) {
        countDownTimer?.cancel()
        _isStepTimerRunning.value = false
        _stepTimerRemainingSeconds.value = step?.timerSeconds
    }

    fun startStepTimer() {
        val seconds = _stepTimerRemainingSeconds.value ?: return
        if (seconds <= 0) return

        countDownTimer?.cancel()
        _isStepTimerRunning.value = true

        countDownTimer = object : CountDownTimer(seconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                _stepTimerRemainingSeconds.value = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {
                _stepTimerRemainingSeconds.value = 0
                _isStepTimerRunning.value = false
                ttsManager.speak("Timer finished for Step ${_currentStepIndex.value + 1}!")
                _snackbarMessage.value = "⏰ Timer completed for step ${_currentStepIndex.value + 1}!"
            }
        }.start()
    }

    fun pauseStepTimer() {
        countDownTimer?.cancel()
        _isStepTimerRunning.value = false
    }

    fun resetStepTimer() {
        val recipe = _activeCookingRecipe.value ?: return
        val step = recipe.steps.getOrNull(_currentStepIndex.value)
        setupStepTimer(step)
    }

    fun closeCookingMode() {
        ttsManager.stop()
        countDownTimer?.cancel()
        _isStepTimerRunning.value = false
        _currentScreen.value = AppScreen.FRIDGE_RECIPES
    }

    // --- Pantry Inventory Operations ---

    fun addPantryItem(name: String, quantity: String, category: String, expirationDateMillis: Long?) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addPantryItem(name, quantity, category, expirationDateMillis)
            _snackbarMessage.value = "Added '$name' to your Pantry Inventory"
        }
    }

    fun updatePantryItem(item: PantryItem) {
        viewModelScope.launch {
            repository.updatePantryItem(item)
            _snackbarMessage.value = "Updated '${item.name}'"
        }
    }

    fun deletePantryItem(item: PantryItem) {
        viewModelScope.launch {
            repository.deletePantryItem(item)
            _snackbarMessage.value = "Removed '${item.name}' from Pantry"
        }
    }

    // --- Shopping List Operations ---

    fun addMissingIngredients(recipe: Recipe) {
        viewModelScope.launch {
            // Cross-reference with pantry items before adding
            val currentPantry = pantryItems.value
            val missingNotYetInPantry = recipe.missingIngredients.filterNot { missing ->
                currentPantry.any { p -> p.name.contains(missing, ignoreCase = true) || missing.contains(p.name, ignoreCase = true) }
            }

            if (missingNotYetInPantry.isEmpty() && recipe.missingIngredients.isNotEmpty()) {
                _snackbarMessage.value = "All missing ingredients are already in your Pantry Inventory!"
            } else {
                repository.addMissingIngredientsFromRecipe(recipe)
                _snackbarMessage.value = "Added ${recipe.missingIngredients.size} items to your Shopping List!"
            }
        }
    }

    fun addSingleMissingItem(name: String, recipeTitle: String) {
        viewModelScope.launch {
            repository.addShoppingItem(name = name, recipeSource = recipeTitle)
            _snackbarMessage.value = "Added '$name' to Shopping List"
        }
    }

    fun addCustomShoppingItem(name: String, amount: String, category: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addShoppingItem(name, amount, category)
            _snackbarMessage.value = "Added '$name' to Shopping List"
        }
    }

    fun addSubstituteToShoppingList(substituteName: String, originalName: String) {
        viewModelScope.launch {
            repository.addShoppingItem(
                name = substituteName,
                amount = "1 item (swap for $originalName)",
                category = "Pantry",
                recipeSource = "Ingredient Swap"
            )
            _snackbarMessage.value = "Added substitute '$substituteName' to Shopping List"
        }
    }

    fun toggleShoppingItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.toggleShoppingItemBought(item)
        }
    }

    fun deleteShoppingItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.deleteShoppingItem(item)
        }
    }

    fun clearBoughtItems() {
        viewModelScope.launch {
            repository.clearBoughtShoppingItems()
            _snackbarMessage.value = "Cleared purchased items"
        }
    }

    fun clearAllShoppingItems() {
        viewModelScope.launch {
            repository.clearAllShoppingItems()
            _snackbarMessage.value = "Shopping list cleared"
        }
    }

    fun setWeeklyBudget(amount: Double) {
        val sanitized = (amount * 100.0).let { Math.round(it) / 100.0 }.coerceIn(5.0, 1000.0)
        _weeklyGroceryBudget.value = sanitized
        _snackbarMessage.value = "Weekly budget updated to $${"%.2f".format(Locale.US, sanitized)}"
    }

    // --- Saved / Favorite Recipes Operations ---

    fun toggleSaveRecipe(recipe: Recipe) {
        viewModelScope.launch {
            repository.toggleSaveRecipe(recipe)
            val isNowSaved = repository.isRecipeSaved(recipe.id)
            _snackbarMessage.value = if (isNowSaved) "❤️ Added '${recipe.title}' to Favorites" else "Removed '${recipe.title}' from Favorites"

            // Update in suggested list as well
            _suggestedRecipes.value = _suggestedRecipes.value.map {
                if (it.id == recipe.id) it.copy(isSaved = isNowSaved) else it
            }
        }
    }

    // --- Nutrition Logging & Intake Tracking Operations ---

    fun autoLogCompletedRecipe(recipe: Recipe, servings: Float = 1.0f) {
        viewModelScope.launch {
            val meal = repository.logCookedRecipeMeal(
                recipe = recipe,
                servings = servings,
                mealType = MealLog.determineMealType(),
                dateString = MealLog.getTodayDateString()
            )
            _lastLoggedMeal.value = meal
            _hasAutoLoggedCurrentSession.value = true
            _snackbarMessage.value = "🔥 Logged ${meal.calories} kcal & ${meal.proteinGrams}g protein for ${recipe.title} to Daily Intake!"
        }
    }

    fun logRecipeMeal(recipe: Recipe, servings: Float = 1.0f, mealType: String = MealLog.determineMealType()) {
        viewModelScope.launch {
            val meal = repository.logCookedRecipeMeal(
                recipe = recipe,
                servings = servings,
                mealType = mealType,
                dateString = _selectedNutritionDate.value
            )
            _snackbarMessage.value = "Added '${recipe.title}' (${meal.calories} kcal) to daily log"
        }
    }

    fun logCustomMeal(
        title: String,
        calories: Int,
        proteinGrams: Int,
        carbsGrams: Int,
        fatGrams: Int,
        servings: Float = 1.0f,
        mealType: String = MealLog.determineMealType()
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val meal = repository.logCustomMeal(
                title = title,
                calories = calories,
                proteinGrams = proteinGrams,
                carbsGrams = carbsGrams,
                fatGrams = fatGrams,
                servings = servings,
                mealType = mealType,
                dateString = _selectedNutritionDate.value
            )
            _snackbarMessage.value = "Added '${meal.recipeTitle}' (${meal.calories} kcal) to daily log"
        }
    }

    fun deleteMealLog(mealLog: MealLog) {
        viewModelScope.launch {
            repository.deleteMealLog(mealLog)
            _snackbarMessage.value = "Removed '${mealLog.recipeTitle}' from nutrition log"
        }
    }

    fun updateNutritionGoals(calories: Int, protein: Int, carbs: Int, fat: Int) {
        _nutritionGoals.value = NutritionGoals(
            targetCalories = calories.coerceAtLeast(500),
            targetProteinGrams = protein.coerceAtLeast(10),
            targetCarbsGrams = carbs.coerceAtLeast(10),
            targetFatGrams = fat.coerceAtLeast(5)
        )
        _snackbarMessage.value = "Updated daily nutrition goals"
    }

    fun setNutritionDate(dateString: String) {
        _selectedNutritionDate.value = dateString
    }

    fun resetNutritionDateToToday() {
        _selectedNutritionDate.value = MealLog.getTodayDateString()
    }

    fun previousNutritionDay() {
        _selectedNutritionDate.value = shiftDateByDays(_selectedNutritionDate.value, -1)
    }

    fun nextNutritionDay() {
        _selectedNutritionDate.value = shiftDateByDays(_selectedNutritionDate.value, 1)
    }

    private fun shiftDateByDays(dateString: String, days: Int): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateString) ?: Date()
            val cal = Calendar.getInstance().apply {
                time = date
                add(Calendar.DAY_OF_YEAR, days)
            }
            sdf.format(cal.time)
        } catch (e: Exception) {
            MealLog.getTodayDateString()
        }
    }

    fun dismissSnackbar() {
        _snackbarMessage.value = null
    }

    // --- Weekly Meal Planner & Drag-and-Drop Operations ---

    fun startDragRecipe(recipe: Recipe) {
        _draggedRecipe.value = recipe
        _draggedMeal.value = null
        _isDragging.value = true
    }

    fun startDragMeal(meal: PlannedMeal) {
        _draggedMeal.value = meal
        _draggedRecipe.value = null
        _isDragging.value = true
    }

    fun updateDragHover(day: String?, slot: String?) {
        _dragHoverDay.value = day
        _dragHoverSlot.value = slot
    }

    fun cancelDrag() {
        _isDragging.value = false
        _draggedRecipe.value = null
        _draggedMeal.value = null
        _dragHoverDay.value = null
        _dragHoverSlot.value = null
    }

    fun dropOnDaySlot(dayName: String, dateString: String, slot: String) {
        val recipe = _draggedRecipe.value
        val existingMeal = _draggedMeal.value

        if (recipe != null) {
            planRecipe(recipe, dayName, dateString, slot)
        } else if (existingMeal != null) {
            moveMeal(existingMeal, dayName, dateString, slot)
        }
        cancelDrag()
    }

    fun planRecipe(
        recipe: Recipe,
        dayName: String,
        dateString: String,
        slot: String = "Dinner",
        servings: Int = 1
    ) {
        viewModelScope.launch {
            val planned = repository.planRecipeMeal(recipe, dayName, dateString, slot, servings)
            _snackbarMessage.value = "📅 Planned '${recipe.title}' for $dayName $slot"
        }
    }

    fun moveMeal(
        meal: PlannedMeal,
        newDayName: String,
        newDateString: String,
        newSlot: String
    ) {
        viewModelScope.launch {
            repository.movePlannedMeal(meal, newDayName, newDateString, newSlot)
            _snackbarMessage.value = "Moved '${meal.recipeTitle}' to $newDayName $newSlot"
        }
    }

    fun deletePlannedMeal(meal: PlannedMeal) {
        viewModelScope.launch {
            repository.deletePlannedMeal(meal)
            _snackbarMessage.value = "Removed '${meal.recipeTitle}' from meal plan"
        }
    }

    fun togglePlannedMealCooked(meal: PlannedMeal) {
        viewModelScope.launch {
            val nowCooked = repository.toggleMealCooked(meal)
            if (nowCooked) {
                _snackbarMessage.value = "🎉 Marked '${meal.recipeTitle}' as Cooked & logged to Daily Intake!"
            } else {
                _snackbarMessage.value = "Marked '${meal.recipeTitle}' as Uncooked"
            }
        }
    }

    fun updatePlannedMealServings(meal: PlannedMeal, newServings: Int) {
        viewModelScope.launch {
            repository.updatePlannedMealServings(meal, newServings)
        }
    }

    fun startCookingPlannedMeal(meal: PlannedMeal) {
        viewModelScope.launch {
            // Find recipe from saved recipes or suggested recipes, or build fallback
            val savedList = savedRecipes.value
            val matched = savedList.firstOrNull { it.id == meal.recipeId || it.title.equals(meal.recipeTitle, ignoreCase = true) }
                ?: _suggestedRecipes.value.firstOrNull { it.id == meal.recipeId || it.title.equals(meal.recipeTitle, ignoreCase = true) }

            val recipeToCook = matched ?: Recipe(
                id = meal.recipeId,
                title = meal.recipeTitle,
                description = "Planned ${meal.mealSlot} meal for ${meal.dayOfWeek}",
                cuisine = meal.cuisine,
                difficulty = com.example.model.Difficulty.MEDIUM,
                prepTimeMinutes = meal.prepTimeMinutes,
                cookTimeMinutes = meal.cookTimeMinutes,
                calories = meal.calories,
                servings = meal.servings,
                proteinGrams = meal.proteinGrams,
                carbsGrams = meal.carbsGrams,
                fatGrams = meal.fatGrams,
                dietaryTags = listOf("Planned Meal"),
                matchedIngredients = meal.matchedIngredients,
                missingIngredients = meal.missingIngredients,
                steps = listOf(
                    CookingStep(1, "Preparation", "Gather all ingredients for ${meal.recipeTitle} and prep your cooking station.", 180),
                    CookingStep(2, "Cook Main Ingredients", "Heat cooking pan and prepare ingredients according to standard method.", 600),
                    CookingStep(3, "Season & Finish", "Adjust seasoning, simmer to perfection, and serve hot.", 300)
                )
            )

            startCooking(recipeToCook)
        }
    }

    fun generateWeeklyShoppingList(meals: List<PlannedMeal>) {
        viewModelScope.launch {
            val count = repository.generateShoppingListForPlannedMeals(meals)
            if (count > 0) {
                _snackbarMessage.value = "🛒 Added $count missing ingredients to your Shopping Bento!"
            } else {
                _snackbarMessage.value = "All ingredients already in pantry or shopping list!"
            }
        }
    }

    fun autoPlanWeek(weekDays: List<DayInfo>) {
        viewModelScope.launch {
            val allRecipes = (_suggestedRecipes.value + savedRecipes.value).distinctBy { it.id }
            if (allRecipes.isEmpty()) {
                _snackbarMessage.value = "Scan fridge or save recipes first to auto-plan!"
                return@launch
            }
            val count = repository.autoPlanWeek(allRecipes, weekDays)
            _snackbarMessage.value = "✨ Auto-planned $count balanced meals for the week!"
        }
    }

    fun clearCurrentWeek(weekDays: List<DayInfo>) {
        if (weekDays.isEmpty()) return
        viewModelScope.launch {
            val start = weekDays.first().dateString
            val end = weekDays.last().dateString
            repository.clearWeekPlan(start, end)
            _snackbarMessage.value = "Cleared weekly meal plan"
        }
    }

    fun previousWeek() {
        _selectedWeekMonday.value = PlannedMeal.shiftWeek(_selectedWeekMonday.value, -1)
    }

    fun nextWeek() {
        _selectedWeekMonday.value = PlannedMeal.shiftWeek(_selectedWeekMonday.value, 1)
    }

    fun resetToCurrentWeek() {
        _selectedWeekMonday.value = PlannedMeal.getMondayOfWeek()
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
        ttsManager.shutdown()
    }
}
