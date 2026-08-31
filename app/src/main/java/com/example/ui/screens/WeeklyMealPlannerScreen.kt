package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DayInfo
import com.example.model.MealSlot
import com.example.model.PlannedMeal
import com.example.model.Recipe
import com.example.model.WeeklyNutritionSummary
import com.example.ui.CulinaryViewModel
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoCardBg
import com.example.ui.theme.BentoCoralPrimary
import com.example.ui.theme.BentoGreenDark
import com.example.ui.theme.BentoGreenPrimary
import com.example.ui.theme.BentoSurface
import com.example.ui.theme.BentoSurfaceVariant
import com.example.ui.theme.BentoTextMuted
import com.example.ui.theme.BentoTextPrimary
import com.example.ui.theme.BentoTextSecondary
import com.example.ui.theme.BentoTileApricot
import com.example.ui.theme.BentoTileHoney
import com.example.ui.theme.BentoTileSage
import com.example.ui.theme.GoldenHoney
import com.example.ui.theme.MissingTagBg
import com.example.ui.theme.MissingTagText
import com.example.ui.theme.SuccessTagBg
import com.example.ui.theme.SuccessTagText
import com.example.ui.theme.WarmSpice
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyMealPlannerScreen(
    viewModel: CulinaryViewModel,
    modifier: Modifier = Modifier
) {
    val selectedMonday by viewModel.selectedWeekMonday.collectAsState()
    val allPlannedMeals by viewModel.allPlannedMeals.collectAsState()
    val savedRecipes by viewModel.savedRecipes.collectAsState()
    val suggestedRecipes by viewModel.suggestedRecipes.collectAsState()

    val isDragging by viewModel.isDragging.collectAsState()
    val draggedRecipe by viewModel.draggedRecipe.collectAsState()
    val draggedMeal by viewModel.draggedMeal.collectAsState()
    val dragHoverDay by viewModel.dragHoverDay.collectAsState()
    val dragHoverSlot by viewModel.dragHoverSlot.collectAsState()

    // 7 days of the selected week (Mon..Sun)
    val weekDays = remember(selectedMonday) {
        PlannedMeal.getWeekDays(selectedMonday)
    }

    // Filter meals for the selected 7 days
    val weekMeals = remember(allPlannedMeals, weekDays) {
        val dateSet = weekDays.map { it.dateString }.toSet()
        allPlannedMeals.filter { it.dateString in dateSet }
    }

    // Weekly summary calculation
    val weeklySummary = remember(weekMeals) {
        val totalCal = weekMeals.sumOf { it.calories }
        WeeklyNutritionSummary(
            totalCalories = totalCal,
            avgDailyCalories = if (weekMeals.isNotEmpty()) totalCal / 7 else 0,
            totalProteinGrams = weekMeals.sumOf { it.proteinGrams },
            totalCarbsGrams = weekMeals.sumOf { it.carbsGrams },
            totalFatGrams = weekMeals.sumOf { it.fatGrams },
            totalMealsCount = weekMeals.size,
            cookedMealsCount = weekMeals.count { it.isCooked }
        )
    }

    // Combined available recipes pool (Saved + Suggested)
    val allAvailableRecipes = remember(savedRecipes, suggestedRecipes) {
        (savedRecipes + suggestedRecipes).distinctBy { it.id }
    }

    // Active Selected Day Tab (null = view all 7 days in grid/list)
    var selectedDayFilter by remember { mutableStateOf<DayInfo?>(null) }
    var showRecipePickerSheet by remember { mutableStateOf(false) }
    var targetSlotForPicker by remember { mutableStateOf<Pair<DayInfo, String>?>(null) }
    var showClearWeekConfirm by remember { mutableStateOf(false) }
    var mealToMove by remember { mutableStateOf<PlannedMeal?>(null) }

    // Floating drag coordinate state
    var dragVisualOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BentoBackground)
            .testTag("weekly_meal_planner_screen")
    ) {
        Scaffold(
            containerColor = BentoBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Weekly Meal Planner",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextPrimary
                            )
                            Text(
                                text = "Drag & drop recipes onto any day of the week",
                                style = MaterialTheme.typography.labelSmall,
                                color = BentoTextMuted
                            )
                        }
                    },
                    actions = {
                        // Auto-Plan AI Button
                        IconButton(
                            onClick = { viewModel.autoPlanWeek(weekDays) },
                            modifier = Modifier.testTag("auto_plan_week_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Auto-Plan Week",
                                tint = BentoGreenPrimary
                            )
                        }

                        // Generate Groceries Button
                        IconButton(
                            onClick = { viewModel.generateWeeklyShoppingList(weekMeals) },
                            modifier = Modifier.testTag("export_weekly_groceries_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Add missing ingredients to Shopping List",
                                tint = WarmSpice
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BentoBackground)
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Week Navigator Bar
                item {
                    WeekNavigatorCard(
                        selectedMonday = selectedMonday,
                        onPrevious = { viewModel.previousWeek() },
                        onNext = { viewModel.nextWeek() },
                        onToday = { viewModel.resetToCurrentWeek() }
                    )
                }

                // 2. Weekly Nutrition & Meal Summary Bento Card
                item {
                    WeeklyBentoSummaryCard(
                        summary = weeklySummary,
                        onExportGroceries = { viewModel.generateWeeklyShoppingList(weekMeals) },
                        onAutoPlan = { viewModel.autoPlanWeek(weekDays) }
                    )
                }

                // 3. Drag instructions hint banner
                item {
                    DragHintBanner(isDragging = isDragging)
                }

                // 4. Day Filter Pills Row
                item {
                    DayPillsRow(
                        weekDays = weekDays,
                        weekMeals = weekMeals,
                        selectedDay = selectedDayFilter,
                        onSelectDay = { selectedDayFilter = if (selectedDayFilter == it) null else it }
                    )
                }

                // 5. Day Cards (Either Single Selected Day or All 7 Days)
                val daysToDisplay = if (selectedDayFilter != null) {
                    listOf(selectedDayFilter!!)
                } else {
                    weekDays
                }

                items(daysToDisplay, key = { it.dateString }) { day ->
                    val dayMeals = weekMeals.filter { it.dateString == day.dateString }
                    DayPlanCard(
                        day = day,
                        meals = dayMeals,
                        isDragging = isDragging,
                        dragHoverDay = dragHoverDay,
                        dragHoverSlot = dragHoverSlot,
                        onDropOnSlot = { slot ->
                            viewModel.dropOnDaySlot(day.dayName, day.dateString, slot)
                        },
                        onQuickAddSlot = { slot ->
                            targetSlotForPicker = Pair(day, slot)
                            showRecipePickerSheet = true
                        },
                        onCookMeal = { meal ->
                            viewModel.startCookingPlannedMeal(meal)
                        },
                        onToggleCooked = { meal ->
                            viewModel.togglePlannedMealCooked(meal)
                        },
                        onDeleteMeal = { meal ->
                            viewModel.deletePlannedMeal(meal)
                        },
                        onMoveMeal = { meal ->
                            mealToMove = meal
                        },
                        onStartDragMeal = { meal ->
                            viewModel.startDragMeal(meal)
                        }
                    )
                }

                // 6. Docked Saved & Suggested Recipes Drag Tray Header
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = WarmSpice,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Saved & Suggested Recipes (${allAvailableRecipes.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextPrimary
                            )
                        }

                        Text(
                            text = "Hold & Drag ✋",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoGreenPrimary
                        )
                    }
                }

                // 7. Horizontal Draggable Recipes Shelf
                item {
                    DraggableRecipesShelf(
                        recipes = allAvailableRecipes,
                        onStartDragRecipe = { recipe ->
                            viewModel.startDragRecipe(recipe)
                        },
                        onTapRecipe = { recipe ->
                            // Quick-assign to current day or tomorrow
                            val targetDay = selectedDayFilter ?: weekDays.firstOrNull { it.isToday } ?: weekDays.first()
                            viewModel.planRecipe(recipe, targetDay.dayName, targetDay.dateString, "Dinner")
                        }
                    )
                }

                // Clear Week action button
                if (weekMeals.isNotEmpty()) {
                    item {
                        OutlinedButton(
                            onClick = { showClearWeekConfirm = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoCoralPrimary),
                            border = BorderStroke(1.dp, BentoBorder),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .testTag("clear_week_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clear Weekly Meal Plan", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }

        // Floating Drag Overlay Indicator
        if (isDragging && (draggedRecipe != null || draggedMeal != null)) {
            val title = draggedRecipe?.title ?: draggedMeal?.recipeTitle ?: "Meal"
            val cal = draggedRecipe?.calories ?: draggedMeal?.calories ?: 0

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BentoGreenPrimary,
                    shadowElevation = 12.dp,
                    border = BorderStroke(2.dp, Color.White),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(bottom = 16.dp)
                        .testTag("floating_drag_indicator")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Dragging: $title",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Tap on any day slot above to place ($cal kcal)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.cancelDrag() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel Drag",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet: Quick Assign Recipe to Slot
    if (showRecipePickerSheet && targetSlotForPicker != null) {
        val (targetDay, targetSlot) = targetSlotForPicker!!
        RecipePickerBottomSheet(
            targetDay = targetDay,
            targetSlot = targetSlot,
            recipes = allAvailableRecipes,
            onDismiss = {
                showRecipePickerSheet = false
                targetSlotForPicker = null
            },
            onSelectRecipe = { recipe, servings ->
                viewModel.planRecipe(recipe, targetDay.dayName, targetDay.dateString, targetSlot, servings)
                showRecipePickerSheet = false
                targetSlotForPicker = null
            }
        )
    }

    // Move Planned Meal Dialog
    if (mealToMove != null) {
        val meal = mealToMove!!
        MoveMealDialog(
            meal = meal,
            weekDays = weekDays,
            onDismiss = { mealToMove = null },
            onMove = { newDay, newSlot ->
                viewModel.moveMeal(meal, newDay.dayName, newDay.dateString, newSlot)
                mealToMove = null
            }
        )
    }

    // Clear Week Confirmation Dialog
    if (showClearWeekConfirm) {
        AlertDialog(
            onDismissRequest = { showClearWeekConfirm = false },
            title = {
                Text(
                    text = "Clear Weekly Plan?",
                    fontWeight = FontWeight.Bold,
                    color = BentoTextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove all planned meals for this week? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BentoTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearCurrentWeek(weekDays)
                        showClearWeekConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoCoralPrimary)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearWeekConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun WeekNavigatorCard(
    selectedMonday: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    val weekRangeText = remember(selectedMonday) {
        PlannedMeal.formatWeekRange(selectedMonday)
    }
    val isCurrentWeek = selectedMonday == PlannedMeal.getMondayOfWeek()

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurface),
        border = BorderStroke(1.dp, BentoBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPrevious,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("prev_week_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Week",
                    tint = BentoTextPrimary
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isCurrentWeek) BentoTileSage else BentoSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToday() }
                    .testTag("current_week_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = if (isCurrentWeek) BentoGreenPrimary else BentoTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = weekRangeText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrentWeek) BentoGreenPrimary else BentoTextPrimary
                    )
                }
            }

            IconButton(
                onClick = onNext,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("next_week_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Week",
                    tint = BentoTextPrimary
                )
            }
        }
    }
}

@Composable
private fun WeeklyBentoSummaryCard(
    summary: WeeklyNutritionSummary,
    onExportGroceries: () -> Unit,
    onAutoPlan: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurface),
        border = BorderStroke(1.dp, BentoBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weekly_summary_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weekly Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                    Text(
                        text = "${summary.totalMealsCount} meals planned • ${summary.cookedMealsCount} cooked",
                        style = MaterialTheme.typography.labelSmall,
                        color = BentoTextMuted
                    )
                }

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = BentoTileSage
                ) {
                    Text(
                        text = "${summary.totalCalories} kcal total",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoGreenPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Macro Mini Bento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MacroPillTile(
                    label = "Avg/Day",
                    value = "${summary.avgDailyCalories} kcal",
                    containerColor = BentoTileApricot,
                    textColor = WarmSpice,
                    modifier = Modifier.weight(1f)
                )
                MacroPillTile(
                    label = "Protein",
                    value = "${summary.totalProteinGrams}g",
                    containerColor = BentoTileSage,
                    textColor = BentoGreenPrimary,
                    modifier = Modifier.weight(1f)
                )
                MacroPillTile(
                    label = "Carbs",
                    value = "${summary.totalCarbsGrams}g",
                    containerColor = BentoTileHoney,
                    textColor = GoldenHoney,
                    modifier = Modifier.weight(1f)
                )
                MacroPillTile(
                    label = "Fat",
                    value = "${summary.totalFatGrams}g",
                    containerColor = BentoSurfaceVariant,
                    textColor = BentoTextSecondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Actions: Auto-plan & Export Groceries
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAutoPlan,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoGreenPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("summary_auto_plan_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Auto-Plan Week",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onExportGroceries,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BentoBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WarmSpice),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("summary_export_groceries_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Weekly Groceries",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MacroPillTile(
    label: String,
    value: String,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                fontWeight = FontWeight.Bold,
                color = BentoTextMuted
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = textColor
            )
        }
    }
}

@Composable
private fun DragHintBanner(isDragging: Boolean) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDragging) BentoTileSage else BentoSurfaceVariant,
        border = BorderStroke(1.dp, if (isDragging) BentoGreenPrimary else BentoBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isDragging) "🎯 Release or tap any slot to place the recipe!" else "💡 Tip: Long-press & drag any saved recipe onto a day slot below, or tap '+' on any slot.",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isDragging) FontWeight.Bold else FontWeight.Normal,
                color = if (isDragging) BentoGreenPrimary else BentoTextSecondary
            )
        }
    }
}

@Composable
private fun DayPillsRow(
    weekDays: List<DayInfo>,
    weekMeals: List<PlannedMeal>,
    selectedDay: DayInfo?,
    onSelectDay: (DayInfo) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(weekDays, key = { it.dateString }) { day ->
            val dayMeals = weekMeals.filter { it.dateString == day.dateString }
            val isSelected = selectedDay?.dateString == day.dateString

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = when {
                    isSelected -> BentoGreenPrimary
                    day.isToday -> BentoTileSage
                    else -> BentoSurface
                },
                border = BorderStroke(
                    1.dp,
                    if (isSelected) BentoGreenPrimary else if (day.isToday) BentoGreenPrimary.copy(alpha = 0.5f) else BentoBorder
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelectDay(day) }
                    .testTag("day_pill_${day.dayShort}")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = day.dayShort.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else BentoTextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = day.dayNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = if (isSelected) Color.White else if (day.isToday) BentoGreenPrimary else BentoTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Meal count dot / indicator
                    if (dayMeals.isNotEmpty()) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) Color.White else BentoGreenPrimary,
                            modifier = Modifier.size(6.dp)
                        ) {}
                    } else {
                        Spacer(modifier = Modifier.size(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayPlanCard(
    day: DayInfo,
    meals: List<PlannedMeal>,
    isDragging: Boolean,
    dragHoverDay: String?,
    dragHoverSlot: String?,
    onDropOnSlot: (slot: String) -> Unit,
    onQuickAddSlot: (slot: String) -> Unit,
    onCookMeal: (PlannedMeal) -> Unit,
    onToggleCooked: (PlannedMeal) -> Unit,
    onDeleteMeal: (PlannedMeal) -> Unit,
    onMoveMeal: (PlannedMeal) -> Unit,
    onStartDragMeal: (PlannedMeal) -> Unit
) {
    val totalCalories = meals.sumOf { it.calories }
    val totalProtein = meals.sumOf { it.proteinGrams }
    val isToday = day.isToday

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurface),
        border = BorderStroke(1.dp, if (isToday) BentoGreenPrimary.copy(alpha = 0.5f) else BentoBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("day_card_${day.dayName}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Day Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = day.formattedDisplay,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )

                    if (isToday) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = BentoGreenPrimary
                        ) {
                            Text(
                                text = "TODAY",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (meals.isNotEmpty()) {
                    Text(
                        text = "$totalCalories kcal • ${totalProtein}g P",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Meal Slots: Breakfast, Lunch, Dinner, Snack
            val standardSlots = listOf("Breakfast", "Lunch", "Dinner", "Snack")

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                standardSlots.forEach { slot ->
                    val slotMeals = meals.filter { it.mealSlot.equals(slot, ignoreCase = true) }
                    val isHovered = isDragging && (dragHoverDay == day.dayName && dragHoverSlot == slot)

                    SlotSection(
                        day = day,
                        slot = slot,
                        meals = slotMeals,
                        isDragging = isDragging,
                        isHovered = isHovered,
                        onDrop = { onDropOnSlot(slot) },
                        onQuickAdd = { onQuickAddSlot(slot) },
                        onCookMeal = onCookMeal,
                        onToggleCooked = onToggleCooked,
                        onDeleteMeal = onDeleteMeal,
                        onMoveMeal = onMoveMeal,
                        onStartDragMeal = onStartDragMeal
                    )
                }
            }
        }
    }
}

@Composable
private fun SlotSection(
    day: DayInfo,
    slot: String,
    meals: List<PlannedMeal>,
    isDragging: Boolean,
    isHovered: Boolean,
    onDrop: () -> Unit,
    onQuickAdd: () -> Unit,
    onCookMeal: (PlannedMeal) -> Unit,
    onToggleCooked: (PlannedMeal) -> Unit,
    onDeleteMeal: (PlannedMeal) -> Unit,
    onMoveMeal: (PlannedMeal) -> Unit,
    onStartDragMeal: (PlannedMeal) -> Unit
) {
    val slotIcon = when (slot.lowercase()) {
        "breakfast" -> "🍳"
        "lunch" -> "🥗"
        "dinner" -> "🍲"
        else -> "🍎"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isHovered) BentoTileSage else BentoCardBg)
            .padding(10.dp)
    ) {
        // Slot Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = slotIcon, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = slot,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextMuted
                )
            }

            // Quick Add '+' or Drop Target button
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isDragging) BentoTileSage else BentoSurfaceVariant,
                border = BorderStroke(1.dp, if (isDragging) BentoGreenPrimary else BentoBorder),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        if (isDragging) onDrop() else onQuickAdd()
                    }
                    .testTag("slot_action_${day.dayShort}_$slot")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isDragging) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = if (isDragging) "Drop Recipe Here" else "Add Meal",
                        tint = if (isDragging) BentoGreenPrimary else BentoTextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isDragging) "Drop Here" else "Add",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        color = if (isDragging) BentoGreenPrimary else BentoTextSecondary
                    )
                }
            }
        }

        if (meals.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                meals.forEach { meal ->
                    PlannedMealItem(
                        meal = meal,
                        onCook = { onCookMeal(meal) },
                        onToggleCooked = { onToggleCooked(meal) },
                        onDelete = { onDeleteMeal(meal) },
                        onMove = { onMoveMeal(meal) },
                        onStartDrag = { onStartDragMeal(meal) }
                    )
                }
            }
        } else if (isDragging) {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = BentoTileSage.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, BentoGreenPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onDrop() }
            ) {
                Text(
                    text = "📥 Tap to drop for ${day.dayShort} $slot",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = BentoGreenPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun PlannedMealItem(
    meal: PlannedMeal,
    onCook: () -> Unit,
    onToggleCooked: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit,
    onStartDrag: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = BentoSurface,
        border = BorderStroke(1.dp, if (meal.isCooked) BentoGreenPrimary.copy(alpha = 0.5f) else BentoBorder),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("planned_meal_${meal.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cooked Checkbox / Circle
                IconButton(
                    onClick = onToggleCooked,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("toggle_cooked_${meal.id}")
                ) {
                    Icon(
                        imageVector = if (meal.isCooked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (meal.isCooked) "Mark as Uncooked" else "Mark as Cooked",
                        tint = if (meal.isCooked) BentoGreenPrimary else BentoTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Text(
                        text = meal.recipeTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${meal.calories} kcal",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.Bold,
                            color = WarmSpice
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = BentoTextMuted
                        )
                        Text(
                            text = "${meal.proteinGrams}g Protein",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.Medium,
                            color = BentoGreenPrimary
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = BentoTextMuted
                        )
                        Text(
                            text = "${meal.totalTimeMinutes}m",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = BentoTextMuted
                        )
                    }
                }
            }

            // Quick Actions: Cook, Move, Delete
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Cook Now Button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BentoGreenPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onCook() }
                        .testTag("cook_planned_meal_${meal.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Cook",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "Cook",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Move Button
                IconButton(
                    onClick = onMove,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("move_meal_${meal.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Move to another day",
                        tint = BentoTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_planned_meal_${meal.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = BentoTextMuted.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DraggableRecipesShelf(
    recipes: List<Recipe>,
    onStartDragRecipe: (Recipe) -> Unit,
    onTapRecipe: (Recipe) -> Unit
) {
    if (recipes.isEmpty()) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = BentoSurface,
            border = BorderStroke(1.dp, BentoBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No saved recipes available yet",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Generate recipes from the Smart Fridge or save favorites to organize them in your weekly plan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(recipes, key = { it.id }) { recipe ->
                DraggableRecipeCard(
                    recipe = recipe,
                    onStartDrag = { onStartDragRecipe(recipe) },
                    onTap = { onTapRecipe(recipe) }
                )
            }
        }
    }
}

@Composable
private fun DraggableRecipeCard(
    recipe: Recipe,
    onStartDrag: () -> Unit,
    onTap: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurface),
        border = BorderStroke(1.dp, BentoBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(190.dp)
            .pointerInput(recipe.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { onStartDrag() },
                    onDrag = { _, _ -> },
                    onDragEnd = { },
                    onDragCancel = { }
                )
            }
            .clip(RoundedCornerShape(18.dp))
            .clickable { onTap() }
            .testTag("draggable_recipe_${recipe.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BentoTileSage
                ) {
                    Text(
                        text = "${recipe.totalTimeMinutes}m",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        color = BentoGreenPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag Recipe",
                    tint = BentoTextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = BentoTextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${recipe.calories} kcal",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = WarmSpice
                )

                Text(
                    text = "${recipe.proteinGrams}g P",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = BentoGreenPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = BentoSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "👆 Hold to Drag",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.Bold,
                    color = BentoTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipePickerBottomSheet(
    targetDay: DayInfo,
    targetSlot: String,
    recipes: List<Recipe>,
    onDismiss: () -> Unit,
    onSelectRecipe: (Recipe, Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    var selectedServings by remember { mutableIntStateOf(1) }

    val filtered = recipes.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.cuisine.contains(searchQuery, ignoreCase = true)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BentoBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Add to ${targetDay.dayShort} $targetSlot",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                    Text(
                        text = "Select a saved or suggested recipe",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoTextMuted
                    )
                }

                // Servings selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { if (selectedServings > 1) selectedServings-- },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("-", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "$selectedServings serv",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoGreenPrimary
                    )
                    IconButton(
                        onClick = { if (selectedServings < 8) selectedServings++ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("+", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search recipes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No matching recipes found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BentoTextMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.id }) { recipe ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = BentoSurface,
                            border = BorderStroke(1.dp, BentoBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onSelectRecipe(recipe, selectedServings) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = recipe.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${recipe.calories * selectedServings} kcal • ${recipe.proteinGrams * selectedServings}g Protein • ${recipe.cuisine}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BentoTextMuted
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = BentoGreenPrimary
                                ) {
                                    Text(
                                        text = "+ Plan",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoveMealDialog(
    meal: PlannedMeal,
    weekDays: List<DayInfo>,
    onDismiss: () -> Unit,
    onMove: (newDay: DayInfo, newSlot: String) -> Unit
) {
    var selectedDay by remember { mutableStateOf(weekDays.firstOrNull { it.dayName == meal.dayOfWeek } ?: weekDays.first()) }
    var selectedSlot by remember { mutableStateOf(meal.mealSlot) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Reschedule '${meal.recipeTitle}'",
                fontWeight = FontWeight.Bold,
                color = BentoTextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Choose target day:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextMuted
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(weekDays, key = { it.dateString }) { day ->
                        val isSel = selectedDay.dateString == day.dateString
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) BentoGreenPrimary else BentoSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedDay = day }
                        ) {
                            Text(
                                text = day.dayShort,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else BentoTextPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Choose meal slot:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextMuted
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Breakfast", "Lunch", "Dinner", "Snack").forEach { slot ->
                        val isSel = selectedSlot.equals(slot, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) BentoGreenPrimary else BentoSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedSlot = slot }
                        ) {
                            Text(
                                text = slot,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else BentoTextPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onMove(selectedDay, selectedSlot) },
                colors = ButtonDefaults.buttonColors(containerColor = BentoGreenPrimary)
            ) {
                Text("Move Meal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
