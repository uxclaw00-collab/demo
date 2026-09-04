package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AllergenType
import com.example.model.DetectedAllergen
import com.example.model.IngredientNutritionDetail
import com.example.model.ParsedRecipeNutrition
import com.example.model.Recipe
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoCardBg
import com.example.ui.theme.BentoCoralPrimary
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
import com.example.util.NutritionParser

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeNutritionSection(
    recipe: Recipe,
    modifier: Modifier = Modifier
) {
    val nutrition = remember(recipe) { NutritionParser.parseRecipe(recipe) }
    var isExpanded by remember { mutableStateOf(false) }
    var showFullDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(BentoSurfaceVariant.copy(alpha = 0.6f))
            .border(BorderStroke(1.dp, BentoBorder), RoundedCornerShape(18.dp))
            .padding(12.dp)
            .testTag("recipe_nutrition_section_${recipe.id}")
    ) {
        // Section Header: Nutrition & Allergen Parser Summary Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .testTag("toggle_nutrition_parser_${recipe.id}"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = BentoTileSage,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "Nutrition Parser",
                            tint = BentoGreenPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Text(
                    text = "NUTRITION & ALLERGEN PARSER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.1.sp,
                        fontSize = 9.5.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = BentoGreenPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BentoBorder),
                    modifier = Modifier
                        .clickable { showFullDialog = true }
                        .testTag("open_nutrition_facts_dialog_${recipe.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Facts Table",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Bold,
                            color = BentoTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse Nutrition" else "Expand Nutrition",
                    tint = BentoTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Macro Ratio Segmented Visual Bar
        MacroRatioSegmentedBar(
            proteinRatio = nutrition.proteinRatioPercent,
            carbsRatio = nutrition.carbsRatioPercent,
            fatRatio = nutrition.fatRatioPercent
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Allergen Status Alert / Shield Row
        if (nutrition.detectedAllergens.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MissingTagBg,
                border = BorderStroke(1.dp, WarmSpice.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Allergens Detected",
                        tint = MissingTagText,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Allergen Warning:",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = MissingTagText
                        )
                        Text(
                            text = nutrition.detectedAllergens.joinToString(" • ") { allergen ->
                                "${allergen.type.displayName} (${allergen.triggerIngredients.joinToString(", ")})"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = BentoTextPrimary,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SuccessTagBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Allergen Safe",
                        tint = SuccessTagText,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Allergen Safe: " + (if (nutrition.allergenFreeTags.isNotEmpty()) nutrition.allergenFreeTags.joinToString(" • ") else "No common allergens detected"),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        color = SuccessTagText
                    )
                }
            }
        }

        // Expanded Nutrition Breakdown & Details
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                // Key Micronutrients & Dietary Stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MiniNutritionPill(
                        label = "Fiber",
                        value = "${String.format("%.1f", nutrition.fiberGramsPerServing)}g",
                        sub = "Digestive",
                        modifier = Modifier.weight(1f)
                    )
                    MiniNutritionPill(
                        label = "Net Carbs",
                        value = "${String.format("%.1f", nutrition.netCarbsGramsPerServing)}g",
                        sub = "Carb Impact",
                        modifier = Modifier.weight(1f)
                    )
                    MiniNutritionPill(
                        label = "Sodium",
                        value = "${nutrition.sodiumMgPerServing}mg",
                        sub = "Electrolytes",
                        modifier = Modifier.weight(1f)
                    )
                    MiniNutritionPill(
                        label = "Sugar",
                        value = "${String.format("%.1f", nutrition.sugarGramsPerServing)}g",
                        sub = "Natural",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Health & Dietary Badges
                if (nutrition.healthBadges.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        nutrition.healthBadges.forEach { badge ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, BentoGreenPrimary.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = badge.emoji, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = badge.label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = BentoGreenPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Ingredient Nutrient Breakdown List
                Text(
                    text = "INGREDIENT NUTRIENT ESTIMATES:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.8.sp,
                        fontSize = 9.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = BentoTextMuted
                )

                Spacer(modifier = Modifier.height(4.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    nutrition.ingredientDetails.forEach { ing ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(1.dp, BentoBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ing.ingredientName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = BentoTextPrimary
                                )
                                Text(
                                    text = ing.portionDescription,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                                    color = BentoTextMuted
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${ing.estimatedCalories} kcal",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = WarmSpice
                                )
                                Text(
                                    text = "P: ${ing.proteinGrams.toInt()}g | C: ${ing.carbsGrams.toInt()}g | F: ${ing.fatGrams.toInt()}g",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = BentoTextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Full Facts Table Button
                TextButton(
                    onClick = { showFullDialog = true },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = BentoGreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "View Detailed Nutrition Facts & Micronutrients",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoGreenPrimary
                    )
                }
            }
        }
    }

    // Full Nutrition Facts & Allergen Breakdown Dialog
    if (showFullDialog) {
        NutritionFactsModalDialog(
            recipe = recipe,
            nutrition = nutrition,
            onDismiss = { showFullDialog = false }
        )
    }
}

@Composable
private fun MacroRatioSegmentedBar(
    proteinRatio: Int,
    carbsRatio: Int,
    fatRatio: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(BentoGreenPrimary)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Protein $proteinRatio%",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                    fontWeight = FontWeight.Bold,
                    color = BentoGreenPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(GoldenHoney)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Carbs $carbsRatio%",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                    fontWeight = FontWeight.Bold,
                    color = GoldenHoney
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(WarmSpice)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Fat $fatRatio%",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                    fontWeight = FontWeight.Bold,
                    color = WarmSpice
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Visual Multi-Color Proportion Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(BentoBorder)
        ) {
            if (proteinRatio > 0) {
                Box(
                    modifier = Modifier
                        .weight(proteinRatio.toFloat().coerceAtLeast(0.1f))
                        .fillMaxWidth()
                        .background(BentoGreenPrimary)
                )
            }
            if (carbsRatio > 0) {
                Box(
                    modifier = Modifier
                        .weight(carbsRatio.toFloat().coerceAtLeast(0.1f))
                        .fillMaxWidth()
                        .background(GoldenHoney)
                )
            }
            if (fatRatio > 0) {
                Box(
                    modifier = Modifier
                        .weight(fatRatio.toFloat().coerceAtLeast(0.1f))
                        .fillMaxWidth()
                        .background(WarmSpice)
                )
            }
        }
    }
}

@Composable
private fun MiniNutritionPill(
    label: String,
    value: String,
    sub: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BentoBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 0.4.sp),
                fontWeight = FontWeight.Bold,
                color = BentoTextMuted
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                fontWeight = FontWeight.Bold,
                color = BentoTextPrimary
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.5.sp),
                color = BentoTextMuted
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NutritionFactsModalDialog(
    recipe: Recipe,
    nutrition: ParsedRecipeNutrition,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Nutrition Facts & Allergens",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoGreenPrimary,
                        maxLines = 1
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Dialog",
                        tint = BentoTextMuted
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Serving Info Header
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BentoSurfaceVariant,
                    border = BorderStroke(1.dp, BentoBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SERVING SIZE",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 1.sp),
                                fontWeight = FontWeight.Bold,
                                color = BentoTextMuted
                            )
                            Text(
                                text = "1 portion (Recipe makes ${nutrition.servings})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "CALORIES",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 1.sp),
                                fontWeight = FontWeight.Bold,
                                color = BentoTextMuted
                            )
                            Text(
                                text = "${nutrition.caloriesPerServing} kcal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = WarmSpice
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Modern Nutrition Facts Table
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BentoBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "MACRONUTRIENT BREAKDOWN",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontSize = 9.5.sp),
                            fontWeight = FontWeight.Bold,
                            color = BentoGreenPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        NutritionRow(
                            name = "Total Fat",
                            value = "${String.format("%.1f", nutrition.fatGramsPerServing)}g",
                            percentDv = "${((nutrition.fatGramsPerServing / 65.0) * 100).toInt()}% DV",
                            isBold = true
                        )
                        NutritionRow(
                            name = "• Saturated Fat",
                            value = "${String.format("%.1f", (nutrition.fatGramsPerServing * 0.28))}g",
                            percentDv = "${((nutrition.fatGramsPerServing * 0.28 / 20.0) * 100).toInt()}% DV",
                            isIndented = true
                        )
                        NutritionRow(
                            name = "Total Carbohydrates",
                            value = "${String.format("%.1f", nutrition.carbsGramsPerServing)}g",
                            percentDv = "${((nutrition.carbsGramsPerServing / 275.0) * 100).toInt()}% DV",
                            isBold = true
                        )
                        NutritionRow(
                            name = "• Dietary Fiber",
                            value = "${String.format("%.1f", nutrition.fiberGramsPerServing)}g",
                            percentDv = "${((nutrition.fiberGramsPerServing / 28.0) * 100).toInt()}% DV",
                            isIndented = true
                        )
                        NutritionRow(
                            name = "• Total Sugars",
                            value = "${String.format("%.1f", nutrition.sugarGramsPerServing)}g",
                            percentDv = "—",
                            isIndented = true
                        )
                        NutritionRow(
                            name = "• Net Carbs",
                            value = "${String.format("%.1f", nutrition.netCarbsGramsPerServing)}g",
                            percentDv = "Keto / Low-Carb metric",
                            isIndented = true
                        )
                        NutritionRow(
                            name = "Protein",
                            value = "${String.format("%.1f", nutrition.proteinGramsPerServing)}g",
                            percentDv = "${((nutrition.proteinGramsPerServing / 50.0) * 100).toInt()}% DV",
                            isBold = true
                        )
                        NutritionRow(
                            name = "Sodium",
                            value = "${nutrition.sodiumMgPerServing}mg",
                            percentDv = "${((nutrition.sodiumMgPerServing / 2300.0) * 100).toInt()}% DV",
                            isBold = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Micronutrients Highlights with Progress Bars
                Text(
                    text = "MICRONUTRIENTS & MINERALS (% DV)",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontSize = 9.5.sp),
                    fontWeight = FontWeight.Bold,
                    color = BentoTextMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    nutrition.micronutrientHighlights.forEach { micro ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BentoSurfaceVariant,
                            border = BorderStroke(1.dp, BentoBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = micro.iconEmoji, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = micro.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoTextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "(${micro.amount})",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = BentoTextMuted
                                        )
                                    }

                                    Text(
                                        text = "${micro.dailyValuePercent}% DV",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoGreenPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                LinearProgressIndicator(
                                    progress = { (micro.dailyValuePercent / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = BentoGreenPrimary,
                                    trackColor = BentoBorder,
                                    strokeCap = StrokeCap.Round
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = micro.benefit,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                                    color = BentoTextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Allergen Information & Safe Tags
                Text(
                    text = "ALLERGEN COMPLIANCE & SAFETY",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontSize = 9.5.sp),
                    fontWeight = FontWeight.Bold,
                    color = BentoTextMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (nutrition.detectedAllergens.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        nutrition.detectedAllergens.forEach { allergen ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MissingTagBg,
                                border = BorderStroke(1.dp, WarmSpice.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(text = allergen.type.iconEmoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = allergen.type.displayName,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MissingTagText
                                        )
                                        Text(
                                            text = "Triggered by: " + allergen.triggerIngredients.joinToString(", "),
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            fontWeight = FontWeight.Medium,
                                            color = BentoTextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = allergen.type.description,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = BentoTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SuccessTagBg,
                        border = BorderStroke(1.dp, BentoGreenPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessTagText,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Zero FDA Major Allergens detected in recipe ingredients.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = SuccessTagText
                            )
                        }
                    }
                }

                if (nutrition.allergenFreeTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        nutrition.allergenFreeTags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = BentoTileSage
                            ) {
                                Text(
                                    text = "✓ $tag",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = BentoGreenPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BentoGreenPrimary,
                    contentColor = Color.White
                )
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun NutritionRow(
    name: String,
    value: String,
    percentDv: String,
    isBold: Boolean = false,
    isIndented: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            else MaterialTheme.typography.bodySmall,
            color = BentoTextPrimary,
            modifier = Modifier.padding(start = if (isIndented) 12.dp else 0.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                else MaterialTheme.typography.bodySmall,
                color = BentoTextPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = percentDv,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = BentoTextMuted
            )
        }
    }
}
