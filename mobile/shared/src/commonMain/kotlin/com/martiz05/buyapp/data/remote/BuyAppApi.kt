package com.martiz05.buyapp.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class BuyAppApi(
    private val baseUrl: String,
    private val sessionStore: SessionStore,
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val refreshClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        install(Auth) {
            bearer {
                cacheTokens = false
                loadTokens {
                    sessionStore.getBearerTokens()
                }
                refreshTokens {
                    val refreshToken = oldTokens?.refreshToken ?: return@refreshTokens null
                    val response = refreshClient.post("$baseUrl/api/v1/auth/refresh") {
                        contentType(ContentType.Application.Json)
                        setBody(RefreshRequest(refreshToken))
                    }
                    if (!response.status.isSuccess()) {
                        sessionStore.clear()
                        return@refreshTokens null
                    }

                    val tokens = response.body<TokenResponse>()
                    sessionStore.save(tokens)
                    sessionStore.getBearerTokens()
                }
                sendWithoutRequest {
                    true
                }
            }
        }
    }

    suspend fun register(email: String, password: String) {
        val response = client.post("$baseUrl/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(CredentialsRequest(email, password))
        }
        response.requireSuccess()
    }

    suspend fun login(email: String, password: String) {
        val response = client.post(
            "$baseUrl/api/v1/auth/login?useCookies=false&useSessionCookies=false",
        ) {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }
        response.requireSuccess()
        sessionStore.save(response.body())
    }

    suspend fun getShoppingLists(): List<ShoppingListRemote> {
        val summariesResponse = client.get("$baseUrl/api/v1/shopping-lists/")
        summariesResponse.requireSuccess()

        return summariesResponse.body<List<ShoppingListSummaryRemote>>().map { summary ->
            val response = client.get("$baseUrl/api/v1/shopping-lists/${summary.id}")
            response.requireSuccess()
            response.body()
        }
    }

    suspend fun createShoppingList(request: CreateShoppingListRequest) {
        val response = client.post("$baseUrl/api/v1/shopping-lists/") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        response.requireSuccess()
    }

    suspend fun addShoppingListItem(
        shoppingListId: String,
        request: AddShoppingListItemRequest,
    ) {
        val response = client.post("$baseUrl/api/v1/shopping-lists/$shoppingListId/items") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        response.requireSuccess()
    }

    suspend fun setShoppingListItemStatus(
        shoppingListId: String,
        itemId: String,
        request: SetShoppingListItemStatusRequest,
    ) {
        val response = client.patch(
            "$baseUrl/api/v1/shopping-lists/$shoppingListId/items/$itemId/status",
        ) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        response.requireSuccess()
    }

    private fun HttpStatusCode.isSuccess(): Boolean {
        return value in 200..299
    }

    private fun io.ktor.client.statement.HttpResponse.requireSuccess() {
        if (!status.isSuccess()) {
            throw RemoteApiException(status.value)
        }
    }
}

class RemoteApiException(statusCode: Int) : Exception("Remote API request failed with status $statusCode.")
