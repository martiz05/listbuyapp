package com.martiz05.buyapp.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<BuyAppDatabase> {
    val appContext = context.applicationContext
    val databaseFile = appContext.getDatabasePath("buyapp.db")

    return Room.databaseBuilder<BuyAppDatabase>(
        context = appContext,
        name = databaseFile.absolutePath,
    )
}
