package com.martiz05.buyapp.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [
        ShoppingListEntity::class,
        ShoppingListItemEntity::class,
        PendingSyncOperationEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(BuyAppDatabaseConstructor::class)
abstract class BuyAppDatabase : RoomDatabase() {
    abstract fun shoppingListDao(): ShoppingListDao
}

@Suppress("KotlinNoActualForExpect")
expect object BuyAppDatabaseConstructor : RoomDatabaseConstructor<BuyAppDatabase> {
    override fun initialize(): BuyAppDatabase
}

fun buildDatabase(builder: RoomDatabase.Builder<BuyAppDatabase>): BuyAppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
