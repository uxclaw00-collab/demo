package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CuisineFilter
import com.example.model.DietaryFilter
import com.example.model.MealType
import com.example.model.PrepTimeFilter
import com.example.model.Recipe
import com.example.ui.AppScreen
import com.example.ui.CulinaryViewModel
import com.example.ui.components.FridgeScannerBar
import com.example.ui.components.IngredientsShelf
import com.example.ui.components.RecipeCard
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoCardBg
import com.example.ui.theme.BentoGreenPrimary
import com.example.ui.theme.BentoSurfaceVariant
import com.example.ui.theme.BentoTextMuted
import com.example.ui.theme.BentoTextPrimary
import com.example.ui.theme.BentoTextSecondary
import com.example.ui.theme.BentoTileApricot
import com.example.ui.theme.BentoTileSage
import com.example.ui.theme.FreshSage
import com.example.ui.theme.GoldenHoney
import com.example.ui.theme.WarmSpice

@Composable
fun FridgeRecipesScreen(
    viewModel: CulinaryViewModel,
    modifier: Modifier = Modifier
) {
    val selectedPreset by viewModel.selectedPreset.collectAsState()
    val customBitmap by viewModel.currentPhotoBitmap.collectAsState()
    val detectedIngredients by viewModel.detectedIngredients.collectAsState()
    val suggestedRecipes by viewModel.suggestedRecipes.collectAsState()
    val displayedSuggestedRecipes by viewModel.displayedSuggestedRecipes.collectAsState()
    val dietaryFilters by viewModel.dietaryFilters.collectAsState()
    val selectedMealType by viewModel.selectedMealType.collectAsState()
    val selectedPrepTime by viewModel.selectedPrepTime.collectAsState()
    val selectedCuisine by viewModel.selectedCuisine.collectAsState()
    val activeFilterCount by viewModel.activeFilterCount.collectAsState()
    val shoppingItems by viewModel.shoppingItems.collectAsState()
    val pantryItems by viewModel.pantryItems.collectAsState()
    val savedRecipes by viewModel.savedRecipes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val loadingStatus by viewModel.loadingStatus.collectAsState()

    val topRecipe = displayedSuggestedRecipes.firstOrNull() ?: suggestedRecipes.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("fridge_recipes_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Fridge Scanner & Photo Bento Banner
        item {
            FridgeScannerBar(
                selectedPreset = selectedPreset,
                customBitmap = customBitmap,
                onSelectPreset = { viewModel.loadPreset(it) },
                onCustomPhotoSelected = { viewModel.scanFridgeWithCustomPhoto(it) }
            )
        }

        // 2. Bento Grid Interactive Hub
        if (topRecipe != null && !isLoading) {
            item {
                BentoGridHub(
                    topRecipe = topRecipe,
                    activeFilterCount = activeFilterCount,
                    shoppingCount = shoppingItems.count { !it.isBought },
                    pantryCount = pantryItems.size,
                    onStartCookingTopRecipe = { viewModel.startCooking(topRecipe) },
                    onOpenFilters = { viewModel.setFilterSheetOpen(true) },
                    onNavigateToShopping = { viewModel.navigateTo(AppScreen.SHOPPING_LIST) },
                    onNavigateToPantry = { viewModel.navigateTo(AppScreen.PANTRY_INVENTORY) },
                    onAddTopRecipeMissing = { viewModel.addMissingIngredients(topRecipe) }
                )
            }
        }

        // 3. Detected Ingredients Bento Shelf with Add / Filter controls
        item {
            IngredientsShelf(
                ingredients = detectedIngredients,
                onToggleIngredient = { viewModel.toggleIngredientSelected(it) },
                onAddIngredient = { name, category -> viewModel.addCustomIngredient(name, category) },
                onRemoveIngredient = { viewModel.removeIngredient(it) },
                onOpenFilterSheet = { viewModel.setFilterSheetOpen(true) },
                activeFilterCount = activeFilterCount
            )
        }

        // 4. AI Loading State
        if (isLoading) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BentoTileSage
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = BentoGreenPrimary,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = loadingStatus,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                    }
                }
            }
        }

        // 5. Section Header for Suggested Recipes & Quick Favorites Access
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "RECIPE DISCOVERY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.6.sp,
                            fontSize = 10.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = BentoTextMuted
                    )
                    Text(
                        text = "Personalized Recommendations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Filter Sheet Trigger Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (activeFilterCount > 0) BentoGreenPrimary else BentoTileSage,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.setFilterSheetOpen(true) }
                            .testTag("recipe_discovery_filter_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Filter Recipes",
                                tint = if (activeFilterCount > 0) Color.White else BentoGreenPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (activeFilterCount > 0) "Filters ($activeFilterCount)" else "Filter",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.Bold,
                                color = if (activeFilterCount > 0) Color.White else BentoGreenPrimary
                            )
                        }
                    }

                    // Favorites Pill
                    if (savedRecipes.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BentoTileApricot,
                            border = androidx.compose.foundation.BorderStroke(1.dp, WarmSpice.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.navigateTo(AppScreen.SAVED_RECIPES) }
                                .testTag("saved_recipes_counter_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Saved Favorites",
                                    tint = WarmSpice,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "${savedRecipes.size} Saved",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = WarmSpice
                                )
                            }
                        }
                    }

                    // Recipe Count Badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BentoTileSage
                    ) {
                        Text(
                            text = "${displayedSuggestedRecipes.size} recipes",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.Bold,
                            color = BentoGreenPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // 6. Interactive Meal Type Selector Bar (Breakfast / Lunch / Dinner / Snack)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MealType.values().forEach { meal ->
                    val isSelected = selectedMealType == meal
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) BentoGreenPrimary else BentoSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) BentoGreenPrimary else BentoBorder
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.setSelectedMealType(meal) }
                            .testTag("discovery_meal_${meal.name.lowercase()}")
                    ) {
                        Text(
                            text = meal.displayName,
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else BentoTextPrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        // 7. Interactive Prep Time & Cuisine Filter Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prep Time Quick Filters
                PrepTimeFilter.values().forEach { prep ->
                    val isSelected = selectedPrepTime == prep
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) BentoTileSage else BentoCardBg,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) BentoGreenPrimary else BentoBorder
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.setSelectedPrepTime(prep) }
                            .testTag("discovery_prep_${prep.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (isSelected) BentoGreenPrimary else BentoTextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = prep.displayName,
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) BentoGreenPrimary else BentoTextPrimary
                            )
                        }
                    }
                }

                // Cuisine Style Quick Filters
                CuisineFilter.values().filter { it != CuisineFilter.ALL }.forEach { cuisine ->
                    val isSelected = selectedCuisine == cuisine
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) BentoTileSage else BentoCardBg,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) BentoGreenPrimary else BentoBorder
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (isSelected) {
                                    viewModel.setSelectedCuisine(CuisineFilter.ALL)
                                } else {
                                    viewModel.setSelectedCuisine(cuisine)
                                }
                            }
                            .testTag("discovery_cuisine_${cuisine.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = cuisine.flagEmoji, fontSize = 12.sp)
                            Text(
                                text = cuisine.displayName,
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) BentoGreenPrimary else BentoTextPrimary
                            )
                        }
                    }
                }
            }
        }

        // 8. Interactive Dietary Preferences Filter Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "All / Reset" chip
                val isAllSelected = dietaryFilters.isEmpty()
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isAllSelected) BentoGreenPrimary else BentoSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isAllSelected) BentoGreenPrimary else BentoBorder
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.clearDietaryFilters() }
                        .testTag("dietary_filter_all")
                ) {
                    Text(
                        text = "🍽️ All Diets",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp),
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isAllSelected) Color.White else BentoTextPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }

                // Individual Dietary Preference Chips
                DietaryFilter.values().forEach { filter ->
                    val isSelected = dietaryFilters.contains(filter)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) BentoTileSage else BentoCardBg,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) BentoGreenPrimary else BentoBorder
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.toggleDietaryFilter(filter) }
                            .testTag("dietary_chip_${filter.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(text = filter.iconEmoji, fontSize = 13.sp)
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) BentoGreenPrimary else BentoTextPrimary
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = BentoGreenPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 9. Recipe Cards List (with Pantry Cross-Referencing, Substitutions, and Favorites)
        if (displayedSuggestedRecipes.isEmpty() && !isLoading) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BentoSurfaceVariant
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BentoTileSage,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.RestaurantMenu,
                                    contentDescription = null,
                                    tint = BentoGreenPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No matching recipes found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your meal type, prep time, or cuisine filters to see more recommendations.",
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.resetAllDiscoveryFilters() },
                            shape = RoundedCornerShape(16.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = BentoGreenPrimary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("reset_all_discovery_filters_button")
                        ) {
                            Text("Reset All Filters")
                        }
                    }
                }
            }
        } else {
            items(displayedSuggestedRecipes, key = { it.id }) { recipe ->
                RecipeCard(
                    recipe = recipe,
                    pantryItems = pantryItems,
                    onStartCooking = { viewModel.startCooking(recipe) },
                    onAddMissingIngredients = { viewModel.addMissingIngredients(recipe) },
                    onToggleSave = { viewModel.toggleSaveRecipe(recipe) },
                    onAddSubstituteToShoppingList = { substitute, original ->
                        viewModel.addSubstituteToShoppingList(substitute, original)
                    }
                )
            }
        }

        // Bottom spacing padding
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BentoGridHub(
    topRecipe: Recipe,
    activeFilterCount: Int,
    shoppingCount: Int,
    pantryCount: Int,
    onStartCookingTopRecipe: () -> Unit,
    onOpenFilters: () -> Unit,
    onNavigateToShopping: () -> Unit,
    onNavigateToPantry: () -> Unit,
    onAddTopRecipeMissing: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Bento Tile 1: Top Match Hero Card
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = BentoTileSage,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .clickable { onStartCookingTopRecipe() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Top Right Pill: TOP MATCH
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = BentoGreenPrimary,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "TOP MATCH",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 90.dp)
                ) {
                    Text(
                        text = topRecipe.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary,
                        lineHeight = 26.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Uses ${topRecipe.matchedIngredients.size} of your fridge ingredients",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoTextSecondary
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 70.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = BentoTextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "${topRecipe.totalTimeMinutes}m",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = BentoTextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "${topRecipe.calories} kcal",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = BentoTextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = topRecipe.difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                    }
                }
            }
        }

        // Bento Row 2: Two Asymmetric Modular Tiles (Dietary Filters + Pantry Inventory)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Bento Tile 2: Dietary Filters
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onOpenFilters() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Dietary Filters",
                        tint = BentoGreenPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Dietary Filters",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BentoSurfaceVariant
                    ) {
                        Text(
                            text = if (activeFilterCount > 0) "$activeFilterCount ACTIVE" else "ALL DIETS",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Bold,
                            color = BentoGreenPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Bento Tile 3: Pantry Inventory Quick Tile
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onNavigateToPantry() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = "Pantry Inventory",
                        tint = GoldenHoney,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pantry Stock",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BentoTileApricot
                    ) {
                        Text(
                            text = "$pantryCount STAPLES",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Bold,
                            color = WarmSpice,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Bento Tile 4: Shopping Quick Access Tile
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = BentoCardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .clickable { onNavigateToShopping() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SHOPPING LIST",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                            fontSize = 10.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = BentoTextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (topRecipe.missingIngredients.isNotEmpty()) {
                            "${topRecipe.missingIngredients.size} items missing for ${topRecipe.title}"
                        } else {
                            "$shoppingCount items in grocery list"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = BentoTextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BentoTileSage,
                    modifier = Modifier.clickable {
                        if (topRecipe.missingIngredients.isNotEmpty()) {
                            onAddTopRecipeMissing()
                        } else {
                            onNavigateToShopping()
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (topRecipe.missingIngredients.isNotEmpty()) "+ Add to list" else "Open List",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoGreenPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = null,
                            tint = BentoGreenPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
