package com.martiz05.buyapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Transaction
    @Query("SELECT * FROM shopping_lists WHERE archivedAtUtc IS NULL ORDER BY createdAtUtc DESC")
    fun observeActiveLists(): Flow<List<ShoppingListWithItems>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertList(shoppingList: ShoppingListEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItems(items: List<ShoppingListItemEntity>)

    @Insert
    suspend fun enqueue(operation: PendingSyncOperationEntity)

    @Query("SELECT * FROM pending_sync_operations ORDER BY id")
    suspend fun getPendingOperations(): List<PendingSyncOperationEntity>

    @Query("SELECT COUNT(*) FROM shopping_list_items WHERE shoppingListId = :shoppingListId")
    suspend fun countItems(shoppingListId: String): Int

    @Query("DELETE FROM pending_sync_operations WHERE id = :operationId")
    suspend fun deletePendingOperation(operationId: Long)

    @Query("UPDATE shopping_list_items SET status = :status WHERE id = :itemId")
    suspend fun updateItemStatus(itemId: String, status: String)

    @Transaction
    suspend fun saveListAndEnqueue(
        shoppingList: ShoppingListEntity,
        operation: PendingSyncOperationEntity,
    ) {
        upsertList(shoppingList)
        enqueue(operation)
    }

    @Transaction
    suspend fun saveItemAndEnqueue(
        item: ShoppingListItemEntity,
        operation: PendingSyncOperationEntity,
    ) {
        upsertItems(listOf(item))
        enqueue(operation)
    }

    @Transaction
    suspend fun updateItemStatusAndEnqueue(
        itemId: String,
        status: String,
        operation: PendingSyncOperationEntity,
    ) {
        updateItemStatus(itemId, status)
        enqueue(operation)
    }

    @Transaction
    suspend fun mergeRemoteList(
        shoppingList: ShoppingListEntity,
        items: List<ShoppingListItemEntity>,
    ) {
        upsertList(shoppingList)
        upsertItems(items)
    }
}
