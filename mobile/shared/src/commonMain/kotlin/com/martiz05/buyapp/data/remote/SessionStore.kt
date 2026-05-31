package com.martiz05.buyapp.data.remote

import io.ktor.client.plugins.auth.providers.BearerTokens

class SessionStore {
    private var tokenResponse: TokenResponse? = null

    fun getBearerTokens(): BearerTokens? {
        val tokens = tokenResponse ?: return null
        return BearerTokens(tokens.accessToken, tokens.refreshToken)
    }

    fun save(tokens: TokenResponse) {
        tokenResponse = tokens
    }

    fun clear() {
        tokenResponse = null
    }
}
