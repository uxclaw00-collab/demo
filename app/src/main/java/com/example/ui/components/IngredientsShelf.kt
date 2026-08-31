package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DetectedIngredient
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoCardBg
import com.example.ui.theme.BentoGreenPrimary
import com.example.ui.theme.BentoSurfaceVariant
import com.example.ui.theme.BentoTextMuted
import com.example.ui.theme.BentoTextPrimary
import com.example.ui.theme.BentoTextSecondary
import com.example.ui.theme.BentoTileSage
import com.example.ui.theme.FreshSage
import com.example.ui.theme.SageBorder
import com.example.ui.theme.SageLight

@Composable
fun IngredientsShelf(
    ingredients: List<DetectedIngredient>,
    onToggleIngredient: (String) -> Unit,
    onAddIngredient: (String, String) -> Unit,
    onRemoveIngredient: (String) -> Unit,
    onOpenFilterSheet: () -> Unit,
    activeFilterCount: Int,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Produce") }

    val categories = listOf("Produce", "Dairy & Eggs", "Meat & Seafood", "Pantry", "Condiments")

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("ingredients_shelf_section"),
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BentoBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Shelf Header with Filter Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FRIDGE INVENTORY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.4.sp,
                            fontSize = 9.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = BentoTextMuted
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Detected Items",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = BentoTileSage
                        ) {
                            Text(
                                text = "${ingredients.count { it.isSelected }}/${ingredients.size}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = BentoGreenPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Add custom ingredient button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BentoSurfaceVariant,
                    border = BorderStroke(1.dp, BentoBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showAddDialog = true }
                        .testTag("add_custom_ingredient_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Ingredient",
                            tint = BentoGreenPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+ Add Item",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal scrolling chips of detected fridge items
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ingredients.forEach { item ->
                    IngredientChip(
                        ingredient = item,
                        onToggle = { onToggleIngredient(item.name) },
                        onRemove = { onRemoveIngredient(item.name) }
                    )
                }
            }
        }
    }

    // Add Ingredient Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Add Fridge Item",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter an ingredient currently in your fridge:",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        label = { Text("Ingredient name (e.g. Greek Yogurt)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_ingredient_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "CATEGORY:",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontSize = 9.sp),
                        fontWeight = FontWeight.Bold,
                        color = BentoTextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = if (selectedCategory == cat) BentoTileSage else BentoSurfaceVariant,
                                border = if (selectedCategory == cat) BorderStroke(1.dp, BentoGreenPrimary) else BorderStroke(1.dp, BentoBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .clickable { selectedCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedCategory == cat) BentoGreenPrimary else BentoTextPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newItemName.isNotBlank()) {
                            onAddIngredient(newItemName.trim(), selectedCategory)
                            newItemName = ""
                            showAddDialog = false
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BentoGreenPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Add to Fridge", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = BentoTextMuted)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }
}

@Composable
private fun IngredientChip(
    ingredient: DetectedIngredient,
    onToggle: () -> Unit,
    onRemove: () -> Unit
) {
    val isSelected = ingredient.isSelected

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) BentoTileSage else BentoSurfaceVariant,
        border = if (isSelected) {
            BorderStroke(1.5.dp, BentoGreenPrimary)
        } else {
            BorderStroke(1.dp, BentoBorder)
        },
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onToggle)
            .testTag("ingredient_chip_${ingredient.name}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(
                        if (isSelected) BentoGreenPrimary else BentoBorder,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(7.dp))

            Column {
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = BentoTextPrimary
                )
                Text(
                    text = ingredient.freshness,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = if (isSelected) BentoGreenPrimary else BentoTextMuted
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = BentoTextMuted,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
