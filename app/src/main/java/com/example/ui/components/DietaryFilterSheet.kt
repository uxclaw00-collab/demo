package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoCardBg
import com.example.ui.theme.BentoGreenPrimary
import com.example.ui.theme.BentoSurfaceVariant
import com.example.ui.theme.BentoTextMuted
import com.example.ui.theme.BentoTextPrimary
import com.example.ui.theme.BentoTextSecondary
import com.example.ui.theme.BentoTileSage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietaryFilterSheet(
    isOpen: Boolean,
    selectedFilters: Set<DietaryFilter>,
    selectedMealType: MealType = MealType.ALL,
    selectedPrepTime: PrepTimeFilter = PrepTimeFilter.ANY,
    selectedCuisine: CuisineFilter = CuisineFilter.ALL,
    onToggleFilter: (DietaryFilter) -> Unit,
    onSelectMealType: (MealType) -> Unit = {},
    onSelectPrepTime: (PrepTimeFilter) -> Unit = {},
    onSelectCuisine: (CuisineFilter) -> Unit = {},
    onClearFilters: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val hasActiveFilters = selectedFilters.isNotEmpty() ||
        selectedMealType != MealType.ALL ||
        selectedPrepTime != PrepTimeFilter.ANY ||
        selectedCuisine != CuisineFilter.ALL

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("dietary_filter_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Sheet Header
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
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = BentoGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "RECIPE DISCOVERY FILTERS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.4.sp,
                                fontSize = 9.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = BentoTextMuted
                        )
                        Text(
                            text = "Customize Recommendations",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = BentoTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scrollable Content
            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Meal Type (Breakfast / Lunch / Dinner / Snack)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = BentoGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Meal Type",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextPrimary
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MealType.values().forEach { meal ->
                                val isSelected = selectedMealType == meal
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) BentoGreenPrimary else BentoSurfaceVariant,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) BentoGreenPrimary else BentoBorder
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onSelectMealType(meal) }
                                        .testTag("filter_meal_${meal.name.lowercase()}")
                                ) {
                                    Text(
                                        text = meal.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else BentoTextPrimary,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 2: Max Prep Time
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = BentoGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Maximum Prep Time",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextPrimary
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PrepTimeFilter.values().forEach { prep ->
                                val isSelected = selectedPrepTime == prep
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) BentoGreenPrimary else BentoSurfaceVariant,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) BentoGreenPrimary else BentoBorder
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onSelectPrepTime(prep) }
                                        .testTag("filter_prep_${prep.name.lowercase()}")
                                ) {
                                    Text(
                                        text = prep.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else BentoTextPrimary,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 3: Cuisine Style
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = BentoGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Cuisine Style",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextPrimary
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CuisineFilter.values().forEach { cuisine ->
                                val isSelected = selectedCuisine == cuisine
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) BentoGreenPrimary else BentoSurfaceVariant,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) BentoGreenPrimary else BentoBorder
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onSelectCuisine(cuisine) }
                                        .testTag("filter_cuisine_${cuisine.name.lowercase()}")
                                ) {
                                    Text(
                                        text = "${cuisine.flagEmoji} ${cuisine.displayName}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else BentoTextPrimary,
                                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 4: Dietary & Nutritional Preferences
                item {
                    Text(
                        text = "Dietary Preferences",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                }

                items(DietaryFilter.values()) { filter ->
                    val isSelected = selectedFilters.contains(filter)
                    DietaryFilterRow(
                        filter = filter,
                        isSelected = isSelected,
                        onToggle = { onToggleFilter(filter) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (hasActiveFilters) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = BentoSurfaceVariant,
                        border = BorderStroke(1.dp, BentoBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(onClick = onClearFilters)
                            .testTag("reset_all_filters_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                tint = BentoTextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Reset",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextPrimary
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        onApply()
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(if (hasActiveFilters) 2f else 1f)
                        .height(48.dp)
                        .testTag("apply_dietary_filters_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BentoGreenPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Apply Filters",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun DietaryFilterRow(
    filter: DietaryFilter,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) BentoTileSage else BentoSurfaceVariant,
        border = if (isSelected) {
            BorderStroke(1.5.dp, BentoGreenPrimary)
        } else {
            BorderStroke(1.dp, BentoBorder)
        },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onToggle)
            .testTag("dietary_filter_item_${filter.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = filter.iconEmoji,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = filter.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                    Text(
                        text = filter.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoTextSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(24.dp)
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
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
