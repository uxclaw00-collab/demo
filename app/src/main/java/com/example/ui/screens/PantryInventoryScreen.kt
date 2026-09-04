package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PantryItem
import com.example.ui.AppScreen
import com.example.ui.CulinaryViewModel
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
import com.example.ui.theme.FreshSage
import com.example.ui.theme.GoldenHoney
import com.example.ui.theme.MissingTagBg
import com.example.ui.theme.MissingTagText
import com.example.ui.theme.SuccessTagBg
import com.example.ui.theme.SuccessTagText
import com.example.ui.theme.WarmSpice
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PantryInventoryScreen(
    viewModel: CulinaryViewModel,
    modifier: Modifier = Modifier
) {
    val pantryItems by viewModel.pantryItems.collectAsState()
    val suggestedRecipes by viewModel.suggestedRecipes.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<PantryItem?>(null) }

    val categories = listOf(
        "All",
        "Expiring Soon",
        "Oils & Vinegars",
        "Dairy & Refrigerated",
        "Spices & Seasonings",
        "Baking & Grains",
        "Condiments & Sauces",
        "Pantry Staples"
    )

    val expiringCount = pantryItems.count { it.isExpiringSoon || it.isExpired }

    val filteredItems = pantryItems.filter { item ->
        val matchesSearch = item.name.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true)
        val matchesCategory = when (selectedCategoryFilter) {
            "All" -> true
            "Expiring Soon" -> item.isExpiringSoon || item.isExpired
            else -> item.category.equals(selectedCategoryFilter, ignoreCase = true)
        }
        matchesSearch && matchesCategory
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BentoBackground)
            .testTag("pantry_inventory_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            // Bento Header Tile: Pantry Summary & Expiration Alert
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color.White,
                border = BorderStroke(1.dp, BentoBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "STAPLE INVENTORY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.4.sp,
                                    fontSize = 9.sp
                                ),
                                fontWeight = FontWeight.Bold,
                                color = BentoTextMuted
                            )
                            Text(
                                text = "Pantry Inventory",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = BentoTileSage
                        ) {
                            Text(
                                text = "${pantryItems.size} items in stock",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = BentoGreenPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Cross-referenced with smart recipes to accurately detect what you have on hand vs grocery needs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoTextSecondary,
                        lineHeight = 18.sp
                    )

                    // Expiring Alert Tile
                    if (expiringCount > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = BentoTileApricot,
                            border = BorderStroke(1.dp, WarmSpice.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedCategoryFilter = "Expiring Soon" }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Alarm,
                                        contentDescription = null,
                                        tint = WarmSpice,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "$expiringCount items expiring soon",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoTextPrimary
                                        )
                                        Text(
                                            text = "Tap to review & use in today's recipes",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                            color = BentoTextSecondary
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White
                                ) {
                                    Text(
                                        text = "View Items",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = WarmSpice,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search and Category Filter Row
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search pantry items (e.g. Olive oil, Rice)...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = BentoTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pantry_search_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = if (isSelected) BentoTileSage else Color.White,
                        border = BorderStroke(1.dp, if (isSelected) BentoGreenPrimary else BentoBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .clickable { selectedCategoryFilter = cat }
                            .testTag("pantry_filter_$cat")
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) BentoGreenPrimary else BentoTextPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Items List
            if (filteredItems.isEmpty()) {
                EmptyPantryState(
                    onAddClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        val matchingRecipesCount = suggestedRecipes.count { recipe ->
                            val combined = recipe.matchedIngredients + recipe.missingIngredients
                            combined.any { it.contains(item.name, ignoreCase = true) || item.name.contains(it, ignoreCase = true) }
                        }

                        PantryItemCard(
                            item = item,
                            recipeUsageCount = matchingRecipesCount,
                            onEdit = { itemToEdit = item },
                            onDelete = { viewModel.deletePantryItem(item) },
                            onFindRecipes = {
                                viewModel.addCustomIngredient(item.name, item.category)
                                viewModel.navigateTo(AppScreen.FRIDGE_RECIPES)
                            },
                            onAddToList = {
                                viewModel.addCustomShoppingItem(item.name, item.quantity, item.category)
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(84.dp))
                    }
                }
            }
        }

        // Floating Action Button to Add Pantry Item
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = BentoGreenPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_pantry_item_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Pantry Item")
        }
    }

    // Add or Edit Pantry Dialog
    if (showAddDialog || itemToEdit != null) {
        PantryItemDialog(
            existingItem = itemToEdit,
            onDismiss = {
                showAddDialog = false
                itemToEdit = null
            },
            onSave = { name, quantity, category, expiryMillis ->
                if (itemToEdit != null) {
                    viewModel.updatePantryItem(
                        itemToEdit!!.copy(
                            name = name,
                            quantity = quantity,
                            category = category,
                            expirationDateMillis = expiryMillis
                        )
                    )
                } else {
                    viewModel.addPantryItem(name, quantity, category, expiryMillis)
                }
                showAddDialog = false
                itemToEdit = null
            }
        )
    }
}

@Composable
private fun PantryItemCard(
    item: PantryItem,
    recipeUsageCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onFindRecipes: () -> Unit,
    onAddToList: () -> Unit
) {
    val expiryText = formatExpiration(item.expirationDateMillis)
    val isAlert = item.isExpired || item.isExpiringSoon

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (isAlert) WarmSpice.copy(alpha = 0.5f) else BentoBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pantry_card_${item.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )

                        // Category tag
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BentoSurfaceVariant
                        ) {
                            Text(
                                text = item.category,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = BentoTextMuted,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Quantity
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = BentoGreenPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = item.quantity,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoTextSecondary
                            )
                        }

                        // Expiration
                        if (item.expirationDateMillis != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isAlert) Icons.Default.Warning else Icons.Default.Event,
                                    contentDescription = null,
                                    tint = if (item.isExpired) Color.Red else if (item.isExpiringSoon) WarmSpice else BentoGreenPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = expiryText,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    fontWeight = if (isAlert) FontWeight.Bold else FontWeight.Medium,
                                    color = if (item.isExpired) Color.Red else if (item.isExpiringSoon) WarmSpice else BentoTextMuted
                                )
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Item",
                            tint = BentoTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Item",
                            tint = BentoTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom action row: Cross-reference stats & quick actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BentoSurfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cross Reference with recipes indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        tint = BentoGreenPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (recipeUsageCount > 0) "Used in $recipeUsageCount recipes" else "Cross-referenced in inventory",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Medium,
                        color = BentoGreenPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Quick add to shopping if low
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, BentoBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onAddToList)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddShoppingCart,
                                contentDescription = null,
                                tint = BentoTextPrimary,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Restock",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Bold,
                                color = BentoTextPrimary
                            )
                        }
                    }

                    // Cook with this
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BentoTileSage,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onFindRecipes)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Find Recipes",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Bold,
                                color = BentoGreenPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PantryItemDialog(
    existingItem: PantryItem?,
    onDismiss: () -> Unit,
    onSave: (name: String, quantity: String, category: String, expiryMillis: Long?) -> Unit
) {
    var name by remember { mutableStateOf(existingItem?.name ?: "") }
    var quantity by remember { mutableStateOf(existingItem?.quantity ?: "1 item") }
    var category by remember { mutableStateOf(existingItem?.category ?: "Pantry Staples") }
    var selectedExpiryDays by remember { mutableStateOf<Int?>(null) }

    val categories = listOf(
        "Pantry Staples",
        "Oils & Vinegars",
        "Dairy & Refrigerated",
        "Spices & Seasonings",
        "Baking & Grains",
        "Condiments & Sauces",
        "Canned Goods",
        "Produce"
    )

    val quickExpirations = listOf(
        Pair("3 Days", 3),
        Pair("1 Week", 7),
        Pair("2 Weeks", 14),
        Pair("1 Month", 30),
        Pair("3 Months", 90),
        Pair("6 Months", 180),
        Pair("1 Year", 365)
    )

    val parsedPantryItems = remember(name) {
        if (name.isBlank()) emptyList()
        else name.split(",", "\n", ";")
            .map { it.trim().trim('•', '-', '*', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '.').trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingItem != null) "Edit Pantry Item" else "Add Pantry Items",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BentoTextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name(s) (e.g. Olive Oil, Garlic Powder, Rice)") },
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pantry_dialog_name_input")
                )

                if (parsedPantryItems.size > 1) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Adding ${parsedPantryItems.size} items:",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        color = BentoGreenPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        parsedPantryItems.forEach { item ->
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = BentoTileSage,
                                border = BorderStroke(1.dp, BentoGreenPrimary.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.SemiBold,
                                    color = BentoGreenPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity / Pack Size (e.g. 500ml, 2 cans, 1 pack)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "CATEGORY:",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontSize = 9.sp),
                    fontWeight = FontWeight.Bold,
                    color = BentoTextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.forEach { cat ->
                        val isSel = category == cat
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = if (isSel) BentoTileSage else BentoSurfaceVariant,
                            border = BorderStroke(1.dp, if (isSel) BentoGreenPrimary else BentoBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .clickable { category = cat }
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) BentoGreenPrimary else BentoTextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "EXPIRATION ESTIMATE:",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontSize = 9.sp),
                    fontWeight = FontWeight.Bold,
                    color = BentoTextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    quickExpirations.forEach { (label, days) ->
                        val isSel = selectedExpiryDays == days
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = if (isSel) BentoTileApricot else BentoSurfaceVariant,
                            border = BorderStroke(1.dp, if (isSel) WarmSpice else BentoBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .clickable { selectedExpiryDays = if (isSel) null else days }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) WarmSpice else BentoTextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
                        val expiryMillis = selectedExpiryDays?.let {
                            System.currentTimeMillis() + (it * 24L * 60 * 60 * 1000)
                        } ?: existingItem?.expirationDateMillis
                        onSave(name.trim(), quantity.trim(), category, expiryMillis)
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BentoGreenPrimary,
                    contentColor = Color.White
                )
            ) {
                Text("Save to Pantry", fontWeight = FontWeight.Bold)
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
private fun EmptyPantryState(onAddClick: () -> Unit) {
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
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = BentoGreenPrimary,
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Your Pantry is Empty",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BentoTextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Track oils, seasonings, canned goods, and grains with expiration dates to get super-accurate recipe suggestions and zero waste.",
            style = MaterialTheme.typography.bodyMedium,
            color = BentoTextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BentoGreenPrimary,
                contentColor = Color.White
            )
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add First Pantry Item", fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatExpiration(expirationDateMillis: Long?): String {
    if (expirationDateMillis == null) return "No expiration set"
    val now = System.currentTimeMillis()
    val diffDays = (expirationDateMillis - now) / (1000 * 60 * 60 * 24)

    return when {
        diffDays < 0 -> "Expired ${-diffDays}d ago"
        diffDays == 0L -> "Expires today!"
        diffDays == 1L -> "Expires tomorrow"
        diffDays in 2..7 -> "Expires in $diffDays days"
        diffDays in 8..30 -> "Expires in ${(diffDays / 7)} weeks"
        else -> {
            val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            "Exp: ${formatter.format(Date(expirationDateMillis))}"
        }
    }
}
