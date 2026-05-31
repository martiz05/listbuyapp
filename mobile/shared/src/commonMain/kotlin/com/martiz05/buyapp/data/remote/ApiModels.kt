package com.martiz05.buyapp.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class CredentialsRequest(
    val email: String,
    val password: String,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val twoFactorCode: String? = null,
    val twoFactorRecoveryCode: String? = null,
)

@Serializable
data class RefreshRequest(
    val refreshToken: String,
)

@Serializable
data class TokenResponse(
    val tokenType: String,
    val accessToken: String,
    val expiresIn: Long,
    val refreshToken: String,
)

@Serializable
data class ShoppingListRemote(
    val id: String,
    val name: String,
    val createdAtUtc: String,
    val archivedAtUtc: String?,
    val items: List<ShoppingListItemRemote>,
)

@Serializable
data class ShoppingListSummaryRemote(
    val id: String,
    val name: String,
    val createdAtUtc: String,
    val itemCount: Int,
    val selectedItemCount: Int,
)

@Serializable
data class ShoppingListItemRemote(
    val id: String,
    val name: String,
    val quantity: Double,
    val unitOfMeasure: String,
    val status: String,
    val position: Int,
)

@Serializable
data class CreateShoppingListRequest(
    val id: String,
    val name: String,
)

@Serializable
data class AddShoppingListItemRequest(
    val id: String,
    val name: String,
    val quantity: Double,
    val unitOfMeasure: String,
)

@Serializable
data class SetShoppingListItemStatusRequest(
    val status: String,
)
