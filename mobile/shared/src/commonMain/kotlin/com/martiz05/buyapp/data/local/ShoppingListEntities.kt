package com.martiz05.buyapp.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAtUtc: String,
    val archivedAtUtc: String?,
)

@Entity(
    tableName = "shopping_list_items",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingListEntity::class,
            parentColumns = ["id"],
            childColumns = ["shoppingListId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("shoppingListId")],
)
data class ShoppingListItemEntity(
    @PrimaryKey val id: String,
    val shoppingListId: String,
    val name: String,
    val quantity: String,
    val unitOfMeasure: String,
    val status: String,
    val position: Int,
)

@Entity(tableName = "pending_sync_operations")
data class PendingSyncOperationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operationType: String,
    val payload: String,
)

data class ShoppingListWithItems(
    @Embedded val shoppingList: ShoppingListEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "shoppingListId",
    )
    val items: List<ShoppingListItemEntity>,
)
