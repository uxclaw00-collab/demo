package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CookingStep
import com.example.model.Recipe
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
import com.example.ui.theme.BentoTileSage
import com.example.ui.theme.FreshSage
import com.example.ui.theme.SageLight
import com.example.ui.theme.TimerAccent
import com.example.ui.theme.TimerAccentLight
import com.example.ui.theme.WarmSpice
import com.example.ui.theme.WarmSpiceLight

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CookingModeScreen(
    viewModel: CulinaryViewModel,
    modifier: Modifier = Modifier
) {
    val recipe by viewModel.activeCookingRecipe.collectAsState()
    val currentStepIndex by viewModel.currentStepIndex.collectAsState()
    val remainingTimerSeconds by viewModel.stepTimerRemainingSeconds.collectAsState()
    val isTimerRunning by viewModel.isStepTimerRunning.collectAsState()
    val completedSteps by viewModel.completedSteps.collectAsState()
    val isSpeaking by viewModel.ttsManager.isSpeaking.collectAsState()
    val speechRate by viewModel.ttsManager.speechRate.collectAsState()
    val isCompleted by viewModel.isRecipeCompleted.collectAsState()

    val currentRecipe = recipe
    if (currentRecipe == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(BentoBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("No active recipe selected.", color = BentoTextMuted)
        }
        return
    }

    val totalSteps = currentRecipe.steps.size
    val currentStep = currentRecipe.steps.getOrNull(currentStepIndex)
    val progress = (currentStepIndex + 1).toFloat() / totalSteps

    Scaffold(
        containerColor = BentoBackground,
        topBar = {
            Surface(
                color = Color.White,
                border = BorderStroke(1.dp, BentoBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BentoSurfaceVariant,
                            border = BorderStroke(1.dp, BentoBorder),
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .clickable { viewModel.closeCookingMode() }
                                .testTag("exit_cooking_mode_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Exit Cooking Mode",
                                    tint = BentoTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "STEP-BY-STEP COOKING",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.2.sp,
                                    fontSize = 9.sp
                                ),
                                fontWeight = FontWeight.Bold,
                                color = BentoTextMuted
                            )
                            Text(
                                text = currentRecipe.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextPrimary,
                                maxLines = 1
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = BentoTileSage
                    ) {
                        Text(
                            text = "${currentStepIndex + 1}/$totalSteps",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.Bold,
                            color = BentoGreenPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            CookingBottomNavigation(
                currentStepIndex = currentStepIndex,
                totalSteps = totalSteps,
                isStepDone = completedSteps.contains(currentStepIndex),
                onToggleDone = { viewModel.toggleStepCompleted(currentStepIndex) },
                onPrev = { viewModel.prevStep() },
                onNext = { viewModel.nextStep() }
            )
        },
        modifier = modifier.testTag("cooking_mode_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = BentoGreenPrimary,
                trackColor = BentoBorder,
                strokeCap = StrokeCap.Round
            )

            // Step Indicator Bubbles Row
            StepIndicatorRow(
                totalSteps = totalSteps,
                currentIndex = currentStepIndex,
                completedSteps = completedSteps,
                onSelectStep = { viewModel.goToStep(it) }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Read-Aloud Voice Control Bar (Hands-Free Assistant)
            VoiceNarrationControlBar(
                isSpeaking = isSpeaking,
                speechRate = speechRate,
                onToggleVoice = { viewModel.toggleTts() },
                onRepeat = { viewModel.speakCurrentStep() },
                onChangeSpeed = { viewModel.setSpeechRate(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (currentStep != null) {
                // Main Step Instruction Display (Large High-Contrast typography for kitchen view)
                StepInstructionCard(step = currentStep)

                Spacer(modifier = Modifier.height(12.dp))

                // Active Step Countdown Timer (if timed)
                if (currentStep.timerSeconds != null) {
                    StepTimerCard(
                        remainingSeconds = remainingTimerSeconds ?: currentStep.timerSeconds,
                        initialSeconds = currentStep.timerSeconds,
                        isRunning = isTimerRunning,
                        onStart = { viewModel.startStepTimer() },
                        onPause = { viewModel.pauseStepTimer() },
                        onReset = { viewModel.resetStepTimer() }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Ingredients Used in this Step
                if (currentStep.ingredientsUsed.isNotEmpty()) {
                    StepIngredientsUsedCard(ingredients = currentStep.ingredientsUsed)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Chef's Pro Tip Card
                if (currentStep.chefTip != null) {
                    ChefTipCard(tip = currentStep.chefTip)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Recipe Finished Celebration Dialog
    if (isCompleted) {
        AlertDialog(
            onDismissRequest = { viewModel.closeCookingMode() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🎉 Dish Completed!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "You've successfully prepared ${currentRecipe.title}!",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = BentoTextSecondary
                    )

                    // Auto Nutrition Log Confirmation Bento Card
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = BentoTileSage,
                        border = BorderStroke(1.dp, BentoGreenPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = BentoGreenPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Auto-Logged to Daily Nutrition",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoGreenPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "+${currentRecipe.calories} kcal",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = BentoTextPrimary
                                    )
                                    Text(
                                        text = "Energy intake",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = BentoTextMuted
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = BentoSurfaceVariant
                                    ) {
                                        Text(
                                            text = "${currentRecipe.proteinGrams}g P",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoGreenPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = BentoSurfaceVariant
                                    ) {
                                        Text(
                                            text = "${currentRecipe.carbsGrams}g C",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoCoralPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = BentoSurfaceVariant
                                    ) {
                                        Text(
                                            text = "${currentRecipe.fatGrams}g F",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoTextSecondary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = BentoCardBg,
                        border = BorderStroke(1.dp, BentoBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "👨‍🍳 Chef's Plating Note",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BentoGreenPrimary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Serve warm, garnish with fresh herbs, and enjoy your meal!",
                                style = MaterialTheme.typography.bodySmall,
                                color = BentoTextPrimary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.closeCookingMode()
                        viewModel.navigateTo(com.example.ui.AppScreen.NUTRITION_LOG)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BentoGreenPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("celebration_view_nutrition_button")
                ) {
                    Text("View Daily Intake", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.closeCookingMode() },
                    modifier = Modifier.testTag("celebration_finish_button")
                ) {
                    Text("Return to Kitchen", color = BentoTextSecondary)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }
}

@Composable
private fun StepIndicatorRow(
    totalSteps: Int,
    currentIndex: Int,
    completedSteps: Set<Int>,
    onSelectStep: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until totalSteps) {
            val isCurrent = i == currentIndex
            val isDone = completedSteps.contains(i)

            val bgColor = when {
                isCurrent -> BentoGreenPrimary
                isDone -> BentoTileSage
                else -> BentoSurfaceVariant
            }

            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { onSelectStep(i) },
                shape = CircleShape,
                color = bgColor,
                border = if (isCurrent) BorderStroke(2.dp, BentoGreenPrimary) else BorderStroke(1.dp, BentoBorder)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isDone && !isCurrent) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = BentoGreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = "${i + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCurrent) Color.White else BentoTextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceNarrationControlBar(
    isSpeaking: Boolean,
    speechRate: Float,
    onToggleVoice: () -> Unit,
    onRepeat: () -> Unit,
    onChangeSpeed: (Float) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BentoBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Speaking status & Play/Pause
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = if (isSpeaking) BentoCoralPrimary else BentoGreenPrimary,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onToggleVoice)
                        .testTag("tts_play_pause_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.Pause else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isSpeaking) "Pause Voice" else "Read Aloud",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isSpeaking) "SPEAKING STEP..." else "HANDS-FREE NARRATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontSize = 9.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = if (isSpeaking) BentoCoralPrimary else BentoGreenPrimary
                    )
                    Text(
                        text = if (isSpeaking) "Active Voice Guidance" else "Tap to read aloud",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                }
            }

            // Controls: Repeat & Speed
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = CircleShape,
                    color = BentoSurfaceVariant,
                    border = BorderStroke(1.dp, BentoBorder),
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onRepeat)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "Repeat",
                            tint = BentoTextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Speed Chip
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = BentoTileSage,
                    border = BorderStroke(1.dp, BentoGreenPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .clickable {
                            val nextRate = when (speechRate) {
                                0.85f -> 1.0f
                                1.0f -> 1.25f
                                else -> 0.85f
                            }
                            onChangeSpeed(nextRate)
                        }
                ) {
                    Text(
                        text = "${speechRate}x",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Bold,
                        color = BentoGreenPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StepInstructionCard(step: CookingStep) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("step_instruction_card"),
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BentoBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Step Number Badge & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = BentoTileSage
                ) {
                    Text(
                        text = "STEP ${step.stepNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                            fontSize = 10.sp
                        ),
                        color = BentoGreenPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "ACTION GUIDE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.2.sp,
                        fontSize = 9.sp
                    ),
                    color = BentoTextMuted,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = step.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = BentoTextPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Large, crystal clear instruction text designed for kitchen counter viewing
            Text(
                text = step.instruction,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 20.sp,
                    lineHeight = 32.sp
                ),
                fontWeight = FontWeight.Normal,
                color = BentoTextPrimary
            )
        }
    }
}

@Composable
private fun StepTimerCard(
    remainingSeconds: Int,
    initialSeconds: Int,
    isRunning: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit
) {
    val minutes = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, secs)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("step_timer_card"),
        shape = RoundedCornerShape(24.dp),
        color = BentoTileApricot,
        border = BorderStroke(1.dp, BentoCoralPrimary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = BentoCoralPrimary,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Kitchen Timer",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "KITCHEN TIMER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontSize = 9.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = BentoCoralPrimary
                    )
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (isRunning) {
                    Button(
                        onClick = onPause,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoCoralPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause Timer", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pause", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onStart,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoCoralPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("start_step_timer_button")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start Timer", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (remainingSeconds == initialSeconds) "Start" else "Resume", fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, BentoBorder),
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onReset)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Replay, contentDescription = "Reset Timer", tint = BentoCoralPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepIngredientsUsedCard(ingredients: List<String>) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BentoBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "STEP INGREDIENTS",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.2.sp,
                    fontSize = 9.sp
                ),
                fontWeight = FontWeight.Bold,
                color = BentoTextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ingredients.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = BentoTileSage,
                        border = BorderStroke(1.dp, BentoBorder)
                    ) {
                        Text(
                            text = "✓ $item",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = BentoGreenPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChefTipCard(tip: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BentoBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = BentoTileApricot,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Chef Tip",
                        tint = BentoCoralPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "CHEF'S PRO TIP",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.2.sp,
                        fontSize = 9.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = BentoCoralPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = BentoTextPrimary
                )
            }
        }
    }
}

@Composable
private fun CookingBottomNavigation(
    currentStepIndex: Int,
    totalSteps: Int,
    isStepDone: Boolean,
    onToggleDone: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Surface(
        color = Color.White,
        border = BorderStroke(1.dp, BentoBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Step Button
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (currentStepIndex > 0) BentoSurfaceVariant else BentoSurfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, BentoBorder),
                modifier = Modifier
                    .height(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(enabled = currentStepIndex > 0, onClick = onPrev)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Step",
                        tint = if (currentStepIndex > 0) BentoTextPrimary else BentoTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Prev",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (currentStepIndex > 0) BentoTextPrimary else BentoTextMuted
                    )
                }
            }

            // Mark Step Done Toggle
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isStepDone) BentoTileSage else BentoSurfaceVariant,
                border = BorderStroke(1.dp, if (isStepDone) BentoGreenPrimary else BentoBorder),
                modifier = Modifier
                    .height(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onToggleDone)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isStepDone) Icons.Default.CheckCircle else Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isStepDone) BentoGreenPrimary else BentoTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isStepDone) "Done ✓" else "Mark Done",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isStepDone) BentoGreenPrimary else BentoTextPrimary
                    )
                }
            }

            // Next Step / Finish Button
            Button(
                onClick = onNext,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentStepIndex == totalSteps - 1) BentoCoralPrimary else BentoGreenPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .height(46.dp)
                    .testTag("next_cooking_step_button")
            ) {
                Text(
                    text = if (currentStepIndex == totalSteps - 1) "Finish 🎉" else "Next",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Step",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
