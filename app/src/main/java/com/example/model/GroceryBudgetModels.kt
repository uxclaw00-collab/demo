package com.example.model

enum class BudgetStatus(val label: String, val emoji: String) {
    WITHIN_BUDGET("On Track", "✅"),
    APPROACHING_BUDGET("Near Limit", "⚠️"),
    OVER_BUDGET("Over Budget", "🚨")
}

data class CategorySpend(
    val category: String,
    val totalCost: Double,
    val itemCount: Int,
    val percentageOfTotal: Float
)

data class BudgetSavingsTip(
    val title: String,
    val description: String,
    val potentialSavings: String,
    val iconEmoji: String
)

data class GroceryBudgetSummary(
    val weeklyBudget: Double,
    val totalEstimatedCost: Double,
    val purchasedCost: Double,
    val pendingCost: Double,
    val remainingBudget: Double,
    val percentUsed: Float,
    val status: BudgetStatus,
    val categorySpends: List<CategorySpend>,
    val averageItemCost: Double,
    val tips: List<BudgetSavingsTip>
)
