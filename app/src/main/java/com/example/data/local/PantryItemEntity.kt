package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.PantryItem

@Entity(tableName = "pantry_items")
data class PantryItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val quantity: String,
    val category: String,
    val expirationDateMillis: Long?,
    val addedAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomain(): PantryItem {
        return PantryItem(
            id = id,
            name = name,
            quantity = quantity,
            category = category,
            expirationDateMillis = expirationDateMillis,
            addedAtMillis = addedAtMillis
        )
    }

    companion object {
        fun fromDomain(item: PantryItem): PantryItemEntity {
            return PantryItemEntity(
                id = item.id,
                name = item.name,
                quantity = item.quantity,
                category = item.category,
                expirationDateMillis = item.expirationDateMillis,
                addedAtMillis = item.addedAtMillis
            )
        }
    }
}
