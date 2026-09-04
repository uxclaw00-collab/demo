package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PantryItem
import com.example.model.PlannedMeal
import com.example.ui.theme.BentoBackground
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
import com.example.util.AggregatedIngredientItem
import com.example.util.MealPlanAggregator

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AggregatedIngredientsSheet(
    allWeekMeals: List<PlannedMeal>,
    pantryItems: List<PantryItem>,
    onAddItemsToShoppingList: (List<AggregatedIngredientItem>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Selection of meals to aggregate from (defaults to all planned meals in the week)
    val selectedMealIds = remember(allWeekMeals) {
        mutableStateMapOf<Long, Boolean>().apply {
            allWeekMeals.forEach { put(it.id, true) }
        }
    }

    val selectedMeals by remember(allWeekMeals, selectedMealIds) {
        derivedStateOf {
            allWeekMeals.filter { selectedMealIds[it.id] == true }
        }
    }

    // Filter view: "All", "Only Needed to Buy", "Already In Pantry"
    var activeFilter by remember { mutableStateOf("ALL") }

    val aggregatedSummary by remember(selectedMeals, pantryItems) {
        derivedStateOf {
            MealPlanAggregator.aggregateIngredients(selectedMeals, pantryItems)
        }
    }

    // Individual item check state for export to shopping list
    val itemExportSelection = remember(aggregatedSummary) {
        mutableStateMapOf<String, Boolean>().apply {
            aggregatedSummary.allItems.forEach { item ->
                put(item.name, item.isNeeded)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BentoBackground,
        modifier = Modifier.testTag("aggregated_ingredients_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = BentoTileSage,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = BentoGreenPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Aggregated Ingredients",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                        Text(
                            text = "Summarized grocery list from ${selectedMeals.size} selected meals",
                            style = MaterialTheme.typography.labelSmall,
                            color = BentoTextMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Copy to clipboard button
                    IconButton(
                        onClick = {
                            val text = aggregatedSummary.toShareableText()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Weekly Meal Plan Grocery List", text)
                            clipboard.setPrimaryClip(clip)
                        },
                        modifier = Modifier.size(36.dp).testTag("copy_grocery_list_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy List",
                            tint = BentoGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = BentoTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Meal Selection Chips Section
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                border = BorderStroke(1.dp, BentoBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SELECTED MEALS (${selectedMeals.size}/${allWeekMeals.size})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                letterSpacing = 1.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = BentoTextMuted
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Select All",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.Bold,
                                color = BentoGreenPrimary,
                                modifier = Modifier
                                    .clickable {
                                        allWeekMeals.forEach { selectedMealIds[it.id] = true }
                                    }
                                    .padding(4.dp)
                            )
                            Text(
                                text = "Clear",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.Bold,
                                color = BentoTextMuted,
                                modifier = Modifier
                                    .clickable {
                                        allWeekMeals.forEach { selectedMealIds[it.id] = false }
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (allWeekMeals.isEmpty()) {
                        Text(
                            text = "No meals planned this week. Add meals to the planner to view aggregated ingredients.",
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoTextMuted
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(allWeekMeals, key = { it.id }) { meal ->
                                val isSelected = selectedMealIds[meal.id] == true
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) BentoTileSage else BentoSurfaceVariant,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) BentoGreenPrimary else BentoBorder
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            selectedMealIds[meal.id] = !isSelected
                                        }
                                        .testTag("meal_toggle_chip_${meal.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = BentoGreenPrimary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = "${meal.dayOfWeek.take(3)} ${meal.mealSlot.take(1)}: ${meal.recipeTitle.take(16)}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) BentoGreenPrimary else BentoTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Aggregation Overview Bento Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Needed to Buy
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BentoTileApricot,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { activeFilter = if (activeFilter == "NEEDED") "ALL" else "NEEDED" }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${aggregatedSummary.neededToBuyCount}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = WarmSpice
                        )
                        Text(
                            text = "Needed to Buy",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = WarmSpice
                        )
                    }
                }

                // In Pantry Stock
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BentoTileSage,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { activeFilter = if (activeFilter == "PANTRY") "ALL" else "PANTRY" }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${aggregatedSummary.inPantryCount}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = BentoGreenPrimary
                        )
                        Text(
                            text = "In Pantry",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = BentoGreenPrimary
                        )
                    }
                }

                // Total Unique
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BentoSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { activeFilter = "ALL" }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${aggregatedSummary.totalUniqueIngredientsCount}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = BentoTextPrimary
                        )
                        Text(
                            text = "Total Ingredients",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = BentoTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Tabs (All / Needed Only / Pantry Only)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = activeFilter == "ALL",
                    onClick = { activeFilter = "ALL" },
                    label = { Text("All (${aggregatedSummary.allItems.size})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BentoGreenPrimary,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(100.dp)
                )

                FilterChip(
                    selected = activeFilter == "NEEDED",
                    onClick = { activeFilter = "NEEDED" },
                    label = { Text("🛒 Missing (${aggregatedSummary.neededToBuyCount})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WarmSpice,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(100.dp)
                )

                FilterChip(
                    selected = activeFilter == "PANTRY",
                    onClick = { activeFilter = "PANTRY" },
                    label = { Text("✅ In Pantry (${aggregatedSummary.inPantryCount})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BentoGreenPrimary,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(100.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Aggregated Ingredients Categorized List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedMeals.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestaurantMenu,
                                contentDescription = null,
                                tint = BentoTextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Select at least one planned meal above to view aggregated ingredients.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BentoTextMuted,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                } else {
                    aggregatedSummary.categories.forEach { categoryGroup ->
                        val displayedItems = categoryGroup.items.filter { item ->
                            when (activeFilter) {
                                "NEEDED" -> item.isNeeded
                                "PANTRY" -> item.isInPantry
                                else -> true
                            }
                        }

                        if (displayedItems.isNotEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, BentoBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        // Category Heading
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = categoryGroup.iconEmoji, fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = categoryGroup.category,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BentoTextPrimary
                                                )
                                            }

                                            Text(
                                                text = "${displayedItems.size} items",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = BentoTextMuted
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Item Rows
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            displayedItems.forEach { item ->
                                                val isChecked = itemExportSelection[item.name] ?: item.isNeeded
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = if (item.isInPantry) SuccessTagBg.copy(alpha = 0.35f) else BentoCardBg,
                                                    border = BorderStroke(
                                                        1.dp,
                                                        if (item.isInPantry) BentoGreenPrimary.copy(alpha = 0.2f) else BentoBorder
                                                    ),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(8.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Checkbox(
                                                                checked = isChecked,
                                                                onCheckedChange = { checked ->
                                                                    itemExportSelection[item.name] = checked
                                                                },
                                                                colors = CheckboxDefaults.colors(
                                                                    checkedColor = BentoGreenPrimary,
                                                                    checkmarkColor = Color.White
                                                                ),
                                                                modifier = Modifier.size(24.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Column {
                                                                Text(
                                                                    text = item.name,
                                                                    style = MaterialTheme.typography.bodyMedium,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = BentoTextPrimary
                                                                )
                                                                Text(
                                                                    text = "Needed in: ${item.recipeSources.joinToString(", ")}",
                                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                                    color = BentoTextMuted,
                                                                    maxLines = 2
                                                                )
                                                            }
                                                        }

                                                        // Status Badge
                                                        if (item.isInPantry) {
                                                            Surface(
                                                                shape = RoundedCornerShape(8.dp),
                                                                color = SuccessTagBg
                                                            ) {
                                                                Row(
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Check,
                                                                        contentDescription = null,
                                                                        tint = SuccessTagText,
                                                                        modifier = Modifier.size(11.dp)
                                                                    )
                                                                    Spacer(modifier = Modifier.width(3.dp))
                                                                    Text(
                                                                        text = item.pantryQuantity?.let { "In Stock ($it)" } ?: "In Stock",
                                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = SuccessTagText
                                                                    )
                                                                }
                                                            }
                                                        } else {
                                                            Surface(
                                                                shape = RoundedCornerShape(8.dp),
                                                                color = MissingTagBg
                                                            ) {
                                                                Row(
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.ShoppingCart,
                                                                        contentDescription = null,
                                                                        tint = MissingTagText,
                                                                        modifier = Modifier.size(10.dp)
                                                                    )
                                                                    Spacer(modifier = Modifier.width(3.dp))
                                                                    Text(
                                                                        text = "To Buy",
                                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = MissingTagText
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
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Bottom Action Bar: Add Selected to Shopping List
            val checkedItems = remember(aggregatedSummary, itemExportSelection) {
                aggregatedSummary.allItems.filter { itemExportSelection[it.name] == true }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                color = BentoBackground
            ) {
                Button(
                    onClick = {
                        onAddItemsToShoppingList(checkedItems)
                        onDismiss()
                    },
                    enabled = checkedItems.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("add_aggregated_to_shopping_list_btn"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BentoGreenPrimary,
                        disabledContainerColor = BentoSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add ${checkedItems.size} Selected Items to Shopping Bento",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
