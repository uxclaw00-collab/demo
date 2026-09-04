package com.example.util

import com.example.model.AllergenType
import com.example.model.DetectedAllergen
import com.example.model.IngredientNutritionDetail
import com.example.model.MicronutrientHighlight
import com.example.model.NutritionHealthBadge
import com.example.model.ParsedRecipeNutrition
import com.example.model.Recipe
import kotlin.math.roundToInt

object NutritionParser {

    private data class IngredientProfile(
        val aliases: List<String>,
        val calories: Int,
        val protein: Double,
        val carbs: Double,
        val fat: Double,
        val fiber: Double = 0.0,
        val sugar: Double = 0.0,
        val sodiumMg: Int = 0,
        val portion: String,
        val allergens: List<AllergenType> = emptyList(),
        val isMainProtein: Boolean = false,
        val isHealthyFat: Boolean = false,
        val vitaminA_DV: Int = 0,
        val vitaminC_DV: Int = 0,
        val calcium_DV: Int = 0,
        val iron_DV: Int = 0,
        val potassium_DV: Int = 0
    )

    private val ingredientDatabase = listOf(
        // Dairy & Eggs
        IngredientProfile(
            aliases = listOf("egg", "eggs", "large egg", "egg whites", "egg yolk"),
            calories = 143, protein = 12.6, carbs = 0.7, fat = 9.5, fiber = 0.0, sugar = 0.4, sodiumMg = 142,
            portion = "2 large eggs (100g)", allergens = listOf(AllergenType.EGGS), isMainProtein = true,
            calcium_DV = 5, iron_DV = 10, vitaminA_DV = 10
        ),
        IngredientProfile(
            aliases = listOf("cheddar", "cheddar cheese", "aged cheddar", "sharp cheddar", "cheese"),
            calories = 115, protein = 7.0, carbs = 0.4, fat = 9.4, fiber = 0.0, sugar = 0.1, sodiumMg = 180,
            portion = "1 oz (28g)", allergens = listOf(AllergenType.DAIRY), calcium_DV = 20
        ),
        IngredientProfile(
            aliases = listOf("parmesan", "parmesan cheese", "parmigiano", "pecorino"),
            calories = 110, protein = 10.0, carbs = 0.9, fat = 7.0, fiber = 0.0, sugar = 0.0, sodiumMg = 335,
            portion = "1 oz (28g)", allergens = listOf(AllergenType.DAIRY), calcium_DV = 30, iron_DV = 2
        ),
        IngredientProfile(
            aliases = listOf("mozzarella", "fresh mozzarella", "shredded mozzarella"),
            calories = 85, protein = 6.3, carbs = 0.7, fat = 6.3, fiber = 0.0, sugar = 0.3, sodiumMg = 175,
            portion = "1 oz (28g)", allergens = listOf(AllergenType.DAIRY), calcium_DV = 15
        ),
        IngredientProfile(
            aliases = listOf("feta", "feta cheese", "crumbled feta"),
            calories = 75, protein = 4.0, carbs = 1.2, fat = 6.0, fiber = 0.0, sugar = 1.2, sodiumMg = 316,
            portion = "1 oz (28g)", allergens = listOf(AllergenType.DAIRY), calcium_DV = 14
        ),
        IngredientProfile(
            aliases = listOf("greek yogurt", "plain greek yogurt", "yogurt"),
            calories = 100, protein = 17.0, carbs = 6.0, fat = 0.7, fiber = 0.0, sugar = 6.0, sodiumMg = 60,
            portion = "3/4 cup (170g)", allergens = listOf(AllergenType.DAIRY), isMainProtein = true, calcium_DV = 18
        ),
        IngredientProfile(
            aliases = listOf("milk", "whole milk", "2% milk", "skim milk"),
            calories = 120, protein = 8.0, carbs = 12.0, fat = 5.0, fiber = 0.0, sugar = 12.0, sodiumMg = 105,
            portion = "1 cup (240ml)", allergens = listOf(AllergenType.DAIRY), calcium_DV = 25, vitaminA_DV = 10
        ),
        IngredientProfile(
            aliases = listOf("butter", "unsalted butter", "salted butter", "ghee"),
            calories = 102, protein = 0.1, carbs = 0.0, fat = 11.5, fiber = 0.0, sugar = 0.0, sodiumMg = 2,
            portion = "1 tbsp (14g)", allergens = listOf(AllergenType.DAIRY), vitaminA_DV = 7
        ),
        IngredientProfile(
            aliases = listOf("heavy cream", "heavy whipping cream", "sour cream", "cream"),
            calories = 100, protein = 0.8, carbs = 0.8, fat = 10.5, fiber = 0.0, sugar = 0.6, sodiumMg = 10,
            portion = "2 tbsp (30ml)", allergens = listOf(AllergenType.DAIRY), vitaminA_DV = 8
        ),

        // Meats & Seafood
        IngredientProfile(
            aliases = listOf("chicken", "chicken breast", "skinless chicken breast", "chicken thigh", "poultry"),
            calories = 165, protein = 31.0, carbs = 0.0, fat = 3.6, fiber = 0.0, sugar = 0.0, sodiumMg = 74,
            portion = "1 breast (100g cooked)", allergens = emptyList(), isMainProtein = true, iron_DV = 6, potassium_DV = 8
        ),
        IngredientProfile(
            aliases = listOf("ground beef", "lean beef", "beef steak", "steak", "flank steak"),
            calories = 210, protein = 26.0, carbs = 0.0, fat = 11.0, fiber = 0.0, sugar = 0.0, sodiumMg = 65,
            portion = "100g cooked", allergens = emptyList(), isMainProtein = true, iron_DV = 15, potassium_DV = 9
        ),
        IngredientProfile(
            aliases = listOf("bacon", "smoked bacon", "thick-cut bacon", "prosciutto", "pancetta"),
            calories = 120, protein = 8.0, carbs = 0.3, fat = 9.8, fiber = 0.0, sugar = 0.0, sodiumMg = 380,
            portion = "2 slices (25g)", allergens = emptyList(), iron_DV = 3
        ),
        IngredientProfile(
            aliases = listOf("salmon", "wild salmon", "salmon fillet", "smoked salmon"),
            calories = 182, protein = 25.0, carbs = 0.0, fat = 8.0, fiber = 0.0, sugar = 0.0, sodiumMg = 60,
            portion = "100g fillet", allergens = listOf(AllergenType.FISH), isMainProtein = true, isHealthyFat = true,
            potassium_DV = 12, vitaminA_DV = 4
        ),
        IngredientProfile(
            aliases = listOf("tuna", "canned tuna", "albacore tuna", "ahi tuna"),
            calories = 130, protein = 28.0, carbs = 0.0, fat = 1.0, fiber = 0.0, sugar = 0.0, sodiumMg = 180,
            portion = "1 can drained (100g)", allergens = listOf(AllergenType.FISH), isMainProtein = true, iron_DV = 7
        ),
        IngredientProfile(
            aliases = listOf("shrimp", "prawns", "jumbo shrimp"),
            calories = 99, protein = 24.0, carbs = 0.2, fat = 0.3, fiber = 0.0, sugar = 0.0, sodiumMg = 111,
            portion = "100g (approx 8-10 shrimp)", allergens = listOf(AllergenType.SHELLFISH), isMainProtein = true, iron_DV = 14
        ),
        IngredientProfile(
            aliases = listOf("crab", "lobster", "clams", "mussels", "scallops"),
            calories = 110, protein = 20.0, carbs = 2.0, fat = 1.2, fiber = 0.0, sugar = 0.0, sodiumMg = 280,
            portion = "100g cooked", allergens = listOf(AllergenType.SHELLFISH), isMainProtein = true, iron_DV = 18
        ),

        // Plant Proteins & Legumes
        IngredientProfile(
            aliases = listOf("tofu", "firm tofu", "organic firm tofu", "silken tofu"),
            calories = 144, protein = 17.0, carbs = 3.0, fat = 8.0, fiber = 2.0, sugar = 0.5, sodiumMg = 14,
            portion = "1/2 block (125g)", allergens = listOf(AllergenType.SOY), isMainProtein = true, calcium_DV = 25, iron_DV = 15
        ),
        IngredientProfile(
            aliases = listOf("edamame", "soybeans", "tempeh"),
            calories = 121, protein = 12.0, carbs = 9.0, fat = 5.0, fiber = 4.0, sugar = 2.0, sodiumMg = 6,
            portion = "1/2 cup (80g)", allergens = listOf(AllergenType.SOY), isMainProtein = true, iron_DV = 12, vitaminC_DV = 8
        ),
        IngredientProfile(
            aliases = listOf("chickpeas", "garbanzo beans", "black beans", "kidney beans", "lentils"),
            calories = 135, protein = 7.5, carbs = 22.0, fat = 2.0, fiber = 6.5, sugar = 3.0, sodiumMg = 140,
            portion = "1/2 cup (130g)", allergens = emptyList(), isMainProtein = true, iron_DV = 12, potassium_DV = 8
        ),

        // Vegetables & Produce
        IngredientProfile(
            aliases = listOf("spinach", "baby spinach", "fresh spinach", "kale", "greens"),
            calories = 23, protein = 2.9, carbs = 3.6, fat = 0.4, fiber = 2.2, sugar = 0.4, sodiumMg = 24,
            portion = "2 cups raw (60g)", allergens = emptyList(), vitaminA_DV = 56, vitaminC_DV = 28, calcium_DV = 10, iron_DV = 15
        ),
        IngredientProfile(
            aliases = listOf("tomato", "tomatoes", "cherry tomatoes", "roma tomatoes", "diced tomatoes"),
            calories = 22, protein = 1.1, carbs = 4.8, fat = 0.2, fiber = 1.5, sugar = 3.2, sodiumMg = 6,
            portion = "1 medium (120g)", allergens = listOf(AllergenType.NIGHTSHADES), vitaminC_DV = 20, vitaminA_DV = 15, potassium_DV = 6
        ),
        IngredientProfile(
            aliases = listOf("bell pepper", "red bell pepper", "green pepper", "yellow bell pepper", "peppers"),
            calories = 30, protein = 1.0, carbs = 7.0, fat = 0.3, fiber = 2.5, sugar = 4.2, sodiumMg = 4,
            portion = "1 medium (120g)", allergens = listOf(AllergenType.NIGHTSHADES), vitaminC_DV = 150, vitaminA_DV = 30
        ),
        IngredientProfile(
            aliases = listOf("broccoli", "broccoli florets", "cauliflower"),
            calories = 35, protein = 2.8, carbs = 7.0, fat = 0.4, fiber = 3.0, sugar = 1.7, sodiumMg = 33,
            portion = "1 cup chopped (90g)", allergens = emptyList(), vitaminC_DV = 90, vitaminA_DV = 11, calcium_DV = 4
        ),
        IngredientProfile(
            aliases = listOf("avocado", "fresh avocado", "hass avocado"),
            calories = 160, protein = 2.0, carbs = 8.5, fat = 14.7, fiber = 6.7, sugar = 0.7, sodiumMg = 7,
            portion = "1/2 avocado (100g)", allergens = emptyList(), isHealthyFat = true, potassium_DV = 14, vitaminC_DV = 10
        ),
        IngredientProfile(
            aliases = listOf("garlic", "garlic cloves", "minced garlic"),
            calories = 15, protein = 0.6, carbs = 3.3, fat = 0.1, fiber = 0.2, sugar = 0.1, sodiumMg = 2,
            portion = "3 cloves (9g)", allergens = emptyList(), vitaminC_DV = 5
        ),
        IngredientProfile(
            aliases = listOf("onion", "yellow onion", "red onion", "shallots", "scallions", "green onions"),
            calories = 44, protein = 1.2, carbs = 10.0, fat = 0.1, fiber = 1.9, sugar = 4.7, sodiumMg = 4,
            portion = "1 medium (110g)", allergens = emptyList(), vitaminC_DV = 12
        ),
        IngredientProfile(
            aliases = listOf("mushroom", "mushrooms", "cremini mushrooms", "portobello", "shiitake"),
            calories = 22, protein = 3.1, carbs = 3.3, fat = 0.3, fiber = 1.0, sugar = 2.0, sodiumMg = 5,
            portion = "1 cup sliced (70g)", allergens = emptyList(), potassium_DV = 9, iron_DV = 3
        ),
        IngredientProfile(
            aliases = listOf("lemon", "fresh lemon", "lemon juice", "lime", "lime juice"),
            calories = 12, protein = 0.4, carbs = 4.0, fat = 0.1, fiber = 1.4, sugar = 1.1, sodiumMg = 1,
            portion = "1/2 fruit (40g)", allergens = emptyList(), vitaminC_DV = 35
        ),

        // Grains, Flours & Carbs
        IngredientProfile(
            aliases = listOf("rice", "white rice", "jasmine rice", "basmati rice", "brown rice"),
            calories = 160, protein = 3.5, carbs = 35.0, fat = 0.4, fiber = 0.6, sugar = 0.1, sodiumMg = 1,
            portion = "1/2 cup cooked (100g)", allergens = emptyList(), iron_DV = 8
        ),
        IngredientProfile(
            aliases = listOf("pasta", "spaghetti", "penne", "fettuccine", "noodles", "macaroni"),
            calories = 175, protein = 6.0, carbs = 36.0, fat = 0.9, fiber = 2.0, sugar = 0.8, sodiumMg = 2,
            portion = "1 cup cooked (140g)", allergens = listOf(AllergenType.GLUTEN), iron_DV = 10
        ),
        IngredientProfile(
            aliases = listOf("bread", "sourdough bread", "white bread", "whole wheat bread", "baguette", "tortilla"),
            calories = 140, protein = 5.0, carbs = 26.0, fat = 1.5, fiber = 1.5, sugar = 2.0, sodiumMg = 230,
            portion = "2 slices (60g)", allergens = listOf(AllergenType.GLUTEN), iron_DV = 8
        ),
        IngredientProfile(
            aliases = listOf("flour", "all-purpose flour", "wheat flour"),
            calories = 110, protein = 3.0, carbs = 23.0, fat = 0.3, fiber = 1.0, sugar = 0.1, sodiumMg = 1,
            portion = "1/4 cup (30g)", allergens = listOf(AllergenType.GLUTEN), iron_DV = 6
        ),
        IngredientProfile(
            aliases = listOf("oats", "rolled oats", "oatmeal"),
            calories = 150, protein = 5.0, carbs = 27.0, fat = 2.5, fiber = 4.0, sugar = 1.0, sodiumMg = 2,
            portion = "1/2 cup dry (40g)", allergens = emptyList(), iron_DV = 10, potassium_DV = 4
        ),
        IngredientProfile(
            aliases = listOf("quinoa", "cooked quinoa"),
            calories = 111, protein = 4.0, carbs = 20.0, fat = 1.8, fiber = 2.6, sugar = 0.9, sodiumMg = 7,
            portion = "1/2 cup cooked (92g)", allergens = emptyList(), iron_DV = 8, potassium_DV = 6
        ),

        // Oils & Fats
        IngredientProfile(
            aliases = listOf("olive oil", "extra virgin olive oil", "vegetable oil", "canola oil", "avocado oil"),
            calories = 119, protein = 0.0, carbs = 0.0, fat = 13.5, fiber = 0.0, sugar = 0.0, sodiumMg = 0,
            portion = "1 tbsp (15ml)", allergens = emptyList(), isHealthyFat = true, vitaminA_DV = 0
        ),
        IngredientProfile(
            aliases = listOf("sesame oil", "toasted sesame oil"),
            calories = 120, protein = 0.0, carbs = 0.0, fat = 13.6, fiber = 0.0, sugar = 0.0, sodiumMg = 0,
            portion = "1 tbsp (15ml)", allergens = listOf(AllergenType.SESAME), isHealthyFat = true
        ),

        // Nuts & Seeds
        IngredientProfile(
            aliases = listOf("peanuts", "peanut butter"),
            calories = 188, protein = 8.0, carbs = 6.0, fat = 16.0, fiber = 2.0, sugar = 2.0, sodiumMg = 140,
            portion = "2 tbsp (32g)", allergens = listOf(AllergenType.PEANUTS), isHealthyFat = true, isMainProtein = true, iron_DV = 4
        ),
        IngredientProfile(
            aliases = listOf("almonds", "walnuts", "cashews", "pecans", "pine nuts", "pistachios"),
            calories = 165, protein = 6.0, carbs = 6.0, fat = 14.0, fiber = 3.5, sugar = 1.2, sodiumMg = 1,
            portion = "1 oz (28g)", allergens = listOf(AllergenType.TREE_NUTS), isHealthyFat = true, calcium_DV = 7, iron_DV = 6
        ),
        IngredientProfile(
            aliases = listOf("sesame seeds", "tahini"),
            calories = 89, protein = 2.6, carbs = 3.2, fat = 8.0, fiber = 1.4, sugar = 0.1, sodiumMg = 2,
            portion = "1 tbsp (15g)", allergens = listOf(AllergenType.SESAME), calcium_DV = 9, iron_DV = 8
        ),

        // Condiments & Seasonings
        IngredientProfile(
            aliases = listOf("soy sauce", "tamari"),
            calories = 10, protein = 1.3, carbs = 1.0, fat = 0.0, fiber = 0.1, sugar = 0.1, sodiumMg = 879,
            portion = "1 tbsp (15ml)", allergens = listOf(AllergenType.SOY, AllergenType.GLUTEN)
        ),
        IngredientProfile(
            aliases = listOf("wine", "white wine", "red wine", "cooking wine"),
            calories = 40, protein = 0.1, carbs = 1.2, fat = 0.0, fiber = 0.0, sugar = 0.6, sodiumMg = 5,
            portion = "2 tbsp (30ml)", allergens = listOf(AllergenType.SULFITES)
        )
    )

    fun parseIngredient(rawIngredient: String): IngredientNutritionDetail {
        val clean = rawIngredient.lowercase().trim()
        val matchedProfile = ingredientDatabase.firstOrNull { profile ->
            profile.aliases.any { alias ->
                clean.contains(alias) || alias.contains(clean)
            }
        }

        if (matchedProfile != null) {
            return IngredientNutritionDetail(
                ingredientName = rawIngredient.trim(),
                estimatedCalories = matchedProfile.calories,
                proteinGrams = matchedProfile.protein,
                carbsGrams = matchedProfile.carbs,
                fatGrams = matchedProfile.fat,
                fiberGrams = matchedProfile.fiber,
                sugarGrams = matchedProfile.sugar,
                sodiumMg = matchedProfile.sodiumMg,
                portionDescription = matchedProfile.portion,
                allergensContained = matchedProfile.allergens,
                isMainProtein = matchedProfile.isMainProtein,
                isHealthyFat = matchedProfile.isHealthyFat
            )
        }

        // Generic fallback heuristic based on ingredient string cues
        val (cal, prot, carbs, fat, allergens) = when {
            clean.contains("oil") || clean.contains("fat") -> listOf(120.0, 0.0, 0.0, 13.5, emptyList<AllergenType>())
            clean.contains("cheese") || clean.contains("dairy") -> listOf(100.0, 6.0, 1.0, 8.0, listOf(AllergenType.DAIRY))
            clean.contains("meat") || clean.contains("pork") || clean.contains("turkey") -> listOf(180.0, 26.0, 0.0, 8.0, emptyList<AllergenType>())
            clean.contains("nut") -> listOf(160.0, 5.0, 5.0, 14.0, listOf(AllergenType.TREE_NUTS))
            clean.contains("sauce") -> listOf(35.0, 1.0, 5.0, 1.0, emptyList<AllergenType>())
            clean.contains("herb") || clean.contains("spice") || clean.contains("pepper") || clean.contains("salt") -> listOf(5.0, 0.2, 0.8, 0.1, emptyList<AllergenType>())
            else -> listOf(40.0, 1.5, 7.0, 0.5, emptyList<AllergenType>())
        }

        @Suppress("UNCHECKED_CAST")
        val allergenList = allergens as List<AllergenType>

        return IngredientNutritionDetail(
            ingredientName = rawIngredient.trim(),
            estimatedCalories = (cal as Double).toInt(),
            proteinGrams = prot as Double,
            carbsGrams = carbs as Double,
            fatGrams = fat as Double,
            fiberGrams = 1.0,
            sugarGrams = 1.0,
            sodiumMg = 30,
            portionDescription = "1 standard measure",
            allergensContained = allergenList
        )
    }

    fun parseRecipe(recipe: Recipe): ParsedRecipeNutrition {
        val allIngredients = (recipe.matchedIngredients + recipe.missingIngredients).distinct()
        val parsedDetails = allIngredients.map { parseIngredient(it) }

        // Servings
        val servings = recipe.servings.coerceAtLeast(1)

        // Aggregated raw sums across all ingredients in the recipe
        val rawSumCalories = parsedDetails.sumOf { it.estimatedCalories }
        val rawSumProtein = parsedDetails.sumOf { it.proteinGrams }
        val rawSumCarbs = parsedDetails.sumOf { it.carbsGrams }
        val rawSumFat = parsedDetails.sumOf { it.fatGrams }
        val rawSumFiber = parsedDetails.sumOf { it.fiberGrams }
        val rawSumSugar = parsedDetails.sumOf { it.sugarGrams }
        val rawSumSodium = parsedDetails.sumOf { it.sodiumMg }

        // Use Recipe values if present and reasonable, or compute from parsed details
        val calsPerServing = if (recipe.calories > 0) recipe.calories else (rawSumCalories / servings).coerceAtLeast(50)
        val proteinPerServing = if (recipe.proteinGrams > 0) recipe.proteinGrams.toDouble() else (rawSumProtein / servings).coerceAtLeast(2.0)
        val carbsPerServing = if (recipe.carbsGrams > 0) recipe.carbsGrams.toDouble() else (rawSumCarbs / servings).coerceAtLeast(2.0)
        val fatPerServing = if (recipe.fatGrams > 0) recipe.fatGrams.toDouble() else (rawSumFat / servings).coerceAtLeast(1.0)
        val fiberPerServing = (rawSumFiber / servings).coerceAtLeast(0.5)
        val sugarPerServing = (rawSumSugar / servings).coerceAtLeast(0.5)
        val sodiumPerServing = (rawSumSodium / servings).coerceAtLeast(20)
        val netCarbs = (carbsPerServing - fiberPerServing).coerceAtLeast(0.0)

        val totalCalories = calsPerServing * servings

        // Macro Ratio Percentage (4 kcal/g protein, 4 kcal/g carb, 9 kcal/g fat)
        val proteinKcal = proteinPerServing * 4.0
        val carbsKcal = carbsPerServing * 4.0
        val fatKcal = fatPerServing * 9.0
        val totalMacroKcal = (proteinKcal + carbsKcal + fatKcal).coerceAtLeast(1.0)

        val proteinRatio = ((proteinKcal / totalMacroKcal) * 100).roundToInt()
        val carbsRatio = ((carbsKcal / totalMacroKcal) * 100).roundToInt()
        val fatRatio = (100 - proteinRatio - carbsRatio).coerceAtLeast(0)

        // Allergen mapping
        val detectedAllergensMap = mutableMapOf<AllergenType, MutableList<String>>()
        parsedDetails.forEach { detail ->
            detail.allergensContained.forEach { allergen ->
                detectedAllergensMap.getOrPut(allergen) { mutableListOf() }.add(detail.ingredientName)
            }
        }

        // Direct allergen scanning on text for secondary protection
        allIngredients.forEach { ing ->
            val lower = ing.lowercase()
            if ((lower.contains("flour") || lower.contains("wheat") || lower.contains("pasta") || lower.contains("bread") || lower.contains("soy sauce")) &&
                !detectedAllergensMap.containsKey(AllergenType.GLUTEN)) {
                detectedAllergensMap.getOrPut(AllergenType.GLUTEN) { mutableListOf() }.add(ing)
            }
            if ((lower.contains("milk") || lower.contains("cheese") || lower.contains("butter") || lower.contains("cream") || lower.contains("yogurt")) &&
                !detectedAllergensMap.containsKey(AllergenType.DAIRY)) {
                detectedAllergensMap.getOrPut(AllergenType.DAIRY) { mutableListOf() }.add(ing)
            }
            if (lower.contains("egg") && !detectedAllergensMap.containsKey(AllergenType.EGGS)) {
                detectedAllergensMap.getOrPut(AllergenType.EGGS) { mutableListOf() }.add(ing)
            }
            if (lower.contains("peanut") && !detectedAllergensMap.containsKey(AllergenType.PEANUTS)) {
                detectedAllergensMap.getOrPut(AllergenType.PEANUTS) { mutableListOf() }.add(ing)
            }
            if ((lower.contains("almond") || lower.contains("walnut") || lower.contains("cashew") || lower.contains("pecan")) &&
                !detectedAllergensMap.containsKey(AllergenType.TREE_NUTS)) {
                detectedAllergensMap.getOrPut(AllergenType.TREE_NUTS) { mutableListOf() }.add(ing)
            }
            if ((lower.contains("tofu") || lower.contains("edamame") || lower.contains("soy")) &&
                !detectedAllergensMap.containsKey(AllergenType.SOY)) {
                detectedAllergensMap.getOrPut(AllergenType.SOY) { mutableListOf() }.add(ing)
            }
            if ((lower.contains("shrimp") || lower.contains("prawn") || lower.contains("crab") || lower.contains("lobster")) &&
                !detectedAllergensMap.containsKey(AllergenType.SHELLFISH)) {
                detectedAllergensMap.getOrPut(AllergenType.SHELLFISH) { mutableListOf() }.add(ing)
            }
            if ((lower.contains("salmon") || lower.contains("tuna") || lower.contains("fish") || lower.contains("anchovy")) &&
                !detectedAllergensMap.containsKey(AllergenType.FISH)) {
                detectedAllergensMap.getOrPut(AllergenType.FISH) { mutableListOf() }.add(ing)
            }
            if ((lower.contains("sesame") || lower.contains("tahini")) &&
                !detectedAllergensMap.containsKey(AllergenType.SESAME)) {
                detectedAllergensMap.getOrPut(AllergenType.SESAME) { mutableListOf() }.add(ing)
            }
            if ((lower.contains("tomato") || lower.contains("bell pepper") || lower.contains("chili") || lower.contains("eggplant")) &&
                !detectedAllergensMap.containsKey(AllergenType.NIGHTSHADES)) {
                detectedAllergensMap.getOrPut(AllergenType.NIGHTSHADES) { mutableListOf() }.add(ing)
            }
        }

        val detectedAllergensList = detectedAllergensMap.map { (type, triggers) ->
            DetectedAllergen(
                type = type,
                triggerIngredients = triggers.distinct()
            )
        }

        // Allergen Free Tags
        val allergenFreeTags = mutableListOf<String>()
        if (!detectedAllergensMap.containsKey(AllergenType.GLUTEN)) allergenFreeTags.add("Gluten-Free")
        if (!detectedAllergensMap.containsKey(AllergenType.DAIRY)) allergenFreeTags.add("Dairy-Free")
        if (!detectedAllergensMap.containsKey(AllergenType.PEANUTS) && !detectedAllergensMap.containsKey(AllergenType.TREE_NUTS)) allergenFreeTags.add("Nut-Free")
        if (!detectedAllergensMap.containsKey(AllergenType.EGGS)) allergenFreeTags.add("Egg-Free")
        if (!detectedAllergensMap.containsKey(AllergenType.SOY)) allergenFreeTags.add("Soy-Free")
        if (!detectedAllergensMap.containsKey(AllergenType.SHELLFISH) && !detectedAllergensMap.containsKey(AllergenType.FISH)) allergenFreeTags.add("Seafood-Free")

        // Health Badges
        val healthBadges = mutableListOf<NutritionHealthBadge>()
        if (proteinPerServing >= 25.0) {
            healthBadges.add(NutritionHealthBadge("High Protein", "💪", "Over 25g protein per serving to support muscle recovery"))
        }
        if (netCarbs <= 15.0) {
            healthBadges.add(NutritionHealthBadge("Low Carb", "🥑", "Under 15g net carbs per serving"))
        }
        if (calsPerServing <= 350) {
            healthBadges.add(NutritionHealthBadge("Calorie Conscious", "🔥", "Under 350 kcal per serving"))
        }
        if (fiberPerServing >= 4.0) {
            healthBadges.add(NutritionHealthBadge("High Fiber", "🌾", "Promotes satiety and healthy digestion"))
        }
        if (sodiumPerServing <= 300) {
            healthBadges.add(NutritionHealthBadge("Low Sodium", "🧂", "Under 300mg sodium per serving"))
        }
        if (parsedDetails.any { it.isHealthyFat }) {
            healthBadges.add(NutritionHealthBadge("Heart Healthy", "❤️", "Rich in monounsaturated fats and essential fatty acids"))
        }

        // Micronutrient highlights based on ingredients
        val micronutrients = mutableListOf<MicronutrientHighlight>()
        var vitA = 0
        var vitC = 0
        var calcium = 0
        var iron = 0
        var potassium = 0

        parsedDetails.forEach { item ->
            val match = ingredientDatabase.firstOrNull { profile ->
                profile.aliases.any { item.ingredientName.contains(it, ignoreCase = true) }
            }
            if (match != null) {
                vitA += match.vitaminA_DV
                vitC += match.vitaminC_DV
                calcium += match.calcium_DV
                iron += match.iron_DV
                potassium += match.potassium_DV
            }
        }

        val vitAPerServing = (vitA / servings).coerceIn(4, 95)
        val vitCPerServing = (vitC / servings).coerceIn(6, 120)
        val calciumPerServing = (calcium / servings).coerceIn(5, 75)
        val ironPerServing = (iron / servings).coerceIn(4, 60)
        val potassiumPerServing = (potassium / servings).coerceIn(5, 50)

        micronutrients.add(MicronutrientHighlight("Vitamin C", "${(vitCPerServing * 0.9).roundToInt()}mg", vitCPerServing, "Immune function & collagen synthesis", "🍊"))
        micronutrients.add(MicronutrientHighlight("Vitamin A", "${vitAPerServing * 9}µg", vitAPerServing, "Vision, cellular repair & immunity", "🥕"))
        micronutrients.add(MicronutrientHighlight("Calcium", "${calciumPerServing * 13}mg", calciumPerServing, "Bone density & muscle contraction", "🥛"))
        micronutrients.add(MicronutrientHighlight("Iron", String.format("%.1fmg", ironPerServing * 0.18), ironPerServing, "Oxygen transport & energy metabolism", "🥩"))
        micronutrients.add(MicronutrientHighlight("Potassium", "${potassiumPerServing * 47}mg", potassiumPerServing, "Electrolyte balance & cardiovascular health", "⚡"))

        return ParsedRecipeNutrition(
            recipeId = recipe.id,
            recipeTitle = recipe.title,
            servings = servings,
            caloriesPerServing = calsPerServing,
            totalCalories = totalCalories,
            proteinGramsPerServing = proteinPerServing,
            carbsGramsPerServing = carbsPerServing,
            fatGramsPerServing = fatPerServing,
            fiberGramsPerServing = fiberPerServing,
            sugarGramsPerServing = sugarPerServing,
            sodiumMgPerServing = sodiumPerServing,
            netCarbsGramsPerServing = netCarbs,
            proteinRatioPercent = proteinRatio,
            carbsRatioPercent = carbsRatio,
            fatRatioPercent = fatRatio,
            detectedAllergens = detectedAllergensList,
            allergenFreeTags = allergenFreeTags,
            ingredientDetails = parsedDetails,
            healthBadges = healthBadges,
            micronutrientHighlights = micronutrients
        )
    }
}
