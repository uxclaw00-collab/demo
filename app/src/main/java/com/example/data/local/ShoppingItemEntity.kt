package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.ShoppingItem

@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: String = "1 item",
    val category: String = "Pantry",
    val recipeSource: String? = null,
    val isBought: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): ShoppingItem = ShoppingItem(
        id = id,
        name = name,
        amount = amount,
        category = category,
        recipeSource = recipeSource,
        isBought = isBought,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(item: ShoppingItem): ShoppingItemEntity = ShoppingItemEntity(
            id = item.id,
            name = item.name,
            amount = item.amount,
            category = item.category,
            recipeSource = item.recipeSource,
            isBought = item.isBought,
            createdAt = item.createdAt
        )
    }
}
