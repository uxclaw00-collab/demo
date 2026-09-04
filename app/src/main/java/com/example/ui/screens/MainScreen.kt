package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.CulinaryViewModel
import com.example.ui.components.DietaryFilterSheet
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoGreenPrimary
import com.example.ui.theme.BentoSurfaceVariant
import com.example.ui.theme.BentoTextMuted
import com.example.ui.theme.BentoTextPrimary
import com.example.ui.theme.BentoTileApricot
import com.example.ui.theme.BentoTileSage
import com.example.ui.theme.WarmSpice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: CulinaryViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isFilterSheetOpen by viewModel.isFilterSheetOpen.collectAsState()
    val dietaryFilters by viewModel.dietaryFilters.collectAsState()
    val selectedMealType by viewModel.selectedMealType.collectAsState()
    val selectedPrepTime by viewModel.selectedPrepTime.collectAsState()
    val selectedCuisine by viewModel.selectedCuisine.collectAsState()
    val activeFilterCount by viewModel.activeFilterCount.collectAsState()
    val shoppingItems by viewModel.shoppingItems.collectAsState()
    val savedRecipes by viewModel.savedRecipes.collectAsState()
    val pantryItems by viewModel.pantryItems.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissSnackbar()
        }
    }

    // Fullscreen Step-by-Step Cooking Mode takes over screen when active
    if (currentScreen == AppScreen.COOKING_MODE) {
        CookingModeScreen(
            viewModel = viewModel,
            modifier = modifier
        )
        return
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AI CULINARY ASSISTANT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.8.sp,
                                fontSize = 10.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = BentoTextMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when (currentScreen) {
                                AppScreen.FRIDGE_RECIPES -> "Smart Fridge"
                                AppScreen.WEEKLY_PLANNER -> "Weekly Planner"
                                AppScreen.NUTRITION_LOG -> "Daily Nutrition"
                                AppScreen.PANTRY_INVENTORY -> "Pantry Inventory"
                                AppScreen.SHOPPING_LIST -> "Shopping Bento"
                                AppScreen.SAVED_RECIPES -> "Favorite Recipes"
                                else -> "Smart Fridge"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (currentScreen == AppScreen.FRIDGE_RECIPES) {
                            Surface(
                                shape = CircleShape,
                                color = BentoTileSage,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable { viewModel.setFilterSheetOpen(true) }
                                    .testTag("top_dietary_filter_icon")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    BadgedBox(
                                        badge = {
                                            if (activeFilterCount > 0) {
                                                Badge(
                                                    containerColor = BentoGreenPrimary,
                                                    contentColor = Color.White
                                                ) {
                                                    Text("$activeFilterCount")
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = "Dietary Restrictions Filter",
                                            tint = BentoGreenPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Bento Header Icon Box
                        Surface(
                            shape = CircleShape,
                            color = when (currentScreen) {
                                AppScreen.SAVED_RECIPES -> BentoTileApricot
                                else -> BentoTileSage
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when (currentScreen) {
                                        AppScreen.WEEKLY_PLANNER -> Icons.Default.CalendarMonth
                                        AppScreen.NUTRITION_LOG -> Icons.Default.PieChart
                                        AppScreen.PANTRY_INVENTORY -> Icons.Default.Inventory2
                                        AppScreen.SHOPPING_LIST -> Icons.Default.ShoppingCart
                                        AppScreen.SAVED_RECIPES -> Icons.Default.Favorite
                                        else -> Icons.Default.Kitchen
                                    },
                                    contentDescription = "Hub",
                                    tint = when (currentScreen) {
                                        AppScreen.SAVED_RECIPES -> WarmSpice
                                        else -> BentoGreenPrimary
                                    },
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = BentoSurfaceVariant,
                border = BorderStroke(1.dp, BentoBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                NavigationBar(
                    containerColor = BentoSurfaceVariant,
                    tonalElevation = 0.dp
                ) {
                    // Tab 1: Smart Fridge Recipes
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.FRIDGE_RECIPES,
                        onClick = { viewModel.navigateTo(AppScreen.FRIDGE_RECIPES) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == AppScreen.FRIDGE_RECIPES) Icons.Filled.Kitchen else Icons.Outlined.Kitchen,
                                contentDescription = "Fridge & Recipes"
                            )
                        },
                        label = {
                            Text(
                                "FRIDGE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BentoGreenPrimary,
                            selectedTextColor = BentoGreenPrimary,
                            unselectedIconColor = BentoTextMuted,
                            unselectedTextColor = BentoTextMuted,
                            indicatorColor = BentoTileSage
                        ),
                        modifier = Modifier.testTag("nav_tab_fridge")
                    )

                    // Tab 2: Weekly Meal Planner
                    val plannedMealsCount = viewModel.allPlannedMeals.collectAsState().value.size
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.WEEKLY_PLANNER,
                        onClick = { viewModel.navigateTo(AppScreen.WEEKLY_PLANNER) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (plannedMealsCount > 0) {
                                        Badge(
                                            containerColor = BentoGreenPrimary,
                                            contentColor = Color.White
                                        ) {
                                            Text("$plannedMealsCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentScreen == AppScreen.WEEKLY_PLANNER) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                                    contentDescription = "Weekly Meal Planner"
                                )
                            }
                        },
                        label = {
                            Text(
                                "PLAN",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BentoGreenPrimary,
                            selectedTextColor = BentoGreenPrimary,
                            unselectedIconColor = BentoTextMuted,
                            unselectedTextColor = BentoTextMuted,
                            indicatorColor = BentoTileSage
                        ),
                        modifier = Modifier.testTag("nav_tab_planner")
                    )

                    // Tab 3: Daily Nutrition Log
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.NUTRITION_LOG,
                        onClick = { viewModel.navigateTo(AppScreen.NUTRITION_LOG) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == AppScreen.NUTRITION_LOG) Icons.Filled.PieChart else Icons.Outlined.PieChart,
                                contentDescription = "Daily Nutrition"
                            )
                        },
                        label = {
                            Text(
                                "INTAKE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BentoGreenPrimary,
                            selectedTextColor = BentoGreenPrimary,
                            unselectedIconColor = BentoTextMuted,
                            unselectedTextColor = BentoTextMuted,
                            indicatorColor = BentoTileSage
                        ),
                        modifier = Modifier.testTag("nav_tab_nutrition")
                    )

                    // Tab 4: Pantry Inventory
                    val expiringCountOk = pantryItems.count { it.isExpiringSoon || it.isExpired }
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.PANTRY_INVENTORY,
                        onClick = { viewModel.navigateTo(AppScreen.PANTRY_INVENTORY) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (expiringCountOk > 0) {
                                        Badge(
                                            containerColor = WarmSpice,
                                            contentColor = Color.White
                                        ) {
                                            Text("$expiringCountOk")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentScreen == AppScreen.PANTRY_INVENTORY) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2,
                                    contentDescription = "Pantry Inventory"
                                )
                            }
                        },
                        label = {
                            Text(
                                "PANTRY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BentoGreenPrimary,
                            selectedTextColor = BentoGreenPrimary,
                            unselectedIconColor = BentoTextMuted,
                            unselectedTextColor = BentoTextMuted,
                            indicatorColor = BentoTileSage
                        ),
                        modifier = Modifier.testTag("nav_tab_pantry")
                    )

                    // Tab 5: Shopping List
                    val unboughtCount = shoppingItems.count { !it.isBought }
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.SHOPPING_LIST,
                        onClick = { viewModel.navigateTo(AppScreen.SHOPPING_LIST) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (unboughtCount > 0) {
                                        Badge(
                                            containerColor = BentoGreenPrimary,
                                            contentColor = Color.White
                                        ) {
                                            Text("$unboughtCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentScreen == AppScreen.SHOPPING_LIST) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart,
                                    contentDescription = "Shopping List"
                                )
                            }
                        },
                        label = {
                            Text(
                                "LIST",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BentoGreenPrimary,
                            selectedTextColor = BentoGreenPrimary,
                            unselectedIconColor = BentoTextMuted,
                            unselectedTextColor = BentoTextMuted,
                            indicatorColor = BentoTileSage
                        ),
                        modifier = Modifier.testTag("nav_tab_shopping")
                    )

                    // Tab 6: Favorite Recipes
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.SAVED_RECIPES,
                        onClick = { viewModel.navigateTo(AppScreen.SAVED_RECIPES) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (savedRecipes.isNotEmpty()) {
                                        Badge(
                                            containerColor = WarmSpice,
                                            contentColor = Color.White
                                        ) {
                                            Text("${savedRecipes.size}")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentScreen == AppScreen.SAVED_RECIPES) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite Recipes"
                                )
                            }
                        },
                        label = {
                            Text(
                                "FAVES",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = WarmSpice,
                            selectedTextColor = WarmSpice,
                            unselectedIconColor = BentoTextMuted,
                            unselectedTextColor = BentoTextMuted,
                            indicatorColor = BentoTileApricot
                        ),
                        modifier = Modifier.testTag("nav_tab_favorites")
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.testTag("main_screen_scaffold")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    AppScreen.FRIDGE_RECIPES -> FridgeRecipesScreen(viewModel = viewModel)
                    AppScreen.WEEKLY_PLANNER -> WeeklyMealPlannerScreen(viewModel = viewModel)
                    AppScreen.NUTRITION_LOG -> NutritionLogScreen(viewModel = viewModel)
                    AppScreen.PANTRY_INVENTORY -> PantryInventoryScreen(viewModel = viewModel)
                    AppScreen.SHOPPING_LIST -> ShoppingListScreen(viewModel = viewModel)
                    AppScreen.SAVED_RECIPES -> SavedRecipesScreen(viewModel = viewModel)
                    AppScreen.COOKING_MODE -> Box {} // Handled above
                }
            }
        }
    }

    // Dietary Filter Bottom Sheet / Sidebar
    DietaryFilterSheet(
        isOpen = isFilterSheetOpen,
        selectedFilters = dietaryFilters,
        selectedMealType = selectedMealType,
        selectedPrepTime = selectedPrepTime,
        selectedCuisine = selectedCuisine,
        onToggleFilter = { viewModel.toggleDietaryFilter(it) },
        onSelectMealType = { viewModel.setSelectedMealType(it) },
        onSelectPrepTime = { viewModel.setSelectedPrepTime(it) },
        onSelectCuisine = { viewModel.setSelectedCuisine(it) },
        onClearFilters = { viewModel.resetAllDiscoveryFilters() },
        onApply = { viewModel.regenerateRecipesWithActiveFilters() },
        onDismiss = { viewModel.setFilterSheetOpen(false) }
    )
}
