package com.example.data.api

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.model.CookingStep
import com.example.model.DetectedIngredient
import com.example.model.DietaryFilter
import com.example.model.Difficulty
import com.example.model.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun analyzeFridgeAndGenerateRecipes(
        bitmap: Bitmap?,
        sampleType: String?,
        dietaryFilters: Set<DietaryFilter>
    ): Result<Pair<List<DetectedIngredient>, List<Recipe>>> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasValidKey = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

        if (bitmap != null && hasValidKey) {
            try {
                val result = callGeminiMultimodal(bitmap, dietaryFilters)
                if (result.isSuccess) {
                    return@withContext result
                } else {
                    Log.w("GeminiService", "Gemini multimodal call failed: ${result.exceptionOrNull()?.message}, falling back to curated assistant logic")
                }
            } catch (e: Exception) {
                Log.e("GeminiService", "Exception during Gemini call", e)
            }
        }

        // Curated smart fallback generation based on sample or image detection simulator
        val fallbackData = generateSmartFallback(sampleType, dietaryFilters)
        return@withContext Result.success(fallbackData)
    }

    private fun callGeminiMultimodal(
        bitmap: Bitmap,
        dietaryFilters: Set<DietaryFilter>
    ): Result<Pair<List<DetectedIngredient>, List<Recipe>>> {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-pro-preview:generateContent?key=$apiKey"

        val outputStream = ByteArrayOutputStream()
        // Resize bitmap if too large to save bandwidth while maintaining clarity
        val scaledBitmap = if (bitmap.width > 1024 || bitmap.height > 1024) {
            val ratio = Math.min(1024f / bitmap.width, 1024f / bitmap.height)
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
        } else {
            bitmap
        }
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

        val activeFiltersText = if (dietaryFilters.isEmpty()) {
            "None"
        } else {
            dietaryFilters.joinToString(", ") { it.label }
        }

        val prompt = """
            You are a world-class smart fridge cooking assistant and master chef.
            Analyze this image of an open refrigerator.
            1. Accurately identify all visible ingredients, vegetables, dairy, meats, condiments, and beverages.
            2. Suggest 3 to 5 realistic, delicious recipes tailored to visible ingredients, adhering strictly to the following dietary restrictions: [$activeFiltersText].
            3. Each recipe must include:
               - title, description, cuisine
               - difficulty (EASY, MEDIUM, or HARD)
               - prepTimeMinutes, cookTimeMinutes, calories (per serving), servings, proteinGrams, carbsGrams, fatGrams
               - dietaryTags (list of strings like "Vegetarian", "Keto", "Vegan", "Gluten-Free", "High-Protein", "Low-Carb", "Dairy-Free", "Under 30 Min")
               - matchedIngredients (which detected ingredients are used)
               - missingIngredients (1 to 3 essential pantry items needed that might not be in fridge)
               - steps (detailed step list where each step has: stepNumber, title, instruction written clearly for hands-free text-to-speech reading, optional timerSeconds if the step involves waiting/cooking e.g. 180 for 3 min, ingredientsUsed list, and optional chefTip).

            Return ONLY valid JSON matching this exact structure:
            {
              "detectedIngredients": [
                {"name": "Eggs", "category": "Dairy & Eggs", "freshness": "Fresh"},
                {"name": "Spinach", "category": "Produce", "freshness": "Crisp"}
              ],
              "recipes": [
                {
                  "title": "Spinach & Herb Omelette",
                  "description": "Fluffy organic eggs whisked with fresh spinach, garlic, and cheddar.",
                  "cuisine": "French / American",
                  "difficulty": "EASY",
                  "prepTimeMinutes": 10,
                  "cookTimeMinutes": 10,
                  "calories": 320,
                  "servings": 2,
                  "proteinGrams": 22,
                  "carbsGrams": 4,
                  "fatGrams": 24,
                  "dietaryTags": ["Keto", "Vegetarian", "Gluten-Free", "Under 30 Min", "High-Protein"],
                  "matchedIngredients": ["Eggs", "Spinach", "Cheddar Cheese", "Butter"],
                  "missingIngredients": ["Black Pepper", "Olive Oil"],
                  "steps": [
                    {
                      "stepNumber": 1,
                      "title": "Prep Ingredients",
                      "instruction": "Chop the fresh spinach finely and crack 4 eggs into a medium mixing bowl.",
                      "timerSeconds": null,
                      "ingredientsUsed": ["Eggs", "Spinach"],
                      "chefTip": "Whisk eggs vigorously for 30 seconds to incorporate air for maximum fluffiness."
                    },
                    {
                      "stepNumber": 2,
                      "title": "Sauté Spinach",
                      "instruction": "Melt 1 tablespoon of butter in a non-stick skillet over medium heat. Sauté the chopped spinach until wilted, about 2 minutes.",
                      "timerSeconds": 120,
                      "ingredientsUsed": ["Butter", "Spinach"],
                      "chefTip": "Don't overcook the spinach; take it off the flame once it turns bright emerald green."
                    },
                    {
                      "stepNumber": 3,
                      "title": "Cook & Fold",
                      "instruction": "Pour the whisked eggs over the spinach. Cook undisturbed for 3 minutes until the edges set, then sprinkle cheddar cheese and fold in half.",
                      "timerSeconds": 180,
                      "ingredientsUsed": ["Eggs", "Cheddar Cheese"],
                      "chefTip": "Gently lift the edges with a spatula to let raw egg flow underneath."
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray()
            val contentObj = JSONObject().apply {
                val partsArray = JSONArray()
                partsArray.put(JSONObject().apply {
                    put("text", prompt)
                })
                partsArray.put(JSONObject().apply {
                    put("inlineData", JSONObject().apply {
                        put("mimeType", "image/jpeg")
                        put("data", base64Image)
                    })
                })
                put("parts", partsArray)
            }
            contentsArray.put(contentObj)
            put("contents", contentsArray)

            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.4)
            })
        }

        val requestBody = requestJson.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            val errBody = response.body?.string()
            return Result.failure(Exception("Gemini API error ${response.code}: $errBody"))
        }

        val responseBodyString = response.body?.string() ?: return Result.failure(Exception("Empty response body from Gemini"))
        return parseGeminiResponseJson(responseBodyString)
    }

    private fun parseGeminiResponseJson(rawResponse: String): Result<Pair<List<DetectedIngredient>, List<Recipe>>> {
        try {
            val root = JSONObject(rawResponse)
            val candidates = root.optJSONArray("candidates") ?: return Result.failure(Exception("No candidates returned"))
            if (candidates.length() == 0) return Result.failure(Exception("Zero candidates in Gemini response"))
            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content") ?: return Result.failure(Exception("Missing content"))
            val parts = content.optJSONArray("parts") ?: return Result.failure(Exception("Missing parts"))
            if (parts.length() == 0) return Result.failure(Exception("Empty parts array"))

            var textContent = parts.getJSONObject(0).optString("text", "")
            // Clean up any markdown code fences if model enclosed them
            if (textContent.startsWith("```json")) {
                textContent = textContent.removePrefix("```json").removeSuffix("```").trim()
            } else if (textContent.startsWith("```")) {
                textContent = textContent.removePrefix("```").removeSuffix("```").trim()
            }

            val parsedJson = JSONObject(textContent)
            val detectedIngredients = mutableListOf<DetectedIngredient>()
            val detectedArray = parsedJson.optJSONArray("detectedIngredients")
            if (detectedArray != null) {
                for (i in 0 until detectedArray.length()) {
                    val item = detectedArray.getJSONObject(i)
                    detectedIngredients.add(
                        DetectedIngredient(
                            name = item.optString("name", "Ingredient"),
                            category = item.optString("category", "Produce"),
                            freshness = item.optString("freshness", "Fresh"),
                            isSelected = true
                        )
                    )
                }
            }

            val recipes = mutableListOf<Recipe>()
            val recipesArray = parsedJson.optJSONArray("recipes")
            if (recipesArray != null) {
                for (i in 0 until recipesArray.length()) {
                    val rObj = recipesArray.getJSONObject(i)
                    val diffStr = rObj.optString("difficulty", "MEDIUM").uppercase()
                    val difficulty = try { Difficulty.valueOf(diffStr) } catch (_: Exception) { Difficulty.MEDIUM }

                    val dietaryTags = mutableListOf<String>()
                    val tagsArr = rObj.optJSONArray("dietaryTags")
                    if (tagsArr != null) {
                        for (k in 0 until tagsArr.length()) dietaryTags.add(tagsArr.getString(k))
                    }

                    val matched = mutableListOf<String>()
                    val matchedArr = rObj.optJSONArray("matchedIngredients")
                    if (matchedArr != null) {
                        for (k in 0 until matchedArr.length()) matched.add(matchedArr.getString(k))
                    }

                    val missing = mutableListOf<String>()
                    val missingArr = rObj.optJSONArray("missingIngredients")
                    if (missingArr != null) {
                        for (k in 0 until missingArr.length()) missing.add(missingArr.getString(k))
                    }

                    val steps = mutableListOf<CookingStep>()
                    val stepsArr = rObj.optJSONArray("steps")
                    if (stepsArr != null) {
                        for (k in 0 until stepsArr.length()) {
                            val sObj = stepsArr.getJSONObject(k)
                            val ingsUsed = mutableListOf<String>()
                            val ingsArr = sObj.optJSONArray("ingredientsUsed")
                            if (ingsArr != null) {
                                for (j in 0 until ingsArr.length()) ingsUsed.add(ingsArr.getString(j))
                            }
                            steps.add(
                                CookingStep(
                                    stepNumber = sObj.optInt("stepNumber", k + 1),
                                    title = sObj.optString("title", "Step ${k + 1}"),
                                    instruction = sObj.optString("instruction", ""),
                                    timerSeconds = if (sObj.has("timerSeconds") && !sObj.isNull("timerSeconds")) sObj.getInt("timerSeconds") else null,
                                    ingredientsUsed = ingsUsed,
                                    chefTip = if (sObj.has("chefTip") && !sObj.isNull("chefTip")) sObj.getString("chefTip") else null
                                )
                            )
                        }
                    }

                    val mealTypeStr = rObj.optString("mealType", "").trim()
                    val inferredMealType = if (mealTypeStr.isNotBlank()) {
                        mealTypeStr
                    } else {
                        val titleLower = rObj.optString("title", "").lowercase()
                        when {
                            titleLower.contains("frittata") || titleLower.contains("scramble") || titleLower.contains("omelette") || titleLower.contains("egg") || titleLower.contains("pancake") || titleLower.contains("waffle") || titleLower.contains("oat") -> "Breakfast"
                            titleLower.contains("salad") || titleLower.contains("wrap") || titleLower.contains("sandwich") || titleLower.contains("soup") || titleLower.contains("bowl") || titleLower.contains("melt") -> "Lunch"
                            titleLower.contains("dip") || titleLower.contains("chip") || titleLower.contains("bite") || titleLower.contains("smoothie") || titleLower.contains("snack") || titleLower.contains("boat") -> "Snack & Light"
                            else -> "Dinner"
                        }
                    }

                    recipes.add(
                        Recipe(
                            id = UUID.randomUUID().toString(),
                            title = rObj.optString("title", "Chef Special"),
                            description = rObj.optString("description", ""),
                            cuisine = rObj.optString("cuisine", "Continental"),
                            difficulty = difficulty,
                            prepTimeMinutes = rObj.optInt("prepTimeMinutes", 15),
                            cookTimeMinutes = rObj.optInt("cookTimeMinutes", 20),
                            calories = rObj.optInt("calories", 400),
                            servings = rObj.optInt("servings", 2),
                            proteinGrams = rObj.optInt("proteinGrams", 20),
                            carbsGrams = rObj.optInt("carbsGrams", 30),
                            fatGrams = rObj.optInt("fatGrams", 15),
                            dietaryTags = dietaryTags,
                            matchedIngredients = matched,
                            missingIngredients = missing,
                            steps = steps,
                            isSaved = false,
                            mealType = inferredMealType
                        )
                    )
                }
            }

            return Result.success(Pair(detectedIngredients, recipes))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    fun generateSmartFallback(
        sampleType: String?,
        dietaryFilters: Set<DietaryFilter>
    ): Pair<List<DetectedIngredient>, List<Recipe>> {
        return when (sampleType) {
            "keto" -> getKetoPreset(dietaryFilters)
            "vegan" -> getVeganPreset(dietaryFilters)
            else -> getFreshStandardPreset(dietaryFilters)
        }
    }

    private fun getFreshStandardPreset(dietaryFilters: Set<DietaryFilter>): Pair<List<DetectedIngredient>, List<Recipe>> {
        val ingredients = listOf(
            DetectedIngredient("Organic Eggs (Carton)", "Dairy & Eggs", "Fresh (10 left)"),
            DetectedIngredient("Aged Sharp Cheddar", "Dairy & Eggs", "Block - Sealed"),
            DetectedIngredient("Fresh Baby Spinach", "Produce", "Crisp"),
            DetectedIngredient("Sweet Bell Peppers", "Produce", "Red & Green"),
            DetectedIngredient("Ripe Roma Tomatoes", "Produce", "Firm & Ripe"),
            DetectedIngredient("Whole Milk (Gallon)", "Dairy & Eggs", "Fresh"),
            DetectedIngredient("Salted Creamery Butter", "Dairy & Eggs", "Fresh"),
            DetectedIngredient("Garlic Cloves", "Produce", "Whole Bulbs"),
            DetectedIngredient("Chicken Breast", "Meat & Seafood", "Chilled")
        )

        val allRecipes = listOf(
            Recipe(
                id = "std-1",
                title = "Rustic Spinach & Cheddar Frittata",
                description = "Golden skillet frittata packed with vibrant baby spinach, sautéed peppers, and melted sharp cheddar.",
                cuisine = "Mediterranean",
                difficulty = Difficulty.EASY,
                prepTimeMinutes = 10,
                cookTimeMinutes = 15,
                calories = 340,
                servings = 3,
                proteinGrams = 24,
                carbsGrams = 6,
                fatGrams = 26,
                dietaryTags = listOf("Vegetarian", "Keto", "Gluten-Free", "Low Carb", "Under 30 Min", "High Protein"),
                matchedIngredients = listOf("Organic Eggs (Carton)", "Aged Sharp Cheddar", "Fresh Baby Spinach", "Sweet Bell Peppers", "Salted Creamery Butter"),
                missingIngredients = listOf("Black Pepper", "Smoked Paprika"),
                mealType = "Breakfast",
                steps = listOf(
                    CookingStep(
                        stepNumber = 1,
                        title = "Whisk the Egg Base",
                        instruction = "In a deep bowl, crack 6 eggs and pour in 2 tablespoons of whole milk. Whisk thoroughly until smooth and airy.",
                        timerSeconds = null,
                        ingredientsUsed = listOf("Organic Eggs (Carton)", "Whole Milk (Gallon)"),
                        chefTip = "Whisking vigorously incorporates tiny air bubbles that make the frittata extraordinarily fluffy."
                    ),
                    CookingStep(
                        stepNumber = 2,
                        title = "Sauté Aromatics & Veggies",
                        instruction = "Melt 1 tablespoon butter in an oven-safe skillet over medium-high heat. Add sliced bell peppers and minced garlic, sautéing for 3 minutes until tender.",
                        timerSeconds = 180,
                        ingredientsUsed = listOf("Sweet Bell Peppers", "Garlic Cloves", "Salted Creamery Butter"),
                        chefTip = "Let the peppers get a slight caramelized char for deeper sweetness."
                    ),
                    CookingStep(
                        stepNumber = 3,
                        title = "Wilt the Spinach",
                        instruction = "Toss in the fresh baby spinach and stir gently for 90 seconds until completely wilted and vibrant emerald green.",
                        timerSeconds = 90,
                        ingredientsUsed = listOf("Fresh Baby Spinach"),
                        chefTip = "Spinach releases water as it wilts, so spread it evenly across the pan."
                    ),
                    CookingStep(
                        stepNumber = 4,
                        title = "Pour & Set",
                        instruction = "Pour the whisked egg mixture evenly over the vegetables. Top with grated sharp cheddar. Lower heat to medium-low and cook for 6 minutes until the edges are set.",
                        timerSeconds = 360,
                        ingredientsUsed = listOf("Aged Sharp Cheddar"),
                        chefTip = "Cover with a lid or finish under the oven broiler for 2 minutes for a golden cheesy crust."
                    ),
                    CookingStep(
                        stepNumber = 5,
                        title = "Rest & Serve",
                        instruction = "Slide the frittata onto a wooden cutting board, let rest for 3 minutes, slice into wedges and serve warm.",
                        timerSeconds = 180,
                        ingredientsUsed = emptyList(),
                        chefTip = "Pair with sliced tomatoes on the side."
                    )
                )
            ),
            Recipe(
                id = "std-2",
                title = "Garlic Butter Pan-Seared Chicken & Peppers",
                description = "Juicy golden chicken breast medallions sautéed with sweet bell peppers in a rich garlic herb butter sauce.",
                cuisine = "American",
                difficulty = Difficulty.MEDIUM,
                prepTimeMinutes = 12,
                cookTimeMinutes = 18,
                calories = 420,
                servings = 2,
                proteinGrams = 42,
                carbsGrams = 8,
                fatGrams = 24,
                dietaryTags = listOf("Keto", "Gluten-Free", "Low Carb", "High Protein", "Under 30 Min"),
                matchedIngredients = listOf("Chicken Breast", "Sweet Bell Peppers", "Garlic Cloves", "Salted Creamery Butter"),
                missingIngredients = listOf("Italian Herb Seasoning", "Olive Oil", "Fresh Parsley"),
                mealType = "Dinner",
                steps = listOf(
                    CookingStep(
                        stepNumber = 1,
                        title = "Season & Slice Chicken",
                        instruction = "Slice chicken breast horizontally into thin cutlets and season generously with salt and pepper.",
                        timerSeconds = null,
                        ingredientsUsed = listOf("Chicken Breast"),
                        chefTip = "Even cutlet thickness ensures fast, even cooking without drying out."
                    ),
                    CookingStep(
                        stepNumber = 2,
                        title = "Sear the Chicken",
                        instruction = "Heat 1 tablespoon butter in a heavy skillet over high heat. Sear chicken cutlets for 4 minutes per side until golden brown.",
                        timerSeconds = 240,
                        ingredientsUsed = listOf("Chicken Breast", "Salted Creamery Butter"),
                        chefTip = "Do not move the chicken during the first 3 minutes to build a golden crust."
                    ),
                    CookingStep(
                        stepNumber = 3,
                        title = "Garlic Pepper Glaze",
                        instruction = "Reduce heat to medium, toss in sliced bell peppers, crushed garlic, and remaining butter. Sauté for 4 minutes until peppers are tender-crisp.",
                        timerSeconds = 240,
                        ingredientsUsed = listOf("Sweet Bell Peppers", "Garlic Cloves", "Salted Creamery Butter"),
                        chefTip = "Spoon the sizzling melted garlic butter over the chicken cutlets continuously."
                    )
                )
            ),
            Recipe(
                id = "std-3",
                title = "Tuscan Tomato & Spinach Scramble",
                description = "Soft-curd creamy eggs gently scrambled with diced tomatoes, baby spinach, and aged cheddar crumbles.",
                cuisine = "Italian",
                difficulty = Difficulty.EASY,
                prepTimeMinutes = 5,
                cookTimeMinutes = 7,
                calories = 290,
                servings = 1,
                proteinGrams = 20,
                carbsGrams = 5,
                fatGrams = 21,
                dietaryTags = listOf("Vegetarian", "Keto", "Gluten-Free", "Low Carb", "Under 30 Min", "High Protein"),
                matchedIngredients = listOf("Organic Eggs (Carton)", "Ripe Roma Tomatoes", "Fresh Baby Spinach", "Aged Sharp Cheddar", "Salted Creamery Butter"),
                missingIngredients = listOf("Dried Oregano"),
                mealType = "Breakfast",
                steps = listOf(
                    CookingStep(
                        stepNumber = 1,
                        title = "Dice & Sauté Tomatoes",
                        instruction = "Dice roma tomatoes and sauté in 1 teaspoon melted butter for 2 minutes to concentrate sweetness.",
                        timerSeconds = 120,
                        ingredientsUsed = listOf("Ripe Roma Tomatoes", "Salted Creamery Butter"),
                        chefTip = "Removing tomato seeds prevents excess liquid in the scramble."
                    ),
                    CookingStep(
                        stepNumber = 2,
                        title = "Fold Eggs & Spinach",
                        instruction = "Pour in 3 lightly whisked eggs and a handful of baby spinach. Stir slowly in wide sweeps on low heat for 3 minutes.",
                        timerSeconds = 180,
                        ingredientsUsed = listOf("Organic Eggs (Carton)", "Fresh Baby Spinach"),
                        chefTip = "Low heat is the secret to velvety, cloud-like scrambled eggs."
                    ),
                    CookingStep(
                        stepNumber = 3,
                        title = "Melt Cheddar",
                        instruction = "Remove skillet from heat while eggs are still glossy. Fold in sharp cheddar until melted.",
                        timerSeconds = null,
                        ingredientsUsed = listOf("Aged Sharp Cheddar"),
                        chefTip = "Residual skillet heat will melt the cheese without overcooking the eggs."
                    )
                )
            ),
            Recipe(
                id = "std-4",
                title = "Fiesta Chicken Fajita Salad Bowl",
                description = "Crisp bell pepper strips and grilled chicken breast tossed with warm spinach and zesty garlic lime seasoning.",
                cuisine = "Mexican",
                difficulty = Difficulty.EASY,
                prepTimeMinutes = 8,
                cookTimeMinutes = 10,
                calories = 380,
                servings = 2,
                proteinGrams = 38,
                carbsGrams = 9,
                fatGrams = 18,
                dietaryTags = listOf("Keto", "Gluten-Free", "Low Carb", "High Protein", "Under 30 Min"),
                matchedIngredients = listOf("Chicken Breast", "Sweet Bell Peppers", "Fresh Baby Spinach", "Garlic Cloves"),
                missingIngredients = listOf("Lime Juice", "Cumin & Chili Powder", "Olive Oil"),
                mealType = "Lunch",
                steps = listOf(
                    CookingStep(
                        stepNumber = 1,
                        title = "Sauté Fajita Chicken",
                        instruction = "Sear sliced chicken strips with garlic and peppers in a hot skillet for 6 minutes until lightly charred.",
                        timerSeconds = 360,
                        ingredientsUsed = listOf("Chicken Breast", "Sweet Bell Peppers", "Garlic Cloves"),
                        chefTip = "High heat gives authentic fajita smoke notes."
                    ),
                    CookingStep(
                        stepNumber = 2,
                        title = "Assemble Bowl",
                        instruction = "Toss with baby spinach, tomatoes, and lime juice for a fresh lunch bowl.",
                        timerSeconds = null,
                        ingredientsUsed = listOf("Fresh Baby Spinach", "Ripe Roma Tomatoes"),
                        chefTip = "Top with fresh cilantro if available."
                    )
                )
            ),
            Recipe(
                id = "std-5",
                title = "Warm Cheddar Spinach Snack Dip",
                description = "Melted aged cheddar folded with wilted garlic spinach and roasted bell pepper bites for a quick savoury snack.",
                cuisine = "American",
                difficulty = Difficulty.EASY,
                prepTimeMinutes = 5,
                cookTimeMinutes = 6,
                calories = 240,
                servings = 2,
                proteinGrams = 14,
                carbsGrams = 4,
                fatGrams = 18,
                dietaryTags = listOf("Vegetarian", "Keto", "Gluten-Free", "Low Carb", "Under 30 Min"),
                matchedIngredients = listOf("Aged Sharp Cheddar", "Fresh Baby Spinach", "Salted Creamery Butter", "Garlic Cloves"),
                missingIngredients = listOf("Black Pepper"),
                mealType = "Snack & Light",
                steps = listOf(
                    CookingStep(
                        stepNumber = 1,
                        title = "Melt & Fold",
                        instruction = "Melt butter, sauté minced garlic and spinach for 2 minutes, then fold in grated cheddar until bubbly and creamy.",
                        timerSeconds = 120,
                        ingredientsUsed = listOf("Aged Sharp Cheddar", "Fresh Baby Spinach", "Garlic Cloves"),
                        chefTip = "Serve warm with sliced bell peppers."
                    )
                )
            )
        )

        val filtered = filterRecipesByDietary(allRecipes, dietaryFilters)
        return Pair(ingredients, filtered)
    }

    private fun getKetoPreset(dietaryFilters: Set<DietaryFilter>): Pair<List<DetectedIngredient>, List<Recipe>> {
        val ingredients = listOf(
            DetectedIngredient("Fresh Chicken Breasts", "Meat & Seafood", "Chilled"),
            DetectedIngredient("Ripe Hass Avocados", "Produce", "Ready to Eat"),
            DetectedIngredient("Free-Range Eggs", "Dairy & Eggs", "Carton of 12"),
            DetectedIngredient("Heavy Whipping Cream", "Dairy & Eggs", "Full Fat"),
            DetectedIngredient("Fresh Green Zucchini", "Produce", "Crisp"),
            DetectedIngredient("Button Mushrooms", "Produce", "Whole"),
            DetectedIngredient("Thick-Cut Smoked Bacon", "Meat & Seafood", "Cured"),
            DetectedIngredient("Fresh Lemons", "Produce", "Juicy")
        )

        val allRecipes = listOf(
            Recipe(
                id = "keto-1",
                title = "Keto Creamy Bacon & Mushroom Chicken",
                description = "Pan-seared tender chicken breast smothered in a decadent garlic mushroom heavy cream sauce topped with crispy bacon bits.",
                cuisine = "French",
                difficulty = Difficulty.MEDIUM,
                prepTimeMinutes = 15,
                cookTimeMinutes = 20,
                calories = 580,
                servings = 3,
                proteinGrams = 46,
                carbsGrams = 5,
                fatGrams = 42,
                dietaryTags = listOf("Keto", "Gluten-Free", "Low Carb", "High Protein", "Under 30 Min"),
                matchedIngredients = listOf("Fresh Chicken Breasts", "Button Mushrooms", "Thick-Cut Smoked Bacon", "Heavy Whipping Cream"),
                missingIngredients = listOf("Garlic Powder", "Fresh Thyme", "Olive Oil"),
                mealType = "Dinner",
                steps = listOf(
                    CookingStep(
                        stepNumber = 1,
                        title = "Crisp the Bacon",
                        instruction = "Fry chopped thick-cut bacon in a deep skillet over medium heat for 6 minutes until golden and crispy. Transfer bacon to a paper towel, leaving 2 tablespoons of rendered drippings in the pan.",
                        timerSeconds = 360,
                        ingredientsUsed = listOf("Thick-Cut Smoked Bacon"),
                        chefTip = "Bacon fat adds unbeatable savory depth to the mushroom cream sauce."
                    ),
                    CookingStep(
                        stepNumber = 2,
                        title = "Sear Chicken Breasts",
                        instruction = "Sear seasoned chicken breasts in the hot bacon drippings for 5 minutes per side until deeply golden. Set aside.",
                        timerSeconds = 300,
                        ingredientsUsed = listOf("Fresh Chicken Breasts"),
                        chefTip = "Ensure internal temperature reaches 165°F (74°C)."
                    ),
                    CookingStep(
                        stepNumber = 3,
                        title = "Simmer Mushroom Cream",
                        instruction = "Add sliced mushrooms to the skillet and sauté for 4 minutes. Pour in 1 cup heavy whipping cream and a squeeze of fresh lemon juice. Simmer gently for 4 minutes until sauce thickens.",
                        timerSeconds = 240,
                        ingredientsUsed = listOf("Button Mushrooms", "Heavy Whipping Cream", "Fresh Lemons"),
                        chefTip = "Keep the heat medium-low so the heavy cream doesn't scorch."
                    ),
                    CookingStep(
                        stepNumber = 4,
                        title = "Combine & Garnish",
                        instruction = "Return chicken to the skillet, spoon rich cream sauce over the top, and sprinkle crispy bacon crumbles before serving.",
                        timerSeconds = null,
                        ingredientsUsed = listOf("Thick-Cut Smoked Bacon"),
                        chefTip = "Serve immediately while sizzling."
                    )
                )
            ),
            Recipe(
                id = "keto-2",
                title = "Avocado & Crispy Bacon Poached Egg Bowl",
                description = "Warm sliced zucchini ribbons sautéed in olive oil, crowned with fan-sliced avocado, crispy bacon, and soft poached eggs.",
                cuisine = "American",
                difficulty = Difficulty.EASY,
                prepTimeMinutes = 8,
                cookTimeMinutes = 12,
                calories = 490,
                servings = 2,
                proteinGrams = 22,
                carbsGrams = 7,
                fatGrams = 42,
                dietaryTags = listOf("Keto", "Gluten-Free", "Low Carb", "Dairy-Free", "High Protein", "Under 30 Min"),
                matchedIngredients = listOf("Ripe Hass Avocados", "Free-Range Eggs", "Thick-Cut Smoked Bacon", "Fresh Green Zucchini", "Fresh Lemons"),
                missingIngredients = listOf("Sea Salt Flakes", "Red Pepper Flakes"),
                mealType = "Breakfast",
                steps = listOf(
                    CookingStep(
                        stepNumber = 1,
                        title = "Crisp Bacon & Spiralize Zucchini",
                        instruction = "Fry bacon until crisp. Use a peeler to create zucchini ribbons and toss them in the pan for 2 minutes with lemon juice.",
                        timerSeconds = 120,
                        ingredientsUsed = listOf("Thick-Cut Smoked Bacon", "Fresh Green Zucchini", "Fresh Lemons"),
                        chefTip = "Zucchini ribbons only need brief heat to stay crunchy."
                    ),
                    CookingStep(
                        stepNumber = 2,
                        title = "Poach or Fry the Eggs",
                        instruction = "Fry 4 eggs sunny-side up in bacon oil for 3 minutes until whites are set and yolks remain rich and runny.",
                        timerSeconds = 180,
                        ingredientsUsed = listOf("Free-Range Eggs"),
                        chefTip = "Spoon hot pan oil over the whites to cook them evenly."
                    ),
                    CookingStep(
                        stepNumber = 3,
                        title = "Assemble & Garnish",
                        instruction = "Layer zucchini ribbons in bowls, arrange sliced avocado, place eggs on top, and sprinkle with bacon bits and sea salt flakes.",
                        timerSeconds = null,
                        ingredientsUsed = listOf("Ripe Hass Avocados"),
                        chefTip = "Break the egg yolks over the avocado for a luscious natural dressing."
                    )
                )
            ),
            Recipe(
                id = "keto-3",
                title = "Zucchini & Avocado Keto Snack Bites",
                description = "Crispy seared zucchini medallions topped with smashed avocado, bacon crumble, and lemon zest.",
                cuisine = "Mexican",
                difficulty = Difficulty.EASY,
                prepTimeMinutes = 5,
                cookTimeMinutes = 5,
                calories = 210,
                servings = 2,
                proteinGrams = 8,
                carbsGrams = 4,
                fatGrams = 18,
                dietaryTags = listOf("Keto", "Gluten-Free", "Low Carb", "Dairy-Free", "Under 30 Min"),
                matchedIngredients = listOf("Fresh Green Zucchini", "Ripe Hass Avocados", "Thick-Cut Smoked Bacon", "Fresh Lemons"),
                missingIngredients = listOf("Sea Salt"),
                mealType = "Snack & Light",
                steps = listOf(
                    CookingStep(
                        stepNumber = 1,
                        title = "Sear & Top",
                        instruction = "Sear sliced zucchini rounds for 2 minutes per side in bacon drippings. Top with seasoned mashed avocado and bacon.",
                        timerSeconds = 120,
                        ingredientsUsed = listOf("Fresh Green Zucchini", "Ripe Hass Avocados"),
                        chefTip = "Quick bite-sized finger food with zero carbs."
                    )
                )
            )
        )

        val filtered = filterRecipesByDietary(allRecipes, dietaryFilters)
        return Pair(ingredients, filtered)
    }

    private fun getVeganPreset(dietaryFilters: Set<DietaryFilter>): Pair<List<DetectedIngredient>, List<Recipe>> {
        val ingredients = listOf(
            DetectedIngredient("Organic Firm Tofu", "Plant Protein", "Chilled Block"),
            DetectedIngredient("Fresh Broccoli Florets", "Produce", "Crisp Green"),
            DetectedIngredient("Sweet Orange Carrots", "Produce", "Whole"),
            DetectedIngredient("Unsweetened Almond Milk", "Dairy Alternatives", "Chilled"),
            DetectedIngredient("Tuscan Dinosaur Kale", "Produce", "Fresh Bundle"),
            DetectedIngredient("Crimini Mushrooms", "Produce", "Earth Brown"),
            DetectedIngredient("Garlic Cloves & Ginger", "Produce", "Root"),
            DetectedIngredient("Fresh Meyer Lemons", "Produce", "Ripe")
        )

        val allRecipes = listOf(
            Recipe(
                id = "vgn-1",
                title = "Crispy Golden Tofu & Broccoli Ginger Stir-Fry",
                description = "Seared firm tofu cubes tossed with broccoli florets, carrots, and mushrooms in a fragrant garlic ginger glaze.",
                cuisine = "Asian",
                difficulty = Difficulty.MEDIUM,
                prepTimeMinutes = 15,
                cookTimeMinutes = 15,
                calories = 360,
                servings = 2,
                proteinGrams = 26,
                carbsGrams = 24,
                fatGrams = 18,
                dietaryTags = listOf("Vegan", "Vegetarian", "Dairy-Free", "High Protein", "Under 30 Min"),
                matchedIngredients = listOf("Organic Firm Tofu", "Fresh Broccoli Florets", "Sweet Orange Carrots", "Crimini Mushrooms", "Garlic Cloves & Ginger"),
                missingIngredients = listOf("Soy Sauce or Tamari", "Sesame Oil", "Cornstarch"),
                mealType = "Dinner",
                steps = listOf(
                    CookingStep(
                        stepNumber = 1,
                        title = "Press & Cube Tofu",
                        instruction = "Press firm tofu with a paper towel for 5 minutes to remove moisture, then cut into 1-inch cubes.",
                        timerSeconds = 300,
                        ingredientsUsed = listOf("Organic Firm Tofu"),
                        chefTip = "Dry tofu yields an extraordinarily crispy, golden exterior when seared."
                    ),
                    CookingStep(
                        stepNumber = 2,
                        title = "Pan-Sear Tofu to Golden",
                        instruction = "Heat 1 tablespoon oil in a wok or large skillet over high heat. Sear tofu cubes for 6 minutes, flipping until golden on all sides.",
                        timerSeconds = 360,
                        ingredientsUsed = listOf("Organic Firm Tofu"),
                        chefTip = "Leave space between cubes so they fry rather than steam."
                    ),
                    CookingStep(
                        stepNumber = 3,
                        title = "Stir-Fry Crisp Veggies",
                        instruction = "Add broccoli florets, sliced carrots, sliced mushrooms, minced garlic, and ginger. Stir-fry for 4 minutes until vibrant and tender-crisp.",
                        timerSeconds = 240,
                        ingredientsUsed = listOf("Fresh Broccoli Florets", "Sweet Orange Carrots", "Crimini Mushrooms", "Garlic Cloves & Ginger"),
                        chefTip = "Keep the wok moving constantly for even blistering."
                    ),
                    CookingStep(
                        stepNumber = 4,
                        title = "Glaze & Serve",
                        instruction = "Toss tofu back in with 2 tablespoons soy sauce and sesame oil. Cook for 1 minute to coat thoroughly and serve hot.",
                        timerSeconds = 60,
                        ingredientsUsed = emptyList(),
                        chefTip = "Garnish with toasted sesame seeds if on hand."
                    )
                )
            ),
            Recipe(
                id = "vgn-2",
                title = "Creamy Garlic & Lemon Tuscan Kale Soup",
                description = "Silky and comforting soup blended with almond milk, sautéed mushrooms, tender carrots, and wilted Tuscan kale.",
                cuisine = "Italian",
                difficulty = Difficulty.EASY,
                prepTimeMinutes = 10,
                cookTimeMinutes = 18,
                calories = 280,
                servings = 3,
                proteinGrams = 12,
                carbsGrams = 28,
                fatGrams = 14,
                dietaryTags = listOf("Vegan", "Vegetarian", "Dairy-Free", "Gluten-Free", "Under 30 Min"),
                matchedIngredients = listOf("Tuscan Dinosaur Kale", "Crimini Mushrooms", "Sweet Orange Carrots", "Unsweetened Almond Milk", "Garlic Cloves & Ginger", "Fresh Meyer Lemons"),
                missingIngredients = listOf("Vegetable Broth", "Olive Oil", "Cracked Pepper"),
                mealType = "Lunch",
                steps = listOf(
                    CookingStep(
                        stepNumber = 1,
                        title = "Sauté Aromatics",
                        instruction = "Heat olive oil in a soup pot. Add sliced carrots, mushrooms, and minced garlic. Sauté for 4 minutes until fragrant.",
                        timerSeconds = 240,
                        ingredientsUsed = listOf("Sweet Orange Carrots", "Crimini Mushrooms", "Garlic Cloves & Ginger"),
                        chefTip = "Let mushrooms brown slightly before adding liquid."
                    ),
                    CookingStep(
                        stepNumber = 2,
                        title = "Simmer Broth & Almond Milk",
                        instruction = "Pour in vegetable broth and unsweetened almond milk. Bring to a gentle simmer for 8 minutes.",
                        timerSeconds = 480,
                        ingredientsUsed = listOf("Unsweetened Almond Milk"),
                        chefTip = "Unsweetened plain almond milk creates a rich dairy-free velvet texture."
                    ),
                    CookingStep(
                        stepNumber = 3,
                        title = "Add Kale & Lemon",
                        instruction = "Stir in chopped Tuscan kale and fresh lemon juice. Simmer for 3 minutes until kale is tender, season with black pepper, and serve.",
                        timerSeconds = 180,
                        ingredientsUsed = listOf("Tuscan Dinosaur Kale", "Fresh Meyer Lemons"),
                        chefTip = "Lemon juice brightens the earthy kale flavor beautifully."
                    )
                )
            ),
            Recipe(
                id = "vgn-3",
                title = "Savory Tofu & Mushroom Breakfast Scramble",
                description = "Crumbled firm tofu pan-seared with turmeric, garlic, sliced mushrooms, and shredded kale.",
                cuisine = "American",
                difficulty = Difficulty.EASY,
                prepTimeMinutes = 7,
                cookTimeMinutes = 8,
                calories = 290,
                servings = 2,
                proteinGrams = 22,
                carbsGrams = 8,
                fatGrams = 16,
                dietaryTags = listOf("Vegan", "Vegetarian", "Dairy-Free", "Gluten-Free", "High Protein", "Under 30 Min"),
                matchedIngredients = listOf("Organic Firm Tofu", "Crimini Mushrooms", "Tuscan Dinosaur Kale", "Garlic Cloves & Ginger"),
                missingIngredients = listOf("Turmeric", "Nutritional Yeast", "Olive Oil"),
                mealType = "Breakfast",
                steps = listOf(
                    CookingStep(
                        stepNumber = 1,
                        title = "Scramble & Sauté",
                        instruction = "Crumble tofu by hand into a hot skillet with olive oil and turmeric. Sauté with mushrooms and kale for 6 minutes.",
                        timerSeconds = 360,
                        ingredientsUsed = listOf("Organic Firm Tofu", "Crimini Mushrooms", "Tuscan Dinosaur Kale"),
                        chefTip = "Turmeric provides a stunning golden color and anti-inflammatory boost."
                    )
                )
            )
        )

        val filtered = filterRecipesByDietary(allRecipes, dietaryFilters)
        return Pair(ingredients, filtered)
    }

    private fun filterRecipesByDietary(recipes: List<Recipe>, filters: Set<DietaryFilter>): List<Recipe> {
        if (filters.isEmpty()) return recipes

        return recipes.filter { recipe ->
            filters.all { filter ->
                when (filter) {
                    DietaryFilter.VEGETARIAN -> recipe.dietaryTags.any { it.contains("Vegetarian", ignoreCase = true) || it.contains("Vegan", ignoreCase = true) }
                    DietaryFilter.KETO -> recipe.dietaryTags.any { it.contains("Keto", ignoreCase = true) } || recipe.carbsGrams <= 15
                    DietaryFilter.VEGAN -> recipe.dietaryTags.any { it.contains("Vegan", ignoreCase = true) }
                    DietaryFilter.GLUTEN_FREE -> recipe.dietaryTags.any { it.contains("Gluten", ignoreCase = true) }
                    DietaryFilter.LOW_CARB -> recipe.dietaryTags.any { it.contains("Low Carb", ignoreCase = true) || it.contains("Keto", ignoreCase = true) } || recipe.carbsGrams <= 20
                    DietaryFilter.HIGH_PROTEIN -> recipe.dietaryTags.any { it.contains("Protein", ignoreCase = true) } || recipe.proteinGrams >= 25
                    DietaryFilter.DAIRY_FREE -> recipe.dietaryTags.any { it.contains("Dairy-Free", ignoreCase = true) || it.contains("Vegan", ignoreCase = true) }
                    DietaryFilter.QUICK_EASY -> recipe.totalTimeMinutes <= 30
                }
            }
        }.ifEmpty { recipes } // Fallback to all recipes if filters are too strict
    }
}
