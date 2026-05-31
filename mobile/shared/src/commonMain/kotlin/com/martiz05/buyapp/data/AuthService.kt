package com.martiz05.buyapp.data

import com.martiz05.buyapp.data.remote.BuyAppApi

class AuthService(
    private val api: BuyAppApi,
) {
    suspend fun login(email: String, password: String) {
        api.login(email.trim(), password)
    }

    suspend fun register(email: String, password: String) {
        val trimmedEmail = email.trim()
        api.register(trimmedEmail, password)
        api.login(trimmedEmail, password)
    }
}
