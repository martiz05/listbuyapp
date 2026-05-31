package com.martiz05.buyapp

import android.content.Context
import com.martiz05.buyapp.data.AuthService
import com.martiz05.buyapp.data.ShoppingListRepository
import com.martiz05.buyapp.data.local.getDatabaseBuilder
import com.martiz05.buyapp.data.local.buildDatabase
import com.martiz05.buyapp.data.remote.BuyAppApi
import com.martiz05.buyapp.data.remote.SessionStore

class AndroidBuyAppContainer(
    context: Context,
    apiBaseUrl: String,
) {
    private val database = buildDatabase(getDatabaseBuilder(context))
    private val api = BuyAppApi(apiBaseUrl, SessionStore())

    val controller = BuyAppController(
        authService = AuthService(api),
        shoppingListRepository = ShoppingListRepository(
            dao = database.shoppingListDao(),
            api = api,
        ),
    )
}
