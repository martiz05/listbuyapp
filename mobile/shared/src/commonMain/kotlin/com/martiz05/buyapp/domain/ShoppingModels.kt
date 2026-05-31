package com.martiz05.buyapp.domain

data class ShoppingListModel(
    val id: String,
    val name: String,
    val items: List<ShoppingListItemModel>,
)

data class ShoppingListItemModel(
    val id: String,
    val shoppingListId: String,
    val name: String,
    val quantity: String,
    val unitOfMeasure: String,
    val status: String,
    val position: Int,
)
