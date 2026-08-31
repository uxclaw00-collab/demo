package com.example.model

data class SubstitutionOption(
    val substituteName: String,
    val ratio: String,
    val note: String,
    val dietaryTag: String? = null
)

data class IngredientSubstitution(
    val originalIngredient: String,
    val category: String,
    val defaultSubstitutes: List<SubstitutionOption>
)

data class PantryItem(
    val id: Long = 0,
    val name: String,
    val quantity: String = "1 item",
    val category: String = "Pantry Staples",
    val expirationDateMillis: Long? = null,
    val addedAtMillis: Long = System.currentTimeMillis()
) {
    val isExpired: Boolean
        get() = expirationDateMillis?.let { it < System.currentTimeMillis() } ?: false

    val isExpiringSoon: Boolean
        get() = expirationDateMillis?.let {
            val now = System.currentTimeMillis()
            val diffDays = (it - now) / (1000 * 60 * 60 * 24)
            diffDays in 0..3
        } ?: false

    val daysUntilExpiration: Long?
        get() = expirationDateMillis?.let {
            val now = System.currentTimeMillis()
            val diffDays = (it - now) / (1000 * 60 * 60 * 24)
            diffDays
        }
}
