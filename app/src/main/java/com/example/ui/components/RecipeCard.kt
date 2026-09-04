package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Difficulty
import com.example.model.PantryItem
import com.example.model.Recipe
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoCardBg
import com.example.ui.theme.BentoGreenPrimary
import com.example.ui.theme.BentoSurfaceVariant
import com.example.ui.theme.BentoTextMuted
import com.example.ui.theme.BentoTextPrimary
import com.example.ui.theme.BentoTextSecondary
import com.example.ui.theme.BentoTileApricot
import com.example.ui.theme.BentoTileSage
import com.example.ui.theme.GoldenHoney
import com.example.ui.theme.MissingTagBg
import com.example.ui.theme.MissingTagText
import com.example.ui.theme.SuccessTagBg
import com.example.ui.theme.SuccessTagText
import com.example.ui.theme.WarmSpice

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeCard(
    recipe: Recipe,
    pantryItems: List<PantryItem> = emptyList(),
    onStartCooking: (Recipe) -> Unit,
    onAddMissingIngredients: (Recipe) -> Unit,
    onToggleSave: (Recipe) -> Unit,
    onAddSubstituteToShoppingList: ((substituteName: String, originalName: String) -> Unit)? = null,
    onScheduleMeal: ((Recipe) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Cross-reference missing ingredients with Pantry inventory
    val inPantryIngredients = recipe.missingIngredients.filter { missing ->
        pantryItems.any { pantry ->
            pantry.name.contains(missing, ignoreCase = true) || missing.contains(pantry.name, ignoreCase = true)
        }
    }
    val trulyMissingIngredients = recipe.missingIngredients.filterNot { inPantryIngredients.contains(it) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("recipe_card_${recipe.id}"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, BentoBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Top Row: Cuisine Pill, Difficulty Badge, Favorite Heart/Star Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cuisine Pill
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = BentoTileSage
                    ) {
                        Text(
                            text = recipe.cuisine.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = BentoGreenPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    // Meal Type Pill
                    if (recipe.mealType.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = BentoTileApricot
                        ) {
                            Text(
                                text = recipe.mealType.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    letterSpacing = 0.8.sp
                                ),
                                color = WarmSpice,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Difficulty Badge
                    DifficultyBadge(difficulty = recipe.difficulty)
                }

                // Favorite Heart/Star Toggle Button (with testTag)
                Surface(
                    shape = CircleShape,
                    color = if (recipe.isSaved) BentoTileApricot else BentoSurfaceVariant,
                    border = BorderStroke(1.dp, if (recipe.isSaved) WarmSpice.copy(alpha = 0.4f) else BentoBorder),
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable { onToggleSave(recipe) }
                        .testTag("favorite_recipe_button_${recipe.id}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (recipe.isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (recipe.isSaved) "Favorite Recipe" else "Add to Favorites",
                            tint = if (recipe.isSaved) WarmSpice else BentoTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title & Description
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = BentoTextPrimary,
                lineHeight = 26.sp
            )

            if (recipe.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recipe.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = BentoTextSecondary,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bento Stats Bar: Total Time, Calories, Servings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BentoSurfaceVariant)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Total Time",
                        tint = BentoGreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${recipe.totalTimeMinutes} min",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                    Text(
                        text = " (${recipe.prepTimeMinutes}p/${recipe.cookTimeMinutes}c)",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = BentoTextMuted
                    )
                }

                // Calories
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Calories",
                        tint = WarmSpice,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${recipe.calories} kcal",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = WarmSpice
                    )
                }

                // Servings
                Text(
                    text = "${recipe.servings} serv",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bento Nutrition & Allergen Parser View
            RecipeNutritionSection(recipe = recipe)

            Spacer(modifier = Modifier.height(10.dp))

            // Dietary Tags Chips
            if (recipe.dietaryTags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    recipe.dietaryTags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BentoTileSage
                        ) {
                            Text(
                                text = tag.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                fontWeight = FontWeight.Bold,
                                color = BentoGreenPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Bento Ingredients Tile: In Fridge, In Pantry (Cross-Referenced), and Truly Missing
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BentoCardBg)
                    .border(BorderStroke(1.dp, BentoBorder), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                // 1. Matched in Fridge
                if (recipe.matchedIngredients.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = BentoGreenPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "In Your Fridge (${recipe.matchedIngredients.size}):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoGreenPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        recipe.matchedIngredients.forEach { ing ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SuccessTagBg
                            ) {
                                Text(
                                    text = ing,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Medium,
                                    color = SuccessTagText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                // 2. Cross-Referenced from Pantry
                if (inPantryIngredients.isNotEmpty()) {
                    if (recipe.matchedIngredients.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = GoldenHoney,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "In Your Pantry (${inPantryIngredients.size}):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GoldenHoney
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        inPantryIngredients.forEach { ing ->
                            val matchedPantryItem = pantryItems.firstOrNull {
                                it.name.contains(ing, ignoreCase = true) || ing.contains(it.name, ignoreCase = true)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BentoSurfaceVariant,
                                border = BorderStroke(1.dp, GoldenHoney.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = ing,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = BentoTextPrimary
                                    )
                                    if (matchedPantryItem != null) {
                                        Text(
                                            text = " (${matchedPantryItem.quantity})",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = BentoTextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Missing items (to buy)
                if (trulyMissingIngredients.isNotEmpty()) {
                    if (recipe.matchedIngredients.isNotEmpty() || inPantryIngredients.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MissingTagText,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Missing (${trulyMissingIngredients.size}):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MissingTagText
                            )
                        }

                        // 1-Click Add Missing to Shopping List Button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MissingTagBg,
                            modifier = Modifier
                                .clickable { onAddMissingIngredients(recipe) }
                                .testTag("add_missing_to_shopping_list_${recipe.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddShoppingCart,
                                    contentDescription = null,
                                    tint = MissingTagText,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+ Add to List",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MissingTagText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        trulyMissingIngredients.forEach { ing ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MissingTagBg
                            ) {
                                Text(
                                    text = ing,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Medium,
                                    color = MissingTagText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ingredient Substitutions & Swaps Section
            IngredientSubstitutionsSection(
                recipe = recipe,
                onAddSubstituteToShoppingList = { substituteName, originalName ->
                    onAddSubstituteToShoppingList?.invoke(substituteName, originalName)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Start Cooking + Schedule to Meal Plan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Prominent Bento "Start Cooking" Button
                Button(
                    onClick = { onStartCooking(recipe) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("start_cooking_button_${recipe.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BentoGreenPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cook (${recipe.steps.size} Steps)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Schedule to Meal Plan Button
                if (onScheduleMeal != null) {
                    Button(
                        onClick = { onScheduleMeal(recipe) },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("schedule_recipe_card_button_${recipe.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoTileSage,
                            contentColor = BentoGreenPrimary
                        ),
                        border = BorderStroke(1.dp, BentoGreenPrimary.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Schedule Recipe",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Schedule",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DifficultyBadge(difficulty: Difficulty) {
    val (color, text) = when (difficulty) {
        Difficulty.EASY -> Pair(BentoGreenPrimary, "Easy")
        Difficulty.MEDIUM -> Pair(GoldenHoney, "Medium")
        Difficulty.HARD -> Pair(WarmSpice, "Master")
    }

    Surface(
        shape = RoundedCornerShape(100.dp),
        color = BentoSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(difficulty.stars) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(11.dp)
                )
            }
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MacroBar(protein: Int, carbs: Int, fat: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MacroPill(label = "Protein", value = "${protein}g", color = BentoGreenPrimary, modifier = Modifier.weight(1f))
        MacroPill(label = "Carbs", value = "${carbs}g", color = GoldenHoney, modifier = Modifier.weight(1f))
        MacroPill(label = "Fat", value = "${fat}g", color = WarmSpice, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MacroPill(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = BentoSurfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    letterSpacing = 0.5.sp
                ),
                fontWeight = FontWeight.Bold,
                color = BentoTextMuted
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
