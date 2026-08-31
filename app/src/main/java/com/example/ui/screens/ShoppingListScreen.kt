package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BudgetStatus
import com.example.model.CategorySpend
import com.example.model.GroceryBudgetSummary
import com.example.model.ShoppingItem
import com.example.ui.CulinaryViewModel
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoCardBg
import com.example.ui.theme.BentoCoralPrimary
import com.example.ui.theme.BentoGreenPrimary
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
import com.example.ui.theme.WarmSpice
import com.example.util.GroceryPriceEstimator
import java.util.Locale

@Composable
fun ShoppingListScreen(
    viewModel: CulinaryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val items by viewModel.shoppingItems.collectAsState()
    val budgetSummary by viewModel.groceryBudgetSummary.collectAsState()
    val weeklyBudget by viewModel.weeklyGroceryBudget.collectAsState()

    var showAddItemDialog by remember { mutableStateOf(false) }
    var showSetBudgetDialog by remember { mutableStateOf(false) }
    var customItemName by remember { mutableStateOf("") }
    var customItemAmount by remember { mutableStateOf("1") }
    var customCategory by remember { mutableStateOf("Fresh Produce") }

    val categories = listOf("Fresh Produce", "Dairy & Eggs", "Meat & Seafood", "Pantry & Oils", "Spices & Seasonings")

    val boughtCount = items.count { it.isBought }
    val totalCount = items.size

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BentoBackground)
            .testTag("shopping_list_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 1. Grocery Budget Tracker Bento Card
            GroceryBudgetTrackerCard(
                summary = budgetSummary,
                onEditBudgetClick = { showSetBudgetDialog = true },
                onShareClick = {
                    val shareText = buildString {
                        appendLine("🛒 Smart Fridge Grocery List & Budget:")
                        appendLine("Weekly Budget: ${GroceryPriceEstimator.formatCurrency(budgetSummary.weeklyBudget)}")
                        appendLine("Estimated Total: ${GroceryPriceEstimator.formatCurrency(budgetSummary.totalEstimatedCost)} (${budgetSummary.status.label})")
                        appendLine("----------------------------------")
                        items.forEach { item ->
                            val mark = if (item.isBought) "✓ [Bought] " else "☐ "
                            val cost = GroceryPriceEstimator.estimateItemCost(item.name, item.amount, item.category)
                            appendLine("$mark${item.name} (${item.amount}) - ~${GroceryPriceEstimator.formatCurrency(cost)}")
                        }
                    }
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Grocery List & Budget"))
                },
                onClearBoughtClick = { viewModel.clearBoughtItems() },
                hasBoughtItems = boughtCount > 0,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Shopping Items Content or Empty State
            if (items.isEmpty()) {
                EmptyShoppingListState(
                    weeklyBudget = weeklyBudget,
                    onAddItemClick = { showAddItemDialog = true },
                    onSetBudgetClick = { showSetBudgetDialog = true }
                )
            } else {
                // Category Grouped List
                val grouped = items.groupBy { it.category }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    grouped.forEach { (category, categoryItems) ->
                        val categorySubtotal = categoryItems.sumOf {
                            GroceryPriceEstimator.estimateItemCost(it.name, it.amount, it.category)
                        }

                        item(key = "header_$category") {
                            CategoryHeader(
                                category = category,
                                count = categoryItems.size,
                                subtotalCost = categorySubtotal
                            )
                        }

                        items(categoryItems, key = { it.id }) { item ->
                            val itemCost = GroceryPriceEstimator.estimateItemCost(item.name, item.amount, item.category)
                            val tier = GroceryPriceEstimator.getPriceTier(item.name, item.category)

                            ShoppingItemRow(
                                item = item,
                                estimatedCost = itemCost,
                                priceTier = tier,
                                onToggle = { viewModel.toggleShoppingItem(item) },
                                onDelete = { viewModel.deleteShoppingItem(item) }
                            )
                        }
                    }

                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(84.dp)) // Space for FAB
                    }
                }
            }
        }

        // Floating Action Button to Add Grocery Item
        FloatingActionButton(
            onClick = { showAddItemDialog = true },
            containerColor = BentoGreenPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_shopping_item_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Grocery Item")
        }
    }

    // Set Budget Dialog
    if (showSetBudgetDialog) {
        SetWeeklyBudgetDialog(
            currentBudget = weeklyBudget,
            onDismiss = { showSetBudgetDialog = false },
            onConfirm = { newBudget ->
                viewModel.setWeeklyBudget(newBudget)
                showSetBudgetDialog = false
            }
        )
    }

    // Add Item Dialog
    if (showAddItemDialog) {
        AddShoppingItemDialog(
            categories = categories,
            onDismiss = { showAddItemDialog = false },
            onAdd = { name, amount, category ->
                viewModel.addCustomShoppingItem(name, amount, category)
                showAddItemDialog = false
            }
        )
    }
}

/**
 * Grocery Budget Tracker Bento Card displaying budget health, stats, and tabs.
 */
@Composable
private fun GroceryBudgetTrackerCard(
    summary: GroceryBudgetSummary,
    onEditBudgetClick: () -> Unit,
    onShareClick: () -> Unit,
    onClearBoughtClick: () -> Unit,
    hasBoughtItems: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Overview, 1: Category Spend, 2: Smart Tips

    val statusContainerColor = when (summary.status) {
        BudgetStatus.WITHIN_BUDGET -> BentoTileSage
        BudgetStatus.APPROACHING_BUDGET -> BentoTileHoney
        BudgetStatus.OVER_BUDGET -> MissingTagBg
    }

    val statusTextColor = when (summary.status) {
        BudgetStatus.WITHIN_BUDGET -> BentoGreenPrimary
        BudgetStatus.APPROACHING_BUDGET -> GoldenHoney
        BudgetStatus.OVER_BUDGET -> MissingTagText
    }

    Surface(
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BentoBorder),
        shadowElevation = 1.dp,
        modifier = modifier.testTag("grocery_budget_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "GROCERY BUDGET TRACKER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.2.sp,
                                fontSize = 9.5.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = BentoTextMuted
                        )

                        // Status Chip
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = statusContainerColor
                        ) {
                            Text(
                                text = "${summary.status.emoji} ${summary.status.label}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Bold,
                                color = statusTextColor,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Estimated Cart Total",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Edit Budget button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BentoSurfaceVariant,
                        border = BorderStroke(1.dp, BentoBorder),
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onEditBudgetClick)
                            .testTag("edit_budget_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Weekly Budget",
                                tint = BentoTextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Share button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BentoSurfaceVariant,
                        border = BorderStroke(1.dp, BentoBorder),
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onShareClick)
                            .testTag("share_shopping_list_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Shopping List",
                                tint = BentoTextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Clear bought items button
                    if (hasBoughtItems) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BentoTileSage,
                            border = BorderStroke(1.dp, BentoGreenPrimary.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onClearBoughtClick)
                                .testTag("clear_bought_items_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Clear Purchased Items",
                                    tint = BentoGreenPrimary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4-Column Stat Tile Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BudgetStatPill(
                    label = "TARGET",
                    value = GroceryPriceEstimator.formatCurrency(summary.weeklyBudget),
                    bgColor = BentoSurfaceVariant,
                    textColor = BentoTextPrimary,
                    modifier = Modifier.weight(1f)
                )

                BudgetStatPill(
                    label = "EST. TOTAL",
                    value = GroceryPriceEstimator.formatCurrency(summary.totalEstimatedCost),
                    bgColor = if (summary.status == BudgetStatus.OVER_BUDGET) MissingTagBg else BentoTileSage,
                    textColor = if (summary.status == BudgetStatus.OVER_BUDGET) MissingTagText else BentoGreenPrimary,
                    modifier = Modifier.weight(1f)
                )

                BudgetStatPill(
                    label = "IN CART",
                    value = GroceryPriceEstimator.formatCurrency(summary.purchasedCost),
                    bgColor = BentoSurfaceVariant,
                    textColor = BentoTextSecondary,
                    modifier = Modifier.weight(1f)
                )

                val remColor = if (summary.remainingBudget >= 0) BentoGreenPrimary else MissingTagText
                val remBg = if (summary.remainingBudget >= 0) BentoSurfaceVariant else MissingTagBg
                BudgetStatPill(
                    label = if (summary.remainingBudget >= 0) "LEFT" else "OVER",
                    value = if (summary.remainingBudget >= 0) {
                        GroceryPriceEstimator.formatCurrency(summary.remainingBudget)
                    } else {
                        "+${GroceryPriceEstimator.formatCurrency(kotlin.math.abs(summary.remainingBudget))}"
                    },
                    bgColor = remBg,
                    textColor = remColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Multi-segment Budget Health Progress Indicator
            val animatedProgress by animateFloatAsState(
                targetValue = summary.percentUsed.coerceIn(0f, 1.2f),
                label = "budget_progress"
            )

            val progressColor = when (summary.status) {
                BudgetStatus.WITHIN_BUDGET -> BentoGreenPrimary
                BudgetStatus.APPROACHING_BUDGET -> GoldenHoney
                BudgetStatus.OVER_BUDGET -> MissingTagText
            }

            LinearProgressIndicator(
                progress = { animatedProgress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(100.dp)),
                color = progressColor,
                trackColor = BentoSurfaceVariant,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Progress Meta Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (summary.totalEstimatedCost == 0.0) {
                        "Add ingredients to estimate weekly grocery cost"
                    } else if (summary.remainingBudget >= 0) {
                        "${(summary.percentUsed * 100).toInt()}% of budget used • ${GroceryPriceEstimator.formatCurrency(summary.remainingBudget)} remaining"
                    } else {
                        "⚠️ Exceeded weekly target by ${GroceryPriceEstimator.formatCurrency(kotlin.math.abs(summary.remainingBudget))}"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = if (summary.remainingBudget < 0) MissingTagText else BentoTextSecondary,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "${(summary.percentUsed * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = progressColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Sub-tabs (Overview, Category Breakdown, Savings Tips)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BentoSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BudgetTabChip(
                    title = "Overview",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                BudgetTabChip(
                    title = "Categories",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
                BudgetTabChip(
                    title = "Savings Tips",
                    isSelected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.weight(1f)
                )
            }

            // Tab Content
            AnimatedVisibility(
                visible = selectedTab == 1,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (summary.categorySpends.isEmpty()) {
                        Text(
                            text = "No items added yet to break down spending.",
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoTextMuted,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    } else {
                        summary.categorySpends.forEach { catSpend ->
                            CategorySpendBar(spend = catSpend)
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = selectedTab == 2,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    summary.tips.forEach { tip ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = BentoCardBg,
                            border = BorderStroke(1.dp, BentoBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(text = tip.iconEmoji, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = tip.title,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoTextPrimary
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = BentoTileSage
                                        ) {
                                            Text(
                                                text = tip.potentialSavings,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                fontWeight = FontWeight.Bold,
                                                color = BentoGreenPrimary,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = tip.description,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                        color = BentoTextSecondary
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
private fun BudgetStatPill(
    label: String,
    value: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.sp,
                    letterSpacing = 0.8.sp
                ),
                fontWeight = FontWeight.Bold,
                color = BentoTextMuted
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.5.sp),
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
private fun BudgetTabChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) Color.White else Color.Transparent,
        border = if (isSelected) BorderStroke(1.dp, BentoBorder) else null,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) BentoTextPrimary else BentoTextMuted
            )
        }
    }
}

@Composable
private fun CategorySpendBar(spend: CategorySpend) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${spend.category} (${spend.itemCount} items)",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                fontWeight = FontWeight.SemiBold,
                color = BentoTextPrimary
            )

            Text(
                text = "${GroceryPriceEstimator.formatCurrency(spend.totalCost)} (${(spend.percentageOfTotal * 100).toInt()}%)",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                fontWeight = FontWeight.Bold,
                color = BentoGreenPrimary
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { spend.percentageOfTotal.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(100.dp)),
            color = when (spend.category) {
                "Meat & Seafood" -> WarmSpice
                "Fresh Produce" -> BentoGreenPrimary
                "Dairy & Eggs" -> GoldenHoney
                else -> BentoTextMuted
            },
            trackColor = BentoSurfaceVariant
        )
    }
}

@Composable
private fun CategoryHeader(
    category: String,
    count: Int,
    subtotalCost: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = category.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.2.sp,
                    fontSize = 10.sp
                ),
                fontWeight = FontWeight.Bold,
                color = BentoTextMuted
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = BentoTileSage
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = BentoGreenPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Text(
            text = "Est. Subtotal: ${GroceryPriceEstimator.formatCurrency(subtotalCost)}",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = BentoTextMuted,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ShoppingItemRow(
    item: ShoppingItem,
    estimatedCost: Double,
    priceTier: String,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onToggle)
            .testTag("shopping_item_${item.id}"),
        shape = RoundedCornerShape(18.dp),
        color = if (item.isBought) BentoSurfaceVariant.copy(alpha = 0.6f) else Color.White,
        border = BorderStroke(1.dp, if (item.isBought) BentoBorder.copy(alpha = 0.5f) else BentoBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Round Checkbox
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            if (item.isBought) BentoGreenPrimary else BentoSurfaceVariant,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.isBought) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Purchased",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
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
                            text = "${item.name} (${item.amount})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (item.isBought) FontWeight.Normal else FontWeight.Bold,
                            textDecoration = if (item.isBought) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (item.isBought) BentoTextMuted else BentoTextPrimary
                        )
                    }

                    if (!item.recipeSource.isNullOrBlank()) {
                        Text(
                            text = "For: ${item.recipeSource}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = BentoGreenPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Simulated Price Tag & Delete Action
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (item.isBought) BentoSurfaceVariant else BentoTileSage,
                    border = BorderStroke(1.dp, BentoBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "~${GroceryPriceEstimator.formatCurrency(estimatedCost)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (item.isBought) BentoTextMuted else BentoGreenPrimary
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete item",
                        tint = BentoTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Dialog to set or edit weekly budget target.
 */
@Composable
private fun SetWeeklyBudgetDialog(
    currentBudget: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var budgetText by remember { mutableStateOf(currentBudget.toInt().toString()) }
    val presets = listOf(40.0, 60.0, 75.0, 100.0, 120.0, 150.0, 200.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = null,
                    tint = BentoGreenPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Weekly Grocery Budget",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextPrimary
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Set your weekly target to track estimated shopping costs and get proactive cost-saving suggestions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BentoTextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Weekly Budget Amount ($)") },
                    leadingIcon = { Text("$", fontWeight = FontWeight.Bold, color = BentoGreenPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "QUICK PRESETS:",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontSize = 9.sp),
                    fontWeight = FontWeight.Bold,
                    color = BentoTextMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.take(4).forEach { amount ->
                        val isSel = budgetText.toDoubleOrNull() == amount
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) BentoTileSage else BentoSurfaceVariant,
                            border = if (isSel) BorderStroke(1.dp, BentoGreenPrimary) else BorderStroke(1.dp, BentoBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { budgetText = amount.toInt().toString() }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$${amount.toInt()}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) BentoGreenPrimary else BentoTextPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.drop(4).forEach { amount ->
                        val isSel = budgetText.toDoubleOrNull() == amount
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) BentoTileSage else BentoSurfaceVariant,
                            border = if (isSel) BorderStroke(1.dp, BentoGreenPrimary) else BorderStroke(1.dp, BentoBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { budgetText = amount.toInt().toString() }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$${amount.toInt()}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) BentoGreenPrimary else BentoTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = budgetText.toDoubleOrNull() ?: currentBudget
                    onConfirm(amount)
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BentoGreenPrimary,
                    contentColor = Color.White
                )
            ) {
                Text("Save Budget", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = BentoTextMuted)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

/**
 * Add Item Dialog with estimated price preview.
 */
@Composable
private fun AddShoppingItemDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("1") }
    var category by remember { mutableStateOf(categories.first()) }

    val previewCost = if (name.isNotBlank()) {
        GroceryPriceEstimator.estimateItemCost(name, amount, category)
    } else null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add to Shopping List",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BentoTextPrimary
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name (e.g. Olive Oil, Chicken Breast)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Quantity / Amount (e.g. 2 lbs, 500ml, 1 bottle)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Estimated Price Live Preview
                if (previewCost != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BentoTileSage,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Simulated Est. Price:",
                                style = MaterialTheme.typography.labelSmall,
                                color = BentoTextSecondary
                            )
                            Text(
                                text = "~${GroceryPriceEstimator.formatCurrency(previewCost)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = BentoGreenPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Text(
                    text = "CATEGORY:",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontSize = 9.sp),
                    fontWeight = FontWeight.Bold,
                    color = BentoTextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.forEach { cat ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (category == cat) BentoTileSage else BentoSurfaceVariant,
                            border = if (category == cat) BorderStroke(1.dp, BentoGreenPrimary) else BorderStroke(1.dp, BentoBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { category = cat }
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (category == cat) FontWeight.Bold else FontWeight.Medium,
                                color = if (category == cat) BentoGreenPrimary else BentoTextPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(name.trim(), amount.trim(), category)
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BentoGreenPrimary,
                    contentColor = Color.White
                )
            ) {
                Text("Add Item", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = BentoTextMuted)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
private fun EmptyShoppingListState(
    weeklyBudget: Double,
    onAddItemClick: () -> Unit,
    onSetBudgetClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = BentoTileSage,
            modifier = Modifier.size(76.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = BentoGreenPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Shopping List is Empty",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BentoTextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Your weekly budget is set to ${GroceryPriceEstimator.formatCurrency(weeklyBudget)}. Missing recipe ingredients or custom items will be dynamically cost-estimated as you add them.",
            style = MaterialTheme.typography.bodyMedium,
            color = BentoTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onAddItemClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BentoGreenPrimary,
                    contentColor = Color.White
                )
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Item", fontWeight = FontWeight.Bold)
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BentoSurfaceVariant,
                border = BorderStroke(1.dp, BentoBorder),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onSetBudgetClick)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = BentoGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Edit Budget",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                }
            }
        }
    }
}
