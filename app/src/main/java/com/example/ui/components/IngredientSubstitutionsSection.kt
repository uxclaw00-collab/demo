package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.model.Recipe
import com.example.model.SubstitutionOption
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
import com.example.ui.theme.WarmSpice
import com.example.util.SubstitutionProvider

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IngredientSubstitutionsSection(
    recipe: Recipe,
    onAddSubstituteToShoppingList: (substituteName: String, originalName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var lookupQuery by remember { mutableStateOf("") }
    val appliedSwaps = remember { mutableStateMapOf<String, String>() }

    val recipeSubstitutions = remember(recipe) {
        SubstitutionProvider.findSubstitutionsForRecipe(recipe)
    }

    val customLookupResult = remember(lookupQuery) {
        if (lookupQuery.isNotBlank()) {
            SubstitutionProvider.findSubstitutionsForIngredient(lookupQuery)
        } else null
    }

    val totalSubstitutionsCount = recipeSubstitutions.size

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = BentoTileSage.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, BentoGreenPrimary.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("ingredient_substitutions_section_${recipe.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with toggle expand
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isExpanded = !isExpanded }
                    .testTag("toggle_substitutions_button_${recipe.id}"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = BentoGreenPrimary,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Ingredient Substitutions",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                        Text(
                            text = if (totalSubstitutionsCount > 0) {
                                "$totalSubstitutionsCount ingredients have smart swaps (e.g. olive oil, yogurt)"
                            } else {
                                "Tap to search alternatives for any ingredient"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = BentoTextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (totalSubstitutionsCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = BentoGreenPrimary
                        ) {
                            Text(
                                text = "$totalSubstitutionsCount SWAPS",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = BentoGreenPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Expanded content with substitution cards & custom lookup
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    // Recipe-specific detected substitutions
                    if (recipeSubstitutions.isNotEmpty()) {
                        recipeSubstitutions.forEach { (originalIng, options) ->
                            val currentSwap = appliedSwaps[originalIng]

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, BentoBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    // Original Ingredient Header
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "Swap for:",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = BentoTextMuted
                                            )
                                            Text(
                                                text = originalIng,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = BentoTextPrimary
                                            )
                                        }

                                        if (currentSwap != null) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = BentoTileSage
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = BentoGreenPrimary,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = "Using $currentSwap",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                        fontWeight = FontWeight.Bold,
                                                        color = BentoGreenPrimary
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // List of Substitution Options for this ingredient
                                    options.forEach { option ->
                                        val isApplied = currentSwap == option.substituteName

                                        SubstitutionOptionCard(
                                            option = option,
                                            isApplied = isApplied,
                                            onToggleApplied = {
                                                if (isApplied) {
                                                    appliedSwaps.remove(originalIng)
                                                } else {
                                                    appliedSwaps[originalIng] = option.substituteName
                                                }
                                            },
                                            onAddToList = {
                                                onAddSubstituteToShoppingList(option.substituteName, originalIng)
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Custom Ingredient Search / Finder
                    Text(
                        text = "LOOKUP OTHER INGREDIENTS:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                            fontSize = 9.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = BentoTextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = lookupQuery,
                        onValueChange = { lookupQuery = it },
                        placeholder = { Text("Search swap (e.g. Buttermilk, Flour, Eggs)...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = BentoTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        trailingIcon = {
                            if (lookupQuery.isNotBlank()) {
                                IconButton(onClick = { lookupQuery = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = BentoTextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("substitution_lookup_input")
                    )

                    if (customLookupResult != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, BentoBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Alternatives for ${customLookupResult.originalIngredient}:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoGreenPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                customLookupResult.defaultSubstitutes.forEach { option ->
                                    SubstitutionOptionCard(
                                        option = option,
                                        isApplied = false,
                                        onToggleApplied = {},
                                        onAddToList = {
                                            onAddSubstituteToShoppingList(option.substituteName, customLookupResult.originalIngredient)
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
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
private fun SubstitutionOptionCard(
    option: SubstitutionOption,
    isApplied: Boolean,
    onToggleApplied: () -> Unit,
    onAddToList: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isApplied) BentoTileSage else BentoSurfaceVariant,
        border = BorderStroke(1.dp, if (isApplied) BentoGreenPrimary else BentoBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onToggleApplied)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = option.substituteName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )

                    if (option.dietaryTag != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isApplied) BentoGreenPrimary else BentoTileApricot
                        ) {
                            Text(
                                text = option.dietaryTag,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Bold,
                                color = if (isApplied) Color.White else WarmSpice,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

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
                            tint = BentoGreenPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "+ List",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Bold,
                            color = BentoGreenPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Ratio specification
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Ratio: ",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.Bold,
                    color = BentoTextPrimary
                )
                Text(
                    text = option.ratio,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = BentoGreenPrimary
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Culinary note
            Text(
                text = option.note,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = BentoTextSecondary,
                lineHeight = 14.sp
            )
        }
    }
}
