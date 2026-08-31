package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Restaurant
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailyNutritionSummary
import com.example.model.MealLog
import com.example.model.NutritionGoals
import com.example.model.Recipe
import com.example.ui.CulinaryViewModel
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoCardBg
import com.example.ui.theme.BentoCoralDark
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionLogScreen(
    viewModel: CulinaryViewModel,
    modifier: Modifier = Modifier
) {
    val allLogs by viewModel.allMealLogs.collectAsState()
    val selectedDate by viewModel.selectedNutritionDate.collectAsState()
    val goals by viewModel.nutritionGoals.collectAsState()
    val suggestedRecipes by viewModel.suggestedRecipes.collectAsState()

    // Filter logs for selected date
    val dayLogs = remember(allLogs, selectedDate) {
        allLogs.filter { it.dateString == selectedDate }
    }

    // Calculate daily totals
    val dailySummary = remember(dayLogs, goals, selectedDate) {
        DailyNutritionSummary(
            dateString = selectedDate,
            totalCalories = dayLogs.sumOf { it.calories },
            totalProteinGrams = dayLogs.sumOf { it.proteinGrams },
            totalCarbsGrams = dayLogs.sumOf { it.carbsGrams },
            totalFatGrams = dayLogs.sumOf { it.fatGrams },
            mealsCount = dayLogs.size,
            targetCalories = goals.targetCalories,
            targetProteinGrams = goals.targetProteinGrams,
            targetCarbsGrams = goals.targetCarbsGrams,
            targetFatGrams = goals.targetFatGrams
        )
    }

    var showGoalsDialog by remember { mutableStateOf(false) }
    var showAddMealDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("nutrition_log_screen"),
        containerColor = BentoBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Daily Nutrition",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                        Text(
                            text = "Auto-calculated from cooked meals",
                            style = MaterialTheme.typography.labelSmall,
                            color = BentoTextMuted
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showGoalsDialog = true },
                        modifier = Modifier.testTag("edit_nutrition_goals_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Goals",
                            tint = BentoGreenPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BentoBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddMealDialog = true },
                containerColor = BentoGreenPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("add_meal_log_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Log Meal")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Log Meal",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Date Navigator Bar
            item {
                DateNavigator(
                    currentDateString = selectedDate,
                    onPrevious = { viewModel.previousNutritionDay() },
                    onNext = { viewModel.nextNutritionDay() },
                    onToday = { viewModel.resetNutritionDateToToday() }
                )
            }

            // 2. Calorie Bento Overview Card
            item {
                CalorieOverviewBentoCard(summary = dailySummary)
            }

            // 3. Macronutrients Bento Grid (Protein, Carbs, Fat)
            item {
                MacroBentoGrid(summary = dailySummary)
            }

            // 4. Macro Ratio Distribution Bar
            item {
                MacroDistributionCard(summary = dailySummary)
            }

            // 5. Meals Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cooked & Logged Meals (${dayLogs.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )

                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = BentoTileSage
                    ) {
                        Text(
                            text = "${dailySummary.totalCalories} kcal Total",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoGreenPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // 6. List of Meals or Empty State
            if (dayLogs.isEmpty()) {
                item {
                    EmptyMealsBentoCard(
                        suggestedRecipes = suggestedRecipes,
                        onQuickLogRecipe = { recipe ->
                            viewModel.logRecipeMeal(recipe)
                        },
                        onOpenCustomLog = { showAddMealDialog = true }
                    )
                }
            } else {
                items(dayLogs, key = { it.id }) { log ->
                    MealLogItemCard(
                        mealLog = log,
                        onDelete = { viewModel.deleteMealLog(log) }
                    )
                }
            }

            // Bottom Spacer for FAB
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Goals Customization Dialog
    if (showGoalsDialog) {
        NutritionGoalsDialog(
            currentGoals = goals,
            onDismiss = { showGoalsDialog = false },
            onSave = { cal, p, c, f ->
                viewModel.updateNutritionGoals(cal, p, c, f)
                showGoalsDialog = false
            }
        )
    }

    // Add / Quick Log Meal Dialog
    if (showAddMealDialog) {
        LogMealDialog(
            suggestedRecipes = suggestedRecipes,
            onDismiss = { showAddMealDialog = false },
            onLogRecipe = { recipe, servings, mealType ->
                viewModel.logRecipeMeal(recipe, servings, mealType)
                showAddMealDialog = false
            },
            onLogCustom = { title, cal, p, c, f, servings, mealType ->
                viewModel.logCustomMeal(title, cal, p, c, f, servings, mealType)
                showAddMealDialog = false
            }
        )
    }
}

@Composable
private fun DateNavigator(
    currentDateString: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    val isToday = currentDateString == MealLog.getTodayDateString()
    val formattedDisplayDate = remember(currentDateString) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(currentDateString) ?: Date()
            val outSdf = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
            if (isToday) "Today, ${outSdf.format(date)}" else outSdf.format(date)
        } catch (e: Exception) {
            currentDateString
        }
    }

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
                    .testTag("prev_day_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Day",
                    tint = BentoTextPrimary
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isToday) BentoTileSage else BentoSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToday() }
                    .testTag("today_date_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = null,
                        tint = if (isToday) BentoGreenPrimary else BentoTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formattedDisplayDate,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isToday) BentoGreenPrimary else BentoTextPrimary
                    )
                }
            }

            IconButton(
                onClick = onNext,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("next_day_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Day",
                    tint = BentoTextPrimary
                )
            }
        }
    }
}

@Composable
private fun CalorieOverviewBentoCard(summary: DailyNutritionSummary) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurface),
        border = BorderStroke(1.dp, BentoBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calorie_overview_card")
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = BentoTileApricot,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = WarmSpice,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Energy Intake",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                        Text(
                            text = "Daily Calorie Target",
                            style = MaterialTheme.typography.labelSmall,
                            color = BentoTextMuted
                        )
                    }
                }

                // Calorie remaining or exceeded pill
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = if (summary.isCalorieOverGoal) MissingTagBg else BentoTileSage
                ) {
                    Text(
                        text = if (summary.isCalorieOverGoal) {
                            "+${summary.totalCalories - summary.targetCalories} kcal over"
                        } else {
                            "${summary.remainingCalories} kcal left"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.isCalorieOverGoal) MissingTagText else BentoGreenPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Calorie Counter numbers
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${summary.totalCalories}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = BentoTextPrimary
                )
                Text(
                    text = "/ ${summary.targetCalories} kcal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = BentoTextMuted,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Calorie Linear Progress
            LinearProgressIndicator(
                progress = { summary.caloriesProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(100.dp)),
                color = if (summary.isCalorieOverGoal) WarmSpice else BentoGreenPrimary,
                trackColor = BentoSurfaceVariant
            )
        }
    }
}

@Composable
private fun MacroBentoGrid(summary: DailyNutritionSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Protein Tile
        MacroBentoTile(
            label = "Protein",
            currentGrams = summary.totalProteinGrams,
            targetGrams = summary.targetProteinGrams,
            progress = summary.proteinProgress,
            accentColor = BentoGreenPrimary,
            containerColor = BentoTileSage,
            modifier = Modifier.weight(1f)
        )

        // 2. Carbs Tile
        MacroBentoTile(
            label = "Carbs",
            currentGrams = summary.totalCarbsGrams,
            targetGrams = summary.targetCarbsGrams,
            progress = summary.carbsProgress,
            accentColor = GoldenHoney,
            containerColor = BentoTileHoney,
            modifier = Modifier.weight(1f)
        )

        // 3. Fat Tile
        MacroBentoTile(
            label = "Fat",
            currentGrams = summary.totalFatGrams,
            targetGrams = summary.targetFatGrams,
            progress = summary.fatProgress,
            accentColor = WarmSpice,
            containerColor = BentoTileApricot,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MacroBentoTile(
    label: String,
    currentGrams: Int,
    targetGrams: Int,
    progress: Float,
    accentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurface),
        border = BorderStroke(1.dp, BentoBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        letterSpacing = 0.6.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = BentoTextMuted
                )

                Surface(
                    shape = CircleShape,
                    color = containerColor,
                    modifier = Modifier.size(10.dp)
                ) {}
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${currentGrams}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = BentoTextPrimary
                )
                Text(
                    text = "/${targetGrams}g",
                    style = MaterialTheme.typography.labelSmall,
                    color = BentoTextMuted,
                    modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(100.dp)),
                color = accentColor,
                trackColor = BentoSurfaceVariant
            )
        }
    }
}

@Composable
private fun MacroDistributionCard(summary: DailyNutritionSummary) {
    val totalMacroCalories = (summary.totalProteinGrams * 4) + (summary.totalCarbsGrams * 4) + (summary.totalFatGrams * 9)
    val pPct = if (totalMacroCalories > 0) ((summary.totalProteinGrams * 4f) / totalMacroCalories * 100).toInt() else 30
    val cPct = if (totalMacroCalories > 0) ((summary.totalCarbsGrams * 4f) / totalMacroCalories * 100).toInt() else 45
    val fPct = if (totalMacroCalories > 0) ((summary.totalFatGrams * 9f) / totalMacroCalories * 100).toInt() else 25

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurface),
        border = BorderStroke(1.dp, BentoBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Macro Ratio Distribution",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextPrimary
                )
                Text(
                    text = "$pPct% P • $cPct% C • $fPct% F",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Multi-color segmented ratio bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(BentoSurfaceVariant)
            ) {
                if (pPct > 0) {
                    Box(
                        modifier = Modifier
                            .weight(pPct.toFloat().coerceAtLeast(1f))
                            .fillMaxSize()
                            .background(BentoGreenPrimary)
                    )
                }
                if (cPct > 0) {
                    Box(
                        modifier = Modifier
                            .weight(cPct.toFloat().coerceAtLeast(1f))
                            .fillMaxSize()
                            .background(GoldenHoney)
                    )
                }
                if (fPct > 0) {
                    Box(
                        modifier = Modifier
                            .weight(fPct.toFloat().coerceAtLeast(1f))
                            .fillMaxSize()
                            .background(WarmSpice)
                    )
                }
            }
        }
    }
}

@Composable
private fun MealLogItemCard(
    mealLog: MealLog,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurface),
        border = BorderStroke(1.dp, BentoBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("meal_log_item_${mealLog.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = when (mealLog.mealType.lowercase()) {
                        "breakfast" -> BentoTileHoney
                        "lunch" -> BentoTileSage
                        "dinner" -> BentoTileApricot
                        else -> BentoSurfaceVariant
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = when (mealLog.mealType.lowercase()) {
                                "breakfast" -> GoldenHoney
                                "lunch" -> BentoGreenPrimary
                                "dinner" -> WarmSpice
                                else -> BentoTextSecondary
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = mealLog.recipeTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${mealLog.mealType} • ${mealLog.formattedTime}",
                            style = MaterialTheme.typography.labelSmall,
                            color = BentoTextMuted
                        )
                        if (mealLog.servings != 1.0f) {
                            Text(
                                text = "(${mealLog.servings} serv)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BentoGreenPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Macro pills
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "${mealLog.proteinGrams}g P",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = BentoGreenPrimary
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = BentoTextMuted
                        )
                        Text(
                            text = "${mealLog.carbsGrams}g C",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = GoldenHoney
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = BentoTextMuted
                        )
                        Text(
                            text = "${mealLog.fatGrams}g F",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = WarmSpice
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "+${mealLog.calories} kcal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = WarmSpice
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_meal_log_${mealLog.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Log",
                        tint = BentoTextMuted.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyMealsBentoCard(
    suggestedRecipes: List<Recipe>,
    onQuickLogRecipe: (Recipe) -> Unit,
    onOpenCustomLog: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurface),
        border = BorderStroke(1.dp, BentoBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = BentoTileSage,
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = BentoGreenPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "No meals logged for this day yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BentoTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "When you finish cooking a recipe in the app, it is automatically logged here! You can also quickly log below.",
                style = MaterialTheme.typography.bodySmall,
                color = BentoTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            if (suggestedRecipes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Quick Log from Available Recipes:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestedRecipes.take(3).forEach { recipe ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = BentoCardBg,
                            border = BorderStroke(1.dp, BentoBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onQuickLogRecipe(recipe) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = recipe.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoTextPrimary
                                    )
                                    Text(
                                        text = "${recipe.calories} kcal • ${recipe.proteinGrams}g Protein",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BentoTextMuted
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = BentoGreenPrimary
                                ) {
                                    Text(
                                        text = "+ Log",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
private fun NutritionGoalsDialog(
    currentGoals: NutritionGoals,
    onDismiss: () -> Unit,
    onSave: (calories: Int, protein: Int, carbs: Int, fat: Int) -> Unit
) {
    var calText by remember { mutableStateOf(currentGoals.targetCalories.toString()) }
    var proteinText by remember { mutableStateOf(currentGoals.targetProteinGrams.toString()) }
    var carbsText by remember { mutableStateOf(currentGoals.targetCarbsGrams.toString()) }
    var fatText by remember { mutableStateOf(currentGoals.targetFatGrams.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Personal Nutrition Goals",
                fontWeight = FontWeight.Bold,
                color = BentoTextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Set your daily target calories and macronutrients.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoTextSecondary
                )

                OutlinedTextField(
                    value = calText,
                    onValueChange = { calText = it },
                    label = { Text("Target Calories (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = proteinText,
                    onValueChange = { proteinText = it },
                    label = { Text("Protein (grams)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = carbsText,
                    onValueChange = { carbsText = it },
                    label = { Text("Carbohydrates (grams)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fatText,
                    onValueChange = { fatText = it },
                    label = { Text("Fats (grams)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cal = calText.toIntOrNull() ?: 2000
                    val p = proteinText.toIntOrNull() ?: 120
                    val c = carbsText.toIntOrNull() ?: 200
                    val f = fatText.toIntOrNull() ?: 65
                    onSave(cal, p, c, f)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BentoGreenPrimary)
            ) {
                Text("Save Goals")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LogMealDialog(
    suggestedRecipes: List<Recipe>,
    onDismiss: () -> Unit,
    onLogRecipe: (Recipe, Float, String) -> Unit,
    onLogCustom: (title: String, cal: Int, p: Int, c: Int, f: Int, servings: Float, mealType: String) -> Unit
) {
    var isCustomTab by remember { mutableStateOf(false) }

    // Custom meal state
    var customTitle by remember { mutableStateOf("") }
    var customCal by remember { mutableStateOf("350") }
    var customP by remember { mutableStateOf("25") }
    var customC by remember { mutableStateOf("30") }
    var customF by remember { mutableStateOf("12") }
    var customServings by remember { mutableFloatStateOf(1.0f) }
    var selectedMealType by remember { mutableStateOf(MealLog.determineMealType()) }

    // Selected recipe state
    var selectedRecipe by remember { mutableStateOf<Recipe?>(suggestedRecipes.firstOrNull()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Meal to Daily Intake",
                fontWeight = FontWeight.Bold,
                color = BentoTextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Tab Row (Recipe vs Custom)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BentoSurfaceVariant)
                        .padding(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (!isCustomTab) BentoSurface else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { isCustomTab = false }
                    ) {
                        Text(
                            text = "From Recipes",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (!isCustomTab) BentoGreenPrimary else BentoTextMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isCustomTab) BentoSurface else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { isCustomTab = true }
                    ) {
                        Text(
                            text = "Custom Dish",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isCustomTab) BentoGreenPrimary else BentoTextMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Meal Type selector chips
                Text(
                    text = "Meal Type:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Breakfast", "Lunch", "Dinner", "Snack").forEach { type ->
                        val isSel = selectedMealType.equals(type, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) BentoGreenPrimary else BentoSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedMealType = type }
                        ) {
                            Text(
                                text = type,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else BentoTextPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!isCustomTab) {
                    // Choose recipe
                    if (suggestedRecipes.isEmpty()) {
                        Text(
                            text = "No generated recipes available. Switch to Custom Dish to enter details.",
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoTextMuted
                        )
                    } else {
                        Text(
                            text = "Select Recipe:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            suggestedRecipes.take(4).forEach { recipe ->
                                val isSelected = selectedRecipe?.id == recipe.id
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) BentoTileSage else BentoCardBg,
                                    border = BorderStroke(1.dp, if (isSelected) BentoGreenPrimary else BentoBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedRecipe = recipe }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = recipe.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = BentoTextPrimary
                                            )
                                            Text(
                                                text = "${recipe.calories} kcal • ${recipe.proteinGrams}g P • ${recipe.carbsGrams}g C",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = BentoTextMuted
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = BentoGreenPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Custom Dish Fields
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customTitle,
                            onValueChange = { customTitle = it },
                            label = { Text("Dish / Meal Name") },
                            placeholder = { Text("e.g., Avocado Toast with Eggs") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = customCal,
                                onValueChange = { customCal = it },
                                label = { Text("Calories") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = customP,
                                onValueChange = { customP = it },
                                label = { Text("Protein (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = customC,
                                onValueChange = { customC = it },
                                label = { Text("Carbs (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = customF,
                                onValueChange = { customF = it },
                                label = { Text("Fat (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!isCustomTab) {
                        selectedRecipe?.let { r ->
                            onLogRecipe(r, 1.0f, selectedMealType)
                        }
                    } else {
                        if (customTitle.isNotBlank()) {
                            onLogCustom(
                                customTitle,
                                customCal.toIntOrNull() ?: 300,
                                customP.toIntOrNull() ?: 20,
                                customC.toIntOrNull() ?: 30,
                                customF.toIntOrNull() ?: 10,
                                customServings,
                                selectedMealType
                            )
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BentoGreenPrimary),
                enabled = if (!isCustomTab) selectedRecipe != null else customTitle.isNotBlank()
            ) {
                Text("Log to Intake")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
