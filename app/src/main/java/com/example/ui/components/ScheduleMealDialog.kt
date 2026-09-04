package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DayInfo
import com.example.model.MealSlot
import com.example.model.PlannedMeal
import com.example.model.Recipe
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoCardBg
import com.example.ui.theme.BentoGreenPrimary
import com.example.ui.theme.BentoSurface
import com.example.ui.theme.BentoSurfaceVariant
import com.example.ui.theme.BentoTextMuted
import com.example.ui.theme.BentoTextPrimary
import com.example.ui.theme.BentoTextSecondary
import com.example.ui.theme.BentoTileApricot
import com.example.ui.theme.BentoTileSage
import com.example.ui.theme.GoldenHoney
import com.example.ui.theme.WarmSpice
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ScheduleMealDialog(
    recipe: Recipe,
    initialDateString: String? = null,
    initialSlot: String = "Dinner",
    onDismiss: () -> Unit,
    onConfirmSchedule: (recipe: Recipe, dayName: String, dateString: String, slot: String, servings: Int) -> Unit
) {
    // 14 days calendar window from current week
    val currentMonday = remember { PlannedMeal.getMondayOfWeek() }
    var weekOffset by remember { mutableIntStateOf(0) }
    val displayedMonday = remember(weekOffset) {
        PlannedMeal.shiftWeek(currentMonday, weekOffset)
    }
    val weekDays = remember(displayedMonday) {
        PlannedMeal.getWeekDays(displayedMonday)
    }

    val todayDateStr = remember { PlannedMeal.getTodayDateString() }
    var selectedDay by remember(initialDateString, weekDays) {
        val target = initialDateString ?: todayDateStr
        mutableStateOf(weekDays.firstOrNull { it.dateString == target } ?: weekDays.firstOrNull { it.isToday } ?: weekDays.first())
    }

    var selectedSlot by remember(initialSlot) { mutableStateOf(initialSlot) }
    var selectedServings by remember { mutableIntStateOf(recipe.servings.coerceAtLeast(1)) }

    val slots = remember {
        listOf(
            MealSlot.BREAKFAST.displayName to "🍳",
            MealSlot.LUNCH.displayName to "🥗",
            MealSlot.DINNER.displayName to "🍲",
            MealSlot.SNACK.displayName to "🍎"
        )
    }

    val totalCal = recipe.calories * selectedServings
    val totalProtein = recipe.proteinGrams * selectedServings

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("schedule_meal_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = BentoTileSage,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = BentoGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Schedule Recipe",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                        Text(
                            text = "Add to your weekly calendar plan",
                            style = MaterialTheme.typography.labelSmall,
                            color = BentoTextMuted
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = BentoTextMuted
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Recipe Info Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BentoSurfaceVariant,
                    border = BorderStroke(1.dp, BentoBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
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
                                text = "${recipe.cuisine} • ${recipe.totalTimeMinutes} min",
                                style = MaterialTheme.typography.labelSmall,
                                color = BentoTextMuted
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BentoTileApricot
                        ) {
                            Text(
                                text = "$totalCal kcal",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = WarmSpice,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Calendar Week Navigator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1. Select Calendar Date",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { weekOffset-- },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Week", modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = if (weekOffset == 0) "This Week" else if (weekOffset == 1) "Next Week" else "${PlannedMeal.formatWeekRange(displayedMonday)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = BentoGreenPrimary
                        )
                        IconButton(
                            onClick = { weekOffset++ },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Week", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // 7-day Horizontal Calendar Strip
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(weekDays, key = { it.dateString }) { day ->
                        val isSelected = selectedDay.dateString == day.dateString
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) BentoGreenPrimary else if (day.isToday) BentoTileSage else BentoSurface,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) BentoGreenPrimary else if (day.isToday) BentoGreenPrimary.copy(alpha = 0.5f) else BentoBorder
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedDay = day }
                                .testTag("schedule_day_btn_${day.dayShort}")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = day.dayShort.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else BentoTextMuted
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = day.dayNumber,
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) Color.White else if (day.isToday) BentoGreenPrimary else BentoTextPrimary
                                )
                            }
                        }
                    }
                }

                // Meal Slot Selector
                Text(
                    text = "2. Select Meal Slot",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    slots.forEach { (slotName, emoji) ->
                        val isSel = selectedSlot.equals(slotName, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) BentoGreenPrimary else BentoSurfaceVariant,
                            border = BorderStroke(1.dp, if (isSel) BentoGreenPrimary else BentoBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedSlot = slotName }
                                .testTag("schedule_slot_btn_$slotName")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = slotName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else BentoTextPrimary
                                )
                            }
                        }
                    }
                }

                // Servings Adjustment Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "3. Planned Servings",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                        Text(
                            text = "$totalCal kcal • ${totalProtein}g protein total",
                            style = MaterialTheme.typography.labelSmall,
                            color = BentoTextMuted
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BentoSurfaceVariant,
                            border = BorderStroke(1.dp, BentoBorder),
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { if (selectedServings > 1) selectedServings-- }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("-", fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                            }
                        }

                        Text(
                            text = "$selectedServings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = BentoGreenPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        Surface(
                            shape = CircleShape,
                            color = BentoSurfaceVariant,
                            border = BorderStroke(1.dp, BentoBorder),
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { if (selectedServings < 12) selectedServings++ }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("+", fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmSchedule(recipe, selectedDay.dayName, selectedDay.dateString, selectedSlot, selectedServings)
                    onDismiss()
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BentoGreenPrimary),
                modifier = Modifier.testTag("confirm_schedule_recipe_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Schedule for ${selectedDay.dayShort} $selectedSlot", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = BentoTextSecondary)
            }
        }
    )
}
